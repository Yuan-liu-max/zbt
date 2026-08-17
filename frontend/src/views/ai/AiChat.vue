<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>智能问答</h2>
        <p class="page-desc">快速解答业务问题，提供精准信息和建议</p>
      </div>
    </div>

    <div class="chat-layout">
      <!-- 聊天区域 -->
      <div class="chat-main">
        <div class="chat-messages" ref="messagesRef">
          <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.role">
            <div class="message-avatar">
              <div v-if="msg.role === 'assistant'" class="avatar ai">AI</div>
              <div v-else class="avatar user">我</div>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="sending" class="message assistant">
            <div class="message-avatar"><div class="avatar ai">AI</div></div>
            <div class="message-content">
              <div class="message-text"><a-spin size="small" /> AI 思考中...</div>
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div class="quick-questions">
          <a-tag v-for="q in quickQuestions" :key="q" class="quick-tag" @click="handleQuickQuestion(q)">
            {{ q }}
          </a-tag>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <a-input
            v-model:value="inputText"
            placeholder="输入您的问题，按 Enter 发送"
            @pressEnter="handleSend"
            allow-clear
          >
            <template #suffix>
              <a-button type="primary" shape="circle" @click="handleSend" :disabled="!inputText.trim()">
                <SendOutlined />
              </a-button>
            </template>
          </a-input>
        </div>
      </div>

      <!-- 历史记录 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <span>历史记录</span>
          <a-popconfirm title="确定要清空所有历史记录吗？" @confirm="handleClear">
            <a class="clear-link">清空</a>
          </a-popconfirm>
        </div>
        <div class="history-list">
          <div v-for="item in pagedHistory" :key="item.id" class="history-item" @click="handleHistoryClick(item)">
            <div class="history-title">{{ item.title }}</div>
            <div class="history-time">{{ item.time }}</div>
          </div>
        </div>
        <a-pagination
          v-if="history.length > historyPageSize"
          class="history-pagination"
          size="small"
          simple
          :current="historyPage"
          :page-size="historyPageSize"
          :total="history.length"
          @change="handleHistoryPageChange"
        />
      </div>
    </div>

    <!-- 历史详情 -->
    <a-modal v-model:open="historyVisible" :title="currentHistory?.title || '历史详情'" :footer="null" width="640px">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="时间">{{ currentHistory?.createdAt ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="模型">{{ currentHistory?.modelName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="问题">
          <div class="history-detail-content">{{ currentHistory?.question || '-' }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="回答">
          <div class="history-detail-content">{{ currentHistory?.answer || '（暂无内容）' }}</div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SendOutlined } from '@ant-design/icons-vue'
import type { ChatMessage } from '@/types/ai-tools'
import { aiApi } from '@/api/ai'

const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const sending = ref(false)
const messagesRef = ref<HTMLElement>()

const quickQuestions = [
  '如何创建销售新客户？', '供应商资质审核流程是怎样的？', '最近的销售数据是怎样的？'
]

interface HistoryItem {
  id: string
  title: string
  time: string
  question: string
  answer: string
  modelName?: string
  createdAt?: string
}
const history = ref<HistoryItem[]>([])
const historyPageSize = 8
const historyPage = ref(1)
const pagedHistory = computed(() => {
  const start = (historyPage.value - 1) * historyPageSize
  return history.value.slice(start, start + historyPageSize)
})

const formatMessage = (content: string) => {
  const escaped = content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
  return escaped.replace(/\n/g, '<br>')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const loadHistory = async () => {
  try {
    const results = await aiApi.getChatHistory()
    history.value = results.slice(0, 20).map(r => ({
      id: String(r.id),
      title: r.question ? r.question.slice(0, 30) : '（空问题）',
      time: (r.createdAt || '').slice(5, 16),
      question: r.question || '',
      answer: r.answer || '',
      modelName: r.modelName,
      createdAt: r.createdAt
    }))
  } catch (e) {
    // 历史记录加载失败不阻塞聊天
  }
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  messages.value.push({
    id: String(Date.now()),
    role: 'user',
    content: text,
    time: new Date().toTimeString().slice(0, 5)
  })
  inputText.value = ''
  scrollToBottom()
  sending.value = true

  try {
    const reply = await aiApi.chat(text)
    messages.value.push({
      id: String(Date.now()),
      role: 'assistant',
      content: reply.reply,
      time: new Date().toTimeString().slice(0, 5)
    })
    loadHistory()
  } catch (e) {
    messages.value.push({
      id: String(Date.now()),
      role: 'assistant',
      content: '抱歉，AI 服务暂时不可用，请稍后重试。',
      time: new Date().toTimeString().slice(0, 5)
    })
  } finally {
    sending.value = false
    scrollToBottom()
  }
}

const handleQuickQuestion = (q: string) => {
  inputText.value = q
  handleSend()
}

const handleClear = async () => {
  messages.value = []
  history.value = []
  historyPage.value = 1
  inputText.value = ''
  try { await aiApi.clearChatHistory() } catch (e) { /* 忽略清空失败 */ }
  message.success('已清空历史记录')
}

const handleHistoryPageChange = (page: number) => {
  historyPage.value = page
}

onMounted(() => {
  loadHistory()
})


const historyVisible = ref(false)
const currentHistory = ref<HistoryItem | null>(null)
const handleHistoryClick = (item: HistoryItem) => {
  currentHistory.value = item
  historyVisible.value = true
}
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-desc { margin: 4px 0 0; font-size: 13px; color: #999; }

.chat-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 180px);
}

.chat-main {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.avatar.ai { background: linear-gradient(135deg, #c8a44d, #e8d59a); color: #fff; }
.avatar.user { background: #1890ff; color: #fff; }

.message-content {
  max-width: 70%;
}

.message.user .message-content { text-align: right; }

.message-text {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.message.user .message-text { background: #1890ff; color: #fff; }

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.quick-questions {
  padding: 12px 24px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-tag {
  cursor: pointer;
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
}

.quick-tag:hover { background: #e6f7ff; color: #1890ff; border-color: #1890ff; }

.chat-input {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.chat-sidebar {
  width: 280px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  font-size: 15px;
}

.clear-link { font-size: 13px; color: #999; cursor: pointer; }
.clear-link:hover { color: #c8a44d; }

.history-list { flex: 1; overflow-y: auto; padding: 8px 0; }
.history-pagination { padding: 8px 12px; text-align: center; }

.history-item {
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover { background: #fafafa; }

.history-title { font-size: 13px; color: #333; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.history-time { font-size: 11px; color: #999; }

.history-detail-content {
  white-space: pre-wrap;
  max-height: 360px;
  overflow-y: auto;
  line-height: 1.7;
  color: #333;
}

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .chat-layout { flex-direction: column; height: auto; }
  .chat-sidebar { width: 100%; max-height: 200px; }
  .chat-main { min-height: 400px; }
}
</style>
