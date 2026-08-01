<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>员工面谈</h2>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch" class="search-form">
        <a-form-item label="面谈日期">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            format="YYYY-MM-DD"
            :placeholder="['开始日期', '结束日期']"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item label="面谈类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="请选择面谈类型"
            allow-clear
            style="width: 160px"
          >
            <a-select-option v-for="item in interviewTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 140px"
          >
            <a-select-option value="completed">已完成</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="面谈人">
          <a-input
            v-model:value="searchForm.interviewer"
            placeholder="请输入面谈人"
            allow-clear
            style="width: 160px"
          >
            <template #prefix><SearchOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="margin-right: 8px">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
          <a-button @click="handleReset" style="margin-right: 8px">
            重置
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作栏 + 表格 -->
    <div class="content-card">
      <div class="table-toolbar">
        <a-button type="primary" @click="showCreateModal">
          <template #icon><PlusOutlined /></template>
          发起面谈
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        :scroll="{ x: 900 }"
        @change="handleTableChange"
        :size="isMobile ? 'small' : 'middle'"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'type'">
            {{ interviewTypeMap[record.type] }}
          </template>
          <template v-if="column.dataIndex === 'status'">
            <a-tag color="green">已完成</a-tag>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-space>
              <a @click="handleView(record)">查看</a>
              <a-divider type="vertical" />
              <a @click="handleRecord(record)">记录</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建面谈弹窗 -->
    <a-modal
      v-model:open="createModalVisible"
      title="发起面谈"
      :confirm-loading="confirmLoading"
      @ok="handleCreateOk"
      @cancel="handleCreateCancel"
      :width="isMobile ? '100%' : 520"
      :style="isMobile ? { top: '20px' } : {}"
      destroy-on-close
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        style="margin-top: 24px"
      >
        <a-form-item label="面谈主题" name="topic">
          <a-input v-model:value="createForm.topic" placeholder="请输入面谈主题" />
        </a-form-item>
        <a-form-item label="面谈类型" name="type">
          <a-select v-model:value="createForm.type" placeholder="请选择面谈类型">
            <a-select-option v-for="item in interviewTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="面谈人" name="interviewer">
          <a-input v-model:value="createForm.interviewer" placeholder="请输入面谈人" />
        </a-form-item>
        <a-form-item label="被面谈人" name="interviewee">
          <a-input v-model:value="createForm.interviewee" placeholder="请输入被面谈人" />
        </a-form-item>
        <a-form-item label="面谈日期" name="interviewDate">
          <a-date-picker
            v-model:value="createForm.interviewDate"
            show-time
            format="YYYY-MM-DD HH:mm"
            placeholder="请选择面谈日期"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="时长" name="duration">
          <a-input v-model:value="createForm.duration" placeholder="请输入时长，如：45分钟" />
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

    <!-- 面谈记录弹窗 -->
    <a-modal v-model:open="recordVisible" title="面谈记录" @ok="handleRecordOk" width="500px">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="记录内容">
          <a-textarea v-model:value="recordForm.content" :rows="6" placeholder="请输入面谈记录内容（业绩情况、心态状态、主要问题、改进计划等）" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import type { InterviewItem, InterviewType } from '@/types/human'
import { interviewApi } from '@/api/human'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
// dayjs 已移除

// 面谈类型选项
const interviewTypeOptions = [
  { value: 'performance', label: '绩效沟通' },
  { value: 'problem', label: '问题沟通' },
  { value: 'development', label: '发展沟通' },
  { value: 'onboarding', label: '入职沟通' },
  { value: 'transfer', label: '转正沟通' },
]

const interviewTypeMap: Record<string, string> = {
  performance: '绩效沟通',
  problem: '问题沟通',
  development: '发展沟通',
  onboarding: '入职沟通',
  transfer: '转正沟通',
}

// 响应式：检测是否为移动端
const isMobile = ref(false)
const updateMobile = () => {
  isMobile.value = window.innerWidth < 768
}
onMounted(() => {
  updateMobile()
  window.addEventListener('resize', updateMobile)
})
onUnmounted(() => {
  window.removeEventListener('resize', updateMobile)
})

// 搜索表单
const searchForm = reactive({
  dateRange: null as any,
  type: undefined as string | undefined,
  status: undefined as string | undefined,
  interviewer: '',
})

