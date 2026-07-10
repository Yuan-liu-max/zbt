// 订单管理 Mock 数据
import type {
  OrderRecord,
  ReturnRecord,
  OrderQueryParams,
  ReturnQueryParams,
  OrderStatus,
  PaymentStatus,
  ReturnStatus
} from '@/types/order'

// 订单数据
export const mockOrders: OrderRecord[] = [
  {
    id: '1', orderCode: 'DD202405240001',
    customerName: '张三', customerPhone: '138****5678',
    customerAddress: '广东省深圳市南山区科技园高新南一道10号',
    items: [
      { id: '1', productCode: 'SP000001', productName: '18K金钻石戒指', spec: '12号', quantity: 1, price: 5680 },
    ],
    totalAmount: 5680, freight: 0, couponDiscount: 0, orderAmount: 5680,
    orderStatus: 'pending', paymentStatus: 'unpaid',
    paymentMethod: '微信支付', deliveryMethod: '顺丰快递',
    createdAt: '2024-05-24 14:30:22',
    logs: [
      { time: '2024-05-24 14:30:22', content: '订单提交成功' },
      { time: '2024-05-24 14:31:05', content: '等待买家付款' },
    ]
  },
  {
    id: '2', orderCode: 'DD202405230008',
    customerName: '李四', customerPhone: '139****1234',
    customerAddress: '广东省广州市天河区体育西路100号',
    items: [
      { id: '2', productCode: 'SP000002', productName: '铂金项链', spec: '45cm', quantity: 1, price: 3980 },
    ],
    totalAmount: 3980, freight: 0, couponDiscount: 0, orderAmount: 3980,
    orderStatus: 'paid', paymentStatus: 'paid',
    paymentMethod: '支付宝', deliveryMethod: '顺丰快递',
    createdAt: '2024-05-23 11:20:15',
    logs: [
      { time: '2024-05-23 11:20:15', content: '订单提交成功' },
      { time: '2024-05-23 11:21:30', content: '买家已付款' },
    ]
  },
  {
    id: '3', orderCode: 'DD202405220015',
    customerName: '王五', customerPhone: '137****8888',
    customerAddress: '广东省深圳市福田区深南大道200号',
    items: [
      { id: '3', productCode: 'SP000005', productName: '黄金手镯', spec: '18cm', quantity: 1, price: 2680 },
    ],
    totalAmount: 2680, freight: 0, couponDiscount: 0, orderAmount: 2680,
    orderStatus: 'completed', paymentStatus: 'paid',
    paymentMethod: '微信支付', deliveryMethod: '顺丰快递',
    createdAt: '2024-05-22 09:15:33',
    logs: [
      { time: '2024-05-22 09:15:33', content: '订单提交成功' },
      { time: '2024-05-22 09:16:00', content: '买家已付款' },
      { time: '2024-05-22 10:30:00', content: '卖家已发货' },
      { time: '2024-05-23 14:00:00', content: '买家已确认收货' },
    ]
  },
  {
    id: '4', orderCode: 'DD202405210021',
    customerName: '赵六', customerPhone: '136****6666',
    customerAddress: '广东省深圳市南山区南海大道100号',
    items: [
      { id: '4', productCode: 'SP000004', productName: '翡翠吊坠', spec: 'A货', quantity: 1, price: 7880 },
    ],
    totalAmount: 7880, freight: 0, couponDiscount: 0, orderAmount: 7880,
    orderStatus: 'completed', paymentStatus: 'paid',
    paymentMethod: '微信支付', deliveryMethod: '顺丰快递',
    createdAt: '2024-05-21 16:45:18',
    logs: [
      { time: '2024-05-21 16:45:18', content: '订单提交成功' },
      { time: '2024-05-21 16:46:00', content: '买家已付款' },
      { time: '2024-05-21 17:00:00', content: '卖家已发货' },
      { time: '2024-05-22 10:00:00', content: '买家已确认收货' },
    ]
  },
  {
    id: '5', orderCode: 'DD202405200030',
    customerName: '钱七', customerPhone: '135****9999',
    customerAddress: '广东省深圳市宝安区新安街道100号',
    items: [
      { id: '5', productCode: 'SP000011', productName: '珍珠耳钉', spec: '8mm', quantity: 1, price: 980 },
    ],
    totalAmount: 980, freight: 0, couponDiscount: 0, orderAmount: 980,
    orderStatus: 'shipped', paymentStatus: 'paid',
    paymentMethod: '支付宝', deliveryMethod: '顺丰快递',
    createdAt: '2024-05-20 10:30:45',
    logs: [
      { time: '2024-05-20 10:30:45', content: '订单提交成功' },
      { time: '2024-05-20 10:31:00', content: '买家已付款' },
      { time: '2024-05-20 14:00:00', content: '卖家已发货' },
    ]
  },
]

