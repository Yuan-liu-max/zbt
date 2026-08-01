import request from '@/utils/request'

export const activityApi = {
  getList: (params: any): Promise<any> => request.get('/activities', { params }),
  create: (data: any): Promise<any> => request.post('/activities', data),
  update: (id: string, data: any): Promise<any> => request.put(`/activities/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/activities/${id}`),
}

export const promotionApi = {
  getList: (params: any): Promise<any> => request.get('/promotions', { params }),
  create: (data: any): Promise<any> => request.post('/promotions', data),
  update: (id: string, data: any): Promise<any> => request.put(`/promotions/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/promotions/${id}`),
}

export { activityStatusMap, activityTypeMap, promotionStatusMap, promotionTypeMap } from '@/api/mock/marketing'