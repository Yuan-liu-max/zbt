// 客户管理相关类型定义

// 客户等级
export type CustomerLevel = 'normal' | 'vip' | 'diamond'

// 客户状态
export type CustomerStatus = 'normal' | 'disabled'

// 会员等级状态
export type MemberLevelStatus = 'enabled' | 'disabled'

// 客户信息
export interface CustomerItem {
  id: string
  code: string              // 客户编号
  name: string              // 客户姓名
  phone: string             // 手机号
  level: CustomerLevel      // 客户等级
  totalConsumption: number  // 累计消费(元)
  points: number            // 积分
  registeredAt: string      // 注册时间
  lastConsumptionAt: string // 最近消费时间
  status: CustomerStatus    // 状态
}

// 会员等级
export interface MemberLevel {
  id: string
  name: string              // 会员等级名称
 标识: string               // 会员标识
  memberCount: number       // 会员数量
  totalConsumption: number  // 累计消费(元)
  pointsMultiplier: number  // 积分倍数
  discount: number          // 折扣率
  benefits: string          // 专属权益
  status: MemberLevelStatus // 状态
}

// 会员等级统计
export interface MemberStats {
  vipCount: number          // VIP会员数
  normalCount: number       // 普通会员数
  diamondCount: number      // 钻石会员数
  totalCount: number        // 会员总数
}

// 客户查询参数
export interface CustomerQueryParams {
  name?: string             // 客户姓名
  phone?: string            // 手机号
  level?: CustomerLevel     // 等级
  startDate?: string        // 注册开始日期
  endDate?: string          // 注册结束日期
  page: number
  pageSize: number
}

// 会员查询参数
export interface MemberQueryParams {
  level?: string            // 会员等级
  name?: string             // 会员姓名
  phone?: string            // 手机号
  status?: MemberLevelStatus // 状态
  page: number
  pageSize: number
}
