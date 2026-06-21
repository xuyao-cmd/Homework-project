<template>
  <div id="app">
    <el-container>
      <el-header class="site-header">
        <div class="brand" @click="go('/home')">
          <i class="el-icon-reading"></i>
          <span>云章小说阅读平台</span>
        </div>
        <el-menu class="nav" mode="horizontal" :router="true" :default-active="$route.path">
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/books">全部图书</el-menu-item>
          <template v-if="isAdmin">
            <el-menu-item index="/admin">管理后台</el-menu-item>
          </template>
          <template v-else>
            <el-menu-item index="/publish">小说投稿</el-menu-item>
            <el-menu-item index="/user">用户中心</el-menu-item>
          </template>
        </el-menu>
        <div class="user-box">
          <template v-if="user">
            <el-avatar :size="32" :src="user.avatar"></el-avatar>
            <span class="nickname">{{ user.nickname }}</span>
            <el-tag size="mini" :type="isAdmin ? 'danger' : 'success'">{{ isAdmin ? '管理员' : '普通用户' }}</el-tag>
            <el-button size="mini" type="text" @click="logout">退出</el-button>
          </template>
          <el-button v-else size="mini" type="primary" @click="$router.push('/login')">登录/注册</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view @login-success="refreshUser" />
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      user: null
    }
  },
  created() {
    this.refreshUser()
  },
  computed: {
    isAdmin() {
      return this.user && (this.user.role === 'ADMIN' || this.user.username === 'admin')
    }
  },
  methods: {
    refreshUser() {
      const raw = localStorage.getItem('novel-user')
      this.user = raw ? JSON.parse(raw) : null
    },
    logout() {
      localStorage.removeItem('novel-user')
      localStorage.removeItem('novel-token')
      this.refreshUser()
      this.$message.success('已退出登录')
      this.go('/home')
    },
    go(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    }
  }
}
</script>
