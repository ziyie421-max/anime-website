// 评分相关API - 处理评分的增删改查
import api from './index'

// ==================== 评分相关 ====================

// 添加或更新评分
export const addOrUpdateRating = (data) => {
  return api.post('/ratings', data)
}

// 获取动漫评分统计（外部动漫）
export const getRatingStats = (externalAnimeId) => {
  return api.get(`/ratings/stats/external/${externalAnimeId}`)
}

// 获取用户对某动漫的评分
export const getUserRating = (externalAnimeId) => {
  return api.get(`/ratings/user/external/${externalAnimeId}`)
}

// 删除评分
export const deleteRating = (externalAnimeId) => {
  return api.delete(`/ratings/external/${externalAnimeId}`)
}

