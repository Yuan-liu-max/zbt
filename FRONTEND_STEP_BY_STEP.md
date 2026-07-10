# 珠宝通 PC管理端 — 前端分步执行文档

> 基于现有骨架：Vue3 + Ant Design Vue + Pinia + Axios + TypeScript + Vite
> 当前状态：路由骨架已搭建，所有页面为占位符（"页面建设中..."），Layout 基础布局已完成
> 执行方式：**一页一页做，做完一页再开始下一页，每页独立可验证**

---

## 前置：核对环境

```bash
cd /d/CompanyProject/zbtProject/frontend
npm install        # 确认依赖安装完整
npm run dev        # 确认能正常启动
```

---

## 阶段零：基础设施铺设（先做，所有页面都会用到）

### Step 0.1：完善 Axios 封装

**文件**: `src/utils/request.ts`（新建）

```
做什么:
  - 创建 axios 实例，baseURL = '/api/v1'
  - 请求拦截器: 从 localStorage 读取 token，注入 Authorization header
  - 响应拦截器: 统一处理 401 跳转登录、错误 toast 提示
  - 封装 get / post / put / delete 方法
  - 导出类型安全的请求函数

验证方式: 浏览器控制台 import 后能正常创建请求
```

### Step 0.2：TypeScript 类型定义

**文件**: 逐个新建 `src/types/` 下的文件

| 顺序 | 文件 | 做什么 |
|------|------|--------|
| 1 | `src/types/common.d.ts` | 定义 ApiResult\<T\>, PageResult\<T\>, PageDTO, 所有枚举的 TS 类型 |
| 2 | `src/types/user.d.ts` | User, Role, Permission, Organization, Store 接口 |
| 3 | `src/types/task.d.ts` | TaskTemplate, TaskInstance, TaskSubmission, TaskAudit 接口 |
| 4 | `src/types/goods.d.ts` | 已有骨架，补充 Product, InventoryCheck 等完整字段 |
| 5 | `src/types/sales.d.ts`（新建）| SalesRecord, SalesItem, 统计指标接口 |

### Step 0.3：Pinia Store 初始化

| 顺序 | 文件 | 做什么 |
|------|------|------|
| 1 | `src/stores/useAuthStore.ts`（新建）| state: token/userInfo/permissions; actions: login/logout/fetchUserInfo |
| 2 | `src/stores/useAppStore.ts`（新建）| state: collapsed/sidebarTheme; 侧边栏折叠 |
| 3 | `src/stores/useTaskStore.ts`（新建）| state: taskList/filters; actions: fetchTasks |
| 4 | `src/stores/useNotificationStore.ts`（新建）| state: unreadCount; actions: fetchUnreadCount |

### Step 0.4：公共组件

| 顺序 | 文件 | 做什么 |
|------|------|------|
| 1 | `src/components/common/ImageUploader.vue`（新建）| 多图上传 + 预览 + 删除，调用 /files/upload |
| 2 | `src/components/common/ImageViewer.vue`（新建）| 点击放大 + 左右切换（用 antd 的 Image 组件即可）|
| 3 | `src/components/common/StoreSelect.vue`（新建）| 门店下拉选择器，调 /stores 接口 |
| 4 | `src/components/common/UserSelect.vue`（新建）| 用户下拉选择器，调 /users/store-staff |
| 5 | `src/components/common/OrganizationTree.vue`（新建）| 组织架构树选择器，调 /organizations/tree |
| 6 | `src/components/common/TaskStatusTag.vue`（新建）| 根据状态码渲染不同颜色标签 |
| 7 | `src/components/common/DynamicForm.vue`（新建）| JSON Schema 驱动动态表单（第8-14页会大量用到）|

---

## 阶段一：登录 + 系统管理（P0 基础）

### Page 1：登录页 —— 1个页面

**文件**: `src/views/login/LoginView.vue`（修改现有空壳）

```
UI构成:
  - 居中卡片布局（背景渐变金色 + 珠宝主题）
  - 表单: 用户名(username) + 密码(password) + 登录按钮
  - 登录成功后的角色/门店选择弹窗（如用户多角色/多门店）

调用的API:
  POST /auth/login  → 获取 token + user 信息 → 存入 useAuthStore

完成后验证:
  1. 输入任意用户名密码，点击登录，能看到请求发出
  2. 模拟后端返回 token，刷新页面后能保持登录状态
```

