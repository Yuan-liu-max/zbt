<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-bar__box" @click="goSearch">
        <van-icon name="search" size="16" color="#999" />
        <span class="search-bar__placeholder">搜索珠宝商品</span>
      </div>
    </div>

    <!-- 加载态 -->
    <template v-if="loading">
      <div class="skeleton--card" style="height:200px;margin-top:var(--space-sm)"></div>
      <div class="product-grid" style="padding:var(--space-sm)">
        <div v-for="i in 4" :key="i" class="product-card">
          <div class="skeleton skeleton--image"></div>
          <div style="padding:var(--space-md)">
            <div class="skeleton skeleton--title"></div>
            <div class="skeleton skeleton--text" style="width:50%"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- 内容 -->
    <template v-else>
      <!-- Hero 轮播 -->
      <div class="hero-section fade-in-up">
        <van-swipe :autoplay="4000" indicator-color="#c8a44d" class="hero-swiper" :loop="true">
          <van-swipe-item v-for="(banner, i) in heroBanners" :key="i">
            <div class="hero-slide" :style="{ background: banner.bg }" @click="goProduct(banner.productId)">
              <div class="hero-slide__text">
                <span class="hero-slide__tag">{{ banner.tag }}</span>
                <h2 class="hero-slide__title">{{ banner.title }}</h2>
                <p class="hero-slide__desc">{{ banner.desc }}</p>
                <span class="hero-slide__cta">{{ banner.cta }} →</span>
              </div>
              <div class="hero-slide__img">
                <span class="hero-slide__emoji">{{ banner.icon }}</span>
              </div>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- 分类导航 -->
      <div class="category-nav fade-in-up stagger-1">
        <div
          v-for="(cat, i) in quickCategories"
          :key="cat.name"
          class="category-nav__item scale-tap"
          :style="{ animationDelay: `${0.1 + i * 0.04}s` }"
          @click="goCategory(cat.name)"
        >
          <div class="category-nav__icon" :style="{ background: cat.gradient }">
            <span>{{ cat.icon }}</span>
          </div>
          <span class="category-nav__text">{{ cat.name }}</span>
        </div>
      </div>

      <!-- 促销横幅 -->
      <div v-if="promotions.length" class="promo-bar scale-tap fade-in-up stagger-2" @click="goPromotions">
        <div class="promo-bar__icon-wrap">
          <span class="promo-bar__icon">🎉</span>
        </div>
        <div class="promo-bar__content">
          <span class="promo-bar__title">{{ promotions[0]?.name || '限时优惠' }}</span>
          <span class="promo-bar__sub">{{ promotions[0]?.discountMethod || '点击查看详情' }}</span>
        </div>
        <van-icon name="arrow" color="#c8a44d" size="16" />
      </div>

      <!-- 精选商品 -->
      <div class="section-header fade-in-up stagger-3">
        <div>
          <h3 class="section-title">精选好物</h3>
          <p class="section-subtitle">为你甄选的珠宝精品</p>
        </div>
        <span class="section-more" @click="goCategory()">全部分类 →</span>
      </div>

      <!-- 商品网格 -->
      <div class="product-grid fade-in-up stagger-4">
        <div
          v-for="item in products"
          :key="item.id"
          class="product-card scale-tap"
          @click="goProduct(item.id)"
        >
          <div class="product-card__img-wrap">
            <van-image
              :src="item.imageUrl || getPlaceholder(item.name)"
              fit="cover"
              class="product-card__img"
            >
              <template #loading>
                <div class="skeleton" style="width:100%;height:100%"></div>
              </template>
              <template #error>
                <div class="product-card__img-fallback">
                  <span class="product-card__img-emoji">💎</span>
                </div>
              </template>
            </van-image>
            <div v-if="item.stock && item.stock <= 10" class="product-card__stock-tag">仅剩 {{ item.stock }} 件</div>
          </div>
          <div class="product-card__info">
            <p class="product-card__name text-ellipsis-2">{{ item.name }}</p>
            <div class="product-card__price-row">
              <span class="price-current">¥{{ item.price }}</span>
              <span v-if="item.storeName" class="product-card__store">{{ item.storeName }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!products.length" class="empty-state">
        <span class="empty-state__icon">💎</span>
        <p class="empty-state__text">暂无商品，敬请期待</p>
      </div>
    </template>

    <!-- TabBar -->
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { productApi } from '@/api/product'
import { marketingApi } from '@/api/services'
import { useCartStore } from '@/stores/useCartStore'
import type { ProductItem, PromotionItem } from '@/types'
import placeholderImg from '@/assets/placeholder.svg'

const router = useRouter()
const cartStore = useCartStore()

const products = ref<ProductItem[]>([])
const promotions = ref<PromotionItem[]>([])
const loading = ref(true)
const activeTab = ref(0)

const cartCount = computed(() => cartStore.totalCount || 0)

// TODO: 从 /products/recommend 或 /api/promotions/banners 动态获取 banner 数据
const heroBanners = ref([
  { tag: '新品首发', title: '足金花开富贵手镯', desc: '足金999 · 吉祥如意', cta: '立即选购', icon: '🥇', bg: 'linear-gradient(135deg, #1a1a2e 0%, #2d2d44 50%, #1a1a2e 100%)', productId: 1001 },
  { tag: '限时特惠', title: '1克拉六爪钻戒', desc: '铂金950 · 经典镶嵌', cta: '立即选购', icon: '💍', bg: 'linear-gradient(135deg, #1a0a0a 0%, #2d1414 50%, #1a0a0a 100%)', productId: 1003 },
  { tag: '精工典藏', title: '冰种翡翠手镯', desc: '冰种质地 · 温润通透', cta: '立即选购', icon: '💚', bg: 'linear-gradient(135deg, #0a1a0a 0%, #142d14 50%, #0a1a0a 100%)', productId: 1005 },
  { tag: '人气爆款', title: '南洋金珠项链', desc: '南洋金珠 · 奢华典雅', cta: '立即选购', icon: '✨', bg: 'linear-gradient(135deg, #1a150a 0%, #2d2414 50%, #1a150a 100%)', productId: 1007 }
])

// Rich category icons with gradient backgrounds
const quickCategories = [
  { name: '黄金', icon: '🥇', gradient: 'linear-gradient(135deg, #fef3c7, #fde68a)' },
  { name: '钻石', icon: '💎', gradient: 'linear-gradient(135deg, #e0f2fe, #bae6fd)' },
  { name: 'K金', icon: '✨', gradient: 'linear-gradient(135deg, #fef9c3, #fef08a)' },
  { name: '翡翠', icon: '💚', gradient: 'linear-gradient(135deg, #dcfce7, #bbf7d0)' },
  { name: '珍珠', icon: '🤍', gradient: 'linear-gradient(135deg, #fdf6f0, #f3ddd0)' },
  { name: '铂金', icon: '🪩', gradient: 'linear-gradient(135deg, #f1f5f9, #e2e8f0)' },
  { name: '银饰', icon: '⚪', gradient: 'linear-gradient(135deg, #f9fafb, #d1d5db)' },
  { name: '其他', icon: '📿', gradient: 'linear-gradient(135deg, #fefce8, #fef9c3)' }
]

// 本地占位图（移除外部 placehold.co 依赖）
function getPlaceholder(_name: string) {
  return placeholderImg
}

function goSearch() { router.push('/search') }
function goProduct(id: string | number) { router.push(`/product?id=${id}`) }
function goCategory(cat?: string) {
  if (cat) router.push(`/search?category=${cat}`)
  else router.push('/category')
}
function goPromotions() { router.push('/promotions') }

async function fetchProducts() {
  try {
    try {
      const recommended = await productApi.recommend(20)
      if (recommended?.length) { products.value = recommended; return }
    } catch { /* fallback */ }
    const res = await productApi.list({ page: 1, pageSize: 50, status: 'ON_SALE' })
    products.value = res.list || []
  } catch { /* network */ }
}

async function fetchPromotions() {
  try {
    const res = await marketingApi.getPromotions({ page: 1, pageSize: 5, status: 'ongoing' })
    promotions.value = res.list || []
  } catch { /* skip */ }
}

onMounted(async () => {
  await Promise.all([fetchProducts(), fetchPromotions()])
  loading.value = false
})
</script>

<style scoped>
.home-page {
  padding-bottom: calc(var(--tabbar-height) + var(--safe-bottom));
  background: var(--bg-page);
  min-height: 100vh;
}

/* ======== Search Bar ======== */
.search-bar {
  padding: var(--space-sm) var(--space-md);
  background: var(--bg-white);
  position: sticky;
  top: var(--safe-top);
  z-index: 50;
}
.search-bar__box {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  background: var(--color-gray-100);
  border-radius: var(--radius-xl);
  padding: 10px var(--space-lg);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.search-bar__box:active { background: var(--color-gray-200); }
.search-bar__placeholder {
  font-size: var(--font-base);
  color: var(--text-hint);
}

/* ======== Hero ======== */
.hero-section { padding: var(--space-sm) var(--space-md); }
.hero-swiper {
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}
.hero-slide {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-xl) var(--space-2xl);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}
.hero-slide::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  pointer-events: none;
}
.hero-slide__text {
  display: flex;
  flex-direction: column;
  gap: var(--space-2xs);
  z-index: 1;
}
.hero-slide__tag {
  font-size: var(--font-3xs);
  color: var(--color-primary-light);
  background: rgba(200, 164, 77, 0.2);
  padding: 2px 10px;
  border-radius: var(--radius-full);
  width: fit-content;
  font-weight: var(--weight-medium);
  letter-spacing: var(--tracking-wider);
  text-transform: uppercase;
}
.hero-slide__title {
  font-size: var(--font-xl);
  font-weight: var(--weight-bold);
  color: #fff;
  margin: 0;
  line-height: var(--leading-tight);
}
.hero-slide__desc {
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}
.hero-slide__cta {
  font-size: var(--font-xs);
  color: var(--color-primary-light);
  font-weight: var(--weight-semibold);
  margin-top: var(--space-2xs);
}
.hero-slide__emoji { font-size: 60px; opacity: 0.9; z-index: 1; filter: drop-shadow(0 4px 8px rgba(0,0,0,0.3)); }

