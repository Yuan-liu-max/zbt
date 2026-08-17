// 珠宝通商城 — 类型定义（复用 frontend 管理后台类型体系）

// ==================== 通用 ====================
export interface PageResult<T> {
  page: number
  size: number
  total: number
  list: T[]
}

export interface PageParams {
  page: number
  pageSize: number
}

// ==================== 商品 ====================
export interface ProductItem {
  id: string | number
  code: string           // 商品编号（对齐 frontend GoodsItem）
  name: string           // 商品名称
  categoryId?: string    // 分类ID
  categoryName: string   // 分类名称
  brandId?: string       // 品牌ID
  brandName?: string     // 品牌名称
  price: number
  costPrice?: number
  stock: number
  material?: string
  weight?: string
  size?: string
  color?: string
  description?: string
  imageUrl?: string
  storeId?: number
  storeName?: string
  status: string
  createdAt?: string
}

export interface CategoryNode {
  id: string
  name: string
  parentId?: string
  level?: number
  sort?: number
  children?: CategoryNode[]
}

export interface BrandItem {
  id: string
  name: string
  logo?: string
  origin?: string
  status?: string
}

export interface StoreItem {
  id: number
  name?: string
  storeName?: string
  storeCode?: string
  address?: string
  contactPhone?: string
  businessHours?: string
}

// ==================== 订单 ====================
export interface OrderItem {
  id?: string
  productId: number
  productCode: string
  productName: string
  imageUrl?: string
  spec?: string
  quantity: number
  price: number
}

export interface OrderRecord {
  id: string
  orderCode: string
  customerName: string
  customerPhone?: string
  customerAddress?: string
  items: OrderItem[]
  totalAmount: number
  freight: number
  couponDiscount: number
  orderAmount: number
  orderStatus: string
  paymentStatus?: string
  paymentMethod?: string
  deliveryMethod?: string
  deliveryCompany?: string
  deliveryTrackNo?: string
  paymentTime?: string
  paymentTradeNo?: string
  receiveTime?: string
  remark?: string
  logs?: OrderLog[]
  createdAt: string
}

export interface OrderLog {
  time: string
  content: string
}

export interface ReturnRecord {
  id: string
  returnCode?: string
  orderCode: string
  returnType: string
  reason: string
  applyTime?: string
  status: string
  orderAmount?: number
  productName?: string
  quantity?: number
}

// ==================== 用户 ====================
export interface UserInfo {
  userId: number
  username: string
  realName?: string
  avatar?: string
  phone?: string
  email?: string
  storeId?: number
  storeName?: string
  regionId?: number
  regionName?: string
  position?: string
  entryDate?: string
  roles?: string[]
  roleNames?: string[]
  // 偏好设置
  timezone?: string
  language?: string
  dateFormat?: string
  notifySystem?: boolean
  notifyOrder?: boolean
  notifyInventory?: boolean
  notifyMarketing?: boolean
  // 时间戳
  lastLoginAt?: string
  createdAt?: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  phone?: string
}

// ==================== 购物车 ====================
export interface CartItem extends ProductItem {
  cartQuantity: number
  checked: boolean
}

// ==================== 地址 ====================
export interface AddressItem {
  id: string | number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: boolean | number
}

// ==================== 通知 ====================
export interface NotificationItem {
  id: string
  title: string
  content: string
  isRead: number
  notificationType?: string
  createdAt: string
}

// ==================== 促销 ====================
export interface PromotionItem {
  id: string
  name: string
  type: string
  discountMethod?: string
  startTime: string
  endTime: string
  status: string
  scope?: string
}

// ==================== 商品查询 ====================
export interface ProductQueryParams extends PageParams {
  keyword?: string
  categoryId?: string
  brandId?: string
  status?: string
  storeId?: number
  minPrice?: number
  maxPrice?: number
  sortBy?: 'price' | 'createdAt'
  sortOrder?: 'asc' | 'desc'
}
