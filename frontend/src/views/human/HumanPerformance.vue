<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>绩效复盘</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建复盘
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="复盘周期">
          <a-date-picker
            v-model:value="searchForm.period"
            picker="month"
            value-format="YYYY-MM"
            placeholder="请选择复盘月份"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="searchForm.keyword" placeholder="搜索经理评语" allow-clear style="width: 150px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" html-type="submit"><SearchOutlined /> 查询</a-button>
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
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'employeeId'">
            {{ userName(record.employeeId) }}
          </template>
          <template v-else-if="column.key === 'reviewerId'">
            {{ userName(record.reviewerId) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建复盘弹窗 -->
    <a-modal v-model:open="modalVisible" title="新建复盘" @ok="handleModalOk" :confirm-loading="modalLoading" width="600px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 8 }" :wrapper-col="{ span: 14 }">
        <a-form-item label="被复盘员工" name="employeeId">
          <a-select v-model:value="formData.employeeId" placeholder="请选择被复盘员工" allow-clear show-search :filter-option="filterOption">
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="复盘人" name="reviewerId">
          <a-select v-model:value="formData.reviewerId" placeholder="请选择复盘人" allow-clear show-search :filter-option="filterOption">
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="复盘周期" name="reviewMonth">
          <a-date-picker
            v-model:value="formData.reviewMonth"
            picker="month"
            value-format="YYYY-MM"
            placeholder="请选择复盘月份"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="销售总额">
          <a-input-number v-model:value="formData.totalSalesAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="订单数">
          <a-input-number v-model:value="formData.salesOrderCount" :min="0" :precision="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="客单价">
          <a-input-number v-model:value="formData.avgOrderAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="新客销售额">
          <a-input-number v-model:value="formData.newCustomerSales" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="老客复购额">
          <a-input-number v-model:value="formData.oldCustomerRepurchaseSales" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="服务评分">
          <a-input-number v-model:value="formData.serviceScore" :min="0" :max="100" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="任务执行分">
          <a-input-number v-model:value="formData.taskExecutionScore" :min="0" :max="100" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="奖励金额">
          <a-input-number v-model:value="formData.rewardAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="处罚金额">
          <a-input-number v-model:value="formData.penaltyAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="经理评语" name="managerReview" class="form-item-full">
          <a-textarea v-model:value="formData.managerReview" :rows="3" placeholder="请输入经理评语" :maxlength="500" show-count />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="700px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="被复盘员工">{{ userName(detailRecord?.employeeId) }}</a-descriptions-item>
        <a-descriptions-item label="复盘人">{{ userName(detailRecord?.reviewerId) }}</a-descriptions-item>
        <a-descriptions-item label="复盘周期">{{ detailRecord?.reviewMonth }}</a-descriptions-item>
        <a-descriptions-item label="销售总额">¥{{ detailRecord?.totalSalesAmount ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="订单数">{{ detailRecord?.salesOrderCount ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="客单价">¥{{ detailRecord?.avgOrderAmount ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="新客销售额">¥{{ detailRecord?.newCustomerSales ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="老客复购额">¥{{ detailRecord?.oldCustomerRepurchaseSales ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="服务评分">{{ detailRecord?.serviceScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="任务执行分">{{ detailRecord?.taskExecutionScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="奖励金额">¥{{ detailRecord?.rewardAmount ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="处罚金额">¥{{ detailRecord?.penaltyAmount ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="经理评语" :span="2">{{ detailRecord?.managerReview || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { PerformanceItem } from '@/types/human'
import { performanceApi } from '@/api/human'
import { userApi } from '@/api/system'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

// 员工下拉（真实用户列表）
const userOptions = ref<{ id: number; name: string }[]>([])
// 用户 ID 映射为人名（找不到时回退显示 ID）
const userName = (id: number | undefined) => {
  if (!id) return '-'
  const u = userOptions.value.find((item) => item.id === Number(id))
  return u ? u.name : `用户#${id}`
}
const filterOption = (input: string, option: any) => {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}
const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 999 })
    userOptions.value = res.list.map((u) => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch {}
}

// 搜索表单：period → status 参数（后端用 status 过滤 reviewMonth）；keyword → 搜索经理评语
const searchForm = reactive({
  period: null as string | null,
  keyword: ''
})

// 表格数据
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => performanceApi.getList({
    keyword: params.keyword || undefined,
    status: params.period || undefined,
    page: params.page,
    pageSize: params.pageSize,
  }),
})

// 表格列配置
const columns = [
  { title: '经理评语', dataIndex: 'managerReview', key: 'managerReview', width: 200, ellipsis: true },
  { title: '复盘周期', dataIndex: 'reviewMonth', key: 'reviewMonth', width: 110 },
  { title: '被复盘员工', key: 'employeeId', width: 110 },
  { title: '复盘人', key: 'reviewerId', width: 110 },
  { title: '销售总额', dataIndex: 'totalSalesAmount', key: 'totalSalesAmount', width: 110 },
  { title: '订单数', dataIndex: 'salesOrderCount', key: 'salesOrderCount', width: 80, align: 'center' as const },
  { title: '操作', key: 'action', width: 90, fixed: 'right' as const }
]

// 详情弹窗
const { detailVisible, detailRecord, openDetail } = useDetailModal<PerformanceItem>()

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({
  employeeId: undefined as number | undefined,
  reviewerId: undefined as number | undefined,
  reviewMonth: null as string | null,
  totalSalesAmount: 0,
  salesOrderCount: 0,
  avgOrderAmount: 0,
  newCustomerSales: 0,
  oldCustomerRepurchaseSales: 0,
  serviceScore: 0,
  taskExecutionScore: 0,
  rewardAmount: 0,
  penaltyAmount: 0,
  managerReview: ''
})
const formRules = {
  employeeId: [{ required: true, message: '请选择被复盘员工', trigger: 'change' }],
  reviewerId: [{ required: true, message: '请选择复盘人', trigger: 'change' }],
  reviewMonth: [{ required: true, message: '请选择复盘周期', trigger: 'change' }],
  managerReview: [{ required: true, message: '请输入经理评语', trigger: 'blur' }]
}

const handleReset = () => { searchForm.period = null; searchForm.keyword = ''; handleSearch() }
const handleAdd = () => { resetForm(); modalVisible.value = true }
const handleView = (record: PerformanceItem) => { openDetail(record) }

const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    await performanceApi.create({
      employeeId: formData.employeeId,
      reviewerId: formData.reviewerId,
      reviewMonth: formData.reviewMonth ?? '',
      totalSalesAmount: formData.totalSalesAmount,
      salesOrderCount: formData.salesOrderCount,
      avgOrderAmount: formData.avgOrderAmount,
      newCustomerSales: formData.newCustomerSales,
      oldCustomerRepurchaseSales: formData.oldCustomerRepurchaseSales,
      serviceScore: formData.serviceScore,
      taskExecutionScore: formData.taskExecutionScore,
      rewardAmount: formData.rewardAmount,
      penaltyAmount: formData.penaltyAmount,
      managerReview: formData.managerReview,
    })
    message.success('创建成功'); modalVisible.value = false; loadData()
  } catch { console.error('表单验证失败') } finally { modalLoading.value = false }
}

const resetForm = () => {
  formData.employeeId = undefined
  formData.reviewerId = undefined
  formData.reviewMonth = null
  formData.totalSalesAmount = 0
  formData.salesOrderCount = 0
  formData.avgOrderAmount = 0
  formData.newCustomerSales = 0
  formData.oldCustomerRepurchaseSales = 0
  formData.serviceScore = 0
  formData.taskExecutionScore = 0
  formData.rewardAmount = 0
  formData.penaltyAmount = 0
  formData.managerReview = ''
}
onMounted(() => { loadUsers(); loadData() })
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
.action-link { font-size: 13px; color: #1890ff; padding: 2px 6px; border-radius: 4px; transition: all 0.2s; cursor: pointer; }
.action-link:hover { color: #40a9ff; background: #e6f7ff; }
.action-btns { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.action-btns :deep(.ant-divider-vertical) { margin: 0 2px; }
.form-item-full { grid-column: 1 / -1; }
.table-card :deep(.ant-table-wrapper) { overflow-x: auto; }
.table-card :deep(.ant-table) { min-width: 800px; }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; margin-bottom: 12px; }
  .table-card { padding: 12px; }
  .search-card { padding: 12px 16px; }
  .search-card :deep(.ant-form-item) { width: 100%; }
  .search-card :deep(.ant-form-item-control) { flex: 1; }
  .table-card :deep(.ant-table) { font-size: 13px; min-width: 600px; }
  .table-card :deep(.ant-table-thead > tr > th), .table-card :deep(.ant-table-tbody > tr > td) { padding: 10px 8px; }
}
@media (max-width: 576px) {
  .page-header { flex-direction: column; align-items: flex-start; }
  .page-header h2 { font-size: 18px; }
  .table-card :deep(.ant-table) { font-size: 12px; min-width: 500px; }
}
</style>
