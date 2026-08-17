<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>采购列表</h2>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="采购单号">
          <a-input
            v-model:value="searchForm.purchaseNo"
            placeholder="请输入采购单号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择"
            allow-clear
            style="width: 140px"
          >
            <a-select-option v-for="(val, key) in purchaseStatusMap" :key="key" :value="key">
              {{ val.text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" html-type="submit">查询</a-button>
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
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 采购单号 -->
          <template v-if="column.key === 'orderNo'">
            <a @click="handleDetail(record)">{{ record.orderNo }}</a>
          </template>
          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">
              {{ statusText(record.status) }}
            </a-tag>
          </template>
          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleDetail(record)" class="action-link">详情</a>
              <template v-if="record.status === 'DRAFT'">
                <a-divider type="vertical" />
                <a @click="handleSubmit(record)" class="action-link">提交</a>
                <a-divider type="vertical" />
                <a @click="handleCancel(record)" class="action-link">取消</a>
                <a-divider type="vertical" />
                <a-popconfirm title="确定要删除该采购单吗？" @confirm="handleDelete(record)">
                  <a class="action-link danger">删除</a>
                </a-popconfirm>
              </template>
              <template v-if="record.status === 'SUBMITTED'">
                <a-divider type="vertical" />
                <a @click="openAuditModal(record)" class="action-link">审核</a>
                <a-divider type="vertical" />
                <a @click="handleCancel(record)" class="action-link">取消</a>
              </template>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" title="采购单详情" :footer="null" width="700px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="采购单号" :span="2">{{ detailRecord?.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="供应商ID">{{ detailRecord?.supplierId ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="申请人ID">{{ detailRecord?.applicantId ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detailRecord?.createdAt }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detailRecord?.status)">
            {{ statusText(detailRecord?.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="总金额" :span="2">
          <span class="amount-text">¥ {{ (detailRecord?.totalAmount || 0).toFixed(2) }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">{{ detailRecord?.remark || '-' }}</a-descriptions-item>
      </a-descriptions>

      <!-- 商品明细 -->
      <div class="detail-items-title">商品明细</div>
      <a-table
        :columns="detailItemColumns"
        :data-source="detailRecord?.items || []"
        :pagination="false"
        row-key="id"
        size="small"
        bordered
      />
    </a-modal>

    <!-- 审核弹窗 -->
    <a-modal
      v-model:open="auditVisible"
      title="审核采购单"
      @ok="handleAuditOk"
      :confirm-loading="auditLoading"
      width="500px"
    >
      <div class="audit-info">
        <p><strong>采购单号：</strong>{{ auditRecord?.orderNo }}</p>
        <p><strong>供应商ID：</strong>{{ auditRecord?.supplierId ?? '-' }}</p>
        <p><strong>总金额：</strong>¥ {{ (auditRecord?.totalAmount || 0).toFixed(2) }}</p>
      </div>
      <a-form :model="auditForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="审核结果" required>
          <a-radio-group v-model:value="auditForm.status">
            <a-radio value="APPROVED">
              <a-tag color="green">通过</a-tag>
            </a-radio>
            <a-radio value="REJECTED">
              <a-tag color="red">拒绝</a-tag>
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="审核意见">
          <a-textarea
            v-model:value="auditForm.auditRemark"
            :rows="3"
            placeholder="请输入审核意见（选填）"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { purchaseApi, purchaseItemApi } from '@/api/purchase'
import type { PurchaseRecord, PurchaseStatus } from '@/types/purchase'
import { purchaseStatusMap } from '@/types/purchase'

// 状态映射辅助函数
const statusText = (status?: PurchaseStatus): string => {
  if (!status) return '-'
  return purchaseStatusMap[status]?.text || status
}

const statusColor = (status?: PurchaseStatus): string => {
  if (!status) return 'default'
  return purchaseStatusMap[status]?.color || 'default'
}

// 搜索表单
const searchForm = reactive({
  purchaseNo: '',
  status: undefined as PurchaseStatus | undefined,
})

// 表格数据
const tableData = ref<PurchaseRecord[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列
const columns = [
  { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo', width: 190 },
  { title: '供应商ID', dataIndex: 'supplierId', key: 'supplierId', width: 100 },
  { title: '申请人ID', dataIndex: 'applicantId', key: 'applicantId', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, align: 'right' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const },
]

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<PurchaseRecord | null>(null)

const detailItemColumns = [
  { title: '商品ID', dataIndex: 'productId', key: 'productId', width: 100 },
  { title: '商品名称', dataIndex: 'productName', key: 'productName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80, align: 'center' as const },
  { title: '单价(元)', dataIndex: 'price', key: 'price', width: 120, align: 'right' as const },
]

// 审核弹窗
const auditVisible = ref(false)
const auditLoading = ref(false)
const auditRecord = ref<PurchaseRecord | null>(null)
const auditForm = reactive({
  status: 'APPROVED' as 'APPROVED' | 'REJECTED',
  auditRemark: '',
})

// 加载列表数据
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      purchaseNo: searchForm.purchaseNo || undefined,
      status: searchForm.status,
      page: pagination.current,
      pageSize: pagination.pageSize,
    }
    const res = await purchaseApi.getList(params)
    tableData.value = res.list || []
    pagination.total = res.total || 0
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
  searchForm.purchaseNo = ''
  searchForm.status = undefined
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 详情（采购单 + 明细）
const handleDetail = async (record: PurchaseRecord) => {
  try {
    const detail = await purchaseApi.getById(record.id)
    detailRecord.value = { ...(detail || record) }
    const items = await purchaseItemApi.getList(String(record.id))
    detailRecord.value.items = items || []
    detailVisible.value = true
  } catch {
    detailRecord.value = record
    detailVisible.value = true
  }
}

// 提交审核
const handleSubmit = async (record: PurchaseRecord) => {
  try {
    await purchaseApi.submit(record.id)
    message.success('已提交审核')
    loadData()
  } catch (error) {
    console.error('提交失败', error)
  }
}

// 取消
const handleCancel = async (record: PurchaseRecord) => {
  try {
    await purchaseApi.cancel(record.id)
    message.success('已取消')
    loadData()
  } catch (error) {
    console.error('取消失败', error)
  }
}

// 删除
const handleDelete = async (record: PurchaseRecord) => {
  try {
    await purchaseApi.delete(record.id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

// 打开审核弹窗
const openAuditModal = (record: PurchaseRecord) => {
  auditRecord.value = record
  auditForm.status = 'APPROVED'
  auditForm.auditRemark = ''
  auditVisible.value = true
}

// 审核提交
const handleAuditOk = async () => {
  if (!auditRecord.value) return

  auditLoading.value = true
  try {
    if (auditForm.status === 'APPROVED') {
      await purchaseApi.approve(auditRecord.value.id)
      message.success('审核通过')
    } else {
      await purchaseApi.reject(auditRecord.value.id, auditForm.auditRemark)
      message.success('已拒绝')
    }
    auditVisible.value = false
    loadData()
  } catch (error) {
    console.error('审核操作失败', error)
  } finally {
    auditLoading.value = false
  }
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
  padding: 16px 24px;
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

.action-link.danger {
  color: #ff4d4f;
}

.amount-text {
  font-weight: 700;
  color: #ff4d4f;
  font-size: 15px;
}

.detail-items-title {
  font-size: 15px;
  font-weight: 600;
  margin: 20px 0 12px;
  padding-left: 10px;
  border-left: 3px solid #1890ff;
}

.audit-info {
  background: #fafafa;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.audit-info p {
  margin: 4px 0;
  color: #333;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 900px;
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

  .search-card {
    padding: 12px 16px;
  }

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
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
  .page-header h2 {
    font-size: 18px;
  }

  .table-card :deep(.ant-table) {
    font-size: 12px;
    min-width: 800px;
  }
}
</style>
