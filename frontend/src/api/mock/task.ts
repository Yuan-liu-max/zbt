// 任务中心 Mock 数据
import type {
  TaskItem,
  TaskTemplate,
  TaskReviewItem,
  TaskSubmission,
  TaskAudit,
  TaskQueryParams,
  TemplateQueryParams,
  ReviewQueryParams,
  TaskStatus,
  TaskDimension,
  TaskPriority,
  TaskSourceType,
  AuditResult,
} from '@/types/task'

// ======================== Mock 数据 ========================

// 任务数据
export const mockTasks: TaskItem[] = [
  {
    id: 1,
    taskNo: 'TSK-20240520-001',
    templateId: 1,
    taskTitle: '供应商资质审核',
    dimension: 'HUMAN',
    category: '供应商管理',
    storeId: 101,
    storeName: '北京朝阳旗舰店',
    assigneeId: 1,
    assigneeName: '张三',
    auditorId: 2,
    auditorName: '李四',
    startTime: '2024-05-20 10:00',
    dueTime: '2024-05-25 18:00',
    status: 'IN_PROGRESS',
    priority: 'HIGH',
    sourceType: 'MANUAL',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 0,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-20',
  },
  {
    id: 2,
    taskNo: 'TSK-20240520-002',
    templateId: 2,
    taskTitle: '采购合同审批',
    dimension: 'PRODUCT',
    category: '合同管理',
    storeId: 102,
    storeName: '上海浦东直营店',
    assigneeId: 3,
    assigneeName: '王五',
    auditorId: 4,
    auditorName: '赵六',
    startTime: '2024-05-20 10:00',
    dueTime: '2024-05-25 18:00',
    completedTime: '2024-05-23 16:30',
    status: 'COMPLETED',
    priority: 'HIGH',
    sourceType: 'CYCLE',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 92,
    aiScore: 88,
    manualScore: 95,
    finalScore: 90,
    createdAt: '2024-05-20',
  },
  {
    id: 3,
    taskNo: 'TSK-20240520-003',
    templateId: 3,
    taskTitle: '供应商准入流程',
    dimension: 'SCENE',
    category: '供应商管理',
    storeId: 103,
    storeName: '广州天河形象店',
    assigneeId: 5,
    assigneeName: '钱七',
    auditorId: 6,
    auditorName: '孙八',
    startTime: '2024-05-20 10:00',
    dueTime: '2024-05-22 18:00',
    status: 'OVERDUE',
    priority: 'URGENT',
    sourceType: 'HQ',
    isOverdue: true,
    overdueMinutes: 4320,
    qualityScore: 0,
    aiScore: 0,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-20',
  },
  {
    id: 4,
    taskNo: 'TSK-20240521-001',
    templateId: 1,
    taskTitle: '门店日常巡检',
    dimension: 'COMPREHENSIVE',
    category: '门店管理',
    storeId: 104,
    storeName: '深圳南山体验店',
    assigneeId: 7,
    assigneeName: '周九',
    auditorId: 8,
    auditorName: '吴十',
    startTime: '2024-05-21 09:00',
    dueTime: '2024-05-21 18:00',
    status: 'PENDING',
    priority: 'MEDIUM',
    sourceType: 'CYCLE',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 0,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-21',
  },
  {
    id: 5,
    taskNo: 'TSK-20240521-002',
    templateId: 2,
    taskTitle: '库存盘点核查',
    dimension: 'PRODUCT',
    category: '库存管理',
    storeId: 105,
    storeName: '成都锦江专柜',
    assigneeId: 9,
    assigneeName: '郑十一',
    auditorId: 10,
    auditorName: '冯十二',
    startTime: '2024-05-21 14:00',
    dueTime: '2024-05-23 18:00',
    status: 'READY',
    priority: 'LOW',
    sourceType: 'AI',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 0,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-21',
  },
  {
    id: 6,
    taskNo: 'TSK-20240522-001',
    templateId: 3,
    taskTitle: '节假日专项检查',
    dimension: 'SCENE',
    category: '节假日管理',
    storeId: 101,
    storeName: '北京朝阳旗舰店',
    assigneeId: 1,
    assigneeName: '张三',
    auditorId: 2,
    auditorName: '李四',
    startTime: '2024-05-22 08:00',
    dueTime: '2024-05-22 18:00',
    completedTime: '2024-05-22 17:45',
    status: 'AUDITING',
    priority: 'HIGH',
    sourceType: 'HOLIDAY',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 78,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-22',
  },
  {
    id: 7,
    taskNo: 'TSK-20240522-002',
    templateId: 1,
    taskTitle: '异常订单跟进处理',
    dimension: 'HUMAN',
    category: '异常管理',
    storeId: 106,
    storeName: '杭州西湖授权店',
    assigneeId: 11,
    assigneeName: '陈十三',
    auditorId: 12,
    auditorName: '褚十四',
    startTime: '2024-05-22 10:00',
    dueTime: '2024-05-24 18:00',
    status: 'SUBMITTED',
    priority: 'MEDIUM',
    sourceType: 'ABNORMAL',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 82,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-22',
  },
  {
    id: 8,
    taskNo: 'TSK-20240523-001',
    templateId: 2,
    taskTitle: '陈列标准审核',
    dimension: 'PRODUCT',
    category: '陈列管理',
    storeId: 107,
    storeName: '南京新街口旗舰店',
    assigneeId: 13,
    assigneeName: '卫十五',
    auditorId: 14,
    auditorName: '蒋十六',
    startTime: '2024-05-23 09:00',
    dueTime: '2024-05-25 18:00',
    status: 'RECTIFYING',
    priority: 'URGENT',
    sourceType: 'MANUAL',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 65,
    aiScore: 60,
    manualScore: 70,
    finalScore: 65,
    createdAt: '2024-05-23',
  },
  {
    id: 9,
    taskNo: 'TSK-20240523-002',
    templateId: 3,
    taskTitle: '总部下发促销任务',
    dimension: 'COMPREHENSIVE',
    category: '促销管理',
    storeId: 108,
    storeName: '武汉光谷潮流店',
    assigneeId: 15,
    assigneeName: '沈十七',
    auditorId: 16,
    auditorName: '韩十八',
    startTime: '2024-05-23 10:00',
    dueTime: '2024-05-28 18:00',
    status: 'REJECTED',
    priority: 'MEDIUM',
    sourceType: 'HQ',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 40,
    aiScore: 35,
    manualScore: 45,
    finalScore: 40,
    createdAt: '2024-05-23',
  },
  {
    id: 10,
    taskNo: 'TSK-20240524-001',
    templateId: 1,
    taskTitle: '取消的周检任务',
    dimension: 'HUMAN',
    category: '门店管理',
    storeId: 109,
    storeName: '天津和平社区店',
    assigneeId: 17,
    assigneeName: '杨十九',
    auditorId: 18,
    auditorName: '朱二十',
    startTime: '2024-05-24 09:00',
    dueTime: '2024-05-24 18:00',
    status: 'CANCELLED',
    priority: 'LOW',
    sourceType: 'CYCLE',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 0,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-24',
  },
  {
    id: 11,
    taskNo: 'TSK-20240524-002',
    templateId: 2,
    taskTitle: '作废的临时任务',
    dimension: 'SCENE',
    category: '临时任务',
    storeId: 110,
    storeName: '重庆渝中旗舰店',
    assigneeId: 19,
    assigneeName: '秦二一',
    auditorId: 20,
    auditorName: '许二二',
    startTime: '2024-05-24 10:00',
    dueTime: '2024-05-24 18:00',
    status: 'VOIDED',
    priority: 'LOW',
    sourceType: 'MANUAL',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 0,
    aiScore: 0,
    manualScore: 0,
    finalScore: 0,
    createdAt: '2024-05-24',
  },
  {
    id: 12,
    taskNo: 'TSK-20240525-001',
    templateId: 3,
    taskTitle: 'AI巡检异常复核',
    dimension: 'HUMAN',
    category: 'AI管理',
    storeId: 101,
    storeName: '北京朝阳旗舰店',
    assigneeId: 1,
    assigneeName: '张三',
    auditorId: 2,
    auditorName: '李四',
    startTime: '2024-05-25 08:00',
    dueTime: '2024-05-26 18:00',
    status: 'APPROVED',
    priority: 'HIGH',
    sourceType: 'AI',
    isOverdue: false,
    overdueMinutes: 0,
    qualityScore: 95,
    aiScore: 93,
    manualScore: 97,
    finalScore: 95,
    createdAt: '2024-05-25',
  },
]

