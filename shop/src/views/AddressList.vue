<template>
  <div class="address-page page-container--no-tabbar">
    <van-nav-bar title="收货地址" left-text="返回" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="plus" size="20" @click="showForm = true" />
      </template>
    </van-nav-bar>

    <div v-if="addresses.length === 0 && !loading" class="text-center" style="padding:60px">
      <van-empty description="暂无收货地址" />
      <van-button type="primary" round @click="showForm = true">新增地址</van-button>
    </div>

    <van-address-list
      v-model="chosenAddressId"
      :list="formattedAddresses"
      default-tag-text="默认"
      @add="showForm = true"
      @edit="onEdit"
      @select="onSelect"
    />

    <!-- 新增/编辑弹窗 -->
    <van-popup v-model:show="showForm" position="bottom" :style="{ height: '80%' }" round>
      <div class="popup-header">
        <span class="text-md" style="font-weight:600">{{ editingId ? '编辑地址' : '新增地址' }}</span>
      </div>
      <van-form @submit="onSave" style="padding:12px">
        <van-cell-group inset>
          <van-field v-model="form.receiverName" label="收货人" placeholder="请输入姓名" required />
          <van-field v-model="form.receiverPhone" label="手机号" placeholder="请输入手机号" required />

          <!-- 所在地区：省市区 + 街道 一栏（点击弹三级下拉；定位后街道自动填入） -->
          <van-cell
            title="所在地区"
            is-link
            :value="regionText || '请选择'"
            :value-class="regionText ? '' : 'text-hint'"
            :border="false"
            @click="openRegionPicker"
          />

          <!-- 街道（点击下拉选择，定位自动填也可选） -->
          <van-cell
            title="街道"
            is-link
            :value="form.street || '请选择街道'"
            :value-class="form.street ? '' : 'text-hint'"
            :border="false"
            :disabled="!form.district"
            @click="openStreetPicker"
          />

          <!-- 详细地址：楼栋门牌号 -->
          <van-field
            v-model="form.detailAddress"
            label="详细地址"
            placeholder="楼栋、门牌号，如 5栋1203室"
            required
          />

          <!-- 定位填写 -->
          <van-cell title="地图定位选点" is-link :border="false" @click="openLocate">
            <template #icon>
              <van-icon name="location-o" color="#c8a44d" size="18" style="margin-right:6px" />
            </template>
            <template #value>
              <span v-if="located" class="text-xs" style="color:#52c41a">已定位，请核对街道</span>
              <span v-else class="text-xs text-hint">地图选点，自动填写省市区街道</span>
            </template>
          </van-cell>
        </van-cell-group>
        <div style="margin:16px;display:flex;gap:12px">
          <van-button round block type="primary" native-type="submit">保存</van-button>
          <van-button v-if="editingId" round block type="danger" @click="onDelete">删除</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- 省市区三级联动选择 -->
    <van-popup v-model:show="showRegionPicker" position="bottom" round :style="{ height: '45%' }">
      <van-area
        :area-list="areaList"
        :columns-num="3"
        v-model:model-value="regionCodes"
        title="选择省市区"
        @confirm="onRegionConfirm"
        @cancel="showRegionPicker = false"
      />
    </van-popup>

    <!-- 街道选择（淘宝式：按区县拉街道列表） -->
    <van-popup v-model:show="showStreetPicker" position="bottom" round :style="{ height: '45%' }">
      <div class="street-picker-header">
        <span class="text-md" style="font-weight:600">选择街道</span>
        <van-icon name="cross" size="18" @click="showStreetPicker = false" />
      </div>
      <div class="street-list">
        <div
          v-for="s in streetOptions"
          :key="s.code"
          class="street-item"
          :class="{ active: form.street === s.name }"
          @click="onStreetSelect(s)"
        >
          {{ s.name }}
          <van-icon v-if="form.street === s.name" name="success" color="#c8a44d" size="16" />
        </div>
        <van-empty v-if="!streetOptions.length" description="暂无街道数据" />
      </div>
    </van-popup>

    <!-- 地图定位选点弹窗 -->
    <van-popup v-model:show="showLocate" position="bottom" :style="{ height: '85%' }" round>
      <div class="locate-header">
        <span class="text-md" style="font-weight:600">定位选点</span>
        <van-icon name="cross" size="18" @click="showLocate = false" />
      </div>
      <LocatePicker
        v-if="showLocate"
        style="height: calc(100% - 44px)"
        @select="onLocateSelect"
        @close="showLocate = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import LocatePicker from '@/components/LocatePicker.vue'
