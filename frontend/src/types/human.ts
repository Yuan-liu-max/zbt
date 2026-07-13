// 人效管理相关类型定义

// 会议状态
export type MeetingStatus = 'ongoing' | 'ended' | 'cancelled'

// 会议类型
export type MeetingType = 'regular' | 'temporary'

// 面谈状态
export type InterviewStatus = 'completed'

// 面谈类型
export type InterviewType = 'performance' | 'problem' | 'development' | 'onboarding' | 'transfer'

// 考核状态
export type AssessStatus = 'ongoing' | 'ended'

// 考核类型
export type AssessType = 'monthly' | 'quarterly' | 'special'

// 复盘状态
export type ReviewStatus = 'ongoing' | 'completed'

// 复盘类型
export type ReviewType = 'quarterly' | 'monthly' | 'project' | 'activity' | 'iteration'

// 晨夕会
export interface MeetingItem {
  id: string
  topic: string              // 会议主题
  type: MeetingType          // 会议类型
  meetingDate: string        // 会议日期
  host: string               // 主持人
  participants: number       // 参与人数
  status: MeetingStatus      // 状态
  createdAt: string
}

// 员工面谈
export interface InterviewItem {
  id: string
  topic: string              // 面谈主题
  type: InterviewType        // 面谈类型
  interviewer: string        // 面谈人
  interviewee: string        // 被面谈人
  interviewDate: string      // 面谈日期
  duration: string           // 时长
  status: InterviewStatus    // 状态
  createdAt: string
}

// 能力考核
export interface AssessItem {
  id: string
  name: string               // 考核名称
  period: string             // 考核周期
  type: AssessType           // 考核类型
  assessor: string           // 考核人
  participants: number       // 参与人数
  status: AssessStatus       // 状态
  createdAt: string
}

// 绩效复盘
export interface PerformanceItem {
  id: string
  topic: string              // 复盘主题
  type: ReviewType           // 复盘类型
  period: string             // 复盘周期
  assignee: string           // 负责人
  participants: number       // 参与人数
  status: ReviewStatus       // 状态
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
