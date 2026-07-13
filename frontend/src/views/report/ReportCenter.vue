<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据报表</h2>
    </div>

    <!-- 筛选条件 -->
    <div class="content-card filter-card">
      <div class="filter-left">
        <a-range-picker
          v-model:value="dateRange"
          style="width: 260px"
        />
        <a-select v-model:value="storeId" placeholder="全部门店" allow-clear style="width: 150px">
          <a-select-option value="1">深圳总店</a-select-option>
          <a-select-option value="2">北京旗舰店</a-select-option>
          <a-select-option value="3">上海中心店</a-select-option>
        </a-select>
      </div>
      <a-button type="primary" @click="handleExport">
        <DownloadOutlined /> 导出报表
      </a-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">销售额（元）</div>
          <div class="stat-value">{{ reportStats.totalSales.toLocaleString() }}</div>
          <div class="stat-trend up">较上月 ↑ {{ reportStats.salesChange }}%</div>
        </div>
        <div class="stat-icon blue"><BarChartOutlined /></div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">订单数（笔）</div>
          <div class="stat-value">{{ reportStats.totalOrders.toLocaleString() }}</div>
          <div class="stat-trend up">较上月 ↑ {{ reportStats.ordersChange }}%</div>
        </div>
        <div class="stat-icon green"><FileTextOutlined /></div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">客户数（人）</div>
          <div class="stat-value">{{ reportStats.totalCustomers.toLocaleString() }}</div>
          <div class="stat-trend up">较上月 ↑ {{ reportStats.customersChange }}%</div>
        </div>
        <div class="stat-icon purple"><TeamOutlined /></div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">客单价（元）</div>
          <div class="stat-value">{{ reportStats.avgOrderAmount.toFixed(2) }}</div>
          <div class="stat-trend up">较上月 ↑ {{ reportStats.avgOrderChange }}%</div>
        </div>
        <div class="stat-icon orange"><TransactionOutlined /></div>
      </div>
      <div class="stat-card">
        <div class="stat-info">
          <div class="stat-label">毛利额（元）</div>
          <div class="stat-value">{{ reportStats.grossProfit.toLocaleString() }}</div>
          <div class="stat-trend up">较上月 ↑ {{ reportStats.profitChange }}%</div>
        </div>
        <div class="stat-icon cyan"><RiseOutlined /></div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 销售趋势 -->
      <div class="content-card chart-card">
        <div class="chart-header">
          <span class="chart-title">销售趋势</span>
          <a-segmented v-model:value="trendType" :options="trendOptions" size="small" />
        </div>
        <div class="chart-legend">
          <span class="legend-item"><span class="legend-dot" style="background:#1890ff" /> 销售额（元）</span>
          <span class="legend-item"><span class="legend-dot" style="background:#52c41a" /> 订单数（笔）</span>
        </div>
        <div class="chart-area">
          <!-- 简化的折线图展示 -->
          <div class="simple-chart">
            <div v-for="(item, index) in salesTrend" :key="index" class="chart-bar-group">
              <div class="chart-bar" :style="{ height: getBarHeight(item.salesAmount) + '%' }">
                <div class="bar-tooltip">¥{{ item.salesAmount.toLocaleString() }}</div>
              </div>
              <div class="bar-label">{{ item.date }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 销售渠道占比 -->
      <div class="content-card chart-card">
        <div class="chart-title">销售渠道占比</div>
        <div class="pie-chart-area">
          <!-- 简化的饼图展示 -->
          <div class="simple-pie">
            <svg viewBox="0 0 100 100" class="pie-svg">
              <circle cx="50" cy="50" r="40" fill="transparent" stroke="#1890ff" stroke-width="20"
                :stroke-dasharray="`${42.5 * 2.513} ${251.3 - 42.5 * 2.513}`"
                stroke-dashoffset="0" />
              <circle cx="50" cy="50" r="40" fill="transparent" stroke="#52c41a" stroke-width="20"
                :stroke-dasharray="`${28.7 * 2.513} ${251.3 - 28.7 * 2.513}`"
                :stroke-dashoffset="`${-42.5 * 2.513}`" />
              <circle cx="50" cy="50" r="40" fill="transparent" stroke="#faad14" stroke-width="20"
                :stroke-dasharray="`${16.4 * 2.513} ${251.3 - 16.4 * 2.513}`"
                :stroke-dashoffset="`${-(42.5 + 28.7) * 2.513}`" />
              <circle cx="50" cy="50" r="40" fill="transparent" stroke="#722ed1" stroke-width="20"
                :stroke-dasharray="`${12.4 * 2.513} ${251.3 - 12.4 * 2.513}`"
                :stroke-dashoffset="`${-(42.5 + 28.7 + 16.4) * 2.513}`" />
            </svg>
          </div>
          <div class="pie-legend">
            <div v-for="item in channelStats" :key="item.name" class="legend-row">
              <span class="legend-color" :style="{ background: item.color }" />
              <span class="legend-name">{{ item.name }}</span>
              <span class="legend-value">{{ item.percentage }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 销售排行榜 -->
    <div class="content-card">
      <div class="ranking-header">
        <span class="section-title">销售排行榜</span>
        <a-tabs v-model:activeKey="rankingTab" size="small">
          <a-tab-pane key="product" tab="商品排行" />
          <a-tab-pane key="store" tab="门店排行" />
          <a-tab-pane key="employee" tab="销售员排行" />
        </a-tabs>
      </div>
      <a-table
        :columns="rankingColumns"
        :data-source="productRanking"
        :pagination="false"
        row-key="rank"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'rank'">
            <span class="rank-badge" :class="'rank-' + record.rank">{{ record.rank }}</span>
          </template>
          <template v-if="column.key === 'salesAmount'">
            <span class="amount">¥{{ record.salesAmount.toLocaleString() }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a class="action-link">查看明细</a>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  DownloadOutlined, BarChartOutlined, FileTextOutlined, TeamOutlined,
  TransactionOutlined, RiseOutlined
} from '@ant-design/icons-vue'
import type { ReportStats, SalesTrend, ChannelStats, RankingItem } from '@/types/report'
import { reportApi } from '@/api/mock/report'

// 标签页
const activeTab = ref('overview')
const rankingTab = ref('product')
const trendType = ref('按天')
const trendOptions = ['按天', '按周', '按月']

// 筛选条件
const dateRange = ref<any>(null)
const storeId = ref<string | undefined>(undefined)

// 统计数据
const reportStats = reactive<ReportStats>({
  totalSales: 0, totalOrders: 0, totalCustomers: 0, avgOrderAmount: 0, grossProfit: 0,
  salesChange: 0, ordersChange: 0, customersChange: 0, avgOrderChange: 0, profitChange: 0
})

const salesTrend = ref<SalesTrend[]>([])
const channelStats = ref<ChannelStats[]>([])
const productRanking = ref<RankingItem[]>([])

// 排行榜列配置
const rankingColumns = [
  { title: '排名', dataIndex: 'rank', key: 'rank', width: 70, align: 'center' as const },
  { title: '商品名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '商品编码', dataIndex: 'code', key: 'code', width: 130 },
  { title: '销售数量', dataIndex: 'quantity', key: 'quantity', width: 100, align: 'center' as const },
  { title: '销售额（元）', dataIndex: 'salesAmount', key: 'salesAmount', width: 140, align: 'right' as const },
  { title: '占比', dataIndex: 'percentage', key: 'percentage', width: 80, align: 'center' as const },
  { title: '操作', key: 'action', width: 100, align: 'center' as const },
]

// 获取柱状图高度
const getBarHeight = (value: number) => {
  const max = Math.max(...salesTrend.value.map(item => item.salesAmount))
  return (value / max) * 100
}

// 导出
const handleExport = () => {
  message.success('导出报表功能开发中...')
}

// 加载数据
const loadData = async () => {
  try {
    const [stats, trend, channel, ranking] = await Promise.all([
      reportApi.getStats(),
      reportApi.getSalesTrend(),
      reportApi.getChannelStats(),
      reportApi.getProductRanking()
    ])
    Object.assign(reportStats, stats)
    salesTrend.value = trend
    channelStats.value = channel
    productRanking.value = ranking
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
}

.tab-card {
  padding: 0 24px;
}

.tab-card :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.filter-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
}

.stat-trend.up { color: #52c41a; }
.stat-trend.down { color: #ff4d4f; }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-icon.blue { background: #e6f7ff; color: #1890ff; }
.stat-icon.green { background: #f6ffed; color: #52c41a; }
.stat-icon.purple { background: #f9f0ff; color: #722ed1; }
.stat-icon.orange { background: #fff7e6; color: #fa8c16; }
.stat-icon.cyan { background: #e6fffb; color: #13c2c2; }

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
  overflow: hidden;
}

.chart-card {
  padding: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-legend {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 12px;
  color: #666;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 12px;
  height: 3px;
  border-radius: 2px;
}

/* 简化折线图 */
.simple-chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 200px;
  padding: 20px 0;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.chart-bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.chart-bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(180deg, #1890ff, #69c0ff);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.5s ease;
  margin-top: auto;
}

.bar-tooltip {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #666;
  white-space: nowrap;
  display: none;
}

.chart-bar:hover .bar-tooltip {
  display: block;
}

.bar-label {
  font-size: 10px;
  color: #999;
  margin-top: 8px;
  white-space: nowrap;
}

/* 简化饼图 */
.pie-chart-area {
  display: flex;
  align-items: center;
  gap: 24px;
}

.simple-pie {
  width: 160px;
  height: 160px;
  flex-shrink: 0;
}

.pie-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.pie-legend {
  flex: 1;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 13px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
  color: #333;
}

.legend-value {
  color: #666;
  font-weight: 500;
}

/* 排行榜 */
.ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.rank-1 { background: #c8a44d; color: #fff; }
.rank-2 { background: #d9d9d9; color: #333; }
.rank-3 { background: #f0d68a; color: #333; }

.amount {
  color: #ff4d4f;
  font-weight: 500;
}

.action-link {
  color: #1890ff;
  cursor: pointer;
  font-size: 13px;
}

.action-link:hover {
  color: #40a9ff;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-container { padding: 16px; }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card { padding: 16px; }
  .stat-value { font-size: 18px; }
  .stat-icon { width: 40px; height: 40px; font-size: 18px; }

  .content-card { padding: 16px; }

  .filter-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-left {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }

  .filter-left :deep(.ant-picker),
  .filter-left :deep(.ant-select) {
    width: 100% !important;
  }

  .charts-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .simple-chart {
    height: 160px;
    gap: 2px;
  }

  .bar-label {
    font-size: 8px;
  }

  .pie-chart-area {
    flex-direction: column;
    align-items: center;
  }

  .pie-legend {
    width: 100%;
  }

  .ranking-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 576px) {
  .page-header h2 { font-size: 18px; }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .stat-card {
    flex-direction: row-reverse;
    justify-content: space-between;
  }

  .simple-chart {
    height: 120px;
  }

  .chart-legend {
    flex-wrap: wrap;
    gap: 8px;
  }

  .legend-item {
    font-size: 11px;
  }

  .section-title {
    font-size: 14px;
  }
}
</style>
