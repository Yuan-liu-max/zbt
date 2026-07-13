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
        <a-form-item label="操作类型">
          <a-select v-model:value="searchForm.action" placeholder="全部类型" allow-clear style="width: 150px">
            <a-select-option value="新增商品">新增商品</a-select-option>
            <a-select-option value="编辑用户">编辑用户</a-select-option>
            <a-select-option value="发货处理">发货处理</a-select-option>
            <a-select-option value="取消订单">取消订单</a-select-option>
            <a-select-option value="重置密码">重置密码</a-select-option>
            <a-select-option value="修改角色">修改角色</a-select-option>
            <a-select-option value="创建活动">创建活动</a-select-option>
            <a-select-option value="退款处理">退款处理</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="用户">
          <a-select v-model:value="searchForm.operator" placeholder="全部用户" allow-clear style="width: 130px">
            <a-select-option value="管理员">管理员</a-select-option>
            <a-select-option value="张三">张三</a-select-option>
            <a-select-option value="李四">李四</a-select-option>
            <a-select-option value="王五">王五</a-select-option>
            <a-select-option value="赵六">赵六</a-select-option>
          </a-select>
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
          <a-button @click="handleExport" :disabled="selectedRowKeys.length === 0">
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
          <template v-if="column.key === 'result'">
            <a-tag :color="resultMap[record.result]?.color">
              {{ resultMap[record.result]?.text }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a @click="handleViewDetail(record)" class="action-link">查看详情</a>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="日志详情" :footer="null" width="500px">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="日志时间">{{ currentLog?.logTime }}</a-descriptions-item>
        <a-descriptions-item label="操作模块">{{ currentLog?.module }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">{{ currentLog?.action }}</a-descriptions-item>
        <a-descriptions-item label="操作人员">{{ currentLog?.operator }}</a-descriptions-item>
        <a-descriptions-item label="IP地址">{{ currentLog?.ip }}</a-descriptions-item>
        <a-descriptions-item label="操作结果">
          <a-tag :color="resultMap[currentLog?.result || 'success']?.color">
            {{ resultMap[currentLog?.result || 'success']?.text }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="详情">{{ currentLog?.detail || '无' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { LogItem, LogQueryParams, LogResult } from '@/types/log'
import { logApi, resultMap } from '@/api/mock/log'

// 搜索表单
const searchForm = reactive({
  dateRange: null as any,
  action: undefined as string | undefined,
  operator: undefined as string | undefined,
  keyword: ''
})

// 表格数据
const tableData = ref<LogItem[]>([])
const loading = ref(false)
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({
  current: 1, pageSize: 10, total: 0,
  showSizeChanger: true, showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 全选状态
const checkAll = computed({
  get: () => tableData.value.length > 0 && selectedRowKeys.value.length === tableData.value.length,
  set: () => {}
})
const indeterminate = computed(() => selectedRowKeys.value.length > 0 && selectedRowKeys.value.length < tableData.value.length)

// 表格列配置
const columns = [
  { title: '日志时间', dataIndex: 'logTime', key: 'logTime', width: 170, sorter: true },
  { title: '操作模块', dataIndex: 'module', key: 'module', width: 110 },
  { title: '操作类型', dataIndex: 'action', key: 'action', width: 110 },
  { title: '操作人员', dataIndex: 'operator', key: 'operator', width: 100 },
  { title: 'IP地址', dataIndex: 'ip', key: 'ip', width: 140 },
  { title: '操作结果', dataIndex: 'result', key: 'result', width: 90, align: 'center' as const },
  { title: '详情', key: 'action', width: 100, align: 'center' as const }
]

// 详情弹窗
const detailVisible = ref(false)
const currentLog = ref<LogItem | null>(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: LogQueryParams = {
      startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD HH:mm:ss') || undefined,
      endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD HH:mm:ss') || undefined,
      action: searchForm.action || undefined,
      operator: searchForm.operator || undefined,
      keyword: searchForm.keyword || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await logApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch { message.error('加载数据失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; selectedRowKeys.value = []; loadData() }
const handleReset = () => {
  searchForm.dateRange = null; searchForm.action = undefined; searchForm.operator = undefined; searchForm.keyword = ''
  handleSearch()
}
const handleTableChange = (pag: any) => { pagination.current = pag.current; pagination.pageSize = pag.pageSize; loadData() }

// 多选
const onSelectChange = (keys: string[]) => { selectedRowKeys.value = keys }
const handleCheckAll = (e: any) => {
  selectedRowKeys.value = e.target.checked ? tableData.value.map(item => item.id) : []
}

// 查看详情
const handleViewDetail = (record: LogItem) => { currentLog.value = record; detailVisible.value = true }

// 导出
const handleExport = () => { message.success(`已导出 ${selectedRowKeys.value.length} 条日志`) }

// 批量删除
const handleBatchDelete = async () => {
  try {
    await logApi.deleteBatch(selectedRowKeys.value)
    message.success(`已删除 ${selectedRowKeys.value.length} 条日志`)
    selectedRowKeys.value = []
    loadData()
  } catch { message.error('删除失败') }
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
