<template>
  <div class="login-wrapper">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-logo">
          <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
            <rect width="56" height="56" rx="14" fill="white" fill-opacity="0.15"/>
            <path d="M28 12C20.268 12 14 18.268 14 26C14 31.5 17.2 36.3 22 38.6V44H34V38.6C38.8 36.3 42 31.5 42 26C42 18.268 35.732 12 28 12Z" fill="white"/>
            <circle cx="28" cy="26" r="4" fill="#5AC8FA"/>
          </svg>
        </div>
        <h1 class="brand-title">Apple Mall</h1>
        <p class="brand-subtitle">优质商品，放心购</p>
        
        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                <line x1="3" y1="6" x2="21" y2="6"/>
                <path d="M16 10a4 4 0 0 1-8 0"/>
              </svg>
            </div>
            <span>精选好物，品质保障</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="1" y="3" width="15" height="13"/>
                <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
                <circle cx="5.5" cy="18.5" r="2.5"/>
                <circle cx="18.5" cy="18.5" r="2.5"/>
              </svg>
            </div>
            <span>快速配送，极速到达</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
            </div>
            <span>安全支付，放心购物</span>
          </div>
        </div>
      </div>
      
      <!-- 背景装饰 -->
      <div class="left-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="login-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-desc">登录您的账户，继续购物</p>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrapper" :class="{ 'has-error': errors.username }">
              <svg class="input-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input
                v-model="form.username"
                type="text"
                class="form-input"
                placeholder="请输入用户名"
                @input="clearError('username')"
              />
            </div>
            <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper" :class="{ 'has-error': errors.password }">
              <svg class="input-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="请输入密码"
                @input="clearError('password')"
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
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
          </div>

          <button
            type="submit"
            class="btn-submit"
            :class="{ 'is-loading': loading }"
            :disabled="loading"
          >
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>登 录</span>
          </button>

          <p v-if="errorMessage" class="global-error">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ errorMessage }}
          </p>
        </form>

        <div class="login-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const errors = reactive({
  username: '',
  password: ''
})

const form = reactive({
  username: '',
  password: ''
})

const clearError = (field) => {
  errors[field] = ''
  errorMessage.value = ''
}

const validate = () => {
  let isValid = true
  
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  }
  
  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  }
  
  return isValid
}

const handleLogin = async () => {
  if (!validate()) return
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const data = await request.post('/user/login', form)
    
    // 保存Token
    userStore.setToken(data.token)
    
    // 获取用户信息
    await userStore.fetchUserInfo()
    
    // 刷新购物车
    await cartStore.fetchCartCount()
    
    showToast({ message: '登录成功', icon: 'success' })
    
    // 跳转到原来页面或首页
    const redirect = route.query.redirect || '/'
    setTimeout(() => router.push(redirect), 600)
  } catch (e) {
    console.error('登录失败', e)
    errorMessage.value = e.message || '用户名或密码错误'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* === 布局 === */
.login-wrapper {
  display: flex;
  min-height: 100vh;
}

/* === 左侧装饰区域 === */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 50%, #FFB347 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  padding: 48px;
}

.brand-logo {
  margin-bottom: 24px;
}

.brand-logo svg {
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.brand-title {
  font-size: 36px;
  font-weight: 600;
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 48px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 15px;
  opacity: 0.95;
}

.feature-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 背景装饰 */
.left-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  bottom: 30%;
  right: 10%;
}

/* === 右侧表单区域 === */
.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 48px;
}

@media (max-width: 900px) {
  .login-right {
    width: 100%;
    padding: 32px 24px;
  }
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
}

.login-header {
  margin-bottom: 40px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
  letter-spacing: -0.01em;
}

.login-desc {
  font-size: 15px;
  color: #6e6e73;
}

/* === 表单样式 === */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1.5px solid #d2d2d7;
  border-radius: 10px;
  transition: all 0.2s ease;
  background: #ffffff;
}

.input-wrapper:focus-within {
  border-color: #FF6B6B;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.12);
}

.input-wrapper.has-error {
  border-color: #ff3b30;
}

.input-wrapper.has-error:focus-within {
  box-shadow: 0 0 0 3px rgba(255, 59, 48, 0.12);
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #86868b;
  pointer-events: none;
}

.form-input {
  width: 100%;
  padding: 14px 14px 14px 46px;
  border: none;
  outline: none;
  font-size: 15px;
  color: #1d1d1f;
  background: transparent;
}

.form-input::placeholder {
  color: #86868b;
}

.password-toggle {
  position: absolute;
  right: 12px;
  padding: 6px;
  color: #86868b;
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.password-toggle:hover {
  color: #6e6e73;
  background: #f5f5f7;
}

.error-text {
  font-size: 13px;
  color: #ff3b30;
  margin-top: 4px;
}

/* === 提交按钮 === */
.btn-submit {
  width: 100%;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 500;
  color: #ffffff;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-submit.is-loading {
  background: #ffb347;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* === 全局错误 === */
.global-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #ffebea;
  border-radius: 8px;
  color: #ff3b30;
  font-size: 14px;
}

/* === 底部 === */
.login-footer {
  margin-top: 32px;
  text-align: center;
}

.login-footer p {
  font-size: 14px;
  color: #6e6e73;
}

.login-footer a {
  color: #FF6B6B;
  font-weight: 500;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
