// 场景运营 Mock 数据
import type { InspectionItem, MaterialItem, DeviceItem, SceneQueryParams, SceneStatus } from '@/types/scenario'

// 卫生巡检数据
export const mockInspections: InspectionItem[] = [
  { id: '1', code: 'HX202506170001', store: '万达广场店', inspector: '张三', inspectDate: '2025-06-17 10:30', checkItems: 28, issueCount: 2, score: 92, status: 'completed', createdAt: '2025-06-17' },
  { id: '2', code: 'HX202506160001', store: '万达广场店', inspector: '李四', inspectDate: '2025-06-16 10:30', checkItems: 28, issueCount: 1, score: 96, status: 'completed', createdAt: '2025-06-16' },
  { id: '3', code: 'HX202506150001', store: '万达广场店', inspector: '王五', inspectDate: '2025-06-15 10:30', checkItems: 28, issueCount: 3, score: 88, status: 'completed', createdAt: '2025-06-15' },
  { id: '4', code: 'HX202506140001', store: '万达广场店', inspector: '赵六', inspectDate: '2025-06-14 10:30', checkItems: 28, issueCount: 0, score: 100, status: 'completed', createdAt: '2025-06-14' },
  { id: '5', code: 'HX202506130001', store: '万达广场店', inspector: '张三', inspectDate: '2025-06-13 10:30', checkItems: 28, issueCount: 4, score: 84, status: 'completed', createdAt: '2025-06-13' },
]

// 陈列检查数据
export const mockDisplays: InspectionItem[] = [
  { id: '1', code: 'CL202506170001', store: '万达广场店', inspector: '张三', inspectDate: '2025-06-17 14:30', checkItems: 32, issueCount: 2, score: 94, status: 'completed', createdAt: '2025-06-17' },
  { id: '2', code: 'CL202506160001', store: '万达广场店', inspector: '李四', inspectDate: '2025-06-16 14:30', checkItems: 32, issueCount: 1, score: 97, status: 'completed', createdAt: '2025-06-16' },
  { id: '3', code: 'CL202506150001', store: '万达广场店', inspector: '王五', inspectDate: '2025-06-15 14:30', checkItems: 32, issueCount: 3, score: 90, status: 'completed', createdAt: '2025-06-15' },
  { id: '4', code: 'CL202506140001', store: '万达广场店', inspector: '赵六', inspectDate: '2025-06-14 14:30', checkItems: 32, issueCount: 0, score: 100, status: 'completed', createdAt: '2025-06-14' },
  { id: '5', code: 'CL202506130001', store: '万达广场店', inspector: '张三', inspectDate: '2025-06-13 14:30', checkItems: 32, issueCount: 2, score: 93, status: 'completed', createdAt: '2025-06-13' },
]

// 物料更新数据
export const mockMaterials: MaterialItem[] = [
  { id: '1', code: 'WL202506170001', store: '万达广场店', materialType: '宣传海报', updateDate: '2025-06-17 09:30', updater: '张三', updateCount: 12, status: 'completed', createdAt: '2025-06-17' },
  { id: '2', code: 'WL202506160001', store: '万达广场店', materialType: '价签', updateDate: '2025-06-16 09:30', updater: '李四', updateCount: 50, status: 'completed', createdAt: '2025-06-16' },
  { id: '3', code: 'WL202506150001', store: '万达广场店', materialType: '宣传单页', updateDate: '2025-06-15 09:30', updater: '王五', updateCount: 200, status: 'completed', createdAt: '2025-06-15' },
  { id: '4', code: 'WL202506140001', store: '万达广场店', materialType: '展架', updateDate: '2025-06-14 09:30', updater: '赵六', updateCount: 8, status: 'completed', createdAt: '2025-06-14' },
  { id: '5', code: 'WL202506130001', store: '万达广场店', materialType: '价签', updateDate: '2025-06-13 09:30', updater: '张三', updateCount: 50, status: 'completed', createdAt: '2025-06-13' },
]

// 设备检查数据
export const mockDevices: DeviceItem[] = [
  { id: '1', code: 'SB202506170001', store: '万达广场店', deviceType: '空调', checkDate: '2025-06-17 11:30', checker: '张三', issueCount: 0, status: 'completed', createdAt: '2025-06-17' },
  { id: '2', code: 'SB202506160001', store: '万达广场店', deviceType: '冷藏柜', checkDate: '2025-06-16 11:30', checker: '李四', issueCount: 1, status: 'completed', createdAt: '2025-06-16' },
  { id: '3', code: 'SB202506150001', store: '万达广场店', deviceType: '收银机', checkDate: '2025-06-15 11:30', checker: '王五', issueCount: 0, status: 'completed', createdAt: '2025-06-15' },
  { id: '4', code: 'SB202506140001', store: '万达广场店', deviceType: '照明设备', checkDate: '2025-06-14 11:30', checker: '赵六', issueCount: 2, status: 'completed', createdAt: '2025-06-14' },
  { id: '5', code: 'SB202506130001', store: '万达广场店', deviceType: '排风扇', checkDate: '2025-06-13 11:30', checker: '张三', issueCount: 1, status: 'completed', createdAt: '2025-06-13' },
]

// 状态映射
export const statusMap: Record<SceneStatus, { text: string; color: string }> = {
  completed: { text: '已完成', color: 'green' },
  pending: { text: '待处理', color: 'orange' },
  abnormal: { text: '异常', color: 'red' },
}

// 模拟 API 延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 通用列表 API 工厂
function createListApi<T extends { id: string }>(data: T[]) {
  return {
    async getList(params: SceneQueryParams) {
      await delay(300)
      let filtered = [...data]
      if (params.store) filtered = filtered.filter(item => (item as any).store === params.store)
      if (params.status) filtered = filtered.filter(item => (item as any).status === params.status)
      const start = (params.page - 1) * params.size
      return { list: filtered.slice(start, start + params.size), total: filtered.length, page: params.page, size: params.size }
    }
  }
}

export const healthApi = createListApi(mockInspections)
export const displayApi = createListApi(mockDisplays)
export const materialApi = createListApi(mockMaterials)
export const deviceApi = createListApi(mockDevices)

// 通用场景 API（别名，方便按模块导入）
export const sceneApi = healthApi
