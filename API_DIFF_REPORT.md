# 前后端接口差异对照表 & 最终标准接口文档

> 基准源：前端 axios Mock 定义 + TypeScript 类型文件
> 对比对象：架构文档 ARCHITECTURE.md 第4节(表结构) + 第6节(API清单)
> 结论：**前端Mock与架构文档存在大量差异，必须以架构文档为准，前端Mock全量重写**

---

## 一、全局性差异（影响所有接口）

### 1.1 响应体外层包装

| 对比项 | 架构文档 | 前端Mock | 裁决 |
|--------|---------|---------|------|
| 成功响应 | `{ code: 200, msg: "success", data: {...} }` | 直接返回 `{ list, total, page, pageSize }` | **用架构文档** |
| 分页响应 | `data: { list, total, page, size }` | `{ list, total, page, pageSize }` | **用架构文档** `size` 替代 `pageSize` |
| 错误响应 | `{ code: 500, msg: "错误描述", data: null }` | 无统一错误包装 | **用架构文档** |

**后端必须返回：**
```json
// 成功
{ "code": 200, "msg": "success", "data": { "list": [...], "total": 100, "page": 1, "size": 20 } }

// 错误
{ "code": 400, "msg": "用户名不能为空", "data": null }
```

**前端 axios 拦截器改为：** `res.data` → 取 `res.data.data`，`res.data.code !== 200` 时报错。

### 1.2 分页字段名

| 架构文档 | 前端Mock | 统一标准 |
|---------|---------|---------|
| `size` | `pageSize` | **`size`** |

### 1.3 枚举值大小写

| 架构文档 | 前端Mock | 统一标准 |
|---------|---------|---------|
| `GOLD` `DIAMOND` `ON_SALE` `ACTIVE` `PENDING` | `'on'` `'open'` `'enabled'` | **全大写 `GOLD` `DIAMOND` `ON_SALE` `ACTIVE` `PENDING`** |

### 1.4 日期时间格式

| 对比项 | 架构文档 | 前端Mock | 统一标准 |
|--------|---------|---------|---------|
| 格式 | `yyyy-MM-dd HH:mm:ss` | `'2024-05-24 10:30:00'` | **`yyyy-MM-dd HH:mm:ss`**（一致） |
| 时区 | GMT+8 | 无 | **GMT+8** |

---

## 二、逐模块差异对照

### 2.1 系统管理模块（M1）

#### 2.1.1 组织架构

| 接口 | 架构文档 | 前端Mock | 差异判定 |
|------|---------|---------|---------|
| 路由 | `GET /organizations/tree` | `orgApi.getTree()` (无HTTP路径) | **补路径** |
| 返回字段 | `id, parentId, orgName, orgType, sortOrder, status, children` | `id, name, parentId, level, memberCount, children` | **全量替换** |
| orgType枚举 | `HEADQUARTERS/GREAT_REGION/REGION/STORE` | 无此字段 | **必须加** |
| CRUD | 有 POST/PUT/DELETE | 仅 getTree | **补 CRUD** |

#### 2.1.2 门店管理

| 接口 | 架构文档 | 前端Mock | 差异判定 |
|------|---------|---------|---------|
| 列表路由 | `GET /stores` | `storeApi.getList()` | **补路径** |
| 字段 | `storeId, storeName, storeCode, regionId, address, storeManagerId, openingDate, storeType, status, businessHours, contactPhone` | `id, name, code, region(string), address, contactPerson, contactPhone, status` | **全量替换** |
| 缺失字段 | `storeManagerId, openingDate, storeType(NEW/OLD/FLAGSHIP/NORMAL), businessHours` | ⚠️ 前端Mock缺少 | **必须加** |
| 多余字段 | 无 `contactPerson` | Mock有 `contactPerson` | **删除，改用 storeManagerId** |
| 状态枚举 | `OPEN/SUSPENDED/CLOSED` | `open/suspended` | **改为大写** |
| CRUD | 有 POST/PUT | 有 create/update/delete | **路径对齐** |

#### 2.1.3 用户管理

| 接口 | 架构文档 | 前端Mock | 差异判定 |
|------|---------|---------|---------|
| 列表路由 | `GET /users` | `userApi.getList()` | **补路径** |
| 字段 | `userId, username, realName, phone, avatar, roleIds[], storeId, position, entryDate, status, lastLoginAt` | `id, username, phone, department, role(string), status, createdAt` | **全量替换** |
| 缺失 | `realName, avatar, roleIds[], storeId, regionId, position, entryDate, lastLoginAt` | ⚠️ 全缺 | **必须加** |
| 多余 | — | `department` (架构文档无) | **删除** |
| 状态枚举 | `ACTIVE/RESIGNED/DISABLED` | `enabled/disabled` | **改为大写三态** |
| CRUD | 有 POST/PUT/DELETE /reset-password | 仅 getList | **补 CRUD + 重置密码** |

