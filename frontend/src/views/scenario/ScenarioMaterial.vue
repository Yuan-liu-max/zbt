<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>物料更新</h2>
      <a-space>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新建更新
        </a-button>
        <a-button @click="handleExport">
          <DownloadOutlined /> 导出
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="门店">
          <a-select
            v-model:value="searchForm.store"
            placeholder="请选择门店"
            allow-clear
            style="width: 180px"
          >
            <a-select-option v-for="s in storeOptions" :key="s.id" :value="String(s.id)">
              {{ s.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="更新日期">
          <a-range-picker
            v-model:value="searchForm.dateRange"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.currentStatus"
            placeholder="请选择状态"
            allow-clear
            style="width: 130px"
          >
            <a-select-option value="NORMAL">正常</a-select-option>
            <a-select-option value="EXPIRED">已过期</a-select-option>
            <a-select-option value="DAMAGED">破损</a-select-option>
            <a-select-option value="MISSING">缺失</a-select-option>
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
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 门店 -->
          <template v-if="column.key === 'storeId'">
            {{ storeName(record.storeId) }}
          </template>
          <!-- 检查人 -->
          <template v-if="column.key === 'checkerId'">
            {{ userName(record.checkerId) }}
          </template>
          <!-- 照片 -->
          <template v-if="column.key === 'updatedPhotos'">
            <img
              v-if="record.updatedPhotos"
              :src="firstPhoto(record.updatedPhotos)"
              style="width:48px;height:48px;object-fit:cover;border-radius:4px;cursor:pointer"
              @click="previewImage(firstPhoto(record.updatedPhotos))"
            />
            <span v-else>—</span>
          </template>
          <!-- 更换 -->
          <template v-if="column.key === 'replacementRequired'">
            {{ record.replacementRequired === 1 ? '是' : '否' }}
          </template>
          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm title="确定要删除该记录吗？" @confirm="handleDelete(record)">
                <a class="action-link danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑物料更新' : '新建物料更新'"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="600px"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="门店" name="storeId">
          <a-select v-model:value="formData.storeId" placeholder="请选择门店">
            <a-select-option v-for="s in storeOptions" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="检查人" name="checkerId">
          <a-select v-model:value="formData.checkerId" placeholder="请选择检查人">
            <a-select-option v-for="u in userOptions" :key="u.id" :value="u.id">{{ u.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="检查日期" name="checkDate">
          <a-date-picker v-model:value="formData.checkDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="物料类型" name="materialType">
          <a-input v-model:value="formData.materialType" placeholder="如：防尘布 / 托盘 / 灯带" />
        </a-form-item>
        <a-form-item label="当前状态" name="currentStatus">
          <a-input v-model:value="formData.currentStatus" placeholder="如：正常 / 破损 / 需更换" />
        </a-form-item>
        <a-form-item label="更新照片" name="updatedPhotos">
          <div class="photo-upload-area">
            <div v-for="(p, idx) in photoUrlList" :key="p.uid" class="photo-upload-item">
              <img :src="p.url" class="photo-upload-thumb" @click="previewImage(p.url)" />
              <div class="photo-upload-remove" @click="removePhoto(idx)">
                <DeleteOutlined />
              </div>
            </div>
            <a-upload
              v-if="photoUrlList.length < 9"
              list-type="picture-card"
              :show-upload-list="false"
              :before-upload="beforePhotoUpload"
              :custom-request="handlePhotoUpload"
              accept="image/*"
            >
              <div>
                <PlusOutlined />
                <div style="margin-top: 8px">上传</div>
              </div>
            </a-upload>
          </div>
        </a-form-item>
        <a-form-item label="问题描述" name="issueDescription">
          <a-textarea v-model:value="formData.issueDescription" :rows="2" placeholder="发现的问题" />
        </a-form-item>
        <a-form-item label="是否需要更换" name="replacementRequired">
          <a-radio-group v-model:value="formData.replacementRequired">
            <a-radio :value="0">否</a-radio>
            <a-radio :value="1">是</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="更新编号">{{ detailRecord?.id ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="门店">{{ storeName(detailRecord?.storeId ?? null) }}</a-descriptions-item>
        <a-descriptions-item label="检查日期">{{ detailRecord?.checkDate ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="检查人">{{ userName(detailRecord?.checkerId ?? null) }}</a-descriptions-item>
        <a-descriptions-item label="物料类型">{{ detailRecord?.materialType ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="当前状态">{{ detailRecord?.currentStatus ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="问题描述" :span="2">{{ detailRecord?.issueDescription ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="更新照片" :span="2">
          <template v-if="photoUrlsArray(detailRecord?.updatedPhotos).length">
            <img
              v-for="(u, i) in photoUrlsArray(detailRecord?.updatedPhotos)"
              :key="i"
              :src="u"
              style="width:56px;height:56px;object-fit:cover;border-radius:4px;margin-right:8px;cursor:pointer"
              @click="previewImage(u)"
            />
          </template>
          <span v-else>—</span>
        </a-descriptions-item>
        <a-descriptions-item label="是否需要更换">{{ detailRecord?.replacementRequired === 1 ? '是' : '否' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detailRecord?.createdAt ?? '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 图片预览 -->
    <a-modal v-model:open="previewVisible" title="照片预览" :footer="null" width="auto" centered>
      <img :src="previewUrl" style="max-width:80vw;max-height:80vh" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import type { MaterialItem } from '@/types/scenario'
import { materialApi } from '@/api/scene'
import { userApi } from '@/api/system'
import request from '@/utils/request'

interface OptionItem {
  id: number
  name: string
}

// 门店 / 用户下拉选项
const storeOptions = ref<OptionItem[]>([])
const userOptions = ref<OptionItem[]>([])

const storeName = (id: number | null) => storeOptions.value.find(s => s.id === id)?.name || (id ?? '-')
const userName = (id: number | null) => userOptions.value.find(u => u.id === id)?.name || (id ?? '-')

const loadStores = async () => {
  try {
    const list: any[] = await request.get('/stores/all')
    storeOptions.value = (list || []).map(s => ({ id: Number(s.id), name: s.name || `门店${s.id}` }))
  } catch (error) {
    console.error('加载门店失败', error)
  }
}

const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 200, roleId: 5 })
    userOptions.value = (res.list || []).map(u => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch (error) {
    console.error('加载用户失败', error)
  }
}

// 搜索表单
const searchForm = reactive({
  store: undefined as string | undefined,
  dateRange: null as [string, string] | null,
  currentStatus: undefined as string | undefined
})

// 表格数据
const tableData = ref<MaterialItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<MaterialItem | null>(null)

// 新增/编辑弹窗
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: 0,
  storeId: undefined as number | undefined,
  checkerId: undefined as number | undefined,
  checkDate: '',
  materialType: '',
  currentStatus: '',
  updatedPhotos: '',
  issueDescription: '',
  replacementRequired: 0
})
const formRules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  checkerId: [{ required: true, message: '请选择检查人', trigger: 'change' }],
  checkDate: [{ required: true, message: '请选择检查日期', trigger: 'change' }],
  materialType: [{ required: true, message: '请输入物料类型', trigger: 'blur' }]
}

// 照片：updatedPhotos 以逗号分隔保存多个图片 URL
const photoUrlList = ref<{ uid: string; url: string }[]>([])
const photoUrlsArray = (urls?: string) => (urls || '').split(',').map((u) => u.trim()).filter(Boolean)
const firstPhoto = (urls?: string) => photoUrlsArray(urls)[0] || ''
const syncPhotoList = () => {
  photoUrlList.value = photoUrlsArray(formData.updatedPhotos).map((url, i) => ({ uid: `photo-${i}`, url }))
}
const beforePhotoUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)
  if (!isImage) { message.error('仅支持 JPG/PNG/WEBP/GIF 图片'); return false }
  if (file.size / 1024 / 1024 > 5) { message.error('图片不能超过 5MB'); return false }
  return true
}
const handlePhotoUpload = async (options: any) => {
  try {
    const uploadForm = new FormData()
    uploadForm.append('file', options.file as File)
    const res: any = await request.post('/files/upload', uploadForm)
    const url = res?.fileUrl
    if (!url) throw new Error('上传失败')
    const urls = photoUrlsArray(formData.updatedPhotos)
    urls.push(url)
    formData.updatedPhotos = urls.join(',')
    syncPhotoList()
    message.success('照片上传成功')
    options.onSuccess?.(res, options.file)
  } catch {
    message.error('照片上传失败')
    options.onError?.()
  }
}
const removePhoto = (idx: number) => {
  const urls = photoUrlsArray(formData.updatedPhotos)
  urls.splice(idx, 1)
  formData.updatedPhotos = urls.join(',')
  syncPhotoList()
}

// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')
const previewImage = (url: string) => {
  previewUrl.value = url
  previewVisible.value = true
}

// 表格列配置
const columns = computed(() => [
  { title: '更新编号', dataIndex: 'id', key: 'id', width: 90 },
  { title: '门店', key: 'storeId', width: 120 },
  { title: '物料类型', dataIndex: 'materialType', key: 'materialType', width: 120 },
  { title: '检查日期', dataIndex: 'checkDate', key: 'checkDate', width: 120 },
  { title: '检查人', key: 'checkerId', width: 90 },
  { title: '当前状态', dataIndex: 'currentStatus', key: 'currentStatus', width: 110 },
  { title: '照片', key: 'updatedPhotos', width: 110, align: 'center' as const },
  { title: '问题描述', dataIndex: 'issueDescription', key: 'issueDescription', width: 180, ellipsis: true },
  { title: '更换', key: 'replacementRequired', width: 70, align: 'center' as const },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      store: searchForm.store || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      currentStatus: searchForm.currentStatus || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await materialApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('加载数据失败', error)
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
  searchForm.store = undefined
  searchForm.dateRange = null
  searchForm.currentStatus = undefined
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: { current?: number; pageSize?: number }) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

// 新建
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: MaterialItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.storeId = record.storeId || undefined
  formData.checkerId = record.checkerId || undefined
  formData.checkDate = record.checkDate || ''
  formData.materialType = record.materialType || ''
  formData.currentStatus = record.currentStatus || ''
  formData.updatedPhotos = record.updatedPhotos || ''
  formData.issueDescription = record.issueDescription || ''
  formData.replacementRequired = record.replacementRequired === 1 ? 1 : 0
  syncPhotoList()
  modalVisible.value = true
}

// 删除
const handleDelete = async (record: MaterialItem) => {
  try {
    await materialApi.delete(record.id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败', error)
  }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true
    const submitData = {
      storeId: formData.storeId,
      checkerId: formData.checkerId,
      checkDate: formData.checkDate,
      materialType: formData.materialType,
      currentStatus: formData.currentStatus,
      updatedPhotos: formData.updatedPhotos,
      issueDescription: formData.issueDescription,
      replacementRequired: formData.replacementRequired
    }
    if (isEdit.value) {
      await materialApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await materialApi.create(submitData)
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
  formData.id = 0
  formData.storeId = undefined
  formData.checkerId = undefined
  formData.checkDate = ''
  formData.materialType = ''
  formData.currentStatus = ''
  formData.updatedPhotos = ''
  formData.issueDescription = ''
  formData.replacementRequired = 0
  syncPhotoList()
}

// 导出
const handleExport = () => {
  message.info('导出功能即将上线，敬请期待')
}

// 查看
const handleView = (record: MaterialItem) => {
  detailRecord.value = record
  detailVisible.value = true
}

onMounted(() => {
  loadData()
  loadStores()
  loadUsers()
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

.action-link {
  font-size: 13px;
}

.action-link.danger {
  color: #ff4d4f;
}

.photo-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.photo-upload-item {
  position: relative;
  width: 102px;
  height: 102px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.photo-upload-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.photo-upload-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
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
    min-width: 900px;
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
    min-width: 800px;
  }
}
</style>
