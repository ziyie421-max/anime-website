<!-- 搜索结果页面 - 专门展示搜索结果的页面 -->
<template>
  <div class="search-results-page">


    <!-- 搜索框区域 -->
    <div class="search-section">
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索动漫名称..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch" :loading="isSearching" type="primary" class="search-button">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
    </div>



    <!-- 动漫列表 -->
    <div class="anime-list-section" v-loading="isLoading">
      <div class="anime-grid">
        <div
          v-for="anime in animeList"
          :key="anime.vod_id"
          class="anime-card"
          @click="viewAnimeDetail(anime)"
        >
          <!-- 动漫海报 -->
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
        v-if="!isLoading && animeList.length === 0 && searchKeyword"
        description="未找到相关动漫"
        :image-size="200"
      />

      <!-- 未搜索状态 -->
      <el-empty
        v-if="!isLoading && !searchKeyword"
        description="请输入关键词开始搜索"
        :image-size="200"
      />
    </div>

    <!-- 分页器 - 与在线动漫页面保持一致的样式 -->
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

        <!-- 总数显示 -->
        <div class="pagination-extra">
          <span class="pagination-total">共 {{ animeData.pagecount }} 页</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 导入Vue相关功能
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 导入Element Plus组件和消息提示
import { ElMessage } from 'element-plus'
// 导入Element Plus图标
import { Search, VideoPlay, Clock } from '@element-plus/icons-vue'
// 导入API服务
import { externalAPI } from '@/services/api'
// 导入动漫筛选工具
import { filterAnimeResponse } from '@/utils/animeFilter'

const router = useRouter()
const route = useRoute()

// 响应式数据
const isLoading = ref(false)
const isSearching = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(24) // 每页期望显示的数量
const targetPageSize = ref(20) // 目标每页显示数量（用于智能填充）
const totalResults = ref(0)
const animeList = ref([])
const animeData = ref(null) // 存储API响应数据用于分页
const allFetchedData = ref([]) // 存储所有已获取的数据

// 从URL参数初始化搜索关键词
const initFromRoute = () => {
  const searchParam = route.query.search
  if (searchParam) {
    searchKeyword.value = searchParam
    console.log('📡 从URL参数获取搜索关键词:', searchParam)
  }
}

// 执行搜索（智能填充版本）
const performSearch = async (page = 1, keyword = null) => {
  const searchTerm = keyword || searchKeyword.value.trim()
  if (!searchTerm) {
    animeList.value = []
    totalResults.value = 0
    animeData.value = null
    allFetchedData.value = []
    return
  }

  try {
    isLoading.value = true
    console.log('🔍 执行搜索，关键词:', searchTerm, '页码:', page)

    // 如果是新搜索（第一页），清空之前的数据
    if (page === 1) {
      allFetchedData.value = []
    }

    // 获取当前页面应该显示的数据
    await fetchDataForPage(searchTerm, page)

  } catch (error) {
    console.error('🔍 搜索失败:', error)
    ElMessage.error('搜索失败: ' + error.message)
    animeList.value = []
    totalResults.value = 0
    animeData.value = null
    allFetchedData.value = []
  } finally {
    isLoading.value = false
  }
}

// 获取指定页面的数据（智能填充）
const fetchDataForPage = async (searchTerm, targetPage) => {
  const startIndex = (targetPage - 1) * targetPageSize.value
  const endIndex = startIndex + targetPageSize.value

  console.log('🔍 需要显示数据范围:', startIndex, '-', endIndex)

  // 检查是否已有足够的数据
  while (allFetchedData.value.length < endIndex) {
    const apiPage = Math.floor(allFetchedData.value.length / 20) + 1 // API每页约20个
    console.log('🔍 获取API第', apiPage, '页数据')

    const response = await externalAPI.searchAnime(searchTerm, apiPage)
    console.log('🔍 API响应:', response)

    if (response && response.code === 1 && response.list && response.list.length > 0) {
      // 应用筛选
      const filteredResponse = filterAnimeResponse(response)
      const newData = filteredResponse.list || []

      console.log('🔍 API第', apiPage, '页筛选后得到', newData.length, '个结果')

      // 添加到总数据中
      allFetchedData.value.push(...newData)

      // 更新分页信息（使用第一次响应的信息）
      if (!animeData.value) {
        animeData.value = response
      }

      // 如果API没有更多数据了，停止获取
      if (apiPage >= response.pagecount) {
        console.log('🔍 已获取所有API页面数据')
        break
      }

      // 如果新获取的数据为空，可能是筛选过于严格，尝试下一页
      if (newData.length === 0) {
        console.log('🔍 当前页筛选后无数据，尝试下一页')
        continue
      }
    } else {
      console.log('🔍 API无更多数据')
      break
    }
  }

  // 提取当前页应该显示的数据
  const pageData = allFetchedData.value.slice(startIndex, endIndex)
  animeList.value = pageData

  // 计算总页数（基于已获取的数据）
  const totalPages = Math.ceil(allFetchedData.value.length / targetPageSize.value)
  if (animeData.value) {
    animeData.value.pagecount = Math.max(totalPages, targetPage)
  }

  console.log('🔍 当前页显示', pageData.length, '个结果，总共', allFetchedData.value.length, '个结果，总页数:', totalPages)

  if (animeList.value.length === 0 && allFetchedData.value.length === 0) {
    ElMessage.info('未找到相关动漫内容（已过滤无关内容）')
  }
}

