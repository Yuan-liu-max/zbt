import request from '@/utils/request'

export const purchaseApi = {
  getList: (params: any): Promise<any> => request.get('/purchases', { params }),
  getById: (id: string): Promise<any> => request.get(`/purchases/${id}`),
  create: (data: any): Promise<any> => request.post('/purchases', data),
  update: (id: string, data: any): Promise<any> => request.put(`/purchases/${id}`, data),
  approve: (id: string): Promise<any> => request.put(`/purchases/${id}/approve`),
  reject: (id: string, reason: string): Promise<any> => request.put(`/purchases/${id}/reject`, { reason }),
  cancel: (id: string): Promise<any> => request.put(`/purchases/${id}/cancel`),
}

export const purchaseItemApi = {
  getList: (purchaseId: string): Promise<any> => request.get(`/purchases/${purchaseId}/items`),
  create: (purchaseId: string, data: any): Promise<any> => request.post(`/purchases/${purchaseId}/items`, data),
  delete: (purchaseId: string, itemId: string): Promise<any> => request.delete(`/purchases/${purchaseId}/items/${itemId}`),
}
