<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品列表</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新增商品
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="关键词">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="请输入商品名称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="商品分类">
          <a-select
            v-model:value="searchForm.categoryId"
            placeholder="请选择"
            allow-clear
            style="width: 150px"
          >
            <a-select-option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属门店">
          <a-select
            v-model:value="searchForm.storeId"
            placeholder="请选择门店"
            allow-clear
            style="width: 160px"
          >
            <a-select-option v-for="store in stores" :key="store.id" :value="store.id">
              {{ store.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="on">上架</a-select-option>
            <a-select-option value="off">下架</a-select-option>
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
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'on' ? 'green' : 'red'" size="small">
              {{ record.status === 'on' ? '上架' : '下架' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleEdit(record)" class="action-link">编辑</a>
              <a-popconfirm
                title="确定要删除该商品吗？"
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
      :title="isEdit ? '编辑商品' : '新增商品'"
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
        <!-- TODO: 根据角色权限决定是否显示敏感字段 -->
        <!--
        <a-form-item label="成本价" name="costPrice">
          <a-input-number
            v-model:value="formData.costPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入成本价"
          />
        </a-form-item>
        <a-form-item label="毛利率" name="grossMarginRate">
          <a-input-number
            v-model:value="formData.grossMarginRate"
            :min="0"
            :max="100"
            :precision="1"
            style="width: 100%"
            placeholder="请输入毛利率(%)"
          />
        </a-form-item>
        -->
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
        <a-form-item label="商品描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入商品描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { GoodsItem, GoodsQueryParams, GoodsStatus } from '@/types/goods'
import { goodsApi, brandApi, categoryApi, storeApi } from '@/api/mock/goods'
import type { BrandItem, GoodsCategory, StoreItem } from '@/types/goods'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: undefined as string | undefined,
  storeId: undefined as string | undefined,
  status: undefined as GoodsStatus | undefined
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

// 表格列配置（成本价和毛利率按角色显示/隐藏）
const columns = computed(() => {
  const baseColumns = [
    { title: '商品编号', dataIndex: 'code', key: 'code', width: 160 },
    { title: '商品名称', dataIndex: 'name', key: 'name', width: 160 },
    { title: '商品分类', dataIndex: 'categoryName', key: 'categoryName', width: 100 },
    { title: '所属门店', dataIndex: 'storeName', key: 'storeName', width: 120 },
    { title: '售价(元)', dataIndex: 'price', key: 'price', width: 100, align: 'right' as const },
  ]

  // TODO: 根据角色权限决定是否显示敏感字段
  // const hasSensitivePermission = true
  // if (hasSensitivePermission) {
  //   baseColumns.push(
  //     { title: '成本价(元)', dataIndex: 'costPrice', key: 'costPrice', width: 100, align: 'right' as const },
  //     { title: '毛利率', dataIndex: 'grossMarginRate', key: 'grossMarginRate', width: 80, align: 'center' as const }
  //   )
  // }

  baseColumns.push(
    { title: '库存', dataIndex: 'stock', key: 'stock', width: 80, align: 'center' as const },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80, align: 'center' as const },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
    { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
  )

  return baseColumns
})

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const formData = reactive({
  id: '',
  name: '',
  categoryId: [] as string[],
  brandId: '',
  price: 0,
  costPrice: 0,
  grossMarginRate: 0,
  stock: 0,
  storeId: '',
  status: 'on' as GoodsStatus,
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  brandId: [{ required: true, message: '请选择品牌', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

// 分类、品牌和门店数据
const categoryTree = ref<GoodsCategory[]>([])
const flatCategories = ref<GoodsCategory[]>([])
const brands = ref<BrandItem[]>([])
const stores = ref<StoreItem[]>([])

// 打平分类数据用于下拉选择
const flattenCategories = (items: GoodsCategory[]): GoodsCategory[] => {
  return items.reduce<GoodsCategory[]>((acc, item) => {
    acc.push(item)
    if (item.children) {
      acc.push(...flattenCategories(item.children))
    }
    return acc
  }, [])
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: GoodsQueryParams = {
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId,
      storeId: searchForm.storeId,
      status: searchForm.status,
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

// 加载分类、品牌和门店
const loadCategoriesAndBrands = async () => {
  try {
    const [catTree, brandList, storeList] = await Promise.all([
      categoryApi.getTree(),
      brandApi.getAll(),
      storeApi.getAll()
    ])
    categoryTree.value = catTree
    flatCategories.value = flattenCategories(catTree)
    brands.value = brandList
    stores.value = storeList
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
  searchForm.categoryId = undefined
  searchForm.storeId = undefined
  searchForm.status = undefined
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
const handleEdit = (record: GoodsItem) => {
  isEdit.value = true
  // 根据分类ID查找路径
  const findCategoryPath = (items: GoodsCategory[], targetId: string): string[] => {
    for (const item of items) {
      if (item.id === targetId) return [item.id]
      if (item.children) {
        const path = findCategoryPath(item.children, targetId)
        if (path.length) return [item.id, ...path]
      }
    }
    return []
  }

  formData.id = record.id
  formData.name = record.name
  formData.categoryId = findCategoryPath(categoryTree.value, record.categoryId)
  formData.brandId = record.brandId
  formData.price = record.price
  formData.costPrice = record.costPrice
  formData.grossMarginRate = record.grossMarginRate
  formData.stock = record.stock
  formData.storeId = record.storeId
  formData.status = record.status
  formData.description = record.description || ''
  modalVisible.value = true
}

// 删除
const handleDelete = async (id: string) => {
  try {
    await goodsApi.delete(id)
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

    // 获取分类名称
    const getCategoryName = (ids: string[]): string => {
      if (!ids.length) return ''
      const lastId = ids[ids.length - 1]
      const cat = flatCategories.value.find(c => c.id === lastId)
      return cat?.name || ''
    }

    // 获取品牌名称
    const brand = brands.value.find(b => b.id === formData.brandId)

    const submitData = {
      name: formData.name,
      categoryId: formData.categoryId[formData.categoryId.length - 1],
      categoryName: getCategoryName(formData.categoryId),
      brandId: formData.brandId,
      brandName: brand?.name || '',
      price: formData.price,
      costPrice: formData.costPrice,
      grossMarginRate: formData.grossMarginRate,
      stock: formData.stock,
      storeId: formData.storeId,
      storeName: stores.value.find(s => s.id === formData.storeId)?.name || '',
      status: formData.status,
      description: formData.description
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
  formData.name = ''
  formData.categoryId = []
  formData.brandId = ''
  formData.price = 0
  formData.costPrice = 0
  formData.grossMarginRate = 0
  formData.stock = 0
  formData.storeId = ''
  formData.status = 'on'
  formData.description = ''
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

.action-link {
  font-size: 13px;
}

.danger-link {
  color: #ff4d4f;
}

.danger-link:hover {
  color: #ff7875;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 700px;
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
