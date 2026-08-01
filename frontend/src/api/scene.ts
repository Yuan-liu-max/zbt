import request from '@/utils/request'

export const sceneApi = {
  getList: (params: any): Promise<any> => request.get('/scenes/health-inspections', { params }),
  create: (data: any): Promise<any> => request.post('/scenes/health-inspections', data),
  update: (id: string, data: any): Promise<any> => request.put(`/scenes/health-inspections/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/scenes/health-inspections/${id}`),
}

export const displayApi = {
  getList: (params: any): Promise<any> => request.get('/scenes/display-inspections', { params }),
  create: (data: any): Promise<any> => request.post('/scenes/display-inspections', data),
  update: (id: string, data: any): Promise<any> => request.put(`/scenes/display-inspections/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/scenes/display-inspections/${id}`),
}

export const materialApi = {
  getList: (params: any): Promise<any> => request.get('/scenes/material-updates', { params }),
  create: (data: any): Promise<any> => request.post('/scenes/material-updates', data),
  update: (id: string, data: any): Promise<any> => request.put(`/scenes/material-updates/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/scenes/material-updates/${id}`),
}

export const deviceApi = {
  getList: (params: any): Promise<any> => request.get('/scenes/equipment-checks', { params }),
  create: (data: any): Promise<any> => request.post('/scenes/equipment-checks', data),
  update: (id: string, data: any): Promise<any> => request.put(`/scenes/equipment-checks/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/scenes/equipment-checks/${id}`),
}

export { statusMap } from '@/api/mock/scenario'