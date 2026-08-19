/**
 * 收货地址 API
 */
import http from './index'
import type { AddressItem } from '@/types'

export const addressApi = {
  /** 获取地址列表 */
  list(): Promise<AddressItem[]> {
    return http.get('/addresses')
  },
  /** 新增地址 */
  create(data: Omit<AddressItem, 'id'>): Promise<AddressItem> {
    return http.post('/addresses', data)
  },
  /** 更新地址 */
  update(id: string | number, data: Partial<AddressItem>): Promise<void> {
    return http.put(`/addresses/${id}`, data)
  },
  /** 删除地址 */
  delete(id: string | number): Promise<void> {
    return http.delete(`/addresses/${id}`)
  },
  /** 设为默认地址 */
  setDefault(id: string | number): Promise<void> {
    return http.put(`/addresses/${id}/default`)
  },
  /** 逆地理编码：经纬度 → 省市区+街道+详细地址（后端代理高德） */
  regeo(lng: number, lat: number): Promise<{ province: string; city: string; district: string; township?: string; detailAddress: string; formattedAddress: string }> {
    return http.get('/addresses/regeo', { params: { lng, lat } })
  },
  /** 省市区字典（Vant Area 组件数据源，后端行政区字典表） */
  areaList(): Promise<{ province_list: Record<string, string>; city_list: Record<string, string>; county_list: Record<string, string> }> {
    return http.get('/regions/area-list')
  },
  /** 按区县查询街道/乡镇列表（淘宝式：省市区选定后街道下拉） */
  streets(districtCode: string | number): Promise<Array<{ name: string; code: string }>> {
    return http.get('/regions/streets', { params: { districtCode } })
  }
}
