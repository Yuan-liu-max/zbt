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
                    <template #icon>
                      <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
                      <UserOutlined v-else style="font-size: 36px" />
                    </template>
                  </a-avatar>
                  <div class="avatar-actions">
                    <a-upload
                      :show-upload-list="false"
                      accept=".jpg,.jpeg,.png"
                      :before-upload="handleAvatarUpload"
                    >
                      <a-button size="small" :loading="avatarLoading">更换头像</a-button>
                    </a-upload>
                    <span class="avatar-hint">支持JPG、PNG格式，大小不超过2MB</span>
                  </div>
                </div>
              </a-form-item>
              <a-form-item label="用户名">
                <a-input v-model:value="formData.username" disabled />
              </a-form-item>
              <a-form-item label="手机号">
                <div class="field-with-btn">
                  <template v-if="editingPhone">
                    <a-input v-model:value="editForm.phone" placeholder="请输入手机号" maxlength="20" />
                    <a-button type="link" @click="handleSavePhone" :loading="phoneSaving">保存</a-button>
                    <a-button type="link" @click="cancelEditPhone">取消</a-button>
                  </template>
                  <template v-else>
                    <a-input :value="formData.phone" disabled />
                    <a-button type="link" @click="startEditPhone">修改</a-button>
                  </template>
                </div>
              </a-form-item>
              <a-form-item label="邮箱">
                <div class="field-with-btn">
                  <template v-if="editingEmail">
                    <a-input v-model:value="editForm.email" placeholder="请输入邮箱" maxlength="100" />
                    <a-button type="link" @click="handleSaveEmail" :loading="emailSaving">保存</a-button>
                    <a-button type="link" @click="cancelEditEmail">取消</a-button>
                  </template>
                  <template v-else>
                    <a-input :value="formData.email" disabled />
                    <a-button type="link" @click="startEditEmail">修改</a-button>
                  </template>
                </div>
              </a-form-item>
              <a-form-item label="所在时区">
                <a-select v-model:value="formData.timezone" style="width: 100%">
                  <a-select-option value="(UTC+08:00) 北京、重庆、香港特别行政区、乌鲁木齐">
                    (UTC+08:00) 北京、重庆、香港特别行政区、乌鲁木齐
                  </a-select-option>
                  <a-select-option value="(UTC+09:00) 首尔、东京">
                    (UTC+09:00) 首尔、东京
                  </a-select-option>
                  <a-select-option value="(UTC+07:00) 曼谷、河内、雅加达">
                    (UTC+07:00) 曼谷、河内、雅加达
                  </a-select-option>
                  <a-select-option value="(UTC+05:30) 新德里">
                    (UTC+05:30) 新德里
                  </a-select-option>
                  <a-select-option value="(UTC+00:00) 伦敦、都柏林、里斯本">
                    (UTC+00:00) 伦敦、都柏林、里斯本
                  </a-select-option>
                  <a-select-option value="(UTC-05:00) 纽约、多伦多、哈瓦那">
                    (UTC-05:00) 纽约、多伦多、哈瓦那
                  </a-select-option>
                  <a-select-option value="(UTC-08:00) 洛杉矶、温哥华">
                    (UTC-08:00) 洛杉矶、温哥华
                  </a-select-option>
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
                <div class="notify-row">
                  <a-switch v-model:checked="notifySettings.system" />
                  <span class="notify-desc">系统公告、账号安全等通知</span>
                </div>
              </a-form-item>
              <a-form-item label="订单提醒">
                <div class="notify-row">
                  <a-switch v-model:checked="notifySettings.order" />
                  <span class="notify-desc">新订单、订单状态变更提醒</span>
                </div>
              </a-form-item>
              <a-form-item label="库存预警">
                <div class="notify-row">
                  <a-switch v-model:checked="notifySettings.inventory" />
                  <span class="notify-desc">库存不足、临期预警提醒</span>
                </div>
              </a-form-item>
              <a-form-item label="营销活动">
                <div class="notify-row">
                  <a-switch v-model:checked="notifySettings.marketing" />
                  <span class="notify-desc">促销活动、营销任务推送</span>
                </div>
              </a-form-item>
              <a-form-item :wrapper-col="{ offset: 4, span: 10 }">
                <a-button type="primary" @click="handleSaveNotify" :loading="notifyLoading" style="width: 120px">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { profileApi } from '@/api/profile'
import request from '@/utils/request'
import { resolveAvatarUrl } from '@/utils/avatar'
import { useAuthStore } from '@/stores/useAuthStore'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const activeTab = ref(route.query.tab === 'password' ? 'password' : 'basic')
const saveLoading = ref(false)
const passwordLoading = ref(false)

