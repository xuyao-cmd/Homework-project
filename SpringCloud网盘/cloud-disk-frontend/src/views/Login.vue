<template>
    <div class="login-container">
        <Sakura />
        <div class="login-card">
            <div class="card-header">
                <div class="cloud-icon">☁️</div>
                <h2>Cloud Disk</h2>
                <p>轻量云盘，安全可靠</p>
            </div>
            <el-form :model="form" :rules="rules" ref="formRef">
                <el-form-item prop="username">
                    <el-input 
                        v-model="form.username" 
                        placeholder="用户名" 
                        prefix-icon="User"
                        size="large"
                        class="custom-input"
                    />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input 
                        v-model="form.password" 
                        type="password" 
                        placeholder="密码" 
                        prefix-icon="Lock"
                        size="large"
                        class="custom-input"
                        show-password
                    />
                </el-form-item>
                <el-form-item>
                    <el-button 
                        type="primary" 
                        @click="handleLogin" 
                        :loading="loading"
                        size="large"
                        class="login-btn"
                    >
                        登 录
                    </el-button>
                </el-form-item>
                <div class="third-login">
                    <div class="divider">
                        <span>第三方登录</span>
                    </div>
                    <div class="third-icons">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <div class="third-btn github-btn" @click="handleThirdLogin('github')">
                                <svg viewBox="0 0 24 24" width="22" height="22"><path fill="currentColor" d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61-.546-1.385-1.335-1.755-1.335-1.755-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 21.795 24 17.295 24 12 24 5.37 18.63 0 12 0z"/></svg>
                            </div>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <div class="third-btn wechat-btn" @click="handleThirdLogin('wechat')">
                                <svg viewBox="0 0 24 24" width="22" height="22"><path fill="currentColor" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.952-7.062-6.122zm-2.18 2.769c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982z"/></svg>
                            </div>
                        </el-tooltip>
                        <el-tooltip content="QQ登录" placement="top">
                            <div class="third-btn qq-btn" @click="handleThirdLogin('qq')">
                                <svg viewBox="0 0 24 24" width="22" height="22"><path fill="currentColor" d="M12.003 0c-.155 0-.31.004-.464.013v.002C6.594.293 2.273 4.95 2.273 10.5c0 1.21.204 2.374.58 3.456-.555 1.073-1.09 2.513-1.63 4.337-.148.5.225 1.06.776 1.06.228 0 .451-.103.603-.272l.002-.003c.183-.204.376-.44.591-.7a20.07 20.07 0 0 1 1.058-1.133c.54.261 1.138.48 1.772.64-.206.744-.38 1.514-.517 2.305l-.003.02c-.057.34.117.67.422.76.304.09.627-.083.746-.4a38.3 38.3 0 0 0 .763-2.317c.68.108 1.4.167 2.144.17h.008c.744-.003 1.464-.062 2.144-.17a38.3 38.3 0 0 0 .763 2.317c.12.317.442.49.746.4.305-.09.479-.42.422-.76l-.003-.02a27.31 27.31 0 0 0-.517-2.305c.634-.16 1.233-.379 1.772-.64.34.374.672.722 1.058 1.133l.002.003c.152.17.375.272.603.272.551 0 .924-.56.776-1.06-.54-1.824-1.075-3.264-1.63-4.337A5.482 5.482 0 0 0 21.727 10.5c0-5.523-4.277-10.16-9.204-10.48A8.89 8.89 0 0 0 12.003 0z"/></svg>
                            </div>
                        </el-tooltip>
                    </div>
                </div>
                <el-form-item>
                    <el-button 
                        @click="$router.push('/register')"
                        size="large"
                        class="register-btn"
                    >
                        还没有账号？立即注册
                    </el-button>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import Sakura from '@/components/Sakura.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()

const form = reactive({
    username: '',
    password: ''
})

const rules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
    if (!formRef.value) return
    await formRef.value.validate()
    
    loading.value = true
    try {
        const res = await authAPI.login(form)
        if (res.code === 200) {
            ElMessage.success('登录成功')
            userStore.setUserInfo({
                token: res.data.token,
                userId: res.data.userId,
                username: res.data.username,
                role: res.data.role
            })
            router.push('/')
        }
    } finally {
        loading.value = false
    }
}

