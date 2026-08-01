import request from '@/utils/request'

export const meetingApi = {
  getList: (params: any): Promise<any> => request.get('/human/meetings', { params }),
  create: (data: any): Promise<any> => request.post('/human/meetings', data),
}

export const interviewApi = {
  getList: (params: any): Promise<any> => request.get('/human/interviews', { params }),
  create: (data: any): Promise<any> => request.post('/human/interviews', data),
}

export const assessApi = {
  getList: (params: any): Promise<any> => request.get('/human/assessments', { params }),
  create: (data: any): Promise<any> => request.post('/human/assessments', data),
  update: (id: string, data: any): Promise<any> => request.put(`/human/assessments/${id}`, data),
}

export const performanceApi = {
  getList: (params: any): Promise<any> => request.get('/human/monthly-reviews', { params }),
  create: (data: any): Promise<any> => request.post('/human/monthly-reviews', data),
}

export { meetingStatusMap, assessStatusMap, performanceStatusMap } from '@/api/mock/human'