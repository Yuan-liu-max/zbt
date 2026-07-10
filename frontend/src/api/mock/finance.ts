// 财务管理 Mock 数据
import type { FinanceStats, TransactionRecord, TransactionQueryParams, TransactionType } from '@/types/finance'

// 财务统计
export const mockFinanceStats: FinanceStats = {
  totalIncome: 1268880,
  totalExpense: 358420,
  netProfit: 910460,
  receivable: 236540,
  payable: 187320,
  incomeChange: 12.5,
  expenseChange: 8.3,
  profitChange: 15.7,
  receivableChange: -3.2,
  payableChange: -5.6
}

// 收支记录
export const mockTransactions: TransactionRecord[] = [
  { id: '1', code: 'R20250424001', type: 'income', account: '工商银行（1234）', relatedObject: '销售订单 SO20250424001', amount: 8650, transactionDate: '2025-04-24 14:30:22', remark: '黄金项链销售收款', createdAt: '2025-04-24' },
  { id: '2', code: 'R20250424002', type: 'expense', account: '建设银行（5678）', relatedObject: '供应商 深圳珠宝供应商', amount: 15200, transactionDate: '2025-04-24 11:20:15', remark: '采购原材料付款', createdAt: '2025-04-24' },
  { id: '3', code: 'R20250424003', type: 'income', account: '支付宝账户', relatedObject: '客户 张先生', amount: 3280, transactionDate: '2025-04-23 16:45:30', remark: '定制服务尾款', createdAt: '2025-04-23' },
  { id: '4', code: 'R20250424004', type: 'expense', account: '工商银行（1234）', relatedObject: '员工报销', amount: 560, transactionDate: '2025-04-23 09:15:45', remark: '差旅费报销', createdAt: '2025-04-23' },
  { id: '5', code: 'R20250424005', type: 'income', account: '微信支付', relatedObject: '客户 李女士', amount: 12800, transactionDate: '2025-04-22 15:20:10', remark: '钻戒销售收款', createdAt: '2025-04-22' },
  { id: '6', code: 'R20250422006', type: 'expense', account: '建设银行（5678）', relatedObject: '供应商 香港珠宝行', amount: 28500, transactionDate: '2025-04-22 10:00:00', remark: '翡翠原料采购', createdAt: '2025-04-22' },
  { id: '7', code: 'R20250422007', type: 'income', account: '工商银行（1234）', relatedObject: '销售订单 SO20250422001', amount: 6800, transactionDate: '2025-04-22 09:30:00', remark: '铂金手链销售', createdAt: '2025-04-22' },
]

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 财务 API
export const financeApi = {
  async getStats(): Promise<FinanceStats> {
    await delay(200)
    return { ...mockFinanceStats }
  },

  async getTransactions(params: TransactionQueryParams) {
    await delay(300)
    let filtered = [...mockTransactions]

    if (params.type) {
      filtered = filtered.filter(item => item.type === params.type)
    }
    if (params.startDate) {
      filtered = filtered.filter(item => item.transactionDate >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.transactionDate <= params.endDate!)
    }
    if (params.account) {
      filtered = filtered.filter(item => item.account.includes(params.account!))
    }
    if (params.relatedObject) {
      filtered = filtered.filter(item => item.relatedObject.includes(params.relatedObject!))
    }
    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.code.includes(params.keyword!) ||
        item.remark.includes(params.keyword!) ||
        item.relatedObject.includes(params.keyword!)
      )
    }

    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize

    return {
      list: filtered.slice(start, end),
      total: filtered.length,
      page: params.page,
      pageSize: params.pageSize
    }
  },

  async create(data: Partial<TransactionRecord>) {
    await delay(500)
    const newItem: TransactionRecord = {
      id: String(mockTransactions.length + 1),
      code: `R${Date.now()}`,
      type: data.type || 'income',
      account: data.account || '',
      relatedObject: data.relatedObject || '',
      amount: data.amount || 0,
      transactionDate: data.transactionDate || new Date().toISOString().replace('T', ' ').slice(0, 19),
      remark: data.remark || '',
      createdAt: new Date().toISOString().slice(0, 10)
    }
    mockTransactions.unshift(newItem)
    return newItem
  },

  async update(id: string, data: Partial<TransactionRecord>) {
    await delay(500)
    const index = mockTransactions.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTransactions[index] = { ...mockTransactions[index], ...data }
      return mockTransactions[index]
    }
    return null
  },

  async delete(id: string) {
    await delay(300)
    const index = mockTransactions.findIndex(item => item.id === id)
    if (index !== -1) {
      mockTransactions.splice(index, 1)
      return true
    }
    return false
  }
}
