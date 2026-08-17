# 珠宝通后台管理系统（frontend）

珠宝通零售连锁门店管理系统的后台管理端，面向门店管理者/总部的运营管理平台。

---

## 技术栈

- Vue 3（`<script setup>`）
- Ant Design Vue 4
- Vite
- Pinia（状态管理）
- Vue Router 4
- TypeScript
- Less

---

## 功能模块

- **商品管理**：商品、分类、品牌
- **库存管理**：库存列表、盘点、预警
- **订单管理**：订单列表、详情、退换货
- **客户管理**：客户档案、会员管理
- **供应商管理** / **证书管理**
- **采购管理**：采购列表、采购申请
- **销售管理**：销售录入、销售报表
- **财务管理**：收支流水
- **数据报表**：数据驾驶舱、门店评分、排行
- **营销管理**：营销活动、促销
- **任务中心**：任务列表、创建、模板、审核
- **人效管理**：晨夕会、面谈、考核、复盘
- **场景运营**：卫生巡检、陈列检查、物料更新、设备检查
- **AI 智能辅助**：智能问答、智能建议、数据分析、文档生成
- **系统管理**：组织、门店、用户、角色、配置
- **日志管理**：操作日志查询导出

---

## 快速开始

```bash
cd frontend
npm install
npm run dev
```

开发地址：`http://localhost:5000`

---

## 构建部署

```bash
npm run build   # 产物在 dist/，部署到 /admin/ 子路径
```

> `vite.config.ts` 已配置 `base: '/admin/'`，生产部署在 Nginx 的 `/admin/` 路径下。

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

- 登录后 Cookie `zbt_admin_token`（HttpOnly，防 XSS）
- 仅管理员/内部员工角色可访问（顾客账号会被拦截）
