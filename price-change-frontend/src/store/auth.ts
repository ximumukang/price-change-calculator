import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUsername = (name: string) => {
    username.value = name
    localStorage.setItem('username', name)
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  return { token, username, setToken, setUsername, logout }
})
