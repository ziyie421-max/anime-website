<template>
  <div class="chinese-anime-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">国产动漫</h1>
      <p class="page-subtitle">精选中国动漫作品</p>
    </div>

    <!-- 动漫列表 -->
    <div class="anime-list-section">
      <div class="anime-grid" v-loading="isLoading">
        <div
          v-for="anime in animeList"
          :key="anime.vod_id"
          class="anime-card"
          @click="viewAnimeDetail(anime)"
        >
          <div class="anime-poster">
            <img
              v-if="anime.vod_pic"
              :src="anime.vod_pic"
              :alt="anime.vod_name"
              @error="handleImageError"
            />
            <div v-else class="placeholder-cover">
              <el-icon><VideoPlay /></el-icon>
            </div>

            <!-- 悬停遮罩层 -->
            <div class="anime-overlay">
              <el-icon class="play-icon"><VideoPlay /></el-icon>
              <!-- 悬停时显示年份 - 位置在底部 -->
              <div class="anime-year" v-if="anime.vod_year">
                {{ anime.vod_year }}年
              </div>
            </div>

            <!-- 集数标签 -->
            <div class="episode-badge" v-if="anime.vod_remarks">
              {{ anime.vod_remarks }}
            </div>
          </div>

          <!-- 动漫信息 -->
          <div class="anime-info">
            <h4 class="anime-title">{{ anime.vod_name }}</h4>
            <p class="anime-meta">
              <span v-if="anime.vod_year">{{ anime.vod_year }}年</span>
              <span v-if="anime.type_name">{{ anime.type_name }}</span>
            </p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!isLoading && animeList.length === 0"
        description="暂无国产动漫内容"
        :image-size="200"
      />
    </div>

    <!-- 分页器 -->
    <div class="pagination-section" v-if="animeData && animeData.pagecount > 1">
      <div class="custom-pagination">
        <!-- 主分页器 -->
        <el-pagination
          v-model:current-page="currentPage"
          :page-count="animeData.pagecount"
          layout="prev, pager, next"
          prev-text="上一页"
          next-text="下一页"
          background
          @current-change="handlePageChange"
        />


      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoPlay, Clock } from '@element-plus/icons-vue'
import { externalAPI } from '@/services/api'
import { filterAnimeResponse } from '@/utils/animeFilter'

// 路由
const router = useRouter()

// 响应式数据
const isLoading = ref(false)
const currentPage = ref(1)
const animeData = ref(null)
const animeList = ref([])


// 固定分类为中国动漫
const CATEGORY = '中国动漫'

// 获取动漫列表
const fetchAnimeList = async (page = 1) => {
  try {
    isLoading.value = true
    console.log('📡 获取国产动漫列表:', { category: CATEGORY, page })

    const response = await externalAPI.getAnimeList(CATEGORY, page)
    console.log('📡 API响应:', response)

    if (response && response.code === 1) {
      // 应用动漫内容筛选
      const filteredResponse = filterAnimeResponse(response)
      console.log('📡 筛选后响应:', filteredResponse)

      animeData.value = response
      animeList.value = filteredResponse.list || []

      const animeCount = filteredResponse.list ? filteredResponse.list.length : 0
      console.log('📡 获取国产动漫数据成功，筛选后:', animeCount, '部动漫')

      // 设置分页器中文文本
      setPaginationChineseText()

      if (animeList.value.length === 0) {
        ElMessage.info('未找到相关动漫内容（已过滤无关内容）')
      }
    } else {
      animeData.value = null
      animeList.value = []
      ElMessage.error('获取动漫数据失败')
    }
  } catch (error) {
    console.error('📡 获取动漫列表失败:', error)
    ElMessage.error('获取动漫数据失败: ' + error.message)
    animeData.value = null
    animeList.value = []
  } finally {
    isLoading.value = false
  }
}

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAnimeList(page)

  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })

  // 重新设置分页器中文文本
  setPaginationChineseText()
}



