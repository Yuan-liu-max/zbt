// 财务管理相关类型定义

// 交易类型
export type TransactionType = 'income' | 'expense'

// 财务统计
export interface FinanceStats {
  totalIncome: number        // 总收入
  totalExpense: number       // 总支出
  netProfit: number          // 净利润
}

// 收支记录
export interface TransactionRecord {
  id: string
  code: string              // 流水号
  type: TransactionType     // 交易类型
  account: string           // 交易账户
  relatedObject: string     // 关联对象
  amount: number            // 交易金额
  transactionDate: string   // 交易日期
  remark: string            // 备注
  createdAt: string
}

// 交易查询参数
export interface TransactionQueryParams {
  type?: TransactionType    // 交易类型
  startDate?: string        // 开始日期
  endDate?: string          // 结束日期
  account?: string          // 交易账户
  relatedObject?: string    // 关联对象
  keyword?: string          // 关键词
  page: number
  pageSize: number
}
