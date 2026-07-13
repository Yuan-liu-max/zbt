<template>
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <svg viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="60" height="60" rx="14" fill="url(#reg-gold-grad)"/>
            <path d="M30 14L20 26h20L30 14z" fill="#fff" opacity="0.95"/>
            <path d="M17 28l13 18 13-18H17z" fill="#fff" opacity="0.7"/>
            <defs>
              <linearGradient id="reg-gold-grad" x1="0" y1="0" x2="60" y2="60">
                <stop stop-color="#e8d59a"/>
                <stop offset="1" stop-color="#c8a44d"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="brand-title">珠宝通</h1>
        <p class="brand-subtitle">珠宝行业管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <CheckCircleOutlined />
            <span>智能门店管理</span>
          </div>
          <div class="feature-item">
            <CheckCircleOutlined />
            <span>人效数据分析</span>
          </div>
          <div class="feature-item">
            <CheckCircleOutlined />
            <span>AI 智能辅助</span>
          </div>
        </div>
      </div>
      <div class="brand-footer">
        © {{ currentYear }} 珠宝通珠宝行业管理系统
      </div>
    </div>

    <!-- 右侧注册区域 -->
    <div class="login-main">
      <div class="login-card">
        <div class="login-header">
          <h2>注册账号</h2>
          <p>创建您的珠宝通管理系统账号</p>
        </div>

        <a-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          layout="vertical"
          class="login-form"
        >
          <a-form-item name="username">
            <a-input
              v-model:value="formData.username"
              size="large"
              placeholder="请输入用户名"
            >
              <template #prefix><UserOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item name="phone">
            <a-input
              v-model:value="formData.phone"
              size="large"
              placeholder="请输入手机号"
            >
              <template #prefix><MobileOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item name="password">
            <a-input-password
              v-model:value="formData.password"
              size="large"
              placeholder="请输入密码（至少6位）"
            >
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="confirmPassword">
            <a-input-password
              v-model:value="formData.confirmPassword"
              size="large"
              placeholder="请确认密码"
            >
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="agreement">
            <a-checkbox v-model:checked="formData.agreement">
              我已阅读并同意 <a class="link">《用户服务协议》</a> 和 <a class="link">《隐私政策》</a>
            </a-checkbox>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              block
              size="large"
              :loading="registerLoading"
              @click="handleRegister"
              class="login-btn"
            >
              注 册
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          已有账号？<router-link to="/login" class="link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, MobileOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const formRef = ref()
const registerLoading = ref(false)
const currentYear = new Date().getFullYear()

const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formData.password) {
    throw new Error('两次输入的密码不一致')
  }
}

const validateAgreement = async (_rule: any, value: boolean) => {
  if (!value) {
    throw new Error('请同意用户服务协议')
  }
}

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value?.validateFields()
    registerLoading.value = true

    // 模拟注册
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success('注册成功！请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style lang="less" scoped>
@import '@/styles/variables.less';

.login-container {
  display: flex;
  min-height: 100vh;
  background: #f7f8fa;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 50%, #1a1a2e 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle at 30% 40%, rgba(200, 164, 77, 0.08) 0%, transparent 50%);
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    right: -30%;
    width: 150%;
    height: 150%;
    background: radial-gradient(circle at 70% 60%, rgba(200, 164, 77, 0.05) 0%, transparent 50%);
    pointer-events: none;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.brand-logo {
  margin-bottom: 32px;

  svg {
    width: 80px;
    height: 80px;
    filter: drop-shadow(0 8px 24px rgba(200, 164, 77, 0.3));
  }
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: @primary-color;
  margin-bottom: 8px;
  letter-spacing: 4px;
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 48px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);

  .anticon {
    color: @primary-color;
    font-size: 18px;
  }
}

.brand-footer {
  position: absolute;
  bottom: 24px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

/* 右侧注册区域 */
.login-main {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: 32px;

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: @text-primary;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: @text-hint;
  }
}

.login-form {
  :deep(.ant-form-item) {
    margin-bottom: 20px;
  }

  :deep(.ant-input-affix-wrapper) {
    border-radius: 8px;
    padding: 12px 16px;
    border-color: @border-color;

    &:hover,
    &:focus,
    &-focused {
      border-color: @primary-color;
      box-shadow: 0 0 0 2px rgba(200, 164, 77, 0.1);
    }
  }

  :deep(.ant-input-prefix) {
    color: @text-hint;
    margin-right: 12px;
  }
}

.link {
  color: @primary-color;
  cursor: pointer;

  &:hover {
    color: @primary-color-dark;
  }
}

.login-btn {
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, @primary-color, @primary-color-dark);
  border: none;
  box-shadow: 0 4px 12px rgba(200, 164, 77, 0.4);

  &:hover {
    background: linear-gradient(135deg, @primary-color-light, @primary-color);
    box-shadow: 0 6px 16px rgba(200, 164, 77, 0.5);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: @text-hint;
}

/* 响应式 */
@media (max-width: 992px) {
  .login-brand {
    display: none;
  }

  .login-main {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .login-main {
    padding: 24px;
  }

  .login-header h2 {
    font-size: 24px;
  }

  .login-card {
    max-width: 100%;
  }
}
</style>
