<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>绩效复盘</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建复盘
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="复盘周期">
          <a-input v-model:value="searchForm.period" placeholder="请输入复盘周期" allow-clear style="width: 200px" />
        </a-form-item>
        <a-form-item label="复盘类型">
          <a-select v-model:value="searchForm.type" placeholder="全部" allow-clear style="width: 130px">
            <a-select-option value="quarterly">季度复盘</a-select-option>
            <a-select-option value="monthly">月度复盘</a-select-option>
            <a-select-option value="project">项目复盘</a-select-option>
            <a-select-option value="activity">活动复盘</a-select-option>
            <a-select-option value="iteration">迭代复盘</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option value="ongoing">进行中</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="searchForm.assignee" placeholder="请输入负责人" allow-clear style="width: 130px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" html-type="submit"><SearchOutlined /> 查询</a-button>
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
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="performanceStatusMap[record.status]?.color">
              {{ performanceStatusMap[record.status]?.text }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a @click="handleReport(record)" class="action-link">报告</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建复盘弹窗 -->
    <a-modal v-model:open="modalVisible" title="新建复盘" @ok="handleModalOk" :confirm-loading="modalLoading" width="500px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="复盘主题" name="topic">
          <a-input v-model:value="formData.topic" placeholder="请输入复盘主题" />
        </a-form-item>
        <a-form-item label="复盘类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择复盘类型">
            <a-select-option value="quarterly">季度复盘</a-select-option>
            <a-select-option value="monthly">月度复盘</a-select-option>
            <a-select-option value="project">项目复盘</a-select-option>
            <a-select-option value="activity">活动复盘</a-select-option>
            <a-select-option value="iteration">迭代复盘</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="复盘周期" name="period">
          <a-input v-model:value="formData.period" placeholder="请输入复盘周期，如：2026-04-01 ~ 2026-06-30" />
        </a-form-item>
        <a-form-item label="负责人" name="assignee">
          <a-input v-model:value="formData.assignee" placeholder="请输入负责人" />
        </a-form-item>
        <a-form-item label="参与人数" name="participants">
          <a-input-number v-model:value="formData.participants" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
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
import request from '@/utils/request'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { PerformanceItem, HumanQueryParams, ReviewType } from '@/types/human'
import { performanceApi, performanceStatusMap } from '@/api/human'

// 搜索表单
const searchForm = reactive({
  period: '',
  type: undefined as ReviewType | undefined,
  status: undefined as string | undefined,
  assignee: ''
})

// 表格数据
const tableData = ref<PerformanceItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showQuickJumper: true, showTotal: (total: number) => `共 ${total} 条` })

// 表格列配置
const columns = [
  { title: '复盘主题', dataIndex: 'topic', key: 'topic', width: 180 },
  { title: '复盘类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '复盘周期', dataIndex: 'period', key: 'period', width: 200 },
  { title: '负责人', dataIndex: 'assignee', key: 'assignee', width: 100 },
  { title: '参与人数', dataIndex: 'participants', key: 'participants', width: 90, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<any>(null)

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({ topic: '', type: undefined as ReviewType | undefined, period: '', assignee: '', participants: 0 })
const formRules = {
  topic: [{ required: true, message: '请输入复盘主题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择复盘类型', trigger: 'change' }],
  assignee: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: HumanQueryParams = {
      keyword: searchForm.assignee || undefined,
      type: searchForm.type,
      status: searchForm.status,
      page: pagination.current, pageSize: pagination.pageSize
    }
    const res = await performanceApi.getList(params)
    tableData.value = res.list; pagination.total = res.total
  } catch { message.error('加载数据失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchForm.period = ''; searchForm.type = undefined; searchForm.status = undefined; searchForm.assignee = ''; handleSearch() }
const handleTableChange = (pag: any) => { pagination.current = pag.current; pagination.pageSize = pag.pageSize; loadData() }
const handleAdd = () => { resetForm(); modalVisible.value = true }
const handleView = (record: PerformanceItem) => { detailRecord.value = record; detailVisible.value = true }
// TODO: 报告功能待实现（需要单独的报告详情页）
const reportVisible = ref(false)
const reportData = ref<any>(null)

const handleReport = async (_record: PerformanceItem) => {
  try {
    const data = await request.get(`/sales/metrics/employee/${record.id}?month=2024-05`)
    reportData.value = data
    reportVisible.value = true
  } catch {
    message.info('报告功能开发中...')
  }
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    await performanceApi.create({ topic: formData.topic, type: formData.type, period: formData.period, assignee: formData.assignee, participants: formData.participants })
    message.success('创建成功'); modalVisible.value = false; loadData()
  } catch { console.error('表单验证失败') } finally { modalLoading.value = false }
}

const resetForm = () => { formData.topic = ''; formData.type = undefined; formData.period = ''; formData.assignee = ''; formData.participants = 0 }
onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.content-card { background: #fff; border-radius: 8px; padding: 24px; margin-bottom: 16px; }
.table-card { padding: 16px; overflow: hidden; }
.search-card { padding: 16px 24px; }
.search-card :deep(.ant-form) { flex-wrap: wrap; }
.search-card :deep(.ant-form-item) { margin-bottom: 12px; margin-right: 0; }
.action-link { font-size: 13px; color: #1890ff; padding: 2px 6px; border-radius: 4px; transition: all 0.2s; cursor: pointer; }
.action-link:hover { color: #40a9ff; background: #e6f7ff; }
.table-card :deep(.ant-table-wrapper) { overflow-x: auto; }
.table-card :deep(.ant-table) { min-width: 700px; }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; margin-bottom: 12px; }
  .table-card { padding: 12px; }
  .search-card { padding: 12px 16px; }
  .search-card :deep(.ant-form-item) { width: 100%; }
  .search-card :deep(.ant-form-item-control) { flex: 1; }
  .table-card :deep(.ant-table) { font-size: 13px; min-width: 600px; }
  .table-card :deep(.ant-table-thead > tr > th), .table-card :deep(.ant-table-tbody > tr > td) { padding: 10px 8px; }
}
@media (max-width: 576px) {
  .page-header { flex-direction: column; align-items: flex-start; }
  .page-header h2 { font-size: 18px; }
  .table-card :deep(.ant-table) { font-size: 12px; min-width: 500px; }
}
</style>
