import request from '@/utils/request'

export const orderApi = {
  getList: (params: any): Promise<any> => request.get('/orders', { params }),
  getDetail: (id: string): Promise<any> => request.get(`/orders/${id}`),
  cancel: (id: string): Promise<any> => request.put(`/orders/${id}/cancel`),
  update: (id: string, data: any): Promise<any> => request.put(`/orders/${id}`, data),
}

export const returnApi = {
  getList: (params: any): Promise<any> => request.get('/orders/returns', { params }),
  getDetail: (id: string): Promise<any> => request.get(`/orders/returns/${id}`),
  cancel: (id: string): Promise<any> => request.put(`/orders/returns/${id}/cancel`),
}

export { orderStatusMap, returnStatusMap } from '@/api/mock/order'