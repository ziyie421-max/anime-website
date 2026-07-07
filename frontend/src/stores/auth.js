import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authAPI } from '@/services/api'
import { ElMessage } from 'element-plus'

/**
 * 认证状态管理 - 管理用户登录状态和用户信息
 */
export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('accessToken'))
  const refreshToken = ref(localStorage.getItem('refreshToken'))
  const isLoading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!accessToken.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const userDisplayName = computed(() => user.value?.nickname || user.value?.username || '未知用户')

  // 保存令牌到本地存储
  const saveTokens = (access, refresh) => {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    if (refresh) {
      localStorage.setItem('refreshToken', refresh)
    }
  }

  // 清除令牌
  const clearTokens = () => {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  // 用户登录
  const login = async (loginData) => {
    try {
      isLoading.value = true
      console.log('开始登录:', loginData.usernameOrEmail)

      const response = await authAPI.login(loginData)
      
      // 保存令牌和用户信息
      saveTokens(response.accessToken, response.refreshToken)
      user.value = response.user

      ElMessage.success('登录成功！')
      console.log('登录成功:', user.value.username)
      
      return response
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 用户注册
  const register = async (registerData) => {
    try {
      isLoading.value = true
      console.log('开始注册:', registerData.username)

      const response = await authAPI.register(registerData)
      
      // 保存令牌和用户信息
      saveTokens(response.accessToken, response.refreshToken)
      user.value = response.user

      ElMessage.success('注册成功！')
      console.log('注册成功:', user.value.username)
      
      return response
    } catch (error) {
      console.error('注册失败:', error)
      ElMessage.error(error.message || '注册失败')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 用户登出
  const logout = async () => {
    try {
      // 调用后端登出API（可选）
      await authAPI.logout()
    } catch (error) {
      console.warn('后端登出失败:', error)
    } finally {
      // 无论后端是否成功，都清除本地状态
      clearTokens()
      ElMessage.success('已退出登录')
      console.log('用户已登出')
    }
  }

  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    if (!accessToken.value) {
      return null
    }

    try {
      const userData = await authAPI.getCurrentUser()
      user.value = userData
      console.log('获取用户信息成功:', userData.username)
      return userData
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果令牌无效，清除本地状态
      if (error.message?.includes('未授权') || error.message?.includes('401')) {
        clearTokens()
      }
      throw error
    }
  }

  // 刷新访问令牌
  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      throw new Error('没有刷新令牌')
    }

    try {
      const response = await authAPI.refreshToken(refreshToken.value)
      saveTokens(response.accessToken, response.refreshToken)
      user.value = response.user
      console.log('令牌刷新成功')
      return response
    } catch (error) {
      console.error('令牌刷新失败:', error)
      clearTokens()
      throw error
    }
  }

  // 检查用户名是否可用
  const checkUsername = async (username) => {
    try {
      const response = await authAPI.checkUsername(username)
      return response.available
    } catch (error) {
      console.error('检查用户名失败:', error)
      return false
    }
  }

  // 检查邮箱是否可用
  const checkEmail = async (email) => {
    try {
      const response = await authAPI.checkEmail(email)
      return response.available
    } catch (error) {
      console.error('检查邮箱失败:', error)
      return false
    }
  }

  // 初始化认证状态
  const initAuth = async () => {
    if (accessToken.value) {
      try {
        await fetchCurrentUser()
      } catch (error) {
        console.warn('初始化认证状态失败:', error)
        clearTokens()
      }
    }
  }

  return {
    // 状态
    user,
    accessToken,
    refreshToken,
    isLoading,
    
    // 计算属性
    isLoggedIn,
    isAdmin,
    userDisplayName,
    
    // 方法
    login,
    register,
    logout,
    fetchCurrentUser,
    refreshAccessToken,
    checkUsername,
    checkEmail,
    initAuth,
    clearTokens
  }
})
