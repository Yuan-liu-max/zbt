// 数据报表 Mock 数据
import type { ReportStats, SalesTrend, ChannelStats, RankingItem } from '@/types/report'

// 报表统计
export const mockReportStats: ReportStats = {
  totalSales: 1250000,
  totalOrders: 1280,
  totalCustomers: 860,
  avgOrderAmount: 976.56,
  grossProfit: 465800,
  salesChange: 12.5,
  ordersChange: 8.3,
  customersChange: 5.6,
  avgOrderChange: 3.2,
  profitChange: 10.8
}

// 销售趋势
export const mockSalesTrend: SalesTrend[] = [
  { date: '05-01', salesAmount: 45000, orderCount: 38 },
  { date: '05-03', salesAmount: 52000, orderCount: 42 },
  { date: '05-05', salesAmount: 48000, orderCount: 40 },
  { date: '05-07', salesAmount: 61000, orderCount: 52 },
  { date: '05-09', salesAmount: 55000, orderCount: 45 },
  { date: '05-11', salesAmount: 68000, orderCount: 58 },
  { date: '05-13', salesAmount: 72000, orderCount: 62 },
  { date: '05-15', salesAmount: 85000, orderCount: 72 },
  { date: '05-17', salesAmount: 78000, orderCount: 65 },
  { date: '05-19', salesAmount: 92000, orderCount: 78 },
  { date: '05-21', salesAmount: 88000, orderCount: 75 },
  { date: '05-23', salesAmount: 95000, orderCount: 82 },
  { date: '05-25', salesAmount: 105000, orderCount: 88 },
  { date: '05-27', salesAmount: 98000, orderCount: 85 },
  { date: '05-29', salesAmount: 112000, orderCount: 92 },
  { date: '05-31', salesAmount: 118000, orderCount: 95 },
]

// 渠道占比
export const mockChannelStats: ChannelStats[] = [
  { name: '门店销售', value: 531250, percentage: 42.5, color: '#1890ff' },
  { name: '线上商城', value: 358750, percentage: 28.7, color: '#52c41a' },
  { name: '微信小程序', value: 205000, percentage: 16.4, color: '#faad14' },
  { name: '其他渠道', value: 155000, percentage: 12.4, color: '#722ed1' },
]

// 商品排行
export const mockProductRanking: RankingItem[] = [
  { rank: 1, name: '足金999项链', code: 'SP20240001', quantity: 156, salesAmount: 312000, percentage: 24.9 },
  { rank: 2, name: '18K金手链', code: 'SP20240002', quantity: 132, salesAmount: 198000, percentage: 15.8 },
  { rank: 3, name: '足金999戒指', code: 'SP20240003', quantity: 98, salesAmount: 156800, percentage: 12.5 },
  { rank: 4, name: '钻石吊坠', code: 'SP20240004', quantity: 76, salesAmount: 125600, percentage: 10.0 },
  { rank: 5, name: '翡翠手镯', code: 'SP20240005', quantity: 45, salesAmount: 98500, percentage: 7.9 },
]

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 报表 API
export const reportApi = {
  async getStats(): Promise<ReportStats> {
    await delay(200)
    return { ...mockReportStats }
  },

  async getSalesTrend(): Promise<SalesTrend[]> {
    await delay(200)
    return [...mockSalesTrend]
  },

  async getChannelStats(): Promise<ChannelStats[]> {
    await delay(200)
    return [...mockChannelStats]
  },

  async getProductRanking(): Promise<RankingItem[]> {
    await delay(200)
    return [...mockProductRanking]
  },

  async exportReport() {
    await delay(1000)
    return { success: true }
  }
}
