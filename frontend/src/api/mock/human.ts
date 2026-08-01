// 人效管理 Mock 数据
import type { MeetingItem, InterviewItem, AssessItem, PerformanceItem, HumanQueryParams } from '@/types/human'

// 晨夕会数据
export const mockMeetings: MeetingItem[] = [
  {
    id: 1,
    meetingType: 'MORNING',
    meetingDate: '2026-06-20 09:00',
    host: '张三',
    participants: 12,
    storeTargetAmount: 50000,
    mainProducts: '智能手表,蓝牙耳机',
    keyCustomers: '王总,李经理',
    todayStrategy: '主推高毛利产品，重点关注老客户复购',
    employeeTargets: { '张三': 8000, '李四': 6000, '王五': 7000 },
    meetingPhotoUrls: ['/photos/meeting1_1.jpg', '/photos/meeting1_2.jpg'],
    actualSalesAmount: 42000,
    targetCompletionRate: 84,
    successfulCases: '张三成功签下王总大单',
    failedCases: '李经理对价格仍有异议',
    tomorrowImprovement: '加强话术培训，准备竞品对比资料',
    status: 'ended',
    createdAt: '2026-06-20',
  },
  {
    id: 2,
    meetingType: 'MORNING',
    meetingDate: '2026-06-20 09:30',
    host: '李四',
    participants: 8,
    storeTargetAmount: 30000,
    mainProducts: '运动鞋,瑜伽垫',
    keyCustomers: '赵总,周经理',
    todayStrategy: '集中精力清理库存，配合促销活动',
    employeeTargets: { '赵六': 5000, '孙七': 4000 },
    meetingPhotoUrls: ['/photos/meeting2_1.jpg'],
    actualSalesAmount: 28000,
    targetCompletionRate: 93,
    status: 'ended',
    createdAt: '2026-06-20',
  },
  {
    id: 3,
    meetingType: 'EVENING',
    meetingDate: '2026-06-19 18:00',
    host: '王五',
    participants: 10,
    storeTargetAmount: 45000,
    mainProducts: '智能手表,蓝牙耳机,运动手环',
    keyCustomers: '吴总,郑经理',
    todayStrategy: '总结当日销售情况，分析未成交原因',
    employeeTargets: { '张三': 7500, '李四': 6500 },
    meetingPhotoUrls: ['/photos/meeting3_1.jpg', '/photos/meeting3_2.jpg'],
    actualSalesAmount: 38000,
    targetCompletionRate: 84,
    successfulCases: '王五成功推荐套装组合',
    status: 'ended',
    createdAt: '2026-06-19',
  },
  {
    id: 4,
    meetingType: 'MORNING',
    meetingDate: '2026-06-19 09:15',
    host: '赵六',
    participants: 6,
    storeTargetAmount: 20000,
    mainProducts: '瑜伽垫,运动水壶',
    keyCustomers: '陈总',
    todayStrategy: '新员工带教，熟悉产品卖点',
    employeeTargets: { '周八': 3000 },
    meetingPhotoUrls: [],
    status: 'cancelled',
    createdAt: '2026-06-19',
  },
  {
    id: 5,
    meetingType: 'EVENING',
    meetingDate: '2026-06-18 18:30',
    host: '孙七',
    participants: 9,
    storeTargetAmount: 40000,
    mainProducts: '蓝牙耳机,运动手环',
    keyCustomers: '刘总,黄经理',
    todayStrategy: '晚间复盘，优化明日销售计划',
    employeeTargets: { '吴九': 6000, '周八': 5500 },
    meetingPhotoUrls: ['/photos/meeting5_1.jpg'],
    actualSalesAmount: 36000,
    targetCompletionRate: 90,
    status: 'ended',
    createdAt: '2026-06-18',
  },
]