### Page 2：Layout 完善 —— 1个文件

**文件**: `src/layouts/DefaultLayout.vue`（修改现有）

```
做什么:
  - 确认侧边栏菜单从 router 配置自动渲染（已有）
  - 顶栏 HeaderBar: 显示当前用户名、角色、门店名、通知铃铛(带未读数红点)、退出按钮
  - 退出时清除 token 跳转登录页
  - 响应式: 小屏幕自动折叠侧边栏（已有基础）

调用的API:
  GET /auth/me          → 获取当前用户信息
  GET /notifications/unread-count → 未读通知数

完成后验证:
  1. 登录后能看到侧边栏菜单正确渲染
  2. 点击菜单能正确跳转（虽然目标页还是空壳）
  3. 退出功能正常
```

### Page 3：组织架构管理 —— 1个页面

**文件**: `src/views/system/SystemOrganization.vue`（修改现有空壳）

```
UI构成:
  - 左侧: 组织架构树（a-tree），支持展开/折叠
  - 右侧: 选中节点后的详情面板（Info + 子节点列表）
  - 顶部: 新增组织按钮 + 搜索框

组织层级: 总部 → 大区 → 区域 → 门店

必需的CRUD:
  - 新增组织: 弹窗表单(parentId, orgName, orgType, sortOrder)
  - 编辑组织: 弹窗表单(orgName, sortOrder, status)
  - 停用/启用: 开关
  - 删除: 确认弹窗（逻辑删除）

调用的API:
  GET    /organizations/tree
  POST   /organizations
  PUT    /organizations/{id}
  DELETE /organizations/{id}

完成后验证:
  1. 能看到组织树（先用 mock 数据）
  2. 能新增、编辑、删除节点
  3. 树的层级关系正确
```

### Page 4：门店管理 —— 1个页面

**文件**: `src/views/system/SystemStore.vue`（修改现有空壳）

```
UI构成:
  - 搜索栏: 区域下拉、门店状态、关键词、搜索/重置按钮
  - 表格: 门店编码、名称、区域、店长、类型、状态、开店日期、操作
  - 新增/编辑弹窗表单

表单字段: storeName, storeCode, regionId(选组织树), address, storeManagerId(选用户),
         openingDate, storeType(NEW/OLD/FLAGSHIP/NORMAL), status(OPEN/SUSPENDED/CLOSED),
         businessHours, contactPhone

调用的API:
  GET    /stores?regionId&status&keyword&page&size
  GET    /stores/{id}
  POST   /stores
  PUT    /stores/{id}

完成后验证: 门店列表分页、搜索筛选、CRUD 正常
```

### Page 5：用户管理 —— 1个页面

**文件**: `src/views/system/SystemUser.vue`（修改现有空壳）

```
UI构成:
  - 搜索栏: 门店、角色、状态、关键词
  - 表格: 用户名、姓名、手机、角色、门店、状态、最后登录、操作
  - 新增/编辑弹窗(含角色多选、门店选择)
  - 重置密码弹窗

调用的API:
  GET    /users?storeId&roleId&status&keyword&page&size
  POST   /users
  PUT    /users/{id}
  PUT    /users/{id}/reset-password

完成后验证: 用户CRUD、角色分配、密码重置
```

### Page 6：角色权限管理 —— 1个页面

**文件**: `src/views/system/SystemRole.vue`（修改现有空壳）

```
UI构成:
  - 左侧: 角色列表（表格）
  - 右侧: 选中角色 → 显示权限分配树（a-tree 带 checkbox）
          权限树节点: 菜单 > 按钮 > API
  - 数据权限范围配置(ALL/REGION/STORE/SELF/CUSTOM)

调用的API:
  GET    /roles
  POST   /roles
  PUT    /roles/{id}
  DELETE /roles/{id}
  GET    /permissions/tree
  GET    /roles/{roleId}/permissions
  PUT    /roles/{roleId}/permissions

完成后验证: 角色CRUD + 权限树勾选 + 数据权限配置
```

### Page 7：系统配置 —— 1个页面

**文件**: `src/views/system/SystemConfig.vue`（修改现有空壳）

```
UI构成:
  - 配置分组(系统参数/业务规则/通知配置)
  - 键值对表单，编辑后批量保存

调用的API:
  GET    /system/configs?configGroup
  PUT    /system/configs

完成后验证: 配置读取和保存
```

