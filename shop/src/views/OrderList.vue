<template>
  <div class="order-page page-container">
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.back()" />

    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="全部" name="" />
      <van-tab title="待付款" name="PENDING_PAY" />
      <van-tab title="待发货" name="PAID" />
      <van-tab title="待收货" name="SHIPPED" />
      <van-tab title="已完成" name="RECEIVED" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="fetchOrders"
      >
        <div v-for="order in orders" :key="order.id" class="order-card card" @click="goDetail(order.id)">
          <div class="flex-between mb-sm">
            <span class="text-sm text-secondary">订单号：{{ order.orderCode }}</span>
            <van-tag :type="(statusTagType(order.orderStatus) as any)" size="medium">
              {{ orderStatusText(order.orderStatus) }}
            </van-tag>
          </div>
          <div v-for="it in (order.items || [])" :key="it.productId" class="order-item flex" style="gap:12px;margin:8px 0">
            <van-image :src="it.imageUrl || '/logo.png'" width="60" height="60" fit="cover" radius="6" />
            <div class="flex-1">
              <p class="text-sm text-ellipsis">{{ it.productName }}</p>
              <p class="text-xs text-hint">x{{ it.quantity }}</p>
              <p class="price-current" style="font-size:14px">¥{{ it.price }}</p>
            </div>
          </div>
          <div class="flex-between mt-sm">
            <span class="text-sm text-secondary">{{ formatDate(order.createdAt) }}</span>
            <span class="text-md" style="font-weight:600">共 ¥{{ order.orderAmount }}</span>
          </div>
          <div class="flex" style="justify-content:flex-end;gap:8px;margin-top:8px" @click.stop>
            <van-button size="small" plain type="default" @click="goDetail(order.id)">详情</van-button>
            <van-button size="small" type="danger" v-if="order.orderStatus === 'PENDING_PAY'" @click="cancelOrder(order.id)">取消</van-button>
            <van-button size="small" type="primary" v-if="order.orderStatus === 'PENDING_PAY'" @click="goPay(order.id)">支付</van-button>
            <van-button size="small" type="primary" v-if="order.orderStatus === 'SHIPPED'" @click="confirmReceive(order.id)">确认收货</van-button>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-empty v-if="!loading && orders.length === 0" description="暂无订单">
      <van-button type="primary" to="/home" round>去逛逛</van-button>
    </van-empty>

    <!-- TabBar -->
    <van-tabbar v-model="tabIdx" :active-color="'#c8a44d'" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/category">分类</van-tabbar-item>
      <van-tabbar-item icon="cart-o" to="/cart">购物车</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { shopOrderApi } from '@/api/services'
import type { OrderRecord } from '@/types'

const router = useRouter()
const orders = ref<OrderRecord[]>([])
const activeTab = ref('')
const tabIdx = ref(3)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)

function orderStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING_PAY: '待付款', PAID: '已付款', SHIPPED: '已发货',
    RECEIVED: '已签收', FINISHED: '已完成', CANCELLED: '已取消',
    completed: '已完成', pending: '待付款', paid: '已付款', shipped: '已发货',
    refund: '退款/售后'
  }
  return map[status] || status
}
function statusTagType(status: string) {
  const map: Record<string, string> = {
    PENDING_PAY: 'warning', PAID: 'primary', SHIPPED: 'primary',
    RECEIVED: 'success', FINISHED: 'success', CANCELLED: 'default'
  }
  return map[status] || 'default'
}
function formatDate(d: string) { return d ? d.substring(0, 10) : '-' }

async function fetchOrders() {
  loading.value = true
  try {
    const res = await shopOrderApi.list({
      page: page.value,
      pageSize: 10,
      status: activeTab.value || undefined
    })
    if (page.value === 1) {
      orders.value = res.list || []
    } else {
      orders.value.push(...(res.list || []))
    }
    finished.value = (res.list || []).length < 10
  } catch {
    // 静默处理
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onTabChange() {
  page.value = 1
  finished.value = false
  orders.value = []
  fetchOrders()
}

function onRefresh() {
  page.value = 1
  finished.value = false
  fetchOrders()
}

function goDetail(id: string | number) {
  router.push(`/order/${id}`)
}

async function cancelOrder(id: string | number) {
  try {
    await showConfirmDialog({ title: '确认取消订单？' })
    await shopOrderApi.cancel(id)
    showToast('已取消')
    onRefresh()
  } catch { /* 用户取消或网络错误 */ }
}

async function goPay(id: string | number) {
  try {
    await shopOrderApi.pay(id)
    showToast('支付成功')
    onRefresh()
  } catch { /* 错误已在拦截器处理 */ }
}

async function confirmReceive(id: string | number) {
  try {
    await showConfirmDialog({ title: '确认已收到商品？' })
    await shopOrderApi.confirmReceive(id)
    showToast('已确认收货')
    onRefresh()
  } catch { /* 用户取消或网络错误 */ }
}
</script>

<style scoped>
.order-page { padding-bottom: 80px; }
.order-card { margin-top: var(--space-sm); cursor: pointer; }
.order-item { padding: 4px 0; }
</style>
