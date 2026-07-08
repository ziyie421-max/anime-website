<!-- 用户观看历史页面 - 显示用户的观看记录 -->
<template>
  <div class="user-history">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>观看历史</h1>
      <div class="header-actions">
        <el-button
          v-if="historyList.length > 0"
          type="danger"
          plain
          @click="handleClearAll"
        >
          <el-icon><Delete /></el-icon> 清空历史
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="historyList.length === 0" description="还没有观看记录哦~">
      <el-button type="primary" @click="$router.push('/external')">去发现动漫</el-button>
    </el-empty>

    <!-- 历史列表 -->
    <div v-else class="history-list">
      <div
        v-for="item in historyList"
        :key="item.id"
        class="history-item"
      >
        <!-- 动漫封面 -->
        <div class="anime-cover" @click="goToAnime(item)">
          <el-image
            :src="item.externalAnimeCover || item.animeCover"
            fit="cover"
            lazy
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="30"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>

        <!-- 观看信息 -->
        <div class="watch-info" @click="goToAnime(item)">
          <h4 class="anime-title">{{ item.externalAnimeTitle || item.animeTitle }}</h4>
          <p class="episode-info">
            <el-icon><VideoPlay /></el-icon>
            {{ item.externalEpisodeName || '第' + item.episodeNumber + '集' }}
          </p>
          <p class="watch-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(item.lastWatchTime) }}
          </p>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" size="small" @click="goToPlay(item)">
            继续观看
          </el-button>
          <el-button type="danger" size="small" plain @click.stop="handleRemove(item)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalCount"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Delete, VideoPlay, Clock } from '@element-plus/icons-vue'
import { getWatchHistory, deleteWatchHistory, clearAllHistory } from '@/api/user'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const historyList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)
const totalPages = ref(0)

// 获取观看历史
const fetchHistory = async (page = 1) => {
  try {
    loading.value = true
    const response = await getWatchHistory(page - 1, pageSize.value) // 后端分页从0开始
    const data = response.data?.data || response.data

    historyList.value = data.content || data || []
    totalCount.value = data.totalElements || historyList.value.length
    totalPages.value = data.totalPages || 1
    currentPage.value = page
  } catch (error) {
    console.error('获取观看历史失败:', error)
    ElMessage.error('获取观看历史失败')
  } finally {
    loading.value = false
  }
}

// 删除单条记录
const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${item.externalAnimeTitle || item.animeTitle}" 的观看记录吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    await deleteWatchHistory(item.id)
    ElMessage.success('已删除')

    // 刷新列表
    fetchHistory(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除记录失败:', error)
      ElMessage.error('删除记录失败')
    }
  }
}

// 清空所有历史
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有观看历史吗？此操作不可恢复！',
      '警告',
      { confirmButtonText: '确定清空', cancelButtonText: '取消', type: 'warning' }
    )

    await clearAllHistory()
    ElMessage.success('已清空所有观看历史')
    historyList.value = []
    totalCount.value = 0
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空历史失败:', error)
      ElMessage.error('清空历史失败')
    }
  }
}

// 分页变化
const handlePageChange = (page) => {
  fetchHistory(page)
}

// 跳转到动漫详情
const goToAnime = (item) => {
  if (item.externalAnimeId) {
    router.push(`/anime/${item.externalAnimeId}`)
  } else if (item.animeId) {
    router.push(`/anime/${item.animeId}`)
  }
}

// 继续观看
const goToPlay = (item) => {
  if (item.externalAnimeId) {
    // 外部动漫跳转到播放页
    router.push({
      path: '/external/play',
      query: {
        id: item.externalAnimeId,
        episode: item.externalEpisodeIndex || 0
      }
    })
  } else if (item.animeId) {
    router.push(`/watch/${item.animeId}/${item.episodeId}`)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
  }
  // 小于24小时
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 小于7天
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  }
  // 其他显示日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.user-history {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  margin: 0;
}

.loading-container {
  padding: 40px;
}

/* 历史列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 历史项 */
.history-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 8px;
  box-shadow: var(--theme-shadow-light); /* 使用主题阴影 */
  transition: transform 0.3s;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.history-item:hover {
  transform: translateX(5px);
}

/* 封面 */
.anime-cover {
  flex-shrink: 0;
  width: 100px;
  height: 140px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
}

.anime-cover .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-background-mute); /* 使用主题 muted 背景 */
  color: var(--theme-text-placeholder); /* 使用主题占位符颜色 */
}

/* 观看信息 */
.watch-info {
  flex: 1;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.anime-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

.episode-info {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: var(--theme-primary); /* 使用主题主色 */
  display: flex;
  align-items: center;
  gap: 5px;
}

.watch-time {
  margin: 0;
  font-size: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 清空历史按钮样式 - 适配主题 */
.header-actions :deep(.el-button--danger.is-plain) {
  color: var(--theme-danger) !important;
  border-color: var(--theme-danger) !important;
}

.header-actions :deep(.el-button--danger.is-plain:hover) {
  background: var(--theme-danger) !important;
  color: white !important;
  border-color: var(--theme-danger) !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-history {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
    margin-bottom: 14px;
  }

  .page-header h1 {
    font-size: 18px;
  }

  .history-list {
    gap: 12px;
  }

  .history-item {
    flex-wrap: wrap;
    padding: 12px;
    gap: 12px;
  }

  .anime-cover {
    width: 80px;
    height: 110px;
  }

  .anime-title {
    font-size: 0.95rem;
    margin-bottom: 6px;
  }

  .episode-info,
  .watch-time {
    font-size: 12px;
  }

  /* 操作按钮换行到下一行，横向排列避免溢出 */
  .action-buttons {
    flex-direction: row;
    width: 100%;
    justify-content: flex-end;
    gap: 8px;
  }

  .pagination {
    margin-top: 20px;
  }
}
</style>

