import request from '@/utils/request'

export const profileApi = {
  getProfile: (): Promise<any> => request.get('/auth/me'),
  getUserInfo: (): Promise<any> => request.get('/auth/me'),
  getStats: (): Promise<any> => request.get('/auth/stats'),
  updateProfile: (data: any): Promise<any> => request.put('/auth/profile', data),
  changePassword: (data: any): Promise<any> => request.put('/auth/password', data),
}
