<template>
  <div class="cart-page page-container">
    <van-nav-bar title="购物车" left-arrow @click-left="$router.back()" />

    <!-- 空购物车 -->
    <div v-if="cartStore.items.length === 0" class="empty-state" style="padding-top:80px">
      <span class="empty-state__icon">🛒</span>
      <p class="empty-state__text">购物车还是空的</p>
      <van-button type="primary" to="/home" round size="small">去逛逛</van-button>
    </div>

    <template v-else>
      <van-checkbox-group v-model="checkedIds" @change="onCheckChange">
        <van-swipe-cell v-for="entry in cartStore.items" :key="entry.product.id">
          <div class="cart-item gradient-card" style="margin:var(--space-xs) var(--space-sm)">
            <van-checkbox :name="String(entry.product.id)" />
            <van-image :src="entry.product.imageUrl || '/logo.png'" width="88" height="88" fit="cover" radius="10" class="cart-item__img" />
            <div class="cart-item__info">
              <p class="text-sm text-ellipsis-2" style="font-weight:var(--weight-medium)">{{ entry.product.name }}</p>
              <p class="price-current text-md">¥{{ entry.product.price }}</p>
            </div>
            <van-stepper v-model="entry.quantity" :min="1" :max="entry.product.stock || 99" @change="cartStore.updateQuantity(entry.product.id, entry.quantity)" />
          </div>
          <template #right>
            <van-button square type="danger" text="删除" style="height:100%" @click="cartStore.remove(entry.product.id)" />
          </template>
        </van-swipe-cell>
      </van-checkbox-group>

      <!-- 结算栏 -->
      <van-submit-bar :price="cartStore.checkedTotal * 100" button-text="去结算" @submit="goCheckout">
        <van-checkbox v-model="allChecked" @change="cartStore.toggleAll(allChecked)">全选</van-checkbox>
      </van-submit-bar>
    </template>

    <van-tabbar v-model="activeTab" :active-color="'#c8a44d'" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/category">分类</van-tabbar-item>
      <van-tabbar-item icon="cart-o" to="/cart" :badge="cartCount || ''">购物车</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useCartStore } from '@/stores/useCartStore'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const activeTab = ref(2)
const checkedIds = ref<string[]>(cartStore.items.filter(e => e.checked).map(e => String(e.product.id)))
const allChecked = ref(cartStore.isAllChecked)
const cartCount = computed(() => cartStore.totalCount || 0)

function onCheckChange(ids: string[]) {
  cartStore.items.forEach(e => {
    const shouldBeChecked = ids.includes(String(e.product.id))
    if (e.checked !== shouldBeChecked) {
      cartStore.toggleCheck(e.product.id)  // toggleCheck 内部会翻转并同步服务端
    }
  })
  allChecked.value = cartStore.isAllChecked
}

function goCheckout() {
  if (!userStore.isLoggedIn) { showToast('请先登录'); router.push('/profile'); return }
  if (cartStore.checkedItems.length === 0) { showToast('请选择商品'); return }
  router.push('/checkout')
}

onMounted(async () => {
  if (userStore.isLoggedIn) await cartStore.loadFromServer()
  checkedIds.value = cartStore.items.filter(e => e.checked).map(e => String(e.product.id))
  allChecked.value = cartStore.isAllChecked
})
</script>

<style scoped>
.cart-page { padding-bottom: 100px; }
.cart-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}
.cart-item__img { flex-shrink: 0; background: var(--color-gray-100); }
.cart-item__info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: var(--space-xs); }
</style>
