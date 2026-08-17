import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  StoreItem,
  GoodsItem,
  GoodsQueryParams,
  GoodsCategory,
  BrandItem,
  BrandQueryParams,
  InventoryCheckRecord,
  ProductImage,
  InventoryCheckParams,
  InventoryWarningItem,
  InventoryStats,
  InventoryWarningParams,
} from '@/types/goods'

export const storeApi = {
  getAll: (): Promise<StoreItem[]> => request.get('/stores/all'),
}

export const goodsApi = {
  getList: (params: GoodsQueryParams): Promise<PageResult<GoodsItem>> => request.get('/products', { params }),
  getById: (id: string): Promise<GoodsItem> => request.get(`/products/${id}`),
  create: (data: Partial<GoodsItem>): Promise<GoodsItem> => request.post('/products', data),
  update: (id: string, data: Partial<GoodsItem>): Promise<GoodsItem> => request.put(`/products/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/products/${id}`),
}

export const categoryApi = {
  getTree: (): Promise<GoodsCategory[]> => request.get('/categories/tree'),
  getList: (): Promise<PageResult<GoodsCategory>> => request.get('/categories'),
  create: (data: Partial<GoodsCategory>): Promise<GoodsCategory> => request.post('/categories', data),
  update: (id: string, data: Partial<GoodsCategory>): Promise<GoodsCategory> => request.put(`/categories/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/categories/${id}`),
}

export const brandApi = {
  getList: (params: BrandQueryParams): Promise<PageResult<BrandItem>> => request.get('/brands', { params }),
  getAll: (): Promise<BrandItem[]> => request.get('/brands/all'),
  create: (data: Partial<BrandItem>): Promise<BrandItem> => request.post('/brands', data),
  update: (id: string, data: Partial<BrandItem>): Promise<BrandItem> => request.put(`/brands/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/brands/${id}`),
}

export const inventoryCheckApi = {
  getList: (params: InventoryCheckParams): Promise<PageResult<InventoryCheckRecord>> => request.get('/products/inventory-checks', { params }),
  create: (data: Partial<InventoryCheckRecord>): Promise<InventoryCheckRecord> => request.post('/products/inventory-checks', data),
  update: (id: string, data: Partial<InventoryCheckRecord>): Promise<InventoryCheckRecord> => request.put(`/products/inventory-checks/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/products/inventory-checks/${id}`),
}

// 文件上传 API（使用后端 FileController）
export const fileApi = {
  upload: (file: File): Promise<{ fileUrl: string; id: number }> => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post('/files/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// 商品图片 API（product_image 表）
export const productImageApi = {
  list: (productId: string): Promise<ProductImage[]> =>
    request.get(`/products/${productId}/images`),
  create: (productId: string, data: Partial<ProductImage>): Promise<ProductImage> =>
    request.post(`/products/${productId}/images`, data),
  update: (productId: string, id: string, data: Partial<ProductImage>): Promise<ProductImage> =>
    request.put(`/products/${productId}/images/${id}`, data),
  delete: (productId: string, id: string): Promise<void> =>
    request.delete(`/products/${productId}/images/${id}`),
}

export const inventoryWarningApi = {
  getList: (params: InventoryWarningParams): Promise<PageResult<InventoryWarningItem>> => request.get('/products/inventory-warnings', { params }),
  getStats: (): Promise<InventoryStats> => request.get('/products/inventory-warnings/stats'),
  handleAlert: (id: string): Promise<void> => request.put(`/products/inventory-warnings/${id}/handle`),
}