// 退换货数据
export const mockReturns: ReturnRecord[] = [
  {
    id: '1', returnCode: 'TH202405240001', orderCode: 'DD202405240001',
    returnType: 'refund', reason: '尺寸不合适',
    applyTime: '2024-05-24 15:20:30', status: 'applying',
    orderAmount: 5680, productName: '18K金钻石戒指', productSpec: '12号', quantity: 1
  },
  {
    id: '2', returnCode: 'TH202405230002', orderCode: 'DD202405230008',
    returnType: 'exchange', reason: '款式不喜欢',
    applyTime: '2024-05-23 11:30:15', status: 'reviewing',
    orderAmount: 3980, productName: '铂金项链', productSpec: '45cm', quantity: 1
  },
  {
    id: '3', returnCode: 'TH202405220003', orderCode: 'DD202405220015',
    returnType: 'refund', reason: '质量问题',
    applyTime: '2024-05-22 16:45:22', status: 'approved',
    orderAmount: 2680, productName: '黄金手镯', productSpec: '18cm', quantity: 1
  },
]

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 订单状态映射
export const orderStatusMap: Record<OrderStatus, { color: string; text: string }> = {
  pending: { color: 'orange', text: '待付款' },
  paid: { color: 'blue', text: '待发货' },
  shipped: { color: 'processing', text: '已发货' },
  completed: { color: 'green', text: '已完成' },
  cancelled: { color: 'default', text: '已取消' },
  refund: { color: 'red', text: '退款/售后' },
}

// 退换货状态映射
export const returnStatusMap: Record<ReturnStatus, { color: string; text: string }> = {
  applying: { color: 'orange', text: '申请中' },
  reviewing: { color: 'blue', text: '审核中' },
  approved: { color: 'green', text: '已同意' },
  rejected: { color: 'red', text: '已拒绝' },
  completed: { color: 'default', text: '已完成' },
}

// 订单 API
export const orderApi = {
  async getList(params: OrderQueryParams) {
    await delay(300)
    let filtered = [...mockOrders]

    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.orderCode.includes(params.keyword!) || item.customerName.includes(params.keyword!)
      )
    }
    if (params.status) {
      filtered = filtered.filter(item => item.orderStatus === params.status)
    }
    if (params.startDate) {
      filtered = filtered.filter(item => item.createdAt >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.createdAt <= params.endDate!)
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

  async getById(id: string) {
    await delay(200)
    return mockOrders.find(item => item.id === id) || null
  },

  async update(id: string, data: Partial<OrderRecord>) {
    await delay(500)
    const index = mockOrders.findIndex(item => item.id === id)
    if (index !== -1) {
      mockOrders[index] = { ...mockOrders[index], ...data }
      return mockOrders[index]
    }
    return null
  },

  async cancel(id: string) {
    await delay(300)
    const index = mockOrders.findIndex(item => item.id === id)
    if (index !== -1) {
      mockOrders[index].orderStatus = 'cancelled'
      mockOrders[index].logs.push({ time: new Date().toISOString().replace('T', ' ').slice(0, 19), content: '订单已取消' })
      return mockOrders[index]
    }
    return null
  }
}

// 退换货 API
export const returnApi = {
  async getList(params: ReturnQueryParams) {
    await delay(300)
    let filtered = [...mockReturns]

    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.returnCode.includes(params.keyword!) || item.orderCode.includes(params.keyword!)
      )
    }
    if (params.returnType) {
      filtered = filtered.filter(item => item.returnType === params.returnType)
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
  }
}
