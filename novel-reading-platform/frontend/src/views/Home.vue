<template>
  <div class="page-wrap">
    <el-card class="hero-card">
      <el-row :gutter="30" type="flex" align="middle">
        <el-col :span="15">
          <h1>发现好故事，记录你的阅读时光</h1>
          <p>首页展示全站热度榜和分类热度榜，完整书库可以进入全部图书按一级、二级分类检索。</p>
          <el-button type="primary" icon="el-icon-search" @click="$router.push('/books')">查看全部图书</el-button>
        </el-col>
        <el-col :span="9">
          <div class="stat-panel">
            <div><b>{{ novels.length }}</b><span>本小说</span></div>
            <div><b>{{ totalClicks }}</b><span>阅读热度</span></div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="rank-layout" v-loading="loading">
      <el-col :span="15">
        <el-card class="rank-card">
          <div slot="header" class="card-header">
            <b>全站热度前20</b>
            <span>按点击热度排序</span>
          </div>
          <el-table :data="hotRank" stripe class="hot-table" @row-click="row => goDetail(row.id)">
            <el-table-column label="排名" width="74">
              <template slot-scope="scope">
                <span class="rank-no" :class="{ top: scope.$index < 3 }">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="书名" min-width="220">
              <template slot-scope="scope">
                <div class="book-title">{{ cleanTitle(scope.row.title) }}</div>
                <div class="book-sub">{{ scope.row.category }} / {{ scope.row.subCategory }}</div>
              </template>
            </el-table-column>
            <el-table-column label="作者" width="110">
              <template slot-scope="scope">{{ displayAuthorName(scope.row.authorName, scope.row.title) }}</template>
            </el-table-column>
            <el-table-column prop="score" label="评分" width="80"></el-table-column>
            <el-table-column prop="clickCount" label="热度" width="100"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-carousel class="promo-carousel" height="210px" indicator-position="outside">
          <el-carousel-item v-for="item in carouselItems" :key="item.title">
            <div class="promo-slide" :class="item.type">
              <span>{{ item.tag }}</span>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
              <el-button size="mini" type="primary" @click.stop="$router.push(item.path)">{{ item.action }}</el-button>
            </div>
          </el-carousel-item>
        </el-carousel>

        <el-card class="category-rank">
          <div slot="header" class="card-header">
            <b>分类榜单</b>
            <span>各分类前10</span>
          </div>
          <el-tabs v-model="activeCategory" stretch>
            <el-tab-pane v-for="group in categoryRanks" :key="group.category" :label="group.category" :name="group.category">
              <div v-for="(item, index) in group.books" :key="item.id" class="rank-item" @click="goDetail(item.id)">
                <span class="mini-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
                <div class="rank-info">
                  <b>{{ cleanTitle(item.title) }}</b>
                  <span>{{ item.subCategory }} · {{ item.clickCount }}</span>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listNovels } from '../api/novel'
import { categoryOptions } from '../utils/categories'
import { cleanTitle, displayAuthorName } from '../utils/format'

export default {
  name: 'Home',
  data() {
    return {
      loading: false,
      novels: [],
      activeCategory: '玄幻',
      carouselItems: [
        { tag: '热门推荐', title: '追踪本周最热小说', desc: '根据阅读热度自动生成榜单，快速发现正在升温的作品。', action: '查看榜单', path: '/home', type: 'blue' },
        { tag: '作者计划', title: '欢迎投稿原创故事', desc: '普通用户投稿后进入审核，管理员通过后展示给全部读者。', action: '去投稿', path: '/publish', type: 'green' },
        { tag: '完整书库', title: '按题材筛选 1000 本书', desc: '一级分类与二级题材横向切换，查找小说更直接。', action: '全部图书', path: '/books', type: 'dark' }
      ]
    }
  },
  computed: {
    totalClicks() {
      return this.novels.reduce((sum, item) => sum + item.clickCount, 0)
    },
    hotRank() {
      return this.novels.slice(0, 20)
    },
    categoryRanks() {
      return categoryOptions.map(option => ({
        category: option.value,
        books: this.novels.filter(item => item.category === option.value).slice(0, 10)
      }))
    }
  },
  created() {
    this.loadNovels()
  },
  methods: {
    cleanTitle,
    displayAuthorName,
    async loadNovels() {
      this.loading = true
      try {
        const res = await listNovels()
        this.novels = res.data || []
      } catch (e) {
        this.novels = []
      } finally {
        this.loading = false
      }
    },
    goDetail(id) {
      this.$router.push(`/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.hero-card,
.rank-card,
.category-rank {
  border-radius: 12px;
}
.hero-card {
  margin-bottom: 24px;
  overflow: hidden;
}
.hero-card h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
.hero-card p {
  color: #606266;
  line-height: 1.8;
}
.stat-panel {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.stat-panel div {
  background: linear-gradient(135deg, #ecf5ff 0%, #f8fbff 100%);
  border-radius: 10px;
  padding: 30px 12px;
  text-align: center;
  border: 1px solid #e4efff;
}
.stat-panel b {
  display: block;
  color: #409eff;
  font-size: 32px;
}
.stat-panel span,
.card-header span,
.rank-info span,
.book-sub {
  color: #606266;
  font-size: 13px;
}
.rank-layout {
  align-items: stretch;
}
.promo-carousel {
  margin-bottom: 18px;
}
.promo-slide {
  height: 100%;
  box-sizing: border-box;
  padding: 28px 30px;
  border-radius: 12px;
  color: #fff;
}
.promo-slide.blue {
  background: linear-gradient(135deg, #2f80ed 0%, #56ccf2 100%);
}
.promo-slide.green {
  background: linear-gradient(135deg, #16a085 0%, #67c23a 100%);
}
.promo-slide.dark {
  background: linear-gradient(135deg, #303133 0%, #606266 100%);
}
.promo-slide span {
  font-size: 13px;
  opacity: 0.9;
}
.promo-slide h3 {
  margin: 18px 0 8px;
  font-size: 24px;
}
.promo-slide p {
  margin: 0 0 18px;
  line-height: 1.7;
  opacity: 0.92;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hot-table {
  cursor: pointer;
}
.book-title {
  font-weight: 700;
  color: #303133;
  line-height: 24px;
}
.book-sub {
  line-height: 20px;
}
.rank-no,
.mini-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #f0f2f5;
  color: #606266;
  font-weight: 700;
}
.rank-no.top,
.mini-rank.top {
  background: #2f80ed;
  color: #fff;
}
.rank-item {
  display: flex;
  gap: 10px;
  padding: 13px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}
.rank-item:hover .rank-info b {
  color: #2f80ed;
}
.rank-item:last-child {
  border-bottom: 0;
}
.rank-info {
  min-width: 0;
}
.rank-info b {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
