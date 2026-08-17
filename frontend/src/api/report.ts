import request from '@/utils/request'
import type { RankingItem } from '@/types/report'

export const reportApi = {
  getDashboard: (): Promise<any> => request.get('/reports/dashboard'),
  getScores: (params: { page?: number; pageSize?: number; month?: string }): Promise<any> => request.get('/reports/scores', { params }),
  getRanking: (type: string, month?: string): Promise<RankingItem[]> => request.get('/reports/ranking', { params: { type, month } }),
  getTaskCompletion: (month?: string): Promise<any> => request.get('/reports/task-completion', { params: { month } }),
}