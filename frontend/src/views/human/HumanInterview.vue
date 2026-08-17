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
          <template v-if="column.dataIndex === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleRecord(record)" class="action-link">记录</a>
            </div>
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
        <a-form-item label="面谈主题" name="mainProblem">
          <a-input v-model:value="createForm.mainProblem" placeholder="请输入主要问题/主题" />
        </a-form-item>
        <a-form-item label="面谈人" name="interviewerId">
          <a-select v-model:value="createForm.interviewerId" placeholder="请选择面谈人" allow-clear>
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="被面谈人" name="employeeId">
          <a-select v-model:value="createForm.employeeId" placeholder="请选择被面谈人" allow-clear>
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="面谈日期" name="interviewDate">
          <a-date-picker
            v-model:value="createForm.interviewDate"
            format="YYYY-MM-DD"
            placeholder="请选择面谈日期"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="面谈主题">{{ detailRecord?.mainProblem }}</a-descriptions-item>
        <a-descriptions-item label="面谈人">{{ getUserName(detailRecord?.interviewerId) }}</a-descriptions-item>
        <a-descriptions-item label="被面谈人">{{ getUserName(detailRecord?.employeeId) }}</a-descriptions-item>
        <a-descriptions-item label="面谈日期">{{ detailRecord?.interviewDate }}</a-descriptions-item>
        <a-descriptions-item label="面谈记录" :span="2">{{ detailRecord?.managerComment || '-' }}</a-descriptions-item>
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
import type { InterviewItem } from '@/types/human'
import { interviewApi } from '@/api/human'
import { userApi } from '@/api/system'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

// 员工下拉（真实用户列表）
const userOptions = ref<{ id: number; name: string }[]>([])
// 通过 userOptions 将用户 ID 映射为人名（找不到时回退显示 ID）
const getUserName = (id: number | undefined) => {
  if (!id) return '-'
  const u = userOptions.value.find((item) => item.id === Number(id))
  return u ? u.name : `用户#${id}`
}
const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 200 })
    userOptions.value = res.list.map((u) => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch {}
}

// 响应式：检测是否为移动端
const isMobile = ref(false)
const updateMobile = () => {
  isMobile.value = window.innerWidth < 768
}
onMounted(() => {
  updateMobile()
  window.addEventListener('resize', updateMobile)
  loadUsers()
  loadData()
})
onUnmounted(() => {
  window.removeEventListener('resize', updateMobile)
})

// 搜索表单（后端仅支持分页，其他筛选参数忽略）
const searchForm = reactive({
  dateRange: null as any,
})

// 表格数据
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => {
    const apiParams: any = { page: params.page, pageSize: params.pageSize }
    if (params.dateRange) {
      apiParams.startDate = params.dateRange[0].format('YYYY-MM-DD')
      apiParams.endDate = params.dateRange[1].format('YYYY-MM-DD')
    }
    return interviewApi.getList(apiParams)
  },
})

// 表格列定义
const columns = computed(() => {
  const base = [
    { title: '面谈主题', dataIndex: 'mainProblem', key: 'mainProblem', ellipsis: true },
    { title: '面谈人ID', dataIndex: 'interviewerId', key: 'interviewerId', width: 100 },
    { title: '被面谈人ID', dataIndex: 'employeeId', key: 'employeeId', width: 100 },
    { title: '面谈日期', dataIndex: 'interviewDate', key: 'interviewDate', width: 160 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 120, fixed: 'right' as const },
  ]
  if (isMobile.value) {
    return base.filter(col => !['employeeId', 'interviewDate'].includes(col.dataIndex))
  }
  return base
})

// 搜索
const handleReset = () => {
  searchForm.dateRange = null
  pagination.current = 1
  loadData()
}

// 详情弹窗
const { detailVisible, detailRecord, openDetail } = useDetailModal<InterviewItem>()

// 新建弹窗
const createModalVisible = ref(false)
const confirmLoading = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  mainProblem: '',
  interviewerId: undefined as number | undefined,
  employeeId: undefined as number | undefined,
  interviewDate: null as any,
})

const createRules = {
  mainProblem: [{ required: true, message: '请输入面谈主题', trigger: 'blur' }],
  interviewerId: [{ required: true, message: '请选择面谈人', trigger: 'change' }],
  employeeId: [{ required: true, message: '请选择被面谈人', trigger: 'change' }],
  interviewDate: [{ required: true, message: '请选择面谈日期', trigger: 'change' }],
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
      employeeId: createForm.employeeId!,
      interviewerId: createForm.interviewerId!,
      interviewDate: createForm.interviewDate ? createForm.interviewDate.format('YYYY-MM-DD') : '',
      mainProblem: createForm.mainProblem,
    })
    message.success('面谈创建成功')
    createModalVisible.value = false
    resetCreateForm()
    loadData()
  } finally {
    confirmLoading.value = false
  }
}

const handleCreateCancel = () => {
  createModalVisible.value = false
  resetCreateForm()
}

const resetCreateForm = () => {
  createForm.mainProblem = ''
  createForm.interviewerId = undefined
  createForm.employeeId = undefined
  createForm.interviewDate = null
}

// 查看 / 记录
const handleView = (record: InterviewItem) => {
  openDetail(record)
}

const recordVisible = ref(false)
const recordForm = reactive({ content: '' })
const recordItemId = ref(0)

const handleRecord = (record: InterviewItem) => {
  recordItemId.value = record.id
  recordForm.content = record.managerComment || ''
  recordVisible.value = true
}

const handleRecordOk = async () => {
  if (!recordForm.content.trim()) { message.warning('请输入面谈记录内容'); return }
  try {
    await interviewApi.update(recordItemId.value, { managerComment: recordForm.content })
    message.success('面谈记录已保存')
    recordVisible.value = false
    loadData()
  } catch {
    message.error('保存失败，请重试')
  }
}

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

.action-link {
  font-size: 13px;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
}

.action-link:hover {
  color: #40a9ff;
  background: #e6f7ff;
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
