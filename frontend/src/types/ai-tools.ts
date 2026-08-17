// 智能工具相关类型定义

// 文档模板分类（按 businessType 归类）
export type TemplateCategory = 'all' | 'EMPLOYEE' | 'PRODUCT' | 'SCENE' | 'TASK' | 'OTHER'

// 文档模板（源自提示词模板 prompt_template）
export interface DocTemplate {
  id: string
  name: string
  description: string
  businessType: string
  content: string
  typeLabel: string
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
