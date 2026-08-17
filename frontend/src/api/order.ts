import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  OrderRecord,
  OrderQueryParams,
  ReturnRecord,
  ReturnQueryParams,
  OrderStatus,
  ReturnStatus,
} from '@/types/order'

export const orderApi = {
  getList: (params: OrderQueryParams): Promise<PageResult<OrderRecord>> => request.get('/orders', { params }),
  getById: (id: string): Promise<OrderRecord> => request.get(`/orders/${id}`),
  getDetail: (id: string): Promise<OrderRecord> => request.get(`/orders/${id}`),
  cancel: (id: string): Promise<void> => request.put(`/orders/${id}/cancel`),
  ship: (id: string, data: { deliveryCompany: string; deliveryTrackNo: string }): Promise<void> => request.put(`/orders/${id}/ship`, data),
  update: (id: string, data: Partial<OrderRecord>): Promise<OrderRecord> => request.put(`/orders/${id}`, data),
}

export const returnApi = {
  getList: (params: ReturnQueryParams): Promise<PageResult<ReturnRecord>> => request.get('/orders/returns', { params }),
  getDetail: (id: string): Promise<ReturnRecord> => request.get(`/orders/returns/${id}`),
  cancel: (id: string): Promise<void> => request.put(`/orders/returns/${id}/cancel`),
  review: (id: string): Promise<void> => request.put(`/orders/returns/${id}/review`),
  approve: (id: string): Promise<void> => request.put(`/orders/returns/${id}/approve`),
  reject: (id: string): Promise<void> => request.put(`/orders/returns/${id}/reject`),
  complete: (id: string): Promise<void> => request.put(`/orders/returns/${id}/complete`),
}

export const orderStatusMap: Record<OrderStatus, { color: string; text: string }> = {
  pending: { color: 'orange', text: '待付款' },
  paid: { color: 'blue', text: '待发货' },
  shipped: { color: 'processing', text: '已发货' },
  completed: { color: 'green', text: '已完成' },
  cancelled: { color: 'default', text: '已取消' },
  refund: { color: 'red', text: '退款/售后' },
}

export const returnStatusMap: Record<ReturnStatus, { color: string; text: string }> = {
  applying: { color: 'orange', text: '申请中' },
  reviewing: { color: 'blue', text: '审核中' },
  approved: { color: 'green', text: '已同意' },
  rejected: { color: 'red', text: '已拒绝' },
  completed: { color: 'default', text: '已完成' },
  cancelled: { color: 'default', text: '已撤销' },
}
