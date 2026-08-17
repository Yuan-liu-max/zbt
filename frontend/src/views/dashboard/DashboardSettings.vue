<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2><SettingOutlined /> 自定义设置</h2>
        <p class="page-desc">配置首页展示内容，打造个性化工作台</p>
      </div>
      <a-button type="primary" @click="handleSave" :loading="saving">
        保存设置
      </a-button>
    </div>

    <div class="settings-layout">
      <!-- 左侧菜单 -->
      <div class="settings-sidebar">
        <div v-for="item in menuItems" :key="item.key" class="menu-item" :class="{ active: activeMenu === item.key }" @click="activeMenu = item.key">
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="settings-content">
        <!-- 基础设置 -->
        <template v-if="activeMenu === 'basic'">
          <div class="section-card">
            <div class="section-title">基础信息</div>
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
              <a-form-item label="欢迎语">
                <a-input v-model:value="config.welcomeText" :maxlength="20" show-count placeholder="请输入欢迎语" />
              </a-form-item>
              <a-form-item label="显示日期">
                <div class="switch-row">
                  <a-switch v-model:checked="config.showDate" />
                  <span class="switch-hint">开启后将在首页顶部显示当前日期</span>
                </div>
              </a-form-item>
            </a-form>
          </div>

          <div class="section-card">
            <div class="section-header">
              <span class="section-title">首页模块设置</span>
              <a-button type="link" @click="handleRestoreDefault">恢复默认</a-button>
            </div>
            <p class="section-desc">选择需要在首页展示的模块，拖拽可调整模块顺序</p>
            <div class="module-list">
              <div v-for="mod in modules" :key="mod.key" class="module-item">
                <div class="module-left">
                  <span class="drag-handle">⋮⋮</span>
                  <div class="module-icon" :style="{ background: mod.color + '15', color: mod.color }">
                    <component :is="mod.icon" />
                  </div>
                  <span class="module-name">{{ mod.label }}</span>
                </div>
                <div class="module-right">
                  <a-switch v-model:checked="mod.enabled" />
                  <a class="expand-btn"><DownOutlined /></a>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 数据概览 -->
        <template v-if="activeMenu === 'overview'">
          <div class="section-card">
            <div class="section-title">数据概览设置</div>
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
              <a-form-item label="显示销售数据">
                <a-switch v-model:checked="config.showSales" />
              </a-form-item>
              <a-form-item label="显示订单数据">
                <a-switch v-model:checked="config.showOrders" />
              </a-form-item>
              <a-form-item label="显示客户数据">
                <a-switch v-model:checked="config.showCustomers" />
              </a-form-item>
            </a-form>
          </div>
        </template>

        <!-- 快捷入口 -->
        <template v-if="activeMenu === 'shortcut'">
          <div class="section-card">
            <div class="section-title">快捷入口设置</div>
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
              <a-form-item label="最大显示数">
                <a-input-number v-model:value="config.shortcutMax" :min="4" :max="12" style="width: 120px" />
              </a-form-item>
              <a-form-item label="显示名称">
                <a-switch v-model:checked="config.showShortcutName" />
              </a-form-item>
            </a-form>
          </div>
        </template>

        <!-- 公告设置 -->
        <template v-if="activeMenu === 'notice'">
          <div class="section-card">
            <div class="section-title">公告设置</div>
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
              <a-form-item label="显示公告">
                <a-switch v-model:checked="config.showNotice" />
              </a-form-item>
              <a-form-item label="公告标题">
                <a-input v-model:value="config.noticeTitle" placeholder="请输入公告标题" />
              </a-form-item>
              <a-form-item label="公告内容">
                <a-textarea v-model:value="config.noticeContent" :rows="4" placeholder="请输入公告内容" />
              </a-form-item>
            </a-form>
          </div>
        </template>

        <!-- 布局设置 -->
        <template v-if="activeMenu === 'layout'">
          <div class="section-card">
            <div class="section-title">布局设置</div>
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
              <a-form-item label="侧边栏主题">
                <a-radio-group v-model:value="config.sidebarTheme">
                  <a-radio value="dark">深色</a-radio>
                  <a-radio value="light">浅色</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item label="固定头部">
                <a-switch v-model:checked="config.fixedHeader" />
              </a-form-item>
            </a-form>
          </div>
        </template>

        <!-- 其他设置 -->
        <template v-if="activeMenu === 'other'">
          <div class="section-card">
            <div class="section-title">其他设置</div>
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
              <a-form-item label="动画效果">
                <a-switch v-model:checked="config.enableAnimation" />
              </a-form-item>
              <a-form-item label="紧凑模式">
                <a-switch v-model:checked="config.compactMode" />
              </a-form-item>
            </a-form>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, markRaw } from 'vue'
