/**
 * Axios 实例 — 珠宝通商城全局 HTTP 客户端
 *
 * 认证方式：HttpOnly Cookie（zbt_token）
 * - 登录时后端 Set-Cookie，浏览器自动携带
 * - 无需手动管理 Token，防 XSS 攻击
 */
import axios from 'axios'
import { showToast } from 'vant'
import router from '@/router'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true, // 携带 HttpOnly Cookie
})

// ---- 请求拦截器：纯 HttpOnly Cookie 认证（无需手动添加 Authorization） ----
http.interceptors.request.use((config) => {
  return config
})

// ---- 响应拦截器 ----
http.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        // /shop/auth/me 和 /shop/auth/stats 的 401 是正常未登录状态，不弹提示不跳转
        const isSilentAuth = response.config.url?.includes('/shop/auth/me') || response.config.url?.includes('/shop/auth/stats')
        if (!isSilentAuth) {
          showToast('登录已过期，请重新登录')
          router.push('/profile')
        }
      } else {
        showToast(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res.data
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        const isSilentAuth = error.config?.url?.includes('/shop/auth/me') || error.config?.url?.includes('/shop/auth/stats')
        if (!isSilentAuth) {
          showToast('登录已过期，请重新登录')
          router.push('/profile')
        }
      } else if (status >= 500) {
        showToast('服务器繁忙，请稍后重试')
      }
    } else {
      showToast('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default http
