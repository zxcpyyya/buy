import request from './request'

// 登录相关
export const authApi = {
  login: (data) => request.post('/api/admin/auth/login', data),
  logout: () => request.post('/api/admin/auth/logout'),
  verify: () => request.get('/api/admin/auth/verify'),
  refresh: () => request.post('/api/admin/auth/refresh')
}

// 用户相关
export const userApi = {
  getUserInfo: () => request.get('/api/admin/user/info'),
  getMenus: () => request.get('/api/admin/user/menus'),
  getPermissions: () => request.get('/api/admin/user/permissions'),
  changePassword: (data) => request.put('/api/admin/user/password', data)
}

// 管理员相关
export const adminApi = {
  list: (params) => request.get('/api/admin/list', { params }),
  getById: (id) => request.get(`/api/admin/${id}`),
  add: (data) => request.post('/api/admin', data),
  update: (data) => request.put('/api/admin', data),
  delete: (id) => request.delete(`/api/admin/${id}`),
  resetPassword: (id) => request.post(`/api/admin/${id}/reset-password`),
  changeStatus: (id, status) => request.post(`/api/admin/${id}/status`, { status })
}

// 角色相关
export const roleApi = {
  list: (params) => request.get('/api/admin/role/list', { params }),
  getById: (id) => request.get(`/api/admin/role/${id}`),
  add: (data) => request.post('/api/admin/role', data),
  update: (data) => request.put('/api/admin/role', data),
  delete: (id) => request.delete(`/api/admin/role/${id}`),
  getPermissions: (id) => request.get(`/api/admin/role/${id}/permissions`),
  assignPermissions: (id, permissionIds) => request.post(`/api/admin/role/${id}/permissions`, { permissionIds })
}

// 权限相关
export const permissionApi = {
  list: () => request.get('/api/admin/permission/list'),
  getById: (id) => request.get(`/api/admin/permission/${id}`),
  add: (data) => request.post('/api/admin/permission', data),
  update: (data) => request.put('/api/admin/permission', data),
  delete: (id) => request.delete(`/api/admin/permission/${id}`),
  getTree: () => request.get('/api/admin/permission/tree')
}

// 操作日志
export const logApi = {
  list: (params) => request.get('/api/admin/log/list', { params }),
  getById: (id) => request.get(`/api/admin/log/${id}`)
}

// 商品管理
export const productApi = {
  list: (params) => request.get('/api/product/list', { params }),
  detail: (id) => request.get(`/api/product/${id}`),
  add: (data) => request.post('/api/admin/product', data),
  update: (data) => request.put('/api/admin/product', data),
  delete: (id) => request.delete(`/api/admin/product/${id}`),
  publish: (id) => request.post(`/api/admin/product/${id}/publish`),
  unpublish: (id) => request.post(`/api/admin/product/${id}/unpublish`)
}

// 分类管理
export const categoryApi = {
  list: () => request.get('/api/category/list'),
  tree: () => request.get('/api/admin/category/tree'),
  add: (data) => request.post('/api/admin/category', data),
  update: (data) => request.put('/api/admin/category', data),
  delete: (id) => request.delete(`/api/admin/category/${id}`)
}

// 订单管理
export const orderApi = {
  list: (params) => request.get('/api/order/list', { params }),
  detail: (id) => request.get(`/api/order/${id}`),
  ship: (id, data) => request.post(`/api/admin/order/${id}/ship`, data),
  cancel: (id, data) => request.post(`/api/admin/order/${id}/cancel`, data),
  export: (params) => request.download('/api/admin/order/export', params)
}

// 用户管理
export const customerApi = {
  list: (params) => request.get('/api/admin/customer/list', { params }),
  detail: (id) => request.get(`/api/admin/customer/${id}`),
  disable: (id) => request.post(`/api/admin/customer/${id}/disable`),
  enable: (id) => request.post(`/api/admin/customer/${id}/enable`)
}

// 商家管理
export const merchantApi = {
  list: (params) => request.get('/api/admin/merchant/list', { params }),
  detail: (id) => request.get(`/api/admin/merchant/${id}`),
  audit: (id, status) => request.post(`/api/admin/merchant/${id}/audit`, { status }),
  update: (data) => request.put('/api/admin/merchant', data)
}

// 优惠券管理
export const couponApi = {
  list: (params) => request.get('/api/admin/coupon/list', { params }),
  add: (data) => request.post('/api/admin/coupon', data),
  update: (data) => request.put('/api/admin/coupon', data),
  delete: (id) => request.delete(`/api/admin/coupon/${id}`),
  publish: (id) => request.post(`/api/admin/coupon/${id}/publish`),
  unpublish: (id) => request.post(`/api/admin/coupon/${id}/unpublish`)
}

// 统计分析
export const statisticsApi = {
  dashboard: () => request.get('/api/admin/statistics/dashboard'),
  salesTrend: (params) => request.get('/api/admin/statistics/sales-trend', { params }),
  userStats: () => request.get('/api/admin/statistics/user'),
  productStats: () => request.get('/api/admin/statistics/product')
}