---

## 阶段二：任务中心（P0 核心）

### Page 8：动作库管理 —— 1个页面

**文件**: `src/views/task/TaskTemplate.vue` 旁边新建 `src/views/action/ActionLibrary.vue`（或复用现有 task 目录结构）

> 路由建议新增：`/action-library` → ActionLibrary.vue

```
UI构成:
  - 搜索栏: 维度(人/货/场/综合)、周期类型、状态、关键词
  - 卡片列表或表格: 动作名称、维度、周期、是否必选、是否强制、执行标准摘要、状态标签
  - 新增/编辑弹窗(字段较多，建议分步表单或折叠面板)

弹窗表单字段:
  基本信息: actionName, dimension(HUMAN/PRODUCT/SCENE/COMPREHENSIVE), category
  周期配置: frequencyType(DAILY/WEEKLY/MONTHLY/QUARTERLY/MANUAL/ABNORMAL),
            cronExpression, dueTimeRule
  执行配置: executionStandard(富文本区), requiredPhotos, requiredText,
            requireAudit, defaultAuditorRole
  表单绑定: requiredForm, formSchemaId
  适用范围: applicableStoreTypes, isForce
  评分: scoreWeight

调用的API:
  GET    /actions?dimension&frequencyType&status&keyword&page&size
  POST   /actions
  PUT    /actions/{id}
  PUT    /actions/{id}/status
  POST   /actions/{id}/dispatch   → 一键下发

完成后验证: 动作CRUD + 启停 + 一键下发弹窗
```

### Page 9：任务模板配置 —— 1个页面

**文件**: `src/views/task/TaskTemplate.vue`（修改现有空壳）

```
UI构成:
  - 表格: 模板名称、来源动作、维度、周期、是否审核、是否强制、适用门店、状态、操作
  - 新增/编辑弹窗(关联到动作库，其余字段类似动作库)

调用的API:
  GET    /task-templates?dimension&category&status&page&size
  POST   /task-templates
  PUT    /task-templates/{id}
  PUT    /task-templates/{id}/status
  POST   /task-templates/{id}/generate  → 手动触发生成

完成后验证: 模板CRUD + 启停 + 手动触发生成
```

### Page 10：任务列表（PC总部视角） —— 1个页面

**文件**: `src/views/task/TaskList.vue`（修改现有空壳）

```
UI构成:
  - 搜索栏(多条件): 门店、执行人、维度、状态、来源类型、日期范围、是否超时、关键词
  - 表格: 任务编号、标题、维度、门店、执行人、状态(彩色标签)、截止时间、优先级、是否超时(红色标记)、操作
  - 操作列: 查看详情、取消任务、作废任务
  - 分页

调用的API:
  GET    /tasks?storeId&assigneeId&dimension&status&sourceType
                &startDate&endDate&keyword&isOverdue&page&size

完成后验证: 多条件筛选 + 分页 + 状态标签正确
```

### Page 11：创建任务（PC手动/强制下发） —— 1个页面

**文件**: `src/views/task/TaskCreate.vue`（修改现有空壳）

```
UI构成:
  - 表单: 任务标题、选择模板(下拉)、选择门店(多选)、选择执行人、审核人
         开始时间、截止时间、优先级
  - 选择模板后自动带出执行标准等信息
  - 提交按钮

调用的API:
  GET    /task-templates(获取可选模板)
  GET    /stores(获取门店列表)
  GET    /users/store-staff?storeId(获取执行人)
  POST   /tasks          → 单门店任务
  POST   /tasks/batch    → 批量下发

完成后验证: 选择模板 → 选门店 → 选人 → 创建成功
```

### Page 12：任务详情 —— 1个页面（新建）

**文件**: `src/views/task/TaskDetail.vue`（新建）

> 路由新增：`/task/detail/:id` → TaskDetail.vue

```
UI构成:
  - 任务基本信息卡片: 标题、维度、分类、状态、门店、执行人、审核人、时间
  - 执行标准区域（只读）
  - 提交内容展示: 文字说明 + 图片网格 + 表单数据(只读渲染) + 附件列表
  - 审核历史时间线（a-timeline）
  - AI 建议面板（如果任务已评分）

调用的API:
  GET    /tasks/{id}
  GET    /tasks/{id}/submission
  GET    /tasks/{id}/audit-history
  GET    /tasks/{id}/timeline

完成后验证: 能看到任务从创建→执行→提交→审核的完整流转
```

