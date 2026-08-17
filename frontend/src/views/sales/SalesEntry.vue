<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>销售录入</h2>
    </div>

    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      layout="vertical"
      class="sales-form"
    >
      <!-- 基本信息 -->
      <div class="content-card">
        <div class="card-title">基本信息</div>
        <a-row :gutter="24">
          <a-col :xs="24" :sm="12" :md="8">
            <a-form-item label="销售日期" name="salesDate">
              <a-date-picker v-model:value="formData.salesDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8">
            <a-form-item label="门店" name="storeId">
              <a-select v-model:value="formData.storeId" placeholder="请选择门店" allow-clear>
                <a-select-option v-for="item in storeOptions" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8">
            <a-form-item label="导购" name="employeeId">
              <a-select v-model:value="formData.employeeId" placeholder="请选择导购" allow-clear>
                <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </div>

      <!-- 客户信息 -->
      <div class="content-card">
        <div class="card-title">客户信息</div>
        <a-row :gutter="24">
          <a-col :xs="24" :sm="12" :md="8">
            <a-form-item label="客户类型" name="customerType">
              <a-select v-model:value="formData.customerType" placeholder="请选择">
                <a-select-option value="new">新客户</a-select-option>
                <a-select-option value="old">老客户</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8">
            <a-form-item label="购买场景" name="purchaseScene">
              <a-select v-model:value="formData.purchaseScene" placeholder="请选择">
                <a-select-option value="WEDDING">婚庆</a-select-option>
                <a-select-option value="GIFT">礼品</a-select-option>
                <a-select-option value="SELF">自用</a-select-option>
                <a-select-option value="INVEST">投资</a-select-option>
                <a-select-option value="HOLIDAY">节日</a-select-option>
                <a-select-option value="OTHER">其他</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8">
            <a-form-item label="实付金额" name="paidAmount">
              <a-input-number v-model:value="formData.paidAmount" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </div>

      <!-- 商品明细 -->
      <div class="content-card">
        <div class="card-title">
          <span>商品明细</span>
          <a-button type="dashed" @click="addItem" size="small">
            <PlusOutlined /> 添加商品
          </a-button>
        </div>

        <div v-for="(item, index) in formData.items" :key="index" class="item-card">
          <div class="item-header">
            <span class="item-index">商品 {{ index + 1 }}</span>
            <a-button type="text" danger @click="removeItem(index)" size="small" v-if="formData.items.length > 1">
              <DeleteOutlined /> 删除
            </a-button>
          </div>
          <a-row :gutter="16">
            <a-col :xs="24" :sm="12" :md="8">
              <a-form-item label="商品名称">
                <a-input v-model:value="item.productName" placeholder="请输入商品名称" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8">
              <a-form-item label="品类">
                <a-select v-model:value="item.category" placeholder="请选择品类">
                  <a-select-option value="戒指">戒指</a-select-option>
                  <a-select-option value="项链">项链</a-select-option>
                  <a-select-option value="手镯">手镯</a-select-option>
                  <a-select-option value="吊坠">吊坠</a-select-option>
                  <a-select-option value="耳饰">耳饰</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8">
              <a-form-item label="规格">
                <a-input v-model:value="item.size" placeholder="请输入规格" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8">
              <a-form-item label="单价">
                <a-input-number v-model:value="item.price" :min="0" :precision="2" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8">
              <a-form-item label="数量">
                <a-input-number v-model:value="item.quantity" :min="1" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8">
              <a-form-item label="金额">
                <span class="amount-text">¥{{ ((item.price || 0) * (item.quantity || 0)).toFixed(2) }}</span>
              </a-form-item>
            </a-col>
          </a-row>
        </div>

        <!-- 费用汇总 -->
        <div class="total-section">
          <div class="total-row">
            <span>商品总额</span>
            <span>¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="total-row">
            <span>实付金额</span>
            <span class="total-price">¥{{ formData.paidAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section">
        <a-space>
          <a-button @click="handleReset">重置</a-button>
          <a-button type="primary" @click="handleSubmit" :loading="submitLoading">
            提交销售单
          </a-button>
        </a-space>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { SalesItem, PurchaseScene } from '@/types/sales'
import { salesApi } from '@/api/sales'
import { storeApi } from '@/api/store'
import { userApi } from '@/api/system'

const formRef = ref()
const submitLoading = ref(false)

// 门店下拉（GET /stores/all）
const storeOptions = ref<{ id: number; name: string }[]>([])
// 导购下拉（GET /users?page=1&pageSize=200）
const userOptions = ref<{ id: number; name: string }[]>([])

const loadOptions = async () => {
  try {
    const [stores, users] = await Promise.all([
      storeApi.getAll(),
      userApi.getList({ page: 1, pageSize: 200, roleId: 5 }), // roleId=5 只查导购角色
    ])
    storeOptions.value = stores.map((s) => ({ id: Number(s.id), name: s.name }))
    userOptions.value = users.list.map((u) => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch {}
}

// 表单数据
const formData = reactive({
  salesDate: null as any,
  storeId: undefined as number | undefined,
  employeeId: undefined as number | undefined,
  customerType: 'new' as 'new' | 'old',
  purchaseScene: 'SELF' as PurchaseScene,
  paidAmount: 0,
  items: [
    { productName: '', category: '', size: '', price: 0, quantity: 1 }
  ] as Partial<SalesItem>[]
})

const formRules = {
  salesDate: [{ required: true, message: '请选择销售日期', trigger: 'change' }],
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  employeeId: [{ required: true, message: '请选择导购', trigger: 'change' }],
  customerType: [{ required: true, message: '请选择客户类型', trigger: 'change' }]
}

// 商品总额
const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
})

// 添加商品
const addItem = () => {
  formData.items.push({ productName: '', category: '', size: '', price: 0, quantity: 1 })
}

// 删除商品
const removeItem = (index: number) => {
  formData.items.splice(index, 1)
}

// 提交（对齐后端 SalesCreateDTO 字段）
const handleSubmit = async () => {
  try {
    await formRef.value?.validateFields()
    submitLoading.value = true

    const items = formData.items.map((item) => ({
      productName: item.productName || '',
      category: item.category || '',
      style: item.style || '',
      material: item.material || '',
      weight: item.weight || '',
      size: item.size || '',
      price: item.price || 0,
      quantity: item.quantity || 1,
      customerFavoritePoint: item.customerFavoritePoint || '',
      objection: item.objection || '',
      closingReason: item.closingReason || '',
    }))

    await salesApi.create({
      salesDate: formData.salesDate?.format?.('YYYY-MM-DD') || new Date().toISOString().slice(0, 10),
      storeId: formData.storeId!,
      employeeId: formData.employeeId!,
      customerType: formData.customerType === 'new' ? 'NEW' : 'OLD',
      purchaseScene: formData.purchaseScene,
      totalAmount: totalAmount.value,
      paidAmount: formData.paidAmount,
      productCount: formData.items.length,
      items: items as SalesItem[]
    })

    message.success('销售单提交成功')
    handleReset()
  } catch (error) {
    console.error('提交失败', error)
  } finally {
    submitLoading.value = false
  }
}

// 重置
const handleReset = () => {
  formRef.value?.resetFields()
  formData.items = [{ productName: '', category: '', size: '', price: 0, quantity: 1 }]
  formData.paidAmount = 0
}

onMounted(() => {
  loadOptions()
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

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-index {
  font-weight: 500;
  color: #333;
}

.amount-text {
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
  line-height: 32px;
}

.total-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.total-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
  color: #666;
}

.total-row:last-child {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.total-price {
  color: #ff4d4f;
  font-size: 20px;
}

.submit-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 24px;
  text-align: right;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  bottom: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
  }

  .item-card {
    padding: 12px;
  }

  .submit-section {
    padding: 12px 16px;
  }
}

@media (max-width: 576px) {
  .page-header h2 {
    font-size: 18px;
  }

  .card-title {
    font-size: 14px;
  }
}
</style>