#### 2.1.4 角色权限

| 接口 | 架构文档 | 前端Mock | 差异判定 |
|------|---------|---------|---------|
| 字段 | `roleCode, roleName, dataScope(ALL/REGION/STORE/SELF/CUSTOM), status, remark` | `id, name, description, permissions[]` | **全量替换** |
| 缺失 | `dataScope`（核心！） | ⚠️ 无 | **必须加** |
| 权限树接口 | `GET /permissions/tree` → `{id,parentId,permName,permType(MENU/BUTTON/API),permCode,path,icon}` | `PermissionItem: {id,name,code,type,enabled}` | **补 permCode/path/icon** |
| 角色-权限关联 | `GET/PUT /roles/{roleId}/permissions` | 无 | **必须加** |

---

### 2.2 任务中心模块（M2）— 差异最大的模块

#### 核心问题：前端Mock任务模型完全不是"珠宝门店主动式任务管理系统"的任务模型

| 架构文档 | 前端Mock | 差异 |
|---------|---------|------|
| `task_instance` 有 20+ 字段 | `TaskItem` 只有 13 个字段 | **前端Mock完全错误** |
| 任务状态 12 种 | 仅 5 种 | **严重不足** |
| 任务类型按人/货/场/综合 | 按 review/approval/process/general | **业务模型完全不同** |
| 任务来源 6 种(周期/手动/总部/异常/节假日/AI) | 无此概念 | **核心功能缺失** |
| 执行→提交→审核→驳回→整改→复核 完整状态机 | 无任何流转逻辑 | **核心闭环缺失** |

**前端Mock必须全量重写为：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 任务ID |
| taskNo | string | 任务编号 TK+日期+序号 |
| templateId | BIGINT | 来源模板ID |
| taskTitle | string | 任务标题 |
| dimension | `HUMAN \| PRODUCT \| SCENE \| COMPREHENSIVE` | 维度 |
| category | string | 分类(晨会/面谈/盘点/巡检等) |
| storeId | BIGINT | 所属门店 |
| storeName | string | 门店名称 |
| assigneeId | BIGINT | 执行人ID |
| assigneeName | string | 执行人姓名 |
| auditorId | BIGINT | 审核人ID |
| auditorName | string | 审核人姓名 |
| startTime | datetime | 开始时间 |
| dueTime | datetime | 截止时间 |
| completedTime | datetime | 完成时间 |
| status | `PENDING \| READY \| IN_PROGRESS \| SUBMITTED \| AUDITING \| APPROVED \| COMPLETED \| REJECTED \| RECTIFYING \| OVERDUE \| CANCELLED \| VOIDED \| EXEMPTED` | 12种状态 |
| priority | `LOW \| MEDIUM \| HIGH \| URGENT` | 优先级 |
| sourceType | `CYCLE \| MANUAL \| HQ \| ABNORMAL \| HOLIDAY \| AI` | 来源类型 |
| isOverdue | boolean | 是否超时 |
| overdueMinutes | number | 超时分钟 |
| qualityScore | number | 执行质量分 |
| aiScore | number | AI评分 |
| manualScore | number | 人工评分 |
| finalScore | number | 最终得分 |
| createdAt | datetime | 创建时间 |

#### 任务提交（task_submission）必须加：

| 字段 | 类型 | 说明 |
|------|------|------|
| submissionId | BIGINT | 提交ID |
| taskId | BIGINT | 任务ID |
| textContent | string | 文字说明 |
| formData | JSON | 表单数据 |
| photoUrls | string[] | 图片URL列表 |
| attachmentUrls | string[] | 附件URL列表 |
| location | `{lat, lng}` | GPS定位 |
| submittedAt | datetime | 提交时间 |

#### 任务审核（task_audit）必须加：

| 字段 | 类型 | 说明 |
|------|------|------|
| auditId | BIGINT | 审核ID |
| auditorName | string | 审核人 |
| auditResult | `APPROVED \| REJECTED \| RECTIFY` | 审核结果 |
| auditComment | string | 审核意见 |
| score | number | 评分 0-100 |
| auditedAt | datetime | 审核时间 |

---

### 2.3 人效管理模块（M3）

#### 晨夕会

