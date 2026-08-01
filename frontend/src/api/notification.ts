import request from '@/utils/request'

export const notificationApi = {
  getUnreadCount: (): Promise<any> => request.get('/notifications/unread-count'),
  getMessageUnreadCount: (): Promise<any> => request.get('/messages/unread-count'),
  getList: (params: any): Promise<any> => request.get('/notifications', { params }),
  markAsRead: (id: string): Promise<any> => request.put(`/notifications/${id}/read`),
  markAllRead: (): Promise<any> => request.put('/notifications/read-all'),
}
