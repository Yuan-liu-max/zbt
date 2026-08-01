<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人中心</h2>
    </div>

    <div class="center-layout">
      <!-- 左侧用户信息 -->
      <div class="profile-card">
        <div class="profile-avatar">
          <a-avatar :size="80" style="background: linear-gradient(135deg, #c8a44d, #e8d59a)">
            <template #icon><UserOutlined style="font-size: 36px" /></template>
          </a-avatar>
        </div>
        <h3 class="profile-name">{{ userInfo.username }}</h3>
        <p class="profile-role">{{ userInfo.role }}</p>

        <div class="profile-info">
          <div class="info-row">
            <span class="info-label">账号ID</span>
            <span class="info-value">{{ userInfo.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ userInfo.phone }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ userInfo.registeredAt }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">最后登录</span>
            <span class="info-value">{{ userInfo.lastLoginAt }}</span>
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
              <div class="stat-value">{{ stats.productCount }}</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon blue"><FileTextOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">订单数量</div>
              <div class="stat-value">{{ stats.orderCount }}</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon green"><TeamOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">今日访客</div>
              <div class="stat-value">{{ stats.todayVisitors.toLocaleString() }}</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon purple"><AccountBookOutlined /></div>
            <div class="stat-info">
              <div class="stat-label">今日成交额</div>
              <div class="stat-value">¥{{ stats.todaySales.toLocaleString() }}</div>
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
import { ref, reactive, markRaw, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined, ShoppingOutlined, FileTextOutlined, TeamOutlined,
  AccountBookOutlined, BarChartOutlined, SettingOutlined
} from '@ant-design/icons-vue'
import type { UserInfo, UserStats } from '@/types/profile'
import { profileApi } from '@/api/profile'

const router = useRouter()

const userInfo = reactive<UserInfo>({
  id: '', username: '', role: '', phone: '', email: '',
  timezone: '', language: '', dateFormat: '',
  registeredAt: '', lastLoginAt: ''
})

const stats = reactive<UserStats>({
  productCount: 0, orderCount: 0, todayVisitors: 0, todaySales: 0
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
  } catch { message.error('加载数据失败') }
}

const handleEntry = (entry: any) => {
  router.push(entry.path)
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.center-layout { display: flex; gap: 24px; }

.profile-card {
  width: 320px; background: #fff; border-radius: 12px; padding: 32px 24px;
  display: flex; flex-direction: column; align-items: center; flex-shrink: 0;
}
.profile-avatar { margin-bottom: 16px; }
.profile-name { font-size: 20px; font-weight: 600; color: #333; margin-bottom: 4px; }
.profile-role { font-size: 13px; color: #c8a44d; margin-bottom: 24px; }
.profile-info { width: 100%; }
.info-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f5f5f5; font-size: 13px; }
.info-label { color: #999; }
.info-value { color: #333; }

.stats-panel { flex: 1; }
.panel-title { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 16px; }
.stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.stat-item {
  background: #fff; border-radius: 12px; padding: 20px;
  display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
.stat-icon.gold { background: linear-gradient(135deg, #fff7e6, #ffe58f); color: #c8a44d; }
.stat-icon.blue { background: linear-gradient(135deg, #e6f7ff, #91d5ff); color: #1890ff; }
.stat-icon.green { background: linear-gradient(135deg, #f6ffed, #b7eb8f); color: #52c41a; }
.stat-icon.purple { background: linear-gradient(135deg, #f9f0ff, #d3adf7); color: #722ed1; }
.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: 700; color: #333; }

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
