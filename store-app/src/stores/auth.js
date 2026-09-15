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
    
    /**
     * GitHub OAuth 登录
     * @param {string} code - GitHub 授权码
     * @param {string} state - 状态码
     */
    async githubLogin(code, state) {
      try {
        const { data } = await request.get('/auth/github/callback', {
          params: { code, state }
        })
        
        // 保存 token 和用户信息
        this.token = data.token
        this.userInfo = {
          id: data.userId,
          username: data.username,
          nickname: data.nickname,
          avatar: data.avatar,
          email: data.email,
          githubUsername: data.githubUsername,
          isNewUser: data.isNewUser
        }
        
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        
        return {
          isNewUser: data.isNewUser,
          ...data
        }
      } catch (error) {
        throw error
      }
    },
    
    /**
     * 获取 GitHub 授权 URL
     */
    async getGithubAuthUrl() {
      try {
        const { data } = await request.get('/auth/github/authorize')
        return data
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
