<template>
  <div class="notify-page page-container--no-tabbar">
    <van-nav-bar title="消息通知" left-text="返回" left-arrow @click-left="$router.back()">
      <template #right><span class="text-sm" style="color:var(--color-primary)" @click="readAll">全部已读</span></template>
    </van-nav-bar>
    <div v-for="n in notifications" :key="n.id" class="notify-item card" @click="markOne(n.id)">
      <div class="flex-between"><span class="text-md" :style="{fontWeight:n.isRead?400:600}">{{ n.title }}</span>
        <van-badge v-if="!n.isRead" dot />
      </div>
      <p class="text-sm text-secondary mt-sm">{{ n.content }}</p>
      <p class="text-xs text-hint mt-sm">{{ n.createdAt }}</p>
    </div>
    <van-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { notificationApi } from '@/api/services'
import type { NotificationItem } from '@/types'

const notifications = ref<NotificationItem[]>([])
const loading = ref(false)

async function fetchAll() {
  loading.value = true
  try {
    const res = await notificationApi.list({ page: 1, pageSize: 50 })
    notifications.value = res.list || []
  } catch { /* 静默 */ }
  finally { loading.value = false }
}

async function markOne(id: string | number) {
  try { await notificationApi.markRead(id) }
  catch { /* 静默 */ }
  const n = notifications.value.find(x => x.id === id)
  if (n) n.isRead = 1
}

async function readAll() {
  try { await notificationApi.markAllRead() }
  catch { /* 静默 */ }
  notifications.value.forEach(n => n.isRead = 1)
  showToast('已全部标为已读')
}

onMounted(fetchAll)
</script>

<style scoped>
.notify-page { min-height: 100vh; background: var(--bg-page); }
.notify-item { margin-top: 4px; cursor: pointer; }
</style>
