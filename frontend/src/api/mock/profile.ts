// 个人中心 Mock 数据
import type { NotificationItem, MessageItem, UserInfo, UserStats } from '@/types/profile'

// 提醒数据
export const mockNotifications: NotificationItem[] = [
  { id: '1', type: 'system', title: '系统通知', content: '您的店铺"优选生活馆"有新的订单，请及时处理。', time: '10:23', isRead: false },
  { id: '2', type: 'order', title: '订单提醒', content: '订单号123456789已支付成功，等待您发货。', time: '09:45', isRead: false },
  { id: '3', type: 'customer', title: '客服消息', content: '有新的客户咨询：请问这款产品还有库存吗？', time: '昨天 18:30', isRead: false },
  { id: '4', type: 'activity', title: '活动通知', content: '双11大促活动即将开始，点击查看活动详情。', time: '昨天 12:15', isRead: true },
  { id: '5', type: 'system', title: '系统通知', content: '您的店铺信息已通过审核。', time: '11-05 16:20', isRead: true },
  { id: '6', type: 'order', title: '订单提醒', content: '订单号987654321已发货，点击查看物流详情。', time: '11-05 10:10', isRead: true },
]

// 信息数据
export const mockMessages: MessageItem[] = [
  { id: '1', type: 'system', title: '系统消息', summary: '店铺保证金即将到期，请及时续费。', content: '您好，您的店铺保证金即将于3天后到期，为避免影响店铺正常经营，请及时前往财务中心完成续费操作。\n\n到期时间：2024-11-09\n当前余额：¥1000.00', time: '10:23', isRead: false, extra: '¥1000.00' },
  { id: '2', type: 'platform', title: '平台通知', summary: '双11大促活动规则更新通知', content: '双11大促活动规则已更新，请仔细阅读新的活动规则，确保店铺活动符合平台要求。', time: '昨天 16:30', isRead: false },
  { id: '3', type: 'customer', title: '客服消息', summary: '关于商品售后问题的处理进度', content: '客户反馈的商品质量问题已处理完毕，请及时跟进客户满意度。', time: '11-05', isRead: true },
  { id: '4', type: 'assistant', title: '运营小助手', summary: '本周店铺运营数据周报', content: '本周店铺运营数据已生成，请查看详细报告。', time: '11-04', isRead: true },
  { id: '5', type: 'operation', title: '系统消息', summary: '您的店铺信息已修改成功', content: '店铺信息已成功更新。', time: '11-03', isRead: true },
]

// 用户信息
export const mockUserInfo: UserInfo = {
  id: '10001',
  username: '管理员',
  role: '超级管理员',
  phone: '138****8888',
  email: '123****@qq.com',
  timezone: '(UTC+08:00) 北京、重庆、香港特别行政区、乌鲁木齐',
  language: '简体中文',
  dateFormat: 'YYYY-MM-DD',
  registeredAt: '2023-05-01 10:00:00',
  lastLoginAt: '2024-11-06 10:30:00',
}

// 用户统计
export const mockUserStats: UserStats = {
  productCount: 128,
  orderCount: 856,
  todayVisitors: 1234,
  todaySales: 12345.67,
}

// 提醒类型映射
export const notificationTypeMap: Record<string, { icon: string; color: string }> = {
  system: { icon: 'BellOutlined', color: '#ff4d4f' },
  order: { icon: 'ShoppingOutlined', color: '#fa8c16' },
  customer: { icon: 'CustomerServiceOutlined', color: '#52c41a' },
  activity: { icon: 'ThunderboltOutlined', color: '#1890ff' },
  operation: { icon: 'SettingOutlined', color: '#722ed1' },
}

// 信息类型映射
export const messageTypeMap: Record<string, { icon: string; color: string }> = {
  system: { icon: 'UserOutlined', color: '#1890ff' },
  platform: { icon: 'GlobalOutlined', color: '#52c41a' },
  customer: { icon: 'CustomerServiceOutlined', color: '#fa8c16' },
  assistant: { icon: 'RobotOutlined', color: '#722ed1' },
  operation: { icon: 'SettingOutlined', color: '#666' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 提醒 API
export const notificationApi = {
  async getList(params: { tab?: string; page: number; pageSize: number }) {
    await delay(300)
    let filtered = [...mockNotifications]
    if (params.tab === 'unread') filtered = filtered.filter(item => !item.isRead)
    if (params.tab === 'read') filtered = filtered.filter(item => item.isRead)
    const start = (params.page - 1) * params.pageSize
    return { list: filtered.slice(start, start + params.pageSize), total: filtered.length }
  },
  async markAllRead() {
    await delay(300)
    mockNotifications.forEach(item => item.isRead = true)
    return { success: true }
  }
}

// 信息 API
export const messageApi = {
  async getList() {
    await delay(300)
    return [...mockMessages]
  }
}

// 用户 API
export const profileApi = {
  async getUserInfo(): Promise<UserInfo> {
    await delay(200)
    return { ...mockUserInfo }
  },
  async updateProfile(data: Partial<UserInfo>) {
    await delay(500)
    Object.assign(mockUserInfo, data)
    return { ...mockUserInfo }
  },
  async getStats(): Promise<UserStats> {
    await delay(200)
    return { ...mockUserStats }
  }
}
