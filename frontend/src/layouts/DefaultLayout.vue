<template>
  <a-config-provider :theme="themeConfig" :locale="zhCN">
    <a-layout class="layout-wrapper">
      <!-- ==================== PC 展开侧边栏（大屏） ==================== -->
      <a-layout-sider
        v-if="!isSmallScreen"
        v-model:collapsed="collapsed"
        :trigger="null"
        collapsible
        :width="220"
        :collapsed-width="64"
        breakpoint="lg"
        class="layout-sidebar"
        @breakpoint="(broken: boolean) => (isSmallScreen = broken)"
      >
        <!-- Logo -->
        <div class="sidebar-logo" :class="{ collapsed }">
          <div class="logo-icon">
            <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="36" height="36" rx="8" fill="url(#gold-grad)"/>
              <path d="M18 8L12 16h12L18 8z" fill="#fff" opacity="0.9"/>
              <path d="M10 18l8 12 8-12H10z" fill="#fff" opacity="0.7"/>
              <defs>
                <linearGradient id="gold-grad" x1="0" y1="0" x2="36" y2="36">
                  <stop stop-color="#e8d59a"/>
                  <stop offset="1" stop-color="#c8a44d"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <div v-show="!collapsed" class="logo-text">
            <span>珠宝通</span>
            <span class="sub">珠宝行业管理系统</span>
          </div>
        </div>

        <!-- 菜单 -->
        <div class="sidebar-menu-wrap">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            v-model:openKeys="openKeys"
            mode="inline"
            theme="dark"
            @click="onMenuClick"
          >
            <template v-for="route in menuList" :key="route.path">
              <!-- 单级菜单 -->
              <a-menu-item
                v-if="!route.children || route.children.length <= 1"
                :key="route.path"
              >
                <component :is="getIcon(route.meta?.icon as string)" />
                <span>{{ route.meta?.title }}</span>
              </a-menu-item>

              <!-- 多级菜单 -->
              <a-sub-menu v-else :key="route.path">
                <template #title>
                  <component :is="getIcon(route.meta?.icon as string)" />
                  <span>{{ route.meta?.title }}</span>
                </template>
                <a-menu-item
                  v-for="child in route.children"
                  :key="`/${route.path}/${child.path}`"
                >
                  {{ child.meta?.title }}
                </a-menu-item>
              </a-sub-menu>
            </template>
          </a-menu>
        </div>
      </a-layout-sider>

      <!-- ==================== 抽屉式侧边栏（小屏） ==================== -->
      <a-drawer
        v-if="isSmallScreen"
        v-model:open="drawerVisible"
        placement="left"
        :width="240"
        :closable="false"
        :body-style="{ padding: 0, background: '#1a1a2e' }"
        :header-style="{ display: 'none' }"
        wrap-class-name="sidebar-drawer"
        @close="drawerVisible = false"
      >
        <!-- Logo -->
        <div class="sidebar-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="36" height="36" rx="8" fill="url(#gold-grad)"/>
              <path d="M18 8L12 16h12L18 8z" fill="#fff" opacity="0.9"/>
              <path d="M10 18l8 12 8-12H10z" fill="#fff" opacity="0.7"/>
              <defs>
                <linearGradient id="gold-grad" x1="0" y1="0" x2="36" y2="36">
                  <stop stop-color="#e8d59a"/>
                  <stop offset="1" stop-color="#c8a44d"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <div class="logo-text">
            <span>珠宝通</span>
            <span class="sub">珠宝行业管理系统</span>
          </div>
        </div>

        <!-- 菜单 -->
        <div class="sidebar-menu-wrap">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            v-model:openKeys="openKeys"
            mode="inline"
            theme="dark"
            @click="onMenuClickDrawer"
          >
            <template v-for="route in menuList" :key="route.path">
              <!-- 单级菜单 -->
              <a-menu-item
                v-if="!route.children || route.children.length <= 1"
                :key="route.path"
              >
                <component :is="getIcon(route.meta?.icon as string)" />
                <span>{{ route.meta?.title }}</span>
              </a-menu-item>

              <!-- 多级菜单 -->
              <a-sub-menu v-else :key="route.path">
                <template #title>
                  <component :is="getIcon(route.meta?.icon as string)" />
                  <span>{{ route.meta?.title }}</span>
                </template>
                <a-menu-item
                  v-for="child in route.children"
                  :key="`/${route.path}/${child.path}`"
                >
                  {{ child.meta?.title }}
                </a-menu-item>
              </a-sub-menu>
            </template>
          </a-menu>
        </div>
      </a-drawer>

      <!-- ==================== 右侧内容 ==================== -->
      <a-layout
        class="layout-main"
        :style="mainStyle"
      >
        <!-- 顶栏 -->
        <a-layout-header class="layout-header">
          <div class="header-left">
            <!-- 小屏菜单按钮 -->
            <a-button
              v-if="isSmallScreen"
              type="text"
              class="trigger"
              @click="drawerVisible = true"
            >
              <MenuOutlined style="font-size: 20px" />
            </a-button>

            <!-- PC 折叠按钮 -->
            <a-button
              v-else
              type="text"
              class="trigger"
              @click="collapsed = !collapsed"
            >
              <component :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined" style="font-size: 18px" />
            </a-button>

            <!-- 面包屑 -->
            <a-breadcrumb v-if="!isSmallScreen" class="header-breadcrumb">
              <a-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
                <router-link v-if="item.path" :to="item.path">{{ item.title }}</router-link>
                <span v-else>{{ item.title }}</span>
              </a-breadcrumb-item>
            </a-breadcrumb>
          </div>

          <div class="header-right">
            <!-- 搜索 -->
            <a-input-search
              class="header-search"
              placeholder="请输入关键词"
              allow-clear
              @search="onSearch"
            />

            <!-- 通知图标 -->
            <a-badge :count="unreadCount" :offset="[-4, 4]">
              <a-button type="text" shape="circle" @click="router.push('/notify')">
                <BellOutlined style="font-size: 18px" />
              </a-button>
            </a-badge>

            <!-- 邮件图标 -->
            <a-badge :count="mailUnread" :offset="[-4, 4]">
              <a-button type="text" shape="circle" @click="router.push('/message')">
                <MailOutlined style="font-size: 18px" />
              </a-button>
            </a-badge>

            <!-- 用户信息 -->
            <a-dropdown>
              <div class="header-user">
                <a-avatar :size="32" style="background-color: #c8a44d">
                  <template #icon><UserOutlined /></template>
                </a-avatar>
                <span v-if="!isSmallScreen" class="header-username">{{ userInfo.name || userInfo.username }}</span>
                <DownOutlined v-if="!isSmallScreen" style="font-size: 12px; margin-left: 4px" />
              </div>
              <template #overlay>
                <a-menu @click="handleUserMenuClick">
                  <a-menu-item key="profile">
                    <UserOutlined /> 个人中心
                  </a-menu-item>
                  <a-menu-item key="settings">
                    <SettingOutlined /> 个人设置
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout">
                    <LogoutOutlined /> 退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </a-layout-header>

        <!-- 主内容区 -->
        <a-layout-content class="layout-content">
          <router-view v-slot="{ Component }">
            <transition name="slide-fade" mode="out-in">
              <keep-alive :max="10">
                <component :is="Component" :key="$route.fullPath" />
              </keep-alive>
            </transition>
          </router-view>
        </a-layout-content>

        <!-- 底部 -->
        <a-layout-footer class="layout-footer">
          © {{ new Date().getFullYear() }} 珠宝通珠宝行业管理系统 版权所有
        </a-layout-footer>
      </a-layout>
    </a-layout>
  </a-config-provider>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, type Component as VueComponent } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { menuRoutes } from '@/router'
