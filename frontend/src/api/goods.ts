import request from '@/utils/request'

export const storeApi = {
  getAll: (): Promise<any> => request.get('/stores'),
}

export const goodsApi = {
  getList: (params: any): Promise<any> => request.get('/products', { params }),
  getById: (id: string): Promise<any> => request.get(`/products/${id}`),
  create: (data: any): Promise<any> => request.post('/products', data),
  update: (id: string, data: any): Promise<any> => request.put(`/products/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/products/${id}`),
}

export const categoryApi = {
  getTree: (): Promise<any> => request.get('/categories/tree'),
  getList: (): Promise<any> => request.get('/categories'),
  create: (data: any): Promise<any> => request.post('/categories', data),
  update: (id: string, data: any): Promise<any> => request.put(`/categories/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/categories/${id}`),
}

export const brandApi = {
  getList: (params: any): Promise<any> => request.get('/brands', { params }),
  getAll: (): Promise<any> => request.get('/brands/all'),
  create: (data: any): Promise<any> => request.post('/brands', data),
  update: (id: string, data: any): Promise<any> => request.put(`/brands/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/brands/${id}`),
}

export const inventoryCheckApi = {
  getList: (params: any): Promise<any> => request.get('/products/inventory-checks', { params }),
  create: (data: any): Promise<any> => request.post('/products/inventory-checks', data),
  update: (id: string, data: any): Promise<any> => request.put(`/products/inventory-checks/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/products/inventory-checks/${id}`),
}

export const inventoryWarningApi = {
  getList: (params: any): Promise<any> => request.get('/products/inventory-warnings', { params }),
  getStats: (): Promise<any> => request.get('/products/inventory-warnings/stats'),
  handleAlert: (id: string): Promise<any> => request.put(`/products/inventory-warnings/${id}/handle`),
}