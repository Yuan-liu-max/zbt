<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统配置</h2>
    </div>

    <!-- 左右布局 -->
    <div class="config-layout">
      <!-- 左侧：分类菜单 -->
      <div class="config-sidebar">
        <div class="sidebar-title">配置分类</div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="inline"
          @click="handleMenuClick"
        >
          <a-menu-item v-for="item in menuItems" :key="item.key">
            <component :is="item.icon" />
            <span>{{ item.label }}</span>
          </a-menu-item>
        </a-menu>
      </div>

      <!-- 右侧：配置表单 -->
      <div class="config-content">
        <!-- 基本配置 -->
        <div v-if="selectedKey === 'basic'" class="config-section">
          <div class="section-title">基本配置</div>
          <a-form
            :model="formState"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="系统名称" name="systemName">
                  <a-input v-model:value="formState.systemName" placeholder="请输入系统名称" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="系统语言" name="language">
                  <a-select v-model:value="formState.language" placeholder="请选择系统语言">
                    <a-select-option value="简体中文">简体中文</a-select-option>
                    <a-select-option value="English">English</a-select-option>
                    <a-select-option value="繁體中文">繁體中文</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="时区设置" name="timezone">
                  <a-select v-model:value="formState.timezone" placeholder="请选择时区">
                    <a-select-option
                      v-for="tz in timezoneOptions"
                      :key="tz.value"
                      :value="tz.value"
                    >
                      {{ tz.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="系统LOGO" name="logo">
                  <a-upload
                    v-model:file-list="logoFileList"
                    action="/api/files/upload"
                    :headers="{ Authorization: `Bearer ${localStorage.getItem('token') || ''}` }"
                    list-type="picture-card"
                    :before-upload="handleLogoBeforeUpload"
                    :max-count="1"
                    @change="handleLogoChange"
                  >
                    <div v-if="logoFileList.length === 0">
                      <PlusOutlined />
                      <div class="upload-text">上传LOGO</div>
                    </div>
                  </a-upload>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="日期格式" name="dateFormat">
                  <a-select v-model:value="formState.dateFormat" placeholder="请选择日期格式">
                    <a-select-option value="YYYY-MM-DD">YYYY-MM-DD</a-select-option>
                    <a-select-option value="YYYY/MM/DD">YYYY/MM/DD</a-select-option>
                    <a-select-option value="DD/MM/YYYY">DD/MM/YYYY</a-select-option>
                    <a-select-option value="MM/DD/YYYY">MM/DD/YYYY</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="时间格式" name="timeFormat">
                  <a-select v-model:value="formState.timeFormat" placeholder="请选择时间格式">
                    <a-select-option value="HH:mm:ss">HH:mm:ss</a-select-option>
                    <a-select-option value="HH:mm">HH:mm</a-select-option>
                    <a-select-option value="hh:mm:ss A">hh:mm:ss A</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="系统描述" name="description">
                  <a-textarea
                    v-model:value="formState.description"
                    placeholder="请输入系统描述"
                    :rows="3"
                    :maxlength="200"
                    show-count
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="ICP备案号" name="icpNumber">
                  <a-input v-model:value="formState.icpNumber" placeholder="请输入ICP备案号" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="系统版本" name="version">
                  <a-input v-model:value="formState.version" disabled />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="首页地址" name="homeUrl">
                  <a-input v-model:value="formState.homeUrl" placeholder="请输入首页地址" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="版权所有" name="copyright">
                  <a-input v-model:value="formState.copyright" disabled />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12"></a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 邮件配置 -->
        <div v-if="selectedKey === 'email'" class="config-section">
          <div class="section-title">邮件配置</div>
          <a-form
            :model="emailForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="SMTP服务器">
                  <a-input v-model:value="emailForm.smtpHost" placeholder="请输入SMTP服务器地址" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="SMTP端口">
                  <a-input-number v-model:value="emailForm.smtpPort" :min="1" :max="65535" style="width: 100%" placeholder="请输入端口号" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="发件人邮箱">
                  <a-input v-model:value="emailForm.senderEmail" placeholder="请输入发件人邮箱" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="邮箱密码">
                  <a-input-password v-model:value="emailForm.senderPassword" placeholder="请输入邮箱密码" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="SSL加密">
                  <a-switch v-model:checked="emailForm.sslEnabled" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="发件人名称">
                  <a-input v-model:value="emailForm.senderName" placeholder="请输入发件人显示名称" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 短信配置 -->
        <div v-if="selectedKey === 'sms'" class="config-section">
          <div class="section-title">短信配置</div>
          <a-form
            :model="smsForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="短信服务商">
                  <a-select v-model:value="smsForm.provider" placeholder="请选择短信服务商">
                    <a-select-option value="aliyun">阿里云短信</a-select-option>
                    <a-select-option value="tencent">腾讯云短信</a-select-option>
                    <a-select-option value="huawei">华为云短信</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="AccessKey">
                  <a-input v-model:value="smsForm.accessKey" placeholder="请输入AccessKey" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="AccessSecret">
                  <a-input-password v-model:value="smsForm.accessSecret" placeholder="请输入AccessSecret" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="签名名称">
                  <a-input v-model:value="smsForm.signName" placeholder="请输入短信签名" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="模板ID">
                  <a-input v-model:value="smsForm.templateId" placeholder="请输入短信模板ID" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12"></a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 支付配置 -->
        <div v-if="selectedKey === 'payment'" class="config-section">
          <div class="section-title">支付配置</div>
          <a-form
            :model="paymentForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="支付平台">
                  <a-select v-model:value="paymentForm.platform" placeholder="请选择支付平台">
                    <a-select-option value="alipay">支付宝</a-select-option>
                    <a-select-option value="wechat">微信支付</a-select-option>
                    <a-select-option value="unionpay">银联支付</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="商户号">
                  <a-input v-model:value="paymentForm.merchantId" placeholder="请输入商户号" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="应用ID">
                  <a-input v-model:value="paymentForm.appId" placeholder="请输入应用ID" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="密钥">
                  <a-input-password v-model:value="paymentForm.secretKey" placeholder="请输入密钥" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="回调地址">
                  <a-input v-model:value="paymentForm.notifyUrl" placeholder="请输入支付回调地址" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="沙箱模式">
                  <a-switch v-model:checked="paymentForm.sandboxMode" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 存储配置 -->
        <div v-if="selectedKey === 'storage'" class="config-section">
          <div class="section-title">存储配置</div>
          <a-form
            :model="storageForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="存储类型">
                  <a-select v-model:value="storageForm.type" placeholder="请选择存储类型">
                    <a-select-option value="local">本地存储</a-select-option>
                    <a-select-option value="oss">阿里云OSS</a-select-option>
                    <a-select-option value="cos">腾讯云COS</a-select-option>
                    <a-select-option value="minio">MinIO</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="存储路径">
                  <a-input v-model:value="storageForm.path" placeholder="请输入存储路径" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="AccessKey">
                  <a-input v-model:value="storageForm.accessKey" placeholder="请输入AccessKey" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="AccessSecret">
                  <a-input-password v-model:value="storageForm.accessSecret" placeholder="请输入AccessSecret" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="Bucket">
                  <a-input v-model:value="storageForm.bucket" placeholder="请输入Bucket名称" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="Endpoint">
                  <a-input v-model:value="storageForm.endpoint" placeholder="请输入Endpoint地址" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 安全配置 -->
        <div v-if="selectedKey === 'security'" class="config-section">
          <div class="section-title">安全配置</div>
          <a-form
            :model="securityForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="登录验证码">
                  <a-switch v-model:checked="securityForm.captchaEnabled" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="密码强度校验">
                  <a-switch v-model:checked="securityForm.passwordStrength" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="登录失败锁定">
                  <a-switch v-model:checked="securityForm.loginLockEnabled" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="最大失败次数">
                  <a-input-number v-model:value="securityForm.maxFailCount" :min="3" :max="10" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="会话超时(分钟)">
                  <a-input-number v-model:value="securityForm.sessionTimeout" :min="5" :max="1440" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="IP白名单">
                  <a-textarea v-model:value="securityForm.ipWhitelist" placeholder="每行一个IP地址" :rows="3" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 其他配置 -->
        <div v-if="selectedKey === 'other'" class="config-section">
          <div class="section-title">其他配置</div>
          <a-form
            :model="otherForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 14 }"
            layout="horizontal"
          >
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="维护模式">
                  <a-switch v-model:checked="otherForm.maintenanceMode" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="调试模式">
                  <a-switch v-model:checked="otherForm.debugMode" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="日志级别">
                  <a-select v-model:value="otherForm.logLevel" placeholder="请选择日志级别">
                    <a-select-option value="debug">Debug</a-select-option>
                    <a-select-option value="info">Info</a-select-option>
                    <a-select-option value="warn">Warn</a-select-option>
                    <a-select-option value="error">Error</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="缓存过期(秒)">
                  <a-input-number v-model:value="otherForm.cacheExpire" :min="60" :max="86400" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="文件大小限制(MB)">
                  <a-input-number v-model:value="otherForm.maxFileSize" :min="1" :max="100" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :sm="24" :md="12">
                <a-form-item label="分页默认条数">
                  <a-input-number v-model:value="otherForm.pageSize" :min="10" :max="100" :step="10" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 底部按钮 -->
        <div class="config-footer">
          <a-button type="primary" :loading="saving" @click="handleSave">
            <SaveOutlined /> 保存配置
          </a-button>
          <a-button @click="handleReset" :loading="saving">
            <ReloadOutlined /> 重置
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SaveOutlined, ReloadOutlined, PlusOutlined, SettingOutlined, MailOutlined, MessageOutlined, WalletOutlined, CloudOutlined, SafetyOutlined, AppstoreOutlined } from '@ant-design/icons-vue'
import type { SystemConfig } from '@/types/system'
import { configApi } from '@/api/system'
import type { UploadChangeParam, UploadFile } from 'ant-design-vue'

// 当前选中的配置分类
const selectedKeys = ref<string[]>(['basic'])
const selectedKey = ref('basic')

// 菜单配置
const menuItems = [
  { key: 'basic', label: '基本配置', icon: SettingOutlined },
  { key: 'email', label: '邮件配置', icon: MailOutlined },
  { key: 'sms', label: '短信配置', icon: MessageOutlined },
  { key: 'payment', label: '支付配置', icon: WalletOutlined },
  { key: 'storage', label: '存储配置', icon: CloudOutlined },
  { key: 'security', label: '安全配置', icon: SafetyOutlined },
  { key: 'other', label: '其他配置', icon: AppstoreOutlined },
]

// 时区选项
const timezoneOptions = [
  { label: '(GMT+08:00) 北京、重庆、香港特别行政区、乌鲁木齐', value: '(GMT+08:00) 北京、重庆、香港特别行政区、乌鲁木齐' },
  { label: '(GMT+08:00) 台北', value: '(GMT+08:00) 台北' },
  { label: '(GMT+09:00) 东京', value: '(GMT+09:00) 东京' },
  { label: '(GMT+00:00) 格林威治标准时间', value: '(GMT+00:00) 格林威治标准时间' },
  { label: '(GMT-05:00) 美国东部时间', value: '(GMT-05:00) 美国东部时间' },
  { label: '(GMT-08:00) 美国太平洋时间', value: '(GMT-08:00) 美国太平洋时间' },
]

// 表单状态
const saving = ref(false)
const formState = reactive<SystemConfig>({
  systemName: '',
  language: '简体中文',
  timezone: '(GMT+08:00) 北京、重庆、香港特别行政区、乌鲁木齐',
  dateFormat: 'YYYY-MM-DD',
  timeFormat: 'HH:mm:ss',
  version: '',
  copyright: '',
  logo: '',
  description: '',
  icpNumber: '',
  homeUrl: '',
})

// LOGO上传
const logoFileList = ref<UploadFile[]>([])

const handleLogoBeforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过2MB')
    return false
  }
  return false // 阻止默认上传行为
}