import { useAddressStore } from '@/stores/useAddressStore'
import { addressApi } from '@/api/address'
import { cleanAddress } from '@/utils/amap'
import type { AddressItem } from '@/types'

const addressStore = useAddressStore()
const addresses = ref<AddressItem[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingId = ref<string | number | null>(null)
const chosenAddressId = ref<string>('')

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  street: '',
  detailAddress: '',
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined
})

// ---- 省市区联动 ----
const showRegionPicker = ref(false)
const areaList = ref<{ province_list: Record<string, string>; city_list: Record<string, string>; county_list: Record<string, string> }>({
  province_list: {}, city_list: {}, county_list: {}
})
const regionCodes = ref('')
const regionText = computed(() => [form.province, form.city, form.district, form.street].filter(Boolean).join(' '))
const located = ref(false)

/** 根据区县名称反查区县编码，作为 van-area 的 v-model 值（Vant4 只需最后一个 code） */
function buildRegionCodes() {
  if (!form.district) { regionCodes.value = ''; return }
  const findCode = (map: Record<string, string>, name: string) =>
    Object.keys(map || {}).find(k => map[k] === name) || ''
  regionCodes.value = findCode(areaList.value.county_list, form.district)
}

function openRegionPicker() {
  buildRegionCodes()
  showRegionPicker.value = true
}

// ---- 地图定位 ----
const showLocate = ref(false)

const formattedAddresses = computed(() =>
  addresses.value.map(a => ({
    id: String(a.id),
    name: a.receiverName || '',
    tel: a.receiverPhone || '',
    address: (a.province || '') + (a.city || '') + (a.district || '') + ((a as any).street || '') + (a.detailAddress || ''),
    isDefault: !!a.isDefault
  }))
)

async function loadAreaList() {
  try {
    const res = await addressApi.areaList()
    if (res) areaList.value = res
  } catch { /* 静默 */ }
}

async function fetchData() {
  loading.value = true
  try {
    await addressStore.fetchAddresses()
    addresses.value = addressStore.addresses as unknown as AddressItem[]
  } catch { /* ignore */ }
  finally { loading.value = false }
}

function onEdit(item: any) {
  const addr = addresses.value.find(a => String(a.id) === item.id)
  if (!addr) return
  editingId.value = addr.id
  form.receiverName = addr.receiverName || ''
  form.receiverPhone = addr.receiverPhone || ''
  form.province = addr.province || ''
  form.city = addr.city || ''
  form.district = addr.district || ''
  form.street = (addr as any).street || ''
  form.detailAddress = addr.detailAddress || ''
  form.longitude = addr.longitude != null ? Number(addr.longitude) : undefined
  form.latitude = addr.latitude != null ? Number(addr.latitude) : undefined
  located.value = !!(addr.longitude != null && addr.latitude != null)
  showForm.value = true
}

function onSelect(item: any) {
  const addr = addresses.value.find(a => String(a.id) === item.id)
  if (addr) {
    addressStore.setDefault(addr.id)
    fetchData()
  }
}

// ---- 街道选择 ----
const showStreetPicker = ref(false)
const streetOptions = ref<Array<{ name: string; code: string }>>([])
const streetLoading = ref(false)

/** 按当前区县编码拉街道列表 */
async function loadStreets(districtName: string) {
  if (!districtName) return
  streetLoading.value = true
  try {
    // 通过 areaList 反查区县编码，再请求街道
    const findCode = (map: Record<string, string>, name: string) =>
      Object.keys(map || {}).find(k => map[k] === name) || ''
    const code = findCode(areaList.value.county_list, districtName)
    if (!code) { streetOptions.value = []; return }
    const list = await addressApi.streets(code)
    streetOptions.value = list || []
  } catch {
    streetOptions.value = []
  } finally {
    streetLoading.value = false
  }
}

function openStreetPicker() {
  if (!form.district) { showToast('请先选择省市区'); return }
  loadStreets(form.district)
  showStreetPicker.value = true
}

function onStreetSelect(s: { name: string; code: string }) {
  form.street = s.name
  showStreetPicker.value = false
}

