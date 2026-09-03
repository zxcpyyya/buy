<template>
  <div class="profile-page">
    <!-- 用户卡片 -->
    <section class="user-card">
      <div class="user-info">
        <img :src="userInfo?.avatar || defaultAvatar" :alt="userInfo?.nickname" class="user-avatar" />
        <div class="user-meta">
          <h1 class="user-name">{{ userInfo?.nickname || '未设置' }}</h1>
          <p class="user-phone">{{ userInfo?.phone || '未绑定手机' }}</p>
        </div>
      </div>
      <button class="btn-edit" @click="showEditDialog = true">编辑资料</button>
    </section>
    
    <!-- 功能区 -->
    <section class="container-large">
      <div class="menu-grid">
        <router-link to="/orders" class="menu-card">
          <div class="menu-icon">
            <svg viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="6" y="6" width="20" height="22" rx="2"/>
              <path d="M11 11h10M11 16h10M11 21h6"/>
            </svg>
          </div>
          <h3>我的订单</h3>
          <p>查看全部订单</p>
        </router-link>
        
        <router-link to="/cart" class="menu-card">
          <div class="menu-icon">
            <svg viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M8 10h16l-2 16a2 2 0 01-2 1.8H12a2 2 0 01-2-1.8L8 10z"/>
              <path d="M12 10V7a4 4 0 018 0v3"/>
            </svg>
          </div>
          <h3>购物袋</h3>
          <p>{{ cartCount }} 件商品</p>
        </router-link>
        
        <router-link to="/addresses" class="menu-card">
          <div class="menu-icon">
            <svg viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M16 4c-5 0-9 4-9 9 0 7 9 15 9 15s9-8 9-15c0-5-4-9-9-9z"/>
              <circle cx="16" cy="13" r="3"/>
            </svg>
          </div>
          <h3>收货地址</h3>
          <p>管理收货地址</p>
        </router-link>
        
        <div class="menu-card" @click="changePassword">
          <div class="menu-icon">
            <svg viewBox="0 0 32 32" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="6" y="14" width="20" height="14" rx="2"/>
              <path d="M10 14V9a6 6 0 0112 0v5"/>
              <circle cx="16" cy="21" r="1.5"/>
            </svg>
          </div>
          <h3>修改密码</h3>
          <p>保障账号安全</p>
        </div>
      </div>
      
      <!-- 设置列表 -->
      <div class="settings-list">
        <div class="setting-item">
          <span>账号信息</span>
          <span class="value">{{ userInfo?.username }}</span>
        </div>
        <div class="setting-item">
          <span>邮箱</span>
          <span class="value">{{ userInfo?.email || '未设置' }}</span>
        </div>
        <div class="setting-item">
          <span>性别</span>
          <span class="value">{{ genderText(userInfo?.gender) }}</span>
        </div>
        <div class="setting-item">
          <span>注册时间</span>
          <span class="value">{{ formatTime(userInfo?.createTime) }}</span>
        </div>
      </div>
      
      <!-- 退出登录 -->
      <button class="btn-logout" @click="handleLogout">退出登录</button>
    </section>
    
    <!-- 编辑资料弹窗 -->
    <van-dialog
      v-model:show="showEditDialog"
      title="编辑资料"
      :show-cancel-button="false"
      :show-confirm-button="false"
      close-on-click-overlay
    >
      <div class="dialog-form">
        <div class="form-item">
          <label>昵称</label>
          <input v-model="editForm.nickname" type="text" placeholder="请输入昵称" />
        </div>
        <div class="form-item">
          <label>邮箱</label>
          <input v-model="editForm.email" type="text" placeholder="请输入邮箱" />
        </div>
        <div class="form-item">
          <label>手机</label>
          <input v-model="editForm.phone" type="text" placeholder="请输入手机号" maxlength="11" />
        </div>
        <div class="form-item">
          <label>性别</label>
          <div class="radio-group">
            <label><input type="radio" :value="1" v-model="editForm.gender" /> 男</label>
            <label><input type="radio" :value="2" v-model="editForm.gender" /> 女</label>
            <label><input type="radio" :value="0" v-model="editForm.gender" /> 保密</label>
          </div>
        </div>
        <div class="form-actions">
          <button class="btn-cancel" @click="showEditDialog = false">取消</button>
          <button class="btn-save" @click="handleSave">保存</button>
        </div>
      </div>
    </van-dialog>
    
    <!-- 修改密码弹窗 -->
    <van-dialog
      v-model:show="showPwdDialog"
      title="修改密码"
      :show-cancel-button="false"
      :show-confirm-button="false"
      close-on-click-overlay
    >
      <div class="dialog-form">
        <div class="form-item">
          <label>原密码</label>
          <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" />
        </div>
        <div class="form-item">
          <label>新密码</label>
          <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" />
        </div>
        <div class="form-item">
          <label>确认</label>
          <input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" />
        </div>
        <div class="form-actions">
          <button class="btn-cancel" @click="showPwdDialog = false">取消</button>
          <button class="btn-save" @click="handleChangePassword">保存</button>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const userInfo = computed(() => userStore.userInfo || {})
