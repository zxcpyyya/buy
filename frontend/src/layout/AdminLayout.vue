<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <img v-if="!isCollapse" src="@/assets/logo.png" alt="logo" class="logo-img" />
        <span v-if="!isCollapse" class="logo-text">商城后台</span>
        <span v-else class="logo-collapse">M</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <template v-for="menu in visibleMenus" :key="menu.id">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id"
              :index="child.path"
            >
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>

          <!-- 没有子菜单 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Fold, Expand, ArrowDown, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { authApi } from '@/api/admin'
import { userApi } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const isCollapse = ref(false)
const currentRoute = route

// 过滤有权限的菜单
const visibleMenus = computed(() => {
  const menus = menuStore.menus
  if (userStore.isSuperAdmin) {
    return menus
  }
  return filterMenusByPermission(menus)
})

// 根据权限过滤菜单
function filterMenusByPermission(menus) {
  return menus.filter(menu => {
    // 如果是超管，显示所有菜单
    if (userStore.isSuperAdmin) return true

    // 检查当前菜单权限
    if (menu.code && !userStore.hasPermission(menu.code)) {
      return false
    }

    // 过滤子菜单
    if (menu.children && menu.children.length) {
      menu.children = filterMenusByPermission(menu.children)
    }

    return true
  })
}

// 当前激活菜单
const activeMenu = computed(() => {
  return route.path
})

// 切换折叠
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
async function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/admin/profile')
      break
    case 'password':
      showPasswordDialog()
      break
    case 'logout':
      await handleLogout()
      break
  }
}

// 修改密码对话框
function showPasswordDialog() {
  ElMessageBox.prompt('请输入新密码', '修改密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password'
  }).then(async ({ value }) => {
    if (!value || value.length < 6) {
      ElMessage.warning('密码长度至少6位')
      return
    }
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    router.push('/admin/login')
  }).catch(() => {})
}

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await authApi.logout()
  } catch (e) {
    // 用户取消
  }

  userStore.logout()
  menuStore.clearMenus()
  router.push('/admin/login')
}

// 加载菜单
async function loadMenus() {
  try {
    const res = await userApi.getMenus()
    if (res.code === 200) {
      menuStore.setMenus(res.data || [])
    }
  } catch (e) {
    console.error('加载菜单失败', e)
  }
}

loadMenus()
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #263445;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.logo-collapse {
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

:deep(.el-menu) {
  background: transparent;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #bfcbd9;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: #263445 !important;
}

:deep(.el-menu-item.is-active) {
  color: #409eff !important;
  background: #263445 !important;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
