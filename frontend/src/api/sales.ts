import request from '@/utils/request'

export const salesApi = {
  getList: (params: any): Promise<any> => request.get('/sales', { params }),
  getDetail: (id: string): Promise<any> => request.get(`/sales/${id}`),
  getItems: (id: string): Promise<any> => request.get(`/sales/${id}/items`),
  getStats: (): Promise<any> => request.get('/sales/stats'),
  create: (data: any): Promise<any> => request.post('/sales', data),
  audit: (id: string, status: string, comment: string): Promise<any> => request.put(`/sales/${id}/audit`, null, { params: { auditStatus: status, comment } }),
  employeeMetrics: (employeeId: string, month: string): Promise<any> => request.get(`/sales/metrics/employee/${employeeId}`, { params: { month } }),
  storeMetrics: (storeId: string, month: string): Promise<any> => request.get(`/sales/metrics/store/${storeId}`, { params: { month } }),
  employeeRanking: (month: string, topN: number): Promise<any> => request.get('/sales/ranking/employees', { params: { month, topN } }),
  storeRanking: (month: string): Promise<any> => request.get('/sales/ranking/stores', { params: { month } }),
  categoryStructure: (month: string, storeId?: string): Promise<any> => request.get('/sales/category-structure', { params: { month, storeId } }),
  // 别名（兼容 mock 命名）
  getEmployeeMetrics: (employeeId: string, month: string) => request.get(`/sales/metrics/employee/${employeeId}`, { params: { month } }),
  getStoreMetrics: (storeId: string, month: string) => request.get(`/sales/metrics/store/${storeId}`, { params: { month } }),
  getEmployeeRanking: (month: string, topN: number) => request.get('/sales/ranking/employees', { params: { month, topN } }),
  getStoreRanking: (month: string) => request.get('/sales/ranking/stores', { params: { month } }),
  getCategoryStructure: (month: string, storeId?: string) => request.get('/sales/category-structure', { params: { month, storeId } }),
}