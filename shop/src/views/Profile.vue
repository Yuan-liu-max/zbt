<template>
  <div class="profile-page page-container">
    <van-nav-bar title="我的" />

    <!-- 未登录 -->
    <div v-if="!userStore.isLoggedIn" class="login-section">
      <div class="login-hero">
        <div class="login-hero__avatar"><van-icon name="user-circle-o" size="72" color="#c8a44d" /></div>
        <h2 class="text-lg" style="font-weight:var(--weight-bold);margin:var(--space-md) 0 0">登录珠宝通</h2>
        <p class="text-sm text-hint">登录后享受会员专属权益</p>
      </div>

      <div class="form-wrap" v-if="!isRegister">
        <van-form @submit="handleLogin" class="login-form">
          <van-cell-group inset>
            <van-field v-model="loginName" placeholder="请输入账号" left-icon="user-o" />
            <van-field v-model="loginPwd" type="password" placeholder="请输入密码" left-icon="lock" />
          </van-cell-group>
          <div class="form-actions">
            <van-button round block type="primary" native-type="submit" :loading="loginLoading">登录</van-button>
          </div>
        </van-form>
        <div class="text-center text-sm"><span class="text-hint">还没有账号？</span><span class="text-link" @click="isRegister = true">立即注册</span></div>
      </div>

      <div class="form-wrap" v-else>
        <van-form @submit="handleRegister" class="login-form">
          <van-cell-group inset>
            <van-field v-model="regName" placeholder="请输入账号" left-icon="user-o" />
            <van-field v-model="regPwd" type="password" placeholder="请输入密码" left-icon="lock" />
            <van-field v-model="regPwd2" type="password" placeholder="再次输入密码" left-icon="lock" />
            <van-field v-model="tel" placeholder="请输入手机号" left-icon="phone-o" />
          </van-cell-group>
          <div class="form-actions"><van-button round block type="primary" native-type="submit" :loading="regLoading">注册</van-button></div>
        </van-form>
        <div class="text-center text-sm"><span class="text-hint">已有账号？</span><span class="text-link" @click="isRegister = false">去登录</span></div>
      </div>
    </div>

    <!-- 已登录 -->
    <div v-else>
      <div class="user-card">
        <div class="user-card__bg" />
        <div class="user-card__content">
          <van-image round width="60" height="60" :src="userStore.userInfo?.avatar || ''">
            <template #error><van-icon name="user-circle-o" size="60" color="#c8a44d" /></template>
          </van-image>
          <div class="flex-1">
            <p class="text-lg" style="font-weight:var(--weight-bold);color:#fff">{{ userStore.userName }}</p>
            <p class="text-sm" style="color:rgba(255,255,255,0.7)">{{ userStore.userInfo?.phone || '' }}</p>
          </div>
          <div class="settings-entry" @click="$router.push('/settings')">
            <span class="settings-entry__text">设置</span>
          </div>
        </div>
      </div>

      <!-- 统计栏 -->
      <div class="stats-row card">
        <div class="stat-item" @click="$router.push('/orders')"><span class="stat-num">{{ orderCount }}</span><span class="stat-label">我的订单</span></div>
        <div class="stat-item" @click="$router.push('/favorites')"><span class="stat-num">{{ favCount }}</span><span class="stat-label">收藏商品</span></div>
        <div class="stat-item"><span class="stat-num">{{ couponCount }}</span><span class="stat-label">优惠券</span></div>
      </div>

      <!-- 菜单 -->
      <div class="menu-list card" style="padding:0;overflow:hidden">
        <van-cell title="我的订单" icon="orders-o" is-link to="/orders" />
        <van-cell title="收货地址" icon="location-o" is-link to="/address" />
        <van-cell title="我的收藏" icon="star-o" is-link to="/favorites" :value="favCount || ''" />
        <van-cell title="优惠券" icon="coupon-o" is-link value="即将上线" />
        <van-cell title="消息通知" icon="chat-o" is-link to="/notifications" />
        <van-cell title="AI 智能导购" icon="service-o" is-link to="/ai-guide" />
        <van-cell title="设置" icon="setting-o" is-link to="/settings" />
      </div>

      <div style="margin:var(--space-2xl)">
        <van-button round block type="default" @click="handleLogout">退出登录</van-button>
      </div>
    </div>

    <van-tabbar v-model="activeTab" :active-color="'#c8a44d'" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/category">分类</van-tabbar-item>
      <van-tabbar-item icon="cart-o" to="/cart">购物车</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { authApi } from '@/api/auth'
import { favoriteApi } from '@/api/services'
import { useUserStore } from '@/stores/useUserStore'
import { useCartStore } from '@/stores/useCartStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isRegister = ref(false)
const activeTab = ref(4)
const loginName = ref('')
const loginPwd = ref('')
const loginLoading = ref(false)
const regName = ref('')
const regPwd = ref('')
const regPwd2 = ref('')
const tel = ref('')
const regLoading = ref(false)

