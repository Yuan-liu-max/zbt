import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { CertificateItem, CertificateQueryParams } from '@/types/certificate'

export const certificateApi = {
  getList: (params: CertificateQueryParams): Promise<PageResult<CertificateItem>> => request.get('/certificates', { params }),
  getDetail: (id: string): Promise<CertificateItem> => request.get(`/certificates/${id}`),
  create: (data: Partial<CertificateItem>): Promise<CertificateItem> => request.post('/certificates', data),
  update: (id: string, data: Partial<CertificateItem>): Promise<CertificateItem> => request.put(`/certificates/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/certificates/${id}`),
}

export const certificateTypeMap: Record<string, string> = {
  gia: 'GIA证书',
  ngtc: 'NGTC证书',
  gic: 'GIC证书',
  other: '其他证书'
}

export const certificateStatusMap: Record<string, { text: string; color: string }> = {
  valid: { text: '有效', color: 'green' },
  expiring: { text: '即将过期', color: 'orange' },
  expired: { text: '已过期', color: 'red' },
}
