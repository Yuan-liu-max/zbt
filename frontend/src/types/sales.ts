// 销售管理相关类型定义

// 客户类型
export type CustomerType = 'new' | 'old'

// 购买场景
export type PurchaseScene = 'wedding' | 'gift' | 'self' | 'invest' | 'holiday' | 'other'

// 销售状态
export type SalesStatus = 'pending' | 'approved' | 'rejected'

// 销售记录
export interface SalesRecord {
  id: string
  orderCode: string          // 销售单号
  storeId: string            // 门店
  storeName: string          // 门店名称
  employeeId: string         // 导购
  employeeName: string       // 导购姓名
  salesDate: string          // 销售日期
  totalAmount: number        // 总金额
  paidAmount: number         // 实付金额
  customerType: CustomerType // 客户类型
  purchaseScene: PurchaseScene // 购买场景
  items: SalesItem[]         // 商品明细
  status: SalesStatus        // 审核状态
  createdAt: string
}

// 销售明细
export interface SalesItem {
  id: string
  productName: string        // 商品名称
  category: string           // 品类
  spec: string               // 规格
  price: number              // 单价
  quantity: number           // 数量
  amount: number             // 金额
}

// 销售查询参数
export interface SalesQueryParams {
  storeId?: string
  employeeId?: string
  startDate?: string
  endDate?: string
  auditStatus?: SalesStatus
  page: number
  pageSize: number
}

// 销售统计
export interface SalesStats {
  totalSales: number         // 销售额
  completionRate: number     // 完成率
  newCustomerRatio: number   // 新客比
  oldCustomerRatio: number   // 老客比
  orderCount: number         // 订单数
  avgOrderAmount: number     // 客单价
}

// 员工排行
export interface EmployeeRanking {
  rank: number
  name: string
  salesAmount: number
  orderCount: number
  avgOrderAmount: number
}

// 品类统计
export interface CategoryStats {
  name: string
  value: number
  percentage: number
}
