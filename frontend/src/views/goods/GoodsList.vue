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
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'imageUrl'">
            <img
              v-if="record.imageUrl"
              :src="record.imageUrl"
              style="width:50px;height:50px;object-fit:cover;border-radius:4px;cursor:pointer"
              @click.stop="previewImage(record.imageUrl)"
            />
            <span v-else class="text-hint">—</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'on' ? 'green' : 'red'" size="small">
              {{ record.status === 'on' ? '上架' : '下架' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="4">
              <a @click="handleDetail(record)" class="action-link">详情</a>
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
        <a-form-item label="商品图片">
          <div class="image-upload-area">
            <div v-for="(img, idx) in imageList" :key="img.uid || idx" class="image-upload-item">
              <img :src="img.url || img.imageUrl" class="image-upload-thumb" />
              <div class="image-upload-actions">
                <a-tag v-if="img.isPrimary" color="gold" style="margin-right:4px">主图</a-tag>
                <a-button v-else size="small" type="link" @click="setPrimary(idx)">设为主图</a-button>
                <a-popconfirm title="删除此图片？" @confirm="removeImage(idx)">
                  <a-button size="small" type="link" danger>删除</a-button>
                </a-popconfirm>
              </div>
            </div>
            <a-upload
              v-if="imageList.length < 8"
              list-type="picture-card"
              :show-upload-list="false"
              :before-upload="beforeUpload"
              :custom-request="handleUpload"
            >
              <div>
                <PlusOutlined />
                <div style="margin-top: 8px">上传</div>
              </div>
            </a-upload>
          </div>
        </a-form-item>
        <a-form-item label="商品描述" name="description">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入商品描述" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item v-for="(val, key) in detailRecord" :key="key" :label="String(key)" :span="typeof val === 'object' ? 2 : 1">
          <template v-if="key === 'imageUrl' && val">
            <img :src="String(val)" style="max-width:200px;max-height:200px;cursor:pointer" @click="previewImage(String(val))" />
          </template>
          <template v-else>
            {{ typeof val === 'object' ? JSON.stringify(val) : val }}
          </template>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
    <!-- 图片预览 -->
    <a-modal v-model:open="previewVisible" title="商品图片" :footer="null" width="auto" centered>
      <img :src="previewUrl" style="max-width:80vw;max-height:80vh" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'
import type { GoodsItem, GoodsStatus } from '@/types/goods'
import { goodsApi, brandApi, categoryApi, storeApi, fileApi, productImageApi } from '@/api/goods'
import type { ProductImage } from '@/types/goods'
import type { BrandItem, GoodsCategory, StoreItem } from '@/types/goods'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: undefined as string | undefined,
  storeId: undefined as string | undefined,
  status: undefined as GoodsStatus | undefined
})

// CRUD 表格逻辑
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange, handleDelete } = useCrudTable({
  searchForm,
  loadFn: (params) => goodsApi.getList(params),
  deleteFn: (id) => goodsApi.delete(id),
  onDeleteSuccess: () => message.success('删除成功'),
})

// 详情弹窗
const { detailVisible, detailRecord, openDetail } = useDetailModal<GoodsItem>()

// 表格列配置（成本价和毛利率按角色显示/隐藏）
const columns = computed(() => {
  const baseColumns = [
    { title: '商品编号', dataIndex: 'code', key: 'code', width: 140 },
    {
      title: '商品图片', dataIndex: 'imageUrl', key: 'imageUrl', width: 80, align: 'center' as const
    },
    { title: '商品名称', dataIndex: 'name', key: 'name', width: 140 },
    { title: '商品分类', dataIndex: 'categoryName', key: 'categoryName', width: 100 },
    { title: '品牌', dataIndex: 'brandName', key: 'brandName', width: 100 },
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
    { title: '库存', dataIndex: 'stock', key: 'stock', width: 80, align: 'right' as const },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80, align: 'right' as const },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 160 },
    { title: '操作', key: 'action', dataIndex: 'action', width: 100 }
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

// 图片列表（多图管理）
const imageList = ref<(ProductImage & { uid?: string; url?: string; _new?: boolean })[]>([])

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

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = undefined
  searchForm.storeId = undefined
  searchForm.status = undefined
  handleSearch()
}

