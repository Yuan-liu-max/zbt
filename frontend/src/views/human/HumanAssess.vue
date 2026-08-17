<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>能力考核</h2>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新建考核
      </a-button>
    </div>

    <!-- 搜索表单 -->
    <div class="content-card search-card">
      <a-form layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="考核周期">
          <a-select
            v-model:value="searchForm.assessmentWeek"
            placeholder="请选择"
            allow-clear
            style="width: 180px"
          >
            <a-select-option
              v-for="item in periodOptions"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核人">
          <a-select
            v-model:value="searchForm.assessorId"
            placeholder="请选择考核人"
            allow-clear
            show-search
            :filter-option="filterOption"
            style="width: 180px"
          >
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" html-type="submit">
              <SearchOutlined /> 查询
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 数据表格 -->
    <div class="content-card table-card">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'employeeId'">
            {{ userName(record.employeeId, record.employeeName) }}
          </template>
          <template v-else-if="column.key === 'assessorId'">
            {{ userName(record.assessorId, record.assessorName) }}
          </template>
          <template v-else-if="column.key === 'type'">
            {{ record.type ? assessTypeMap[record.type as AssessType] : '未设置' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-btns">
              <a @click="handleView(record)" class="action-link">查看</a>
              <a-divider type="vertical" />
              <a @click="handleScore(record)" class="action-link">评分</a>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新建考核弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      title="新建考核"
      @ok="handleModalOk"
      :confirm-loading="modalLoading"
      width="560px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="考核员工" name="employeeId">
          <a-select v-model:value="formData.employeeId" placeholder="请选择被考核员工" allow-clear>
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核人" name="assessorId">
          <a-select v-model:value="formData.assessorId" placeholder="请选择考核人" allow-clear>
            <a-select-option v-for="item in userOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择考核类型" allow-clear>
            <a-select-option value="monthly">月度考核</a-select-option>
            <a-select-option value="quarterly">季度考核</a-select-option>
            <a-select-option value="special">专项考核</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="考核周期" name="assessmentWeekRange">
          <a-range-picker
            v-model:value="formData.assessmentWeekRange"
            picker="month"
            value-format="YYYY-MM"
            :placeholder="['开始月份', '结束月份']"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal v-model:open="detailVisible" title="详情" :footer="null" width="600px">
      <a-descriptions :column="2" bordered size="small">
        <a-descriptions-item label="被考核员工">{{ userName(detailRecord?.employeeId, detailRecord?.employeeName) }}</a-descriptions-item>
        <a-descriptions-item label="考核人">{{ userName(detailRecord?.assessorId, detailRecord?.assessorName) }}</a-descriptions-item>
        <a-descriptions-item label="考核周期">{{ detailRecord?.assessmentWeek }}</a-descriptions-item>
        <a-descriptions-item label="考核类型">{{ detailRecord?.type ? assessTypeMap[detailRecord.type as AssessType] : '未设置' }}</a-descriptions-item>
        <a-descriptions-item label="商品知识">{{ detailRecord?.productKnowledgeScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="搭配技巧">{{ detailRecord?.matchingSkillScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="接待能力">{{ detailRecord?.receptionScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="异议处理">{{ detailRecord?.objectionHandlingScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="话术演练">{{ detailRecord?.promotionScriptScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="总分">{{ detailRecord?.totalScore ?? 0 }}</a-descriptions-item>
        <a-descriptions-item label="改进建议" :span="2">{{ detailRecord?.improvementAdvice || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 5维度评分弹窗 -->
    <a-modal v-model:open="scoreVisible" title="能力评分" @ok="handleScoreOk" width="560px">
      <div class="score-record" v-if="scoreRecord">
        <span class="score-record-name">员工：{{ userName(scoreRecord.employeeId, scoreRecord.employeeName) }}</span>
        <span class="score-record-week">{{ scoreRecord.assessmentWeek }}</span>
      </div>
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="考核类型">
          <a-select v-model:value="scoreForm.type" placeholder="请选择考核类型">
            <a-select-option v-for="(text, value) in assessTypeMap" :key="value" :value="value">
              {{ text }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="商品知识">
          <a-input-number v-model:value="scoreForm.productKnowledge" :min="0" :max="25" style="width: 100%" />
        </a-form-item>
        <a-form-item label="搭配技巧">
          <a-input-number v-model:value="scoreForm.matchingSkill" :min="0" :max="20" style="width: 100%" />
        </a-form-item>
        <a-form-item label="接待能力">
          <a-input-number v-model:value="scoreForm.reception" :min="0" :max="20" style="width: 100%" />
        </a-form-item>
        <a-form-item label="异议处理">
          <a-input-number v-model:value="scoreForm.objectionHandling" :min="0" :max="20" style="width: 100%" />
        </a-form-item>
        <a-form-item label="话术演练">
          <a-input-number v-model:value="scoreForm.promotionScript" :min="0" :max="15" style="width: 100%" />
        </a-form-item>
        <a-form-item label="总分">
          <span class="score-total">{{ totalScore }}</span>
        </a-form-item>
        <a-form-item label="改进建议">
          <a-textarea
            v-model:value="scoreForm.improvementAdvice"
            placeholder="请输入改进建议"
            :rows="3"
            :maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { AssessItem, AssessType } from '@/types/human'
import { assessApi } from '@/api/human'
import { userApi } from '@/api/system'
import { useCrudTable } from '@/composables/useCrudTable'
import { useDetailModal } from '@/composables/useDetailModal'

// 员工/考核人下拉（真实用户列表）
const userOptions = ref<{ id: number; name: string }[]>([])
const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 999 })
    userOptions.value = res.list.map((u) => ({ id: Number(u.id), name: u.realName || u.username }))
  } catch {}
}

// 用户 ID -> 姓名（用于表格列 / 详情 / 评分弹窗显示）
const userName = (id?: number | null, name?: string | null) => {
  if (name) return name
  if (id == null) return '-'
  const u = userOptions.value.find((o) => o.id === id)
  return u ? u.name : String(id)
}

const filterOption = (input: string, option: any) => {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

// 考核类型文案
const assessTypeMap: Record<AssessType, string> = {
  monthly: '月度考核',
  quarterly: '季度考核',
  special: '专项考核',
}

// 考核周期选项：动态生成当前年份前后 1 年的月份列表
const periodOptions = (() => {
  const now = new Date()
  const startYear = now.getFullYear() - 1
  const endYear = now.getFullYear() + 1
  const options: { label: string; value: string }[] = []
  for (let y = startYear; y <= endYear; y++) {
    for (let m = 1; m <= 12; m++) {
      const value = `${y}-${String(m).padStart(2, '0')}`
      options.push({ label: `${y}年${m}月`, value })
    }
  }
  return options
})()

// 搜索表单
const searchForm = reactive({
  assessmentWeek: undefined as string | undefined,
  assessorId: undefined as number | undefined
})

// 表格数据
const { tableData, loading, pagination, loadData, handleSearch, handleTableChange } = useCrudTable<any, typeof searchForm>({
  searchForm,
  loadFn: (params) => assessApi.getList({
    assessmentWeek: params.assessmentWeek || undefined,
    assessorId: params.assessorId || undefined,
    page: params.page,
    pageSize: params.pageSize,
  }),
})

// 表格列配置
const columns = [
  { title: '被考核员工', dataIndex: 'employeeId', key: 'employeeId', width: 110 },
  { title: '考核类型', dataIndex: 'type', key: 'type', width: 100, align: 'center' as const },
  { title: '考核周期', dataIndex: 'assessmentWeek', key: 'assessmentWeek', width: 200 },
  { title: '考核人', dataIndex: 'assessorId', key: 'assessorId', width: 110 },
  { title: '商品知识', dataIndex: 'productKnowledgeScore', key: 'productKnowledgeScore', width: 90, align: 'center' as const },
  { title: '搭配技巧', dataIndex: 'matchingSkillScore', key: 'matchingSkillScore', width: 90, align: 'center' as const },
  { title: '接待能力', dataIndex: 'receptionScore', key: 'receptionScore', width: 90, align: 'center' as const },
  { title: '异议处理', dataIndex: 'objectionHandlingScore', key: 'objectionHandlingScore', width: 90, align: 'center' as const },
  { title: '话术演练', dataIndex: 'promotionScriptScore', key: 'promotionScriptScore', width: 90, align: 'center' as const },
  { title: '总分', dataIndex: 'totalScore', key: 'totalScore', width: 90, align: 'center' as const },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

// 详情弹窗
const { detailVisible, detailRecord, openDetail } = useDetailModal<AssessItem>()

// 弹窗相关
const modalVisible = ref(false)
const modalLoading = ref(false)
const formRef = ref()
const formData = reactive({
  employeeId: undefined as number | undefined,
  assessorId: undefined as number | undefined,
  type: 'monthly' as AssessType,
  assessmentWeekRange: null as [string, string] | null
})

const formRules = {
  employeeId: [{ required: true, message: '请选择被考核员工', trigger: 'change' }],
  assessorId: [{ required: true, message: '请选择考核人', trigger: 'change' }],
  type: [{ required: true, message: '请选择考核类型', trigger: 'change' }],
  assessmentWeekRange: [{ required: true, message: '请选择考核周期', trigger: 'change' }]
}

// 重置
const handleReset = () => {
  searchForm.assessmentWeek = undefined
  searchForm.assessorId = undefined
  pagination.current = 1
  loadData()
}

// 新建
const handleAdd = () => {
  resetForm()
  modalVisible.value = true
}

// 查看
const handleView = (record: AssessItem) => {
  openDetail(record)
}

// 5维度评分弹窗
const scoreVisible = ref(false)
const scoreRecord = ref<AssessItem | null>(null)
const scoreForm = reactive({
  type: 'monthly' as AssessType,
  productKnowledge: 0,
  matchingSkill: 0,
  reception: 0,
  objectionHandling: 0,
  promotionScript: 0,
  improvementAdvice: ''
})
const totalScore = computed(() => {
  return scoreForm.productKnowledge + scoreForm.matchingSkill +
    scoreForm.reception + scoreForm.objectionHandling + scoreForm.promotionScript
})

const handleScore = (item: AssessItem) => {
  scoreRecord.value = item
  scoreForm.type = item.type || 'monthly'
  scoreForm.productKnowledge = 0
  scoreForm.matchingSkill = 0
  scoreForm.reception = 0
  scoreForm.objectionHandling = 0
  scoreForm.promotionScript = 0
  scoreForm.improvementAdvice = ''
  scoreVisible.value = true
}

const handleScoreOk = async () => {
  try {
    await assessApi.update(String(scoreRecord.value!.id), {
      type: scoreForm.type,
      productKnowledgeScore: scoreForm.productKnowledge,
      matchingSkillScore: scoreForm.matchingSkill,
      receptionScore: scoreForm.reception,
      objectionHandlingScore: scoreForm.objectionHandling,
      promotionScriptScore: scoreForm.promotionScript,
      totalScore: totalScore.value,
      improvementAdvice: scoreForm.improvementAdvice
    })
    message.success('评分已提交')
    scoreVisible.value = false
    loadData()
  } catch { message.error('评分提交失败') }
}

// 弹窗确认
const handleModalOk = async () => {
  try {
    await formRef.value?.validateFields()
    modalLoading.value = true

    await assessApi.create({
      employeeId: formData.employeeId,
      assessorId: formData.assessorId,
      type: formData.type,
      assessmentWeek: formData.assessmentWeekRange ? formData.assessmentWeekRange.join(' ~ ') : ''
    })

    message.success('新建成功')
    modalVisible.value = false
    loadData()
  } catch (error) {
    console.error('表单验证失败', error)
  } finally {
    modalLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.employeeId = undefined
  formData.assessorId = undefined
  formData.type = 'monthly'
  formData.assessmentWeekRange = null
}

onMounted(() => {
  loadUsers()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
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

.table-card {
  padding: 16px;
  overflow: hidden;
}

.search-card {
  padding: 16px 24px;
}

.search-card :deep(.ant-form) {
  flex-wrap: wrap;
}

.search-card :deep(.ant-form-item) {
  margin-bottom: 12px;
  margin-right: 0;
}

.action-link {
  font-size: 13px;
  color: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
  cursor: pointer;
}

.action-link:hover {
  color: #40a9ff;
  background: #e6f7ff;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.action-btns :deep(.ant-divider-vertical) {
  margin: 0 2px;
}

.score-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.score-record-name {
  font-weight: 600;
  color: #333;
}

.score-record-week {
  font-size: 13px;
  color: #999;
}

.score-total {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

/* 表格横向滚动 */
.table-card :deep(.ant-table-wrapper) {
  overflow-x: auto;
}

.table-card :deep(.ant-table) {
  min-width: 900px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .content-card {
    padding: 16px;
    margin-bottom: 12px;
  }

  .table-card {
    padding: 12px;
  }

  .search-card {
    padding: 12px 16px;
  }

  .search-card :deep(.ant-form-item) {
    width: 100%;
  }

  .search-card :deep(.ant-form-item-control) {
    flex: 1;
  }

  .table-card :deep(.ant-table) {
    font-size: 13px;
    min-width: 700px;
  }

  .table-card :deep(.ant-table-thead > tr > th),
  .table-card :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
  }
}

@media (max-width: 576px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-header h2 {
    font-size: 18px;
  }

  .table-card :deep(.ant-table) {
    font-size: 12px;
    min-width: 600px;
  }
}
</style>
