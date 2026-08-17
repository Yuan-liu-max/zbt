<template>
  <div class="search-page page-container">
    <van-search v-model="keyword" shape="round" placeholder="搜索珠宝商品" show-action @search="doSearch">
      <template #action><span @click="doSearch" style="color:var(--color-primary)">搜索</span></template>
    </van-search>

    <div v-if="!searched" class="hot-section fade-in-up">
      <p class="text-sm text-secondary mb-sm">🔥 热门搜索</p>
      <div class="hot-tags">
        <span v-for="t in hotTags" :key="t" class="hot-tag" @click="keyword = t; doSearch()">{{ t }}</span>
      </div>
    </div>

    <div v-else class="fade-in-up">
      <div class="text-sm text-hint px-lg py-sm">找到 {{ products.length }} 件商品</div>
      <div class="product-grid p-md">
        <div v-for="p in products" :key="p.id" class="product-card scale-tap" @click="goProduct(p.id)">
          <van-image :src="p.imageUrl || '/logo.png'" fit="cover" class="product-card__img">
            <template #loading><div class="skeleton" style="width:100%;height:100%"></div></template>
          </van-image>
          <div class="product-card__info">
            <p class="product-card__name text-ellipsis-2">{{ p.name }}</p>
            <span class="price-current">¥{{ p.price }}</span>
          </div>
        </div>
      </div>
    </div>

    <van-empty v-if="searched && !products.length" description="没有找到相关商品" />

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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import type { ProductItem } from '@/types'

const route = useRoute()
const router = useRouter()
const keyword = ref((route.query.category as string) || (route.query.keyword as string) || '')
const products = ref<ProductItem[]>([])
const searched = ref(false)
const activeTab = ref(0)
const hotTags = ref<string[]>(['黄金手镯', '钻石戒指', '珍珠项链', 'K金耳环', '翡翠挂件', '铂金对戒', '银饰手链', '古法黄金'])

onMounted(() => {
  productApi.hotSearchKeywords().then(tags => {
    if (tags && tags.length) hotTags.value = tags
  }).catch(() => {})
  // 如果 URL 带有关键词，自动恢复搜索结果
  if (keyword.value.trim()) {
    doSearch()
  }
})

function goProduct(id: string | number) { router.push(`/product?id=${id}`) }

async function doSearch() {
  if (!keyword.value.trim()) return
  // 将搜索词写入 URL，确保返回时状态可恢复
  router.replace({ query: { keyword: keyword.value } })
  searched.value = true
  try {
    const res = await productApi.list({ page: 1, pageSize: 50, keyword: keyword.value, status: 'ON_SALE' })
    products.value = res.list || []
  } catch { products.value = [] }
}
</script>

<style scoped>
.search-page { padding-bottom: 80px; }
.hot-section { padding: var(--space-lg) var(--space-md); }
.hot-tags { display: flex; flex-wrap: wrap; gap: var(--space-sm); }
.hot-tag {
  display: inline-block; background: var(--bg-white); padding: 6px 14px;
  border-radius: var(--radius-full); font-size: var(--font-sm);
  color: var(--text-secondary); cursor: pointer;
  border: 1px solid var(--color-gray-200); transition: all var(--transition-fast);
}
.hot-tag:active { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }
.px-lg { padding-left: var(--space-lg); padding-right: var(--space-lg); }
.py-sm { padding-top: var(--space-sm); padding-bottom: var(--space-sm); }
</style>
