/**
 * 订单 API
 */
import http from './index'
import type { OrderRecord, ReturnRecord, PageResult } from '@/types'

export const orderApi = {
  list(params: { page?: number; pageSize?: number; status?: string }): Promise<PageResult<OrderRecord>> {
    return http.get('/orders', { params })
  },
  detail(id: string | number): Promise<OrderRecord> {
    return http.get(`/orders/${id}`)
  },
  create(data: Record<string, any>): Promise<OrderRecord> {
    return http.post('/orders', data)
  },
  cancel(id: string | number): Promise<void> {
    return http.put(`/orders/${id}/cancel`)
  },
  // 退换货
  listReturns(params: { page?: number; pageSize?: number; status?: string }): Promise<PageResult<ReturnRecord>> {
    return http.get('/orders/returns', { params })
  },
  createReturn(data: Record<string, any>): Promise<ReturnRecord> {
    return http.post('/orders/returns', data)
  },
  cancelReturn(id: string | number): Promise<void> {
    return http.put(`/orders/returns/${id}/cancel`)
  }
}
