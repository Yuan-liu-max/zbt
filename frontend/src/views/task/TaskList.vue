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
              v-model:value="searchForm.name"
              placeholder="请输入任务名称"
              allow-clear
              style="width: 180px"
            />
          </a-form-item>
          <a-form-item label="任务类型">
            <a-select
              v-model:value="searchForm.type"
              placeholder="请选择"
              allow-clear
              style="width: 150px"
            >
              <a-select-option value="review">审核任务</a-select-option>
              <a-select-option value="approval">审批任务</a-select-option>
              <a-select-option value="process">流程任务</a-select-option>
              <a-select-option value="general">通用任务</a-select-option>
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
          <template v-if="column.key === 'type'">
            {{ taskTypeMap[record.type as TaskType] }}
          </template>

          <!-- 优先级 -->
          <template v-if="column.key === 'priority'">
            <a-tag :color="priorityMap[record.priority as TaskPriority].color">
              {{ priorityMap[record.priority as TaskPriority].text }}
            </a-tag>
          </template>

          <!-- 参与人 -->
          <template v-if="column.key === 'participants'">
            {{ record.participants.join(', ') }}
          </template>

          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="taskStatusMap[record.status as TaskStatus].color">
              {{ taskStatusMap[record.status as TaskStatus].text }}
            </a-tag>
          </template>

          <!-- 进度 -->
          <template v-if="column.key === 'progress'">
            <a-progress :percent="record.progress" size="small" :stroke-color="record.progress === 100 ? '#52c41a' : '#1890ff'" />
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
                  <a-menu @click="({ key }) => handleMoreAction(key as string, record)">
                    <a-menu-item key="edit">
                      <EditOutlined /> 编辑
                    </a-menu-item>
                    <a-menu-item key="copy">
                      <CopyOutlined /> 复制
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'in_progress'" key="complete">
                      <CheckCircleOutlined /> 完成
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'in_progress'" key="cancel">
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
        <a-form-item label="任务名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入任务名称" :maxlength="50" show-count />
        </a-form-item>
        <a-form-item label="任务类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择任务类型">
            <a-select-option value="review">审核任务</a-select-option>
            <a-select-option value="approval">审批任务</a-select-option>
            <a-select-option value="process">流程任务</a-select-option>
            <a-select-option value="general">通用任务</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-select v-model:value="formData.priority" placeholder="请选择优先级">
            <a-select-option value="high">高</a-select-option>
            <a-select-option value="medium">中</a-select-option>
            <a-select-option value="low">低</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="负责人" name="assignee">
          <a-input v-model:value="formData.assignee" placeholder="请输入负责人" />
        </a-form-item>
        <a-form-item label="参与人" name="participants">
          <a-select
            v-model:value="formData.participants"
            mode="tags"
            placeholder="输入后回车添加"
            :token-separators="[',']"
          />
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
        <a-form-item label="截止时间" name="endTime">
          <a-date-picker
            v-model:value="formData.endTime"
            show-time
            value-format="YYYY-MM-DD HH:mm"
            placeholder="请选择截止时间"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="任务描述" name="description" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-textarea v-model:value="formData.description" :rows="4" placeholder="请输入任务描述" :maxlength="500" show-count />
        </a-form-item>
      </a-form>
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
import type { TaskItem, TaskQueryParams, TaskStatus, TaskType, TaskPriority } from '@/types/task'
import {
  taskApi,
  taskStatusMap,
  taskTypeMap,
  priorityMap
} from '@/api/mock/task'
const router = useRouter()

// 当前激活的标签页
const activeTab = ref<string>('all')

// 搜索表单
const searchForm = reactive({
  name: '',
  type: undefined as TaskType | undefined,
  assignee: '',
  dateRange: null as [string, string] | null
})

// 表格数据
const tableData = ref<TaskItem[]>([])
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
  { title: '任务名称', dataIndex: 'name', key: 'name', width: 180, ellipsis: true },
  { title: '任务类型', key: 'type', width: 100, align: 'center' as const },
  { title: '优先级', key: 'priority', width: 80, align: 'center' as const },
  { title: '负责人', dataIndex: 'assignee', key: 'assignee', width: 90 },
  { title: '参与人', key: 'participants', width: 140, ellipsis: true },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 150 },
  { title: '截止时间', dataIndex: 'endTime', key: 'endTime', width: 150 },
  { title: '状态', key: 'status', width: 90, align: 'center' as const },
  { title: '进度', key: 'progress', width: 120 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
])

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  type: undefined as TaskType | undefined,
  priority: 'medium' as TaskPriority,
  assignee: '',
  participants: [] as string[],
  startTime: null as any,
  endTime: null as any,
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  assignee: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: TaskQueryParams = {
      name: searchForm.name || undefined,
      type: searchForm.type,
      assignee: searchForm.assignee || undefined,
      status: activeTab.value === 'all' ? undefined : activeTab.value as TaskStatus,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await taskApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = () => {
  pagination.current = 1
  loadData()
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.type = undefined
  searchForm.assignee = ''
  searchForm.dateRange = null
  activeTab.value = 'all'
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: { current?: number; pageSize?: number }) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

// 跳转创建页
const handleCreate = () => {
  router.push('/task/create')
}

// 查看详情
const handleDetail = (record: TaskItem) => {
  message.info(`查看详情: ${record.name}`)
}

// 编辑
const handleEdit = (record: TaskItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.name = record.name
  formData.type = record.type
  formData.priority = record.priority
  formData.assignee = record.assignee
  formData.participants = [...record.participants]
  formData.startTime = record.startTime
  formData.endTime = record.endTime
  formData.description = record.description || ''
  modalVisible.value = true
}

// 更多操作
const handleMoreAction = async (key: string, record: TaskItem) => {
  if (key === 'edit') {
    handleEdit(record)
  } else if (key === 'copy') {
    try {
      await taskApi.create({
        name: `${record.name}（副本）`,
        type: record.type,
        priority: record.priority,
        assignee: record.assignee,
        participants: [...record.participants],
        startTime: record.startTime,
        endTime: record.endTime,
        description: record.description
      })
      message.success('复制成功')
      loadData()
    } catch {
      message.error('复制失败')
    }
  } else if (key === 'complete') {
    try {
      await taskApi.update(record.id, { status: 'completed', progress: 100 })
      message.success('任务已标记为完成')
      loadData()
    } catch {
      message.error('操作失败')
    }
  } else if (key === 'cancel') {
    try {
      await taskApi.update(record.id, { status: 'cancelled' })
      message.success('任务已取消')
      loadData()
    } catch {
      message.error('操作失败')
    }
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData: Partial<TaskItem> = {
      name: formData.name,
      type: formData.type!,
      priority: formData.priority,
      assignee: formData.assignee,
      participants: formData.participants,
      startTime: formData.startTime,
      endTime: formData.endTime,
      description: formData.description
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
    // validation failed
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.name = ''
  formData.type = undefined
  formData.priority = 'medium'
  formData.assignee = ''
  formData.participants = []
  formData.startTime = null
  formData.endTime = null
  formData.description = ''
}

// 监听弹窗关闭，重置表单
const handleModalCancel = () => {
  resetForm()
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
