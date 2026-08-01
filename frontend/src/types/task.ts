// 任务中心相关类型定义

// 任务维度
export type TaskDimension = 'HUMAN' | 'PRODUCT' | 'SCENE' | 'COMPREHENSIVE'

// 任务状态（12种）
export type TaskStatus = 'PENDING' | 'READY' | 'IN_PROGRESS' | 'SUBMITTED' | 'AUDITING' | 'APPROVED' | 'COMPLETED' | 'REJECTED' | 'RECTIFYING' | 'OVERDUE' | 'CANCELLED' | 'VOIDED'

// 任务优先级
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

// 任务来源
export type TaskSourceType = 'CYCLE' | 'MANUAL' | 'HQ' | 'ABNORMAL' | 'HOLIDAY' | 'AI'

// 审核结果
export type AuditResult = 'APPROVED' | 'REJECTED' | 'RECTIFY'

// 任务实例
export interface TaskItem {
  id: number
  taskNo: string
  templateId: number
  taskTitle: string
  dimension: TaskDimension
  category: string
  storeId: number
  storeName: string
  assigneeId: number
  assigneeName: string
  auditorId: number
  auditorName: string
  startTime: string
  dueTime: string
  completedTime?: string
  status: TaskStatus
  priority: TaskPriority
  sourceType: TaskSourceType
  isOverdue: boolean
  overdueMinutes: number
  qualityScore: number
  aiScore: number
  manualScore: number
  finalScore: number
  createdAt: string
}

// 任务提交
export interface TaskSubmission {
  submissionId: number
  taskId: number
  textContent: string
  formData: Record<string, any>
  photoUrls: string[]
  attachmentUrls: string[]
  location?: { lat: number; lng: number }
  submittedAt: string
}

// 任务审核
export interface TaskAudit {
  auditId: number
  auditorName: string
  auditResult: AuditResult
  auditComment: string
  score: number
  auditedAt: string
}

// 任务模板
export interface TaskTemplate {
  id: number
  name: string
  type: string
  creator: string
  createdAt: string
  updatedAt: string
}

// 任务审查
export interface TaskReviewItem {
  id: number
  name: string
  type: string
  initiator: string
  initiateTime: string
  currentNode: string
  status: string
}

// 查询参数
export interface TaskQueryParams {
  keyword?: string
  dimension?: TaskDimension
  status?: TaskStatus
  priority?: TaskPriority
  sourceType?: TaskSourceType
  startDate?: string
  endDate?: string
  page: number
  size: number
}

export interface TemplateQueryParams {
  name?: string
  type?: string
  page: number
  size: number
}

export interface ReviewQueryParams {
  name?: string
  type?: string
  initiator?: string
  page: number
  size: number
}
