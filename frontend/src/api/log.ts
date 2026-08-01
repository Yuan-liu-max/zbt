import request from '@/utils/request'

export const logApi = {
  getList: (params: any): Promise<any> => request.get('/reports/operate-logs', { params }),
}

export { resultMap } from '@/api/mock/log'