| 架构文档字段 | 前端Mock字段 | 裁决 |
|------------|-------------|------|
| `meetingType: MORNING/EVENING` | `type: regular/temporary` | **改为 MORNING/EVENING** |
| `meetingDate` | `meetingDate` | 保留 |
| `storeTargetAmount` | ❌ 无 | **必须加** |
| `mainProducts` | ❌ 无 | **必须加** |
| `keyCustomers` | ❌ 无 | **必须加** |
| `todayStrategy` | ❌ 无 | **必须加** |
| `employeeTargets` (JSON表格) | ❌ 无 | **必须加** |
| `meetingPhotoUrls` | ❌ 无 | **必须加** |
| `actualSalesAmount`(夕会用) | ❌ 无 | **必须加** |
| `targetCompletionRate`(夕会用) | ❌ 无 | **必须加** |
| `successfulCases`(夕会用) | ❌ 无 | **必须加** |
| `failedCases`(夕会用) | ❌ 无 | **必须加** |
| `tomorrowImprovement`(夕会用) | ❌ 无 | **必须加** |
| `host` | `host` | 保留 |
| `topic` | ⚠️ 架构文档无此字段 | **删除，改用 meetingType 区分** |

#### 员工面谈

| 架构文档字段 | 前端Mock字段 | 裁决 |
|------------|-------------|------|
| `employeeId` | `interviewee` (只有姓名) | **改为 employeeId + employeeName** |
| `interviewerId` | `interviewer` | **改为 interviewerId + interviewerName** |
| `interviewDate` | `interviewDate` | 保留 |
| `currentWeekSales` | ❌ 无 | **必须加** |
| `targetCompletionRate` | ❌ 无 | **必须加** |
| `mainProblem` | ❌ 无 | **必须加** |
| `customerFollowIssue` | ❌ 无 | **必须加** |
| `productKnowledgeGap` | ❌ 无 | **必须加** |
| `mindsetStatus: POSITIVE/NORMAL/LOW/ABNORMAL` | ❌ 无 | **必须加** |
| `nextWeekGoal` | ❌ 无 | **必须加** |
| `improvementPlan` | ❌ 无 | **必须加** |
| `managerComment` | ❌ 无 | **必须加** |
| `employeeFeedback` | ❌ 无 | **必须加** |
| `topic` | `topic` | **删除，改为 interviewDate+employeeId 唯一标识** |

#### 能力考核

| 架构文档字段 | 前端Mock字段 | 裁决 |
|------------|-------------|------|
| `assessmentWeek` | `period` | **改为 assessmentWeek** |
| `productKnowledgeScore(25)` | ❌ 无 | **必须加** |
| `matchingSkillScore(20)` | ❌ 无 | **必须加** |
| `receptionScore(20)` | ❌ 无 | **必须加** |
| `objectionHandlingScore(20)` | ❌ 无 | **必须加** |
| `promotionScriptScore(15)` | ❌ 无 | **必须加** |
| `totalScore` | ❌ 无 | **必须加(自动计算)** |
| `improvementAdvice` | ❌ 无 | **必须加** |

---

### 2.4 货品管理模块（M4）

#### 商品档案

| 架构文档字段 | 前端Mock字段 | 裁决 |
|------------|-------------|------|
| `productCode` | `code` | **改为 productCode** |
| `productName` | `name` | **改为 productName** |
| `category: GOLD/DIAMOND/K_GOLD/ANCIENT/SILVER/GEM/OTHER` | `categoryId + categoryName(扁平)` | **改为枚举值 category** |
| `style` (款式) | ❌ 无 | **必须加** |
| `material` (材质) | ❌ 无 | **必须加** |
| `weight` (重量) | ❌ 无 | **必须加** |
| `size` (手寸/圈号) | ❌ 无 | **必须加** |
| `color` (颜色) | ❌ 无 | **必须加** |
| `shape` (形状) | ❌ 无 | **必须加** |
| `meaning` (寓意) | ❌ 无 | **必须加** |
| `costPrice` | `costPrice` | 保留(权限控制) |
| `retailPrice` | `price` | **改为 retailPrice** |
| `grossMarginRate` | `grossMarginRate` | 保留(权限控制) |
| `status: ON_SALE/SOLD/TRANSFER/REPAIR/OFF_SHELF` | `status: on/off` | **改为5态枚举** |
| `storeId` | `storeId` | 保留 |
| ❌ 无 | `stock` | **删除，库存独立管理** |

#### 盘点管理

