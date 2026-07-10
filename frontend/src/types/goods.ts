// 商品管理相关类型定义

// 商品状态
export type GoodsStatus = 'on' | 'off'

// 商品分类层级
export type CategoryLevel = 1 | 2 | 3

// 商品信息
export interface GoodsItem {
  id: string
  code: string           // 商品编号
  name: string           // 商品名称
  categoryId: string     // 分类ID
  categoryName: string   // 分类名称
  brandId: string        // 品牌ID
  brandName: string      // 品牌名称
  price: number          // 售价
  costPrice: number      // 成本价（敏感字段）
  grossMarginRate: number // 毛利率（敏感字段）
  stock: number          // 库存数量
  storeId: string        // 所属门店ID
  storeName: string      // 所属门店名称
  status: GoodsStatus    // 状态：上架/下架
  imageUrl?: string      // 商品图片
  description?: string   // 商品描述
  createdAt: string      // 创建时间
  updatedAt?: string     // 更新时间
}

// 商品分类
export interface GoodsCategory {
  id: string
  name: string           // 分类名称
  parentId: string | null // 父级分类ID
  level: CategoryLevel   // 分类层级
  sort: number           // 排序
  status: GoodsStatus    // 状态
  children?: GoodsCategory[] // 子分类
  createdAt: string
}

// 品牌信息
export interface BrandItem {
  id: string
  name: string           // 品牌名称
  logo?: string          // 品牌LOGO
  origin: string         // 品牌产地
  sort: number           // 排序
  status: GoodsStatus    // 状态
  description?: string   // 品牌描述
  createdAt: string
}

// 门店信息（用于筛选）
export interface StoreItem {
  id: string
  name: string
}

// 商品查询参数
export interface GoodsQueryParams {
  keyword?: string       // 关键词（商品名称）
  categoryId?: string    // 分类
  status?: GoodsStatus   // 状态
  storeId?: string       // 门店
  page: number
  pageSize: number
}

// 品牌查询参数
export interface BrandQueryParams {
  name?: string
  status?: GoodsStatus
  page: number
  pageSize: number
}

// 分页响应
export interface PaginatedResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// ==================== 库存管理类型 ====================

// 库存状态（三级）
export type InventoryStatus = 'normal' | 'warning' | 'shortage'

// 盘点状态
export type CheckStatus = 'planning' | 'counting' | 'completed' | 'cancelled'

// 盘点记录
export interface InventoryCheckRecord {
  id: string
  checkCode: string        // 盘点单号
  checkName: string        // 盘点名称
  warehouse: string        // 仓库
  checkType: string        // 盘点类型
  startDate: string        // 开始日期
  endDate: string          // 结束日期
  creator: string          // 创建人
  createdAt: string        // 创建时间
  status: CheckStatus      // 状态
  remark?: string
}

// 盘点查询参数
export interface InventoryCheckParams {
  checkCode?: string
  warehouse?: string
  status?: CheckStatus
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}

// 预警类型
export type WarningType = 'shortage' | 'warning' | 'expiring' | 'transit_timeout'

// 预警记录
export interface InventoryWarningItem {
  id: string
  alertType: WarningType
  productCode: string
  productName: string
  spec: string
  warehouse: string
  currentQty: number
  safetyStock: number
  threshold: string
  alertTime: string
  status: 'pending' | 'handled'
  handleTime?: string
  handler?: string
}

// 预警统计
export interface InventoryStats {
  shortageCount: number
  warningCount: number
  expiringCount: number
  transitTimeoutCount: number
}

// 预警查询参数
export interface InventoryWarningParams {
  alertType?: WarningType
  keyword?: string
  warehouse?: string
  status?: 'pending' | 'handled'
  page: number
  pageSize: number
}
