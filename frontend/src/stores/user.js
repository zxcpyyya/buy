import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_userInfo') || '{}'))
  const permissions = ref(JSON.parse(localStorage.getItem('admin_permissions') || '[]'))

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isSuperAdmin = computed(() => userInfo.value?.isSuperAdmin === true)
  const username = computed(() => userInfo.value?.username || '')

  // Actions
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('admin_userInfo', JSON.stringify(info))
  }

  function setPermissions(perms) {
    permissions.value = perms
    localStorage.setItem('admin_permissions', JSON.stringify(perms))
  }

  function hasPermission(perm) {
    // 超管拥有所有权限
    if (isSuperAdmin.value) return true
    return permissions.value.includes(perm)
  }

  function hasAnyPermission(...perms) {
    if (isSuperAdmin.value) return true
    return perms.some(p => permissions.value.includes(p))
  }

  function hasAllPermissions(...perms) {
    if (isSuperAdmin.value) return true
    return perms.every(p => permissions.value.includes(p))
  }

  function hasRole(role) {
    if (isSuperAdmin.value) return true
    return userInfo.value?.roles?.includes(role)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_userInfo')
    localStorage.removeItem('admin_permissions')
  }

  return {
    // State
    token,
    userInfo,
    permissions,
    // Getters
    isLoggedIn,
    isSuperAdmin,
    username,
    // Actions
    setToken,
    setUserInfo,
    setPermissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    logout
  }
})
