// 人效管理相关类型定义

// 晨夕会类型
export type MeetingType = 'MORNING' | 'EVENING'

// 面谈类型
export type InterviewType = 'PERFORMANCE' | 'PROBLEM' | 'DEVELOPMENT' | 'ONBOARDING' | 'TRANSFER'

// 心态状态
export type MindsetStatus = 'POSITIVE' | 'NORMAL' | 'LOW' | 'ABNORMAL'

// 考核类型
export type AssessType = 'monthly' | 'quarterly' | 'special'

// 复盘类型
export type ReviewType = 'quarterly' | 'monthly' | 'project' | 'activity' | 'iteration'

// 晨夕会
export interface MeetingItem {
  id: number
  meetingType: MeetingType
  meetingDate: string
  host: string
  participants: number
  storeTargetAmount: number
  mainProducts: string
  keyCustomers: string
  todayStrategy: string
  employeeTargets: Record<string, number>
  meetingPhotoUrls: string[]
  actualSalesAmount?: number
  targetCompletionRate?: number
  successfulCases?: string
  failedCases?: string
  tomorrowImprovement?: string
  status: string
  createdAt: string
}

// 员工面谈
export interface InterviewItem {
  id: number
  employeeId: number
  employeeName: string
  interviewerId: number
  interviewerName: string
  interviewDate: string
  interviewType: InterviewType
  currentWeekSales: number
  targetCompletionRate: number
  mainProblem: string
  customerFollowIssue: string
  productKnowledgeGap: string
  mindsetStatus: MindsetStatus
  nextWeekGoal: string
  improvementPlan: string
  managerComment: string
  employeeFeedback: string
  duration: string
  status: string
  createdAt: string
}

// 能力考核
export interface AssessItem {
  id: number
  name: string
  assessmentWeek: string
  type: AssessType
  assessor: string
  participants: number
  productKnowledgeScore: number
  matchingSkillScore: number
  receptionScore: number
  objectionHandlingScore: number
  promotionScriptScore: number
  totalScore: number
  improvementAdvice: string
  status: string
  createdAt: string
}

// 绩效复盘
export interface PerformanceItem {
  id: number
  topic: string
  type: ReviewType
  period: string
  assignee: string
  participants: number
  status: string
  createdAt: string
}

// 通用查询参数
export interface HumanQueryParams {
  keyword?: string
  startDate?: string
  endDate?: string
  type?: string
  status?: string
  page: number
  pageSize: number
}
