<template>
  <div class="category-page">
    <van-nav-bar title="商品分类" left-arrow @click-left="$router.back()" />
    <div class="category-layout flex">
      <div class="sidebar">
        <div
          v-for="cat in categories" :key="cat.id"
          :class="['sidebar__item', { active: activeCat === cat.name }]"
          @click="activeCat = cat.name"
        >{{ cat.name }}</div>
      </div>
      <div class="main flex-1" v-if="activeCat">
        <div class="main__header"><span class="main__title">{{ activeCat }}</span><span class="text-xs text-hint">{{ filteredProducts.length }} 件商品</span></div>
        <div class="product-grid p-md">
          <div v-for="p in filteredProducts" :key="p.id" class="product-card scale-tap" @click="goProduct(p.id)">
            <van-image :src="p.imageUrl || '/logo.png'" fit="cover" class="product-card__img">
              <template #loading><div class="skeleton" style="width:100%;height:100%"></div></template>
            </van-image>
            <div class="product-card__info">
              <p class="product-card__name text-ellipsis-2">{{ p.name }}</p>
              <span class="price-current">¥{{ p.price }}</span>
            </div>
          </div>
        </div>
        <van-empty v-if="!filteredProducts.length" description="该分类暂无商品" />
      </div>
    </div>
    <van-tabbar v-model="activeTab" :active-color="'#c8a44d'" route>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/category">分类</van-tabbar-item>
      <van-tabbar-item icon="cart-o" to="/cart">购物车</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/orders">订单</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { productApi, categoryApi } from '@/api/product'
import type { CategoryNode, ProductItem } from '@/types'

const router = useRouter()
const categories = ref<CategoryNode[]>([])
const activeCat = ref('')
const allProducts = ref<ProductItem[]>([])
const activeTab = ref(1)

const filteredProducts = computed(() =>
  allProducts.value.filter(p => (p.categoryName || '').includes(activeCat.value) || (p.brandName || '').includes(activeCat.value))
)

function goProduct(id: string | number) { router.push(`/product?id=${id}`) }

onMounted(async () => {
  try {
    const cats = await categoryApi.tree()
    categories.value = cats
    if (categories.value.length) activeCat.value = categories.value[0].name
  } catch { /* skip */ }
  try {
    const res = await productApi.list({ page: 1, pageSize: 200, status: 'ON_SALE' })
    allProducts.value = res.list || []
  } catch { /* skip */ }
})
</script>

<style scoped>
.category-page { padding-bottom: 60px; }
.category-layout { min-height: 100vh; }
.sidebar { width: 90px; background: var(--color-gray-50); }
.sidebar__item {
  padding: 14px var(--space-sm); font-size: var(--font-sm); text-align: center;
  cursor: pointer; color: var(--text-secondary); transition: all var(--transition-fast);
  border-left: 3px solid transparent;
}
.sidebar__item.active {
  background: var(--bg-white); color: var(--color-primary-dark);
  font-weight: var(--weight-semibold); border-left-color: var(--color-primary);
}
.main { background: var(--bg-white); }
.main__header { display: flex; align-items: baseline; justify-content: space-between; padding: var(--space-lg) var(--space-md) var(--space-sm); }
.main__title { font-size: var(--font-lg); font-weight: var(--weight-bold); }
</style>