// ---- 省市区确认 ----
function onRegionConfirm(params: any) {
  // Vant4 confirm 事件参数为 { selectedValues, selectedOptions, selectedIndexes }
  // Area 的 option 结构为 { text, value, children }（text=名称, value=编码）
  const values = params?.selectedOptions || []
  const [prov, city, dist] = values
  const newProvince = prov?.text || ''
  const newCity = city?.text || ''
  const newDistrict = dist?.text || ''

  // 区县未变化（用户翻看后点确认）→ 直接关闭，不打断已有填写
  if (newProvince === form.province && newCity === form.city && newDistrict === form.district) {
    showRegionPicker.value = false
    return
  }

  form.province = newProvince
  form.city = newCity
  form.district = newDistrict
  // 区县变更：保留新选的省市区；街道按新区县重载；详细地址（门牌号）属于旧位置，清空重填
  form.street = ''
  form.detailAddress = ''
  showRegionPicker.value = false
  if (form.district) {
    loadStreets(form.district).then(() => {
      // 新区县街道加载完成后自动弹出选择，方便快速换街道
      showStreetPicker.value = true
    })
  }
}

// ---- 地图定位 ----
function openLocate() {
  showLocate.value = true
}

function onLocateSelect(data: { province: string; city: string; district: string; street?: string; detailAddress: string; lng: number; lat: number }) {
  form.province = data.province || form.province
  form.city = data.city || form.city
  form.district = data.district || form.district
  // 街道（淘宝式：省市区+街道一栏）——定位返回的街道/乡镇
  if (data.street) form.street = data.street
  // 详细地址：仅门牌号（交叉口/路口/距离描述一律丢弃，由用户手动补充楼栋门牌）
  form.detailAddress = cleanAddress(data.detailAddress) || form.detailAddress
  form.longitude = data.lng
  form.latitude = data.lat
  located.value = true
  showLocate.value = false
  // 兜底：POI 未带省市区/街道时（部分高德 POI 缺 pname/adname/township），用后端 regeo 补齐
  if ((!form.province || !form.district || !form.street) && data.lng && data.lat) {
    addressApi.regeo(data.lng, data.lat).then((res: any) => {
      if (res?.province) {
        form.province = res.province || form.province
        form.city = res.city || form.city
        form.district = res.district || form.district
        if (res.township) form.street = res.township
        // regeo 返回的详细地址已剥离街道，仅门牌，再清洗一次
        const cleaned = cleanAddress(res.detailAddress)
        if (cleaned) form.detailAddress = cleaned
      }
    }).catch(() => {})
  }
  showToast(form.detailAddress ? '已选点，请补充楼栋/门牌号' : '已选点，请填写详细地址与门牌号')
}

async function onSave() {
  if (!form.receiverName || !form.receiverPhone) {
    showToast('请填写收货人和手机号')
    return
  }
  if (!form.province || !form.city || !form.district) {
    showToast('请选择省市区')
    return
  }
  if (!form.detailAddress || form.detailAddress.trim().length < 5) {
    showToast('请填写详细地址（楼栋、门牌号）')
    return
  }
  try {
    if (editingId.value) {
      await addressStore.updateAddress(editingId.value, { ...form })
    } else {
      await addressStore.createAddress({ ...form, isDefault: addresses.value.length === 0 ? 1 : 0 })
    }
    showToast('保存成功')
    showForm.value = false
    editingId.value = null
    resetForm()
    await fetchData()
  } catch (e: any) {
    showToast(e?.message || '保存失败')
  }
}

async function onDelete() {
  if (!editingId.value) return
  try {
    await addressStore.deleteAddress(editingId.value)
    showToast('已删除')
    showForm.value = false
    editingId.value = null
    resetForm()
    await fetchData()
  } catch { /* error handled */ }
}

function resetForm() {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.street = ''
  form.detailAddress = ''
  form.longitude = undefined
  form.latitude = undefined
  located.value = false
}

onMounted(() => {
  fetchData()
  loadAreaList()
})
</script>

<style scoped>
.address-page { min-height: 100vh; background: var(--bg-page); }
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}
.locate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  height: 44px;
  box-sizing: border-box;
}

/* 街道选择器 */
.street-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.street-list {
  height: calc(100% - 45px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
.street-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f7f7f7;
  font-size: 14px;
  color: #333;
  cursor: pointer;
}
.street-item.active {
  color: #c8a44d;
  font-weight: 600;
}
</style>
