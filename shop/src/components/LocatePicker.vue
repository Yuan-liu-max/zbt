<template>
  <div class="locate-picker">
    <!-- 顶部搜索框 -->
    <div class="locate-search">
      <div class="search-input">
        <van-icon name="search" size="16" color="#999" />
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索地点（如：万达广场、公司名）"
          @keyup.enter="doSearch"
        />
        <van-icon v-if="keyword" name="clear" size="14" color="#ccc" @click="keyword = ''" />
      </div>
      <span class="search-btn" @click="doSearch">搜索</span>
    </div>

    <!-- 地图画布 -->
    <div ref="mapContainer" class="locate-map">
      <div class="center-marker">
        <div class="center-dot"></div>
      </div>
    </div>

    <!-- 定位按钮 -->
    <div class="locate-btn" @click="locateMe">
      <van-icon name="aim" size="20" :color="locating ? '#c8a44d' : '#333'" />
    </div>

    <!-- POI 列表 -->
    <div class="poi-panel">
      <div class="poi-panel__header">
        <span class="text-sm" style="font-weight:600">{{ pois.length ? '附近地点' : '选择地点' }}</span>
        <span v-if="centerText" class="text-xs text-hint">{{ centerText }}</span>
      </div>
      <div class="poi-list">
        <div
          v-for="poi in pois"
          :key="poi.id"
          class="poi-item"
          :class="{ active: selectedId === poi.id }"
          @click="selectPoi(poi)"
        >
          <div class="poi-item__info">
            <p class="poi-name">{{ poi.name }}</p>
            <p class="poi-address">{{ poi.address || poi.pname + poi.cityname + poi.adname }}</p>
          </div>
          <div class="poi-item__right">
            <span class="poi-distance">{{ formatDistance(poi.distance) }}</span>
            <van-button
              size="mini"
              type="primary"
              round
              :disabled="selectedId === poi.id"
              @click.stop="confirmPoi(poi)"
            >{{ selectedId === poi.id ? '已选' : '使用' }}</van-button>
          </div>
        </div>
        <div v-if="!pois.length && !loadingPois" class="poi-empty">
          <p class="text-sm text-hint">暂无周边地点，可搜索或点击"使用当前位置"</p>
        </div>
        <van-loading v-if="loadingPois" class="poi-loading" size="20">加载中...</van-loading>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { showToast } from 'vant'
import { loadAMap, formatDistance, cleanAddress, type LngLat } from '@/utils/amap'

const emit = defineEmits<{
  (e: 'select', data: { province: string; city: string; district: string; detailAddress: string; lng: number; lat: number }): void
  (e: 'close'): void
}>()

const mapContainer = ref<HTMLElement | null>(null)
const keyword = ref('')
const pois = ref<any[]>([])
const selectedId = ref<string | null>(null)
const loadingPois = ref(false)
const locating = ref(false)
const centerText = ref('')

let map: any = null
let geolocation: any = null
let placeSearch: any = null
let marker: any = null
let currentPos: LngLat = { lng: 116.397428, lat: 39.90923 } // 默认北京

// 周边搜索防抖定时器（减少拖动/缩放过程中的高频请求）
let searchTimer: any = null

onMounted(async () => {
  try {
    const AMap = await loadAMap()
    initMap(AMap)
    locateMe()
  } catch (e: any) {
    showToast(e?.message || '地图加载失败')
  }
})

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer)
  if (map) { try { map.destroy() } catch {} }
})

function initMap(AMap: any) {
  if (!mapContainer.value) return
  map = new AMap.Map(mapContainer.value, {
    zoom: 16,
    center: [currentPos.lng, currentPos.lat],
    viewMode: '2D',
  })
  geolocation = new AMap.Geolocation({
    enableHighAccuracy: true,
    timeout: 10000,
    zoomToAccuracy: true,
  })
  map.addControl(geolocation)
  placeSearch = new AMap.PlaceSearch({
    pageSize: 20,
    pageIndex: 1,
    citylimit: false,
  })
  marker = new AMap.Marker({
    position: [currentPos.lng, currentPos.lat],
    icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
  })
  map.add(marker)
  // 减少周边搜索请求：
  // 1. 用 dragend 代替 moveend —— 过滤定位/选POI等程序化 setCenter 触发的多余搜索
  // 2. 防抖 —— 连续拖动/缩放只搜最后一次（停手 500ms 后）
  // 3. 缩放级别限制 —— 缩到省级/全国视图(zoom<12)不搜，避免无效请求
  map.on('dragend', debouncedSearchAround)
  map.on('zoomend', debouncedSearchAround)
}

/** 防抖包装：连续触发只执行最后一次 */
function debouncedSearchAround() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => searchAround(), 500)
}

