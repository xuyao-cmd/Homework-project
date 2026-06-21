// pages/publish/publish.js
const db = wx.cloud.database();

Page({
  data: {
    title: "",
    categoryIndex: -1,
    categoryList: ["志愿服务", "学术讲座", "文体活动", "社团活动"],
    startDate: "",
    endDate: "",
    address: "",
    statusIndex: -1,
    statusList: ["即将开始", "报名中", "已结束"],
    statusMap: ["upcoming", "ongoing", "ended"],
    details: "",
    bannerUrl: "",

    statusBarHeight: 0,
    navBarHeight: 0,
    navContentHeight: 44,
    submitting: false,
  },

  onLoad() {
    const sysInfo = wx.getSystemInfoSync();
    const statusBarHeight = sysInfo.statusBarHeight;
    const menuButton = wx.getMenuButtonBoundingClientRect();
    const navContentHeight = (menuButton.top - statusBarHeight) * 2 + menuButton.height;
    this.setData({
      statusBarHeight,
      navContentHeight,
      navBarHeight: statusBarHeight + navContentHeight,
      pagePaddingTop: statusBarHeight + navContentHeight + 56,
    });
  },

  goBack() {
    wx.navigateBack();
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value });
  },
  onCategoryChange(e) {
    this.setData({ categoryIndex: e.detail.value });
  },
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value });
  },
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value });
  },
  onAddressInput(e) {
    this.setData({ address: e.detail.value });
  },
  onStatusChange(e) {
    this.setData({ statusIndex: e.detail.value });
  },
  onDetailsInput(e) {
    this.setData({ details: e.detail.value });
  },

  uploadBanner() {
    wx.chooseMedia({
      count: 1,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: (res) => {
        const tempFile = res.tempFiles[0].tempFilePath;
        // 显示加载提示
        wx.showLoading({ title: "裁剪中..." });
        this.cropTo16x9(tempFile);
      },
      fail: () => {
        wx.showToast({ title: "取消选择", icon: "none" });
      },
    });
  },

  // 居中裁剪为 16:9 横幅比例
  cropTo16x9(srcPath) {
    const that = this;
    wx.getImageInfo({
      src: srcPath,
      success: (info) => {
        const imgW = info.width;
        const imgH = info.height;
        const targetRatio = 16 / 9;
        const imgRatio = imgW / imgH;

        let sx = 0, sy = 0, sw = imgW, sh = imgH;

        if (imgRatio > targetRatio) {
          // 原图比16:9更宽 → 裁左右两边
          sw = imgH * targetRatio;
          sh = imgH;
          sx = (imgW - sw) / 2;
        } else if (imgRatio < targetRatio) {
          // 原图比16:9更高 → 裁上下两边
          sw = imgW;
          sh = imgW / targetRatio;
          sy = (imgH - sh) / 2;
        }
        // 比例相同则不裁剪

        try {
          const canvas = wx.createOffscreenCanvas({ type: "2d", width: sw, height: sh });
          const ctx = canvas.getContext("2d");
          const img = canvas.createImage();
          img.src = srcPath;
          img.onload = () => {
            ctx.drawImage(img, sx, sy, sw, sh, 0, 0, sw, sh);
            wx.canvasToTempFilePath({
              canvas,
              x: 0, y: 0,
              width: sw, height: sh,
              destWidth: sw, destHeight: sh,
              success: (exportRes) => {
                that.uploadCroppedImage(exportRes.tempFilePath, srcPath);
              },
              fail: () => {
                wx.hideLoading();
                // 裁剪失败则上传原图
                that.uploadCroppedImage(srcPath, srcPath);
              },
            });
          };
          img.onerror = () => {
            wx.hideLoading();
            that.uploadCroppedImage(srcPath, srcPath);
          };
        } catch (e) {
          // createOffscreenCanvas 不可用时走 canvas 组件裁剪
          that.cropViaHiddenCanvas(srcPath, imgW, imgH, sx, sy, sw, sh);
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: "图片加载失败", icon: "none" });
      },
    });
  },

  // 备用方案：通过隐藏的 canvas 组件裁剪
  cropViaHiddenCanvas(srcPath, imgW, imgH, sx, sy, sw, sh) {
    const that = this;
    const query = wx.createSelectorQuery();
    query.select("#cropCanvas")
      .fields({ node: true, size: true })
      .exec((resList) => {
        if (!resList || !resList[0] || !resList[0].node) {
          wx.hideLoading();
          that.uploadCroppedImage(srcPath, srcPath);
          return;
        }
        const canvasNode = resList[0].node;
        canvasNode.width = sw;
        canvasNode.height = sh;
        const ctx = canvasNode.getContext("2d");
        const img = canvasNode.createImage();
        img.src = srcPath;
        img.onload = () => {
          ctx.drawImage(img, sx, sy, sw, sh, 0, 0, sw, sh);
          wx.canvasToTempFilePath({
            canvas: canvasNode,
            x: 0, y: 0,
            width: sw, height: sh,
            destWidth: sw, destHeight: sh,
            success: (exportRes) => {
              that.uploadCroppedImage(exportRes.tempFilePath, srcPath);
            },
            fail: () => {
              wx.hideLoading();
              that.uploadCroppedImage(srcPath, srcPath);
            },
          });
        };
        img.onerror = () => {
          wx.hideLoading();
          that.uploadCroppedImage(srcPath, srcPath);
        };
      });
  },

  // 上传裁剪后的图片到云存储
  uploadCroppedImage(filePath, fallbackPath) {
    const that = this;
    const ext = filePath.match(/\.([^.]+)$/) ? filePath.match(/\.([^.]+)$/)[1] : "jpg";
    const cloudPath = `banner/${Date.now()}_${Math.random().toString(36).slice(2)}.${ext}`;
    wx.cloud.uploadFile({
      cloudPath,
      filePath,
      success: (upRes) => {
        wx.hideLoading();
        that.setData({ bannerUrl: upRes.fileID });
        wx.showToast({ title: "上传成功", icon: "success" });
      },
      fail: (err) => {
        wx.hideLoading();
        console.error("上传失败:", err);
        // 如果是裁剪后的文件上传失败，用原图再试一次
        if (filePath !== fallbackPath) {
          that.uploadCroppedImage(fallbackPath, fallbackPath);
        } else {
          wx.showToast({ title: "上传失败，请重试", icon: "none" });
        }
      },
    });
  },

  submitActivity() {
    const {
      title, categoryIndex, startDate, endDate,
      address, statusIndex, details, bannerUrl, submitting,
    } = this.data;

    if (submitting) return;

    if (!title.trim()) {
      return wx.showToast({ title: "请输入活动标题", icon: "none" });
    }
    if (categoryIndex === -1) {
      return wx.showToast({ title: "请选择活动分类", icon: "none" });
    }
    if (!startDate) {
      return wx.showToast({ title: "请选择开始日期", icon: "none" });
    }
    if (!address.trim()) {
      return wx.showToast({ title: "请输入活动地址", icon: "none" });
    }
    if (statusIndex === -1) {
      return wx.showToast({ title: "请选择活动状态", icon: "none" });
    }

    this.setData({ submitting: true });

    const timeStr = endDate
      ? `${startDate} 09:00至${endDate} 12:00`
      : `${startDate} 09:00`;

    const data = {
      title: title.trim(),
      category: this.data.categoryList[categoryIndex],
      time: timeStr,
      address: address.trim(),
      status: this.data.statusMap[statusIndex],
      details: details.trim(),
      bannerUrl: bannerUrl || "/images/banner_campus_common.png",
      currentParticipants: 0,
      createTime: db.serverDate(),
      updateTime: db.serverDate(),
    };

    db.collection("activity")
      .add({ data })
      .then(() => {
        wx.showToast({ title: "发布成功", icon: "success" });
        setTimeout(() => {
          const pages = getCurrentPages();
          const prevPage = pages[pages.length - 2];
          if (prevPage && prevPage.loadActivities) {
            prevPage.loadActivities();
          }
          wx.navigateBack();
        }, 1200);
      })
      .catch((err) => {
        console.error("发布失败:", err);
        this.setData({ submitting: false });
        wx.showToast({ title: "发布失败", icon: "none" });
      });
  },
});