### Page 13：任务审核（PC端） —— 1个页面

**文件**: `src/views/task/TaskReview.vue`（修改现有空壳）

```
UI构成:
  - 左侧列表: 待审核任务列表（每项显示标题、执行人、提交时间）
  - 右侧详情: 选中任务展示完整提交内容（图片+表单+文字）
  - 底部操作栏: 通过 / 驳回 / 整改 按钮 + 审核意见输入框 + 评分滑块(0-100)

调用的API:
  GET    /tasks/my-audit?status&page&size
  GET    /tasks/{id}
  GET    /tasks/{id}/submission
  POST   /tasks/{id}/audit   → { auditResult, auditComment, score }

完成后验证: 左列表右详情的审核流程
```

---

## 阶段三：人效管理（P1 业务表单）

### Page 14：晨夕会管理 —— 1个页面

**文件**: `src/views/human/HumanMeeting.vue`（修改现有空壳）

```
UI构成:
  - Tab切换: 晨会记录 / 夕会复盘 / 仪容仪表检查
  - 晨会Tab: 日期筛选 + 列表(日期/门店/目标/主推款/照片) + 新增按钮
  - 新增弹窗: 动态表单(绑定 form_schema 的晨会 JSON Schema)

晨会字段: meeting_date, store_target_amount, main_products, key_customers,
         today_strategy, employee_targets(表格), meeting_photo_urls(图片上传), remarks

夕会字段: meeting_date, actual_sales_amount, target_completion_rate(自动),
         successful_cases, failed_cases, customer_objections,
         tomorrow_improvement, meeting_photo_urls

调用的API:
  POST   /human/meetings
  GET    /human/meetings?storeId&meetingType&startDate&endDate&page&size
  GET    /human/meetings/{id}

完成后验证: 晨会/夕会 CRUD + 图片展示
```

### Page 15：员工面谈 —— 1个页面

**文件**: `src/views/human/HumanInterview.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 员工、日期范围
  - 列表: 员工姓名、面谈日期、本周业绩、心态状态(颜色标签)、主要问题(截断)、店长评语
  - 新增弹窗(字段多，用分步表单或大弹窗)

面谈字段: employeeId, interviewDate, currentWeekSales, targetCompletionRate,
         mainProblem, customerFollowIssue, productKnowledgeGap,
         mindsetStatus(POSITIVE/NORMAL/LOW/ABNORMAL),
         nextWeekGoal, improvementPlan, managerComment, employeeFeedback, followUpDate

调用的API:
  POST   /human/interviews
  GET    /human/interviews?employeeId&storeId&startDate&endDate&page&size
  GET    /human/interviews/{id}

完成后验证: 面谈CRUD + 员工筛选
```

### Page 16：能力考核 —— 1个页面

**文件**: `src/views/human/HumanAssess.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 员工、考核周
  - 列表: 员工、周、各维度分、总分、操作
  - 新增/编辑弹窗(5个维度评分输入 + 改进建议)
  - 员工考核雷达图（ECharts）

考核维度: productKnowledgeScore(25), matchingSkillScore(20),
         receptionScore(20), objectionHandlingScore(20), promotionScriptScore(15)

调用的API:
  POST   /human/assessments
  GET    /human/assessments?employeeId&assessmentWeek&page&size
  GET    /human/assessments/employee/{employeeId}?months=6 → 雷达图数据

完成后验证: 考核CRUD + 雷达图正确展示
```

### Page 17：绩效复盘 —— 1个页面

**文件**: `src/views/human/HumanPerformance.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 门店、月份
  - 列表: 员工、月份、销售额、成交单数、客单价、新客/老客销售、服务分、任务分、分层
  - 新增弹窗(大部分数据系统自动带出，人工填评语+奖罚)

复盘字段: employeeId, reviewMonth, totalSalesAmount(自动), salesOrderCount(自动),
         avgOrderAmount(自动), newCustomerSales, oldCustomerRepurchaseSales,
         keyCategorySales(JSON), serviceScore, taskExecutionScore,
         rewardAmount, penaltyAmount, managerReview

调用的API:
  POST   /human/monthly-reviews
  GET    /human/monthly-reviews?storeId&reviewMonth&page&size

完成后验证: 自动汇总数据 + 人工评语填写
```

