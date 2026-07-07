<!-- 外部动漫浏览页面 - 展示豪华资源的动漫数据 -->
<template>
  <div class="external-anime-page">


    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-container">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索动漫名称..."
          size="large"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch" :loading="isSearching">
              搜索
            </el-button>
          </template>
        </el-input>

        <!-- 刷新按钮 -->
        <el-button
          type="primary"
          size="large"
          @click="refreshData"
          :loading="isLoading"
          class="refresh-button"
        >
          刷新数据
        </el-button>
      </div>
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
        description="暂无动漫数据"
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
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, VideoPlay, Clock } from '@element-plus/icons-vue'
import { externalAPI } from '@/services/api'
// 导入动漫筛选工具
import { filterAnimeResponse } from '@/utils/animeFilter'

const router = useRouter()

// 响应式数据
const isLoading = ref(false)
const isSearching = ref(false)
const searchKeyword = ref('')
// 移除了分类选择，默认显示日本动漫
const defaultCategory = '日本动漫'
const currentPage = ref(1)
const animeData = ref(null)
const animeList = ref([])


// 移除了分类选择功能，现在默认显示日本动漫

// 计算属性
const isSearchMode = computed(() => !!searchKeyword.value.trim())

// 获取动漫列表
const fetchAnimeList = async (page = 1, keyword = null) => {
  try {
    isLoading.value = true
    console.log('📡 获取动漫列表:', { category: defaultCategory, page, keyword })

    let response
    if (keyword) {
      // 搜索模式
      console.log('📡 进入搜索模式，调用searchAnime API')
      response = await externalAPI.searchAnime(keyword, page)
      console.log('📡 searchAnime API响应:', response)
    } else {
      // 分类浏览模式
      console.log('📡 进入分类浏览模式，调用getAnimeList API')
      response = await externalAPI.getAnimeList(defaultCategory, page)
      console.log('📡 getAnimeList API响应:', response)
    }

    if (response && response.code === 1) {
      // 应用动漫内容筛选
      const filteredResponse = filterAnimeResponse(response)
      console.log('📡 筛选后响应:', filteredResponse)

      animeData.value = filteredResponse
      animeList.value = filteredResponse.list || []
      // 修复：使用list.length获取实际动漫数量，因为API返回的数据中没有total字段
      const animeCount = filteredResponse.list ? filteredResponse.list.length : 0
      console.log('📡 获取动漫数据成功，筛选后:', animeCount, '部动漫')
      console.log('📡 筛选后动漫列表:', animeList.value)

      // 设置分页器中文文本
      setPaginationChineseText()

      if (animeList.value.length === 0) {
        ElMessage.info('未找到相关动漫内容（已过滤无关内容）')
      }
    } else {
      console.error('📡 API响应异常:', response)
      throw new Error(response?.msg || '获取数据失败')
    }

  } catch (error) {
    console.error('📡 获取动漫列表失败:', error)
    ElMessage.error('获取动漫数据失败: ' + error.message)
    animeList.value = []
  } finally {
    isLoading.value = false
  }
}

// 处理搜索
const handleSearch = async () => {
  console.log('🔍 开始搜索，关键词:', searchKeyword.value)

  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    isSearching.value = true
    currentPage.value = 1
    console.log('🔍 调用fetchAnimeList，关键词:', searchKeyword.value.trim())
    await fetchAnimeList(1, searchKeyword.value.trim())
    console.log('🔍 搜索完成')
  } catch (error) {
    console.error('🔍 搜索过程中出错:', error)
    ElMessage.error('搜索失败: ' + error.message)
  } finally {
    isSearching.value = false
  }
}


// 移除了分类变化处理函数

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  const keyword = isSearchMode.value ? searchKeyword.value.trim() : null
  fetchAnimeList(page, keyword)

  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })

  // 重新设置分页器中文文本
  setPaginationChineseText()
}

// 移除了跳转到指定页面的功能

