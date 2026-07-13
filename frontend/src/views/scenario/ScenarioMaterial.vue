<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>物料更新</h2>
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新建更新
        </a-button>
        <a-button @click="handleExport">
          <DownloadOutlined /> 导出
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="门店">
          <a-select
            v-model:value="searchForm.store"
            placeholder="请选择门店"
            allow-clear
            style="width: 180px"
          >
            <a-select-option
              v-for="item in storeOptions"
              :key="item"
              :value="item"
            >
              {{ item }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="更新日期">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="completed">已完成</a-select-option>
            <a-select-option value="pending">待处理</a-select-option>
            <a-select-option value="abnormal">异常</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" html-type="submit">
              <SearchOutlined /> 查询
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
        :scroll="{ x: 900 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusMap[record.status]?.color">
              {{ statusMap[record.status]?.text || record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleDetail(record)" class="action-link">详情</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import type { MaterialItem, SceneStatus } from '@/types/scenario'
import { materialApi, statusMap } from '@/api/mock/scenario'

// 门店选项
const storeOptions = ['万达广场店', '龙湖天街店', '华润万家店', '大悦城店']

// 搜索表单
const searchForm = reactive({
  store: undefined as string | undefined,
  dateRange: null as [string, string] | null,
  status: undefined as SceneStatus | undefined
})

// 表格数据
const tableData = ref<MaterialItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 全量数据（用于前端筛选）
const allData = ref<MaterialItem[]>([])

// 表格列配置
const columns = [
  { title: '更新编号', dataIndex: 'code', key: 'code', width: 170 },
  { title: '门店', dataIndex: 'store', key: 'store', width: 140 },
  { title: '物料类型', dataIndex: 'materialType', key: 'materialType', width: 120 },
  { title: '更新日期', dataIndex: 'updateDate', key: 'updateDate', width: 170 },
  { title: '更新人', dataIndex: 'updater', key: 'updater', width: 100, align: 'center' as const },
  { title: '更新数量', dataIndex: 'updateCount', key: 'updateCount', width: 100, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await materialApi.getList({
      page: 1,
      pageSize: 100
    })
    allData.value = res.list
    applyFilter()
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 前端筛选
const applyFilter = () => {
  let filtered = [...allData.value]

  if (searchForm.store) {
    filtered = filtered.filter(item => item.store === searchForm.store)
  }
  if (searchForm.status) {
    filtered = filtered.filter(item => item.status === searchForm.status)
  }
  if (searchForm.dateRange && searchForm.dateRange[0] && searchForm.dateRange[1]) {
    const [start, end] = searchForm.dateRange
    filtered = filtered.filter(item => {
      const date = item.updateDate.split(' ')[0]
      return date >= start && date <= end
    })
  }

  pagination.total = filtered.length
  const start = (pagination.current - 1) * pagination.pageSize
  tableData.value = filtered.slice(start, start + pagination.pageSize)
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  applyFilter()
}

// 重置
const handleReset = () => {
  searchForm.store = undefined
  searchForm.dateRange = null
  searchForm.status = undefined
  pagination.current = 1
  applyFilter()
}

// 分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  applyFilter()
}

// 新建更新
const handleAdd = () => {
  message.info('新建物料更新')
}

// 导出
const handleExport = () => {
  message.success('导出成功')
}

// 查看
const handleView = (record: MaterialItem) => {
  message.info(`查看物料更新：${record.code}`)
}

// 详情
const handleDetail = (record: MaterialItem) => {
  message.info(`物料更新详情：${record.code}`)
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

.table-card {
  padding: 16px;
  overflow: hidden;
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

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 800px;
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

  .table-card {
    padding: 12px;
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

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 700px;
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
    min-width: 600px;
  }
}
</style>
