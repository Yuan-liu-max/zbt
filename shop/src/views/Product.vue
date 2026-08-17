<template>
  <div class="product-page" v-if="item">
    <van-skeleton :loading="loading" animate>
    <van-nav-bar left-text="返回" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="share-o" size="20" />
      </template>
    </van-nav-bar>

    <!-- 图片轮播 -->
    <div class="product-gallery">
      <van-swipe :autoplay="0" indicator-color="#c8a44d" class="product-swiper">
        <van-swipe-item>
          <van-image :src="item.imageUrl || getPlaceholder(item.name)" fit="cover" class="product-swiper__img">
            <template #loading><div class="skeleton" style="width:100%;height:100%"></div></template>
            <template #error><div class="img-fallback">💎</div></template>
          </van-image>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 价格区 -->
    <div class="product-price-section card--flush fade-in-up">
      <div class="flex-between">
        <div>
          <span class="price-current price-large">¥{{ item.price }}</span>
          <span v-if="item.costPrice != null && item.costPrice > 0" class="price-original">¥{{ item.costPrice }}</span>
        </div>
        <span class="tag tag-red tag-pill" v-if="item.stock && item.stock <= 10">仅剩 {{ item.stock }} 件</span>
      </div>
      <h1 class="product-name">{{ item.name }}</h1>
      <p v-if="item.description" class="product-desc">{{ item.description }}</p>
    </div>

    <!-- 属性网格 -->
    <div class="card fade-in-up stagger-1">
      <h4 class="card__title">商品属性</h4>
      <div class="attr-pills">
        <div v-if="item.material" class="attr-pill">
          <span class="attr-pill__icon">🏷</span>
          <div><span class="text-hint text-2xs">材质</span><p class="text-sm">{{ item.material }}</p></div>
        </div>
        <div v-if="item.weight" class="attr-pill">
          <span class="attr-pill__icon">⚖</span>
          <div><span class="text-hint text-2xs">重量</span><p class="text-sm">{{ item.weight }}</p></div>
        </div>
        <div v-if="item.size" class="attr-pill">
          <span class="attr-pill__icon">📐</span>
          <div><span class="text-hint text-2xs">尺寸</span><p class="text-sm">{{ item.size }}</p></div>
        </div>
        <div v-if="item.storeName" class="attr-pill">
          <span class="attr-pill__icon">📍</span>
          <div><span class="text-hint text-2xs">门店</span><p class="text-sm">{{ item.storeName }}</p></div>
        </div>
        <div v-if="item.stock != null" class="attr-pill">
          <span class="attr-pill__icon">📦</span>
          <div><span class="text-hint text-2xs">库存</span><p class="text-sm">{{ item.stock > 0 ? item.stock + '件' : '暂时缺货' }}</p></div>
        </div>
      </div>
    </div>

    <!-- 服务保证 -->
    <div class="service-row fade-in-up stagger-2">
      <div class="service-chip"><van-icon name="shield-o" size="14" color="#07c160" /><span>正品保证</span></div>
      <div class="service-chip"><van-icon name="logistics" size="14" color="#1989fa" /><span>顺丰包邮</span></div>
      <div class="service-chip"><van-icon name="balance-o" size="14" color="#ff976a" /><span>7天退换</span></div>
      <div class="service-chip"><van-icon name="gold-coin-o" size="14" color="#c8a44d" /><span>会员积分</span></div>
    </div>

    <!-- 加载/错误 -->
    <div v-if="error" class="status-box"><van-empty description="获取商品详情失败" /></div>

    <!-- 底部操作栏 -->
    <div class="action-bar">
      <div class="action-bar__fav" @click="toggleFavorite">
        <van-icon :name="isFavorited ? 'star' : 'star-o'" size="22" :color="isFavorited ? '#ff976a' : '#666'" />
        <span :class="['text-3xs', isFavorited ? 'text-warning' : 'text-hint']">{{ isFavorited ? '已收藏' : '收藏' }}</span>
      </div>
      <div class="action-bar__cart" @click="$router.push('/cart')">
        <van-icon name="cart-o" size="22" color="#666" />
        <van-badge :content="cartCount" v-if="cartCount > 0" />
        <span class="text-3xs text-hint">购物车</span>
      </div>
      <button class="btn-cart" @click="addToCart">加入购物车</button>
      <button class="btn-buy" @click="buyNow">立即购买</button>
    </div>
    </van-skeleton>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { productApi } from '@/api/product'
import { favoriteApi } from '@/api/services'
import { useCartStore } from '@/stores/useCartStore'
import { useUserStore } from '@/stores/useUserStore'
import type { ProductItem } from '@/types'
import placeholderImg from '@/assets/placeholder.svg'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const item = ref<ProductItem | null>(null)
const loading = ref(true)
const error = ref('')
const isFavorited = ref(false)
const quantity = ref(1)

