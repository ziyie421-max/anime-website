<template>
  <!-- 评分面板组件 -->
  <div class="rating-panel">
    <!-- 评分统计 -->
    <div class="rating-stats">
      <div class="average-score">
        <span class="score-value">{{ displayAverageScore }}</span>
        <span class="score-max">/10</span>
      </div>
      <div class="rating-info">
        <el-rate
          :model-value="averageScoreForRate"
          disabled
          :colors="[themeColors.averageStar, themeColors.averageStar, themeColors.averageStar]"
          :void-color="isDarkTheme ? '#4a4a4a' : '#dcdfe6'"
          :disabled-void-color="isDarkTheme ? '#4a4a4a' : '#dcdfe6'"
          :max="5"
        />
        <span class="rating-count">{{ ratingCount }} 人评分</span>
      </div>
    </div>

    <!-- 用户评分 -->
    <div class="user-rating" v-if="isLoggedIn">
      <div class="rating-label">
        {{ userRating ? '我的评分' : '给个评分吧' }}
      </div>
      <div class="rating-input">
        <el-rate
          v-model="userScore"
          :colors="[themeColors.userStar, themeColors.userStar, themeColors.userStar]"
          :void-color="isDarkTheme ? '#4a4a4a' : '#dcdfe6'"
          :disabled-void-color="isDarkTheme ? '#4a4a4a' : '#dcdfe6'"
          :max="5"
          allow-half
          @change="handleRatingChange"
        />
        <span class="user-score-display" v-if="userScore > 0">
          {{ (userScore * 2).toFixed(1) }} 分
        </span>
      </div>
      <el-button 
        v-if="userRating"
        type="danger"
        link
        size="small"
        @click="handleDeleteRating"
      >
        删除评分
      </el-button>
    </div>
    <div class="login-hint" v-else>
      <el-button type="primary" link @click="goToLogin">登录</el-button>
      后评分
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getRatingStats, addOrUpdateRating, deleteRating } from '@/api/rating'

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
const averageScore = ref(null)
const ratingCount = ref(0)
const userRating = ref(null)
const userScore = ref(0)

// 计算属性
const isLoggedIn = computed(() => !!localStorage.getItem('accessToken'))

// 判断当前是否为暗色主题
const isDarkTheme = computed(() => {
  return document.body.classList.contains('theme-dark')
})

// 主题颜色计算属性 - 监听主题变化
const themeColors = computed(() => {
  const isDark = document.body.classList.contains('theme-dark')
  return {
    // 平均评分星星颜色：两种主题都使用橙色/黄色
    averageStar: isDark ? '#f4d03f' : '#e6a23c',
    // 用户评分星星颜色：暗粉主题粉色，蓝白主题蓝色
    userStar: isDark ? '#EA7A99' : '#4EABE6',
    // 警告色
    warning: isDark ? '#f4d03f' : '#e6a23c'
  }
})

// 监听主题变化，强制重新渲染评分组件
watch(() => document.body.classList.contains('theme-dark'), () => {
  // 主题变化时，重新加载评分以更新颜色
  loadRatingStats()
})

// 显示的平均分
const displayAverageScore = computed(() => {
  if (averageScore.value === null) return '-'
  return averageScore.value.toFixed(1)
})

// 用于 el-rate 的平均分（5星制）
const averageScoreForRate = computed(() => {
  if (averageScore.value === null) return 0
  return averageScore.value / 2
})

// 跳转登录
const goToLogin = () => {
  router.push('/login')
}

// 加载评分统计
const loadRatingStats = async () => {
  try {
    const response = await getRatingStats(props.externalAnimeId)
    if (response.data.success) {
      const data = response.data.data
      averageScore.value = data.averageScore
      ratingCount.value = data.ratingCount
      
      // 如果有用户评分
      if (data.userRating) {
        userRating.value = data.userRating
        userScore.value = data.userRating.displayScore / 2  // 转换为5星制
      }
    }
  } catch (error) {
    console.error('加载评分统计失败:', error)
  }
}

// 处理评分变化
const handleRatingChange = async (value) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    userScore.value = 0
    return
  }

  try {
    // 转换为100分制（value是5星制，每星2分，所以乘以20）
    const score = Math.round(value * 20)
    
    const response = await addOrUpdateRating({
      externalAnimeId: props.externalAnimeId,
      externalAnimeTitle: props.externalAnimeTitle,
      score: score
    })

    if (response.data.success) {
      ElMessage.success('评分成功')
      userRating.value = response.data.data
      // 重新加载统计
      loadRatingStats()
    }
  } catch (error) {
    console.error('评分失败:', error)
    ElMessage.error('评分失败')
    // 恢复原来的评分
    if (userRating.value) {
      userScore.value = userRating.value.displayScore / 2
    } else {
      userScore.value = 0
    }
  }
}

// 删除评分
const handleDeleteRating = async () => {
  try {
    const response = await deleteRating(props.externalAnimeId)
    if (response.data.success) {
      ElMessage.success('评分已删除')
      userRating.value = null
      userScore.value = 0
      loadRatingStats()
    }
  } catch (error) {
    console.error('删除评分失败:', error)
    ElMessage.error('删除失败')
  }
}

// 监听动漫ID变化
watch(() => props.externalAnimeId, () => {
  loadRatingStats()
})

// 初始化
onMounted(() => {
  loadRatingStats()
})
</script>

<style scoped>
.rating-panel {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

/* 评分统计 */
.rating-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--theme-border); /* 使用主题边框 */
  margin-bottom: 16px;
}

.average-score {
  display: flex;
  align-items: baseline;
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  color: var(--theme-warning); /* 使用主题警告色 (橙色) */
}

.score-max {
  font-size: 16px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

.rating-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rating-count {
  font-size: 13px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

/* 用户评分 */
.user-rating {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-label {
  font-size: 14px;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
}

.rating-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-score-display {
  font-size: 16px;
  font-weight: 500;
  color: var(--theme-primary); /* 使用主题主色 */
}

.login-hint {
  text-align: center;
  padding: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  border-radius: 8px;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

/* 登录按钮主题适配 */
.login-hint :deep(.el-button--primary) {
  color: var(--theme-primary) !important;
}

.login-hint :deep(.el-button--primary:hover) {
  color: var(--theme-primary-light) !important;
}

/* 删除评分按钮主题适配 */
.user-rating :deep(.el-button--danger.is-link) {
  color: var(--theme-danger) !important;
}

.user-rating :deep(.el-button--danger.is-link:hover) {
  color: var(--theme-danger) !important;
}

/* 响应式 - 移动端适配 */
@media (max-width: 768px) {
  .rating-panel {
    padding: 14px;
  }

  .rating-stats {
    gap: 12px;
    padding-bottom: 12px;
    margin-bottom: 12px;
  }

  /* 36px 大字号在窄屏偏大，适度缩小 */
  .score-value {
    font-size: 28px;
  }

  .score-max {
    font-size: 14px;
  }

  .rating-count {
    font-size: 12px;
  }

  .rating-label {
    font-size: 13px;
  }

  .user-score-display {
    font-size: 14px;
  }

  .login-hint {
    padding: 10px;
  }
}
</style>

