<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>供应商管理</h2>
        <p class="page-desc">管理和维护供应商信息，建立稳定的供应链合作关系</p>
      </div>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新增供应商
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="供应商名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入供应商名称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="联系人">
          <a-input
            v-model:value="searchForm.contactPerson"
            placeholder="请输入联系人"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="供应商类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="选择供应商类型"
            allow-clear
            style="width: 160px"
          >
            <a-select-option value="raw_material">原材料供应商</a-select-option>
            <a-select-option value="gemstone">宝石供应商</a-select-option>
            <a-select-option value="pearl">珍珠供应商</a-select-option>
            <a-select-option value="processing">加工服务商</a-select-option>
            <a-select-option value="packaging">包装供应商</a-select-option>
            <a-select-option value="consumable">耗材供应商</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="选择状态"
            allow-clear
            style="width: 130px"
          >
            <a-select-option value="cooperating">合作中</a-select-option>
            <a-select-option value="suspended">已暂停</a-select-option>
            <a-select-option value="terminated">已终止</a-select-option>
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
          <template v-if="column.key === 'name'">
            <div class="supplier-name">
              <div class="supplier-logo" :style="{ background: getLogoColor(record.type) }">
                {{ record.name.charAt(0) }}
              </div>
              <span>{{ record.name }}</span>
            </div>
          </template>
          <template v-if="column.key === 'type'">
            <a-tag :color="supplierTypeMap[record.type]?.color">
              {{ supplierTypeMap[record.type]?.text }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="cooperationStatusMap[record.status]?.color">
              {{ cooperationStatusMap[record.status]?.text }}
            </a-tag>
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
                    <a-menu-item v-if="record.status === 'cooperating'" @click="handleSuspend(record)">
                      暂停合作
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'suspended'" @click="handleResume(record)">
                      恢复合作
                    </a-menu-item>
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
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑供应商' : '新增供应商'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="供应商名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入供应商名称" />
        </a-form-item>
        <a-form-item label="供应商类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择供应商类型">
            <a-select-option value="raw_material">原材料供应商</a-select-option>
            <a-select-option value="gemstone">宝石供应商</a-select-option>
            <a-select-option value="pearl">珍珠供应商</a-select-option>
            <a-select-option value="processing">加工服务商</a-select-option>
            <a-select-option value="packaging">包装供应商</a-select-option>
            <a-select-option value="consumable">耗材供应商</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="联系人" name="contactPerson">
          <a-input v-model:value="formData.contactPerson" placeholder="请输入联系人" />
        </a-form-item>
        <a-form-item label="联系电话" name="contactPhone">
          <a-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="地址" name="address">
          <a-input v-model:value="formData.address" placeholder="请输入地址" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注" />
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'
import type { SupplierItem, SupplierType, CooperationStatus } from '@/types/supplier'
import { supplierApi, supplierTypeMap, cooperationStatusMap } from '@/api/supplier'

// 搜索表单
const searchForm = reactive({
  name: '',
  contactPerson: undefined as string | undefined,
  type: undefined as SupplierType | undefined,
  status: undefined as CooperationStatus | undefined
})

// 表格数据（使用 useCrudTable composable）
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange, handleDelete: _handleDelete } = useCrudTable<SupplierItem, typeof searchForm>({
  searchForm,
  loadFn: (params) => supplierApi.getList({
    name: (params as any).name || undefined,
    contactPerson: (params as any).contactPerson,
    type: (params as any).type,
    status: (params as any).status,
    page: params.page || 1,
    pageSize: params.pageSize || 10,
  }),
  deleteFn: (id) => supplierApi.delete(id),
  onDeleteSuccess: () => message.success('删除成功'),
})

