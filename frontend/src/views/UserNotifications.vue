<template>
  <!-- 用户通知页面 -->
  <div class="notifications-page">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1>
          <el-icon><Bell /></el-icon>
          我的通知
        </h1>
        <div class="header-actions">
          <el-button 
            v-if="notifications.length > 0"
            type="primary" 
            link 
            @click="handleMarkAllRead"
          >
            <el-icon><Check /></el-icon>
            全部已读
          </el-button>
          <el-button 
            v-if="notifications.length > 0"
            type="danger" 
            link 
            @click="handleDeleteAll"
          >
            <el-icon><Delete /></el-icon>
            清空通知
          </el-button>
        </div>
      </div>

      <!-- 通知列表 -->
      <div class="notifications-list" v-loading="loading">
        <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />

        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <!-- 发送者头像 -->
          <el-avatar :size="48" :src="notification.senderAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>

          <!-- 通知内容 -->
          <div class="notification-content">
            <div class="notification-header">
              <span class="notification-type">
                <el-tag size="small" :type="getTypeTagType(notification.type)">
                  {{ notification.typeName }}
                </el-tag>
              </span>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-text">{{ notification.content }}</div>
            <div class="notification-source" v-if="notification.externalAnimeTitle">
              来自: {{ notification.externalAnimeTitle }}
            </div>
          </div>

          <!-- 未读标记 -->
          <div class="unread-dot" v-if="!notification.isRead"></div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalElements"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Check, Delete, User } from '@element-plus/icons-vue'
import { 
  getNotifications, 
  markAsRead, 
  markAllAsRead, 
  deleteAllNotifications 
} from '@/api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 状态
const loading = ref(false)
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const totalPages = ref(0)

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  switch (type) {
    case 'COMMENT_REPLY':
      return 'primary'
    case 'COMMENT_LIKE':
      return 'warning'
    case 'SYSTEM':
      return 'info'
    default:
      return ''
  }
}

// 加载通知
const loadNotifications = async () => {
  try {
    loading.value = true
    const response = await getNotifications(currentPage.value - 1, pageSize.value)
    if (response.data.success) {
      const data = response.data.data
      notifications.value = data.notifications
      totalElements.value = data.totalElements
      totalPages.value = data.totalPages
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

// 处理通知点击
const handleNotificationClick = async (notification) => {
  // 标记为已读
  if (!notification.isRead) {
    try {
      await markAsRead(notification.id)
      notification.isRead = true
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 跳转到相关页面
  if (notification.link) {
    router.push(notification.link)
  }
}

// 全部标记已读
const handleMarkAllRead = async () => {
  try {
    const response = await markAllAsRead()
    if (response.data.success) {
      ElMessage.success('已全部标记为已读')
      // 更新本地状态
      notifications.value.forEach(n => n.isRead = true)
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 清空所有通知
const handleDeleteAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteAllNotifications()
    if (response.data.success) {
      ElMessage.success('已清空所有通知')
      notifications.value = []
      totalElements.value = 0
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空通知失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadNotifications()
}

// 初始化
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  min-height: 100vh;
  padding: 20px;
  background: var(--theme-background); /* 使用主题背景色 */
}

.page-container {
  max-width: 800px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 16px;
}

/* 通知列表 */
.notifications-list {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--theme-border); /* 使用主题边框 */
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
}

.notification-item.unread {
  background: var(--theme-background-mute); /* 使用主题 muted 背景 */
}

.notification-item.unread:hover {
  background: var(--theme-background-soft); /* 悬停时使用浅色背景 */
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.notification-time {
  font-size: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

.notification-title {
  font-weight: 500;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  margin-bottom: 4px;
}

.notification-text {
  font-size: 14px;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-source {
  font-size: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  margin-top: 6px;
}

/* 未读标记 */
.unread-dot {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  background: var(--theme-primary); /* 使用主题主色 */
  border-radius: 50%;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式 - 移动端适配 */
@media (max-width: 768px) {
  .notifications-page {
    padding: 12px;
  }

  .page-header {
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 16px;
  }

  .page-header h1 {
    font-size: 18px;
    gap: 6px;
  }

  .header-actions {
    gap: 10px;
  }

  .notification-item {
    padding: 12px;
    gap: 12px;
  }

  .notification-header {
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
  }

  .notification-text {
    font-size: 13px;
  }

  /* 未读圆点贴近右上角，避免与小屏内容重叠 */
  .unread-dot {
    top: 12px;
    right: 12px;
  }

  .pagination {
    margin-top: 18px;
  }
}
</style>

