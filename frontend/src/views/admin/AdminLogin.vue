<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Logo区域 -->
      <div class="login-header">
        <div class="login-logo">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="url(#gradient)"/>
            <path d="M14 24C14 18.477 18.477 14 24 14V14C29.523 14 34 18.477 34 24V34H14V24Z" fill="white" fill-opacity="0.9"/>
            <circle cx="24" cy="24" r="4" fill="#0071E3"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="48" y2="48" gradientUnits="userSpaceOnUse">
                <stop stop-color="#5AC8FA"/>
                <stop offset="1" stop-color="#0071E3"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="login-title">商城管理系统</h1>
        <p class="login-subtitle">请登录以继续管理您的商城</p>
      </div>

      <!-- 登录表单 -->
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label required">用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
            autocomplete="username"
          />
        </div>

        <div class="form-group">
          <label class="form-label required">密码</label>
          <div class="password-input-wrapper">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
            <button
              type="button"
              class="password-toggle"
              @click="showPassword = !showPassword"
            >
              <svg v-if="!showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input v-model="form.remember" type="checkbox" />
            <span>记住我</span>
          </label>
          <a href="#" class="forgot-link">忘记密码？</a>
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-lg w-full"
          :disabled="loading"
        >
          <span v-if="loading" class="loading"></span>
          <span v-else>登录</span>
        </button>

        <p v-if="error" class="error-message">{{ error }}</p>
      </form>

      <!-- 底部信息 -->
      <div class="login-footer">
        <p>登录即表示您同意我们的 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a></p>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-gradient"></div>
      <div class="bg-pattern"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '@/api/admin'

const router = useRouter()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

const handleLogin = async () => {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const res = await adminLogin({
      username: form.username,
      password: form.password
    })

    if (res.code === 200) {
      // 保存token
      localStorage.setItem('admin_token', res.data.token)
      localStorage.setItem('admin_user', JSON.stringify(res.data.userInfo))
      
      // 跳转到首页
      router.push('/admin/dashboard')
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (err) {
    error.value = err.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: var(--spacing-2xl);
  background: var(--bg-secondary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  position: relative;
  z-index: 10;
  margin: var(--spacing-lg);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.login-logo {
  margin-bottom: var(--spacing-lg);
}

.login-logo svg {
  width: 64px;
  height: 64px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.02em;
  margin-bottom: var(--spacing-sm);
}

.login-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
}

.login-form {
  margin-top: var(--spacing-xl);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.password-input-wrapper {
  position: relative;
}

.password-input-wrapper .form-input {
  padding-right: 48px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover {
  color: var(--text-secondary);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.forgot-link {
  font-size: var(--font-size-sm);
  color: var(--text-link);
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.error-message {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  background: #FFEBE9;
  border-radius: var(--border-radius-sm);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
  text-align: center;
}

.login-footer {
  margin-top: var(--spacing-xl);
  text-align: center;
}

.login-footer p {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.login-footer a {
  color: var(--text-link);
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}

/* 背景装饰 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  top: -50%;
  right: -30%;
  width: 80%;
  height: 150%;
  background: radial-gradient(ellipse at center, rgba(0, 113, 227, 0.08) 0%, transparent 70%);
  animation: float 20s ease-in-out infinite;
}

.bg-pattern {
  position: absolute;
  bottom: -20%;
  left: -20%;
  width: 60%;
  height: 100%;
  background: radial-gradient(ellipse at center, rgba(90, 200, 250, 0.06) 0%, transparent 70%);
  animation: float 25s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(30px, -30px);
  }
}

/* 响应式 */
@media (max-width: 480px) {
  .login-container {
    padding: var(--spacing-lg);
    margin: var(--spacing-md);
  }

  .login-title {
    font-size: 24px;
  }
}
</style>
