import request from '@/utils/request'

export const financeApi = {
  getList: (params: any): Promise<any> => request.get('/transactions', { params }),
  getStats: (): Promise<any> => request.get('/transactions/stats'),
  getDetail: (id: string): Promise<any> => request.get(`/transactions/${id}`),
  create: (data: any): Promise<any> => request.post('/transactions', data),
  update: (id: string, data: any): Promise<any> => request.put(`/transactions/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/transactions/${id}`),
}