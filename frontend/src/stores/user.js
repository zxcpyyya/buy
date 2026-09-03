import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  
  const isLogin = computed(() => !!token.value)
  
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }
  
  const fetchUserInfo = async () => {
    try {
      const data = await request.get('/user/info')
      setUserInfo(data)
      return data
    } catch (e) {
      console.error('获取用户信息失败', e)
      return null
    }
  }
  
  return {
    token,
    userInfo,
    isLogin,
    setToken,
    setUserInfo,
    logout,
    fetchUserInfo
  }
})
