<template>
  <!-- 评论区组件 -->
  <div class="comment-section">
    <!-- 评论区标题 -->
    <div class="section-header">
      <h3>
        <el-icon><ChatDotRound /></el-icon>
        评论区 ({{ totalComments }})
      </h3>
    </div>

    <!-- 发表评论框 -->
    <div class="comment-input-box" v-if="isLoggedIn">
      <el-avatar :size="40" :src="userAvatar">
        <el-icon><User /></el-icon>
      </el-avatar>
      <div class="input-wrapper">
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="发表你的评论..."
          maxlength="1000"
          show-word-limit
        />
        <el-button 
          type="primary" 
          @click="submitComment"
          :loading="submitting"
          :disabled="!newComment.trim()"
        >
          发表评论
        </el-button>
      </div>
    </div>
    <div class="login-hint" v-else>
      <el-button type="primary" link @click="goToLogin">登录</el-button>
      后发表评论
    </div>

    <!-- 评论列表 -->
    <div class="comment-list" v-loading="loading">
      <div v-if="comments.length === 0 && !loading" class="empty-comments">
        <el-empty description="暂无评论，快来抢沙发吧~" />
      </div>

      <div 
        v-for="comment in comments" 
        :key="comment.id" 
        class="comment-item"
        :id="'comment-' + comment.id"
      >
        <!-- 主评论 -->
        <div class="comment-main">
          <el-avatar :size="40" :src="comment.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <div class="comment-content">
            <div class="comment-header">
              <span class="username">{{ comment.nickname || comment.username }}</span>
              <span class="time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
            <div class="comment-actions">
              <span class="action-btn" @click="handleLike(comment)">
                <el-icon><Star /></el-icon>
                {{ comment.likeCount || 0 }}
              </span>
              <span class="action-btn" @click="showReplyInput(comment)">
                <el-icon><ChatLineRound /></el-icon>
                回复
              </span>
              <span 
                v-if="canDelete(comment)" 
                class="action-btn delete-btn" 
                @click="handleDelete(comment)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </span>
            </div>

            <!-- 回复输入框 -->
            <div v-if="replyingTo === comment.id" class="reply-input">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="'回复 @' + (comment.nickname || comment.username)"
                maxlength="500"
              />
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="submitReply(comment)"
                  :loading="submitting"
                >
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 回复列表 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
          <div 
            v-for="reply in comment.replies" 
            :key="reply.id" 
            class="reply-item"
            :id="'comment-' + reply.id"
          >
            <el-avatar :size="32" :src="reply.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="comment-content">
              <div class="comment-header">
                <span class="username">{{ reply.nickname || reply.username }}</span>
                <span class="time">{{ formatTime(reply.createdAt) }}</span>
              </div>
              <div class="comment-text">{{ reply.content }}</div>
              <div class="comment-actions">
                <span class="action-btn" @click="handleLike(reply)">
                  <el-icon><Star /></el-icon>
                  {{ reply.likeCount || 0 }}
                </span>
                <span class="action-btn" @click="showReplyInput(comment, reply)">
                  <el-icon><ChatLineRound /></el-icon>
                  回复
                </span>
                <span 
                  v-if="canDelete(reply)" 
                  class="action-btn delete-btn" 
                  @click="handleDelete(reply)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <el-button @click="loadMore" :loading="loadingMore">加载更多评论</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, User, Star, ChatLineRound, Delete } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { addComment, getCommentsByExternalAnime, likeComment, deleteComment } from '@/api/comment'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// Props