// 员工面谈数据
export const mockInterviews: InterviewItem[] = [
  {
    id: 1,
    employeeId: 101,
    employeeName: '李四',
    interviewerId: 201,
    interviewerName: '张三',
    interviewDate: '2026-06-20 10:00',
    interviewType: 'PERFORMANCE',
    currentWeekSales: 12000,
    targetCompletionRate: 80,
    mainProblem: '本周客单价偏低，需要提升连带销售能力',
    customerFollowIssue: '3位重点客户跟进不及时',
    productKnowledgeGap: '新品功能参数掌握不够熟练',
    mindsetStatus: 'POSITIVE',
    nextWeekGoal: '提升客单价至500元以上',
    improvementPlan: '每日练习连带话术，参加产品培训',
    managerComment: '态度积极，建议加强产品知识学习',
    employeeFeedback: '会积极配合改进',
    duration: '45分钟',
    status: 'completed',
    createdAt: '2026-06-20',
  },
  {
    id: 2,
    employeeId: 102,
    employeeName: '赵六',
    interviewerId: 202,
    interviewerName: '王五',
    interviewDate: '2026-06-19 15:00',
    interviewType: 'PROBLEM',
    currentWeekSales: 8000,
    targetCompletionRate: 60,
    mainProblem: '连续两周未完成销售目标，状态低迷',
    customerFollowIssue: '客户投诉处理不够及时',
    productKnowledgeGap: '竞品分析能力不足',
    mindsetStatus: 'LOW',
    nextWeekGoal: '完成10000元销售目标',
    improvementPlan: '每日复盘，主管一对一辅导',
    managerComment: '需要更多关注和引导',
    employeeFeedback: '希望能得到更多支持',
    duration: '30分钟',
    status: 'completed',
    createdAt: '2026-06-19',
  },
  {
    id: 3,
    employeeId: 103,
    employeeName: '孙七',
    interviewerId: 201,
    interviewerName: '李四',
    interviewDate: '2026-06-18 14:30',
    interviewType: 'DEVELOPMENT',
    currentWeekSales: 15000,
    targetCompletionRate: 95,
    mainProblem: '销售能力突出，需要规划晋升路径',
    customerFollowIssue: '大客户维护良好',
    productKnowledgeGap: '管理能力有待提升',
    mindsetStatus: 'POSITIVE',
    nextWeekGoal: '开始带领新人，锻炼管理能力',
    improvementPlan: '参加管理培训课程，承担更多团队责任',
    managerComment: '表现优秀，建议纳入储备干部培养',
    employeeFeedback: '愿意承担更多责任',
    duration: '60分钟',
    status: 'completed',
    createdAt: '2026-06-18',
  },
  {
    id: 4,
    employeeId: 104,
    employeeName: '周八',
    interviewerId: 203,
    interviewerName: '张三',
    interviewDate: '2026-06-17 11:00',
    interviewType: 'ONBOARDING',
    currentWeekSales: 3000,
    targetCompletionRate: 50,
    mainProblem: '新人入职适应期，需要熟悉产品和流程',
    customerFollowIssue: '客户接待经验不足',
    productKnowledgeGap: '产品知识需要系统学习',
    mindsetStatus: 'NORMAL',
    nextWeekGoal: '独立完成客户接待，熟悉全部产品',
    improvementPlan: '跟随老员工学习，每日总结学习笔记',
    managerComment: '学习态度好，适应能力较强',
    employeeFeedback: '感谢公司的培训安排',
    duration: '30分钟',
    status: 'completed',
    createdAt: '2026-06-17',
  },
  {
    id: 5,
    employeeId: 105,
    employeeName: '吴九',
    interviewerId: 202,
    interviewerName: '王五',
    interviewDate: '2026-06-16 16:00',
    interviewType: 'TRANSFER',
    currentWeekSales: 10000,
    targetCompletionRate: 85,
    mainProblem: '试用期即将结束，评估转正资格',
    customerFollowIssue: '客户满意度较高',
    productKnowledgeGap: '已基本掌握全部产品知识',
    mindsetStatus: 'POSITIVE',
    nextWeekGoal: '完成转正考核各项指标',
    improvementPlan: '继续保持良好状态，冲刺转正目标',
    managerComment: '试用期表现良好，建议按期转正',
    employeeFeedback: '对转正充满信心',
    duration: '40分钟',
    status: 'completed',
    createdAt: '2026-06-16',
  },
]