// 模板数据
export const mockTemplates: TaskTemplate[] = [
  { id: 1, name: '供应商资质审核模板', type: '供应商管理', creator: '张三', createdAt: '2024-05-18 10:00', updatedAt: '2024-05-20 15:30' },
  { id: 2, name: '采购合同审批模板', type: '合同管理', creator: '李四', createdAt: '2024-05-16 09:30', updatedAt: '2024-05-19 11:20' },
  { id: 3, name: '供应商准入流程模板', type: '供应商管理', creator: '王五', createdAt: '2024-05-14 14:20', updatedAt: '2024-05-18 16:40' },
  { id: 4, name: '月度销售目标制定模板', type: '销售管理', creator: '赵六', createdAt: '2024-05-10 15:00', updatedAt: '2024-05-15 10:10' },
]

// 审查数据
export const mockReviews: TaskReviewItem[] = [
  { id: 1, name: '供应商资质审核', type: '供应商管理', initiator: '张三', initiateTime: '2024-05-20 10:00', currentNode: '部门负责人审核', status: 'AUDITING' },
  { id: 2, name: '采购合同审批', type: '合同管理', initiator: '李四', initiateTime: '2024-05-20 09:30', currentNode: '财务审核', status: 'SUBMITTED' },
  { id: 3, name: '供应商准入申请', type: '供应商管理', initiator: '王五', initiateTime: '2024-05-20 09:00', currentNode: '法务审核', status: 'PENDING' },
]

