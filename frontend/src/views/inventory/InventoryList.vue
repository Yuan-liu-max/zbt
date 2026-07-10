<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>库存列表</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新增商品
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="商品名称/编码">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入商品名称或编码"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="类别">
          <a-select
            v-model:value="searchForm.categoryName"
            placeholder="全部类别"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="黄金饰品">黄金饰品</a-select-option>
            <a-select-option value="铂金饰品">铂金饰品</a-select-option>
            <a-select-option value="钻石饰品">钻石饰品</a-select-option>
            <a-select-option value="翡翠饰品">翡翠饰品</a-select-option>
            <a-select-option value="K金饰品">K金饰品</a-select-option>
            <a-select-option value="珍珠饰品">珍珠饰品</a-select-option>
            <a-select-option value="银饰">银饰</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="库存状态">
          <a-select
            v-model:value="searchForm.inventoryStatus"
            placeholder="全部"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="normal">正常</a-select-option>
            <a-select-option value="warning">预警</a-select-option>
            <a-select-option value="shortage">缺货</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">
              <SearchOutlined /> 查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button @click="handleExport">
              <ExportOutlined /> 导出
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
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'stock'">
            <span class="qty-highlight">{{ record.stock }}</span>
          </template>
          <template v-if="column.key === 'inventoryStatus'">
            <a-tag :color="getStatusColor(record.stock, record.brandId)">
              {{ getStatusText(record.stock, record.brandId) }}
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
                    <a-menu-item @click="handleAdjust(record)">库存调整</a-menu-item>
                    <a-menu-item @click="handleTransfer(record)">调拨</a-menu-item>
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
      :title="isEdit ? '编辑库存' : '新增商品'"
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
        <a-form-item label="商品编码" name="code">
          <a-input v-model:value="formData.code" placeholder="请输入商品编码" />
        </a-form-item>
        <a-form-item label="商品名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入商品名称" />
        </a-form-item>
        <a-form-item label="商品分类" name="categoryId">
          <a-cascader
            v-model:value="formData.categoryId"
            :options="categoryTree"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择商品分类"
            change-on-select
          />
        </a-form-item>
        <a-form-item label="所属门店" name="storeId">
          <a-select v-model:value="formData.storeId" placeholder="请选择门店">
            <a-select-option v-for="store in stores" :key="store.id" :value="store.id">
              {{ store.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="品牌" name="brandId">
          <a-select v-model:value="formData.brandId" placeholder="请选择品牌">
            <a-select-option v-for="brand in brands" :key="brand.id" :value="brand.id">
              {{ brand.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="售价" name="price">
          <a-input-number
            v-model:value="formData.price"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入售价"
          />
        </a-form-item>
        <a-form-item label="库存数量" name="stock">
          <a-input-number
            v-model:value="formData.stock"
            :min="0"
            style="width: 100%"
            placeholder="请输入库存数量"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio value="on">上架</a-radio>
            <a-radio value="off">下架</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ExportOutlined, DownOutlined } from '@ant-design/icons-vue'
import type { GoodsItem, GoodsQueryParams, GoodsStatus, GoodsCategory, BrandItem, StoreItem } from '@/types/goods'
import { goodsApi, categoryApi, brandApi } from '@/api/mock/goods'

// 库存状态（独立于商品上下架状态）
type InventoryStatus = 'normal' | 'warning' | 'shortage'

// 门店数据
const stores = ref<StoreItem[]>([
  { id: '1', name: '深圳总仓' },
  { id: '2', name: '北京分仓' },
  { id: '3', name: '上海分仓' },
])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryName: undefined as string | undefined,
  inventoryStatus: undefined as InventoryStatus | undefined,
})

// 表格数据
const tableData = ref<GoodsItem[]>([])
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
  { title: '商品编码', dataIndex: 'code', key: 'code', width: 140 },
  { title: '商品名称', dataIndex: 'name', key: 'name', width: 140 },
  { title: '商品分类', dataIndex: 'categoryName', key: 'categoryName', width: 110 },
  { title: '所属门店', dataIndex: 'storeName', key: 'storeName', width: 120 },
  { title: '售价(元)', dataIndex: 'price', key: 'price', width: 100, align: 'right' as const },
  { title: '库存数量', dataIndex: 'stock', key: 'stock', width: 100, align: 'right' as const },
  { title: '库存状态', dataIndex: 'status', key: 'inventoryStatus', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 160, fixed: 'right' as const }
]

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  code: '',
  name: '',
  categoryId: [] as string[],
  storeId: '',
  brandId: '',
  price: 0,
  stock: 0,
  status: 'on' as GoodsStatus
})

