# 珠宝通零售连锁门店主动式管理系统 — 标准化架构文档

> 文档版本：V1.0 | 适用对象：后端/前端/测试/DevOps | 技术栈：SpringBoot + Vue3 + Ant Design + MySQL
> 架构模式：单体MVP（模块化分包，预留微服务拆分接口）

---

## 1. 项目整体分层架构图

```
客户端层:
  PC管理端 (Vue3+Ant Design)           H5移动端 (Vue3+Vant/Mobile)
  - 总部运营/区域经理/管理员            - 导购/店长/区域经理
          |                                       |
          +-------- HTTPS / WSS ------------------+
                         |
网关层:         Nginx (反向代理 + 限流 + 动静分离 + SSL终结)
              /api/*  ->  后端服务
              /admin/* -> PC静态资源
              /h5/*    -> H5静态资源
                         |
应用服务层 (SpringBoot 单体):
  +----------------------------------------------------------+
  |  拦截器层: AuthInterceptor | DataScopeInterceptor | LogInterceptor |
  +----------------------------------------------------------+
  |  Controller 层 (RESTful API)                              |
  |    auth | organization | user | role | task | actiontemplate |
  |    human | product | scene | sales | ai | report | notification |
  +----------------------------------------------------------+
  |  Service 层 (业务逻辑)                                    |
  |    TaskGenerateService | AIAdvisorService | ScoreCalcService |
  |    ReminderService | FormEngineService | FileStorageService |
  +----------------------------------------------------------+
  |  Repository 层 (数据访问, MyBatis-Plus)                    |
  +----------------------------------------------------------+
                         |
数据层:
  +------------------+  +------------------+  +------------------+
  | MySQL 8.0 (主库) |  | Redis 7.x (缓存)  |  | MinIO (文件存储)  |
  | 读写分离预留      |  | Token/任务锁/队列 |  | 图片/附件/报表    |
  +------------------+  +------------------+  +------------------+
                         |
中间件层:
  +------------------+  +------------------+  +------------------+
  | XXL-JOB (定时任务)|  | 大模型API (AI)    |  | 企业微信/短信     |
  | 周期任务生成/提醒 |  | GPT/文心/通义    |  | 消息推送预留      |
  +------------------+  +------------------+  +------------------+
```

**后续微服务拆分方向（MVP后）:**
- 当前: 单体 jar 包部署
- 拆分候选: task-center | human-service | product-service | scene-service | sales-service | ai-service | report-service
- 通信方式: 逐步引入 Spring Cloud Gateway + Nacos + OpenFeign

---

## 2. 完整项目目录树

### 2.1 后端分包结构 (SpringBoot)

```
zb-tong-server/
├── pom.xml
├── src/main/java/com/zhubao/manage/
│   ├── ZbtApplication.java                    # 启动类
│   │
│   ├── common/                                 # 公共模块
│   │   ├── config/
│   │   │   ├── WebMvcConfig.java               # MVC配置(拦截器注册)
│   │   │   ├── MyBatisPlusConfig.java          # MyBatis-Plus分页/乐观锁
│   │   │   ├── RedisConfig.java                # Redis序列化配置
│   │   │   ├── SwaggerConfig.java              # Knife4j API文档
│   │   │   ├── SecurityConfig.java             # Spring Security配置
│   │   │   ├── CorsConfig.java                 # 跨域配置
│   │   │   └── FileUploadConfig.java           # 文件上传大小/类型限制
│   │   ├── interceptor/
│   │   │   ├── AuthInterceptor.java            # JWT认证拦截器
│   │   │   ├── DataScopeInterceptor.java       # 数据权限拦截器(MyBatis插件)
│   │   │   └── LogInterceptor.java             # 操作日志拦截器
│   │   ├── annotation/
│   │   │   ├── DataScope.java                  # 数据权限注解
│   │   │   ├── OperateLog.java                 # 操作日志注解
│   │   │   └── RateLimit.java                  # 限流注解
│   │   ├── enums/
│   │   │   ├── DimensionEnum.java              # 人/货/场/综合
│   │   │   ├── TaskStatusEnum.java             # 任务状态枚举
│   │   │   ├── TaskPriorityEnum.java           # 任务优先级
│   │   │   ├── SourceTypeEnum.java             # 任务来源类型
│   │   │   ├── AuditResultEnum.java            # 审核结果
│   │   │   ├── OrgTypeEnum.java                # 组织类型(总部/大区/区域/门店)
│   │   │   ├── StoreTypeEnum.java              # 门店类型
│   │   │   ├── StoreStatusEnum.java            # 门店状态
│   │   │   ├── UserStatusEnum.java             # 用户状态(在职/离职/停用)
│   │   │   ├── EmployeeLevelEnum.java          # 员工分层(标杆/达标/待提升)
│   │   │   ├── ProductCategoryEnum.java        # 商品品类(黄金/钻石/K金/古法/银饰/彩宝)
│   │   │   ├── ProductStatusEnum.java          # 商品状态(在售/售出/调拨/维修/下架)
│   │   │   ├── CustomerTypeEnum.java           # 新客/老客
│   │   │   ├── PurchaseSceneEnum.java          # 购买场景(婚庆/送礼/自戴/投资/节日/其他)
│   │   │   ├── GenderEnum.java                 # 性别
│   │   │   ├── AgeRangeEnum.java               # 年龄段
│   │   │   ├── MindsetStatusEnum.java          # 心态状态(积极/正常/低迷/异常)
│   │   │   ├── NotificationTypeEnum.java       # 通知类型
│   │   │   ├── NotificationChannelEnum.java    # 通知渠道
│   │   │   └── FileTypeEnum.java               # 文件类型
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java     # 全局异常处理
│   │   │   ├── BusinessException.java          # 业务异常
│   │   │   └── ErrorCode.java                  # 错误码枚举
│   │   ├── dto/
│   │   │   ├── PageDTO.java                    # 分页请求基类
│   │   │   ├── PageResult.java                 # 分页返回基类
│   │   │   └── ApiResult.java                  # 统一响应体 {code, msg, data}
│   │   └── utils/
│   │       ├── JwtUtil.java                    # JWT生成/验证
│   │       ├── CodeGenerator.java              # 编码生成器
│   │       ├── DateUtil.java                   # 日期工具类
│   │       ├── FileUtil.java                   # 文件校验工具
│   │       └── SpringContextUtil.java          # Spring上下文工具
│   │
│   ├── module/
│   │   ├── auth/                               # 认证模块
│   │   │   ├── controller/AuthController.java
│   │   │   ├── service/AuthService.java
│   │   │   └── dto/ (LoginDTO, LoginResultDTO, SelectStoreDTO)
│   │   │
│   │   ├── organization/                       # 组织架构模块
│   │   │   ├── controller/OrganizationController.java
│   │   │   ├── controller/StoreController.java
│   │   │   ├── service/OrganizationService.java, StoreService.java
│   │   │   ├── mapper/OrganizationMapper.java, StoreMapper.java
│   │   │   └── entity/Organization.java, Store.java
│   │   │
│   │   ├── user/                               # 用户模块
│   │   │   ├── controller/UserController.java
│   │   │   ├── service/UserService.java
│   │   │   ├── mapper/UserMapper.java
│   │   │   └── entity/User.java
│   │   │
│   │   ├── role/                               # 角色权限模块
│   │   │   ├── controller/RoleController.java
│   │   │   ├── controller/PermissionController.java
│   │   │   ├── service/RoleService.java, PermissionService.java
│   │   │   ├── mapper/RoleMapper.java, PermissionMapper.java
│   │   │   └── entity/Role.java, Permission.java
│   │   │
│   │   ├── task/                               # 任务中心模块(核心)
│   │   │   ├── controller/TaskController.java
│   │   │   ├── controller/TaskAuditController.java
│   │   │   ├── service/TaskGenerateService.java     # 周期任务生成
│   │   │   ├── service/TaskService.java
│   │   │   ├── service/TaskSubmitService.java
│   │   │   ├── service/TaskAuditService.java
│   │   │   ├── service/TaskReminderService.java
│   │   │   ├── mapper/ (TaskTemplate,TaskInstance,TaskSubmission,TaskAudit)
│   │   │   └── entity/ (TaskTemplate,TaskInstance,TaskSubmission,TaskAudit)
│   │   │
│   │   ├── actiontemplate/                     # 动作库模块
│   │   │   ├── controller/ActionTemplateController.java
│   │   │   ├── service/ActionTemplateService.java
│   │   │   ├── mapper/ActionTemplateMapper.java
│   │   │   └── entity/ActionTemplate.java
│   │   │
│   │   ├── human/                              # 人效管理模块
│   │   │   ├── controller/HumanController.java
│   │   │   ├── service/HumanService.java
│   │   │   ├── mapper/ (EmployeeProfile,Interview,Assessment,
│   │   │   │           Training,MonthlyReview,LevelRecord)
│   │   │   └── entity/ (同上6个实体)
│   │   │
│   │   ├── product/                            # 货品管理模块
│   │   │   ├── controller/ProductController.java
│   │   │   ├── service/ProductService.java
│   │   │   ├── mapper/ (Product,InventoryCheck,MaintenanceCheck,
│   │   │   │           SalesAnalysis,NewProductPlan,PromotionPlan)
│   │   │   └── entity/ (同上6个实体)
│   │   │
│   │   ├── scene/                              # 场景运营模块
│   │   │   ├── controller/SceneController.java
│   │   │   ├── service/SceneService.java
│   │   │   ├── mapper/ (HealthInspection,DisplayInspection,
│   │   │   │           MaterialUpdate,EquipmentCheck,CustomerExperience)
│   │   │   └── entity/ (同上5个实体)
│   │   │
│   │   ├── sales/                              # 业绩数据模块
│   │   │   ├── controller/SalesController.java
│   │   │   ├── service/SalesService.java
│   │   │   ├── mapper/ (SalesRecord,SalesItem,SalesAudit)
│   │   │   └── entity/ (同上3个实体)
│   │   │
│   │   ├── ai/                                 # AI智能辅助模块
│   │   │   ├── controller/AIController.java
│   │   │   ├── service/AIService.java
│   │   │   ├── service/AIScoreService.java
│   │   │   ├── service/PromptTemplateService.java
│   │   │   ├── gateway/AIGatewayService.java        # AI服务网关接口
│   │   │   ├── gateway/impl/OpenAIGatewayImpl.java  # GPT适配
│   │   │   ├── gateway/impl/WenXinGatewayImpl.java  # 文心一言适配(预留)
│   │   │   ├── assembler/DataAssembler.java         # 数据组装器
│   │   │   ├── parser/ResultParser.java             # 结果解析器
│   │   │   ├── mapper/ (AIResult,PromptTemplate)
│   │   │   └── entity/ (AIResult,PromptTemplate)
│   │   │
│   │   ├── report/                             # 报表模块
│   │   │   ├── controller/ReportController.java
│   │   │   ├── service/ReportService.java
│   │   │   ├── service/ScoreCalcService.java        # 评分计算引擎
│   │   │   ├── mapper/ (StoreMonthlyScore,ReportSnapshot)
│   │   │   └── entity/ (StoreMonthlyScore,ReportSnapshot)
│   │   │
│   │   ├── notification/                       # 消息通知模块
│   │   │   ├── controller/NotificationController.java
│   │   │   ├── service/NotificationService.java
│   │   │   ├── channel/StationChannel.java          # 站内信
│   │   │   ├── channel/WeComChannel.java            # 企业微信(预留)
│   │   │   ├── channel/SMSChannel.java              # 短信(预留)
│   │   │   ├── mapper/NotificationMapper.java
│   │   │   └── entity/Notification.java
│   │   │
│   │   └── file/                               # 文件管理模块
│   │       ├── controller/FileController.java
│   │       ├── service/FileService.java
│   │       ├── storage/FileStorageStrategy.java     # 存储策略接口
│   │       ├── storage/MinIOStorageImpl.java
│   │       ├── storage/OSSStorageImpl.java(预留)
│   │       ├── mapper/FileResourceMapper.java
│   │       └── entity/FileResource.java
│   │
│   ├── scheduler/                              # 定时任务(XxlJob调度)
│   │   ├── TaskGenerateJob.java                # 日/周/月/季度任务生成
│   │   ├── TaskOverdueJob.java                 # 超时任务检查(每5分钟)
│   │   ├── TaskReminderJob.java                # 任务提醒推送
│   │   ├── ReportGenerateJob.java              # 月度报表生成
│   │   ├── StoreScoreCalcJob.java              # 门店月度评分计算
│   │   └── AIAnalysisJob.java                  # AI分析触发
│   │
│   └── infrastructure/                         # 基础设施
│       ├── mybatis/
│       │   └── DataScopePlugin.java            # MyBatis数据权限拦截插件
│       └── aop/
│           ├── OperateLogAspect.java           # 操作日志AOP
│           └── RateLimitAspect.java            # 限流AOP
│
├── src/main/resources/
│   ├── application.yml                         # 主配置
│   ├── application-dev.yml                     # 开发环境
│   ├── application-prod.yml                    # 生产环境
│   ├── db/migration/                           # Flyway SQL迁移脚本
│   │   ├── V1__init_org_user.sql
│   │   ├── V2__init_task.sql
│   │   ├── V3__init_human.sql
│   │   ├── V4__init_product.sql
│   │   ├── V5__init_scene.sql
│   │   ├── V6__init_sales.sql
│   │   ├── V7__init_ai_report.sql
│   │   └── V8__init_seed_data.sql              # 种子数据(默认动作库)
│   └── logback-spring.xml
│
└── src/test/java/com/zhubao/manage/
    └── module/ (task,sales,report 单元测试)
```

### 2.2 PC管理端目录 (Vue3 + Ant Design Vue)

