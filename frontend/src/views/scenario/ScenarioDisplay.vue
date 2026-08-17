<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>陈列检查</h2>
      <a-space>
        <a-button type="primary" @click="handleCreate">
          <PlusOutlined /> 新建检查
        </a-button>
        <a-button @click="handleExport">
          <DownloadOutlined /> 导出
        </a-button>
      </a-space>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card">
      <div class="search-card">
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
          <a-form-item label="检查日期">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              value-format="YYYY-MM-DD"
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
          <template v-if="column.key === 'inspectorId'">
            {{ userName(record.inspectorId) }}
          </template>
          <!-- 照片 -->
          <template v-if="column.key === 'photos'">
            <img
              v-if="listPhoto(record)"
              :src="listPhoto(record)"
              style="width:48px;height:48px;object-fit:cover;border-radius:4px;cursor:pointer"
              @click="previewImage(listPhoto(record))"
            />
            <span v-else>—</span>
          </template>
          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm title="确定要删除该检查记录吗？" @confirm="handleDelete(record)">
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
      :title="isEdit ? '编辑检查' : '新建检查'"
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
        <a-form-item label="检查人" name="inspectorId">
          <a-select v-model:value="formData.inspectorId" placeholder="请选择检查人">
            <a-select-option v-for="u in userOptions" :key="u.id" :value="u.id">{{ u.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="检查日期" name="inspectionDate">
          <a-date-picker v-model:value="formData.inspectionDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="陈列区域" name="displayArea">
          <a-input v-model:value="formData.displayArea" placeholder="如：主柜台 / 橱窗" />
        </a-form-item>
        <a-form-item label="标准得分" name="standardScore">
          <a-input-number v-model:value="formData.standardScore" :min="0" :max="100" :precision="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="问题描述" name="issueDescription">
          <a-textarea v-model:value="formData.issueDescription" :rows="2" placeholder="检查发现的问题" />
        </a-form-item>
        <a-form-item label="整改前照片" name="beforePhotos">
          <div class="photo-upload-area">
            <div v-for="(p, idx) in beforePhotoList" :key="p.uid" class="photo-upload-item">
              <img :src="p.url" class="photo-upload-thumb" @click="previewImage(p.url)" />
              <div class="photo-upload-remove" @click="removePhoto('before', idx)">
                <DeleteOutlined />
              </div>
            </div>
            <a-upload
              v-if="beforePhotoList.length < 9"
              list-type="picture-card"
              :show-upload-list="false"
              :before-upload="beforePhotoUpload"
              :custom-request="handleBeforeUpload"
              accept="image/*"
            >
              <div>
                <PlusOutlined />
                <div style="margin-top: 8px">上传</div>
              </div>
            </a-upload>
          </div>
        </a-form-item>
        <a-form-item label="整改后照片" name="afterPhotos">
          <div class="photo-upload-area">
            <div v-for="(p, idx) in afterPhotoList" :key="p.uid" class="photo-upload-item">
              <img :src="p.url" class="photo-upload-thumb" @click="previewImage(p.url)" />
              <div class="photo-upload-remove" @click="removePhoto('after', idx)">
                <DeleteOutlined />
              </div>
            </div>
            <a-upload
              v-if="afterPhotoList.length < 9"
              list-type="picture-card"
              :show-upload-list="false"
              :before-upload="beforePhotoUpload"
              :custom-request="handleAfterUpload"
              accept="image/*"
            >
              <div>
                <PlusOutlined />
                <div style="margin-top: 8px">上传</div>
              </div>
            </a-upload>
          </div>
        </a-form-item>
        <a-form-item label="整改计划" name="rectificationPlan">
          <a-textarea v-model:value="formData.rectificationPlan" :rows="2" placeholder="整改计划" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="检查编号">{{ detailRecord?.id ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="门店">{{ storeName(detailRecord?.storeId ?? null) }}</a-descriptions-item>
        <a-descriptions-item label="检查日期">{{ detailRecord?.inspectionDate ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="检查人">{{ userName(detailRecord?.inspectorId ?? null) }}</a-descriptions-item>
        <a-descriptions-item label="陈列区域">{{ detailRecord?.displayArea ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="标准得分">{{ detailRecord?.standardScore ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="问题描述" :span="2">{{ detailRecord?.issueDescription ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="整改前照片" :span="2">
          <template v-if="photoUrlList(detailRecord?.beforePhotos).length">
            <img
              v-for="(u, i) in photoUrlList(detailRecord?.beforePhotos)"
              :key="i"
              :src="u"
              style="width:56px;height:56px;object-fit:cover;border-radius:4px;margin-right:8px;cursor:pointer"
              @click="previewImage(u)"
            />
          </template>
          <span v-else>—</span>
        </a-descriptions-item>
        <a-descriptions-item label="整改后照片" :span="2">
          <template v-if="photoUrlList(detailRecord?.afterPhotos).length">
            <img
              v-for="(u, i) in photoUrlList(detailRecord?.afterPhotos)"
              :key="i"
              :src="u"
              style="width:56px;height:56px;object-fit:cover;border-radius:4px;margin-right:8px;cursor:pointer"
              @click="previewImage(u)"
            />
          </template>
          <span v-else>—</span>
        </a-descriptions-item>
        <a-descriptions-item label="整改计划" :span="2">{{ detailRecord?.rectificationPlan ?? '-' }}</a-descriptions-item>
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
import type { DisplayInspection } from '@/types/scenario'
import { displayApi } from '@/api/scene'
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
  dateRange: null as [string, string] | null
})

// 表格数据
const tableData = ref<DisplayInspection[]>([])
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
  { title: '检查编号', dataIndex: 'id', key: 'id', width: 90 },
  { title: '门店', key: 'storeId', width: 120 },
  { title: '检查日期', dataIndex: 'inspectionDate', key: 'inspectionDate', width: 120 },
  { title: '检查人', key: 'inspectorId', width: 90 },
  { title: '照片', key: 'photos', width: 110, align: 'center' as const },
  { title: '陈列区域', dataIndex: 'displayArea', key: 'displayArea', width: 120 },
  { title: '标准得分', dataIndex: 'standardScore', key: 'standardScore', width: 90, align: 'center' as const },
  { title: '问题描述', dataIndex: 'issueDescription', key: 'issueDescription', width: 180, ellipsis: true },
  { title: '整改计划', dataIndex: 'rectificationPlan', key: 'rectificationPlan', width: 160, ellipsis: true },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
])

// 详情弹窗
const detailVisible = ref(false)
const detailRecord = ref<DisplayInspection | null>(null)

// 新增/编辑弹窗
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: 0,
  storeId: undefined as number | undefined,
  inspectorId: undefined as number | undefined,
  inspectionDate: '',
  displayArea: '',
  standardScore: undefined as number | undefined,
  issueDescription: '',
  beforePhotos: '',
  afterPhotos: '',
  rectificationPlan: ''
})
const formRules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  inspectorId: [{ required: true, message: '请选择检查人', trigger: 'change' }],
  inspectionDate: [{ required: true, message: '请选择检查日期', trigger: 'change' }],
  displayArea: [{ required: true, message: '请输入陈列区域', trigger: 'blur' }]
}

// 照片：beforePhotos/afterPhotos 以逗号分隔保存多个图片 URL
const photoUrlList = (raw?: string) => (raw || '').split(',').map((u) => u.trim()).filter(Boolean)
const firstPhoto = (raw?: string) => photoUrlList(raw)[0] || ''
const beforePhotoList = ref<{ uid: string; url: string }[]>([])
const afterPhotoList = ref<{ uid: string; url: string }[]>([])
const syncPhotoLists = () => {
  beforePhotoList.value = photoUrlList(formData.beforePhotos).map((url, i) => ({ uid: `b-${i}`, url }))
  afterPhotoList.value = photoUrlList(formData.afterPhotos).map((url, i) => ({ uid: `a-${i}`, url }))
}
const beforePhotoUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)
  if (!isImage) { message.error('仅支持 JPG/PNG/WEBP/GIF 图片'); return false }
  if (file.size / 1024 / 1024 > 5) { message.error('图片不能超过 5MB'); return false }
  return true
}
const uploadFile = async (file: File): Promise<string> => {
  const uploadForm = new FormData()
  uploadForm.append('file', file)
  const res: any = await request.post('/files/upload', uploadForm)
  const url = res?.fileUrl
  if (!url) throw new Error('上传失败')
  return url
}
const makeUpload = (field: 'beforePhotos' | 'afterPhotos') => async (options: any) => {
  try {
    const url = await uploadFile(options.file as File)
    const urls = photoUrlList(formData[field])
    urls.push(url)
    formData[field] = urls.join(',')
    syncPhotoLists()
    message.success('照片上传成功')
    options.onSuccess?.(url, options.file)
  } catch {
    message.error('照片上传失败')
    options.onError?.()
  }
}
const handleBeforeUpload = makeUpload('beforePhotos')
const handleAfterUpload = makeUpload('afterPhotos')
const removePhoto = (kind: 'before' | 'after', idx: number) => {
  const field = kind === 'before' ? 'beforePhotos' : 'afterPhotos'
  const urls = photoUrlList(formData[field])
  urls.splice(idx, 1)
  formData[field] = urls.join(',')
  syncPhotoLists()
}
const listPhoto = (r: DisplayInspection) => firstPhoto(r.beforePhotos) || firstPhoto(r.afterPhotos)

// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')
const previewImage = (url: string) => {
  previewUrl.value = url
  previewVisible.value = true
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      store: searchForm.store || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await displayApi.getList(params)
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
  handleSearch()
}

// 表格分页
const handleTableChange = (pag: { current?: number; pageSize?: number }) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadData()
}

// 新建
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

// 编辑
const handleEdit = (record: DisplayInspection) => {
  isEdit.value = true
  formData.id = record.id
  formData.storeId = record.storeId || undefined
  formData.inspectorId = record.inspectorId || undefined
  formData.inspectionDate = record.inspectionDate || ''
  formData.displayArea = record.displayArea || ''
  formData.standardScore = record.standardScore ?? undefined
  formData.issueDescription = record.issueDescription || ''
  formData.beforePhotos = record.beforePhotos || ''
  formData.afterPhotos = record.afterPhotos || ''
  formData.rectificationPlan = record.rectificationPlan || ''
  syncPhotoLists()
  modalVisible.value = true
}

// 删除
const handleDelete = async (record: DisplayInspection) => {
  try {
    await displayApi.delete(record.id)
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
      inspectorId: formData.inspectorId,
      inspectionDate: formData.inspectionDate,
      displayArea: formData.displayArea,
      standardScore: formData.standardScore,
      issueDescription: formData.issueDescription,
      beforePhotos: formData.beforePhotos,
      afterPhotos: formData.afterPhotos,
      rectificationPlan: formData.rectificationPlan
    }
    if (isEdit.value) {
      await displayApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await displayApi.create(submitData)
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
  formData.inspectorId = undefined
  formData.inspectionDate = ''
  formData.displayArea = ''
  formData.standardScore = undefined
  formData.issueDescription = ''
  formData.beforePhotos = ''
  formData.afterPhotos = ''
  formData.rectificationPlan = ''
  syncPhotoLists()
}

// 导出
const handleExport = () => {
  message.info('导出功能即将上线，敬请期待')
}

// 查看
const handleView = (record: DisplayInspection) => {
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
