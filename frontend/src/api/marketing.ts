import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  ActivityItem,
  ActivityQueryParams,
  PromotionItem,
  PromotionQueryParams,
} from '@/types/marketing'

export const activityApi = {
  getList: (params: ActivityQueryParams): Promise<PageResult<ActivityItem>> => request.get('/activities', { params }),
  create: (data: Partial<ActivityItem>): Promise<ActivityItem> => request.post('/activities', data),
  update: (id: string, data: Partial<ActivityItem>): Promise<ActivityItem> => request.put(`/activities/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/activities/${id}`),
}

export const promotionApi = {
  getList: (params: PromotionQueryParams): Promise<PageResult<PromotionItem>> => request.get('/promotions', { params }),
  create: (data: Partial<PromotionItem>): Promise<PromotionItem> => request.post('/promotions', data),
  update: (id: string, data: Partial<PromotionItem>): Promise<PromotionItem> => request.put(`/promotions/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/promotions/${id}`),
}

export const activityStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  not_started: { text: '未开始', color: 'orange' },
}

export const promotionStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  not_started: { text: '未开始', color: 'orange' },
}

export const activityTypeMap: Record<string, string> = {
  promotion: '促销活动',
  new_customer: '新客活动',
  theme: '主题活动',
  membership: '会员活动',
}

export const promotionTypeMap: Record<string, string> = {
  discount: '折扣',
  full_reduction: '满减',
  gift: '赠品',
  member_price: '会员价',
}
