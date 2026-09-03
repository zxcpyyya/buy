<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <h1 class="login-title">登录</h1>
        <p class="login-subtitle">欢迎回来</p>
        
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-item">
            <label>用户名</label>
            <input 
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>
          
          <div class="form-item">
            <label>密码</label>
            <input 
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </div>
          
          <button type="submit" class="btn-submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
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
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username || !form.password) {
    showToast('请填写完整的登录信息')
    return
  }
  
  loading.value = true
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
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22px;
}

.login-container {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: #fff;
  border-radius: 24px;
  padding: 48px 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  text-align: center;
  color: #1d1d1f;
  letter-spacing: -0.01em;
  margin-bottom: 4px;
}

.login-subtitle {
  font-size: 15px;
  text-align: center;
  color: #6e6e73;
  margin-bottom: 32px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
  
  label {
    font-size: 13px;
    font-weight: 500;
    color: #1d1d1f;
  }
  
  input {
    padding: 12px 16px;
    background: #f5f5f7;
    border: 1px solid transparent;
    border-radius: 12px;
    font-size: 15px;
    color: #1d1d1f;
    transition: all 0.2s;
    
    &::placeholder {
      color: #86868b;
    }
    
    &:focus {
      background: #fff;
      border-color: #0071e3;
    }
  }
}

.btn-submit {
  width: 100%;
  height: 48px;
  background: #0071e3;
  color: #fff;
  border-radius: 980px;
  font-size: 17px;
  font-weight: 500;
  margin-top: 16px;
  transition: background 0.2s;
  
  &:hover:not(:disabled) {
    background: #0077ed;
  }
  
  &:disabled {
    background: #d2d2d7;
    cursor: not-allowed;
  }
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #6e6e73;
  
  a {
    color: #0071e3;
    font-weight: 500;
    margin-left: 4px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
