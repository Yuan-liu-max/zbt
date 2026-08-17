<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>日志管理</h2>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </a-form-item>
        <a-form-item label="操作模块">
          <a-input v-model:value="searchForm.module" placeholder="请输入操作模块" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item label="用户">
          <a-input v-model:value="searchForm.operator" placeholder="请输入操作人员" allow-clear style="width: 130px" />
        </a-form-item>
        <a-form-item label="关键字搜索">
          <a-input v-model:value="searchForm.keyword" placeholder="请输入操作模块、详情内容关键字" allow-clear style="width: 250px">
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit"><SearchOutlined /> 查询</a-button>
            <a-button @click="handleReset"><ReloadOutlined /> 重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作栏 + 表格 -->
    <div class="content-card table-card">
      <!-- 操作栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <a-checkbox v-model:checked="checkAll" :indeterminate="indeterminate" @change="handleCheckAll">
            全选
          </a-checkbox>
        </div>
        <div class="toolbar-right">
          <a-button @click="handleExport">
            <DownloadOutlined /> 导出日志
          </a-button>
          <a-popconfirm
            title="确定要删除选中的日志吗？"
            :disabled="selectedRowKeys.length === 0"
            @confirm="handleBatchDelete"
          >
            <a-button danger :disabled="selectedRowKeys.length === 0">
              <DeleteOutlined /> 批量删除
            </a-button>
          </a-popconfirm>
        </div>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 900 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 详情 -->
          <template v-if="column.key === 'detail'">
            <a @click="handleViewDetail(record)" class="action-link">查看详情</a>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="日志详情" :footer="null" width="600px">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="日志时间">{{ currentLog?.createdAt }}</a-descriptions-item>
        <a-descriptions-item label="操作模块">{{ currentLog?.module }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">{{ currentLog?.action }}</a-descriptions-item>
        <a-descriptions-item label="操作人员">{{ currentLog?.operatorName || currentLog?.operatorId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ currentLog?.requestIp || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求参数">{{ currentLog?.requestParams || '无' }}</a-descriptions-item>
        <a-descriptions-item label="变更前数据">{{ currentLog?.oldData || '无' }}</a-descriptions-item>
        <a-descriptions-item label="变更后数据">{{ currentLog?.newData || '无' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { LogItem } from '@/types/log'
import { logApi } from '@/api/log'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

// 搜索表单
const searchForm = reactive({
  dateRange: null as any,
  module: undefined as string | undefined,
  operator: undefined as string | undefined,
  keyword: ''
})

// 表格数据（useCrudTable 封装，适配 dateRange -> startDate/endDate）
const { tableData, loading, pagination, loadData, handleSearch: _handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => {
    const p: any = { ...params }
    if (p.dateRange?.[0]) p.startDate = p.dateRange[0].format?.('YYYY-MM-DD HH:mm:ss') || undefined
    if (p.dateRange?.[1]) p.endDate = p.dateRange[1].format?.('YYYY-MM-DD HH:mm:ss') || undefined
    delete p.dateRange
    return logApi.getList(p)
  },
})
const selectedRowKeys = ref<string[]>([])

// 全选状态
const checkAll = computed({
  get: () => tableData.value.length > 0 && selectedRowKeys.value.length === tableData.value.length,
  set: () => {}
})
const indeterminate = computed(() => selectedRowKeys.value.length > 0 && selectedRowKeys.value.length < tableData.value.length)

// 表格列配置
const columns = [
  { title: '日志时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '操作模块', dataIndex: 'module', key: 'module', width: 110 },
  { title: '操作类型', dataIndex: 'action', key: 'action', width: 110 },
  { title: '操作人员', dataIndex: 'operatorName', key: 'operatorName', width: 110 },
  { title: 'IP地址', dataIndex: 'requestIp', key: 'requestIp', width: 140 },
  { title: '操作对象', dataIndex: 'targetType', key: 'targetType', width: 100 },
  { title: '详情', key: 'detail', width: 100, align: 'center' as const }
]

// 详情弹窗（useDetailModal 封装，内部 ref 重命名为 currentLog）
const { detailVisible, detailRecord: currentLog, openDetail } = useDetailModal<LogItem>()

// 搜索（扩展：清除已选行）
const handleSearch = () => { selectedRowKeys.value = []; _handleSearch() }

const handleReset = () => {
  searchForm.dateRange = null; searchForm.module = undefined; searchForm.operator = undefined; searchForm.keyword = ''
  handleSearch()
}

// 多选
const onSelectChange = (keys: string[]) => { selectedRowKeys.value = keys }
const handleCheckAll = (e: any) => {
  selectedRowKeys.value = e.target.checked ? tableData.value.map(item => item.id) : []
}

// 查看详情
const handleViewDetail = openDetail

// 导出：按当前搜索条件导出 CSV
const handleExport = () => {
  const params: Record<string, string> = {}
  if (searchForm.module) params.module = searchForm.module
  if (searchForm.operator) params.operator = searchForm.operator
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.dateRange?.[0]) params.startDate = searchForm.dateRange[0].format?.('YYYY-MM-DD HH:mm:ss')
  if (searchForm.dateRange?.[1]) params.endDate = searchForm.dateRange[1].format?.('YYYY-MM-DD HH:mm:ss')
  const qs = new URLSearchParams(params).toString()
  const link = document.createElement('a')
  link.href = `/api/reports/operate-logs/export${qs ? '?' + qs : ''}`
  link.download = 'operate-logs.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await logApi.deleteBatch(selectedRowKeys.value)
    message.success(`已删除 ${selectedRowKeys.value.length} 条日志`)
    selectedRowKeys.value = []
    loadData()
  } catch (error) { console.error('删除失败', error) }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.content-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; }
.table-card { padding: 20px; }
.search-card { padding: 20px 24px; }
.search-card :deep(.ant-form) { flex-wrap: wrap; }
.search-card :deep(.ant-form-item) { margin-bottom: 12px; margin-right: 0; }
.table-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }
.action-link { font-size: 13px; color: #1890ff; cursor: pointer; }
.action-link:hover { color: #40a9ff; text-decoration: underline; }
.table-card :deep(.ant-table-wrapper) { overflow-x: auto; }
.table-card :deep(.ant-table) { min-width: 800px; }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .search-card :deep(.ant-form-item) { width: 100%; }
  .search-card :deep(.ant-form-item-control) { flex: 1; }
  .table-toolbar { flex-direction: column; gap: 12px; align-items: flex-start; }
}
@media (max-width: 576px) {
  .page-header h2 { font-size: 18px; }
}
</style>
