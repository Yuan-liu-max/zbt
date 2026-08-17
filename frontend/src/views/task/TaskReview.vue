<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>任务审查</h2>
        <p class="page-desc">管理任务审查流程，查看和处理待审查任务</p>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="pending" tab="待我审查" />
        <a-tab-pane key="reviewed" tab="我已审查" />
        <a-tab-pane key="initiated" tab="我发起的" />
      </a-tabs>

      <!-- 搜索表单 -->
      <div class="search-card">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="任务名称">
            <a-input
              v-model:value="searchForm.name"
              placeholder="请输入任务名称"
              allow-clear
              style="width: 160px"
            />
          </a-form-item>
          <a-form-item label="任务类型">
            <a-select
              v-model:value="searchForm.type"
              placeholder="请选择任务类型"
              allow-clear
              style="width: 150px"
            >
              <a-select-option v-for="(info, value) in taskDimensionMap" :key="value" :value="value">
                {{ info.text }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="发起人">
            <a-input
              v-model:value="searchForm.initiator"
              placeholder="请输入发起人"
              allow-clear
              style="width: 140px"
            />
          </a-form-item>
          <a-form-item label="创建时间">
            <a-range-picker
              v-model:value="searchForm.dateRange"
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

      <!-- 数据表格 -->
      <div class="table-card">
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
            <template v-if="column.key === 'dimension'">
              <span>{{ taskDimensionMap[record.dimension as TaskDimension]?.text }}</span>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="taskStatusMap[record.status as TaskStatus]?.color">
                {{ taskStatusMap[record.status as TaskStatus]?.text }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <div class="action-btns">
                <a @click="handleView(record)" class="action-link">查看</a>
                <a-divider type="vertical" />
                <a
                  v-if="activeTab === 'pending'"
                  @click="handleReview(record)"
                  class="action-link"
                >
                  审查
                </a>
                <a v-else-if="activeTab === 'reviewed'" class="action-link disabled">
                  已审查
                </a>
              </div>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- 审查弹窗 -->
    <a-modal
      v-model:open="reviewModalVisible"
      title="任务审查"
      @ok="handleReviewOk"
      :confirm-loading="reviewLoading"
      width="500px"
    >
      <div class="review-detail" v-if="currentRecord">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="任务名称">{{ currentRecord.taskTitle }}</a-descriptions-item>
          <a-descriptions-item label="任务编号">{{ currentRecord.taskNo }}</a-descriptions-item>
          <a-descriptions-item label="任务类型">{{ taskDimensionMap[currentRecord.dimension]?.text }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ currentRecord.assigneeName }}</a-descriptions-item>
          <a-descriptions-item label="开始时间">{{ currentRecord.startTime }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="taskStatusMap[currentRecord.status as TaskStatus]?.color">
              {{ taskStatusMap[currentRecord.status as TaskStatus]?.text }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
        <a-form style="margin-top: 16px">
          <a-form-item label="审查结果">
            <a-radio-group v-model:value="reviewResult">
              <a-radio value="APPROVED">通过</a-radio>
              <a-radio value="REJECTED">驳回</a-radio>
              <a-radio value="RECTIFY">整改</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="审查意见">
            <a-textarea
              v-model:value="reviewComment"
              :rows="4"
              placeholder="请输入审查意见"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="任务名称">{{ detailRecord?.taskTitle }}</a-descriptions-item>
        <a-descriptions-item label="任务编号">{{ detailRecord?.taskNo }}</a-descriptions-item>
        <a-descriptions-item label="任务类型">{{ taskDimensionMap[detailRecord?.dimension as TaskDimension]?.text || detailRecord?.dimension }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ taskStatusMap[detailRecord?.status as TaskStatus]?.text || detailRecord?.status }}</a-descriptions-item>
        <a-descriptions-item label="负责人">{{ detailRecord?.assigneeName }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ detailRecord?.startTime }}</a-descriptions-item>
        <a-descriptions-item label="截止时间">{{ detailRecord?.dueTime }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import type { TaskReviewItem, TaskDimension, TaskStatus } from '@/types/task'
import { reviewApi, taskApi, taskStatusMap, taskDimensionMap } from '@/api/task'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

// 当前标签页
const activeTab = ref('pending')

// 搜索表单
const searchForm = reactive({
  name: '',
  type: undefined as TaskDimension | undefined,
  initiator: '',
  dateRange: null as any
})

// 标签页 -> 前端过滤（后端 my-audit 不分页、不支持查询参数）
const tabFilter: Record<string, (t: TaskReviewItem) => boolean> = {
  pending: (t) => ['SUBMITTED', 'AUDITING', 'REJECTED', 'RECTIFYING'].includes(t.status),
  reviewed: (t) => ['APPROVED', 'COMPLETED'].includes(t.status),
  initiated: () => true,
}

// 表格数据（useCrudTable 封装；my-audit 返回数组，包装为 {list,total} 并做前端过滤）
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: async (params) => {
    const arr = await reviewApi.getList()
    const [d0, d1] = params.dateRange || []
    const startDate = d0?.format?.('YYYY-MM-DD')
    const endDate = d1?.format?.('YYYY-MM-DD')
    const list = arr.filter((t) => {
      if (params.name && !t.taskTitle.includes(params.name)) return false
      if (params.type && t.dimension !== params.type) return false
      if (params.initiator && !t.assigneeName.includes(params.initiator)) return false
      const day = t.startTime?.slice(0, 10)
      if (startDate && day && day < startDate) return false
      if (endDate && day && day > endDate) return false
      return tabFilter[activeTab.value]?.(t) ?? true
    })
    return { list, total: list.length }
  },
})

