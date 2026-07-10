// 客户管理 Mock 数据
import type {
  CustomerItem,
  MemberLevel,
  MemberStats,
  CustomerQueryParams,
  MemberQueryParams,
  CustomerLevel,
  CustomerStatus
} from '@/types/customer'

// 客户数据
export const mockCustomers: CustomerItem[] = [
  { id: '1', code: 'CUST20250528001', name: '张女士', phone: '138****5678', level: 'vip', totalConsumption: 28560, points: 2860, registeredAt: '2025-01-15 10:30', lastConsumptionAt: '2025-05-27 16:45', status: 'normal' },
  { id: '2', code: 'CUST20250528002', name: '李先生', phone: '139****1234', level: 'normal', totalConsumption: 6890, points: 689, registeredAt: '2025-02-20 14:20', lastConsumptionAt: '2025-05-25 11:20', status: 'normal' },
  { id: '3', code: 'CUST20250528003', name: '王女士', phone: '137****4321', level: 'vip', totalConsumption: 52300, points: 5230, registeredAt: '2024-11-05 09:15', lastConsumptionAt: '2025-05-28 10:10', status: 'normal' },
  { id: '4', code: 'CUST20250528004', name: '陈先生', phone: '136****8765', level: 'normal', totalConsumption: 3260, points: 326, registeredAt: '2025-03-10 16:40', lastConsumptionAt: '2025-05-20 09:30', status: 'normal' },
  { id: '5', code: 'CUST20250528005', name: '刘女士', phone: '158****2345', level: 'diamond', totalConsumption: 156800, points: 15680, registeredAt: '2024-08-18 11:30', lastConsumptionAt: '2025-05-28 15:30', status: 'normal' },
]

// 会员等级数据
export const mockMemberLevels: MemberLevel[] = [
  { id: '1', name: '钻石会员', '标识': 'Diamond', memberCount: 32, totalConsumption: 2568000, pointsMultiplier: 3, discount: 8.5, benefits: '生日礼遇,专属客服,优先预订', status: 'enabled' },
  { id: '2', name: 'VIP会员', '标识': 'VIP', memberCount: 128, totalConsumption: 1256800, pointsMultiplier: 2, discount: 9, benefits: '积分加倍,会员日优惠,免费保养', status: 'enabled' },
  { id: '3', name: '普通会员', '标识': 'Normal', memberCount: 356, totalConsumption: 856300, pointsMultiplier: 1, discount: 9.5, benefits: '积分累计,会员专享价', status: 'enabled' },
]

// 等级颜色
export const levelColorMap: Record<CustomerLevel, string> = {
  diamond: 'purple',
  vip: 'gold',
  normal: 'blue'
}

// 等级文本
export const levelTextMap: Record<CustomerLevel, string> = {
  diamond: '钻石会员',
  vip: 'VIP',
  normal: '普通会员'
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 客户 API
export const customerApi = {
  async getList(params: CustomerQueryParams) {
    await delay(300)
    let filtered = [...mockCustomers]

    if (params.name) {
      filtered = filtered.filter(item => item.name.includes(params.name!))
    }
    if (params.phone) {
      filtered = filtered.filter(item => item.phone.includes(params.phone!))
    }
    if (params.level) {
      filtered = filtered.filter(item => item.level === params.level)
    }
    if (params.startDate) {
      filtered = filtered.filter(item => item.registeredAt >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.registeredAt <= params.endDate!)
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
    return mockCustomers.find(item => item.id === id) || null
  },

  async create(data: Partial<CustomerItem>) {
    await delay(500)
    const newItem: CustomerItem = {
      id: String(mockCustomers.length + 1),
      code: `CUST${Date.now()}`,
      name: data.name || '',
      phone: data.phone || '',
      level: data.level || 'normal',
      totalConsumption: 0,
      points: 0,
      registeredAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      lastConsumptionAt: '-',
      status: 'normal'
    }
    mockCustomers.push(newItem)
    return newItem
  },

  async update(id: string, data: Partial<CustomerItem>) {
    await delay(500)
    const index = mockCustomers.findIndex(item => item.id === id)
    if (index !== -1) {
      mockCustomers[index] = { ...mockCustomers[index], ...data }
      return mockCustomers[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockCustomers.findIndex(item => item.id === id)
    if (index !== -1) {
      mockCustomers.splice(index, 1)
      return true
    }
    return false
  }
}

// 会员 API
export const memberApi = {
  async getStats(): Promise<MemberStats> {
    await delay(200)
    const vipCount = mockMemberLevels.find(m => m.name === 'VIP会员')?.memberCount || 0
    const normalCount = mockMemberLevels.find(m => m.name === '普通会员')?.memberCount || 0
    const diamondCount = mockMemberLevels.find(m => m.name === '钻石会员')?.memberCount || 0
    return { vipCount, normalCount, diamondCount, totalCount: vipCount + normalCount + diamondCount }
  },

  async getList(params: MemberQueryParams) {
    await delay(300)
    let filtered = [...mockMemberLevels]

    if (params.level) {
      filtered = filtered.filter(item => item.name.includes(params.level!))
    }
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

  async create(data: Partial<MemberLevel>) {
    await delay(500)
    const newItem: MemberLevel = {
      id: String(mockMemberLevels.length + 1),
      name: data.name || '',
      '标识': data['标识'] || '',
      memberCount: 0,
      totalConsumption: 0,
      pointsMultiplier: data.pointsMultiplier || 1,
      discount: data.discount || 9.5,
      benefits: data.benefits || '',
      status: 'enabled'
    }
    mockMemberLevels.push(newItem)
    return newItem
  },

  async update(id: string, data: Partial<MemberLevel>) {
    await delay(500)
    const index = mockMemberLevels.findIndex(item => item.id === id)
    if (index !== -1) {
      mockMemberLevels[index] = { ...mockMemberLevels[index], ...data }
      return mockMemberLevels[index]
    }
    return null
  }
}
