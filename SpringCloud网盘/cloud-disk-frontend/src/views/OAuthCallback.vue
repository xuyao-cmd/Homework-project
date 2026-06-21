<template>
  <div class="oauth-callback">
    <div class="callback-card">
      <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
      <h2>正在登录中...</h2>
      <p>{{ statusText }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const statusText = ref('正在获取授权信息...')

onMounted(async () => {
  const platform = route.query.platform
  const code = route.query.code

  if (!platform || !code) {
    ElMessage.error('授权参数不完整')
    setTimeout(() => router.replace('/login'), 1500)
    return
  }

  statusText.value = `正在通过${getPlatformName(platform)}登录...`

  try {
    // 调用后端回调接口，用code换取token
    const res = await authAPI.oauthCallback(platform, code)

    if (res.code === 200 && res.data) {
      const { token, userId, username, role } = res.data
      userStore.setUserInfo({ token, userId, username, role })
      ElMessage.success(`${getPlatformName(platform)}登录成功！`)
      // 跳转到首页
      setTimeout(() => {
        router.replace('/')
      }, 500)
    } else {
      throw new Error(res.message || '登录失败')
    }
  } catch (err) {
    statusText.value = '登录失败，正在返回登录页...'
    ElMessage.error(err.message || '第三方登录失败')
    setTimeout(() => {
      router.replace('/login')
    }, 1500)
  }
})

function getPlatformName(platform) {
  const map = { github: 'GitHub', wechat: '微信', qq: 'QQ' }
  return map[platform] || platform
}
</script>

<style scoped>
.oauth-callback {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5a6b5 0%, #fbc2c8 25%, #fad0d4 50%, #c8b6e2 75%, #a18cd1 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.callback-card {
  text-align: center;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 50px 60px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.loading-icon {
  color: #a18cd1;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.callback-card h2 {
  color: #2c3e50;
  margin: 0 0 12px 0;
  font-size: 22px;
}

.callback-card p {
  color: #95a5a6;
  margin: 0;
  font-size: 14px;
}
</style>
