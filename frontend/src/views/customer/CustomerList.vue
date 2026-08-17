<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>客户列表</h2>
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新增客户
        </a-button>
        <a-button @click="handleExport">
          <ExportOutlined /> 导出
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="客户姓名">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入客户姓名"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="手机号码">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="客户等级">
          <a-select
            v-model:value="searchForm.level"
            placeholder="请选择客户等级"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="vip">VIP</a-select-option>
            <a-select-option value="normal">普通会员</a-select-option>
            <a-select-option value="diamond">钻石会员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="注册时间">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            style="width: 240px"
          />
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
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'level'">
            <a-tag :color="levelColorMap[record.level]">
              {{ levelTextMap[record.level] }}
            </a-tag>
          </template>
          <template v-if="column.key === 'totalConsumption'">
            <span class="price">{{ record.totalConsumption.toLocaleString() }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'normal' ? 'green' : 'red'">
              {{ record.status === 'normal' ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm
                title="确定要删除该客户吗？"
                @confirm="handleDelete(record)"
              >
                <a class="action-link danger">删除</a>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑客户' : '新增客户'"
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
        <a-form-item label="客户编号" name="code">
          <a-input v-model:value="formData.code" placeholder="自动生成，可手动修改" />
        </a-form-item>
        <a-form-item label="客户姓名" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入客户姓名" />
        </a-form-item>
        <a-form-item label="手机号码" name="phone">
          <a-input v-model:value="formData.phone" placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item label="客户等级" name="level">
          <a-select v-model:value="formData.level" placeholder="请选择客户等级">
            <a-select-option value="vip">VIP</a-select-option>
            <a-select-option value="normal">普通会员</a-select-option>
            <a-select-option value="diamond">钻石会员</a-select-option>
          </a-select>
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
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ExportOutlined } from '@ant-design/icons-vue'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'
import type { CustomerItem, CustomerLevel } from '@/types/customer'
import { customerApi, levelColorMap, levelTextMap } from '@/api/customer'
import { exportComingSoon } from '@/utils/export'

// 搜索表单
const searchForm = reactive({
  name: '',
  phone: '',
  level: undefined as CustomerLevel | undefined,
  dateRange: null as any
})

// 表格数据（使用 useCrudTable composable）
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange, handleDelete: _handleDelete } = useCrudTable<CustomerItem, typeof searchForm>({
  searchForm,
  loadFn: (params) => customerApi.getList({
    name: (params as any).name || undefined,
    phone: (params as any).phone || undefined,
    level: (params as any).level,
    startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
    endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
    page: params.page || 1,
    pageSize: params.pageSize || 10,
  }),
  deleteFn: (id) => customerApi.delete(id),
  onDeleteSuccess: () => message.success('删除成功'),
})

// 表格列配置
const columns = [
  { title: '客户编号', dataIndex: 'code', key: 'code', width: 160 },
  { title: '客户姓名', dataIndex: 'name', key: 'name', width: 100 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 120 },
  { title: '客户等级', dataIndex: 'level', key: 'level', width: 100, align: 'center' as const },
  { title: '累计消费(元)', dataIndex: 'totalConsumption', key: 'totalConsumption', width: 120, align: 'right' as const },
  { title: '积分', dataIndex: 'points', key: 'points', width: 80, align: 'right' as const },
  { title: '注册时间', dataIndex: 'registeredAt', key: 'registeredAt', width: 140 },
  { title: '最近消费时间', dataIndex: 'lastConsumptionAt', key: 'lastConsumptionAt', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 70, align: 'center' as const },
  { title: '操作', key: 'action', width: 140, fixed: 'right' as const }
]

// 详情弹窗（使用 useDetailModal composable）
const { detailVisible, detailRecord, openDetail } = useDetailModal<CustomerItem>()

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  code: '',
  name: '',
  phone: '',
  level: 'normal' as CustomerLevel
})

const formRules = {
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号码', trigger: 'blur' }],
  level: [{ required: true, message: '请选择客户等级', trigger: 'change' }]
}

// 重置（覆盖 composable 版本以正确清空表单字段）
const handleReset = () => {
  searchForm.name = ''
  searchForm.phone = ''
  searchForm.level = undefined
  searchForm.dateRange = null
  handleSearch()
}

// 导出
const handleExport = () => {
  exportComingSoon('客户数据')
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 查看（使用 useDetailModal 的 openDetail）
const handleView = (record: CustomerItem) => {
  openDetail(record)
}

// 编辑
const handleEdit = (record: CustomerItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.code = record.code || ''
  formData.name = record.name
  formData.phone = record.phone
  formData.level = record.level
  modalVisible.value = true
}

// 删除（包装 composable 版本，保留 record 参数签名）
const handleDelete = (record: CustomerItem) => {
  _handleDelete(record.id)
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      code: formData.code,
      name: formData.name,
      phone: formData.phone,
      level: formData.level
    }

    if (isEdit.value) {
      await customerApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await customerApi.create(submitData)
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
  formData.code = ''
  formData.name = ''
  formData.phone = ''
  formData.level = 'normal'
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

.price {
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

.action-link.danger {
  color: #ff4d4f;
}

.action-link.danger:hover {
  color: #ff7875;
  background: #fff1f0;
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
    min-width: 700px;
  }
}
</style>
