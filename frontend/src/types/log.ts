// 日志管理相关类型定义

// 日志记录（后端 OperateLog）
export interface LogItem {
  id: number
  operatorId: number | null    // 操作人ID
  module: string               // 操作模块
  action: string               // 操作类型
  targetType: string           // 操作对象类型
  targetId: number | null      // 操作对象ID
  requestIp: string            // 请求IP
  requestParams: string        // 请求参数
  oldData: string              // 变更前数据
  newData: string              // 变更后数据
  operatorName: string         // 操作人姓名（后端冗余）
  createdAt: string            // 操作时间
}

// 日志查询参数
export interface LogQueryParams {
  startDate?: string
  endDate?: string
  module?: string
  operator?: string
  keyword?: string
  page: number
  pageSize: number
}
