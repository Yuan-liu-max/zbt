<template>
  <div class="promo-page page-container--no-tabbar">
    <van-nav-bar title="营销活动" left-text="返回" left-arrow @click-left="$router.back()" />
    <div v-for="p in promotions" :key="p.id" class="promo-card card">
      <p class="text-md" style="font-weight:600">{{ p.name }}</p>
      <p class="text-sm text-secondary mt-sm">{{ p.discountMethod || '限时优惠' }}</p>
      <div class="flex-between mt-sm"><span class="text-xs text-hint">{{ p.startTime }} ~ {{ p.endTime }}</span>
        <van-tag type="warning">{{ p.status?.toUpperCase() === 'ONGOING' ? '进行中' : '即将开始' }}</van-tag>
      </div>
    </div>
    <van-empty v-if="promotions.length === 0" description="暂无活动" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { marketingApi } from '@/api/services'
import type { PromotionItem } from '@/types'

const promotions = ref<PromotionItem[]>([])

onMounted(async () => {
  try {
    const res = await marketingApi.getPromotions({ page: 1, pageSize: 20 })
    promotions.value = res.list || []
  } catch { /* 静默 */ }
})
</script>

<style scoped>
.promo-page { min-height: 100vh; background: var(--bg-page); }
.promo-card { margin-top: var(--space-sm); }
</style>