const props = defineProps({
  externalAnimeId: {
    type: String,
    required: true
  },
  externalAnimeTitle: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const authStore = useAuthStore()

// 状态
const loading = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const comments = ref([])
const totalComments = ref(0)
const currentPage = ref(0)
const hasMore = ref(false)
const newComment = ref('')
const replyingTo = ref(null)
const replyContent = ref('')

// 计算属性
const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'))
const userAvatar = computed(() => authStore.user?.avatar || '')
const currentUserId = computed(() => authStore.user?.id)

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 判断是否可以删除
const canDelete = (comment) => {
  return currentUserId.value && comment.userId === currentUserId.value
}

// 跳转登录
const goToLogin = () => {
  router.push('/login')
}

// 加载评论
const loadComments = async (page = 0) => {
  try {
    if (page === 0) {
      loading.value = true
    } else {
      loadingMore.value = true
    }

    const response = await getCommentsByExternalAnime(props.externalAnimeId, page, 20)
    if (response.data.success) {
      const data = response.data.data
      if (page === 0) {
        comments.value = data.comments
      } else {
        comments.value.push(...data.comments)
      }
      totalComments.value = data.totalComments
      currentPage.value = page
      hasMore.value = page < data.totalPages - 1
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 加载更多
const loadMore = () => {
  loadComments(currentPage.value + 1)
}

// 发表评论
const submitComment = async () => {
  if (!newComment.value.trim()) return

  try {
    submitting.value = true
    const response = await addComment({
      externalAnimeId: props.externalAnimeId,
      externalAnimeTitle: props.externalAnimeTitle,
      content: newComment.value.trim()
    })

    if (response.data.success) {
      ElMessage.success('评论发表成功')
      newComment.value = ''
      // 重新加载评论
      loadComments(0)
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

// 显示回复输入框
const showReplyInput = (comment, reply = null) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  replyingTo.value = comment.id
  replyContent.value = reply ? `@${reply.nickname || reply.username} ` : ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async (parentComment) => {
  if (!replyContent.value.trim()) return

  try {
    submitting.value = true
    const response = await addComment({
      externalAnimeId: props.externalAnimeId,
      externalAnimeTitle: props.externalAnimeTitle,
      parentId: parentComment.id,
      content: replyContent.value.trim()
    })

    if (response.data.success) {
      ElMessage.success('回复成功')
      cancelReply()
      loadComments(0)
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

// 点赞
const handleLike = async (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const response = await likeComment(comment.id)
    if (response.data.success) {
      comment.likeCount = response.data.data.likeCount
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 删除评论
const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteComment(comment.id)
    if (response.data.success) {
      ElMessage.success('评论已删除')
      loadComments(0)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadComments(0)
})
</script>

<style scoped>
.comment-section {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px 0;
  font-size: 18px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

/* 评论输入框 */
.comment-input-box {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--theme-border); /* 使用主题边框 */
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-wrapper .el-button {
  align-self: flex-end;
}

/* 评论输入框主题适配 */
.comment-input-box :deep(.el-textarea__inner) {
  background-color: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
}

.comment-input-box :deep(.el-textarea__inner::placeholder) {
  color: var(--theme-text-placeholder) !important;
}

.comment-input-box :deep(.el-textarea__inner:focus) {
  border-color: var(--theme-primary) !important;
}

/* 修复输入框字数统计背景色 */
.comment-input-box :deep(.el-input__count) {
  background-color: var(--theme-background) !important;
  color: var(--theme-text-secondary) !important;
}

.comment-input-box :deep(.el-input__count-inner) {
  background-color: var(--theme-background) !important;
  color: var(--theme-text-secondary) !important;
}

/* 发表评论按钮主题适配 */
.comment-input-box :deep(.el-button--primary) {
  background-color: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

.comment-input-box :deep(.el-button--primary:hover) {
  background-color: var(--theme-primary-light) !important;
  border-color: var(--theme-primary-light) !important;
}

.login-hint {
  text-align: center;
  padding: 20px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  border-radius: 8px;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
  margin-bottom: 20px;
}

/* 评论列表 */
.comment-list {
  min-height: 100px;
}

.empty-comments {
  padding: 40px 0;
}

.comment-item {
  margin-bottom: 20px;
}

.comment-main,
.reply-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.username {
  font-weight: 500;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

.time {
  font-size: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

.comment-text {
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  line-height: 1.6;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  cursor: pointer;
  transition: color 0.2s;
}

.action-btn:hover {
  color: var(--theme-primary); /* 使用主题主色 */
}

.delete-btn:hover {
  color: var(--theme-danger); /* 使用主题危险色 */
}

/* 回复输入框 */
.reply-input {
  margin-top: 12px;
  padding: 12px;
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  border-radius: 8px;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

/* 回复输入框主题适配 */
.reply-input :deep(.el-textarea__inner) {
  background-color: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
}

.reply-input :deep(.el-textarea__inner::placeholder) {
  color: var(--theme-text-placeholder) !important;
}

/* 修复回复输入框字数统计背景色 */
.reply-input :deep(.el-input__count) {
  background-color: var(--theme-background) !important;
  color: var(--theme-text-secondary) !important;
}

.reply-input :deep(.el-input__count-inner) {
  background-color: var(--theme-background) !important;
  color: var(--theme-text-secondary) !important;
}

/* 回复按钮主题适配 */
.reply-input :deep(.el-button--primary) {
  background-color: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

.reply-input :deep(.el-button--primary:hover) {
  background-color: var(--theme-primary-light) !important;
  border-color: var(--theme-primary-light) !important;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

/* 回复列表 */
.replies-list {
  margin-left: 52px;
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid var(--theme-border); /* 使用主题边框 */
}

.reply-item {
  margin-bottom: 12px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

/* 加载更多 */
.load-more {
  text-align: center;
  margin-top: 20px;
}
</style>

