import request from '@/utils/request'

export const orgApi = {
  getTree: (): Promise<any> => request.get('/organizations/tree'),
  getList: (): Promise<any> => request.get('/organizations'),
  create: (data: any): Promise<any> => request.post('/organizations', data),
  update: (id: string, data: any): Promise<any> => request.put(`/organizations/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/organizations/${id}`),
}

export const userApi = {
  getList: (params: any): Promise<any> => request.get('/users', { params }),
  create: (data: any): Promise<any> => request.post('/users', data),
  update: (id: string, data: any): Promise<any> => request.put(`/users/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/users/${id}`),
}

export const roleApi = {
  getList: (): Promise<any> => request.get('/roles'),
  create: (data: any): Promise<any> => request.post('/roles', data),
  update: (id: string, data: any): Promise<any> => request.put(`/roles/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/roles/${id}`),
  assignPermissions: (id: string, permIds: string[]): Promise<any> => request.put(`/roles/${id}/permissions`, { permissionIds: permIds }),
  getPermissions: (id: string): Promise<any> => request.get(`/roles/${id}/permissions`),
}

export const configApi = {
  getList: (group?: string): Promise<any> => request.get('/system/configs', { params: { configGroup: group } }),
  save: (data: any): Promise<any> => request.put('/system/configs', data),
}
