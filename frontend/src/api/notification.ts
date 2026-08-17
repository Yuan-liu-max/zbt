import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { NotificationItem } from '@/types/profile'

export const notificationApi = {
  getUnreadCount: (): Promise<number> => request.get('/notifications/unread-count'),
  getCount: (): Promise<{ total: number; unread: number; read: number }> => request.get('/notifications/count'),
  getList: (params: { page?: number; pageSize?: number; isRead?: number }): Promise<PageResult<NotificationItem>> => request.get('/notifications', { params }),
  markAsRead: (id: string | number): Promise<void> => request.put(`/notifications/${id}/read`),
  markAllRead: (): Promise<void> => request.put('/notifications/read-all'),
}
