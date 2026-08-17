// 采购管理相关类型定义

// 采购单状态
export type PurchaseStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

// 采购单状态映射
export const purchaseStatusMap: Record<PurchaseStatus, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'default' },
  SUBMITTED: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已通过', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
  CANCELLED: { text: '已取消', color: 'default' },
}

// 采购申请明细项
export interface PurchaseItem {
  id?: number
  orderId?: number
  productId?: number          // 商品ID（可选）
  productName: string         // 商品名称
  quantity: number            // 数量
  price: number               // 单价
}

// 采购单信息
export interface PurchaseRecord {
  id: number
  orderNo: string             // 采购单号
  storeId: number | null      // 门店ID
  supplierId: number | null   // 供应商ID
  applicantId: number | null  // 申请人ID
  approverId: number | null   // 审核人ID
  totalAmount: number         // 总金额
  status: PurchaseStatus      // 状态
  remark?: string             // 备注
  items?: PurchaseItem[]      // 采购明细
  createdAt: string
}

// 采购查询参数（后端仅分页，多余参数会被忽略）
export interface PurchaseQueryParams {
  purchaseNo?: string
  orderNo?: string
  status?: PurchaseStatus
  supplierId?: string
  startDate?: string
  endDate?: string
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
