// pages/detail/detail.js - 活动详情页
const db = wx.cloud.database();
const _ = db.command;
const app = getApp();

Page({
  data: {
    activityId: "",
    activity: {},
    isRegistered: false,
    signupCount: 0,
    loading: true,
    displayTime: "",
    statusText: "",
    statusClass: "",
    endTimeDisplay: "",
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ activityId: options.id });
      this.loadActivityDetail(options.id);
    }
  },

  // 加载活动详情
  loadActivityDetail(id) {
    const that = this;
    wx.showLoading({ title: "加载中..." });

    db.collection("activity")
      .doc(id)
      .get()
      .then((res) => {
        const activity = res.data;
        // 直接显示 time 字段
        activity.displayTime = activity.time || "时间待定";
        // 兼容没有 status 的旧数据
        const status = activity.status || "upcoming";
        activity.statusText = that.getStatusText(status);
        activity.statusClass = `status-tag status-${status}`;
        activity.signupPercent = 0; // 占位，等 getSignupCount 后更新

        that.setData({ activity, loading: false });
        wx.hideLoading();

        // 检查当前用户是否已报名
        that.checkRegistration(id);
        // 统计报名人数（会同步更新进度条）
        that.getSignupCount(id);
      })
      .catch((err) => {
        console.error("加载详情失败:", err);
        wx.hideLoading();
        wx.showToast({ title: "加载失败", icon: "none" });
        that.setData({ loading: false });
      });
  },

  // 检查用户是否已报名
  checkRegistration(activityId) {
    const openid = app.globalData.openid;
    if (!openid) {
      // openid 尚未获取到，延迟重试
      setTimeout(() => this.checkRegistration(activityId), 500);
      return;
    }

    db.collection("signup")
      .where({
        activityId: activityId,
        openid: openid,
      })
      .get()
      .then((res) => {
        this.setData({
          isRegistered: res.data.length > 0,
        });
      })
      .catch((err) => {
        console.error("查询报名状态失败:", err);
      });
  },

  // 统计报名人数（排除已取消），并同步更新进度条
  getSignupCount(activityId) {
    const that = this;
    db.collection("signup")
      .where({
        activityId: activityId,
        status: _.neq("cancelled"),
      })
      .count()
      .then((res) => {
        const count = res.total;
        const activity = that.data.activity;
        let percent = 0;
        if (activity.maxParticipants && activity.maxParticipants > 0) {
          percent = Math.min(
            Math.round((count / activity.maxParticipants) * 100),
            100
          );
        }
        that.setData({
          signupCount: count,
          "activity.signupPercent": percent,
          "activity.currentParticipants": count,
        });
      })
      .catch((err) => {
        console.error("统计报名人数失败:", err);
      });
  },

  // 点击报名按钮
  onSignup() {
    // 检查活动状态（兼容没有 status 的旧数据）
    const status = this.data.activity.status;
    if (status === "ended") {
      wx.showToast({ title: "活动已结束", icon: "none" });
      return;
    }
    if (this.data.isRegistered) {
      wx.showToast({ title: "您已报名该活动", icon: "none" });
      return;
    }
    // 检查报名人数是否已满
    const activity = this.data.activity;
    if (
      activity.maxParticipants &&
      activity.maxParticipants > 0 &&
      this.data.signupCount >= activity.maxParticipants
    ) {
      wx.showToast({ title: "报名人数已满", icon: "none" });
      return;
    }

    wx.navigateTo({
      url: `/pages/signup/signup?id=${this.data.activityId}&title=${encodeURIComponent(this.data.activity.title || '')}`,
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
      ongoing: "进行中",
      ended: "已结束",
    };
    return map[status] || "未知";
  },

  onShareAppMessage() {
    return {
      title: this.data.activity.title || "校园活动",
      path: `/pages/detail/detail?id=${this.data.activityId}`,
    };
  },
});
