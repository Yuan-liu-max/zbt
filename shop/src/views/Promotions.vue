<template>
  <div class="promo-page page-container--no-tabbar">
    <van-nav-bar title="营销活动" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- 优惠券入口 -->
    <div class="coupon-entry" @click="goMyCoupons">
      <van-icon name="coupon-o" color="#c8a44d" size="18" />
      <span class="text-sm flex-1">我的优惠券</span>
      <van-tag v-if="unusedCount > 0" type="warning" round>{{ unusedCount }} 张可用</van-tag>
      <van-icon name="arrow" color="#999" size="14" />
    </div>

    <!-- 可领取列表 -->
    <div v-for="p in coupons" :key="p.promotionId" class="promo-card card">
      <div class="promo-card__main">
        <div class="promo-card__left">
          <p class="text-md" style="font-weight:600">{{ p.name }}</p>
          <p class="text-sm text-secondary mt-sm">{{ p.discountMethod || '限时优惠' }}</p>
          <p class="text-xs text-hint mt-sm">{{ p.startTime }} ~ {{ p.endTime }}</p>
          <div class="flex-between mt-sm">
            <van-tag type="warning">{{ p.status?.toUpperCase() === 'ONGOING' ? '进行中' : '即将开始' }}</van-tag>
          </div>
        </div>
        <div class="promo-card__right">
          <van-button
            v-if="!p.claimed"
            round
            size="small"
            type="primary"
            :loading="claimingId === p.promotionId"
            @click="claim(p.promotionId)"
          >领取</van-button>
          <van-tag v-else type="default">已领取</van-tag>
        </div>
      </div>
    </div>
    <van-empty v-if="coupons.length === 0 && !loading" description="暂无活动" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import { couponApi } from '@/api/coupon'

const router = useRouter()
const coupons = ref<any[]>([])
const unusedCount = ref(0)
const loading = ref(false)
const claimingId = ref<number | null>(null)

onMounted(async () => {
  await loadAvailable()
  loadUnusedCount()
})

async function loadAvailable() {
  loading.value = true
  try {
    coupons.value = await couponApi.available()
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

async function loadUnusedCount() {
  try {
    const res = await couponApi.unusedCount()
    unusedCount.value = res?.count || 0
  } catch { /* 非关键 */ }
}

async function claim(promotionId: number) {
  claimingId.value = promotionId
  try {
    await couponApi.receive(promotionId)
    showSuccessToast('领取成功')
    await loadAvailable()
    await loadUnusedCount()
  } catch (e: any) {
    showToast(e?.message || '领取失败')
  } finally {
    claimingId.value = null
  }
}

function goMyCoupons() {
  router.push('/coupons')
}
</script>

<style scoped>
.promo-page { min-height: 100vh; background: var(--bg-page); padding-bottom: 40px; }
.promo-card { margin-top: var(--space-sm); }
.promo-card__main { display: flex; align-items: center; gap: var(--space-md); }
.promo-card__left { flex: 1; }
.promo-card__right { flex-shrink: 0; }
.coupon-entry {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin: var(--space-sm) var(--space-md) 0;
  padding: var(--space-md);
  background: #fff;
  border-radius: var(--radius-lg);
}
</style>
