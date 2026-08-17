import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { OrgNode, UserItem, RoleItem, PermissionItem, SysPermissionNode, SystemConfigItem } from '@/types/system'

export const orgApi = {
  getTree: (): Promise<OrgNode[]> => request.get('/organizations/tree'),
  getList: (): Promise<PageResult<OrgNode>> => request.get('/organizations'),
  create: (data: Partial<OrgNode>): Promise<OrgNode> => request.post('/organizations', data),
  update: (id: string, data: Partial<OrgNode>): Promise<OrgNode> => request.put(`/organizations/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/organizations/${id}`),
}

export const userApi = {
  getList: (params: { keyword?: string; username?: string; phone?: string; status?: string; roleId?: number; page?: number; pageSize?: number }): Promise<PageResult<UserItem>> => request.get('/users', { params }),
  create: (data: Partial<UserItem>): Promise<UserItem> => request.post('/users', data),
  update: (id: string, data: Partial<UserItem>): Promise<UserItem> => request.put(`/users/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/users/${id}`),
  forceLogout: (id: string): Promise<void> => request.put(`/users/${id}/force-logout`),
}

export const roleApi = {
  getList: (): Promise<PageResult<RoleItem>> => request.get('/roles'),
  create: (data: Partial<RoleItem>): Promise<RoleItem> => request.post('/roles', data),
  update: (id: string, data: Partial<RoleItem>): Promise<RoleItem> => request.put(`/roles/${id}`, data),
  delete: (id: string): Promise<void> => request.delete(`/roles/${id}`),
  assignPermissions: (id: string, permIds: string[]): Promise<void> => request.put(`/roles/${id}/permissions`, { permissionIds: permIds }),
  getPermissions: (id: string): Promise<PermissionItem[]> => request.get(`/roles/${id}/permissions`),
}

export const permApi = {
  getTree: (): Promise<SysPermissionNode[]> => request.get('/permissions/tree'),
  getList: (): Promise<PermissionItem[]> => request.get('/permissions'),
}

export const configApi = {
  getList: (group?: string): Promise<SystemConfigItem[]> => request.get('/system/configs', { params: { configGroup: group } }),
  getConfig: (group?: string): Promise<SystemConfigItem[]> => request.get('/system/configs', { params: { configGroup: group } }),
  save: (configs: SystemConfigItem[]): Promise<void> => request.put('/system/configs', { configs }),
  saveConfig: (configs: SystemConfigItem[]): Promise<void> => request.put('/system/configs', { configs }),
}
