import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const routes: RouteRecordRaw[] = [
  // 旧路径重定向
  { path: '/goods', redirect: '/home' },
  { path: '/index', redirect: '/home' },
  { path: '/order', redirect: '/orders' },
  { path: '/login', redirect: '/profile' },
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', tabbar: 0, transition: 'fade' }
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/Category.vue'),
    meta: { title: '分类', tabbar: 1, transition: 'fade' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '购物车', needLogin: true, tabbar: 2, transition: 'fade' }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/OrderList.vue'),
    meta: { title: '订单', needLogin: true, tabbar: 3, transition: 'fade' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '我的', tabbar: 4, transition: 'fade' }
  },
  {
    path: '/product',
    name: 'Product',
    component: () => import('@/views/Product.vue'),
    meta: { title: '商品详情', transition: 'slide-up' }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: { title: '搜索', transition: 'slide-up' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { title: '确认订单', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { title: '订单详情', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/return',
    name: 'ReturnApply',
    component: () => import('@/views/ReturnApply.vue'),
    meta: { title: '申请退款', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/address',
    name: 'AddressList',
    component: () => import('@/views/AddressList.vue'),
    meta: { title: '收货地址', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: { title: '设置', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { title: '消息通知', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/Favorites.vue'),
    meta: { title: '我的收藏', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/promotions',
    name: 'Promotions',
    component: () => import('@/views/Promotions.vue'),
    meta: { title: '营销活动', transition: 'slide-up' }
  },
  {
    path: '/coupons',
    name: 'MyCoupons',
    component: () => import('@/views/MyCoupons.vue'),
    meta: { title: '我的优惠券', needLogin: true, transition: 'slide-up' }
  },
  {
    path: '/ai-guide',
    name: 'AiGuide',
    component: () => import('@/views/AiGuide.vue'),
    meta: { title: 'AI 导购', transition: 'slide-up' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Home.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory('/h5/'),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, _from) => {
  document.title = `${to.meta.title || '珠宝通'} - 珠宝通`

  if (to.meta.needLogin) {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      return `/profile?redirect=${encodeURIComponent(to.fullPath)}`
    }
  }
})

export default router