```
zb-tong-admin/
├── package.json / vite.config.ts / tsconfig.json / index.html
│
└── src/
    ├── main.ts / App.vue
    ├── router/index.ts                         # 路由配置(已有骨架)
    │
    ├── stores/                                 # Pinia 状态管理
    │   ├── useAuthStore.ts                     # 登录用户/Token/角色/门店
    │   ├── useAppStore.ts                      # 侧边栏/主题/语言
    │   ├── useTaskStore.ts                     # 任务中心状态
    │   └── useNotificationStore.ts             # 消息通知未读数
    │
    ├── layouts/
    │   ├── DefaultLayout.vue                   # 侧边栏+顶栏+内容区
    │   ├── SidebarMenu.vue                     # 侧边栏菜单(递归渲染)
    │   ├── HeaderBar.vue                       # 顶栏(通知/头像/门店切换)
    │   └── BlankLayout.vue                     # 空白布局(登录页)
    │
    ├── views/                                  # 页面视图(已有骨架)
    │   ├── login/LoginView.vue                 # 登录 + 角色选择 + 门店选择
    │   ├── dashboard/DashboardView.vue         # 数据驾驶舱
    │   ├── system/
    │   │   ├── SystemOrganization.vue          # 组织架构树+门店管理
    │   │   ├── SystemStore.vue                 # 门店详情
    │   │   ├── SystemUser.vue                  # 用户管理
    │   │   ├── SystemRole.vue                  # 角色权限矩阵
    │   │   └── SystemConfig.vue                # 系统配置
    │   ├── task/
    │   │   ├── TaskList.vue                    # 任务列表(筛选/批量下发)
    │   │   ├── TaskCreate.vue                  # 创建任务(手动/强制)
    │   │   ├── TaskTemplate.vue                # 任务模板配置
    │   │   └── TaskReview.vue                  # 任务审核(PC端)
    │   ├── human/
    │   │   ├── HumanMeeting.vue                # 晨会/夕会记录
    │   │   ├── HumanInterview.vue              # 员工面谈记录
    │   │   ├── HumanAssess.vue                 # 能力考核
    │   │   └── HumanPerformance.vue            # 绩效复盘
    │   ├── goods/
    │   │   ├── GoodsList.vue                   # 商品列表
    │   │   ├── GoodsCategory.vue               # 商品分类
    │   │   └── GoodsBrand.vue                  # 品牌管理
    │   ├── inventory/
    │   │   ├── InventoryList.vue               # 库存列表
    │   │   ├── InventoryCheck.vue              # 盘点管理
    │   │   └── InventoryWarning.vue            # 库存预警
    │   ├── scenario/
    │   │   ├── ScenarioHealth.vue              # 卫生巡检
    │   │   ├── ScenarioDisplay.vue             # 陈列检查
    │   │   ├── ScenarioMaterial.vue            # 物料更新
    │   │   └── ScenarioDevice.vue              # 设备检查
    │   ├── sales/
    │   │   ├── SalesEntry.vue                  # 销售录入(PC也可录入)
    │   │   └── SalesReport.vue                 # 销售报表
    │   ├── report/ReportCenter.vue             # 报表中心
    │   ├── ai/AICenter.vue                     # AI建议/提示词管理
    │   ├── notification/NotificationList.vue   # 消息通知列表(新增)
    │   └── log/LogManage.vue                   # 操作日志
    │
    ├── components/                             # 公共组件
    │   ├── common/
    │   │   ├── ImageUploader.vue               # 图片上传(多图/拖拽/压缩/预览)
    │   │   ├── ImageViewer.vue                 # 图片查看器(放大/旋转)
    │   │   ├── FileUploader.vue                # 附件上传
    │   │   ├── DynamicForm.vue                 # 动态表单渲染器(JSON Schema驱动)
    │   │   ├── OrganizationTree.vue            # 组织架构树选择器
    │   │   ├── StoreSelect.vue                 # 门店选择器
    │   │   ├── UserSelect.vue                  # 用户选择器
    │   │   ├── TaskStatusTag.vue               # 任务状态标签
    │   │   ├── AuditAction.vue                 # 审核操作按钮组(通过/驳回/评分)
    │   │   └── ChartCard.vue                   # 图表卡片容器
    │   └── business/
    │       ├── TaskCard.vue                    # 任务卡片
    │       ├── TaskTimeline.vue                # 任务流转时间线
    │       ├── SalesForm.vue                   # 销售录入表单
    │       ├── EmployeeSelector.vue            # 员工选择器
    │       └── AIAdvicePanel.vue               # AI建议面板
    │
    ├── composables/                            # 组合式函数
    │   ├── useAuth.ts                          # 登录/登出/Token刷新
    │   ├── usePermission.ts                    # 按钮/菜单权限判断
    │   ├── useUpload.ts                        # 文件上传封装
    │   ├── usePagination.ts                     # 分页查询封装
    │   └── useChart.ts                         # ECharts图表封装
    │
    ├── utils/
    │   ├── request.ts                          # Axios封装(拦截器/Token注入)
    │   ├── storage.ts                          # localStorage封装
    │   ├── date.ts                             # 日期格式化
    │   └── validators.ts                       # 表单校验规则
    │
    ├── types/                                  # TypeScript类型定义
    │   ├── api.d.ts                            # API响应体泛型
    │   ├── user.d.ts                           # 用户/角色/权限类型
    │   ├── task.d.ts                           # 任务相关类型
    │   ├── goods.d.ts                          # 商品类型
    │   ├── sales.d.ts                          # 销售类型
    │   └── common.d.ts                         # 公共枚举/分页类型
    │
    └── styles/
        ├── index.less                          # 全局样式入口
        ├── variables.less                      # 主题变量
        └── reset.css                           # 浏览器重置
```

### 2.3 H5移动端目录 (Vue3 + Vant UI)

```
zb-tong-h5/
├── package.json / vite.config.ts / tsconfig.json / index.html
│
└── src/
    ├── main.ts / App.vue
    ├── router/index.ts                         # 移动端路由(5个Tab)
    │
    ├── stores/
    │   ├── useAuthStore.ts                     # 登录状态(自动记住门店)
    │   ├── useTaskStore.ts                     # 我的任务
    │   └── useHomeStore.ts                     # 首页数据(待办/销售额/目标)
    │
    ├── layouts/MobileLayout.vue                # TabBar底部导航布局
    │   # Tab1: 工作台  Tab2: 任务  Tab3: 销售录入  Tab4: AI建议  Tab5: 我的
    │
    ├── views/
    │   ├── home/HomeView.vue                   # 工作台:待办+销售额+提醒+快捷入口
    │   ├── task/
    │   │   ├── TaskListMobile.vue              # 我的任务(筛选+下拉刷新)
    │   │   ├── TaskExecuteMobile.vue           # 任务执行(核心):拍照/表单/AI建议
    │   │   └── TaskAuditMobile.vue             # 店长审核页
    │   ├── sales/
    │   │   ├── SalesEntryMobile.vue            # 销售录入(移动端核心)
    │   │   └── SalesListMobile.vue             # 我的销售记录
    │   ├── ai/AIMobile.vue                     # AI建议(一键转任务)
    │   └── profile/
    │       ├── ProfileView.vue                 # 我的
    │       ├── NotificationMobile.vue          # 消息通知
    │       └── SettingMobile.vue               # 设置/退出
    │
    ├── components/
    │   ├── CameraUploader.vue                  # 移动端拍照上传(调起相机)
    │   ├── PhotoGrid.vue                       # 照片网格展示
    │   ├── TaskStatusBar.vue                   # 任务状态进度条
    │   ├── CountdownBadge.vue                  # 截止时间倒计时
    │   └── EmptyState.vue                      # 空状态插图
    │
    ├── composables/
    │   ├── useCamera.ts                        # 调起相机/相册
    │   ├── useLocation.ts                      # GPS定位(可选)
    │   └── usePullRefresh.ts                   # 下拉刷新
    │
    └── utils/
        ├── request.ts                          # Axios封装(自动带Token/门店)
        ├── compress.ts                         # 图片压缩
        └── date.ts                             # 相对时间("2小时前截止")
```
## 3. RBAC权限 + 组织架构设计 + 角色数据权限隔离规则

### 3.1 权限模型

采用 **RBAC（角色权限模型） + 数据权限模型** 双层权限体系:

```
用户(User) --N:N--> 角色(Role) --N:N--> 权限(Permission)
                                                  |
                                    菜单权限(Menu) + 按钮权限(Button) + 接口权限(API)
```

### 3.2 角色定义

| 角色编码 | 角色名称 | 使用端 | 数据范围 | 说明 |
|---------|---------|--------|---------|------|
| ROLE_ADMIN | 系统管理员 | PC管理端 | 全系统 | 管理组织、账号、角色、权限、系统配置 |
| ROLE_HQ | 总部运营 | PC管理端 | 全部/按品牌/区域/门店 | 配置动作库、任务模板、查看全国数据 |
| ROLE_REGIONAL | 区域经理 | PC+H5 | 所辖区域全部门店 | 查看排名、抽查任务、督导整改 |
| ROLE_MANAGER | 店长 | PC+H5 | 所属门店全部数据 | 管理门店员工、审核、查看门店报表 |
| ROLE_ASSOCIATE | 导购 | H5为主 | 仅本人数据 | 录入销售、执行个人任务、查看个人业绩 |
| ROLE_AI_ADMIN | AI管理员 | PC管理端 | AI配置数据 | 配置提示词、评分规则、模型策略(可选角色) |
| ROLE_FINANCE | 财务/绩效 | PC管理端 | 业绩/考核数据 | 查看业绩、考核、绩效(可选角色) |

### 3.3 数据权限隔离规则

```
数据权限层级(Data Scope):
  LEVEL_1: 全系统 (ALL)        -- 系统管理员
  LEVEL_2: 指定区域 (REGION)    -- 区域经理
  LEVEL_3: 指定门店 (STORE)     -- 店长
  LEVEL_4: 仅本人 (SELF)       -- 导购
  LEVEL_5: 自定义 (CUSTOM)     -- 总部运营可配置

实现方式:
  后端 MyBatis 拦截器 @DataScope 注解:
    1. 解析当前用户角色, 获取 data_scope_level
    2. 根据 scope 自动拼接 SQL WHERE 条件:
       LEVEL_1 -> 无限制
       LEVEL_2 -> AND store.region_id = #{user.regionId}
       LEVEL_3 -> AND store_id = #{user.storeId}
       LEVEL_4 -> AND user_id = #{userId}
       LEVEL_5 -> 读取 sys_role_data_scope 表配置

  敏感字段权限:
    product.cost_price     -- 仅店长/区域经理/总部/管理员可见
    product.gross_margin_rate -- 仅店长/区域经理/总部/管理员可见
    sales.total_amount      -- 本人+上级可见
```

### 3.4 角色功能权限矩阵

| 功能模块 | 导购 | 店长 | 区域经理 | 总部运营 | 系统管理员 |
|---------|------|------|---------|---------|-----------|
| 查看个人任务 | Y | Y | Y | Y | Y |
| 执行任务 | Y | Y | 部分 | 部分 | N |
| 任务审核 | N | Y | Y | N | N |
| 业绩录入 | Y | N | N | N | N |
| 员工面谈 | N | Y | 可查看 | 可查看 | N |
| 员工考核 | N | Y | 可查看/抽查 | 可查看 | N |
| 货品盘点 | Y/协助 | Y | 可查看 | 可查看 | N |
| 动销分析 | N | Y | Y | N | N |
| 陈列检查 | Y/协助 | Y | Y | N | N |
| 动作库管理 | N | 可建议 | N | Y | Y |
| 任务模板配置 | N | 门店级 | 区域级 | Y | Y |
| 组织架构管理 | N | N | N | 部分 | Y |
| AI建议查看 | 个人 | 门店 | 区域 | 全局 | Y |
| 报表查看 | 个人 | 门店 | 区域 | 全部 | 全部 |

### 3.5 组织架构层级

```
总部 (HEADQUARTERS)
 ├── 大区 (GREAT_REGION)         -- 可选层级
 │    ├── 区域 (REGION)
 │    │    ├── 门店 (STORE)
 │    │    │    ├── 店长 (store_manager_id)
 │    │    │    └── 导购 (store_id 关联)
 │    │    └── 区域经理 (region_id 关联)
 │    └── ...
 └── ...

核心规则:
  - 每级组织可停用/启用
  - 门店必须绑定一个区域
  - 店长绑定门店(一对一)
  - 导购绑定门店(多对一)
  - 区域经理绑定区域(一对一)
  - 删除为逻辑删除(is_deleted=1)
```

---

## 4. 全量数据库表设计

### 4.1 基础权限类 (6张)

#### sys_organization (组织表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 组织ID |
| parent_id | BIGINT | NULLABLE, FK | 上级组织ID |
| org_name | VARCHAR(100) | NOT NULL | 组织名称 |
| org_type | VARCHAR(20) | NOT NULL | HEADQUARTERS/GREAT_REGION/REGION/STORE |
| org_code | VARCHAR(50) | UNIQUE | 组织编码 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: idx_parent_id, uk_org_code

#### sys_store (门店表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 门店ID |
| org_id | BIGINT | FK->sys_organization.id | 关联组织节点 |
| store_name | VARCHAR(200) | NOT NULL | 门店名称 |
| store_code | VARCHAR(50) | UNIQUE, NOT NULL | 门店编码 |
| region_id | BIGINT | FK->sys_organization.id | 所属区域 |
| address | VARCHAR(500) | NULLABLE | 详细地址 |
| store_manager_id | BIGINT | FK->sys_user.id | 店长ID |
| opening_date | DATE | NULLABLE | 开店日期 |
| store_type | VARCHAR(20) | DEFAULT 'NORMAL' | NEW/OLD/FLAGSHIP/NORMAL |
| status | VARCHAR(20) | DEFAULT 'OPEN' | OPEN/SUSPENDED/CLOSED |
| business_hours | VARCHAR(100) | NULLABLE | 营业时间 |
| contact_phone | VARCHAR(20) | NULLABLE | 联系电话 |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: uk_store_code, idx_region_id, idx_store_manager_id, idx_status

#### sys_user (用户表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 登录账号 |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt密码哈希 |
| real_name | VARCHAR(50) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | NULLABLE | 手机号 |
| avatar | VARCHAR(500) | NULLABLE | 头像URL |
| store_id | BIGINT | FK->sys_store.id | 所属门店 |
| region_id | BIGINT | FK->sys_organization.id | 所属区域 |
| position | VARCHAR(50) | NULLABLE | 岗位 |
| entry_date | DATE | NULLABLE | 入职日期 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/RESIGNED/DISABLED |
| last_login_at | DATETIME | NULLABLE | 最近登录时间 |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: uk_username, idx_phone, idx_store_id, idx_region_id, idx_status

#### sys_role (角色表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 角色ID |
| role_code | VARCHAR(50) | UNIQUE, NOT NULL | 角色编码(ROLE_ADMIN等) |
| role_name | VARCHAR(100) | NOT NULL | 角色名称 |
| data_scope | VARCHAR(20) | NOT NULL | ALL/REGION/STORE/SELF/CUSTOM |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| remark | VARCHAR(500) | NULLABLE | 备注 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: uk_role_code

#### sys_permission (权限表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 权限ID |
| parent_id | BIGINT | DEFAULT 0 | 上级权限ID(树形) |
| perm_name | VARCHAR(100) | NOT NULL | 权限名称 |
| perm_type | VARCHAR(20) | NOT NULL | MENU/BUTTON/API |
| perm_code | VARCHAR(100) | UNIQUE, NOT NULL | 权限标识(user:create等) |
| path | VARCHAR(200) | NULLABLE | 路由路径(菜单类型) |
| component | VARCHAR(200) | NULLABLE | 组件路径 |
| icon | VARCHAR(50) | NULLABLE | 图标 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| created_at | DATETIME | NOT NULL | 创建时间 |