| 架构文档字段 | 前端Mock字段 | 裁决 |
|------------|-------------|------|
| `checkDate` | `startDate + endDate` | **改为 checkDate** |
| `storeId` | `warehouse(字符串)` | **改为 storeId** |
| `checkedBy` | `creator` | **改为 checkedBy** |
| `totalCheckedCount` | ❌ 无 | **必须加** |
| `abnormalCount` | ❌ 无 | **必须加** |
| `abnormalItems` (JSON) | ❌ 无 | **必须加** |
| `photos` | ❌ 无 | **必须加** |
| `remark` | ❌ 无 | **必须加** |

---

### 2.5 业绩数据模块（M6）

| 架构文档字段 | 前端Mock字段 | 裁决 |
|------------|-------------|------|
| `salesNo` | `orderCode` | **改为 salesNo** |
| `customerGender: MALE/FEMALE/UNKNOWN` | ❌ 无 | **必须加** |
| `customerAgeRange: 18-25/26-35/36-45/46+` | ❌ 无 | **必须加** |
| `customerConcern` | ❌ 无 | **必须加** |
| `salesPhotoUrls` | ❌ 无 | **必须加** |
| `productCount` | ❌ 无 | **必须加** |
| `auditStatus: PENDING/AUDITED/REJECTED` | `status: pending/approved/rejected` | **改为 auditStatus + AUDITED** |
| SalesItem 字段 | 前端仅有 productName/category/spec/price/quantity/amount | **必须加: style/material/weight/size/color/shape/meaning/customerFavoritePoint/objection/closingReason/productPhotoUrls** |

---

### 2.6 场景运营模块（M5）

架构文档5张独立场景表，前端Mock全部用一个 `InspectionItem` 结构。**必须拆分为5个独立类型：**

| 表 | 核心字段 | 前端Mock现状 |
|----|---------|------------|
| scene_health_inspection | inspectionTime(MORNING/MIDDAY/EVENING), inspectionDate, inspectorId, storeId, areaResults(JSON), issueDescription, photoUrls, rectificationRequired | 没有这些字段 |
| scene_display_inspection | displayArea, standardScore, beforePhotos, afterPhotos, rectificationPlan | 没有这些字段 |
| scene_material_update | materialType(POSTER/FLAG/STAND/CARD), currentStatus(NORMAL/EXPIRED/DAMAGED/MISSING), replacementRequired | 没有这些字段 |
| scene_equipment_check | equipmentType(LIGHT/AC/CAMERA/AUDIO/POS/CABINET_LIGHT/SAFE/NETWORK), status(NORMAL/ABNORMAL), repairRequired | 没有这些字段 |
| scene_customer_experience_review | feedbackCount, complaintCount, commonFeedback, improvementPlan, responsiblePersonId | 没有此表 |

---

## 三、接口路由路径差异

架构文档中定义的所有路由未在前端Mock中体现（Mock是函数级调用，无HTTP路径映射）。**按架构文档第6节补全所有路由路径。**

关键路由速查：

| 模块 | 路由前缀 | 接口数量 |
|------|---------|---------|
| 认证 | `/auth/` | 6 |
| 组织 | `/organizations/` | 5 |
| 门店 | `/stores/` | 6 |
| 用户 | `/users/` | 7 |
| 角色 | `/roles/` | 8 |
| 动作库 | `/actions/` | 7 |
| 任务模板 | `/task-templates/` | 6 |
| 任务实例 | `/tasks/` | 15 |
| 人效 | `/human/` | 18 |
| 货品 | `/products/` | 14 |
| 场景 | `/scene/` | 10 |
| 销售 | `/sales/` | 11 |
| AI | `/ai/` | 10 |
| 报表 | `/reports/` | 12 |
| 通知 | `/notifications/` | 5 |
| 文件 | `/files/` | 5 |
| 日志 | `/logs/` | 1 |

---

## 四、最终标准接口文档

### 4.1 统一规范

```yaml
请求前缀: /api
响应格式:
  成功: { "code": 200, "msg": "success", "data": <T> }
  失败: { "code": <ErrorCode>, "msg": "<错误描述>", "data": null }
分页格式:
  请求: { "page": 1, "size": 20, "orderBy": "createdAt", "orderDir": "DESC" }
  响应: { "code": 200, "msg": "success", "data": { "list": [...], "total": 100, "page": 1, "size": 20 } }
认证: Header Authorization: Bearer <jwt_token>
日期: yyyy-MM-dd HH:mm:ss, 时区 GMT+8
枚举: 全大写字符串, 如 GOLD / ACTIVE / PENDING / ON_SALE
空值: JSON null, 前端用 ?. 和 ?? 处理
```

