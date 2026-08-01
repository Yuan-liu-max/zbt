import request from '@/utils/request'

export const reportApi = {
  getDashboard: (): Promise<any> => request.get('/reports/dashboard'),
  getScores: (params: any): Promise<any> => request.get('/reports/scores', { params }),
  getRanking: (month: string): Promise<any> => request.get('/reports/ranking', { params: { month } }),
  getTaskCompletion: (month: string): Promise<any> => request.get('/reports/task-completion', { params: { month } }),
}