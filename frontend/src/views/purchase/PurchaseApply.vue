<template>
  <div class="page-container">
    <!-- 面包屑 -->
    <a-breadcrumb class="page-breadcrumb">
      <a-breadcrumb-item>采购管理</a-breadcrumb-item>
      <a-breadcrumb-item>采购申请</a-breadcrumb-item>
    </a-breadcrumb>

    <!-- 页面标题 -->
    <div class="page-header">
      <h2>采购申请</h2>
    </div>

    <!-- 基本信息 -->
    <div class="content-card">
      <div class="section-title">基本信息</div>
      <a-form
        ref="basicFormRef"
        :model="basicForm"
        :rules="basicRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 18 }"
        layout="horizontal"
        class="apply-form"
      >
        <a-form-item label="申请人" name="applicantId">
          <a-select
            v-model:value="basicForm.applicantId"
            placeholder="请选择申请人"
            show-search
            :filter-option="filterOption"
          >
            <a-select-option v-for="u in users" :key="u.id" :value="u.id">
              {{ u.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="门店" name="storeId">
          <a-select
            v-model:value="basicForm.storeId"
            placeholder="请选择门店"
            show-search
            :filter-option="filterOption"
          >
            <a-select-option v-for="s in stores" :key="s.id" :value="s.id">
              {{ s.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="供应商" name="supplierId">
          <a-select
            v-model:value="basicForm.supplierId"
            placeholder="请选择供应商"
            show-search
            :filter-option="filterOption"
          >
            <a-select-option v-for="s in suppliers" :key="s.id" :value="s.id">
              {{ s.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="basicForm.remark"
            :rows="3"
            placeholder="请输入备注信息（选填）"
          />
        </a-form-item>
      </a-form>
    </div>

    <!-- 商品明细 -->
    <div class="content-card">
      <div class="section-title">商品明细</div>
      <div class="table-wrapper">
        <a-table
          :columns="itemColumns"
          :data-source="items"
          :pagination="false"
          row-key="_key"
          :scroll="{ x: 700 }"
          size="small"
          bordered
        >
          <template #bodyCell="{ column, record, index }">
            <!-- 商品名称 -->
            <template v-if="column.key === 'productName'">
              <a-input
                v-model:value="record.productName"
                placeholder="请输入商品名称"
                size="small"
              />
            </template>
            <!-- 数量 -->
            <template v-if="column.key === 'quantity'">
              <a-input-number
                v-model:value="record.quantity"
                :min="1"
                :precision="0"
                style="width: 100%"
                size="small"
                @change="calcSubtotal(index)"
              />
            </template>
            <!-- 单价 -->
            <template v-if="column.key === 'price'">
              <a-input-number
                v-model:value="record.price"
                :min="0"
                :precision="2"
                style="width: 100%"
                size="small"
                @change="calcSubtotal(index)"
              />
            </template>
            <!-- 小计 -->
            <template v-if="column.key === 'subtotal'">
              <span class="subtotal-text">{{ formatMoney(record.subtotal) }}</span>
            </template>
            <!-- 操作 -->
            <template v-if="column.key === 'action'">
              <a-button
                type="link"
                danger
                size="small"
                :disabled="items.length <= 1"
                @click="removeItem(index)"
              >
                删除
              </a-button>
            </template>
          </template>
        </a-table>
      </div>
      <div class="add-row-btn">
        <a-button type="dashed" block @click="addItem">
          <PlusOutlined /> 添加商品
        </a-button>
      </div>
    </div>

    <!-- 费用汇总 -->
    <div class="content-card summary-card">
      <div class="summary-row">
        <span class="summary-label">商品总额：</span>
        <span class="summary-value">{{ formatMoney(totalAmount) }}</span>
      </div>
      <div class="summary-actions">
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">
          提交申请
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { purchaseApi } from '@/api/purchase'
import { supplierApi } from '@/api/supplier'
import { userApi } from '@/api/system'
import type { SupplierItem } from '@/types/supplier'
import request from '@/utils/request'

const router = useRouter()

interface OptionItem {
  id: number
  name: string
}

// 基本信息表单
const basicFormRef = ref()
const basicForm = reactive({
  applicantId: undefined as number | undefined,
  storeId: undefined as number | undefined,
  supplierId: undefined as number | undefined,
  remark: '',
})

const basicRules = {
  applicantId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
}

// 申请人 / 门店 / 供应商下拉
const users = ref<OptionItem[]>([])
const stores = ref<OptionItem[]>([])
const suppliers = ref<SupplierItem[]>([])

const filterOption = (input: string, option: any) => {
  return option.children?.[0]?.toLowerCase().includes(input.toLowerCase()) ?? false
}

const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 100 })
    users.value = (res.list || []).map(u => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch (error) {
    console.error('加载用户失败', error)
  }
}

const loadStores = async () => {
  try {
    const list: any[] = await request.get('/stores/all')
    stores.value = (list || []).map(s => ({ id: Number(s.id), name: s.name || `门店${s.id}` }))
  } catch (error) {
    console.error('加载门店失败', error)
  }
}

const loadSuppliers = async () => {
  try {
    const res = await supplierApi.getList({ page: 1, pageSize: 100 })
    suppliers.value = res.list || []
  } catch (error) {
    console.error('加载供应商失败', error)
  }
}

// 商品明细（后端 PurchaseItem：productName / quantity / price）
let keyCounter = 0
interface ApplyItem {
  _key: number
  productName: string
  quantity: number
  price: number
  subtotal: number
}
const createEmptyItem = (): ApplyItem => ({
  _key: ++keyCounter,
  productName: '',
  quantity: 1,
  price: 0,
  subtotal: 0,
})

const items = ref<ApplyItem[]>([createEmptyItem()])

// 表格列配置
const itemColumns = [
  { title: '商品名称', dataIndex: 'productName', key: 'productName', width: 200 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 120, align: 'center' as const },
  { title: '单价(元)', dataIndex: 'price', key: 'price', width: 140, align: 'right' as const },
  { title: '小计(元)', dataIndex: 'subtotal', key: 'subtotal', width: 140, align: 'right' as const },
  { title: '操作', key: 'action', width: 80, align: 'center' as const },
]

// 计算小计
const calcSubtotal = (index: number) => {
  const item = items.value[index]
  item.subtotal = (item.quantity || 0) * (item.price || 0)
}

// 总金额
const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + (item.subtotal || 0), 0)
})

// 添加商品行
const addItem = () => {
  items.value.push(createEmptyItem())
}

// 删除商品行
const removeItem = (index: number) => {
  if (items.value.length <= 1) return
  items.value.splice(index, 1)
}

// 格式化金额
const formatMoney = (val: number) => {
  return `¥ ${(val || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`
}

// 提交中状态
const submitting = ref(false)

// 提交申请
const handleSubmit = async () => {
  try {
    await basicFormRef.value?.validateFields()
  } catch {
    message.warning('请完善基本信息')
    return
  }

  // 校验商品明细
  const validItems = items.value.filter((item) => item.productName.trim())
  if (validItems.length === 0) {
    message.warning('请至少填写一项商品')
    return
  }

  for (const item of validItems) {
    if (!item.productName.trim()) {
      message.warning('商品名称不能为空')
      return
    }
    if (!item.quantity || item.quantity <= 0) {
      message.warning(`"${item.productName}" 数量必须大于0`)
      return
    }
    if (!item.price || item.price <= 0) {
      message.warning(`"${item.productName}" 单价必须大于0`)
      return
    }
  }

  const payload = {
    storeId: basicForm.storeId,
    supplierId: basicForm.supplierId,
    applicantId: basicForm.applicantId,
    remark: basicForm.remark,
    totalAmount: totalAmount.value,
    items: validItems.map((item) => ({
      productName: item.productName,
      quantity: item.quantity,
      price: item.price,
    })),
  }

  submitting.value = true
  try {
    await purchaseApi.create(payload)
    message.success('采购申请提交成功')
    router.push('/purchase/list')
  } catch (error) {
    console.error('提交失败，请重试', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/purchase/list')
}

onMounted(() => {
  loadUsers()
  loadStores()
  loadSuppliers()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-breadcrumb {
  margin-bottom: 16px;
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

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #1890ff;
}

.apply-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.table-wrapper {
  overflow-x: auto;
}

.table-wrapper :deep(.ant-table) {
  min-width: 650px;
}

.subtotal-text {
  font-weight: 600;
  color: #1890ff;
}

.add-row-btn {
  margin-top: 12px;
}

.summary-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.summary-row {
  display: flex;
  align-items: center;
}

.summary-label {
  font-size: 15px;
  color: #666;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #ff4d4f;
}

.summary-actions {
  display: flex;
  gap: 8px;
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

  .apply-form :deep(.ant-form-item) {
    display: flex;
    flex-direction: column;
  }

  .apply-form :deep(.ant-form-item-label) {
    text-align: left;
    padding-bottom: 4px;
  }

  .summary-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-value {
    font-size: 18px;
  }

  .table-wrapper :deep(.ant-table) {
    font-size: 13px;
    min-width: 550px;
  }

  .table-wrapper :deep(.ant-table-thead > tr > th),
  .table-wrapper :deep(.ant-table-tbody > tr > td) {
    padding: 8px 6px;
  }
}

@media (max-width: 576px) {
  .page-header h2 {
    font-size: 18px;
  }

  .table-wrapper :deep(.ant-table) {
    font-size: 12px;
    min-width: 480px;
  }
}
</style>
