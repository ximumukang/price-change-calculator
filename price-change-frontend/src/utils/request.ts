import axios, { AxiosRequestConfig, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/store/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动添加 Token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 auth store 获取 token
    let token = ''
    try {
      const authStore = useAuthStore()
      token = authStore.token || localStorage.getItem('token') || ''
    } catch (e) {
      // 如果无法获取 store，回退到 localStorage
      token = localStorage.getItem('token') || ''
    }
    
    // 只有在 token 非空时才添加 Authorization header
    if (token && token.trim() !== '') {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理后端返回格式
interface BackendResponse<T = unknown> {
  code: number
  message: string
  data: T
}

request.interceptors.response.use(
  <T>(response: { data: BackendResponse<T> }): T | Promise<T> => {
    const res = response.data as BackendResponse<T>
    
    // 后端返回的统一格式：{code, message, data}
    if (res.code === 200) {
      // 成功：返回 data 字段（已解包）
      return res.data as T
    } else {
      // 业务错误：抛出带详细信息的错误
      return Promise.reject<T>(new Error(res.message || '请求失败'))
    }
  },
  (error: AxiosError) => {
    // HTTP 错误处理
    if (error.response) {
      const status = error.response.status
      
      // 401/403：未授权或禁止，清除登录状态并跳转登录页
      if (status === 401 || status === 403) {
        try {
          const authStore = useAuthStore()
          authStore.logout()
        } catch (e) {
          // ignore
        }
        // 避免重复跳转
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
      }
      
      // 尝试从后端响应中提取错误信息
      const responseData = error.response.data as BackendResponse | undefined
      const errorMsg = responseData?.message || error.message || '请求失败'
      return Promise.reject(new Error(errorMsg))
    }
    
    // 网络错误或超时
    return Promise.reject(new Error(error.message || '网络错误'))
  }
)

export default request

// 封装的请求方法（响应拦截器已自动解包 data）
export function get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request.get(url, config) as unknown as Promise<T>
}

export function post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return request.post(url, data, config) as unknown as Promise<T>
}

export function put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return request.put(url, data, config) as unknown as Promise<T>
}

export function delete_<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request.delete(url, config) as unknown as Promise<T>
}
