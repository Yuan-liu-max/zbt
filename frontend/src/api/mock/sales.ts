// 销售管理 Mock 数据
import type { SalesRecord, SalesQueryParams, SalesStats, EmployeeRanking, CategoryStats } from '@/types/sales'

// 通用 API 响应结构
interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

// 分页响应结构
interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 销售记录
export const mockSales: SalesRecord[] = [
  {
    id: 1, salesNo: 'XS202405240001', storeId: 1, storeName: '深圳总店',
    employeeId: 101, employeeName: '张导', salesDate: '2024-05-24',
    totalAmount: 5680, paidAmount: 5680, customerType: 'OLD',
    customerGender: 'MALE', customerAgeRange: '36-45',
    purchaseScene: 'SELF', customerConcern: '品质与工艺',
    salesPhotoUrls: ['/uploads/sales/1/photo1.jpg'],
    items: [{
      id: 1, productName: '18K金钻石戒指', category: '戒指',
      style: '经典款', material: '18K金', weight: '3.5g', size: '12号',
      color: '金色', shape: '圆形', meaning: '永恒爱情',
      price: 5680, quantity: 1,
      customerFavoritePoint: '钻石切工精美', objection: '价格偏高',
      closingReason: '品质认可', productPhotoUrls: ['/uploads/sales/1/item1.jpg']
    }],
    productCount: 1, auditStatus: 'AUDITED',
    createdAt: '2024-05-24 14:30:00'
  },
  {
    id: 2, salesNo: 'XS202405230008', storeId: 1, storeName: '深圳总店',
    employeeId: 102, employeeName: '李导', salesDate: '2024-05-23',
    totalAmount: 3980, paidAmount: 3980, customerType: 'NEW',
    customerGender: 'FEMALE', customerAgeRange: '26-35',
    purchaseScene: 'WEDDING', customerConcern: '款式设计',
    salesPhotoUrls: ['/uploads/sales/2/photo1.jpg'],
    items: [{
      id: 2, productName: '铂金项链', category: '项链',
      style: '简约款', material: '铂金', weight: '5.2g', size: '45cm',
      color: '银白色', shape: '心形', meaning: '纯洁爱情',
      price: 3980, quantity: 1,
      customerFavoritePoint: '设计优雅', objection: '无',
      closingReason: '婚庆需求', productPhotoUrls: ['/uploads/sales/2/item1.jpg']
    }],
    productCount: 1, auditStatus: 'AUDITED',
    createdAt: '2024-05-23 11:20:00'
  },
  {
    id: 3, salesNo: 'XS202405220015', storeId: 1, storeName: '深圳总店',
    employeeId: 103, employeeName: '王导', salesDate: '2024-05-22',
    totalAmount: 2680, paidAmount: 2680, customerType: 'OLD',
    customerGender: 'FEMALE', customerAgeRange: '46+',
    purchaseScene: 'GIFT', customerConcern: '品牌与寓意',
    salesPhotoUrls: [],
    items: [{
      id: 3, productName: '黄金手镯', category: '手镯',
      style: '传统款', material: '足金', weight: '28g', size: '18cm',
      color: '金黄色', shape: '圆形', meaning: '平安幸福',
      price: 2680, quantity: 1,
      customerFavoritePoint: '寓意好', objection: '款式较传统',
      closingReason: '送礼合适', productPhotoUrls: ['/uploads/sales/3/item1.jpg']
    }],
    productCount: 1, auditStatus: 'PENDING',
    createdAt: '2024-05-22 09:15:00'
  },
  {
    id: 4, salesNo: 'XS202405210021', storeId: 1, storeName: '深圳总店',
    employeeId: 101, employeeName: '张导', salesDate: '2024-05-21',
    totalAmount: 7880, paidAmount: 7880, customerType: 'NEW',
    customerGender: 'MALE', customerAgeRange: '36-45',
    purchaseScene: 'HOLIDAY', customerConcern: '收藏价值',
    salesPhotoUrls: ['/uploads/sales/4/photo1.jpg', '/uploads/sales/4/photo2.jpg'],
    items: [{
      id: 4, productName: '翡翠吊坠', category: '吊坠',
      style: '古典款', material: '翡翠', weight: '15g', size: 'A货',
      color: '翠绿色', shape: '葫芦形', meaning: '福禄安康',
      price: 7880, quantity: 1,
      customerFavoritePoint: '翡翠成色好', objection: '真伪鉴定',
      closingReason: '节日送礼', productPhotoUrls: ['/uploads/sales/4/item1.jpg']
    }],
    productCount: 1, auditStatus: 'AUDITED',
    createdAt: '2024-05-21 16:45:00'
  },
  {
    id: 5, salesNo: 'XS202405200030', storeId: 1, storeName: '深圳总店',
    employeeId: 104, employeeName: '赵导', salesDate: '2024-05-20',
    totalAmount: 980, paidAmount: 980, customerType: 'OLD',
    customerGender: 'FEMALE', customerAgeRange: '18-25',
    purchaseScene: 'SELF', customerConcern: '性价比',
    salesPhotoUrls: [],
    items: [{
      id: 5, productName: '珍珠耳钉', category: '耳饰',
      style: '时尚款', material: '淡水珍珠', weight: '2g', size: '8mm',
      color: '白色', shape: '圆形', meaning: '优雅气质',
      price: 980, quantity: 1,
      customerFavoritePoint: '佩戴舒适', objection: '珍珠大小',
      closingReason: '自用购买', productPhotoUrls: ['/uploads/sales/5/item1.jpg']
    }],
    productCount: 1, auditStatus: 'AUDITED',
    createdAt: '2024-05-20 10:30:00'
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

// 包装响应
function wrapResponse<T>(data: T): ApiResponse<T> {
  return { code: 200, msg: 'success', data }
}

// 销售 API
export const salesApi = {
  async getList(params: SalesQueryParams): Promise<ApiResponse<PageResult<SalesRecord>>> {
    await delay(300)
    let filtered = [...mockSales]

    if (params.startDate) {
      filtered = filtered.filter(item => item.salesDate >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.salesDate <= params.endDate!)
    }
    if (params.storeId) {
      filtered = filtered.filter(item => item.storeId === params.storeId)
    }
    if (params.employeeId) {
      filtered = filtered.filter(item => item.employeeId === params.employeeId)
    }
    if (params.auditStatus) {
      filtered = filtered.filter(item => item.auditStatus === params.auditStatus)
    }

    const start = (params.page - 1) * params.size
    const end = start + params.size

    return wrapResponse({
      list: filtered.slice(start, end),
      total: filtered.length,
      page: params.page,
      size: params.size
    })
  },

  async getStats(): Promise<ApiResponse<SalesStats>> {
    await delay(200)
    return wrapResponse({ ...mockSalesStats })
  },

  async getEmployeeRanking(): Promise<ApiResponse<EmployeeRanking[]>> {
    await delay(200)
    return wrapResponse([...mockEmployeeRanking])
  },

  async getCategoryStats(): Promise<ApiResponse<CategoryStats[]>> {
    await delay(200)
    return wrapResponse([...mockCategoryStats])
  },

  async create(data: Partial<SalesRecord>): Promise<ApiResponse<SalesRecord>> {
    await delay(500)
    const newItem: SalesRecord = {
      id: mockSales.length + 1,
      salesNo: `XS${Date.now()}`,
      storeId: data.storeId || 1,
      storeName: data.storeName || '深圳总店',
      employeeId: data.employeeId || 101,
      employeeName: data.employeeName || '张导',
      salesDate: data.salesDate || new Date().toISOString().slice(0, 10),
      totalAmount: data.totalAmount || 0,
      paidAmount: data.paidAmount || 0,
      customerType: data.customerType || 'NEW',
      customerGender: data.customerGender || 'UNKNOWN',
      customerAgeRange: data.customerAgeRange || '26-35',
      purchaseScene: data.purchaseScene || 'SELF',
      customerConcern: data.customerConcern || '',
      salesPhotoUrls: data.salesPhotoUrls || [],
      items: data.items || [],
      productCount: data.productCount || 0,
      auditStatus: 'PENDING',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockSales.unshift(newItem)
    return wrapResponse(newItem)
  }
}
