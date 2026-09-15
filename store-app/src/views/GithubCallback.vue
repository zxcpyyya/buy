<template>
  <div class="github-callback-page">
    <div class="loading-container">
      <div v-if="loading" class="loading-content">
        <div class="loading-spinner"></div>
        <p class="loading-text">正在处理 GitHub 登录...</p>
      </div>
      
      <div v-if="error" class="error-content">
        <div class="error-icon">⚠️</div>
        <p class="error-text">{{ error }}</p>
        <button class="btn btn-primary" @click="goToLogin">返回登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(true)
const error = ref('')

onMounted(async () => {
  const { code, state } = route.query
  
  // 检查参数
  if (!code || !state) {
    error.value = '授权参数不完整'
    loading.value = false
    return
  }
  
  // 验证 state（防止 CSRF）
  const savedState = localStorage.getItem('github_oauth_state')
  if (state !== savedState) {
    error.value = '授权验证失败，请重试'
    loading.value = false
    // 清理 localStorage
    localStorage.removeItem('github_oauth_state')
    localStorage.removeItem('github_oauth_redirect')
    return
  }
  
  try {
    // 调用后端完成登录
    const result = await authStore.githubLogin(code, state)
    
    // 清理 localStorage
    localStorage.removeItem('github_oauth_state')
    localStorage.removeItem('github_oauth_redirect')
    
    // 登录成功提示
    if (result.isNewUser) {
      showToast('欢迎！您已使用 GitHub 账号登录')
    } else {
      showToast('登录成功')
    }
    
    // 跳转到之前的页面或首页
    const redirect = localStorage.getItem('github_oauth_redirect') || '/'
    setTimeout(() => {
      router.push(redirect)
    }, 500)
    
  } catch (err) {
    console.error('GitHub 登录失败', err)
    error.value = err.message || '登录失败，请重试'
    loading.value = false
  }
})

const goToLogin = () => {
  localStorage.removeItem('github_oauth_state')
  localStorage.removeItem('github_oauth_redirect')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.github-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  padding: 20px;
}

.loading-container {
  text-align: center;
}

.loading-content,
.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--bg-secondary);
  border-top-color: var(--apple-blue);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 16px;
  color: var(--text-secondary);
}

.error-icon {
  font-size: 48px;
}

.error-text {
  font-size: 16px;
  color: var(--text-secondary);
  text-align: center;
  max-width: 280px;
}

.btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--apple-blue);
  color: white;
  
  &:hover {
    opacity: 0.9;
  }
  
  &:active {
    opacity: 0.8;
  }
}
</style>
