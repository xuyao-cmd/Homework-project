// pages/feedback/feedback.js - 意见反馈
const db = wx.cloud.database();
const app = getApp();

Page({
  data: {
    // 反馈类型
    feedbackTypes: ["功能建议", "问题反馈", "活动投诉", "其他意见"],
    typeIndex: 0,
    feedbackType: "功能建议",

    // 表单
    content: "",
    contact: "",

    // 提交状态
    submitting: false,
  },

  onLoad() {
    // 页面加载
  },

  // 类型选择
  onTypeChange(e) {
    const index = parseInt(e.detail.value);
    this.setData({
      typeIndex: index,
      feedbackType: this.data.feedbackTypes[index],
    });
  },

  // 内容输入
  onContentInput(e) {
    this.setData({ content: e.detail.value });
  },

  // 联系方式输入
  onContactInput(e) {
    this.setData({ contact: e.detail.value });
  },

  // 提交反馈
  submitFeedback() {
    const { content, contact, feedbackType, submitting } = this.data;

    if (submitting) return;

    // 校验
    if (!content.trim()) {
      wx.showToast({ title: "请输入反馈内容", icon: "none" });
      return;
    }
    if (content.trim().length < 5) {
      wx.showToast({ title: "内容至少5个字", icon: "none" });
      return;
    }

    this.setData({ submitting: true });

    const openid = app.globalData.openid || "";
    const data = {
      type: feedbackType,
      content: content.trim(),
      contact: contact.trim(),
      openid: openid || null,
      status: "pending",
      createTime: db.serverDate(),
    };

    db.collection("feedback")
      .add({ data })
      .then(() => {
        wx.showToast({ title: "提交成功", icon: "success" });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch((err) => {
        console.error("反馈提交失败:", err);
        this.setData({ submitting: false });
        let msg = "提交失败，请重试";
        if (err && err.errCode === -502005) {
          msg = "数据库异常，请联系管理员";
        } else if (err && err.errMsg && err.errMsg.includes("permission")) {
          msg = "权限不足，请先登录";
        }
        wx.showToast({ title: msg, icon: "none" });
      });
  },
});
