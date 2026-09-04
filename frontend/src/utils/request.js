import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()

    // 添加Token
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
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

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return res
    }

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // Token过期
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/admin/login')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/admin/login')
          break
        case 403:
          ElMessage.error(data.message || '没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时')
    } else {
      ElMessage.error('网络错误')
    }

    return Promise.reject(error)
  }
)

// 文件下载
request.download = (url, params, filename = 'export.xlsx') => {
  return request({
    url,
    method: 'GET',
    params,
    responseType: 'blob',
    timeout: 60000
  }).then((blob) => {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.click()
    window.URL.revokeObjectURL(url)
  })
}

export default request
