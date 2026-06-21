// pages/admin/admin.js - 管理员后台
const db = wx.cloud.database();
const _ = db.command;
const app = getApp();

Page({
  data: {
    // 登录状态
    isLoggedIn: false,
    adminPassword: "",
    showLoginError: false,

    // 角色权限：是否为真正的管理员（admin集合中的用户）
    isAdminUser: false,

    // 活动管理
    activityList: [],
    loading: true,
    noData: false,

    // 弹窗
    showModal: false,
    modalTitle: "",
    isEditing: false,
    editId: "",

    // 表单数据
    formTitle: "",
    formDescription: "",
    formLocation: "",
    formStartTime: "",
    formEndTime: "",
    formOrganizer: "",
    formMaxParticipants: "",
    formStatus: "upcoming",
    formCategory: "志愿服务",
    categoryOptions: ["志愿服务", "学术讲座", "文体活动", "社团活动", "其他"],

    // 意见反馈
    feedbackUnread: 0,

    // 报名名单弹窗
    showSignupModal: false,
    signupList: [],
    signupLoading: false,

    // 提交状态
    submitting: false,

    // 自定义导航栏
    navBarHeight: 0,
    statusBarHeight: 0,
    navContentHeight: 44,
  },

  onLoad() {
    // 计算自定义导航栏高度
    const sysInfo = wx.getSystemInfoSync();
    const statusBarHeight = sysInfo.statusBarHeight;
    const menuButton = wx.getMenuButtonBoundingClientRect();
    const navContentHeight = (menuButton.top - statusBarHeight) * 2 + menuButton.height;
    this.setData({
      statusBarHeight: statusBarHeight,
      navContentHeight: navContentHeight,
      navBarHeight: statusBarHeight + navContentHeight,
      pagePaddingTop: statusBarHeight + navContentHeight + 56,
    });

    // 检查是否已保存登录状态
    const savedLogin = wx.getStorageSync("adminLoggedIn");
    if (savedLogin) {
      this.setData({ isLoggedIn: true, isAdminUser: true });
      this.updateTabBar(true);
      this.loadActivities();
    }
  },

  onShow() {
    if (this.data.isLoggedIn) {
      this.loadActivities();
      if (this.data.isAdminUser) {
        this.loadFeedbackUnread();
      }
    }
  },

  // 检查当前用户是否为真正管理员（admin 集合中）
  checkAdminRole() {
    // 优先使用全局缓存的 isAdmin 标记
    if (app.globalData.isAdmin) {
      this.setData({ isAdminUser: true });
      this.updateTabBar(true);
      return;
    }

    // 如果 openid 已获取但 isAdmin 未标记，直接查询数据库
    const openid = app.globalData.openid;
    if (!openid) {
      // openid 尚未获取，等待后重试
      setTimeout(() => this.checkAdminRole(), 800);
      return;
    }

    db.collection("admin").where({ openid: openid }).get().then((res) => {
      if (res.data.length > 0) {
        app.globalData.isAdmin = true;
        this.setData({ isAdminUser: true });
        this.updateTabBar(true);
      } else {
        this.setData({ isAdminUser: false });
        this.updateTabBar(false);
      }
    }).catch(() => {
      this.setData({ isAdminUser: false });
      this.updateTabBar(false);
    });
  },

  // 根据权限更新底部 Tab 样式：管理员高亮，普通学生置灰
  updateTabBar(isAdmin) {
    if (isAdmin) {
      wx.setTabBarItem({
        index: 2,
        text: "管理",
        iconPath: "/images/管理员.png",
        selectedIconPath: "/images/管理员 (1).png",
      });
    } else {
      wx.setTabBarItem({
        index: 2,
        text: "管理",
        iconPath: "/images/管理员.png",
        selectedIconPath: "/images/管理员.png",
      });
    }
  },

  // 管理密码输入
  onPasswordInput(e) {
    this.setData({ adminPassword: e.detail.value, showLoginError: false });
  },

  // 管理员登录
  onLogin() {
    // 简单的密码验证（演示用，实际项目中应使用更安全的验证方式）
    const ADMIN_PASSWORD = "admin123";

    if (this.data.adminPassword === ADMIN_PASSWORD) {
      this.setData({ isLoggedIn: true, isAdminUser: true, showLoginError: false });
      wx.setStorageSync("adminLoggedIn", true);
      this.updateTabBar(true);
      this.loadActivities();
      this.loadFeedbackUnread();
    } else {
      this.setData({ showLoginError: true });
      wx.showToast({ title: "密码错误", icon: "none" });
    }
  },

  // 加载活动列表（管理员显示全部含已下架，普通学生过滤已下架）
  loadActivities() {
    const that = this;
    const isAdmin = this.data.isAdminUser || app.globalData.isAdmin;
    this.setData({ loading: true });

    db.collection("activity")
      .orderBy("_id", "desc")
      .get()
      .then((res) => {
        // 非管理员过滤掉已下架的活动
        let rawList = res.data;
        if (!isAdmin) {
          rawList = rawList.filter((item) => item.status !== "offline");
        }

        const validStatuses = ["upcoming", "ongoing", "ended", "offline"];
        const list = rawList.map((item) => {
          item.displayTime = item.time || "时间待定";
          // 优先使用数据库中的有效状态，否则自动推断
          const status = validStatuses.includes(item.status)
            ? item.status
            : that.inferStatus(item);
          item.statusText = that.getStatusText(status);
          item.statusClass = that.getStatusClass(status);
          item.statusIcon = that.getStatusIcon(status);
          item.participants = 0; // 占位，后面从 signup 集合统计
          item.maxP = item.maxParticipants || 0;
          return item;
        });

        // 并行查询每个活动的真实报名人数
        const countPromises = list.map((item) =>
          db.collection("signup")
            .where({
              activityId: item._id,
              status: _.neq("cancelled"),
            })
            .count()
            .then((r) => ({ id: item._id, count: r.total }))
            .catch(() => ({ id: item._id, count: 0 }))
        );

        return Promise.all(countPromises).then((counts) => {
          const countMap = {};
          counts.forEach((c) => (countMap[c.id] = c.count));
          list.forEach((item) => {
            item.participants = countMap[item._id] || 0;
          });
          that.setData({
            activityList: list,
            loading: false,
            noData: list.length === 0,
          });
        });
      })
      .catch((err) => {
        console.error("加载活动失败:", err);
        that.setData({ loading: false, noData: true });
      });
  },

  // 根据活动时间自动推断状态
  inferStatus(item) {
    const timeStr = item.time || "";
    const now = new Date();
    // 尝试解析 "2026-07-01 09:00至2026-07-01 12:00" 格式
    const parts = timeStr.split("至");
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
    const singleDate = new Date(timeStr.trim());
    if (!isNaN(singleDate)) {
      return now < singleDate ? "upcoming" : "ended";
    }
    return "upcoming";
  },

  // 跳转到发布活动页面
  goToPublish() {
    wx.navigateTo({ url: "/pages/publish/publish" });
  },

  // 打开发布活动弹窗（编辑用）
  openAddModal() {
    this.setData({
      showModal: true,
      modalTitle: "发布新活动",
      isEditing: false,
      editId: "",
      formTitle: "",
      formDescription: "",
      formLocation: "",
      formStartTime: "",
      formEndTime: "",
      formOrganizer: "",
      formMaxParticipants: "",
      formStatus: "upcoming",
      formCategory: "志愿服务",
    });
  },

  // 打开编辑活动弹窗
  openEditModal(e) {
    const id = e.currentTarget.dataset.id;
    const activity = this.data.activityList.find((a) => a._id === id);
    if (!activity) return;

    // 从 time 字段解析 startTime 和 endTime（如 "2026-07-01 09:00至2026-07-01 12:00"）
    let startTime = "";
    let endTime = "";
    if (activity.time) {
      const parts = activity.time.split("至");
      if (parts.length >= 1) startTime = parts[0].trim().substring(0, 10);
      if (parts.length >= 2) endTime = parts[1].trim().substring(0, 10);
    }

    this.setData({
      showModal: true,
      modalTitle: "编辑活动",
      isEditing: true,
      editId: id,
      formTitle: activity.title || "",
      formDescription: activity.details || "",
      formLocation: activity.address || "",
      formStartTime: startTime,
      formEndTime: endTime,
      formOrganizer: activity.organizer || "",
      formMaxParticipants: String(activity.maxParticipants || ""),
      formStatus: activity.status || "upcoming",
      formCategory: activity.category || "志愿服务",
    });
  },

  // 关闭弹窗
  closeModal() {
    this.setData({ showModal: false });
  },

  // 表单输入绑定
  onFieldChange(e) {
    const field = e.currentTarget.dataset.field;
    let value = e.detail.value;

    // 处理状态选择器（返回索引）
    if (field === "formStatus") {
      const statusMap = ["upcoming", "ongoing", "ended"];
      value = statusMap[value] || "upcoming";
    }

    this.setData({ [field]: value });
  },

  // 提交活动（新增/编辑）
  submitActivity() {
    const {
      formTitle, formDescription, formLocation, formStartTime,
      formEndTime, formOrganizer, formMaxParticipants, formStatus,
      formCategory, isEditing, editId, submitting,
    } = this.data;

    // 防重复提交
    if (submitting) return;

    // 表单校验
    if (!formTitle.trim()) {
      wx.showToast({ title: "请输入活动标题", icon: "none" });
      return;
    }
    if (!formStartTime) {
      wx.showToast({ title: "请选择开始时间", icon: "none" });
      return;
    }
    if (!formLocation.trim()) {
      wx.showToast({ title: "请输入活动地点", icon: "none" });
      return;
    }

    this.setData({ submitting: true });

    // 拼接 time 字段
    const timeStr = formEndTime
      ? `${formStartTime} 至 ${formEndTime}`
      : formStartTime;

    const data = {
      title: formTitle.trim(),
      details: formDescription.trim(),
      address: formLocation.trim(),
      time: timeStr,
      organizer: formOrganizer.trim(),
      maxParticipants: Number(formMaxParticipants) || 0,
      status: formStatus,
      category: formCategory,
      updateTime: db.serverDate(),
    };

    const that = this;

    if (isEditing) {
      // 编辑活动
      db.collection("activity")
        .doc(editId)
        .update({ data })
        .then(() => {
          wx.showToast({ title: "更新成功", icon: "success" });
          that.setData({ showModal: false, submitting: false });
          that.loadActivities();
        })
        .catch((err) => {
          console.error("更新失败:", err);
          that.setData({ submitting: false });
          wx.showToast({ title: "更新失败", icon: "none" });
        });
    } else {
      // 新增活动
      data.createTime = db.serverDate();
      db.collection("activity")
        .add({ data })
        .then(() => {
          wx.showToast({ title: "发布成功", icon: "success" });
          that.setData({ showModal: false, submitting: false });
          that.loadActivities();
        })
        .catch((err) => {
          console.error("发布失败:", err);
          that.setData({ submitting: false });
          wx.showToast({ title: "发布失败", icon: "none" });
        });
    }
  },

  // 删除/下架活动
  deleteActivity(e) {
    const id = e.currentTarget.dataset.id;
    const title = e.currentTarget.dataset.title;

    wx.showModal({
      title: "确认删除",
      content: `确定要删除活动「${title}」吗？删除后不可恢复。`,
      confirmColor: "#E54545",
      success: (res) => {
        if (res.confirm) {
          this.doDeleteActivity(id);
        }
      },
    });
  },

  doDeleteActivity(id) {
    const that = this;
    db.collection("activity")
      .doc(id)
      .remove()
      .then(() => {
        wx.showToast({ title: "已删除", icon: "success" });
        that.loadActivities();
      })
      .catch((err) => {
        console.error("删除失败:", err);
        wx.showToast({ title: "操作失败", icon: "none" });
      });
  },

  // 查看报名名单
  viewSignupList(e) {
    const activityId = e.currentTarget.dataset.id;
    const activityTitle = e.currentTarget.dataset.title;

    this.setData({
      showSignupModal: true,
      modalTitle: `报名名单 - ${activityTitle}`,
      signupList: [],
      signupLoading: true,
    });

    const that = this;

    db.collection("signup")
      .where({
        activityId: activityId,
      })
      .orderBy("createTime", "asc")
      .get()
      .then((res) => {
        // 过滤掉已取消的报名（兼容没有 status 的旧数据，默认视为有效）
        const list = res.data
          .filter((item) => item.status !== "cancelled")
          .map((item) => ({
            ...item,
            createTimeDisplay: that.formatDate(item.createTime),
          }));
        that.setData({
          signupList: list,
          signupLoading: false,
        });
      })
      .catch((err) => {
        console.error("加载报名名单失败:", err);
        that.setData({ signupLoading: false });
      });
  },

  // 关闭报名名单弹窗
  closeSignupModal() {
    this.setData({ showSignupModal: false });
  },

  // ===== 意见反馈 =====
  goToFeedbackMgr() {
    wx.navigateTo({
      url: "/pages/admin/feedback",
    });
  },

  loadFeedbackUnread() {
    const that = this;
    db.collection("feedback")
      .where({ status: "pending" })
      .count()
      .then((res) => {
        that.setData({ feedbackUnread: res.total });
      })
      .catch(() => {});
  },

  // 退出管理
  logout() {
    wx.removeStorageSync("adminLoggedIn");
    // 恢复 Tab 为默认样式
    wx.setTabBarItem({
      index: 2,
      text: "管理",
      iconPath: "/images/管理员.png",
      selectedIconPath: "/images/管理员 (1).png",
    });
    this.setData({
      isLoggedIn: false,
      isAdminUser: false,
      adminPassword: "",
      activityList: [],
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

  // 获取状态文字
  getStatusText(status) {
    const map = {
      upcoming: "即将开始",
      ongoing: "报名中",
      ended: "已结束",
      offline: "已下架",
    };
    return map[status] || "未知";
  },

  // 获取状态样式类
  getStatusClass(status) {
    const map = {
      upcoming: "status-upcoming",
      ongoing: "status-ongoing",
      ended: "status-ended",
      offline: "status-offline",
    };
    return map[status] || "";
  },

  // 获取状态对应图标
  getStatusIcon(status) {
    const map = {
      upcoming: "/images/icon_status_soon.png.png",
      ongoing: "/images/icon_status_signing.png.png",
      ended: "/images/icon_status_end.png.png",
      offline: "/images/icon_status_end.png.png",
    };
    return map[status] || "";
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadActivities();
    setTimeout(() => wx.stopPullDownRefresh(), 1000);
  },
});
