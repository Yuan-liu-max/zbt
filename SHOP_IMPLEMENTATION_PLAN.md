# 珠宝通商城（Shop）全链路实现计划书

> **版本**: v1.0  
> **日期**: 2026-08-06  
> **目标**: 模仿淘宝/京东购物逻辑，打通 shop → backend → DB 全链路，完成珠宝通C端商城  
> **预计工期**: 15~20 人天（含联调测试）

---

## 一、项目现状诊断

### 1.1 已有资产

| 层级 | 已有内容 | 完成度 |
|------|---------|--------|
| **shop/ 前端** | Vue3 + Vant 移动端商城，18个页面，路由/Store/Types 定义完整 | 页面骨架 70%，数据流 20% |
| **backend/ API** | 27个 Controller，覆盖商品/订单/用户/营销/通知/文件等 | API 骨架 80%，C端适配 30% |
| **DB 迁移** | 31个 Flyway 脚本，完整的表结构（product/order/user/customer/marketing/notification） | 表结构 85% |
| **frontend/ 后台** | 19个管理模块，可管理商品/订单/库存/用户/营销 | CRUD 功能 70% |

### 1.2 核心断层（需修复）

| 断层 | 严重度 | 现状 | 影响 |
|------|--------|------|------|
| **认证隔离** | 🔴 致命 | shop用`zbt_token`本地模拟，backend认证体系面向管理后台；没有CUSTOMER角色的注册/登录API | 用户无法真正登录购物 |
| **API权限阻断** | 🔴 致命 | `/categories/tree`、`/brands/all`、`/orders/*`、`/notifications/*` 等全部加了`@PreAuthorize` | shop调用这些接口直接401 |
| **购物车无服务端** | 🟠 严重 | Cart Store纯localStorage，无服务端持久化 | 换设备丢失，无法做营销推荐 |
| **订单模型不匹配** | 🟠 严重 | Order实体面向管理端（含customerName/customerPhone），缺少地址/支付/物流字段 | 商城下单流程走不通 |
| **商品图片缺失** | 🟠 严重 | imageUrl字段存在但无图片数据，轮播图硬编码本地路径 | 商品展示无图 |
| **无收货地址** | 🟡 中等 | 前端类型定义了AddressItem，但无后端API和数据库表 | 下单时无法选地址 |
| **无支付对接** | 🟡 中等 | Checkout页面存在，但无支付回调/支付状态流转 | 订单款无法收取 |
| **分类/品牌双重来源** | 🟡 中等 | ProductController和CategoriesController/BrandsController各有分类/品牌端点，shop调用的端点可能不对 | 数据不一致 |

---

## 二、技术架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    C端商城 (shop/)                        │
│  Vue3 + Vant4 + Pinia + Axios                           │
│  Port: 3001  代理 /api → localhost:8080                  │
│  页面: 首页/分类/搜索/商品详情/购物车/结算/订单/我的       │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP REST API
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot 后端 (zb-tong-server)           │
│  Port: 8080                                             │
│  ┌─────────────────────────────────────────────────┐    │
│  │  新增: Shop模块 (module/shop/)                    │    │
│  │  - ShopAuthController    C端登录/注册/用户信息     │    │
│  │  - ShopProductController 商品搜索/推荐/筛选        │    │
│  │  - ShopCartController    购物车 CRUD              │    │
│  │  - ShopOrderController   下单/支付/物流查询        │    │
│  │  - AddressController     收货地址管理             │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  改造: 现有Controller放开C端权限                   │    │
│  │  - CategoriesController  /categories/tree (公开)  │    │
│  │  - BrandsController      /brands/all    (公开)   │    │
│  │  - StoreController       /stores/all    (公开)   │    │
│  │  - NotificationController (C端用户可访问)          │    │
│  │  - FileController        (C端用户可上传)          │    │
│  └─────────────────────────────────────────────────┘    │
└────────────────────┬────────────────────────────────────┘
                     │ MyBatis-Plus / JDBC
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    MySQL Database                        │
│  新增表: shop_cart, user_address, order_payment,         │
│          order_logistics                                │
│  改造表: user (增加role=CUSTOMER), order (增加地址/支付/  │
│          物流字段)                                       │
└─────────────────────────────────────────────────────────┘
```

### 2.2 C端 vs B端认证隔离方案

```
┌──────────────────────────────────────────────────────┐
│              统一 JWT 认证体系                         │
│                                                      │
│  /auth/login    → 管理后台登录 (ROLE_ADMIN/HQ/...)    │
│  /auth/register → 管理后台注册                         │
│  /shop/auth/login    → 商城登录 (ROLE_CUSTOMER)       │
│  /shop/auth/register → 商城注册 (默认ROLE_CUSTOMER)    │
│                                                      │
│  Spring Security:                                     │
│  - 管理端接口: @PreAuthorize("hasAnyRole('ADMIN',...)")│
│  - C端公开接口: permitAll() 或 @PreAuthorize("permitAll()")│
│  - C端需登录: @PreAuthorize("isAuthenticated()")       │
│  - 所有角色统一用JWT Token，通过roles区分权限           │
└──────────────────────────────────────────────────────┘
```

### 2.3 商城完整购物流程（模仿淘宝/京东）

```
用户进入首页
  │
  ├─ 浏览推荐商品 / 轮播Banner → 点击进入商品详情
  ├─ 点击分类图标 → 分类商品列表
  ├─ 搜索商品 → 搜索结果页
  │
  ▼
