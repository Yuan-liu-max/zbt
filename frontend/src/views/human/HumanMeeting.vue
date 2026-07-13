<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>晨夕会</h2>
      <a-button type="primary" @click="handleCreate">
        <PlusOutlined /> 创建会议
      </a-button>
    </div>

    <!-- 标签页 -->
    <div class="content-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="created" tab="我创建的" />
        <a-tab-pane key="joined" tab="我参与的" />
        <a-tab-pane key="followed" tab="我关注的" />
      </a-tabs>

      <!-- 搜索表单 -->
      <div class="search-card">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="会议日期">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </a-form-item>
          <a-form-item label="会议类型">
            <a-select
              v-model:value="searchForm.type"
              placeholder="请选择"
              allow-clear
              style="width: 150px"
            >
              <a-select-option value="regular">例会</a-select-option>
              <a-select-option value="temporary">临时会议</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="请选择"
              allow-clear
              style="width: 130px"
            >
              <a-select-option value="ongoing">进行中</a-select-option>
              <a-select-option value="ended">已结束</a-select-option>
              <a-select-option value="cancelled">已取消</a-select-option>
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
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 会议类型 -->
          <template v-if="column.key === 'type'">
            {{ record.type === 'regular' ? '例会' : '临时会议' }}
          </template>

          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="meetingStatusMap[record.status]?.color || 'default'">
              {{ meetingStatusMap[record.status]?.text || record.status }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a @click="handleMinutes(record)" class="action-link">纪要</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建会议弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      title="创建会议"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="560px"
      :mask-closable="false"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="会议主题" name="topic">
          <a-input
            v-model:value="formData.topic"
            placeholder="请输入会议主题"
            :maxlength="50"
            show-count
          />
        </a-form-item>
        <a-form-item label="会议类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择会议类型">
            <a-select-option value="regular">例会</a-select-option>
            <a-select-option value="temporary">临时会议</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会议日期" name="meetingDate">
          <a-date-picker
            v-model:value="formData.meetingDate"
            show-time
            value-format="YYYY-MM-DD HH:mm"
            placeholder="请选择会议日期"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="主持人" name="host">
          <a-input v-model:value="formData.host" placeholder="请输入主持人" />
        </a-form-item>
        <a-form-item label="参与人数" name="participants">
          <a-input-number
            v-model:value="formData.participants"
            :min="0"
            placeholder="请输入参与人数"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import type { MeetingItem, MeetingStatus, MeetingType, HumanQueryParams } from '@/types/human'
import {
  meetingApi,
  meetingStatusMap
} from '@/api/mock/human'

// 当前激活的标签页
const activeTab = ref<string>('all')

// 搜索表单
const searchForm = reactive({
  dateRange: null as [string, string] | null,
  type: undefined as MeetingType | undefined,
  status: undefined as MeetingStatus | undefined
})

// 表格数据
const tableData = ref<MeetingItem[]>([])
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
  { title: '会议主题', dataIndex: 'topic', key: 'topic', width: 180, ellipsis: true },
  { title: '会议类型', key: 'type', width: 100, align: 'center' as const },
  { title: '会议日期', dataIndex: 'meetingDate', key: 'meetingDate', width: 160 },
  { title: '主持人', dataIndex: 'host', key: 'host', width: 90 },
  { title: '参与人数', dataIndex: 'participants', key: 'participants', width: 90, align: 'center' as const },
  { title: '状态', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
])

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({
  topic: '',
  type: undefined as MeetingType | undefined,
  meetingDate: null as any,
  host: '',
  participants: undefined as number | undefined
})

const formRules = {
  topic: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择会议类型', trigger: 'change' }],
  meetingDate: [{ required: true, message: '请选择会议日期', trigger: 'change' }],
  host: [{ required: true, message: '请输入主持人', trigger: 'blur' }],
  participants: [{ required: true, message: '请输入参与人数', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: HumanQueryParams = {
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await meetingApi.getList(params)
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
  searchForm.dateRange = null
  searchForm.type = undefined
  searchForm.status = undefined
  activeTab.value = 'all'
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: { current?: number; pageSize?: number }) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

// 创建会议
const handleCreate = () => {
  resetForm()
  modalVisible.value = true
}

// 查看
const handleView = (record: MeetingItem) => {
  message.info(`查看会议: ${record.topic}`)
}

// 纪要
const handleMinutes = (record: MeetingItem) => {
  message.info(`查看纪要: ${record.topic}`)
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData: Partial<MeetingItem> = {
      topic: formData.topic,
      type: formData.type!,
      meetingDate: formData.meetingDate,
      host: formData.host,
      participants: formData.participants || 0
    }

    await meetingApi.create(submitData)
    message.success('创建成功')
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
  formData.topic = ''
  formData.type = undefined
  formData.meetingDate = null
  formData.host = ''
  formData.participants = undefined
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
  min-width: 900px;
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
    min-width: 800px;
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
    min-width: 700px;
  }

  .content-card :deep(.ant-tabs-nav) {
    overflow-x: auto;
    white-space: nowrap;
  }
}
</style>