import { useAuthStore } from '@/stores/useAuthStore'
import { notificationApi } from '@/api/notification'
import {
  HomeOutlined,
  ShoppingOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  TeamOutlined,
  ShopOutlined,
  SafetyCertificateOutlined,
  ImportOutlined,
  RiseOutlined,
  AccountBookOutlined,
  BarChartOutlined,
  PercentageOutlined,
  ScheduleOutlined,
  UserSwitchOutlined,
  EnvironmentOutlined,
  RobotOutlined,
  SettingOutlined,
  FileSearchOutlined,
  MenuOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  MailOutlined,
  UserOutlined,
  DownOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'

/* ---------- 响应式状态 ---------- */
const collapsed = ref(false)
const drawerVisible = ref(false)
const isSmallScreen = ref(false)
const selectedKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 通知未读数
const unreadCount = ref(0)
// 邮件未读数（暂无后端接口，先静态置 0）
const mailUnread = ref(0)

const fetchUnreadCount = async () => {
  try {
    const res: any = await notificationApi.getUnreadCount()
    unreadCount.value = res?.count ?? res?.unreadCount ?? 0
  } catch {
    // 静默失败，不影响页面
  }
}

/* ---------- 用户信息 ---------- */
const userInfo = computed(() => authStore.userInfo || { username: '管理员', name: '管理员' })

const handleUserMenuClick = ({ key }: { key: string }) => {
  if (key === 'logout') {
    authStore.logout()
    message.success('已退出登录')
    router.push('/login')
  } else if (key === 'profile') {
    router.push('/profile')
  } else if (key === 'settings') {
    router.push('/settings')
  }
}

/* ---------- 菜单数据（过滤隐藏项） ---------- */
const menuList = computed(() =>
  menuRoutes[0]?.children?.filter((r: any) => !r.meta?.hidden && r.path !== 'login') ?? []
)

/* ---------- 图标映射 ---------- */
const iconMap: Record<string, VueComponent> = {
  HomeOutlined,
  ShoppingOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  TeamOutlined,
  ShopOutlined,
  SafetyCertificateOutlined,
  ImportOutlined,
  RiseOutlined,
  AccountBookOutlined,
  BarChartOutlined,
  PercentageOutlined,
  ScheduleOutlined,
  UserSwitchOutlined,
  EnvironmentOutlined,
  RobotOutlined,
  SettingOutlined,
  FileSearchOutlined,
}

const getIcon = (name?: string): VueComponent => {
  return iconMap[name || ''] || HomeOutlined
}

/* ---------- 主内容区偏移 ---------- */
const mainStyle = computed(() => {
  if (isSmallScreen.value) return { marginLeft: 0 }
  return { marginLeft: collapsed.value ? '64px' : '220px' }
})

/* ---------- 面包屑 ---------- */
const breadcrumbs = computed(() => {
  const matched = route.matched.filter((r) => r.meta?.title)
  return matched.map((r) => ({
    title: r.meta.title as string,
    path: r.redirect ? r.path : undefined,
  }))
})

/* ---------- 菜单点击 ---------- */
const onMenuClick = ({ key }: { key: string }) => {
  // 确保路径以 / 开头，避免相对路径导致的重复
  const path = key.startsWith('/') ? key : `/${key}`
  // 如果当前路径与目标路径相同，则不进行导航
  if (route.path !== path) {
    router.push(path)
  }
}

const onMenuClickDrawer = ({ key }: { key: string }) => {
  const path = key.startsWith('/') ? key : `/${key}`
  if (route.path !== path) {
    router.push(path)
  }
  drawerVisible.value = false
}

/* ---------- 搜索 ---------- */
const onSearch = (value: string) => {
  console.log('搜索:', value)
}

/* ---------- 路由同步 ---------- */
watch(
  () => route.path,
  (path) => {
    const segments = path.split('/').filter(Boolean)
    selectedKeys.value = [path]

    if (segments.length > 1) {
      openKeys.value = [`/${segments[0]}`]
    }
  },
  { immediate: true }
)

/* ---------- 监听屏幕尺寸 ---------- */
const checkScreen = () => {
  isSmallScreen.value = window.innerWidth < 992
}

// 定时器引用
let unreadTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  checkScreen()
  fetchUnreadCount()
  // 每60秒刷新未读数
  unreadTimer = setInterval(fetchUnreadCount, 60000)
  window.addEventListener('resize', checkScreen)
})

