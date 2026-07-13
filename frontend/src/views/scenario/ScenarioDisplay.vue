<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>陈列检查</h2>
      <a-space>
        <a-button type="primary" @click="handleCreate">
          <PlusOutlined /> 新建检查
        </a-button>
        <a-button @click="handleExport">
          <DownloadOutlined /> 导出
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card">
      <div class="search-card">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="门店">
            <a-select
              v-model:value="searchForm.store"
              placeholder="请选择门店"
              allow-clear
              style="width: 180px"
            >
              <a-select-option value="万达广场店">万达广场店</a-select-option>
              <a-select-option value="龙湖天街店">龙湖天街店</a-select-option>
              <a-select-option value="华润万象店">华润万象店</a-select-option>
              <a-select-option value="大悦城店">大悦城店</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="检查日期">
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
              style="width: 130px"
            >
              <a-select-option v-for="(val, key) in statusMap" :key="key" :value="key">
                {{ val.text }}
              </a-select-option>
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
          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="statusMap[record.status].color">
              {{ statusMap[record.status].text }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a @click="handleDetail(record)" class="action-link">详情</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import type { InspectionItem, SceneQueryParams, SceneStatus } from '@/types/scenario'
import { displayApi, statusMap } from '@/api/mock/scenario'

// 搜索表单
const searchForm = reactive({
  store: undefined as string | undefined,
  dateRange: null as [string, string] | null,
  status: undefined as SceneStatus | undefined
})

// 表格数据
const tableData = ref<InspectionItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = computed(() => [
  { title: '检查编号', dataIndex: 'code', key: 'code', width: 160, ellipsis: true },
  { title: '门店', dataIndex: 'store', key: 'store', width: 140 },
  { title: '检查日期', dataIndex: 'inspectDate', key: 'inspectDate', width: 160 },
  { title: '检查人', dataIndex: 'inspector', key: 'inspector', width: 90 },
  { title: '检查项数', dataIndex: 'checkItems', key: 'checkItems', width: 90, align: 'center' as const },
  { title: '问题数', dataIndex: 'issueCount', key: 'issueCount', width: 80, align: 'center' as const },
  { title: '得分', dataIndex: 'score', key: 'score', width: 70, align: 'center' as const },
  { title: '状态', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: SceneQueryParams = {
      store: searchForm.store || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await displayApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch {
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
  searchForm.store = undefined
  searchForm.dateRange = null
  searchForm.status = undefined
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: { current?: number; pageSize?: number }) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

// 新建检查
const handleCreate = () => {
  message.info('新建陈列检查')
}

// 导出
const handleExport = () => {
  message.success('导出成功')
}

// 查看
const handleView = (record: InspectionItem) => {
  message.info(`查看检查记录: ${record.code}`)
}

// 详情
const handleDetail = (record: InspectionItem) => {
  message.info(`检查详情: ${record.code}`)
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
  padding: 0;
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
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 1000px;
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

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .search-card :deep(.ant-form-item-label) {
    flex: 0 0 80px;
    max-width: 80px;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 900px;
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
    min-width: 800px;
  }
}
</style>
