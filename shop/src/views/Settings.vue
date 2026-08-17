<template>
  <div class="settings-page page-container--no-tabbar">
    <van-nav-bar title="设置" left-text="返回" left-arrow @click-left="$router.back()" />

    <!-- ===== 头像区域 ===== -->
    <div class="avatar-section">
      <div class="avatar-wrap" @click="triggerUpload">
        <van-image round width="80" height="80" :src="userStore.userInfo?.avatar || ''">
          <template #loading><van-icon name="photo-o" size="40" color="#ccc" /></template>
          <template #error><van-icon name="user-circle-o" size="80" color="#c8a44d" /></template>
        </van-image>
        <div class="avatar-camera"><van-icon name="photograph" size="16" color="#fff" /></div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />
      </div>
      <p class="avatar-name">{{ userStore.userName }}</p>
      <p class="avatar-phone">{{ userStore.userInfo?.phone || '未绑定手机号' }}</p>
      <p v-if="uploading" class="text-xs text-hint">上传中...</p>
    </div>

    <!-- ===== 个人资料 ===== -->
    <van-cell-group inset title="个人资料">
      <van-cell title="编辑资料" is-link @click="showProfile = true">
        <template #value><span class="text-hint">{{ userStore.userInfo?.realName || '未设置' }}</span></template>
      </van-cell>
      <van-cell title="修改密码" is-link @click="showPwd = true" />
      <van-cell title="收货地址" is-link to="/address" />
    </van-cell-group>

    <!-- ===== 通知设置 ===== -->
    <van-cell-group inset title="通知设置">
      <van-cell title="系统通知" center>
        <template #right-icon><van-switch v-model="notifySystem" size="24" @change="onNotifyChange('notifySystem', $event)" /></template>
      </van-cell>
      <van-cell title="订单通知" center>
        <template #right-icon><van-switch v-model="notifyOrder" size="24" @change="onNotifyChange('notifyOrder', $event)" /></template>
      </van-cell>
      <van-cell title="库存通知" center>
        <template #right-icon><van-switch v-model="notifyInventory" size="24" @change="onNotifyChange('notifyInventory', $event)" /></template>
      </van-cell>
      <van-cell title="营销通知" center>
        <template #right-icon><van-switch v-model="notifyMarketing" size="24" @change="onNotifyChange('notifyMarketing', $event)" /></template>
      </van-cell>
    </van-cell-group>

    <!-- ===== 账号安全 ===== -->
    <van-cell-group inset title="账号安全">
      <van-cell title="最后登录" :value="formatTime(userStore.userInfo?.lastLoginAt)" />
      <van-cell title="注册时间" :value="formatTime(userStore.userInfo?.createdAt)" />
    </van-cell-group>

    <!-- ===== 其他 ===== -->
    <van-cell-group inset style="margin-top:12px">
      <van-cell title="清除缓存" is-link @click="clearCache" />
      <van-cell title="账号注销" is-link @click="showDeactivate = true">
        <template #title><span style="color:#ee0a24">账号注销</span></template>
      </van-cell>
    </van-cell-group>

    <div style="height:40px" />

    <!-- ===== 编辑资料弹窗 ===== -->
    <van-popup v-model:show="showProfile" position="bottom" :style="{ height: '55%' }" round>
      <div class="popup-header">
        <span class="text-md" style="font-weight:600">编辑个人资料</span>
      </div>
      <van-form @submit="saveProfile" style="padding:12px">
        <van-cell-group inset>
          <van-field v-model="profileForm.realName" label="真实姓名" placeholder="请输入姓名" />
          <van-field v-model="profileForm.phone" label="手机号" placeholder="请输入手机号" />
          <van-field v-model="profileForm.email" label="邮箱" placeholder="请输入邮箱" />
        </van-cell-group>
        <div style="margin:16px;display:flex;gap:12px">
          <van-button round block type="primary" native-type="submit" :loading="profileSaving">保存</van-button>
        </div>
      </van-form>
    </van-popup>

    <!-- ===== 修改密码弹窗 ===== -->
    <van-dialog v-model:show="showPwd" title="修改密码" show-cancel-button :before-close="onPwdBeforeClose" :close-on-click-overlay="false">
      <van-form style="padding:16px">
        <van-field v-model="oldPwd" label="原密码" type="password" placeholder="请输入原密码" required />
        <van-field v-model="newPwd" label="新密码" type="password" placeholder="至少6位" required />
        <van-field v-model="confirmPwd" label="确认密码" type="password" placeholder="再次输入新密码" required />
      </van-form>
    </van-dialog>

    <!-- ===== 注销确认弹窗 ===== -->
    <van-dialog v-model:show="showDeactivate" title="确认注销账号？" show-cancel-button :before-close="onDeactivateBeforeClose" confirm-button-color="#ee0a24">
      <div style="padding:20px 16px;text-align:center">
        <van-icon name="warning-o" size="48" color="#ee0a24" />
        <p style="margin:12px 0 0;color:#666;font-size:14px">注销后账号将被禁用，无法登录和使用商城服务。</p>
        <p style="margin:4px 0 0;color:#999;font-size:12px">如需恢复，请联系客服。</p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { authApi } from '@/api/auth'
