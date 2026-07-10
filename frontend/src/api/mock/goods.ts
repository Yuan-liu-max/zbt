// 商品管理 Mock 数据
import type { GoodsItem, GoodsCategory, BrandItem, GoodsStatus, StoreItem } from '@/types/goods'

// 门店数据
export const mockStores: StoreItem[] = [
  { id: '1', name: '总店' },
  { id: '2', name: '北京旗舰店' },
  { id: '3', name: '上海中心店' },
  { id: '4', name: '深圳万象城店' },
]

// 品牌数据
export const mockBrands: BrandItem[] = [
  { id: '1', name: '周大福', logo: '', origin: '中国香港', sort: 1, status: 'on', createdAt: '2024-01-10 09:30:21' },
  { id: '2', name: '老凤祥', logo: '', origin: '中国大陆', sort: 2, status: 'on', createdAt: '2024-01-12 10:15:30' },
  { id: '3', name: '周生生', logo: '', origin: '中国香港', sort: 3, status: 'on', createdAt: '2024-01-15 11:45:18' },
  { id: '4', name: '七彩云南', logo: '', origin: '中国大陆', sort: 4, status: 'on', createdAt: '2024-01-18 14:20:33' },
  { id: '5', name: 'TASAKI', logo: '', origin: '日本', sort: 5, status: 'on', createdAt: '2024-01-20 16:05:42' },
]

// 分类数据（树形结构）
export const mockCategories: GoodsCategory[] = [
  {
    id: '1', name: '戒指', parentId: null, level: 1, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '1-1', name: '黄金戒指', parentId: '1', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '1-2', name: '钻石戒指', parentId: '1', level: 2, sort: 2, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '1-3', name: 'K金戒指', parentId: '1', level: 2, sort: 3, status: 'on', createdAt: '2024-01-01 00:00:00' },
    ]
  },
  {
    id: '2', name: '项链', parentId: null, level: 1, sort: 2, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '2-1', name: '黄金项链', parentId: '2', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '2-2', name: '钻石项链', parentId: '2', level: 2, sort: 2, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '2-3', name: 'K金项链', parentId: '2', level: 2, sort: 3, status: 'on', createdAt: '2024-01-01 00:00:00' },
    ]
  },
  {
    id: '3', name: '手链', parentId: null, level: 1, sort: 3, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '3-1', name: '黄金手链', parentId: '3', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '3-2', name: '钻石手链', parentId: '3', level: 2, sort: 2, status: 'on', createdAt: '2024-01-01 00:00:00' },
    ]
  },
  {
    id: '4', name: '吊坠', parentId: null, level: 1, sort: 4, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '4-1', name: '黄金吊坠', parentId: '4', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '4-2', name: '钻石吊坠', parentId: '4', level: 2, sort: 2, status: 'on', createdAt: '2024-01-01 00:00:00' },
    ]
  },
  {
    id: '5', name: '耳饰', parentId: null, level: 1, sort: 5, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '5-1', name: '黄金耳饰', parentId: '5', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '5-2', name: '钻石耳饰', parentId: '5', level: 2, sort: 2, status: 'on', createdAt: '2024-01-01 00:00:00' },
    ]
  },
  {
    id: '6', name: '手镯', parentId: null, level: 1, sort: 6, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '6-1', name: '黄金手镯', parentId: '6', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
    ]
  },
  {
    id: '7', name: '摆件', parentId: null, level: 1, sort: 7, status: 'on', createdAt: '2024-01-01 00:00:00',
  },
  {
    id: '8', name: '配饰', parentId: null, level: 1, sort: 8, status: 'on', createdAt: '2024-01-01 00:00:00',
    children: [
      { id: '8-1', name: '胸针', parentId: '8', level: 2, sort: 1, status: 'on', createdAt: '2024-01-01 00:00:00' },
      { id: '8-2', name: '领带夹', parentId: '8', level: 2, sort: 2, status: 'off', createdAt: '2024-01-01 00:00:00' },
    ]
  },
]

