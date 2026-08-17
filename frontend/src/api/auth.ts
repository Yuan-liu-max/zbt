import request from '@/utils/request'

export interface LoginResult {
  token: string
  userId: number
  username: string
  realName?: string
  avatar?: string
  storeId?: number
  regionId?: number
  roles?: string[]
  permissions?: string[]
}

export const authApi = {
  login: (username: string, password: string): Promise<LoginResult> =>
    request.post('/auth/login', { username, password }),
  register: (username: string, password: string, phone?: string): Promise<LoginResult> =>
    request.post('/auth/register', { username, password, phone }),
  me: (): Promise<LoginResult> => request.get('/auth/me'),
  logout: (): Promise<void> => request.post('/auth/logout'),
  updateProfile: (data: Record<string, any>): Promise<void> => request.put('/auth/profile', data),
  changePassword: (oldPassword: string, newPassword: string): Promise<void> =>
    request.put('/auth/password', { oldPassword, newPassword }),
}
