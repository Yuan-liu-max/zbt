// AI 智能辅助 Mock 数据
import type { AiTool, RecentConversation, RecommendScenario } from '@/types/ai'

// 智能工具
export const mockTools: AiTool[] = [
  { id: '1', name: '智能问答', description: '快速解答业务问题，提供精准信息和建议', icon: 'MessageOutlined', color: '#1890ff' },
  { id: '2', name: '文档生成', description: '根据需求生成各类文档，提升工作效率', icon: 'FileTextOutlined', color: '#52c41a' },
  { id: '3', name: '数据分析', description: '智能分析业务数据，生成可视化报表', icon: 'BarChartOutlined', color: '#722ed1' },
  { id: '4', name: '智能建议', description: '基于数据和经验，提供决策建议', icon: 'BulbOutlined', color: '#fa8c16' },
]

// 最近对话
export const mockConversations: RecentConversation[] = [
  { id: '1', title: '分析上月销售数据趋势', time: '10:30' },
  { id: '2', title: '生成供应商评估报告', time: '昨天 15:20' },
  { id: '3', title: '优化库存管理建议', time: '昨天 11:45' },
  { id: '4', title: '客户满意度分析', time: '07-10 16:30' },
  { id: '5', title: '制定营销活动方案', time: '07-10 14:20' },
]

// 推荐场景
export const mockScenarios: RecommendScenario[] = [
  { id: '1', title: '销售数据分析', description: '分析销售趋势，发现增长机会', icon: 'LineChartOutlined', color: '#1890ff' },
  { id: '2', title: '客户画像分析', description: '深度了解客户特征和需求', icon: 'UserOutlined', color: '#52c41a' },
  { id: '3', title: '库存优化建议', description: '优化库存结构，降低库存成本', icon: 'ShoppingOutlined', color: '#fa8c16' },
  { id: '4', title: '营销效果评估', description: '评估营销活动效果和ROI', icon: 'PercentageOutlined', color: '#722ed1' },
]
