import request from '@/utils/request'

export const storeApi = {
  getList: (params: any): Promise<any> => request.get('/stores', { params }),
  getById: (id: string): Promise<any> => request.get(`/stores/${id}`),
  create: (data: any): Promise<any> => request.post('/stores', data),
  update: (id: string, data: any): Promise<any> => request.put(`/stores/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/stores/${id}`),
}
