/**
 * 门店 + 通知 + 营销 + AI + 收藏 + 购物车 API
 */
import http from './index'
import type { StoreItem, NotificationItem, PromotionItem, PageResult, ProductItem } from '@/types'

export const storeApi = {
  all(): Promise<StoreItem[]> {
    return http.get('/stores/all')
  }
}

export const notificationApi = {
  unreadCount(): Promise<number> {
    return http.get('/notifications/unread-count')
  },
  list(params: { page?: number; pageSize?: number; isRead?: number }): Promise<PageResult<NotificationItem>> {
    return http.get('/notifications', { params })
  },
  markRead(id: string | number): Promise<void> {
    return http.put(`/notifications/${id}/read`)
  },
  markAllRead(): Promise<void> {
    return http.put('/notifications/read-all')
  }
}

export const marketingApi = {
  getPromotions(params: { page?: number; pageSize?: number; status?: string }): Promise<PageResult<PromotionItem>> {
    return http.get('/promotions', { params })
  },
}

export const aiApi = {
  chat(question: string): Promise<{ reply: string; modelName?: string }> {
    return http.post('/ai/chat', { question })
  }
}

export const uploadApi = {
  upload(file: File): Promise<{ fileUrl: string; fileName: string }> {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

/** 收藏 API */
export const favoriteApi = {
  list(): Promise<any[]> {
    return http.get('/favorites')
  },
  add(productId: string | number): Promise<void> {
    return http.post('/favorites', { productId })
  },
  remove(productId: string | number): Promise<void> {
    return http.delete(`/favorites/${productId}`)
  },
  check(productId: string | number): Promise<boolean> {
    return http.get(`/favorites/check/${productId}`)
  }
}

/** 服务端购物车 API */
export const serverCartApi = {
  list(): Promise<any[]> {
    return http.get('/shop/cart')
  },
  add(productId: string | number, quantity?: number): Promise<void> {
    return http.post('/shop/cart', { productId, quantity: quantity || 1 })
  },
  updateQuantity(id: string | number, quantity: number): Promise<void> {
    return http.put(`/shop/cart/${id}`, { quantity })
  },
  toggleCheck(id: string | number, checked: number): Promise<void> {
    return http.put(`/shop/cart/${id}/check`, { checked })
  },
  checkAll(checked: number): Promise<void> {
    return http.put('/shop/cart/check-all', { checked })
  },
  remove(id: string | number): Promise<void> {
    return http.delete(`/shop/cart/${id}`)
  },
  removeChecked(): Promise<void> {
    return http.delete('/shop/cart')
  },
  sync(items: any[]): Promise<any[]> {
    return http.post('/shop/cart/sync', items)
  }
}

/** 商城订单 API */
export const shopOrderApi = {
  create(data: any): Promise<any> {
    return http.post('/shop/orders', data)
  },
  list(params: { page?: number; pageSize?: number; status?: string }): Promise<PageResult<any>> {
    return http.get('/shop/orders', { params })
  },
  detail(id: string | number): Promise<any> {
    return http.get(`/shop/orders/${id}`)
  },
  cancel(id: string | number): Promise<void> {
    return http.put(`/shop/orders/${id}/cancel`)
  },
  pay(id: string | number): Promise<any> {
    return http.put(`/shop/orders/${id}/pay`)
  },
  confirmReceive(id: string | number): Promise<void> {
    return http.put(`/shop/orders/${id}/confirm-receive`)
  },
  applyReturn(id: string | number, reason: string, refundAmount?: number): Promise<any> {
    return http.post(`/shop/orders/${id}/apply-return`, { reason, refundAmount })
  }
}
