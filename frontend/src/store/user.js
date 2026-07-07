// 用户状态管理 - 管理用户登录状态、个人信息等
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, logout as apiLogout, getUserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // 状态定义
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  // 登录方法
  const login = async (credentials) => {
    try {
      const response = await apiLogin(credentials)
      token.value = response.data.token
      userInfo.value = response.data.user
      
      // 保存到本地存储
      localStorage.setItem('token', token.value)
      localStorage.setItem('userRole', userInfo.value.role)
      
      return { success: true }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  // 登出方法
  const logout = async () => {
    try {
      await apiLogout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除状态和本地存储
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return
    
    try {
      const response = await getUserInfo()
      userInfo.value = response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果token无效，清除登录状态
      if (error.response?.status === 401) {
        logout()
      }
    }
  }

  // 初始化时获取用户信息
  if (token.value) {
    fetchUserInfo()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    logout,
    fetchUserInfo
  }
})
