<template>
  <div class="page-wrap read-page" v-loading="loading">
    <el-page-header @back="$router.back()" content="章节阅读"></el-page-header>
    <h1 v-if="chapter">{{ chapter.title }}</h1>
    <div class="reader-toolbar">
      <el-button size="mini" @click="fontSize = Math.max(14, fontSize - 1)">A-</el-button>
      <el-button size="mini" @click="fontSize = Math.min(26, fontSize + 1)">A+</el-button>
      <el-button size="mini" type="primary" @click="$router.push(`/detail/${$route.params.novelId}`)">返回目录</el-button>
    </div>
    <div v-if="chapter" class="reader-content" :style="{ fontSize: fontSize + 'px' }">
      {{ chapter.content }}
    </div>
  </div>
</template>

<script>
import { readChapter } from '../api/novel'

export default {
  name: 'Read',
  data() {
    return {
      loading: false,
      chapter: null,
      fontSize: 18
    }
  },
  created() {
    this.loadChapter()
  },
  methods: {
    async loadChapter() {
      this.loading = true
      try {
        const res = await readChapter(this.$route.params.novelId, this.$route.params.chapterId)
        this.chapter = res.data
      } catch (e) {
        this.chapter = null
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.read-page h1 {
  text-align: center;
  margin: 24px 0 12px;
}
.reader-toolbar {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 18px;
}
</style>