// 刷新数据
const refreshData = () => {
  const keyword = isSearchMode.value ? searchKeyword.value.trim() : null
  fetchAnimeList(currentPage.value, keyword)
}

// 查看动漫详情
const viewAnimeDetail = (anime) => {
  console.log('查看动漫详情:', anime.vod_name)
  // 跳转到动漫详情页面
  router.push({
    name: 'NewAnimeDetail',
    params: { id: anime.vod_id }
  })
}

// 解析播放源
const parsePlaySources = (vodPlayFrom) => {
  if (!vodPlayFrom) return []
  return vodPlayFrom.split(',').map(source => source.trim())
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

// 动态添加选择框下拉面板样式
const addSelectDropdownStyles = () => {
  // 创建样式元素
  const styleId = 'external-anime-select-styles'
  let styleElement = document.getElementById(styleId)

  if (!styleElement) {
    styleElement = document.createElement('style')
    styleElement.id = styleId
    document.head.appendChild(styleElement)
  }

  // 移除了主题选择框相关的DOM操作代码

  // 移除了主题选择框相关的CSS规则
}

// 组件挂载时获取数据并添加样式
onMounted(() => {
  fetchAnimeList(1)
  addSelectDropdownStyles() // 添加选择框下拉面板样式

  // 监听主题变化，重新应用样式
  const observer = new MutationObserver(() => {
    addSelectDropdownStyles()
  })

  observer.observe(document.body, {
    attributes: true,
    attributeFilter: ['class']
  })

  // 监听选择框的变化，确保样式始终应用
  const selectObserver = new MutationObserver(() => {
    addSelectDropdownStyles()
  })

  // 观察整个页面的变化
  selectObserver.observe(document.body, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ['class', 'style']
  })

  // 组件卸载时清理观察器
  onUnmounted(() => {
    observer.disconnect()
    selectObserver.disconnect()
  })
})
</script>

<style scoped>
.external-anime-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--theme-background); /* 使用主题背景色 */
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 2.5rem;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  margin-bottom: 10px;
}

.page-header p {
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  font-size: 1.1rem;
}

.search-section {
  margin-bottom: 30px;
  padding: 20px;
  background: var(--theme-background-soft); /* 使用主题背景色 */
  border-radius: 12px;
  border: 1px solid var(--theme-border); /* 添加主题边框 */
  box-shadow: var(--theme-shadow-light); /* 添加阴影效果 */
}

.search-container {
  display: flex;
  align-items: center;
  gap: 15px;
  max-width: 800px;
  margin: 0 auto;
}

.search-input {
  flex: 1;
}

.refresh-button {
  flex-shrink: 0;
}

/* 在线动漫页面搜索框暗色主题样式 */
.search-section :deep(.el-input__wrapper) {
  background: var(--theme-background) !important; /* 使用主题背景色 */
  border: 1px solid var(--theme-border) !important; /* 使用主题边框色 */
  box-shadow: none !important;
  transition: all 0.3s ease;
}

.search-section :deep(.el-input__inner) {
  color: var(--theme-text-primary) !important; /* 使用主题文字颜色 */
  background: transparent !important;
}

.search-section :deep(.el-input__inner::placeholder) {
  color: var(--theme-text-placeholder) !important; /* 使用主题占位符颜色 */
}

/* 在线动漫页面搜索框聚焦状态 */
.search-section :deep(.el-input__wrapper:focus-within) {
  border-color: var(--theme-primary) !important; /* 聚焦时使用主题主色 */
  box-shadow: 0 0 0 2px var(--theme-primary-light) !important; /* 主题色光晕 */
}

/* 暗色主题下的在线动漫页面搜索框聚焦效果 */
.theme-dark .search-section :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px var(--theme-primary-light) !important; /* 主题色光晕 */
}

