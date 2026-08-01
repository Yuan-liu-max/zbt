<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>订单列表</h2>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="订单号">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入订单号"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="客户姓名">
          <a-input
            v-model:value="searchForm.customerName"
            placeholder="请输入客户姓名"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="订单状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="全部状态"
            allow-clear
            style="width: 130px"
          >
            <a-select-option value="pending">待付款</a-select-option>
            <a-select-option value="paid">待发货</a-select-option>
            <a-select-option value="shipped">已发货</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
            <a-select-option value="cancelled">已取消</a-select-option>
            <a-select-option value="refund">退款/售后</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="下单时间">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <SearchOutlined /> 查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 状态标签页 -->
    <div class="content-card tab-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="pending" tab="待付款" />
        <a-tab-pane key="paid" tab="待发货" />
        <a-tab-pane key="shipped" tab="已发货" />
        <a-tab-pane key="completed" tab="已完成" />
        <a-tab-pane key="cancelled" tab="已取消" />
        <a-tab-pane key="refund" tab="退款/售后" />
      </a-tabs>
    </div>

    <!-- 订单列表 -->
    <div class="content-card order-list-card">
      <a-spin :spinning="loading">
        <div v-if="tableData.length === 0" class="empty-state">
          <a-empty description="暂无订单数据" />
        </div>
        <div v-else>
          <div v-for="order in tableData" :key="order.id" class="order-item">
            <!-- 订单头部 -->
            <div class="order-header">
              <div class="order-info">
                <span class="order-code">订单号：{{ order.orderCode }}</span>
                <span class="order-time">下单时间：{{ order.createdAt }}</span>
              </div>
            </div>

            <!-- 订单商品 -->
            <div class="order-body">
              <div v-for="item in order.items" :key="item.id" class="order-product">
                <div class="product-info">
                  <div class="product-icon">
                    <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.productName" />
                    <div v-else class="product-placeholder" />
                  </div>
                  <div class="product-detail">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-spec">规格：{{ item.spec }}</div>
                    <div class="product-qty">x{{ item.quantity }}</div>
                  </div>
                  <div class="product-price">
                    <span class="price-symbol">¥</span>
                    <span class="price-value">{{ item.price.toFixed(2) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 订单操作 -->
            <div class="order-footer">
              <div class="order-status">
                <a-tag :color="getOrderStatusColor(order.orderStatus)">
                  {{ getOrderStatusText(order.orderStatus) }}
                </a-tag>
              </div>
              <div class="order-actions">
                <a @click="handleDetail(order)" class="action-link">查看详情</a>
                <template v-if="order.orderStatus === 'pending'">
                  <a-divider type="vertical" />
                  <a class="action-link" @click="handleCancel(order)">取消订单</a>
                </template>
                <template v-if="order.orderStatus === 'shipped'">
                  <a-divider type="vertical" />
                  <a class="action-link" @click="handleConfirm(order)">确认收货</a>
                </template>
                <template v-if="order.orderStatus === 'completed'">
                  <a-divider type="vertical" />
                  <a class="action-link" @click="handleRepurchase(order)">再次购买</a>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="pagination.total > 0">
          <a-pagination
            v-model:current="pagination.current"
            v-model:pageSize="pagination.pageSize"
            :total="pagination.total"
            :show-size-changer="true"
            :show-quick-jumper="true"
            :show-total="(total: number) => `共 ${total} 条`"
            @change="handleTableChange"
          />
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import type { OrderRecord, OrderQueryParams, OrderStatus } from '@/types/order'
import { orderApi, orderStatusMap } from '@/api/order'

const router = useRouter()

// 当前标签页
const activeTab = ref<string>('all')

// 搜索表单
const searchForm = reactive({
  keyword: '',
  customerName: '',
  status: undefined as OrderStatus | undefined,
  dateRange: null as any
})

// 表格数据
const tableData = ref<OrderRecord[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 监听标签页切换
watch(activeTab, () => {
  pagination.current = 1
  loadData()
})

// 状态颜色
const getOrderStatusColor = (status: OrderStatus) => {
  return orderStatusMap[status]?.color || 'default'
}

// 状态文本
const getOrderStatusText = (status: OrderStatus) => {
  return orderStatusMap[status]?.text || status
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const statusFilter = activeTab.value === 'all' ? searchForm.status : activeTab.value as OrderStatus
    const params: OrderQueryParams = {
      keyword: searchForm.keyword || undefined,
      status: statusFilter,
      startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
      endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await orderApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.customerName = ''
  searchForm.status = undefined
  searchForm.dateRange = null
  activeTab.value = 'all'
  handleSearch()
}

// 分页
const handleTableChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadData()
}

// 标签页切换
const handleTabChange = () => {
  pagination.current = 1
  loadData()
}

// 详情
const handleDetail = (order: OrderRecord) => {
  router.push(`/order/detail/${order.id}`)
}

// 取消订单
const handleCancel = async (order: OrderRecord) => {
  try {
    await orderApi.cancel(order.id)
    message.success('订单已取消')
    loadData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 确认收货
const handleConfirm = async (order: OrderRecord) => {
  try {
    await orderApi.update(order.id, { orderStatus: 'completed' })
    message.success('已确认收货')
    loadData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 再次购买
const handleRepurchase = (order: OrderRecord) => {
  router.push('/goods/list')
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.search-card {
  padding: 16px 24px;
}

.search-card :deep(.ant-form) {
  flex-wrap: wrap;
}

.search-card :deep(.ant-form-item) {
  margin-bottom: 12px;
  margin-right: 0;
}

.tab-card {
  padding: 0 24px;
}

.tab-card :deep(.ant-tabs) {
  margin-bottom: 0;
}

.tab-card :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

/* 订单列表卡片 */
.order-list-card {
  padding: 16px;
}

.order-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.order-header {
  background: #fafafa;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 13px;
  color: #666;
}

.order-code {
  font-weight: 500;
  color: #333;
}

.order-body {
  padding: 16px;
}

.order-product {
  padding: 8px 0;
}

.order-product + .order-product {
  border-top: 1px dashed #f0f0f0;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
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

.product-detail {
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

.price-symbol {
  font-size: 12px;
}

.price-value {
  font-size: 16px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.action-link {
  font-size: 13px;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
  cursor: pointer;
}

.action-link:hover {
  color: #40a9ff;
  background: #e6f7ff;
}

.empty-state {
  padding: 40px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .search-card {
    padding: 12px 16px;
  }

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .tab-card {
    padding: 0 16px;
  }

  .product-info {
    gap: 8px;
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

  .order-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-header h2 {
    font-size: 18px;
  }

  .order-header {
    padding: 10px 12px;
  }

  .order-body {
    padding: 12px;
  }

  .order-footer {
    padding: 10px 12px;
  }
}
</style>
