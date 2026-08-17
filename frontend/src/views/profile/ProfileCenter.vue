<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <div class="center-layout">
      <!-- 左侧用户信息 -->
      <div class="profile-card">
        <div class="profile-avatar">
          <a-avatar :size="88" style="background: linear-gradient(135deg, #c8a44d, #e8d59a)">
            <template #icon>
              <img v-if="avatarUrl" :src="avatarUrl" class="profile-avatar-img" />
              <UserOutlined v-else style="font-size: 40px" />
            </template>
          </a-avatar>
        </div>
        <h3 class="profile-name">{{ userInfo.realName || userInfo.username }}</h3>
        <div class="profile-roles">
          <a-tag v-for="name in displayRoles" :key="name" color="gold">{{ name }}</a-tag>
        </div>

        <div class="profile-actions">
          <a-button type="primary" size="small" @click="goEdit">
            <EditOutlined /> 编辑资料
          </a-button>
          <a-button size="small" @click="goChangePassword">
            <LockOutlined /> 修改密码
          </a-button>
        </div>

        <div class="profile-info">
          <div class="info-row" v-if="userInfo.id">
            <span class="info-label">账号ID</span>
            <span class="info-value">{{ userInfo.id }}</span>
          </div>
          <div class="info-row" v-if="userInfo.storeName">
            <span class="info-label">所属门店</span>
            <span class="info-value">{{ userInfo.storeName }}</span>
          </div>
          <div class="info-row" v-if="userInfo.regionName">
            <span class="info-label">所属区域</span>
            <span class="info-value">{{ userInfo.regionName }}</span>
          </div>
          <div class="info-row" v-if="userInfo.position">
            <span class="info-label">职位</span>
            <span class="info-value">{{ userInfo.position }}</span>
          </div>
          <div class="info-row" v-if="userInfo.phone">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ userInfo.phone }}</span>
          </div>
          <div class="info-row" v-if="userInfo.email">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email }}</span>
          </div>
          <div class="info-row" v-if="userInfo.entryDate">
            <span class="info-label">入职日期</span>
            <span class="info-value">{{ userInfo.entryDate }}</span>
          </div>
          <div class="info-row" v-if="userInfo.createdAt">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(userInfo.createdAt) }}</span>
          </div>
          <div class="info-row" v-if="userInfo.lastLoginAt">
            <span class="info-label">最后登录</span>
            <span class="info-value">{{ formatDate(userInfo.lastLoginAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧数据概览 -->
      <div class="stats-panel">
        <h3 class="panel-title">数据概览</h3>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon gold"><ShoppingOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">商品数量</div>
              <div class="stat-value">{{ stats.productCount.toLocaleString() }}</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon blue"><FileTextOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">订单数量</div>
              <div class="stat-value">{{ stats.orderCount.toLocaleString() }}</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon green"><TeamOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">客户数量</div>
              <div class="stat-value">{{ stats.customerCount.toLocaleString() }}</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon purple"><AccountBookOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">今日成交额</div>
              <div class="stat-value">¥{{ formatMoney(stats.todaySales) }}</div>
            </div>
          </div>
        </div>

        <!-- 快捷入口 -->
        <h3 class="panel-title" style="margin-top: 24px;">快捷入口</h3>
        <div class="quick-entry">
          <div class="entry-item" v-for="entry in quickEntries" :key="entry.label" @click="handleEntry(entry)">
            <div class="entry-icon" :style="{ background: entry.color + '15', color: entry.color }">
              <component :is="entry.icon" />
            </div>
            <span class="entry-label">{{ entry.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, markRaw, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  UserOutlined, EditOutlined, LockOutlined, ShoppingOutlined, FileTextOutlined,
  TeamOutlined, AccountBookOutlined, BarChartOutlined
} from '@ant-design/icons-vue'
import type { UserInfo, UserStats } from '@/types/profile'
import { profileApi } from '@/api/profile'
import { resolveAvatarUrl } from '@/utils/avatar'

const router = useRouter()

const userInfo = reactive<UserInfo>({
  id: '', username: '', role: '', phone: '', email: '',
  timezone: '', language: '', dateFormat: ''
})

const stats = reactive<UserStats>({
  productCount: 0, orderCount: 0, customerCount: 0, todaySales: 0
})

const avatarUrl = computed(() => resolveAvatarUrl(userInfo.avatar))

const displayRoles = computed(() => {
  const names = userInfo.roleNames?.length ? userInfo.roleNames : (userInfo.roles || [])
  return names.length ? names : (userInfo.role ? [userInfo.role] : [])
})

const quickEntries = [
  { label: '商品管理', icon: markRaw(ShoppingOutlined), color: '#c8a44d', path: '/goods/list' },
  { label: '订单列表', icon: markRaw(FileTextOutlined), color: '#1890ff', path: '/order/list' },
  { label: '财务中心', icon: markRaw(AccountBookOutlined), color: '#52c41a', path: '/finance' },
  { label: '数据统计', icon: markRaw(BarChartOutlined), color: '#722ed1', path: '/report' },
]

const loadData = async () => {
  try {
    const [info, userStats] = await Promise.all([profileApi.getUserInfo(), profileApi.getStats()])
    Object.assign(userInfo, info)
    Object.assign(stats, userStats)
  } catch (error) { console.error('加载数据失败', error) }
}

const handleEntry = (entry: any) => {
  router.push(entry.path)
}

const goEdit = () => {
  router.push('/settings')
}

const goChangePassword = () => {
  router.push('/settings?tab=password')
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  const d = new Date(value)
  if (isNaN(d.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatMoney = (value: number) => {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.center-layout { display: flex; gap: 24px; }

.profile-card {
  width: 320px; background: #fff; border-radius: 12px; padding: 32px 24px 24px;
  display: flex; flex-direction: column; align-items: center; flex-shrink: 0;
}
.profile-avatar { margin-bottom: 12px; }
.profile-avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.profile-name { font-size: 20px; font-weight: 600; color: #333; margin-bottom: 4px; }
.profile-roles { display: flex; gap: 4px; margin-bottom: 16px; flex-wrap: wrap; justify-content: center; }
.profile-actions { display: flex; gap: 8px; margin-bottom: 20px; }
.profile-info { width: 100%; }
.info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 13px; }
.info-row:last-child { border-bottom: none; }
.info-label { color: #999; flex-shrink: 0; margin-right: 16px; }
.info-value { color: #333; text-align: right; word-break: break-all; }

.stats-panel { flex: 1; min-width: 0; }
.panel-title { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 16px; }
.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.stat-item {
  background: #fff; border-radius: 12px; padding: 20px;
  display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
.stat-icon.gold { background: linear-gradient(135deg, #fff7e6, #ffe58f); color: #c8a44d; }
.stat-icon.blue { background: linear-gradient(135deg, #e6f7ff, #91d5ff); color: #1890ff; }
.stat-icon.green { background: linear-gradient(135deg, #f6ffed, #b7eb8f); color: #52c41a; }
.stat-icon.purple { background: linear-gradient(135deg, #f9f0ff, #d3adf7); color: #722ed1; }
.stat-info { flex: 1; min-width: 0; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: 700; color: #333; white-space: nowrap; }

.quick-entry { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.entry-item {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  padding: 16px; border-radius: 10px; cursor: pointer; transition: background 0.2s;
}
.entry-item:hover { background: #f5f5f5; }
.entry-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; }
.entry-label { font-size: 13px; color: #333; }

@media (max-width: 992px) { .center-layout { flex-direction: column; } .profile-card { width: 100%; } }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr; }
  .quick-entry { grid-template-columns: repeat(2, 1fr); }
}
</style>
