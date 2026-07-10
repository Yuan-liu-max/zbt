// 销售管理 Mock 数据
import type { SalesRecord, SalesQueryParams, SalesStats, EmployeeRanking, CategoryStats } from '@/types/sales'

// 销售记录
export const mockSales: SalesRecord[] = [
  {
    id: '1', orderCode: 'XS202405240001', storeId: '1', storeName: '深圳总店',
    employeeId: '101', employeeName: '张导', salesDate: '2024-05-24',
    totalAmount: 5680, paidAmount: 5680, customerType: 'old', purchaseScene: 'self',
    items: [{ id: '1', productName: '18K金钻石戒指', category: '戒指', spec: '12号', price: 5680, quantity: 1, amount: 5680 }],
    status: 'approved', createdAt: '2024-05-24 14:30:00'
  },
  {
    id: '2', orderCode: 'XS202405230008', storeId: '1', storeName: '深圳总店',
    employeeId: '102', employeeName: '李导', salesDate: '2024-05-23',
    totalAmount: 3980, paidAmount: 3980, customerType: 'new', purchaseScene: 'wedding',
    items: [{ id: '2', productName: '铂金项链', category: '项链', spec: '45cm', price: 3980, quantity: 1, amount: 3980 }],
    status: 'approved', createdAt: '2024-05-23 11:20:00'
  },
  {
    id: '3', orderCode: 'XS202405220015', storeId: '1', storeName: '深圳总店',
    employeeId: '103', employeeName: '王导', salesDate: '2024-05-22',
    totalAmount: 2680, paidAmount: 2680, customerType: 'old', purchaseScene: 'gift',
    items: [{ id: '3', productName: '黄金手镯', category: '手镯', spec: '18cm', price: 2680, quantity: 1, amount: 2680 }],
    status: 'pending', createdAt: '2024-05-22 09:15:00'
  },
  {
    id: '4', orderCode: 'XS202405210021', storeId: '1', storeName: '深圳总店',
    employeeId: '101', employeeName: '张导', salesDate: '2024-05-21',
    totalAmount: 7880, paidAmount: 7880, customerType: 'new', purchaseScene: 'holiday',
    items: [{ id: '4', productName: '翡翠吊坠', category: '吊坠', spec: 'A货', price: 7880, quantity: 1, amount: 7880 }],
    status: 'approved', createdAt: '2024-05-21 16:45:00'
  },
  {
    id: '5', orderCode: 'XS202405200030', storeId: '1', storeName: '深圳总店',
    employeeId: '104', employeeName: '赵导', salesDate: '2024-05-20',
    totalAmount: 980, paidAmount: 980, customerType: 'old', purchaseScene: 'self',
    items: [{ id: '5', productName: '珍珠耳钉', category: '耳饰', spec: '8mm', price: 980, quantity: 1, amount: 980 }],
    status: 'approved', createdAt: '2024-05-20 10:30:00'
  },
]

// 统计数据
export const mockSalesStats: SalesStats = {
  totalSales: 21320,
  completionRate: 85.5,
  newCustomerRatio: 35,
  oldCustomerRatio: 65,
  orderCount: 156,
  avgOrderAmount: 3680
}

// 员工排行
export const mockEmployeeRanking: EmployeeRanking[] = [
  { rank: 1, name: '张导', salesAmount: 125800, orderCount: 38, avgOrderAmount: 3310 },
  { rank: 2, name: '李导', salesAmount: 98600, orderCount: 32, avgOrderAmount: 3081 },
  { rank: 3, name: '王导', salesAmount: 86400, orderCount: 28, avgOrderAmount: 3086 },
  { rank: 4, name: '赵导', salesAmount: 72300, orderCount: 25, avgOrderAmount: 2892 },
  { rank: 5, name: '孙导', salesAmount: 65800, orderCount: 22, avgOrderAmount: 2991 },
]

// 品类统计
export const mockCategoryStats: CategoryStats[] = [
  { name: '戒指', value: 86500, percentage: 32 },
  { name: '项链', value: 72300, percentage: 27 },
  { name: '手镯', value: 54200, percentage: 20 },
  { name: '吊坠', value: 36100, percentage: 13 },
  { name: '耳饰', value: 21600, percentage: 8 },
]

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 销售 API
export const salesApi = {
  async getList(params: SalesQueryParams) {
    await delay(300)
    let filtered = [...mockSales]

    if (params.startDate) {
      filtered = filtered.filter(item => item.salesDate >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.salesDate <= params.endDate!)
    }

    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize

    return {
      list: filtered.slice(start, end),
      total: filtered.length,
      page: params.page,
      pageSize: params.pageSize
    }
  },

  async getStats(): Promise<SalesStats> {
    await delay(200)
    return { ...mockSalesStats }
  },

  async getEmployeeRanking(): Promise<EmployeeRanking[]> {
    await delay(200)
    return [...mockEmployeeRanking]
  },

  async getCategoryStats(): Promise<CategoryStats[]> {
    await delay(200)
    return [...mockCategoryStats]
  },

  async create(data: Partial<SalesRecord>) {
    await delay(500)
    const newItem: SalesRecord = {
      id: String(mockSales.length + 1),
      orderCode: `XS${Date.now()}`,
      storeId: data.storeId || '1',
      storeName: data.storeName || '深圳总店',
      employeeId: data.employeeId || '101',
      employeeName: data.employeeName || '张导',
      salesDate: data.salesDate || new Date().toISOString().slice(0, 10),
      totalAmount: data.totalAmount || 0,
      paidAmount: data.paidAmount || 0,
      customerType: data.customerType || 'new',
      purchaseScene: data.purchaseScene || 'self',
      items: data.items || [],
      status: 'pending',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockSales.unshift(newItem)
    return newItem
  }
}
