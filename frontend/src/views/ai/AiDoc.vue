<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>文档生成</h2>
        <p class="page-desc">基于提示词模板 + 填写内容，AI 生成文档</p>
      </div>
    </div>

    <!-- 步骤条 -->
    <div class="content-card">
      <a-steps :current="currentStep" size="small">
        <a-step title="选择模板" />
        <a-step title="填写内容" />
        <a-step title="生成文档" />
      </a-steps>
    </div>

    <!-- 步骤0：选择模板 -->
    <div v-if="currentStep === 0" class="content-card">
      <div class="template-header">
        <span class="section-title">选择模板</span>
        <div class="category-tabs">
          <a-radio-group v-model:value="activeCategory" button-style="solid" size="small">
            <a-radio-button v-for="cat in categories" :key="cat.value" :value="cat.value">
              {{ cat.label }}
            </a-radio-button>
          </a-radio-group>
        </div>
      </div>

      <div v-if="templates.length === 0" class="empty-tip">暂无模板，请先在后台配置提示词模板</div>
      <div v-else class="template-grid">
        <div v-for="tpl in filteredTemplates" :key="tpl.id" class="template-card" @click="handleSelectTemplate(tpl)">
          <div class="template-icon" :class="tpl.businessType">
            <FileTextOutlined />
          </div>
          <div class="template-info">
            <div class="template-name">{{ tpl.name }}</div>
            <div class="template-desc">{{ tpl.description }}</div>
          </div>
          <div class="template-meta">
            <span class="type-label">{{ tpl.typeLabel }}</span>
            <span class="template-actions">
              <a-button type="link" size="small" @click.stop="handleViewDetail(tpl)">查看</a-button>
              <a-button type="link" size="small" @click.stop="handleSelectTemplate(tpl)">使用</a-button>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 步骤1：填写内容 -->
    <div v-if="currentStep === 1" class="content-card">
      <div class="gen-header">
        <span class="section-title">填写内容</span>
        <a @click="goStep(0)">重新选择模板</a>
      </div>
      <div class="selected-template" v-if="selectedTemplate">
        <span class="selected-name">已选模板：{{ selectedTemplate.name }}</span>
        <span class="selected-type">{{ selectedTemplate.typeLabel }}</span>
      </div>
      <a-textarea
        v-model:value="genContent"
        :rows="6"
        placeholder="请输入需要生成文档的内容要点，例如：2026年Q2门店经营分析报告，包含销售、库存、人员、活动效果四个方面，各给出结论与改进建议。"
      />
      <div class="step-actions">
        <a-button @click="goStep(0)">上一步</a-button>
        <a-button type="primary" :loading="generating" @click="handleGenerate">生成文档</a-button>
      </div>
    </div>

    <!-- 步骤2：生成结果 -->
    <div v-if="currentStep === 2" class="content-card">
      <div class="gen-header">
        <span class="section-title">生成结果</span>
        <a @click="goStep(1)">返回修改</a>
      </div>
      <div v-if="generating" class="generating-tip">
        <a-spin /> 正在生成文档，请稍候...
      </div>
      <pre v-else class="generated-content">{{ generatedContent || '暂无生成结果' }}</pre>
      <div class="step-actions" v-if="!generating">
        <a-button @click="handleCopyContent">复制内容</a-button>
        <a-button type="primary" @click="handleGenerate">重新生成</a-button>
      </div>
    </div>

    <!-- 模板详情弹窗 -->
    <a-modal v-model:open="detailVisible" :title="detailTemplate?.name || '模板详情'" :footer="null" width="640">
      <pre class="template-content">{{ detailTemplate?.content || '暂无内容' }}</pre>
      <a-button type="primary" block class="copy-btn" @click="handleCopyTemplate">复制内容</a-button>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { FileTextOutlined } from '@ant-design/icons-vue'
import type { DocTemplate, TemplateCategory } from '@/types/ai-tools'
import { aiApi } from '@/api/ai'

const currentStep = ref(0)
const activeCategory = ref<TemplateCategory>('all')
const templates = ref<DocTemplate[]>([])
const selectedTemplate = ref<DocTemplate | null>(null)
const genContent = ref('')
const generating = ref(false)
const generatedContent = ref('')
const detailVisible = ref(false)
const detailTemplate = ref<{ name: string; content: string } | null>(null)

// 业务类型文案（与后端 prompt_template.businessType 对齐）
const businessTypeLabelMap: Record<string, string> = {
  EMPLOYEE: '员工分析', PRODUCT: '货品分析', SCENE: '场景分析', TASK: '任务相关',
}

