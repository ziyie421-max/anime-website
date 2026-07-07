// 通知相关API - 处理通知的获取和管理
import api from './index'

// ==================== 通知相关 ====================

// 获取用户通知列表
export const getNotifications = (page = 0, size = 20) => {
  return api.get('/notifications', {
    params: { page, size }
  })
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return api.get('/notifications/unread/count')
}

// 获取未读通知列表
export const getUnreadNotifications = (page = 0, size = 20) => {
  return api.get('/notifications/unread', {
    params: { page, size }
  })
}

// 标记单个通知为已读
export const markAsRead = (notificationId) => {
  return api.put(`/notifications/${notificationId}/read`)
}

// 标记所有通知为已读
export const markAllAsRead = () => {
  return api.put('/notifications/read-all')
}

// 删除所有通知
export const deleteAllNotifications = () => {
  return api.delete('/notifications/all')
}

