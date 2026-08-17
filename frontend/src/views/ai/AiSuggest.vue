<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>智能建议</h2>
        <p class="page-desc">基于数据和经验，提供决策建议</p>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="content-card">
      <div class="filter-row">
        <div class="category-list">
          <div v-for="cat in analysisCategories" :key="cat" class="category-item" :class="{ active: activeCategory === cat }" @click="selectCategory(cat)">
            {{ cat }}
          </div>
        </div>
        <div class="filter-right">
          <a-select
            v-if="showStoreSelect"
            v-model:value="selectedStoreId"
            placeholder="请选择门店"
            allow-clear
            style="width: 180px"
            :options="storeOptions"
          />
          <a-select
            v-if="showUserSelect"
            v-model:value="selectedUserId"
            placeholder="请选择员工"
            allow-clear
            style="width: 180px"
            :options="userOptions"
          />
          <span class="filter-label">时间范围</span>
          <a-range-picker v-model:value="dateRange" style="width: 260px" />
          <a-button type="primary" :loading="analyzing" @click="handleAnalyze">
            <RocketOutlined /> 开始分析
          </a-button>
        </div>
      </div>
    </div>

    <!-- 建议列表 -->
    <div class="content-card">
      <div class="suggestion-list" v-if="!loading && pagedSuggestions.length">
        <div v-for="item in pagedSuggestions" :key="item.id" class="suggestion-item" @click="openDetail(item)">
          <div class="suggestion-header">
            <div class="suggestion-title">
              <span class="title-text">{{ getTypeText(item.businessType) }}建议</span>
              <a-tag :color="getStatusText(item).color" size="small">
                {{ getStatusText(item).text }}
              </a-tag>
              <a-tag size="small">对象：{{ getObjectName(item) }}</a-tag>
            </div>
            <span class="suggestion-date">{{ (item.createdAt || '').slice(0, 16) }}</span>
          </div>
          <div class="suggestion-body">
            <div class="suggestion-row">
              <span class="label">分析内容</span>
              <span class="value">{{ formatContent(item.outputText).slice(0, 200) }}</span>
            </div>
          </div>
          <div class="suggestion-footer">
            <a class="view-link" @click.stop="openDetail(item)">查看详情</a>
          </div>
        </div>
        <a-pagination
          v-if="total > pageSize"
          :current="pageNum"
          :page-size="pageSize"
          :total="total"
          :show-total="(t: number) => `共 ${t} 条`"
          style="text-align: right; margin-top: 8px"
          @change="handlePageChange"
        />
      </div>
      <a-empty v-else-if="!loading" description="暂无AI建议记录，点击「开始分析」触发分析" />
      <a-spin v-if="loading" class="spin-center" />
    </div>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="detailVisible" :title="detailItem ? getTypeText(detailItem.businessType) + '建议详情' : ''" :footer="null" width="680">
      <template v-if="detailItem">
        <div class="detail-meta">
          <a-tag :color="getStatusText(detailItem).color">{{ getStatusText(detailItem).text }}</a-tag>
          <span>业务类型：{{ getTypeText(detailItem.businessType) }}</span>
          <span>关联对象：{{ getObjectName(detailItem) }}</span>
          <span>模型：{{ detailItem.modelName || '-' }}</span>
          <span>时间：{{ (detailItem.createdAt || '').slice(0, 19) }}</span>
        </div>
        <pre class="detail-content">{{ formatContent(detailItem.outputText) }}</pre>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { RocketOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import { aiApi } from '@/api/ai'
import { userApi } from '@/api/system'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const activeCategory = ref('全部')
const dateRange = ref<Dayjs[] | null>(null)
const loading = ref(false)
const analyzing = ref(false)
const suggestions = ref<any[]>([])
const detailVisible = ref(false)
const detailItem = ref<any>(null)

const storeOptions = ref<{ value: number; label: string }[]>([])
const userOptions = ref<{ value: number; label: string }[]>([])
const selectedStoreId = ref<number | undefined>(undefined)
const selectedUserId = ref<number | undefined>(undefined)

const analysisCategories = ['全部', '员工分析', '货品分析', '场景分析', '门店综合']

// 统一分类映射：类型码 -> 中文名
const BUSINESS_TYPE_MAP: Record<string, string> = {
  EMPLOYEE: '员工分析',
  PRODUCT: '货品分析',
  SCENE: '场景分析',
  STORE: '门店综合',
}

const getTypeText = (bt: string) => BUSINESS_TYPE_MAP[bt] || bt || '其他'
const categoryToType = (cat: string) => Object.keys(BUSINESS_TYPE_MAP).find(k => BUSINESS_TYPE_MAP[k] === cat) || ''

const showStoreSelect = computed(() => ['货品分析', '场景分析', '门店综合', '全部'].includes(activeCategory.value))
const showUserSelect = computed(() => ['员工分析'].includes(activeCategory.value))

const pageSize = 10
const pageNum = ref(1)

const filteredSuggestions = computed(() => {
  let list = suggestions.value
  if (activeCategory.value !== '全部') {
    const bt = categoryToType(activeCategory.value)
    list = list.filter((s: any) => s.businessType === bt)
  }
  if (dateRange.value?.[0] && dateRange.value?.[1]) {
    const [start, end] = dateRange.value
    list = list.filter((s: any) => {
      if (!s.createdAt) return false
      const d = dayjs(s.createdAt)
      return !d.isBefore(start, 'day') && !d.isAfter(end, 'day')
    })
  }
  return list
})

const total = computed(() => filteredSuggestions.value.length)
const pagedSuggestions = computed(() => {
  const start = (pageNum.value - 1) * pageSize
  return filteredSuggestions.value.slice(start, start + pageSize)
})

const getObjectName = (s: any) => {
  const map = s.businessType === 'EMPLOYEE' ? userOptions.value : storeOptions.value
  const found = map.find((o: any) => o.value === Number(s.relatedId))
  return found ? found.label : String(s.relatedId ?? '-')
}

const openDetail = (item: any) => {
  detailItem.value = item
  detailVisible.value = true
}

const formatContent = (text: string) => {
  if (!text) return '（暂无内容）'
  try {
    const obj = JSON.parse(text)
    if (obj && typeof obj === 'object') return formatObject(obj)
    return text
  } catch {
    return text
  }
}

const formatObject = (obj: any): string => {
  if (Array.isArray(obj)) {
    return obj.length
      ? obj.map((v) => `• ${typeof v === 'object' ? JSON.stringify(v) : v}`).join('\n')
      : '（无）'
  }
  return Object.entries(obj)
    .map(([key, val]) => {
      if (Array.isArray(val)) {
        return val.length
          ? `${key}：\n${val.map((v) => `• ${typeof v === 'object' ? JSON.stringify(v) : v}`).join('\n')}`
          : `${key}：（无）`
      }
      if (val && typeof val === 'object') {
        return `${key}：\n${formatObject(val)}`
      }
      return `${key}：${val ?? '（无）'}`
    })
    .join('\n\n')
}

const loadStores = async () => {
  try {
    const list: any[] = await request.get('/stores/all')
    storeOptions.value = (list || []).map((s: any) => ({ value: Number(s.id), label: s.name || s.storeName || `门店${s.id}` }))
  } catch (e) { /* 门店加载失败不阻塞 */ }
}

const loadUsers = async () => {
  try {
    const res: any = await userApi.getList({ page: 1, pageSize: 200 })
    userOptions.value = (res.list || []).map((u: any) => ({ value: Number(u.id), label: u.realName || u.username }))
  } catch (e) { /* 员工加载失败不阻塞 */ }
}

const loadSuggestions = async () => {
  loading.value = true
  try {
    suggestions.value = await aiApi.getResults()
  } catch (e) {
    suggestions.value = []
    message.error('建议加载失败')
  } finally {
    loading.value = false
  }
}

let pollTimer: number | null = null
let pollTries = 0
let pendingBeforeId = 0

// 轮询 /ai/results，直到生成新结果（新 id 出现）
const pollResult = async () => {
  await loadSuggestions()
  const latest = suggestions.value[0]
  if (latest && Number(latest.id) > pendingBeforeId) return
  if (pollTries >= 40) return  // 最多约 80 秒
  pollTries++
  pollTimer = window.setTimeout(pollResult, 2000)
}

const handleAnalyze = async () => {
  if (analyzing.value) return
  const cat = activeCategory.value
  const currentUserId = (authStore.userInfo as any)?.userId ?? (authStore.userInfo as any)?.id
  const currentStoreId = (authStore.userInfo as any)?.storeId

  let type = 'store'
  let id: number | undefined
  if (cat === '员工分析') {
    type = 'employee'
    id = selectedUserId.value ?? currentUserId
  } else if (cat === '货品分析') {
    type = 'product'
    id = selectedStoreId.value ?? currentStoreId
  } else if (cat === '场景分析') {
    type = 'scene'
    id = selectedStoreId.value ?? currentStoreId
  } else {
    // 门店综合 / 全部
    type = 'store'
    id = selectedStoreId.value ?? currentStoreId
  }

  if (!id) {
    message.warning('请选择分析对象（门店/员工）')
    return
  }

  analyzing.value = true
  try {
    const maxId = suggestions.value.reduce((m, s) => Math.max(m, Number(s.id) || 0), 0)
    pendingBeforeId = maxId
    pollTries = 0
    await aiApi.getAdvice(type, id)
    message.success('已触发分析，结果生成后会自动刷新')
    pollResult()
  } catch (e: any) {
    message.error(e?.message || '分析触发失败')
  } finally {
    analyzing.value = false
  }
}

const selectCategory = (cat: string) => {
  activeCategory.value = cat
  pageNum.value = 1
}

const handlePageChange = (page: number) => {
  pageNum.value = page
}

const getStatusText = (s: any) => {
  if (s.status === 'SUCCESS') return { text: '已完成', color: 'green' }
  if (s.status === 'FAILED') return { text: '失败', color: 'red' }
  return { text: '分析中', color: 'blue' }
}

watch([activeCategory, dateRange], () => { pageNum.value = 1 })

onMounted(() => {
  loadStores()
  loadUsers()
  loadSuggestions()
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
.category-item {
  padding: 6px 16px; border-radius: 20px; font-size: 13px; cursor: pointer;
  border: 1px solid #d9d9d9; transition: all 0.2s; color: #666;
}
.category-item:hover { border-color: #c8a44d; color: #c8a44d; }
.category-item.active { background: #c8a44d; color: #fff; border-color: #c8a44d; }
.filter-right { display: flex; align-items: center; gap: 12px; }
.filter-label { font-size: 13px; color: #666; white-space: nowrap; }

.suggestion-item {
  border: 1px solid #f0f0f0; border-radius: 10px; padding: 20px;
  margin-bottom: 16px; transition: box-shadow 0.2s;
}
.suggestion-item:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.suggestion-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.suggestion-title { display: flex; align-items: center; gap: 8px; }
.title-text { font-size: 16px; font-weight: 600; color: #333; }
.suggestion-date { font-size: 12px; color: #999; }
.suggestion-body { padding: 16px; background: #fafafa; border-radius: 8px; }
.suggestion-row { display: flex; gap: 12px; margin-bottom: 10px; font-size: 13px; line-height: 1.8; }
.suggestion-row:last-child { margin-bottom: 0; }
.suggestion-row .label { color: #999; white-space: nowrap; min-width: 70px; }
.suggestion-row .value { color: #333; white-space: pre-line; }
.suggestion-row .value.highlight { color: #c8a44d; font-weight: 600; }
.suggestion-footer { margin-top: 12px; text-align: right; }
.view-link { font-size: 13px; color: #1890ff; cursor: pointer; }
.view-link:hover { color: #40a9ff; }
.spin-center { display: flex; justify-content: center; padding: 40px 0; }
.detail-meta { display: flex; flex-wrap: wrap; gap: 12px; font-size: 13px; color: #666; margin-bottom: 16px; }
.detail-content { max-height: 480px; overflow: auto; white-space: pre-wrap; word-break: break-all; background: #fafafa; padding: 16px; border-radius: 8px; font-size: 13px; line-height: 1.7; color: #333; }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .filter-row { flex-direction: column; align-items: flex-start; }
  .filter-right { width: 100%; flex-wrap: wrap; }
}
</style>
