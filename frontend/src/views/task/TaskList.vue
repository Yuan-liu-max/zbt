<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务列表</h2>
      <a-button type="primary" @click="handleCreate">
        <PlusOutlined /> 创建任务
      </a-button>
    </div>

    <!-- 标签页 -->
    <div class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="in_progress" tab="进行中" />
        <a-tab-pane key="completed" tab="已完成" />
        <a-tab-pane key="overdue" tab="已过期" />
        <a-tab-pane key="cancelled" tab="已取消" />
      </a-tabs>

      <!-- 搜索表单 -->
      <div class="search-card">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="任务名称">
            <a-input
              v-model:value="searchForm.keyword"
              placeholder="请输入任务名称"
              allow-clear
              style="width: 180px"
            />
          </a-form-item>
          <a-form-item label="任务类型">
            <a-select
              v-model:value="searchForm.dimension"
              placeholder="请选择"
              allow-clear
              style="width: 150px"
            >
              <a-select-option v-for="(info, value) in taskDimensionMap" :key="value" :value="value">
                {{ info.text }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="负责人">
            <a-input
              v-model:value="searchForm.assignee"
              placeholder="请输入负责人"
              allow-clear
              style="width: 140px"
            />
          </a-form-item>
          <a-form-item label="创建时间">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              value-format="YYYY-MM-DD"
              style="width: 240px"
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
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 任务类型 -->
          <template v-if="column.key === 'dimension'">
            {{ taskDimensionMap[record.dimension as TaskDimension]?.text || record.dimension }}
          </template>

          <!-- 优先级 -->
          <template v-if="column.key === 'priority'">
            <a-tag :color="priorityMap[record.priority as TaskPriority]?.color || 'default'">
              {{ priorityMap[record.priority as TaskPriority]?.text || record.priority }}
            </a-tag>
          </template>

          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="taskStatusMap[record.status as TaskStatus].color">
              {{ taskStatusMap[record.status as TaskStatus].text }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleDetail(record)" class="action-link">详情</a>
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-dropdown :trigger="['click']">
                <a class="action-link">
                  更多 <DownOutlined style="font-size: 10px" />
                </a>
                <template #overlay>
                  <a-menu @click="onMenuClick($event, record)">
                    <a-menu-item key="edit">
                      <EditOutlined /> 编辑
                    </a-menu-item>
                    <a-menu-item key="copy">
                      <CopyOutlined /> 复制
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'IN_PROGRESS'" key="complete">
                      <CheckCircleOutlined /> 完成
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'IN_PROGRESS'" key="cancel">
                      <CloseCircleOutlined /> 取消
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑任务' : '创建任务'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="640px"
      :mask-closable="false"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="任务名称" name="taskTitle">
          <a-input v-model:value="formData.taskTitle" placeholder="请输入任务名称" :maxlength="50" show-count />
        </a-form-item>
        <a-form-item label="任务类型" name="dimension">
          <a-select v-model:value="formData.dimension" placeholder="请选择任务类型">
            <a-select-option v-for="(info, value) in taskDimensionMap" :key="value" :value="value">
              {{ info.text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-select v-model:value="formData.priority" placeholder="请选择优先级">
            <a-select-option value="HIGH">高</a-select-option>
            <a-select-option value="MEDIUM">中</a-select-option>
            <a-select-option value="LOW">低</a-select-option>
            <a-select-option value="URGENT">紧急</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="负责人" name="assigneeId">
          <a-select
            v-model:value="formData.assigneeId"
            placeholder="请选择负责人"
            show-search
            :filter-option="filterOption"
            allow-clear
          >
            <a-select-option v-for="p in personOptions" :key="p.value" :value="p.value">
              {{ p.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始时间" name="startTime">
          <a-date-picker
            v-model:value="formData.startTime"
            show-time
            value-format="YYYY-MM-DD HH:mm"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="截止时间" name="dueTime">
          <a-date-picker
            v-model:value="formData.dueTime"
            show-time
            value-format="YYYY-MM-DD HH:mm"
            placeholder="请选择截止时间"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="任务名称">{{ detailRecord?.taskTitle }}</a-descriptions-item>
        <a-descriptions-item label="任务编号">{{ detailRecord?.taskNo }}</a-descriptions-item>
        <a-descriptions-item label="任务类型">{{ taskDimensionMap[detailRecord?.dimension as TaskDimension]?.text || detailRecord?.dimension }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ taskStatusMap[detailRecord?.status as TaskStatus]?.text || detailRecord?.status }}</a-descriptions-item>
        <a-descriptions-item label="优先级">{{ priorityMap[detailRecord?.priority as TaskPriority]?.text || detailRecord?.priority }}</a-descriptions-item>
        <a-descriptions-item label="负责人">{{ detailRecord?.assigneeName }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ detailRecord?.startTime }}</a-descriptions-item>
        <a-descriptions-item label="截止时间">{{ detailRecord?.dueTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  DownOutlined,
  EditOutlined,
  CopyOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import type { TaskItem, TaskStatus, TaskDimension, TaskPriority } from '@/types/task'
import { taskApi, taskStatusMap, taskDimensionMap, priorityMap } from '@/api/task'
import { userApi } from '@/api/system'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'
const router = useRouter()

// 当前激活的标签页
const activeTab = ref<string>('all')

// 标签页 -> 任务状态映射（全部不传 status）
const tabStatusMap: Record<string, TaskStatus | undefined> = {
  all: undefined,
  in_progress: 'IN_PROGRESS',
  completed: 'COMPLETED',
  overdue: 'OVERDUE',
  cancelled: 'CANCELLED',
}
const tabStatus = ref<TaskStatus | undefined>(undefined)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  dimension: undefined as TaskDimension | undefined,
  assignee: '',
  dateRange: null as [string, string] | null
})

// 表格数据（useCrudTable 封装）
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => {
    const p: any = { ...params }
    if (tabStatus.value) p.status = tabStatus.value
    if (p.dateRange?.[0]) p.startDate = p.dateRange[0]
    if (p.dateRange?.[1]) p.endDate = p.dateRange[1]
    delete p.dateRange
    return taskApi.getList(p)
  },
})

// 表格列配置
const columns = computed(() => [
  { title: '任务名称', dataIndex: 'taskTitle', key: 'taskTitle', width: 180, ellipsis: true },
  { title: '任务类型', key: 'dimension', width: 100, align: 'center' as const },
  { title: '优先级', key: 'priority', width: 80, align: 'center' as const },
  { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 90 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 150 },
  { title: '截止时间', dataIndex: 'dueTime', key: 'dueTime', width: 150 },
  { title: '状态', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
])

// 详情弹窗（useDetailModal 封装）
const { detailVisible, detailRecord, openDetail } = useDetailModal<TaskItem>()

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  taskTitle: '',
  dimension: undefined as TaskDimension | undefined,
  priority: 'MEDIUM' as TaskPriority,
  assigneeId: undefined as string | undefined,
  startTime: null as any,
  dueTime: null as any,
  status: 'PENDING' as TaskStatus
})

const formRules = {
  taskTitle: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  dimension: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  dueTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

// 负责人下拉：真实用户列表（GET /users）
interface UserOption {
  value: string
  label: string
}
const personOptions = ref<UserOption[]>([])
const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 999 })
    personOptions.value = (res.list || []).map((u) => ({
      value: String(u.id),
      label: u.realName || u.username,
    }))
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const filterOption = (input: string, option: any) => {
  return (
    String(option?.label || '').toLowerCase().includes(input.toLowerCase()) ||
    String(option?.value || '').toLowerCase().includes(input.toLowerCase())
  )
}

