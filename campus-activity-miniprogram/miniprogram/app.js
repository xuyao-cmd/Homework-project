// app.js
App({
  onLaunch: function () {
    this.globalData = {
 env: "cloud1-d6giemnj7716f0b28",
      openid: "",
      isAdmin: false,
    };
    if (!wx.cloud) {
      console.error("请使用 2.2.3 或以上的基础库以使用云能力");
    } else {
      wx.cloud.init({
        env: this.globalData.env,
        traceUser: true,
      });
    }
    // 获取用户 openid
    this.getOpenId();
  },

  // 获取用户 openid
  getOpenId: function () {
    const that = this;
    wx.cloud.callFunction({
      name: "quickstartFunctions",
      data: { type: "getOpenId" },
    }).then((res) => {
      that.globalData.openid = res.result.openid;
      console.log("用户 openid:", res.result.openid);
      // 检查是否为管理员
      that.checkAdmin(res.result.openid);
    }).catch((err) => {
      console.error("获取 openid 失败:", err);
    });
  },

  // 检查是否为管理员
  checkAdmin: function (openid) {
    const db = wx.cloud.database();
    db.collection("admin").where({ openid: openid }).get().then((res) => {
      if (res.data.length > 0) {
        this.globalData.isAdmin = true;
      }
    }).catch(() => {});
  },
});
