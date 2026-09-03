<template>
  <header class="app-header" :class="{ scrolled: isScrolled }">
    <nav class="nav-container">
      <router-link to="/" class="nav-logo">Mall</router-link>
      
      <ul class="nav-menu">
        <li><router-link to="/">首页</router-link></li>
        <li><router-link to="/products">所有产品</router-link></li>
        <li><a href="#" @click.prevent="goCategory(1)">数码电子</a></li>
        <li><a href="#" @click.prevent="goCategory(2)">服装鞋包</a></li>
        <li><a href="#" @click.prevent="goCategory(3)">食品生鲜</a></li>
        <li><a href="#" @click.prevent="goCategory(4)">家居百货</a></li>
      </ul>
      
      <div class="nav-actions">
        <button class="icon-btn" @click="$router.push('/cart')" aria-label="购物袋">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M6 7h12l-1.5 12.5a1.5 1.5 0 01-1.5 1.4H9a1.5 1.5 0 01-1.5-1.4L6 7z"/>
            <path d="M9 7V5a3 3 0 016 0v2"/>
          </svg>
          <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
        </button>
        
        <button v-if="!isLogin" class="icon-btn" @click="$router.push('/login')" aria-label="登录">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="8" r="4"/>
            <path d="M4 21v-1a6 6 0 016-6h4a6 6 0 016 6v1"/>
          </svg>
        </button>
        
        <div v-else class="user-menu" @click="$router.push('/profile')">
          <img :src="userInfo?.avatar || defaultAvatar" :alt="userInfo?.nickname" />
        </div>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isScrolled = ref(false)
const isLogin = computed(() => userStore.isLogin)
const userInfo = computed(() => userStore.userInfo)
const cartCount = computed(() => cartStore.totalCount)
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI1MCIgZmlsbD0iI2Y1ZjVmNyIvPjwvc3ZnPg=='

const goCategory = (id) => router.push(`/category/${id}`)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  if (isLogin.value) {
    cartStore.fetchCartCount()
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 44px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  z-index: 999;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  border-bottom: 1px solid transparent;
  
  &.scrolled {
    border-bottom-color: rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.92);
  }
}

.nav-container {
  max-width: 1024px;
  margin: 0 auto;
  height: 100%;
  padding: 0 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  font-size: 21px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.02em;
  
  &:hover {
    color: #1d1d1f;
  }
}

.nav-menu {
  display: flex;
  gap: 24px;
  list-style: none;
  
  li a {
    font-size: 14px;
    font-weight: 400;
    color: #1d1d1f;
    opacity: 0.85;
    transition: opacity 0.2s;
    
    &:hover {
      opacity: 1;
      color: #1d1d1f;
    }
    
    &.router-link-active {
      opacity: 1;
    }
  }
  
  @media (max-width: 833px) {
    display: none;
  }
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #1d1d1f;
  transition: background 0.2s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

.cart-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: #ff3b30;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-menu {
  width: 32px;
  height: 32px;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