// 商品数据
export const mockGoods: GoodsItem[] = [
  { id: '1', code: 'SP000001', name: '足金项链', categoryId: '2-1', categoryName: '黄金饰品', brandId: '1', brandName: '周大福', price: 5280, costPrice: 3200, grossMarginRate: 39.4, stock: 120, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 10:30:21' },
  { id: '2', code: 'SP000002', name: '18K金戒圈', categoryId: '1-3', categoryName: '黄金饰品', brandId: '1', brandName: '周大福', price: 3680, costPrice: 3100, grossMarginRate: 15.8, stock: 80, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 11:20:15' },
  { id: '3', code: 'SP000003', name: '铂金吊坠', categoryId: '4-1', categoryName: '铂金饰品', brandId: '3', brandName: '周生生', price: 2980, costPrice: 1800, grossMarginRate: 39.6, stock: 30, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 13:15:42' },
  { id: '4', code: 'SP000004', name: '钻石耳钉', categoryId: '5-2', categoryName: '钻石饰品', brandId: '3', brandName: '周生生', price: 6800, costPrice: 4500, grossMarginRate: 33.8, stock: 12, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 14:22:33' },
  { id: '5', code: 'SP000005', name: '翡翠手镯', categoryId: '6-1', categoryName: '翡翠饰品', brandId: '4', brandName: '七彩云南', price: 15800, costPrice: 9800, grossMarginRate: 38.0, stock: 25, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 15:40:18' },
  { id: '6', code: 'SP000006', name: 'K金手链', categoryId: '3-2', categoryName: 'K金饰品', brandId: '6', brandName: '周大生', price: 1280, costPrice: 980, grossMarginRate: 23.4, stock: 5, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 16:00:00' },
  { id: '7', code: 'SP000007', name: '黄金戒指', categoryId: '1-1', categoryName: '黄金饰品', brandId: '2', brandName: '老凤祥', price: 3280, costPrice: 2600, grossMarginRate: 20.7, stock: 45, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 16:30:00' },
  { id: '8', code: 'SP000008', name: '钻石项链', categoryId: '2-2', categoryName: '钻石饰品', brandId: '3', brandName: '周生生', price: 8999, costPrice: 5500, grossMarginRate: 38.9, stock: 8, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 17:00:00' },
  { id: '9', code: 'SP000009', name: '银饰耳环', categoryId: '5-1', categoryName: '银饰', brandId: '5', brandName: 'TASAKI', price: 680, costPrice: 350, grossMarginRate: 48.5, stock: 200, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 17:30:00' },
  { id: '10', code: 'SP000010', name: '黄金吊坠', categoryId: '4-1', categoryName: '黄金饰品', brandId: '1', brandName: '周大福', price: 2580, costPrice: 1800, grossMarginRate: 30.2, stock: 38, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 18:00:00' },
  { id: '11', code: 'SP000011', name: '珍珠项链', categoryId: '2-1', categoryName: '珍珠饰品', brandId: '5', brandName: 'TASAKI', price: 4580, costPrice: 2800, grossMarginRate: 38.9, stock: 18, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 18:30:00' },
  { id: '12', code: 'SP000012', name: '黄金手镯', categoryId: '6-1', categoryName: '黄金饰品', brandId: '2', brandName: '老凤祥', price: 12800, costPrice: 9500, grossMarginRate: 25.8, stock: 15, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 19:00:00' },
  { id: '13', code: 'SP000013', name: '钻石戒指', categoryId: '1-2', categoryName: '钻石饰品', brandId: '3', brandName: '周生生', price: 9999, costPrice: 6000, grossMarginRate: 40.0, stock: 6, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 19:30:00' },
  { id: '14', code: 'SP000014', name: '黄金耳环', categoryId: '5-1', categoryName: '黄金饰品', brandId: '1', brandName: '周大福', price: 1680, costPrice: 1100, grossMarginRate: 34.5, stock: 55, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 20:00:00' },
  { id: '15', code: 'SP000015', name: '铂金项链', categoryId: '2-3', categoryName: '铂金饰品', brandId: '3', brandName: '周生生', price: 3980, costPrice: 2500, grossMarginRate: 37.2, stock: 22, storeId: '1', storeName: '深圳总仓', status: 'on', createdAt: '2024-05-24 20:30:00' },
]

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 门店 API
export const storeApi = {
  async getAll() {
    await delay(200)
    return [...mockStores]
  }
}

// 商品 API
export const goodsApi = {
  // 获取商品列表
  async getList(params: { keyword?: string; categoryId?: string; status?: GoodsStatus; storeId?: string; page: number; pageSize: number }) {
    await delay(300)
    let filtered = [...mockGoods]

    if (params.keyword) {
      filtered = filtered.filter(item => item.name.includes(params.keyword!))
    }
    if (params.categoryId) {
      filtered = filtered.filter(item => item.categoryId.startsWith(params.categoryId!))
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
    }
    if (params.storeId) {
      filtered = filtered.filter(item => item.storeId === params.storeId)
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

  // 获取商品详情
  async getById(id: string) {
    await delay(200)
    return mockGoods.find(item => item.id === id) || null
  },

  // 创建商品
  async create(data: Partial<GoodsItem>) {
    await delay(500)
    const newItem: GoodsItem = {
      id: String(mockGoods.length + 1),
      code: `SP${Date.now()}`,
      name: data.name || '',
      categoryId: data.categoryId || '',
      categoryName: data.categoryName || '',
      brandId: data.brandId || '',
      brandName: data.brandName || '',
      price: data.price || 0,
      costPrice: data.costPrice || 0,
      grossMarginRate: data.grossMarginRate || 0,
      stock: data.stock || 0,
      storeId: data.storeId || '',
      storeName: data.storeName || '',
      status: data.status || 'on',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockGoods.push(newItem)
    return newItem
  },

  // 更新商品
  async update(id: string, data: Partial<GoodsItem>) {
    await delay(500)
    const index = mockGoods.findIndex(item => item.id === id)
    if (index !== -1) {
      mockGoods[index] = { ...mockGoods[index], ...data }
      return mockGoods[index]
    }
    return null
  },

  // 删除商品
  async delete(id: string) {
    await delay(300)
    const index = mockGoods.findIndex(item => item.id === id)
    if (index !== -1) {
      mockGoods.splice(index, 1)
      return true
    }
    return false
  }
}

// 分类 API
export const categoryApi = {
  // 获取分类树
  async getTree() {
    await delay(300)
    return [...mockCategories]
  },

  // 获取分类列表（平铺）
  async getList() {
    await delay(200)
    const flatten = (items: GoodsCategory[]): GoodsCategory[] => {
      return items.reduce<GoodsCategory[]>((acc, item) => {
        acc.push(item)
        if (item.children) {
          acc.push(...flatten(item.children))
        }
        return acc
      }, [])
    }
    return flatten(mockCategories)
  },

  // 创建分类
  async create(data: Partial<GoodsCategory>) {
    await delay(500)
    const newItem: GoodsCategory = {
      id: String(Date.now()),
      name: data.name || '',
      parentId: data.parentId || null,
      level: data.level || 1,
      sort: data.sort || 1,
      status: data.status || 'on',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    if (data.parentId) {
      const findParent = (items: GoodsCategory[]): boolean => {
        for (const item of items) {
          if (item.id === data.parentId) {
            if (!item.children) item.children = []
            item.children.push(newItem)
            return true
          }
          if (item.children && findParent(item.children)) return true
        }
        return false
      }
      findParent(mockCategories)
    } else {
      mockCategories.push(newItem)
    }
    return newItem
  },

  // 更新分类
  async update(id: string, data: Partial<GoodsCategory>) {
    await delay(500)
    const findAndUpdate = (items: GoodsCategory[]): boolean => {
      for (const item of items) {
        if (item.id === id) {
          Object.assign(item, data)
          return true
        }
        if (item.children && findAndUpdate(item.children)) return true
      }
      return false
    }
    findAndUpdate(mockCategories)
    return data
  },

  // 删除分类
  async delete(id: string) {
    await delay(300)
    const findAndDelete = (items: GoodsCategory[]): boolean => {
      for (let i = 0; i < items.length; i++) {
        if (items[i].id === id) {
          items.splice(i, 1)
          return true
        }
        if (items[i].children && findAndDelete(items[i].children!)) return true
      }
      return false
    }
    return findAndDelete(mockCategories)
  }
}

// 品牌 API
export const brandApi = {
  // 获取品牌列表
  async getList(params: { name?: string; status?: GoodsStatus; page: number; pageSize: number }) {
    await delay(300)
    let filtered = [...mockBrands]

    if (params.name) {
      filtered = filtered.filter(item => item.name.includes(params.name!))
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
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

  // 获取所有品牌（下拉选择用）
  async getAll() {
    await delay(200)
    return mockBrands.filter(item => item.status === 'on')
  },

  // 创建品牌
  async create(data: Partial<BrandItem>) {
    await delay(500)
    const newItem: BrandItem = {
      id: String(mockBrands.length + 1),
      name: data.name || '',
      logo: data.logo || '',
      origin: data.origin || '',
      sort: data.sort || mockBrands.length + 1,
      status: data.status || 'on',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockBrands.push(newItem)
    return newItem
  },

  // 更新品牌
  async update(id: string, data: Partial<BrandItem>) {
    await delay(500)
    const index = mockBrands.findIndex(item => item.id === id)
    if (index !== -1) {
      mockBrands[index] = { ...mockBrands[index], ...data }
      return mockBrands[index]
    }
    return null
  },

  // 删除品牌
  async delete(id: string) {
    await delay(300)
    const index = mockBrands.findIndex(item => item.id === id)
    if (index !== -1) {
      mockBrands.splice(index, 1)
      return true
    }
    return false
  }
}

// ==================== 库存管理 Mock 数据 ====================

import type { InventoryCheckRecord, InventoryWarningItem, InventoryStats, CheckStatus, WarningType, InventoryCheckParams, InventoryWarningParams } from '@/types/goods'

// 仓库数据
export const mockWarehouses = [
  { id: '1', name: '深圳总仓', code: 'WH001' },
  { id: '2', name: '北京分仓', code: 'WH002' },
  { id: '3', name: '上海分仓', code: 'WH003' },
]

// 盘点数据
export const mockChecks: InventoryCheckRecord[] = [
  {
    id: '1', checkCode: 'PD20240524001', checkName: '深圳总仓月度盘点',
    warehouse: '深圳总仓', checkType: '月度盘点',
    startDate: '2024-05-24', endDate: '2024-05-24',
    creator: '管理员', createdAt: '2024-05-20 10:30:00', status: 'counting'
  },
  {
    id: '2', checkCode: 'PD20240515001', checkName: '深圳总仓周盘点',
    warehouse: '深圳总仓', checkType: '周盘点',
    startDate: '2024-05-15', endDate: '2024-05-15',
    creator: '管理员', createdAt: '2024-05-13 09:15:00', status: 'completed'
  },
  {
    id: '3', checkCode: 'PD20240430001', checkName: '深圳总仓月度盘点',
    warehouse: '深圳总仓', checkType: '月度盘点',
    startDate: '2024-04-30', endDate: '2024-04-30',
    creator: '管理员', createdAt: '2024-04-28 10:20:00', status: 'completed'
  },
  {
    id: '4', checkCode: 'PD20240415001', checkName: '深圳总仓周盘点',
    warehouse: '深圳总仓', checkType: '周盘点',
    startDate: '2024-04-15', endDate: '2024-04-15',
    creator: '管理员', createdAt: '2024-04-13 09:10:00', status: 'cancelled'
  },
]

// 预警数据
export const mockWarnings: InventoryWarningItem[] = [
  {
    id: '1', alertType: 'shortage', productCode: 'SP000004', productName: '钻石耳钉',
    spec: '约0.50ct', warehouse: '深圳总仓', currentQty: 10, safetyStock: 15,
    threshold: '≤ 15', alertTime: '2024-05-24 09:30:00', status: 'pending'
  },
  {
    id: '2', alertType: 'warning', productCode: 'SP000003', productName: '铂金吊坠',
    spec: '约5.20g', warehouse: '深圳总仓', currentQty: 28, safetyStock: 10,
    threshold: '≤ 10', alertTime: '2024-05-24 09:25:00', status: 'pending'
  },
  {
    id: '3', alertType: 'warning', productCode: 'SP000005', productName: '翡翠手镯',
    spec: '约55.00mm', warehouse: '深圳总仓', currentQty: 22, safetyStock: 10,
    threshold: '≤ 10', alertTime: '2024-05-24 09:20:00', status: 'pending'
  },
  {
    id: '4', alertType: 'expiring', productCode: 'SP000006', productName: 'K金手链',
    spec: '约8.60g', warehouse: '深圳总仓', currentQty: 5, safetyStock: 0,
    threshold: '30天', alertTime: '2024-05-24 09:15:00', status: 'pending'
  },
  {
    id: '5', alertType: 'transit_timeout', productCode: 'SP000009', productName: '银饰耳环',
    spec: '约2.30g', warehouse: '深圳总仓', currentQty: 195, safetyStock: 50,
    threshold: '7天', alertTime: '2024-05-24 09:10:00', status: 'handled',
    handleTime: '2024-05-24 10:00:00', handler: '管理员'
  },
  {
    id: '6', alertType: 'shortage', productCode: 'SP000010', productName: '黄金吊坠',
    spec: '约3.80g', warehouse: '深圳总仓', currentQty: 6, safetyStock: 15,
    threshold: '≤ 15', alertTime: '2024-05-24 09:05:00', status: 'pending'
  },
]

// 盘点 API
export const inventoryCheckApi = {
  async getList(params: InventoryCheckParams) {
    await delay(300)
    let filtered = [...mockChecks]

    if (params.checkCode) {
      filtered = filtered.filter(item => item.checkCode.includes(params.checkCode!))
    }
    if (params.warehouse) {
      filtered = filtered.filter(item => item.warehouse === params.warehouse)
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
    }
    if (params.startDate) {
      filtered = filtered.filter(item => item.startDate >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.endDate <= params.endDate!)
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

  async create(data: Partial<InventoryCheckRecord>) {
    await delay(500)
    const newItem: InventoryCheckRecord = {
      id: String(mockChecks.length + 1),
      checkCode: `PD${Date.now()}`,
      checkName: data.checkName || '',
      warehouse: data.warehouse || '',
      checkType: data.checkType || '周盘点',
      startDate: data.startDate || '',
      endDate: data.endDate || '',
      creator: data.creator || '管理员',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19),
      status: 'planning'
    }
    mockChecks.unshift(newItem)
    return newItem
  },

  async update(id: string, data: Partial<InventoryCheckRecord>) {
    await delay(500)
    const index = mockChecks.findIndex(item => item.id === id)
    if (index !== -1) {
      mockChecks[index] = { ...mockChecks[index], ...data }
      return mockChecks[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockChecks.findIndex(item => item.id === id)
    if (index !== -1) {
      mockChecks.splice(index, 1)
      return true
    }
    return false
  }
}

// 预警 API
export const inventoryWarningApi = {
  async getStats(): Promise<InventoryStats> {
    await delay(200)
    const shortageCount = mockWarnings.filter(a => a.alertType === 'shortage' && a.status === 'pending').length
    const warningCount = mockWarnings.filter(a => a.alertType === 'warning' && a.status === 'pending').length
    const expiringCount = mockWarnings.filter(a => a.alertType === 'expiring' && a.status === 'pending').length
    const transitTimeoutCount = mockWarnings.filter(a => a.alertType === 'transit_timeout' && a.status === 'pending').length
    return { shortageCount, warningCount, expiringCount, transitTimeoutCount }
  },

  async getList(params: InventoryWarningParams) {
    await delay(300)
    let filtered = [...mockWarnings]

    if (params.alertType) {
      filtered = filtered.filter(item => item.alertType === params.alertType)
    }
    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.productName.includes(params.keyword!) || item.productCode.includes(params.keyword!)
      )
    }
    if (params.warehouse) {
      filtered = filtered.filter(item => item.warehouse === params.warehouse)
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
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

  async handleAlert(id: string) {
    await delay(300)
    const index = mockWarnings.findIndex(item => item.id === id)
    if (index !== -1) {
      mockWarnings[index].status = 'handled'
      mockWarnings[index].handleTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
      mockWarnings[index].handler = '管理员'
      return mockWarnings[index]
    }
    return null
  }
}
