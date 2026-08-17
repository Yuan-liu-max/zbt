import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  PurchaseRecord,
  PurchaseItem,
  PurchaseQueryParams,
} from '@/types/purchase'

export const purchaseApi = {
  getList: (params: PurchaseQueryParams): Promise<PageResult<PurchaseRecord>> => request.get('/purchases', { params }),
  getById: (id: number | string): Promise<PurchaseRecord> => request.get(`/purchases/${id}`),
  create: (data: Partial<PurchaseRecord>): Promise<PurchaseRecord> => request.post('/purchases', data),
  update: (id: number | string, data: Partial<PurchaseRecord>): Promise<PurchaseRecord> => request.put(`/purchases/${id}`, data),
  submit: (id: number): Promise<void> => request.put(`/purchases/${id}/submit`),
  approve: (id: number): Promise<void> => request.put(`/purchases/${id}/approve`),
  reject: (id: number, reason: string): Promise<void> => request.put(`/purchases/${id}/reject`, { reason }),
  cancel: (id: number): Promise<void> => request.put(`/purchases/${id}/cancel`),
  delete: (id: number): Promise<void> => request.delete(`/purchases/${id}`),
}

export const purchaseItemApi = {
  getList: (purchaseId: number | string): Promise<PurchaseItem[]> => request.get(`/purchases/${purchaseId}/items`),
  create: (purchaseId: number | string, data: Partial<PurchaseItem>): Promise<PurchaseItem> => request.post(`/purchases/${purchaseId}/items`, data),
  delete: (purchaseId: number | string, itemId: number | string): Promise<void> => request.delete(`/purchases/${purchaseId}/items/${itemId}`),
}
