import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  InspectionItem,
  DisplayInspection,
  MaterialItem,
  DeviceItem,
  SceneQueryParams,
} from '@/types/scenario'

export const sceneApi = {
  getList: (params: SceneQueryParams): Promise<PageResult<InspectionItem>> => request.get('/scenes/health-inspections', { params }),
  create: (data: Partial<InspectionItem>): Promise<InspectionItem> => request.post('/scenes/health-inspections', data),
  update: (id: number | string, data: Partial<InspectionItem>): Promise<InspectionItem> => request.put(`/scenes/health-inspections/${id}`, data),
  delete: (id: number | string): Promise<void> => request.delete(`/scenes/health-inspections/${id}`),
}

export const displayApi = {
  getList: (params: SceneQueryParams): Promise<PageResult<DisplayInspection>> => request.get('/scenes/display-inspections', { params }),
  create: (data: Partial<DisplayInspection>): Promise<DisplayInspection> => request.post('/scenes/display-inspections', data),
  update: (id: number | string, data: Partial<DisplayInspection>): Promise<DisplayInspection> => request.put(`/scenes/display-inspections/${id}`, data),
  delete: (id: number | string): Promise<void> => request.delete(`/scenes/display-inspections/${id}`),
}

export const materialApi = {
  getList: (params: SceneQueryParams): Promise<PageResult<MaterialItem>> => request.get('/scenes/material-updates', { params }),
  create: (data: Partial<MaterialItem>): Promise<MaterialItem> => request.post('/scenes/material-updates', data),
  update: (id: number | string, data: Partial<MaterialItem>): Promise<MaterialItem> => request.put(`/scenes/material-updates/${id}`, data),
  delete: (id: number | string): Promise<void> => request.delete(`/scenes/material-updates/${id}`),
}

export const deviceApi = {
  getList: (params: SceneQueryParams): Promise<PageResult<DeviceItem>> => request.get('/scenes/equipment-checks', { params }),
  create: (data: Partial<DeviceItem>): Promise<DeviceItem> => request.post('/scenes/equipment-checks', data),
  update: (id: number | string, data: Partial<DeviceItem>): Promise<DeviceItem> => request.put(`/scenes/equipment-checks/${id}`, data),
  delete: (id: number | string): Promise<void> => request.delete(`/scenes/equipment-checks/${id}`),
}