### Page 18：培训管理 —— 1个页面

**文件**: `src/views/human/TrainingList.vue`（新建）

> 路由新增：在 human 路由下添加 training 子路由

```
UI构成:
  - 筛选: 培训类型、日期
  - 列表: 标题、类型、培训人、日期、参与人数
  - 新增弹窗: trainingTitle, trainingType, trainerId, trainingDate, materialUrls
  - 点击行展开: 参与员工列表 + 签到状态 + 考核分

调用的API:
  POST   /human/trainings
  GET    /human/trainings?storeId&trainingType&page&size
  PUT    /human/trainings/{id}/sign-in
  PUT    /human/trainings/{id}/exam-score

完成后验证: 培训CRUD + 签到管理
```

### Page 19：员工综合档案 —— 1个页面（新建）

**文件**: `src/views/human/EmployeeProfile.vue`（新建）

> 路由新增：/human/employee/:id → EmployeeProfile.vue

```
UI构成:
  - 顶部: 员工基本信息卡片(头像、姓名、岗位、入职日期、分层标签)
  - Tab1 销售趋势: 折线图（月度销售 + 目标对比）
  - Tab2 面谈记录: 时间线列表
  - Tab3 能力考核: 雷达图 + 各期分数趋势
  - Tab4 培训记录: 列表
  - Tab5 AI 画像: 优势/短板/风险/建议卡片

调用的API:
  GET /human/employees/{employeeId}/profile

完成后验证: 综合档案5个Tab完整展示
```

---

## 阶段四：货品 + 场景 + 销售（P1 业务表单）

### Page 20：货品列表 —— 已有骨架，直接完善

**文件**: `src/views/goods/GoodsList.vue`（修改现有空壳）

```
UI构成: 搜索(品类/状态/关键词) + 表格 + 新增/编辑弹窗

调用的API:
  GET    /products?category&status&storeId&keyword&page&size
  GET    /products/{id}
  POST   /products
  PUT    /products/{id}

注意: cost_price 和 gross_margin_rate 按角色显示/隐藏

完成后验证: 商品CRUD + 敏感字段权限控制
```

### Page 21：货品分类 + 品牌

**文件**: `src/views/goods/GoodsCategory.vue` + `GoodsBrand.vue`（修改现有空壳）

```
分类页: 树形表格(a-table 的 children) + CRUD
品牌页: 搜索 + 表格 + CRUD

完成后验证: 基础CRUD
```

### Page 22：盘点管理

**文件**: `src/views/inventory/InventoryCheck.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 门店、日期范围
  - 列表: 日期、门店、盘点总数、异常数量、检查人、照片预览
  - 新增弹窗: checkDate, storeId, totalCheckedCount, abnormalCount,
              abnormalItems(动态添加异常商品行), photos(ImageUploader), remark

调用的API:
  POST   /products/inventory-checks
  GET    /products/inventory-checks?storeId&startDate&endDate&page&size

完成后验证: 盘点CRUD + 异常商品明细
```

### Page 23：动销分析

**文件**: `src/views/goods/SalesAnalysis.vue`（新建）

```
UI构成:
  - 筛选: 门店、周
  - 卡片区: 爆款商品(标签) / 平销 / 慢销 / 7天无动销 / 缺货风险
  - 文字区: analysisSummary, actionPlan

调用的API:
  POST   /products/sales-analyses
  GET    /products/sales-analyses?storeId&analysisWeek&page&size

完成后验证: 多分类商品展示 + 分析文字
```

### Page 24：新品推介方案 + 促销筹备

**文件**: `src/views/goods/NewProductPlan.vue` + `PromotionPlan.vue`（新建）

```
新品方案: 月份、新品清单(动态表格)、卖点、人群、陈列方案、培训计划、销售目标
促销筹备: 月份、活动名称/主题、时间段、优惠规则、主推商品、物料、话术、预热计划

调用的API:
  POST/GET /products/new-product-plans
  POST/GET /products/promotion-plans

完成后验证: 两个表单CRUD
```

### Page 25：卫生巡检