// 动态列配置（响应式）
const getColumnsValue = () => {
  const isMobile = window.innerWidth < 768

  if (isMobile) {
    return [
      { title: '任务名称', dataIndex: 'taskTitle', key: 'taskTitle', width: 120 },
      { title: '任务类型', dataIndex: 'dimension', key: 'dimension', width: 80 },
      { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 70 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 70, align: 'center' as const },
      { title: '操作', key: 'action', width: 90, fixed: 'right' as const }
    ]
  }

  return [
    { title: '任务名称', dataIndex: 'taskTitle', key: 'taskTitle', width: 160 },
    { title: '任务类型', dataIndex: 'dimension', key: 'dimension', width: 100 },
    { title: '负责人', dataIndex: 'assigneeName', key: 'assigneeName', width: 80 },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 150 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80, align: 'center' as const },
    { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
  ]
}

const columns = ref(getColumnsValue())

// 详情弹窗（useDetailModal 封装）
const { detailVisible, detailRecord, openDetail } = useDetailModal<TaskReviewItem>()

// 审查弹窗
const reviewModalVisible = ref(false)
const reviewLoading = ref(false)
const currentRecord = ref<TaskReviewItem | null>(null)
const reviewResult = ref<string>('APPROVED')
const reviewComment = ref('')

// 标签页切换
const handleTabChange = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.type = undefined
  searchForm.initiator = ''
  searchForm.dateRange = null
  handleSearch()
}

// 查看
const handleView = openDetail

// 审查
const handleReview = (record: TaskReviewItem) => {
  currentRecord.value = record
  reviewResult.value = 'APPROVED'
  reviewComment.value = ''
  reviewModalVisible.value = true
}

// 审查确认
const handleReviewOk = async () => {
  if (!currentRecord.value) return
  if (!reviewResult.value) {
    message.warning('请选择审查结果')
    return
  }
  reviewLoading.value = true
  try {
    await taskApi.audit({
      taskId: currentRecord.value.id,
      auditResult: reviewResult.value as 'APPROVED' | 'REJECTED' | 'RECTIFY',
      auditComment: reviewComment.value,
    })
    message.success('审查提交成功')
    reviewModalVisible.value = false
    reviewResult.value = 'APPROVED'
    reviewComment.value = ''
    loadData()
  } catch (error) {
    console.error('审查提交失败', error)
  } finally {
    reviewLoading.value = false
  }
}

// 监听窗口大小变化
const handleResize = () => {
  columns.value = getColumnsValue()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
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
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: #999;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.search-card {
  padding: 16px 0;
}

.search-card :deep(.ant-form) {
  flex-wrap: wrap;
}

.search-card :deep(.ant-form-item) {
  margin-bottom: 12px;
  margin-right: 0;
}

.table-card {
  padding: 0;
}

.review-detail :deep(.ant-descriptions) {
  margin-bottom: 0;
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

.action-link.disabled {
  color: #999;
  cursor: not-allowed;
}

.action-link.disabled:hover {
  color: #999;
  background: transparent;
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

.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.table-card :deep(.ant-table) {
  min-width: 700px;
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

  .search-card {
    padding: 12px 0;
  }

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 500px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 6px;
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
    min-width: 400px;
  }
}
</style>