const formData = reactive({
  username: '',
  avatar: '',
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

const editingPhone = ref(false)
const editingEmail = ref(false)
const phoneSaving = ref(false)
const emailSaving = ref(false)
const notifyLoading = ref(false)

const editForm = reactive({
  phone: '',
  email: ''
})

const loadData = async () => {
  try {
    const info = await profileApi.getUserInfo()
    formData.username = info.username
    formData.avatar = info.avatar || ''
    formData.phone = info.phone || ''
    formData.email = info.email || ''
    formData.timezone = info.timezone || '(UTC+08:00) 北京、重庆、香港特别行政区、乌鲁木齐'
    formData.language = info.language || '简体中文'
    formData.dateFormat = info.dateFormat || 'YYYY-MM-DD'
    notifySettings.system = info.notifySystem !== false
    notifySettings.order = info.notifyOrder !== false
    notifySettings.inventory = info.notifyInventory !== false
    notifySettings.marketing = !!info.notifyMarketing
    authStore.updateUserInfo({ avatar: info.avatar || '', username: info.username, phone: info.phone, email: info.email })
  } catch (error) { console.error('加载数据失败', error) }
}

const startEditPhone = () => {
  editForm.phone = formData.phone || ''
  editingPhone.value = true
}

const cancelEditPhone = () => {
  editingPhone.value = false
}

const handleSavePhone = async () => {
  const phone = editForm.phone.trim()
  if (!phone) {
    message.warning('手机号不能为空')
    return
  }
  if (!/^1\d{10}$/.test(phone)) {
    message.warning('请输入正确的11位手机号')
    return
  }
  phoneSaving.value = true
  try {
    await profileApi.updateProfile({ phone })
    formData.phone = phone
    authStore.updateUserInfo({ phone })
    editingPhone.value = false
    message.success('手机号修改成功')
  } catch (error) {
    console.error('手机号保存失败', error)
    message.error('手机号修改失败')
  } finally {
    phoneSaving.value = false
  }
}

const startEditEmail = () => {
  editForm.email = formData.email || ''
  editingEmail.value = true
}

const cancelEditEmail = () => {
  editingEmail.value = false
}

const handleSaveEmail = async () => {
  const email = editForm.email.trim()
  if (!email) {
    message.warning('邮箱不能为空')
    return
  }
  if (!/^[\w.%+-]+@[\w.-]+\.[A-Za-z]{2,}$/.test(email)) {
    message.warning('请输入正确的邮箱地址')
    return
  }
  emailSaving.value = true
  try {
    await profileApi.updateProfile({ email })
    formData.email = email
    authStore.updateUserInfo({ email })
    editingEmail.value = false
    message.success('邮箱修改成功')
  } catch (error) {
    console.error('邮箱保存失败', error)
    message.error('邮箱修改失败')
  } finally {
    emailSaving.value = false
  }
}

const avatarLoading = ref(false)

const avatarUrl = computed(() => resolveAvatarUrl(formData.avatar))

const handleAvatarUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  if (!isImage) {
    message.error('仅支持JPG、PNG格式图片')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    message.error('图片大小不能超过2MB')
    return false
  }
  avatarLoading.value = true
  const uploadForm = new FormData()
  uploadForm.append('file', file)
  request
    .post('/files/upload', uploadForm)
    .then(async (res: any) => {
      const avatar = res?.fileUrl
      if (!avatar) throw new Error('上传失败')
      formData.avatar = avatar
      try {
        await profileApi.updateProfile({ avatar })
        authStore.updateUserInfo({ avatar })
        message.success('头像更换成功')
      } catch (error) {
        console.error('头像保存失败', error)
        message.error('头像保存失败')
      }
    })
    .catch((error) => {
      console.error('头像上传失败', error)
      message.error('头像上传失败')
    })
    .finally(() => {
      avatarLoading.value = false
    })
  return false
}

const handleSave = async () => {
  saveLoading.value = true
  try {
    await profileApi.updateProfile(formData)
    message.success('保存成功')
  } catch (error) { console.error('保存失败', error) }
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
    await profileApi.changePassword({
      oldPassword: passwordForm.current,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功，请重新登录')
    passwordForm.current = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 修改成功后退出，要求重新登录
    setTimeout(() => {
      authStore.logout()
      router.push('/login')
    }, 1500)
  } catch (error: any) {
    message.error(error?.response?.data?.msg || '修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleSaveNotify = async () => {
  notifyLoading.value = true
  try {
    await request.put('/auth/profile', {
      notifySystem: notifySettings.system,
      notifyOrder: notifySettings.order,
      notifyInventory: notifySettings.inventory,
      notifyMarketing: notifySettings.marketing
    })
    message.success('通知设置已保存')
  } catch (error: any) {
    message.error(error?.response?.data?.msg || '通知设置保存失败')
  } finally {
    notifyLoading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 24px; min-height: calc(100vh - 64px); }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; }
.content-card { background: #fff; border-radius: 12px; padding: 24px; }
.settings-form { max-width: 700px; padding: 20px 0; }
.avatar-upload { display: flex; align-items: center; gap: 20px; }
.avatar-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.avatar-actions { display: flex; flex-direction: column; gap: 8px; }
.avatar-hint { font-size: 12px; color: #999; }
.field-with-btn { display: flex; align-items: center; gap: 8px; }
.field-with-btn .ant-input { flex: 1; }
.notify-row { display: flex; align-items: center; gap: 12px; }
.notify-desc { font-size: 12px; color: #999; }

@media (max-width: 768px) {
  .page-container { padding: 16px; }
  .content-card { padding: 16px; }
  .settings-form :deep(.ant-form-item) { flex-direction: column; }
  .settings-form :deep(.ant-form-item-label) { padding: 0 0 4px; text-align: left; }
}
</style>
