// 供应商管理 Mock 数据
import type { SupplierItem, SupplierQueryParams, SupplierType, CooperationStatus } from '@/types/supplier'

// 供应商数据
export const mockSuppliers: SupplierItem[] = [
  { id: '1', name: '深圳市金瑞珠宝有限公司', type: 'raw_material', contactPerson: '张先生', contactPhone: '138****5678', email: 'zhang@jinrui.com', status: 'cooperating', createdAt: '2024-05-20' },
  { id: '2', name: '广州钻石贸易有限公司', type: 'gemstone', contactPerson: '李女士', contactPhone: '139****8765', email: 'li@diamond.com', status: 'cooperating', createdAt: '2024-05-18' },
  { id: '3', name: '香港珍珠批发商行', type: 'pearl', contactPerson: '王先生', contactPhone: '137****4321', email: 'wang@pearl.com', status: 'cooperating', createdAt: '2024-05-15' },
  { id: '4', name: '佛山精工首饰加工厂', type: 'processing', contactPerson: '陈先生', contactPhone: '136****2468', email: 'chen@jgc.com', status: 'cooperating', createdAt: '2024-05-10' },
  { id: '5', name: '上海包装制品有限公司', type: 'packaging', contactPerson: '刘女士', contactPhone: '135****1357', email: 'liu@package.com', status: 'suspended', createdAt: '2024-04-28' },
  { id: '6', name: '北京标签印刷厂', type: 'consumable', contactPerson: '赵先生', contactPhone: '134****9753', email: 'zhao@label.com', status: 'terminated', createdAt: '2024-04-20' },
]

// 供应商类型映射
export const supplierTypeMap: Record<SupplierType, { text: string; color: string }> = {
  raw_material: { text: '原材料供应商', color: 'blue' },
  gemstone: { text: '宝石供应商', color: 'purple' },
  pearl: { text: '珍珠供应商', color: 'gold' },
  processing: { text: '加工服务商', color: 'orange' },
  packaging: { text: '包装供应商', color: 'cyan' },
  consumable: { text: '耗材供应商', color: 'red' },
}

// 合作状态映射
export const cooperationStatusMap: Record<CooperationStatus, { text: string; color: string }> = {
  cooperating: { text: '合作中', color: 'green' },
  suspended: { text: '已暂停', color: 'orange' },
  terminated: { text: '已终止', color: 'red' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 供应商 API
export const supplierApi = {
  async getList(params: SupplierQueryParams) {
    await delay(300)
    let filtered = [...mockSuppliers]

    if (params.name) {
      filtered = filtered.filter(item => item.name.includes(params.name!))
    }
    if (params.contactPerson) {
      filtered = filtered.filter(item => item.contactPerson.includes(params.contactPerson!))
    }
    if (params.type) {
      filtered = filtered.filter(item => item.type === params.type)
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

  async getById(id: string) {
    await delay(200)
    return mockSuppliers.find(item => item.id === id) || null
  },

  async create(data: Partial<SupplierItem>) {
    await delay(500)
    const newItem: SupplierItem = {
      id: String(mockSuppliers.length + 1),
      name: data.name || '',
      type: data.type || 'raw_material',
      contactPerson: data.contactPerson || '',
      contactPhone: data.contactPhone || '',
      email: data.email || '',
      status: 'cooperating',
      createdAt: new Date().toISOString().slice(0, 10)
    }
    mockSuppliers.push(newItem)
    return newItem
  },

  async update(id: string, data: Partial<SupplierItem>) {
    await delay(500)
    const index = mockSuppliers.findIndex(item => item.id === id)
    if (index !== -1) {
      mockSuppliers[index] = { ...mockSuppliers[index], ...data }
      return mockSuppliers[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockSuppliers.findIndex(item => item.id === id)
    if (index !== -1) {
      mockSuppliers.splice(index, 1)
      return true
    }
    return false
  }
}
