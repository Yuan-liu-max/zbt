# API 类型安全改造实施计划

> **For agentic workers:** 按任务列表逐项改造 `src/api/*.ts`，确保类型一致并可编译。

**Goal:** 将 `src/api/` 下所有真实 API 文件中的 `Promise<any>` 替换为来自 `src/types/` 的具体类型，并导入 `PageResult<T>`。

**Architecture:** 逐个 API 文件分析其操作的实体，匹配到 `src/types/xxx.ts` 中对应的接口；列表/分页接口返回 `Promise<PageResult<T>>`，详情/统计返回 `Promise<T>`，写操作根据后端语义返回 `Promise<T>` 或 `Promise<void>`。

**Tech Stack:** Vue 3 + TypeScript + Axios（响应拦截器已解包 `res.data`）

---

### Task 1: 建立类型映射

**Files:**
- Read: `src/types/common.ts`
- Read: `src/api/*.ts`
- Read: `src/types/*.ts`

- [x] **Step 1: 读取 `PageResult<T>` 定义**
- [x] **Step 2: 遍历 API 文件并记录方法名、URL、当前返回类型**
- [x] **Step 3: 遍历类型文件并建立实体映射表**

---

### Task 2: 改造 API 文件

**Files:** Modify `src/api/*.ts` (excluding `src/api/mock/`)

- [ ] **Step 1: `report.ts`**
  - `getDashboard`: `Promise<ReportStats>`
  - `getScores`: `Promise<FinanceStats>`
  - `getRanking`: `Promise<PageResult<RankingItem>>`
  - `getTaskCompletion`: `Promise<PageResult<RankingItem>>` (任务完成度结构与排行榜项复用)

- [ ] **Step 2: `finance.ts`**
  - `getList`: `Promise<PageResult<TransactionRecord>>`
  - `getStats`: `Promise<FinanceStats>`
  - `getDetail`: `Promise<TransactionRecord>`
  - `create/update`: `Promise<TransactionRecord>`
  - `delete`: `Promise<void>`

- [ ] **Step 3: `task.ts`**
  - `taskApi.getList`: `Promise<PageResult<TaskItem>>`
  - `taskApi.getMyTasks`: `Promise<PageResult<TaskItem>>`
  - `taskApi.getMyAudit`: `Promise<PageResult<TaskReviewItem>>`
  - `taskApi.getDetail`: `Promise<TaskItem>`
  - `taskApi.generate/create/update`: `Promise<TaskItem>`
  - `taskApi.submit`: `Promise<TaskSubmission>`
  - `taskApi.audit`: `Promise<TaskAudit>`
  - `taskApi.cancel/voidTask/start`: `Promise<void>`
  - `templateApi.getList`: `Promise<PageResult<TaskTemplate>>`
  - `templateApi.create/update`: `Promise<TaskTemplate>`
  - `templateApi.delete/toggle`: `Promise<void>`
  - `reviewApi.getList`: `Promise<PageResult<TaskReviewItem>>`

- [ ] **Step 4: `customer.ts`**
  - `customerApi.getList`: `Promise<PageResult<CustomerItem>>`
  - `customerApi.getDetail/create/update`: `Promise<CustomerItem>`
  - `customerApi.delete`: `Promise<void>`
  - `memberApi.getList`: `Promise<PageResult<MemberLevel>>`
  - `memberApi.getStats`: `Promise<MemberStats>`
  - `memberApi.create/update`: `Promise<MemberLevel>`

- [ ] **Step 5: `marketing.ts`**
  - `activityApi.getList`: `Promise<PageResult<ActivityItem>>`
  - `activityApi.create/update`: `Promise<ActivityItem>`
  - `activityApi.delete`: `Promise<void>`
  - `promotionApi.getList`: `Promise<PageResult<PromotionItem>>`
  - `promotionApi.create/update`: `Promise<PromotionItem>`
  - `promotionApi.delete`: `Promise<void>`

- [ ] **Step 6: `certificate.ts`**
  - `getList`: `Promise<PageResult<CertificateItem>>`
  - `getDetail/create/update`: `Promise<CertificateItem>`
  - `delete`: `Promise<void>`

- [ ] **Step 7: `supplier.ts`**
  - `getList`: `Promise<PageResult<SupplierItem>>`
  - `getDetail/create/update`: `Promise<SupplierItem>`
  - `delete`: `Promise<void>`

- [ ] **Step 8: `scene.ts`**
  - `sceneApi.getList`: `Promise<PageResult<InspectionItem>>`
  - `displayApi.getList`: `Promise<PageResult<InspectionItem>>`
  - `materialApi.getList`: `Promise<PageResult<MaterialItem>>`
  - `deviceApi.getList`: `Promise<PageResult<DeviceItem>>`
  - 所有 create/update: 对应实体；delete: `Promise<void>`

- [ ] **Step 9: `log.ts`**
  - `getList`: `Promise<PageResult<LogItem>>`

