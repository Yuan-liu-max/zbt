<template>
  <div class="page-container">
    <!-- 面包屑 -->
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <router-link to="/order/list">订单管理</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>订单详情</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <a-spin :spinning="loading">
      <div v-if="order" class="detail-content">
        <!-- 订单信息 -->
        <div class="detail-card">
          <div class="card-title">订单信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">订单号</span>
              <span class="info-value">{{ order.orderCode }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">下单时间</span>
              <span class="info-value">{{ order.createdAt }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">订单状态</span>
              <a-tag :color="getOrderStatusColor(order.orderStatus)">
                {{ getOrderStatusText(order.orderStatus) }}
              </a-tag>
            </div>
            <div class="info-item">
              <span class="info-label">支付状态</span>
              <a-tag :color="getPaymentStatusColor(order.paymentStatus)">
                {{ getPaymentStatusText(order.paymentStatus) }}
              </a-tag>
            </div>
            <div class="info-item">
              <span class="info-label">支付方式</span>
              <span class="info-value">{{ order.paymentMethod }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">配送方式</span>
              <span class="info-value">{{ order.deliveryMethod }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">买家留言</span>
              <span class="info-value">{{ order.remark || '无' }}</span>
            </div>
          </div>
        </div>

        <!-- 客户信息 -->
        <div class="detail-card">
          <div class="card-title">客户信息</div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">客户姓名</span>
              <span class="info-value">{{ order.customerName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ order.customerPhone }}</span>
            </div>
            <div class="info-item full-width">
              <span class="info-label">收货地址</span>
              <span class="info-value">{{ order.customerAddress }}</span>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-card">
          <div class="card-title">商品信息</div>
          <div v-for="item in order.items" :key="item.id" class="product-item">
            <div class="product-icon">
              <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.productName" />
              <div v-else class="product-placeholder" />
            </div>
            <div class="product-info">
              <div class="product-name">{{ item.productName }}</div>
              <div class="product-spec">规格：{{ item.spec }}  编号：{{ item.productCode }}</div>
              <div class="product-qty">x{{ item.quantity }}</div>
            </div>
            <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
          </div>

          <!-- 费用明细 -->
          <div class="price-detail">
            <div class="price-row">
              <span>商品总额</span>
              <span>¥{{ order.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="price-row">
              <span>运费</span>
              <span>¥{{ order.freight.toFixed(2) }}</span>
            </div>
            <div class="price-row" v-if="order.couponDiscount > 0">
              <span>优惠券</span>
              <span class="discount">-¥{{ order.couponDiscount.toFixed(2) }}</span>
            </div>
            <div class="price-row total">
              <span>订单总额</span>
              <span class="total-price">¥{{ order.orderAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 订单操作记录 -->
        <div class="detail-card">
          <div class="card-title">订单操作记录</div>
          <a-timeline>
            <a-timeline-item v-for="(log, index) in order.logs" :key="index">
              <div class="log-time">{{ log.time }}</div>
              <div class="log-content">{{ log.content }}</div>
            </a-timeline-item>
          </a-timeline>
        </div>

        <!-- 底部操作 -->
        <div class="detail-footer" v-if="order.orderStatus === 'pending'">
          <a-space>
            <a-button @click="handleBack">取消订单</a-button>
            <a-button type="primary" @click="handlePay">立即付款</a-button>
          </a-space>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <a-empty description="订单不存在" />
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { OrderRecord, OrderStatus, PaymentStatus } from '@/types/order'
import { orderApi, orderStatusMap } from '@/api/order'

const router = useRouter()
const route = useRoute()

const order = ref<OrderRecord | null>(null)
const loading = ref(false)

// 状态颜色
const getOrderStatusColor = (status: OrderStatus) => {
  return orderStatusMap[status]?.color || 'default'
}

const getOrderStatusText = (status: OrderStatus) => {
  return orderStatusMap[status]?.text || status
}

// 支付状态
const getPaymentStatusColor = (status: PaymentStatus) => {
  const map: Record<PaymentStatus, string> = {
    unpaid: 'orange',
    paid: 'green',
    refunded: 'red'
  }
  return map[status] || 'default'
}

const getPaymentStatusText = (status: PaymentStatus) => {
  const map: Record<PaymentStatus, string> = {
    unpaid: '未支付',
    paid: '已支付',
    refunded: '已退款'
  }
  return map[status] || status
}

// 加载数据
const loadData = async () => {
  const id = route.params.id as string
  if (!id) return

  loading.value = true
  try {
    order.value = await orderApi.getById(id)
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 付款
const handlePay = () => {
  message.info('跳转到支付页面...')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.detail-content {
  max-width: 800px;
}

.detail-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  min-width: 70px;
}

.info-value {
  font-size: 13px;
  color: #333;
}

/* 商品信息 */
.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.product-item:last-of-type {
  border-bottom: none;
}

.product-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f5f5f5;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-placeholder {
  width: 32px;
  height: 32px;
  background: #e8e8e8;
  border-radius: 4px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.product-spec {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.product-qty {
  font-size: 12px;
  color: #999;
}

.product-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
  white-space: nowrap;
}

/* 费用明细 */
.price-detail {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
  color: #666;
}

.price-row.total {
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #f0f0f0;
  font-weight: 600;
  color: #333;
}

.discount {
  color: #ff4d4f;
}

.total-price {
  font-size: 16px;
  color: #ff4d4f;
}

/* 操作记录 */
.log-time {
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.log-content {
  font-size: 14px;
  color: #333;
}

/* 底部操作 */
.detail-footer {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  text-align: right;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  bottom: 0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .detail-card {
    padding: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .info-label {
    min-width: auto;
  }

  .product-icon {
    width: 50px;
    height: 50px;
  }

  .product-name {
    font-size: 13px;
  }

  .product-price {
    font-size: 14px;
  }

  .detail-footer {
    padding: 12px 16px;
  }
}

@media (max-width: 576px) {
  .page-header h2 {
    font-size: 18px;
  }

  .detail-card {
    padding: 12px;
  }

  .card-title {
    font-size: 14px;
  }
}
</style>
