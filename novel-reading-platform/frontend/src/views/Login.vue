<template>
  <div class="login-page">
    <el-card class="login-card">
      <div slot="header" class="login-title">
        <span>{{ activeName === 'login' ? '用户登录' : '用户注册' }}</span>
      </div>
      <el-tabs v-model="activeName" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="admin / author / 普通用户"></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" placeholder="123456" show-password></el-input>
            </el-form-item>
            <el-button type="primary" class="full-btn" :loading="loading" @click="handleLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" placeholder="请输入昵称"></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password></el-input>
            </el-form-item>
            <el-button type="success" class="full-btn" :loading="loading" @click="handleRegister">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { login, register } from '../api/user'

export default {
  name: 'Login',
  data() {
    return {
      activeName: 'login',
      loading: false,
      loginForm: { username: 'admin', password: '123456' },
      registerForm: { username: '', nickname: '', password: '' }
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      try {
        const res = await login(this.loginForm)
        localStorage.setItem('novel-token', res.data.token)
        localStorage.setItem('novel-user', JSON.stringify(res.data.user))
        this.$emit('login-success')
        this.$message.success('登录成功')
        this.$router.push(this.$route.query.redirect || (res.data.user.role === 'ADMIN' || res.data.user.username === 'admin' ? '/admin' : '/home'))
      } catch (e) {
        // error already shown by axios interceptor
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      this.loading = true
      try {
        await register(this.registerForm)
        const res = await login({ username: this.registerForm.username, password: this.registerForm.password })
        localStorage.setItem('novel-token', res.data.token)
        localStorage.setItem('novel-user', JSON.stringify(res.data.user))
        this.$emit('login-success')
        this.$message.success('注册成功，已作为普通用户登录')
        this.$router.push(this.$route.query.redirect || '/home')
      } catch (e) {
        // error already shown by axios interceptor
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 120px);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 430px;
}
.login-title {
  font-size: 18px;
  font-weight: 700;
  text-align: center;
}
.full-btn {
  width: 100%;
}
</style>
