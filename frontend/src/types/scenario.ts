// 场景运营相关类型定义

// 卫生巡检记录
export interface InspectionItem {
  id: number
  inspectionTime: string      // 巡检时段
  inspectionDate: string      // 巡检日期
  inspectorId: number | null  // 巡检人ID
  storeId: number | null      // 门店ID
  areaResults: string         // 区域检查结果
  issueDescription: string    // 问题描述
  photoUrls: string           // 照片地址
  rectificationRequired: number // 是否需要整改 0/1
  createdAt: string
}

// 陈列检查记录
export interface DisplayInspection {
  id: number
  inspectionDate: string      // 检查日期
  storeId: number | null      // 门店ID
  inspectorId: number | null  // 检查人ID
  displayArea: string         // 陈列区域
  standardScore: number | null // 标准得分
  issueDescription: string    // 问题描述
  beforePhotos: string        // 整改前照片
  afterPhotos: string         // 整改后照片
  rectificationPlan: string   // 整改计划
  createdAt: string
}

// 物料更新记录
export interface MaterialItem {
  id: number
  storeId: number | null      // 门店ID
  checkerId: number | null    // 检查人ID
  checkDate: string           // 检查日期
  materialType: string        // 物料类型
  currentStatus: string       // 当前状态
  updatedPhotos: string       // 更新照片
  issueDescription: string    // 问题描述
  replacementRequired: number // 是否需要更换 0/1
  createdAt: string
}

// 设备检查记录
export interface DeviceItem {
  id: number
  storeId: number | null      // 门店ID
  checkerId: number | null    // 检查人ID
  checkDate: string           // 检查日期
  equipmentType: string       // 设备类型
  status: string              // 状态
  issueDescription: string    // 问题描述
  repairRequired: number      // 是否需要维修 0/1
  photoUrls: string           // 照片地址
  createdAt: string
}

// 通用查询参数
export interface SceneQueryParams {
  store?: string         // 门店ID
  startDate?: string     // 开始日期 YYYY-MM-DD
  endDate?: string       // 结束日期 YYYY-MM-DD
  status?: string        // 状态（设备检查 NORMAL/ABNORMAL）
  currentStatus?: string // 当前状态（物料更新 NORMAL/EXPIRED/DAMAGED/MISSING）
  page: number
  pageSize: number
}