### 4.2 核心接口（严格以此为准）

完整的 146 个接口定义见 [ARCHITECTURE.md](./ARCHITECTURE.md) 第6节。以下是与前端Mock差异最大、最需要后端优先实现的核心接口：

#### 认证
```
POST   /api/auth/login              { username, password }                    → { token, user:{userId,realName,role,storeId,regionId,avatar} }
GET    /api/auth/me                                                           → { userId,realName,role,storeId,regionId,permissions:[] }
POST   /api/auth/logout
```

#### 任务中心（核心）
```
GET    /api/tasks/my                  ?status&dimension&page&size              → PageResult<TaskInstance>
GET    /api/tasks/{id}                                                          → TaskInstance + submission + audits + timeline
POST   /api/tasks/{id}/submit         { textContent, formData, photoUrls, attachmentUrls, location }
POST   /api/tasks/{id}/audit          { auditResult, auditComment, score }
GET    /api/tasks/{id}/timeline                                                 → [{ time, action, operator }]
```

#### 销售录入
```
POST   /api/sales/records            { salesDate, storeId, totalAmount, paidAmount, customerType, customerGender,
                                       customerAgeRange, purchaseScene, customerConcern, salesPhotoUrls,
                                       items: [{ productName, category, style, material, weight, size, color, shape,
                                       meaning, price, quantity, customerFavoritePoint, objection, closingReason, productPhotoUrls }] }
```

#### 晨会/夕会
```
POST   /api/human/meetings           { meetingType:MORNING, meetingDate, storeTargetAmount, mainProducts,
                                       todayStrategy, employeeTargets:{"u001":80000}, meetingPhotoUrls }
```

#### 数据驾驶舱
```
GET    /api/reports/dashboard/hq                                                → { storeTaskCompletionRate, storeScoreDistribution, overdueDistribution, salesCompletionRank, ... }
GET    /api/reports/dashboard/store  ?storeId                                     → { taskCompletionRate, pendingAuditCount, dailySales, employeeRanking, abnormalAlerts }
GET    /api/reports/dashboard/associate                                           → { todayTasks, todaySales, monthSales, targetCompletionRate, aiAdvice }
```

完整 146 个接口定义参见 [ARCHITECTURE.md 第6节](./ARCHITECTURE.md)。

---

## 五、后端修改清单（必须逐条照做）

| # | 修改项 | 优先级 | 说明 |
|---|--------|--------|------|
| 1 | 所有响应体包裹 `{code, msg, data}` | **P0** | 不改前端连不上 |
| 2 | 分页字段 `pageSize` → `size` | **P0** | 影响所有列表页 |
| 3 | 所有枚举值改为全大写 | **P0** | 前后端统一 |
| 4 | sys_organization 字段对齐（orgName/orgType/sortOrder） | **P0** | 组织架构用不了 |
| 5 | sys_store 字段对齐（storeManagerId/openingDate/storeType/businessHours） | **P0** | 门店管理用不了 |
| 6 | sys_user 字段对齐（realName/roleIds[]/position/entryDate） | **P0** | 用户管理用不了 |
| 7 | sys_role 加 dataScope + 权限矩阵接口 | **P0** | 权限控制核心 |
| 8 | task_instance 按架构文档字段重建 | **P0** | 整个系统核心 |
| 9 | task_submission + task_audit 独立表 | **P0** | 任务闭环 |
| 10 | human 模块晨会/面谈/考核 按架构字段重建 | **P1** | 人效核心表单 |
| 11 | product 加 style/material/weight/size/color/shape/meaning | **P1** | 珠宝行业特有字段 |
| 12 | sales 加 customerGender/customerAgeRange/customerConcern/salesPhotoUrls | **P1** | 销售画像 |
| 13 | scene 拆5张独立表 | P2 | 场景管理 |
| 14 | 报表4个dashboard接口 | P2 | 驾驶舱 |

---

## 六、前端修改清单（必须逐条照做）

| # | 修改项 | 说明 |
|---|--------|------|
| 1 | `src/utils/request.ts` 拦截器改为 `res.data.code !== 200` 判断 | 适配 `{code,msg,data}` 外层 |
| 2 | 所有 `types/*.ts` 按本文第4.2节重写字段定义 | 对齐架构文档 |
| 3 | 所有 `api/mock/*.ts` 按本文第4.2节重写 mock 数据和返回结构 | 对齐架构文档 |
| 4 | 所有页面中 `pageSize` → `size` | 全局替换 |
| 5 | 所有页面中状态枚举字符串改为大写 | 全局替换 |
