<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>文档生成</h2>
        <p class="page-desc">根据需求生成各类文档，提升工作效率</p>
      </div>
    </div>

    <!-- 步骤条 -->
    <div class="content-card">
      <a-steps :current="currentStep" size="small">
        <a-step title="选择模板" />
        <a-step title="选择内容" />
        <a-step title="生成文档" />
      </a-steps>
    </div>

    <!-- 模板分类 -->
    <div class="content-card">
      <div class="template-header">
        <span class="section-title">选择文档模板</span>
        <div class="category-tabs">
          <a-radio-group v-model:value="activeCategory" button-style="solid" size="small">
            <a-radio-button v-for="cat in categories" :key="cat.value" :value="cat.value">
              {{ cat.label }}
            </a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <!-- 模板列表 -->
      <div class="template-grid">
        <div v-for="tpl in filteredTemplates" :key="tpl.id" class="template-card" @click="handleSelectTemplate(tpl)">
          <div class="template-icon" :class="tpl.category">
            <FileTextOutlined />
          </div>
          <div class="template-info">
            <div class="template-name">{{ tpl.name }}</div>
            <div class="template-desc">{{ tpl.description }}</div>
          </div>
          <div class="template-meta">
            <span class="usage-count">使用 {{ tpl.usageCount }} 次</span>
            <a-button type="link" size="small">使用</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { FileTextOutlined } from '@ant-design/icons-vue'
import type { DocTemplate, TemplateCategory } from '@/types/ai-tools'
import { mockTemplates } from '@/api/mock/ai-tools'

const currentStep = ref(0)
const activeCategory = ref<TemplateCategory>('all')

const categories = [
  { label: '全部模板', value: 'all' as TemplateCategory },
  { label: '合同协议', value: 'contract' as TemplateCategory },
  { label: '报告文案', value: 'report' as TemplateCategory },
  { label: '申请表单', value: 'form' as TemplateCategory },
  { label: '通知公告', value: 'notice' as TemplateCategory },
  { label: '其他模板', value: 'other' as TemplateCategory },
]

const filteredTemplates = computed(() => {
  if (activeCategory.value === 'all') return mockTemplates
  return mockTemplates.filter(t => t.category === activeCategory.value)
})

const handleSelectTemplate = (tpl: DocTemplate) => {
  message.info(`使用模板：${tpl.name}`)
}
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.page-desc { margin: 4px 0 0; font-size: 13px; color: #999; }
.content-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: #333; }
.template-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.category-tabs :deep(.ant-radio-button-wrapper) { border-color: #d9d9d9; }
.category-tabs :deep(.ant-radio-button-wrapper-checked) { border-color: #c8a44d; background: #c8a44d; color: #fff; }
.template-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.template-card {
  border: 1px solid #f0f0f0; border-radius: 10px; padding: 20px;
  cursor: pointer; transition: all 0.3s; display: flex; flex-direction: column; gap: 12px;
}
.template-card:hover { border-color: #c8a44d; box-shadow: 0 4px 12px rgba(200,164,77,0.15); transform: translateY(-2px); }
.template-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.template-icon.contract { background: #e6f7ff; color: #1890ff; }
.template-icon.report { background: #f6ffed; color: #52c41a; }
.template-icon.form { background: #fff7e6; color: #fa8c16; }
.template-icon.notice { background: #f9f0ff; color: #722ed1; }
.template-icon.other { background: #f5f5f5; color: #666; }
.template-info { flex: 1; }
.template-name { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 6px; }
.template-desc { font-size: 12px; color: #999; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.template-meta { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #f5f5f5; }
.usage-count { font-size: 12px; color: #999; }
@media (max-width: 992px) { .template-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .template-grid { grid-template-columns: 1fr; }
  .template-header { flex-direction: column; align-items: flex-start; }
}
</style>
