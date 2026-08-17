import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { showToast } from 'vant'
import { authApi } from '@/api/auth'
import http from '@/api'
import type { UserInfo } from '@/types'

const USER_KEY = 'zbt_user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('login_flag') || '') // 仅标记登录态，实际认证依赖 HttpOnly Cookie
  const userInfo = ref<UserInfo | null>(loadUser())

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => userInfo.value?.userId)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const userRoles = computed(() => userInfo.value?.roles || [])

  function loadUser(): UserInfo | null {
    try {
      const raw = localStorage.getItem(USER_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  }

  /** 应用启动时初始化 — 纯 Cookie 认证，无 localStorage JWT 备份 */
  async function init() {
    // 先尝试从 localStorage 恢复用户信息（快速展示 UI）
    const cached = loadUser()
    if (cached) {
      userInfo.value = cached
      token.value = localStorage.getItem('login_flag') || ''
    }
    // 调用 /shop/auth/me 校验登录态（纯 Cookie 认证）
    try {
      const user = await http.get('/shop/auth/me', { timeout: 5000 }) as UserInfo | null
      if (user && user.userId) {
        token.value = 'cookie-auth'
        userInfo.value = user
        localStorage.setItem(USER_KEY, JSON.stringify(user))
        localStorage.setItem('login_flag', '1')
      } else {
        clearLocalAuth()
      }
    } catch {
      // 网络错误时保留本地缓存，不清除登录态
      if (!cached) clearLocalAuth()
    }
  }

  function clearLocalAuth() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('login_flag')
    localStorage.removeItem(USER_KEY)
  }

  function setAuth(newToken: string, user: UserInfo) {
    // 角色校验：C端商城仅允许顾客账号登录
    if (!user.roles || user.roles.length === 0 || !user.roles.includes('ROLE_CUSTOMER')) {
      showToast('该账号无C端商城权限，请联系管理员')
      console.warn('非顾客账号，已阻止登录')
      logout()
      return false
    }
    token.value = 'cookie-auth'
    userInfo.value = user
    localStorage.setItem(USER_KEY, JSON.stringify(user))
    localStorage.setItem('login_flag', '1')
    return true
  }

  function updateProfile(patch: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...patch }
      localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
    }
  }

  function logout() {
    clearLocalAuth()
    // 清除所有按用户隔离的购物车缓存，避免跨用户数据污染
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith('zbt_cart')) keysToRemove.push(key)
    }
    keysToRemove.forEach(k => localStorage.removeItem(k))
  }

  /** 注销账号：调后端软禁用 + 清除本地登录态 */
  async function deactivateAccount() {
    await authApi.deactivate()
    logout()
  }

  return {
    token, userInfo, isLoggedIn, userId, userName, userRoles,
    init, setAuth, updateProfile, logout, deactivateAccount
  }
})