索引: uk_perm_code, idx_parent_id

#### sys_user_role / sys_role_permission (关联表)
```
sys_user_role:        sys_role_permission:
  user_id (FK)          role_id (FK)
  role_id (FK)          permission_id (FK)
  PK(user_id,role_id)   PK(role_id,permission_id)
```

#### sys_role_data_scope (角色数据权限扩展表 - 自定义范围)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| role_id | BIGINT | FK->sys_role.id | 角色ID |
| scope_type | VARCHAR(20) | NOT NULL | REGION/STORE/BRAND |
| scope_value | BIGINT | NOT NULL | 对应的区域/门店/品牌ID |

索引: idx_role_id

### 4.2 任务类 (7张)

#### action_template (动作库表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 动作ID |
| action_name | VARCHAR(200) | NOT NULL | 动作名称 |
| dimension | VARCHAR(20) | NOT NULL | HUMAN/PRODUCT/SCENE/COMPREHENSIVE |
| category | VARCHAR(50) | NOT NULL | 分类(晨会/面谈/盘点等) |
| description | TEXT | NULLABLE | 动作说明 |
| execution_standard | TEXT | NULLABLE | 执行标准 |
| frequency_type | VARCHAR(20) | NOT NULL | DAILY/WEEKLY/MONTHLY/QUARTERLY/MANUAL/ABNORMAL |
| cron_expression | VARCHAR(100) | NULLABLE | 周期表达式 |
| due_time_rule | VARCHAR(100) | NULLABLE | 截止时间规则(如当日10:00) |
| required_photos | INT | DEFAULT 1 | 最少上传照片数 |
| required_text | TINYINT | DEFAULT 1 | 是否必填文字 |
| required_form | TINYINT | DEFAULT 0 | 是否绑定表单 |
| form_schema_id | BIGINT | FK->form_schema.id | 表单模板ID |
| require_audit | TINYINT | DEFAULT 1 | 是否需要审核 |
| default_auditor_role | VARCHAR(50) | NULLABLE | 默认审核角色 |
| score_weight | DECIMAL(5,2) | DEFAULT 1.00 | 分值权重 |
| is_default | TINYINT | DEFAULT 1 | 是否默认动作库 |
| is_force | TINYINT | DEFAULT 0 | 是否强制任务 |
| applicable_store_types | VARCHAR(200) | NULLABLE | 适用门店类型(逗号分隔) |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| created_by | BIGINT | FK->sys_user.id | 创建人 |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: idx_dimension, idx_frequency_type, idx_status

#### task_template (任务模板表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 模板ID |
| template_name | VARCHAR(200) | NOT NULL | 模板名称 |
| action_id | BIGINT | FK->action_template.id | 来源动作 |
| dimension | VARCHAR(20) | NOT NULL | 人/货/场/综合 |
| category | VARCHAR(50) | NOT NULL | 任务分类 |
| description | TEXT | NULLABLE | 任务说明 |
| execution_standard | TEXT | NULLABLE | 执行标准 |
| required_photos | INT | DEFAULT 1 | 最少照片数 |
| required_text | TINYINT | DEFAULT 1 | 是否必填文字 |
| required_form | TINYINT | DEFAULT 0 | 是否绑定表单 |
| form_schema_id | BIGINT | FK->form_schema.id | 表单模板ID |
| require_audit | TINYINT | DEFAULT 1 | 是否需要审核 |
| default_auditor_role | VARCHAR(50) | NULLABLE | 默认审核角色 |
| frequency_type | VARCHAR(20) | NOT NULL | 周期类型 |
| cron_expression | VARCHAR(100) | NULLABLE | Cron表达式 |
| due_time_rule | VARCHAR(100) | NULLABLE | 截止时间规则 |
| reminder_rule | JSON | NULLABLE | 提醒规则JSON |
| score_weight | DECIMAL(5,2) | DEFAULT 1.00 | 分值权重 |
| is_default | TINYINT | DEFAULT 0 | 是否默认动作库 |
| is_force | TINYINT | DEFAULT 0 | 是否强制任务 |
| applicable_store_ids | TEXT | NULLABLE | 适用门店ID(逗号分隔) |
| applicable_region_ids | TEXT | NULLABLE | 适用区域ID(逗号分隔) |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| created_by | BIGINT | FK->sys_user.id | 创建人 |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: idx_action_id, idx_frequency_type, idx_status

#### task_instance (任务实例表) — 核心表
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 任务ID |
| task_no | VARCHAR(50) | UNIQUE, NOT NULL | 任务编号(TK+日期+序号) |
| template_id | BIGINT | FK->task_template.id | 来源模板 |
| task_title | VARCHAR(200) | NOT NULL | 任务标题 |
| dimension | VARCHAR(20) | NOT NULL | HUMAN/PRODUCT/SCENE/COMPREHENSIVE |
| category | VARCHAR(50) | NOT NULL | 分类 |
| store_id | BIGINT | FK->sys_store.id, NOT NULL | 所属门店 |
| assignee_id | BIGINT | FK->sys_user.id, NOT NULL | 执行人 |
| auditor_id | BIGINT | FK->sys_user.id | 审核人 |
| start_time | DATETIME | NOT NULL | 开始时间 |
| due_time | DATETIME | NOT NULL | 截止时间 |
| completed_time | DATETIME | NULLABLE | 完成时间 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | PENDING/READY/IN_PROGRESS/SUBMITTED/AUDITING/APPROVED/COMPLETED/REJECTED/RECTIFYING/OVERDUE/CANCELLED/VOIDED/EXEMPTED |
| priority | VARCHAR(10) | DEFAULT 'MEDIUM' | LOW/MEDIUM/HIGH/URGENT |
| source_type | VARCHAR(20) | NOT NULL | CYCLE/MANUAL/HQ/ABNORMAL/HOLIDAY/AI |
| related_object_type | VARCHAR(50) | NULLABLE | 关联对象类型(EMPLOYEE/PRODUCT/STORE) |
| related_object_id | BIGINT | NULLABLE | 关联对象ID |
| is_overdue | TINYINT | DEFAULT 0 | 是否超时 |
| overdue_minutes | INT | DEFAULT 0 | 超时分钟数 |
| quality_score | DECIMAL(5,2) | NULLABLE | 执行质量分 |
| ai_score | DECIMAL(5,2) | NULLABLE | AI评分 |
| manual_score | DECIMAL(5,2) | NULLABLE | 人工评分 |
| final_score | DECIMAL(5,2) | NULLABLE | 最终得分 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | ON UPDATE | 更新时间 |

索引: uk_task_no, idx_assignee_id, idx_store_id, idx_status, idx_due_time, idx_template_id,
      idx_source_type, idx_is_overdue, idx_created_at

#### task_submission (任务提交表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 提交ID |
| task_id | BIGINT | FK->task_instance.id, NOT NULL | 任务ID |
| submitter_id | BIGINT | FK->sys_user.id, NOT NULL | 提交人 |
| text_content | TEXT | NULLABLE | 文字说明 |
| form_data | JSON | NULLABLE | 表单数据JSON |
| photo_urls | JSON | NULLABLE | 图片URL列表 |
| attachment_urls | JSON | NULLABLE | 附件URL列表 |
| location | JSON | NULLABLE | GPS定位 {lat, lng} |
| submitted_at | DATETIME | NOT NULL | 提交时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |

索引: idx_task_id, idx_submitter_id

#### task_audit (任务审核表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 审核ID |
| task_id | BIGINT | FK->task_instance.id, NOT NULL | 任务ID |
| submission_id | BIGINT | FK->task_submission.id | 提交ID |
| auditor_id | BIGINT | FK->sys_user.id, NOT NULL | 审核人 |
| audit_result | VARCHAR(20) | NOT NULL | APPROVED/REJECTED/RECTIFY |
| audit_comment | TEXT | NULLABLE | 审核意见 |
| score | DECIMAL(5,2) | NULLABLE | 人工评分(0-100) |
| audited_at | DATETIME | NOT NULL | 审核时间 |

索引: idx_task_id, idx_auditor_id

#### task_reminder_log (提醒日志表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| task_id | BIGINT | FK->task_instance.id | 任务ID |
| reminder_type | VARCHAR(20) | NOT NULL | START/DEADLINE_2H/DEADLINE_30M/OVERDUE/OVERDUE_24H |
| channel | VARCHAR(20) | NOT NULL | STATION/WECOM/SMS/PUSH |
| receiver_id | BIGINT | FK->sys_user.id | 接收人 |
| send_status | VARCHAR(10) | DEFAULT 'PENDING' | PENDING/SUCCESS/FAILED |
| sent_at | DATETIME | NULLABLE | 发送时间 |
| created_at | DATETIME | NOT NULL | |

索引: idx_task_id, idx_receiver_id

### 4.3 人效类 (6张)

#### employee_profile (员工档案表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| user_id | BIGINT | FK->sys_user.id, UNIQUE | 关联用户 |
| store_id | BIGINT | FK->sys_store.id | 所属门店 |
| position | VARCHAR(50) | NULLABLE | 岗位 |
| entry_date | DATE | NULLABLE | 入职日期 |
| level | VARCHAR(20) | DEFAULT 'STANDARD' | BENCHMARK/STANDARD/IMPROVING |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | ACTIVE/RESIGNED/DISABLED |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_user_id, idx_store_id, idx_level

#### employee_interview (员工面谈记录表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| employee_id | BIGINT | FK->sys_user.id, NOT NULL | 员工ID |
| interviewer_id | BIGINT | FK->sys_user.id | 面谈人(店长) |
| interview_date | DATE | NOT NULL | 面谈日期 |
| current_week_sales | DECIMAL(12,2) | NULLABLE | 本周业绩 |
| target_completion_rate | DECIMAL(5,2) | NULLABLE | 目标完成率(%) |
| main_problem | TEXT | NULLABLE | 主要问题 |
| customer_follow_issue | TEXT | NULLABLE | 客户跟进难点 |
| product_knowledge_gap | TEXT | NULLABLE | 产品知识短板 |
| mindset_status | VARCHAR(20) | NULLABLE | POSITIVE/NORMAL/LOW/ABNORMAL |
| next_week_goal | DECIMAL(12,2) | NULLABLE | 下周目标 |
| improvement_plan | TEXT | NULLABLE | 改进计划 |
| manager_comment | TEXT | NULLABLE | 店长评语 |
| employee_feedback | TEXT | NULLABLE | 员工反馈 |
| follow_up_date | DATE | NULLABLE | 跟进日期 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_employee_id, idx_interview_date, idx_interviewer_id

#### employee_assessment (员工能力考核表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| employee_id | BIGINT | FK->sys_user.id, NOT NULL | 员工ID |
| assessor_id | BIGINT | FK->sys_user.id | 考核人 |
| assessment_week | VARCHAR(20) | NOT NULL | 考核周(如2026-W01) |
| product_knowledge_score | DECIMAL(5,2) | NULLABLE | 产品知识(满分25) |
| matching_skill_score | DECIMAL(5,2) | NULLABLE | 搭配技巧(满分20) |
| reception_score | DECIMAL(5,2) | NULLABLE | 接待流程(满分20) |
| objection_handling_score | DECIMAL(5,2) | NULLABLE | 异议处理(满分20) |
| promotion_script_score | DECIMAL(5,2) | NULLABLE | 活动话术(满分15) |
| total_score | DECIMAL(5,2) | NULLABLE | 总分 |
| improvement_advice | TEXT | NULLABLE | 改进建议 |
| created_at | DATETIME | NOT NULL | |

索引: idx_employee_id, idx_assessment_week

#### employee_training (培训记录表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| training_title | VARCHAR(200) | NOT NULL | 培训标题 |
| training_type | VARCHAR(50) | NOT NULL | 新品知识/婚庆销售/古法黄金/钻石镶嵌/高毛利等 |
| trainer_id | BIGINT | FK->sys_user.id | 培训人 |
| training_date | DATE | NOT NULL | 培训日期 |
| exam_score | DECIMAL(5,2) | NULLABLE | 考核分 |
| training_summary | TEXT | NULLABLE | 培训总结 |
| material_urls | JSON | NULLABLE | 培训资料URL |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_trainer_id, idx_training_date

#### employee_training_record (培训参与记录表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| training_id | BIGINT | FK->employee_training.id | 培训ID |
| employee_id | BIGINT | FK->sys_user.id | 参与员工 |
| sign_in_status | VARCHAR(10) | DEFAULT 'SIGNED' | SIGNED/ABSENT/LATE |
| exam_score | DECIMAL(5,2) | NULLABLE | 个人考核分 |
| created_at | DATETIME | NOT NULL | |

索引: pk(training_id, employee_id)

#### employee_monthly_review (月度绩效复盘表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| employee_id | BIGINT | FK->sys_user.id, NOT NULL | 员工ID |
| reviewer_id | BIGINT | FK->sys_user.id | 复盘人 |
| review_month | VARCHAR(7) | NOT NULL | 月份(2026-01) |
| total_sales_amount | DECIMAL(12,2) | NULLABLE | 总销售额 |
| sales_order_count | INT | NULLABLE | 成交单数 |
| avg_order_amount | DECIMAL(10,2) | NULLABLE | 客单价 |
| new_customer_sales | DECIMAL(12,2) | NULLABLE | 新客销售 |
| old_customer_repurchase_sales | DECIMAL(12,2) | NULLABLE | 老客复购 |
| key_category_sales | JSON | NULLABLE | 品类销售JSON |
| service_score | DECIMAL(5,2) | NULLABLE | 服务评分 |
| task_execution_score | DECIMAL(5,2) | NULLABLE | 任务执行分 |
| reward_amount | DECIMAL(10,2) | NULLABLE | 奖励金额 |
| penalty_amount | DECIMAL(10,2) | NULLABLE | 处罚金额 |
| manager_review | TEXT | NULLABLE | 店长评语 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_employee_id, idx_review_month

#### employee_level_record (员工分层记录表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| employee_id | BIGINT | FK->sys_user.id, NOT NULL | 员工ID |
| eval_month | VARCHAR(7) | NOT NULL | 评估月份 |
| performance_score | DECIMAL(5,2) | NULLABLE | 业绩分 |
| service_score | DECIMAL(5,2) | NULLABLE | 服务分 |
| execution_score | DECIMAL(5,2) | NULLABLE | 执行分 |
| final_level | VARCHAR(20) | NOT NULL | BENCHMARK/STANDARD/IMPROVING |
| reason | TEXT | NULLABLE | 定级原因 |
| next_month_plan | TEXT | NULLABLE | 下月计划 |
| created_at | DATETIME | NOT NULL | |

