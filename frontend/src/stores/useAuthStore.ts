import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import axios from 'axios'

const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_HQ', 'ROLE_REGIONAL', 'ROLE_MANAGER', 'ROLE_ASSOCIATE']
const JWT_KEY = 'zbt_admin_jwt'

function loadJwt(): string | null {
  try { return localStorage.getItem(JWT_KEY) } catch { return null }
}
function saveJwt(jwt: string) {
  try { localStorage.setItem(JWT_KEY, jwt) } catch { /* ignore */ }
}
function clearJwt() {
  try { localStorage.removeItem(JWT_KEY) } catch { /* ignore */ }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const userInfo = ref<any>(null)
  const isLoading = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  /** 应用启动时通过 Cookie 校验登录态，仅管理员角色算已登录 */
  async function init() {
    if (token.value) return

    // 从 localStorage 恢复 JWT 供 Authorization header 使用（刷新后内存丢失的补偿）
    const cachedJwt = loadJwt()
    if (cachedJwt) (window as any).__AUTH_TOKEN__ = cachedJwt

    isLoading.value = true
    try {
      const headers: Record<string, string> = {}
      if (cachedJwt) headers['Authorization'] = `Bearer ${cachedJwt}`
      const res = await axios.get('/api/auth/me', { withCredentials: true, timeout: 5000, headers })
      const data = res.data
      if (data && data.code === 200 && data.data) {
        const roles: string[] = data.data.roles || []
        if (roles.some((r: string) => ADMIN_ROLES.includes(r))) {
          token.value = 'cookie-auth'
          userInfo.value = data.data
        } else {
          clearJwt()
        }
      }
    } catch {
      token.value = ''
      userInfo.value = null
    } finally {
      isLoading.value = false
    }
  }

  async function login(username: string, password: string) {
    const data = await request.post('/auth/login', { username, password }) as any
    token.value = data.token || ''
    userInfo.value = data
    // 持久化 JWT 到 localStorage，刷新页面后可恢复
    if (data.token) {
      (window as any).__AUTH_TOKEN__ = data.token
      saveJwt(data.token)
    }
    return data
  }

  function updateUserInfo(patch: Record<string, any>) {
    userInfo.value = { ...(userInfo.value || {}), ...patch }
  }

  async function logout() {
    try { await request.post('/auth/logout') } catch { /* ignore */ }
    token.value = ''
    userInfo.value = null
    ;(window as any).__AUTH_TOKEN__ = null
    clearJwt()
  }

  return { token, userInfo, isLoggedIn, isLoading, init, login, updateUserInfo, logout }
})