商品详情页
  ├─ 商品大图/轮播/属性/价格/库存/门店信息
  ├─ 收藏 / 加入购物车 → 购物车数量+1
  ├─ 立即购买 → 跳转结算页(带入当前商品)
  │
  ▼
购物车页
  ├─ 商品列表(勾选/增减数量/删除) 
  ├─ 全选 / 合计金额
  ├─ 去结算 → 跳转结算页
  │
  ▼
结算/确认订单页
  ├─ 选择/新增收货地址
  ├─ 确认商品清单(数量/价格)
  ├─ 选择支付方式(微信/支付宝/余额)
  ├─ 选择配送方式(门店自提/快递)
  ├─ 使用优惠券/促销
  ├─ 提交订单 → 订单创建 → 跳转支付页/订单详情
  │
  ▼
订单详情页
  ├─ 订单状态: 待付款 → 已付款 → 已发货 → 已签收 → 已完成
  ├─ 待付款: 去支付 / 取消订单
  ├─ 已发货: 查看物流 / 确认收货
  ├─ 已完成: 申请退货/退款
  │
  ▼
订单列表页（Tab切换）
  ├─ 全部 / 待付款 / 待发货 / 待收货 / 待评价
  └─ 各状态订单卡片
```

---

## 三、分阶段实施计划

### Phase 1: 基础通络 —— 认证 + 商品数据打通（3天）

**目标**: 用户能注册登录、浏览商品、看到真实数据

#### 1.1 后端：C端认证模块（1天）

```java
// 新增: module/shop/controller/ShopAuthController.java
@RestController
@RequestMapping("/shop/auth")
public class ShopAuthController {
    // POST /shop/auth/register  C端用户注册(默认ROLE_CUSTOMER)
    // POST /shop/auth/login     C端用户登录(校验ROLE_CUSTOMER)
    // GET  /shop/auth/me        获取当前C端用户信息
    // PUT  /shop/auth/profile   更新个人信息
    // POST /shop/auth/logout    登出
}
```

要做的：
- [ ] 新增 `ShopAuthController`，与后台AuthController逻辑类似但角色限定为CUSTOMER
- [ ] User实体确保有 `CUSTOMER` 角色支持
- [ ] 注册时默认分配 ROLE_CUSTOMER
- [ ] 登录时校验用户必须是 CUSTOMER 角色

#### 1.2 后端：放开C端公开API权限（0.5天）

- [ ] `CategoriesController.tree()` → 去掉`@PreAuthorize`，改为`permitAll()`
- [ ] `BrandsController.all()` → 去掉`@PreAuthorize`，改为`permitAll()`
- [ ] `StoreController.all()` → 添加`@PreAuthorize("isAuthenticated()")`允许所有登录用户
- [ ] `ProductController.listProducts()` GET `/products` → 去掉权限限制
- [ ] `ProductController.getProduct()` GET `/products/{id}` → 去掉权限限制
- [ ] `FileController` → 允许CUSTOMER角色访问
- [ ] `NotificationController` → 允许CUSTOMER角色访问

#### 1.3 后端：商品搜索增强（0.5天）

- [ ] `ProductController.listProducts()` 增强：支持 `sortBy=price|createdAt`、`sortOrder=asc|desc`、`minPrice`/`maxPrice`、`brandId` 过滤
- [ ] 新增 `GET /products/search/hot` 热门搜索词
- [ ] 新增 `GET /products/recommend` 首页推荐商品（按销量/新品/促销）

#### 1.4 前端：对接真实API（1天）

- [ ] shop `src/api/auth.ts` → 改调 `/shop/auth/login` 和 `/shop/auth/register`
- [ ] shop `src/api/product.ts` → 确认 `/products` 和 `/categories/tree` 无401
- [ ] shop `src/views/Home.vue` → 确保商品列表从API加载、分类从API加载
- [ ] shop `src/views/Category.vue` → 分类树从 `/categories/tree` 加载
- [ ] shop `src/views/Product.vue` → 商品详情从API加载
- [ ] shop `src/views/Search.vue` → 搜索结果从API加载

**Phase 1 验收标准**:
- 用户可注册C端账号并登录
- 首页/分类/搜索/商品详情均展示真实数据
- 无401/403错误

---

### Phase 2: 购物车 + 地址体系（3天）

**目标**: 加入购物车、管理收货地址，为下单做准备

#### 2.1 后端：购物车API（1天）

新建表 `shop_cart`:
```sql
CREATE TABLE shop_cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    checked TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME DEFAULT NOW() ON UPDATE NOW(),
    UNIQUE KEY uk_user_product (user_id, product_id)
);
```

```java
// 新增: module/shop/controller/ShopCartController.java
@RestController
@RequestMapping("/shop/cart")
@PreAuthorize("isAuthenticated()")
public class ShopCartController {
    // GET    /shop/cart        获取购物车列表
    // POST   /shop/cart        添加商品到购物车 {productId, quantity}
    // PUT    /shop/cart/{id}   更新数量 {quantity}
    // PUT    /shop/cart/{id}/check  勾选/取消勾选 {checked}
    // PUT    /shop/cart/check-all   全选/取消全选 {checked}
    // DELETE /shop/cart/{id}   删除单个
    // DELETE /shop/cart        清空已勾选
    // POST   /shop/cart/sync   从客户端同步购物车(登录后合并)
}
```

- [ ] 数据库迁移脚本 V32
- [ ] ShopCart 实体 + Mapper + Service + Controller
- [ ] 登录后同步逻辑：合并localStorage中的购物车到服务端

#### 2.2 后端：收货地址API（1天）

新建表 `user_address`:
```sql
CREATE TABLE user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail_address VARCHAR(200) NOT NULL,
    is_default TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME DEFAULT NOW() ON UPDATE NOW()
);
```

- [ ] 数据库迁移脚本 V33
- [ ] UserAddress 实体 + Mapper + Service + Controller (CRUD + 设为默认)

#### 2.3 前端：购物车/地址对接（1天）

- [ ] shop `src/stores/useCartStore.ts` → 登录用户走服务端API，未登录走localStorage
- [ ] shop `src/views/Cart.vue` → 对接真实购物车API
- [ ] 新建 shop `src/api/address.ts` → 收货地址API
- [ ] 新建 shop `src/stores/useAddressStore.ts` → 收货地址状态管理
- [ ] shop `src/views/Checkout.vue` → 增加地址选择/新增逻辑

**Phase 2 验收标准**:
- 未登录可加购到本地，登录后同步到服务端
- 购物车商品可增删改查、勾选、全选
- 可新增/编辑/删除收货地址，设置默认地址

---

### Phase 3: 下单 + 订单流转（4天）

**目标**: 完整下单流程（选品→下单→支付→物流→收货）

#### 3.1 订单模型改造（1天）

改造 `order` 表，增加商城必要字段:
```sql
ALTER TABLE `order` 
  ADD COLUMN buyer_id BIGINT COMMENT '买家用户ID',
  ADD COLUMN address_id BIGINT COMMENT '收货地址ID',
  ADD COLUMN address_snapshot TEXT COMMENT '下单时地址快照(JSON)',
  ADD COLUMN payment_method VARCHAR(30) COMMENT '支付方式: WECHAT/ALIPAY/BALANCE',
  ADD COLUMN payment_time DATETIME COMMENT '支付时间',
  ADD COLUMN payment_trade_no VARCHAR(64) COMMENT '第三方支付流水号',
  ADD COLUMN delivery_method VARCHAR(30) COMMENT '配送方式: EXPRESS/SELF_PICKUP',
  ADD COLUMN delivery_company VARCHAR(50) COMMENT '物流公司',
  ADD COLUMN delivery_track_no VARCHAR(64) COMMENT '物流单号',
  ADD COLUMN delivery_time DATETIME COMMENT '发货时间',
  ADD COLUMN receive_time DATETIME COMMENT '签收时间',
  ADD COLUMN finish_time DATETIME COMMENT '完成时间',
  ADD COLUMN order_type VARCHAR(20) DEFAULT 'SHOP' COMMENT '订单类型: SHOP/MANUAL';
