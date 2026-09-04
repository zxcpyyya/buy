/**
 * 后台管理系统路由配置
 * 包含权限控制
 */
import AdminLayout from '@/views/admin/AdminLayout.vue'
import AdminLogin from '@/views/admin/AdminLogin.vue'
import Dashboard from '@/views/admin/Dashboard.vue'
import ProductList from '@/views/admin/ProductList.vue'
import OrderList from '@/views/admin/OrderList.vue'
import RoleList from '@/views/admin/RoleList.vue'

// 路由守卫：检查是否登录
const requireAuth = (to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    next()
  } else {
    next('/admin/login')
  }
}

// 登录页守卫：已登录则跳转首页
const noAuth = (to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    next('/admin/dashboard')
  } else {
    next()
  }
}

// 权限检查
const checkPermission = (to, from, next) => {
  const userStr = localStorage.getItem('admin_user')
  if (!userStr) {
    next('/admin/login')
    return
  }

  const user = JSON.parse(userStr)
  
  // 超级管理员拥有所有权限
  if (user.isSuperAdmin || (user.roles && user.roles.includes('SUPER_ADMIN'))) {
    next()
    return
  }

  // 检查路由所需的权限
  const requiredPermission = to.meta.permission
  if (requiredPermission) {
    const userPermissions = user.permissions || []
    
    // 特殊处理：商家角色
    if (user.roles && user.roles.includes('MERCHANT')) {
      // 商家不能访问的路由
      const merchantForbidden = ['user-list', 'admin-list', 'role-list', 'merchant-list', 'sales-stat', 'user-stat']
      if (merchantForbidden.some(r => to.path.includes(r))) {
        next('/admin/dashboard')
        return
      }
      
      // 商家只能看到自己的商品和订单（后端控制）
    }
    
    // 检查权限
    if (!userPermissions.includes(requiredPermission)) {
      // 没有权限，跳转到首页或显示无权限
      next('/admin/dashboard')
      return
    }
  }

  next()
}

// 商家角色检查
const checkMerchantAccess = (to, from, next) => {
  const userStr = localStorage.getItem('admin_user')
  
  if (!userStr) {
    next('/admin/login')
    return
  }

  const user = JSON.parse(userStr)
  
  // 商家禁止访问的页面
  const merchantForbiddenRoutes = [
    '/admin/user-list',       // 用户管理
    '/admin/admin-list',      // 管理员管理
    '/admin/role-list',       // 角色权限
    '/admin/merchant-list',   // 商家管理
    '/admin/sales-stat',      // 销售统计
    '/admin/user-stat',       // 用户统计
  ]

  // 检查是否是商家
  if (user.roles && user.roles.includes('MERCHANT')) {
    const isForbidden = merchantForbiddenRoutes.some(route => to.path.startsWith(route))
    if (isForbidden) {
      // 商家不能访问，跳转到首页
      next('/admin/dashboard')
      return
    }
  }

  next()
}

