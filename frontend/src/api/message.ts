import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { MessageItem } from '@/types/profile'

export const messageApi = {
  getUnreadCount: (): Promise<number> => request.get('/messages/unread-count'),
  getList: (params: { page?: number; pageSize?: number; isRead?: number }): Promise<PageResult<MessageItem>> => request.get('/messages', { params }),
  markAsRead: (id: string | number): Promise<void> => request.put(`/messages/${id}/read`),
  markAllRead: (): Promise<void> => request.put('/messages/read-all'),
  send: (data: { receiverId: number; title: string; content?: string }): Promise<MessageItem> => request.post('/messages', data),
}
