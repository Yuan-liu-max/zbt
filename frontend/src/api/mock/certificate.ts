// 证书管理 Mock 数据
import type { CertificateItem, CertificateQueryParams, CertificateType, CertificateStatus } from '@/types/certificate'

// 证书数据
export const mockCertificates: CertificateItem[] = [
  { id: '1', code: 'CERT202405210001', type: 'gia', productName: '18K金钻石戒指', issuer: 'GIA美国宝石学院', issueDate: '2024-05-21', expiryDate: '2027-05-20', status: 'valid', createdAt: '2024-05-21' },
  { id: '2', code: 'CERT202405200032', type: 'ngtc', productName: '翡翠手镯（冰种）', issuer: '国家珠宝玉石质量监督检验中心', issueDate: '2024-05-20', expiryDate: '2026-05-19', status: 'valid', createdAt: '2024-05-20' },
  { id: '3', code: 'CERT202405190018', type: 'gic', productName: '红宝石吊坠', issuer: '中国地质大学珠宝学院检测中心', issueDate: '2024-05-19', expiryDate: '2026-05-18', status: 'expiring', createdAt: '2024-05-19' },
  { id: '4', code: 'CERT202405180045', type: 'ngtc', productName: '和田玉挂件', issuer: '国家珠宝玉石质量监督检验中心', issueDate: '2024-05-18', expiryDate: '2025-05-17', status: 'expired', createdAt: '2024-05-18' },
  { id: '5', code: 'CERT202405170027', type: 'gia', productName: '钻石耳钉', issuer: 'GIA美国宝石学院', issueDate: '2024-05-17', expiryDate: '2027-05-16', status: 'valid', createdAt: '2024-05-17' },
]

// 证书类型映射
export const certificateTypeMap: Record<CertificateType, string> = {
  gia: 'GIA证书',
  ngtc: 'NGTC证书',
  gic: 'GIC证书',
  other: '其他证书'
}

// 证书状态映射
export const certificateStatusMap: Record<CertificateStatus, { text: string; color: string }> = {
  valid: { text: '有效', color: 'green' },
  expiring: { text: '即将过期', color: 'orange' },
  expired: { text: '已过期', color: 'red' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 证书 API
export const certificateApi = {
  async getList(params: CertificateQueryParams) {
    await delay(300)
    let filtered = [...mockCertificates]

    if (params.code) {
      filtered = filtered.filter(item => item.code.includes(params.code!))
    }
    if (params.type) {
      filtered = filtered.filter(item => item.type === params.type)
    }
    if (params.status) {
      filtered = filtered.filter(item => item.status === params.status)
    }
    if (params.issuer) {
      filtered = filtered.filter(item => item.issuer.includes(params.issuer!))
    }
    if (params.startDate) {
      filtered = filtered.filter(item => item.issueDate >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.issueDate <= params.endDate!)
    }

    const start = (params.page - 1) * params.size
    const end = start + params.size

    return {
      list: filtered.slice(start, end),
      total: filtered.length,
      page: params.page,
      size: params.size
    }
  },

  async getById(id: string) {
    await delay(200)
    return mockCertificates.find(item => item.id === id) || null
  },

  async create(data: Partial<CertificateItem>) {
    await delay(500)
    const newItem: CertificateItem = {
      id: String(mockCertificates.length + 1),
      code: `CERT${Date.now()}`,
      type: data.type || 'gia',
      productName: data.productName || '',
      issuer: data.issuer || '',
      issueDate: data.issueDate || '',
      expiryDate: data.expiryDate || '',
      status: 'valid',
      createdAt: new Date().toISOString().slice(0, 10)
    }
    mockCertificates.push(newItem)
    return newItem
  },

  async update(id: string, data: Partial<CertificateItem>) {
    await delay(500)
    const index = mockCertificates.findIndex(item => item.id === id)
    if (index !== -1) {
      mockCertificates[index] = { ...mockCertificates[index], ...data }
      return mockCertificates[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockCertificates.findIndex(item => item.id === id)
    if (index !== -1) {
      mockCertificates.splice(index, 1)
      return true
    }
    return false
  }
}
