<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品分类</h2>
      <a-button type="primary" @click="handleAddRoot">
        <PlusOutlined /> 新增一级分类
      </a-button>
    </div>

    <!-- 树形表格 -->
    <div class="content-card">
      <a-table
        :columns="columns"
        :data-source="categoryTree"
        :loading="loading"
        :pagination="false"
        row-key="id"
        default-expand-all-rows
        :children-field-name="'children'"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'level'">
            <a-tag :color="record.level === 1 ? 'blue' : 'green'">
              {{ record.level === 1 ? '一级' : '二级' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'on' ? 'green' : 'red'">
              {{ record.status === 'on' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a @click="handleEdit(record)">编辑</a>
              <a v-if="record.level === 1" @click="handleAddChild(record)">新增子类</a>
              <a-popconfirm
                title="确定要删除该分类吗？"
                @confirm="handleDelete(record.id)"
              >
                <a class="danger-link">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
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
        <a-form-item label="分类名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入分类名称" />
        </a-form-item>
        <a-form-item label="父级分类" name="parentId" v-if="formData.level === 2">
          <a-select v-model:value="formData.parentId" placeholder="请选择父级分类" disabled>
            <a-select-option v-for="cat in rootCategories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio value="on">启用</a-radio>
            <a-radio value="off">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { GoodsCategory, GoodsStatus } from '@/types/goods'
import { categoryApi } from '@/api/mock/goods'

// 分类数据（树形结构）
const categoryTree = ref<GoodsCategory[]>([])
const loading = ref(false)

// 表格列配置
const columns = [
  { title: '分类名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '分类层级', dataIndex: 'level', key: 'level', width: 100, align: 'center' as const },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80, align: 'center' as const },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80, align: 'center' as const },
  { title: '操作', key: 'action', width: 200 }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  parentId: null as string | null,
  level: 1 as 1 | 2,
  sort: 1,
  status: 'on' as GoodsStatus
})

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 计算属性
const modalTitle = computed(() => {
  if (isEdit.value) return '编辑分类'
  return formData.level === 1 ? '新增一级分类' : '新增子分类'
})

// 根分类列表（用于父级选择）
const rootCategories = computed(() => {
  return categoryTree.value.filter(item => item.level === 1)
})

// 加载分类树
const loadCategoryTree = async () => {
  loading.value = true
  try {
    categoryTree.value = await categoryApi.getTree()
  } catch (error) {
    message.error('加载分类数据失败')
  } finally {
    loading.value = false
  }
}

// 新增一级分类
const handleAddRoot = () => {
  isEdit.value = false
  resetForm()
  formData.level = 1
  formData.parentId = null
  modalVisible.value = true
}

// 新增子分类
const handleAddChild = (record: GoodsCategory) => {
  isEdit.value = false
  resetForm()
  formData.level = 2
  formData.parentId = record.id
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: GoodsCategory) => {
  isEdit.value = true
  formData.id = record.id
  formData.name = record.name
  formData.parentId = record.parentId
  formData.level = record.level
  formData.sort = record.sort
  formData.status = record.status
  modalVisible.value = true
}

// 删除
const handleDelete = async (id: string) => {
  try {
    await categoryApi.delete(id)
    message.success('删除成功')
    loadCategoryTree()
  } catch (error) {
    message.error('删除失败')
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      name: formData.name,
      parentId: formData.parentId,
      level: formData.level,
      sort: formData.sort,
      status: formData.status
    }

    if (isEdit.value) {
      await categoryApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await categoryApi.create(submitData)
      message.success('新增成功')
    }

    modalVisible.value = false
    loadCategoryTree()
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
  formData.parentId = null
  formData.level = 1
  formData.sort = 1
  formData.status = 'on'
}

onMounted(() => {
  loadCategoryTree()
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
  overflow: hidden;
}

.danger-link {
  color: #ff4d4f;
}

.danger-link:hover {
  color: #ff7875;
}

/* 表格横向滚动 */
.content-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.content-card :deep(.ant-table) {
  min-width: 500px;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
  }

  .content-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 450px;
  }

  .content-card :deep(.ant-table-thead > tr > th),
  .content-card :deep(.ant-table-tbody > tr > td) {
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

  .content-card :deep(.ant-table) {
    font-size: 12px;
    min-width: 400px;
  }
}
</style>
