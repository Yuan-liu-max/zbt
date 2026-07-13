// 人效管理 Mock 数据
import type { MeetingItem, InterviewItem, AssessItem, PerformanceItem, HumanQueryParams } from '@/types/human'

// 晨夕会数据
export const mockMeetings: MeetingItem[] = [
  { id: '1', topic: '销售部晨会', type: 'regular', meetingDate: '2026-06-20 09:00', host: '张三', participants: 12, status: 'ended', createdAt: '2026-06-20' },
  { id: '2', topic: '产品部晨会', type: 'regular', meetingDate: '2026-06-20 09:30', host: '李四', participants: 8, status: 'ended', createdAt: '2026-06-20' },
  { id: '3', topic: '运营部夕会', type: 'regular', meetingDate: '2026-06-19 18:00', host: '王五', participants: 10, status: 'ended', createdAt: '2026-06-19' },
  { id: '4', topic: '项目组晨会', type: 'temporary', meetingDate: '2026-06-19 09:15', host: '赵六', participants: 6, status: 'cancelled', createdAt: '2026-06-19' },
  { id: '5', topic: '市场部夕会', type: 'regular', meetingDate: '2026-06-18 18:30', host: '孙七', participants: 9, status: 'ended', createdAt: '2026-06-18' },
]

// 员工面谈数据
export const mockInterviews: InterviewItem[] = [
  { id: '1', topic: '员工发展沟通', type: 'performance', interviewer: '张三', interviewee: '李四', interviewDate: '2026-06-20 10:00', duration: '45分钟', status: 'completed', createdAt: '2026-06-20' },
  { id: '2', topic: '工作问题反馈', type: 'problem', interviewer: '王五', interviewee: '赵六', interviewDate: '2026-06-19 15:00', duration: '30分钟', status: 'completed', createdAt: '2026-06-19' },
  { id: '3', topic: '职业发展规划', type: 'development', interviewer: '李四', interviewee: '孙七', interviewDate: '2026-06-18 14:30', duration: '60分钟', status: 'completed', createdAt: '2026-06-18' },
  { id: '4', topic: '新人入职沟通', type: 'onboarding', interviewer: '张三', interviewee: '周八', interviewDate: '2026-06-17 11:00', duration: '30分钟', status: 'completed', createdAt: '2026-06-17' },
  { id: '5', topic: '转正面谈', type: 'transfer', interviewer: '王五', interviewee: '吴九', interviewDate: '2026-06-16 16:00', duration: '40分钟', status: 'completed', createdAt: '2026-06-16' },
]

// 能力考核数据
export const mockAssesses: AssessItem[] = [
  { id: '1', name: '2026年Q2能力考核', period: '2026-04-01 ~ 2026-06-30', type: 'quarterly', assessor: '张三', participants: 32, status: 'ongoing', createdAt: '2026-04-01' },
  { id: '2', name: '销售岗位能力考核', period: '2026-06-01 ~ 2026-06-30', type: 'monthly', assessor: '李四', participants: 18, status: 'ongoing', createdAt: '2026-06-01' },
  { id: '3', name: '管理能力专项考核', period: '2026-05-01 ~ 2026-05-31', type: 'special', assessor: '王五', participants: 12, status: 'ended', createdAt: '2026-05-01' },
  { id: '4', name: '2026年Q1能力考核', period: '2026-01-01 ~ 2026-03-31', type: 'quarterly', assessor: '张三', participants: 30, status: 'ended', createdAt: '2026-01-01' },
  { id: '5', name: '产品岗位能力考核', period: '2026-04-01 ~ 2026-04-30', type: 'monthly', assessor: '赵六', participants: 15, status: 'ended', createdAt: '2026-04-01' },
]

// 绩效复盘数据
export const mockPerformances: PerformanceItem[] = [
  { id: '1', topic: '2026年Q2绩效复盘会', type: 'quarterly', period: '2026-04-01 ~ 2026-06-30', assignee: '张三', participants: 12, status: 'ongoing', createdAt: '2026-04-01' },
  { id: '2', topic: '销售团队月度复盘', type: 'monthly', period: '2026-06-01 ~ 2026-06-30', assignee: '李四', participants: 8, status: 'ongoing', createdAt: '2026-06-01' },
  { id: '3', topic: '项目交付复盘', type: 'project', period: '2026-05-01 ~ 2026-05-31', assignee: '王五', participants: 6, status: 'completed', createdAt: '2026-05-01' },
  { id: '4', topic: '市场活动复盘', type: 'activity', period: '2026-04-10 ~ 2026-04-20', assignee: '赵六', participants: 7, status: 'completed', createdAt: '2026-04-10' },
  { id: '5', topic: '产品迭代复盘', type: 'iteration', period: '2026-03-01 ~ 2026-03-31', assignee: '孙七', participants: 5, status: 'completed', createdAt: '2026-03-01' },
]

// 状态映射
export const meetingStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
  cancelled: { text: '已取消', color: 'red' },
}

export const assessStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  ended: { text: '已结束', color: 'default' },
}

export const performanceStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  completed: { text: '已完成', color: 'default' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 晨夕会 API
export const meetingApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockMeetings]
    if (params.keyword) filtered = filtered.filter(item => item.topic.includes(params.keyword!))
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async create(data: Partial<MeetingItem>) {
    await delay(500)
    const newItem: MeetingItem = {
      id: String(mockMeetings.length + 1), topic: data.topic || '', type: data.type || 'regular',
      meetingDate: data.meetingDate || '', host: data.host || '', participants: data.participants || 0,
      status: 'ongoing', createdAt: new Date().toISOString().slice(0, 10)
    }
    mockMeetings.push(newItem)
    return newItem
  }
}

// 员工面谈 API
export const interviewApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockInterviews]
    if (params.keyword) filtered = filtered.filter(item => item.topic.includes(params.keyword!))
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async create(data: Partial<InterviewItem>) {
    await delay(500)
    const newItem: InterviewItem = {
      id: String(mockInterviews.length + 1), topic: data.topic || '', type: data.type || 'performance',
      interviewer: data.interviewer || '', interviewee: data.interviewee || '',
      interviewDate: data.interviewDate || '', duration: data.duration || '', status: 'completed',
      createdAt: new Date().toISOString().slice(0, 10)
    }
    mockInterviews.push(newItem)
    return newItem
  }
}

// 能力考核 API
export const assessApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockAssesses]
    if (params.keyword) filtered = filtered.filter(item => item.name.includes(params.keyword!))
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async create(data: Partial<AssessItem>) {
    await delay(500)
    const newItem: AssessItem = {
      id: String(mockAssesses.length + 1), name: data.name || '', period: data.period || '',
      type: data.type || 'monthly', assessor: data.assessor || '', participants: data.participants || 0,
      status: 'ongoing', createdAt: new Date().toISOString().slice(0, 10)
    }
    mockAssesses.push(newItem)
    return newItem
  }
}

// 绩效复盘 API
export const performanceApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockPerformances]
    if (params.keyword) filtered = filtered.filter(item => item.topic.includes(params.keyword!))
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async create(data: Partial<PerformanceItem>) {
    await delay(500)
    const newItem: PerformanceItem = {
      id: String(mockPerformances.length + 1), topic: data.topic || '', type: data.type || 'monthly',
      period: data.period || '', assignee: data.assignee || '', participants: data.participants || 0,
      status: 'ongoing', createdAt: new Date().toISOString().slice(0, 10)
    }
    mockPerformances.push(newItem)
    return newItem
  }
}
