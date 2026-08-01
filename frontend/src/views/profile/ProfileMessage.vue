<template>
  <div class="page-container">
    <div class="page-header">
      <h2>信息</h2>
    </div>

    <div class="content-card message-layout">
      <!-- 左侧消息列表 -->
      <div class="message-list-panel">
        <div class="list-tabs">
          <a-tabs v-model:activeKey="activeTab" size="small">
            <a-tab-pane key="all" :tab="`全部 (${allMessages.length})`" />
            <a-tab-pane key="unread" :tab="`未读 (${unreadCount})`" />
          </a-tabs>
        </div>
        <div class="message-list">
          <div v-for="msg in filteredMessages" :key="msg.id" class="message-item" :class="{ active: selectedId === msg.id, unread: !msg.isRead }" @click="handleSelect(msg)">
            <div class="msg-icon" :style="{ background: getTypeColor(msg.type) + '15', color: getTypeColor(msg.type) }">
              <component :is="getTypeIcon(msg.type)" />
            </div>
            <div class="msg-info">
              <div class="msg-title-row">
                <span class="msg-title">{{ msg.title }}</span>
                <span class="msg-time">{{ msg.time }}</span>
              </div>
              <div class="msg-summary">{{ msg.summary }}</div>
            </div>
            <div v-if="!msg.isRead" class="unread-badge"></div>
          </div>
        </div>
      </div>

      <!-- 右侧消息详情 -->
      <div class="message-detail-panel">
        <template v-if="selectedMessage">
          <div class="detail-header">
            <h3>{{ selectedMessage.title }}</h3>
            <span class="detail-time">{{ selectedMessage.time }}</span>
          </div>
          <div class="detail-content" v-html="formatContent(selectedMessage.content)"></div>
          <div v-if="selectedMessage.extra" class="detail-extra">
            <div class="extra-item">
              <span class="extra-label">到期时间：</span>
              <span class="extra-value">2024-11-09</span>
            </div>
            <div class="extra-item">
              <span class="extra-label">当前金额：</span>
              <span class="extra-value highlight">{{ selectedMessage.extra }}</span>
            </div>
            <a-button type="primary" class="action-btn">立即续费</a-button>
          </div>
        </template>
        <div v-else class="empty-detail">
          <a-empty description="选择一条消息查看详情" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UserOutlined, GlobalOutlined, CustomerServiceOutlined, RobotOutlined, SettingOutlined
} from '@ant-design/icons-vue'
import type { MessageItem } from '@/types/profile'
import { notificationApi } from '@/api/notification'

const messageTypeMap: Record<string, string> = {
  TASK_START: '任务开始', TASK_DEADLINE: '任务截止', TASK_OVERDUE: '任务超时',
  TASK_AUDIT: '任务审核', TASK_REJECTED: '已驳回', SYSTEM: '系统通知'
}

const activeTab = ref('all')
const allMessages = ref<MessageItem[]>([])
const selectedId = ref<string | null>(null)

const unreadCount = computed(() => allMessages.value.filter(m => !m.isRead).length)

const filteredMessages = computed(() => {
  if (activeTab.value === 'unread') return allMessages.value.filter(m => !m.isRead)
  return allMessages.value
})

const selectedMessage = computed(() => {
  if (!selectedId.value) return null
  return allMessages.value.find(m => m.id === selectedId.value) || null
})

const iconMap: Record<string, any> = {
  UserOutlined, GlobalOutlined, CustomerServiceOutlined, RobotOutlined, SettingOutlined
}

const getTypeIcon = () => UserOutlined
const getTypeColor = () => '#1890ff'

const formatContent = (content: string) => content.replace(/\n/g, '<br>')

const loadData = async () => {
  try {
    allMessages.value = await notificationApi.getList()
  } catch { message.error('加载失败') }
}

const handleSelect = (msg: MessageItem) => {
  selectedId.value = msg.id
  msg.isRead = true
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.content-card { background: #fff; border-radius: 12px; padding: 0; overflow: hidden; }
.message-layout { display: flex; height: calc(100vh - 160px); }

.message-list-panel { width: 360px; border-right: 1px solid #f0f0f0; display: flex; flex-direction: column; }
.list-tabs { padding: 16px 20px 0; }
.list-tabs :deep(.ant-tabs-nav) { margin-bottom: 0; }
.message-list { flex: 1; overflow-y: auto; }

.message-item {
  display: flex; align-items: flex-start; gap: 12px; padding: 14px 20px;
  cursor: pointer; transition: background 0.2s; border-bottom: 1px solid #f5f5f5; position: relative;
}
.message-item:hover { background: #fafafa; }
.message-item.active { background: #fff7e6; border-left: 3px solid #c8a44d; }
.message-item.unread { background: #fffbe6; }

.msg-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 16px; flex-shrink: 0; }
.msg-info { flex: 1; min-width: 0; }
.msg-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.msg-title { font-size: 14px; font-weight: 500; color: #333; }
.msg-time { font-size: 11px; color: #999; white-space: nowrap; }
.msg-summary { font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.unread-badge { width: 8px; height: 8px; background: #ff4d4f; border-radius: 50%; position: absolute; top: 16px; right: 12px; }

.message-detail-panel { flex: 1; padding: 24px; overflow-y: auto; display: flex; flex-direction: column; }
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.detail-header h3 { margin: 0; font-size: 18px; font-weight: 600; color: #333; }
.detail-time { font-size: 13px; color: #999; }
.detail-content { font-size: 14px; color: #333; line-height: 1.8; flex: 1; }
.detail-extra { margin-top: 24px; padding: 20px; background: #fafafa; border-radius: 8px; }
.extra-item { margin-bottom: 12px; font-size: 14px; }
.extra-label { color: #999; }
.extra-value { color: #333; }
.extra-value.highlight { color: #ff4d4f; font-weight: 600; font-size: 16px; }
.action-btn { margin-top: 12px; border-radius: 8px; background: #c8a44d; border-color: #c8a44d; }
.empty-detail { flex: 1; display: flex; align-items: center; justify-content: center; }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .message-layout { flex-direction: column; height: auto; }
  .message-list-panel { width: 100%; max-height: 300px; border-right: none; border-bottom: 1px solid #f0f0f0; }
  .message-detail-panel { min-height: 300px; }
}
</style>