索引: idx_employee_id, idx_eval_month

### 4.4 货品类 (6张)

#### product (商品表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 商品ID |
| product_code | VARCHAR(50) | UNIQUE, NOT NULL | 商品编码 |
| product_name | VARCHAR(200) | NOT NULL | 商品名称 |
| category | VARCHAR(20) | NOT NULL | GOLD/DIAMOND/K_GOLD/ANCIENT/SILVER/GEM/OTHER |
| style | VARCHAR(100) | NULLABLE | 款式 |
| material | VARCHAR(50) | NULLABLE | 材质 |
| weight | VARCHAR(50) | NULLABLE | 重量 |
| size | VARCHAR(50) | NULLABLE | 手寸/圈号 |
| color | VARCHAR(50) | NULLABLE | 颜色 |
| shape | VARCHAR(50) | NULLABLE | 形状 |
| meaning | VARCHAR(200) | NULLABLE | 寓意 |
| cost_price | DECIMAL(10,2) | NULLABLE | 成本价(权限控制) |
| retail_price | DECIMAL(10,2) | NULLABLE | 零售价 |
| gross_margin_rate | DECIMAL(5,2) | NULLABLE | 毛利率(%)(权限控制) |
| status | VARCHAR(20) | DEFAULT 'ON_SALE' | ON_SALE/SOLD/TRANSFER/REPAIR/OFF_SHELF |
| store_id | BIGINT | FK->sys_store.id | 所属门店 |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: uk_product_code, idx_category, idx_status, idx_store_id

#### product_inventory_check (每日盘点表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| check_date | DATE | NOT NULL | 检查日期 |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| checked_by | BIGINT | FK->sys_user.id | 检查人 |
| total_checked_count | INT | NOT NULL | 盘点总数 |
| abnormal_count | INT | DEFAULT 0 | 异常数量 |
| abnormal_items | JSON | NULLABLE | 异常商品列表JSON |
| photos | JSON | NULLABLE | 照片URL |
| remark | TEXT | NULLABLE | 备注 |
| created_at | DATETIME | NOT NULL | |

索引: idx_check_date, idx_store_id

#### product_maintenance_check (货品养护检查表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| product_id | BIGINT | FK->product.id | 商品 |
| category | VARCHAR(20) | NOT NULL | 品类 |
| check_date | DATE | NOT NULL | 检查日期 |
| checker_id | BIGINT | FK->sys_user.id | 检查人 |
| maintenance_result | VARCHAR(20) | NOT NULL | NORMAL/NEED_CLEAN/NEED_REPAIR/NEED_OFF_SHELF |
| issue_description | TEXT | NULLABLE | 问题描述 |
| photo_urls | JSON | NULLABLE | 照片 |
| handled_result | TEXT | NULLABLE | 处理结果 |
| created_at | DATETIME | NOT NULL | |

索引: idx_product_id, idx_check_date

#### product_sales_analysis (周度动销分析表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| analysis_week | VARCHAR(20) | NOT NULL | 周标识(2026-W01) |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| analyzer_id | BIGINT | FK->sys_user.id | 分析人 |
| hot_products | JSON | NULLABLE | 爆款商品JSON |
| normal_products | JSON | NULLABLE | 平销商品 |
| slow_products | JSON | NULLABLE | 慢销商品 |
| no_sales_7_days | JSON | NULLABLE | 连续7天无动销 |
| stockout_risk_products | JSON | NULLABLE | 缺货风险商品 |
| analysis_summary | TEXT | NULLABLE | 分析总结 |
| action_plan | TEXT | NULLABLE | 行动计划 |
| created_at | DATETIME | NOT NULL | |

索引: idx_analysis_week, idx_store_id

#### new_product_plan (新品推介方案表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| plan_month | VARCHAR(7) | NOT NULL | 计划月份 |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| planner_id | BIGINT | FK->sys_user.id | 策划人 |
| new_product_list | JSON | NULLABLE | 新品清单JSON |
| selling_points | TEXT | NULLABLE | 核心卖点 |
| target_customer_group | TEXT | NULLABLE | 主推人群 |
| display_plan | TEXT | NULLABLE | 陈列展示方案 |
| training_plan | TEXT | NULLABLE | 员工培训计划 |
| sales_target | DECIMAL(12,2) | NULLABLE | 销售目标 |
| promotion_script | TEXT | NULLABLE | 推荐话术 |
| attachment_urls | JSON | NULLABLE | 附件 |
| status | VARCHAR(20) | DEFAULT 'DRAFT' | DRAFT/SUBMITTED/APPROVED |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_plan_month, idx_store_id

#### promotion_plan (促销活动筹备表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| activity_month | VARCHAR(7) | NOT NULL | 活动月份 |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| activity_name | VARCHAR(200) | NOT NULL | 活动名称 |
| activity_theme | VARCHAR(200) | NULLABLE | 活动主题 |
| activity_period_start | DATE | NULLABLE | 活动开始 |
| activity_period_end | DATE | NULLABLE | 活动结束 |
| promotion_rules | TEXT | NULLABLE | 优惠规则 |
| main_products | JSON | NULLABLE | 主推商品 |
| material_requirements | TEXT | NULLABLE | 活动物料 |
| employee_script | TEXT | NULLABLE | 员工话术 |
| preheat_plan | TEXT | NULLABLE | 预热计划 |
| customer_reach_plan | TEXT | NULLABLE | 老客触达计划 |
| expected_sales | DECIMAL(12,2) | NULLABLE | 预期销售额 |
| status | VARCHAR(20) | DEFAULT 'DRAFT' | DRAFT/SUBMITTED/APPROVED |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_activity_month, idx_store_id

### 4.5 场景类 (5张)

#### scene_health_inspection (卫生巡检表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| inspection_time | VARCHAR(10) | NOT NULL | MORNING/MIDDAY/EVENING |
| inspection_date | DATE | NOT NULL | 巡检日期 |
| inspector_id | BIGINT | FK->sys_user.id | 巡检人 |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| area_results | JSON | NULLABLE | 各区域检查结果JSON |
| issue_description | TEXT | NULLABLE | 问题描述 |
| photo_urls | JSON | NULLABLE | 照片 |
| rectification_required | TINYINT | DEFAULT 0 | 是否需要整改 |
| created_at | DATETIME | NOT NULL | |

索引: idx_inspection_date, idx_store_id

#### scene_display_inspection (陈列检查表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| inspection_date | DATE | NOT NULL | 检查日期 |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| inspector_id | BIGINT | FK->sys_user.id | 检查人 |
| display_area | VARCHAR(50) | NOT NULL | 区域(黄金区/钻石区/K金区/古法区/银饰区/C位) |
| standard_score | DECIMAL(5,2) | NULLABLE | 标准分 |
| issue_description | TEXT | NULLABLE | 问题描述 |
| before_photos | JSON | NULLABLE | 整改前照片 |
| after_photos | JSON | NULLABLE | 整改后照片 |
| rectification_plan | TEXT | NULLABLE | 整改方案 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_inspection_date, idx_store_id, idx_display_area

#### scene_material_update (物料更新表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| checker_id | BIGINT | FK->sys_user.id | 检查人 |
| check_date | DATE | NOT NULL | 检查日期 |
| material_type | VARCHAR(20) | NOT NULL | POSTER/FLAG/STAND/CARD |
| current_status | VARCHAR(10) | NOT NULL | NORMAL/EXPIRED/DAMAGED/MISSING |
| updated_photos | JSON | NULLABLE | 更新后照片 |
| issue_description | TEXT | NULLABLE | 问题说明 |
| replacement_required | TINYINT | DEFAULT 0 | 是否需要更换 |
| created_at | DATETIME | NOT NULL | |

索引: idx_check_date, idx_store_id

#### scene_equipment_check (设备检查表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| checker_id | BIGINT | FK->sys_user.id | 检查人 |
| check_date | DATE | NOT NULL | 检查日期 |
| equipment_type | VARCHAR(30) | NOT NULL | LIGHT/AC/CAMERA/AUDIO/POS/CABINET_LIGHT/SAFE/NETWORK |
| status | VARCHAR(10) | NOT NULL | NORMAL/ABNORMAL |
| issue_description | TEXT | NULLABLE | 问题描述 |
| repair_required | TINYINT | DEFAULT 0 | 是否报修 |
| photo_urls | JSON | NULLABLE | 照片 |
| created_at | DATETIME | NOT NULL | |

索引: idx_check_date, idx_store_id

#### scene_customer_experience_review (客户体验复盘表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| store_id | BIGINT | FK->sys_store.id | 门店 |
| reviewer_id | BIGINT | FK->sys_user.id | 复盘人 |
| review_week | VARCHAR(20) | NOT NULL | 周标识 |
| feedback_count | INT | DEFAULT 0 | 反馈数 |
| complaint_count | INT | DEFAULT 0 | 投诉数 |
| common_feedback | TEXT | NULLABLE | 常见反馈 |
| improvement_plan | TEXT | NULLABLE | 改进计划 |
| responsible_person_id | BIGINT | FK->sys_user.id | 负责人 |
| created_at | DATETIME | NOT NULL | |

索引: idx_review_week, idx_store_id

### 4.6 销售类 (3张)

#### sales_record (销售记录表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 销售记录ID |
| sales_no | VARCHAR(50) | UNIQUE, NOT NULL | 销售单号 |
| store_id | BIGINT | FK->sys_store.id, NOT NULL | 门店 |
| employee_id | BIGINT | FK->sys_user.id, NOT NULL | 导购 |
| sales_date | DATE | NOT NULL | 销售日期 |
| order_no | VARCHAR(100) | NULLABLE | 外部单据号 |
| total_amount | DECIMAL(12,2) | NOT NULL | 成交金额 |
| paid_amount | DECIMAL(12,2) | NOT NULL | 实收金额 |
| product_count | INT | NOT NULL | 商品数量 |
| customer_type | VARCHAR(10) | NOT NULL | NEW/OLD |
| customer_gender | VARCHAR(10) | NULLABLE | MALE/FEMALE/UNKNOWN |
| customer_age_range | VARCHAR(10) | NULLABLE | 18-25/26-35/36-45/46+ |
| purchase_scene | VARCHAR(20) | NOT NULL | WEDDING/GIFT/SELF/INVEST/HOLIDAY/OTHER |
| customer_concern | TEXT | NULLABLE | 客户成交时在意点 |
| sales_photo_urls | JSON | NULLABLE | 销售单据照片 |
| remark | TEXT | NULLABLE | 备注 |
| audit_status | VARCHAR(10) | DEFAULT 'PENDING' | PENDING/AUDITED/REJECTED |
| auditor_id | BIGINT | FK->sys_user.id | 审核人 |
| audit_comment | TEXT | NULLABLE | 审核意见 |
| audited_at | DATETIME | NULLABLE | 审核时间 |
| external_source | VARCHAR(50) | NULLABLE | POS系统来源(预留) |
| external_order_id | VARCHAR(100) | NULLABLE | POS订单号(预留) |
| sync_status | VARCHAR(10) | NULLABLE | 同步状态(预留) |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: uk_sales_no, idx_sales_date, idx_store_id, idx_employee_id, idx_audit_status

#### sales_item (销售明细表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | 明细ID |
| sales_record_id | BIGINT | FK->sales_record.id, NOT NULL | 关联销售单 |
| product_id | BIGINT | NULLABLE | 商品ID(可选) |
| product_name | VARCHAR(200) | NOT NULL | 商品名称 |
| category | VARCHAR(20) | NOT NULL | 品类 |
| style | VARCHAR(100) | NULLABLE | 款式 |
| material | VARCHAR(50) | NULLABLE | 材质 |
| weight | VARCHAR(50) | NULLABLE | 重量 |
| size | VARCHAR(50) | NULLABLE | 手寸/圈号 |
| color | VARCHAR(50) | NULLABLE | 颜色 |
| shape | VARCHAR(50) | NULLABLE | 形状 |
| meaning | VARCHAR(200) | NULLABLE | 寓意 |
| price | DECIMAL(10,2) | NOT NULL | 成交价 |
| quantity | INT | DEFAULT 1 | 数量 |
| gross_margin_rate | DECIMAL(5,2) | NULLABLE | 毛利率(%) |
| customer_favorite_point | TEXT | NULLABLE | 客户喜欢点 |
| objection | TEXT | NULLABLE | 客户异议 |
| closing_reason | TEXT | NULLABLE | 成交原因 |
| product_photo_urls | JSON | NULLABLE | 货品图 |
| created_at | DATETIME | NOT NULL | |

索引: idx_sales_record_id, idx_product_id, idx_category

### 4.7 AI与报表类 (5张)

#### prompt_template (提示词模板表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| template_name | VARCHAR(200) | NOT NULL | 模板名称 |
| business_type | VARCHAR(20) | NOT NULL | EMPLOYEE/PRODUCT/SCENE/TASK |
| prompt_content | TEXT | NOT NULL | 提示词内容(支持变量占位符) |
| input_schema | JSON | NULLABLE | 输入变量Schema |
| output_schema | JSON | NULLABLE | 输出格式Schema |
| model_name | VARCHAR(50) | DEFAULT 'gpt-4' | 模型名称 |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| created_by | BIGINT | FK->sys_user.id | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_business_type, idx_status

#### ai_result (AI结果表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| business_type | VARCHAR(20) | NOT NULL | EMPLOYEE/PRODUCT/SCENE/TASK |
| related_id | BIGINT | NOT NULL | 关联对象ID |
| prompt_template_id | BIGINT | FK->prompt_template.id | 提示词模板 |
| input_snapshot | JSON | NULLABLE | 输入快照 |
| output_text | TEXT | NULLABLE | 输出结果 |
| output_json | JSON | NULLABLE | 结构化结果 |
| score | DECIMAL(5,2) | NULLABLE | AI分数 |
| model_name | VARCHAR(50) | NULLABLE | 模型名称 |
| token_usage | JSON | NULLABLE | Token消耗 |
| status | VARCHAR(10) | DEFAULT 'SUCCESS' | SUCCESS/FAILED |
| created_at | DATETIME | NOT NULL | |

索引: idx_business_type_related(business_type, related_id), idx_created_at

