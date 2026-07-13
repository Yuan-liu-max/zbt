// 日志管理 Mock 数据
import type { LogItem, LogQueryParams, LogResult } from '@/types/log'

// 日志数据
export const mockLogs: LogItem[] = [
  { id: '1', logTime: '2024-05-26 23:15:42', module: '商品管理', action: '新增商品', operator: '管理员', ip: '192.168.1.100', result: 'success', detail: '新增商品：18K金钻石戒指' },
  { id: '2', logTime: '2024-05-26 22:48:31', module: '订单管理', action: '发货处理', operator: '张三', ip: '192.168.1.101', result: 'success', detail: '订单SO202405260001已发货' },
  { id: '3', logTime: '2024-05-26 22:10:05', module: '用户管理', action: '编辑用户', operator: '李四', ip: '192.168.1.102', result: 'success', detail: '修改用户信息：张三' },
  { id: '4', logTime: '2024-05-26 21:33:17', module: '商品管理', action: '下架商品', operator: '管理员', ip: '192.168.1.100', result: 'warning', detail: '商品库存不足，已自动下架' },
  { id: '5', logTime: '2024-05-26 20:55:09', module: '内容管理', action: '发布文章', operator: '王五', ip: '192.168.1.103', result: 'success', detail: '发布文章：618促销活动' },
  { id: '6', logTime: '2024-05-26 20:12:38', module: '订单管理', action: '取消订单', operator: '赵六', ip: '192.168.1.104', result: 'failed', detail: '取消失败：订单已发货' },
  { id: '7', logTime: '2024-05-26 19:45:21', module: '用户管理', action: '重置密码', operator: '张三', ip: '192.168.1.101', result: 'success', detail: '重置用户李四的密码' },
  { id: '8', logTime: '2024-05-26 19:12:16', module: '系统管理', action: '修改角色', operator: '管理员', ip: '192.168.1.100', result: 'success', detail: '修改角色：运营管理员权限' },
  { id: '9', logTime: '2024-05-26 18:33:50', module: '营销管理', action: '创建活动', operator: '李四', ip: '192.168.1.102', result: 'success', detail: '创建营销活动：618大促' },
  { id: '10', logTime: '2024-05-26 17:58:07', module: '财务管理', action: '退款处理', operator: '王五', ip: '192.168.1.103', result: 'warning', detail: '退款金额超过限额，需人工审核' },
]

// 结果映射
export const resultMap: Record<LogResult, { text: string; color: string }> = {
  success: { text: '成功', color: 'green' },
  warning: { text: '警告', color: 'orange' },
  failed: { text: '失败', color: 'red' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 日志 API
export const logApi = {
  async getList(params: LogQueryParams) {
    await delay(300)
    let filtered = [...mockLogs]

    if (params.startDate) {
      filtered = filtered.filter(item => item.logTime >= params.startDate!)
    }
    if (params.endDate) {
      filtered = filtered.filter(item => item.logTime <= params.endDate!)
    }
    if (params.action) {
      filtered = filtered.filter(item => item.action.includes(params.action!))
    }
    if (params.operator) {
      filtered = filtered.filter(item => item.operator === params.operator)
    }
    if (params.keyword) {
      filtered = filtered.filter(item =>
        item.module.includes(params.keyword!) ||
        item.action.includes(params.keyword!) ||
        item.detail?.includes(params.keyword!)
      )
    }

    const start = (params.page - 1) * params.pageSize
    return {
      list: filtered.slice(start, start + params.pageSize),
      total: filtered.length,
      page: params.page,
      pageSize: params.pageSize
    }
  },

  async deleteBatch(ids: string[]) {
    await delay(500)
    ids.forEach(id => {
      const index = mockLogs.findIndex(item => item.id === id)
      if (index !== -1) mockLogs.splice(index, 1)
    })
    return { success: true, deletedCount: ids.length }
  }
}
