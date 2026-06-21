<template>
  <div class="page-wrap" v-loading="loading">
    <el-card class="admin-card">
      <div slot="header" class="admin-header">
        <b>平台管理员后台</b>
        <el-tag type="danger" size="mini">admin</el-tag>
      </div>
      <el-row :gutter="16" class="admin-stats">
        <el-col :span="8">
          <div class="stat-box"><b>{{ pendingNovels.length }}</b><span>待审核</span></div>
        </el-col>
        <el-col :span="8">
          <div class="stat-box"><b>{{ approvedCount }}</b><span>本次发布</span></div>
        </el-col>
        <el-col :span="8">
          <div class="stat-box"><b>admin</b><span>唯一管理员</span></div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="review-card">
      <div slot="header"><b>投稿审核</b></div>
      <el-table :data="pendingNovels" stripe>
        <el-table-column label="小说标题" min-width="150">
          <template slot-scope="scope">{{ cleanTitle(scope.row.title) }}</template>
        </el-table-column>
        <el-table-column label="投稿用户" width="120">
          <template slot-scope="scope">{{ displayAuthorName(scope.row.authorName, scope.row.title) }}</template>
        </el-table-column>
        <el-table-column label="分类" width="150">
          <template slot-scope="scope">{{ scope.row.category }} / {{ scope.row.subCategory }}</template>
        </el-table-column>
        <el-table-column prop="wordCount" label="字数" width="100"></el-table-column>
        <el-table-column prop="summary" label="简介" min-width="240" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" :loading="approvingId === scope.row.id" @click="approve(scope.row)">通过并发布</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && pendingNovels.length === 0" description="暂无待审核投稿"></el-empty>
    </el-card>
  </div>
</template>

<script>
import { approveNovel, listPendingNovels } from '../api/novel'
import { cleanTitle, displayAuthorName } from '../utils/format'

export default {
  name: 'Admin',
  data() {
    return {
      loading: false,
      approvingId: null,
      pendingNovels: [],
      approvedCount: 0
    }
  },
  created() {
    this.loadPending()
  },
  methods: {
    cleanTitle,
    displayAuthorName,
    currentAdmin() {
      return JSON.parse(localStorage.getItem('novel-user') || '{}')
    },
    async loadPending() {
      this.loading = true
      try {
        const admin = this.currentAdmin()
        const res = await listPendingNovels(admin.username)
        this.pendingNovels = res.data || []
      } catch (e) {
        this.pendingNovels = []
      } finally {
        this.loading = false
      }
    },
    async approve(row) {
      this.approvingId = row.id
      try {
        const admin = this.currentAdmin()
        await approveNovel(row.id, admin.username)
        this.pendingNovels = this.pendingNovels.filter(item => item.id !== row.id)
        this.approvedCount += 1
        this.$message.success('审核通过，小说已发布')
      } catch (e) {
        // error already shown by axios interceptor
      } finally {
        this.approvingId = null
      }
    }
  }
}
</script>

<style scoped>
.admin-card,
.review-card {
  border-radius: 12px;
}
.admin-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.admin-stats {
  margin-top: 4px;
}
.stat-box {
  background: #fef0f0;
  border-radius: 10px;
  padding: 22px 12px;
  text-align: center;
}
.stat-box b {
  display: block;
  color: #f56c6c;
  font-size: 26px;
}
.stat-box span {
  color: #606266;
  font-size: 13px;
}
.review-card {
  margin-top: 20px;
}
</style>