#### store_monthly_score (门店月度评分表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| store_id | BIGINT | FK->sys_store.id, NOT NULL | 门店 |
| score_month | VARCHAR(7) | NOT NULL | 评分月份 |
| total_score | DECIMAL(5,2) | NULLABLE | 总分(100) |
| human_score | DECIMAL(5,2) | NULLABLE | 人效分(35%) |
| product_score | DECIMAL(5,2) | NULLABLE | 货品分(30%) |
| scene_score | DECIMAL(5,2) | NULLABLE | 场景分(25%) |
| discipline_score | DECIMAL(5,2) | NULLABLE | 纪律分(10%) |
| overdue_count | INT | DEFAULT 0 | 超时次数 |
| rejected_count | INT | DEFAULT 0 | 被驳回次数 |
| ranking | INT | NULLABLE | 排名 |
| detail_json | JSON | NULLABLE | 详细评分JSON |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: uk_store_month(store_id, score_month), idx_ranking

#### report_snapshot (报表快照表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| report_type | VARCHAR(50) | NOT NULL | 报表类型 |
| report_period | VARCHAR(50) | NOT NULL | 报表周期(2026-01/2026-Q1) |
| store_id | BIGINT | FK->sys_store.id | 门店(NULL=全系统) |
| report_json | JSON | NOT NULL | 报表数据JSON |
| generated_at | DATETIME | NOT NULL | 生成时间 |

索引: idx_report_type_period(report_type, report_period), idx_store_id

### 4.8 系统支持类 (4张)

#### notification (消息通知表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| receiver_id | BIGINT | FK->sys_user.id, NOT NULL | 接收人 |
| title | VARCHAR(200) | NOT NULL | 标题 |
| content | TEXT | NULLABLE | 内容 |
| notification_type | VARCHAR(20) | NOT NULL | TASK_REMIND/OVERDUE/AUDIT/REJECT/ABNORMAL/AI_ADVICE/HQ_NOTICE |
| business_type | VARCHAR(50) | NULLABLE | 业务类型 |
| business_id | BIGINT | NULLABLE | 业务ID |
| is_read | TINYINT | DEFAULT 0 | 是否已读 |
| read_at | DATETIME | NULLABLE | 阅读时间 |
| channel | VARCHAR(20) | DEFAULT 'STATION' | STATION/WECOM/SMS/PUSH |
| send_status | VARCHAR(10) | DEFAULT 'PENDING' | PENDING/SUCCESS/FAILED |
| created_at | DATETIME | NOT NULL | |

索引: idx_receiver_id, idx_is_read, idx_created_at, idx_notification_type

#### file_resource (文件资源表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| file_name | VARCHAR(500) | NOT NULL | 原始文件名 |
| file_key | VARCHAR(500) | UNIQUE, NOT NULL | 存储Key |
| file_url | VARCHAR(500) | NOT NULL | 访问URL |
| file_type | VARCHAR(20) | NOT NULL | IMAGE/DOCUMENT/VIDEO/OTHER |
| file_size | BIGINT | NOT NULL | 文件大小(字节) |
| mime_type | VARCHAR(100) | NULLABLE | MIME类型 |
| storage_type | VARCHAR(20) | DEFAULT 'MINIO' | MINIO/OSS/COS |
| uploader_id | BIGINT | FK->sys_user.id | 上传人 |
| business_type | VARCHAR(50) | NULLABLE | 关联业务类型 |
| business_id | BIGINT | NULLABLE | 关联业务ID |
| is_deleted | TINYINT | DEFAULT 0 | 逻辑删除 |
| created_at | DATETIME | NOT NULL | |

索引: uk_file_key, idx_uploader_id, idx_business(business_type, business_id)

#### form_schema (表单模板表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| form_name | VARCHAR(200) | NOT NULL | 表单名称 |
| form_json_schema | JSON | NOT NULL | JSON Schema定义 |
| status | VARCHAR(10) | DEFAULT 'ENABLED' | ENABLED/DISABLED |
| created_by | BIGINT | FK->sys_user.id | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | ON UPDATE | |

索引: idx_status

#### operate_log (操作日志表)
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK AUTO_INCREMENT | |
| operator_id | BIGINT | FK->sys_user.id | 操作人 |
| module | VARCHAR(50) | NOT NULL | 操作模块 |
| action | VARCHAR(50) | NOT NULL | 操作动作(CREATE/UPDATE/DELETE/SUBMIT/AUDIT) |
| target_type | VARCHAR(50) | NULLABLE | 操作对象类型 |
| target_id | BIGINT | NULLABLE | 操作对象ID |
| request_ip | VARCHAR(50) | NULLABLE | 请求IP |
| request_params | JSON | NULLABLE | 请求参数(脱敏) |
| old_data | JSON | NULLABLE | 修改前数据 |
| new_data | JSON | NULLABLE | 修改后数据 |
| created_at | DATETIME | NOT NULL | |

索引: idx_operator_id, idx_module, idx_created_at

### 4.9 数据库总体ER关系

```
sys_organization ───< sys_store ───< sys_user
       │                              │(N:N)
       │                          sys_user_role ──< sys_role ──< sys_role_permission >── sys_permission
       │                                                │
       │                                          sys_role_data_scope
       │
  ┌────┴────────────────────────────────────────────┐
  │  task_template ──< task_instance ──< task_submission  │
  │       │                 │──< task_audit               │
  │       │                 │──< task_reminder_log         │
  │  action_template                                       │
  │                                                        │
  │  employee_profile ──< employee_interview               │
  │       │──< employee_assessment                         │
  │       │──< employee_training ──< employee_training_record│
  │       │──< employee_monthly_review                     │
  │       │──< employee_level_record                       │
  │                                                        │
  │  product ──< product_inventory_check                   │
  │       │──< product_maintenance_check                   │
  │       │──< product_sales_analysis                      │
  │       │──< new_product_plan                            │
  │       │──< promotion_plan                              │
  │                                                        │
  │  scene_health_inspection                               │
  │  scene_display_inspection                              │
  │  scene_material_update                                 │
  │  scene_equipment_check                                 │
  │  scene_customer_experience_review                      │
  │                                                        │
  │  sales_record ──< sales_item                           │
  │                                                        │
  │  prompt_template ──< ai_result                         │
  │  store_monthly_score / report_snapshot                 │
  │  notification / file_resource / form_schema / operate_log│
  └────────────────────────────────────────────────────────┘
```

---

## 5. 七大核心模块拆分

### 5.1 模块总览

| 编号 | 模块名称 | 包路径 | 职责 | 依赖模块 |
|------|---------|--------|------|---------|
| M1 | 组织权限 | auth/organization/user/role | 登录认证、组织树、门店/用户CRUD、RBAC、数据权限 | 无 |
| M2 | 任务中心 | task/actiontemplate | 动作库管理、任务模板、周期任务生成、任务列表、执行提交、审核驳回、超时提醒 | M1, M7(文件) |
| M3 | 人效管理 | human | 晨夕会、面谈、能力考核、培训、绩效复盘、员工分层 | M1, M2(任务), M7 |
| M4 | 货品管理 | product | 商品档案、盘点、养护、动销分析、新品推介、促销筹备 | M1, M2(任务), M7 |
| M5 | 场景运营 | scene | 卫生巡检、陈列检查、物料更新、设备检查、客户体验复盘 | M1, M2(任务), M7 |
| M6 | 业绩数据 | sales | 销售录入、销售审核、业绩统计、指标计算 | M1, M2(任务), M7 |
| M7 | AI辅助 | ai | 提示词管理、数据组装、大模型调用、结果解析、AI评分 | M1, M3, M4, M5, M6 |
| M8 | 报表中心 | report | 门店评分、员工排名、商品报表、任务报表 | M1, M2, M3, M4, M5, M6 |
| M9 | 消息通知 | notification | 站内信、多渠道推送、已读/未读、预警中心 | M1, M2 |

### 5.2 各模块核心Service

#### M1 组织权限
- **AuthService**: login(token生成)/logout/refreshToken/selectStore
- **OrganizationService**: 组织树CRUD、启停、排序
- **StoreService**: 门店CRUD、店长绑定
- **UserService**: 用户CRUD、在职/离职状态、密码重置
- **RoleService**: 角色CRUD、角色-权限关联、数据范围配置
- **PermissionService**: 菜单/按钮/API权限树、角色权限矩阵

#### M2 任务中心 (最核心)
- **ActionTemplateService**: 动作库CRUD、启用/停用、强制配置、适用门店设置
- **TaskTemplateService**: 模板CRUD、关联动作库、配置周期/审核/评分
- **TaskGenerateService**: 根据模板+Cron自动生成task_instance (XXL-JOB调度)
- **TaskService**: 任务列表(多维筛选)、任务详情、手动创建、批量下发、取消/作废
- **TaskSubmitService**: 暂存/提交、图片/附件/表单数据保存
- **TaskAuditService**: 通过/驳回/整改、审核意见、人工评分
- **TaskReminderService**: 根据reminder_rule推送多级提醒

#### M3 人效管理
- **HumanService**: 晨会/夕会/仪容检查 CRUD
- **InterviewService**: 员工面谈记录
- **AssessmentService**: 周度能力考核
- **TrainingService**: 培训主题+参与记录
- **MonthlyReviewService**: 月度绩效复盘(自动汇总业绩数据)
- **LevelService**: 员工分层定级(根据规则自动计算)

#### M4 货品管理
- **ProductService**: 商品档案CRUD
- **InventoryCheckService**: 每日盘点
- **MaintenanceCheckService**: 货品养护
- **SalesAnalysisService**: 周度动销分析
- **NewProductPlanService**: 新品推介方案
- **PromotionPlanService**: 促销活动筹备

#### M5 场景运营
- **HealthInspectionService**: 卫生巡检(早中晚)
- **DisplayInspectionService**: 陈列检查
- **MaterialUpdateService**: 物料更新
- **EquipmentCheckService**: 设备检查
- **CustomerExperienceService**: 客户体验复盘

#### M6 业绩数据
- **SalesRecordService**: 销售录入、销售单CRUD、多条件查询
- **SalesAuditService**: 店长审核(通过/驳回/标记异常)
- **SalesStatsService**: 员工/门店/品类维度指标统计

#### M7 AI辅助
- **AIGatewayService**: 多模型适配(策略模式, 默认GPT, 预留文心/通义)
- **DataAssembler**: 根据business_type组装输入数据
- **ResultParser**: 解析AI返回为结构化数据
- **AIScoreService**: 任务质量AI评分
- **AIService**: AI建议生成(员工/货品/场景), 一键转任务
- **PromptTemplateService**: 提示词模板管理

#### M8 报表中心
- **ScoreCalcService**: 门店月度评分引擎(人效35% + 货品30% + 场景25% + 纪律10%)
- **ReportService**: 各类报表数据聚合, Excel导出
- **RankingService**: 各类排行榜(门店/员工/维度)

#### M9 消息通知
- **NotificationService**: 消息创建/已读/未读数
- **ChannelService**: 多渠道分发(站内信/企业微信/短信, 策略模式)
## 6. 全部RESTful API清单

> 统一前缀: `/api/v1`
> 统一响应体: `{ "code": 200, "msg": "success", "data": {...} }`
> 分页响应: `{ "code": 200, "msg": "success", "data": { "list": [...], "total": 100, "page": 1, "size": 20 } }`
> Token: Header `Authorization: Bearer <jwt_token>`
> 标注: [M] = 移动端导购/店长, [P] = PC总部管理端, [ALL] = 两端共用

### 6.1 认证模块 (auth)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 1 | POST | /auth/login | ALL | 登录 | { username, password } | { token, user: {userId,realName,role,storeId,regionId,avatar} } |
| 2 | POST | /auth/logout | ALL | 登出 | - | { } |
| 3 | POST | /auth/refresh | ALL | 刷新Token | { refreshToken } | { token } |
| 4 | GET | /auth/me | ALL | 当前用户信息 | - | { userId,realName,role,storeId,regionId,permissions:[] } |
| 5 | POST | /auth/select-store | M | 切换门店(多门店角色) | { storeId } | { token } |
| 6 | PUT | /auth/password | ALL | 修改密码 | { oldPassword, newPassword } | { } |

### 6.2 组织架构 (organization)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 7 | GET | /organizations/tree | P | 组织架构树 | - | [{ id,parentId,orgName,orgType,children:[...] }] |
| 8 | POST | /organizations | P | 新增组织 | { parentId,orgName,orgType,sortOrder } | { id } |
| 9 | PUT | /organizations/{id} | P | 编辑组织 | { orgName,sortOrder,status } | { } |
| 10 | DELETE | /organizations/{id} | P | 删除组织(逻辑) | - | { } |
| 11 | GET | /stores | P | 门店列表 | ?regionId&status&page&size | { list:[{storeId,storeName,storeCode,regionId,storeType,status,...}], total } |
| 12 | GET | /stores/{id} | ALL | 门店详情 | - | { storeId,storeName,storeCode,address,storeManagerId,businessHours,... } |
| 13 | POST | /stores | P | 新增门店 | { storeName,storeCode,regionId,address,storeManagerId,openingDate,storeType,... } | { id } |
| 14 | PUT | /stores/{id} | P | 编辑门店 | { storeName,address,storeManagerId,status,... } | { } |
| 15 | GET | /stores/my | M | 我的门店(店长/导购) | - | { storeId,storeName,storeCode } |

### 6.3 用户管理 (user)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 16 | GET | /users | P | 用户列表 | ?storeId&regionId&roleId&status&keyword&page&size | { list:[{userId,username,realName,phone,roleName,storeName,status,...}], total } |
| 17 | GET | /users/{id} | P | 用户详情 | - | { userId,username,realName,phone,avatar,roleIds:[],storeId,position,entryDate,... } |
| 18 | POST | /users | P | 新增用户 | { username,password,realName,phone,roleIds:[],storeId,position,... } | { id } |
| 19 | PUT | /users/{id} | P | 编辑用户 | { realName,phone,roleIds:[],storeId,position,status } | { } |
| 20 | PUT | /users/{id}/reset-password | P | 重置密码 | { newPassword } | { } |
| 21 | GET | /users/store-staff | ALL | 获取门店员工列表 | ?storeId&keyword | [{ userId,realName,position,avatar }] |
| 22 | PUT | /users/profile | ALL | 修改个人信息 | { realName,phone,avatar } | { } |

### 6.4 角色权限 (role)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 23 | GET | /roles | P | 角色列表 | ?status&page&size | { list:[{roleId,roleCode,roleName,dataScope,status,...}], total } |
| 24 | POST | /roles | P | 新增角色 | { roleCode,roleName,dataScope,remark } | { id } |
| 25 | PUT | /roles/{id} | P | 编辑角色 | { roleName,dataScope,remark,status } | { } |
| 26 | DELETE | /roles/{id} | P | 删除角色 | - | { } |
| 27 | GET | /permissions/tree | P | 权限树(菜单+按钮+API) | - | [{ id,parentId,permName,permType,permCode,path,icon,children:[...] }] |
| 28 | GET | /roles/{roleId}/permissions | P | 获取角色权限 | - | { roleId, permissionIds:[] } |
| 29 | PUT | /roles/{roleId}/permissions | P | 设置角色权限 | { permissionIds:[] } | { } |
| 30 | PUT | /roles/{roleId}/data-scope | P | 设置数据权限范围 | { scopeType, scopeValues:[] } | { } |