// 处理搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    isSearching.value = true
    currentPage.value = 1
    
    // 更新URL参数
    router.push({
      path: '/search',
      query: { search: searchKeyword.value.trim() }
    })
    
    await performSearch(1, searchKeyword.value.trim())
  } catch (error) {
    console.error('🔍 搜索过程中出错:', error)
    ElMessage.error('搜索失败: ' + error.message)
  } finally {
    isSearching.value = false
  }
}

// 处理页码变化
const handlePageChange = async (page) => {
  console.log('🔄 页码变化:', currentPage.value, '->', page)
  currentPage.value = page

  // 检查是否需要获取更多数据
  const startIndex = (page - 1) * targetPageSize.value
  const endIndex = startIndex + targetPageSize.value

  if (allFetchedData.value.length >= endIndex) {
    // 已有足够数据，直接显示
    animeList.value = allFetchedData.value.slice(startIndex, endIndex)
    console.log('🔄 使用已缓存数据，显示', animeList.value.length, '个结果')
  } else {
    // 需要获取更多数据
    await performSearch(page)
  }

  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}



// 处理页面大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  performSearch(1)
}

// 查看动漫详情
const viewAnimeDetail = (anime) => {
  console.log('查看动漫详情:', anime.vod_name)
  router.push({
    name: 'NewAnimeDetail',
    params: { id: anime.vod_id }
  })
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}

// 解析播放源（暂时未使用，保留备用）
// const parsePlaySources = (vodPlayFrom) => {
//   if (!vodPlayFrom) return []
//   return vodPlayFrom.split(',').map(source => source.trim())
// }

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.style.display = 'none'
  const placeholder = event.target.parentNode.querySelector('.placeholder-cover')
  if (placeholder) {
    placeholder.style.display = 'flex'
  }
}

// 监听路由参数变化
watch(() => route.query.search, (newSearch) => {
  if (newSearch && newSearch !== searchKeyword.value) {
    searchKeyword.value = newSearch
    console.log('📡 路由参数变化，更新搜索关键词:', newSearch)
    currentPage.value = 1
    performSearch(1, newSearch)
  }
}, { immediate: false })

// 组件挂载时初始化
onMounted(() => {
  initFromRoute()
  if (searchKeyword.value.trim()) {
    console.log('📡 组件挂载时发现搜索关键词，自动执行搜索:', searchKeyword.value)
    performSearch(1, searchKeyword.value)
  }
})
</script>

<style scoped>
.search-results-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
}



/* 搜索框区域样式 - 美化并支持暗色主题 */
.search-section {
  margin-top: 20px;
  margin-bottom: 50px;
  padding: 0 20px;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
}

.search-input {
  width: 100%;
  border-radius: 25px;
  overflow: hidden;
  box-shadow: var(--theme-shadow-light);
  transition: all 0.3s ease;
  position: relative;
}

.search-input:hover {
  box-shadow: var(--theme-shadow);
  transform: translateY(-2px);
}

/* 确保搜索框组件内部元素正确对齐 */
.search-input :deep(.el-input-group) {
  display: flex;
  align-items: stretch;
}

.search-input :deep(.el-input-group__prepend),
.search-input :deep(.el-input-group__append) {
  display: flex;
  align-items: center;
}

/* 搜索框整体样式 */
.search-input :deep(.el-input-group) {
  border-radius: 25px;
  overflow: hidden;
  box-shadow: var(--theme-shadow-light);
}

/* 搜索框输入区域 */
.search-input :deep(.el-input__wrapper) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  border-right: none !important;
  border-radius: 25px 0 0 25px !important;
  box-shadow: none !important;
}

.search-input :deep(.el-input__inner) {
  color: var(--theme-text-primary) !important;
  background: transparent !important;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: var(--theme-primary) !important;
  border-right: none !important;
}

.search-input :deep(.el-input__wrapper:focus-within) {
  border-color: var(--theme-primary) !important;
  border-right: none !important;
}

/* 搜索框前缀图标区域 */
.search-input :deep(.el-input-group__prepend) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  border-right: none !important;
  border-radius: 25px 0 0 25px !important;
  padding: 0 15px !important;
}

/* 搜索框后缀按钮区域 */
.search-input :deep(.el-input-group__append) {
  background: var(--theme-primary) !important;
  border: 1px solid var(--theme-primary) !important;
  border-left: none !important;
  border-radius: 0 25px 25px 0 !important;
  padding: 0 !important;
  box-shadow: none !important;
}

/* 搜索图标样式 */
.search-icon {
  color: var(--theme-text-secondary);
  font-size: 18px;
}

/* 前缀图标容器样式 */
.search-input :deep(.el-input__prefix) {
  display: flex;
  align-items: center;
  padding-left: 15px;
}