/* 修复搜索框和按钮连接处的边框 */
.search-section :deep(.el-input-group--append .el-input__wrapper) {
  border-right: none !important; /* 移除输入框右边框，避免双重边框 */
}

.search-section :deep(.el-input-group__append) {
  border-left: 1px solid var(--theme-primary) !important; /* 确保按钮左边框与主题一致 */
}

/* 搜索框前缀图标样式 */
.search-section :deep(.el-input__prefix) {
  color: var(--theme-text-secondary) !important;
}

/* 搜索框后缀图标样式（清除按钮等） */
.search-section :deep(.el-input__suffix) {
  color: var(--theme-text-secondary) !important;
}

.search-section :deep(.el-input__suffix .el-input__clear) {
  color: var(--theme-text-secondary) !important;
  transition: color 0.3s ease;
}

.search-section :deep(.el-input__suffix .el-input__clear:hover) {
  color: var(--theme-text-primary) !important;
}

/* 搜索框清除图标的圆形背景 */
.search-section :deep(.el-input__clear) {
  background: var(--theme-background-soft) !important;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.search-section :deep(.el-input__clear:hover) {
  background: var(--theme-border) !important;
}

/* 搜索按钮样式 - 修复黑色边框问题 */
.search-section :deep(.el-input-group__append) {
  background: var(--theme-primary) !important;
  border: 1px solid var(--theme-primary) !important;
  border-left: none !important;
  box-shadow: none !important; /* 移除可能的阴影 */
}

.search-section :deep(.el-input-group__append .el-button) {
  background: transparent !important;
  border: none !important;
  color: white !important;
  font-weight: 600;
  box-shadow: none !important; /* 移除按钮阴影 */
  outline: none !important; /* 移除聚焦时的轮廓 */
}

.search-section :deep(.el-input-group__append .el-button:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  border: none !important; /* 确保悬停时也没有边框 */
  box-shadow: none !important; /* 确保悬停时也没有阴影 */
}

.search-section :deep(.el-input-group__append .el-button:focus) {
  background: rgba(255, 255, 255, 0.1) !important;
  border: none !important; /* 确保聚焦时也没有边框 */
  box-shadow: none !important; /* 确保聚焦时也没有阴影 */
  outline: none !important; /* 移除聚焦轮廓 */
}

.search-section :deep(.el-input-group__append .el-button:active) {
  background: rgba(255, 255, 255, 0.2) !important;
  border: none !important; /* 确保按下时也没有边框 */
  box-shadow: none !important; /* 确保按下时也没有阴影 */
}

/* 移除了主题选择框相关样式 */



/* 刷新按钮样式 */
.search-section :deep(.el-button--primary) {
  background: var(--theme-primary) !important;
  border: 1px solid var(--theme-primary) !important;
  color: white !important;
  font-weight: 600;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.search-section :deep(.el-button--primary:hover) {
  background: var(--theme-primary-dark) !important;
  border-color: var(--theme-primary-dark) !important;
  transform: translateY(-2px);
  box-shadow: var(--theme-shadow) !important;
}

.search-section :deep(.el-button--primary:active) {
  transform: translateY(0);
}

/* 加载状态下的按钮样式 */
.search-section :deep(.el-button--primary.is-loading) {
  background: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    flex-direction: column;
    gap: 15px;
  }

  .search-input {
    width: 100%;
  }

  .refresh-button {
    width: 100%;
  }

  .search-section {
    padding: 15px;
  }
}



.stats-section {
  margin-bottom: 30px;
  padding: 20px;
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 8px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
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
  background: linear-gradient(135deg, var(--theme-background-soft), var(--theme-background-mute));
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

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 4rem;
  color: white;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

/* 年份显示样式 - 使用主题色 */
.anime-year {
  color: white; /* 白色文字确保在主题色背景上清晰可见 */
  font-size: 14px;
  font-weight: 600;
  background: var(--theme-primary); /* 使用主题主色作为背景 */
  padding: 6px 12px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  box-shadow: var(--theme-shadow-light); /* 添加主题阴影 */
}

/* 集数标签样式 */
.episode-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--theme-primary); /* 使用主题主色 */
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  box-shadow: var(--theme-shadow-light);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 暗色主题下的年份标签 */
.theme-dark .anime-year {
  background: var(--theme-primary); /* 暗色主题也使用主题主色 */
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 暗色主题下的集数标签 */
.theme-dark .episode-badge {
  background: var(--theme-primary);
  border: 1px solid rgba(0, 0, 0, 0.2);
}

/* 暗色主题下的年份标签 */
.theme-dark .anime-year {
  background: var(--theme-primary); /* 暗色主题也使用主题主色 */
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
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
  /* 改为单行显示，超出省略号 */
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

.anime-sources .el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

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

/* 跳转相关样式 */
.pagination-section :deep(.el-pagination__jump) {
  color: var(--theme-text-primary) !important;
  margin: 0 8px;
}

.pagination-section :deep(.el-pagination__classifier) {
  color: var(--theme-text-primary) !important;
}

/* 跳转输入框样式 - 使用更强的选择器 */
.pagination-section :deep(.el-pagination__editor.el-input) {
  width: 50px;
}

.pagination-section :deep(.el-pagination__editor.el-input .el-input__wrapper) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  border-radius: 6px;
  transition: all 0.3s ease;
  box-shadow: none !important;
}

.pagination-section :deep(.el-pagination__editor.el-input .el-input__inner) {
  color: var(--theme-text-primary) !important;
  background: transparent !important;
  text-align: center;
}

.pagination-section :deep(.el-pagination__editor.el-input .el-input__wrapper:hover) {
  border-color: var(--theme-primary) !important;
}

.pagination-section :deep(.el-pagination__editor.el-input .el-input__wrapper:focus-within) {
  border-color: var(--theme-primary) !important;
  box-shadow: 0 0 0 2px rgba(78, 171, 230, 0.2) !important;
}

/* 暗色主题下的跳转输入框聚焦效果 */
.theme-dark .pagination-section :deep(.el-pagination__editor.el-input .el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(234, 122, 153, 0.3) !important;
}

/* 总数显示样式 */
.pagination-section :deep(.el-pagination__total) {
  color: var(--theme-text-secondary) !important;
  font-weight: 500;
  margin: 0 8px;
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

.pagination-jumper {
  display: flex;
  align-items: center;
  color: var(--theme-text-primary);
}

/* 跳转输入框样式 */
.pagination-jumper :deep(.el-input__wrapper) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  box-shadow: none !important;
  border-radius: 4px;
}

.pagination-jumper :deep(.el-input__inner) {
  color: var(--theme-text-primary) !important;
  background: transparent !important;
  text-align: center;
  font-size: 13px;
}

.pagination-jumper :deep(.el-input__wrapper:hover) {
  border-color: var(--theme-primary) !important;
}

.pagination-jumper :deep(.el-input__wrapper:focus-within) {
  border-color: var(--theme-primary) !important;
  box-shadow: 0 0 0 2px rgba(78, 171, 230, 0.2) !important;
}

/* 暗色主题下的跳转输入框聚焦效果 */
.theme-dark .pagination-jumper :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(234, 122, 153, 0.3) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .custom-pagination {
    flex-direction: column;
    gap: 15px;
  }

  .pagination-extra {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .external-anime-page {
    padding: 15px;
  }

  .anime-grid {
    grid-template-columns: repeat(3, 280px); /* 中等屏幕3列，宽度280px */
    gap: 30px; /* 中等屏幕间距30px */
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .external-anime-page {
    padding: 10px;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .anime-grid {
    grid-template-columns: repeat(2, 250px); /* 移动端2列，宽度250px */
    gap: 25px; /* 移动端间距25px */
    padding: 15px;
  }

  .anime-poster {
    height: 250px;
  }
}
</style>
