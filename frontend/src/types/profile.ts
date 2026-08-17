// 个人中心相关类型定义

// 通知消息（对应后端 Notification 实体）
export interface NotificationItem {
  id: string | number
  receiverId?: string | number
  title: string
  content: string
  notificationType?: string
  businessType?: string
  businessId?: string | number
  isRead: 0 | 1
  readAt?: string
  channel?: string
  sendStatus?: string
  createdAt: string
}

// 站内信/私信（对应后端 sys_message 实体）
export interface MessageItem {
  id: string | number
  senderId?: string | number
  receiverId?: string | number
  title: string
  content: string
  isRead: 0 | 1
  readAt?: string
  createdAt: string
}

// 用户信息
export interface UserInfo {
  id: string
  username: string
  realName?: string
  avatar?: string
  role: string
  roles?: string[]
  roleNames?: string[]
  phone: string
  email: string
  storeId?: string
  storeName?: string
  regionId?: string
  regionName?: string
  position?: string
  entryDate?: string
  timezone: string
  language: string
  dateFormat: string
  registeredAt?: string
  createdAt?: string
  lastLoginAt?: string
  notifySystem?: boolean
  notifyOrder?: boolean
  notifyInventory?: boolean
  notifyMarketing?: boolean
}

// 用户统计
export interface UserStats {
  productCount: number
  orderCount: number
  customerCount: number
  todaySales: number
}