**文件**: `src/views/scenario/ScenarioHealth.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 门店、日期、时段(早/中/晚)
  - 列表: 日期、时段、门店、巡检人、各区域结果、是否需整改
  - 新增弹窗: inspectionTime(MORNING/MIDDAY/EVENING), inspectionDate,
              areaResults(每个区域: 合格/不合格+描述),
              photoUrls, rectificationRequired

调用的API:
  POST   /scene/health-inspections
  GET    /scene/health-inspections?storeId&startDate&endDate&inspectionTime&page&size

完成后验证: 巡检CRUD + 分区域检查结果
```

### Page 26：陈列检查

**文件**: `src/views/scenario/ScenarioDisplay.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 门店、日期、区域(黄金/钻石/K金/古法/银饰/C位)
  - 列表 + 详情弹窗（含整改前/后照片对比）

陈列区域: GOLD/DIAMOND/K_GOLD/ANCIENT/SILVER/SPOTLIGHT

调用的API:
  POST   /scene/display-inspections
  GET    /scene/display-inspections?storeId&startDate&endDate&displayArea&page&size

完成后验证: 陈列CRUD + 前后照片对比展示
```

### Page 27：物料更新 + 设备检查

**文件**: `src/views/scenario/ScenarioMaterial.vue` + `ScenarioDevice.vue`（修改现有空壳）

```
物料: materialType(POSTER/FLAG/STAND/CARD), currentStatus(NORMAL/EXPIRED/DAMAGED/MISSING)
设备: equipmentType(LIGHT/AC/CAMERA/AUDIO/POS/CABINET_LIGHT/SAFE/NETWORK),
      status(NORMAL/ABNORMAL), repairRequired

调用的API:
  POST/GET /scene/material-updates
  POST/GET /scene/equipment-checks

完成后验证: 两个表单CRUD
```

### Page 28：销售录入（PC端）

**文件**: `src/views/sales/SalesEntry.vue`（修改现有空壳）

```
UI构成:
  - 销售单表单: salesDate, storeId, totalAmount, paidAmount,
               customerType(NEW/OLD), customerGender, customerAgeRange,
               purchaseScene(WEDDING/GIFT/SELF/INVEST/HOLIDAY/OTHER),
               customerConcern, salesPhotoUrls
  - 商品明细(动态添加行): productName, category, style, material, weight, size,
                         color, shape, meaning, price, quantity,
                         customerFavoritePoint, objection, closingReason,
                         productPhotoUrls
  - 提交按钮

调用的API:
  POST   /sales/records
  GET    /sales/records?storeId&employeeId&startDate&endDate&auditStatus&page&size

完成后验证: 销售单 + 多条商品明细 录入成功
```

### Page 29：销售审核

**文件**: 在 sales 目录新建 `SalesAudit.vue`

```
UI构成:
  - 待审核列表: 销售单号、导购、日期、金额、客户类型
  - 详情弹窗: 销售单信息 + 商品明细 + 照片
  - 操作: 通过 / 驳回 + 审核意见

调用的API:
  GET    /sales/records?auditStatus=PENDING&page&size
  GET    /sales/records/{id}
  POST   /sales/records/{id}/audit

完成后验证: 审核通过/驳回流程
```

### Page 30：销售报表

**文件**: `src/views/sales/SalesReport.vue`（修改现有空壳）

```
UI构成:
  - Tab1 门店业绩: 统计卡片(销售额/完成率/新客比/老客比) + 品类饼图
  - Tab2 员工排行: 表格(排名/姓名/销售额/单数/客单价)
  - Tab3 品类结构: 饼图 + 表格
  - Tab4 趋势图: 折线图(多门店对比 / 员工对比)

调用的API:
  GET    /sales/stats/store?storeId&startDate&endDate
  GET    /sales/stats/employee?employeeId&startDate&endDate
  GET    /sales/stats/employee-ranking?storeId&startDate&endDate&sortBy
  GET    /sales/stats/store-ranking?regionId&startDate&endDate
  GET    /sales/stats/category-breakdown?storeId&startDate&endDate

完成后验证: 4个Tab图表正确展示（ECharts）
```

---

## 阶段五：报表 + AI + 通知（P2-P3）

### Page 31：数据驾驶舱 —— 已有骨架，直接完善

**文件**: `src/views/dashboard/DashboardView.vue`（修改现有空壳）

