<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg width="24" height="24" viewBox="0 0 48 48" fill="none">
            <path d="M14 24C14 18.477 18.477 14 24 14V14C29.523 14 34 18.477 34 24V34H14V24Z" fill="white" fill-opacity="0.9"/>
            <circle cx="24" cy="24" r="4" fill="#0071E3"/>
          </svg>
        </div>
        <span v-if="!sidebarCollapsed" class="logo-text">商城管理</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <!-- 仪表盘 - 所有人可见 -->
        <div class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-section-title">概览</div>
          <router-link
            to="/admin/dashboard"
            class="nav-item"
            :class="{ active: isActive('/admin/dashboard') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/>
                <rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">仪表盘</span>
          </router-link>
        </div>

        <!-- 系统管理 - 只有超管可见 -->
        <div v-if="isSuperAdmin" class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-section-title">系统</div>
          <router-link
            v-for="item in systemMenu"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span v-if="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
          </router-link>
        </div>

        <!-- 业务管理 - 所有人可见（但商家只能看到自己的数据） -->
        <div class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-section-title">业务</div>

          <router-link
            to="/admin/product-list"
            class="nav-item"
            :class="{ active: isActive('/admin/product-list') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                <line x1="3" y1="6" x2="21" y2="6"/>
                <path d="M16 10a4 4 0 0 1-8 0"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">商品管理</span>
          </router-link>

          <router-link
            to="/admin/order-list"
            class="nav-item"
            :class="{ active: isActive('/admin/order-list') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">订单管理</span>
            <span v-if="!sidebarCollapsed && pendingOrders > 0" class="nav-badge">{{ pendingOrders }}</span>
          </router-link>

          <!-- 用户管理 - 管理员和客服可见，商家不可见 -->
          <router-link
            v-if="canViewUserManagement"
            to="/admin/user-list"
            class="nav-item"
            :class="{ active: isActive('/admin/user-list') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">用户管理</span>
          </router-link>

          <!-- 商家管理 - 只有管理员可见 -->
          <router-link
            v-if="isAdmin && !isMerchantUser"
            to="/admin/merchant-list"
            class="nav-item"
            :class="{ active: isActive('/admin/merchant-list') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">商家管理</span>
          </router-link>
        </div>

        <!-- 营销管理 -->
        <div class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-section-title">营销</div>

          <router-link
            to="/admin/coupon-list"
            class="nav-item"
            :class="{ active: isActive('/admin/coupon-list') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                <line x1="7" y1="7" x2="7.01" y2="7"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">优惠券</span>
          </router-link>

          <!-- 积分规则 - 只有管理员可见 -->
          <router-link
            v-if="isAdmin && !isMerchantUser"
            to="/admin/points-rule"
            class="nav-item"
            :class="{ active: isActive('/admin/points-rule') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">积分规则</span>
          </router-link>
        </div>

        <!-- 数据统计 - 只有管理员可见 -->
        <div v-if="isAdmin && !isMerchantUser" class="nav-section">
          <div v-if="!sidebarCollapsed" class="nav-section-title">数据</div>

          <router-link
            to="/admin/sales-stat"
            class="nav-item"
            :class="{ active: isActive('/admin/sales-stat') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="20" x2="18" y2="10"/>
                <line x1="12" y1="20" x2="12" y2="4"/>
                <line x1="6" y1="20" x2="6" y2="14"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">销售统计</span>
          </router-link>

          <router-link
            to="/admin/user-stat"
            class="nav-item"
            :class="{ active: isActive('/admin/user-stat') }"
          >
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </span>
            <span v-if="!sidebarCollapsed" class="nav-label">用户统计</span>
          </router-link>
        </div>
      </nav>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <div class="user-profile">
          <div class="user-avatar" :style="{ background: roleGradient }">{{ userInitials }}</div>
          <div v-if="!sidebarCollapsed" class="user-info">
            <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
            <div class="user-role">
              <span v-if="isSuperAdmin" class="role-tag super">超管</span>
              <span v-else-if="isMerchantUser" class="role-tag merchant">商家</span>
              <span v-else class="role-tag admin">管理员</span>
            </div>
          </div>
        </div>
        <div class="sidebar-actions">
          <button v-if="!sidebarCollapsed" class="action-btn" @click="goToProfile">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            个人中心
          </button>
          <button v-if="!sidebarCollapsed" class="action-btn danger" @click="handleLogout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            退出登录
          </button>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <div class="header-toggle" @click="toggleSidebar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="12" x2="21" y2="12"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </div>
          <div class="header-breadcrumb">
            <span class="breadcrumb-item">{{ currentPageTitle }}</span>
          </div>
        </div>

        <div class="header-right">
          <!-- 快捷入口 -->
          <div class="quick-entry">
            <span class="entry-label">{{ isMerchantUser ? '商家端' : '管理端' }}</span>
          </div>

          <!-- 消息通知 -->
          <div class="header-action" @click="showNotifications = !showNotifications">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          </div>

          <!-- 用户下拉 -->
          <div class="user-dropdown" @click="showUserMenu = !showUserMenu">
            <div class="user-avatar-sm" :style="{ background: roleGradient }">{{ userInitials }}</div>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>

          <!-- 用户下拉菜单 -->
          <div v-if="showUserMenu" class="user-dropdown-menu">
            <div class="dropdown-item" @click="goToProfile">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              个人中心
            </div>
            <div class="dropdown-item" @click="goToSettings">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
              </svg>
              账户设置
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item danger" @click="handleLogout">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              退出登录
            </div>
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 通知面板 -->
    <div v-if="showNotifications" class="notifications-panel">
      <div class="panel-header">
        <span>通知</span>
        <span class="mark-all" @click="markAllRead">全部已读</span>
      </div>
      <div class="notifications-list">
        <div
          v-for="notif in notifications"
          :key="notif.id"
          class="notification-item"
          :class="{ unread: !notif.read }"
          @click="handleNotification(notif)"
        >
          <div class="notification-icon" :class="notif.type">
            <span v-html="notif.icon"></span>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notif.title }}</div>
            <div class="notification-desc">{{ notif.description }}</div>
            <div class="notification-time">{{ notif.time }}</div>
          </div>
        </div>
        <div v-if="notifications.length === 0" class="empty-notifications">
          暂无通知
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 状态
const sidebarCollapsed = ref(false)
const showUserMenu = ref(false)
const showNotifications = ref(false)
const isFullscreen = ref(false)

