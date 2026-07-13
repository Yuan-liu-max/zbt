// 个人中心相关类型定义

// 提醒消息
export interface NotificationItem {
  id: string
  type: 'system' | 'order' | 'customer' | 'activity' | 'operation'
  title: string
  content: string
  time: string
  isRead: boolean
}

// 信息消息
export interface MessageItem {
  id: string
  type: 'system' | 'platform' | 'customer' | 'assistant' | 'operation'
  title: string
  summary: string
  content: string
  time: string
  isRead: boolean
  extra?: string
}

// 用户信息
export interface UserInfo {
  id: string
  username: string
  avatar?: string
  role: string
  phone: string
  email: string
  timezone: string
  language: string
  dateFormat: string
  registeredAt: string
  lastLoginAt: string
}

// 用户统计
export interface UserStats {
  productCount: number
  orderCount: number
  todayVisitors: number
  todaySales: number
}
