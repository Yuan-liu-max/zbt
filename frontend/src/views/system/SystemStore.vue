<template>
  <div class="page-container">
    <div class="page-header">
      <h2>门店管理</h2>
      <a-button type="primary" @click="handleCreate">
        <PlusOutlined /> 新增门店
      </a-button>
    </div>

    <!-- 搜索 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="门店名称">
          <a-input v-model:value="searchForm.storeName" placeholder="请输入门店名称" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item label="门店状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择" allow-clear style="width: 140px">
            <a-select-option value="OPEN">营业中</a-select-option>
            <a-select-option value="SUSPENDED">休息中</a-select-option>
            <a-select-option value="CLOSED">已关闭</a-select-option>
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
    <div class="content-card table-card">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="storeId"
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColorMap[(record as any).status as StoreStatus]">
              {{ statusTextMap[(record as any).status as StoreStatus] }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-popconfirm title="确定要删除该门店吗？" @confirm="handleDelete(record)">
                <a class="action-link danger">删除</a>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="modalVisible" :title="isEdit ? '编辑门店' : '新增门店'" @ok="handleModalOk" :confirm-loading="modalLoading" width="600px">
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="门店名称" name="storeName">
          <a-input v-model:value="formData.storeName" placeholder="请输入门店名称" />
        </a-form-item>
        <a-form-item label="门店编码" name="storeCode">
          <a-input v-model:value="formData.storeCode" placeholder="请输入门店编码" />
        </a-form-item>
        <a-form-item label="所属区域" name="regionId">
          <a-input v-model:value="formData.regionId" placeholder="请输入区域ID" />
        </a-form-item>
        <a-form-item label="门店地址" name="address">
          <a-input v-model:value="formData.address" placeholder="请输入门店地址" />
        </a-form-item>
        <a-form-item label="门店类型" name="storeType">
          <a-select v-model:value="formData.storeType" placeholder="请选择门店类型">
            <a-select-option value="NORMAL">普通门店</a-select-option>
            <a-select-option value="FLAGSHIP">旗舰店</a-select-option>
            <a-select-option value="NEW">新门店</a-select-option>
            <a-select-option value="OLD">老门店</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="联系电话" name="contactPhone">
          <a-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="营业时间" name="businessHours">
          <a-input v-model:value="formData.businessHours" placeholder="如 09:00-21:00" />
        </a-form-item>
        <a-form-item label="门店状态" name="status">
          <a-select v-model:value="formData.status" placeholder="请选择状态">
            <a-select-option value="OPEN">营业中</a-select-option>
            <a-select-option value="SUSPENDED">休息中</a-select-option>
            <a-select-option value="CLOSED">已关闭</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { StoreItem, StoreStatus } from '@/types/system'
import { storeApi } from '@/api/store'

const searchForm = reactive({
  storeName: '',
  status: undefined as StoreStatus | undefined,
})

const tableData = ref<StoreItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true, showQuickJumper: true, showTotal: (total: number) => `共 ${total} 条` })

const columns = [
  { title: '门店名称', dataIndex: 'storeName', key: 'storeName', width: 140 },
  { title: '门店编码', dataIndex: 'storeCode', key: 'storeCode', width: 120 },
  { title: '门店类型', dataIndex: 'storeType', key: 'storeType', width: 100 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 120 },
  { title: '营业时间', dataIndex: 'businessHours', key: 'businessHours', width: 120 },
  { title: '门店状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

const statusColorMap: Record<StoreStatus, string> = { OPEN: 'green', SUSPENDED: 'orange', CLOSED: 'red' }
const statusTextMap: Record<StoreStatus, string> = { OPEN: '营业中', SUSPENDED: '休息中', CLOSED: '已关闭' }

const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({ storeId: '', storeName: '', storeCode: '', regionId: '', address: '', storeType: 'NORMAL' as any, contactPhone: '', businessHours: '', status: 'OPEN' as StoreStatus })

const formRules = {
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  storeCode: [{ required: true, message: '请输入门店编码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.current, pageSize: pagination.pageSize }
    if (searchForm.storeName) params.storeName = searchForm.storeName
    if (searchForm.status) params.status = searchForm.status
    const res: any = await storeApi.getList(params)
    tableData.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    message.error('加载数据失败')
  } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchForm.storeName = ''; searchForm.status = undefined; handleSearch() }
const handleTableChange = (pag: any) => { pagination.current = pag.current; pagination.pageSize = pag.size || pag.pageSize; loadData() }
const handleCreate = () => { isEdit.value = false; resetForm(); modalVisible.value = true }
const handleEdit = (record: StoreItem) => {
  isEdit.value = true
  Object.assign(formData, record)
  modalVisible.value = true
}
const handleDelete = async (record: StoreItem) => {
  try { await storeApi.delete(record.storeId); message.success('删除成功'); loadData() }
  catch { message.error('删除失败') }
}
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields(); modalLoading.value = true
    const submitData = { storeName: formData.storeName, storeCode: formData.storeCode, regionId: formData.regionId, address: formData.address, storeType: formData.storeType, contactPhone: formData.contactPhone, businessHours: formData.businessHours, status: formData.status }
    if (isEdit.value) { await storeApi.update(formData.storeId, submitData); message.success('更新成功') }
    else { await storeApi.create(submitData); message.success('新增成功') }
    modalVisible.value = false; loadData()
  } catch { console.error('表单验证失败') } finally { modalLoading.value = false }
}
const resetForm = () => { Object.assign(formData, { storeId: '', storeName: '', storeCode: '', regionId: '', address: '', storeType: 'NORMAL', contactPhone: '', businessHours: '', status: 'OPEN' }) }
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
.action-link.danger { color: #ff4d4f; }
.action-link.danger:hover { color: #ff7875; background: #fff1f0; }
.action-btns { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.table-card :deep(.ant-table-wrapper) { overflow-x: auto; }
.table-card :deep(.ant-table) { min-width: 800px; }
@media (max-width: 768px) { .page-container { padding: 16px; } .content-card { padding: 16px; margin-bottom: 12px; } .search-card :deep(.ant-form-item) { width: 100%; } .search-card :deep(.ant-form-item-control) { flex: 1; } .table-card :deep(.ant-table) { font-size: 13px; min-width: 600px; } }
@media (max-width: 576px) { .page-header { flex-direction: column; align-items: flex-start; } .page-header h2 { font-size: 18px; } }
</style>
