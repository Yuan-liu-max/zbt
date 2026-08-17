<template>
  <div class="page-container">
    <!-- ==================== 面包屑 ==================== -->
    <a-breadcrumb class="page-breadcrumb">
      <a-breadcrumb-item>任务中心</a-breadcrumb-item>
      <a-breadcrumb-item>创建任务</a-breadcrumb-item>
    </a-breadcrumb>

    <!-- ==================== 基本信息 ==================== -->
    <div class="content-card">
      <div class="content-card__header">
        <h3>基本信息</h3>
      </div>
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
        class="task-form"
      >
        <div class="form-grid">
          <!-- 任务名称 -->
          <a-form-item label="任务名称" name="name" class="form-item-full">
            <a-input
              v-model:value="formData.name"
              placeholder="请输入任务名称"
              :maxlength="100"
              allow-clear
            />
          </a-form-item>

          <!-- 任务类型 -->
          <a-form-item label="任务类型" name="type">
            <a-select
              v-model:value="formData.type"
              placeholder="请选择任务类型"
              allow-clear
            >
              <a-select-option v-for="item in taskTypeOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <!-- 优先级 -->
          <a-form-item label="优先级" name="priority">
            <a-select
              v-model:value="formData.priority"
              placeholder="请选择优先级"
              allow-clear
            >
              <a-select-option v-for="item in priorityOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <!-- 负责人 -->
          <a-form-item label="负责人" name="assignee">
            <a-select
              v-model:value="formData.assignee"
              placeholder="请选择负责人"
              show-search
              :filter-option="filterOption"
              allow-clear
            >
              <a-select-option v-for="p in personOptions" :key="p.value" :value="p.value">
                {{ p.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <!-- 参与人 -->
          <a-form-item label="参与人" name="participants">
            <a-select
              v-model:value="formData.participants"
              mode="multiple"
              placeholder="请选择参与人"
              show-search
              :filter-option="filterOption"
              allow-clear
              :max-tag-count="isMobileScreen ? 1 : 3"
            >
              <a-select-option v-for="p in personOptions" :key="p.value" :value="p.value">
                {{ p.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <!-- 开始时间 -->
          <a-form-item label="开始时间" name="startTime">
            <a-date-picker
              v-model:value="formData.startTime"
              show-time
              format="YYYY-MM-DD HH:mm"
              placeholder="请选择开始时间"
              style="width: 100%"
            />
          </a-form-item>

          <!-- 截止时间 -->
          <a-form-item label="截止时间" name="endTime">
            <a-date-picker
              v-model:value="formData.endTime"
              show-time
              format="YYYY-MM-DD HH:mm"
              placeholder="请选择截止时间"
              style="width: 100%"
            />
          </a-form-item>

          <!-- 任务描述 -->
          <a-form-item label="任务描述" name="description" class="form-item-full">
            <a-textarea
              v-model:value="formData.description"
              placeholder="请输入任务描述"
              :rows="4"
              :maxlength="500"
              show-count
              allow-clear
            />
          </a-form-item>
        </div>
      </a-form>
    </div>

    <!-- ==================== 其他设置 ==================== -->
    <div class="content-card" style="margin-top: 16px">
      <div class="content-card__header">
        <h3>其他设置</h3>
      </div>

      <div class="form-grid">
        <!-- 提醒设置 -->
        <a-form-item label="提醒设置" class="form-item-full">
          <div class="reminder-row">
            <span class="reminder-label">截止时间前</span>
            <a-input-number
              v-model:value="formData.remindDays"
              :min="1"
              :max="30"
              :precision="0"
              style="width: 80px"
              placeholder="天数"
            />
            <span class="reminder-label">天提醒</span>
          </div>
        </a-form-item>

        <!-- 附件上传 -->
        <a-form-item label="附件上传" class="form-item-full">
          <a-upload
            v-model:file-list="formData.attachments"
            action="/api/files/upload"
            :before-upload="beforeUpload"
            :max-count="10"
            :multiple="true"
            accept=".jpg,.png,.pdf,.doc,.docx,.xls,.xlsx"
          >
            <a-button>
              <UploadOutlined />
              选择文件
            </a-button>
          </a-upload>
          <div class="upload-tip">支持 jpg, png, pdf, doc, docx, xls, xlsx 格式，单个文件不超过 20MB，最多上传 10 个文件</div>
        </a-form-item>
      </div>
    </div>

    <!-- ==================== 底部操作栏 ==================== -->
    <div class="form-footer">
      <a-space>
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">
          保存
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'
import type { TaskDimension, TaskPriority } from '@/types/task'
import { taskApi, templateApi } from '@/api/task'
import { userApi } from '@/api/system'

// 本地映射表
const taskTypeMap: Record<string, string> = { HUMAN: '人效', PRODUCT: '货品', SCENE: '场景', COMPREHENSIVE: '综合' }
const priorityMap: Record<string, { text: string; color: string }> = {
  LOW: { text: '低', color: 'blue' },
  MEDIUM: { text: '中', color: 'orange' },
  HIGH: { text: '高', color: 'red' },
  URGENT: { text: '紧急', color: 'magenta' },
}

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)

/* ---------- 响应式 ---------- */
const isMobileScreen = ref(false)
const checkScreen = () => {
  isMobileScreen.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreen()
  window.addEventListener('resize', checkScreen)
  loadUsers()

  // URL 携带 templateId（从任务模板「使用」跳转）：预填模板信息
  const templateId = route.query.templateId
  if (templateId) {
    templateApi
      .getDetail(String(templateId))
      .then((tpl) => {
        formData.templateId = Number(templateId)
        formData.name = tpl.templateName || ''
        if (tpl.dimension) formData.type = tpl.dimension
      })
      .catch((error) => {
        console.error('加载任务模板失败', error)
      })
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreen)
})

/* ---------- 下拉选项 ---------- */
const taskTypeOptions = Object.entries(taskTypeMap).map(([value, label]) => ({
  value: value,
  label,
}))

const priorityOptions = Object.entries(priorityMap).map(([value, info]) => ({
  value: value,
  label: info.text,
}))

// 负责人下拉：真实用户列表（GET /users）
interface UserOption {
  value: string
  label: string
}
const personOptions = ref<UserOption[]>([])
const loadUsers = async () => {
  try {
    const res = await userApi.getList({ page: 1, pageSize: 999 })
    personOptions.value = (res.list || []).map((u) => ({
      value: String(u.id),
      label: u.realName || u.username,
    }))
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const filterOption = (input: string, option: any) => {
  return (
    String(option?.label || '').toLowerCase().includes(input.toLowerCase()) ||
    String(option?.value || '').toLowerCase().includes(input.toLowerCase())
  )
}

/* ---------- 表单数据 ---------- */
interface FormData {
  name: string
  type: TaskDimension | undefined
  priority: TaskPriority | undefined
  assignee: string | undefined
  participants: string[]
  startTime: any
  endTime: any
  description: string
  remindDays: number
  attachments: any[]
  templateId?: number
}

const formData = reactive<FormData>({
  name: '',
  type: undefined,
  priority: undefined,
  assignee: undefined,
  participants: [],
  startTime: null,
  endTime: null,
  description: '',
  remindDays: 3,
  attachments: [],
  templateId: undefined,
})

/* ---------- 校验规则 ---------- */
const formRules = {
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择任务类型', trigger: 'change' },
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' },
  ],
  assignee: [
    { required: true, message: '请选择负责人', trigger: 'change' },
  ],
  participants: [
    { required: false, trigger: 'change', type: 'array' as const },
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' },
  ],
  endTime: [
    { required: true, message: '请选择截止时间', trigger: 'change' },
  ],
}

/* ---------- 附件校验 ---------- */
const allowedTypes = [
  'image/jpeg',
  'image/png',
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
]
const MAX_FILE_SIZE = 20 * 1024 * 1024 // 20MB

const beforeUpload = (file: File) => {
  const isValidType = allowedTypes.includes(file.type)
  if (!isValidType) {
    message.error('仅支持 jpg, png, pdf, doc, docx, xls, xlsx 格式')
    return false
  }
  const isValidSize = file.size <= MAX_FILE_SIZE
  if (!isValidSize) {
    message.error('文件大小不能超过 20MB')
    return false
  }
  return true
}

/* ---------- 取消 ---------- */
const handleCancel = () => {
  router.back()
}

/* ---------- 提交 ---------- */
const handleSubmit = async () => {
  try {
    await formRef.value?.validateFields()
  } catch {
    message.warning('请填写必填项')
    return
  }

  submitting.value = true
  try {
    const params: any = {
      taskTitle: formData.name,
      dimension: formData.type!,
      priority: formData.priority!,
      assigneeId: formData.assignee ? Number(formData.assignee) : undefined,
      category: formData.description,
      sourceType: 'MANUAL',
      templateId: formData.templateId,
    }
    if (formData.startTime) params.startTime = formData.startTime.format('YYYY-MM-DD HH:mm:ss')
    if (formData.endTime) params.dueTime = formData.endTime.format('YYYY-MM-DD HH:mm:ss')

    await taskApi.create(params)
    message.success('任务创建成功')
    router.push('/task')
  } catch (err: any) {
    message.error(err?.message || '创建失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="less" scoped>
@import '@/styles/variables.less';

.page-breadcrumb {
  margin-bottom: @spacing-md;
  font-size: @font-size-sm;

  :deep(.ant-breadcrumb-link),
  :deep(.ant-breadcrumb-separator) {
    color: @text-hint;
  }

  :deep(.ant-breadcrumb-link:last-child) {
    color: @text-primary;
    font-weight: 500;
  }
}

.task-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 @spacing-lg;

  @media (max-width: @screen-md) {
    grid-template-columns: 1fr;
    gap: 0;
  }
}

.form-item-full {
  grid-column: 1 / -1;
}

.reminder-row {
  display: flex;
  align-items: center;
  gap: @spacing-sm;
  flex-wrap: wrap;
}

.reminder-label {
  font-size: @font-size-base;
  color: @text-secondary;
  white-space: nowrap;
}

.upload-tip {
  margin-top: @spacing-xs;
  font-size: @font-size-xs;
  color: @text-hint;
  line-height: 1.6;
}

.form-footer {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  padding: @spacing-md @spacing-lg;
  margin: @spacing-md calc(-1 * @spacing-lg) calc(-1 * @spacing-lg);
  background: @bg-card;
  border-top: 1px solid @border-color;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;

  @media (max-width: @screen-md) {
    padding: @spacing-md;
    margin: @spacing-md calc(-1 * @spacing-md) calc(-1 * @spacing-md);
  }
}

/* 移动端表单间距优化 */
@media (max-width: @screen-md) {
  .form-item-full {
    grid-column: 1;
  }
}
</style>
