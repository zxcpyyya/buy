/**
 * 后台管理 API 服务
 */
import request from '@/utils/request'

// 认证相关
export const adminLogin = (data) => {
  return request({
    url: '/api/admin/auth/login',
    method: 'post',
    data
  })
}

export const adminLogout = () => {
  return request({
    url: '/api/admin/auth/logout',
    method: 'post'
  })
}

export const verifyToken = () => {
  return request({
    url: '/api/admin/auth/verify',
    method: 'get'
  })
}

export const refreshToken = () => {
  return request({
    url: '/api/admin/auth/refresh',
    method: 'post'
  })
}

// 用户信息
export const getUserInfo = () => {
  return request({
    url: '/api/admin/user/info',
    method: 'get'
  })
}

export const getUserMenus = () => {
  return request({
    url: '/api/admin/user/menus',
    method: 'get'
  })
}

export const getUserPermissions = () => {
  return request({
    url: '/api/admin/user/permissions',
    method: 'get'
  })
}

export const changePassword = (oldPassword, newPassword) => {
  return request({
    url: '/api/admin/user/password',
    method: 'put',
    params: { oldPassword, newPassword }
  })
}

// 管理员管理
export const getAdminList = (params) => {
  return request({
    url: '/api/admin/list',
    method: 'get',
    params
  })
}

export const getAdminDetail = (id) => {
  return request({
    url: `/api/admin/${id}`,
    method: 'get'
  })
}

export const addAdmin = (data) => {
  return request({
    url: '/api/admin',
    method: 'post',
    data
  })
}

export const updateAdmin = (id, data) => {
  return request({
    url: `/api/admin/${id}`,
    method: 'put',
    data
  })
}

export const deleteAdmin = (id) => {
  return request({
    url: `/api/admin/${id}`,
    method: 'delete'
  })
}

export const updateAdminStatus = (id, status) => {
  return request({
    url: `/api/admin/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export const assignAdminRoles = (adminId, roleIds) => {
  return request({
    url: `/api/admin/${adminId}/roles`,
    method: 'put',
    data: { roleIds }
  })
}

// 角色管理
export const getRoleList = (params) => {
  return request({
    url: '/api/admin/role/list',
    method: 'get',
    params
  })
}

export const getRoleDetail = (id) => {
  return request({
    url: `/api/admin/role/${id}`,
    method: 'get'
  })
}

export const addRole = (data) => {
  return request({
    url: '/api/admin/role',
    method: 'post',
    data
  })
}

export const updateRole = (id, data) => {
  return request({
    url: `/api/admin/role/${id}`,
    method: 'put',
    data
  })
}

export const deleteRole = (id) => {
  return request({
    url: `/api/admin/role/${id}`,
    method: 'delete'
  })
}

export const getRolePermissions = (id) => {
  return request({
    url: `/api/admin/role/${id}/permissions`,
    method: 'get'
  })
}

export const assignRolePermissions = (roleId, permissionIds) => {
  return request({
    url: `/api/admin/role/${roleId}/permissions`,
    method: 'put',
    data: { permissionIds }
  })
}

// 权限管理
export const getPermissionList = () => {
  return request({
    url: '/api/admin/permission/list',
    method: 'get'
  })
}

export const getPermissionTree = () => {
  return request({
    url: '/api/admin/permission/tree',
    method: 'get'
  })
}

// 商家管理
export const getMerchantList = (params) => {
  return request({
    url: '/api/admin/merchant/list',
    method: 'get',
    params
  })
}

export const getMerchantDetail = (id) => {
  return request({
    url: `/api/admin/merchant/${id}`,
    method: 'get'
  })
}

export const auditMerchant = (id, status, reason) => {
  return request({
    url: `/api/admin/merchant/${id}/audit`,
    method: 'put',
    data: { status, reason }
  })
}

// 仪表盘统计
export const getDashboardStats = () => {
  return request({
    url: '/api/admin/dashboard/stats',
    method: 'get'
  })
}

export const getSalesTrend = (params) => {
  return request({
    url: '/api/admin/dashboard/sales-trend',
    method: 'get',
    params
  })
}

export const getRecentOrders = (params) => {
  return request({
    url: '/api/admin/dashboard/recent-orders',
    method: 'get',
    params
  })
}
