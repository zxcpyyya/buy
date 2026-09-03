<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <h1 class="register-title">注册</h1>
        <p class="register-subtitle">创建您的账号</p>
        
        <form class="register-form" @submit.prevent="handleRegister">
          <div class="form-item">
            <label>用户名 *</label>
            <input 
              v-model="form.username"
              type="text"
              placeholder="4-20位字符"
              autocomplete="username"
            />
          </div>
          
          <div class="form-item">
            <label>昵称</label>
            <input 
              v-model="form.nickname"
              type="text"
              placeholder="选填"
            />
          </div>
          
          <div class="form-item">
            <label>密码 *</label>
            <input 
              v-model="form.password"
              type="password"
              placeholder="至少6位"
              autocomplete="new-password"
            />
          </div>
          
          <div class="form-item">
            <label>确认密码 *</label>
            <input 
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              autocomplete="new-password"
            />
          </div>
          
          <div class="form-item">
            <label>邮箱</label>
            <input 
              v-model="form.email"
              type="email"
              placeholder="选填"
            />
          </div>
          
          <div class="form-item">
            <label>手机号</label>
            <input 
              v-model="form.phone"
              type="text"
              placeholder="选填"
              maxlength="11"
            />
          </div>
          
          <button type="submit" class="btn-submit" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

const handleRegister = async () => {
  // 校验
  if (form.username.length < 4) {
    showToast('用户名长度不能少于4位')
    return
  }
  
  if (form.password.length < 6) {
    showToast('密码长度不能少于6位')
    return
  }
  
  if (form.password !== form.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }
  
  if (form.email && !/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(form.email)) {
    showToast('邮箱格式不正确')
    return
  }
  
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    showToast('手机号格式不正确')
    return
  }
  
  loading.value = true
  try {
    await request.post('/user/register', form)
    
    // 自动登录
    userStore.setToken((await request.post('/user/login', {
      username: form.username,
      password: form.password
    })).token)
    
    await userStore.fetchUserInfo()
    await cartStore.fetchCartCount()
    
    showToast({ message: '注册成功', icon: 'success' })
    setTimeout(() => router.push('/'), 600)
  } catch (e) {
    console.error('注册失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22px;
}

.register-container {
  width: 100%;
  max-width: 460px;
}

.register-card {
  background: #fff;
  border-radius: 24px;
  padding: 48px 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

.register-title {
  font-size: 32px;
  font-weight: 600;
  text-align: center;
  color: #1d1d1f;
  letter-spacing: -0.01em;
  margin-bottom: 4px;
}

.register-subtitle {
  font-size: 15px;
  text-align: center;
  color: #6e6e73;
  margin-bottom: 32px;
}

.register-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  
  .form-item:nth-child(5),
  .form-item:nth-child(6),
  .btn-submit {
    grid-column: 1 / -1;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  
  label {
    font-size: 13px;
    font-weight: 500;
    color: #1d1d1f;
  }
  
  input {
    padding: 10px 14px;
    background: #f5f5f7;
    border: 1px solid transparent;
    border-radius: 10px;
    font-size: 14px;
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

.register-footer {
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
