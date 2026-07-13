<template>
  <div class="page-container">
    <div class="page-header">
      <h2>个人设置</h2>
    </div>

    <div class="content-card">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 基本设置 -->
        <a-tab-pane key="basic" tab="基本设置">
          <div class="settings-form">
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 10 }" layout="horizontal">
              <a-form-item label="头像">
                <div class="avatar-upload">
                  <a-avatar :size="80" style="background: linear-gradient(135deg, #c8a44d, #e8d59a)">
                    <template #icon><UserOutlined style="font-size: 36px" /></template>
                  </a-avatar>
                  <div class="avatar-actions">
                    <a-button size="small">更换头像</a-button>
                    <span class="avatar-hint">支持JPG、PNG格式，大小不超过2MB</span>
                  </div>
                </div>
              </a-form-item>
              <a-form-item label="用户名">
                <a-input v-model:value="formData.username" disabled />
              </a-form-item>
              <a-form-item label="手机号">
                <div class="field-with-btn">
                  <a-input v-model:value="formData.phone" disabled />
                  <a-button type="link" @click="handleEditPhone">修改</a-button>
                </div>
              </a-form-item>
              <a-form-item label="邮箱">
                <div class="field-with-btn">
                  <a-input v-model:value="formData.email" disabled />
                  <a-button type="link" @click="handleEditEmail">修改</a-button>
                </div>
              </a-form-item>
              <a-form-item label="所在时区">
                <a-select v-model:value="formData.timezone" style="width: 100%">
                  <a-select-option value="(UTC+08:00) 北京、重庆、香港特别行政区、乌鲁木齐">
                    (UTC+08:00) 北京、重庆、香港特别行政区、乌鲁木齐
                  </a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="语言设置">
                <a-select v-model:value="formData.language" style="width: 100%">
                  <a-select-option value="简体中文">简体中文</a-select-option>
                  <a-select-option value="English">English</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="日期格式">
                <a-select v-model:value="formData.dateFormat" style="width: 100%">
                  <a-select-option value="YYYY-MM-DD">YYYY-MM-DD</a-select-option>
                  <a-select-option value="DD/MM/YYYY">DD/MM/YYYY</a-select-option>
                  <a-select-option value="MM/DD/YYYY">MM/DD/YYYY</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item :wrapper-col="{ offset: 4, span: 10 }">
                <a-button type="primary" @click="handleSave" :loading="saveLoading" style="width: 120px">
                  保存设置
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <!-- 修改密码 -->
        <a-tab-pane key="password" tab="修改密码">
          <div class="settings-form">
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 10 }" layout="horizontal">
              <a-form-item label="当前密码">
                <a-input-password v-model:value="passwordForm.current" placeholder="请输入当前密码" />
              </a-form-item>
              <a-form-item label="新密码">
                <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码（至少6位）" />
              </a-form-item>
              <a-form-item label="确认密码">
                <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
              </a-form-item>
              <a-form-item :wrapper-col="{ offset: 4, span: 10 }">
                <a-button type="primary" @click="handleChangePassword" :loading="passwordLoading" style="width: 120px">
                  修改密码
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <!-- 通知设置 -->
        <a-tab-pane key="notification" tab="通知设置">
          <div class="settings-form">
            <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 10 }" layout="horizontal">
              <a-form-item label="系统通知">
                <a-switch v-model:checked="notifySettings.system" />
              </a-form-item>
              <a-form-item label="订单提醒">
                <a-switch v-model:checked="notifySettings.order" />
              </a-form-item>
              <a-form-item label="库存预警">
                <a-switch v-model:checked="notifySettings.inventory" />
              </a-form-item>
              <a-form-item label="营销活动">
                <a-switch v-model:checked="notifySettings.marketing" />
              </a-form-item>
              <a-form-item :wrapper-col="{ offset: 4, span: 10 }">
                <a-button type="primary" @click="handleSaveNotify" style="width: 120px">
                  保存设置
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import type { UserInfo } from '@/types/profile'
import { profileApi } from '@/api/mock/profile'

const activeTab = ref('basic')
const saveLoading = ref(false)
const passwordLoading = ref(false)

const formData = reactive({
  username: '',
  phone: '',
  email: '',
  timezone: '',
  language: '',
  dateFormat: ''
})

const passwordForm = reactive({
  current: '',
  newPassword: '',
  confirmPassword: ''
})

const notifySettings = reactive({
  system: true,
  order: true,
  inventory: true,
  marketing: false
})

const loadData = async () => {
  try {
    const info = await profileApi.getUserInfo()
    formData.username = info.username
    formData.phone = info.phone
    formData.email = info.email
    formData.timezone = info.timezone
    formData.language = info.language
    formData.dateFormat = info.dateFormat
  } catch { message.error('加载数据失败') }
}

const handleSave = async () => {
  saveLoading.value = true
  try {
    await profileApi.updateProfile(formData)
    message.success('保存成功')
  } catch { message.error('保存失败') }
  finally { saveLoading.value = false }
}

const handleChangePassword = async () => {
  if (!passwordForm.current || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    message.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  passwordLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    message.success('密码修改成功')
    passwordForm.current = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch { message.error('修改失败') }
  finally { passwordLoading.value = false }
}

const handleSaveNotify = () => {
  message.success('通知设置已保存')
}

const handleEditPhone = () => { message.info('修改手机号功能开发中...') }
const handleEditEmail = () => { message.info('修改邮箱功能开发中...') }

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.content-card { background: #fff; border-radius: 12px; padding: 24px; }
.settings-form { max-width: 700px; padding: 20px 0; }
.avatar-upload { display: flex; align-items: center; gap: 20px; }
.avatar-actions { display: flex; flex-direction: column; gap: 8px; }
.avatar-hint { font-size: 12px; color: #999; }
.field-with-btn { display: flex; align-items: center; gap: 8px; }
.field-with-btn .ant-input { flex: 1; }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .settings-form :deep(.ant-form-item) { flex-direction: column; }
  .settings-form :deep(.ant-form-item-label) { padding: 0 0 4px; text-align: left; }
}
</style>