### 6.5 动作库 (action-template)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 31 | GET | /actions | P | 动作库列表 | ?dimension&frequencyType&status&keyword&page&size | { list:[{...}], total } |
| 32 | GET | /actions/{id} | P | 动作详情 | - | { actionId,actionName,dimension,category,executionStandard,... } |
| 33 | POST | /actions | P | 新增动作 | { actionName,dimension,category,frequencyType,executionStandard,requiredPhotos,... } | { id } |
| 34 | PUT | /actions/{id} | P | 编辑动作 | { actionName,executionStandard,frequencyType,cronExpression,... } | { } |
| 35 | PUT | /actions/{id}/status | P | 启用/停用 | { status } | { } |
| 36 | POST | /actions/{id}/dispatch | P | 一键下发到门店 | { storeIds:[], isForce } | { generatedTasks } |
| 37 | GET | /actions/store-available | M | 门店可选动作库(店长) | ?dimension&storeId | [{ actionId,actionName,... }] |

### 6.6 任务模板 (task-template) — PC端

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 38 | GET | /task-templates | P | 模板列表 | ?dimension&category&status&page&size | { list:[{...}], total } |
| 39 | GET | /task-templates/{id} | P | 模板详情 | - | { templateId,templateName,actionId,dimension,executionStandard,... } |
| 40 | POST | /task-templates | P | 创建模板 | { templateName,actionId,dimension,executionStandard,requiredPhotos,requireAudit,frequencyType,cronExpression,... } | { id } |
| 41 | PUT | /task-templates/{id} | P | 编辑模板 | { templateName,executionStandard,frequencyType,cronExpression,... } | { } |
| 42 | PUT | /task-templates/{id}/status | P | 启用/停用 | { status } | { } |
| 43 | POST | /task-templates/{id}/generate | P | 手动触发任务生成 | { storeIds:[], executeDate } | { generatedCount } |

### 6.7 任务实例 (task) — 核心

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 44 | GET | /tasks | P | 全部任务列表(总部) | ?storeId&assigneeId&dimension&status&sourceType&startDate&endDate&keyword&isOverdue&page&size | { list:[{taskId,taskTitle,dimension,status,assigneeName,storeName,dueTime,...}], total } |
| 45 | GET | /tasks/my | M | 我的任务(导购/店长) | ?status(TODO/TODAY/OVERDUE/AUDIT_PENDING/REJECTED/COMPLETED)&dimension&page&size | { list:[{taskId,taskTitle,dimension,status,dueTime,priority,isOverdue,...}], total } |
| 46 | GET | /tasks/my-audit | M | 待我审核(店长) | ?status&page&size | { list:[{taskId,taskTitle,dimension,assigneeName,status,submittedAt,...}], total } |
| 47 | GET | /tasks/{id} | ALL | 任务详情 | - | { taskId,taskTitle,dimension,category,executionStandard,status,dueTime,submission:{...},audits:[{...}],aiAdvice:{...} } |
| 48 | POST | /tasks | P | 手动创建任务 | { taskTitle,templateId,storeId,assigneeId,auditorId,startTime,dueTime,priority,sourceType } | { id } |
| 49 | POST | /tasks/batch | P | 批量下发任务 | { taskTitle,templateId,storeIds:[],assigneeIds:[],auditorId,startTime,dueTime,priority } | { generatedCount } |
| 50 | GET | /tasks/{id}/timeline | ALL | 任务流转时间线 | - | [{ time,action,operator,detail }] |

### 6.8 任务执行与提交

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 51 | PUT | /tasks/{id}/start | M | 开始执行 | - | { } |
| 52 | POST | /tasks/{id}/draft | M | 保存草稿 | { textContent,formData:{},photoUrls:[],attachmentUrls:[] } | { submissionId } |
| 53 | POST | /tasks/{id}/submit | M | 提交任务 | { textContent,formData:{},photoUrls:[],attachmentUrls:[],location:{lat,lng} } | { submissionId } |
| 54 | GET | /tasks/{id}/submission | ALL | 查看提交详情 | - | { submissionId,textContent,formData,photoUrls,attachmentUrls,submittedAt } |
| 55 | GET | /tasks/{id}/ai-advice | M | 获取AI辅助填写建议 | - | { advice:{ summary, suggestions:[] } } |

### 6.9 任务审核

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 56 | POST | /tasks/{id}/audit | M/P | 审核任务 | { auditResult:APPROVED/REJECTED/RECTIFY, auditComment, score } | { auditId } |
| 57 | GET | /tasks/{id}/audit-history | ALL | 审核历史 | - | [{ auditId,auditorName,auditResult,auditComment,score,auditedAt }] |
| 58 | GET | /tasks/audit-stats | M | 审核统计(店长首页) | ?storeId | { pendingCount,todayAuditedCount,rejectedCount } |

### 6.10 人效管理 (human)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 59 | POST | /human/meetings | M | 提交晨会/夕会 | { meetingType:MORNING/EVENING, meetingDate, storeTargetAmount, mainProducts, todayStrategy, employeeTargets:{}, meetingPhotoUrls:[], remarks,... } | { id } |
| 60 | GET | /human/meetings | ALL | 会议记录列表 | ?storeId&meetingType&startDate&endDate&page&size | { list:[{...}], total } |
| 61 | GET | /human/meetings/{id} | ALL | 会议详情 | - | { id, meetingType, meetingDate, storeTargetAmount, actualSalesAmount, targetCompletionRate, ... } |
| 62 | POST | /human/interviews | M | 提交面谈记录 | { employeeId,interviewDate,currentWeekSales,mainProblem,mindsetStatus,nextWeekGoal,improvementPlan,managerComment,... } | { id } |
| 63 | GET | /human/interviews | ALL | 面谈列表 | ?employeeId&storeId&startDate&endDate&page&size | { list:[{...}], total } |
| 64 | GET | /human/interviews/{id} | ALL | 面谈详情 | - | { id,employeeId,interviewDate,mainProblem,mindsetStatus,improvementPlan,... } |
| 65 | POST | /human/assessments | M | 提交能力考核 | { employeeId,assessmentWeek,productKnowledgeScore,matchingSkillScore,receptionScore,objectionHandlingScore,promotionScriptScore,improvementAdvice } | { id } |
| 66 | GET | /human/assessments | ALL | 考核列表 | ?employeeId&assessmentWeek&page&size | { list:[{...}], total } |
| 67 | GET | /human/assessments/employee/{employeeId} | ALL | 员工考核趋势 | ?months=6 | [{ assessmentWeek, totalScore, detailScores:{} }] |
| 68 | POST | /human/trainings | M/P | 创建培训 | { trainingTitle,trainingType,trainerId,trainingDate,trainingSummary,materialUrls:[],participantIds:[] } | { id } |
| 69 | GET | /human/trainings | ALL | 培训列表 | ?storeId&trainingType&page&size | { list:[{...}], total } |
| 70 | PUT | /human/trainings/{id}/sign-in | M | 培训签到 | { employeeId, signInStatus } | { } |
| 71 | PUT | /human/trainings/{id}/exam-score | M | 录入考核分 | { employeeId, examScore } | { } |
| 72 | POST | /human/monthly-reviews | M | 提交月度绩效复盘 | { employeeId,reviewMonth,totalSalesAmount,salesOrderCount,avgOrderAmount,... } | { id } |
| 73 | GET | /human/monthly-reviews | ALL | 绩效复盘列表 | ?storeId&reviewMonth&page&size | { list:[{...}], total } |
| 74 | POST | /human/level-records | M/P | 员工分层定级 | { employeeId,evalMonth,finalLevel,reason,nextMonthPlan } | { id } |
| 75 | GET | /human/level-records | ALL | 分层记录 | ?storeId&evalMonth&finalLevel&page&size | { list:[{...}], total } |
| 76 | GET | /human/employees/{employeeId}/profile | ALL | 员工综合档案 | - | { basicInfo:{}, salesTrend:[], interviews:[], assessments:[], trainingRecords:[], levelHistory:[], aiPortrait:{advantages,weaknesses,risks,suggestions} } |

### 6.11 货品管理 (product)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 77 | GET | /products | ALL | 商品列表 | ?category&status&storeId&keyword&page&size | { list:[{productId,productCode,productName,category,retailPrice,status,...}], total } |
| 78 | GET | /products/{id} | ALL | 商品详情 | - | { productId,productCode,productName,category,...,costPrice(权限控制) } |
| 79 | POST | /products | P | 新增商品 | { productCode,productName,category,style,material,weight,size,costPrice,retailPrice,storeId,... } | { id } |
| 80 | PUT | /products/{id} | P | 编辑商品 | { productName,category,retailPrice,status,... } | { } |
| 81 | POST | /products/inventory-checks | M | 提交盘点记录 | { checkDate,storeId,totalCheckedCount,abnormalCount,abnormalItems:[],photos:[],remark } | { id } |
| 82 | GET | /products/inventory-checks | ALL | 盘点列表 | ?storeId&startDate&endDate&page&size | { list:[{...}], total } |
| 83 | POST | /products/maintenance-checks | M | 提交养护检查 | { productId,category,checkDate,maintenanceResult,issueDescription,photoUrls:[] } | { id } |
| 84 | GET | /products/maintenance-checks | ALL | 养护列表 | ?storeId&productId&page&size | { list:[{...}], total } |
| 85 | POST | /products/sales-analyses | M | 提交动销分析 | { analysisWeek,storeId,hotProducts:[],normalProducts:[],slowProducts:[],noSales7Days:[],stockoutRiskProducts:[],analysisSummary,actionPlan } | { id } |
| 86 | GET | /products/sales-analyses | ALL | 动销分析列表 | ?storeId&analysisWeek&page&size | { list:[{...}], total } |
| 87 | POST | /products/new-product-plans | M | 提交新品推介方案 | { planMonth,storeId,newProductList:[],sellingPoints,targetCustomerGroup,displayPlan,trainingPlan,salesTarget,... } | { id } |
| 88 | GET | /products/new-product-plans | ALL | 新品方案列表 | ?storeId&planMonth&page&size | { list:[{...}], total } |
| 89 | POST | /products/promotion-plans | M | 提交促销筹备 | { activityMonth,storeId,activityName,activityTheme,activityPeriodStart,activityPeriodEnd,promotionRules,mainProducts:[],... } | { id } |
| 90 | GET | /products/promotion-plans | ALL | 促销列表 | ?storeId&activityMonth&page&size | { list:[{...}], total } |

### 6.12 场景运营 (scene)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 91 | POST | /scene/health-inspections | M | 提交卫生巡检 | { inspectionTime,inspectionDate,inspectorId,storeId,areaResults:{},issueDescription,photoUrls:[],rectificationRequired } | { id } |
| 92 | GET | /scene/health-inspections | ALL | 卫生巡检列表 | ?storeId&startDate&endDate&inspectionTime&page&size | { list:[{...}], total } |
| 93 | POST | /scene/display-inspections | M | 提交陈列检查 | { inspectionDate,storeId,displayArea,standardScore,issueDescription,beforePhotos:[],afterPhotos:[],rectificationPlan } | { id } |
| 94 | GET | /scene/display-inspections | ALL | 陈列检查列表 | ?storeId&startDate&endDate&displayArea&page&size | { list:[{...}], total } |
| 95 | POST | /scene/material-updates | M | 提交物料更新 | { storeId,checkDate,materialType,currentStatus,updatedPhotos:[],issueDescription,replacementRequired } | { id } |
| 96 | GET | /scene/material-updates | ALL | 物料更新列表 | ?storeId&startDate&endDate&materialType&page&size | { list:[{...}], total } |
| 97 | POST | /scene/equipment-checks | M | 提交设备检查 | { storeId,checkDate,equipmentType,status,issueDescription,repairRequired,photoUrls:[] } | { id } |
| 98 | GET | /scene/equipment-checks | ALL | 设备检查列表 | ?storeId&startDate&endDate&equipmentType&page&size | { list:[{...}], total } |
| 99 | POST | /scene/customer-experiences | M | 提交体验复盘 | { storeId,reviewWeek,feedbackCount,complaintCount,commonFeedback,improvementPlan,responsiblePersonId } | { id } |
| 100 | GET | /scene/customer-experiences | ALL | 体验复盘列表 | ?storeId&reviewWeek&page&size | { list:[{...}], total } |

### 6.13 业绩数据 (sales)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 101 | POST | /sales/records | M | 录入销售单 | { salesDate,storeId,totalAmount,paidAmount,customerType,customerGender,customerAgeRange,purchaseScene,customerConcern,salesPhotoUrls:[],items:[{productName,category,style,price,customerFavoritePoint,objection,closingReason,productPhotoUrls:[]}] } | { id } |
| 102 | GET | /sales/records | ALL | 销售列表 | ?storeId&employeeId&startDate&endDate&auditStatus&customerType&purchaseScene&page&size | { list:[{...}], total } |
| 103 | GET | /sales/records/my | M | 我的销售记录 | ?startDate&endDate&auditStatus&page&size | { list:[{...}], total } |
| 104 | GET | /sales/records/{id} | ALL | 销售详情(含明细) | - | { salesRecord:{...}, items:[{...}] } |
| 105 | PUT | /sales/records/{id} | M | 修改销售单(未审核前) | { totalAmount,paidAmount,customerConcern,items:[...] } | { } |
| 106 | POST | /sales/records/{id}/audit | M/P | 审核销售单 | { auditResult:APPROVED/REJECTED, auditComment } | { } |
| 107 | GET | /sales/stats/employee | ALL | 员工业绩统计 | ?employeeId&startDate&endDate | { totalSalesAmount,salesOrderCount,avgOrderAmount,avgItemPrice,bundleRate,newCustomerSales,oldCustomerRepurchase,categoryBreakdown:{},targetCompletionRate } |
| 108 | GET | /sales/stats/store | ALL | 门店业绩统计 | ?storeId&startDate&endDate | { totalSalesAmount,targetCompletionRate,employeeContributions:[],categoryBreakdown:{},highMarginRatio,newCustomerRatio,oldCustomerRatio } |
| 109 | GET | /sales/stats/employee-ranking | ALL | 员工销售排行 | ?storeId&startDate&endDate&sortBy&page&size | { list:[{employeeId,employeeName,totalSalesAmount,orderCount,avgOrderAmount,rank}] } |
| 110 | GET | /sales/stats/store-ranking | ALL | 门店销售排行 | ?regionId&startDate&endDate&sortBy | [{ storeId,storeName,totalSalesAmount,targetCompletionRate,rank }] |
| 111 | GET | /sales/stats/category-breakdown | ALL | 品类销售结构 | ?storeId&startDate&endDate | [{ category, salesAmount, salesCount, ratio }] |

