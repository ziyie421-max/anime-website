// API配置文件 - 配置axios实例和请求拦截器
import axios from 'axios'
import { ElMessage } from 'element-plus'

// API 基础地址：统一用相对路径 /api
//  - 开发：由 vite.config.js 的 /api 代理转发到本地 8080 后端
//  - 生产：前端与后端同源（打包进同一个 jar），直接命中 /api
const getApiBaseUrl = () => '/api'

// 创建axios实例
const api = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token到请求头
api.interceptors.request.use(
  (config) => {
    // 优先使用accessToken（新系统），兼容旧的token字段
    const token = localStorage.getItem('accessToken') || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    const { response } = error

    if (response) {
      switch (response.status) {
        case 401:
          // 只在非登录页面时才处理401
          if (!window.location.pathname.includes('/login')) {
            ElMessage.error('登录已过期，请重新登录')
            // 清除所有token
            localStorage.removeItem('token')
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('userRole')
            window.location.href = '/login'
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

export default api
