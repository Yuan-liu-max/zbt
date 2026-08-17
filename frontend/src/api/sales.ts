import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { SalesRecord, SalesItem, SalesStats, SalesQueryParams, EmployeeRanking, CategoryStats } from '@/types/sales'

export const salesApi = {
  getList: (params: SalesQueryParams): Promise<PageResult<SalesRecord>> => request.get('/sales', { params }),
  getDetail: (id: string): Promise<SalesRecord> => request.get(`/sales/${id}`),
  getItems: (id: string): Promise<SalesItem[]> => request.get(`/sales/${id}/items`),
  getStats: (): Promise<SalesStats> => request.get('/sales/stats'),
  create: (data: Partial<SalesRecord>): Promise<SalesRecord> => request.post('/sales', data),
  audit: (id: string, status: string, comment: string): Promise<void> => request.put(`/sales/${id}/audit`, null, { params: { auditStatus: status, comment } }),
  employeeMetrics: (employeeId: string, month: string): Promise<Record<string, number>> =>
    request.get(`/sales/metrics/employee/${employeeId}`, { params: { month } }),
  storeMetrics: (storeId: string, month: string): Promise<Record<string, number>> =>
    request.get(`/sales/metrics/store/${storeId}`, { params: { month } }),
  employeeRanking: (month: string, topN: number): Promise<EmployeeRanking[]> => request.get('/sales/ranking/employees', { params: { month, topN } }),
  storeRanking: (month: string): Promise<EmployeeRanking[]> => request.get('/sales/ranking/stores', { params: { month } }),
  categoryStructure: (month: string, storeId?: number): Promise<CategoryStats[]> =>
    request.get('/sales/category-structure', { params: { month, storeId } }),
  // 别名（兼容 mock 命名）
  getEmployeeMetrics: (employeeId: string, month: string): Promise<Record<string, number>> =>
    request.get(`/sales/metrics/employee/${employeeId}`, { params: { month } }),
  getStoreMetrics: (storeId: string, month: string): Promise<Record<string, number>> =>
    request.get(`/sales/metrics/store/${storeId}`, { params: { month } }),
  getEmployeeRanking: (month: string, topN: number): Promise<EmployeeRanking[]> =>
    request.get('/sales/ranking/employees', { params: { month, topN } }),
  getStoreRanking: (month: string): Promise<EmployeeRanking[]> => request.get('/sales/ranking/stores', { params: { month } }),
  getCategoryStructure: (month: string, storeId?: number): Promise<CategoryStats[]> =>
    request.get('/sales/category-structure', { params: { month, storeId } }),
  getCategoryStats: (month: string, storeId?: number): Promise<CategoryStats[]> =>
    request.get('/sales/category-structure', { params: { month, storeId } }),
}