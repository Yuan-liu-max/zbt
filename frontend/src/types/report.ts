// 数据报表相关类型定义

// 报表统计
export interface ReportStats {
  totalSales: number        // 销售额
  totalOrders: number       // 订单数
  totalCustomers: number    // 客户数
  avgOrderAmount: number    // 客单价
  grossProfit: number       // 毛利额
  salesChange: number       // 销售额变化
  ordersChange: number      // 订单数变化
  customersChange: number   // 客户数变化
  avgOrderChange: number    // 客单价变化
  profitChange: number      // 毛利额变化
}

// 销售趋势数据
export interface SalesTrend {
  date: string
  salesAmount: number
  orderCount: number
}

// 渠道占比
export interface ChannelStats {
  name: string
  value: number
  percentage: number
  color: string
}

// 排行榜项
export interface RankingItem {
  rank: number
  name: string
  code: string
  quantity: number
  salesAmount: number
  percentage: number
}

// 报表查询参数
export interface ReportQueryParams {
  startDate: string
  endDate: string
  storeId?: string
}
