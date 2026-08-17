/**
 * 认证 API
 */
import http from './index'
import type { UserInfo, LoginPayload, RegisterPayload } from '@/types'

export const authApi = {
  /** C端商城登录（走商城专用端点，设置 zbt_shop_token Cookie） */
  login(data: LoginPayload): Promise<{ token: string } & UserInfo> {
    return http.post('/shop/auth/login', data)
  },
  /** C端商城注册 */
  register(data: RegisterPayload): Promise<{ token: string } & UserInfo> {
    return http.post('/shop/auth/register', data)
  },
  /** 获取当前用户信息（走商城专用端点，避免与管理端 Cookie 串号） */
  getMe(): Promise<UserInfo> {
    return http.get('/shop/auth/me')
  },
  /** 登出（清除 zbt_shop_token Cookie） */
  logout(): Promise<void> {
    return http.post('/shop/auth/logout')
  },
  /** 更新个人信息 */
  updateProfile(data: Record<string, any>): Promise<void> {
    return http.put('/shop/auth/profile', data)
  },
  /** 修改密码 */
  changePassword(oldPassword: string, newPassword: string): Promise<void> {
    return http.put('/shop/auth/password', { oldPassword, newPassword })
  },
  /** 注销账号（软禁用） */
  deactivate(): Promise<void> {
    return http.delete('/shop/auth/account')
  },
  /** 获取用户统计数据（订单数、收藏数、优惠券数） */
  getStats(): Promise<{ orderCount: number; favoriteCount: number; couponCount: number }> {
    return http.get('/shop/auth/stats')
  }
}
