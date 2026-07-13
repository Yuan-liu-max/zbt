<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>能力考核</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建考核
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="考核周期">
          <a-select
            v-model:value="searchForm.period"
            placeholder="请选择"
            allow-clear
            style="width: 180px"
          >
            <a-select-option
              v-for="item in periodOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="请选择"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="monthly">月度考核</a-select-option>
            <a-select-option value="quarterly">季度考核</a-select-option>
            <a-select-option value="special">专项考核</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ongoing">进行中</a-select-option>
            <a-select-option value="ended">已结束</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核人">
          <a-input
            v-model:value="searchForm.assessor"
            placeholder="请输入考核人"
            allow-clear
            style="width: 150px"
          />
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
          <template v-if="column.key === 'type'">
            {{ assessTypeMap[record.type] || record.type }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="assessStatusMap[record.status]?.color">
              {{ assessStatusMap[record.status]?.text || record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleScore(record)" class="action-link">评分</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建考核弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      title="新建考核"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="560px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="考核名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入考核名称" />
        </a-form-item>
        <a-form-item label="考核周期" name="period">
          <a-input v-model:value="formData.period" placeholder="请输入考核周期，如 2026-04-01 ~ 2026-06-30" />
        </a-form-item>
        <a-form-item label="考核类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择考核类型">
            <a-select-option value="monthly">月度考核</a-select-option>
            <a-select-option value="quarterly">季度考核</a-select-option>
            <a-select-option value="special">专项考核</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核人" name="assessor">
          <a-input v-model:value="formData.assessor" placeholder="请输入考核人" />
        </a-form-item>
        <a-form-item label="参与人数" name="participants">
          <a-input-number
            v-model:value="formData.participants"
            :min="0"
            style="width: 100%"
            placeholder="请输入参与人数"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { AssessItem, AssessType, AssessStatus } from '@/types/human'
import { assessApi, assessStatusMap } from '@/api/mock/human'

// 考核类型映射
const assessTypeMap: Record<string, string> = {
  monthly: '月度考核',
  quarterly: '季度考核',
  special: '专项考核'
}

// 考核周期选项
const periodOptions = [
  { label: '2026-Q2 (04~06月)', value: '2026-Q2' },
  { label: '2026-Q1 (01~03月)', value: '2026-Q1' },
  { label: '2026年6月', value: '2026-06' },
  { label: '2026年5月', value: '2026-05' },
  { label: '2026年4月', value: '2026-04' }
]

// 搜索表单
const searchForm = reactive({
  period: undefined as string | undefined,
  type: undefined as AssessType | undefined,
  status: undefined as AssessStatus | undefined,
  assessor: ''
})

// 表格数据
const tableData = ref<AssessItem[]>([])
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
const allData = ref<AssessItem[]>([])

// 表格列配置
const columns = [
  { title: '考核名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '考核周期', dataIndex: 'period', key: 'period', width: 220 },
  { title: '考核类型', dataIndex: 'type', key: 'type', width: 100, align: 'center' as const },
  { title: '考核人', dataIndex: 'assessor', key: 'assessor', width: 100 },
  { title: '参与人数', dataIndex: 'participants', key: 'participants', width: 90, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({
  name: '',
  period: '',
  type: 'monthly' as AssessType,
  assessor: '',
  participants: 0
})

const formRules = {
  name: [{ required: true, message: '请输入考核名称', trigger: 'blur' }],
  period: [{ required: true, message: '请输入考核周期', trigger: 'blur' }],
  type: [{ required: true, message: '请选择考核类型', trigger: 'change' }],
  assessor: [{ required: true, message: '请输入考核人', trigger: 'blur' }],
  participants: [{ required: true, message: '请输入参与人数', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchForm.assessor || undefined,
      page: 1,
      pageSize: 100
    }
    const res = await assessApi.getList(params)
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

  if (searchForm.type) {
    filtered = filtered.filter(item => item.type === searchForm.type)
  }
  if (searchForm.status) {
    filtered = filtered.filter(item => item.status === searchForm.status)
  }
  if (searchForm.assessor) {
    filtered = filtered.filter(item => item.assessor.includes(searchForm.assessor))
  }
  if (searchForm.period) {
    // 按周期关键词模糊匹配
    const keyword = searchForm.period.toLowerCase()
    filtered = filtered.filter(item => {
      const periodLower = item.period.toLowerCase()
      // 匹配季度
      if (keyword.includes('q1') && periodLower.includes('01-01') && periodLower.includes('03-31')) return true
      if (keyword.includes('q2') && periodLower.includes('04-01') && periodLower.includes('06-30')) return true
      // 匹配月份 (如 2026-06)
      const monthMatch = keyword.match(/(\d{4})-(\d{2})$/)
      if (monthMatch && periodLower.includes(monthMatch[1]) && periodLower.includes(monthMatch[2])) return true
      return false
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
  searchForm.period = undefined
  searchForm.type = undefined
  searchForm.status = undefined
  searchForm.assessor = ''
  pagination.current = 1
  applyFilter()
}

// 分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  applyFilter()
}

// 新建
const handleAdd = () => {
  resetForm()
  modalVisible.value = true
}

// 查看
const handleView = (record: AssessItem) => {
  message.info(`查看考核：${record.name}`)
}

// 评分
const handleScore = (record: AssessItem) => {
  message.info(`评分考核：${record.name}`)
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    await assessApi.create({
      name: formData.name,
      period: formData.period,
      type: formData.type,
      assessor: formData.assessor,
      participants: formData.participants
    })

    message.success('新建成功')
    modalVisible.value = false
    loadData()
  } catch (error) {
    console.error('表单验证失败', error)
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.period = ''
  formData.type = 'monthly'
  formData.assessor = ''
  formData.participants = 0
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
