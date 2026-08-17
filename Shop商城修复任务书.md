# Shop 商城前端 — 修复任务书

> 基于 2026-08-08 代码审查，共计 40 个问题，按优先级分三批修复。

---

## 🚨 第一批：阻塞性修复（P0 — 立即处理）

### 任务 1：删除 `utils/token.js`，统一登录态

**问题**：`token.js` 用 `sessionStorage`，`token.ts` 用 `localStorage`，两套体系并存。`About.vue` 导入 token.js，导致其登录态与整个 App 隔离。

**修复步骤**：
```
1. 删除 shop/src/utils/token.js
2. 打开 shop/src/views/About.vue
3. 移除: import { getToken, setToken, removeToken, getUserInfo, setUserInfo } from '@/utils/token'
4. 新增: import { useUserStore } from '@/stores/useUserStore'
5. 所有 getToken/setToken/removeToken 调用替换为 useUserStore().token
6. 所有 getUserInfo/setUserInfo 调用替换为 useUserStore().userInfo
```

---

### 任务 2：修复 401 跳转死路由

**问题**：`api/index.ts` 第 39 行 `router.push('/login')`，但 `/login` 路由不存在。

**修复步骤**：
```
打开 shop/src/api/index.ts，找到两处：
  router.push('/login')
改为：
  router.push('/profile')
```

---

### 任务 3：移除 `Order.vue` 和 `Goods.vue` 的旧 localStorage 购物车

**问题**：这两个文件使用旧 `localStorage` key `"gsStore"` 独立管理购物车，与 Pinia `useCartStore` 完全隔离。

**修复步骤**：
```
方案 A（推荐）：
  1. 删除 shop/src/views/Goods.vue（功能与 Product.vue 完全重复，且是 Options API）
  2. 删除 shop/src/views/Order.vue（功能与 OrderList.vue 完全重复，且是 Options API）
  3. 删除 shop/src/views/Index.vue（功能与 Home.vue 完全重复）

方案 B（保留文件）：
  1. Goods.vue: 移除所有 localStorage.getItem/setItem('gsStore', ...) 
  2. Goods.vue: 导入 useCartStore，改用 cartStore.add() / cartStore.items
  3. Goods.vue: 所有 alert() 改为 showToast()
  4. Goods.vue: 下单改用 shopOrderApi 统一格式
  5. Order.vue: 导入 useOrderStore，移除 gsStore 逻辑
  6. Order.vue: alert()/confirm() 改为 showToast()/showConfirmDialog()
```

---

### 任务 4：修复不存在路由的导航

**问题**：多个视图跳转到不存在的路由（`/goods`、`/index`、`/login`、`/order`）。

**修复步骤**：
```
1. 任务 2 已处理 /login → /profile
2. 任务 3 删除 Goods.vue/Order.vue/Index.vue 后，这些路径的导航自然消失
3. 如果保留，在 shop/src/router/index.ts 添加：
   { path: '/goods', redirect: '/home' },
   { path: '/index', redirect: '/home' },
   { path: '/order', redirect: '/orders' },
   { path: '/login', redirect: '/profile' },
```

---

### 任务 5：修复 `stores/useAddressStore.ts` 的 `defaultAddress` 非响应式

**问题**：`defaultAddress` 是普通函数而非 `computed`。

**修复步骤**：
```
打开 shop/src/stores/useAddressStore.ts
找到:
  const defaultAddress = () => addresses.value.find(...)
改为:
  const defaultAddress = computed(() => addresses.value.find(...))
同时替换所有 defaultAddress() 调用为 defaultAddress（去掉括号）
```

---

### 任务 6：修复 Category.vue 无意义的 status 过滤

**问题**：`c.status !== 'off'` 过滤在 `CategoryNode` 类型中字段不存在，永远为 true。

**修复步骤**：
```
打开 shop/src/views/Category.vue 第 59 行
删除:
  .filter((c: any) => c.status !== 'off')
或改为:
  // 分类由后端控制，无需前端过滤
```

---

## ⚠️ 第二批：功能修复（P1 — 本周处理）

### 任务 7：移除 `useUserStore` 的 localStorage JWT 备份（XSS 风险）

**问题**：`window.__AUTH_TOKEN__` + `localStorage zbt_shop_jwt` 重新引入 XSS 风险。

**修复步骤**：
```
打开 shop/src/stores/useUserStore.ts
1. 删除第 9-18 行的 loadJwt/saveJwt/clearJwt 函数
2. 删除 init() 中第 41-43 行 __AUTH_TOKEN__ 恢复逻辑
3. 删除 init() 中第 53-55 行 Authorization header 构建
4. 删除 setAuth() 中第 93-94 行 __AUTH_TOKEN__ 和 saveJwt
5. 简化 init()：只用 http.get('/shop/auth/me') + Cookie 认证
```

---

### 任务 8：完整 ProductItem 映射

**问题**：`useCartStore.loadFromServer()` 只映射 6 个字段。

**修复步骤**：
```
打开 shop/src/stores/useCartStore.ts 第 61-73 行
扩展 serverEntries 映射：
  product: {
    id: item.productId,
    name: item.productName || item.name,
    code: item.productCode || item.code,
    price: item.price,
    imageUrl: item.imageUrl,
    stock: item.stock,
    storeName: item.storeName,
    categoryName: item.categoryName,    // 新增
    material: item.material,            // 新增
    weight: item.weight,                // 新增
    description: item.description,      // 新增
  } as ProductItem,
```

---

