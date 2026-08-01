import request from '@/utils/request'

export const taskApi = {
  getList: (params: any): Promise<any> => request.get('/tasks', { params }),
  getMyTasks: (status: string): Promise<any> => request.get('/tasks/my', { params: { status } }),
  getMyAudit: (): Promise<any> => request.get('/tasks/my-audit'),
  getDetail: (id: string): Promise<any> => request.get(`/tasks/${id}`),
  generate: (data: any): Promise<any> => request.post('/tasks/generate', data),
  submit: (data: any): Promise<any> => request.post('/tasks/submit', data),
  audit: (data: any): Promise<any> => request.post('/tasks/audit', data),
  cancel: (id: string): Promise<any> => request.put(`/tasks/${id}/cancel`),
  voidTask: (id: string): Promise<any> => request.put(`/tasks/${id}/void`),
  start: (id: string): Promise<any> => request.put(`/tasks/${id}/start`),
  create: (data: any): Promise<any> => request.post('/tasks/generate', data),
  update: (id: string, data: any): Promise<any> => request.put(`/tasks/${id}`, data),
}

export const templateApi = {
  getList: (params: any): Promise<any> => request.get('/task-templates', { params }),
  create: (data: any): Promise<any> => request.post('/task-templates', data),
  update: (id: string, data: any): Promise<any> => request.put(`/task-templates/${id}`, data),
  delete: (id: string): Promise<any> => request.delete(`/task-templates/${id}`),
  toggle: (id: string): Promise<any> => request.put(`/task-templates/${id}/toggle`),
}

export const reviewApi = {
  getList: (params: any): Promise<any> => request.get('/tasks/my-audit', { params }),
}

export { taskStatusMap, taskDimensionMap, priorityMap, sourceTypeMap, auditResultMap } from '@/api/mock/task'