- [ ] **Step 10: `system.ts`**
  - `orgApi.getTree`: `Promise<OrgNode[]>`
  - `orgApi.getList`: `Promise<PageResult<OrgNode>>`
  - `orgApi.create/update`: `Promise<OrgNode>`
  - `orgApi.delete`: `Promise<void>`
  - `userApi.getList`: `Promise<PageResult<UserItem>>`
  - `userApi.create/update`: `Promise<UserItem>`
  - `userApi.delete`: `Promise<void>`
  - `roleApi.getList`: `Promise<PageResult<RoleItem>>`
  - `roleApi.create/update/assignPermissions`: `Promise<RoleItem>`
  - `roleApi.delete`: `Promise<void>`
  - `roleApi.getPermissions`: `Promise<PermissionItem[]>`
  - `configApi.getList`: `Promise<SystemConfig>`
  - `configApi.save`: `Promise<SystemConfig>`

- [ ] **Step 11: `ai.ts`**
  - `getTools`: `Promise<PageResult<AiTool>>`
  - `getAdvice`: `Promise<AiSuggestion>`
  - `getResults`: `Promise<PageResult<AnalysisReport>>`
  - `getPromptTemplates`: `Promise<PageResult<DocTemplate>>`

- [ ] **Step 12: `goods.ts`**
  - `storeApi.getAll`: `Promise<StoreItem[]>`
  - `goodsApi.getList`: `Promise<PageResult<GoodsItem>>`
  - `goodsApi.getById/create/update`: `Promise<GoodsItem>`
  - `goodsApi.delete`: `Promise<void>`
  - `categoryApi.getTree`: `Promise<GoodsCategory[]>`
  - `categoryApi.getList`: `Promise<PageResult<GoodsCategory>>`
  - `categoryApi.create/update`: `Promise<GoodsCategory>`
  - `categoryApi.delete`: `Promise<void>`
  - `brandApi.getList`: `Promise<PageResult<BrandItem>>`
  - `brandApi.getAll`: `Promise<BrandItem[]>`
  - `brandApi.create/update`: `Promise<BrandItem>`
  - `brandApi.delete`: `Promise<void>`
  - `inventoryCheckApi`: `Promise<PageResult<InventoryCheckRecord>>` / create/update/detail / delete void
  - `inventoryWarningApi.getList`: `Promise<PageResult<InventoryWarningItem>>`
  - `inventoryWarningApi.getStats`: `Promise<InventoryStats>`
  - `inventoryWarningApi.handleAlert`: `Promise<void>`

- [ ] **Step 13: `order.ts`**
  - `orderApi.getList`: `Promise<PageResult<OrderRecord>>`
  - `orderApi.getDetail/update`: `Promise<OrderRecord>`
  - `orderApi.cancel`: `Promise<void>`
  - `returnApi.getList`: `Promise<PageResult<ReturnRecord>>`
  - `returnApi.getDetail`: `Promise<ReturnRecord>`
  - `returnApi.cancel`: `Promise<void>`

- [ ] **Step 14: `human.ts`**
  - `meetingApi.getList`: `Promise<PageResult<MeetingItem>>`
  - `meetingApi.create`: `Promise<MeetingItem>`
  - `interviewApi.getList`: `Promise<PageResult<InterviewItem>>`
  - `interviewApi.create`: `Promise<InterviewItem>`
  - `assessApi.getList`: `Promise<PageResult<AssessItem>>`
  - `assessApi.create/update`: `Promise<AssessItem>`
  - `performanceApi.getList`: `Promise<PageResult<PerformanceItem>>`
  - `performanceApi.create`: `Promise<PerformanceItem>`

- [ ] **Step 15: `purchase.ts`**
  - `purchaseApi.getList`: `Promise<PageResult<PurchaseRecord>>`
  - `purchaseApi.getById`: `Promise<PurchaseRecord>`
  - `purchaseApi.create/update`: `Promise<PurchaseRecord>`
  - `purchaseApi.approve/reject/cancel`: `Promise<void>`
  - `purchaseItemApi.getList`: `Promise<PageResult<PurchaseItem>>`
  - `purchaseItemApi.create`: `Promise<PurchaseItem>`
  - `purchaseItemApi.delete`: `Promise<void>`

- [ ] **Step 16: `notification.ts`**
  - `getUnreadCount/getMessageUnreadCount`: `Promise<number>`
  - `getList`: `Promise<PageResult<NotificationItem>>`
  - `markAsRead/markAllRead`: `Promise<void>`

- [ ] **Step 17: `sales.ts`**
  - `getList`: `Promise<PageResult<SalesRecord>>`
  - `getDetail/create`: `Promise<SalesRecord>`
  - `getItems`: `Promise<SalesItem[]>`
  - `getStats`: `Promise<SalesStats>`
  - `audit`: `Promise<void>`
  - `employeeMetrics/storeMetrics`: `Promise<unknown>` (结构未在 types/sales.ts 中定义，保留 any/unknown)
  - `employeeRanking`: `Promise<PageResult<EmployeeRanking>>`
  - `storeRanking/categoryStructure`: `Promise<unknown>`
  - 别名方法同步类型

- [ ] **Step 18: `profile.ts`**
  - `getProfile/getUserInfo`: `Promise<UserInfo>`
  - `getStats`: `Promise<UserStats>`
  - `updateProfile/changePassword`: `Promise<void>`

---

### Task 3: 类型检查

**Files:** All modified `src/api/*.ts`

- [ ] **Step 1: 运行 `npx vue-tsc -b --noEmit`**
- [ ] **Step 2: 修复所有类型错误**

---

### Task 4: 汇总报告

- [ ] **Step 1: 列出修改文件、方法数量、返回类型**
- [ ] **Step 2: 标注无法确定类型的方法**