### 任务 9：Cart.vue 切换选中时同步到服务端

**问题**：`onCheckChange` 只改本地状态。

**修复步骤**：
```
打开 shop/src/views/Cart.vue 第 62-65 行
onCheckChange 函数内新增：
  cartStore.toggleCheck(entry.product.id)
```

---

### 任务 10：补全 OrderRecord 类型字段

**问题**：`deliveryCompany`、`deliveryTrackNo` 缺失。

**修复步骤**：
```
打开 shop/src/types/index.ts，找到 OrderRecord 接口
新增字段：
  deliveryCompany?: string
  deliveryTrackNo?: string
  paymentTime?: string
  paymentTradeNo?: string
  receiveTime?: string
```

---

### 任务 11：首页硬编码数据改为动态

**问题**：`Home.vue` Banner 和分类图标硬编码 productId。

**修复步骤**：
```
打开 shop/src/views/Home.vue 第 155-172 行
方案 A：从 /products/recommend 响应中提取前 N 个商品作为 banner
方案 B：创建 /api/promotions/banners 端点返回配置数据
方案 C：暂时保留硬编码，标记 TODO
```

---

### 任务 12：Search.vue 热搜词改为动态

**问题**：热搜标签硬编码，`hotSearchKeywords()` 未调用。

**修复步骤**：
```
打开 shop/src/views/Search.vue
1. 新增: import { productApi } from '@/api/product'
2. onMounted 中调用:
   productApi.hotSearchKeywords().then(tags => hotTags.value = tags).catch(() => {})
```

---

### 任务 13：登录后同步本地收藏到服务端

**问题**：`Product.vue` 本地收藏未上传。

**修复步骤**：
```
打开 shop/src/views/Product.vue 或 stores/useUserStore.ts
在 setAuth() 登录成功后新增:
  const favs = JSON.parse(localStorage.getItem('zbt_favs') || '[]') as string[]
  if (favs.length > 0) {
    for (const id of favs) {
      favoriteApi.add(id).catch(() => {})
    }
    localStorage.removeItem('zbt_favs')
  }
```

---

### 任务 14：Favorites.vue 登录后不 fallback localStorage

**问题**：登录后 `favoriteApi.list()` 为空时显示 localStorage 旧数据。

**修复步骤**：
```
打开 shop/src/views/Favorites.vue 第 34-50 行
在 if (userStore.isLoggedIn) 分支内，API 返回空时直接 return []，不 fallback
```

---

### 任务 15：Promotions.vue 状态枚举统一

**问题**：`status === 'ongoing'` 小写硬编码。

**修复步骤**：
```
打开 shop/src/views/Promotions.vue 第 8 行
改为: p.status?.toUpperCase() === 'ONGOING'
```

---

### 任务 16：登录成功后回跳原页面

**问题**：路由守卫跳 Profile 后不记录来源。

**修复步骤**：
```
打开 shop/src/router/index.ts 第 126-128 行
return '/profile' 改为:
  return `/profile?redirect=${encodeURIComponent(to.fullPath)}`

打开 shop/src/views/Profile.vue
登录成功后读取 route.query.redirect，有则 router.push(redirect)
```

---

## 🔵 第三批：体验优化（P2 — 迭代处理）

### 任务 17：Product.vue 添加 loading 骨架屏
```
打开 views/Product.vue，在 v-if="item" 前加 <van-skeleton :loading="loading"> 包裹
```

### 任务 18：Product.vue costPrice 判断修正
```
第 26 行: v-if="item.costPrice && item.costPrice > 0"
改为: v-if="item.costPrice != null && item.costPrice > 0"
```

### 任务 19：Notifications.vue 加无限滚动分页
```
导入 van-list，参考 OrderList.vue 实现 onLoad 分页
```

### 任务 20：AiGuide.vue 限制 max-width
```
输入栏 CSS 中加 max-width: var(--max-width); margin: 0 auto;
```

### 任务 21：About.vue 手机号正则更新
```
第 165 行: /^1[3456789]\d{9}$/ 改为 /^1[3-9]\d{9}$/
```

### 任务 22：token.ts setToken 重命名
```
setToken 改名为 setLoginFlag，去掉无用参数 _token
```

### 任务 23：Store 类型对齐
```
useAddressStore 返回类型直接匹配 AddressItem[]，去掉 views 中的 as unknown as 双重转换
```

### 任务 24：清理死代码
```
- api/services.ts: getActivities() 如果确认不用则删除
- api/order.ts: 与 shop/services.ts 中的 shopOrderApi 合并或加注释区分
- stores/useOrderStore.ts: 清理未被赋值的 serverOrders
```

### 任务 25：Checkout.vue price 类型安全
```
checkedTotal 计算时加 Number(entry.product.price) 显式转换
```

---

## 修复顺序建议

```
第 1 天: 任务 1 → 2 → 3 → 4（核心阻塞）
第 2 天: 任务 5 → 6 → 7 → 8 → 9（存储和类型）
第 3 天: 任务 10 → 11 → 12 → 13 → 14（数据和同步）
第 4 天: 任务 15 → 16（状态一致性）
第 5 天: 任务 17-25（体验优化批量处理）
```

## 影响范围

| 涉及文件数 | ~18 个 |
|-----------|--------|
| 需删除文件 | `utils/token.js`，可选删 `views/Goods.vue` `views/Order.vue` `views/Index.vue` |
| 需新增文件 | `views/NotFound.vue`（可选） |
| 回归风险 | 购物车/订单模块改动较大，修复后需全流程回归测试 |
