import { createRouter, createWebHistory } from 'vue-router'
import store from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', guest: true }
  },
  {
    path: '/category',
    name: 'category',
    component: () => import('@/views/Category.vue'),
    meta: { title: '分类' }
  },
  {
    path: '/category/:id',
    name: 'category-products',
    component: () => import('@/views/Products.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('@/views/Cart.vue'),
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/product/:id',
    name: 'product',
    component: () => import('@/views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/checkout',
    name: 'checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { title: '确认订单', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'orders',
    component: () => import('@/views/Orders.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'order-detail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/addresses',
    name: 'addresses',
    component: () => import('@/views/Addresses.vue'),
    meta: { title: '收货地址', requiresAuth: true }
  },
  {
    path: '/coupons',
    name: 'coupons',
    component: () => import('@/views/Coupons.vue'),
    meta: { title: '优惠券', requiresAuth: true }
  },
  {
    path: '/points',
    name: 'points',
    component: () => import('@/views/Points.vue'),
    meta: { title: '积分中心', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/express/:id',
    name: 'express',
    component: () => import('@/views/ExpressTrack.vue'),
    meta: { title: '物流追踪', requiresAuth: true }
  },
  {
    path: '/github-callback',
    name: 'github-callback',
    component: () => import('@/views/GithubCallback.vue'),
    meta: { title: 'GitHub 登录中', guest: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - Store` : 'Store'
  
  // 检查登录状态
  const isLoggedIn = !!localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !isLoggedIn) {
    // 需要登录
    next({ name: 'login', query: { redirect: to.fullPath } })
  } else if (to.meta.guest && isLoggedIn) {
    // 已登录用户访问游客页面
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
