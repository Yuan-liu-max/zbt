// 系统管理相关类型定义

// 组织节点
export interface OrgNode {
  id: string
  orgName: string
  name?: string // 兼容后端返回 name 字段
  parentId: string | null
  orgType: string
  orgCode?: string
  sortOrder: number
  status: string
  memberCount?: number // 成员数量（兼容后端返回）
  level?: number // 层级（兼容后端返回）
  children?: OrgNode[]
}

// 门店状态（全大写）
export type StoreStatus = 'OPEN' | 'SUSPENDED' | 'CLOSED'

// 门店类型（全大写）
export type StoreType = 'NEW' | 'OLD' | 'FLAGSHIP' | 'NORMAL'

// 门店信息（与后端 Store 字段对齐）
export interface StoreItem {
  id: string
  orgId?: string | number
  storeName: string
  storeCode: string
  regionId: string | number
  address: string
  storeManagerId?: string | number
  openingDate?: string
  storeType: StoreType
  status: StoreStatus
  businessHours: string
  contactPhone: string
}

// 用户状态（全大写，与后端一致）
export type UserStatus = 'ENABLED' | 'DISABLED'

// 用户信息
export interface UserItem {
  id: string
  username: string
  realName?: string
  phone: string
  position?: string
  storeId?: number
  roleNames?: string[]
  roleIds?: number[]
  status: UserStatus
  createdAt: string
}

// 角色权限（与后端 Role 字段对齐）
export interface RoleItem {
  id: string
  roleCode?: string
  roleName: string
  dataScope?: string
  status?: string
  remark?: string
  createdAt?: string
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

// 权限树节点（与后端 PermissionService.tree() 返回结构对齐）
export interface SysPermissionNode {
  id: string | number
  parentId?: string | number
  permName: string
  permType: string
  permCode?: string
  path?: string
  sortOrder?: number
  children?: SysPermissionNode[]
}

// 系统配置项（与后端 SystemConfig 字段对齐）
export interface SystemConfigItem {
  id?: number
  configKey: string
  configValue?: string
  configGroup?: string
  description?: string
  status?: string
  sortOrder?: number
}

// 系统配置（表单态）
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
