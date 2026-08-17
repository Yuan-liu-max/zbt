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

// 晨夕会（对应 employee_meeting 表）
export interface MeetingItem {
  id: number
  topic: string
  meetingType: MeetingType
  meetingDate: string
  host: string
  participants: string
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
  employeeId: number
  assessorId: number
  employeeName?: string
  assessorName?: string
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

// 绩效复盘（对应 employee_monthly_review 表）
export interface PerformanceItem {
  id: number
  employeeId: number
  reviewerId: number
  reviewMonth: string
  totalSalesAmount: number
  salesOrderCount: number
  avgOrderAmount: number
  newCustomerSales: number
  oldCustomerRepurchaseSales: number
  keyCategorySales?: string
  serviceScore: number
  taskExecutionScore: number
  rewardAmount: number
  penaltyAmount: number
  managerReview: string
  createdAt: string
}

// 通用查询参数
export interface HumanQueryParams {
  keyword?: string
  startDate?: string
  endDate?: string
  type?: string
  status?: string
  meetingType?: string
  assessmentWeek?: string
  assessorId?: number
  page: number
  pageSize: number
}