```

订单状态机:
```
待付款(PENDING_PAY) 
  → 已付款(PAID) [支付成功]
  → 已发货(SHIPPED) [录入物流]  
  → 已签收(RECEIVED) [确认收货]
  → 已完成(FINISHED) [超时自动/手动确认]
  
待付款(PENDING_PAY) → 已取消(CANCELLED) [超时/手动取消]
已付款(PAID) → 退款中(REFUNDING) → 已退款(REFUNDED) / 已拒绝(REFUND_REJECTED)
```

#### 3.2 后端：下单API（1天）

```java
// 新增: module/shop/controller/ShopOrderController.java
@RestController
@RequestMapping("/shop/orders")
@PreAuthorize("isAuthenticated()")
public class ShopOrderController {
    // POST   /shop/orders             提交订单
    // GET    /shop/orders             我的订单列表(分页+状态筛选)
    // GET    /shop/orders/{id}        订单详情
    // PUT    /shop/orders/{id}/cancel 取消订单
    // PUT    /shop/orders/{id}/pay    模拟支付(开发阶段)
    // GET    /shop/orders/{id}/logistics  查询物流信息
    // PUT    /shop/orders/{id}/confirm-receive  确认收货
}
```

提交订单请求体:
```json
{
  "cartItemIds": [1, 2, 3],       // 从购物车结算
  "addressId": 1,                  // 收货地址
  "paymentMethod": "WECHAT",       // 支付方式
  "deliveryMethod": "EXPRESS",     // 配送方式
  "couponCode": "COUPON2024",      // 优惠券码(可选)
  "remark": "请发顺丰"             // 备注(可选)
}
```

下单流程:
```
1. 校验地址、商品库存、商品状态(on)
2. 计算金额: 商品总价 + 运费 - 优惠券
3. 创建订单记录(order + order_items)  
4. 扣减库存
5. 清空购物车中已购商品
6. 返回订单号 → 前端跳转支付页
```

#### 3.3 后端：退换货增强（0.5天）

- [ ] 现有 `/orders/returns` 端点适配C端角色
- [ ] 增加 `POST /shop/orders/{id}/apply-return` 用户申请退款/退货
- [ ] 退款原因枚举：不喜欢/质量不好/发错货/少件/破损/其他

#### 3.4 前端：完整下单流程（1.5天）

- [ ] shop `src/views/Checkout.vue` → 完整的结算页：
  - 地址选择/新增组件
  - 商品清单确认（含数量/单价/小计）
  - 支付方式选择（微信/支付宝/余额）
  - 配送方式选择
  - 优惠券选择/输入
  - 费用明细（商品总计/运费/优惠/实付款）
  - 提交订单按钮
- [ ] shop `src/views/OrderDetail.vue` → 订单详情页：
  - 订单状态时序（待付款→已付款→已发货→已签收→已完成）
  - 物流信息查询
  - 操作按钮（取消/付款/确认收货/申请退货）
- [ ] shop `src/views/OrderList.vue` → Tab切换（全部/待付款/待发货/待收货）
- [ ] shop `src/views/ReturnApply.vue` → 退货/退款申请

**Phase 3 验收标准**:
- 完整下单流程跑通：选商品→加购→结算→下单
- 订单状态流转正确
- 取消订单/申请退货功能正常

---

### Phase 4: 营销 + 通知 + 个人中心（2天）

**目标**: 促销可见、通知触达、个人中心完善

#### 4.1 营销活动展示（0.5天）

- [ ] `MarketingController` 的 GET `/promotions` 和 GET `/activities` 放开C端权限
- [ ] shop `src/views/Promotions.vue` → 促销活动列表（优惠券可领）
- [ ] 首页 Banner 对接后端配置（新建 `banner` 表或使用系统配置表）

#### 4.2 通知消息（0.5天）

- [ ] `NotificationController` 适配C端（当前已支持，只需确认权限）
- [ ] shop `src/views/Notifications.vue` → 对接真实API
- [ ] 订单状态变更自动发通知（下单成功/发货提醒/退款处理）

#### 4.3 个人中心完善（1天）

- [ ] shop `src/views/Profile.vue` → 展示用户信息/订单统计/入口列表
- [ ] shop `src/views/Settings.vue` → 修改个人信息/修改密码
- [ ] 收藏功能对接后端（新建 `user_favorite` 表）
  ```sql
  CREATE TABLE user_favorite (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      user_id BIGINT NOT NULL,
      product_id BIGINT NOT NULL,
      created_at DATETIME DEFAULT NOW(),
      UNIQUE KEY uk_user_product (user_id, product_id)
  );
  ```
- [ ] shop `src/views/Favorites.vue` → 我的收藏

**Phase 4 验收标准**:
- 促销列表正常展示
- 通知消息可查看，订单通知自动推送
- 个人资料可修改，收藏功能正常

---

### Phase 5: 优化 + 全链路联调（3天）

**目标**: 样式还原、异常处理、性能优化、全链路走通

#### 5.1 UI/UX 优化（1天）

- [ ] 商品卡片骨架屏 loading
- [ ] 空状态占位图（无商品/无订单/无地址）
- [ ] 错误重试按钮
- [ ] 下拉刷新（商品列表/订单列表）
- [ ] 上拉加载更多

#### 5.2 异常场景覆盖（0.5天）

- [ ] 网络断开时 Toasts 提示
- [ ] Token 过期自动跳转 Profile 页（引导重新登录）
- [ ] 下单时库存不足的 Toast
- [ ] 重复提交订单的防重
- [ ] 接口超时重试

#### 5.3 种子数据填充（0.5天）

- [ ] 新增迁移脚本 V34，插入C端测试数据：
  - 20~50条真实珠宝商品数据（含imageUrl、material、weight、size）
  - 3~5条促销活动数据
  - 2~3个测试用户（customer1/customer2）
  - 测试收货地址

#### 5.4 全链路走查清单（1天）

- [ ] 注册 → 登录 → 浏览首页 → 进入分类 → 搜索 → 商品详情 → 加入购物车
- [ ] 购物车 → 全选 → 去结算 → 选地址 → 提交订单 → 查看订单
- [ ] 订单详情 → 取消订单 → 重新下单
- [ ] 订单详情 → 确认收货 → 申请退货
- [ ] 个人中心 → 查看收藏 → 修改资料 → 查看通知
- [ ] 未登录态 → 加购 → 登录 → 购物车合并

**Phase 5 验收标准**:
- 全链路无阻断
- 正常/异常/边界场景均覆盖
- 各页面 loading/empty/error 状态正确

---

## 四、数据库迁移清单

| 编号 | 文件名 | 内容 | 所属阶段 |
|------|--------|------|----------|
| V32 | `V32__init_shop_cart.sql` | 购物车表 | Phase 2 |
| V33 | `V33__init_user_address.sql` | 收货地址表 | Phase 2 |
| V34 | `V34__alter_order_shop_fields.sql` | 订单增加地址/支付/物流/类型字段 | Phase 3 |
| V35 | `V35__init_user_favorite.sql` | 用户收藏表 | Phase 4 |
| V36 | `V36__init_shop_seed_data.sql` | C端种子数据（商品/活动/测试用户） | Phase 5 |

---

## 五、新增文件清单

### 后端新增（`backend/zb-tong-server/src/main/java/com/zhubao/manage/module/shop/`）

```
module/shop/
├── controller/
│   ├── ShopAuthController.java        # C端认证
│   ├── ShopCartController.java         # 购物车
│   ├── ShopOrderController.java        # C端订单
│   └── AddressController.java          # 收货地址
├── entity/
│   ├── ShopCart.java                   # 购物车实体
│   ├── UserAddress.java                # 地址实体
│   └── UserFavorite.java               # 收藏实体
├── mapper/
│   ├── ShopCartMapper.java
│   ├── UserAddressMapper.java
│   └── UserFavoriteMapper.java
├── service/
│   ├── ShopCartService.java
│   ├── UserAddressService.java
│   ├── UserFavoriteService.java
│   └── ShopOrderService.java           # C端下单业务逻辑
└── dto/
    ├── CreateOrderRequest.java          # 下单请求DTO
    ├── OrderDetailVO.java               # C端订单详情VO
    └── CartSyncRequest.java             # 购物车同步请求
