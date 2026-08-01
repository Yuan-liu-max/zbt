<template>
  <div class="login-container">
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <svg viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="60" height="60" rx="14" fill="url(#login-gold-grad)"/>
            <path d="M30 14L20 26h20L30 14z" fill="#fff" opacity="0.95"/>
            <path d="M17 28l13 18 13-18H17z" fill="#fff" opacity="0.7"/>
            <defs>
              <linearGradient id="login-gold-grad" x1="0" y1="0" x2="60" y2="60">
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

    <!-- 右侧登录区域 -->
    <div class="login-main">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>珠宝通珠宝行业管理系统</p>
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

          <a-form-item name="password">
            <a-input-password
              v-model:value="formData.password"
              size="large"
              placeholder="请输入密码"
              @pressEnter="handleLogin"
            >
              <template #prefix><LockOutlined /></template>
            </a-input-password>
          </a-form-item>

          <a-form-item>
            <div class="login-options">
              <a-checkbox v-model:checked="rememberMe">记住我</a-checkbox>
              <a class="forgot-link">忘记密码？</a>
            </div>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              block
              size="large"
              :loading="loginLoading"
              @click="handleLogin"
              class="login-btn"
            >
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">立即注册</router-link>
          <span style="margin: 0 8px">|</span>
          <span>技术支持：珠宝通科技</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loginLoading = ref(false)
const rememberMe = ref(false)
const currentYear = new Date().getFullYear()

const formData = reactive({
  username: '',
  password: ''
})

const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value?.validateFields()
    loginLoading.value = true

    const data = await authStore.login(formData.username, formData.password)

    message.success('登录成功！')
    router.push('/dashboard')
  } catch (error: any) {
    // 错误已由 request.ts 拦截器统一提示
  } finally {
    loginLoading.value = false
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

/* 右侧登录区域 */
.login-main {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 40px;

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
    margin-bottom: 24px;
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

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-link {
  font-size: 13px;
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
  margin-top: 32px;
  font-size: 12px;
  color: @text-hint;
}

.link {
  color: @primary-color;
  text-decoration: none;
  cursor: pointer;

  &:hover {
    color: @primary-color-dark;
  }
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
