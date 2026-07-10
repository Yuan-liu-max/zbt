// 订单管理相关类型定义

// 订单状态
export type OrderStatus = 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled' | 'refund'

// 支付状态
export type PaymentStatus = 'unpaid' | 'paid' | 'refunded'

// 退换货申请状态
export type ReturnStatus = 'applying' | 'reviewing' | 'approved' | 'rejected' | 'completed'

// 退换货类型
export type ReturnType = 'refund' | 'exchange'

// 订单商品
export interface OrderItem {
  id: string
  productCode: string
  productName: string
  imageUrl?: string
  spec: string
  quantity: number
  price: number
}

// 订单操作记录
export interface OrderLog {
  time: string
  content: string
}

// 订单信息
export interface OrderRecord {
  id: string
  orderCode: string          // 订单号
  customerName: string       // 客户姓名
  customerPhone: string      // 联系电话
  customerAddress: string    // 收货地址
  items: OrderItem[]         // 商品列表
  totalAmount: number        // 商品总额
  freight: number            // 运费
  couponDiscount: number     // 优惠券
  orderAmount: number        // 订单总额
  orderStatus: OrderStatus   // 订单状态
  paymentStatus: PaymentStatus // 支付状态
  paymentMethod: string      // 支付方式
  deliveryMethod: string     // 配送方式
  remark?: string            // 买家留言
  createdAt: string          // 下单时间
  logs: OrderLog[]           // 操作记录
}

// 退换货申请
export interface ReturnRecord {
  id: string
  returnCode: string         // 申请单号
  orderCode: string          // 订单号
  returnType: ReturnType     // 申请类型
  reason: string             // 申请原因
  applyTime: string          // 申请时间
  status: ReturnStatus       // 状态
  orderAmount?: number       // 订单金额
  productName?: string       // 商品名称
  productSpec?: string       // 商品规格
  imageUrl?: string          // 商品图片
  quantity?: number          // 数量
}

// 订单查询参数
export interface OrderQueryParams {
  keyword?: string           // 订单号/客户姓名
  status?: OrderStatus       // 订单状态
  startDate?: string         // 开始日期
  endDate?: string           // 结束日期
  page: number
  pageSize: number
}

// 退换货查询参数
export interface ReturnQueryParams {
  keyword?: string           // 申请单号/订单号
  returnType?: ReturnType    // 申请类型
  status?: ReturnStatus      // 状态
  startDate?: string         // 开始日期
  endDate?: string           // 结束日期
  page: number
  pageSize: number
}