const favCount = ref(0)
const orderCount = ref(0)
const couponCount = ref(0)

/** 获取用户统计数据（订单数、收藏数、优惠券数） */
async function fetchStats() {
  if (!userStore.isLoggedIn) return
  try {
    const stats = await authApi.getStats()
    orderCount.value = stats.orderCount || 0
    favCount.value = stats.favoriteCount || 0
    couponCount.value = stats.couponCount || 0
  } catch { /* 非关键 */ }
}

/** 登录/注册后拉取完整用户信息（含通知偏好、时间戳等） */
async function refreshUserInfo() {
  try {
    const full = await authApi.getMe()
    userStore.updateProfile(full)
  } catch { /* 非关键，使用 login 返回的基础信息 */ }
}

/** 登录后：同步本地未登录商品 + 收藏 + 加载统计数据 */
async function onLoginSuccess() {
  // 同步本地未登录时添加的商品到服务端
  await cartStore.syncToServer()
  // 同步本地收藏到服务端
  const favs = JSON.parse(localStorage.getItem('zbt_favs') || '[]') as string[]
  if (favs.length > 0) {
    for (const id of favs) { favoriteApi.add(id).catch(() => {}) }
    localStorage.removeItem('zbt_favs')
  }
  // 拉取统计数据
  await fetchStats()
  // 登录成功后回跳原页面
  const redirect = route.query.redirect as string
  if (redirect) router.push(redirect)
}

async function handleLogin() {
  if (!loginName.value || !loginPwd.value) { showToast('账号和密码不能为空'); return }
  loginLoading.value = true
  try {
    const data = await authApi.login({ username: loginName.value, password: loginPwd.value })
    userStore.setAuth(data.token, { userId: data.userId, username: data.username, realName: data.realName, avatar: data.avatar, phone: data.phone, roles: data.roles })
    showToast('登录成功')
    refreshUserInfo()
    await onLoginSuccess()
  } catch { /* handled */ } finally { loginLoading.value = false }
}

async function handleRegister() {
  if (!regName.value || !regPwd.value || !tel.value) { showToast('请填写完整信息'); return }
  if (regPwd.value !== regPwd2.value) { showToast('两次密码不一致'); return }
  regLoading.value = true
  try {
    const data = await authApi.register({ username: regName.value, password: regPwd.value, phone: tel.value })
    userStore.setAuth(data.token, { userId: data.userId, username: data.username, realName: data.realName, avatar: data.avatar, phone: data.phone, roles: data.roles })
    showToast('注册成功')
    refreshUserInfo()
    await onLoginSuccess()
  } catch { /* handled */ } finally { regLoading.value = false }
}

function handleLogout() {
  authApi.logout().catch(() => {})
  cartStore.clear()   // 清空内存中的购物车
  userStore.logout()  // 清除登录态 + localStorage 购物车缓存
  orderCount.value = 0
  favCount.value = 0
  couponCount.value = 0
  showToast('已退出')
}

onMounted(() => {
  if (userStore.isLoggedIn) fetchStats()
})

// 监听登录态变化：从其他页面登录后回到此页面时自动刷新统计
watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) fetchStats()
})
</script>

<style scoped>
.profile-page { padding-bottom: 80px; }

/* Login */
.login-section { padding: var(--space-lg); }
.login-hero { text-align: center; padding: var(--space-3xl) 0 var(--space-xl); }
.login-hero__avatar { width: 88px; height: 88px; border-radius: 50%; background: var(--color-primary-bg); display: flex; align-items: center; justify-content: center; margin: 0 auto; }
.login-form { margin-top: var(--space-lg); }
.form-actions { margin: var(--space-lg) var(--space-md); }

/* User Card */
.user-card { position: relative; margin: var(--space-sm) var(--space-md); border-radius: var(--radius-xl); overflow: hidden; }
.user-card__bg { position: absolute; inset: 0; background: var(--gradient-dark); }
.user-card__bg::after { content: ''; position: absolute; top: -30%; right: -10%; width: 150px; height: 150px; border-radius: 50%; background: rgba(255,255,255,0.05); }
.user-card__content { position: relative; display: flex; align-items: center; gap: var(--space-lg); padding: var(--space-xl) var(--space-lg); z-index: 1; }

/* Stats */
.stats-row { display: flex; padding: var(--space-lg) var(--space-md) !important; }
.stat-item { flex: 1; text-align: center; cursor: pointer; display: flex; flex-direction: column; gap: var(--space-2xs); }
.stat-num { font-size: var(--font-xl); font-weight: var(--weight-bold); color: var(--text-primary); }
.stat-label { font-size: var(--font-2xs); color: var(--text-hint); }

/* Settings entry */
.settings-entry {
  padding: 4px 12px;
  border-radius: 20px;
  background: rgba(255,255,255,0.25);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid rgba(255,255,255,0.4);
}
.settings-entry__text {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

/* Menu */
.menu-list { margin-top: var(--space-sm); }
.menu-list :deep(.van-cell):last-child { margin-bottom: 80px; }
</style>
