// 系统管理相关类型定义

// 组织节点
export interface OrgNode {
  id: string
  name: string
  parentId: string | null
  level: number
  memberCount: number
  children?: OrgNode[]
}

// 门店状态
export type StoreStatus = 'open' | 'suspended'

// 门店信息
export interface StoreItem {
  id: string
  name: string
  code: string
  region: string
  address: string
  contactPerson: string
  contactPhone: string
  status: StoreStatus
}

// 用户状态
export type UserStatus = 'enabled' | 'disabled'

// 用户信息
export interface UserItem {
  id: string
  username: string
  phone: string
  department: string
  role: string
  status: UserStatus
  createdAt: string
}

// 角色权限
export interface RoleItem {
  id: string
  name: string
  description: string
  permissions: PermissionItem[]
}

// 权限项
export interface PermissionItem {
  id: string
  name: string
  code: string
  type: 'menu' | 'button' | 'api'
  enabled: boolean
  children?: PermissionItem[]
}

// 系统配置
export interface SystemConfig {
  systemName: string
  language: string
  timezone: string
  dateFormat: string
  timeFormat: string
  version: string
  copyright: string
  logo?: string
  description?: string
  icpNumber?: string
  homeUrl?: string
}
