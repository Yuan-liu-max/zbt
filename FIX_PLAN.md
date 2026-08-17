# 珠宝通C端商城四端贯通修复计划表

> 项目根目录已存在 `SHOP_IMPLEMENTATION_PLAN.md`（需求设计阶段写的），现在这份 `FIX_PLAN.md` 是基于审计报告产物、按依赖排序的精确执行计划。

## 审计结论回顾

| 端 | 贯通率 | 断点数 |
|----|--------|--------|
| frontend (管理后台) | 100% | 0 |
| shop (C端商城) | ≈10% | 26个端点缺失 + 字段不匹配 + 权限阻断 + 3张表缺失 + 11列缺失 + 2处JS错误 |

8类根因：字段名不匹配 → 认证路径错 → 权限阻断 → 端点缺失 → 表缺失 → 列缺失 → JS错误 → 数据缺失。

---

## 第一阶段：通络（让商品能看、用户能登）

**工期**: 2天 | **目标**: 用户可注册登录、浏览商品/分类/品牌、看到真实数据

| # | 任务 | 文件 | 改动内容 | 工时 | 依赖 | 验收标准 |
|---|------|------|----------|------|------|----------|
| A1 | shop字段名对齐ProductVO | `shop/src/types/index.ts` | `ProductItem.productCode→code`、`productName→name`、`category→categoryName`、`style→brandName` | 0.5h | — | TS编译通过 |
| A2 | shop模板字段引用批量替换 | `shop/src/views/` 下全部.vue | `item.productCode→item.code`、`item.productName→item.name`、`item.category→item.categoryName`、`item.style→item.brandName` | 2h | A1 | `grep -rn "productCode\|productName" shop/src/views/` 无输出 |
| A3 | ProductVO.imageUrl 取真实值 | `backend/.../product/dto/ProductVO.java` | `vo.imageUrl = p.getImageUrl()` 替代 `null` | 0.5h | — | GET /products 返回的JSON含imageUrl字段非null |
| A4 | product表加image_url列 + Product实体加字段 | V32迁移 + `Product.java` | `ALTER TABLE product ADD COLUMN image_url VARCHAR(500)` + 实体加 `@TableField` | 0.5h | A3 | DB有列、实体有映射 |
| A5 | getProduct()统一返回ProductVO | `ProductController.java:91-94` | `return ApiResult.ok(ProductVO.from(p))` 替代 `return ApiResult.ok(p)` | 0.5h | A1 | 详情页JSON字段名与列表页一致 |
| A6 | shop分类/品牌改调ProductController内嵌端点 | `shop/src/api/product.ts` | `categoryApi.tree()` → `GET /products/categories/tree`；`brandApi.all()` → `GET /products/brands/all` | 0.5h | — | 未登录访问分类/品牌不403 |
| A7 | shop认证路径改为/auth/* | `shop/src/api/auth.ts` | 所有 `/shop/auth/*` → `/auth/*` | 0.5h | — | 注册返回JWT、登录返回JWT |
| A8 | shop登录后角色校验 | `shop/src/stores/useUserStore.ts` | `setAuth()` 检查 `roles.includes('ROLE_CUSTOMER')`，不含则Toast+logout | 0.5h | A7 | admin用户登录shop被拦截 |
| A9 | SecurityConfig增加公开端点 | `backend/.../config/SecurityConfig.java` | `.antMatchers(GET, "/products/categories/tree", "/products/brands/all").permitAll()` | 0.5h | A6 | 无需Token调通 |

**第一阶段验收**:
```
未登录访问shop首页 → 商品列表展示(名称/价格/图片)
→ 点分类图标 → 分类商品列表
→ 搜索 → 搜索结果
→ 商品详情页(标题/属性/价格/库存/图片)
→ 注册 → 登录 → 刷新页面登录态保持
```

---

## 第二阶段：筑基（购物车 + 地址）

**工期**: 2天 | **目标**: 购物车服务端持久化、收货地址CRUD

| # | 任务 | 文件 | 改动内容 | 工时 | 依赖 | 验收标准 |
|---|------|------|----------|------|------|----------|
| B1 | V33__init_shop_cart.sql | 新建迁移脚本 | `CREATE TABLE shop_cart (id, user_id, product_id, quantity, checked, created_at, updated_at, UNIQUE(user_id,product_id))` | 0.5h | — | 表创建成功 |
| B2 | V34__init_user_address.sql | 新建迁移脚本 | `CREATE TABLE user_address (id, user_id, receiver_name, receiver_phone, province, city, district, detail_address, is_default, created_at, updated_at)` | 0.5h | — | 表创建成功 |
| B3 | ShopCartController | 新建 `module/shop/` 下6个文件 | 购物车CRUD(8个端点) + 登录合并去重(取max quantity) + 勾选/全选 | 4h | B1 | Postman调通全部8端点 |
| B4 | AddressController | 新建 `module/shop/` 下5个文件 | 地址CRUD(5个端点) + 设默认(其余地址is_default置0) | 3h | B2 | Postman调通全部5端点 |
| B5 | SecurityConfig注册新端点 | `SecurityConfig.java` | `.antMatchers("/shop/cart/**", "/addresses/**").authenticated()` | 0.5h | B3,B4 | 未登录401，已登录200 |
| B6 | useCartStore补方法 + CartEntry加cartId | `shop/src/stores/useCartStore.ts` + `shop/src/types/index.ts` | `loadFromServer()`、`syncToServer()`；CartEntry加 `cartId?: number` | 2h | B3 | 登录后购物车从服务端加载 |
| B7 | Checkout.vue修复cartId | `shop/src/views/Checkout.vue:209` | `entry.cartId` 正确取值 | 0.5h | B6 | 下单传正确的cartItemIds |

**第二阶段验收**:
```
未登录加购商品A(量2)+商品B(量1) → 购物车显示2件 → 可勾选/改量/删
→ 登录 → 购物车仍有2件(合并成功)
→ 退出再登录 → 购物车仍在(服务端持久化)
→ 结算页 → 新增地址 → 设默认 → 编辑 → 删除
```

---

## 第三阶段：交易（下单→支付→收货→退货）

**工期**: 3天 | **目标**: 完整交易闭环

| # | 任务 | 文件 | 改动内容 | 工时 | 依赖 | 验收标准 |
|---|------|------|----------|------|------|----------|
| C1 | V35__ensure_order_shop_fields.sql | 新建迁移脚本 | sales_order补buyer_id/address_id/address_snapshot/payment_method/payment_time/payment_trade_no/delivery_method/delivery_company/delivery_track_no/delivery_time/receive_time/finish_time/order_type；加索引idx_buyer/idx_order_type | 1h | — | 列全部存在有默认值 |
| C2 | ShopOrderController | 新建 `module/shop/` 下5个文件 | 下单7端点: ①地址/商品校验 ②价格快照(取product.retailPrice) ③库存乐观锁扣减 ④状态机 ⑤支付模拟(幂等) ⑥取消/退货库存回滚 | 6h | C1,B3,B4 | Postman调通 |
| C3 | V36+FavoriteController | 新建迁移+4个文件 | `CREATE TABLE user_favorite` + 收藏CRUD | 2h | — | 收藏/取消/列表正常 |
| C4 | SecurityConfig注册新端点 | `SecurityConfig.java` | `.antMatchers("/shop/orders/**", "/favorites/**").authenticated()` | 0.5h | C2,C3 | 认证正确 |
| C5 | shop订单页面对接 | Checkout/OrderList/OrderDetail/ReturnApply | 对接C2的API、订单状态按钮逻辑 | 4h | C2,C4 | 页面操作正常 |

**状态机约束**:
```
PENDING_PAY ──pay()──→ PAID ──ship()──→ SHIPPED ──confirm()──→ RECEIVED ──→ FINISHED
     │                    │                  │
     └──cancel()──→ CANCELLED                └──applyReturn()──→ REFUNDING
```

**第三阶段验收**:
```
选品→加购→结算→选地址→选支付→提交订单→订单详情(待付款)
→ 取消订单 → 列表显示已取消 → 库存恢复
→ 重新下单 → 支付 → 已付款 → 库存扣减
→ 确认收货 → 已完成
→ 申请退货 → 退货记录创建
→ 收藏/取消收藏
```

---

## 第四阶段：数据填充 + 联调 + 边界

**工期**: 2天 | **目标**: 全链路无阻断、异常场景全覆盖

| # | 任务 | 文件 | 改动内容 | 工时 | 依赖 | 验收标准 |
|---|------|------|----------|------|------|----------|
| D1 | V37__init_shop_seed.sql | 新建迁移脚本 | 20条商品(图片用picsum CDN)、2个C端测试用户(customer1/customer2密码123456角色ROLE_CUSTOMER)、各1地址、3促销、2通知 | 2h | 全部 | DB有可用测试数据 |
| D2 | 全链路走查 | — | 按下表checklist逐条执行 | 4h | D1 | 全部通过 |
| D3 | 边界case修复 | — | 未登录加购→登录合并、库存不足Toast、空地址Toast、重复支付幂等、Token过期跳转、网络断开提示、空购物车结算提示、商品无货状态 | 3h | D2 | 边界不崩溃 |
| D4 | loading/empty/error状态 | `shop/src/views/` | 各页面van-skeleton(加载中)、van-empty(空数据)、错误重试 | 2h | D2 | 三种状态可见 |

**全链路走查checklist**:
```
□ 注册(customer3/customer3) → 登录 → 跳首页
□ 首页Banner/分类/促销/商品列表均正常
□ 分类→分类商品列表  □ 搜索→搜索结果
□ 商品详情→图/名/价/属性/门店/库存
□ 收藏→已收藏态→取消收藏
□ 加购(量2)+另一商品(量1)→购物车2件
□ 购物车→勾选/改量/删除
□ 全选→结算→选地址→新增地址→提交订单
□ 订单详情→取消→库存恢复
□ 重新下单→支付→确认收货
□ Tab切换(全部/待付款/已付款/已完成)
□ 通知→有新订单通知
□ 个人中心→修改资料→修改密码
□ 退出→重新登录→购物车/地址/收藏仍在
```

---

## 第五阶段：美化（可选）

**工期**: 1天

| # | 任务 | 工时 |
|----|------|------|
| E1 | 首页Banner对接后端配置 | 2h |
| E2 | 商品图片上传对接FileController | 2h |
| E3 | 下拉刷新+上拉加载更多 | 2h |
| E4 | 骨架屏loading | 1h |

---

## 依赖图

```
A1──A2──┐
A3──A4──┤
A5 ─────┤
A6──A9──┤──► 第一阶段(2天)
A7──A8──┘
         │
         ▼
B1──B3──B5──┐
B2──B4──B5──┤──► 第二阶段(2天)
B6──B7 ─────┘
         │
         ▼
C1──C2──C4──┐
C3──C4 ─────┤──► 第三阶段(3天)
C5 ─────────┘
         │
         ▼
D1──D2──D3──D4 ──► 第四阶段(2天)
         │
         ▼
      E1~E4 ────► 第五阶段(可选1天)
```

## 工时汇总

| 阶段 | 后端 | shop前端 | 数据库 | 合计 |
|------|------|---------|--------|------|
| 一 | 2.5h | 4h | 0.5h | 7h |
| 二 | 7.5h | 2.5h | 1h | 11h |
| 三 | 8.5h | 4h | 1h | 13.5h |
| 四 | — | 7h | 2h | 9h |
| 五 | — | 7h | — | 7h |
| **合计** | **18.5h** | **24.5h** | **4.5h** | **≈47.5h (约6人天)** |
