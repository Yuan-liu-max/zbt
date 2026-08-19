<template>
  <div class="coupons-page page-container--no-tabbar">
    <van-nav-bar title="我的优惠券" left-text="返回" left-arrow @click-left="$router.back()" />

    <van-tabs v-model:active="activeTab" @change="loadData">
      <van-tab title="可使用" name="UNUSED" />
      <van-tab title="已使用" name="USED" />
      <van-tab title="已过期" name="EXPIRED" />
    </van-tabs>

    <div v-for="c in coupons" :key="c.id" class="coupon-card" :class="{ disabled: c.status !== 'UNUSED' }">
      <div class="coupon-card__amount">
        <template v-if="c.type === 'discount'">
          <span class="amount-num">{{ c.discountValue ?? 9 }}</span><span class="amount-suffix">折</span>
        </template>
        <template v-else>
          <span class="amount-prefix">¥</span><span class="amount-num">{{ c.discountValue ?? 0 }}</span>
        </template>
      </div>
      <div class="coupon-card__info">
        <p class="coupon-name">{{ c.name }}</p>
        <p class="coupon-method">{{ c.discountMethod }}</p>
        <p class="coupon-threshold" v-if="c.threshold > 0">满 {{ c.threshold }} 元可用</p>
        <p class="coupon-expire" v-if="c.expireTime">有效期至 {{ formatTime(c.expireTime) }}</p>
      </div>
      <van-tag :type="statusTag(c.status)">{{ statusText(c.status) }}</van-tag>
    </div>
    <van-empty v-if="!loading && coupons.length === 0" description="暂无优惠券" />

    <div style="height: 30px" />
    <div style="padding: var(--space-md)">
      <van-button round block plain type="primary" icon="coupon-o" @click="$router.push('/promotions')">去领券中心</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { couponApi, type CouponItem } from '@/api/coupon'

const activeTab = ref('UNUSED')
const coupons = ref<CouponItem[]>([])
const loading = ref(false)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await couponApi.mine({ page: 1, pageSize: 50, status: activeTab.value })
    coupons.value = res.list || []
  } catch { /* 静默 */ } finally {
    loading.value = false
  }
}

function statusText(s: string): string {
  return { UNUSED: '可使用', USED: '已使用', EXPIRED: '已过期', DISABLED: '已失效' }[s] || s
}

function statusTag(s: string): 'primary' | 'default' | 'danger' {
  return s === 'UNUSED' ? 'primary' : s === 'USED' ? 'default' : 'danger'
}

function formatTime(t?: string): string {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped>
.coupons-page { min-height: 100vh; background: var(--bg-page); padding-bottom: 60px; }
.coupon-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin: var(--space-sm) var(--space-md) 0;
  padding: var(--space-md);
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.coupon-card.disabled { opacity: 0.55; }
.coupon-card__amount {
  width: 72px;
  text-align: center;
  color: var(--color-primary, #c8a44d);
  flex-shrink: 0;
}
.amount-prefix { font-size: 14px; font-weight: 600; }
.amount-num { font-size: 26px; font-weight: 700; }
.amount-suffix { font-size: 14px; font-weight: 600; }
.coupon-card__info { flex: 1; min-width: 0; }
.coupon-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.coupon-method { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.coupon-threshold { font-size: 12px; color: var(--color-primary, #c8a44d); margin-top: 4px; }
.coupon-expire { font-size: 11px; color: var(--text-hint); margin-top: 2px; }
</style>
