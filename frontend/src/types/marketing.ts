// 营销管理相关类型定义

// 活动状态
export type ActivityStatus = 'ongoing' | 'ended' | 'not_started'

// 活动类型
export type ActivityType = 'promotion' | 'new_customer' | 'theme' | 'membership'

// 营销活动
export interface ActivityItem {
  id: string
  name: string              // 活动名称
  type: ActivityType        // 活动类型
  startTime: string         // 开始时间
  endTime: string           // 结束时间
  status: ActivityStatus    // 活动状态
  scope: string             // 参与范围
  registeredCount: number   // 报名人数
  totalCount: number        // 参与总人数
  createdAt: string
}

// 促销状态
export type PromotionStatus = 'ongoing' | 'ended' | 'not_started'

// 促销类型
export type PromotionType = 'discount' | 'full_reduction' | 'gift' | 'member_price'

// 促销管理
export interface PromotionItem {
  id: string
  name: string              // 促销名称
  type: PromotionType       // 促销类型
  discountMethod: string    // 优惠方式
  startTime: string         // 开始时间
  endTime: string           // 结束时间
  status: PromotionStatus   // 促销状态
  scope: string             // 适用范围
  usageCount: number        // 使用次数
  createdAt: string
}

// 活动查询参数
export interface ActivityQueryParams {
  name?: string
  status?: ActivityStatus
  type?: ActivityType
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}

// 促销查询参数
export interface PromotionQueryParams {
  name?: string
  status?: PromotionStatus
  type?: PromotionType
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}
