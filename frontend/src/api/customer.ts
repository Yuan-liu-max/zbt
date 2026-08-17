import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  CustomerItem,
  CustomerQueryParams,
  MemberLevel,
  MemberQueryParams,
  MemberStats,
} from '@/types/customer'

export const customerApi = {
  getList: (params: CustomerQueryParams): Promise<PageResult<CustomerItem>> => request.get('/customers', { params }),
  getDetail: (id: string): Promise<CustomerItem> => request.get(`/customers/${id}`),
  create: (data: Partial<CustomerItem>): Promise<CustomerItem> => request.post('/customers', data),
  update: (id: string, data: Partial<CustomerItem>): Promise<CustomerItem> => request.put(`/customers/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/customers/${id}`),
}

export const memberApi = {
  getList: (params: MemberQueryParams): Promise<PageResult<MemberLevel>> => request.get('/member-levels', { params }),
  getStats: (): Promise<MemberStats> => request.get('/member-levels/stats'),
  create: (data: Partial<MemberLevel>): Promise<MemberLevel> => request.post('/member-levels', data),
  update: (id: string, data: Partial<MemberLevel>): Promise<MemberLevel> => request.put(`/member-levels/${id}`, data),
}

export const levelColorMap: Record<string, string> = {
  diamond: 'purple',
  vip: 'gold',
  normal: 'blue',
}

export const levelTextMap: Record<string, string> = {
  diamond: '钻石会员',
  vip: 'VIP',
  normal: '普通会员',
}
