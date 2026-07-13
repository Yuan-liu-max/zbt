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
          <a class="clear-link" @click="handleClear">清空</a>
        </div>
        <div class="history-list">
          <div v-for="item in history" :key="item.id" class="history-item" @click="handleHistoryClick(item)">
            <div class="history-title">{{ item.title }}</div>
            <div class="history-time">{{ item.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { SendOutlined } from '@ant-design/icons-vue'
import type { ChatMessage } from '@/types/ai-tools'
import { mockChatMessages } from '@/api/mock/ai-tools'

const messages = ref<ChatMessage[]>([...mockChatMessages])
const inputText = ref('')
const messagesRef = ref<HTMLElement>()

const quickQuestions = [
  '如何创建销售新客户？', '供应商资质审核流程是怎样的？', '最近的销售数据是怎样的？'
]

const history = ref([
  { id: '1', title: '如何创建销售客户？', time: '07-10 10:30' },
  { id: '2', title: '供应商资质审核流程是怎样的？', time: '07-10 09:15' },
  { id: '3', title: '最近的销售数据是怎样的？', time: '07-09 16:45' },
  { id: '4', title: '如何生成销售报表？', time: '07-09 14:20' },
  { id: '5', title: '库存管理规则怎么设置？', time: '07-08 11:30' },
])

const formatMessage = (content: string) => {
  return content.replace(/\n/g, '<br>')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text) return

  messages.value.push({
    id: String(Date.now()),
    role: 'user',
    content: text,
    time: new Date().toTimeString().slice(0, 5)
  })
  inputText.value = ''
  scrollToBottom()

  setTimeout(() => {
    messages.value.push({
      id: String(Date.now()),
      role: 'assistant',
      content: '正在分析您的问题，请稍候...\n\n根据您的描述，我建议您：\n\n1. 首先检查相关数据\n2. 分析当前业务状况\n3. 制定改进方案\n\n如需更详细的分析，请告诉我具体需求。',
      time: new Date().toTimeString().slice(0, 5)
    })
    scrollToBottom()
  }, 1000)
}

const handleQuickQuestion = (q: string) => {
  inputText.value = q
  handleSend()
}

const handleClear = () => {
  messages.value = [mockChatMessages[0]]
  message.success('已清空对话')
}

const handleHistoryClick = (item: any) => {
  message.info(`查看历史：${item.title}`)
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

.history-item {
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover { background: #fafafa; }

.history-title { font-size: 13px; color: #333; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.history-time { font-size: 11px; color: #999; }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .chat-layout { flex-direction: column; height: auto; }
  .chat-sidebar { width: 100%; max-height: 200px; }
  .chat-main { min-height: 400px; }
}
</style>
