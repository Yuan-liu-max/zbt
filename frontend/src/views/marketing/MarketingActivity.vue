<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>营销活动</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建活动
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="活动名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入活动名称" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="活动状态">
          <a-select v-model:value="searchForm.status" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option value="ongoing">进行中</a-select-option>
            <a-select-option value="ended">已结束</a-select-option>
            <a-select-option value="not_started">未开始</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="活动类型">
          <a-select v-model:value="searchForm.type" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option value="promotion">促销活动</a-select-option>
            <a-select-option value="new_customer">新客活动</a-select-option>
            <a-select-option value="theme">主题活动</a-select-option>
            <a-select-option value="membership">会员活动</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="活动时间">
          <a-range-picker v-model:value="searchForm.dateRange" style="width: 240px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit"><SearchOutlined /> 搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
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
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            {{ activityTypeMap[record.type] }}
          </template>
          <template v-if="column.key === 'time'">
            <div class="time-cell">
              <div>{{ record.startTime }}</div>
              <div>~</div>
              <div>{{ record.endTime }}</div>
            </div>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="activityStatusMap[record.status]?.color">
              {{ activityStatusMap[record.status]?.text }}
            </a-tag>
          </template>
          <template v-if="column.key === 'participants'">
            {{ record.registeredCount.toLocaleString() }} / {{ record.totalCount.toLocaleString() }}
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a-dropdown>
                <a class="action-link">更多 <DownOutlined /></a>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleView(record)">查看详情</a-menu-item>
                    <a-menu-item @click="handleDelete(record)">
                      <span class="danger-link">删除</span>
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑活动' : '新建活动'" @ok="handleModalOk" :confirm-loading="modalLoading" width="600px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="活动名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入活动名称" />
        </a-form-item>
        <a-form-item label="活动类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择活动类型">
            <a-select-option value="promotion">促销活动</a-select-option>
            <a-select-option value="new_customer">新客活动</a-select-option>
            <a-select-option value="theme">主题活动</a-select-option>
            <a-select-option value="membership">会员活动</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="活动时间" name="dateRange" required>
          <a-range-picker v-model:value="formData.dateRange" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="参与范围" name="scope">
          <a-select v-model:value="formData.scope" placeholder="请选择参与范围">
            <a-select-option value="全平台用户">全平台用户</a-select-option>
            <a-select-option value="新用户">新用户</a-select-option>
            <a-select-option value="会员用户">会员用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="参与人数" name="totalCount">
          <a-input-number v-model:value="formData.totalCount" :min="0" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { ActivityItem, ActivityQueryParams, ActivityStatus, ActivityType } from '@/types/marketing'
import { activityApi, activityStatusMap, activityTypeMap } from '@/api/mock/marketing'

const searchForm = reactive({
  name: '',
  status: undefined as ActivityStatus | undefined,
  type: undefined as ActivityType | undefined,
  dateRange: null as any
})

const tableData = ref<ActivityItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1, pageSize: 10, total: 0,
  showSizeChanger: true, showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '活动名称', dataIndex: 'name', key: 'name', width: 140 },
  { title: '活动类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '活动时间', key: 'time', width: 220 },
  { title: '活动状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '参与范围', dataIndex: 'scope', key: 'scope', width: 120 },
  { title: '报名/参与人数', key: 'participants', width: 140, align: 'center' as const },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
]

const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '', name: '', type: 'promotion' as ActivityType,
  dateRange: null as any, scope: '全平台用户', totalCount: 0
})
const formRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params: ActivityQueryParams = {
      name: searchForm.name || undefined, status: searchForm.status, type: searchForm.type,
      page: pagination.current, pageSize: pagination.pageSize
    }
    const res = await activityApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) { message.error('加载数据失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => {
  searchForm.name = ''; searchForm.status = undefined; searchForm.type = undefined; searchForm.dateRange = null; handleSearch()
}
const handleTableChange = (pag: any) => { pagination.current = pag.current; pagination.pageSize = pag.pageSize; loadData() }
const handleAdd = () => { isEdit.value = false; resetForm(); modalVisible.value = true }
const handleView = (record: ActivityItem) => { message.info(`查看 ${record.name} 详情`) }
const handleEdit = (record: ActivityItem) => {
  isEdit.value = true; formData.id = record.id; formData.name = record.name; formData.type = record.type
  formData.scope = record.scope; formData.totalCount = record.totalCount; modalVisible.value = true
}
const handleDelete = async (record: ActivityItem) => {
  try { await activityApi.delete(record.id); message.success('删除成功'); loadData() } catch { message.error('删除失败') }
}
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    const submitData = { name: formData.name, type: formData.type, scope: formData.scope, totalCount: formData.totalCount,
      startTime: formData.dateRange?.[0]?.format?.('YYYY-MM-DD HH:mm:ss') || '', endTime: formData.dateRange?.[1]?.format?.('YYYY-MM-DD HH:mm:ss') || '' }
    if (isEdit.value) { await activityApi.update(formData.id, submitData); message.success('更新成功') }
    else { await activityApi.create(submitData); message.success('创建成功') }
    modalVisible.value = false; loadData()
  } catch { console.error('表单验证失败') } finally { modalLoading.value = false }
}
const resetForm = () => { formData.id = ''; formData.name = ''; formData.type = 'promotion'; formData.dateRange = null; formData.scope = '全平台用户'; formData.totalCount = 0 }
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
.time-cell { font-size: 12px; color: #666; text-align: center; }
.action-link { font-size: 13px; color: #1890ff; padding: 2px 6px; border-radius: 4px; transition: all 0.2s; cursor: pointer; }
.action-link:hover { color: #40a9ff; background: #e6f7ff; }
.danger-link { color: #ff4d4f; }
.action-btns { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.action-btns :deep(.ant-divider-vertical) { margin: 0 2px; }
.table-card :deep(.ant-table-wrapper) { overflow-x: auto; }
.table-card :deep(.ant-table) { min-width: 900px; }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; margin-bottom: 12px; }
  .table-card { padding: 12px; }
  .search-card { padding: 12px 16px; }
  .search-card :deep(.ant-form-item) { width: 100%; }
  .search-card :deep(.ant-form-item-control) { flex: 1; }
  .table-card :deep(.ant-table) { font-size: 13px; min-width: 700px; }
  .table-card :deep(.ant-table-thead > tr > th), .table-card :deep(.ant-table-tbody > tr > td) { padding: 10px 8px; }
}
@media (max-width: 576px) {
  .page-header { flex-direction: column; align-items: flex-start; }
  .page-header h2 { font-size: 18px; }
  .table-card :deep(.ant-table) { font-size: 12px; min-width: 600px; }
}
</style>
