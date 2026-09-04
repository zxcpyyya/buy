/**
 * 权限管理工具
 * 基于用户角色和权限控制菜单、按钮的显示和访问
 */
import { ref } from 'vue'

// 用户信息
const currentUser = ref(JSON.parse(localStorage.getItem('admin_user') || '{}'))

// 权限定义
const permissions = {
  // 超级管理员 - 全部权限
  SUPER_ADMIN: ['*'],

  // 运营管理员 - 运营相关权限
  OPERATION_ADMIN: [
    'dashboard',
    'product:list', 'product:add', 'product:edit', 'product:delete', 'product:publish',
    'order:list', 'order:detail', 'order:ship', 'order:cancel',
    'user:list', 'user:detail',
    'marketing:coupon', 'marketing:coupon:add',
    'marketing:points',
    'statistics:sales', 'statistics:user'
  ],

  // 商品管理员 - 商品相关权限
  PRODUCT_ADMIN: [
    'dashboard',
    'product:list', 'product:add', 'product:edit', 'product:delete', 'product:publish',
    'order:list', 'order:detail', 'order:ship'
  ],

  // 商家 - 只有商家自己的数据
  MERCHANT: [
    'dashboard',
    'product:list', 'product:add', 'product:edit', 'product:publish',  // 没有delete
    'order:list', 'order:detail', 'order:ship',
    'marketing:coupon', 'marketing:coupon:add'  // 只能看到自己的优惠券
  ],

  // 客服 - 查看权限
  CUSTOMER_SERVICE: [
    'dashboard',
    'order:list', 'order:detail', 'order:cancel',
    'user:list', 'user:detail'
  ]
}

// 菜单权限配置
const menuPermissions = {
  // 所有人都能看到的菜单
  public: ['dashboard'],

  // 系统管理 - 只有管理员
  system: ['SUPER_ADMIN', 'OPERATION_ADMIN'],

  // 用户管理 - 管理员和客服（商家不能看）
  user: ['SUPER_ADMIN', 'OPERATION_ADMIN', 'CUSTOMER_SERVICE'],

  // 商家管理 - 只有管理员
  merchant: ['SUPER_ADMIN', 'OPERATION_ADMIN'],

  // 业务管理
  business: ['SUPER_ADMIN', 'OPERATION_ADMIN', 'PRODUCT_ADMIN', 'MERCHANT'],

  // 营销管理
  marketing: ['SUPER_ADMIN', 'OPERATION_ADMIN', 'MERCHANT'],

  // 数据统计 - 管理员
  statistics: ['SUPER_ADMIN', 'OPERATION_ADMIN']
}

/**
 * 检查用户是否有特定权限
 * @param {string} permission 权限标识
 * @returns {boolean}
 */
export const hasPermission = (permission) => {
  const user = currentUser.value

  // 超管拥有所有权限
  if (user.isSuperAdmin) return true

  // 获取用户角色
  const roles = user.roles || []

  // 遍历用户的所有角色
  for (const role of roles) {
    const rolePerms = permissions[role] || []
    // 超管角色
    if (rolePerms.includes('*')) return true
    // 检查具体权限
    if (rolePerms.includes(permission)) return true
  }

  return false
}

/**
 * 检查用户是否有任意一个权限
 * @param {string[]} perms 权限标识数组
 * @returns {boolean}
 */
export const hasAnyPermission = (perms) => {
  return perms.some(p => hasPermission(p))
}

/**
 * 检查用户是否有所有权限
 * @param {string[]} perms 权限标识数组
 * @returns {boolean}
 */
export const hasAllPermissions = (perms) => {
  return perms.every(p => hasPermission(p))
}

/**
 * 检查用户是否是特定角色
 * @param {string} role 角色标识
 * @returns {boolean}
 */
export const hasRole = (role) => {
  const roles = currentUser.value.roles || []
  return roles.includes(role)
}

/**
 * 检查用户是否是管理员（不是商家）
 * @returns {boolean}
 */
export const isAdmin = () => {
  const roles = currentUser.value.roles || []
  return roles.some(r => ['SUPER_ADMIN', 'OPERATION_ADMIN', 'PRODUCT_ADMIN', 'CUSTOMER_SERVICE'].includes(r))
}

/**
 * 检查用户是否是商家
 * @returns {boolean}
 */
