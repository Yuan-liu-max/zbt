<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>财务管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">总收入</div>
          <div class="stat-value">¥ {{ financeStats.totalIncome.toLocaleString() }}</div>
        </div>
        <div class="stat-icon income">
          <DollarOutlined />
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">总支出</div>
          <div class="stat-value">¥ {{ financeStats.totalExpense.toLocaleString() }}</div>
        </div>
        <div class="stat-icon expense">
          <FallOutlined />
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">净利润</div>
          <div class="stat-value">¥ {{ financeStats.netProfit.toLocaleString() }}</div>
        </div>
        <div class="stat-icon profit">
          <RiseOutlined />
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="content-card">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 收支流水 -->
        <a-tab-pane key="transaction" tab="收支流水">
          <!-- 搜索表单 -->
          <div class="search-bar">
            <a-form layout="inline" :model="searchForm" @finish="handleSearch">
              <a-form-item label="全部类型">
                <a-select v-model:value="searchForm.type" placeholder="全部类型" allow-clear style="width: 120px">
                  <a-select-option value="income">收入</a-select-option>
                  <a-select-option value="expense">支出</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="日期范围">
                <a-range-picker v-model:value="searchForm.dateRange" style="width: 240px" />
              </a-form-item>
              <a-form-item label="交易账户">
                <a-select v-model:value="searchForm.account" placeholder="交易账户" allow-clear style="width: 150px">
                  <a-select-option value="工商银行（1234）">工商银行（1234）</a-select-option>
                  <a-select-option value="建设银行（5678）">建设银行（5678）</a-select-option>
                  <a-select-option value="支付宝账户">支付宝账户</a-select-option>
                  <a-select-option value="微信支付">微信支付</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="关联对象">
                <a-input v-model:value="searchForm.relatedObject" placeholder="关联对象" allow-clear style="width: 150px" />
              </a-form-item>
              <a-form-item label="关键词">
                <a-input v-model:value="searchForm.keyword" placeholder="请输入关键词" allow-clear style="width: 150px" />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button @click="handleReset">重置</a-button>
                  <a-button @click="handleExport">导出</a-button>
                  <a-button type="primary" @click="handleAdd">
                    <PlusOutlined /> 新增记录
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <!-- 数据表格 -->
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
                <a-tag :color="record.type === 'income' ? 'green' : 'red'">
                  {{ record.type === 'income' ? '收入' : '支出' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'amount'">
                <span :class="record.type === 'income' ? 'amount-income' : 'amount-expense'">
                  {{ record.type === 'income' ? '+' : '-' }} ¥{{ record.amount.toLocaleString() }}
                </span>
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
        </a-tab-pane>

        <!-- 其他标签页占位 -->
        <a-tab-pane key="receivable" tab="应收账款">
          <a-empty description="应收账款功能即将上线" />
        </a-tab-pane>
        <a-tab-pane key="payable" tab="应付账款">
          <a-empty description="应付账款功能即将上线" />
        </a-tab-pane>
        <a-tab-pane key="account" tab="账户管理">
          <a-empty description="账户管理功能即将上线" />
        </a-tab-pane>
        <a-tab-pane key="expense" tab="费用管理">
          <a-empty description="费用管理功能即将上线" />
        </a-tab-pane>
        <a-tab-pane key="invoice" tab="发票管理">
          <a-empty description="发票管理功能即将上线" />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑记录' : '新增记录'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="500px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="交易类型" name="type">
          <a-radio-group v-model:value="formData.type">
            <a-radio value="income">收入</a-radio>
            <a-radio value="expense">支出</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="交易账户" name="account">
          <a-select v-model:value="formData.account" placeholder="请选择交易账户">
            <a-select-option value="工商银行（1234）">工商银行（1234）</a-select-option>
            <a-select-option value="建设银行（5678）">建设银行（5678）</a-select-option>
            <a-select-option value="支付宝账户">支付宝账户</a-select-option>
            <a-select-option value="微信支付">微信支付</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关联对象" name="relatedObject">
          <a-input v-model:value="formData.relatedObject" placeholder="请输入关联对象" />
        </a-form-item>
        <a-form-item label="交易金额" name="amount">
          <a-input-number v-model:value="formData.amount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="交易日期" name="transactionDate">
          <a-date-picker v-model:value="formData.transactionDate" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="流水号">{{ detailRecord?.code }}</a-descriptions-item>
        <a-descriptions-item label="交易类型">{{ detailRecord?.type === 'income' ? '收入' : '支出' }}</a-descriptions-item>
        <a-descriptions-item label="交易账户">{{ detailRecord?.account }}</a-descriptions-item>
        <a-descriptions-item label="关联对象">{{ detailRecord?.relatedObject }}</a-descriptions-item>
        <a-descriptions-item label="交易金额">¥{{ detailRecord?.amount }}</a-descriptions-item>
        <a-descriptions-item label="交易日期">{{ detailRecord?.transactionDate }}</a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailRecord?.remark || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined, DollarOutlined, FallOutlined, RiseOutlined } from '@ant-design/icons-vue'
import type { FinanceStats, TransactionRecord, TransactionQueryParams, TransactionType } from '@/types/finance'
import { financeApi } from '@/api/finance'
import { exportComingSoon } from '@/utils/export'

// 当前标签页
const activeTab = ref('transaction')

// 财务统计
const financeStats = reactive<FinanceStats>({
  totalIncome: 0, totalExpense: 0, netProfit: 0
})

// 搜索表单
const searchForm = reactive({
  type: undefined as TransactionType | undefined,
  dateRange: null as any,
  account: undefined as string | undefined,
  relatedObject: '',
  keyword: ''
})

// 表格数据
const tableData = ref<TransactionRecord[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = [
  { title: '流水号', dataIndex: 'code', key: 'code', width: 160 },
  { title: '交易类型', dataIndex: 'type', key: 'type', width: 80, align: 'center' as const },
  { title: '交易账户', dataIndex: 'account', key: 'account', width: 140 },
  { title: '关联对象', dataIndex: 'relatedObject', key: 'relatedObject', width: 180 },
  { title: '交易金额', dataIndex: 'amount', key: 'amount', width: 130, align: 'right' as const },
  { title: '交易日期', dataIndex: 'transactionDate', key: 'transactionDate', width: 160 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 180 },
  { title: '操作', key: 'action', width: 140, fixed: 'right' as const }
]

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<TransactionRecord | null>(null)

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  type: 'income' as TransactionType,
  account: '',
  relatedObject: '',
  amount: 0,
  transactionDate: null as any,
  remark: ''
})

