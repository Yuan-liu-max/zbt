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
        <div v-for="item in notifications" :key="item.id" class="notify-item" :class="{ unread: item.isRead !== 1 }" @click="handleClick(item)">
          <div class="notify-icon" :style="{ background: getTypeColor(item.notificationType) + '15', color: getTypeColor(item.notificationType) }">
            <component :is="getTypeIcon(item.notificationType)" />
          </div>
          <div class="notify-content">
            <div class="notify-title-row">
              <span class="notify-title">{{ item.title }}</span>
              <span class="notify-time">{{ item.createdAt }}</span>
            </div>
            <div class="notify-desc">{{ item.content }}</div>
          </div>
          <div v-if="item.isRead !== 1" class="unread-dot"></div>
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

    <!-- 通知详情弹窗 -->
    <a-modal v-model:open="detailVisible" :title="detailItem?.title || '通知详情'" :footer="null" width="520px">
      <template v-if="detailItem">
        <div class="detail-meta">
          <a-tag :color="getTypeColor(detailItem.notificationType)">{{ getTypeText(detailItem.notificationType) }}</a-tag>
          <span class="detail-time">{{ detailItem.createdAt }}</span>
        </div>
        <div class="detail-body">{{ detailItem.content }}</div>
        <div v-if="detailRoute" class="detail-action">
          <a-button type="primary" @click="handleGoDetail">前往查看</a-button>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, type Component } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  CheckCircleOutlined, BellOutlined,
  ScheduleOutlined, ClockCircleOutlined, AuditOutlined, RobotOutlined, SoundOutlined,
} from '@ant-design/icons-vue'
import type { NotificationItem } from '@/types/profile'
import { notificationApi } from '@/api/notification'

const router = useRouter()

const activeTab = ref('all')
const notifications = ref<NotificationItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 各状态计数（来自后端 /notifications/count，不受当前分页影响）
const totalCount = ref(0)
const unreadCount = ref(0)
const readCount = ref(0)

// 通知类型图标/颜色/文案映射
const typeMetaMap: Record<string, { icon: Component; color: string; text: string }> = {
  TASK_REMIND: { icon: ScheduleOutlined, color: '#1890ff', text: '任务提醒' },
  OVERDUE: { icon: ClockCircleOutlined, color: '#ff4d4f', text: '任务超时' },
  AUDIT: { icon: AuditOutlined, color: '#fa8c16', text: '审核通知' },
  AI_ADVICE: { icon: RobotOutlined, color: '#722ed1', text: 'AI建议' },
  HQ_NOTICE: { icon: SoundOutlined, color: '#faad14', text: '总部公告' },
}

const getTypeIcon = (type?: string): Component => typeMetaMap[type || '']?.icon || BellOutlined
const getTypeColor = (type?: string): string => typeMetaMap[type || '']?.color || '#1890ff'
const getTypeText = (type?: string): string => typeMetaMap[type || '']?.text || '通知'

// 详情弹窗
const detailVisible = ref(false)
const detailItem = ref<NotificationItem | null>(null)
const detailRoute = computed(() => (detailItem.value ? getTargetRoute(detailItem.value) : null))

// 根据 businessType / notificationType 映射跳转路由
const getTargetRoute = (item: NotificationItem): string | null => {
  const type = item.businessType || item.notificationType || ''
  if (['TASK', 'TASK_REMIND', 'OVERDUE', 'AUDIT'].includes(type)) return '/task/list'
  if (['ABNORMAL', 'SCENE', 'SCENARIO'].includes(type)) return '/scenario'
  return null
}

const loadCounts = async () => {
  try {
    const res = await notificationApi.getCount()
    totalCount.value = res.total ?? 0
    unreadCount.value = res.unread ?? 0
    readCount.value = res.read ?? 0
  } catch { /* 静默失败，不影响列表 */ }
}

const loadData = async () => {
  try {
    const isRead = activeTab.value === 'all' ? undefined : activeTab.value === 'unread' ? 0 : 1
    const res = await notificationApi.getList({ page: currentPage.value, pageSize: pageSize.value, isRead })
    notifications.value = res.list || []
    total.value = res.total || 0
  } catch { message.error('加载失败') }
}

const handleTabChange = () => { currentPage.value = 1; loadData() }
const handlePageChange = (page: number) => { currentPage.value = page; loadData() }

const handleClick = async (item: NotificationItem) => {
  if (item.isRead !== 1) {
    item.isRead = 1
    try {
      await notificationApi.markAsRead(item.id)
    } catch { message.error('标记已读失败') }
    loadCounts()
  }
  detailItem.value = item
  detailVisible.value = true
}

const handleGoDetail = () => {
  const route = detailRoute.value
  if (route) {
    detailVisible.value = false
    router.push(route)
  }
}

const handleMarkAllRead = async () => {
  await notificationApi.markAllRead()
  message.success('已全部标记为已读')
  loadData()
  loadCounts()
}

onMounted(() => { loadData(); loadCounts() })
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
.detail-meta { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.detail-time { font-size: 12px; color: #999; }
.detail-body { font-size: 14px; color: #333; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }
.detail-action { margin-top: 20px; text-align: right; }
@media (max-width: 768px) { .page-container { padding: 16px; } .content-card { padding: 16px; } .notify-tabs { flex-direction: column; gap: 12px; } }
</style>