const cartCount = computed(() => cartStore.totalCount)

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI1MCIgZmlsbD0iI2Y1ZjVmNyIvPjwvc3ZnPg=='

const showEditDialog = ref(false)
const showPwdDialog = ref(false)

const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 0
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const genderText = (gender) => {
  const map = { 0: '保密', 1: '男', 2: '女' }
  return map[gender] || '保密'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN')
}

const openEditDialog = () => {
  Object.assign(editForm, {
    nickname: userInfo.value.nickname || '',
    email: userInfo.value.email || '',
    phone: userInfo.value.phone || '',
    gender: userInfo.value.gender || 0
  })
  showEditDialog.value = true
}

const handleSave = async () => {
  try {
    await request.put('/user/info', editForm)
    await userStore.fetchUserInfo()
    showToast('保存成功')
    showEditDialog.value = false
  } catch (e) {
    console.error('保存失败', e)
  }
}

const changePassword = () => {
  Object.keys(pwdForm).forEach(k => pwdForm[k] = '')
  showPwdDialog.value = true
}

const handleChangePassword = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }
  
  if (pwdForm.newPassword.length < 6) {
    showToast('密码不能少于6位')
    return
  }
  
  try {
    await request.put('/user/password', null, {
      params: {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      }
    })
    showToast('密码修改成功，请重新登录')
    showPwdDialog.value = false
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1000)
  } catch (e) {
    console.error('修改密码失败', e)
  }
}

const handleLogout = async () => {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出登录吗？' })
  } catch { return }
  
  userStore.logout()
  cartStore.clearCart()
  showToast('已退出登录')
  setTimeout(() => router.push('/login'), 600)
}

onMounted(async () => {
  await userStore.fetchUserInfo()
  cartStore.fetchCartCount()
})
</script>

<style lang="scss" scoped>
.profile-page {
  padding-top: 44px;
  padding-bottom: 80px;
  background: #fbfbfd;
  min-height: 100vh;
}

// 用户卡片
.user-card {
  background: linear-gradient(135deg, #0071e3 0%, #0077ed 100%);
  padding: 80px 22px 48px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  object-fit: cover;
}

.user-meta {
  .user-name {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  
  .user-phone {
    font-size: 14px;
    opacity: 0.85;
  }
}

.btn-edit {
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border-radius: 980px;
  font-size: 13px;
  backdrop-filter: blur(10px);
  transition: background 0.2s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }
}

// 菜单
.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: -24px;
  margin-bottom: 24px;
  
  @media (max-width: 833px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.menu-card {
  background: #fff;
  border-radius: 18px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
  }
  
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #1d1d1f;
    margin-bottom: 4px;
  }
  
  p {
    font-size: 12px;
    color: #6e6e73;
  }
}

.menu-icon {
  width: 36px;
  height: 36px;
  margin: 0 auto 12px;
  color: #0071e3;
}

// 设置
.settings-list {
  background: #fff;
  border-radius: 18px;
  padding: 8px 24px;
  margin-bottom: 24px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  font-size: 15px;
  border-bottom: 1px solid #f5f5f7;
  
  &:last-child {
    border-bottom: none;
  }
  
  .value {
    color: #6e6e73;
  }
}

// 退出
.btn-logout {
  display: block;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  height: 48px;
  background: #fff;
  color: #ff3b30;
  border-radius: 980px;
  font-size: 15px;
  font-weight: 500;
  transition: background 0.2s;
  
  &:hover {
    background: #fff5f5;
  }
}

// 弹窗表单
.dialog-form {
  padding: 8px 16px;
}

.form-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f7;
  
  label {
    flex-shrink: 0;
    width: 80px;
    font-size: 14px;
    color: #1d1d1f;
  }
  
  input {
    flex: 1;
    padding: 8px 0;
    border: none;
    font-size: 14px;
    color: #1d1d1f;
    background: transparent;
    
    &::placeholder {
      color: #86868b;
    }
  }
}

.radio-group {
  display: flex;
  gap: 16px;
  flex: 1;
  
  label {
    width: auto;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #1d1d1f;
  }
}

.form-actions {
  display: flex;
  gap: 8px;
  padding: 16px 0;
  
  button {
    flex: 1;
    height: 44px;
    border-radius: 980px;
    font-size: 15px;
  }
}

.btn-cancel {
  background: #f5f5f7;
  color: #1d1d1f;
}

.btn-save {
  background: #0071e3;
  color: #fff;
}
</style>
