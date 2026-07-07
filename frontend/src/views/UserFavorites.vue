<!-- 用户收藏页面 - 显示用户收藏的动漫列表 -->
<template>
  <div class="user-favorites">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>我的收藏</h1>
      <div class="header-actions">
        <el-button 
          v-if="favorites.length > 0" 
          type="danger" 
          plain 
          @click="handleClearAll"
        >
          <el-icon><Delete /></el-icon> 清空收藏
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="favorites.length === 0" description="还没有收藏任何动漫哦~">
      <el-button type="primary" @click="$router.push('/external')">去发现动漫</el-button>
    </el-empty>

    <!-- 收藏列表 -->
    <div v-else class="favorites-grid">
      <div 
        v-for="item in favorites" 
        :key="item.id" 
        class="anime-card"
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
                <el-icon :size="40"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <!-- 删除按钮 -->
          <div class="remove-btn" @click.stop="handleRemove(item)">
            <el-icon><Close /></el-icon>
          </div>
        </div>
        
        <!-- 动漫信息 -->
        <div class="anime-info" @click="goToAnime(item)">
          <h4 class="anime-title">{{ item.externalAnimeTitle || item.animeTitle }}</h4>
          <p class="source-info">{{ item.sourceName || '本地资源' }}</p>
          <p class="collect-time">收藏于 {{ formatTime(item.createdAt) }}</p>
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
import { Picture, Delete, Close } from '@element-plus/icons-vue'
import { getFavorites, removeFavorite, clearAllFavorites } from '@/api/user'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const favorites = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)
const totalPages = ref(0)

// 获取收藏列表
const fetchFavorites = async (page = 1) => {
  try {
    loading.value = true
    const response = await getFavorites(page - 1, pageSize.value) // 后端分页从0开始
    const data = response.data?.data || response.data
    
    favorites.value = data.content || data || []
    totalCount.value = data.totalElements || favorites.value.length
    totalPages.value = data.totalPages || 1
    currentPage.value = page
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 取消收藏
const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏 "${item.externalAnimeTitle || item.animeTitle}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    await removeFavorite(item.externalAnimeId, item.animeId)
    ElMessage.success('已取消收藏')
    
    // 刷新列表
    fetchFavorites(currentPage.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error('取消收藏失败')
    }
  }
}

// 清空所有收藏
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有收藏吗？此操作不可恢复！',
      '警告',
      { confirmButtonText: '确定清空', cancelButtonText: '取消', type: 'warning' }
    )
    
    await clearAllFavorites()
    ElMessage.success('已清空所有收藏')
    favorites.value = []
    totalCount.value = 0
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空收藏失败:', error)
      ElMessage.error('清空收藏失败')
    }
  }
}

// 分页变化
const handlePageChange = (page) => {
  fetchFavorites(page)
}

// 跳转到动漫详情
const goToAnime = (item) => {
  if (item.externalAnimeId) {
    router.push(`/anime/${item.externalAnimeId}`)
  } else if (item.animeId) {
    router.push(`/anime/${item.animeId}`)
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 初始化
onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.user-favorites {
  max-width: 1200px;
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

/* 收藏列表网格 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

/* 动漫卡片 */
.anime-card {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--theme-shadow-light); /* 使用主题阴影 */
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.anime-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--theme-shadow); /* 悬停时使用更深的阴影 */
}

/* 封面 */
.anime-cover {
  position: relative;
  cursor: pointer;
  aspect-ratio: 3/4;
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

/* 删除按钮 */
.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.anime-card:hover .remove-btn {
  opacity: 1;
}

.remove-btn:hover {
  background: var(--theme-danger); /* 使用主题危险色 */
}

/* 动漫信息 */
.anime-info {
  padding: 12px;
  cursor: pointer;
}

.anime-title {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

/* 移除资源来源显示，隐藏 source-info 元素 */
.source-info {
  display: none; /* 隐藏资源来源信息 */
}

.collect-time {
  margin: 0;
  font-size: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 清空收藏按钮样式 - 适配主题 */
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
  .favorites-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>

