import axios from 'axios'
import type { AxiosInstance } from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  withCredentials: true, // 允许携带 HttpOnly Cookie
})

// 请求拦截器 — Cookie 由浏览器自动携带，同时附加 Authorization header 作为后备
request.interceptors.request.use((config) => {
  // 动态获取 token（避免循环依赖）
  const token = (window as any).__AUTH_TOKEN__
  if (token) {
    config.headers = config.headers || {}
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 - 统一处理 {code, msg, data} 外层
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      const isAuthCheck = response.config.url?.includes('/auth/me')
      if (!isAuthCheck) message.error(res.msg || '请求失败')
      if (res.code === 401 && !isAuthCheck) {
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg))
    }
    return res.data
  },
  (error) => {
    if (error.response?.status === 401) {
      const isAuthCheck = error.config?.url?.includes('/auth/me')
      if (!isAuthCheck) router.push('/login')
    } else {
      message.error(error.response?.data?.msg || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
