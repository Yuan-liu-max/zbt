<template>
  <div class="login-page">
    <!-- 认证状态检查中 -->
    <div v-if="!authChecked" class="login-box">
      <p style="color:#999">加载中...</p>
    </div>

    <!-- 已登录区域 -->
    <div v-else-if="userStore.isLoggedIn" class="login-box">
      <h2>欢迎: {{ userName }}</h2>
      <p v-if="userStore.userInfo" class="user-detail">
        <span v-if="userStore.userInfo.storeName">{{ userStore.userInfo.storeName }}</span>
        <span v-if="userStore.userInfo.realName"> | {{ userStore.userInfo.realName }}</span>
      </p>
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <!-- 未登录：登录/注册切换 -->
    <div v-else class="form-wrap">
      <div class="tab-box">
        <span :class="['tab', { active: !isRegister }]" @click="isRegister = false">登录</span>
        <span :class="['tab', { active: isRegister }]" @click="isRegister = true">注册</span>
      </div>

      <!-- 登录表单 -->
      <div v-if="!isRegister" class="form">
        <div class="item">
          <label>账号</label>
          <input v-model="loginName" placeholder="输入账号">
        </div>
        <div class="item">
          <label>密码</label>
          <input v-model="loginPwd" type="password" placeholder="输入密码">
        </div>
        <button class="sub-btn" @click="handleLogin" :disabled="loginLoading">
          {{ loginLoading ? '登录中...' : '登录' }}
        </button>
      </div>

      <!-- 注册表单 -->
      <div v-if="isRegister" class="form">
        <div class="item">
          <label>账号</label>
          <input v-model="regName" placeholder="输入账号">
        </div>
        <div class="item">
          <label>密码</label>
          <input v-model="regPwd" type="password" placeholder="输入密码">
        </div>
        <div class="item">
          <label>确认密码</label>
          <input v-model="regPwdRepeat" type="password" placeholder="再次输入密码">
        </div>
        <div class="item">
          <label>手机号</label>
          <input v-model="tel" placeholder="输入手机号">
        </div>
        <button class="sub-btn" @click="handleRegister" :disabled="regLoading">
          {{ regLoading ? '注册中...' : '注册' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/useUserStore'

const userStore = useUserStore()

const isRegister = ref(false)
const authChecked = ref(false)
const loginName = ref('')
const loginPwd = ref('')
const loginLoading = ref(false)
const regName = ref('')
const regPwd = ref('')
const regPwdRepeat = ref('')
const tel = ref('')
const regLoading = ref(false)

const userName = computed(() => userStore.userInfo?.realName || userStore.userInfo?.username || '')

onMounted(async () => {
  if (userStore.isLoggedIn || userStore.token) {
    try {
      const data = await authApi.getMe()
      if (data && data.userId) {
        // 已在 init() 中处理，这里补一下兼容
      }
    } catch {
      // 静默处理
    }
  }
  authChecked.value = true
})

// 登录
async function handleLogin() {
  if (!loginName.value || !loginPwd.value) {
    showToast('账号和密码不能为空！')
    return
  }
  loginLoading.value = true
  try {
    const data = await authApi.login({ username: loginName.value, password: loginPwd.value })
    userStore.setAuth(data.token, data)
    showToast('登录成功！')
  } catch {
    // 错误已在拦截器处理
  } finally {
    loginLoading.value = false
  }
}

// 退出登录
async function handleLogout() {
  try {
    await authApi.logout()
  } catch {
    // 忽略登出错误
  }
  userStore.logout()
}

// 注册
async function handleRegister() {
  if (!regName.value || !regPwd.value || !regPwdRepeat.value || !tel.value) {
    showToast('所有字段不能为空!')
    return
  }
  if (regPwd.value !== regPwdRepeat.value) {
    showToast('密码设置前后不一致!')
    return
  }
  if (!(/^1[3-9]\d{9}$/.test(tel.value))) {
    showToast('手机号码有误,请重填')
    return
  }
  regLoading.value = true
  try {
    const data = await authApi.register({
      username: regName.value,
      password: regPwd.value,
      phone: tel.value
    })
    userStore.setAuth(data.token, data)
    showToast('注册成功!')
  } catch {
    // 错误已在拦截器处理
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.login-page { padding: 20px; }
.tab-box { display: flex; margin-bottom: 15px; }
.tab { flex: 1; text-align: center; line-height: 35px; border: 1px solid #ccc; cursor: pointer; }
.tab.active { background: #ff7d00; color: #fff; }
.item { margin: 10px 0; }
label { display: inline-block; width: 70px; }
input { width: 60%; padding: 6px; border: 1px solid #ccc; border-radius: 4px; }
.sub-btn { width: 100%; height: 36px; background: #ff7d00; color: #fff; border: none; margin-top: 10px; border-radius: 4px; }
.sub-btn:disabled { opacity: 0.6; }
.logout-btn { margin-top: 10px; padding: 6px 20px; color: red; background: none; border: 1px solid red; border-radius: 4px; }
.user-detail { color: #666; font-size: 14px; margin: 8px 0; }
.login-box { text-align: center; padding: 20px 0; }
</style>