```

### 前端新增/改造（`shop/src/`）

```
src/
├── api/
│   └── address.ts                       # 新增: 收货地址API
├── stores/
│   └── useAddressStore.ts               # 新增: 地址状态管理
├── views/
│   ├── Home.vue                         # 改造: 对接真实数据
│   ├── Product.vue                      # 改造: 收藏/加购逻辑
│   ├── Cart.vue                         # 改造: 服务端同步
│   ├── Checkout.vue                     # 改造: 完整结算流程
│   ├── OrderList.vue                    # 改造: Tab+分页
│   ├── OrderDetail.vue                  # 改造: 完整订单详情
│   ├── Profile.vue                      # 改造: 用户信息展示
│   ├── Settings.vue                     # 改造: 密码修改
│   ├── Favorites.vue                    # 改造: 对接收藏API
│   └── ReturnApply.vue                  # 改造: 对接退货API
```

---

## 六、接口契约对照表

### C端公开接口（无需登录）

| Method | Path | 说明 | shop调用位置 |
|--------|------|------|-------------|
| GET | `/products` | 商品列表(支持keyword/categoryId/status/price范围/排序) | Home/Search/Category |
| GET | `/products/{id}` | 商品详情 | Product |
| GET | `/categories/tree` | 分类树 | Home/Category |
| GET | `/brands/all` | 全部品牌 | Search筛选 |
| GET | `/stores/all` | 全部门店 | Search筛选 |

### C端需登录接口

| Method | Path | 说明 | shop调用位置 |
|--------|------|------|-------------|
| POST | `/shop/auth/register` | C端注册 | Profile(未登录) |
| POST | `/shop/auth/login` | C端登录 | Profile(未登录) |
| GET | `/shop/auth/me` | 当前用户信息 | App初始化 |
| PUT | `/shop/auth/profile` | 更新个人信息 | Settings |
| GET | `/shop/cart` | 购物车列表 | Cart |
| POST | `/shop/cart` | 添加商品 | Product |
| PUT | `/shop/cart/{id}` | 更新数量 | Cart |
| DELETE | `/shop/cart/{id}` | 删除商品 | Cart |
| GET | `/addresses` | 地址列表 | Checkout |
| POST | `/addresses` | 新增地址 | Checkout |
| PUT | `/addresses/{id}` | 编辑地址 | Checkout |
| DELETE | `/addresses/{id}` | 删除地址 | Checkout |
| POST | `/shop/orders` | 提交订单 | Checkout |
| GET | `/shop/orders` | 我的订单列表 | OrderList |
| GET | `/shop/orders/{id}` | 订单详情 | OrderDetail |
| PUT | `/shop/orders/{id}/cancel` | 取消订单 | OrderDetail |
| GET | `/notifications` | 通知列表 | Notifications |
| PUT | `/notifications/{id}/read` | 标记已读 | Notifications |
| POST | `/files/upload` | 上传文件 | Profile |
| GET | `/promotions` | 促销列表 | Home/Promotions |
| POST | `/favorites` | 添加收藏 | Product |
| DELETE | `/favorites/{productId}` | 取消收藏 | Favorites |
| GET | `/favorites` | 我的收藏 | Favorites |

---

## 七、风险与对策

| 风险 | 概率 | 影响 | 对策 |
|------|------|------|------|
| 管理端已有订单数据与新字段不兼容 | 中 | 高 | 所有新字段设DEFAULT值，order_type默认'MANUAL'与商城订单区分 |
| 前端Vant组件版本API变动 | 低 | 中 | 锁定vant@^4.9.24，参考官方文档 |
| C端与管理端认证冲突（同端口同JWT） | 低 | 高 | 统一JWT体系，用roles区分；SecurityConfig增加C端路径白名单 |
| 商品图片缺失导致UI难看 | 高 | 中 | 种子数据包含真实图片URL或使用占位图 |
| 移动端适配问题（不同屏幕尺寸） | 中 | 低 | 使用Vant的viewport单位 + rem适配方案 |

---

## 八、执行顺序与依赖

```
Phase 1 (基础通络)
  ├─ Task 1.1: 后端 C端认证 ← 无依赖
  ├─ Task 1.2: 后端 放开C端权限 ← 无依赖
  ├─ Task 1.3: 后端 商品搜索增强 ← 无依赖
  └─ Task 1.4: 前端 对接真实API ← 依赖 Task 1.1 ~ 1.3
       ↓
