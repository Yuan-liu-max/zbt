// AI 智能辅助相关类型定义

// 智能工具
export interface AiTool {
  id: string
  name: string
  description: string
  icon: string
  color: string
}

// 最近对话
export interface RecentConversation {
  id: string
  title: string
  time: string
}

// 推荐场景
export interface RecommendScenario {
  id: string
  title: string
  description: string
  icon: string
  color: string
}
