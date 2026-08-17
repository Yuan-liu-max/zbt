<template>
  <div class="ai-page page-container--no-tabbar">
    <van-nav-bar title="AI 智能导购" left-text="返回" left-arrow @click-left="$router.back()" />
    <div class="chat-area">
      <div v-for="(msg, i) in messages" :key="i" :class="['chat-bubble', msg.role === 'user' ? 'chat-bubble--user' : 'chat-bubble--ai']">
        {{ msg.content }}
      </div>
    </div>
    <div class="chat-input flex" style="padding:12px;background:var(--bg-white);position:fixed;bottom:0;left:0;right:0;max-width:var(--max-width);margin:0 auto">
      <van-field v-model="input" placeholder="输入问题，例如：推荐一款送妈妈的礼物" class="flex-1" @keyup.enter="send" />
      <van-button type="primary" size="small" @click="send" :loading="sending">发送</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'
import { aiApi } from '@/api/services'

const messages = ref<{ role: string; content: string }[]>([
  { role: 'ai', content: '您好！我是珠宝通 AI 导购助手。您可以问我：\n• 推荐适合送妈妈的首饰\n• 黄金和铂金有什么区别？\n• 我的预算5000元有什么推荐？' }
])
const input = ref('')
const sending = ref(false)

async function send() {
  const q = input.value.trim()
  if (!q) return
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  sending.value = true
  try {
    const res = await aiApi.chat(q)
    messages.value.push({ role: 'ai', content: res.reply || '抱歉，我暂时无法回答这个问题' })
  } catch {
    messages.value.push({ role: 'ai', content: '网络异常，请稍后重试' })
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.ai-page { background: var(--bg-page); }
.chat-area { padding: var(--space-md) var(--space-md) 80px; min-height: 100vh; }
.chat-bubble { max-width: 80%; padding: 10px 14px; border-radius: var(--radius-md); margin-bottom: var(--space-sm); font-size: var(--font-base); line-height: 1.6; white-space: pre-wrap; }
.chat-bubble--ai { background: var(--bg-white); align-self: flex-start; box-shadow: var(--shadow-card); }
.chat-bubble--user { background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary)); color: var(--text-white); margin-left: auto; }
</style>
