<template>
  <div class="page-container">
    <div class="page-header">
      <h2>晨夕会</h2>
      <a-button type="primary" @click="handleAdd"><PlusOutlined /> 创建会议</a-button>
    </div>

    <div class="content-card">
      <!-- 搜索 -->
      <div class="search-card">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="会议日期">
            <a-range-picker v-model:value="searchForm.dateRange" style="width: 240px" />
          </a-form-item>
          <a-form-item label="会议类型">
            <a-select v-model:value="searchForm.type" placeholder="全部" allow-clear style="width: 130px">
              <a-select-option value="MORNING">晨会</a-select-option>
              <a-select-option value="EVENING">夕会</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button @click="handleReset">重置</a-button>
              <a-button type="primary" html-type="submit"><SearchOutlined /> 查询</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 表格 -->
      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="pagination" @change="handleTableChange" row-key="id" :scroll="{ x: 700 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'meetingType'">
            <a-tag :color="record.meetingType === 'MORNING' ? 'blue' : 'orange'">
              {{ record.meetingType === 'MORNING' ? '晨会' : '夕会' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 创建弹窗 -->
    <a-modal v-model:open="modalVisible" title="创建会议" @ok="handleModalOk" :confirm-loading="modalLoading" width="560px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="会议主题" name="topic">
          <a-input v-model:value="formData.topic" placeholder="请输入会议主题" />
        </a-form-item>
        <a-form-item label="会议类型" name="meetingType">
          <a-select v-model:value="formData.meetingType" placeholder="请选择会议类型">
            <a-select-option value="MORNING">晨会</a-select-option>
            <a-select-option value="EVENING">夕会</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会议日期" name="meetingDate">
          <a-date-picker v-model:value="formData.meetingDate" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="主持人" name="host">
          <a-input v-model:value="formData.host" placeholder="请输入主持人" />
        </a-form-item>
        <a-form-item label="参与人数" name="participants">
          <a-input-number v-model:value="formData.participants" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="会议主题">{{ detailRecord?.topic }}</a-descriptions-item>
        <a-descriptions-item label="会议类型">{{ detailRecord?.meetingType === 'MORNING' ? '晨会' : '夕会' }}</a-descriptions-item>
        <a-descriptions-item label="会议日期">{{ detailRecord?.meetingDate }}</a-descriptions-item>
        <a-descriptions-item label="主持人">{{ detailRecord?.host }}</a-descriptions-item>
        <a-descriptions-item label="参与人员">{{ detailRecord?.participants }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ detailRecord ? getMeetingStatus(detailRecord).text : '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { MeetingItem, MeetingType } from '@/types/human'
import { meetingApi } from '@/api/human'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

const searchForm = reactive({ dateRange: null as any, type: undefined as MeetingType | undefined })

const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => meetingApi.getList({
    page: params.page,
    pageSize: params.pageSize,
    meetingType: params.type,
    ...(params.dateRange?.[0] ? {
      startDate: params.dateRange[0].format('YYYY-MM-DD'),
      endDate: params.dateRange[1].format('YYYY-MM-DD'),
    } : {}),
  }),
})

const { detailVisible, detailRecord, openDetail } = useDetailModal<MeetingItem>()

const columns = [
  { title: '会议主题', dataIndex: 'topic', key: 'topic', width: 160 },
  { title: '会议类型', key: 'meetingType', width: 100, align: 'center' as const },
  { title: '会议日期', dataIndex: 'meetingDate', key: 'meetingDate2', width: 160 },
  { title: '主持人', dataIndex: 'host', key: 'host', width: 90 },
  { title: '参与人数', dataIndex: 'participants', key: 'participants', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({ topic: '', meetingType: undefined as MeetingType | undefined, meetingDate: null as any, host: '', participants: 0 })
const formRules = { topic: [{ required: true, message: '请输入会议主题', trigger: 'blur' }], meetingType: [{ required: true, message: '请选择会议类型', trigger: 'change' }], host: [{ required: true, message: '请输入主持人', trigger: 'blur' }] }

const handleReset = () => { searchForm.dateRange = null; searchForm.type = undefined; handleSearch() }
const handleView = (record: MeetingItem) => { openDetail(record) }
const handleAdd = () => { resetForm(); modalVisible.value = true }

// 根据会议日期 + 当前时间动态计算状态，解决状态不自动更新的问题
const getMeetingStatus = (record: MeetingItem): { text: string; color: string } => {
  // 已取消优先（手动取消）
  if (record.status === 'cancelled') return { text: '已取消', color: 'red' }
  // 显式标记为已结束
  if (record.status === 'ended') return { text: '已结束', color: 'default' }
  // 按会议日期动态判断
  if (record.meetingDate) {
    const date = dayjs(record.meetingDate)
    const now = dayjs()
    if (date.isBefore(now)) return { text: '已结束', color: 'default' }
    if (date.isAfter(now)) return { text: '未开始', color: 'orange' }
  }
  return { text: '进行中', color: 'green' }
}

const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    // 创建时根据会议日期计算初始状态：过去=已结束，未来/今天=进行中
    const meetingDate = formData.meetingDate?.format?.('YYYY-MM-DD HH:mm:ss') || ''
    const initialStatus = meetingDate && dayjs(meetingDate).isBefore(dayjs()) ? 'ended' : 'ongoing'
    await meetingApi.create({ topic: formData.topic, meetingType: formData.meetingType!, meetingDate, host: formData.host, participants: String(formData.participants), status: initialStatus })
    message.success('创建成功'); modalVisible.value = false; loadData()
  } catch {} finally { modalLoading.value = false }
}
const resetForm = () => { formData.topic = ''; formData.meetingType = undefined; formData.meetingDate = null; formData.host = ''; formData.participants = 0 }
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
.action-link { font-size: 13px; color: #1890ff; padding: 2px 6px; border-radius: 4px; cursor: pointer; }
.action-link:hover { color: #40a9ff; background: #e6f7ff; }
.action-btns { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.table-card :deep(.ant-table-wrapper) { overflow-x: auto; }
.table-card :deep(.ant-table) { min-width: 600px; }
@media (max-width: 768px) { .page-container { padding: 16px; } .content-card { padding: 16px; margin-bottom: 12px; } .search-card :deep(.ant-form-item) { width: 100%; } .search-card :deep(.ant-form-item-control) { flex: 1; } .table-card :deep(.ant-table) { font-size: 13px; min-width: 500px; } }
@media (max-width: 576px) { .page-header { flex-direction: column; align-items: flex-start; } .page-header h2 { font-size: 18px; } }
</style>