// 能力考核数据
export const mockAssesses: AssessItem[] = [
  {
    id: 1,
    name: '2026年第25周能力考核',
    assessmentWeek: '2026-06-15 ~ 2026-06-21',
    type: 'monthly',
    assessor: '张三',
    participants: 32,
    productKnowledgeScore: 85,
    matchingSkillScore: 78,
    receptionScore: 82,
    objectionHandlingScore: 75,
    promotionScriptScore: 80,
    totalScore: 80,
    improvementAdvice: '加强异议处理和连带销售话术练习',
    status: 'completed',
    createdAt: '2026-06-15',
  },
  {
    id: 2,
    name: '2026年第24周能力考核',
    assessmentWeek: '2026-06-08 ~ 2026-06-14',
    type: 'monthly',
    assessor: '李四',
    participants: 18,
    productKnowledgeScore: 82,
    matchingSkillScore: 75,
    receptionScore: 80,
    objectionHandlingScore: 72,
    promotionScriptScore: 78,
    totalScore: 77,
    improvementAdvice: '重点提升异议处理能力，多做模拟演练',
    status: 'completed',
    createdAt: '2026-06-08',
  },
  {
    id: 3,
    name: '2026年Q2季度能力考核',
    assessmentWeek: '2026-04-01 ~ 2026-06-30',
    type: 'quarterly',
    assessor: '王五',
    participants: 12,
    productKnowledgeScore: 88,
    matchingSkillScore: 82,
    receptionScore: 85,
    objectionHandlingScore: 80,
    promotionScriptScore: 84,
    totalScore: 84,
    improvementAdvice: '整体表现良好，建议参加高阶销售技巧培训',
    status: 'ongoing',
    createdAt: '2026-04-01',
  },
  {
    id: 4,
    name: '2026年第23周能力考核',
    assessmentWeek: '2026-06-01 ~ 2026-06-07',
    type: 'monthly',
    assessor: '张三',
    participants: 30,
    productKnowledgeScore: 80,
    matchingSkillScore: 73,
    receptionScore: 78,
    objectionHandlingScore: 70,
    promotionScriptScore: 76,
    totalScore: 75,
    improvementAdvice: '匹配技能和异议处理需要重点加强',
    status: 'ended',
    createdAt: '2026-06-01',
  },
  {
    id: 5,
    name: '2026年第22周能力考核',
    assessmentWeek: '2026-05-25 ~ 2026-05-31',
    type: 'special',
    assessor: '赵六',
    participants: 15,
    productKnowledgeScore: 78,
    matchingSkillScore: 70,
    receptionScore: 76,
    objectionHandlingScore: 68,
    promotionScriptScore: 74,
    totalScore: 73,
    improvementAdvice: '建议增加实战演练频次，提升综合销售能力',
    status: 'ended',
    createdAt: '2026-05-25',
  },
]

// 绩效复盘数据
export const mockPerformances: PerformanceItem[] = [
  { id: 1, topic: '2026年Q2绩效复盘会', type: 'quarterly', period: '2026-04-01 ~ 2026-06-30', assignee: '张三', participants: 12, status: 'ongoing', createdAt: '2026-04-01' },
  { id: 2, topic: '销售团队6月复盘', type: 'monthly', period: '2026-06-01 ~ 2026-06-30', assignee: '李四', participants: 8, status: 'ongoing', createdAt: '2026-06-01' },
  { id: 3, topic: '新品推广项目复盘', type: 'project', period: '2026-05-01 ~ 2026-05-31', assignee: '王五', participants: 6, status: 'completed', createdAt: '2026-05-01' },
  { id: 4, topic: '618营销活动复盘', type: 'activity', period: '2026-06-01 ~ 2026-06-18', assignee: '赵六', participants: 7, status: 'completed', createdAt: '2026-06-01' },
  { id: 5, topic: '小程序迭代复盘', type: 'iteration', period: '2026-03-01 ~ 2026-03-31', assignee: '孙七', participants: 5, status: 'completed', createdAt: '2026-03-01' },
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
  completed: { text: '已完成', color: 'blue' },
}

export const performanceStatusMap: Record<string, { text: string; color: string }> = {
  ongoing: { text: '进行中', color: 'green' },
  completed: { text: '已完成', color: 'default' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 包装响应
const wrapResponse = <T>(data: T) => ({ code: 200, msg: 'success', data })

// 晨夕会 API
export const meetingApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockMeetings]
    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.host.includes(params.keyword!) ||
        item.mainProducts.includes(params.keyword!) ||
        item.keyCustomers.includes(params.keyword!)
      )
    }
    if (params.type) filtered = filtered.filter(item => item.meetingType === params.type)
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    const start = (params.page - 1) * params.size
    const list = filtered.slice(start, start + params.size)
    return wrapResponse({ list, total: filtered.length, page: params.page, size: params.size })
  },
  async create(data: Partial<MeetingItem>) {
    await delay(500)
    const newItem: MeetingItem = {
      id: mockMeetings.length + 1,
      meetingType: data.meetingType || 'MORNING',
      meetingDate: data.meetingDate || '',
      host: data.host || '',
      participants: data.participants || 0,
      storeTargetAmount: data.storeTargetAmount || 0,
      mainProducts: data.mainProducts || '',
      keyCustomers: data.keyCustomers || '',
      todayStrategy: data.todayStrategy || '',
      employeeTargets: data.employeeTargets || {},
      meetingPhotoUrls: data.meetingPhotoUrls || [],
      status: 'ongoing',
      createdAt: new Date().toISOString().slice(0, 10),
    }
    mockMeetings.push(newItem)
    return wrapResponse(newItem)
  },
}