// 用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('admin_user') || '{}'))

// 待处理订单数
const pendingOrders = ref(5)
const unreadCount = ref(3)

// 系统菜单
const systemMenu = [
  {
    path: '/admin/admin-list',
    label: '管理员',
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
  },
  {
    path: '/admin/role-list',
    label: '角色权限',
    icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>'
  }
]

// 通知
const notifications = ref([
  {
    id: 1,
    title: '新订单提醒',
    description: '您有 5 个新订单待处理',
    time: '10分钟前',
    type: 'order',
    read: false,
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>'
  },
  {
    id: 2,
    title: '商品库存预警',
    description: 'iPhone 15 Pro 库存不足 10 件',
    time: '30分钟前',
    type: 'warning',
    read: false,
    icon: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>'
  }
])

// 计算属性
const userInitials = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || 'A'
  return name.charAt(0).toUpperCase()
})

// 角色判断
const isSuperAdmin = computed(() => {
  return userInfo.value.isSuperAdmin || (userInfo.value.roles && userInfo.value.roles.includes('SUPER_ADMIN'))
})

const isMerchantUser = computed(() => {
  return userInfo.value.roles && userInfo.value.roles.includes('MERCHANT')
})

const isAdmin = computed(() => {
  if (isSuperAdmin.value) return true
  const roles = userInfo.value.roles || []
  return roles.some(r => ['OPERATION_ADMIN', 'PRODUCT_ADMIN', 'CUSTOMER_SERVICE'].includes(r))
})

// 商家不能访问用户管理
const canViewUserManagement = computed(() => {
  if (isSuperAdmin.value) return true
  const roles = userInfo.value.roles || []
  return roles.some(r => ['OPERATION_ADMIN', 'CUSTOMER_SERVICE'].includes(r))
})

// 角色渐变色
const roleGradient = computed(() => {
  if (isSuperAdmin.value) return 'linear-gradient(135deg, #FFD700, #FFA500)'
  if (isMerchantUser.value) return 'linear-gradient(135deg, #FF6B6B, #FF8E53)'
  return 'linear-gradient(135deg, var(--color-primary), var(--color-info))'
})

const currentPageTitle = computed(() => {
  const path = route.path
  const menus = [
    { path: '/admin/dashboard', label: '仪表盘' },
    { path: '/admin/product-list', label: '商品管理' },
    { path: '/admin/order-list', label: '订单管理' },
    { path: '/admin/user-list', label: '用户管理' },
    { path: '/admin/merchant-list', label: '商家管理' },
    { path: '/admin/coupon-list', label: '优惠券' },
    { path: '/admin/points-rule', label: '积分规则' },
    { path: '/admin/admin-list', label: '管理员' },
    { path: '/admin/role-list', label: '角色权限' },
    { path: '/admin/sales-stat', label: '销售统计' },
    { path: '/admin/user-stat', label: '用户统计' },
    { path: '/admin/profile', label: '个人中心' },
    { path: '/admin/settings', label: '账户设置' }
  ]
  const current = menus.find(item => path.startsWith(item.path))
  return current?.label || '仪表盘'
})