onUnmounted(() => {
  if (unreadTimer) clearInterval(unreadTimer)
  window.removeEventListener('resize', checkScreen)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreen)
})

/* ---------- Ant Design 主题 ---------- */
const themeConfig = {
  token: {
    colorPrimary: '#c8a44d',
    borderRadius: 6,
    fontFamily: "'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif",
  },
}

// 中文语言包
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')
</script>

<style lang="less" scoped>
@import '@/styles/variables.less';

/* 布局 */
.layout-wrapper {
  min-height: 100vh;
}

.layout-sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 200;
  background: @bg-dark;

  &::-webkit-scrollbar {
    width: 0;
  }

  .ant-menu-dark {
    background: transparent;
  }

  .ant-menu-dark .ant-menu-item-selected {
    background: linear-gradient(90deg, @primary-color, @primary-color-light) !important;
    border-radius: 0 20px 20px 0;
    margin-right: 12px;
  }
}

.sidebar-logo {
  height: @header-height;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  overflow: hidden;
  transition: padding 0.2s;

  &.collapsed {
    padding: 0;
    justify-content: center;
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    margin-left: 10px;
    white-space: nowrap;
    overflow: hidden;
    line-height: 1.3;

    span:first-child {
      font-size: @font-size-md;
      font-weight: 700;
      color: @primary-color;
    }

    .sub {
      display: block;
      font-size: @font-size-xs;
      font-weight: 400;
      color: rgba(255,255,255,0.45);
    }
  }
}