/* 搜索按钮样式 */
.search-button {
  border: none !important;
  border-radius: 0 25px 25px 0 !important;
  padding: 0 30px !important;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  background: var(--theme-primary) !important;
  color: white !important;
  box-shadow: none !important;
  height: 100% !important;
  margin: 0 !important;
}

.search-button:hover {
  background: var(--theme-primary-dark) !important;
  transform: translateY(-1px);
}

.search-button:focus {
  background: var(--theme-primary) !important;
  box-shadow: none !important;
  outline: none !important;
}

.search-button:active {
  background: var(--theme-primary-dark) !important;
  transform: translateY(0);
}

.search-stats {
  margin-bottom: 30px;
}

.anime-list-section {
  margin-bottom: 30px;
}

/* 动漫网格布局 - 与其他页面保持一致 */
.anime-grid {
  display: grid;
  grid-template-columns: repeat(4, 300px); /* 4列，每列宽度300px */
  gap: 40px; /* 调整间距为40px，适合4列布局 */
  padding: 20px;
  justify-content: center; /* 居中显示 */
}

/* 动漫卡片样式 - 与其他页面完全一致 */
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-background-mute);
  color: var(--theme-text-secondary);
  font-size: 48px;
}

.anime-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(var(--theme-primary-rgb), 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.anime-card:hover .anime-overlay {
  opacity: 1;
}

.play-icon {
  font-size: 48px;
  color: white;
  margin-bottom: 20px;
}

.anime-year {
  position: absolute;
  bottom: 20px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.episode-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--theme-primary);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.placeholder-cover {
  width: 100%;
  height: 100%;
  background: var(--theme-gradient); /* 使用主题渐变色 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

.placeholder-cover .el-icon {
  margin-bottom: 10px;
  opacity: 0.7;
}

.anime-info {
  padding: 20px;
  background: var(--theme-background); /* 使用主题背景色 */
}

.anime-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  line-height: 1.4;
  /* 单行显示，超出省略号 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.anime-meta {
  color: var(--theme-text-secondary);
  font-size: 0.9rem;
  line-height: 1.4;
}

.anime-meta span {
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .anime-grid {
    grid-template-columns: repeat(3, 280px); /* 中等屏幕3列，宽度280px */
    gap: 30px; /* 中等屏幕间距30px */
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .search-results-page {
    padding: 10px;
  }

  /* 移动端 2 列自适应宽度，避免固定 250px 撑出横向溢出 */
  .anime-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 6px;
  }

  .anime-poster {
    height: 200px;
  }

  .anime-info {
    padding: 12px;
  }

  .anime-title {
    font-size: 0.92rem;
  }

  /* 移动端分页器样式调整 */
  .custom-pagination {
    flex-direction: column;
    gap: 12px;
  }

  .search-section {
    margin-top: 10px;
    margin-bottom: 24px;
    padding: 0 4px;
  }

  .search-button {
    padding: 0 18px !important;
  }
}

/* 分页器样式 - 与在线动漫页面完全一致 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: var(--theme-background-soft); /* 添加背景色 */
  border-radius: 8px; /* 添加圆角 */
  border: 1px solid var(--theme-border); /* 添加边框 */
  margin-top: 20px; /* 添加上边距 */
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

/* 自定义分页器布局 */
.custom-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.pagination-extra {
  display: flex;
  align-items: center;
  gap: 15px;
  color: var(--theme-text-primary);
  font-size: 14px;
}

.pagination-total {
  color: var(--theme-text-secondary);
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-results-page {
    padding: 15px;
  }

  .anime-card-col {
    margin-bottom: 15px;
  }
}

/* 超大屏幕：每行6个 (xl: 4 span = 24/4 = 6个) */
@media (min-width: 1920px) {
  .anime-card {
    max-width: 280px;
  }
}

/* 大屏幕：每行4个 (lg: 6 span = 24/6 = 4个) */
@media (min-width: 1200px) and (max-width: 1919px) {
  .anime-card {
    max-width: 300px;
  }
}

/* 中等屏幕：每行4个 (md: 6 span = 24/6 = 4个) */
@media (min-width: 992px) and (max-width: 1199px) {
  .anime-card {
    max-width: 280px;
  }
}

/* 小屏幕：每行3个 (sm: 8 span = 24/8 = 3个) */
@media (min-width: 768px) and (max-width: 991px) {
  .anime-card {
    max-width: 250px;
  }
}

/* 超小屏幕：每行2个 (xs: 12 span = 24/12 = 2个) */
@media (max-width: 767px) {
  .anime-card {
    max-width: 200px;
  }
}

/* 分页器响应式设计 */
@media (max-width: 768px) {
  .custom-pagination {
    flex-direction: column;
    gap: 15px;
  }

  .pagination-extra {
    text-align: center;
  }
}

@media (max-width: 768px) {
  .search-results-page {
    padding: 10px;
  }



  .anime-cover {
    height: 200px;
  }

  .anime-info {
    padding: 15px;
  }
}
</style>
