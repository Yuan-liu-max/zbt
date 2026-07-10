// 供应商管理相关类型定义

// 供应商类型
export type SupplierType = 'raw_material' | 'gemstone' | 'pearl' | 'processing' | 'packaging' | 'consumable'

// 合作状态
export type CooperationStatus = 'cooperating' | 'suspended' | 'terminated'

// 供应商信息
export interface SupplierItem {
  id: string
  name: string              // 供应商名称
  logo?: string             // 供应商LOGO
  type: SupplierType        // 供应商类型
  contactPerson: string     // 联系人
  contactPhone: string      // 联系电话
  email: string             // 邮箱
  status: CooperationStatus // 合作状态
  address?: string          // 地址
  remark?: string           // 备注
  createdAt: string         // 创建时间
}

// 供应商查询参数
export interface SupplierQueryParams {
  name?: string             // 供应商名称
  contactPerson?: string    // 联系人
  type?: SupplierType       // 供应商类型
  status?: CooperationStatus // 状态
  page: number
  pageSize: number
}
