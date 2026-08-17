<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>品牌管理</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新增品牌
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="品牌名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入品牌名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="on">启用</a-select-option>
            <a-select-option value="off">禁用</a-select-option>
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
        :scroll="{ x: 600 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'logo'">
            <div class="brand-logo">
              <img v-if="record.logo" :src="record.logo" :alt="record.name" />
              <span v-else class="brand-logo-placeholder">-</span>
            </div>
          </template>
          <template v-if="column.key === 'name'">
            <span class="brand-name">{{ record.name }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'on' ? 'green' : 'red'" size="small">
              {{ record.status === 'on' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-popconfirm
                title="确定要删除该品牌吗？"
                @confirm="handleDelete(record.id)"
              >
                <a class="danger-link action-link">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑品牌' : '新增品牌'"
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
        <a-form-item label="品牌名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入品牌名称" />
        </a-form-item>
        <a-form-item label="品牌LOGO" name="logo">
          <a-upload
            :show-upload-list="false"
            accept=".jpg,.jpeg,.png,.gif,.webp,.svg"
            :before-upload="handleLogoUpload"
          >
            <div v-if="formData.logo" class="logo-preview">
              <img :src="formData.logo" alt="品牌LOGO" />
              <div class="logo-mask">
                <a-button type="link" size="small" @click.stop="removeLogo">删除</a-button>
              </div>
            </div>
            <div v-else class="logo-uploader">
              <PlusOutlined />
              <div class="logo-uploader-text">上传LOGO</div>
            </div>
          </a-upload>
          <div v-if="logoUploading" class="logo-uploading">上传中...</div>
        </a-form-item>
        <a-form-item label="品牌产地" name="origin">
          <a-input v-model:value="formData.origin" placeholder="请输入品牌产地" />
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
        <a-form-item label="品牌描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入品牌描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useCrudTable } from '@/composables/useCrudTable'
import type { BrandItem, GoodsStatus } from '@/types/goods'
import { brandApi } from '@/api/goods'
import request from '@/utils/request'

// LOGO 上传
const logoUploading = ref(false)

const handleLogoUpload = (file: File) => {
  const isImage = /^image\/(jpeg|png|gif|webp|svg\+xml)$/.test(file.type) || /\.(jpg|jpeg|png|gif|webp|svg)$/i.test(file.name)
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    message.error('图片大小不能超过2MB')
    return false
  }
  logoUploading.value = true
  const uploadForm = new FormData()
  uploadForm.append('file', file)
  request
    .post('/files/upload', uploadForm)
    .then((res: any) => {
      const url = res?.fileUrl
      if (!url) throw new Error('上传失败')
      formData.logo = url
      message.success('LOGO上传成功')
    })
    .catch((error) => {
      console.error('LOGO上传失败', error)
      message.error('LOGO上传失败')
    })
    .finally(() => {
      logoUploading.value = false
    })
  return false
}

const removeLogo = () => {
  formData.logo = ''
}

// 搜索表单
const searchForm = reactive({
  name: '',
  status: undefined as GoodsStatus | undefined
})

// CRUD 表格逻辑
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange, handleDelete } = useCrudTable({
  searchForm,
  loadFn: (params) => brandApi.getList(params),
  deleteFn: (id) => brandApi.delete(id),
  onDeleteSuccess: () => message.success('删除成功'),
})

// 表格列配置（移动端自适应）
const getColumns = () => {
  const isMobile = window.innerWidth < 768

  if (isMobile) {
    // 移动端：精简列，隐藏非关键信息
    return [
      { title: '品牌', dataIndex: 'name', key: 'name', width: 100 },
      { title: '产地', dataIndex: 'origin', key: 'origin', width: 80 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 60, align: 'center' as const },
      { title: '操作', key: 'action', width: 80, fixed: 'right' as const }
    ]
  }

  // PC端：完整列
  return [
    { title: '品牌LOGO', dataIndex: 'logo', key: 'logo', width: 100, align: 'center' as const },
    { title: '品牌名称', dataIndex: 'name', key: 'name', width: 120 },
    { title: '品牌产地', dataIndex: 'origin', key: 'origin', width: 100 },
    { title: '排序', dataIndex: 'sort', key: 'sort', width: 60, align: 'center' as const },
    { title: '状态', dataIndex: 'status', key: 'status', width: 70, align: 'center' as const },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
    { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
  ]
}

const columns = ref(getColumns())

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  logo: '',
  origin: '',
  sort: 1,
  status: 'on' as GoodsStatus,
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }],
  origin: [{ required: true, message: '请输入品牌产地', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }]
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: BrandItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.name = record.name
  formData.logo = record.logo || ''
  formData.origin = record.origin
  formData.sort = record.sort
  formData.status = record.status
  formData.description = record.description || ''
  modalVisible.value = true
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const submitData = {
      name: formData.name,
      logo: formData.logo,
      origin: formData.origin,
      sort: formData.sort,
      status: formData.status,
      description: formData.description
    }

    if (isEdit.value) {
      await brandApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await brandApi.create(submitData)
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
  formData.logo = ''
  formData.origin = ''
  formData.sort = 1
  formData.status = 'on'
  formData.description = ''
}

// 监听窗口大小变化，更新列配置
const handleResize = () => {
  columns.value = getColumns()
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

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.brand-logo-placeholder {
  color: #ccc;
  font-size: 14px;
}

.brand-name {
  font-weight: 500;
  color: #333;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-link {
  font-size: 13px;
}

.danger-link {
  color: #ff4d4f;
}

.danger-link:hover {
  color: #ff7875;
}

/* LOGO 上传 */
.logo-uploader {
  width: 104px;
  height: 104px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: border-color 0.3s;
  color: #999;
  font-size: 14px;
}

.logo-uploader:hover {
  border-color: #c8a44d;
}

.logo-uploader-text {
  font-size: 12px;
}

.logo-preview {
  position: relative;
  width: 104px;
  height: 104px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.logo-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-preview .logo-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.logo-preview:hover .logo-mask {
  opacity: 1;
}

.logo-uploading {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 500px;
}

/* 响应式表单 */
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
    min-width: 400px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
  }

  .brand-name {
    font-size: 13px;
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
    min-width: 350px;
  }
}
</style>
