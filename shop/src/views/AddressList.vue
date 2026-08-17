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
    <van-popup v-model:show="showForm" position="bottom" :style="{ height: '70%' }" round>
      <div class="popup-header">
        <span class="text-md" style="font-weight:600">{{ editingId ? '编辑地址' : '新增地址' }}</span>
      </div>
      <van-form @submit="onSave" style="padding:12px">
        <van-cell-group inset>
          <van-field v-model="form.receiverName" label="收货人" placeholder="请输入姓名" required />
          <van-field v-model="form.receiverPhone" label="手机号" placeholder="请输入手机号" required />
          <van-field v-model="form.province" label="省" placeholder="省份" />
          <van-field v-model="form.city" label="市" placeholder="城市" />
          <van-field v-model="form.district" label="区" placeholder="区县" />
          <van-field v-model="form.detailAddress" label="详细地址" placeholder="街道/门牌号" required />
        </van-cell-group>
        <div style="margin:16px;display:flex;gap:12px">
          <van-button round block type="primary" native-type="submit">保存</van-button>
          <van-button v-if="editingId" round block type="danger" @click="onDelete">删除</van-button>
        </div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { useAddressStore } from '@/stores/useAddressStore'
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
  detailAddress: ''
})

const formattedAddresses = computed(() =>
  addresses.value.map(a => ({
    id: String(a.id),
    name: a.receiverName || '',
    tel: a.receiverPhone || '',
    address: (a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || ''),
    isDefault: !!a.isDefault
  }))
)

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
  form.detailAddress = addr.detailAddress || ''
  showForm.value = true
}

function onSelect(item: any) {
  const addr = addresses.value.find(a => String(a.id) === item.id)
  if (addr) {
    addressStore.setDefault(addr.id)
    fetchData()
  }
}

async function onSave() {
  if (!form.receiverName || !form.receiverPhone || !form.detailAddress) {
    showToast('请填写必填信息')
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
  } catch { /* error handled */ }
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
  form.detailAddress = ''
}

onMounted(fetchData)
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
</style>
