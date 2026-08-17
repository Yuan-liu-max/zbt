<template>
  <div class="checkout-page page-container--no-tabbar">
    <van-nav-bar title="确认订单" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- 收货地址 -->
    <div class="address-section" @click="showAddressPicker = true">
      <template v-if="selectedAddress">
        <div class="address-card">
          <div class="address-card__icon"><van-icon name="location-o" size="22" color="#c8a44d" /></div>
          <div class="flex-1">
            <p class="text-base" style="font-weight:var(--weight-semibold)">{{ selectedAddress.receiverName }} <span class="text-hint text-sm" style="font-weight:var(--weight-normal);margin-left:8px">{{ selectedAddress.receiverPhone }}</span></p>
            <p class="text-sm text-secondary mt-xs">{{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}</p>
          </div>
          <van-icon name="arrow" color="#999" />
        </div>
      </template>
      <div v-else class="address-empty" @click.stop="showAddressForm = true">
        <van-icon name="add-o" size="20" color="#c8a44d" />
        <span class="text-sm text-hint">请添加收货地址</span>
      </div>
    </div>

    <!-- 商品清单 -->
    <div class="card">
      <p class="text-sm text-secondary mb-sm">商品清单（{{ cartStore.checkedItems.length }} 件）</p>
      <div v-for="entry in cartStore.checkedItems" :key="entry.product.id" class="checkout-item flex gap-md" style="margin:var(--space-md) 0">
        <van-image :src="entry.product.imageUrl || '/logo.png'" width="64" height="64" fit="cover" radius="8" />
        <div class="flex-1">
          <p class="text-sm text-ellipsis-2" style="font-weight:var(--weight-medium)">{{ entry.product.name }}</p>
          <p class="text-xs text-hint mt-xs">x{{ entry.quantity }}</p>
        </div>
        <span class="text-base" style="font-weight:var(--weight-semibold)">¥{{ (Number(entry.product.price) * entry.quantity).toFixed(2) }}</span>
      </div>
    </div>

    <!-- 支付方式 -->
    <div class="card">
      <p class="text-sm text-secondary mb-sm">支付方式</p>
      <div class="selector-list">
        <div v-for="pm in paymentMethods" :key="pm.value" class="selector-item" @click="paymentMethod = pm.value">
          <span class="selector-item__icon">{{ pm.icon }}</span>
          <span class="text-sm flex-1">{{ pm.label }}</span>
          <van-icon :name="paymentMethod === pm.value ? 'checked' : 'circle'" :color="paymentMethod === pm.value ? '#c8a44d' : '#d9d9d9'" size="20" />
        </div>
      </div>
    </div>

    <!-- 配送方式 -->
    <div class="card">
      <p class="text-sm text-secondary mb-sm">配送方式</p>
      <div class="selector-list">
        <div v-for="dm in deliveryMethods" :key="dm.value" class="selector-item" @click="deliveryMethod = dm.value">
          <span class="selector-item__icon">{{ dm.icon }}</span>
          <span class="text-sm flex-1">{{ dm.label }}</span>
          <van-icon :name="deliveryMethod === dm.value ? 'checked' : 'circle'" :color="deliveryMethod === dm.value ? '#c8a44d' : '#d9d9d9'" size="20" />
        </div>
      </div>
    </div>

    <!-- 价格明细 -->
    <div class="card">
      <div class="price-row"><span class="text-sm text-secondary">商品总额</span><span class="text-sm">¥{{ cartStore.checkedTotal.toFixed(2) }}</span></div>
      <div class="price-row"><span class="text-sm text-secondary">运费</span><span class="tag tag-green">免运费</span></div>
      <div class="price-row"><span class="text-sm text-secondary">优惠</span><span class="text-sm text-hint">暂无</span></div>
      <div class="divider" />
      <div class="price-row" style="margin-top:var(--space-sm)">
        <span class="text-base" style="font-weight:var(--weight-semibold)">实付款</span>
        <span class="price-current price-large" style="color:var(--color-danger)">¥{{ cartStore.checkedTotal.toFixed(2) }}</span>
      </div>
    </div>

    <!-- 留言 -->
    <div class="card">
      <van-field v-model="remark" placeholder="选填：给卖家的留言" borderless />
    </div>

    <!-- 提交 -->
    <div style="padding:var(--space-xl) var(--space-md) var(--space-4xl)">
      <van-button round block type="danger" :loading="submitting" @click="submitOrder" style="height:48px;font-size:var(--font-lg)">
        提交订单 ¥{{ cartStore.checkedTotal.toFixed(2) }}
      </van-button>
    </div>

    <!-- 地址选择弹窗 (same as before) -->
    <van-popup v-model:show="showAddressPicker" position="bottom" :style="{ height: '60%' }" round>
      <div class="popup-header"><span class="text-md" style="font-weight:var(--weight-semibold)">选择收货地址</span><van-button size="small" type="primary" @click="showAddressForm = true; showAddressPicker = false">新增</van-button></div>
      <div v-if="addresses.length === 0" class="text-center text-hint" style="padding:40px">暂无收货地址</div>
      <div v-for="addr in addresses" :key="addr.id" class="address-item" @click="selectAddress(addr)">
        <div class="flex gap-sm" style="align-items:flex-start">
          <van-icon :name="selectedAddress?.id === addr.id ? 'checked' : ''" :color="selectedAddress?.id === addr.id ? '#c8a44d' : '#ccc'" size="18" />
          <div class="flex-1">
            <p class="text-sm" style="font-weight:var(--weight-semibold)">{{ addr.receiverName }} <span class="text-hint text-xs">{{ addr.receiverPhone }}</span></p>
            <p class="text-xs text-secondary mt-xs">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</p>
          </div>
          <van-tag v-if="addr.isDefault" type="warning">默认</van-tag>
        </div>
      </div>
    </van-popup>

    <!-- 新增地址弹窗 -->
    <van-popup v-model:show="showAddressForm" position="bottom" :style="{ height: '70%' }" round>
      <div class="popup-header"><span class="text-md" style="font-weight:var(--weight-semibold)">新增收货地址</span></div>
      <van-form @submit="onAddAddress" style="padding:var(--space-md)">
        <van-cell-group inset>
          <van-field v-model="addrForm.receiverName" label="收货人" placeholder="请输入姓名" />
          <van-field v-model="addrForm.receiverPhone" label="手机号" placeholder="请输入手机号" />
          <van-field v-model="addrForm.province" label="省" placeholder="省份" />
          <van-field v-model="addrForm.city" label="市" placeholder="城市" />
          <van-field v-model="addrForm.district" label="区" placeholder="区县" />
          <van-field v-model="addrForm.detailAddress" label="详细地址" placeholder="街道/门牌号" />
        </van-cell-group>
        <div style="margin:var(--space-lg)"><van-button round block type="primary" native-type="submit">保存地址</van-button></div>
      </van-form>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { shopOrderApi } from '@/api/services'
import { useCartStore } from '@/stores/useCartStore'
import { useUserStore } from '@/stores/useUserStore'
import { useAddressStore } from '@/stores/useAddressStore'
import type { AddressItem } from '@/types'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const addressStore = useAddressStore()

const remark = ref('')
const submitting = ref(false)
const paymentMethod = ref('WECHAT')
const deliveryMethod = ref('EXPRESS')
const showAddressPicker = ref(false)
const showAddressForm = ref(false)

const paymentMethods = [
  { label: '微信支付', value: 'WECHAT', icon: '💚' },
  { label: '支付宝', value: 'ALIPAY', icon: '💙' },
  { label: '余额支付', value: 'BALANCE', icon: '🪙' }
]
const deliveryMethods = [
  { label: '快递配送', value: 'EXPRESS', icon: '📦' },
  { label: '门店自提', value: 'SELF_PICKUP', icon: '🏪' }
]

const addresses = ref<AddressItem[]>([])
const selectedAddress = ref<AddressItem | null>(null)
const addrForm = reactive({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '' })

onMounted(async () => {
  try {
    await addressStore.fetchAddresses()
    addresses.value = addressStore.addresses
    selectedAddress.value = addresses.value.find(a => a.isDefault) || addresses.value[0] || null
  } catch { /* skip */ }
})

function selectAddress(addr: AddressItem) { selectedAddress.value = addr; showAddressPicker.value = false }

async function onAddAddress() {
  if (!addrForm.receiverName || !addrForm.receiverPhone || !addrForm.detailAddress) { showToast('请填写必填信息'); return }
  try {
    await addressStore.createAddress({ ...addrForm, isDefault: addresses.value.length === 0 })
    await addressStore.fetchAddresses()
    addresses.value = addressStore.addresses
    selectedAddress.value = addresses.value[addresses.value.length - 1] || null
    showAddressForm.value = false
    Object.assign(addrForm, { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '' })
  } catch { /* skip */ }
}

async function submitOrder() {
  if (cartStore.checkedItems.length === 0) { showToast('商品清单为空'); return }
  if (!selectedAddress.value) { showToast('请选择收货地址'); return }
  submitting.value = true
  try {
    const cartItemIds = cartStore.checkedItems.filter(e => e.cartId).map(e => Number(e.cartId))
    const data = await shopOrderApi.create({
      cartItemIds: cartItemIds.length > 0 ? cartItemIds : undefined,
      addressId: selectedAddress.value ? Number(selectedAddress.value.id) : undefined,
      paymentMethod: paymentMethod.value, deliveryMethod: deliveryMethod.value, remark: remark.value
    })
    showToast('下单成功！')
    cartStore.removeChecked()
    router.push(`/order/${data.id}`)
  } catch { /* handled */ } finally { submitting.value = false }
}
</script>

<style scoped>
.checkout-page { padding-bottom: var(--space-4xl); background: var(--bg-page); min-height: 100vh; }

.address-section { margin: var(--space-sm) var(--space-md); cursor: pointer; }
.address-card {
  display: flex; align-items: center; gap: var(--space-md);
  background: var(--bg-gradient-card); border-radius: var(--radius-lg);
  padding: var(--space-lg); border-left: 3px solid var(--color-primary);
  box-shadow: var(--shadow-xs);
}
.address-card__icon { flex-shrink: 0; }
.address-empty {
  display: flex; align-items: center; justify-content: center; gap: var(--space-sm);
  padding: var(--space-xl); background: var(--bg-white);
  border-radius: var(--radius-lg); border: 1px dashed var(--color-gray-300);
}

.checkout-item:last-child { margin-bottom: 0 !important; }

.selector-list { display: flex; flex-direction: column; gap: var(--space-xs); }
.selector-item {
  display: flex; align-items: center; gap: var(--space-md);
  padding: var(--space-md); background: var(--color-gray-50);
  border-radius: var(--radius-md); cursor: pointer;
  transition: background var(--transition-fast);
}
.selector-item:active { background: var(--color-gray-150); }
.selector-item__icon { font-size: 20px; width: 32px; text-align: center; }

.price-row { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; }

.popup-header { display: flex; justify-content: space-between; align-items: center; padding: var(--space-lg); border-bottom: 1px solid #eee; }
.address-item { padding: var(--space-md) var(--space-lg); border-bottom: 1px solid var(--color-gray-100); cursor: pointer; }

.mt-xs { margin-top: var(--space-xs); }
.mb-sm { margin-bottom: var(--space-sm); }
.gap-md { gap: var(--space-md); }
</style>
