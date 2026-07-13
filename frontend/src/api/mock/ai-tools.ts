// 智能工具 Mock 数据
import type { DocTemplate, AiSuggestion, AnalysisReport, ChatMessage } from '@/types/ai-tools'

// 文档模板
export const mockTemplates: DocTemplate[] = [
  { id: '1', name: '采购合同模板', description: '标准采购合同模板，包含双方信息、商品明细、付款条款等', category: 'contract', usageCount: 128 },
  { id: '2', name: '销售合同模板', description: '销售合同模板，包含销售条款、退换货政策等', category: 'contract', usageCount: 96 },
  { id: '3', name: '供应商评估报告', description: '供应商综合评估报告模板', category: 'report', usageCount: 85 },
  { id: '4', name: '采购申请单', description: '采购申请表单模板', category: 'form', usageCount: 156 },
  { id: '5', name: '库存盘点报告', description: '库存盘点结果报告模板', category: 'report', usageCount: 72 },
  { id: '6', name: '会议通知', description: '会议通知公告模板', category: 'notice', usageCount: 64 },
  { id: '7', name: '库存预警通知', description: '库存预警通知模板', category: 'notice', usageCount: 45 },
  { id: '8', name: '员工培训方案', description: '员工培训计划方案模板', category: 'other', usageCount: 38 },
]

// 智能建议
export const mockSuggestions: AiSuggestion[] = [
  { id: '1', title: '优化库存结构', category: '库存优化分析', priority: 'high', impact: '通过分析库存周转率，优化热销商品采购比例', roi: '降低库存成本15-20%', date: '2024-07-10', description: '建议：减少滞销商品库存\n预期效果：降低库存成本15-20%' },
  { id: '2', title: '拓展优质客户', category: '客户分析', priority: 'medium', impact: '分析高价值客户特征，精准营销', roi: '提升销售额25-30%', date: '2024-07-09', description: '建议：精准营销高价值客户群体\n预期效果：提升销售额25-30%' },
  { id: '3', title: '优化定价策略', category: '销售分析', priority: 'medium', impact: '根据市场数据调整商品定价', roi: '提升利润率5-10%', date: '2024-07-09', description: '建议：调整热销商品定价\n预期效果：提升利润率5-10%' },
  { id: '4', title: '供应商谈判', category: '供应链分析', priority: 'low', impact: '通过批量采购降低采购成本', roi: '降低采购成本8-12%', date: '2024-07-08', description: '建议：与主要供应商谈判\n预期效果：降低采购成本8-12%' },
]

// 分析报告
export const mockAnalysisReport: AnalysisReport = {
  totalSales: 2456799,
  orderCount: 1234,
  customerCount: 567,
  avgOrderAmount: 1989,
  salesTrend: [
    { date: '07-01', value: 75000 },
    { date: '07-03', value: 82000 },
    { date: '07-05', value: 78000 },
    { date: '07-07', value: 95000 },
    { date: '07-09', value: 88000 },
  ],
  channelBreakdown: [
    { name: '线上销售', value: 45, color: '#1890ff' },
    { name: '线下门店', value: 35, color: '#52c41a' },
    { name: '分销渠道', value: 20, color: '#faad14' },
  ]
}

// 对话消息
export const mockChatMessages: ChatMessage[] = [
  { id: '1', role: 'assistant', content: '你好，我是您的AI智能助手！\n\n我可以为您提供以下帮助：\n\n• 如何创建销售新客户？\n• 供应商资质审核流程是怎样的？\n• 最近的销售数据是怎样的？\n\n请问您想了解什么？', time: '10:30' },
]

// 历史记录
export const mockHistory = [
  { id: '1', title: '如何创建销售客户？', time: '07-10 10:30' },
  { id: '2', title: '供应商资质审核流程是怎样的？', time: '07-10 09:15' },
  { id: '3', title: '最近的销售数据是怎样的？', time: '07-09 16:45' },
  { id: '4', title: '如何生成销售报表？', time: '07-09 14:20' },
  { id: '5', title: '库存管理规则怎么设置？', time: '07-08 11:30' },
]
