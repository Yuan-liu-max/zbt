/**
 * 高德地图 JS API 加载器（V2.0）
 *
 * 用法：
 *   const AMap = await loadAMap()
 *   const map = new AMap.Map('container', {...})
 *
 * 安全密钥 securityJsCode 通过 window._AMapSecurityConfig 注入
 * （个人开发/内网可用；生产环境建议改用服务端代理方式）
 */
declare global {
  interface Window {
    _AMapSecurityConfig?: { securityJsCode: string }
    AMap: any
  }
}

let amapPromise: Promise<any> | null = null

export const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || '4c6dd1f99baf916591a116b31aa49638'
export const AMAP_SECURITY_CODE = import.meta.env.VITE_AMAP_SECURITY_CODE || '54f67460d395d95a2f7aff0373c6fdd9'

/** 动态加载高德 JS API，返回 AMap 全局对象（幂等，可多次调用） */
export function loadAMap(): Promise<any> {
  if (amapPromise) return amapPromise
  amapPromise = new Promise((resolve, reject) => {
    if (window.AMap) {
      resolve(window.AMap)
      return
    }
    // 注入安全密钥
    window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_CODE }
    // 需要的插件：定位 + POI 搜索 + 输入提示
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}&plugin=AMap.Geolocation,AMap.PlaceSearch,AMap.AutoComplete`
    script.async = true
    script.onload = () => {
      if (window.AMap) resolve(window.AMap)
      else reject(new Error('高德地图加载失败：AMap 未初始化'))
    }
    script.onerror = () => {
      amapPromise = null
      reject(new Error('高德地图 JS API 加载失败，请检查网络或 Key 配置'))
    }
    document.head.appendChild(script)
  })
  return amapPromise
}

/** 经纬度坐标点 */
export interface LngLat {
  lng: number
  lat: number
}

/** 格式化距离（米 → 中文可读） */
export function formatDistance(meters?: number): string {
  if (meters == null || isNaN(meters)) return ''
  if (meters < 1000) return `${Math.round(meters)}m`
  return `${(meters / 1000).toFixed(1)}km`
}

/**
 * 清洗 POI 详细地址，过滤无效的交叉口/路口/距离描述。
 *
 * 高德 PlaceSearch 的 address 字段常为"XX路与XX路交叉口"、"距XX米"等描述性文本，
 * 直接入库会导致无法派送的无效收货地址。规则：
 * 1. 含 交叉口/路口/十字/三岔/环岛 等 → 视为交叉口描述，整段丢弃
 * 2. 含 距离/米 等距离描述 → 丢弃
 * 3. 仅保留 街道+门牌 形态（如"天河路123号"、"建设大道88号"）
 * 4. 清洗后仍无有效门牌 → 返回空字符串，由顾客手动补充楼栋/门牌号
 */
export function cleanAddress(text?: string): string {
  if (!text) return ''
  // 去空白，压缩连续空格
  let addr = text.trim().replace(/\s+/g, '')
  // 交叉口/路口/环岛类描述 → 无效
  if (/(交叉口|路口|十字|三岔|五岔|环岛|立交桥|匝道)/.test(addr)) return ''
  // 距离描述（如"距XX米""距离XX米""XX米处"）→ 无效
  if (/(距|距离|约).{0,8}(米|公里)/.test(addr)) return ''
  // 纯距离/纯描述（如"100米"、"附近"）→ 无效
  if (/^\d+(\.\d+)?(米|公里)$/.test(addr)) return ''
  if (/^(附近|周边|内)/.test(addr)) return ''
  // 提取 街道名+门牌 形态：如 "天河路123号" / "建设大道88号" / "学院路"（无门牌也保留街道）
  const m = addr.match(/[\u4e00-\u9fa5A-Za-z0-9]+(路|街|道|巷|大道|大街|公路|弄|里)[\u4e00-\u9fa5A-Za-z0-9]*(号|栋|幢|楼|室|单元)?/)
  if (!m) return ''
  const street = m[0]
  // 街道名至少2个字（避免"1路"这类伪街道）
  const streetName = street.match(/[\u4e00-\u9fa5A-Za-z]{2,}/)
  if (!streetName) return ''
  return street
}