import { uploadApi } from '@/api/services'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()

// ---- 头像上传 ----
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

function triggerUpload() {
  fileInput.value?.click()
}

async function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadApi.upload(file)
    await authApi.updateProfile({ avatar: res.fileUrl })
    userStore.updateProfile({ avatar: res.fileUrl })
    showSuccessToast('头像更新成功')
  } catch {
    showToast('上传失败，请稍后重试')
  } finally {
    uploading.value = false
    // 清空 input 以允许重复上传同一文件
    if (fileInput.value) fileInput.value.value = ''
  }
}

// ---- 编辑资料 ----
const showProfile = ref(false)
const profileSaving = ref(false)
const profileForm = reactive({ realName: '', phone: '', email: '' })

watch(showProfile, (v) => {
  if (v) {
    profileForm.realName = userStore.userInfo?.realName || ''
    profileForm.phone = userStore.userInfo?.phone || ''
    profileForm.email = userStore.userInfo?.email || ''
  }
})

async function saveProfile() {
  profileSaving.value = true
  try {
    const patch: Record<string, any> = {}
    if (profileForm.realName) patch.realName = profileForm.realName
    if (profileForm.phone) patch.phone = profileForm.phone
    if (profileForm.email !== undefined) patch.email = profileForm.email
    await authApi.updateProfile(patch)
    userStore.updateProfile(patch)
    showSuccessToast('资料已更新')
    showProfile.value = false
  } catch {
    // 拦截器已提示
  } finally {
    profileSaving.value = false
  }
}

// ---- 修改密码 ----
const showPwd = ref(false)
const oldPwd = ref('')
const newPwd = ref('')
const confirmPwd = ref('')

async function onPwdBeforeClose(action: string): Promise<boolean> {
  if (action === 'cancel') return true
  // 校验
  if (!oldPwd.value || !newPwd.value || !confirmPwd.value) {
    showToast('请填写完整信息')
    return false
  }
  if (newPwd.value.length < 6) {
    showToast('新密码长度至少6位')
    return false
  }
  if (newPwd.value !== confirmPwd.value) {
    showToast('两次密码输入不一致')
    return false
  }
  try {
    await authApi.changePassword(oldPwd.value, newPwd.value)
    showSuccessToast('密码修改成功，请重新登录')
    oldPwd.value = ''
    newPwd.value = ''
    confirmPwd.value = ''
    userStore.logout()
    router.push('/profile')
    return true
  } catch {
    return false // API 失败，阻止关闭
  }
}

// ---- 通知开关 ----
const notifySystem = ref(userStore.userInfo?.notifySystem !== false)
const notifyOrder = ref(userStore.userInfo?.notifyOrder !== false)
const notifyInventory = ref(userStore.userInfo?.notifyInventory !== false)
const notifyMarketing = ref(userStore.userInfo?.notifyMarketing !== false)

async function onNotifyChange(key: string, val: any) {
  const patch: Record<string, any> = {}
  patch[key] = !!val
  try {
    await authApi.updateProfile(patch)
    userStore.updateProfile(patch)
  } catch {
    // 拦截器已处理
  }
}

// ---- 账号注销 ----
const showDeactivate = ref(false)

async function onDeactivateBeforeClose(action: string): Promise<boolean> {
  if (action === 'cancel') return true
  try {
    await userStore.deactivateAccount()
    showToast('账号已注销')
    router.push('/profile')
    return true
  } catch {
    return false
  }
}

// ---- 清除缓存 ----
function clearCache() {
  showToast('缓存已清除')
}

// ---- 工具 ----
function formatTime(val?: string): string {
  if (!val) return '—'
  // 后端返回可能是数组 [2026,8,5,14,30] 或字符串
  try {
    if (typeof val === 'string' && val.startsWith('[')) {
      const arr = JSON.parse(val)
      if (Array.isArray(arr) && arr.length >= 5) {
        const [y, m, d, h, min] = arr
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
      }
    }
    // 字符串格式: "2026-08-05T14:30:00"
    if (val.includes('T')) {
      return val.replace('T', ' ').substring(0, 19)
    }
    return val.length > 16 ? val.substring(0, 19).replace('T', ' ') : val
  } catch {
    return val.length > 16 ? val.substring(0, 19).replace('T', ' ') : val
  }
}
</script>

<style scoped>
.settings-page { min-height: 100vh; background: var(--bg-page); }

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 16px 20px;
  background: #fff;
  margin-bottom: 12px;
}
.avatar-wrap {
  position: relative;
  cursor: pointer;
}
.avatar-camera {
  position: absolute;
  right: -2px;
  bottom: -2px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary, #c8a44d);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}
.avatar-name {
  margin: 10px 0 4px;
  font-size: 16px;
  font-weight: 600;
}
.avatar-phone {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 弹窗 */
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}
</style>
