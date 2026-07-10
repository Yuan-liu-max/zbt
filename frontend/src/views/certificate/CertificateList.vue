<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>证书列表</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新增证书
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="证书编号">
          <a-input
            v-model:value="searchForm.code"
            placeholder="请输入证书编号"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="证书类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="请选择证书类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="gia">GIA证书</a-select-option>
            <a-select-option value="ngtc">NGTC证书</a-select-option>
            <a-select-option value="gic">GIC证书</a-select-option>
            <a-select-option value="other">其他证书</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 130px"
          >
            <a-select-option value="valid">有效</a-select-option>
            <a-select-option value="expiring">即将过期</a-select-option>
            <a-select-option value="expired">已过期</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="签发机构">
          <a-input
            v-model:value="searchForm.issuer"
            placeholder="请输入签发机构"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="签发日期">
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
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'code'">
            <a class="code-link">{{ record.code }}</a>
          </template>
          <template v-if="column.key === 'type'">
            {{ certificateTypeMap[record.type] }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="certificateStatusMap[record.status]?.color">
              {{ certificateStatusMap[record.status]?.text }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleDownload(record)" class="action-link">下载</a>
              <a-divider type="vertical" />
              <a-dropdown>
                <a class="action-link">更多 <DownOutlined /></a>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleEdit(record)">编辑</a-menu-item>
                    <a-menu-item @click="handleView(record)">查看详情</a-menu-item>
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
      :title="isEdit ? '编辑证书' : '新增证书'"
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
        <a-form-item label="证书类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择证书类型">
            <a-select-option value="gia">GIA证书</a-select-option>
            <a-select-option value="ngtc">NGTC证书</a-select-option>
            <a-select-option value="gic">GIC证书</a-select-option>
            <a-select-option value="other">其他证书</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关联商品" name="productName">
          <a-input v-model:value="formData.productName" placeholder="请输入关联商品" />
        </a-form-item>
        <a-form-item label="签发机构" name="issuer">
          <a-input v-model:value="formData.issuer" placeholder="请输入签发机构" />
        </a-form-item>
        <a-form-item label="签发日期" name="issueDate" required>
          <a-date-picker v-model:value="formData.issueDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="有效期至" name="expiryDate" required>
          <a-date-picker v-model:value="formData.expiryDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { CertificateItem, CertificateQueryParams, CertificateType, CertificateStatus } from '@/types/certificate'
import { certificateApi, certificateTypeMap, certificateStatusMap } from '@/api/mock/certificate'

// 搜索表单
const searchForm = reactive({
  code: '',
  type: undefined as CertificateType | undefined,
  status: undefined as CertificateStatus | undefined,
  issuer: '',
  dateRange: null as any
})

// 表格数据
const tableData = ref<CertificateItem[]>([])
const loading = ref(false)
const selectedRowKeys = ref<string[]>([])
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
  { title: '证书编号', dataIndex: 'code', key: 'code', width: 160 },
  { title: '证书类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '关联商品', dataIndex: 'productName', key: 'productName', width: 140 },
  { title: '签发机构', dataIndex: 'issuer', key: 'issuer', width: 180 },
  { title: '签发日期', dataIndex: 'issueDate', key: 'issueDate', width: 110 },
  { title: '有效期至', dataIndex: 'expiryDate', key: 'expiryDate', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  type: 'gia' as CertificateType,
  productName: '',
  issuer: '',
  issueDate: null as any,
  expiryDate: null as any,
  remark: ''
})

const formRules = {
  type: [{ required: true, message: '请选择证书类型', trigger: 'change' }],
  productName: [{ required: true, message: '请输入关联商品', trigger: 'blur' }],
  issuer: [{ required: true, message: '请输入签发机构', trigger: 'blur' }]
}

// 多选
const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: CertificateQueryParams = {
      code: searchForm.code || undefined,
      type: searchForm.type,
      status: searchForm.status,
      issuer: searchForm.issuer || undefined,
      startDate: searchForm.dateRange?.[0]?.format?.('YYYY-MM-DD') || undefined,
      endDate: searchForm.dateRange?.[1]?.format?.('YYYY-MM-DD') || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await certificateApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
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
  searchForm.code = ''
  searchForm.type = undefined
  searchForm.status = undefined
  searchForm.issuer = ''
  searchForm.dateRange = null
  handleSearch()
}

// 分页
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

// 查看
const handleView = (record: CertificateItem) => {
  message.info(`查看 ${record.code} 详情`)
}

// 下载
const handleDownload = (record: CertificateItem) => {
  message.success(`下载 ${record.code}`)
}

// 编辑
const handleEdit = (record: CertificateItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.type = record.type
  formData.productName = record.productName
  formData.issuer = record.issuer
  formData.remark = record.remark || ''
  modalVisible.value = true
}

// 删除
const handleDelete = async (record: CertificateItem) => {
  try {
    await certificateApi.delete(record.id)
    message.success('删除成功')
    loadData()
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
      type: formData.type,
      productName: formData.productName,
      issuer: formData.issuer,
      issueDate: formData.issueDate?.format?.('YYYY-MM-DD') || '',
      expiryDate: formData.expiryDate?.format?.('YYYY-MM-DD') || '',
      remark: formData.remark
    }

    if (isEdit.value) {
      await certificateApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await certificateApi.create(submitData)
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
  formData.type = 'gia'
  formData.productName = ''
  formData.issuer = ''
  formData.issueDate = null
  formData.expiryDate = null
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

.code-link {
  color: #1890ff;
  cursor: pointer;
}

.code-link:hover {
  color: #40a9ff;
  text-decoration: underline;
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
}

.table-card :deep(.ant-table) {
  min-width: 900px;
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
    min-width: 700px;
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
    min-width: 600px;
  }
}
</style>
