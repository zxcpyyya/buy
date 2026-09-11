<template>
  <div class="profile-page safe-area-top">
    <!-- 用户信息头部 -->
    <header class="profile-header">
      <div class="header-bg"></div>
      <div class="user-info" v-if="isLoggedIn">
        <div class="avatar">
          <img src="https://picsum.photos/100/100?random=avatar" alt="头像" />
        </div>
        <div class="user-details">
          <h2 class="username">{{ userInfo.username || '用户' }}</h2>
          <p class="user-level">
            <span class="vip-badge">VIP会员</span>
          </p>
        </div>
        <button class="edit-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
        </button>
      </div>
      
      <!-- 未登录状态 -->
      <div class="login-prompt" v-else>
        <div class="login-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <p class="login-text">登录后享受更多权益</p>
        <div class="login-btns">
          <button class="btn btn-outline" @click="$router.push('/login')">登录</button>
          <button class="btn btn-text" @click="$router.push('/register')">注册</button>
        </div>
      </div>
    </header>

    <!-- 会员卡 -->
    <div class="member-card" v-if="isLoggedIn">
      <div class="card-content">
        <div class="card-left">
          <span class="card-label">会员积分</span>
          <span class="card-value">{{ userPoints }}</span>
        </div>
        <button class="card-btn" @click="$router.push('/points')">积分商城</button>
      </div>
    </div>

    <!-- 订单入口 -->
    <section class="order-section">
      <div class="section-header">
        <h3 class="section-title">我的订单</h3>
        <button class="more-btn" @click="$router.push('/orders')">
          全部订单
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </div>
      <div class="order-tabs">
        <div class="order-tab" @click="$router.push('/orders?status=0')">
          <div class="tab-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <path d="M8 12h8"/>
            </svg>
            <span v-if="orderCounts.pending > 0" class="tab-badge">{{ orderCounts.pending }}</span>
          </div>
          <span class="tab-label">待支付</span>
        </div>
        <div class="order-tab" @click="$router.push('/orders?status=1')">
          <div class="tab-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
            </svg>
            <span v-if="orderCounts.processing > 0" class="tab-badge">{{ orderCounts.processing }}</span>
          </div>
          <span class="tab-label">待发货</span>
        </div>
        <div class="order-tab" @click="$router.push('/orders?status=2')">
          <div class="tab-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="1" y="3" width="15" height="13"/>
              <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
              <circle cx="5.5" cy="18.5" r="2.5"/>
              <circle cx="18.5" cy="18.5" r="2.5"/>
            </svg>
            <span v-if="orderCounts.shipped > 0" class="tab-badge">{{ orderCounts.shipped }}</span>
          </div>
          <span class="tab-label">待收货</span>
        </div>
        <div class="order-tab" @click="$router.push('/orders?status=4')">
          <div class="tab-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
            </svg>
          </div>
          <span class="tab-label">已完成</span>
        </div>
      </div>
    </section>

    <!-- 功能菜单 -->
    <section class="menu-section">
      <div class="menu-group">
        <div class="menu-item" @click="$router.push('/addresses')">
          <div class="menu-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <span class="menu-text">收货地址</span>
          <svg class="menu-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
        <div class="menu-item" @click="$router.push('/coupons')">
          <div class="menu-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
              <line x1="7" y1="7" x2="7.01" y2="7"/>
            </svg>
          </div>
          <span class="menu-text">优惠券</span>
          <span class="menu-badge">{{ couponCount }}张</span>
          <svg class="menu-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
        <div class="menu-item" @click="$router.push('/points')">
          <div class="menu-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
          </div>
          <span class="menu-text">我的积分</span>
          <span class="menu-value">{{ userPoints }}分</span>
          <svg class="menu-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </div>

      <div class="menu-group">
        <div class="menu-item">
          <div class="menu-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
          </div>
          <span class="menu-text">设置</span>
          <svg class="menu-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
        <div class="menu-item">
          <div class="menu-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
          </div>
          <span class="menu-text">帮助与反馈</span>
          <svg class="menu-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
        <div class="menu-item">
          <div class="menu-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
          </div>
          <span class="menu-text">关于我们</span>
          <svg class="menu-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </div>

      <!-- 退出登录 -->
      <button class="logout-btn" @click="handleLogout" v-if="isLoggedIn">
        退出登录
      </button>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const userInfo = computed(() => authStore.userInfo)
const userPoints = ref(1280)
const couponCount = ref(5)

const orderCounts = ref({
  pending: 2,
  processing: 1,
  shipped: 3,
  completed: 12
})

const handleLogout = () => {
  authStore.logout()
  showToast('已退出登录')
  router.replace('/')
}

onMounted(() => {
  if (isLoggedIn.value) {
    authStore.fetchUserInfo()
  }
})
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-secondary);
  padding-bottom: 30px;
}

.profile-header {
  position: relative;
  background: linear-gradient(135deg, #1d1d1f 0%, #424245 100%);
  padding: 60px 20px 30px;
  color: white;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, transparent 100%);
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(255,255,255,0.2);
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.user-details {
  flex: 1;
}

.username {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
}

.vip-badge {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, #ffd700 0%, #ff8c00 100%);
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  color: #1d1d1f;
}

.edit-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.1);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-prompt {
  text-align: center;
  padding: 20px 0;
}

.login-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-text {
  font-size: 16px;
  margin-bottom: 20px;
  opacity: 0.9;
}

.login-btns {
  display: flex;
  justify-content: center;
  gap: 16px;
  
  .btn {
    padding: 10px 24px;
    font-size: 14px;
  }
  
  .btn-outline {
    border-color: white;
    color: white;
  }
  
  .btn-text {
    color: white;
  }
}

.member-card {
  margin: -20px 16px 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 100%);
  border-radius: var(--radius-xl);
  padding: 16px 20px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -20px;
    right: -20px;
    width: 100px;
    height: 100px;
    background: rgba(255,255,255,0.1);
    border-radius: 50%;
  }
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.card-label {
  font-size: 13px;
  opacity: 0.9;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  display: block;
  margin-top: 4px;
}

.card-btn {
  padding: 8px 16px;
  background: rgba(255,255,255,0.2);
  border: none;
  border-radius: var(--radius-full);
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.order-section {
  margin: 24px 16px;
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--bg-secondary);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.more-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  background: none;
  border: none;
  cursor: pointer;
}

.order-tabs {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.order-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.tab-icon {
  position: relative;
  color: var(--text-primary);
}

.tab-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--apple-red);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.menu-section {
  padding: 0 16px;
}

.menu-group {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  margin-bottom: 16px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:active {
    background: var(--bg-secondary);
  }
  
  &:not(:last-child) {
    border-bottom: 1px solid var(--bg-secondary);
  }
}

.menu-icon {
  width: 32px;
  height: 32px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--apple-blue);
}

.menu-text {
  flex: 1;
  font-size: 15px;
}

.menu-badge, .menu-value {
  font-size: 13px;
  color: var(--text-secondary);
}

.menu-badge {
  margin-right: 4px;
}

.menu-value {
  margin-right: 8px;
  color: var(--apple-orange);
  font-weight: 600;
}

.menu-arrow {
  color: var(--text-tertiary);
}

.logout-btn {
  width: 100%;
  padding: 16px;
  background: var(--bg-primary);
  border: none;
  font-size: 16px;
  color: var(--apple-red);
  cursor: pointer;
  border-radius: var(--radius-xl);
  
  &:active {
    background: var(--bg-secondary);
  }
}
</style>