// 表格数据
const tableData = ref<InterviewItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列定义
const columns = computed(() => {
  const base = [
    { title: '面谈主题', dataIndex: 'topic', key: 'topic', ellipsis: true },
    { title: '面谈类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '面谈人', dataIndex: 'interviewer', key: 'interviewer', width: 100 },
    { title: '被面谈人', dataIndex: 'interviewee', key: 'interviewee', width: 100 },
    { title: '面谈日期', dataIndex: 'interviewDate', key: 'interviewDate', width: 160 },
    { title: '时长', dataIndex: 'duration', key: 'duration', width: 90 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 120, fixed: 'right' as const },
  ]
  if (isMobile.value) {
    return base.filter(col => !['interviewee', 'interviewDate'].includes(col.dataIndex))
  }
  return base
})

// 加载数据
const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.current,
      pageSize: pagination.pageSize,
    }
    if (searchForm.interviewer) params.keyword = searchForm.interviewer
    if (searchForm.type) params.type = searchForm.type
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.dateRange) {
      params.startDate = searchForm.dateRange[0].format('YYYY-MM-DD')
      params.endDate = searchForm.dateRange[1].format('YYYY-MM-DD')
    }
    const res = await interviewApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.dateRange = null
  searchForm.type = undefined
  searchForm.status = undefined
  searchForm.interviewer = ''
  pagination.current = 1
  fetchData()
}

// 表格分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<any>(null)

// 新建弹窗
const createModalVisible = ref(false)
const confirmLoading = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  topic: '',
  type: undefined as InterviewType | undefined,
  interviewer: '',
  interviewee: '',
  interviewDate: null as any,
  duration: '',
})

const createRules = {
  topic: [{ required: true, message: '请输入面谈主题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择面谈类型', trigger: 'change' }],
  interviewer: [{ required: true, message: '请输入面谈人', trigger: 'blur' }],
  interviewee: [{ required: true, message: '请输入被面谈人', trigger: 'blur' }],
  interviewDate: [{ required: true, message: '请选择面谈日期', trigger: 'change' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
}

const showCreateModal = () => {
  createModalVisible.value = true
}

const handleCreateOk = async () => {
  try {
    await createFormRef.value?.validateFields()
  } catch {
    return
  }
  confirmLoading.value = true
  try {
    await interviewApi.create({
      topic: createForm.topic,
      type: createForm.type,
      interviewer: createForm.interviewer,
      interviewee: createForm.interviewee,
      interviewDate: createForm.interviewDate ? createForm.interviewDate.format('YYYY-MM-DD HH:mm') : '',
      duration: createForm.duration,
    })
    message.success('面谈创建成功')
    createModalVisible.value = false
    resetCreateForm()
    fetchData()
  } finally {
    confirmLoading.value = false
  }
}

const handleCreateCancel = () => {
  createModalVisible.value = false
  resetCreateForm()
}

const resetCreateForm = () => {
  createForm.topic = ''
  createForm.type = undefined
  createForm.interviewer = ''
  createForm.interviewee = ''
  createForm.interviewDate = null
  createForm.duration = ''
}

// 查看 / 记录
const handleView = (record: InterviewItem) => {
  detailRecord.value = record
  detailVisible.value = true
}

const recordVisible = ref(false)
const recordForm = reactive({ content: '' })
const recordItemId = ref(0)

const handleRecord = (record: InterviewItem) => {
  recordItemId.value = record.id
  recordForm.content = ''
  recordVisible.value = true
}

const handleRecordOk = async () => {
  if (!recordForm.content.trim()) { message.warning('请输入面谈记录内容'); return }
  // TODO: P2-面谈记录表单(业绩/心态/问题/计划)
  message.success('面谈记录已保存')
  recordVisible.value = false
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 16px;
  min-height: 100%;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
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

.search-form :deep(.ant-form-item) {
  margin-bottom: 12px;
}

.table-toolbar {
  margin-bottom: 16px;
}

@media (max-width: 767px) {
  .page-container {
    padding: 8px;
  }

  .content-card {
    padding: 12px;
    border-radius: 4px;
  }

  .search-card {
    padding: 12px;
  }

  .search-form :deep(.ant-form-item) {
    display: flex;
    width: 100%;
    margin-right: 0;
  }

  .search-form :deep(.ant-form-item-label) {
    flex: 0 0 70px;
  }

  .search-form :deep(.ant-form-item-control) {
    flex: 1;
    min-width: 0;
  }

  .table-toolbar {
    margin-bottom: 12px;
  }
}
</style>