// 方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const handleLogout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_user')
  router.push('/admin/login')
}

const goToProfile = () => {
  showUserMenu.value = false
  router.push('/admin/profile')
}

const goToSettings = () => {
  showUserMenu.value = false
  router.push('/admin/settings')
}

const markAllRead = () => {
  notifications.value.forEach(n => n.read = true)
  unreadCount.value = 0
}

const handleNotification = (notif) => {
  notif.read = true
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  showNotifications.value = false
}

// 点击外部关闭菜单
const handleClickOutside = (e) => {
  if (!e.target.closest('.user-dropdown')) {
    showUserMenu.value = false
  }
  if (!e.target.closest('.notifications-panel') && !e.target.closest('.header-action')) {
    showNotifications.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
}

/* 侧边栏 */
.admin-sidebar {
  width: var(--sidebar-width);
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  transition: width var(--transition-normal);
}

.admin-sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

/* Logo */
.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  gap: var(--spacing-md);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-info));
  border-radius: var(--border-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  padding: var(--spacing-md);
  overflow-y: auto;
}

.nav-section {
  margin-bottom: var(--spacing-lg);
}

.nav-section-title {
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-xs);
}

.nav-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-sm);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  margin-bottom: 2px;
  cursor: pointer;
}

.nav-item:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 500;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: var(--spacing-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
  font-size: var(--font-size-base);
  white-space: nowrap;
}

.nav-badge {
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

/* 用户信息 */
.sidebar-footer {
  padding: var(--spacing-md);
  border-top: 1px solid var(--border-light);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  cursor: pointer;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.role-tag {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  text-transform: uppercase;
}

.role-tag.super {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
}

.role-tag.merchant {
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: white;
}

.role-tag.admin {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  background: transparent;
  border: 1px solid var(--border-light);
  border-radius: var(--border-radius-sm);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.action-btn.danger:hover {
  background: #FFEBE9;
  border-color: var(--color-danger);
  color: var(--color-danger);
}

/* 主内容区 */
.admin-main {
  flex: 1;
  margin-left: var(--sidebar-width);
  min-height: 100vh;
  transition: margin-left var(--transition-normal);
}

.admin-main.sidebar-collapsed {
  margin-left: var(--sidebar-collapsed-width);
}

/* 顶部栏 */
.admin-header {
  height: var(--header-height);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.header-toggle {
  width: 36px;
  height: 36px;
  border-radius: var(--border-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.header-toggle:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.header-breadcrumb {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  position: relative;
}

.quick-entry {
  padding: 4px 12px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  font-size: var(--font-size-xs);
  font-weight: 500;
  color: var(--text-secondary);
}

.header-action {
  width: 36px;
  height: 36px;
  border-radius: var(--border-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  position: relative;
}

.header-action:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--color-danger);
  color: white;
  font-size: 10px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 用户下拉 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-dropdown:hover {
  background: var(--bg-hover);
}

.user-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.user-dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 200px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  padding: var(--spacing-xs);
  z-index: 100;
  animation: scaleIn 0.15s ease;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-sm);
  font-size: var(--font-size-base);
  color: var(--text-primary);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--bg-hover);
}

.dropdown-item.danger {
  color: var(--color-danger);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-light);
  margin: var(--spacing-xs) 0;
}

/* 通知面板 */
.notifications-panel {
  position: fixed;
  top: var(--header-height);
  right: var(--spacing-lg);
  width: 360px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  z-index: 100;
  animation: slideUp 0.2s ease;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  font-weight: 600;
}

.mark-all {
  font-size: var(--font-size-sm);
  font-weight: 400;
  color: var(--text-link);
  cursor: pointer;
}

.mark-all:hover {
  text-decoration: underline;
}

.notifications-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.notification-item:hover {
  background: var(--bg-hover);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background: var(--color-primary-light);
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon.order {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.notification-icon.warning {
  background: #FFF4E5;
  color: var(--color-warning);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.notification-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: 4px;
}

.notification-time {
  font-size: var(--font-size-xs);
  color: var(--text-tertiary);
}

.empty-notifications {
  padding: var(--spacing-xl);
  text-align: center;
  color: var(--text-tertiary);
}

/* 内容区 */
.admin-content {
  padding: var(--spacing-xl);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .admin-sidebar {
    transform: translateX(-100%);
  }

  .admin-sidebar:not(.collapsed) {
    transform: translateX(0);
  }

  .admin-main {
    margin-left: 0;
  }

  .notifications-panel {
    left: var(--spacing-md);
    right: var(--spacing-md);
    width: auto;
  }
}
</style>
