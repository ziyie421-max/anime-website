// 评论相关API - 处理评论的增删改查
import api from './index'

// ==================== 评论相关 ====================

// 发表评论
export const addComment = (data) => {
  return api.post('/comments', data)
}

// 获取动漫评论列表（外部动漫）
export const getCommentsByExternalAnime = (externalAnimeId, page = 0, size = 20) => {
  return api.get(`/comments/anime/external/${externalAnimeId}`, {
    params: { page, size }
  })
}

// 获取评论的回复列表
export const getReplies = (commentId) => {
  return api.get(`/comments/${commentId}/replies`)
}

// 删除评论
export const deleteComment = (commentId) => {
  return api.delete(`/comments/${commentId}`)
}

// 点赞评论
export const likeComment = (commentId) => {
  return api.post(`/comments/${commentId}/like`)
}

