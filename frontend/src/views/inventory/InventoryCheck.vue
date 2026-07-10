<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>盘点管理</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建盘点
      </a-button>
    </div>

    <!-- 状态标签页 -->
    <div class="content-card tab-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="planning" tab="盘点计划" />
        <a-tab-pane key="counting" tab="盘点中" />
        <a-tab-pane key="completed" tab="已完成" />
        <a-tab-pane key="cancelled" tab="已取消" />
      </a-tabs>

      <!-- 搜索表单 -->
      <div class="search-form">
        <a-form layout="inline" :model="searchForm" @finish="handleSearch">
          <a-form-item label="盘点单号">
            <a-input
              v-model:value="searchForm.checkCode"
              placeholder="请输入盘点单号"
              allow-clear
              style="width: 180px"
            />
          </a-form-item>
          <a-form-item label="仓库">
            <a-select
              v-model:value="searchForm.warehouse"
              placeholder="全部仓库"
              allow-clear
              style="width: 150px"
            >
              <a-select-option v-for="wh in warehouses" :key="wh.id" :value="wh.name">
                {{ wh.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="盘点状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="全部状态"
              allow-clear
              style="width: 120px"
            >
              <a-select-option value="planning">计划中</a-select-option>
              <a-select-option value="counting">盘点中</a-select-option>
              <a-select-option value="completed">已完成</a-select-option>
              <a-select-option value="cancelled">已取消</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="盘点时间">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              style="width: 240px"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit">
                <SearchOutlined /> 查询
              </a-button>
              <a-button @click="handleReset">重置</a-button>
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
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'checkTime'">
            <span>{{ record.startDate }} ~ {{ record.endDate }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleDetail(record)" class="action-link">详情</a>
              <a-divider type="vertical" />
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a-dropdown>
                <a class="action-link">更多 <DownOutlined /></a>
                <template #overlay>
                  <a-menu>
                    <a-menu-item v-if="record.status === 'planning'" @click="handleStart(record)">
                      开始盘点
                    </a-menu-item>
                    <a-menu-item v-if="record.status === 'counting'" @click="handleComplete(record)">
                      完成盘点
                    </a-menu-item>
                    <a-menu-item v-if="record.status !== 'completed'" @click="handleCancel(record)">
                      取消盘点
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
      :title="isEdit ? '编辑盘点' : '新建盘点'"
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
        <a-form-item label="盘点名称" name="checkName">
          <a-input v-model:value="formData.checkName" placeholder="请输入盘点名称" />
        </a-form-item>
        <a-form-item label="仓库" name="warehouse">
          <a-select v-model:value="formData.warehouse" placeholder="请选择仓库">
            <a-select-option v-for="wh in warehouses" :key="wh.id" :value="wh.name">
              {{ wh.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="盘点类型" name="checkType">
          <a-select v-model:value="formData.checkType" placeholder="请选择盘点类型">
            <a-select-option value="月度盘点">月度盘点</a-select-option>
            <a-select-option value="周盘点">周盘点</a-select-option>
            <a-select-option value="临时盘点">临时盘点</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="盘点时间" name="checkDate" required>
          <a-range-picker
            v-model:value="formData.checkDate"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import type { InventoryCheckRecord, InventoryCheckParams, CheckStatus } from '@/types/goods'
import { inventoryCheckApi } from '@/api/mock/goods'

// 仓库数据
const warehouses = [
  { id: '1', name: '深圳总仓' },
  { id: '2', name: '北京分仓' },
  { id: '3', name: '上海分仓' },
]

// 当前标签页
const activeTab = ref<string>('planning')

// 搜索表单
const searchForm = reactive({
  checkCode: '',
  warehouse: undefined as string | undefined,
  status: undefined as CheckStatus | undefined,
  dateRange: null as any
})

// 表格数据
const tableData = ref<InventoryCheckRecord[]>([])
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
const columns = [
  { title: '盘点单号', dataIndex: 'checkCode', key: 'checkCode', width: 160 },
  { title: '盘点名称', dataIndex: 'checkName', key: 'checkName', width: 180 },
  { title: '仓库', dataIndex: 'warehouse', key: 'warehouse', width: 100 },
  { title: '盘点类型', dataIndex: 'checkType', key: 'checkType', width: 100 },
  { title: '盘点时间', key: 'checkTime', width: 200 },
  { title: '创建人', dataIndex: 'creator', key: 'creator', width: 80 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80, align: 'center' as const },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  checkName: '',
  warehouse: '',
  checkType: '周盘点',
  checkDate: null as any,
  remark: ''
})

const formRules = {
  checkName: [{ required: true, message: '请输入盘点名称', trigger: 'blur' }],
  warehouse: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  checkType: [{ required: true, message: '请选择盘点类型', trigger: 'change' }],
  checkDate: [{ required: true, message: '请选择盘点时间', trigger: 'change' }]
}

// 监听标签页切换
watch(activeTab, () => {
  pagination.current = 1
  loadData()
})

// 状态颜色
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    planning: 'blue',
    counting: 'processing',
    completed: 'success',
    cancelled: 'default'
  }
  return map[status] || 'default'
}

// 状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    planning: '计划中',
    counting: '盘点中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: InventoryCheckParams = {
      checkCode: searchForm.checkCode || undefined,
      warehouse: searchForm.warehouse,
      status: searchForm.status,
      startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
      endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await inventoryCheckApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (key: string) => {
  activeTab.value = key
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.checkCode = ''
  searchForm.warehouse = undefined
  searchForm.status = undefined
  searchForm.dateRange = null
  activeTab.value = 'planning'
  handleSearch()
}

// 表格分页
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

// 编辑
const handleEdit = (record: InventoryCheckRecord) => {
  isEdit.value = true
  formData.id = record.id
  formData.checkName = record.checkName
  formData.warehouse = record.warehouse
  formData.checkType = record.checkType
  formData.checkDate = [dayjs(record.startDate), dayjs(record.endDate)]
  formData.remark = record.remark || ''
  modalVisible.value = true
}

// 详情
const handleDetail = (record: InventoryCheckRecord) => {
  message.info(`查看 ${record.checkCode} 详情`)
}

// 开始盘点
const handleStart = (record: InventoryCheckRecord) => {
  Modal.confirm({
    title: '确认开始盘点',
    content: `确定要开始盘点 ${record.checkName} 吗？`,
    onOk: async () => {
      await inventoryCheckApi.update(record.id, { status: 'counting' })
      message.success('盘点已开始')
      loadData()
    }
  })
}

// 完成盘点
const handleComplete = (record: InventoryCheckRecord) => {
  Modal.confirm({
    title: '确认完成盘点',
    content: `确定要完成盘点 ${record.checkName} 吗？`,
    onOk: async () => {
      await inventoryCheckApi.update(record.id, { status: 'completed' })
      message.success('盘点已完成')
      loadData()
    }
  })
}

// 取消盘点
const handleCancel = (record: InventoryCheckRecord) => {
  Modal.confirm({
    title: '确认取消盘点',
    content: `确定要取消盘点 ${record.checkName} 吗？`,
    onOk: async () => {
      await inventoryCheckApi.update(record.id, { status: 'cancelled' })
      message.success('盘点已取消')
      loadData()
    }
  })
}

// 删除
const handleDelete = (record: InventoryCheckRecord) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除盘点 ${record.checkCode} 吗？`,
    onOk: async () => {
      await inventoryCheckApi.delete(record.id)
      message.success('删除成功')
      loadData()
    }
  })
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      checkName: formData.checkName,
      warehouse: formData.warehouse,
      checkType: formData.checkType,
      startDate: formData.checkDate?.[0]?.format?.('YYYY-MM-DD') || '',
      endDate: formData.checkDate?.[1]?.format?.('YYYY-MM-DD') || '',
      remark: formData.remark
    }

    if (isEdit.value) {
      await inventoryCheckApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await inventoryCheckApi.create(submitData)
      message.success('创建成功')
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
  formData.checkName = ''
  formData.warehouse = ''
  formData.checkType = '周盘点'
  formData.checkDate = null
  formData.remark = ''
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

.tab-card {
  padding: 16px 24px;
}

.tab-card :deep(.ant-tabs) {
  margin-bottom: 16px;
}

.tab-card :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.search-form {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 12px;
  margin-right: 0;
}

.table-card {
  padding: 16px;
  overflow: hidden;
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

.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-btns :deep(.ant-divider-vertical) {
  margin: 0 2px;
}

.danger-link {
  color: #ff4d4f;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 700px;
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

  .tab-card {
    padding: 12px 16px;
  }

  .search-form :deep(.ant-form-item) {
    width: 100%;
  }

  .search-form :deep(.ant-form-item-control) {
    flex: 1;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 600px;
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
    min-width: 500px;
  }
}
</style>