// 任务提交数据
export const mockSubmissions: TaskSubmission[] = [
  {
    submissionId: 1,
    taskId: 1,
    textContent: '供应商资质材料已收到，初审通过。营业执照、税务登记证齐全，经营范围覆盖合作品类。',
    formData: { materialCheck: true, licenseValid: true, scopeMatch: true },
    photoUrls: ['/uploads/submissions/1/photo1.jpg', '/uploads/submissions/1/photo2.jpg'],
    attachmentUrls: ['/uploads/submissions/1/license.pdf', '/uploads/submissions/1/tax_cert.pdf'],
    location: { lat: 39.9042, lng: 116.4074 },
    submittedAt: '2024-05-22 14:30',
  },
  {
    submissionId: 2,
    taskId: 7,
    textContent: '异常订单已跟进完毕，客户已确认退款方案，订单状态已更改为已处理。',
    formData: { orderHandled: true, refundIssued: true, customerConfirmed: true },
    photoUrls: [],
    attachmentUrls: ['/uploads/submissions/2/refund_receipt.pdf'],
    submittedAt: '2024-05-23 11:15',
  },
  {
    submissionId: 3,
    taskId: 8,
    textContent: '门店陈列标准检查完毕，发现3处不符合标准，已提交整改要求。',
    formData: { checked: true, issues: 3, recheckRequired: true },
    photoUrls: ['/uploads/submissions/3/issue1.jpg', '/uploads/submissions/3/issue2.jpg', '/uploads/submissions/3/issue3.jpg'],
    attachmentUrls: ['/uploads/submissions/3/rectify_plan.docx'],
    location: { lat: 32.0603, lng: 118.7969 },
    submittedAt: '2024-05-23 16:45',
  },
  {
    submissionId: 4,
    taskId: 6,
    textContent: '节假日专项检查完成，门店运营状态良好，客流正常，无安全隐患。',
    formData: { safetyChecked: true, operationNormal: true, crowdControl: true },
    photoUrls: ['/uploads/submissions/4/store_front.jpg', '/uploads/submissions/4/interior.jpg'],
    attachmentUrls: [],
    location: { lat: 39.9163, lng: 116.3972 },
    submittedAt: '2024-05-22 17:30',
  },
  {
    submissionId: 5,
    taskId: 12,
    textContent: 'AI巡检异常已复核确认，2处商品陈列偏差属实，已通知门店调整。',
    formData: { aiAlertConfirmed: true, itemsAdjusted: 2, adjustedItems: ['A区货架', 'B区展台'] },
    photoUrls: ['/uploads/submissions/5/ai_alert_1.jpg', '/uploads/submissions/5/ai_alert_2.jpg'],
    attachmentUrls: ['/uploads/submissions/5/ai_report.pdf'],
    submittedAt: '2024-05-25 15:20',
  },
]

