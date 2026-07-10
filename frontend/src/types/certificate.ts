// 证书管理相关类型定义

// 证书类型
export type CertificateType = 'gia' | 'ngtc' | 'gic' | 'other'

// 证书状态
export type CertificateStatus = 'valid' | 'expiring' | 'expired'

// 证书信息
export interface CertificateItem {
  id: string
  code: string              // 证书编号
  type: CertificateType     // 证书类型
  productName: string       // 关联商品
  issuer: string            // 签发机构
  issueDate: string         // 签发日期
  expiryDate: string        // 有效期至
  status: CertificateStatus // 状态
  fileUrl?: string          // 文件地址
  remark?: string           // 备注
  createdAt: string
}

// 证书查询参数
export interface CertificateQueryParams {
  code?: string             // 证书编号
  type?: CertificateType    // 证书类型
  status?: CertificateStatus // 状态
  issuer?: string           // 签发机构
  startDate?: string        // 开始日期
  endDate?: string          // 结束日期
  page: number
  pageSize: number
}
