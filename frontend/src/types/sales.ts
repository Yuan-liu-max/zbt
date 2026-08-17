// 销售管理相关类型定义

// 客户类型
export type CustomerType = 'NEW' | 'OLD'

// 客户性别
export type CustomerGender = 'MALE' | 'FEMALE' | 'UNKNOWN'

// 客户年龄段
export type CustomerAgeRange = '18-25' | '26-35' | '36-45' | '46+'

// 购买场景
export type PurchaseScene = 'WEDDING' | 'GIFT' | 'SELF' | 'INVEST' | 'HOLIDAY' | 'OTHER'

// 审核状态
export type AuditStatus = 'PENDING' | 'AUDITED' | 'REJECTED'

// 销售记录
export interface SalesRecord {
  id: number
  salesNo: string
  storeId: number
  storeName: string
  employeeId: number
  employeeName: string
  salesDate: string
  totalAmount: number
  paidAmount: number
  customerType: CustomerType
  customerGender: CustomerGender
  customerAgeRange: CustomerAgeRange
  purchaseScene: PurchaseScene
  customerConcern: string
  salesPhotoUrls: string[]
  items: SalesItem[]
  productCount: number
  auditStatus: AuditStatus
  createdAt: string
}

// 销售明细
export interface SalesItem {
  id: number
  productName: string
  category: string
  spec?: string
  style: string
  material: string
  weight: string
  size: string
  color: string
  shape: string
  meaning: string
  price: number
  quantity: number
  customerFavoritePoint: string
  objection: string
  closingReason: string
  productPhotoUrls: string[]
}

// 销售查询参数
export interface SalesQueryParams {
  storeId?: number
  employeeId?: number
  startDate?: string
  endDate?: string
  auditStatus?: AuditStatus
  page: number
  size: number
}

// 销售统计（后端 GET /sales/stats）
export interface SalesStats {
  totalAmount: number
  orderCount: number
  todayAmount: number
}

// 员工排行（后端 GET /sales/ranking/employees → [{employeeId, amount}]）
export interface EmployeeRanking {
  employeeId: number
  amount: number
}

// 品类统计（后端 GET /sales/category-structure → [{category, amount}]）
export interface CategoryStats {
  category: string
  amount: number
}
