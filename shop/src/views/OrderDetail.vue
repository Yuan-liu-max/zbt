<template>
  <div class="order-detail page-container--no-tabbar" v-if="order">
    <van-nav-bar title="订单详情" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- 订单状态 -->
    <div class="status-card">
      <van-icon :name="statusIcon(order.orderStatus)" size="28" color="#c8a44d" />
      <p class="text-lg" style="font-weight:700;margin:8px 0">{{ orderStatusText(order.orderStatus) }}</p>
      <p class="text-hint text-xs" v-if="order.orderStatus === 'PENDING_PAY'">请尽快完成支付</p>
      <p class="text-hint text-xs" v-else-if="order.orderStatus === 'SHIPPED'">商品正在配送中</p>
    </div>

    <!-- 订单信息 -->
    <div class="card">
      <div class="flex-between"><span class="text-secondary">订单号</span><span>{{ order.orderCode }}</span></div>
      <div class="flex-between mt-sm"><span class="text-secondary">下单时间</span><span class="text-sm">{{ order.createdAt }}</span></div>
      <div class="flex-between mt-sm" v-if="order.paymentMethod"><span class="text-secondary">支付方式</span><span>{{ order.paymentMethod }}</span></div>
      <div class="flex-between mt-sm" v-if="order.deliveryMethod"><span class="text-secondary">配送方式</span><span>{{ order.deliveryMethod === 'EXPRESS' ? '快递配送' : '门店自提' }}</span></div>
      <div class="flex-between mt-sm" v-if="order.remark"><span class="text-secondary">留言</span><span>{{ order.remark }}</span></div>
    </div>

    <!-- 收货信息 -->
    <div class="card" v-if="order.customerName">
      <p class="text-sm text-secondary mb-sm">收货信息</p>
      <p class="text-sm">{{ order.customerName }} {{ order.customerPhone }}</p>
      <p class="text-xs text-hint mt-xs">{{ order.customerAddress }}</p>
    </div>

    <!-- 商品明细 -->
    <div class="card">
      <p class="text-sm text-secondary mb-sm">商品明细</p>
      <div v-for="it in (order.items || [])" :key="it.productId" class="flex" style="gap:12px;margin:8px 0">
        <van-image :src="it.imageUrl || '/logo.png'" width="60" height="60" fit="cover" radius="6" />
        <div class="flex-1">
          <p class="text-sm">{{ it.productName }}</p>
          <p class="text-xs text-hint">x{{ it.quantity }}  ¥{{ it.price }}</p>
        </div>
      </div>
    </div>

    <!-- 价格明细 -->
    <div class="card">
      <div class="flex-between"><span class="text-secondary">商品总额</span><span>¥{{ order.totalAmount }}</span></div>
      <div class="flex-between mt-sm" v-if="order.freight"><span class="text-secondary">运费</span><span>¥{{ order.freight }}</span></div>
      <div class="flex-between mt-sm" v-if="order.couponDiscount"><span class="text-secondary">优惠</span><span>-¥{{ order.couponDiscount }}</span></div>
      <div class="flex-between mt-sm"><span class="text-secondary">实付款</span><span class="price-current" style="font-size:18px">¥{{ order.orderAmount }}</span></div>
    </div>

    <!-- 物流信息 -->
    <div class="card" v-if="order.deliveryCompany || order.deliveryTrackNo">
      <p class="text-sm text-secondary mb-sm">物流信息</p>
      <p class="text-sm">{{ order.deliveryCompany || '快递' }}</p>
      <p class="text-xs text-hint mt-xs">单号：{{ order.deliveryTrackNo || '-' }}</p>
    </div>

    <!-- 操作日志 -->
    <div class="card" v-if="(order.logs || []).length">
      <p class="text-sm text-secondary mb-sm">订单日志</p>
      <van-steps direction="vertical" :active="(order.logs || []).length - 1">
        <van-step v-for="log in (order.logs || [])" :key="log.time">
          <p class="text-sm">{{ log.content }}</p>
          <p class="text-xs text-hint">{{ log.time }}</p>
        </van-step>
      </van-steps>
    </div>

    <!-- 操作按钮 -->
    <div class="flex-center" style="margin:24px;gap:12px;flex-wrap:wrap">
      <van-button type="danger" v-if="order.orderStatus === 'PENDING_PAY'" @click="cancelOrder">取消订单</van-button>
      <van-button type="primary" v-if="order.orderStatus === 'PENDING_PAY'" @click="payOrder">立即支付</van-button>
      <van-button type="primary" v-if="order.orderStatus === 'SHIPPED'" @click="confirmReceive">确认收货</van-button>
      <van-button type="warning" v-if="['PAID','SHIPPED','RECEIVED','FINISHED'].includes(order.orderStatus)" @click="goReturn">申请退款</van-button>
    </div>
  </div>
  <van-empty v-else description="订单不存在" />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { shopOrderApi } from '@/api/services'
import type { OrderRecord } from '@/types'

const route = useRoute()
const router = useRouter()
const order = ref<OrderRecord | null>(null)

function orderStatusText(s: string) {
  const m: Record<string, string> = {
    PENDING_PAY: '待付款', PAID: '已付款', SHIPPED: '已发货',
    RECEIVED: '已签收', FINISHED: '已完成', CANCELLED: '已取消',
    completed: '已完成', pending: '待付款', paid: '已付款', shipped: '已发货',
    refund: '退款/售后'
  }
  return m[s] || s || '未知'
}

function statusIcon(s: string): string {
  const icons: Record<string, string> = {
    PENDING_PAY: 'clock-o', PAID: 'checked', SHIPPED: 'logistics',
    RECEIVED: 'passed', FINISHED: 'passed', CANCELLED: 'close'
  }
  return icons[s] || 'info-o'
}

async function cancelOrder() {
  try {
    await showConfirmDialog({ title: '确认取消订单？' })
    await shopOrderApi.cancel(route.params.id as string)
    showToast('已取消')
    if (order.value) order.value.orderStatus = 'CANCELLED'
  } catch { /* 取消 */ }
}

async function payOrder() {
  try {
    await shopOrderApi.pay(route.params.id as string)
    showToast('支付成功')
    if (order.value) order.value.orderStatus = 'PAID'
  } catch { /* error handled */ }
}

async function confirmReceive() {
  try {
    await showConfirmDialog({ title: '确认已收到商品？' })
    await shopOrderApi.confirmReceive(route.params.id as string)
    showToast('已确认收货')
    if (order.value) order.value.orderStatus = 'RECEIVED'
  } catch { /* cancel */ }
}

function goReturn() {
  router.push(`/return?orderId=${order.value!.id}&amount=${order.value!.orderAmount}`)
}

onMounted(async () => {
  try {
    order.value = await shopOrderApi.detail(route.params.id as string)
  } catch { /* 错误 */ }
})
</script>

<style scoped>
.order-detail { min-height: 100vh; background: var(--bg-page); padding-bottom: 40px; }
.status-card {
  text-align: center;
  padding: 24px;
  background: var(--bg-white);
  margin-bottom: 8px;
}
.mt-sm { margin-top: 8px; }
.mb-sm { margin-bottom: 8px; }
.mt-xs { margin-top: 4px; }
</style>
