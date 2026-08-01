<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>库存预警</h2>
    </div>

    <!-- 预警统计卡片 -->
    <div class="alert-summary">
      <div class="summary-card shortage">
        <div class="summary-icon">
          <ShoppingCartOutlined />
        </div>
        <div class="summary-info">
          <div class="summary-label">缺货商品</div>
          <div class="summary-value">{{ alertSummary.shortageCount }}</div>
        </div>
        <div class="summary-trend">较昨日 <span class="up">+2</span></div>
      </div>
      <div class="summary-card warning">
        <div class="summary-icon">
          <WarningOutlined />
        </div>
        <div class="summary-info">
          <div class="summary-label">库存预警</div>
          <div class="summary-value">{{ alertSummary.warningCount }}</div>
        </div>
        <div class="summary-trend">较昨日 <span class="up">+3</span></div>
      </div>
      <div class="summary-card expiring">
        <div class="summary-icon">
          <ClockCircleOutlined />
        </div>
        <div class="summary-info">
          <div class="summary-label">接近过期</div>
          <div class="summary-value">{{ alertSummary.expiringCount }}</div>
        </div>
        <div class="summary-trend">较昨日 <span class="up">+1</span></div>
      </div>
      <div class="summary-card transit">
        <div class="summary-icon">
          <CarOutlined />
        </div>
        <div class="summary-info">
          <div class="summary-label">在途超时</div>
          <div class="summary-value">{{ alertSummary.transitTimeoutCount }}</div>
        </div>
        <div class="summary-trend">较昨日 <span class="down">+0</span></div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="预警类型">
          <a-select
            v-model:value="searchForm.alertType"
            placeholder="全部"
            allow-clear
            style="width: 140px"
          >
            <a-select-option value="shortage">缺货</a-select-option>
            <a-select-option value="warning">库存预警</a-select-option>
            <a-select-option value="expiring">接近过期</a-select-option>
            <a-select-option value="transit_timeout">在途超时</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="商品名称/编码">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入商品名称或编码"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="仓库">
          <a-select
            v-model:value="searchForm.warehouse"
            placeholder="全部仓库"
            allow-clear
            style="width: 150px"
          >
            <a-select-option v-for="wh in warehouses" :key="wh.id" :value="wh.name">
              {{ wh.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="预警状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="全部"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="pending">未处理</a-select-option>
            <a-select-option value="handled">已处理</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <SearchOutlined /> 查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button @click="handleExport">
              <ExportOutlined /> 导出
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 数据表格 -->
    <div class="content-card table-card">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'alertType'">
            <a-tag :color="getAlertTypeColor(record.alertType)">
              {{ getAlertTypeText(record.alertType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'pending' ? 'orange' : 'green'">
              {{ record.status === 'pending' ? '未处理' : '已处理' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a v-if="record.status === 'pending'" @click="handleProcess(record)" class="action-link primary">
                处理
              </a>
              <a-divider v-if="record.status === 'pending'" type="vertical" />
              <a @click="handleDetail(record)" class="action-link">详情</a>
            </div>
          </template>
        </template>
      </a-table>
    </div>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item v-for="(val, key) in detailRecord" :key="key" :label="String(key)" :span="typeof val === 'object' ? 2 : 1">
          {{ typeof val === 'object' ? JSON.stringify(val) : val }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ExportOutlined,
  ShoppingCartOutlined,
  WarningOutlined,
  ClockCircleOutlined,
  CarOutlined
} from '@ant-design/icons-vue'
import { exportComingSoon } from '@/utils/export'
import type { InventoryWarningItem, InventoryStats, InventoryWarningParams, WarningType } from '@/types/goods'
import { inventoryWarningApi } from '@/api/goods'

// 仓库数据
const warehouses = [
  { id: '1', name: '深圳总仓' },
  { id: '2', name: '北京分仓' },
  { id: '3', name: '上海分仓' },
]

// 预警统计
const alertSummary = reactive<InventoryStats>({
  shortageCount: 0,
  warningCount: 0,
  expiringCount: 0,
  transitTimeoutCount: 0
})

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<any>(null)

// 搜索表单
const searchForm = reactive({
  alertType: undefined as WarningType | undefined,
  keyword: '',
  warehouse: undefined as string | undefined,
  status: undefined as 'pending' | 'handled' | undefined
})

// 表格数据
const tableData = ref<InventoryWarningItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  { title: '预警类型', dataIndex: 'alertType', key: 'alertType', width: 100 },
  { title: '商品编码', dataIndex: 'productCode', key: 'productCode', width: 120 },
  { title: '商品名称', dataIndex: 'productName', key: 'productName', width: 120 },
  { title: '规格', dataIndex: 'spec', key: 'spec', width: 100 },
  { title: '仓库', dataIndex: 'warehouse', key: 'warehouse', width: 100 },
  { title: '当前库存', dataIndex: 'currentQty', key: 'currentQty', width: 90, align: 'right' as const },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock', width: 90, align: 'right' as const },
  { title: '预警阈值', dataIndex: 'threshold', key: 'threshold', width: 90, align: 'center' as const },
  { title: '预警时间', dataIndex: 'alertTime', key: 'alertTime', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80, align: 'center' as const },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
]

// 预警类型颜色
const getAlertTypeColor = (type: WarningType) => {
  const map: Record<WarningType, string> = {
    shortage: 'red',
    warning: 'orange',
    expiring: 'gold',
    transit_timeout: 'blue'
  }
  return map[type] || 'default'
}

// 预警类型文本
const getAlertTypeText = (type: WarningType) => {
  const map: Record<WarningType, string> = {
    shortage: '缺货',
    warning: '库存预警',
    expiring: '接近过期',
    transit_timeout: '在途超时'
  }
  return map[type] || type
}

// 加载统计
const loadSummary = async () => {
  try {
    const summary = await inventoryWarningApi.getStats()
    Object.assign(alertSummary, summary)
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: InventoryWarningParams = {
      alertType: searchForm.alertType,
      keyword: searchForm.keyword || undefined,
      warehouse: searchForm.warehouse,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await inventoryWarningApi.getList(params)
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
  searchForm.alertType = undefined
  searchForm.keyword = ''
  searchForm.warehouse = undefined
  searchForm.status = undefined
  handleSearch()
}

// 导出
const handleExport = () => {
  exportComingSoon('库存预警数据')
}

// 表格分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 处理预警
const handleProcess = (record: InventoryWarningItem) => {
  Modal.confirm({
    title: '确认处理',
    content: `确定要处理 ${record.productName} 的预警吗？`,
    onOk: async () => {
      await inventoryWarningApi.handleAlert(record.id)
      message.success('处理成功')
      loadData()
      loadSummary()
    }
  })
}

// 详情
const handleDetail = (record: InventoryWarningItem) => {
  detailRecord.value = record
  detailVisible.value = true
}

onMounted(() => {
  loadSummary()
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

/* 预警统计卡片 */
.alert-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.summary-card.shortage .summary-icon {
  background: #fff1f0;
  color: #ff4d4f;
}

.summary-card.warning .summary-icon {
  background: #fff7e6;
  color: #fa8c16;
}

.summary-card.expiring .summary-icon {
  background: #fffbe6;
  color: #faad14;
}

.summary-card.transit .summary-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.summary-info {
  flex: 1;
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.summary-trend {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.summary-trend .up {
  color: #ff4d4f;
}

.summary-trend .down {
  color: #52c41a;
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

.table-card {
  padding: 16px;
  overflow: hidden;
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

.action-link.primary {
  color: #1890ff;
  font-weight: 500;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-btns :deep(.ant-divider-vertical) {
  margin: 0 2px;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 800px;
}

/* 响应式 */
@media (max-width: 992px) {
  .alert-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .alert-summary {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .summary-card {
    padding: 16px;
  }

  .summary-value {
    font-size: 24px;
  }

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 600px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
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

  .table-card :deep(.ant-table) {
    font-size: 12px;
    min-width: 500px;
  }
}
</style>