// 获取列配置
const getColumnsValue = () => {
  const isMobile = window.innerWidth < 768

  if (isMobile) {
    return [
      { title: '供应商', dataIndex: 'name', key: 'name', width: 140 },
      { title: '联系人', dataIndex: 'contactPerson', key: 'contactPerson', width: 70 },
      { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 110 },
      { title: '合作状态', dataIndex: 'status', key: 'status', width: 70, align: 'center' as const },
      { title: '操作', key: 'action', width: 80, fixed: 'right' as const }
    ]
  }

  return [
    { title: '供应商名称', dataIndex: 'name', key: 'name', width: 180 },
    { title: '供应商类型', dataIndex: 'type', key: 'type', width: 120 },
    { title: '联系人', dataIndex: 'contactPerson', key: 'contactPerson', width: 80 },
    { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 110 },
    { title: '邮箱', dataIndex: 'email', key: 'email', width: 150 },
    { title: '合作状态', dataIndex: 'status', key: 'status', width: 80, align: 'center' as const },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 100 },
    { title: '操作', key: 'action', width: 140, fixed: 'right' as const }
  ]
}

// 表格列配置
const columns = ref(getColumnsValue())

// 详情弹窗（使用 useDetailModal composable）
const { detailVisible, detailRecord, openDetail } = useDetailModal<SupplierItem>()

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  type: 'raw_material' as SupplierType,
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择供应商类型', trigger: 'change' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }]
}

// LOGO 颜色
const getLogoColor = (type: SupplierType) => {
  const colorMap: Record<SupplierType, string> = {
    raw_material: '#1890ff',
    gemstone: '#722ed1',
    pearl: '#faad14',
    processing: '#fa8c16',
    packaging: '#13c2c2',
    consumable: '#ff4d4f'
  }
  return colorMap[type] || '#1890ff'
}

// 重置（覆盖 composable 版本以正确清空表单字段）
const handleReset = () => {
  searchForm.name = ''
  searchForm.contactPerson = undefined
  searchForm.type = undefined
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 查看（使用 useDetailModal 的 openDetail）
const handleView = (record: SupplierItem) => {
  openDetail(record)
}

// 编辑
const handleEdit = (record: SupplierItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.name = record.name
  formData.type = record.type
  formData.contactPerson = record.contactPerson
  formData.contactPhone = record.contactPhone
  formData.email = record.email
  formData.address = record.address || ''
  formData.remark = record.remark || ''
  modalVisible.value = true
}

// 暂停合作
const handleSuspend = async (record: SupplierItem) => {
  try {
    await supplierApi.update(record.id, { status: 'suspended' })
    message.success('已暂停合作')
    loadData()
  } catch (error) {
    console.error('操作失败', error)
  }
}

// 恢复合作
const handleResume = async (record: SupplierItem) => {
  try {
    await supplierApi.update(record.id, { status: 'cooperating' })
    message.success('已恢复合作')
    loadData()
  } catch (error) {
    console.error('操作失败', error)
  }
}

// 删除（包装 composable 版本，保留 record 参数签名）
const handleDelete = (record: SupplierItem) => {
  _handleDelete(record.id)
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      name: formData.name,
      type: formData.type,
      contactPerson: formData.contactPerson,
      contactPhone: formData.contactPhone,
      email: formData.email,
      address: formData.address,
      remark: formData.remark
    }

    if (isEdit.value) {
      await supplierApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await supplierApi.create(submitData)
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
  formData.name = ''
  formData.type = 'raw_material'
  formData.contactPerson = ''
  formData.contactPhone = ''
  formData.email = ''
  formData.address = ''
  formData.remark = ''
}

// 监听窗口大小变化
const handleResize = () => {
  columns.value = getColumnsValue()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
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
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.page-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: #999;
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

.supplier-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.supplier-logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
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

.danger-link {
  color: #ff4d4f;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-btns :deep(.ant-divider-vertical) {
  margin: 0 2px;
}

.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.table-card :deep(.ant-table) {
  min-width: 600px;
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
    min-width: 500px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 6px;
  }

  .supplier-logo {
    width: 30px;
    height: 30px;
    font-size: 12px;
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
    min-width: 400px;
  }
}
</style>
