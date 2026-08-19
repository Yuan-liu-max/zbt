/**
 * 优惠券 API —— 营销三端打通（管理端配置 → C端领取 → 下单抵扣）
 */
import http from './index'
import type { PageResult } from '@/types'

export interface CouponItem {
  id: string | number
  userId: number
  promotionId?: number
  name: string
  type: string
  discountMethod?: string
  threshold?: number
  discountValue?: number
  status: 'UNUSED' | 'USED' | 'EXPIRED' | 'DISABLED'
  expireTime?: string
  receivedAt?: string
}

export const couponApi = {
  /** 可领取的优惠券列表 */
  available(): Promise<Array<Record<string, any>>> {
    return http.get('/shop/coupons/available')
  },
  /** 领取优惠券 */
  receive(promotionId: number | string): Promise<CouponItem> {
    return http.post(`/shop/coupons/receive/${promotionId}`)
  },
  /** 我的优惠券 */
  mine(params: { page?: number; pageSize?: number; status?: string }): Promise<PageResult<CouponItem>> {
    return http.get('/shop/coupons/mine', { params })
  },
  /** 未使用优惠券数量 */
  unusedCount(): Promise<{ count: number }> {
    return http.get('/shop/coupons/unused-count')
  },
}
