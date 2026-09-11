import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.id
  },
  
  actions: {
    async login(username, password) {
      try {
        const { data } = await request.post('/user/login', { username, password })
        this.token = data.token
        this.userInfo = data.user || {}
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return true
      } catch (error) {
        throw error
      }
    },
    
    async register(registerData) {
      try {
        const userId = await request.post('/user/register', registerData)
        return userId
      } catch (error) {
        throw error
      }
    },
    
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    async fetchUserInfo() {
      try {
        const data = await request.get('/user/info')
        this.userInfo = data
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    }
  }
})

export default useAuthStore
