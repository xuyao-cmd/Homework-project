// pages/signup/signup.js - 活动报名页
const db = wx.cloud.database();
const app = getApp();

Page({
  data: {
    activityId: "",
    activityTitle: "",
    userName: "",
    stuId: "",
    phone: "",
    remark: "",
    submitting: false,
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ 
        activityId: options.id,
        activityTitle: decodeURIComponent(options.title || ''),
      });
    }
  },

  // 输入框绑定
  onNameInput(e) {
    this.setData({ userName: e.detail.value });
  },

  onStuIdInput(e) {
    this.setData({ stuId: e.detail.value });
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  // 表单提交
  formSubmit() {
    const { userName, stuId, phone, activityId } = this.data;

    // 非空校验
    if (!userName || !userName.trim()) {
      wx.showToast({ title: "请输入姓名", icon: "none" });
      return;
    }
    if (!stuId || !stuId.trim()) {
      wx.showToast({ title: "请输入学号", icon: "none" });
      return;
    }
    if (!phone || !phone.trim()) {
      wx.showToast({ title: "请输入联系方式", icon: "none" });
      return;
    }
    // 手机号格式校验
    if (!/^1\d{10}$/.test(phone.trim())) {
      wx.showToast({ title: "请输入正确的手机号", icon: "none" });
      return;
    }

    // 防止重复提交
    if (this.data.submitting) return;
    this.setData({ submitting: true });

    const that = this;
    const openid = app.globalData.openid;

    // 先校验是否重复报名
    db.collection("signup")
      .where({
        activityId: activityId,
        openid: openid,
      })
      .get()
      .then((res) => {
        if (res.data.length > 0) {
          wx.showToast({ title: "您已报名该活动，请勿重复报名", icon: "none" });
          that.setData({ submitting: false });
          return;
        }
        // 进行报名
        that.submitSignup(openid);
      })
      .catch((err) => {
        console.error("校验重复报名失败:", err);
        that.setData({ submitting: false });
        wx.showToast({ title: "网络异常，请重试", icon: "none" });
      });
  },

  // 提交报名数据
  submitSignup(openid) {
    const { activityId, userName, stuId, phone, remark } = this.data;
    const that = this;

    db.collection("signup")
      .add({
        data: {
          activityId: activityId,
          openid: openid,
          userName: userName.trim(),
          stuId: stuId.trim(),
          phone: phone.trim(),
          remark: remark.trim() || "",
          createTime: db.serverDate(),
          status: "active",
        },
      })
      .then(() => {
        // 更新活动报名人数
        that.updateActivityCount();
        wx.showToast({ title: "报名成功！", icon: "success", duration: 1500 });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch((err) => {
        console.error("报名提交失败:", err);
        that.setData({ submitting: false });
        wx.showToast({ title: "报名失败，请重试", icon: "none" });
      });
  },

  // 更新活动报名人数
  updateActivityCount() {
    const { activityId } = this.data;
    // 使用 inc 原子操作增加计数
    db.collection("activity")
      .doc(activityId)
      .update({
        data: {
          currentParticipants: db.command.inc(1),
        },
      })
      .then(() => {
        console.log("报名人数更新成功");
      })
      .catch((err) => {
        console.error("更新报名人数失败:", err);
      });
  },

  // 重置表单
  resetForm() {
    this.setData({
      userName: "",
      stuId: "",
      phone: "",
      remark: "",
    });
  },
});