// 查看动漫详情
const viewAnimeDetail = (anime) => {
  console.log('🎬 查看动漫详情:', anime.vod_name)
  router.push({
    name: 'NewAnimeDetail',
    params: { id: anime.vod_id }
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  try {
    const date = new Date(timeStr)
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.style.display = 'none'
  // 显示占位符
  const placeholder = event.target.parentNode.querySelector('.placeholder-cover')
  if (placeholder) {
    placeholder.style.display = 'flex'
  }
}

// 设置分页器中文文本
const setPaginationChineseText = () => {
  // 多次尝试设置，确保DOM完全渲染后生效
  const attempts = [100, 300, 500]

  attempts.forEach(delay => {
    setTimeout(() => {
      // 查找分页器容器
      const paginationSection = document.querySelector('.pagination-section')
      if (!paginationSection) return

      // 设置"Go to"为"跳至"
      const jumpText = paginationSection.querySelector('.el-pagination__jump')
      if (jumpText && jumpText.textContent.includes('Go to')) {
        jumpText.textContent = jumpText.textContent.replace('Go to', '跳至')
      }

      // 设置"Total"为"共 X 页"
      const totalText = paginationSection.querySelector('.el-pagination__total')
      if (totalText && totalText.textContent.includes('Total')) {
        const pageCount = animeData.value?.pagecount || 0
        totalText.textContent = `共 ${pageCount} 页`
      }

      // 查找并替换所有可能的英文文本
      const walker = document.createTreeWalker(
        paginationSection,
        NodeFilter.SHOW_TEXT,
        null,
        false
      )

      let node
      while (node = walker.nextNode()) {
        if (node.textContent.includes('Go to')) {
          node.textContent = node.textContent.replace('Go to', '跳至')
        }
        if (node.textContent.includes('Total')) {
          const pageCount = animeData.value?.pagecount || 0
          node.textContent = node.textContent.replace(/Total\s+\d+/, `共 ${pageCount} 页`)
        }
      }
    }, delay)
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAnimeList(1)
})
</script>

<style scoped>
.chinese-anime-page {
  padding: 20px;
  background: var(--theme-background);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--theme-text-primary);
  margin: 0 0 10px 0;
  background: linear-gradient(135deg, var(--theme-primary), var(--theme-primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 16px;
  color: var(--theme-text-secondary);
  margin: 0;
}

.anime-list-section {
  margin-bottom: 30px;
}

/* 动漫网格布局 - 4列布局，保持卡片宽度，调整间距 */
.anime-grid {
  display: grid;
  grid-template-columns: repeat(4, 300px); /* 4列，每列宽度300px */
  gap: 40px; /* 调整间距为40px，适合4列布局 */
  padding: 20px;
  justify-content: center; /* 居中显示 */
}

/* 动漫卡片样式 - 与首页完全一致 */
.anime-card {
  background: var(--theme-background);
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--theme-shadow-light);
  border: 1px solid var(--theme-border);
}

.anime-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--theme-shadow);
}

.anime-poster {
  position: relative;
  overflow: hidden;
  height: 300px;
}

.anime-poster img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.anime-card:hover .anime-poster img {
  transform: scale(1.05);
}

.placeholder-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--theme-background-soft), var(--theme-background-mute)); /* 使用主题色渐变 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--theme-text-secondary);
  font-size: 1.5rem;
  font-weight: bold;
}

.placeholder-cover .el-icon {
  margin-bottom: 10px;
  opacity: 0.7;
}

.anime-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8); /* 浅色主题：半透明白色 */
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 20px;
  opacity: 0; /* 初始透明 */
  transition: all 0.3s ease;
  pointer-events: none; /* 防止遮罩层阻止点击事件 */
}

/* 暗色主题下的悬停效果 */
.theme-dark .anime-overlay {
  background: rgba(0, 0, 0, 0.8); /* 暗色主题：半透明暗色 */
}

.anime-card:hover .anime-overlay {
  opacity: 0.95; /* 悬停时显示，降低透明度让年份标志更清晰 */
}

.anime-year {
  color: white; /* 白色文字确保在主题色背景上清晰可见 */
  font-size: 12px;
  background: var(--theme-primary); /* 使用主题主色作为背景 */
  padding: 4px 8px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  box-shadow: var(--theme-shadow-light); /* 添加主题阴影 */
}

/* 暗色主题下的年份标签 */
.theme-dark .anime-year {
  background: var(--theme-primary); /* 暗色主题也使用主题主色 */
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.anime-info {
  padding: 15px;
  background: var(--theme-background); /* 使用主题背景色 */
}

.anime-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.anime-meta {
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.anime-meta span {
  margin-right: 10px;
}

/* 分页器样式 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: var(--theme-background-soft);
  border-radius: 8px;
  border: 1px solid var(--theme-border);
  margin-top: 20px;
}

.custom-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}



/* Element Plus 分页器主题样式 */
.pagination-section :deep(.el-pagination) {
  --el-pagination-bg-color: var(--theme-background);
  --el-pagination-text-color: var(--theme-text-primary);
  --el-pagination-border-radius: 6px;
  --el-pagination-button-color: var(--theme-text-secondary);
  --el-pagination-hover-color: var(--theme-primary);
  --el-pagination-button-bg-color: var(--theme-background);
  --el-pagination-button-disabled-color: var(--theme-text-placeholder);
  --el-pagination-button-disabled-bg-color: var(--theme-background-mute);
  --el-pagination-hover-bg-color: var(--theme-background-soft);
}

/* 分页按钮样式 */
.pagination-section :deep(.el-pager li) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
  margin: 0 2px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.pagination-section :deep(.el-pager li:hover:not(.is-active)) {
  background: var(--theme-background-soft) !important;
  border-color: var(--theme-primary) !important;
  color: var(--theme-primary) !important;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-section :deep(.el-pager li.is-active) {
  background: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
  color: white !important;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 上一页/下一页按钮 */
.pagination-section :deep(.btn-prev),
.pagination-section :deep(.btn-next) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
  border-radius: 6px;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.pagination-section :deep(.btn-prev:hover),
.pagination-section :deep(.btn-next:hover) {
  background: var(--theme-background-soft) !important;
  border-color: var(--theme-primary) !important;
  color: var(--theme-primary) !important;
  transform: translateY(-1px);
}

.pagination-section :deep(.btn-prev:disabled),
.pagination-section :deep(.btn-next:disabled) {
  background: var(--theme-background-mute) !important;
  border-color: var(--theme-border-lighter) !important;
  color: var(--theme-text-placeholder) !important;
  cursor: not-allowed;
}



/* 响应式设计 */
@media (max-width: 1200px) {
  .chinese-anime-page {
    padding: 15px;
  }

  .anime-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); /* 自适应列，避免固定宽度平板溢出 */
    gap: 24px;
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .chinese-anime-page {
    padding: 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .anime-grid {
    grid-template-columns: repeat(2, 1fr); /* 移动端2列自适应，避免 250px 撑爆视口 */
    gap: 12px;
    padding: 8px;
  }

  .anime-poster {
    height: 200px;
  }

  /* 移动端分页器样式调整 */
  .custom-pagination {
    flex-direction: column;
    gap: 15px;
  }
}
</style>
