import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const auth = JSON.parse(sessionStorage.getItem('auth') || '{}')
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        sessionStorage.removeItem('auth')
        Message.error(res.message || '登录状态已失效，请重新登录')
        if (router.currentRoute.path !== '/login') {
          router.push('/login')
        }
      } else {
        Message.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    const status = error.response && error.response.status
    if (status === 401) {
      sessionStorage.removeItem('auth')
      if (router.currentRoute.path !== '/login') {
        router.push('/login')
      }
    }
    Message.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
