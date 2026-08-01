import request from '@/utils/request'

export const certificateApi = {
  getList: (params: any): Promise<any> => request.get('/certificates', { params }),
  getDetail: (id: string): Promise<any> => request.get(`/certificates/${id}`),
  create: (data: any): Promise<any> => request.post('/certificates', data),
  update: (id: string, data: any): Promise<any> => request.put(`/certificates/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/certificates/${id}`),
}

export { certificateTypeMap, certificateStatusMap } from '@/api/mock/certificate'