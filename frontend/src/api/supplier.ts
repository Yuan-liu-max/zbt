import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { SupplierItem, SupplierQueryParams } from '@/types/supplier'

export const supplierApi = {
  getList: (params: SupplierQueryParams): Promise<PageResult<SupplierItem>> => request.get('/suppliers', { params }),
  getDetail: (id: string): Promise<SupplierItem> => request.get(`/suppliers/${id}`),
  create: (data: Partial<SupplierItem>): Promise<SupplierItem> => request.post('/suppliers', data),
  update: (id: string, data: Partial<SupplierItem>): Promise<SupplierItem> => request.put(`/suppliers/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/suppliers/${id}`),
}

export const supplierTypeMap: Record<string, { text: string; color: string }> = {
  raw_material: { text: '原材料供应商', color: 'blue' },
  gemstone: { text: '宝石供应商', color: 'purple' },
  pearl: { text: '珍珠供应商', color: 'gold' },
  processing: { text: '加工服务商', color: 'orange' },
  packaging: { text: '包装供应商', color: 'cyan' },
  consumable: { text: '耗材供应商', color: 'red' },
}

export const cooperationStatusMap: Record<string, { text: string; color: string }> = {
  cooperating: { text: '合作中', color: 'green' },
  suspended: { text: '已暂停', color: 'orange' },
  terminated: { text: '已终止', color: 'red' },
}