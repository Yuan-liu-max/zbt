<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>促销管理</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建促销
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="促销名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入促销名称" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="促销状态">
          <a-select v-model:value="searchForm.status" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option value="ongoing">进行中</a-select-option>
            <a-select-option value="ended">已结束</a-select-option>
            <a-select-option value="not_started">未开始</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="促销类型">
          <a-select v-model:value="searchForm.type" placeholder="全部" allow-clear style="width: 120px">
            <a-select-option value="discount">折扣</a-select-option>
            <a-select-option value="full_reduction">满减</a-select-option>
            <a-select-option value="gift">赠品</a-select-option>
            <a-select-option value="member_price">会员价</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="生效时间">
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
            {{ promotionTypeMap[record.type] }}
          </template>
          <template v-if="column.key === 'time'">
            <div class="time-cell">
              <div>{{ record.startTime }}</div>
              <div v-if="record.endTime">~ {{ record.endTime }}</div>
              <div v-else class="permanent">长期有效</div>
            </div>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="promotionStatusMap[record.status]?.color">
              {{ promotionStatusMap[record.status]?.text }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm title="确定要删除吗？" @confirm="handleDelete(record)">
                <a class="action-link danger">删除</a>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑促销' : '新建促销'" @ok="handleModalOk" :confirm-loading="modalLoading" width="600px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="促销名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入促销名称" />
        </a-form-item>
        <a-form-item label="促销类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择促销类型">
            <a-select-option value="discount">折扣</a-select-option>
            <a-select-option value="full_reduction">满减</a-select-option>
            <a-select-option value="gift">赠品</a-select-option>
            <a-select-option value="member_price">会员价</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优惠方式" name="discountMethod">
          <a-input v-model:value="formData.discountMethod" placeholder="请输入优惠方式" />
        </a-form-item>
        <a-form-item label="生效时间" name="dateRange" required>
          <a-range-picker v-model:value="formData.dateRange" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="适用范围" name="scope">
          <a-select v-model:value="formData.scope" placeholder="请选择适用范围">
            <a-select-option value="全场商品">全场商品</a-select-option>
            <a-select-option value="指定商品">指定商品</a-select-option>
            <a-select-option value="会员用户">会员用户</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="促销名称">{{ detailRecord?.name }}</a-descriptions-item>
        <a-descriptions-item label="促销类型">{{ promotionTypeMap[detailRecord?.type ?? ''] || detailRecord?.type }}</a-descriptions-item>
        <a-descriptions-item label="优惠方式">{{ detailRecord?.discountMethod }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ detailRecord?.startTime }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ detailRecord?.endTime || '长期有效' }}</a-descriptions-item>
        <a-descriptions-item label="促销状态">{{ promotionStatusMap[detailRecord?.status ?? '']?.text || detailRecord?.status }}</a-descriptions-item>
        <a-descriptions-item label="适用范围">{{ detailRecord?.scope }}</a-descriptions-item>
        <a-descriptions-item label="使用次数">{{ detailRecord?.usageCount ?? 0 }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { PromotionItem, PromotionStatus, PromotionType } from '@/types/marketing'
import { promotionApi, promotionStatusMap, promotionTypeMap } from '@/api/marketing'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

const searchForm = reactive({
  name: '',
  status: undefined as PromotionStatus | undefined,
  type: undefined as PromotionType | undefined,
  dateRange: null as any
})

// 表格数据（useCrudTable 封装，dateRange 转为 startDate/endDate 传参）
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => {
    const { dateRange, ...rest } = params as any
    return promotionApi.getList({
      ...rest,
      startDate: dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
      endDate: dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
    })
  },
  deleteFn: (id) => promotionApi.delete(id),
  onDeleteSuccess: () => message.success('删除成功'),
})

const columns = [
  { title: '促销名称', dataIndex: 'name', key: 'name', width: 130 },
  { title: '促销类型', dataIndex: 'type', key: 'type', width: 90 },
  { title: '优惠方式', dataIndex: 'discountMethod', key: 'discountMethod', width: 150 },
  { title: '生效时间', key: 'time', width: 220 },
  { title: '促销状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '适用范围', dataIndex: 'scope', key: 'scope', width: 110 },
  { title: '使用次数', dataIndex: 'usageCount', key: 'usageCount', width: 100, align: 'right' as const },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 130, fixed: 'right' as const }
]

// 详情弹窗（useDetailModal 封装）
const { detailVisible, detailRecord, openDetail } = useDetailModal<PromotionItem>()

const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '', name: '', type: 'discount' as PromotionType, discountMethod: '',
  dateRange: null as any, scope: '全场商品'
})
const formRules = {
  name: [{ required: true, message: '请输入促销名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择促销类型', trigger: 'change' }],
  discountMethod: [{ required: true, message: '请输入优惠方式', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择生效时间', trigger: 'change' }]
}

const handleReset = () => {
  searchForm.name = ''; searchForm.status = undefined; searchForm.type = undefined; searchForm.dateRange = null; handleSearch()
}
const handleAdd = () => { isEdit.value = false; resetForm(); modalVisible.value = true }
const handleView = openDetail
const handleEdit = (record: PromotionItem) => {
  isEdit.value = true; formData.id = record.id; formData.name = record.name; formData.type = record.type
  formData.discountMethod = record.discountMethod; formData.scope = record.scope
  formData.dateRange = record.startTime && record.endTime
    ? [dayjs(record.startTime), dayjs(record.endTime)]
    : null
  modalVisible.value = true
}
const handleDelete = async (record: PromotionItem) => {
  try { await promotionApi.delete(record.id); message.success('删除成功'); loadData() } catch (error) { console.error('删除失败', error) }
}
// 按时间计算促销状态（与前端 statusMap 保持一致）
const calcStatus = (startTime: string, endTime: string): PromotionStatus => {
  const now = dayjs()
  if (startTime && now.isBefore(dayjs(startTime))) return 'not_started'
  if (endTime && now.isAfter(dayjs(endTime))) return 'ended'
  return 'ongoing'
}
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    const startTime = formData.dateRange?.[0]?.format?.('YYYY-MM-DD HH:mm:ss') || ''
    const endTime = formData.dateRange?.[1]?.format?.('YYYY-MM-DD HH:mm:ss') || ''
    const submitData = { name: formData.name, type: formData.type, discountMethod: formData.discountMethod, scope: formData.scope,
      startTime, endTime, status: calcStatus(startTime, endTime) }
    if (isEdit.value) { await promotionApi.update(formData.id, submitData); message.success('更新成功') }
    else { await promotionApi.create(submitData); message.success('创建成功') }
    modalVisible.value = false; loadData()
  } catch { message.error('操作失败') } finally { modalLoading.value = false }
}
const resetForm = () => { formData.id = ''; formData.name = ''; formData.type = 'discount'; formData.discountMethod = ''; formData.dateRange = null; formData.scope = '全场商品' }
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
.time-cell { font-size: 12px; color: #666; }
.permanent { color: #52c41a; font-weight: 500; }
.action-link { font-size: 13px; color: #1890ff; padding: 2px 6px; border-radius: 4px; transition: all 0.2s; cursor: pointer; }
.action-link:hover { color: #40a9ff; background: #e6f7ff; }
.action-link.danger { color: #ff4d4f; }
.action-link.danger:hover { color: #ff7875; background: #fff1f0; }
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
