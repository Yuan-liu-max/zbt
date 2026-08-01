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
          <div v-for="cat in analysisCategories" :key="cat" class="category-item" :class="{ active: activeCategory === cat }" @click="activeCategory = cat">
            {{ cat }}
          </div>
        </div>
        <div class="filter-right">
          <span class="filter-label">时间范围</span>
          <a-range-picker v-model:value="dateRange" style="width: 260px" />
          <a-button type="primary" @click="handleAnalyze">
            <RocketOutlined /> 开始分析
          </a-button>
        </div>
      </div>
    </div>

    <!-- 建议列表 -->
    <div class="content-card">
      <div class="suggestion-list">
        <div v-for="item in filteredSuggestions" :key="item.id" class="suggestion-item">
          <div class="suggestion-header">
            <div class="suggestion-title">
              <span class="title-text">{{ item.title }}</span>
              <a-tag :color="item.priority === 'high' ? 'red' : item.priority === 'medium' ? 'orange' : 'blue'" size="small">
                影响程度：{{ item.priority === 'high' ? '高' : item.priority === 'medium' ? '中' : '低' }}
              </a-tag>
            </div>
            <span class="suggestion-date">{{ item.date }}</span>
          </div>
          <div class="suggestion-body">
            <div class="suggestion-row">
              <span class="label">预期效果</span>
              <span class="value">{{ item.impact }}</span>
            </div>
            <div class="suggestion-row">
              <span class="label">ROI</span>
              <span class="value highlight">{{ item.roi }}</span>
            </div>
            <div class="suggestion-row">
              <span class="label">详细建议</span>
              <span class="value">{{ item.description }}</span>
            </div>
          </div>
          <div class="suggestion-footer">
            <a class="view-link">查看详情</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { RocketOutlined } from '@ant-design/icons-vue'
import type { AiSuggestion } from '@/types/ai-tools'
import { mockSuggestions } from '@/api/ai'

const activeCategory = ref('库存优化分析')
const dateRange = ref<any>(null)

const analysisCategories = ['库存优化分析', '销售分析', '客户分析', '供应链分析', '数据分析', '绩效分析']

const filteredSuggestions = computed(() => {
  return mockSuggestions
})

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

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .filter-row { flex-direction: column; align-items: flex-start; }
  .filter-right { width: 100%; flex-wrap: wrap; }
}
</style>
