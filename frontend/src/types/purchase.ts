// 采购管理相关类型定义

// 采购单状态
export type PurchaseStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

// 采购单状态映射
export const purchaseStatusMap: Record<PurchaseStatus, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已通过', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
  CANCELLED: { text: '已取消', color: 'default' },
}

// 采购申请明细项
export interface PurchaseItem {
  id?: string
  productName: string       // 商品名称
  spec: string              // 规格
  quantity: number          // 数量
  unitPrice: number         // 单价
  subtotal: number          // 小计（自动计算）
  remark?: string           // 备注
}

// 采购单信息
export interface PurchaseRecord {
  id: string
  purchaseNo: string         // 采购单号
  supplierId: string         // 供应商ID
  supplierName: string       // 供应商名称
  applicantId: string        // 申请人ID
  applicantName: string      // 申请人姓名
  applyDate: string          // 申请日期
  totalAmount: number        // 总金额
  status: PurchaseStatus     // 状态
  remark?: string            // 备注
  auditRemark?: string       // 审核意见
  auditorId?: string         // 审核人ID
  auditorName?: string       // 审核人姓名
  auditTime?: string         // 审核时间
  items: PurchaseItem[]      // 采购明细
  createdAt: string
  updatedAt?: string
}

// 采购查询参数
export interface PurchaseQueryParams {
  purchaseNo?: string        // 采购单号
  status?: PurchaseStatus    // 状态
  supplierId?: string        // 供应商
  startDate?: string         // 开始日期
  endDate?: string           // 结束日期
  page: number
  pageSize: number
}

// 审核参数
export interface AuditParams {
  status: 'APPROVED' | 'REJECTED'
  auditRemark: string
}

// 分页响应
export interface PurchasePaginatedResponse<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