const formRules = {
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  storeId: [{ required: true, message: '请选择所属门店', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

// 分类和品牌数据
const categoryTree = ref<GoodsCategory[]>([])
const flatCategories = ref<GoodsCategory[]>([])
const brands = ref<BrandItem[]>([])

// 打平分类数据
const flattenCategories = (items: GoodsCategory[]): GoodsCategory[] => {
  return items.reduce<GoodsCategory[]>((acc, item) => {
    acc.push(item)
    if (item.children) {
      acc.push(...flattenCategories(item.children))
    }
    return acc
  }, [])
}

// 计算库存状态（基于 stock 和 brandId 的简单映射，实际应由后端返回）
const getInventoryStatus = (stock: number): InventoryStatus => {
  if (stock <= 10) return 'shortage'
  if (stock <= 20) return 'warning'
  return 'normal'
}

// 状态颜色
const getStatusColor = (stock: number, _brandId?: string) => {
  const status = getInventoryStatus(stock)
  const map: Record<InventoryStatus, string> = {
    normal: 'green',
    warning: 'orange',
    shortage: 'red'
  }
  return map[status]
}

// 状态文本
const getStatusText = (stock: number, _brandId?: string) => {
  const status = getInventoryStatus(stock)
  const map: Record<InventoryStatus, string> = {
    normal: '正常',
    warning: '预警',
    shortage: '缺货'
  }
  return map[status]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: GoodsQueryParams = {
      keyword: searchForm.keyword || undefined,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const res = await goodsApi.getList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载分类和品牌
const loadCategoriesAndBrands = async () => {
  try {
    const [catTree, brandList] = await Promise.all([
      categoryApi.getTree(),
      brandApi.getAll()
    ])
    categoryTree.value = catTree
    flatCategories.value = flattenCategories(catTree)
    brands.value = brandList
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryName = undefined
  searchForm.inventoryStatus = undefined
  handleSearch()
}

// 导出
const handleExport = () => {
  message.success('导出功能开发中...')
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
const handleEdit = (record: GoodsItem) => {
  isEdit.value = true
  formData.id = record.id
  formData.code = record.code
  formData.name = record.name
  formData.categoryId = record.categoryId ? [record.categoryId] : []
  formData.storeId = record.storeId
  formData.brandId = record.brandId
  formData.price = record.price
  formData.stock = record.stock
  formData.status = record.status
  modalVisible.value = true
}

// TODO: 详情
const handleDetail = (record: GoodsItem) => {
  // TODO: 实现详情查看功能
  message.info(`查看 ${record.name} 详情`)
}

// TODO: 库存调整
const handleAdjust = (record: GoodsItem) => {
  // TODO: 实现库存调整功能
  message.info(`调整 ${record.name} 库存`)
}

// TODO: 调拨
const handleTransfer = (record: GoodsItem) => {
  // TODO: 实现调拨功能
  message.info(`调拨 ${record.name}`)
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    const lastCategoryId = formData.categoryId[formData.categoryId.length - 1] || ''
    const categoryName = flatCategories.value.find(c => c.id === lastCategoryId)?.name || ''
    const storeName = stores.value.find(s => s.id === formData.storeId)?.name || ''
    const brandName = brands.value.find(b => b.id === formData.brandId)?.name || ''

    const submitData = {
      code: formData.code,
      name: formData.name,
      categoryId: lastCategoryId,
      categoryName,
      storeId: formData.storeId,
      storeName,
      brandId: formData.brandId,
      brandName,
      price: formData.price,
      stock: formData.stock,
      status: formData.status
    }

    if (isEdit.value) {
      await goodsApi.update(formData.id, submitData)
      message.success('更新成功')
    } else {
      await goodsApi.create(submitData)
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
  formData.categoryId = []
  formData.storeId = ''
  formData.brandId = ''
  formData.price = 0
  formData.stock = 0
  formData.status = 'on'
}

onMounted(() => {
  loadCategoriesAndBrands()
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

.qty-highlight {
  font-weight: 600;
  color: #1890ff;
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
