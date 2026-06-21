// pages/index/index.js - 首页活动列表
const db = wx.cloud.database();
const _ = db.command;
const app = getApp();

Page({
  data: {
    activityList: [],
    loading: true,
    hasMore: true,
    noData: false,
    pageSize: 10,
    currentPage: 0,
    // 自定义导航栏高度
    navBarHeight: 0,
    statusBarHeight: 0,
    navContentHeight: 44,

    // 轮播图（动态：最热门的3个活动）
    swiperList: [],

    // 搜索
    searchKeyword: "",
    isFilterMode: false,

    // 分类
    categories: ["全部", "志愿服务", "学术讲座", "文体活动", "社团活动"],
    currentCategory: "全部",

    // 热门活动
    hotActivities: [],
    showHotSection: true,
  },

  onLoad() {
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
    this.loadActivities();
    this.loadHotActivities();
  },

  onShow() {
    if (this.data.activityList.length > 0) {
      this.refreshActivities();
      this.loadHotActivities();
    }
  },

  onPullDownRefresh() {
    this.refreshActivities();
    this.loadHotActivities();
  },

  // 根据分类匹配轮播图图片
  getBannerByCategory(category) {
    const map = {
      "志愿服务": "/images/banner_volunteer_clean.png",
      "学术讲座": "/images/banner_study_lecture.png",
      "文体活动": "/images/banner_sport_basketball.png",
      "社团活动": "/images/banner_club_anime.png",
    };
    return map[category] || "/images/banner_campus_common.png";
  },

  // 根据标题直接匹配图片（比分类更精准）
  getBannerByTitle(title) {
    const t = (title || "").toLowerCase();
    if (/篮球|足球|排球|羽毛球|乒乓球|体育|运动|比赛/.test(t)) return "/images/banner_sport_basketball.png";
    if (/动漫|社团|招新|纳新|cos|手办|二次元/.test(t)) return "/images/banner_club_anime.png";
    if (/讲座|学术|考研|科研|论坛|报告/.test(t)) return "/images/banner_study_lecture.png";
    if (/志愿|清扫|环保|公益|服务|献血/.test(t)) return "/images/banner_volunteer_clean.png";
    return null;
  },

  // 加载热门活动（按真实报名人数降序取前3）并同步更新轮播图
  loadHotActivities() {
    const that = this;
    db.collection("activity")
      .where({ status: _.neq("offline") })
      .limit(20)
      .get()
      .then((res) => {
        let list = res.data.map((item) => {
          item.displayTime = item.time || "时间待定";
          const status = item.status || "upcoming";
          item.statusText = that.getStatusText(status);
          item.statusClass = that.getStatusClass(status);
          // 无分类字段则智能推断
          if (!item.category) {
            item.category = that.inferCategory(item.title + item.details);
          }
          // 优先使用用户上传的 bannerUrl，没有再用标题/分类匹配默认图
          item.bannerImage = item.bannerUrl || that.getBannerByTitle(item.title) || that.getBannerByCategory(item.category);
          return item;
        });

        // 并行统计每个活动的真实报名人数（排除已取消）
        const countPromises = list.map((item) =>
          db.collection("signup")
            .where({ activityId: item._id, status: _.neq("cancelled") })
            .count()
            .then((r) => ({ id: item._id, count: r.total }))
            .catch(() => ({ id: item._id, count: 0 }))
        );

        return Promise.all(countPromises).then((counts) => {
          const countMap = {};
          counts.forEach((c) => (countMap[c.id] = c.count));
          list.forEach((item) => {
            item.signupCount = countMap[item._id] || 0;
          });
          // 按真实报名人数降序排列，取前3
          list.sort((a, b) => b.signupCount - a.signupCount);
          const top3 = list.slice(0, 3);

          // 动态生成轮播图数据
          let swiperList = top3.map((item) => ({
            id: item._id,
            activityId: item._id,
            image: item.bannerUrl || that.getBannerByTitle(item.title) || that.getBannerByCategory(item.category),
            title: `${item.title} · ${item.signupCount}人已报名`,
          }));
          // 若热门不足3条，补充默认轮播图
          const defaults = [
            { id: "default1", image: "/images/banner_campus_common.png", title: "校园活动精彩纷呈" },
            { id: "default2", image: "/images/banner_study_lecture.png", title: "学术讲座启迪未来" },
            { id: "default3", image: "/images/banner_sport_basketball.png", title: "文体竞技活力四射" },
          ];
          while (swiperList.length < 3) {
            swiperList.push(defaults[swiperList.length % defaults.length]);
          }
          that.setData({ hotActivities: top3, swiperList });
        });
      })
      .catch((err) => {
        console.error("加载热门活动失败:", err);
      });
  },

  // 加载活动列表
  loadActivities() {
    const that = this;
    this.setData({ loading: true });

    const isCategoryFilter = this.data.currentCategory !== "全部";

    let query = db.collection("activity").where({
      status: db.command.neq("offline"),
    });

    query
      .orderBy("_id", "desc")
      .skip(this.data.currentPage * this.data.pageSize)
      .limit(isCategoryFilter ? 100 : this.data.pageSize)
      .get()
      .then((res) => {
        let list = that.processActivityList(res.data);

        // 分类模式：前端过滤旧数据（无 category 字段的按标题推断）
        if (isCategoryFilter) {
          list = list.filter((item) => item.inferredCategory === that.data.currentCategory);
        }

        const newList = that.data.currentPage === 0 ? list : that.data.activityList.concat(list);
        that.setData({
          activityList: newList,
          loading: false,
          noData: newList.length === 0,
          hasMore: !isCategoryFilter && list.length >= that.data.pageSize,
        });
        wx.stopPullDownRefresh();
      })
      .catch((err) => {
        console.error("加载活动失败:", err);
        that.setData({ loading: false, noData: true });
        wx.stopPullDownRefresh();
        wx.showToast({ title: "加载失败，请重试", icon: "none" });
      });
  },

  // 根据标题/描述智能推断分类
  inferCategory(text) {
    const t = (text || "").toLowerCase();
    if (/志愿|服务|清扫|环保|公益|义工|献血|支教|社区|捡垃圾/.test(t)) return "志愿服务";
    if (/学术|讲座|论坛|报告|科研|研讨|创新|科技/.test(t)) return "学术讲座";
    if (/社团|协会|学生会|纳新|招新|俱乐部/.test(t)) return "社团活动";
    if (/体育|文艺|篮球|足球|歌唱|舞蹈|比赛|运动会|排球|羽毛球|乒乓球|绘画|书法|摄影/.test(t)) return "文体活动";
    return "其他";
  },

  // 处理活动数据（统一格式化）
  processActivityList(rawList) {
    const that = this;
    return rawList.map((item) => {
      item.displayTime = item.time || "时间待定";
      const status = item.status || "upcoming";
      item.statusText = that.getStatusText(status);
      item.statusClass = that.getStatusClass(status);
      item.statusIcon = that.getStatusIcon(status);
      item.shortDetails = item.details
        ? item.details.length > 40 ? item.details.substring(0, 40) + "..." : item.details
        : "暂无介绍";
      // 兼容无分类字段的旧数据：智能推断分类
      if (item.category) {
        item.inferredCategory = item.category;
        item.categoryText = item.category;
      } else {
        item.inferredCategory = that.inferCategory(item.title + item.details);
        item.categoryText = item.inferredCategory;
      }
      // 优先使用用户上传的 bannerUrl，没有再用标题/分类匹配默认图
      item.bannerImage = item.bannerUrl || that.getBannerByTitle(item.title) || that.getBannerByCategory(item.inferredCategory);
      return item;
    });
  },

  // 刷新活动列表
  refreshActivities() {
    this.setData({ currentPage: 0, activityList: [] });
    this.loadActivities();
  },

  // 加载更多
  loadMore() {
    if (!this.data.hasMore || this.data.loading || this.data.isFilterMode) return;
    this.setData({ currentPage: this.data.currentPage + 1 });
    this.loadActivities();
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  // 执行搜索
  onSearchConfirm() {
    const keyword = this.data.searchKeyword.trim();
    if (!keyword) {
      this.setData({ isFilterMode: false, currentPage: 0, showHotSection: true });
      this.refreshActivities();
      return;
    }
    this.setData({ isFilterMode: true, loading: true, showHotSection: false, currentPage: 0 });
    const that = this;
    db.collection("activity")
      .where({
        title: db.RegExp({ regexp: keyword, options: "i" }),
        status: db.command.neq("offline"),
      })
      .orderBy("_id", "desc")
      .get()
      .then((res) => {
        const list = that.processActivityList(res.data);
        that.setData({
          activityList: list,
          loading: false,
          noData: list.length === 0,
          hasMore: false,
        });
      })
      .catch((err) => {
        console.error("搜索失败:", err);
        that.setData({ loading: false });
        wx.showToast({ title: "搜索失败", icon: "none" });
      });
  },

  // 清空搜索
  onClearSearch() {
    this.setData({ searchKeyword: "", isFilterMode: false, showHotSection: true, currentPage: 0 });
    this.refreshActivities();
  },

  // 分类切换
  onCategoryTap(e) {
    const category = e.currentTarget.dataset.category;
    this.setData({ currentCategory: category, currentPage: 0 });
    if (category === "全部") {
      this.setData({ isFilterMode: false, showHotSection: true });
    } else {
      this.setData({ isFilterMode: true, showHotSection: false });
    }
    this.refreshActivities();
  },

  // 跳转到活动详情页
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${id}`,
    });
  },

  // 获取状态文字
  getStatusText(status) {
    const map = { upcoming: "即将开始", ongoing: "报名中", ended: "已结束" };
    return map[status] || "未知";
  },

  // 获取状态样式类
  getStatusClass(status) {
    const map = { upcoming: "status-upcoming", ongoing: "status-ongoing", ended: "status-ended" };
    return map[status] || "";
  },

  // 获取状态对应图标
  getStatusIcon(status) {
    const map = {
      upcoming: "/images/icon_status_soon.png.png",
      ongoing: "/images/icon_status_signing.png.png",
      ended: "/images/icon_status_end.png.png",
    };
    return map[status] || "";
  },

  // 格式化日期
  formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hour = String(d.getHours()).padStart(2, "0");
    const min = String(d.getMinutes()).padStart(2, "0");
    return `${year}-${month}-${day} ${hour}:${min}`;
  },

  // 上拉触底加载更多
  onReachBottom() {
    this.loadMore();
  },
});
