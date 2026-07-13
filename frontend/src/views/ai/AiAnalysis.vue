<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>数据分析</h2>
        <p class="page-desc">智能分析业务数据，生成可视化报表</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="content-card">
      <div class="filter-row">
        <div class="category-list">
          <div v-for="cat in analysisTypes" :key="cat" class="category-item" :class="{ active: activeType === cat }" @click="activeType = cat">
            {{ cat }}
          </div>
        </div>
        <div class="filter-right">
          <a-space>
            <span class="filter-label">时间范围</span>
            <a-range-picker v-model:value="dateRange" style="width: 240px" />
          </a-space>
          <a-space>
            <span class="filter-label">分析门店</span>
            <a-select v-model:value="storeId" placeholder="全部门店" style="width: 140px">
              <a-select-option value="all">全部门店</a-select-option>
              <a-select-option value="1">深圳总店</a-select-option>
              <a-select-option value="2">北京旗舰店</a-select-option>
            </a-select>
          </a-space>
          <a-button type="primary" @click="handleAnalyze">
            <RocketOutlined /> 开始分析
          </a-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card blue">
        <div class="stat-label">销售额</div>
        <div class="stat-value">¥ {{ report.totalSales.toLocaleString() }}</div>
        <div class="stat-trend up">较上月 ↑ 5.2%</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">订单数</div>
        <div class="stat-value">{{ report.orderCount.toLocaleString() }}</div>
        <div class="stat-trend up">较上月 ↑ 3.8%</div>
      </div>
      <div class="stat-card purple">
        <div class="stat-label">客户数</div>
        <div class="stat-value">{{ report.customerCount }}</div>
        <div class="stat-trend up">较上月 ↑ 2.5%</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">客单价</div>
        <div class="stat-value">¥ {{ report.avgOrderAmount.toLocaleString() }}</div>
        <div class="stat-trend down">较上月 ↓ 1.2%</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="content-card chart-card">
        <div class="chart-title">销售趋势分布</div>
        <div class="simple-chart">
          <div v-for="(item, index) in report.salesTrend" :key="index" class="chart-bar-group">
            <div class="chart-bar" :style="{ height: getBarHeight(item.value) + '%' }"></div>
            <div class="bar-label">{{ item.date }}</div>
          </div>
        </div>
      </div>
      <div class="content-card chart-card">
        <div class="chart-title">销售渠道分布</div>
        <div class="channel-list">
          <div v-for="item in report.channelBreakdown" :key="item.name" class="channel-item">
            <div class="channel-info">
              <span class="channel-dot" :style="{ background: item.color }"></span>
              <span class="channel-name">{{ item.name }}</span>
              <span class="channel-value">{{ item.value }}%</span>
            </div>
            <a-progress :percent="item.value" :stroke-color="item.color" :show-info="false" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { RocketOutlined } from '@ant-design/icons-vue'
import type { AnalysisReport } from '@/types/ai-tools'
import { mockAnalysisReport } from '@/api/mock/ai-tools'

const activeType = ref('销售数据分析')
const dateRange = ref<any>(null)
const storeId = ref('all')
const analysisTypes = ['销售数据分析', '客户分析', '货品分析', '营销分析', '财务分析']

const report = reactive<AnalysisReport>({ ...mockAnalysisReport })

const getBarHeight = (value: number) => {
  const max = Math.max(...report.salesTrend.map(i => i.value))
  return (value / max) * 100
}

const handleAnalyze = () => {
  message.success('正在分析数据，请稍候...')
}
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-desc { margin: 4px 0 0; font-size: 13px; color: #999; }
.content-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; }
.filter-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; }
.category-list { display: flex; gap: 8px; flex-wrap: wrap; }
.category-item { padding: 6px 16px; border-radius: 20px; font-size: 13px; cursor: pointer; border: 1px solid #d9d9d9; transition: all 0.2s; color: #666; }
.category-item:hover { border-color: #c8a44d; color: #c8a44d; }
.category-item.active { background: #c8a44d; color: #fff; border-color: #c8a44d; }
.filter-right { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.filter-label { font-size: 13px; color: #666; white-space: nowrap; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; }
.stat-label { font-size: 13px; color: #999; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
.stat-card.blue .stat-value { color: #1890ff; }
.stat-card.green .stat-value { color: #52c41a; }
.stat-card.purple .stat-value { color: #722ed1; }
.stat-card.orange .stat-value { color: #fa8c16; }
.stat-trend { font-size: 12px; }
.stat-trend.up { color: #52c41a; }
.stat-trend.down { color: #ff4d4f; }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-title { font-size: 16px; font-weight: 600; margin-bottom: 20px; }
.simple-chart { display: flex; align-items: flex-end; gap: 12px; height: 200px; padding: 20px 0; }
.chart-bar-group { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; }
.chart-bar { width: 100%; max-width: 40px; background: linear-gradient(180deg, #c8a44d, #e8d59a); border-radius: 4px 4px 0 0; margin-top: auto; transition: height 0.5s; }
.bar-label { font-size: 11px; color: #999; margin-top: 8px; }
.channel-item { margin-bottom: 16px; }
.channel-info { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; font-size: 13px; }
.channel-dot { width: 10px; height: 10px; border-radius: 50%; }
.channel-name { flex: 1; color: #333; }
.channel-value { color: #666; font-weight: 500; }

@media (max-width: 992px) { .stats-row { grid-template-columns: repeat(2, 1fr); } .charts-row { grid-template-columns: 1fr; } }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .filter-row { flex-direction: column; align-items: flex-start; }
  .filter-right { width: 100%; }
  .stats-row { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .stat-value { font-size: 20px; }
}
@media (max-width: 576px) { .stats-row { grid-template-columns: 1fr; } }
</style>
