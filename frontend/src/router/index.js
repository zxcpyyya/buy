import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/AdminLayout.vue'

// 静态路由
export const constantRoutes = [
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/admin/403',
    name: '403',
    component: () => import('@/views/admin/Error403.vue'),
    meta: { title: '无权限', hidden: true }
  },
  {
    path: '/admin/404',
    name: '404',
    component: () => import('@/views/admin/Error404.vue'),
    meta: { title: '页面不存在', hidden: true }
  }
]

// 动态路由（根据权限动态生成）
export const dynamicRoutes = [
  {
    path: '/admin',
    component: Layout,
    redirect: '/admin/dashboard',
    name: 'AdminHome',
    meta: { title: '首页' },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '工作台' }
      }
    ]
  },
  {
    path: '/admin/product',
    component: Layout,
    meta: { title: '商品管理', icon: 'Goods' },
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('@/views/admin/product/ProductList.vue'),
        meta: { title: '商品列表', permission: 'product:list' }
      },
      {
        path: 'add',
        name: 'ProductAdd',
        component: () => import('@/views/admin/product/ProductForm.vue'),
        meta: { title: '添加商品', permission: 'product:add' }
      },
      {
        path: 'edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/admin/product/ProductForm.vue'),
        meta: { title: '编辑商品', permission: 'product:edit', hidden: true }
      },
      {
        path: 'category',
        name: 'CategoryList',
        component: () => import('@/views/admin/product/CategoryList.vue'),
        meta: { title: '商品分类', permission: 'product:category' }
      }
    ]
  },
  {
    path: '/admin/order',
    component: Layout,
    meta: { title: '订单管理', icon: 'Document' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/views/admin/order/OrderList.vue'),
        meta: { title: '订单列表', permission: 'order:list' }
      },
      {
        path: 'detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/admin/order/OrderDetail.vue'),
        meta: { title: '订单详情', permission: 'order:detail', hidden: true }
      }
    ]
  },
  {
    path: '/admin/user',
    component: Layout,
    meta: { title: '用户管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'CustomerList',
        component: () => import('@/views/admin/user/CustomerList.vue'),
        meta: { title: '用户列表', permission: 'user:list' }
      }
    ]
  },
  {
    path: '/admin/merchant',
    component: Layout,
    meta: { title: '商家管理', icon: 'OfficeBuilding' },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('@/views/admin/merchant/MerchantList.vue'),
        meta: { title: '商家列表', permission: 'merchant:list' }
      },
      {
        path: 'audit',
        name: 'MerchantAudit',
        component: () => import('@/views/admin/merchant/MerchantAudit.vue'),
        meta: { title: '商家审核', permission: 'merchant:audit' }
      }
    ]
  },
  {
    path: '/admin/marketing',
    component: Layout,
    meta: { title: '营销管理', icon: 'Discount' },
    children: [
      {
        path: 'coupon',
        name: 'CouponList',
        component: () => import('@/views/admin/marketing/CouponList.vue'),
        meta: { title: '优惠券管理', permission: 'marketing:coupon' }
      },
      {
        path: 'coupon/add',
        name: 'CouponAdd',
        component: () => import('@/views/admin/marketing/CouponForm.vue'),
        meta: { title: '创建优惠券', permission: 'marketing:coupon:add' }
      }
    ]
  },
  {
    path: '/admin/system',
    component: Layout,
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'admin',
        name: 'AdminList',
        component: () => import('@/views/admin/system/AdminList.vue'),
        meta: { title: '管理员管理', permission: 'system:admin' }
      },
      {
        path: 'role',
        name: 'RoleList',
        component: () => import('@/views/admin/system/RoleList.vue'),
        meta: { title: '角色管理', permission: 'system:role' }
      },
      {
        path: 'permission',
        name: 'PermissionList',
        component: () => import('@/views/admin/system/PermissionList.vue'),
        meta: { title: '权限管理', permission: 'system:permission' }
      },
      {
        path: 'log',
        name: 'OperationLog',
        component: () => import('@/views/admin/system/OperationLog.vue'),
        meta: { title: '操作日志', permission: 'system:log' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...constantRoutes]
})

export default router