// 任务审核数据
export const mockAudits: TaskAudit[] = [
  {
    auditId: 1,
    auditorName: '李四',
    auditResult: 'APPROVED',
    auditComment: '材料齐全，流程合规，审核通过。',
    score: 92,
    auditedAt: '2024-05-22 16:00',
  },
  {
    auditId: 2,
    auditorName: '褚十四',
    auditResult: 'APPROVED',
    auditComment: '异常订单处理及时，客户反馈良好。',
    score: 88,
    auditedAt: '2024-05-23 17:00',
  },
  {
    auditId: 3,
    auditorName: '蒋十六',
    auditResult: 'RECTIFY',
    auditComment: '陈列有3处不符合标准，需在3个工作日内完成整改并提交复查。',
    score: 65,
    auditedAt: '2024-05-24 10:00',
  },
  {
    auditId: 4,
    auditorName: '韩十八',
    auditResult: 'REJECTED',
    auditComment: '促销方案缺少成本核算，不符合总部规范要求，请重新提交。',
    score: 40,
    auditedAt: '2024-05-24 14:30',
  },
  {
    auditId: 5,
    auditorName: '李四',
    auditResult: 'APPROVED',
    auditComment: 'AI巡检异常复核结果准确，调整到位，审核通过。',
    score: 95,
    auditedAt: '2024-05-25 16:30',
  },
]

// ======================== 状态映射 ========================

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

// ======================== 模拟延迟 ========================

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// ======================== 统一返回结构 ========================

function success<T>(data: T) {
  return { code: 200, msg: 'success', data }
}

function fail(msg: string) {
  return { code: 500, msg, data: null }
}

// ======================== 任务 API ========================

export const taskApi = {
  async getList(params: TaskQueryParams) {
    await delay(300)
    let filtered = [...mockTasks]
    if (params.keyword) {
      const kw = params.keyword.toLowerCase()
      filtered = filtered.filter(
        item =>
          item.taskNo.toLowerCase().includes(kw) ||
          item.taskTitle.toLowerCase().includes(kw) ||
          item.storeName.toLowerCase().includes(kw) ||
          item.assigneeName.toLowerCase().includes(kw),
      )
    }
    if (params.dimension) filtered = filtered.filter(item => item.dimension === params.dimension)
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    if (params.priority) filtered = filtered.filter(item => item.priority === params.priority)
    if (params.sourceType) filtered = filtered.filter(item => item.sourceType === params.sourceType)
    if (params.startDate) filtered = filtered.filter(item => item.createdAt >= params.startDate!)
    if (params.endDate) filtered = filtered.filter(item => item.createdAt <= params.endDate!)
    const start = (params.page - 1) * params.size
    return success({
      list: filtered.slice(start, start + params.size),
      total: filtered.length,
      page: params.page,
      size: params.size,
    })
  },

  async getById(id: number) {
    await delay(200)
    const task = mockTasks.find(item => item.id === id)
    return task ? success(task) : fail('任务不存在')
  },

  async create(data: Partial<TaskItem>) {
    await delay(500)
    const maxId = mockTasks.reduce((max, item) => Math.max(max, item.id), 0)
    const newItem: TaskItem = {
      id: maxId + 1,
      taskNo: `TSK-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(maxId + 1).padStart(3, '0')}`,
      templateId: data.templateId ?? 0,
      taskTitle: data.taskTitle ?? '',
      dimension: data.dimension ?? 'COMPREHENSIVE',
      category: data.category ?? '',
      storeId: data.storeId ?? 0,
      storeName: data.storeName ?? '',
      assigneeId: data.assigneeId ?? 0,
      assigneeName: data.assigneeName ?? '',
      auditorId: data.auditorId ?? 0,
      auditorName: data.auditorName ?? '',
      startTime: data.startTime ?? '',
      dueTime: data.dueTime ?? '',
      status: 'PENDING',
      priority: data.priority ?? 'MEDIUM',
      sourceType: data.sourceType ?? 'MANUAL',
      isOverdue: false,
      overdueMinutes: 0,
      qualityScore: 0,
      aiScore: 0,
      manualScore: 0,
      finalScore: 0,
      createdAt: new Date().toISOString().slice(0, 10),
    }
    mockTasks.push(newItem)
    return success(newItem)
  },

  async update(id: number, data: Partial<TaskItem>) {
    await delay(500)
    const index = mockTasks.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTasks[index] = { ...mockTasks[index], ...data }
      return success(mockTasks[index])
    }
    return fail('任务不存在')
  },

  async delete(id: number) {
    await delay(300)
    const index = mockTasks.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTasks.splice(index, 1)
      return success(true)
    }
    return fail('任务不存在')
  },
}

// ======================== 提交 API ========================

