import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  MeetingItem,
  InterviewItem,
  AssessItem,
  PerformanceItem,
  HumanQueryParams,
} from '@/types/human'

export const meetingApi = {
  getList: (params: HumanQueryParams): Promise<PageResult<MeetingItem>> => request.get('/human/meetings', { params }),
  create: (data: Partial<MeetingItem>): Promise<MeetingItem> => request.post('/human/meetings', data),
}

export const interviewApi = {
  getList: (params: HumanQueryParams): Promise<PageResult<InterviewItem>> => request.get('/human/interviews', { params }),
  create: (data: Partial<InterviewItem>): Promise<InterviewItem> => request.post('/human/interviews', data),
  update: (id: number, data: Partial<InterviewItem>): Promise<InterviewItem> => request.put(`/human/interviews/${id}`, data),
}

export const assessApi = {
  getList: (params: HumanQueryParams): Promise<PageResult<AssessItem>> => request.get('/human/assessments', { params }),
  create: (data: Partial<AssessItem>): Promise<AssessItem> => request.post('/human/assessments', data),
  update: (id: string, data: Partial<AssessItem>): Promise<AssessItem> => request.put(`/human/assessments/${id}`, data),
}

export const performanceApi = {
  getList: (params: HumanQueryParams): Promise<PageResult<PerformanceItem>> => request.get('/human/monthly-reviews', { params }),
  create: (data: Partial<PerformanceItem>): Promise<PerformanceItem> => request.post('/human/monthly-reviews', data),
}

export const meetingStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  cancelled: { text: '已取消', color: 'red' },
  completed: { text: '已完成', color: 'blue' },
}

export const assessStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  completed: { text: '已完成', color: 'blue' },
}

export const performanceStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  completed: { text: '已完成', color: 'default' },
}