.sidebar-menu-wrap {
  padding: 8px 0;
}

/* 抽屉样式覆盖 */
:deep(.sidebar-drawer) {
  .ant-drawer-content {
    background: @bg-dark;
  }

  .ant-drawer-body {
    padding: 0 !important;
    background: @bg-dark !important;
  }

  .ant-menu-dark {
    background: transparent;
  }

  .ant-menu-dark .ant-menu-item-selected {
    background: linear-gradient(90deg, @primary-color, @primary-color-light) !important;
    border-radius: 0 20px 20px 0;
    margin-right: 12px;
  }
}

/* 顶栏 */
.layout-header {
  background: @bg-card;
  height: @header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 @spacing-lg;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  z-index: @z-header;
  position: sticky;
  top: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: @spacing-md;
}

.header-right {
  display: flex;
  align-items: center;
  gap: @spacing-md;
}

.header-breadcrumb {
  margin-left: 8px;
}

.header-search {
  width: 240px;

  @media (max-width: @screen-md) {
    display: none;
  }
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;

  &:hover {
    background: rgba(0,0,0,0.04);
  }
}

.header-username {
  font-size: @font-size-sm;
  color: @text-primary;
  white-space: nowrap;
}

.trigger {
  font-size: 18px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 主内容区 */
.layout-content {
  padding: @spacing-lg;
  min-height: calc(100vh - @header-height - @footer-height);
  background: @bg-body;
}

/* 底部 */
.layout-footer {
  text-align: center;
  font-size: @font-size-xs;
  color: @text-hint;
  padding: @spacing-md 0;
  background: transparent;
}

/* 小屏适配 */
@media (max-width: @screen-md) {
  .layout-header {
    padding: 0 12px;
  }

  .layout-content {
    padding: @spacing-md;
  }

  .header-breadcrumb {
    display: none;
  }
}
</style>