// 图片上传处理
const beforeUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)
  if (!isImage) { message.error('仅支持 JPG/PNG/WEBP/GIF'); return false }
  if (file.size / 1024 / 1024 > 2) { message.error('图片不能超过 2MB'); return false }
  return true
}

const handleUpload = async (options: any) => {
  try {
    const res = await fileApi.upload(options.file as File)
    imageList.value.push({
      id: '', productId: '', imageUrl: res.fileUrl,
      sortOrder: imageList.value.length, isPrimary: imageList.value.length === 0 ? 1 : 0,
      createdAt: '', uid: String(Date.now()), url: res.fileUrl, _new: true
    })
    options.onSuccess?.(res, options.file)
  } catch { message.error('上传失败'); options.onError?.() }
}

const setPrimary = (idx: number) => {
  imageList.value.forEach((img, i) => { img.isPrimary = i === idx ? 1 : 0 })
}

const removeImage = (idx: number) => {
  imageList.value.splice(idx, 1)
  // 如果删除的是主图，重新指定第一张为主图
  if (imageList.value.length > 0 && !imageList.value.some(i => i.isPrimary)) {
    imageList.value[0].isPrimary = 1
  }
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
  // 根据分类名称查找路径（后端返回 categoryName，无 categoryId）
  const findCategoryPath = (items: GoodsCategory[], targetName: string): string[] => {
    for (const item of items) {
      if (item.name === targetName) return [item.id]
      if (item.children) {
        const path = findCategoryPath(item.children, targetName)
        if (path.length) return [item.id, ...path]
      }
    }
    return []
  }

  formData.id = record.id
  formData.name = record.name
  formData.categoryId = findCategoryPath(categoryTree.value, record.categoryName || '')
  // 后端返回 brandName，根据名称匹配品牌ID
  const matchedBrand = brands.value.find(b => b.name === record.brandName)
  formData.brandId = matchedBrand?.id || ''
  formData.price = record.price ?? record.retailPrice ?? 0
  formData.costPrice = record.costPrice || 0
  formData.grossMarginRate = record.grossMarginRate || 0
  formData.stock = record.stock || 0
  formData.storeId = record.storeId || ''
  formData.status = record.status === 'on' ? 'on' : 'off'
  formData.description = record.description || ''
  // 编辑时加载已有图片
  loadProductImages(record.id)
  modalVisible.value = true
}

// 加载商品已有图片
const loadProductImages = async (productId: string) => {
  imageList.value = []
  try {
    const imgs = await productImageApi.list(productId)
    imageList.value = imgs.map(img => ({ ...img, uid: img.id, url: img.imageUrl }))
  } catch { /* skip */ }
}

// 保存商品图片
const saveProductImages = async (productId: string) => {
  if (!productId) return
  // 删除旧的（不在当前列表中）
  try {
    const existing = await productImageApi.list(productId)
    const keepIds = new Set(imageList.value.map(i => i.id).filter(Boolean))
    for (const img of existing) {
      if (!keepIds.has(img.id)) {
        await productImageApi.delete(productId, img.id).catch(() => {})
      }
    }
  } catch { /* skip */ }
  // 新增/更新图片
  for (let i = 0; i < imageList.value.length; i++) {
    const img = imageList.value[i]
    try {
      if (img._new || !img.id) {
        await productImageApi.create(productId, {
          imageUrl: img.imageUrl,
          sortOrder: i,
          isPrimary: img.isPrimary
        })
      } else {
        await productImageApi.update(productId, img.id, {
          sortOrder: i,
          isPrimary: img.isPrimary
        })
      }
    } catch { /* skip */ }
  }
}

// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')
const previewImage = (url: string) => {
  previewUrl.value = url
  previewVisible.value = true
}

// 详情
const handleDetail = (record: GoodsItem) => {
  openDetail(record)
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
      await saveProductImages(formData.id)
      message.success('更新成功')
    } else {
      const created = await goodsApi.create(submitData)
      if (created?.id) {
        await saveProductImages(created.id)
      }
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
  imageList.value = []
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

/* 商品图片上传区域 */
.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.image-upload-item {
  position: relative;
  width: 102px;
  height: 102px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}
.image-upload-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-upload-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  padding: 2px 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
