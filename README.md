# 珠宝通（zb-tong）零售连锁门店主动式管理系统

珠宝行业零售连锁门店的管理系统，包含**后台管理系统**、**C 端商城**和**后端服务**三端。

---

## 项目结构

```
zbtProject/
├── backend/                # 后端服务（SpringBoot）
│   └── zb-tong-server/     # 主服务
│       ├── src/main/java/com/zhubao/manage/
│       │   ├── module/     # 业务模块（auth/user/product/order/sales/... 共 24 个）
│       │   ├── common/     # 公共组件（安全、拦截器、统一响应）
│       │   └── infrastructure/  # 基础设施（MyBatis 插件、AOP）
│       └── src/main/resources/
│           ├── db/migration/    # Flyway 数据库迁移脚本
│           ├── application.yml  # 基础配置
│           ├── application-dev.yml  # 开发环境
│           └── application-prod.yml # 生产环境
├── frontend/               # 后台管理系统（Vue3 + Ant Design Vue）
│   └── src/
│       ├── views/          # 页面（商品/库存/订单/销售/财务/报表/营销/任务/人效/场景/AI/系统/日志）
│       ├── api/            # API 封装
│       ├── stores/         # Pinia 状态管理
│       └── router/         # 路由配置
├── shop/                   # C 端商城（Vue3 + Vant）
│   └── src/
│       ├── views/          # 页面（首页/分类/购物车/订单/我的/...）
│       ├── api/            # API 封装
│       └── stores/         # Pinia 状态管理
├── deploy/                 # 部署配置
│   └── nginx.conf          # Nginx 反向代理模板
├── ARCHITECTURE.md         # 架构文档
├── BACKEND_SPRINGBOOT_SETUP.md    # 后端搭建说明
└── FRONTEND_STEP_BY_STEP.md       # 前端搭建说明
```

---

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | SpringBoot 2.7.18 · MyBatis-Plus 3.5 · MySQL · Redis · Flyway · JWT · Knife4j · XXL-JOB · MinIO · EasyExcel · OpenAI |
| 管理端 | Vue 3 · Ant Design Vue 4 · Vite · Pinia · Vue Router · TypeScript |
| 商城端 | Vue 3 · Vant 4 · Vite · Pinia · TypeScript |

---

## 核心功能模块

- **商品管理**：商品 CRUD、分类、品牌、库存盘点、库存预警、商品调拨
- **销售管理**：销售录入、审核、员工/门店排行、品类结构分析
- **订单管理**：订单、退换货、发货
- **采购管理**：采购单、供应商、审批流
- **任务中心**：主动式周期任务、任务模板、动作库、审核、提醒
- **人效管理**：晨夕会、员工面谈、能力考核、绩效复盘
- **场景运营**：卫生巡检、陈列检查、物料更新、设备检查
- **客户管理**：客户档案、会员等级
- **营销管理**：营销活动、促销
- **财务管理**：收支流水
- **数据报表**：数据驾驶舱、门店评分、销售排行
- **AI 智能辅助**：智能问答、智能建议、数据分析、文档生成、任务评分
- **系统管理**：组织架构、门店、用户、角色权限、系统配置
- **C 端商城**：商品浏览、购物车、订单、收藏、收货地址
- **日志管理**：操作日志查询、导出

---

## 快速开始（开发环境）

### 前置依赖

- JDK 1.8+
- Maven 3.6+
- Node.js 16+（前端）
- MySQL 8.0
- Redis（可选，dev 环境已排除）

### 1. 启动后端

```bash
cd backend/zb-tong-server

# 初始化数据库（首次运行）
# dev 环境 Flyway 已禁用，需手动执行 migration 脚本
mysql -u root -p zb_tong_dev < src/main/resources/db/migration/*.sql

# 启动（默认 dev profile）
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端地址：`http://localhost:8080/api`（Swagger 文档：`/api/doc.html`）

### 2. 启动管理端

```bash
cd frontend
npm install
npm run dev
```

管理端地址：`http://localhost:5000`

### 3. 启动商城端

```bash
cd shop
npm install
npm run dev
```

商城端地址：`http://localhost:3001`

---

## 生产部署

详细部署步骤见 `生产上线修复清单` 完成后的部署流程：

### 1. 后端打包

```bash
cd backend/zb-tong-server
mvn clean package -DskipTests
# 产物：target/zb-tong-server-1.0.0-SNAPSHOT.jar
```

### 2. 后端启动（生产 profile）

```bash
java -jar zb-tong-server-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  -DDB_HOST=生产数据库地址 \
  -DDB_USERNAME=生产数据库用户 \
  -DDB_PASSWORD=生产数据库密码 \
  -DREDIS_HOST=生产Redis地址 \
  -DREDIS_PASSWORD=生产Redis密码 \
  -DJWT_SECRET=随机密钥 \
  -DOPENAI_API_KEY=你的OpenAI密钥
```

### 3. 前端打包

```bash
cd frontend && npm run build   # 产物 dist/，部署到 /var/www/frontend/dist/
cd shop && npm run build       # 产物 dist/，部署到 /var/www/shop/dist/
```

### 4. Nginx 配置

使用 `deploy/nginx.conf` 模板，替换域名和 SSL 证书路径后部署。

---

## 环境变量说明

| 变量 | 说明 | 必填 |
|------|------|:--:|
| `DB_HOST` | 数据库地址 | ✅ |
| `DB_USERNAME` | 数据库用户名 | ✅ |
| `DB_PASSWORD` | 数据库密码 | ✅ |
| `REDIS_HOST` | Redis 地址 | ✅ |
| `REDIS_PASSWORD` | Redis 密码 | ✅ |
| `JWT_SECRET` | JWT 签名密钥（生产必须） | ✅ |
| `OPENAI_API_KEY` | OpenAI API 密钥 | 可选 |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | MinIO 凭证 | 可选 |
| `XXL_ACCESS_TOKEN` | XXL-JOB 令牌 | 可选 |

---

## 认证说明

- 管理端登录 → Cookie `zbt_admin_token`
- 商城端登录 → Cookie `zbt_shop_token`
- JWT 通过 HttpOnly Cookie 传递（防 XSS）
- 生产环境 Cookie 带 `Secure` 标志（HTTPS）

---

## 文档

- [架构文档](ARCHITECTURE.md)
- [后端搭建说明](BACKEND_SPRINGBOOT_SETUP.md)
- [前端搭建说明](FRONTEND_STEP_BY_STEP.md)