import { message } from 'ant-design-vue'
import {
  SettingOutlined, HomeOutlined, BarChartOutlined, LinkOutlined,
  NotificationOutlined, AppstoreOutlined, ToolOutlined,
  DownOutlined, CheckCircleOutlined
} from '@ant-design/icons-vue'

const activeMenu = ref('basic')
const saving = ref(false)

// 菜单项
const menuItems = [
  { key: 'basic', label: '基础设置', icon: markRaw(HomeOutlined) },
  { key: 'overview', label: '数据概览', icon: markRaw(BarChartOutlined) },
  { key: 'shortcut', label: '快捷入口', icon: markRaw(LinkOutlined) },
  { key: 'notice', label: '公告设置', icon: markRaw(NotificationOutlined) },
  { key: 'layout', label: '布局设置', icon: markRaw(AppstoreOutlined) },
  { key: 'other', label: '其他设置', icon: markRaw(ToolOutlined) },
]

// 配置数据
const config = reactive({
  welcomeText: '欢迎回来，管理员',
  showDate: true,
  showSales: true,
  showOrders: true,
  showCustomers: true,
  shortcutMax: 8,
  showShortcutName: true,
  showNotice: true,
  noticeTitle: '系统通知',
  noticeContent: '',
  sidebarTheme: 'dark',
  fixedHeader: true,
  enableAnimation: true,
  compactMode: false,
})

// 首页模块
const modules = reactive([
  { key: 'overview', label: '数据概览', icon: markRaw(BarChartOutlined), color: '#1890ff', enabled: true },
  { key: 'shortcut', label: '快捷入口', icon: markRaw(LinkOutlined), color: '#c8a44d', enabled: true },
  { key: 'todo', label: '待办事项', icon: markRaw(CheckCircleOutlined), color: '#52c41a', enabled: true },
  { key: 'trend', label: '数据趋势', icon: markRaw(BarChartOutlined), color: '#722ed1', enabled: false },
  { key: 'notice', label: '系统公告', icon: markRaw(NotificationOutlined), color: '#fa8c16', enabled: true },
])

const handleSave = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    localStorage.setItem('dashboardSettings', JSON.stringify(config))
    message.success('设置已保存')
  } catch (error) { console.error('保存失败', error) }
  finally { saving.value = false }
}

const handleRestoreDefault = () => {
  config.welcomeText = '欢迎回来，管理员'
  config.showDate = true
  modules.forEach(m => m.enabled = true)
  message.success('已恢复默认设置')
}
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.page-desc { margin: 4px 0 0; font-size: 13px; color: #999; }

.settings-layout { display: flex; gap: 24px; }

.settings-sidebar {
  width: 200px; background: #fff; border-radius: 12px; padding: 12px; flex-shrink: 0;
}

.menu-item {
  display: flex; align-items: center; gap: 10px; padding: 12px 16px;
  border-radius: 8px; cursor: pointer; transition: all 0.2s; font-size: 14px; color: #666;
}
.menu-item:hover { background: #f5f5f5; }
.menu-item.active { background: #fff7e6; color: #c8a44d; font-weight: 500; }
.menu-item .anticon { font-size: 16px; }

.settings-content { flex: 1; }

.section-card {
  background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px;
}

.section-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;
}

.section-title {
  font-size: 16px; font-weight: 600; color: #333; margin-bottom: 16px;
  padding-left: 12px; border-left: 4px solid #c8a44d;
}

.section-header .section-title { margin-bottom: 0; }

.section-desc {
  font-size: 13px; color: #999; margin-bottom: 16px;
}

.switch-row {
  display: flex; align-items: center; gap: 12px;
}

.switch-hint {
  font-size: 13px; color: #999;
}

.module-list {
  display: flex; flex-direction: column; gap: 8px;
}

.module-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border: 1px solid #f0f0f0; border-radius: 8px;
  transition: all 0.2s;
}
.module-item:hover { border-color: #d9d9d9; }

.module-left {
  display: flex; align-items: center; gap: 12px;
}

.drag-handle {
  color: #ccc; cursor: grab; font-size: 14px; user-select: none;
}

.module-icon {
  width: 36px; height: 36px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center; font-size: 16px;
}

.module-name { font-size: 14px; color: #333; }

.module-right {
  display: flex; align-items: center; gap: 12px;
}

.expand-btn {
  color: #999; cursor: pointer; font-size: 12px; transition: transform 0.2s;
}
.expand-btn:hover { color: #666; }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .settings-layout { flex-direction: column; }
  .settings-sidebar { width: 100%; display: flex; overflow-x: auto; padding: 8px; }
  .menu-item { white-space: nowrap; padding: 10px 12px; font-size: 13px; }
  .section-card { padding: 16px; }
}
</style>
