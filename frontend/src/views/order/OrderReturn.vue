<template>
  <div class="page-container">
    <!-- 面包屑 -->
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <router-link to="/order/list">订单管理</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>退换货</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="申请单号">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入申请单号"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="订单号">
          <a-input
            v-model:value="searchForm.orderKeyword"
            placeholder="请输入订单号"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="申请类型">
          <a-select
            v-model:value="searchForm.returnType"
            placeholder="全部类型"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="refund">退货退款</a-select-option>
            <a-select-option value="exchange">换货</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="申请状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="全部状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="applying">申请中</a-select-option>
            <a-select-option value="reviewing">审核中</a-select-option>
            <a-select-option value="approved">已同意</a-select-option>
            <a-select-option value="rejected">已拒绝</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="申请时间">
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
        <a-tab-pane key="applying" tab="申请中" />
        <a-tab-pane key="reviewing" tab="审核中" />
        <a-tab-pane key="approved" tab="已同意" />
        <a-tab-pane key="rejected" tab="已拒绝" />
        <a-tab-pane key="completed" tab="已完成" />
      </a-tabs>
    </div>

    <!-- 退换货列表 -->
    <div class="content-card return-list-card">
      <a-spin :spinning="loading">
        <div v-if="tableData.length === 0" class="empty-state">
          <a-empty description="暂无退换货申请" />
        </div>
        <div v-else>
          <div v-for="item in tableData" :key="item.id" class="return-item">
            <!-- 申请头部 -->
            <div class="return-header">
              <div class="return-info">
                <span class="return-code">申请单号：{{ item.returnCode }}</span>
                <a-tag :color="getReturnStatusColor(item.status)" size="small">
                  {{ getReturnStatusText(item.status) }}
                </a-tag>
              </div>
            </div>

            <!-- 申请内容 -->
            <div class="return-body">
              <div class="product-section">
                <div class="product-icon">
                  <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.productName" />
                  <div v-else class="product-placeholder" />
                </div>
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                  <div class="product-spec">规格：{{ item.productSpec }}</div>
                  <div class="product-qty" v-if="item.quantity">x{{ item.quantity }}</div>
                </div>
                <div class="product-price" v-if="item.orderAmount">
                  ¥{{ item.orderAmount.toFixed(2) }}
                </div>
              </div>

              <div class="return-detail">
                <div class="detail-row">
                  <span class="detail-label">申请类型：</span>
                  <span>{{ item.returnType === 'refund' ? '退货退款' : '换货' }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">申请时间：</span>
                  <span>{{ item.applyTime }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">申请原因：</span>
                  <span>{{ item.reason }}</span>
                </div>
                <div class="detail-row">
                  <span class="detail-label">订单号：</span>
                  <a class="action-link" @click="handleViewOrder(item.orderCode)">{{ item.orderCode }}</a>
                </div>
              </div>
            </div>

            <!-- 操作 -->
            <div class="return-footer">
              <a class="action-link" @click="handleViewDetail(item)">查看详情</a>
              <template v-if="item.status === 'applying'">
                <a-divider type="vertical" />
                <a class="action-link" @click="handleCancelApply(item)">撤销申请</a>
              </template>
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

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="退换货详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item v-for="(val, key) in detailRecord" :key="key" :label="String(key)" :span="typeof val === 'object' ? 2 : 1">
          {{ typeof val === 'object' ? JSON.stringify(val) : val }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import type { ReturnRecord, ReturnQueryParams, ReturnStatus, ReturnType } from '@/types/order'
import { returnApi, returnStatusMap } from '@/api/order'

const router = useRouter()

// 当前标签页
const activeTab = ref<string>('all')

// 搜索表单
const searchForm = reactive({
  keyword: '',
  orderKeyword: '',
  returnType: undefined as ReturnType | undefined,
  status: undefined as ReturnStatus | undefined,
  dateRange: null as any
})

// 表格数据
const tableData = ref<ReturnRecord[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<any>(null)

// 监听标签页切换
watch(activeTab, () => {
  pagination.current = 1
  loadData()
})

// 状态颜色
const getReturnStatusColor = (status: ReturnStatus) => {
  return returnStatusMap[status]?.color || 'default'
}

// 状态文本
const getReturnStatusText = (status: ReturnStatus) => {
  return returnStatusMap[status]?.text || status
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const statusFilter = activeTab.value === 'all' ? searchForm.status : activeTab.value as ReturnStatus
    const params: ReturnQueryParams = {
      keyword: searchForm.keyword || undefined,
      returnType: searchForm.returnType,
      status: statusFilter,
      startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
      endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await returnApi.getList(params)
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
  searchForm.orderKeyword = ''
  searchForm.returnType = undefined
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

// 查看详情
const handleViewDetail = (item: ReturnRecord) => {
  detailRecord.value = item
  detailVisible.value = true
}

// 查看订单
const handleViewOrder = (orderCode: string) => {
  router.push(`/order/detail/${orderCode}`)
}

// 撤销申请
const handleCancelApply = async (item: ReturnRecord) => {
  try {
    await returnApi.cancel(item.id)
    message.success('已撤销')
    loadData()
  } catch { message.error('撤销失败') }
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

/* 退换货列表 */
.return-list-card {
  padding: 16px;
}

.return-item {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.return-header {
  background: #fafafa;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.return-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.return-code {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.return-body {
  padding: 16px;
}

.product-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
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

.return-detail {
  background: #fafafa;
  border-radius: 6px;
  padding: 12px 16px;
}

.detail-row {
  font-size: 13px;
  color: #666;
  padding: 4px 0;
}

.detail-label {
  color: #999;
  margin-right: 4px;
}

.return-footer {
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

  .return-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 576px) {
  .page-header h2 {
    font-size: 18px;
  }

  .return-header {
    padding: 10px 12px;
  }

  .return-body {
    padding: 12px;
  }

  .return-footer {
    padding: 10px 12px;
  }
}
</style>
