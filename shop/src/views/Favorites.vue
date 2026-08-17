<template>
  <div class="fav-page page-container--no-tabbar">
    <van-nav-bar title="我的收藏" left-text="返回" left-arrow @click-left="$router.back()" />
    <div class="product-grid p-md fade-in-up" v-if="favProducts.length">
      <div v-for="p in favProducts" :key="p.id" class="product-card scale-tap" @click="goProduct(p.id)">
        <van-image :src="p.imageUrl || '/logo.png'" fit="cover" class="product-card__img">
          <template #loading><div class="skeleton" style="width:100%;height:100%"></div></template>
        </van-image>
        <div class="product-card__info">
          <p class="product-card__name text-ellipsis-2">{{ p.name }}</p>
          <span class="price-current">¥{{ p.price }}</span>
        </div>
      </div>
    </div>
    <van-empty v-else description="暂无收藏"><van-button type="primary" to="/home" round>去逛逛</van-button></van-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { favoriteApi } from '@/api/services'
import { useUserStore } from '@/stores/useUserStore'
import type { ProductItem } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const favProducts = ref<ProductItem[]>([])

function goProduct(id: string | number) { router.push(`/product?id=${id}`) }

onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      const serverFavs = await favoriteApi.list()
      favProducts.value = (serverFavs || []).map((item: any) => ({
        id: item.productId, name: item.name, code: item.code,
        price: item.price, imageUrl: item.imageUrl, storeName: item.storeName, status: item.status
      } as ProductItem))
      return
    } catch { /* 服务端异常时 fallback 到本地 */ }
  }
  // 未登录时使用 localStorage 本地收藏
  const favs = JSON.parse(localStorage.getItem('zbt_favs') || '[]') as string[]
  if (!favs.length) return
  const results: ProductItem[] = []
  for (const id of favs) { try { results.push(await productApi.detail(id)) } catch { /* skip */ } }
  favProducts.value = results
})
</script>

<style scoped>
.fav-page { min-height: 100vh; background: var(--bg-page); }
</style>
