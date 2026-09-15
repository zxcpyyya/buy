<template>
  <div class="register-page safe-area-top">
    <div class="register-container">
      <!-- 返回按钮 -->
      <button class="back-btn" @click="$router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </button>

      <!-- 标题 -->
      <div class="title-section">
        <h1 class="title">创建账户</h1>
        <p class="subtitle">加入 Store，开始购物之旅</p>
      </div>

      <!-- 注册表单 -->
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="input-group">
          <label>用户名</label>
          <input 
            v-model="form.username"
            type="text"
            class="input"
            placeholder="请输入用户名"
            autocomplete="username"
            required
          />
        </div>

        <div class="input-group">
          <label>邮箱</label>
          <input 
            v-model="form.email"
            type="email"
            class="input"
            placeholder="请输入邮箱"
            autocomplete="email"
            required
          />
        </div>

        <div class="input-group">
          <label>手机号</label>
          <input 
            v-model="form.phone"
            type="tel"
            class="input"
            placeholder="请输入手机号"
            autocomplete="tel"
            required
          />
        </div>

        <div class="input-group">
          <label>密码</label>
          <div class="password-wrapper">
            <input 
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="input"
              placeholder="请设置密码（至少6位）"
              autocomplete="new-password"
              required
              minlength="6"
            />
            <button 
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
            >
              <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="input-group">
          <label>确认密码</label>
          <input 
            v-model="form.confirmPassword"
            :type="showPassword ? 'text' : 'password'"
            class="input"
            placeholder="请再次输入密码"
            autocomplete="new-password"
            required
          />
        </div>

        <div class="terms">
          <label class="checkbox-label">
            <input type="checkbox" v-model="agreeTerms" required />
            <span>我已阅读并同意 <a href="#">《用户协议》</a> 和 <a href="#">《隐私政策》</a></span>
          </label>
        </div>

        <button 
          type="submit" 
          class="btn btn-primary btn-full"
          :disabled="loading || !agreeTerms"
        >
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>创建账户</span>
        </button>
      </form>

      <!-- 登录链接 -->
      <div class="login-section">
        <p>已有账户？</p>
        <router-link to="/login" class="login-link">立即登录 ›</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const agreeTerms = ref(false)
const loading = ref(false)

const handleRegister = async () => {
  // 验证密码
  if (form.value.password !== form.value.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }

  if (form.value.password.length < 6) {
    showToast('密码长度至少6位')
    return
  }

  loading.value = true
  try {
    await authStore.register({
      username: form.value.username,
      password: form.value.password,
      email: form.value.email,
      phone: form.value.phone
    })
    showToast('注册成功')
    
    // 跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 500)
  } catch (error) {
    console.error('注册失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 0 20px;
  padding-bottom: env(safe-area-inset-bottom);
}

.register-container {
  max-width: 400px;
  margin: 0 auto;
  padding-top: 20px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--bg-secondary);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  transition: background 0.2s;
  
  &:hover {
    background: #e8e8ed;
  }
}

.title-section {
  margin-bottom: 40px;
}

.title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 15px;
  color: var(--text-secondary);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  label {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-primary);
  }
}

.password-wrapper {
  position: relative;
  
  .input {
    padding-right: 48px;
  }
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 4px;
  
  &:hover {
    color: var(--text-secondary);
  }
}

.terms {
  .checkbox-label {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    font-size: 13px;
    color: var(--text-secondary);
    cursor: pointer;
    
    input[type="checkbox"] {
      width: 18px;
      height: 18px;
      margin-top: 2px;
      border-radius: 4px;
      accent-color: var(--apple-blue);
    }
    
    a {
      color: var(--apple-blue);
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.btn-full {
  width: 100%;
  height: 50px;
  font-size: 17px;
  font-weight: 500;
  margin-top: 8px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.login-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 40px;
  font-size: 14px;
  color: var(--text-secondary);
}

.login-link {
  color: var(--apple-blue);
  font-weight: 500;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}
</style>
