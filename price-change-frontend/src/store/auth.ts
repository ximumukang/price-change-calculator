import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPublicKey as fetchPublicKey } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 初始化时只获取非空的 token
  const storedToken = localStorage.getItem('token')
  const storedRefreshToken = localStorage.getItem('refreshToken')
  const storedUsername = localStorage.getItem('username')

  const token = ref(storedToken && storedToken.trim() !== '' ? storedToken : '')
  const refreshToken = ref(storedRefreshToken && storedRefreshToken.trim() !== '' ? storedRefreshToken : '')
  const username = ref(storedUsername || '')

  // 计算属性：是否已认证
  const isAuthenticated = computed(() => {
    return !!(token.value && token.value.trim() !== '')
  })

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setRefreshToken = (newToken: string) => {
    refreshToken.value = newToken
    localStorage.setItem('refreshToken', newToken)
  }

  const setUsername = (name: string) => {
    username.value = name
    localStorage.setItem('username', name)
  }

  /**
   * 初始化认证信息（页面刷新时调用）
   * 恢复 token 并获取最新的 RSA 公钥
   */
  const initAuth = async () => {
    // 如果有 token，尝试获取公钥（用于后续可能的重新登录）
    try {
      await fetchPublicKey()
    } catch (e) {
      console.warn('[Auth] 获取公钥失败', e)
    }
  }

  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
  }

  return { token, refreshToken, username, isAuthenticated, setToken, setRefreshToken, setUsername, initAuth, logout }
})
