// 场景运营相关类型定义

// 通用状态
export type SceneStatus = 'completed' | 'pending' | 'abnormal'

// 巡检/检查记录
export interface InspectionItem {
  id: string
  code: string              // 编号
  store: string             // 门店
  inspector: string         // 巡检人/检查人
  inspectDate: string       // 巡检/检查日期
  checkItems: number        // 巡检/检查项数
  issueCount: number        // 问题数
  score: number             // 得分
  status: SceneStatus       // 状态
  createdAt: string
}

// 物料更新记录
export interface MaterialItem {
  id: string
  code: string              // 更新编号
  store: string             // 门店
  materialType: string      // 物料类型
  updateDate: string        // 更新日期
  updater: string           // 更新人
  updateCount: number       // 更新数量
  status: SceneStatus       // 状态
  createdAt: string
}

// 设备检查记录
export interface DeviceItem {
  id: string
  code: string              // 检查编号
  store: string             // 门店
  deviceType: string        // 设备类型
  checkDate: string         // 检查日期
  checker: string           // 检查人
  issueCount: number        // 问题数
  status: SceneStatus       // 状态
  createdAt: string
}

// 通用查询参数
export interface SceneQueryParams {
  store?: string
  startDate?: string
  endDate?: string
  status?: SceneStatus
  page: number
  pageSize: number
}
