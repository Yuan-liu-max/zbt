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
          <a-space v-if="activeType !== '员工分析'">
            <span class="filter-label">分析门店</span>
            <a-select v-model:value="storeId" placeholder="选择门店" style="width: 140px">
              <a-select-option v-for="s in storeOptions" :key="s.id" :value="String(s.id)">{{ s.name }}</a-select-option>
            </a-select>
          </a-space>
          <a-space v-else>
            <span class="filter-label">分析员工</span>
            <a-select v-model:value="employeeId" placeholder="选择员工" style="width: 140px">
              <a-select-option v-for="u in employeeOptions" :key="u.id" :value="String(u.id)">{{ u.name }}</a-select-option>
            </a-select>
          </a-space>
          <a-button type="primary" :loading="analyzing" @click="handleAnalyze">
            <RocketOutlined /> 开始分析
          </a-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div v-if="hasReport" class="stats-row">
      <div class="stat-card blue">
        <div class="stat-label">销售额</div>
        <div class="stat-value">¥ {{ report.totalSales.toLocaleString() }}</div>
      </div>
      <div class="stat-card green">
        <div class="stat-label">订单数</div>
        <div class="stat-value">{{ report.orderCount.toLocaleString() }}</div>
      </div>
      <div class="stat-card purple">
        <div class="stat-label">客户数</div>
        <div class="stat-value">{{ report.customerCount }}</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-label">客单价</div>
        <div class="stat-value">¥ {{ report.avgOrderAmount.toLocaleString() }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div v-if="hasReport" class="charts-row">
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

    <!-- 空态 / 加载 -->
    <div class="content-card">
      <a-spin v-if="loading" class="spin-center" />
      <a-empty v-else-if="!hasReport" description="暂无分析报告，点击「开始分析」生成门店综合报告" />
      <template v-else>
        <div class="report-meta">
          <a-tag color="green">最新报告</a-tag>
          <span>业务类型：{{ latestResult?.businessType }}</span>
          <span>关联对象：{{ latestResult?.relatedId }}</span>
          <span>模型：{{ latestResult?.modelName || '-' }}</span>
          <span>生成时间：{{ (latestResult?.createdAt || '').slice(0, 19) }}</span>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { RocketOutlined } from '@ant-design/icons-vue'
import type { Dayjs } from 'dayjs'
import { aiApi } from '@/api/ai'
import { userApi } from '@/api/system'
import request from '@/utils/request'

const activeType = ref('门店综合')
const dateRange = ref<Dayjs[] | null>(null)
const storeId = ref('')
const employeeId = ref('')
const storeOptions = ref<{ id: number; name: string }[]>([])
const employeeOptions = ref<{ id: number; name: string }[]>([])
const loading = ref(false)
const analyzing = ref(false)
const latestResult = ref<any>(null)
const analysisTypes = ['门店综合', '员工分析', '货品分析', '场景分析']

const emptyReport = {
  totalSales: 0,
  orderCount: 0,
  customerCount: 0,
  avgOrderAmount: 0,
  salesTrend: [] as { date: string; value: number }[],
  channelBreakdown: [] as { name: string; value: number; color: string }[],
}

const report = reactive({ ...emptyReport })
const hasReport = computed(() => latestResult.value != null)

const getBarHeight = (value: number) => {
  const arr = report.salesTrend.map(i => i.value)
  if (arr.length === 0) return 0
  const max = Math.max(...arr, 1)
  return (value / max) * 100
}

// 解析 AI 输出中的结构化字段（防御性解析，字段缺失用默认值）
const parseReport = (item: any) => {
  Object.assign(report, emptyReport)
  latestResult.value = item
  let data: any = null
  if (item.outputJson) {
    try {
      data = typeof item.outputJson === 'string' ? JSON.parse(item.outputJson) : item.outputJson
    } catch (e) {
      data = null
    }
  }
  if (data) {
    report.totalSales = Number(data.totalSales ?? data.salesAmount ?? data.amount ?? 0)
    report.orderCount = Number(data.orderCount ?? data.orders ?? 0)
    report.customerCount = Number(data.customerCount ?? data.customers ?? 0)
    report.avgOrderAmount = Number(data.avgOrderAmount ?? 0)
    report.salesTrend = Array.isArray(data.salesTrend)
      ? data.salesTrend.map((t: any) => ({ date: String(t.date ?? t.label ?? ''), value: Number(t.value ?? 0) }))
      : []
    report.channelBreakdown = Array.isArray(data.channelBreakdown)
      ? data.channelBreakdown.map((c: any, i: number) => ({
          name: String(c.name ?? '渠道' + (i + 1)),
          value: Number(c.value ?? 0),
          color: c.color || ['#1890ff', '#52c41a', '#fa8c16', '#722ed1'][i % 4],
        }))
      : []
  }
}

const loadReport = async () => {
  loading.value = true
  try {
    const results = await aiApi.getResults()
    // 后端 /ai/results 无日期参数，时间范围在前端过滤
    let filtered = results || []
    if (dateRange.value?.[0] && dateRange.value?.[1]) {
      const start = dateRange.value[0].format('YYYY-MM-DD')
      const end = dateRange.value[1].format('YYYY-MM-DD')
      filtered = filtered.filter(r => {
        const d = (r.createdAt || '').slice(0, 10)
        return d >= start && d <= end
      })
    }
    const success = filtered.find(r => r.status === 'SUCCESS')
    if (success) {
      parseReport(success)
    } else {
      latestResult.value = null
    }
  } catch (e) {
    latestResult.value = null
  } finally {
    loading.value = false
  }
}

const loadStores = async () => {
  try {
    const list: any[] = await request.get('/stores/all')
    storeOptions.value = (list || []).map(s => ({ id: Number(s.id), name: s.name || `门店${s.id}` }))
  } catch (e) {
    storeOptions.value = []
  }
}

const loadEmployees = async () => {
  try {
    const res: any = await userApi.getList({ page: 1, pageSize: 200 })
    employeeOptions.value = (res.list || []).map((u: any) => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch (e) {
    employeeOptions.value = []
  }
}

let pollTimer: number | null = null
let pollTries = 0
let pendingBeforeId = 0

const pollReport = async () => {
  try {
    const results = await aiApi.getResults()
    const hasNew = (results || []).some(r => Number(r.id) > pendingBeforeId)
    if (hasNew) {
      await loadReport()
      return
    }
  } catch (e) { /* 忽略轮询错误 */ }
  if (pollTries >= 40) return
  pollTries++
  pollTimer = window.setTimeout(pollReport, 2000)
}

const handleAnalyze = async () => {
  if (analyzing.value) return
  const typeMap: Record<string, { type: string; id: number } | null> = {
    '门店综合': storeId.value ? { type: 'store', id: Number(storeId.value) } : null,
    '员工分析': employeeId.value ? { type: 'employee', id: Number(employeeId.value) } : null,
    '货品分析': storeId.value ? { type: 'product', id: Number(storeId.value) } : null,
    '场景分析': storeId.value ? { type: 'scene', id: Number(storeId.value) } : null,
  }
  const target = typeMap[activeType.value]
  if (!target) {
    message.warning(activeType.value === '员工分析' ? '请选择要分析的员工' : '请选择要分析的门店')
    return
  }
  analyzing.value = true
  try {
    const results = await aiApi.getResults()
    pendingBeforeId = results.reduce((m, s) => Math.max(m, Number(s.id) || 0), 0)
    pollTries = 0
    await aiApi.getAdvice(target.type, target.id)
    message.success('分析已触发，生成完成后自动刷新')
    pollReport()
  } catch (e: any) {
    message.error(e?.message || '分析触发失败')
  } finally {
    analyzing.value = false
  }
}

onMounted(() => {
  loadReport()
  loadStores()
  loadEmployees()
})

onBeforeUnmount(() => {
  if (pollTimer) clearTimeout(pollTimer)
})
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
.spin-center { display: flex; justify-content: center; padding: 40px 0; }
.report-meta { display: flex; flex-wrap: wrap; gap: 12px; font-size: 13px; color: #666; }

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
