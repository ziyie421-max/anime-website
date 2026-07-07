// 用户相关API - 处理用户登录、注册、个人信息等接口
import api from './index'

// ==================== 认证相关 ====================

// 用户登录
export const login = (credentials) => {
  return api.post('/auth/login', credentials)
}

// 用户注册
export const register = (userData) => {
  return api.post('/auth/register', userData)
}

// 用户登出
export const logout = () => {
  return api.post('/auth/logout')
}

// ==================== 用户资料 ====================

// 获取用户资料
export const getUserProfile = () => {
  return api.get('/user/profile')
}

// 获取用户信息（兼容旧接口）
export const getUserInfo = () => {
  return api.get('/user/profile')
}

// 更新用户资料
export const updateUserProfile = (userData) => {
  return api.put('/user/profile', userData)
}

// 更新用户信息（兼容旧接口）
export const updateUserInfo = (userData) => {
  return api.put('/user/profile', userData)
}

// 修改密码
export const changePassword = (passwordData) => {
  return api.put('/user/password', passwordData)
}

// 获取用户统计信息
export const getUserStats = () => {
  return api.get('/user/stats')
}

// ==================== 收藏功能 ====================

// 获取收藏列表（分页）
export const getFavorites = (page = 0, size = 20) => {
  return api.get('/favorites', { params: { page, size } })
}

// 获取全部收藏
export const getAllFavorites = () => {
  return api.get('/favorites/all')
}

// 添加收藏（外部动漫）
export const addFavorite = (animeData) => {
  return api.post('/favorites', animeData)
}

// 取消收藏
export const removeFavorite = (externalAnimeId, animeId = null) => {
  const params = {}
  if (externalAnimeId) params.externalAnimeId = externalAnimeId
  if (animeId) params.animeId = animeId
  return api.delete('/favorites', { params })
}

// 检查是否已收藏
export const checkFavorited = (externalAnimeId, animeId = null) => {
  const params = {}
  if (externalAnimeId) params.externalAnimeId = externalAnimeId
  if (animeId) params.animeId = animeId
  return api.get('/favorites/check', { params })
}

// 批量检查是否已收藏
export const checkFavoritedBatch = (externalAnimeIds) => {
  return api.post('/favorites/check-batch', externalAnimeIds)
}

// 获取收藏数量
export const getFavoriteCount = () => {
  return api.get('/favorites/count')
}

// 清空所有收藏
export const clearAllFavorites = () => {
  return api.delete('/favorites/all')
}

// 兼容旧接口
export const getUserFavorites = (page = 0, size = 20) => {
  return getFavorites(page, size)
}

// ==================== 观看历史 ====================

// 获取观看历史（分页）
export const getWatchHistory = (page = 0, size = 20) => {
  return api.get('/history', { params: { page, size } })
}

// 添加/更新观看历史
export const addWatchHistory = (historyData) => {
  return api.post('/history', historyData)
}

// 更新观看进度
export const updateWatchProgress = (historyId, progress, totalDuration = null) => {
  const params = { progress }
  if (totalDuration) params.totalDuration = totalDuration
  return api.put(`/history/${historyId}/progress`, null, { params })
}

// 获取最近观看的动漫
export const getRecentWatched = (limit = 10) => {
  return api.get('/history/recent', { params: { limit } })
}

// 获取继续观看列表
export const getContinueWatching = (limit = 10) => {
  return api.get('/history/continue', { params: { limit } })
}

// 获取某部动漫的观看记录
export const getAnimeWatchHistory = (externalAnimeId) => {
  return api.get(`/history/anime/${externalAnimeId}`)
}

// 删除单条观看记录
export const deleteWatchHistory = (historyId) => {
  return api.delete(`/history/${historyId}`)
}

// 清空所有观看历史
export const clearAllHistory = () => {
  return api.delete('/history/all')
}

// 获取观看历史数量
export const getHistoryCount = () => {
  return api.get('/history/count')
}
