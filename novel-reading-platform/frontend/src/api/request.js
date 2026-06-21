import axios from 'axios'
import { Message } from 'element-ui'

const service = axios.create({
  baseURL: process.env.VUE_APP_API_BASE || '',
  timeout: 8000
})

service.interceptors.response.use(
  response => {
    const data = response.data
    if (data && data.success === false) {
      Message.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  error => {
    Message.error('接口请求失败，请确认网关和微服务已启动')
    return Promise.reject(error)
  }
)

export default service
