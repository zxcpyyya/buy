import axios from 'axios'
import { showToast } from 'vant'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 统一处理业务错误
    if (res.code && res.code !== 200 && res.code !== 0) {
      showToast(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 未登录或token过期
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          showToast('请先登录')
          router.push('/login')
          break
        case 403:
          showToast('无权限访问')
          break
        case 404:
          showToast('请求资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(data?.message || '网络错误')
      }
    } else {
      showToast('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

// 封装常用请求方法
const http = {
  get(url, params, config = {}) {
    return request.get(url, { params, ...config })
  },
  
  post(url, data, config = {}) {
    return request.post(url, data, config)
  },
  
  put(url, data, config = {}) {
    return request.put(url, data, config)
  },
  
  delete(url, params, config = {}) {
    return request.delete(url, { params, ...config })
  }
}

export default http
