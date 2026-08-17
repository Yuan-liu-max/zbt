<template>
  <div class="page-container">
    <!-- ==================== 欢迎栏 ==================== -->
    <div class="welcome-bar">
      <div>
        <h2>
          <SmileOutlined style="color: #c8a44d; margin-right: 8px" />
          欢迎回来，管理员
        </h2>
        <p class="welcome-date">今天是 {{ todayStr }} {{ weekdayStr }}</p>
      </div>
      <a-button type="primary" ghost @click="router.push('/dashboard/settings')">
        <SettingOutlined /> 自定义设置
      </a-button>
    </div>

    <!-- ==================== 统计卡片 ==================== -->
    <div class="dashboard-stats">
      <div v-for="(item, idx) in statCards" :key="idx" class="stat-card" @click="router.push(item.path)">
        <div class="stat-card__header">
          <div class="stat-card__label">{{ item.label }}</div>
          <div :class="['stat-card__icon', item.color]">
            <component :is="item.icon" />
          </div>
        </div>
        <div class="stat-card__value">{{ item.value }}</div>
        <div :class="['stat-card__trend', item.trendDir]">
          较昨日 <ArrowUpOutlined v-if="item.trendDir === 'up'" />
          <ArrowDownOutlined v-else />
          {{ item.trend }}
        </div>
      </div>
    </div>

    <!-- ==================== 图表行 ==================== -->
    <div class="dashboard-charts">
      <!-- 销售额趋势 -->
      <div class="content-card">
        <div class="content-card__header">
          <h3>销售额趋势</h3>
          <a-radio-group v-model:value="trendRange" button-style="solid" size="small">
            <a-radio-button value="day">今日</a-radio-button>
            <a-radio-button value="week">本周</a-radio-button>
            <a-radio-button value="month">本月</a-radio-button>
            <a-radio-button value="year">本年</a-radio-button>
          </a-radio-group>
        </div>
        <div class="chart-placeholder">
          <div class="chart-y-axis">
            <span v-for="(t, i) in yTicks" :key="i">{{ t }}</span>
          </div>
          <div class="chart-area">
            <svg v-if="trendPoints.length > 0" viewBox="0 0 600 200" preserveAspectRatio="none" class="chart-svg">
              <defs>
                <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#c8a44d" stop-opacity="0.3"/>
                  <stop offset="100%" stop-color="#c8a44d" stop-opacity="0.02"/>
                </linearGradient>
              </defs>
              <!-- 面积 -->
              <path :d="trendAreaPath" fill="url(#areaGrad)"/>
              <!-- 线 -->
              <path :d="trendLinePath" fill="none" stroke="#c8a44d" stroke-width="2.5" stroke-linecap="round"/>
              <!-- 数据点 -->
              <circle v-for="(p, i) in trendPoints" :key="i" :cx="p.x" :cy="p.y" r="3" fill="#c8a44d"/>
            </svg>
            <div v-else class="chart-empty">暂无数据</div>
            <div class="chart-x-axis">
              <span v-for="(t, i) in trendLabels" :key="i">{{ t }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 销售渠道占比 -->
      <div class="content-card">
        <div class="content-card__header">
          <h3>销售渠道占比</h3>
        </div>
        <div class="chart-placeholder donut-wrap">
          <svg v-if="channelSegments.length > 0" viewBox="0 0 200 200" class="donut-svg">
            <circle v-for="(seg, i) in channelSegments" :key="i" cx="100" cy="100" r="80" fill="none"
              :stroke="seg.color || '#c8a44d'" stroke-width="28"
              :stroke-dasharray="seg.dash" :stroke-dashoffset="seg.offset" transform="rotate(-90 100 100)"/>
            <!-- 中心文字 -->
            <text x="100" y="92" text-anchor="middle" font-size="11" fill="#999">总销售额</text>
            <text x="100" y="115" text-anchor="middle" font-size="16" font-weight="bold" fill="#1a1a1a">¥{{ fmtNum(totalSales) }}</text>
          </svg>
          <div v-else class="chart-empty">暂无数据</div>
          <div class="donut-legend">
            <div v-for="(seg, i) in channelSegments" :key="i" class="legend-item">
              <span class="dot" :style="{ background: seg.color || '#c8a44d' }"></span>{{ seg.name }} {{ seg.percentage.toFixed(1) }}%
            </div>
          </div>
        </div>
      </div>

      <!-- 系统快捷入口 -->
      <div class="content-card">
        <div class="content-card__header">
          <h3>系统快捷入口</h3>
        </div>
        <div class="quick-entry">
          <div
            v-for="(entry, idx) in quickEntries"
            :key="idx"
            class="quick-entry-item"
            @click="$router.push(entry.path)"
          >
            <div class="entry-icon">
              <component :is="entry.icon" />
            </div>
            <span class="entry-label">{{ entry.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 底部区域 ==================== -->
    <div class="dashboard-bottom">
      <!-- 最新订单 -->
      <div class="content-card">
        <div class="content-card__header">
          <h3>最新订单</h3>
          <a-button type="link" size="small" @click="router.push('/order/list')">更多 <RightOutlined /></a-button>
        </div>

        <!-- 大屏表格 -->
        <a-table
          v-if="!isSmallScreen"
          :columns="orderColumns"
          :data-source="latestOrders"
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'amount'">
              <span style="font-weight: 600; color: #1a1a1a">¥{{ record.amount.toLocaleString() }}</span>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="router.push('/order/list')">查看</a-button>
            </template>
          </template>
        </a-table>

        <!-- 小屏卡片列表 -->
        <div v-else class="order-card-list">
          <div
            v-for="(order, idx) in latestOrders"
            :key="idx"
            class="order-card-item"
          >
            <div class="order-card-row">
              <span class="order-no">{{ order.no }}</span>
              <a-tag :color="getStatusColor(order.status)">{{ getStatusText(order.status) }}</a-tag>
            </div>
            <div class="order-card-row">
              <span class="order-customer">{{ order.customer }}</span>
              <span class="order-amount">¥{{ order.amount.toLocaleString() }}</span>
            </div>
            <div class="order-time">{{ order.time }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧栏 -->
      <div class="dashboard-right-col">
        <!-- 待办事项 -->
        <div class="content-card">
          <div class="content-card__header">
            <h3>待办事项</h3>
            <a-button type="link" size="small" @click="router.push('/task/list')">更多 <RightOutlined /></a-button>
          </div>
          <div class="todo-list">
            <div v-for="(item, idx) in todoItems" :key="idx" class="todo-item" @click="$router.push(item.path)">
              <span class="todo-label">{{ item.label }}</span>
              <span :class="['todo-badge', item.color]">{{ item.count }}</span>
            </div>
          </div>
        </div>

        <!-- 系统通知 -->
        <div class="content-card">
          <div class="content-card__header">
            <h3>系统通知</h3>
            <a-button type="link" size="small" @click="router.push('/notify')">更多 <RightOutlined /></a-button>
          </div>
          <div class="notice-list">
            <div v-for="(item, idx) in notices" :key="idx" class="notice-item" @click="item.path && $router.push(item.path)">
              <span class="notice-dot" />
              <span class="notice-content">{{ item.content }}</span>
              <span class="notice-time">{{ item.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
import {
  SmileOutlined,
  SettingOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  RightOutlined,
  ShoppingCartOutlined,
  FileTextOutlined,
  TeamOutlined,
  DatabaseOutlined,
  SafetyCertificateOutlined,
  AuditOutlined,
  ImportOutlined,
  BarChartOutlined,
  GiftOutlined,
} from '@ant-design/icons-vue'
import { reportApi } from '@/api/report'
import { orderStatusMap } from '@/api/order'

/* ---------- 日期 ---------- */
const now = new Date()
const weekdayMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const todayStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
const weekdayStr = weekdayMap[now.getDay()]

/* ---------- 数据驾驶舱 ---------- */
const dashboardData = ref<any>({})
const loading = ref(false)

/* ---------- 统计卡片 ---------- */
const statCards = ref([
  { label: '任务总数', value: '-', icon: ShoppingCartOutlined, color: 'gold', trend: '0.00%', trendDir: 'up', path: '/task/list' },
  { label: '已完成任务', value: '-', icon: FileTextOutlined, color: 'blue', trend: '0.00%', trendDir: 'up', path: '/task/list' },
  { label: '超时任务', value: '-', icon: TeamOutlined, color: 'green', trend: '0.00%', trendDir: 'down', path: '/task/list' },
  { label: '门店数量', value: '-', icon: DatabaseOutlined, color: 'purple', trend: '0.00%', trendDir: 'down', path: '/system/store' },
  { label: '待处理订单', value: '-', icon: AuditOutlined, color: 'orange', trend: '0.00%', trendDir: 'down', path: '/order/list?status=pending' },
  { label: '证书到期预警', value: '-', icon: SafetyCertificateOutlined, color: 'red', trend: '0.00%', trendDir: 'up', path: '/certificate' },
])

const loadDashboard = async () => {
  loading.value = true
  try {
    const data: any = await reportApi.getDashboard()
    dashboardData.value = data || {}

    statCards.value[0].value = String(data?.totalTasks ?? 0)
    statCards.value[1].value = String(data?.completedTasks ?? 0)
    statCards.value[2].value = String(data?.overdueTasks ?? 0)
    statCards.value[3].value = String(data?.storeCount ?? 0)
    statCards.value[4].value = String(data?.pendingOrders ?? 0)
    statCards.value[5].value = String(data?.certExpiring ?? 0)

    todoItems.value = data?.todos || []
    notices.value = data?.notices || []
    latestOrders.value = data?.latestOrders || []
    trendData.value = data?.salesTrend || []
    channelData.value = data?.channelStats || []
    totalSales.value = Number(data?.kpis?.totalSales) || 0
  } catch (error) {
    console.error('加载驾驶舱数据失败', error)
  } finally {
    loading.value = false
  }
}

/* ---------- 趋势区间 ---------- */
const trendRange = ref('month')
const trendData = ref<any[]>([])
const channelData = ref<any[]>([])
const totalSales = ref(0)

const formatDate = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const trendView = computed(() => {
  const list = trendData.value || []
  const now = new Date()
  let cutoff = ''
  if (trendRange.value === 'day') {
    cutoff = formatDate(now)
  } else if (trendRange.value === 'week') {
    const d = new Date(now); d.setDate(d.getDate() - 6); cutoff = formatDate(d)
  } else if (trendRange.value === 'month') {
    const d = new Date(now); d.setDate(d.getDate() - 29); cutoff = formatDate(d)
  } else {
    cutoff = `${now.getFullYear()}-01-01`
  }
  return list.filter((it: any) => it.date && String(it.date) >= cutoff)
})

const trendPoints = computed(() => {
  const list = trendView.value
  if (!list.length) return []
  const W = 600, H = 200, PAD = 6
  const maxVal = Math.max(...list.map((it: any) => Number(it.salesAmount) || 0), 1)
  return list.map((it: any, i: number) => {
    const x = list.length === 1 ? W / 2 : PAD + (i * (W - PAD * 2)) / (list.length - 1)
    const y = H - PAD - ((Number(it.salesAmount) || 0) / maxVal) * (H - PAD * 2)
    return { x, y }
  })
})

const trendLinePath = computed(() =>
  trendPoints.value.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x},${p.y}`).join(' ')
)

const trendAreaPath = computed(() => {
  const pts = trendPoints.value
  if (!pts.length) return ''
  const line = trendLinePath.value
  return `${line} L${pts[pts.length - 1].x},200 L${pts[0].x},200 Z`
})

const trendLabels = computed(() => {
  const list = trendView.value
  if (!list.length) return []
  const step = Math.max(1, Math.ceil(list.length / 6))
  return list.filter((_, i) => i % step === 0).map((it: any) => String(it.date || '').slice(5))
})

const fmtNum = (v: number) => Math.round(v).toLocaleString()

const yTicks = computed(() => {
  const list = trendView.value
  const maxVal = Math.max(...list.map((it: any) => Number(it.salesAmount) || 0), 0)
  return [maxVal, maxVal * 0.8, maxVal * 0.6, maxVal * 0.4, maxVal * 0.2, 0].map((v) => fmtNum(v))
})

const DONUT_C = 2 * Math.PI * 80
const channelSegments = computed(() => {
  const list = (channelData.value || []).filter((it: any) => (Number(it.value) || 0) > 0)
  const total = list.reduce((s: number, it: any) => s + (Number(it.value) || 0), 0)
  let cumulative = 0
  return list.map((it: any) => {
    const value = Number(it.value) || 0
    const percentage = total > 0 ? (value / total) * 100 : 0
    const len = (percentage / 100) * DONUT_C
    const seg = {
      ...it,
      percentage,
      dash: `${len} ${DONUT_C}`,
      offset: -cumulative,
    }
    cumulative += len
    return seg
  })
})

/* ---------- 快捷入口 ---------- */
const quickEntries = ref([
  { label: '新增商品', icon: ShoppingCartOutlined, path: '/goods' },
  { label: '新增订单', icon: FileTextOutlined, path: '/order' },
  { label: '客户管理', icon: TeamOutlined, path: '/customer' },
  { label: '库存查询', icon: DatabaseOutlined, path: '/inventory' },
  { label: '采购申请', icon: ImportOutlined, path: '/purchase' },
  { label: '证书管理', icon: SafetyCertificateOutlined, path: '/certificate' },
  { label: '数据报表', icon: BarChartOutlined, path: '/report' },
  { label: '营销活动', icon: GiftOutlined, path: '/marketing' },
])

/* ---------- 最新订单 ---------- */
const orderColumns = [
  { title: '订单号', dataIndex: 'no', key: 'no', width: 140, ellipsis: true },
  { title: '客户名称', dataIndex: 'customer', key: 'customer', width: 90, ellipsis: true },
  { title: '订单金额', dataIndex: 'amount', key: 'amount', width: 100, align: 'right' },
  { title: '订单状态', dataIndex: 'status', key: 'status', width: 90, align: 'center' },
  { title: '下单时间', dataIndex: 'time', key: 'time', width: 150, ellipsis: true },
  { title: '操作', key: 'action', width: 60, align: 'center' },
]

const latestOrders = ref<any[]>([])

const statusMap = orderStatusMap as Record<string, { color: string; text: string }>
const getStatusColor = (status: string) => statusMap[String(status || '').toLowerCase()]?.color || 'default'
const getStatusText = (status: string) => statusMap[String(status || '').toLowerCase()]?.text || status

/* ---------- 待办事项 ---------- */
const todoItems = ref<any[]>([])

/* ---------- 系统通知 ---------- */
const notices = ref<any[]>([])

/* ---------- 响应式 ---------- */
const isSmallScreen = ref(false)
const checkScreen = () => {
  isSmallScreen.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreen()
  loadDashboard()
  window.addEventListener('resize', checkScreen)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreen)
})
</script>

<style lang="less" scoped>
@import '@/styles/variables.less';

.welcome-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: @spacing-lg;
  gap: @spacing-md;

  @media (max-width: @screen-md) {
    flex-direction: column;
    align-items: flex-start;
  }

  h2 {
    font-size: @font-size-lg;
    font-weight: 600;
    color: @text-primary;
    margin-bottom: 4px;
    word-break: break-all;
  }

  .welcome-date {
    font-size: @font-size-sm;
    color: @text-hint;
  }
}

/* 图表占位 */
.chart-placeholder {
  height: 240px;
  display: flex;
  align-items: flex-end;
  position: relative;
}

.chart-empty {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
  font-size: 13px;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  font-size: 11px;
  color: @text-hint;
  padding-right: 12px;
  min-width: 56px;
  text-align: right;
}

.chart-area {
  flex: 1;
  height: 100%;
  position: relative;
}

.chart-svg {
  width: 100%;
  height: calc(100% - 24px);
}

.chart-tooltip {
  position: absolute;
  top: 35%;
  left: 48%;
  background: #333;
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  pointer-events: none;

  &::after {
    content: '';
    position: absolute;
    bottom: -4px;
    left: 50%;
    transform: translateX(-50%);
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid #333;
  }
}

.chart-x-axis {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: @text-hint;
  padding-top: 8px;
}

/* 环形图 */
.donut-wrap {
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-svg {
  width: 160px;
  height: 160px;
}

.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: @font-size-sm;
  color: @text-secondary;

  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    flex-shrink: 0;
  }
}

/* 移动端适配 */
@media (max-width: @screen-md) {
  .welcome-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .chart-placeholder {
    height: 200px;
  }

  .chart-tooltip {
    display: none;
  }

  .donut-svg {
    width: 130px;
    height: 130px;
  }
}

/* 小屏订单卡片 */
.order-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card-item {
  padding: 14px;
  border-radius: @border-radius;
  background: #fafafa;
  border: 1px solid @border-color;
  transition: background 0.2s ease;

  &:hover {
    background: @primary-bg;
  }

  .order-card-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .order-no {
    font-size: @font-size-sm;
    color: @text-secondary;
    font-weight: 500;
  }

  .order-customer {
    font-size: @font-size-sm;
    color: @text-hint;
  }

  .order-amount {
    font-size: @font-size-md;
    font-weight: 700;
    color: @text-primary;
  }

  .order-time {
    font-size: @font-size-xs;
    color: @text-hint;
    margin-top: 8px;
  }
}

/* 右侧栏 */
.dashboard-right-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
