// 智能工具相关类型定义

// 文档模板类型
export type TemplateCategory = 'all' | 'contract' | 'report' | 'form' | 'notice' | 'other'

// 文档模板
export interface DocTemplate {
  id: string
  name: string
  description: string
  category: TemplateCategory
  usageCount: number
}

// 智能建议
export interface AiSuggestion {
  id: string
  title: string
  category: string
  priority: 'high' | 'medium' | 'low'
  impact: string
  roi: string
  date: string
  description: string
}

// 分析报告
export interface AnalysisReport {
  totalSales: number
  orderCount: number
  customerCount: number
  avgOrderAmount: number
  salesTrend: { date: string; value: number }[]
  channelBreakdown: { name: string; value: number; color: string }[]
}

// 对话消息
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  time: string
}