```
UI构成(总部视角):
  - 顶部统计卡片行: 全部门店数、任务完成率、销售完成率、超时门店数
  - 中部图表区:
    - 门店评分分布柱状图
    - 人货场评分趋势折线图
    - 区域对比柱状图
  - 右侧列表区:
    - 门店排名 Top10/Bottom5
    - 滞销商品排行
    - AI 风险提醒列表

调用的API:
  GET    /reports/dashboard/hq
  GET    /reports/dashboard/regional?regionId
  GET    /reports/dashboard/store?storeId

完成后验证: 仪表盘图表正确 + 数据卡片正确
```

### Page 32：报表中心

**文件**: `src/views/report/ReportCenter.vue`（修改现有空壳）

```
UI构成:
  - Tab1 门店评分: 月份选择 + 表格(排名/门店/总分/人效分/货品分/场景分/纪律分/超时次数)
  - Tab2 任务完成率: 门店维度表格 + 按维度(人/货/场)柱状图
  - Tab3 人效汇总: 晨会率/面谈率/考核率/培训率 指标卡片 + 表格
  - Tab4 货品汇总: 动销趋势 + 滞销Top10
  - Tab5 场景汇总: 巡检率/陈列分/物料率/设备率
  - 导出Excel按钮

调用的API:
  GET    /reports/store-monthly-scores?scoreMonth&regionId&page&size
  GET    /reports/task-completion?storeId&startDate&endDate&dimension
  GET    /reports/human-summary?storeId&period
  GET    /reports/product-summary?storeId&period
  GET    /reports/scene-summary?storeId&period
  GET    /reports/export/{reportType}

完成后验证: 5个Tab完整 + Excel下载
```

### Page 33：AI 智能辅助

**文件**: `src/views/ai/AICenter.vue`（修改现有空壳）

```
UI构成:
  - Tab1 员工建议: 员工选择器 → AI 面板(总结/问题/原因/话术/培训/跟进)
  - Tab2 货品建议: 门店选择器 → AI 面板(爆款/滞销原因/主推/搭配/清货)
  - Tab3 场景建议: 门店选择器 → AI 面板(问题/陈列/物料/动线/灯光)
  - Tab4 提示词管理: 表格 + CRUD弹窗(管理员可见)
  - Tab5 AI调用历史: 列表

一键转任务按钮(生成建议后出现)

调用的API:
  GET    /ai/advice/employee/{employeeId}
  GET    /ai/advice/product/{storeId}
  GET    /ai/advice/scene/{storeId}
  GET    /ai/advice/store/{storeId}
  POST   /ai/advice/to-task/{adviceId}
  GET    /ai/prompt-templates
  POST   /ai/prompt-templates
  PUT    /ai/prompt-templates/{id}
  GET    /ai/results

完成后验证: AI建议面板 + 一键转任务
```

### Page 34：消息通知

**文件**: `src/views/notification/NotificationList.vue`（新建）

> 路由新增：/notification → NotificationList.vue

```
UI构成:
  - 左侧分类: 全部/任务提醒/超时/审核/驳回/异常/AI建议/总部通知
  - 右侧列表: 未读加粗 + 标题 + 摘要 + 时间 + 标记已读按钮
  - 全部已读按钮

调用的API:
  GET    /notifications?notificationType&isRead&page&size
  PUT    /notifications/{id}/read
  PUT    /notifications/read-all

完成后验证: 通知列表 + 已读/未读状态切换
```

### Page 35：操作日志

**文件**: `src/views/log/LogManage.vue`（修改现有空壳）

```
UI构成:
  - 筛选: 操作人、模块、时间范围
  - 表格: 时间、操作人、模块、动作、目标类型、目标ID、IP、详情按钮

调用的API:
  GET    /logs/operate?operatorId&module&action&startDate&endDate&page&size

完成后验证: 日志列表 + 筛选
```

---

## 附录A：全部页面清单（53页）

