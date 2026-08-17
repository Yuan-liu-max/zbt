import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { FinanceStats, TransactionRecord, TransactionQueryParams } from '@/types/finance'

export const financeApi = {
  getList: (params: TransactionQueryParams): Promise<PageResult<TransactionRecord>> => request.get('/transactions', { params }),
  getTransactions: (params: TransactionQueryParams): Promise<PageResult<TransactionRecord>> => request.get('/transactions', { params }),
  getStats: (): Promise<FinanceStats> => request.get('/transactions/stats'),
  getDetail: (id: string): Promise<TransactionRecord> => request.get(`/transactions/${id}`),
  create: (data: Partial<TransactionRecord>): Promise<TransactionRecord> => request.post('/transactions', data),
  update: (id: string, data: Partial<TransactionRecord>): Promise<TransactionRecord> => request.put(`/transactions/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/transactions/${id}`),
}