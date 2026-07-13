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
            <span>1,500,000</span>
            <span>1,200,000</span>
            <span>900,000</span>
            <span>600,000</span>
            <span>300,000</span>
            <span>0</span>
          </div>
          <div class="chart-area">
            <svg viewBox="0 0 600 200" preserveAspectRatio="none" class="chart-svg">
              <defs>
                <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#c8a44d" stop-opacity="0.3"/>
                  <stop offset="100%" stop-color="#c8a44d" stop-opacity="0.02"/>
                </linearGradient>
              </defs>
              <!-- 面积 -->
              <path d="M0,160 C60,150 120,100 180,90 C240,80 300,120 360,60 C420,20 480,40 540,30 L600,20 L600,200 L0,200 Z" fill="url(#areaGrad)"/>
              <!-- 线 -->
              <path d="M0,160 C60,150 120,100 180,90 C240,80 300,120 360,60 C420,20 480,40 540,30 L600,20" fill="none" stroke="#c8a44d" stroke-width="2.5" stroke-linecap="round"/>
              <!-- 数据点 -->
              <circle cx="180" cy="90" r="4" fill="#c8a44d"/>
            </svg>
            <div class="chart-tooltip">
              <strong>05-13</strong><br/>
              销售额：¥1,125,000
            </div>
            <div class="chart-x-axis">
              <span>05-01</span>
              <span>05-05</span>
              <span>05-09</span>
              <span>05-13</span>
              <span>05-17</span>
              <span>05-20</span>
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
          <svg viewBox="0 0 200 200" class="donut-svg">
            <!-- 门店销售 45% -->
            <circle cx="100" cy="100" r="80" fill="none" stroke="#c8a44d" stroke-width="28"
              stroke-dasharray="226.2 502.65" stroke-dashoffset="0" transform="rotate(-90 100 100)"/>
            <!-- 线上商城 30% -->
            <circle cx="100" cy="100" r="80" fill="none" stroke="#e8d59a" stroke-width="28"
              stroke-dasharray="150.8 502.65" stroke-dashoffset="-226.2" transform="rotate(-90 100 100)"/>
            <!-- 客户定制 15% -->
            <circle cx="100" cy="100" r="80" fill="none" stroke="#bfa76a" stroke-width="28"
              stroke-dasharray="75.4 502.65" stroke-dashoffset="-377" transform="rotate(-90 100 100)"/>
            <!-- 批发销售 10% -->
            <circle cx="100" cy="100" r="80" fill="none" stroke="#8a7340" stroke-width="28"
              stroke-dasharray="50.3 502.65" stroke-dashoffset="-452.4" transform="rotate(-90 100 100)"/>
            <!-- 中心文字 -->
            <text x="100" y="92" text-anchor="middle" font-size="11" fill="#999">总销售额</text>
            <text x="100" y="115" text-anchor="middle" font-size="16" font-weight="bold" fill="#1a1a1a">¥12,345,678</text>
          </svg>
          <div class="donut-legend">
            <div class="legend-item"><span class="dot" style="background:#c8a44d"></span>门店销售 45.00%</div>
            <div class="legend-item"><span class="dot" style="background:#e8d59a"></span>线上商城 30.00%</div>
            <div class="legend-item"><span class="dot" style="background:#bfa76a"></span>客户定制 15.00%</div>
            <div class="legend-item"><span class="dot" style="background:#8a7340"></span>批发销售 10.00%</div>
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
              <a-tag :color="getStatusColor(record.status)">{{ record.status }}</a-tag>
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
              <a-tag :color="getStatusColor(order.status)">{{ order.status }}</a-tag>
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
            <div v-for="(item, idx) in todoItems" :key="idx" class="todo-item">
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
            <div v-for="(item, idx) in notices" :key="idx" class="notice-item">
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
import { ref, onMounted, onUnmounted } from 'vue'
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

/* ---------- 日期 ---------- */
const now = new Date()
const weekdayMap = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const todayStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
const weekdayStr = weekdayMap[now.getDay()]

/* ---------- 统计卡片 ---------- */
const statCards = ref([
  { label: '今日销售额', value: '¥1,234,567', icon: ShoppingCartOutlined, color: 'gold', trend: '12.45%', trendDir: 'up', path: '/sales/report' },
  { label: '今日订单数', value: '126', icon: FileTextOutlined, color: 'blue', trend: '8.00%', trendDir: 'up', path: '/order/list' },
  { label: '新增客户数', value: '32', icon: TeamOutlined, color: 'green', trend: '14.29%', trendDir: 'up', path: '/customer/list' },
  { label: '库存商品数', value: '3,456', icon: DatabaseOutlined, color: 'purple', trend: '3.21%', trendDir: 'down', path: '/inventory/list' },
  { label: '待处理订单', value: '18', icon: AuditOutlined, color: 'orange', trend: '10.00%', trendDir: 'down', path: '/task/list' },
  { label: '证书到期预警', value: '7', icon: SafetyCertificateOutlined, color: 'red', trend: '40.00%', trendDir: 'up', path: '/certificate' },
])

/* ---------- 趋势区间 ---------- */
const trendRange = ref('month')

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

const latestOrders = ref([
  { no: 'DD20240520001', customer: '张女士', amount: 8888, status: '待支付', time: '2024-05-20 10:23:45' },
  { no: 'DD20240520002', customer: '李先生', amount: 15680, status: '待发货', time: '2024-05-20 09:15:32' },
  { no: 'DD20240520003', customer: '王小姐', amount: 3680, status: '待发货', time: '2024-05-20 08:45:18' },
  { no: 'DD20240519001', customer: '陈先生', amount: 22990, status: '已发货', time: '2024-05-19 16:20:11' },
  { no: 'DD20240519002', customer: '刘女士', amount: 6520, status: '已完成', time: '2024-05-19 14:05:33' },
])

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    '待支付': 'orange',
    '待发货': 'gold',
    '已发货': 'blue',
    '已完成': 'green',
  }
  return map[status] || 'default'
}

/* ---------- 待办事项 ---------- */
const todoItems = ref([
  { label: '待处理订单', count: 18, color: 'red' },
  { label: '待审核采购单', count: 7, color: 'orange' },
  { label: '证书即将到期', count: 7, color: 'gold' },
  { label: '库存预警商品', count: 23, color: 'orange' },
  { label: '客户跟进提醒', count: 12, color: 'gold' },
])

/* ---------- 系统通知 ---------- */
const notices = ref([
  { content: '【系统更新】珠宝通系统 V2.3.0 版本已更新', time: '2024-05-20' },
  { content: '【证书预警】有 7 张证书即将到期，请及时处理', time: '2024-05-20' },
  { content: '【库存预警】有 23 个商品库存低于预警值', time: '2024-05-19' },
  { content: '【营销活动】520 钜惠活动已开启，点击查看详情', time: '2024-05-19' },
])

/* ---------- 响应式 ---------- */
const isSmallScreen = ref(false)
const checkScreen = () => {
  isSmallScreen.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreen()
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