// 员工面谈 API
export const interviewApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockInterviews]
    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.employeeName.includes(params.keyword!) ||
        item.interviewerName.includes(params.keyword!) ||
        item.mainProblem.includes(params.keyword!)
      )
    }
    if (params.type) filtered = filtered.filter(item => item.interviewType === params.type)
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    const start = (params.page - 1) * params.size
    const list = filtered.slice(start, start + params.size)
    return wrapResponse({ list, total: filtered.length, page: params.page, size: params.size })
  },
  async create(data: Partial<InterviewItem>) {
    await delay(500)
    const newItem: InterviewItem = {
      id: mockInterviews.length + 1,
      employeeId: data.employeeId || 0,
      employeeName: data.employeeName || '',
      interviewerId: data.interviewerId || 0,
      interviewerName: data.interviewerName || '',
      interviewDate: data.interviewDate || '',
      interviewType: data.interviewType || 'PERFORMANCE',
      currentWeekSales: data.currentWeekSales || 0,
      targetCompletionRate: data.targetCompletionRate || 0,
      mainProblem: data.mainProblem || '',
      customerFollowIssue: data.customerFollowIssue || '',
      productKnowledgeGap: data.productKnowledgeGap || '',
      mindsetStatus: data.mindsetStatus || 'NORMAL',
      nextWeekGoal: data.nextWeekGoal || '',
      improvementPlan: data.improvementPlan || '',
      managerComment: data.managerComment || '',
      employeeFeedback: data.employeeFeedback || '',
      duration: data.duration || '',
      status: 'completed',
      createdAt: new Date().toISOString().slice(0, 10),
    }
    mockInterviews.push(newItem)
    return wrapResponse(newItem)
  },
}

// 能力考核 API
export const assessApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockAssesses]
    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.name.includes(params.keyword!) ||
        item.assessor.includes(params.keyword!)
      )
    }
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    const start = (params.page - 1) * params.size
    const list = filtered.slice(start, start + params.size)
    return wrapResponse({ list, total: filtered.length, page: params.page, size: params.size })
  },
  async create(data: Partial<AssessItem>) {
    await delay(500)
    const newItem: AssessItem = {
      id: mockAssesses.length + 1,
      name: data.name || '',
      assessmentWeek: data.assessmentWeek || '',
      type: data.type || 'monthly',
      assessor: data.assessor || '',
      participants: data.participants || 0,
      productKnowledgeScore: data.productKnowledgeScore || 0,
      matchingSkillScore: data.matchingSkillScore || 0,
      receptionScore: data.receptionScore || 0,
      objectionHandlingScore: data.objectionHandlingScore || 0,
      promotionScriptScore: data.promotionScriptScore || 0,
      totalScore: data.totalScore || 0,
      improvementAdvice: data.improvementAdvice || '',
      status: 'ongoing',
      createdAt: new Date().toISOString().slice(0, 10),
    }
    mockAssesses.push(newItem)
    return wrapResponse(newItem)
  },
}

// 绩效复盘 API
export const performanceApi = {
  async getList(params: HumanQueryParams) {
    await delay(300)
    let filtered = [...mockPerformances]
    if (params.keyword) filtered = filtered.filter(item => item.topic.includes(params.keyword!))
    if (params.type) filtered = filtered.filter(item => item.type === params.type)
    if (params.status) filtered = filtered.filter(item => item.status === params.status)
    const start = (params.page - 1) * params.size
    const list = filtered.slice(start, start + params.size)
    return wrapResponse({ list, total: filtered.length, page: params.page, size: params.size })
  },
  async create(data: Partial<PerformanceItem>) {
    await delay(500)
    const newItem: PerformanceItem = {
      id: mockPerformances.length + 1,
      topic: data.topic || '',
      type: data.type || 'monthly',
      period: data.period || '',
      assignee: data.assignee || '',
      participants: data.participants || 0,
      status: 'ongoing',
      createdAt: new Date().toISOString().slice(0, 10),
    }
    mockPerformances.push(newItem)
    return wrapResponse(newItem)
  },
}
