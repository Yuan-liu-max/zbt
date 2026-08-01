import request from '@/utils/request'

export const supplierApi = {
  getList: (params: any): Promise<any> => request.get('/suppliers', { params }),
  getDetail: (id: string): Promise<any> => request.get(`/suppliers/${id}`),
  create: (data: any): Promise<any> => request.post('/suppliers', data),
  update: (id: string, data: any): Promise<any> => request.put(`/suppliers/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/suppliers/${id}`),
}

export { supplierTypeMap, cooperationStatusMap } from '@/api/mock/supplier'