export const submissionApi = {
  async getByTaskId(taskId: number) {
    await delay(200)
    const list = mockSubmissions.filter(item => item.taskId === taskId)
    return success(list)
  },

  async submit(data: Partial<TaskSubmission>) {
    await delay(500)
    const maxId = mockSubmissions.reduce((max, item) => Math.max(max, item.submissionId), 0)
    const newSubmission: TaskSubmission = {
      submissionId: maxId + 1,
      taskId: data.taskId ?? 0,
      textContent: data.textContent ?? '',
      formData: data.formData ?? {},
      photoUrls: data.photoUrls ?? [],
      attachmentUrls: data.attachmentUrls ?? [],
      location: data.location,
      submittedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
    }
    mockSubmissions.push(newSubmission)
    // 同步更新任务状态
    const taskIndex = mockTasks.findIndex(item => item.id === data.taskId)
    if (taskIndex !== -1) {
      mockTasks[taskIndex].status = 'SUBMITTED'
    }
    return success(newSubmission)
  },
}

// ======================== 审核 API ========================

export const auditApi = {
  async getByTaskId(taskId: number) {
    await delay(200)
    const task = mockTasks.find(item => item.id === taskId)
    if (!task) return fail('任务不存在')
    const audit = mockAudits.find(a => a.auditorName === task.auditorName)
    return success(audit ? [audit] : [])
  },

  async audit(taskId: number, data: { auditResult: AuditResult; auditComment: string; score: number }) {
    await delay(500)
    const taskIndex = mockTasks.findIndex(item => item.id === taskId)
    if (taskIndex === -1) return fail('任务不存在')
    const task = mockTasks[taskIndex]

    const newAudit: TaskAudit = {
      auditId: mockAudits.length + 1,
      auditorName: task.auditorName,
      auditResult: data.auditResult,
      auditComment: data.auditComment,
      score: data.score,
      auditedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
    }
    mockAudits.push(newAudit)

    // 根据审核结果更新任务状态和分数
    if (data.auditResult === 'APPROVED') {
      task.status = 'APPROVED'
      task.completedTime = newAudit.auditedAt
    } else if (data.auditResult === 'REJECTED') {
      task.status = 'REJECTED'
    } else if (data.auditResult === 'RECTIFY') {
      task.status = 'RECTIFYING'
    }
    task.manualScore = data.score
    task.finalScore = Math.round((task.aiScore + task.manualScore) / 2 * 10) / 10

    return success(newAudit)
  },

  async getList(params: ReviewQueryParams) {
    await delay(300)
    let filtered = [...mockReviews]
    if (params.name) filtered = filtered.filter(item => item.name.includes(params.name!))
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    if (params.initiator) filtered = filtered.filter(item => item.initiator.includes(params.initiator!))
    const start = (params.page - 1) * params.size
    return success({
      list: filtered.slice(start, start + params.size),
      total: filtered.length,
      page: params.page,
      size: params.size,
    })
  },
}

// ======================== 模板 API ========================

export const templateApi = {
  async getList(params: TemplateQueryParams) {
    await delay(300)
    let filtered = [...mockTemplates]
    if (params.name) filtered = filtered.filter(item => item.name.includes(params.name!))
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    const start = (params.page - 1) * params.size
    return success({
      list: filtered.slice(start, start + params.size),
      total: filtered.length,
      page: params.page,
      size: params.size,
    })
  },

  async create(data: Partial<TaskTemplate>) {
    await delay(500)
    const maxId = mockTemplates.reduce((max, item) => Math.max(max, item.id), 0)
    const newItem: TaskTemplate = {
      id: maxId + 1,
      name: data.name ?? '',
      type: data.type ?? '',
      creator: data.creator ?? '管理员',
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
    }
    mockTemplates.push(newItem)
    return success(newItem)
  },

  async update(id: number, data: Partial<TaskTemplate>) {
    await delay(500)
    const index = mockTemplates.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTemplates[index] = {
        ...mockTemplates[index],
        ...data,
        updatedAt: new Date().toISOString().replace('T', ' ').slice(0, 16),
      }
      return success(mockTemplates[index])
    }
    return fail('模板不存在')
  },

  async delete(id: number) {
    await delay(300)
    const index = mockTemplates.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTemplates.splice(index, 1)
      return success(true)
    }
    return fail('模板不存在')
  },
}

// ======================== 审查 API（兼容旧接口） ========================

export const reviewApi = {
  async getList(params: ReviewQueryParams) {
    return auditApi.getList(params)
  },

  async submitReview(id: number, data: { approved: boolean; comment: string }) {
    await delay(500)
    const index = mockReviews.findIndex(item => item.id === id)
    if (index !== -1) {
      mockReviews[index].status = data.approved ? 'APPROVED' : 'REJECTED'
      return success(mockReviews[index])
    }
    return fail('审查记录不存在')
  },
}