/* ======== Category Nav ======== */
.category-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md) var(--space-sm);
  padding: var(--space-lg) var(--space-md);
  background: var(--bg-white);
  margin: var(--space-sm) var(--space-md);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xs);
}
.category-nav__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
}
.category-nav__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  transition: all var(--transition-fast);
}
.category-nav__item:active .category-nav__icon {
  transform: scale(0.92);
}
.category-nav__text {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  font-weight: var(--weight-medium);
}

/* ======== Promo Bar ======== */
.promo-bar {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin: var(--space-sm) var(--space-md);
  padding: var(--space-md) var(--space-lg);
  background: linear-gradient(135deg, #fff8e1 0%, #fff3e0 50%, #fefce8 100%);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(200, 164, 77, 0.15);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.promo-bar:active { transform: scale(0.98); }
.promo-bar__icon-wrap {
  width: 40px; height: 40px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.promo-bar__icon { font-size: 18px; }
.promo-bar__content { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.promo-bar__title { font-size: var(--font-sm); font-weight: var(--weight-semibold); color: var(--text-primary); }
.promo-bar__sub { font-size: var(--font-2xs); color: var(--text-hint); }

/* ======== Section Header ======== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: var(--space-xl) var(--space-md) var(--space-sm);
}
.section-title {
  font-size: var(--font-xl);
  font-weight: var(--weight-bold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: var(--tracking-tight);
}
.section-subtitle {
  font-size: var(--font-xs);
  color: var(--text-hint);
  margin: 2px 0 0 0;
}
.section-more {
  font-size: var(--font-sm);
  color: var(--text-hint);
  cursor: pointer;
  transition: color var(--transition-fast);
}
.section-more:active { color: var(--color-primary); }

/* ======== Product Card Enhancements ======== */
.product-card__img-wrap {
  position: relative;
  overflow: hidden;
}
.product-card__img {
  width: 100%;
  aspect-ratio: 1;
  background: var(--color-gray-100);
}
.product-card__img-fallback {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-gray-100);
}
.product-card__img-emoji { font-size: 48px; opacity: 0.6; }

.product-card__stock-tag {
  position: absolute;
  top: var(--space-sm);
  left: var(--space-sm);
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: var(--font-3xs);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  font-weight: var(--weight-medium);
}
</style>
