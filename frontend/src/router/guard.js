import router from './router'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { userApi } from '@/api/admin'

// 白名单路由（不需要登录）
const whiteList = ['/admin/login']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const menuStore = useMenuStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 商城后台` : '商城后台管理系统'

  // 白名单，直接通过
  if (whiteList.includes(to.path)) {
    // 如果已登录，跳转到首页
    if (userStore.isLoggedIn && to.path === '/admin/login') {
      return next('/admin')
    }
    return next()
  }

  // 需要登录的页面
  if (!userStore.isLoggedIn) {
    return next(`/admin/login?redirect=${to.fullPath}`)
  }

  // 已登录，验证Token有效性
  try {
    // 如果没有菜单，加载菜单
    if (menuStore.menus.length === 0) {
      const res = await userApi.getMenus()
      if (res.code === 200) {
        menuStore.setMenus(res.data || [])
      }
    }

    // 检查权限
    if (to.meta.permission) {
      const hasPermission = userStore.hasPermission(to.meta.permission)
      if (!hasPermission) {
        return next({ path: '/admin/403' })
      }
    }

    next()
  } catch (error) {
    // Token无效，重新登录
    userStore.logout()
    menuStore.clearMenus()
    next(`/admin/login?redirect=${to.fullPath}`)
  }
})

export default router
