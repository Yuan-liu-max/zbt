// 通用类型定义

/** 分页查询参数 */
export interface PageParams {
  page: number
  pageSize: number
  keyword?: string
}

/** 分页响应结果 */
export interface PageResult<T> {
  list: T[]
  total: number
  page?: number
  pageSize?: number
  size?: number
}

/** 通用 API 响应 */
export interface ApiResult<T> {
  code: number
  data: T
  msg: string
}

/** 通用 ID 参数 */
export type IdType = string | number

/** 通用表单数据 */
export type FormData<T = Record<string, unknown>> = T