const handleLogoChange = (info: UploadChangeParam) => {
  if (info.file.status === 'removed') {
    formState.logo = ''
  } else if (info.file.originFileObj) {
    formState.logo = info.file.name
  }
}

// 邮件配置
const emailForm = reactive({
  smtpHost: '',
  smtpPort: 465,
  senderEmail: '',
  senderPassword: '',
  sslEnabled: true,
  senderName: '',
})

// 短信配置
const smsForm = reactive({
  provider: undefined as string | undefined,
  accessKey: '',
  accessSecret: '',
  signName: '',
  templateId: '',
})

// 支付配置
const paymentForm = reactive({
  platform: undefined as string | undefined,
  merchantId: '',
  appId: '',
  secretKey: '',
  notifyUrl: '',
  sandboxMode: false,
})

// 存储配置
const storageForm = reactive({
  type: undefined as string | undefined,
  path: '',
  accessKey: '',
  accessSecret: '',
  bucket: '',
  endpoint: '',
})

// 安全配置
const securityForm = reactive({
  captchaEnabled: true,
  passwordStrength: true,
  loginLockEnabled: true,
  maxFailCount: 5,
  sessionTimeout: 30,
  ipWhitelist: '',
})

// 其他配置
const otherForm = reactive({
  maintenanceMode: false,
  debugMode: false,
  logLevel: 'info',
  cacheExpire: 3600,
  maxFileSize: 10,
  pageSize: 20,
})