### 6.14 AI智能辅助 (ai)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 112 | GET | /ai/advice/employee/{employeeId} | ALL | AI员工建议 | - | { summary,mainProblems,possibleReasons,coachingScript,trainingFocus,followUpPlan,assessmentAdvice } |
| 113 | GET | /ai/advice/product/{storeId} | ALL | AI货品建议 | - | { hotProductAnalysis,slowProductReasons,nextMonthRecommendations,bundleStrategies,clearanceSuggestions,transferSuggestions } |
| 114 | GET | /ai/advice/scene/{storeId} | ALL | AI场景建议 | - | { problemSummary,displayOptimizations,materialAdjustments,flowOptimizations,lightingSuggestions,rectificationTasks } |
| 115 | GET | /ai/advice/store/{storeId} | ALL | AI门店综合建议 | - | { humanAdvice:{...}, productAdvice:{...}, sceneAdvice:{...}, comprehensiveAdvice } |
| 116 | POST | /ai/score/task/{taskId} | M/P | AI任务评分 | - | { score, breakdown:{ onTime, contentCompleteness, photoQuality, formCompleteness, reasonability }, comment } |
| 117 | POST | /ai/advice/to-task/{adviceId} | ALL | AI建议一键转任务 | { assigneeId, dueTime, priority } | { taskId } |
| 118 | GET | /ai/prompt-templates | P | 提示词模板列表 | ?businessType&status&page&size | { list:[{...}], total } |
| 119 | POST | /ai/prompt-templates | P | 新增提示词模板 | { templateName,businessType,promptContent,inputSchema,outputSchema,modelName } | { id } |
| 120 | PUT | /ai/prompt-templates/{id} | P | 编辑提示词模板 | { templateName,promptContent,inputSchema,outputSchema,modelName,status } | { } |
| 121 | GET | /ai/results | P | AI调用历史 | ?businessType&relatedId&page&size | { list:[{...}], total } |

### 6.15 报表中心 (report)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 122 | GET | /reports/store-monthly-score | ALL | 门店月度评分 | ?storeId&scoreMonth | { storeId,storeName,scoreMonth,totalScore,humanScore,productScore,sceneScore,disciplineScore,overdueCount,ranking,detailJson } |
| 123 | GET | /reports/store-monthly-scores | P | 门店评分列表 | ?scoreMonth&regionId&page&size | { list:[{storeId,storeName,totalScore,ranking,...}], total } |
| 124 | GET | /reports/store-ranking | ALL | 门店排名(多维) | ?rankType(MANAGEMENT/SALES/TASK/HUMAN/PRODUCT/SCENE)&period&page&size | { list:[{storeId,storeName,score/value,ranking}] } |
| 125 | GET | /reports/task-completion | P | 任务完成率报表 | ?storeId&startDate&endDate&dimension | { totalTasks,completedTasks,completionRate,overdueRate,rejectedRate,byCategory:{},byStore:[] } |
| 126 | GET | /reports/human-summary | P | 人效汇总报表 | ?storeId&period | { meetingRate,interviewRate,assessmentRate,trainingRate,reviewRate,employeeLevelDistribution } |
| 127 | GET | /reports/product-summary | P | 货品汇总报表 | ?storeId&period | { inventoryCheckRate,maintenanceRate,analysisRate,salesTrend:[],slowProductCount } |
| 128 | GET | /reports/scene-summary | P | 场景汇总报表 | ?storeId&period | { healthInspectionRate,displayScoreAvg,materialUpdateRate,equipmentCheckRate,rectificationRate } |
| 129 | GET | /reports/dashboard/hq | P | 总部驾驶舱数据 | - | { storeTaskCompletionRate,taskCompletionTrend[],storeScoreDistribution[],overdueDistribution[],regionalComparison[],slowProductRanking[],aiRiskAlerts[] } |
| 130 | GET | /reports/dashboard/regional | M | 区域经理驾驶舱 | ?regionId | { storeRankings[],overdueStores[],salesCompletionRank[],managementScoreRank[],pendingAudits[] } |
| 131 | GET | /reports/dashboard/store | M | 店长驾驶舱 | ?storeId | { taskCompletionRate,pendingAuditCount,overdueTaskCount,dailySales,targetCompletionRate,employeeRanking[],abnormalAlerts[] } |
| 132 | GET | /reports/dashboard/associate | M | 导购驾驶舱 | - | { todayTasks:[],todaySales,monthSales,targetCompletionRate,pendingSalesEntry,rejectedTasks:[],aiAdvice } |
| 133 | GET | /reports/export/{reportType} | P | 导出Excel | ?storeId&startDate&endDate | (文件流) |

### 6.16 消息通知 (notification)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 134 | GET | /notifications | ALL | 通知列表 | ?notificationType&isRead&page&size | { list:[{notificationId,title,content,notificationType,isRead,businessType,businessId,createdAt}], total, unreadCount } |
| 135 | PUT | /notifications/{id}/read | ALL | 标记已读 | - | { } |
| 136 | PUT | /notifications/read-all | ALL | 全部已读 | { notificationType(可选) } | { } |
| 137 | GET | /notifications/unread-count | ALL | 未读数量 | - | { total, byType:{ TASK_REMIND:3, OVERDUE:1, ... } } |
| 138 | POST | /notifications/test | P | 测试通知发送(开发用) | { receiverId,title,content,notificationType,channel } | { } |

### 6.17 文件管理 (file)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 139 | POST | /files/upload | ALL | 上传文件 | multipart/form-data: file | { fileId,fileName,fileUrl,fileKey } |
| 140 | POST | /files/upload-batch | ALL | 批量上传 | multipart/form-data: files[] | [{ fileId,fileName,fileUrl }] |
| 141 | GET | /files/{id} | ALL | 获取文件信息 | - | { fileId,fileName,fileUrl,fileSize,mimeType } |
| 142 | DELETE | /files/{id} | P | 删除文件(逻辑) | - | { } |
| 143 | GET | /files/presigned-url | M | 获取预签名上传URL(移动端直传) | ?fileName&contentType | { uploadUrl, fileKey, expiresIn } |

### 6.18 操作日志 (log)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 144 | GET | /logs/operate | P | 操作日志 | ?operatorId&module&action&startDate&endDate&page&size | { list:[{operatorId,operatorName,module,action,targetType,targetId,requestIp,createdAt}], total } |

### 6.19 系统配置 (system)

| # | 方法 | 路径 | 端 | 说明 | 请求体/参数 | 返回结构 |
|---|------|------|------|------|-----------|---------|
| 145 | GET | /system/configs | P | 配置列表 | ?configGroup | [{ configKey,configValue,description }] |
| 146 | PUT | /system/configs | P | 批量更新配置 | { configs:[{configKey,configValue}] } | { } |

---

## 7. 表单引擎 + 文件上传 + 定时任务

### 7.1 通用表单引擎设计

```
设计思路:
  不硬编码每种表单，而是通过 JSON Schema 驱动动态表单渲染。

数据库表: form_schema (已建)
  id, form_name, form_json_schema(JSON), status, created_by, created_at, updated_at

JSON Schema 示例(晨会表单):
{
  "formId": "morning_meeting",
  "formName": "晨会记录",
  "version": 1,
  "fields": [
    {
      "fieldKey": "meeting_date",
      "fieldLabel": "会议日期",
      "fieldType": "date",
      "required": true,
      "placeholder": "请选择日期"
    },
    {
      "fieldKey": "store_target_amount",
      "fieldLabel": "当日门店销售目标",
      "fieldType": "number",
      "required": true,
      "min": 0,
      "unit": "元",
      "placeholder": "请输入当日销售目标"
    },
    {
      "fieldKey": "main_products",
      "fieldLabel": "今日主推款",
      "fieldType": "textarea",
      "required": true,
      "maxLength": 500,
      "placeholder": "请填写主推商品及理由"
    },
    {
      "fieldKey": "key_customers",
      "fieldLabel": "重点客户跟进",
      "fieldType": "textarea",
      "required": false,
      "maxLength": 500
    },
    {
      "fieldKey": "today_strategy",
      "fieldLabel": "当日活动话术",
      "fieldType": "textarea",
      "required": true,
      "maxLength": 500
    },
    {
      "fieldKey": "employee_targets",
      "fieldLabel": "导购个人目标",
      "fieldType": "employee_target_table",
      "required": true,
      "columns": [
        {"key": "employee_id", "label": "员工", "type": "user_select"},
        {"key": "individual_target", "label": "个人销售目标", "type": "number", "unit": "元"},
        {"key": "focus_category", "label": "主攻品类", "type": "select", "options": ["黄金","钻石","K金","古法","银饰","彩宝"]}
      ]
    },
    {
      "fieldKey": "meeting_photo_urls",
      "fieldLabel": "会议照片",
      "fieldType": "image",
      "required": true,
      "minCount": 1,
      "maxCount": 5,
      "accept": "image/*"
    },
    {
      "fieldKey": "remarks",
      "fieldLabel": "备注",
      "fieldType": "textarea",
      "required": false,
      "maxLength": 500
    }
  ]
}

支持字段类型:
  text | textarea | number | date | datetime | select | multi_select | 
  radio | checkbox | image | file | user_select | store_select |
  employee_target_table | product_table | score_table

前端渲染:
  - PC端: DynamicForm.vue 组件解析 JSON Schema, 动态渲染 Ant Design 表单组件
  - H5端: 同样解析 Schema, 使用 Vant UI 组件渲染
  - 组件根据 fieldType 切换内部渲染逻辑
  - 校验规则从 required/min/max/maxLength/minCount 自动生成

后端存储:
  - form_data 以 JSON 类型存入 task_submission.form_data
  - 数据库层面不做强校验, 校验逻辑在前端+后端DTO层双重执行
```

### 7.2 文件上传存储方案

```
架构:
  客户端 --> 后端 /files/upload (小文件 < 5MB)
         --> 后端 /files/presigned-url 获取预签名URL --> 直传 MinIO (大文件/移动端推荐)

存储选型: MinIO (自建, 兼容S3协议, 后续可平滑迁移到阿里云OSS/腾讯云COS)

上传流程:
  小文件(PC端):
    1. 前端用 ImageUploader.vue 组件选择图片
    2. 前端压缩图片(>1MB自动压缩至 1920px 宽 + JPEG 85%质量)
    3. 调用 POST /api/v1/files/upload multipart/form-data
    4. 后端校验: 类型(jpg/jpeg/png/webp/pdf/doc/docx/xls/xlsx), 大小(图≤5MB,文档≤20MB)
    5. 后端生成 file_key = "2026/01/09/uuid.ext" 上传到 MinIO
    6. 返回 { fileId, fileName, fileUrl, fileKey }

  大文件/移动端:
    1. 前端调 POST /api/v1/files/presigned-url?fileName=xxx&contentType=image/jpeg
    2. 后端返回 { uploadUrl(预签名PUT URL), fileKey, expiresIn(3600秒) }
    3. 前端直接 PUT 到 MinIO (绕过后端, 减少带宽压力)
    4. 上传完成后调 POST /api/v1/files/confirm { fileKey, fileName, businessType, businessId }
    5. 后端写入 file_resource 表, 返回 fileId

图片压缩规则:
  - 移动端拍照后自动压缩: 最大宽度 1920px, JPEG质量 85%
  - 原图保留: 调用 MinIO 图片处理(缩略图 / 水印)
  - 水印(可选): "门店名称 | 上传人 | 时间 | GPS"

文件存储目录结构(MinIO bucket: zhubao):
  zhubao/
  ├── images/
  │   ├── tasks/         # 任务执行照片
  │   ├── sales/         # 销售单据+货品图
  │   ├── scene/         # 卫生/陈列照片
  │   ├── human/         # 晨会/面谈照片
  │   ├── product/       # 货品照片
  │   └── avatars/       # 用户头像
  ├── documents/
  │   ├── reports/       # 导出报表
  │   └── attachments/   # 附件
  └── temp/              # 临时文件(24h自动清理)
```

### 7.3 定时任务触发逻辑

```
调度框架: XXL-JOB (推荐) 或 Spring @Scheduled (简单部署)

XXL-JOB 任务清单:

┌─────────────────────┬──────────────┬──────────────────────────────────┐
│ 任务名称             │ Cron         │ 执行逻辑                          │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ DailyTaskGenerate    │ 0 1 * * *    │ 每日凌晨1点生成当天任务:            │
│                      │              │ 1. 查询所有启用的每日模板           │
│                      │              │ 2. 根据 applicable_store_ids       │
│                      │              │    确定目标门店                    │
│                      │              │ 3. 根据模板的 assignee 规则         │
│                      │              │    确定执行人(店长/指定员工)        │
│                      │              │ 4. 生成 task_instance              │
│                      │              │ 5. 发送通知给执行人                │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ WeeklyTaskGenerate   │ 0 2 * * 1    │ 每周一凌晨2点生成当周任务           │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ MonthlyTaskGenerate  │ 0 3 * * *    │ 每天凌晨3点检查: 是否为每月         │
│                      │              │ 指定日期(15/20/25日)             │
│                      │              │ 是则生成月度任务                   │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ TaskOverdueCheck     │ */5 * * * *  │ 每5分钟检查:                      │
│                      │              │ 1. 扫描 status IN (PENDING,        │
│                      │              │    READY, IN_PROGRESS)            │
│                      │              │    AND due_time < NOW()           │
│                      │              │ 2. 更新 status=OVERDUE             │
│                      │              │ 3. 更新 is_overdue=1               │
│                      │              │ 4. 计算 overdue_minutes            │
│                      │              │ 5. 通知执行人+店长                 │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ TaskReminderSend     │ */10 * * * * │ 每10分钟检查reminder_rule:         │
│                      │              │ - 任务开始提醒(generate后立即)      │
│                      │              │ - 截止前2h提醒                    │
│                      │              │ - 截止前30min提醒(短信)            │
│                      │              │ - 超时后提醒执行人+上级             │
│                      │              │ - 超时24h提醒区域经理/总部运营      │
│                      │              │ 已发送的记录在 task_reminder_log   │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ StoreMonthlyScoreCalc│ 0 5 1 * *    │ 每月1日凌晨5点:                    │
│                      │              │ 1. 计算上月各门店评分              │
│                      │              │ 2. 写入 store_monthly_score        │
│                      │              │ 3. 生成排名                       │
│                      │              │ 4. 通知区域经理/总部               │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ ReportSnapshotGen    │ 0 6 1 * *    │ 每月1日凌晨6点:                    │
│                      │              │ 1. 生成各类月度报表快照            │
│                      │              │ 2. 写入 report_snapshot            │
│                      │              │ 3. 存储JSON快照供后续对比          │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ AIAnalysisTrigger   │ 0 7 * * *    │ 每天凌晨7点:                       │
│                      │              │ 1. 触发AI员工画像分析              │
│                      │              │ 2. 触发AI货品运营分析              │
│                      │              │ 3. 触发AI场景问题分析              │
│                      │              │ 4. 触发AI异常检测                  │
│                      │              │    (业绩下滑/连续滞销/任务低完成率) │
│                      │              │ 5. 结果写入 ai_result 表           │
│                      │              │ 6. 重要发现生成通知                │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ TempFileCleanup      │ 0 3 * * *    │ 每天凌晨3点清理 /temp/ 下超24h文件  │
├─────────────────────┼──────────────┼──────────────────────────────────┤
│ HolidayTaskGenerate  │ 0 4 * * *    │ 每天凌晨4点检查: 是否距指定节日     │
│                      │              │ 还有N天, 触发节日专项任务          │
│                      │              │ (情人节前14天, 七夕前14天,         │
│                      │              │  春节前30天, 婚庆季前30天等)       │
└─────────────────────┴──────────────┴──────────────────────────────────┘

异常触发任务(非定时, 实时触发):
  触发条件(在业务Service中实时检测):
    1. 员工连续两周目标完成率 < 70% -> 生成"重点辅导"任务
    2. 单品连续7天无动销 -> 生成"滞销品处理"建议
    3. 门店任务连续3次被驳回 -> 生成"整改督导"任务
    4. 陈列检查不合格 -> 生成"整改复核"任务
    5. 库存低于安全库存 -> 生成"紧急补货"提醒
```
## 8. 模块依赖关系 + 中间件部署方案 + MVP优先级