const formRules = {
  type: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
  account: [{ required: true, message: '请选择交易账户', trigger: 'change' }],
  relatedObject: [{ required: true, message: '请输入关联对象', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入交易金额', trigger: 'blur' }],
  transactionDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }]
}

// 加载统计
const loadStats = async () => {
  try {
    const stats = await financeApi.getStats()
    Object.assign(financeStats, stats)
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: TransactionQueryParams = {
      type: searchForm.type,
      startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
      endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
      account: searchForm.account,
      relatedObject: searchForm.relatedObject || undefined,
      keyword: searchForm.keyword || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await financeApi.getTransactions(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('加载数据失败', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.type = undefined
  searchForm.dateRange = null
  searchForm.account = undefined
  searchForm.relatedObject = ''
  searchForm.keyword = ''
  handleSearch()
}

// 导出
const handleExport = () => {
  exportComingSoon('财务数据')
}

// 分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 查看
const handleView = (record: TransactionRecord) => {
  detailRecord.value = record
  detailVisible.value = true
}

// 编辑
const handleEdit = (record: TransactionRecord) => {
  isEdit.value = true
  formData.id = record.id
  formData.type = record.type
  formData.account = record.account
  formData.relatedObject = record.relatedObject
  formData.amount = record.amount
  formData.transactionDate = record.transactionDate ? dayjs(record.transactionDate) : null
  formData.remark = record.remark
  modalVisible.value = true
}

// 删除
const handleDelete = async (record: TransactionRecord) => {
  try {
    await financeApi.delete(record.id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      type: formData.type,
      account: formData.account,
      relatedObject: formData.relatedObject,
      amount: formData.amount,
      transactionDate: formData.transactionDate?.format?.('YYYY-MM-DD HH:mm:ss') || new Date().toISOString().replace('T', ' ').slice(0, 19),
      remark: formData.remark
    }

    if (isEdit.value) {
      await financeApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await financeApi.create(submitData)
      message.success('新增成功')
    }

    modalVisible.value = false
    loadData()
  } catch (error) {
    console.error('表单验证失败', error)
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.type = 'income'
  formData.account = ''
  formData.relatedObject = ''
  formData.amount = 0
  formData.transactionDate = null
  formData.remark = ''
}

onMounted(() => {
  loadStats()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
}

.stat-trend.up { color: #52c41a; }
.stat-trend.down { color: #ff4d4f; }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-icon.income { background: #e6f7ff; color: #1890ff; }
.stat-icon.expense { background: #fff7e6; color: #fa8c16; }
.stat-icon.profit { background: #f6ffed; color: #52c41a; }

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.search-bar {
  margin-bottom: 16px;
}

.search-bar :deep(.ant-form-item) {
  margin-bottom: 12px;
}

.amount-income {
  color: #52c41a;
  font-weight: 500;
}

.amount-expense {
  color: #ff4d4f;
  font-weight: 500;
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

.action-link.danger { color: #ff4d4f; }
.action-link.danger:hover { color: #ff7875; background: #fff1f0; }

.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-btns :deep(.ant-divider-vertical) { margin: 0 2px; }

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .page-container { padding: 16px; }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card { padding: 16px; }
  .stat-value { font-size: 18px; }
  .stat-icon { width: 40px; height: 40px; font-size: 18px; }

  .content-card { padding: 16px; }

  .search-bar :deep(.ant-form-item) {
    width: 100%;
  }

  .search-bar :deep(.ant-form-item-control) {
    flex: 1;
  }
}

@media (max-width: 576px) {
  .page-header h2 { font-size: 18px; }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: row-reverse;
    justify-content: space-between;
  }
}
</style>
