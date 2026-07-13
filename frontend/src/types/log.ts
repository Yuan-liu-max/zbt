// 日志管理相关类型定义

// 操作结果
export type LogResult = 'success' | 'warning' | 'failed'

// 操作类型
export type LogAction = string

// 日志记录
export interface LogItem {
  id: string
  logTime: string           // 日志时间
  module: string            // 操作模块
  action: string            // 操作类型
  operator: string          // 操作人员
  ip: string                // IP地址
  result: LogResult         // 操作结果
  detail?: string           // 详情
}

// 日志查询参数
export interface LogQueryParams {
  startDate?: string
  endDate?: string
  action?: string
  operator?: string
  keyword?: string
  page: number
  pageSize: number
}