| # | 页面 | 文件路径 | 阶段 | 状态 |
|---|------|---------|------|------|
| 0.1-0.4 | 基础设施 | utils/stores/types/components | 阶段零 | 新建/完善 |
| 1 | 登录 | login/LoginView.vue | 阶段一 | 修改 |
| 2 | Layout | layouts/DefaultLayout.vue | 阶段一 | 修改 |
| 3 | 组织架构 | system/SystemOrganization.vue | 阶段一 | 修改 |
| 4 | 门店管理 | system/SystemStore.vue | 阶段一 | 修改 |
| 5 | 用户管理 | system/SystemUser.vue | 阶段一 | 修改 |
| 6 | 角色权限 | system/SystemRole.vue | 阶段一 | 修改 |
| 7 | 系统配置 | system/SystemConfig.vue | 阶段一 | 修改 |
| 8 | 动作库 | action/ActionLibrary.vue | 阶段二 | 新建 |
| 9 | 任务模板 | task/TaskTemplate.vue | 阶段二 | 修改 |
| 10 | 任务列表 | task/TaskList.vue | 阶段二 | 修改 |
| 11 | 创建任务 | task/TaskCreate.vue | 阶段二 | 修改 |
| 12 | 任务详情 | task/TaskDetail.vue | 阶段二 | 新建 |
| 13 | 任务审核 | task/TaskReview.vue | 阶段二 | 修改 |
| 14 | 晨夕会 | human/HumanMeeting.vue | 阶段三 | 修改 |
| 15 | 员工面谈 | human/HumanInterview.vue | 阶段三 | 修改 |
| 16 | 能力考核 | human/HumanAssess.vue | 阶段三 | 修改 |
| 17 | 绩效复盘 | human/HumanPerformance.vue | 阶段三 | 修改 |
| 18 | 培训管理 | human/TrainingList.vue | 阶段三 | 新建 |
| 19 | 员工档案 | human/EmployeeProfile.vue | 阶段三 | 新建 |
| 20 | 货品列表 | goods/GoodsList.vue | 阶段四 | 修改 |
| 21 | 货品分类 | goods/GoodsCategory.vue | 阶段四 | 修改 |
| 22 | 品牌管理 | goods/GoodsBrand.vue | 阶段四 | 修改 |
| 23 | 盘点管理 | inventory/InventoryCheck.vue | 阶段四 | 修改 |
| 24 | 库存列表 | inventory/InventoryList.vue | 阶段四 | 修改 |
| 25 | 库存预警 | inventory/InventoryWarning.vue | 阶段四 | 修改 |
| 26 | 动销分析 | goods/SalesAnalysis.vue | 阶段四 | 新建 |
| 27 | 新品方案 | goods/NewProductPlan.vue | 阶段四 | 新建 |
| 28 | 促销筹备 | goods/PromotionPlan.vue | 阶段四 | 新建 |
| 29 | 卫生巡检 | scenario/ScenarioHealth.vue | 阶段四 | 修改 |
| 30 | 陈列检查 | scenario/ScenarioDisplay.vue | 阶段四 | 修改 |
| 31 | 物料更新 | scenario/ScenarioMaterial.vue | 阶段四 | 修改 |
| 32 | 设备检查 | scenario/ScenarioDevice.vue | 阶段四 | 修改 |
| 33 | 销售录入 | sales/SalesEntry.vue | 阶段四 | 修改 |
| 34 | 销售审核 | sales/SalesAudit.vue | 阶段四 | 新建 |
| 35 | 销售报表 | sales/SalesReport.vue | 阶段四 | 修改 |
| 36 | 驾驶舱 | dashboard/DashboardView.vue | 阶段五 | 修改 |
| 37 | 报表中心 | report/ReportCenter.vue | 阶段五 | 修改 |
| 38 | AI辅助 | ai/AICenter.vue | 阶段五 | 修改 |
| 39 | 消息通知 | notification/NotificationList.vue | 阶段五 | 新建 |
| 40 | 操作日志 | log/LogManage.vue | 阶段五 | 修改 |

已有路由但PRD中非MVP核心的页面（可后做/保留占位）:
- customer/CustomerList.vue, CustomerMember.vue
- supplier/SupplierList.vue
- certificate/CertificateList.vue
- order/OrderList.vue, OrderDetail.vue, OrderReturn.vue
- purchase/PurchaseList.vue, PurchaseApply.vue
- marketing/MarketingActivity.vue, MarketingPromotion.vue
- finance/FinanceView.vue

## 附录B：开发节奏建议

| 周次 | 做什么 | 页面数 |
|------|--------|--------|
| 第1周 | 阶段零(基础设施) + 阶段一(登录+系统管理) | Page 0.1~7 |
| 第2周 | 阶段二(任务中心) | Page 8~13 |
| 第3周 | 阶段三(人效) | Page 14~19 |
| 第4周 | 阶段四(货品+场景+销售) | Page 20~35 |
| 第5周 | 阶段五(报表+AI+通知+日志) | Page 36~40 |
| 第6周 | H5移动端（另行规划） | - |