// 切换配置分类
const handleMenuClick = (info: { key: string }) => {
  selectedKey.value = info.key
}

// 加载配置
const loadConfig = async () => {
  try {
    const config = await configApi.getConfig()
    Object.assign(formState, config)
  } catch (error) {
    message.error('加载配置失败')
  }
}

// 保存配置
const handleSave = async () => {
  saving.value = true
  try {
    await configApi.saveConfig(formState)
    message.success('配置保存成功')
  } catch (error) {
    message.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

// 重置配置
const handleReset = async () => {
  await loadConfig()
  logoFileList.value = []
  message.info('配置已重置')
}

onMounted(() => {
  loadConfig()
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

.config-layout {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}

.config-sidebar {
  width: 220px;
  border-right: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.sidebar-title {
  padding: 16px 24px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.config-sidebar :deep(.ant-menu) {
  border-right: none;
}

.config-sidebar :deep(.ant-menu-item) {
  margin: 0;
  height: 44px;
  line-height: 44px;
}

.config-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.config-section {
  max-width: 900px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.config-footer {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
}

.upload-text {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .config-layout {
    flex-direction: column;
    min-height: auto;
  }

  .config-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }

  .config-sidebar :deep(.ant-menu) {
    display: flex;
    overflow-x: auto;
    white-space: nowrap;
  }

  .config-sidebar :deep(.ant-menu-item) {
    flex-shrink: 0;
  }

  .config-content {
    padding: 16px;
  }

  .config-section :deep(.ant-form) {
    flex-direction: column;
  }

  .config-section :deep(.ant-col) {
    display: block;
    flex: 1;
    max-width: 100%;
  }
}

@media (max-width: 576px) {
  .page-header h2 {
    font-size: 18px;
  }

  .config-footer {
    flex-direction: column;
  }

  .config-footer :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
