/**
 * 收货地址 API
 */
import http from './index'
import type { AddressItem } from '@/types'

export const addressApi = {
  /** 获取地址列表 */
  list(): Promise<AddressItem[]> {
    return http.get('/addresses')
  },
  /** 新增地址 */
  create(data: Omit<AddressItem, 'id'>): Promise<AddressItem> {
    return http.post('/addresses', data)
  },
  /** 更新地址 */
  update(id: string | number, data: Partial<AddressItem>): Promise<void> {
    return http.put(`/addresses/${id}`, data)
  },
  /** 删除地址 */
  delete(id: string | number): Promise<void> {
    return http.delete(`/addresses/${id}`)
  },
  /** 设为默认地址 */
  setDefault(id: string | number): Promise<void> {
    return http.put(`/addresses/${id}/default`)
  }
}