const adminRoutes = [
  // 登录页
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    beforeEnter: noAuth,
    meta: { title: '管理员登录', public: true }
  },

  // 后台主布局
  {
    path: '/admin',
    component: AdminLayout,
    beforeEnter: requireAuth,
    redirect: '/admin/dashboard',
    children: [
      // 仪表盘 - 所有人可访问
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表盘', icon: 'dashboard', permission: 'dashboard' }
      },

      // ============ 商品管理 - 所有角色可访问（数据根据角色过滤） ============
      {
        path: 'product-list',
        name: 'ProductList',
        component: ProductList,
        meta: { title: '商品列表', icon: 'product', permission: 'product:list' }
      },
      {
        path: 'product-add',
        name: 'ProductAdd',
        component: () => import('@/views/admin/ProductAdd.vue'),
        meta: { title: '添加商品', icon: 'product', permission: 'product:add' }
      },
      {
        path: 'product-edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/admin/ProductAdd.vue'),
        meta: { title: '编辑商品', icon: 'product', permission: 'product:edit' }
      },

      // ============ 订单管理 - 所有角色可访问 ============
      {
        path: 'order-list',
        name: 'OrderList',
        component: OrderList,
        meta: { title: '订单列表', icon: 'order', permission: 'order:list' }
      },
      {
        path: 'order-detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/admin/OrderDetail.vue'),
        meta: { title: '订单详情', icon: 'order', permission: 'order:detail' }
      },

      // ============ 用户管理 - 管理员和客服可访问，商家不能访问 ============
      {
        path: 'user-list',
        name: 'UserList',
        component: () => import('@/views/admin/UserList.vue'),
        meta: { 
          title: '用户列表', 
          icon: 'user', 
          permission: 'user:list',
          roles: ['SUPER_ADMIN', 'OPERATION_ADMIN', 'CUSTOMER_SERVICE'],  // 排除商家
          hideForMerchant: true  // 商家隐藏
        },
        beforeEnter: checkMerchantAccess
      },

      // ============ 商家管理 - 只有管理员可访问 ============
      {
        path: 'merchant-list',
        name: 'MerchantList',
        component: () => import('@/views/admin/MerchantList.vue'),
        meta: { 
          title: '商家列表', 
          icon: 'merchant', 
          permission: 'merchant:list',
          roles: ['SUPER_ADMIN', 'OPERATION_ADMIN'],  // 只有管理员
          hideForMerchant: true
        },
        beforeEnter: checkMerchantAccess
      },

      // ============ 营销管理 ============
      {
        path: 'coupon-list',
        name: 'CouponList',
        component: () => import('@/views/admin/CouponList.vue'),
        meta: { title: '优惠券', icon: 'coupon', permission: 'marketing:coupon' }
      },
      {
        path: 'points-rule',
        name: 'PointsRule',
        component: () => import('@/views/admin/PointsRule.vue'),
        meta: { 
          title: '积分规则', 
          icon: 'points', 
          permission: 'marketing:points',
          roles: ['SUPER_ADMIN', 'OPERATION_ADMIN'],  // 只有管理员可编辑
          hideForMerchant: true
        },
        beforeEnter: checkMerchantAccess
      },

      // ============ 权限管理 - 只有超管可访问 ============
      {
        path: 'admin-list',
        name: 'AdminList',
        component: () => import('@/views/admin/AdminList.vue'),
        meta: { 
          title: '管理员', 
          icon: 'admin', 
          permission: 'system:admin',
          roles: ['SUPER_ADMIN'],
          hideForMerchant: true
        },
        beforeEnter: checkMerchantAccess
      },
      {
        path: 'role-list',
        name: 'RoleList',
        component: RoleList,
        meta: { 
          title: '角色权限', 
          icon: 'role', 
          permission: 'system:role',
          roles: ['SUPER_ADMIN'],
          hideForMerchant: true
        },
        beforeEnter: checkMerchantAccess
      },

      // ============ 数据统计 - 只有管理员可访问 ============
      {
        path: 'sales-stat',
        name: 'SalesStat',
        component: () => import('@/views/admin/SalesStat.vue'),
        meta: { 
          title: '销售统计', 
          icon: 'chart', 
          permission: 'statistics:sales',
          roles: ['SUPER_ADMIN', 'OPERATION_ADMIN'],
          hideForMerchant: true
        },
        beforeEnter: checkMerchantAccess
      },
      {
        path: 'user-stat',
        name: 'UserStat',
        component: () => import('@/views/admin/UserStat.vue'),
        meta: { 
          title: '用户统计', 
          icon: 'chart', 
          permission: 'statistics:user',
          roles: ['SUPER_ADMIN', 'OPERATION_ADMIN'],
          hideForMerchant: true
        },
        beforeEnter: checkMerchantAccess
      },

      // ============ 个人设置 - 所有人可访问 ============
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '账户设置' }
      }
    ]
  }
]

export default adminRoutes
