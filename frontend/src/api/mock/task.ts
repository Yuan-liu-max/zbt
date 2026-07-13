// 任务中心 Mock 数据
import type { TaskItem, TaskTemplate, TaskReviewItem, TaskQueryParams, TemplateQueryParams, ReviewQueryParams, TaskStatus, TaskType, TaskPriority, ReviewStatus } from '@/types/task'

// 任务数据
export const mockTasks: TaskItem[] = [
  { id: '1', name: '供应商资质审核', type: 'review', priority: 'high', assignee: '张三', participants: ['李四', '王五'], startTime: '2024-05-20 10:00', endTime: '2024-05-25 18:00', status: 'in_progress', progress: 60, createdAt: '2024-05-20' },
  { id: '2', name: '采购合同审批', type: 'approval', priority: 'high', assignee: '张三', participants: ['李四', '王五'], startTime: '2024-05-20 10:00', endTime: '2024-05-25 18:00', status: 'overdue', progress: 0, createdAt: '2024-05-20' },
  { id: '3', name: '供应商准入流程', type: 'process', priority: 'high', assignee: '张三', participants: ['李四', '王五'], startTime: '2024-05-20 10:00', endTime: '2024-05-25 18:00', status: 'completed', progress: 100, createdAt: '2024-05-20' },
]

// 模板数据
export const mockTemplates: TaskTemplate[] = [
  { id: '1', name: '供应商资质审核模板', type: 'review', creator: '张三', createdAt: '2024-05-18 10:00', updatedAt: '2024-05-20 15:30' },
  { id: '2', name: '采购合同审批模板', type: 'process', creator: '李四', createdAt: '2024-05-16 09:30', updatedAt: '2024-05-19 11:20' },
  { id: '3', name: '供应商准入流程模板', type: 'process', creator: '王五', createdAt: '2024-05-14 14:20', updatedAt: '2024-05-18 16:40' },
  { id: '4', name: '月度销售目标制定模板', type: 'general', creator: '赵六', createdAt: '2024-05-10 15:00', updatedAt: '2024-05-15 10:10' },
]

// 审查数据
export const mockReviews: TaskReviewItem[] = [
  { id: '1', name: '供应商资质审核', type: 'review', initiator: '张三', initiateTime: '2024-05-20 10:00', currentNode: '部门负责人审核', status: 'pending' },
  { id: '2', name: '采购合同审批', type: 'approval', initiator: '李四', initiateTime: '2024-05-20 09:30', currentNode: '财务审核', status: 'pending' },
  { id: '3', name: '供应商准入申请', type: 'process', initiator: '王五', initiateTime: '2024-05-20 09:00', currentNode: '法务审核', status: 'pending' },
]

// 状态映射
export const taskStatusMap: Record<TaskStatus, { text: string; color: string }> = {
  pending: { text: '待处理', color: 'orange' },
  in_progress: { text: '进行中', color: 'green' },
  completed: { text: '已完成', color: 'blue' },
  overdue: { text: '已过期', color: 'red' },
  cancelled: { text: '已取消', color: 'default' },
}

export const taskTypeMap: Record<TaskType, string> = {
  review: '审核任务',
  approval: '审批任务',
  process: '流程任务',
  general: '通用任务',
}

export const priorityMap: Record<TaskPriority, { text: string; color: string }> = {
  high: { text: '高', color: 'red' },
  medium: { text: '中', color: 'orange' },
  low: { text: '低', color: 'blue' },
}

export const reviewStatusMap: Record<ReviewStatus, { text: string; color: string }> = {
  pending: { text: '待审查', color: 'orange' },
  approved: { text: '已通过', color: 'green' },
  rejected: { text: '已拒绝', color: 'red' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 任务 API
export const taskApi = {
  async getList(params: TaskQueryParams) {
    await delay(300)
    let filtered = [...mockTasks]
    if (params.name) filtered = filtered.filter(item => item.name.includes(params.name!))
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async create(data: Partial<TaskItem>) {
    await delay(500)
    const newItem: TaskItem = {
      id: String(mockTasks.length + 1), name: data.name || '', type: data.type || 'general',
      priority: data.priority || 'medium', assignee: data.assignee || '',
      participants: data.participants || [], startTime: data.startTime || '',
      endTime: data.endTime || '', status: 'pending', progress: 0,
      description: data.description, createdAt: new Date().toISOString().slice(0, 10)
    }
    mockTasks.push(newItem)
    return newItem
  },
  async update(id: string, data: Partial<TaskItem>) {
    await delay(500)
    const index = mockTasks.findIndex(item => item.id === id)
    if (index !== -1) { mockTasks[index] = { ...mockTasks[index], ...data }; return mockTasks[index] }
    return null
  }
}

// 模板 API
export const templateApi = {
  async getList(params: TemplateQueryParams) {
    await delay(300)
    let filtered = [...mockTemplates]
    if (params.name) filtered = filtered.filter(item => item.name.includes(params.name!))
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async create(data: Partial<TaskTemplate>) {
    await delay(500)
    const newItem: TaskTemplate = {
      id: String(mockTemplates.length + 1), name: data.name || '', type: data.type || 'general',
      creator: data.creator || '管理员', createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16)
    }
    mockTemplates.push(newItem)
    return newItem
  },
  async update(id: string, data: Partial<TaskTemplate>) {
    await delay(500)
    const index = mockTemplates.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTemplates[index] = { ...mockTemplates[index], ...data, updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16) }
      return mockTemplates[index]
    }
    return null
  },
  async delete(id: string) {
    await delay(300)
    const index = mockTemplates.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTemplates.splice(index, 1)
      return true
    }
    return false
  }
}

// 审查 API
export const reviewApi = {
  async getList(params: ReviewQueryParams) {
    await delay(300)
    let filtered = [...mockReviews]
    if (params.name) filtered = filtered.filter(item => item.name.includes(params.name!))
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length, page: params.page, pageSize: params.pageSize }
  },
  async submitReview(id: string, data: { approved: boolean; comment: string }) {
    await delay(500)
    const index = mockReviews.findIndex(item => item.id === id)
    if (index !== -1) {
      mockReviews[index].status = data.approved ? 'approved' : 'rejected'
      return mockReviews[index]
    }
    return null
  }
}

// 审查 API（已合并到下方的 reviewApi）
