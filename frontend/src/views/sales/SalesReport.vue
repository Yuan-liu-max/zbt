<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>销售报表</h2>
    </div>

    <!-- 筛选条件 -->
    <div class="content-card filter-card">
      <a-form layout="inline" :model="filterForm">
        <a-form-item label="门店">
          <a-select v-model:value="filterForm.storeId" placeholder="全部门店" allow-clear style="width: 150px">
            <a-select-option v-for="item in storeOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
          <a-range-picker v-model:value="filterForm.dateRange" style="width: 240px" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">
            <SearchOutlined /> 查询
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 标签页 -->
    <div class="content-card">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 门店业绩 -->
        <a-tab-pane key="store" tab="门店业绩">
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-label">销售额</div>
              <div class="stat-value gold">¥{{ salesStats.totalAmount.toLocaleString() }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">订单数</div>
              <div class="stat-value">{{ salesStats.orderCount }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">今日销售</div>
              <div class="stat-value">¥{{ salesStats.todayAmount.toLocaleString() }}</div>
            </div>
          </div>

          <!-- 品类分布 -->
          <div class="section-title">品类分布</div>
          <div class="category-chart">
            <div v-for="item in categoryStats" :key="item.name" class="category-bar">
              <div class="bar-label">{{ item.name }}</div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percentage + '%' }" />
              </div>
              <div class="bar-value">{{ item.percentage }}%</div>
              <div class="bar-amount">¥{{ item.value.toLocaleString() }}</div>
            </div>
          </div>
        </a-tab-pane>

        <!-- 员工排行 -->
        <a-tab-pane key="employee" tab="员工排行">
          <a-table
            :columns="rankingColumns"
            :data-source="employeeRanking"
            :pagination="false"
            row-key="rank"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'rank'">
                <span class="rank-badge" :class="'rank-' + record.rank">{{ record.rank }}</span>
              </template>
              <template v-if="column.key === 'salesAmount'">
                <span class="amount">¥{{ record.amount.toLocaleString() }}</span>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 品类结构 -->
        <a-tab-pane key="category" tab="品类结构">
          <div class="category-detail">
            <div v-for="item in categoryStats" :key="item.name" class="category-item">
              <div class="category-info">
                <div class="category-name">{{ item.name }}</div>
                <div class="category-percent">{{ item.percentage }}%</div>
              </div>
              <a-progress :percent="item.percentage" :stroke-color="getCategoryColor(item.name)" />
              <div class="category-amount">¥{{ item.value.toLocaleString() }}</div>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import type { SalesStats, EmployeeRanking } from '@/types/sales'
import { salesApi } from '@/api/sales'
import { storeApi } from '@/api/store'
import { userApi } from '@/api/system'

// 当前标签页
const activeTab = ref('store')

// 门店下拉（GET /stores/all）
const storeOptions = ref<{ id: number; name: string }[]>([])

// 筛选条件
const filterForm = reactive({
  storeId: undefined as number | undefined,
  dateRange: null as any
})

// 统计数据（后端 /sales/stats → {totalAmount, orderCount, todayAmount}）
const salesStats = reactive<SalesStats>({
  totalAmount: 0,
  orderCount: 0,
  todayAmount: 0
})

// 员工排行（后端 /sales/ranking/employees → [{employeeId, amount}]，无姓名）
interface RankingRow extends EmployeeRanking {
  rank: number
  name: string
}
const employeeRanking = ref<RankingRow[]>([])

// 品类统计（后端 /sales/category-structure → [{category, amount}]）
interface CategoryRow {
  name: string
  value: number
  percentage: number
}
const categoryStats = ref<CategoryRow[]>([])

// 排行榜列配置
const rankingColumns = [
  { title: '排名', dataIndex: 'rank', key: 'rank', width: 80, align: 'center' as const },
  { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
  { title: '销售额', dataIndex: 'amount', key: 'salesAmount', width: 150, align: 'right' as const },
]

// 品类颜色
const getCategoryColor = (name: string) => {
  const colors: Record<string, string> = {
    '戒指': '#c8a44d',
    '项链': '#1890ff',
    '手镯': '#52c41a',
    '吊坠': '#faad14',
    '耳饰': '#ff4d4f'
  }
  return colors[name] || '#1890ff'
}

// 当前查询月份（筛选条件优先，否则取当前月）
const getMonth = (): string => {
  if (filterForm.dateRange?.[0]) {
    return filterForm.dateRange[0].format?.('YYYY-MM') || String(filterForm.dateRange[0]).slice(0, 7)
  }
  return new Date().toISOString().slice(0, 7)
}

// 加载数据
const loadData = async () => {
  try {
    const month = getMonth()
    const [stats, ranking, category] = await Promise.all([
      salesApi.getStats(),
      salesApi.getEmployeeRanking(month, 10),
      salesApi.getCategoryStats(month, filterForm.storeId)
    ])
    Object.assign(salesStats, stats)
    const total = category.reduce((sum, item) => sum + Number(item.amount || 0), 0)
    categoryStats.value = category.map((item) => ({
      name: item.category,
      value: Number(item.amount || 0),
      percentage: total > 0 ? Math.round((Number(item.amount || 0) / total) * 100) : 0
    }))
    const userMap = new Map(userOptions.value.map((u) => [u.id, u.name]))
    employeeRanking.value = ranking.map((item, idx) => ({
      ...item,
      rank: idx + 1,
      name: userMap.get(item.employeeId) || `员工${item.employeeId}`
    }))
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

// 用户列表（用于排行姓名映射）
const userOptions = ref<{ id: number; name: string }[]>([])
const loadOptions = async () => {
  try {
    const [stores, users] = await Promise.all([
      storeApi.getAll(),
      userApi.getList({ page: 1, pageSize: 200, roleId: 5 }), // roleId=5 只查导购角色（销售排行用）
    ])
    storeOptions.value = stores.map((s) => ({ id: Number(s.id), name: s.name }))
    userOptions.value = users.list.map((u) => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch {}
}

onMounted(() => {
  loadOptions()
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

.filter-card {
  padding: 16px 24px;
}

.filter-card :deep(.ant-form-item) {
  margin-bottom: 12px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
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
}

.stat-value.gold {
  color: #c8a44d;
}

/* 品类分布 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.category-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.bar-label {
  width: 50px;
  font-size: 13px;
  color: #666;
  text-align: right;
}

.bar-track {
  flex: 1;
  height: 20px;
  background: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #c8a44d, #e8d59a);
  border-radius: 10px;
  transition: width 0.5s ease;
}

.bar-value {
  width: 40px;
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.bar-amount {
  width: 100px;
  font-size: 13px;
  color: #999;
  text-align: right;
}

/* 员工排行 */
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

/* 品类详情 */
.category-detail {
  max-width: 600px;
}

.category-item {
  margin-bottom: 20px;
}

.category-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.category-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.category-percent {
  font-size: 14px;
  font-weight: 600;
  color: #c8a44d;
}

.category-amount {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
  }

  .filter-card {
    padding: 12px 16px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-value {
    font-size: 20px;
  }

  .category-bar {
    flex-wrap: wrap;
  }

  .bar-amount {
    width: 100%;
    text-align: left;
    margin-top: 4px;
  }
}

@media (max-width: 576px) {
  .page-header h2 {
    font-size: 18px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
