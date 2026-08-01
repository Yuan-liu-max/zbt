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
              <a-divider type="vertical" />
              <a @click="handleMinutes(record)" class="action-link">纪要</a>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 创建弹窗 -->
    <a-modal v-model:open="modalVisible" title="创建会议" @ok="handleModalOk" :confirm-loading="modalLoading" width="560px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
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
        <a-descriptions-item v-for="(val, key) in detailRecord" :key="key" :label="String(key)" :span="typeof val === 'object' ? 2 : 1">
          {{ typeof val === 'object' ? JSON.stringify(val) : val }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { MeetingItem, MeetingType, HumanQueryParams } from '@/types/human'
import { meetingApi } from '@/api/human'

const searchForm = reactive({ dateRange: null as any, type: undefined as MeetingType | undefined })
const tableData = ref<MeetingItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showQuickJumper: true, showTotal: (total: number) => `共 ${total} 条` })

const columns = [
  { title: '会议主题', dataIndex: 'meetingDate', key: 'meetingDate', width: 160 },
  { title: '会议类型', key: 'meetingType', width: 100, align: 'center' as const },
  { title: '会议日期', dataIndex: 'meetingDate', key: 'meetingDate2', width: 160 },
  { title: '主持人', dataIndex: 'host', key: 'host', width: 90 },
  { title: '参与人数', dataIndex: 'participants', key: 'participants', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

const detailVisible = ref(false)
const detailRecord = ref<any>(null)

const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({ meetingType: undefined as MeetingType | undefined, meetingDate: null as any, host: '', participants: 0 })
const formRules = { meetingType: [{ required: true, message: '请选择会议类型', trigger: 'change' }], host: [{ required: true, message: '请输入主持人', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.current, pageSize: pagination.pageSize }
    const res: any = await meetingApi.getList(params)
    tableData.value = res.list || []; pagination.total = res.total || 0
  } catch { message.error('加载数据失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchForm.dateRange = null; searchForm.type = undefined; handleSearch() }
const handleTableChange = (pag: any) => { pagination.current = pag.current; pagination.pageSize = pag.size || 10; loadData() }
const handleView = (record: MeetingItem) => { detailRecord.value = record; detailVisible.value = true }
const handleMinutes = (record: MeetingItem) => {
  detailRecord.value = record
  detailVisible.value = true
}
const handleAdd = () => { resetForm(); modalVisible.value = true }
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    await meetingApi.create({ meetingType: formData.meetingType!, meetingDate: formData.meetingDate?.format?.('YYYY-MM-DD HH:mm:ss') || '', host: formData.host, participants: formData.participants, storeTargetAmount: 0, mainProducts: '', keyCustomers: '', todayStrategy: '', employeeTargets: {}, meetingPhotoUrls: [], status: 'completed' })
    message.success('创建成功'); modalVisible.value = false; loadData()
  } catch {} finally { modalLoading.value = false }
}
const resetForm = () => { formData.meetingType = undefined; formData.meetingDate = null; formData.host = ''; formData.participants = 0 }
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