Phase 2 (购物车+地址)
  ├─ Task 2.1: 后端 购物车API ← 依赖 Phase 1 认证
  ├─ Task 2.2: 后端 收货地址API ← 依赖 Phase 1 认证
  └─ Task 2.3: 前端 购物车/地址对接 ← 依赖 Task 2.1, 2.2
       ↓
Phase 3 (下单+订单)
  ├─ Task 3.1: 订单模型改造 ← 依赖 Phase 1
  ├─ Task 3.2: 后端 下单API ← 依赖 Task 3.1, Phase 2
  ├─ Task 3.3: 后端 退换货增强 ← 依赖 Task 3.1
  └─ Task 3.4: 前端 下单流程 ← 依赖 Task 3.2, 3.3
       ↓
Phase 4 (营销+通知+个人中心)
  ├─ Task 4.1: 营销展示 ← 依赖 Phase 1
  ├─ Task 4.2: 通知消息 ← 依赖 Phase 3 订单
  └─ Task 4.3: 个人中心完善 ← 依赖 Phase 1~3
       ↓
Phase 5 (优化+联调)
  └─ 全链路联调 ← 依赖 Phase 1~4
```

---

## 九、技术规范约定

### 后端
- 包路径：`com.zhubao.manage.module.shop`
- 响应格式：统一使用 `ApiResult<T>`（code=200成功 / code=500失败 / code=401未登录）
- 分页：`PageDTO(pageNum, pageSize)` 入参 → `PageResult(page, size, total, list)` 出参
- 认证：`@PreAuthorize("isAuthenticated()")` 用于需登录接口
- 用户ID获取：`userContextHolder.getUserId()`
- 数据库迁移：Flyway，命名 `V{序号}__{描述}.sql`

### 前端 (shop)
- 组件库：Vant 4.x
- 状态管理：Pinia (Composition API 风格)
- HTTP：Axios，baseURL `/api`，已配置Token拦截器
- 路由守卫：`meta.needLogin` 标记需登录页面
- localStorage key: `zbt_token`(JWT) / `zbt_user`(用户信息JSON) / `zbt_cart`(离线购物车)

### 测试
- 使用 [测试用户/种子数据] 验证
- 开发阶段支付回调使用模拟端点
- CORS：shop(3001) → backend(8080) 通过vite proxy代理

---

## 十、完成定义（Definition of Done）

- [x] 用户能用手机号/用户名注册C端账号并登录
- [x] 首页展示真实商品数据、分类数据、Banner
- [x] 商品可按分类/关键词/价格区间搜索筛选
- [x] 商品详情页完整展示（图片/属性/价格/库存/门店）
- [x] 购物车支持添加/删除/改数量/勾选
- [x] 登录后购物车自动同步到服务端
- [x] 收货地址可增删改查，支持默认地址
- [x] 完整下单流程：选品→结算→下单→查看订单
- [x] 订单状态流转：待付款→已付款→已发货→已签收→已完成
- [x] 可取消订单、可申请退货/退款
- [x] 消息通知可查看、订单状态变更自动通知
- [x] 个人资料可修改、密码可修改
- [x] 所有页面 loading / empty / error 状态覆盖
- [x] 全链路无401/403/500错误
- [x] 移动端适配正常（375/414/390 主流分辨率）

---

> **文档维护**: 实施过程中如发现新的技术难点或需求变更，请及时更新本文档，保持计划与实现一致。