export const isMerchant = () => {
  return hasRole('MERCHANT')
}

/**
 * 检查用户是否是超管
 * @returns {boolean}
 */
export const isSuperAdmin = () => {
  return currentUser.value.isSuperAdmin || hasRole('SUPER_ADMIN')
}

/**
 * 获取用户可以访问的菜单
 * @returns {Object[]} 菜单列表
 */
export const getAccessibleMenus = () => {
  const allMenus = getAllMenus()
  return filterMenusByPermission(allMenus)
}

/**
 * 过滤菜单根据权限
 */
const filterMenusByPermission = (menus) => {
  return menus.filter(menu => {
    // 检查菜单权限
    if (menu.permission) {
      if (!hasPermission(menu.permission)) return false
    }

    // 检查模块权限
    if (menu.module && menuPermissions[menu.module]) {
      const allowedRoles = menuPermissions[menu.module]
      if (!allowedRoles.some(r => hasRole(r))) return false
    }

    // 递归过滤子菜单
    if (menu.children) {
      menu.children = filterMenusByPermission(menu.children)
      // 如果没有子菜单了，也移除父菜单
      return menu.children.length > 0 || !menu.requireChildren
    }

    return true
  })
}

/**
 * 获取所有菜单配置
 */
const getAllMenus = () => [
  {
    id: 'dashboard',
    name: '仪表盘',
    path: '/admin/dashboard',
    icon: 'dashboard',
    permission: 'dashboard',
    module: 'public'
  },
  {
    id: 'system',
    name: '系统',
    module: 'system',
    children: [
      {
        id: 'admin-list',
        name: '管理员',
        path: '/admin/admin-list',
        icon: 'admin',
        permission: 'system:admin'
      },
      {
        id: 'role-list',
        name: '角色权限',
        path: '/admin/role-list',
        icon: 'role',
        permission: 'system:role'
      }
    ]
  },
  {
    id: 'business',
    name: '业务',
    module: 'business',
    children: [
      {
        id: 'product',
        name: '商品管理',
        path: '/admin/product-list',
        icon: 'product',
        permission: 'product:list'
      },
      {
        id: 'order',
        name: '订单管理',
        path: '/admin/order-list',
        icon: 'order',
        permission: 'order:list'
      },
      {
        id: 'user',
        name: '用户管理',
        path: '/admin/user-list',
        icon: 'user',
        permission: 'user:list',
        module: 'user'  // 商家不能访问
      }
    ]
  },
  {
    id: 'merchant',
    name: '商家管理',
    module: 'merchant',
    children: [
      {
        id: 'merchant-list',
        name: '商家列表',
        path: '/admin/merchant-list',
        icon: 'merchant',
        permission: 'merchant:list'
      }
    ]
  },
  {
    id: 'marketing',
    name: '营销',
    module: 'marketing',
    children: [
      {
        id: 'coupon',
        name: '优惠券',
        path: '/admin/coupon-list',
        icon: 'coupon',
        permission: 'marketing:coupon'
      },
      {
        id: 'points',
        name: '积分规则',
        path: '/admin/points-rule',
        icon: 'points',
        permission: 'marketing:points'
      }
    ]
  },
  {
    id: 'statistics',
    name: '数据',
    module: 'statistics',
    children: [
      {
        id: 'sales',
        name: '销售统计',
        path: '/admin/sales-stat',
        icon: 'chart',
        permission: 'statistics:sales'
      },
      {
        id: 'user-stat',
        name: '用户统计',
        path: '/admin/user-stat',
        icon: 'chart',
        permission: 'statistics:user'
      }
    ]
  }
]

/**
 * 更新当前用户信息
 */
export const updateCurrentUser = (user) => {
  currentUser.value = user
  if (user) {
    localStorage.setItem('admin_user', JSON.stringify(user))
  } else {
    localStorage.removeItem('admin_user')
  }
}

/**
 * 获取商家ID（如果是商家用户）
 */
export const getMerchantId = () => {
  return currentUser.value.merchantId
}

export default {
  hasPermission,
  hasAnyPermission,
  hasAllPermissions,
  hasRole,
  isAdmin,
  isMerchant,
  isSuperAdmin,
  getAccessibleMenus,
  updateCurrentUser,
  getMerchantId,
  permissions,
  menuPermissions
}