### 8.1 模块依赖关系图

```
                     +--------------+
                     | M1 组织权限   |  <-- 所有模块的基石
                     +-------+------+
                             |
         +-------------------+-------------------+
         |                   |                   |
   +-----v------+     +-----v------+     +------v-----+
   | M9 消息通知 |     | M2 任务中心 |     | M7 文件管理 |
   +------------+     +-----+------+     +------------+
                             |
       +------+------+------+------+------+------+
       |      |      |      |      |      |      |
  +----v-+ +-v---+ +v---+ +v---+ +v---+ +v---+ +-v----+
  | M3   | | M4  | | M5 | | M6 | | M7 | | M8 | | M9   |
  | 人效 | | 货品| | 场景| | 业绩| | AI | | 报表| | 通知 |
  +------+ +-----+ +----+ +----+ +----+ +----+ +------+

依赖关系说明:
  M1 组织权限: 零依赖，被所有模块依赖
  M2 任务中心: 依赖 M1(权限) + M7(文件上传)
  M3 人效管理: 依赖 M1 + M2(任务驱动) + M7(拍照) + M9(通知)
  M4 货品管理: 依赖 M1 + M2(任务驱动) + M7(拍照) + M9(通知)
  M5 场景运营: 依赖 M1 + M2(任务驱动) + M7(拍照) + M9(通知)
  M6 业绩数据: 依赖 M1 + M7 + M9
  M7 AI辅助:   依赖 M1 + M3/M4/M5/M6(分析数据源)
  M8 报表中心: 依赖 M1 + M2/M3/M4/M5/M6(数据源)
  M9 消息通知: 依赖 M1(用户), 被 M2/M3/M4/M5/M6/M7 调用
```

### 8.2 第三方中间件部署方案

```
部署架构(单机MVP -> 后续扩展集群):

                   +-------------------+
                   |    Nginx :80/443  |
                   +--------+----------+
                            |
          +-----------------+------------------+
          |                                    |
    +-----v-------+                      +-----v-------+
    | 前端静态资源  |                      | SpringBoot  |
    | PC: /admin   |                      | :8080       |
    | H5: /h5      |                      | jar 部署     |
    +-------------+                      +------+------+
                                               |
           +-------------------+---------------+---------------+
           |                   |               |               |
     +-----v-----+     +------v------+  +-----v-----+  +------v------+
     | MySQL 8.0  |     | Redis 7.x   |  | MinIO     |  | XXL-JOB    |
     | :3306      |     | :6379       |  | :9000     |  | :9080      |
     | (主库)      |     | 缓存/Session|  | API/Console|  | 调度中心    |
     +------------+     +------------+  +-----------+  +-------------+

中间件版本与用途:
┌──────────┬──────────┬──────────────────────────────────────────┐
│ 中间件    │ 版本      │ 用途                                     │
├──────────┼──────────┼──────────────────────────────────────────┤
│ MySQL    │ 8.0.33+  │ 主数据库, InnoDB引擎, 字符集utf8mb4       │
│ Redis    │ 7.0+     │ JWT黑名单, 验证码, 接口限流, 统计数据缓存   │
│ MinIO    │ latest   │ 图片/附件对象存储, S3兼容                  │
│ XXL-JOB  │ 2.4.0+   │ 分布式定时任务调度(即使单机部署也推荐)      │
│ Nginx    │ 1.24+    │ 反向代理, 动静分离, SSL终结, Gzip压缩       │
└──────────┴──────────┴──────────────────────────────────────────┘

外部服务:
┌──────────────────┬──────────────────────────────────────────┐
│ 服务              │ 用途                                     │
├──────────────────┼──────────────────────────────────────────┤
│ OpenAI/文心/通义  │ AI大模型API (MVP先用一个)                 │
│ 企业微信API       │ 消息推送(可选, MVP先做站内信)             │
│ 短信API           │ 紧急提醒(可选, MVP预留接口)               │
└──────────────────┴──────────────────────────────────────────┘

推荐部署顺序:
  1. MySQL 8.0 + Redis 7.x  (基础数据层)
  2. MinIO                    (文件存储, 可与第1步并行)
  3. XXL-JOB                  (调度中心)
  4. SpringBoot 应用           (后端服务)
  5. Nginx                    (前端代理 + 后端反向代理)
```

### 8.3 MVP开发优先级 — 20个核心功能排序

```
优先级排序原则:
  P0(1-7):   任务闭环核心链路, 没有它系统不成立
  P1(8-14):  人货场核心业务表单, 打通完整业务流
  P2(15-17): 考核报表, 让管理有数据抓手
  P3(18-20): AI辅助和增强功能, 锦上添花

┌──────┬─────────────────────────┬──────────┬──────────────────────┐
│ 优先级│ 功能                     │ 对应模块  │ 理由                  │
├──────┼─────────────────────────┼──────────┼──────────────────────┤
│  1   │ 登录与角色权限           │ M1       │ 所有功能入口, 最先做    │
│  2   │ 组织/门店/员工管理       │ M1       │ 基础数据, 第1步依赖    │
│  3   │ 文件上传(图片/附件)      │ M7(文件)  │ 任务执行拍照必需       │
│  4   │ 动作库管理               │ M2       │ 任务模板的数据源       │
│  5   │ 任务模板配置             │ M2       │ 定义任务规则           │
│  6   │ 周期任务自动生成         │ M2       │ 系统核心"主动式"引擎    │
│  7   │ 我的任务(列表+执行+提交) │ M2       │ 导购/店长核心操作       │
│  8   │ 任务审核+驳回+整改       │ M2       │ 闭环关键环节           │
│  9   │ 超时任务预警             │ M2/M9    │ 推动执行的约束力        │
│ 10   │ 晨会/夕会表单            │ M3       │ 人效每日固定动作        │
│ 11   │ 员工面谈表单             │ M3       │ 人效每周核心动作        │
│ 12   │ 卫生巡检表单             │ M5       │ 场景每日固定动作        │
│ 13   │ 陈列检查表单             │ M5       │ 场景每周核心动作        │
│ 14   │ 货品盘点表单             │ M4       │ 货品每日固定动作        │
│ 15   │ 导购销售录入             │ M6       │ 业绩数据源             │
│ 16   │ 店长销售审核             │ M6       │ 业绩数据校验           │
│ 17   │ 任务完成率报表           │ M8       │ 衡量执行效果           │
│ 18   │ 门店月度评分+排名        │ M8       │ 考核驱动               │
│ 19   │ AI管理建议               │ M7       │ 管理辅助差异化价值      │
│ 20   │ 消息通知完善(企业微信等) │ M9       │ 提升触达效率           │
└──────┴─────────────────────────┴──────────┴──────────────────────┘

MVP第一阶段必做 (P0: 1-7):  打通 登录 -> 动作库 -> 任务模板 -> 周期生成 -> 执行提交
MVP第二阶段必做 (P1: 8-16): 打通 审核 -> 人货场表单 -> 业绩录入审核
MVP第三阶段必做 (P2: 17-18): 报表 -> 评分 -> 排名
MVP第四阶段 (P3: 19-20):      AI辅助 -> 多渠道通知
```

---

## 9. 前后端开发约束

### 9.1 前端开发职责与边界

```
前端职责(仅做这些):
  1. 页面渲染与交互
     - Vue3 + Ant Design (PC) / Vant UI (H5) 实现所有页面
     - 表单组件渲染(包括 DynamicForm 动态表单)
     - 列表分页、搜索筛选、排序
     - 图表展示(ECharts, 使用 useChart 封装)
     - 状态标签、时间线、卡片等 UI 组件

  2. 文件上传与展示
     - 图片选择(相机/相册/拖拽)
     - 图片压缩(>1MB 自动压到 1920px)
     - 图片预览、旋转、删除
     - 调用后端接口获取上传URL或直传

  3. 图表可视化
     - 数据驾驶舱 ECharts 图表
     - 报表图表(趋势图/饼图/柱状图/雷达图)
     - 排名列表/评分仪表盘
     - 图表数据从后端API获取, 前端仅做展示

  4. Token管理与路由守卫
     - localStorage 存储Token
     - Axios 拦截器自动注入 Authorization header
     - 路由守卫检查登录状态
     - 按钮/菜单级权限控制(usePermission)

前端禁止行为:
  - 不做复杂数据计算/统计聚合 (由后端SQL/Service完成)
  - 不做数据库直接访问
  - 不做定时任务调度
  - 不做AI模型调用
  - 不做数据校验为最终校验 (前端校验只是UX优化, 以后端校验为准)
  - 不存储敏感数据到 localStorage (密码/成本价等)
  - 不在前端做权限判断的逻辑短路 (后端必须再做一次校验)
```

### 9.2 后端开发职责与边界

```
后端职责(完整负责):
  1. 数据校验与安全
     - 所有API入口参数校验 (DTO + @Valid)
     - 权限校验: 每个API必须验证用户角色和操作权限
     - 数据权限: 通过 @DataScope 注解自动过滤
     - 敏感字段: cost_price/gross_margin_rate 按角色过滤
     - SQL注入防护 (MyBatis #{} 参数化)
     - XSS 防护
     - 文件上传类型/大小校验(后端最终防线)

  2. RBAC权限控制
     - Spring Security + JWT 认证
     - AuthInterceptor 拦截所有 /api/** 请求
     - @PreAuthorize 或自定义注解控制接口权限
     - MyBatis DataScopePlugin 注入数据范围SQL

  3. 数据库操作
     - MyBatis-Plus 作为ORM
     - 所有CRUD操作
     - 复杂统计查询 (GROUP BY / 聚合函数 / 子查询)
     - 事务管理 (@Transactional, 多表操作)
     - 逻辑删除 (is_deleted=1)
     - 乐观锁 (version字段, 防止并发冲突)

  4. 定时任务
     - XXL-JOB 调度中心注册任务
     - 任务生成、超时检查、提醒发送、报表生成、AI触发
     - 任务执行日志记录

  5. AI调用
     - AIGatewayService 封装大模型API调用
     - DataAssembler 组装业务数据为Prompt
     - ResultParser 解析模型返回为结构化数据
     - 异步执行(@Async), 不阻塞用户请求
     - Token消耗记录、失败重试

  6. 报表计算
     - SQL聚合查询
     - 评分计算引擎 (门店评分/员工分层)
     - Excel 导出 (Apache POI / EasyExcel)

  7. 文件管理
     - MinIO 文件上传/下载
     - 预签名URL生成
     - 文件类型/大小校验
     - 临时文件清理

后端禁止行为:
  - 不做HTML页面渲染 (前后端分离, 后端只返回JSON)
  - 不在后端拼接前端UI逻辑
  - 不存储大文件到数据库(只存file_resource元数据, 文件存MinIO)
```

### 9.3 前后端协作规约

```
1. API规范:
   - RESTful 风格
   - 统一前缀 /api/v1
   - 统一响应体 ApiResult { code, msg, data }
   - 分页响应 PageResult { list, total, page, size }
   - 错误码枚举 ErrorCode 前后端共享

2. 认证流程:
   - 登录 -> 后端返回 JWT -> 前端存入 localStorage
   - 每次请求 -> Axios拦截器自动注入 Authorization: Bearer <token>
   - Token过期 -> 后端返回 401 -> 前端自动跳转登录页

3. 文件上传流程:
   - 小文件: 前端 -> 后端 /files/upload -> MinIO
   - 大文件/移动端: 前端获取 presigned-url -> 直传 MinIO -> 回调后端确认

4. 权限协作:
   - 后端返回用户权限列表 (GET /auth/me 时)
   - 前端根据权限隐藏菜单/按钮 (usePermission)
   - 但后端必须在每个API入口再做权限校验 (防止前端绕过)

5. 数据校验协作:
   - 前端做即时校验(格式/必填/长度) -> 提升UX
   - 后端做最终校验(格式/业务规则/权限) -> 安全保障
   - 校验失败返回 400 + 具体错误信息

6. 类型共享:
   - 枚举值定义: 后端 enums/ 目录 -> 前端 types/ 目录手动同步
   - 后续推荐: 使用 OpenAPI/Swagger 生成前端类型定义
```

---

## 附录：文档索引

| 章节 | 内容 | 目标读者 |
|------|------|---------|
| 第1节 | 分层架构图 | 全体 |
| 第2节 | 完整目录树 | 前端 + 后端 |
| 第3节 | RBAC权限+数据隔离 | 后端 + 测试 |
| 第4节 | 全量数据库表设计(50+张) | 后端 + DBA |
| 第5节 | 核心模块拆分(9大模块) | 后端 |
| 第6节 | RESTful API清单(146个接口) | 前端 + 后端 + 测试 |
| 第7节 | 表单引擎+文件上传+定时任务 | 前端 + 后端 |
| 第8节 | 模块依赖+中间件+MVP优先级 | 全体 + DevOps |
| 第9节 | 前后端开发约束 | 前端 + 后端 |
