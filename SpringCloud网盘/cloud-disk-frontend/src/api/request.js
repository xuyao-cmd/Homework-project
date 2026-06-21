import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: 'http://localhost:8080',  // Gateway 地址
    timeout: 30000
})

// 请求拦截器 - 添加 token, 处理数组参数
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }
    // 处理 params 中的数组参数，Spring Boot 需要逗号分隔
    if (config.params) {
        for (const key in config.params) {
            if (Array.isArray(config.params[key])) {
                config.params[key] = config.params[key].join(',')
            }
        }
    }
    return config
})

// 判断是否是管理员接口的错误（静默处理，不弹窗）
function isAdminSilentError(error) {
    const url = error.config?.url || ''
    const status = error.response?.status
    const adminPaths = ['/auth/admin/', '/file/admin/']
    return status === 403 && adminPaths.some(p => url.includes(p))
}

// 响应拦截器 - 处理错误
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            if (res.code === 401) {
                localStorage.removeItem('token')
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.message))
        }
        return res
    },
    error => {
        if (isAdminSilentError(error)) {
            // 管理员接口 403 静默处理，由调用方自行提示
            return Promise.reject(error)
        }
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request