const handleThirdLogin = (platform) => {
    // 前端回调地址，后端授权完成后会重定向回这里
    const frontendCallback = encodeURIComponent(`http://localhost:5173/oauth/callback?platform=${platform}`)
    const config = {
        github: { name: 'GitHub', url: `http://localhost:8080/oauth/github/authorize?redirect_uri=${frontendCallback}` },
        wechat: { name: '微信', url: `http://localhost:8080/oauth/wechat/authorize?redirect_uri=${frontendCallback}` },
        qq: { name: 'QQ', url: `http://localhost:8080/oauth/qq/authorize?redirect_uri=${frontendCallback}` }
    }
    const item = config[platform]
    if (item) {
        ElMessage.info(`正在跳转到${item.name}授权...`)
        localStorage.setItem('oauth_platform', platform)
        window.location.href = item.url
    }
}

// 页面加载时检查是否是 OAuth 回调
onMounted(async () => {
    const urlParams = new URLSearchParams(window.location.search)
    const code = urlParams.get('code')
    const platform = localStorage.getItem('oauth_platform')

    if (code && platform) {
        localStorage.removeItem('oauth_platform')
        try {
            const res = await authAPI.oauthCallback(platform, code)
            if (res.code === 200) {
                ElMessage.success(`${platform} 登录成功`)
                userStore.setUserInfo({
                    token: res.data.token,
                    userId: res.data.userId,
                    username: res.data.username,
                    role: res.data.role
                })
                router.push('/')
            }
        } catch (e) {
            ElMessage.error('第三方登录失败')
        }
    }
})
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background: linear-gradient(135deg, #f5a6b5 0%, #fbc2c8 25%, #fad0d4 50%, #c8b6e2 75%, #a18cd1 100%);
    background-size: 400% 400%;
    animation: gradientShift 15s ease infinite;
    position: relative;
}

@keyframes gradientShift {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}

.login-card {
    width: 420px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.3);
    z-index: 1;
}

.card-header {
    text-align: center;
    margin-bottom: 35px;
}

.cloud-icon {
    font-size: 48px;
    margin-bottom: 10px;
    animation: float 3s ease-in-out infinite;
}

@keyframes float {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-8px); }
}

.card-header h2 {
    font-size: 28px;
    font-weight: 700;
    color: #2c3e50;
    margin: 0 0 8px 0;
    letter-spacing: 2px;
}

.card-header p {
    color: #95a5a6;
    margin: 0;
    font-size: 14px;
}

.custom-input :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.login-btn {
    width: 100%;
    height: 44px;
    border-radius: 12px;
    font-size: 16px;
    letter-spacing: 4px;
    background: linear-gradient(135deg, #f5a6b5 0%, #a18cd1 100%);
    border: none;
    transition: all 0.3s;
}

.login-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(161, 140, 209, 0.4);
}

.register-btn {
    width: 100%;
    height: 44px;
    border-radius: 12px;
    font-size: 14px;
    color: #a18cd1;
    border: 1.5px dashed #d5c8e8;
    background: transparent;
    transition: all 0.3s;
}

.register-btn:hover {
    border-color: #a18cd1;
    background: rgba(161, 140, 209, 0.05);
}

.third-login { margin-top: 8px; margin-bottom: 16px; }
.divider { display: flex; align-items: center; margin-bottom: 16px; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #e8e8e8; }
.divider span { padding: 0 16px; font-size: 12px; color: #999; }
.third-icons { display: flex; justify-content: center; gap: 20px; }
.third-btn { width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.3s; border: 1px solid #e8e8e8; }
.third-btn:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,0.12); }
.github-btn { color: #333; background: #f5f5f5; }
.github-btn:hover { background: #24292e; color: white; border-color: #24292e; }
.wechat-btn { color: #07c160; background: #e8fce8; }
.wechat-btn:hover { background: #07c160; color: white; border-color: #07c160; }
.qq-btn { color: #12b7f5; background: #e6f7ff; }
.qq-btn:hover { background: #12b7f5; color: white; border-color: #12b7f5; }
</style>
