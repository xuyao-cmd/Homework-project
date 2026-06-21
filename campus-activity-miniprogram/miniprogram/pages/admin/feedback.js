// pages/admin/feedback.js - 意见反馈管理
const db = wx.cloud.database();

Page({
  data: {
    feedbackList: [],
    loading: true,
    noData: false,
    // 筛选
    currentFilter: "all", // all / pending / resolved
    filteredList: [],
    // 统计
    totalCount: 0,
    pendingCount: 0,
    resolvedCount: 0,
  },

  onLoad() {
    this.loadFeedback();
  },

  onShow() {
    // 每次显示时刷新（从标记已处理后返回）
    this.loadFeedback();
  },

  loadFeedback() {
    const that = this;
    this.setData({ loading: true });

    db.collection("feedback")
      .orderBy("createTime", "desc")
      .limit(100)
      .get()
      .then((res) => {
        const list = res.data.map((item) => ({
          ...item,
          statusText: item.status === "pending" ? "待处理" : "已处理",
          createTimeDisplay: that.formatDate(item.createTime),
        }));

        const pendingCount = list.filter((i) => i.status === "pending").length;
        const resolvedCount = list.filter((i) => i.status === "resolved").length;

        that.setData({
          feedbackList: list,
          totalCount: list.length,
          pendingCount: pendingCount,
          resolvedCount: resolvedCount,
          loading: false,
          noData: list.length === 0,
        });

        // 应用筛选
        that.applyFilter();
      })
      .catch((err) => {
        console.error("加载反馈失败:", err);
        that.setData({ loading: false, noData: true });
        wx.showToast({ title: "加载失败，请重试", icon: "none" });
      });
  },

  // 切换筛选
  switchFilter(e) {
    const filter = e.currentTarget.dataset.filter;
    this.setData({ currentFilter: filter }, () => {
      this.applyFilter();
    });
  },

  // 应用筛选
  applyFilter() {
    const { feedbackList, currentFilter } = this.data;
    let filteredList = feedbackList;

    if (currentFilter === "pending") {
      filteredList = feedbackList.filter((i) => i.status === "pending");
    } else if (currentFilter === "resolved") {
      filteredList = feedbackList.filter((i) => i.status === "resolved");
    }

    this.setData({ filteredList });
  },

  // 标记已处理
  resolveFeedback(e) {
    const id = e.currentTarget.dataset.id;
    const that = this;

    wx.showModal({
      title: "确认处理",
      content: "确定将该反馈标记为已处理吗？",
      confirmColor: "#4096FF",
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: "处理中..." });
          db.collection("feedback")
            .doc(id)
            .update({
              data: {
                status: "resolved",
                resolveTime: db.serverDate(),
              },
            })
            .then(() => {
              wx.hideLoading();
              wx.showToast({ title: "已标记处理", icon: "success" });
              that.loadFeedback();
            })
            .catch((err) => {
              wx.hideLoading();
              console.error("标记失败:", err);
              wx.showToast({ title: "操作失败，请重试", icon: "none" });
            });
        }
      },
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
    this.loadFeedback();
    setTimeout(() => wx.stopPullDownRefresh(), 1000);
  },
});