/** 定位到当前位置 */
function locateMe() {
  if (!geolocation) return
  locating.value = true
  geolocation.getCurrentPosition((status: string, result: any) => {
    locating.value = false
    if (status === 'complete' && result?.position) {
      const { lng, lat } = result.position
      currentPos = { lng, lat }
      map.setCenter([lng, lat])
      marker.setPosition([lng, lat])
      showToast('定位成功')
      // 定位是主动操作，直接立即搜索（不过防抖）
      searchAround()
    } else {
      showToast('定位失败，请检查定位权限')
      searchAround()
    }
  })
}

/** 以地图中心搜索周边 POI */
function searchAround() {
  if (!placeSearch || !map) return
  // 缩放级别限制：省级/全国视图下周边搜索无意义，跳过
  if (map.getZoom() < 12) {
    pois.value = []
    centerText.value = '地图范围过大，请放大后查看周边'
    return
  }
  const center = map.getCenter()
  if (!center) return
  currentPos = { lng: center.getLng(), lat: center.getLat() }
  centerText.value = ''
  loadingPois.value = true
  placeSearch.searchNearBy(keyword.value || '', [currentPos.lng, currentPos.lat], 3000, (status: string, result: any) => {
    loadingPois.value = false
    if (status === 'complete' && result?.poiList?.pois?.length) {
      pois.value = result.poiList.pois
      centerText.value = `${formatDistance(0)}·周边 ${pois.value.length} 个地点`
    } else {
      pois.value = []
      centerText.value = ''
    }
  })
}

/** 关键字搜索 */
function doSearch() {
  if (!keyword.value.trim()) { searchAround(); return }
  if (!placeSearch) return
  loadingPois.value = true
  placeSearch.search(keyword.value.trim(), (status: string, result: any) => {
    loadingPois.value = false
    if (status === 'complete' && result?.poiList?.pois?.length) {
      const first = result.poiList.pois[0]
      const lnglat = first.location
      map.setCenter([lnglat.getLng(), lnglat.getLat()])
      marker.setPosition([lnglat.getLng(), lnglat.getLat()])
      pois.value = result.poiList.pois
    } else {
      showToast('未找到相关地点')
    }
  })
}

function selectPoi(poi: any) {
  selectedId.value = poi.id
  const lnglat = poi.location
  if (lnglat) {
    map.setCenter([lnglat.getLng(), lnglat.getLat()])
    marker.setPosition([lnglat.getLng(), lnglat.getLat()])
  }
}

/** 选中 POI：回填省市区+街道+门牌（街道优先用后端 regeo 解析，POI 缺 township 字段） */
function confirmPoi(poi: any) {
  const lnglat = poi.location
  const lng = lnglat?.getLng?.() ?? (Array.isArray(lnglat) ? lnglat[0] : currentPos.lng)
  const lat = lnglat?.getLat?.() ?? (Array.isArray(lnglat) ? lnglat[1] : currentPos.lat)

  // POI 自带省市区（高德 place 返回 pname/cityname/adname）
  const province = poi.pname || ''
  const city = poi.cityname || province
  const district = poi.adname || ''
  // 详细地址：仅取清洗后的街道门牌（"XX路XX号"），过滤交叉口/路口/距离描述
  const detailAddress = cleanAddress(poi.address)

  emit('select', { province, city, district, detailAddress, lng, lat })
}
</script>

<style scoped>
.locate-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

/* 搜索框 */
.locate-search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f5f5f5;
  border-radius: 18px;
  padding: 8px 12px;
}
.search-input input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
}
.search-btn {
  color: #c8a44d;
  font-size: 14px;
  font-weight: 500;
  flex-shrink: 0;
}

/* 地图 */
.locate-map {
  position: relative;
  height: 42%;
  min-height: 200px;
  flex-shrink: 0;
}
.center-marker {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
  z-index: 10;
  pointer-events: none;
}
.center-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #c8a44d;
  border: 3px solid rgba(200, 164, 77, 0.3);
  box-shadow: 0 0 0 1px #fff;
}

/* 定位按钮 */
.locate-btn {
  position: absolute;
  right: 12px;
  top: 42%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

/* POI 列表 */
.poi-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.poi-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #f5f5f5;
}
.poi-list {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.poi-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid #f7f7f7;
  cursor: pointer;
}
.poi-item.active { background: #faf6ee; }
.poi-item__info { flex: 1; min-width: 0; }
.poi-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.poi-address {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.poi-item__right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.poi-distance {
  font-size: 12px;
  color: #666;
}
.poi-empty {
  text-align: center;
  padding: 40px 0;
}
.poi-loading {
  justify-content: center;
  padding: 24px 0;
}
</style>
