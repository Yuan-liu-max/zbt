import request from '@/utils/request'

export const customerApi = {
  getList: (params: any): Promise<any> => request.get('/customers', { params }),
  getDetail: (id: string): Promise<any> => request.get(`/customers/${id}`),
  create: (data: any): Promise<any> => request.post('/customers', data),
  update: (id: string, data: any): Promise<any> => request.put(`/customers/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/customers/${id}`),
}

export const memberApi = {
  getList: (params: any): Promise<any> => request.get('/member-levels', { params }),
  getStats: (): Promise<any> => request.get('/member-levels/stats'),
  create: (data: any): Promise<any> => request.post('/member-levels', data),
  update: (id: string, data: any): Promise<any> => request.put(`/member-levels/${id}`, data),
}

export { levelColorMap, levelTextMap } from '@/api/mock/customer'