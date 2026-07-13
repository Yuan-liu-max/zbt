// 营销管理 Mock 数据
import type { ActivityItem, PromotionItem, ActivityQueryParams, PromotionQueryParams, ActivityStatus, ActivityType, PromotionStatus, PromotionType } from '@/types/marketing'

// 营销活动数据
export const mockActivities: ActivityItem[] = [
  { id: '1', name: '618年中大促', type: 'promotion', startTime: '2024-06-01 00:00:00', endTime: '2024-06-20 23:59:59', status: 'ongoing', scope: '全平台用户', registeredCount: 2345, totalCount: 5000, createdAt: '2024-05-20 14:30:00' },
  { id: '2', name: '新客专享礼', type: 'new_customer', startTime: '2024-05-10 00:00:00', endTime: '2024-06-10 23:59:59', status: 'ended', scope: '新用户', registeredCount: 1234, totalCount: 2000, createdAt: '2024-05-01 10:15:00' },
  { id: '3', name: '夏日清凉节', type: 'theme', startTime: '2024-07-01 00:00:00', endTime: '2024-07-31 23:59:59', status: 'not_started', scope: '全平台用户', registeredCount: 0, totalCount: 10000, createdAt: '2024-06-15 09:20:00' },
  { id: '4', name: '老客户回馈', type: 'membership', startTime: '2024-04-15 00:00:00', endTime: '2024-05-15 23:59:59', status: 'ended', scope: '会员用户', registeredCount: 856, totalCount: 1500, createdAt: '2024-04-01 08:00:00' },
]

// 促销管理数据
export const mockPromotions: PromotionItem[] = [
  { id: '1', name: '满300减30', type: 'full_reduction', discountMethod: '满300元减30元', startTime: '2024-06-01 00:00:00', endTime: '2024-06-30 23:59:59', status: 'ongoing', scope: '指定商品', usageCount: 1234, createdAt: '2024-05-25 11:20:00' },
  { id: '2', name: '折扣优惠', type: 'discount', discountMethod: '全场8折', startTime: '2024-05-20 00:00:00', endTime: '2024-06-20 23:59:59', status: 'ended', scope: '全场商品', usageCount: 2345, createdAt: '2024-05-10 09:30:00' },
  { id: '3', name: '买一送一', type: 'gift', discountMethod: '买指定商品送同款', startTime: '2024-06-10 00:00:00', endTime: '2024-06-25 23:59:59', status: 'not_started', scope: '指定商品', usageCount: 0, createdAt: '2024-06-01 14:00:00' },
  { id: '4', name: '会员专享价', type: 'member_price', discountMethod: '会员专享8折', startTime: '长期有效', endTime: '', status: 'ongoing', scope: '会员用户', usageCount: 856, createdAt: '2024-04-01 16:45:00' },
]

// 状态映射
export const activityStatusMap: Record<ActivityStatus, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  not_started: { text: '未开始', color: 'orange' },
}

export const promotionStatusMap: Record<PromotionStatus, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  not_started: { text: '未开始', color: 'orange' },
}

// 类型映射
export const activityTypeMap: Record<ActivityType, string> = {
  promotion: '促销活动',
  new_customer: '新客活动',
  theme: '主题活动',
  membership: '会员活动',
}

export const promotionTypeMap: Record<PromotionType, string> = {
  discount: '折扣',
  full_reduction: '满减',
  gift: '赠品',
  member_price: '会员价',
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 活动 API
export const activityApi = {
  async getList(params: ActivityQueryParams) {
    await delay(300)
    let filtered = [...mockActivities]

    if (params.name) {
      filtered = filtered.filter(item => item.name.includes(params.name!))
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
    }
    if (params.type) {
      filtered = filtered.filter(item => item.type === params.type)
    }

    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize

    return {
      list: filtered.slice(start, end),
      total: filtered.length,
      page: params.page,
      pageSize: params.pageSize
    }
  },

  async create(data: Partial<ActivityItem>) {
    await delay(500)
    const newItem: ActivityItem = {
      id: String(mockActivities.length + 1),
      name: data.name || '',
      type: data.type || 'promotion',
      startTime: data.startTime || '',
      endTime: data.endTime || '',
      status: 'not_started',
      scope: data.scope || '全平台用户',
      registeredCount: 0,
      totalCount: data.totalCount || 0,
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockActivities.push(newItem)
    return newItem
  },

  async update(id: string, data: Partial<ActivityItem>) {
    await delay(500)
    const index = mockActivities.findIndex(item => item.id === id)
    if (index !== -1) {
      mockActivities[index] = { ...mockActivities[index], ...data }
      return mockActivities[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockActivities.findIndex(item => item.id === id)
    if (index !== -1) {
      mockActivities.splice(index, 1)
      return true
    }
    return false
  }
}

// 促销 API
export const promotionApi = {
  async getList(params: PromotionQueryParams) {
    await delay(300)
    let filtered = [...mockPromotions]

    if (params.name) {
      filtered = filtered.filter(item => item.name.includes(params.name!))
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
    }
    if (params.type) {
      filtered = filtered.filter(item => item.type === params.type)
    }

    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize

    return {
      list: filtered.slice(start, end),
      total: filtered.length,
      page: params.page,
      pageSize: params.pageSize
    }
  },

  async create(data: Partial<PromotionItem>) {
    await delay(500)
    const newItem: PromotionItem = {
      id: String(mockPromotions.length + 1),
      name: data.name || '',
      type: data.type || 'discount',
      discountMethod: data.discountMethod || '',
      startTime: data.startTime || '',
      endTime: data.endTime || '',
      status: 'not_started',
      scope: data.scope || '全场商品',
      usageCount: 0,
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockPromotions.push(newItem)
    return newItem
  },

  async update(id: string, data: Partial<PromotionItem>) {
    await delay(500)
    const index = mockPromotions.findIndex(item => item.id === id)
    if (index !== -1) {
      mockPromotions[index] = { ...mockPromotions[index], ...data }
      return mockPromotions[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockPromotions.findIndex(item => item.id === id)
    if (index !== -1) {
      mockPromotions.splice(index, 1)
      return true
    }
    return false
  }
}