const categories: { label: string; value: TemplateCategory }[] = [
  { label: '全部', value: 'all' },
  { label: '员工分析', value: 'EMPLOYEE' },
  { label: '货品分析', value: 'PRODUCT' },
  { label: '场景分析', value: 'SCENE' },
  { label: '任务相关', value: 'TASK' },
]

const filteredTemplates = computed(() => {
  if (activeCategory.value === 'all') return templates.value
  return templates.value.filter(t => t.businessType === activeCategory.value)
})

const loadTemplates = async () => {
  try {
    const list = await aiApi.getPromptTemplates()
    templates.value = (list || []).map((r: any) => {
      const bt = r.businessType || 'OTHER'
      return {
        id: String(r.id),
        name: r.templateName || `模板${r.id}`,
        description: (r.promptContent || '').slice(0, 60),
        businessType: bt,
        content: r.promptContent || '',
        typeLabel: businessTypeLabelMap[bt] || '其他',
      } as DocTemplate
    })
  } catch (e) {
    templates.value = []
    message.error('模板加载失败')
  }
}

const goStep = (s: number) => { currentStep.value = s }

// 选择模板并进入填写内容
const handleSelectTemplate = (tpl: DocTemplate) => {
  selectedTemplate.value = tpl
  genContent.value = ''
  currentStep.value = 1
}

// 查看模板内容
const handleViewDetail = (tpl: DocTemplate) => {
  detailTemplate.value = { name: tpl.name, content: tpl.content }
  detailVisible.value = true
}

// 生成文档：调后端 /ai/doc/generate
const handleGenerate = async () => {
  if (!selectedTemplate.value) { message.warning('请先选择模板'); return }
  if (!genContent.value.trim()) { message.warning('请填写内容要点'); return }
  generating.value = true
  try {
    const res = await aiApi.generateDoc({
      templateId: Number(selectedTemplate.value.id),
      content: genContent.value,
    })
    generatedContent.value = res.content || '生成结果为空'
    currentStep.value = 2
  } catch (e: any) {
    message.error(e?.message || '文档生成失败')
  } finally {
    generating.value = false
  }
}

const handleCopyContent = async () => {
  if (!generatedContent.value) return
  try {
    await navigator.clipboard.writeText(generatedContent.value)
    message.success('已复制到剪贴板')
  } catch (e) { message.error('复制失败') }
}

const handleCopyTemplate = async () => {
  if (!detailTemplate.value) return
  try {
    await navigator.clipboard.writeText(detailTemplate.value.content)
    message.success('已复制到剪贴板')
  } catch (e) { message.error('复制失败') }
}

onMounted(() => {
  loadTemplates()
})
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
.template-icon.EMPLOYEE { background: #e6f7ff; color: #1890ff; }
.template-icon.PRODUCT { background: #f6ffed; color: #52c41a; }
.template-icon.SCENE { background: #fff7e6; color: #fa8c16; }
.template-icon.TASK { background: #f9f0ff; color: #722ed1; }
.template-icon.OTHER { background: #f5f5f5; color: #666; }
.template-info { flex: 1; }
.template-name { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 6px; }
.template-desc { font-size: 12px; color: #999; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.template-meta { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #f5f5f5; }
.type-label { font-size: 12px; color: #999; background: #f5f5f5; padding: 2px 8px; border-radius: 4px; }
.template-actions { white-space: nowrap; }
.template-content { max-height: 480px; overflow: auto; white-space: pre-wrap; word-break: break-all; background: #fafafa; padding: 16px; border-radius: 8px; font-size: 13px; line-height: 1.7; }
.copy-btn { margin-top: 16px; }
.empty-tip { padding: 40px 0; text-align: center; color: #999; }

/* 步骤1/2：填写内容 / 生成结果 */
.gen-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.gen-header a { font-size: 13px; color: #c8a44d; }
.selected-template { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #fafafa; border-radius: 8px; margin-bottom: 16px; }
.selected-name { font-weight: 600; color: #333; }
.selected-type { font-size: 12px; color: #999; background: #f5f5f5; padding: 2px 8px; border-radius: 4px; }
.step-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.step-actions .ant-btn-primary { background: #c8a44d; border-color: #c8a44d; }
.step-actions .ant-btn-primary:hover { background: #b8943d; border-color: #b8943d; }
.generating-tip { display: flex; align-items: center; gap: 12px; padding: 40px 0; justify-content: center; color: #999; }
.generated-content { max-height: 480px; overflow: auto; white-space: pre-wrap; word-break: break-all; background: #fafafa; padding: 16px; border-radius: 8px; font-size: 13px; line-height: 1.7; }

@media (max-width: 992px) { .template-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .template-grid { grid-template-columns: 1fr; }
  .template-header { flex-direction: column; align-items: flex-start; }
}
</style>
