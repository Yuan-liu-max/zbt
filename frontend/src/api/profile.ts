import request from '@/utils/request'
import type { UserInfo, UserStats } from '@/types/profile'

export const profileApi = {
  getProfile: (): Promise<UserInfo> => request.get('/auth/me'),
  getUserInfo: (): Promise<UserInfo> => request.get('/auth/me'),
  getStats: (): Promise<UserStats> => request.get('/auth/stats'),
  updateProfile: (data: Partial<UserInfo>): Promise<void> => request.put('/auth/profile', data),
  changePassword: (data: { oldPassword: string; newPassword: string }): Promise<void> => request.put('/auth/password', data),
}
