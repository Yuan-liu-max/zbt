// 任务中心相关类型定义

// 任务状态
export type TaskStatus = 'pending' | 'in_progress' | 'completed' | 'overdue' | 'cancelled'

// 任务类型
export type TaskType = 'review' | 'approval' | 'process' | 'general'

// 优先级
export type TaskPriority = 'high' | 'medium' | 'low'

// 审查状态
export type ReviewStatus = 'pending' | 'approved' | 'rejected'

// 任务信息
export interface TaskItem {
  id: string
  name: string              // 任务名称
  type: TaskType            // 任务类型
  priority: TaskPriority    // 优先级
  assignee: string          // 负责人
  participants: string[]    // 参与人
  startTime: string         // 开始时间
  endTime: string           // 截止时间
  status: TaskStatus        // 状态
  progress: number          // 进度
  description?: string      // 描述
  attachments?: string[]    // 附件
  createdAt: string
}

// 任务模板
export interface TaskTemplate {
  id: string
  name: string              // 模板名称
  type: TaskType            // 模板类型
  creator: string           // 创建人
  createdAt: string         // 创建时间
  updatedAt: string         // 更新时间
}

// 任务审查
export interface TaskReviewItem {
  id: string
  name: string              // 任务名称
  type: TaskType            // 任务类型
  initiator: string         // 发起人
  initiateTime: string      // 发起时间
  currentNode: string       // 当前节点
  status: ReviewStatus      // 状态
}

// 任务查询参数
export interface TaskQueryParams {
  name?: string
  type?: TaskType
  assignee?: string
  status?: TaskStatus
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}

// 模板查询参数
export interface TemplateQueryParams {
  name?: string
  type?: TaskType
  page: number
  pageSize: number
}

// 审查查询参数
export interface ReviewQueryParams {
  name?: string
  type?: TaskType
  initiator?: string
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}
