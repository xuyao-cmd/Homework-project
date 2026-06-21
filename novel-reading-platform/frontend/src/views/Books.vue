<template>
  <div class="page-wrap">
    <el-card class="filter-card">
      <div slot="header" class="card-header">
        <b>全部图书</b>
        <span>{{ filteredNovels.length }} 本</span>
      </div>
      <div class="channel-bar">
        <button class="channel-item" :class="{ active: query.category === '全部' }" @click="selectCategory('全部')">全部作品</button>
        <button v-for="item in categoryOptions" :key="item.value" class="channel-item" :class="{ active: query.category === item.value }" @click="selectCategory(item.value)">
          {{ item.label }}
        </button>
      </div>

      <div class="sub-channel-row">
        <span class="row-label">二级分类</span>
        <button class="sub-channel" :class="{ active: query.subCategory === '全部' }" @click="selectSubCategory('全部')">全部题材</button>
        <button v-for="item in subCategoryOptions" :key="item" class="sub-channel" :class="{ active: query.subCategory === item }" @click="selectSubCategory(item)">
          {{ item }}
        </button>
      </div>

      <div class="search-row">
        <span class="row-label">搜索</span>
        <el-input ref="keywordInput" v-model="query.keyword" clearable placeholder="书名、简介或标签" class="keyword-input" @keyup.enter.native="loadNovels"></el-input>
        <el-button type="primary" icon="el-icon-search" :loading="loading" @click="loadNovels">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <div class="result-bar">
      <div>
        <b>{{ currentScope }}</b>
        <span>共找到 {{ filteredNovels.length }} 本作品</span>
      </div>
      <span class="sort-note">按阅读热度排序</span>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="item in pageBooks" :key="item.id">
        <el-card class="book-card card-hover" @click.native="goDetail(item.id)">
          <div class="cover-wrap" :class="coverClass(item.category)">
            <div class="cover-title">{{ cleanTitle(item.title) }}</div>
            <div class="cover-author">{{ displayAuthorName(item.authorName, item.title) }}</div>
            <span class="category-badge">{{ item.category }}</span>
          </div>
          <h3>{{ cleanTitle(item.title) }}</h3>
          <div class="meta-line">作者：{{ displayAuthorName(item.authorName, item.title) }} · {{ item.subCategory }}</div>
          <div class="score-line">
            <span>评分 {{ item.score }}</span>
            <span>热度 {{ item.clickCount }}</span>
          </div>
          <p class="summary two-line">{{ item.summary }}</p>
          <div class="tags">
            <el-tag v-for="tag in item.tags" :key="tag" size="mini" type="info">{{ tag }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && filteredNovels.length === 0" description="没有找到符合条件的作品"></el-empty>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, jumper"
        :current-page.sync="page"
        :page-size="pageSize"
        :total="filteredNovels.length">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { listNovels } from '../api/novel'
import { categoryOptions, getSubCategories } from '../utils/categories'
import { cleanTitle, displayAuthorName } from '../utils/format'

export default {
  name: 'Books',
  data() {
    return {
      loading: false,
      novels: [],
      page: 1,
      pageSize: 24,
      categoryOptions,
      query: {
        keyword: '',
        category: '全部',
        subCategory: '全部'
      }
    }
  },
  computed: {
    subCategoryOptions() {
      if (this.query.category === '全部') {
        return this.categoryOptions.reduce((result, item) => result.concat(item.children), [])
      }
      return getSubCategories(this.query.category)
    },
    filteredNovels() {
      return this.novels
    },
    currentScope() {
      const category = this.query.category === '全部' ? '全部作品' : this.query.category
      const subCategory = this.query.subCategory === '全部' ? '全部题材' : this.query.subCategory
      return `${category} · ${subCategory}`
    },
    pageBooks() {
      const start = (this.page - 1) * this.pageSize
      return this.filteredNovels.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.loadNovels()
  },
  methods: {
    cleanTitle,
    displayAuthorName,
    coverClass(category) {
      return {
        'cover-fantasy': category === '玄幻',
        'cover-history': category === '历史',
        'cover-city': category === '都市',
        'cover-other': category === '其他'
      }
    },
    selectCategory(category) {
      this.query.category = category
      this.query.subCategory = '全部'
      this.loadNovels()
    },
    selectSubCategory(subCategory) {
      this.query.subCategory = subCategory
      this.loadNovels()
    },
    async loadNovels() {
      this.loading = true
      try {
        const params = {
          keyword: this.query.keyword,
          category: this.query.category,
          subCategory: this.query.subCategory
        }
        const res = await listNovels(params)
        this.novels = res.data || []
        this.page = 1
      } catch (e) {
        this.novels = []
      } finally {
        this.loading = false
      }
    },
    resetFilters() {
      this.query.keyword = ''
      this.query.category = '全部'
      this.query.subCategory = '全部'
      this.loadNovels()
    },
    goDetail(id) {
      this.$router.push(`/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 22px;
  border-radius: 12px;
  overflow: hidden;
  border: 0;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-header span {
  color: #606266;
  font-size: 13px;
}
.channel-bar {
  display: flex;
  align-items: center;
  background: #343840;
  margin: -20px -20px 18px;
  padding: 0 24px;
  min-height: 56px;
}
.channel-item {
  height: 56px;
  padding: 0 30px;
  border: 0;
  background: transparent;
  color: #fff;
  font-size: 17px;
  cursor: pointer;
  transition: background 0.18s ease;
}
.channel-item.active,
.channel-item:hover {
  background: #5b8def;
}
.sub-channel-row,
.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.row-label {
  width: 72px;
  color: #606266;
  font-weight: 700;
}
.sub-channel {
  height: 34px;
  padding: 0 16px;
  border: 1px solid #dcdfe6;
  border-radius: 17px;
  background: #fff;
  color: #606266;
  cursor: pointer;
  transition: all 0.18s ease;
}
.sub-channel.active,
.sub-channel:hover {
  border-color: #5b8def;
  background: #eef4ff;
  color: #3f73c8;
}
.keyword-input {
  width: 320px;
}
.result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding: 14px 18px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.05);
}
.result-bar b {
  margin-right: 10px;
  color: #303133;
}
.result-bar span {
  color: #606266;
  font-size: 13px;
}
.sort-note {
  color: #909399;
}
.book-card {
  min-height: 432px;
  margin-bottom: 20px;
  cursor: pointer;
  border-radius: 10px;
  border: 0;
}
.cover-wrap {
  position: relative;
  height: 220px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px;
  box-sizing: border-box;
  color: #fff;
}
.cover-wrap::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1), rgba(0, 0, 0, 0.22));
}
.cover-fantasy {
  background: linear-gradient(135deg, #5b8def 0%, #7aa7d9 100%);
}
.cover-history {
  background: linear-gradient(135deg, #d9a24a 0%, #c58f63 100%);
}
.cover-city {
  background: linear-gradient(135deg, #6bbf8a 0%, #4aa3a2 100%);
}
.cover-other {
  background: linear-gradient(135deg, #8d79c7 0%, #7f8ca3 100%);
}
.cover-title,
.cover-author {
  position: relative;
  z-index: 1;
}
.cover-title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.25;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
}
.cover-author {
  margin-top: 10px;
  font-size: 13px;
  opacity: 0.92;
}
.category-badge {
  position: absolute;
  right: 10px;
  top: 10px;
  padding: 4px 9px;
  border-radius: 14px;
  background: rgba(48, 49, 51, 0.42);
  color: #fff;
  font-size: 12px;
  z-index: 1;
}
.book-card h3 {
  margin: 14px 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.score-line {
  display: flex;
  justify-content: space-between;
  color: #3f73c8;
  font-size: 13px;
  line-height: 26px;
  font-weight: 700;
}
.two-line {
  height: 54px;
  overflow: hidden;
}
.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.pager {
  display: flex;
  justify-content: center;
  padding: 10px 0 26px;
}
</style>
