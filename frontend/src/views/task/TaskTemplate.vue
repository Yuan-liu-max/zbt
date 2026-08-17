<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>任务模板</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建模板
      </a-button>
    </div>

    <!-- 标签页 -->
    <div class="content-card tab-card">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane key="HUMAN" tab="人效" />
        <a-tab-pane key="PRODUCT" tab="货品" />
        <a-tab-pane key="SCENE" tab="场景" />
        <a-tab-pane key="COMPREHENSIVE" tab="综合" />
      </a-tabs>

      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" @finish="handleSearch" class="search-form">
        <a-form-item label="模板名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入模板名称"
            allow-clear
            style="width: 200px"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
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
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'dimension'">
            <a-tag :color="typeColorMap[record.dimension] || 'default'" size="small">
              {{ taskTypeMap[record.dimension] || record.dimension }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleUse(record)" class="action-link">使用</a>
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-dropdown :trigger="['click']">
                <a class="action-link">
                  更多 <DownOutlined style="font-size: 10px" />
                </a>
                <template #overlay>
                  <a-menu @click="(info: any) => handleMoreAction(info.key, record)">
                    <a-menu-item key="copy">复制模板</a-menu-item>
                    <a-menu-item key="export">导出模板</a-menu-item>
                    <a-menu-item key="delete" class="danger-menu-item">删除</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑模板' : '新建模板'"
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
        <a-form-item label="模板名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入模板名称" />
        </a-form-item>
        <a-form-item label="模板类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择模板类型">
            <a-select-option v-for="(label, value) in taskTypeMap" :key="value" :value="value">
              {{ label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'

const router = useRouter()
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { TaskTemplate, TaskDimension } from '@/types/task'
import { templateApi } from '@/api/task'
import { useCrudTable } from '@/composables/useCrudTable'

// 本地映射表
const taskTypeMap: Record<string, string> = { HUMAN: '人效', PRODUCT: '货品', SCENE: '场景', COMPREHENSIVE: '综合' }

// 标签页
const activeTab = ref<string>('all')

// 搜索表单
const searchForm = reactive({
  name: '',
  dimension: undefined as string | undefined
})

// 表格数据（useCrudTable 封装，name -> templateName）
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => {
    const p: any = { ...params, templateName: params.name }
    delete p.name
    return templateApi.getList(p)
  },
})

// 模板类型颜色映射（dimension 枚举）
const typeColorMap: Record<string, string> = {
  HUMAN: 'blue',
  PRODUCT: 'green',
  SCENE: 'orange',
  COMPREHENSIVE: 'purple'
}

// 表格列配置
const columns = [
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 200 },
  { title: '模板类型', dataIndex: 'dimension', key: 'dimension', width: 120, align: 'center' as const },
  { title: '创建人', dataIndex: 'createdBy', key: 'createdBy', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 170 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const editId = ref('')
const formRef = ref()
const formData = reactive({
  name: '',
  type: undefined as TaskDimension | undefined
})

const formRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择模板类型', trigger: 'change' }]
}

// 标签页切换
const handleTabChange = (key: string) => {
  searchForm.dimension = key === 'all' ? undefined : key
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.dimension = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  editId.value = ''
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: TaskTemplate) => {
  isEdit.value = true
  editId.value = String(record.id)
  formData.name = record.templateName
  formData.type = record.dimension
  modalVisible.value = true
}

// 使用模板
const handleUse = (record: TaskTemplate) => {
  router.push(`/task/create?templateId=${record.id}`)
}

// 更多操作
const handleMoreAction = (action: string, record: TaskTemplate) => {
  switch (action) {
    case 'copy':
      handleCopy(record)
      break
    case 'export':
      // TODO: P2-导出模板功能
      message.info('导出模板功能即将上线')
      break
    case 'delete':
      handleDelete(record)
      break
  }
}

// 复制模板
const handleCopy = async (record: TaskTemplate) => {
  try {
    await templateApi.create({
      templateName: `${record.templateName}（副本）`,
      dimension: record.dimension
    })
    message.success('复制成功')
    loadData()
  } catch (error) {
    console.error('复制失败', error)
  }
}

// 删除模板
const handleDelete = (record: TaskTemplate) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除模板「${record.templateName}」吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await templateApi.delete(String(record.id))
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

    const payload = { templateName: formData.name, dimension: formData.type }

    if (isEdit.value) {
      await templateApi.update(editId.value, payload)
      message.success('编辑成功')
    } else {
      await templateApi.create(payload)
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
  formData.name = ''
  formData.type = undefined
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
  padding: 0 24px 24px;
}

.tab-card :deep(.ant-tabs) {
  margin-bottom: 16px;
}

.tab-card :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 0;
  margin-right: 12px;
}

.table-card {
  padding: 16px;
  overflow: hidden;
}

.action-link {
  font-size: 13px;
}

.danger-menu-item {
  color: #ff4d4f;
}

.danger-menu-item:hover {
  color: #ff7875;
  background: #fff1f0;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 700px;
}

/* 响应式 - 平板 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .tab-card {
    padding: 0 16px 16px;
  }

  .table-card {
    padding: 12px;
  }

  .search-form :deep(.ant-form-item) {
    width: 100%;
    margin-right: 0;
    margin-bottom: 12px;
  }

  .search-form :deep(.ant-form-item-control) {
    flex: 1;
  }

  .search-form :deep(.ant-form-item-label) {
    flex: none;
  }

  .search-form :deep(.ant-input-affix-wrapper) {
    width: 100% !important;
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

/* 响应式 - 手机 */
@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-header h2 {
    font-size: 18px;
  }

  .tab-card :deep(.ant-tabs-tab) {
    padding: 8px 12px;
    font-size: 13px;
  }

  .table-card :deep(.ant-table) {
    font-size: 12px;
    min-width: 500px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 8px 6px;
  }

  .table-card :deep(.ant-dropdown-trigger) {
    font-size: 12px;
  }
}
</style>
