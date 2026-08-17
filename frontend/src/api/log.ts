import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { LogItem, LogQueryParams } from '@/types/log'

export const logApi = {
  getList: (params: LogQueryParams): Promise<PageResult<LogItem>> => request.get('/reports/operate-logs', { params }),
  deleteBatch: (ids: string[]): Promise<void> => request.delete('/reports/operate-logs', { data: { ids } }),
}
