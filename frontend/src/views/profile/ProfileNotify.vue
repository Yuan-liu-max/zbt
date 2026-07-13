<template>
  <div class="page-container">
    <div class="page-header">
      <h2>提醒</h2>
    </div>

    <div class="content-card">
      <!-- 标签页 -->
      <div class="notify-tabs">
        <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
          <a-tab-pane key="all" :tab="`全部 (${totalCount})`" />
          <a-tab-pane key="unread" :tab="`未读 (${unreadCount})`" />
          <a-tab-pane key="read" :tab="`已读 (${readCount})`" />
        </a-tabs>
        <a-button type="link" @click="handleMarkAllRead">
          <CheckCircleOutlined /> 全部已读
        </a-button>
      </div>

      <!-- 提醒列表 -->
      <div class="notify-list">
        <div v-for="item in notifications" :key="item.id" class="notify-item" :class="{ unread: !item.isRead }" @click="handleClick(item)">
          <div class="notify-icon" :style="{ background: getTypeColor(item.type) + '15', color: getTypeColor(item.type) }">
            <component :is="getTypeIcon(item.type)" />
          </div>
          <div class="notify-content">
            <div class="notify-title-row">
              <span class="notify-title">{{ item.title }}</span>
              <span class="notify-time">{{ item.time }}</span>
            </div>
            <div class="notify-desc">{{ item.content }}</div>
          </div>
          <div v-if="!item.isRead" class="unread-dot"></div>
        </div>
        <a-empty v-if="notifications.length === 0" description="暂无提醒" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > pageSize">
        <a-pagination
          v-model:current="currentPage"
          :total="total"
          :page-size="pageSize"
          :show-size-changer="false"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  CheckCircleOutlined, BellOutlined, ShoppingOutlined,
  CustomerServiceOutlined, ThunderboltOutlined, SettingOutlined
} from '@ant-design/icons-vue'
import type { NotificationItem } from '@/types/profile'
import { notificationApi, notificationTypeMap } from '@/api/mock/profile'

const activeTab = ref('all')
const notifications = ref<NotificationItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const totalCount = computed(() => notifications.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const readCount = computed(() => notifications.value.filter(n => n.isRead).length)

const iconMap: Record<string, any> = {
  BellOutlined, ShoppingOutlined, CustomerServiceOutlined, ThunderboltOutlined, SettingOutlined
}

const getTypeIcon = (type: string) => iconMap[notificationTypeMap[type]?.icon] || BellOutlined
const getTypeColor = (type: string) => notificationTypeMap[type]?.color || '#999'

const loadData = async () => {
  try {
    const res = await notificationApi.getList({ tab: activeTab.value, page: currentPage.value, pageSize: pageSize.value })
    notifications.value = res.list
    total.value = res.total
  } catch { message.error('加载失败') }
}

const handleTabChange = () => { currentPage.value = 1; loadData() }
const handlePageChange = (page: number) => { currentPage.value = page; loadData() }

const handleClick = (item: NotificationItem) => {
  item.isRead = true
  message.info(item.content)
}

const handleMarkAllRead = async () => {
  await notificationApi.markAllRead()
  message.success('已全部标记为已读')
  loadData()
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.content-card { background: #fff; border-radius: 12px; padding: 24px; }
.notify-tabs { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.notify-list { display: flex; flex-direction: column; }
.notify-item {
  display: flex; align-items: flex-start; gap: 14px; padding: 16px;
  border-bottom: 1px solid #f5f5f5; cursor: pointer; transition: background 0.2s; position: relative;
}
.notify-item:hover { background: #fafafa; }
.notify-item.unread { background: #fffbe6; }
.notify-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.notify-content { flex: 1; min-width: 0; }
.notify-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.notify-title { font-size: 15px; font-weight: 500; color: #333; }
.notify-time { font-size: 12px; color: #999; white-space: nowrap; }
.notify-desc { font-size: 13px; color: #666; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.unread-dot { width: 8px; height: 8px; background: #ff4d4f; border-radius: 50%; position: absolute; top: 18px; right: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
@media (max-width: 768px) { .page-container { padding: 16px; } .content-card { padding: 16px; } .notify-tabs { flex-direction: column; gap: 12px; } }
</style>
