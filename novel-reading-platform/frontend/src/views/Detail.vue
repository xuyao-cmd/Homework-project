<template>
  <div class="page-wrap" v-loading="loading">
    <el-card v-if="novel" class="detail-card">
      <el-row :gutter="28">
        <el-col :span="5">
          <img :src="novel.cover" class="book-cover detail-cover" />
        </el-col>
        <el-col :span="19">
          <h1>{{ cleanTitle(novel.title) }}</h1>
          <div class="meta-line">作者：{{ displayAuthorName(novel.authorName, novel.title) }} ｜ 分类：{{ novel.category }} / {{ novel.subCategory }} ｜ 状态：{{ novel.status }}</div>
          <div class="score-row">
            <el-rate v-model="novel.score" disabled show-score text-color="#ff9900" score-template="{value}"></el-rate>
            <span>字数：{{ novel.wordCount }} ｜ 点击：{{ novel.clickCount }}</span>
          </div>
          <p class="summary">{{ novel.summary }}</p>
          <div class="tags">
            <el-tag v-for="tag in novel.tags" :key="tag">{{ tag }}</el-tag>
          </div>
          <el-button type="primary" icon="el-icon-reading" :disabled="chapters.length === 0" @click="startRead">开始阅读</el-button>
          <el-button :type="inShelf ? 'success' : 'default'" :icon="inShelf ? 'el-icon-star-on' : 'el-icon-star-off'" @click="addShelf">
            {{ inShelf ? '已在书架' : '加入书架' }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="chapter-card">
      <div slot="header">
        <b>章节目录</b>
      </div>
      <el-table :data="chapters" stripe>
        <el-table-column prop="chapterNo" label="序号" width="80"></el-table-column>
        <el-table-column prop="title" label="章节标题"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="150"></el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" @click="readChapter(scope.row)">阅读</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getNovel, getChapters } from '../api/novel'
import { cleanTitle, displayAuthorName } from '../utils/format'
import { addToShelf, isInShelf, isLoggedIn } from '../utils/shelf'

export default {
  name: 'Detail',
  data() {
    return {
      loading: false,
      novel: null,
      chapters: [],
      inShelf: false
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    cleanTitle,
    displayAuthorName,
    async loadData() {
      this.loading = true
      try {
        const id = this.$route.params.id
        const detailRes = await getNovel(id)
        const chaptersRes = await getChapters(id)
        this.novel = detailRes.data
        this.chapters = chaptersRes.data || []
        this.inShelf = isInShelf(this.novel.id)
      } catch (e) {
        this.novel = null
        this.chapters = []
      } finally {
        this.loading = false
      }
    },
    startRead() {
      if (!this.requireLogin()) return
      const first = this.chapters[0]
      if (first) {
        this.$router.push(`/read/${first.novelId}/${first.id}`)
      }
    },
    readChapter(chapter) {
      if (!this.requireLogin()) return
      this.$router.push(`/read/${chapter.novelId}/${chapter.id}`)
    },
    addShelf() {
      if (!this.requireLogin()) return
      addToShelf(this.novel)
      this.inShelf = true
      this.$message.success('已加入书架')
    },
    requireLogin() {
      if (isLoggedIn()) {
        return true
      }
      this.$message.warning('请先登录后继续操作')
      this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
      return false
    }
  }
}
</script>

<style scoped>
.detail-card {
  margin-bottom: 20px;
  border-radius: 12px;
}
.detail-cover {
  height: 300px;
}
.score-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 18px 0;
  color: #909399;
}
.tags {
  display: flex;
  gap: 8px;
  margin: 18px 0;
}
.chapter-card {
  border-radius: 12px;
}
</style>
