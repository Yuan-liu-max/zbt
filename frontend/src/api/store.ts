import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { StoreItem } from '@/types/goods'

export const storeApi = {
  getAll: (): Promise<{ id: number; name: string; storeCode?: string }[]> => request.get('/stores/all'),
  getList: (params: { keyword?: string; page?: number; pageSize?: number }): Promise<PageResult<StoreItem>> => request.get('/stores', { params }),
  getById: (id: string): Promise<StoreItem> => request.get(`/stores/${id}`),
  create: (data: Partial<StoreItem>): Promise<StoreItem> => request.post('/stores', data),
  update: (id: string, data: Partial<StoreItem>): Promise<StoreItem> => request.put(`/stores/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/stores/${id}`),
}
