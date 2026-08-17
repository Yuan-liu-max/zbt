import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  TaskItem,
  TaskTemplate,
  TaskReviewItem,
  TaskSubmission,
  TaskAudit,
  TaskQueryParams,
  TemplateQueryParams,
  TaskStatus,
  TaskDimension,
  TaskPriority,
  TaskSourceType,
  AuditResult,
} from '@/types/task'

export const taskApi = {
  getList: (params: TaskQueryParams): Promise<PageResult<TaskItem>> => request.get('/tasks', { params }),
  getMyTasks: (status: string): Promise<TaskItem[]> => request.get('/tasks/my', { params: { status } }),
  getMyAudit: (): Promise<TaskReviewItem[]> => request.get('/tasks/my-audit'),
  getDetail: (id: string): Promise<TaskItem> => request.get(`/tasks/${id}`),
  generate: (data: Partial<TaskItem>): Promise<TaskItem> => request.post('/tasks/generate', data),
  submit: (data: Partial<TaskSubmission>): Promise<TaskSubmission> => request.post('/tasks/submit', data),
  audit: (data: Partial<TaskAudit>): Promise<TaskAudit> => request.post('/tasks/audit', data),
  cancel: (id: string): Promise<void> => request.put(`/tasks/${id}/cancel`),
  voidTask: (id: string): Promise<void> => request.put(`/tasks/${id}/void`),
  start: (id: string): Promise<void> => request.put(`/tasks/${id}/start`),
  create: (data: Partial<TaskItem>): Promise<TaskItem> => request.post('/tasks', data),
  update: (id: string, data: Partial<TaskItem>): Promise<TaskItem> => request.put(`/tasks/${id}`, data),
}

export const templateApi = {
  getList: (params: TemplateQueryParams): Promise<PageResult<TaskTemplate>> => request.get('/task-templates', { params }),
  getDetail: (id: string): Promise<TaskTemplate> => request.get(`/task-templates/${id}`),
  create: (data: Partial<TaskTemplate>): Promise<TaskTemplate> => request.post('/task-templates', data),
  update: (id: string, data: Partial<TaskTemplate>): Promise<TaskTemplate> => request.put(`/task-templates/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/task-templates/${id}`),
  toggle: (id: string): Promise<void> => request.put(`/task-templates/${id}/toggle`),
}

export const reviewApi = {
  getList: (): Promise<TaskReviewItem[]> => request.get('/tasks/my-audit'),
}

export const taskStatusMap: Record<TaskStatus, { text: string; color: string }> = {
  PENDING: { text: '待处理', color: 'orange' },
  READY: { text: '待执行', color: 'cyan' },
  IN_PROGRESS: { text: '进行中', color: 'green' },
  SUBMITTED: { text: '已提交', color: 'blue' },
  AUDITING: { text: '审核中', color: 'geekblue' },
  APPROVED: { text: '已通过', color: 'green' },
  COMPLETED: { text: '已完成', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
  RECTIFYING: { text: '整改中', color: 'gold' },
  OVERDUE: { text: '已超时', color: 'volcano' },
  CANCELLED: { text: '已取消', color: 'default' },
  VOIDED: { text: '已作废', color: 'default' },
}

export const taskDimensionMap: Record<TaskDimension, { text: string; color: string }> = {
  HUMAN: { text: '人员维度', color: 'blue' },
  PRODUCT: { text: '商品维度', color: 'green' },
  SCENE: { text: '场景维度', color: 'orange' },
  COMPREHENSIVE: { text: '综合维度', color: 'purple' },
}

export const priorityMap: Record<TaskPriority, { text: string; color: string }> = {
  LOW: { text: '低', color: 'default' },
  MEDIUM: { text: '中', color: 'blue' },
  HIGH: { text: '高', color: 'orange' },
  URGENT: { text: '紧急', color: 'red' },
}

export const sourceTypeMap: Record<TaskSourceType, { text: string; color: string }> = {
  CYCLE: { text: '周期任务', color: 'blue' },
  MANUAL: { text: '手动创建', color: 'green' },
  HQ: { text: '总部下发', color: 'purple' },
  ABNORMAL: { text: '异常触发', color: 'red' },
  HOLIDAY: { text: '节假日', color: 'orange' },
  AI: { text: 'AI派发', color: 'cyan' },
}

export const auditResultMap: Record<AuditResult, { text: string; color: string }> = {
  APPROVED: { text: '通过', color: 'green' },
  REJECTED: { text: '拒绝', color: 'red' },
  RECTIFY: { text: '整改', color: 'gold' },
}