const cartCount = computed(() => cartStore.totalCount)

function getPlaceholder(_name: string) {
  return placeholderImg
}

async function fetchDetail() {
  try {
    const id = route.query.id as string
    const data = await productApi.detail(id)
    item.value = data
    if (userStore.isLoggedIn) {
      try { isFavorited.value = await favoriteApi.check(String(data.id)) } catch { /* skip */ }
    } else {
      const favs = JSON.parse(localStorage.getItem('zbt_favs') || '[]')
      isFavorited.value = favs.includes(String(data.id))
    }
  } catch { error.value = '获取商品详情失败' } finally { loading.value = false }
}

function addToCart() {
  if (!item.value) return
  cartStore.add(item.value, quantity.value)
  showToast('已加入购物车')
}

async function buyNow() {
  if (!item.value) return
  if (!userStore.isLoggedIn) { showToast('请先登录'); router.push('/profile'); return }
  addToCart()
  router.push('/checkout')
}

async function toggleFavorite() {
  if (!item.value) return
  const id = String(item.value.id)
  if (userStore.isLoggedIn) {
    try {
      isFavorited.value ? await favoriteApi.remove(id) : await favoriteApi.add(id)
      isFavorited.value = !isFavorited.value
      showToast(isFavorited.value ? '已收藏' : '已取消收藏')
    } catch { /* fallback */ }
  } else {
    const favs = JSON.parse(localStorage.getItem('zbt_favs') || '[]') as string[]
    const idx = favs.indexOf(id)
    idx >= 0 ? favs.splice(idx, 1) : favs.push(id)
    isFavorited.value = idx < 0
    localStorage.setItem('zbt_favs', JSON.stringify(favs))
    showToast(isFavorited.value ? '已收藏' : '已取消收藏')
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.product-page { background: var(--bg-page); min-height: 100vh; padding-bottom: 80px; }

/* ======== Gallery ======== */
.product-gallery { background: var(--bg-white); }
.product-swiper__img { width: 100%; height: 375px; background: var(--color-gray-100); }
.img-fallback { width: 100%; height: 375px; display: flex; align-items: center; justify-content: center; font-size: 64px; background: var(--color-gray-100); }

/* ======== Price Section ======== */
.product-price-section { background: var(--bg-white); padding: var(--space-lg) var(--space-md); }
.card--flush { margin: 0; border-radius: 0; }
.product-name { font-size: var(--font-lg); font-weight: var(--weight-semibold); color: var(--text-primary); margin: var(--space-sm) 0; line-height: var(--leading-tight); }
.product-desc { font-size: var(--font-sm); color: var(--text-hint); margin: 0; }

/* ======== Attributes ======== */
.card__title { font-size: var(--font-base); font-weight: var(--weight-semibold); margin: 0 0 var(--space-md) 0; color: var(--text-primary); }
.attr-pills { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--space-sm); }
.attr-pill {
  display: flex; align-items: center; gap: var(--space-sm);
  background: var(--color-gray-50); padding: var(--space-md); border-radius: var(--radius-md);
}
.attr-pill__icon { font-size: 18px; flex-shrink: 0; width: 28px; text-align: center; }

/* ======== Service Row ======== */
.service-row {
  display: flex; justify-content: space-around;
  padding: var(--space-md) var(--space-md); margin: var(--space-sm) var(--space-md);
  background: var(--bg-white); border-radius: var(--radius-lg); box-shadow: var(--shadow-xs);
}
.service-chip { display: flex; align-items: center; gap: 4px; font-size: var(--font-2xs); color: var(--text-secondary); }

/* ======== Action Bar ======== */
.action-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  max-width: var(--max-width); margin: 0 auto;
  background: rgba(255,255,255,0.94);
  backdrop-filter: blur(var(--blur-lg));
  -webkit-backdrop-filter: blur(var(--blur-lg));
  display: flex; align-items: center; gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md) calc(var(--space-sm) + var(--safe-bottom));
  border-top: 1px solid var(--color-gray-150);
  z-index: 100;
}
.action-bar__fav, .action-bar__cart {
  display: flex; flex-direction: column; align-items: center;
  width: 48px; cursor: pointer; position: relative;
}

.btn-cart, .btn-buy {
  flex: 1; height: 42px; border-radius: var(--radius-xl); border: none;
  font-size: var(--font-base); font-weight: var(--weight-semibold); cursor: pointer;
  transition: all var(--transition-fast);
}
.btn-cart {
  background: linear-gradient(135deg, #1a1a1a, #333);
  color: #fff;
}
.btn-buy {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: var(--shadow-gold);
}
.btn-cart:active, .btn-buy:active { transform: scale(0.97); opacity: 0.9; }

.status-box { text-align: center; padding: 60px; }
.text-warning { color: var(--color-warning); }
</style>
