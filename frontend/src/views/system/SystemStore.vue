<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>门店管理</h2>
      <a-button type="primary" @click="handleCreate">
        <PlusOutlined /> 新增门店
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card">
      <div class="search-card">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="门店名称">
            <a-input
              v-model:value="searchForm.name"
              placeholder="请输入门店名称"
              allow-clear
              style="width: 180px"
            />
          </a-form-item>
          <a-form-item label="门店状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="请选择"
              allow-clear
              style="width: 140px"
            >
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="open">营业中</a-select-option>
              <a-select-option value="suspended">休息中</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="所属区域">
            <a-select
              v-model:value="searchForm.region"
              placeholder="请选择"
              allow-clear
              style="width: 150px"
            >
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="华北区域">华北区域</a-select-option>
              <a-select-option value="华东区域">华东区域</a-select-option>
              <a-select-option value="华南区域">华南区域</a-select-option>
              <a-select-option value="西南区域">西南区域</a-select-option>
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
          <!-- 门店状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'open' ? 'green' : 'orange'">
              {{ record.status === 'open' ? '营业中' : '休息中' }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-popconfirm
                title="确定要删除该门店吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a class="action-link delete-link">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑门店' : '新增门店'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="600px"
      :mask-closable="false"
      @after-close="resetForm"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="门店名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入门店名称" :maxlength="50" show-count />
        </a-form-item>
        <a-form-item label="门店编码" name="code">
          <a-input v-model:value="formData.code" placeholder="请输入门店编码" :maxlength="20" show-count />
        </a-form-item>
        <a-form-item label="所属区域" name="region">
          <a-select v-model:value="formData.region" placeholder="请选择所属区域">
            <a-select-option value="华北区域">华北区域</a-select-option>
            <a-select-option value="华东区域">华东区域</a-select-option>
            <a-select-option value="华南区域">华南区域</a-select-option>
            <a-select-option value="西南区域">西南区域</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="门店地址" name="address">
          <a-input v-model:value="formData.address" placeholder="请输入门店地址" :maxlength="100" show-count />
        </a-form-item>
        <a-form-item label="联系人" name="contactPerson">
          <a-input v-model:value="formData.contactPerson" placeholder="请输入联系人" :maxlength="20" />
        </a-form-item>
        <a-form-item label="联系电话" name="contactPhone">
          <a-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" :maxlength="11" />
        </a-form-item>
        <a-form-item label="门店状态" name="status">
          <a-select v-model:value="formData.status" placeholder="请选择门店状态">
            <a-select-option value="open">营业中</a-select-option>
            <a-select-option value="suspended">休息中</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { StoreItem, StoreStatus } from '@/types/system'
import { storeApi } from '@/api/mock/system'

// 搜索表单
const searchForm = reactive({
  name: '',
  status: '' as StoreStatus | '',
  region: ''
})

// 表格数据
const tableData = ref<StoreItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格列配置
const columns = computed(() => [
  { title: '门店名称', dataIndex: 'name', key: 'name', width: 150, ellipsis: true },
  { title: '门店编码', dataIndex: 'code', key: 'code', width: 100 },
  { title: '所属区域', dataIndex: 'region', key: 'region', width: 110 },
  { title: '门店地址', dataIndex: 'address', key: 'address', width: 220, ellipsis: true },
  { title: '联系人', dataIndex: 'contactPerson', key: 'contactPerson', width: 90 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 120 },
  { title: '门店状态', key: 'status', width: 100, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
])

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  code: '',
  region: undefined as string | undefined,
  address: '',
  contactPerson: '',
  contactPhone: '',
  status: 'open' as StoreStatus
})

const formRules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入门店编码', trigger: 'blur' }],
  region: [{ required: true, message: '请选择所属区域', trigger: 'change' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  status: [{ required: true, message: '请选择门店状态', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await storeApi.getList({
      name: searchForm.name || undefined,
      status: searchForm.status || undefined,
      region: searchForm.region || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch {
    message.error('加载数据失败')
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
  searchForm.name = ''
  searchForm.status = ''
  searchForm.region = ''
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: { current?: number; pageSize?: number }) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

// 新增
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: StoreItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.name = record.name
  formData.code = record.code
  formData.region = record.region
  formData.address = record.address
  formData.contactPerson = record.contactPerson
  formData.contactPhone = record.contactPhone
  formData.status = record.status
  modalVisible.value = true
}

// 删除
const handleDelete = async (record: StoreItem) => {
  try {
    await storeApi.delete(record.id)
    message.success('删除成功')
    loadData()
  } catch {
    message.error('删除失败')
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData: Partial<StoreItem> = {
      name: formData.name,
      code: formData.code,
      region: formData.region,
      address: formData.address,
      contactPerson: formData.contactPerson,
      contactPhone: formData.contactPhone,
      status: formData.status
    }

    if (isEdit.value) {
      await storeApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await storeApi.create(submitData)
      message.success('创建成功')
    }

    modalVisible.value = false
    loadData()
  } catch {
    // validation failed or API error
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = ''
  formData.name = ''
  formData.code = ''
  formData.region = undefined
  formData.address = ''
  formData.contactPerson = ''
  formData.contactPhone = ''
  formData.status = 'open'
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
  padding: 0;
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

.delete-link {
  color: #ff4d4f;
}

.delete-link:hover {
  color: #ff7875;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 1000px;
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

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .search-card :deep(.ant-form-item-label) {
    flex: 0 0 80px;
    max-width: 80px;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 800px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
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
    min-width: 750px;
  }
}
</style>
