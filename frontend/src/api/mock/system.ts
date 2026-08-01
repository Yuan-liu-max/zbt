// 系统管理 Mock 数据
import type { OrgNode, StoreItem, UserItem, RoleItem, SystemConfig } from '@/types/system'

// 组织架构
export const mockOrgTree: OrgNode[] = [
  { id: '1', name: '总公司', parentId: null, level: 1, memberCount: 1, children: [
    { id: '2', name: '运营中心', parentId: '1', level: 2, memberCount: 23, children: [
      { id: '5', name: '运营部', parentId: '2', level: 3, memberCount: 8 },
      { id: '6', name: '市场部', parentId: '2', level: 3, memberCount: 6 },
      { id: '7', name: '客服部', parentId: '2', level: 3, memberCount: 9 },
    ]},
    { id: '3', name: '产品中心', parentId: '1', level: 2, memberCount: 18, children: [
      { id: '8', name: '产品部', parentId: '3', level: 3, memberCount: 7 },
      { id: '9', name: '研发部', parentId: '3', level: 3, memberCount: 6 },
      { id: '10', name: '设计部', parentId: '3', level: 3, memberCount: 5 },
    ]},
    { id: '4', name: '技术中心', parentId: '1', level: 2, memberCount: 15, children: [
      { id: '11', name: '技术部', parentId: '4', level: 3, memberCount: 10 },
      { id: '12', name: '测试部', parentId: '4', level: 3, memberCount: 5 },
    ]},
  ]},
]

// 门店数据
export const mockStores: StoreItem[] = [
  { id: '1', name: '北京旗舰店', code: 'M0001', region: '华北区域', address: '北京市朝阳区建国路88号', contactPerson: '张三', contactPhone: '138****1234', status: 'open' },
  { id: '2', name: '上海静安店', code: 'M0002', region: '华东区域', address: '上海市静安区南京西路1266号', contactPerson: '李四', contactPhone: '139****5678', status: 'open' },
  { id: '3', name: '杭州西湖店', code: 'M0003', region: '华东区域', address: '杭州市西湖区天目山路228号', contactPerson: '王五', contactPhone: '137****9012', status: 'suspended' },
  { id: '4', name: '深圳南山店', code: 'M0004', region: '华南区域', address: '深圳市南山区科技路1号', contactPerson: '赵六', contactPhone: '136****3456', status: 'open' },
  { id: '5', name: '成都锦江店', code: 'M0005', region: '西南区域', address: '成都市锦江区春熙路99号', contactPerson: '刘七', contactPhone: '135****7890', status: 'open' },
]

// 用户数据
export const mockUsers: UserItem[] = [
  { id: '1', username: 'admin', phone: '138****1234', department: '运营部', role: '超级管理员', status: 'enabled', createdAt: '2024-05-20 10:30:00' },
  { id: '2', username: 'zhangsan', phone: '139****5678', department: '市场部', role: '市场专员', status: 'enabled', createdAt: '2024-05-19 15:20:00' },
  { id: '3', username: 'lisi', phone: '137****9012', department: '客服部', role: '客服专员', status: 'enabled', createdAt: '2024-05-18 09:15:00' },
  { id: '4', username: 'wangwu', phone: '136****3456', department: '产品部', role: '产品经理', status: 'disabled', createdAt: '2024-05-17 14:45:00' },
  { id: '5', username: 'zhaoliu', phone: '135****7890', department: '技术部', role: '开发工程师', status: 'enabled', createdAt: '2024-05-16 11:30:00' },
]

// 角色数据
export const mockRoles: RoleItem[] = [
  { id: '1', name: '超级管理员', description: '拥有系统最高权限', permissions: [] },
  { id: '2', name: '运营管理员', description: '拥有运营管理权限', permissions: [] },
  { id: '3', name: '市场专员', description: '市场相关操作权限', permissions: [] },
  { id: '4', name: '客服专员', description: '客服相关操作权限', permissions: [] },
  { id: '5', name: '产品经理', description: '产品相关操作权限', permissions: [] },
  { id: '6', name: '开发工程师', description: '开发相关操作权限', permissions: [] },
  { id: '7', name: '财务专员', description: '财务相关操作权限', permissions: [] },
]

// 系统配置
export const mockConfig: SystemConfig = {
  systemName: '企业管理系统',
  language: '简体中文',
  timezone: '(GMT+08:00) 北京、重庆、香港特别行政区、乌鲁木齐',
  dateFormat: 'YYYY-MM-DD',
  timeFormat: 'HH:mm:ss',
  version: 'v2.1.0',
  copyright: '© 2024 企业管理系统. All Rights Reserved.',
  description: '一站式企业管理解决方案',
  icpNumber: '粤ICP备2024001234号',
  homeUrl: '/dashboard',
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 门店 API
export const storeApi = {
  async getList(params: { name?: string; status?: StoreStatus; region?: string; page: number; size: number }) {
    await delay(300)
    let filtered = [...mockStores]
    if (params.name) filtered = filtered.filter(item => item.name.includes(params.name!))
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    if (params.region) filtered = filtered.filter(item => item.region.includes(params.region!))
    const start = (params.page - 1) * params.size
    return { list: filtered.slice(start, start + params.size), total: filtered.length, page: params.page, size: params.size }
  },
  async create(data: Partial<StoreItem>) {
    await delay(500)
    const newItem: StoreItem = {
      id: String(mockStores.length + 1),
      name: data.name || '',
      code: data.code || '',
      region: data.region || '',
      address: data.address || '',
      contactPerson: data.contactPerson || '',
      contactPhone: data.contactPhone || '',
      status: data.status || 'open'
    }
    mockStores.push(newItem)
    return newItem
  },
  async update(id: string, data: Partial<StoreItem>) {
    await delay(500)
    const index = mockStores.findIndex(item => item.id === id)
    if (index !== -1) {
      mockStores[index] = { ...mockStores[index], ...data }
      return mockStores[index]
    }
    return null
  },
  async delete(id: string) {
    await delay(300)
    const index = mockStores.findIndex(item => item.id === id)
    if (index !== -1) {
      mockStores.splice(index, 1)
      return true
    }
    return false
  }
}

// 用户 API
export const userApi = {
  async getList(params: { username?: string; phone?: string; status?: UserStatus; page: number; size: number }) {
    await delay(300)
    let filtered = [...mockUsers]
    if (params.username) filtered = filtered.filter(item => item.username.includes(params.username!))
    if (params.phone) filtered = filtered.filter(item => item.phone.includes(params.phone!))
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    const start = (params.page - 1) * params.size
    return { list: filtered.slice(start, start + params.size), total: filtered.length, page: params.page, size: params.size }
  }
}

// 角色 API
export const roleApi = {
  async getList() { await delay(300); return [...mockRoles] }
}

// 组织架构 API
export const orgApi = {
  async getTree() { await delay(300); return [...mockOrgTree] }
}

// 系统配置 API
export const configApi = {
  async getConfig(): Promise<SystemConfig> { await delay(200); return { ...mockConfig } },
  async saveConfig(data: Partial<SystemConfig>) { await delay(500); Object.assign(mockConfig, data); return { ...mockConfig } }
}
