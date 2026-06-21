// pages/myrecord/myrecord.js - 我的报名记录
const db = wx.cloud.database();
const app = getApp();

Page({
  data: {
    recordList: [],
    loading: true,
    noData: false,
    currentTab: "all", // all / upcoming / ended
  },

  onLoad() {
    this.loadRecords();
  },

  onShow() {
    // 刷新报名记录
    if (this.data.recordList.length > 0) {
      this.loadRecords();
    }
  },

  // 加载报名记录
  loadRecords() {
    const that = this;
    const openid = app.globalData.openid;

    if (!openid) {
      // openid 未获取，延迟重试
      setTimeout(() => this.loadRecords(), 500);
      return;
    }

    this.setData({ loading: true });

    db.collection("signup")
      .where({
        openid: openid,
      })
      .orderBy("createTime", "desc")
      .get()
      .then(async (res) => {
        // 过滤掉已取消的报名记录
        const signups = res.data.filter(s => s.status !== "cancelled");
        if (signups.length === 0) {
          that.setData({ recordList: [], loading: false, noData: true });
          return;
        }

        // 获取关联的活动信息
        const activityIds = [...new Set(signups.map((s) => s.activityId))];
        const activityMap = {};

        // 批量查询活动信息
        const promises = activityIds.map((id) =>
          db.collection("activity").doc(id).get().then((r) => {
            if (r.data) {
              activityMap[id] = r.data;
            }
          }).catch(() => {})
        );

        await Promise.all(promises);

        // 合并数据
        const records = signups.map((item) => {
          const activity = activityMap[item.activityId] || {};
          // 兼容旧数据没有 status 的情况
          const signupStatus = item.status || "active";
          // 根据活动时间推断真实状态，不依赖可能错误的 activity.status 字段
          const activityStatus = that.inferStatusFromTime(activity.time);
          return {
            ...item,
            activityTitle: activity.title || "活动已删除",
            activityTime: activity.time || "时间待定",
            activityLocation: activity.address || "",
            activityStatus: activityStatus,
            createTimeDisplay: that.formatDate(item.createTime),
            statusText: signupStatus === "active" ? "已报名" : "已取消",
          };
        });

        // 按标签筛选
        let filteredList = records;
        if (that.data.currentTab === "upcoming") {
          filteredList = records.filter(
            (r) => r.activityStatus === "upcoming" || r.activityStatus === "ongoing"
          );
        } else if (that.data.currentTab === "ended") {
          filteredList = records.filter(
            (r) => r.activityStatus === "ended"
          );
        }

        that.setData({
          recordList: filteredList,
          allRecords: records,
          loading: false,
          noData: filteredList.length === 0,
        });
      })
      .catch((err) => {
        console.error("加载报名记录失败:", err);
        that.setData({ loading: false, noData: true });
        wx.showToast({ title: "加载失败，请重试", icon: "none" });
      });
  },

  // 切换标签
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ currentTab: tab });

    const allRecords = this.data.allRecords || [];
    let filteredList = allRecords;

    if (tab === "upcoming") {
      filteredList = allRecords.filter(
        (r) => r.activityStatus === "upcoming" || r.activityStatus === "ongoing"
      );
    } else if (tab === "ended") {
      filteredList = allRecords.filter(
        (r) => r.activityStatus === "ended"
      );
    }

    this.setData({
      recordList: filteredList,
      noData: filteredList.length === 0,
    });
  },

  // 根据活动时间推断状态
  inferStatusFromTime(timeStr) {
    const str = timeStr || "";
    const now = new Date();
    // 尝试解析 "2026-07-01 09:00至2026-07-01 12:00" 格式
    const parts = str.split("至");
    if (parts.length >= 2) {
      const start = new Date(parts[0].trim());
      const end = new Date(parts[1].trim());
      if (!isNaN(start) && !isNaN(end)) {
        if (now < start) return "upcoming";
        if (now > end) return "ended";
        return "ongoing";
      }
    }
    // 尝试解析单个日期
    const singleDate = new Date(str.trim());
    if (!isNaN(singleDate)) {
      return now < singleDate ? "upcoming" : "ended";
    }
    return "upcoming";
  },

  // 取消报名
  cancelSignup(e) {
    const id = e.currentTarget.dataset.id;
    const activityId = e.currentTarget.dataset.activityid;

    wx.showModal({
      title: "提示",
      content: "确定要取消报名吗？此操作不可恢复。",
      confirmColor: "#E54545",
      success: (res) => {
        if (res.confirm) {
          this.doCancelSignup(id, activityId);
        }
      },
    });
  },

  // 执行取消报名
  doCancelSignup(signupId, activityId) {
    const that = this;
    wx.showLoading({ title: "取消中..." });

    db.collection("signup")
      .doc(signupId)
      .update({
        data: {
          status: "cancelled",
          cancelTime: db.serverDate(),
        },
      })
      .then(() => {
        // 更新活动报名人数（减少1人）
        return db.collection("activity")
          .doc(activityId)
          .update({
            data: {
              currentParticipants: db.command.inc(-1),
            },
          });
      })
      .then(() => {
        wx.hideLoading();
        wx.showToast({ title: "已取消报名", icon: "success" });
        // 刷新列表
        setTimeout(() => {
          that.loadRecords();
        }, 1000);
      })
      .catch((err) => {
        wx.hideLoading();
        console.error("取消报名失败:", err);
        wx.showToast({ title: "操作失败，请重试", icon: "none" });
      });
  },

  // 跳转到意见反馈
  goToFeedback() {
    wx.navigateTo({
      url: "/pages/feedback/feedback",
    });
  },

  // 格式化日期
  formatDate(dateStr) {
    if (!dateStr) return "暂无";
    const d = new Date(dateStr);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hour = String(d.getHours()).padStart(2, "0");
    const min = String(d.getMinutes()).padStart(2, "0");
    return `${year}-${month}-${day} ${hour}:${min}`;
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadRecords();
    setTimeout(() => wx.stopPullDownRefresh(), 1000);
  },
});
