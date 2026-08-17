# 珠宝通 C 端商城（shop）

珠宝通零售连锁门店管理系统的 C 端商城，面向顾客的移动端购物平台。

---

## 技术栈

- Vue 3（`<script setup>`）
- Vant 4（移动端 UI 组件库）
- Vite 5
- Pinia（状态管理）
- Vue Router 4
- TypeScript

---

## 功能模块

- **首页**：推荐商品、分类导航、促销横幅、精选好物
- **分类**：商品分类浏览、分类筛选
- **搜索**：关键词搜索、热搜词
- **商品详情**：商品图片、价格、属性、收藏
- **购物车**：本地购物车 + 服务端同步、勾选、数量管理
- **下单结算**：收货地址、支付方式、提交订单
- **订单**：订单列表、订单详情、取消、确认收货、退换货申请
- **收藏**：商品收藏/取消收藏
- **收货地址**：地址增删改查、默认地址
- **个人中心**：登录/注册、个人信息、通知、设置
- **AI 问答**：智能客服对话

---

## 快速开始

```bash
cd shop
npm install
npm run dev
```

开发地址：`http://localhost:3001`

> 手机真机调试：手机与电脑连接同一 WiFi，访问 `http://<电脑局域网IP>:3001`

---

## 构建部署

```bash
npm run build   # 产物在 dist/，部署到 /h5/ 子路径
```

> `vite.config.ts` 已配置 `base: '/h5/'`，生产部署在 Nginx 的 `/h5/` 路径下。

---

## 环境变量

| 文件 | 说明 |
|------|------|
| `.env.development` | 开发环境 `VITE_API_BASE_URL=/api` |
| `.env.production` | 生产环境 `VITE_API_BASE_URL=/api` |

---

## API 访问

- 开发环境：通过 Vite `server.proxy` 代理 `/api` → `http://localhost:8080`
- 生产环境：通过 Nginx 反向代理 `/api` → 后端服务

---

## 认证方式

- 登录后 Cookie `zbt_shop_token`（HttpOnly，防 XSS）
- 与后台管理系统（`zbt_admin_token`）隔离，互不影响