// 标签页切换
const handleTabChange = (key: string) => {
  tabStatus.value = tabStatusMap[key]
  pagination.current = 1
  loadData()
}

// 重置（扩展：同时重置标签页）
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.dimension = undefined
  searchForm.assignee = ''
  searchForm.dateRange = null
  activeTab.value = 'all'
  tabStatus.value = undefined
  handleSearch()
}

// 跳转创建页
const handleCreate = () => {
  router.push('/task/create')
}

// 查看详情
const handleDetail = openDetail

// 编辑
const handleEdit = (record: TaskItem) => {
  isEdit.value = true
  formData.id = String(record.id)
  formData.taskTitle = record.taskTitle
  formData.dimension = record.dimension
  formData.priority = record.priority
  formData.assigneeId = String(record.assigneeId ?? '')
  formData.startTime = record.startTime
  formData.dueTime = record.dueTime
  formData.status = record.status
  modalVisible.value = true
}

// 菜单点击（避免模板内联解构导致 implicit any）
const onMenuClick = (e: { key: string | number }, record: TaskItem) => {
  handleMoreAction(String(e.key), record)
}

// 更多操作
const handleMoreAction = async (key: string, record: TaskItem) => {
  if (key === 'edit') {
    handleEdit(record)
  } else if (key === 'copy') {
    try {
      await taskApi.create({
        taskTitle: `${record.taskTitle}（副本）`,
        dimension: record.dimension,
        priority: record.priority,
        assigneeName: record.assigneeName,
        storeId: record.storeId,
        startTime: record.startTime,
        dueTime: record.dueTime
      })
      message.success('复制成功')
      loadData()
    } catch (error) {
      console.error('复制失败', error)
    }
  } else if (key === 'complete') {
    try {
      await taskApi.update(String(record.id), { status: 'COMPLETED' })
      message.success('任务已标记为完成')
      loadData()
    } catch (error) {
      console.error('操作失败', error)
    }
  } else if (key === 'cancel') {
    try {
      await taskApi.update(String(record.id), { status: 'CANCELLED' })
      message.success('任务已取消')
      loadData()
    } catch (error) {
      console.error('操作失败', error)
    }
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
  } catch {
    return
  }

  modalLoading.value = true
  try {
    const submitData: any = {
      taskTitle: formData.taskTitle,
      dimension: formData.dimension!,
      priority: formData.priority,
      assigneeId: formData.assigneeId ? Number(formData.assigneeId) : undefined,
      startTime: formData.startTime,
      dueTime: formData.dueTime,
      status: formData.status
    }

    if (isEdit.value) {
      await taskApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await taskApi.create(submitData)
      message.success('创建成功')
    }

    modalVisible.value = false
    loadData()
  } catch {
    message.error('操作失败')
  } finally {
    modalLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadUsers()
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
  min-width: 1100px;
}

/* 标签页样式 */
.content-card :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
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

  .content-card :deep(.ant-tabs-nav) {
    overflow-x: auto;
    white-space: nowrap;
  }
}
</style>
