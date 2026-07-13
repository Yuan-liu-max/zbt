import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

/* ---------- Layout ---------- */
import DefaultLayout from '@/layouts/DefaultLayout.vue'

/* ---------- 懒加载页面 ---------- */
const lazy = (path: string) => {
  return () => import(/* @vite-ignore */ `/src/views/${path}.vue`)
}

/* ============================
   路由配置 — 珠宝通管理系统
   ============================ */

export const menuRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: DefaultLayout,
    redirect: '/dashboard',
    children: [
      /* ---- 首页 ---- */
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: lazy('dashboard/DashboardView'),
        meta: { title: '首页', icon: 'HomeOutlined', affix: true },
      },
      {
        path: 'dashboard/settings',
        name: 'DashboardSettings',
        component: lazy('dashboard/DashboardSettings'),
        meta: { title: '自定义设置', hidden: true },
      },

      /* ---- 商品管理 ---- */
      {
        path: 'goods',
        name: 'Goods',
        redirect: '/goods/list',
        meta: { title: '商品管理', icon: 'ShoppingOutlined' },
        children: [
          { path: 'list', name: 'GoodsList', component: lazy('goods/GoodsList'), meta: { title: '商品列表' } },
          { path: 'category', name: 'GoodsCategory', component: lazy('goods/GoodsCategory'), meta: { title: '商品分类' } },
          { path: 'brand', name: 'GoodsBrand', component: lazy('goods/GoodsBrand'), meta: { title: '品牌管理' } },
        ],
      },

      /* ---- 库存管理 ---- */
      {
        path: 'inventory',
        name: 'Inventory',
        redirect: '/inventory/list',
        meta: { title: '库存管理', icon: 'DatabaseOutlined' },
        children: [
          { path: 'list', name: 'InventoryList', component: lazy('inventory/InventoryList'), meta: { title: '库存列表' } },
          { path: 'check', name: 'InventoryCheck', component: lazy('inventory/InventoryCheck'), meta: { title: '盘点管理' } },
          { path: 'warning', name: 'InventoryWarning', component: lazy('inventory/InventoryWarning'), meta: { title: '库存预警' } },
        ],
      },

      /* ---- 订单管理 ---- */
      {
        path: 'order',
        name: 'Order',
        redirect: '/order/list',
        meta: { title: '订单管理', icon: 'FileTextOutlined' },
        children: [
          { path: 'list', name: 'OrderList', component: lazy('order/OrderList'), meta: { title: '订单列表' } },
          { path: 'detail/:id', name: 'OrderDetail', component: lazy('order/OrderDetail'), meta: { title: '订单详情', hidden: true } },
          { path: 'return', name: 'OrderReturn', component: lazy('order/OrderReturn'), meta: { title: '退换货' } },
        ],
      },

      /* ---- 客户管理 ---- */
      {
        path: 'customer',
        name: 'Customer',
        redirect: '/customer/list',
        meta: { title: '客户管理', icon: 'TeamOutlined' },
        children: [
          { path: 'list', name: 'CustomerList', component: lazy('customer/CustomerList'), meta: { title: '客户列表' } },
          { path: 'member', name: 'CustomerMember', component: lazy('customer/CustomerMember'), meta: { title: '会员管理' } },
        ],
      },

      /* ---- 供应商管理 ---- */
      {
        path: 'supplier',
        name: 'Supplier',
        component: lazy('supplier/SupplierList'),
        meta: { title: '供应商管理', icon: 'ShopOutlined' },
      },

      /* ---- 证书管理 ---- */
      {
        path: 'certificate',
        name: 'Certificate',
        component: lazy('certificate/CertificateList'),
        meta: { title: '证书管理', icon: 'SafetyCertificateOutlined' },
      },

      /* ---- 采购管理 ---- */
      {
        path: 'purchase',
        name: 'Purchase',
        redirect: '/purchase/list',
        meta: { title: '采购管理', icon: 'ImportOutlined' },
        children: [
          { path: 'list', name: 'PurchaseList', component: lazy('purchase/PurchaseList'), meta: { title: '采购列表' } },
          { path: 'apply', name: 'PurchaseApply', component: lazy('purchase/PurchaseApply'), meta: { title: '采购申请' } },
        ],
      },

      /* ---- 销售管理 ---- */
      {
        path: 'sales',
        name: 'Sales',
        redirect: '/sales/entry',
        meta: { title: '销售管理', icon: 'RiseOutlined' },
        children: [
          { path: 'entry', name: 'SalesEntry', component: lazy('sales/SalesEntry'), meta: { title: '销售录入' } },
          { path: 'report', name: 'SalesReport', component: lazy('sales/SalesReport'), meta: { title: '销售报表' } },
        ],
      },

      /* ---- 财务管理 ---- */
      {
        path: 'finance',
        name: 'Finance',
        component: lazy('finance/FinanceView'),
        meta: { title: '财务管理', icon: 'AccountBookOutlined' },
      },

      /* ---- 数据报表 ---- */
      {
        path: 'report',
        name: 'ReportCenter',
        component: lazy('report/ReportCenter'),
        meta: { title: '数据报表', icon: 'BarChartOutlined' },
      },

      /* ---- 营销管理 ---- */
      {
        path: 'marketing',
        name: 'Marketing',
        redirect: '/marketing/activity',
        meta: { title: '营销管理', icon: 'PercentageOutlined' },
        children: [
          { path: 'activity', name: 'MarketingActivity', component: lazy('marketing/MarketingActivity'), meta: { title: '营销活动' } },
          { path: 'promotion', name: 'MarketingPromotion', component: lazy('marketing/MarketingPromotion'), meta: { title: '促销管理' } },
        ],
      },

      /* ---- 任务中心（主动式周期任务） ---- */
      {
        path: 'task',
        name: 'TaskCenter',
        redirect: '/task/list',
        meta: { title: '任务中心', icon: 'ScheduleOutlined', badge: 18 },
        children: [
          { path: 'list', name: 'TaskList', component: lazy('task/TaskList'), meta: { title: '任务列表' } },
          { path: 'create', name: 'TaskCreate', component: lazy('task/TaskCreate'), meta: { title: '创建任务' } },
          { path: 'template', name: 'TaskTemplate', component: lazy('task/TaskTemplate'), meta: { title: '任务模板' } },
          { path: 'review', name: 'TaskReview', component: lazy('task/TaskReview'), meta: { title: '任务审核' } },
        ],
      },

      /* ---- 人效管理 ---- */
      {
        path: 'human',
        name: 'HumanEffect',
        redirect: '/human/meeting',
        meta: { title: '人效管理', icon: 'UserSwitchOutlined' },
        children: [
          { path: 'meeting', name: 'HumanMeeting', component: lazy('human/HumanMeeting'), meta: { title: '晨夕会' } },
          { path: 'interview', name: 'HumanInterview', component: lazy('human/HumanInterview'), meta: { title: '员工面谈' } },
          { path: 'assess', name: 'HumanAssess', component: lazy('human/HumanAssess'), meta: { title: '能力考核' } },
          { path: 'performance', name: 'HumanPerformance', component: lazy('human/HumanPerformance'), meta: { title: '绩效复盘' } },
        ],
      },

      /* ---- 场景运营 ---- */
      {
        path: 'scenario',
        name: 'ScenarioOps',
        redirect: '/scenario/health',
        meta: { title: '场景运营', icon: 'EnvironmentOutlined' },
        children: [
          { path: 'health', name: 'ScenarioHealth', component: lazy('scenario/ScenarioHealth'), meta: { title: '卫生巡检' } },
          { path: 'display', name: 'ScenarioDisplay', component: lazy('scenario/ScenarioDisplay'), meta: { title: '陈列检查' } },
          { path: 'material', name: 'ScenarioMaterial', component: lazy('scenario/ScenarioMaterial'), meta: { title: '物料更新' } },
          { path: 'device', name: 'ScenarioDevice', component: lazy('scenario/ScenarioDevice'), meta: { title: '设备检查' } },
        ],
      },

      /* ---- AI 智能辅助 ---- */
      {
        path: 'ai',
        name: 'AICenter',
        component: lazy('ai/AICenter'),
        meta: { title: 'AI智能辅助', icon: 'RobotOutlined' },
      },
      {
        path: 'ai/chat',
        name: 'AiChat',
        component: lazy('ai/AiChat'),
        meta: { title: '智能问答', hidden: true },
      },
      {
        path: 'ai/doc',
        name: 'AiDoc',
        component: lazy('ai/AiDoc'),
        meta: { title: '文档生成', hidden: true },
      },
      {
        path: 'ai/suggest',
        name: 'AiSuggest',
        component: lazy('ai/AiSuggest'),
        meta: { title: '智能建议', hidden: true },
      },
      {
        path: 'ai/analysis',
        name: 'AiAnalysis',
        component: lazy('ai/AiAnalysis'),
        meta: { title: '数据分析', hidden: true },
      },

      /* ---- 系统管理 ---- */
      {
        path: 'system',
        name: 'SystemManage',
        redirect: '/system/organization',
        meta: { title: '系统管理', icon: 'SettingOutlined' },
        children: [
          { path: 'organization', name: 'SystemOrg', component: lazy('system/SystemOrganization'), meta: { title: '组织架构' } },
          { path: 'store', name: 'SystemStore', component: lazy('system/SystemStore'), meta: { title: '门店管理' } },
          { path: 'user', name: 'SystemUser', component: lazy('system/SystemUser'), meta: { title: '用户管理' } },
          { path: 'role', name: 'SystemRole', component: lazy('system/SystemRole'), meta: { title: '角色权限' } },
          { path: 'config', name: 'SystemConfig', component: lazy('system/SystemConfig'), meta: { title: '系统配置' } },
        ],
      },

      /* ---- 日志管理 ---- */
      {
        path: 'log',
        name: 'LogManage',
        component: lazy('log/LogManage'),
        meta: { title: '日志管理', icon: 'FileSearchOutlined' },
      },
    ],
  },

  /* ---- 登录 ---- */
  {
    path: '/login',
    name: 'Login',
    component: lazy('login/LoginView'),
    meta: { title: '登录', hidden: true },
  },

  /* ---- 注册 ---- */
  {
    path: '/register',
    name: 'Register',
    component: lazy('login/RegisterView'),
    meta: { title: '注册', hidden: true },
  },

  /* ---- 提醒 ---- */
  {
    path: '/notify',
    name: 'ProfileNotify',
    component: lazy('profile/ProfileNotify'),
    meta: { title: '提醒', hidden: true },
  },

  /* ---- 信息 ---- */
  {
    path: '/message',
    name: 'ProfileMessage',
    component: lazy('profile/ProfileMessage'),
    meta: { title: '信息', hidden: true },
  },

  /* ---- 个人中心 ---- */
  {
    path: '/profile',
    name: 'ProfileCenter',
    component: lazy('profile/ProfileCenter'),
    meta: { title: '个人中心', hidden: true },
  },

  /* ---- 个人设置 ---- */
  {
    path: '/settings',
    name: 'ProfileSetting',
    component: lazy('profile/ProfileSetting'),
    meta: { title: '个人设置', hidden: true },
  },

  /* ---- 404 ---- */
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: lazy('login/LoginView'),
    meta: { title: '页面不存在', hidden: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: menuRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

/* ---------- 全局前置守卫 ---------- */
router.beforeEach((to, _from, next) => {
  const title = (to.meta.title as string) || '珠宝通'
  document.title = `${title} - 珠宝通珠宝行业管理系统`

  // 登录鉴权 —— token 不存在时跳 /login
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login' && to.path !== '/register' && !to.path.startsWith('/profile') && !to.path.startsWith('/settings') && !to.path.startsWith('/notify') && !to.path.startsWith('/message')) {
    return next('/login')
  }
  // 已登录访问登录页时跳转到首页
  if (token && (to.path === '/login' || to.path === '/register')) {
    return next('/dashboard')
  }

  next()
})

export default router
