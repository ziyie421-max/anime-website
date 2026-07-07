<!-- 新的动漫详情页面 - 根据设计图制作 -->
<template>
  <!-- 加载状态 -->
  <div v-if="loading || !animeData" class="loading-container">
    <el-skeleton :rows="8" animated />
  </div>

  <!-- 详情内容 -->
  <div class="anime-detail-page" v-else>
    <!-- 顶部导航栏 -->
    <div class="detail-header">
      <div class="breadcrumb">
        <span @click="$router.push('/')" class="breadcrumb-item">首页</span>
        <span class="separator">></span>
        <span @click="$router.push('/external')" class="breadcrumb-item">动漫列表</span>
        <span class="separator">></span>
        <span class="current">{{ animeData.title }}</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="detail-content">
      <!-- 左侧海报 -->
      <div class="poster-section">
        <div class="poster-container">
          <img :src="animeData.poster" :alt="animeData.title" class="anime-poster" />
        </div>
      </div>

      <!-- 右侧信息区域 -->
      <div class="info-section">
        <!-- 标题和基本信息 -->
        <div class="title-section">
          <h1 class="anime-title">{{ animeData.title }}</h1>
          <p class="anime-subtitle" v-if="animeData.originalTitle">{{ animeData.originalTitle }}</p>

          <!-- 收藏按钮 -->
          <div class="action-buttons">
            <el-button
              :type="isFavorited ? 'danger' : 'default'"
              :icon="isFavorited ? StarFilled : Star"
              @click="toggleFavorite"
              :loading="favoriteLoading"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button type="primary" @click="startWatch">
              <el-icon><VideoPlay /></el-icon> 开始观看
            </el-button>
          </div>
        </div>

        <!-- 详细信息列表 -->
        <div class="details-grid">
          <div class="detail-row">
            <span class="detail-label">类型</span>
            <span class="detail-value">{{ animeData.categories?.join('、') || '未知' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">制作</span>
            <span class="detail-value">{{ animeData.studio || '未知' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">导演</span>
            <span class="detail-value">{{ animeData.director || '未知' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上映</span>
            <span class="detail-value">{{ animeData.year || '未知' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">地区</span>
            <span class="detail-value">{{ animeData.region || '日本' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">年份</span>
            <span class="detail-value">{{ animeData.year || '2025' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态</span>
            <span class="detail-value">{{ getStatusText(animeData.status) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">更新</span>
            <span class="detail-value">{{ formatUpdateTime(animeData.updatedAt) }}</span>
          </div>
        </div>

        <!-- 简介 -->
        <div class="description-section">
          <h3 class="section-title">简介</h3>
          <p class="description-text">{{ cleanHtmlTags(animeData.description) || '暂无简介' }}</p>
        </div>
      </div>
    </div>

    <!-- 剧集选择区域 -->
    <div class="episodes-section">
      <div class="episodes-header">
        <h3 class="episodes-title">选集</h3>
        <!-- 暂时隐藏播放源选择按钮 -->
        <!-- <div class="episodes-nav">
          <span class="nav-item active">Ourma1</span>
        </div> -->
      </div>

      <div class="episodes-grid">
        <div
          v-for="episode in episodes"
          :key="episode.id"
          class="episode-item"
          @click="watchEpisode(episode)"
        >
          第{{ String(episode.episodeNumber || episode.number).padStart(2, '0') }}话
        </div>

        <!-- 如果没有剧集数据，显示默认的 -->
        <div v-if="episodes.length === 0" class="episode-item" @click="startWatch">
          第01话
        </div>
        <div v-if="episodes.length === 0" class="episode-item">
          第02话
        </div>
      </div>
    </div>

    <!-- 剧情简介区域 -->
    <div class="plot-section">
      <h3 class="plot-title">剧情简介：</h3>
      <p class="plot-text">{{ cleanHtmlTags(animeData.description) || '暂无详细剧情简介' }}</p>
    </div>

    <!-- 评分和评论区域 -->
    <div class="interaction-section">
      <!-- 评分面板 -->
      <div class="rating-wrapper">
        <RatingPanel
          v-if="animeData.isExternal"
          :external-anime-id="String(animeData.id)"
          :external-anime-title="animeData.title"
        />
      </div>

      <!-- 评论区 -->
      <div class="comment-wrapper">
        <CommentSection
          v-if="animeData.isExternal"
          :external-anime-id="String(animeData.id)"
          :external-anime-title="animeData.title"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, VideoPlay } from '@element-plus/icons-vue'
import { animeAPI, episodeAPI, externalAPI, handleApiError } from '@/services/api'
import { addFavorite, removeFavorite, checkFavorited } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
// 导入评论和评分组件
import CommentSection from '@/components/CommentSection.vue'
import RatingPanel from '@/components/RatingPanel.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const animeData = ref(null)
const episodes = ref([])
const loading = ref(false)
const isFavorited = ref(false)        // 是否已收藏
const favoriteLoading = ref(false)    // 收藏操作加载状态

// 获取动漫详情数据
const fetchAnimeDetail = async () => {
  const animeId = route.params.id

  try {
    loading.value = true
    console.log('开始获取动漫详情，ID:', animeId)

    // 先尝试从外部API获取数据
    try {
      const externalResponse = await externalAPI.getAnimeDetail(animeId)
      if (externalResponse && externalResponse.code === 1 && externalResponse.list && externalResponse.list.length > 0) {
        const externalAnime = externalResponse.list[0]
        console.log('获取到外部动漫数据:', externalAnime)

        // 处理外部动漫数据
        animeData.value = {
          id: animeId,
          title: externalAnime.vod_name,
          originalTitle: externalAnime.vod_sub || '',
          description: externalAnime.vod_content || externalAnime.vod_blurb || '暂无简介',
          poster: externalAnime.vod_pic,
          year: externalAnime.vod_year || new Date().getFullYear(),
          season: '未知',
          status: 'ongoing',
          categories: externalAnime.type_name ? [externalAnime.type_name] : ['动漫'],
          studio: externalAnime.vod_director || '未知',
          director: externalAnime.vod_actor || '未知',
          region: externalAnime.vod_area || '日本',
          updatedAt: externalAnime.vod_time || new Date().toISOString(),
          isExternal: true // 标记为外部动漫
        }

        // 解析剧集信息
        if (externalAnime.vod_play_url && externalAnime.vod_play_from) {
          const playUrls = externalAnime.vod_play_url.split('$$$')
          const playSources = externalAnime.vod_play_from.split(',')

          if (playUrls.length > 0 && playUrls[0]) {
            const episodeList = playUrls[0].split('#')
            episodes.value = episodeList.map((episode, index) => {
              const [name, url] = episode.split('$')
              return {
                id: index + 1,
                episodeNumber: index + 1,
                number: index + 1,
                title: name || `第${index + 1}集`,
                url: url
              }
            }).filter(ep => ep.url) // 过滤掉没有URL的剧集
          }
        }

        return // 成功获取外部数据，直接返回
      }
    } catch (externalError) {
      console.warn('外部API获取失败，尝试本地API:', externalError)
    }

    // 如果外部API失败，尝试本地API
    const animeResponse = await animeAPI.getById(animeId)
    console.log('本地动漫详情响应:', animeResponse)

    // 处理本地动漫数据
    animeData.value = {
      ...animeResponse,
      categories: animeResponse.categories || [],
      season: getSeasonText(animeResponse.season),
      status: animeResponse.status ? animeResponse.status.toLowerCase() : 'unknown',
      isExternal: false // 标记为本地动漫
    }

    // 获取本地剧集数据
    try {
      const episodesResponse = await episodeAPI.getByAnimeId(animeId, true)
      if (episodesResponse && episodesResponse.length > 0) {
        episodes.value = episodesResponse.map(episode => ({
          ...episode,
          watched: false,
          thumbnail: episode.thumbnail || 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=200&fit=crop',
          duration: episode.duration || 1440
        }))
      }
    } catch (episodeError) {
      console.warn('获取本地剧集数据失败:', episodeError)
    }

  } catch (error) {
    console.error('获取动漫详情失败:', error)
    ElMessage.error('获取动漫详情失败，请稍后重试')

    // 使用备用数据
    animeData.value = {
      id: animeId,
      title: `动漫详情 ID:${animeId}`,
      originalTitle: '',
      description: '抱歉，无法加载动漫详情信息。请稍后重试或联系管理员。',
      poster: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=300&h=400&fit=crop',
      year: new Date().getFullYear(),
      season: '未知',
      status: 'unknown',
      categories: ['未知'],
      studio: '未知',
      director: '未知',
      region: '未知',
      updatedAt: new Date().toISOString(),
      isExternal: false
    }

    // 添加一些测试剧集数据
    episodes.value = [
      { id: 1, episodeNumber: 1, number: 1, title: '第01话' },
      { id: 2, episodeNumber: 2, number: 2, title: '第02话' }
    ]
  } finally {
    loading.value = false
  }
}

// 辅助函数
const getSeasonText = (season) => {
  const seasonMap = {
    'spring': '春季',
    'summer': '夏季',
    'autumn': '秋季',
    'winter': '冬季'
  }
  return seasonMap[season] || season || '未知'
}

const getStatusText = (status) => {
  const statusMap = {
    'ongoing': '连载中',
    'completed': '已完结',
    'upcoming': '即将播出',
    'unknown': '未知'
  }
  return statusMap[status] || '未知'
}

const formatUpdateTime = (time) => {
  if (!time) return '未知'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 清理HTML标签和实体编码的函数 - 简化版本
const cleanHtmlTags = (text) => {
  if (!text) return ''

  // 移除HTML标签
  let cleanText = text.replace(/<[^>]*>/g, '')

  // 直接替换常见的HTML实体编码
  cleanText = cleanText
    .replace(/&nbsp;/g, ' ')      // 空格
    .replace(/&amp;/g, '&')      // &符号
    .replace(/&lt;/g, '<')       // 小于号
    .replace(/&gt;/g, '>')       // 大于号
    .replace(/&quot;/g, '"')     // 双引号
    .replace(/&#39;/g, '\u0027')      // 单引号
    .replace(/&apos;/g, '\u0027')     // 单引号
    .replace(/&copy;/g, '©')     // 版权符号
    .replace(/&reg;/g, '®')      // 注册商标
    .replace(/&trade;/g, '™')    // 商标符号
    .replace(/&hellip;/g, '…')   // 省略号
    .replace(/&mdash;/g, '—')    // 长破折号
    .replace(/&ndash;/g, '–')    // 短破折号
    .replace(/&ldquo;/g, '"')    // 左双引号
    .replace(/&rdquo;/g, '"')    // 右双引号
    .replace(/&lsquo;/g, '\u2018')    // 左单引号
    .replace(/&rsquo;/g, '\u2019')    // 右单引号

  // 清理多余的空白字符
  cleanText = cleanText.replace(/\s+/g, ' ').trim()

  return cleanText
}

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    fetchAnimeDetail()
  }
}, { immediate: false })

// 开始观看 - 跳转到外部播放页面
const startWatch = () => {
  router.push({
    path: '/external/play',
    query: {
      id: animeData.value.id,
      title: animeData.value.title
    }
  })
}

// 观看指定剧集 - 跳转到外部播放页面
const watchEpisode = (episode) => {
  router.push({
    path: '/external/play',
    query: {
      id: animeData.value.id,
      title: animeData.value.title,
      episode: episode.number || episode.episodeNumber || 1
    }
  })
}

// 检查是否已收藏
const checkIsFavorited = async () => {
  // 检查 localStorage 是否有 token（不依赖 authStore.isLoggedIn）
  const hasToken = localStorage.getItem('accessToken')
  if (!hasToken) {
    isFavorited.value = false
    return
  }

  try {
    const animeId = route.params.id
    console.log('检查收藏状态 - 动漫ID:', animeId)
    const response = await checkFavorited(animeId)
    // 后端返回格式: { success: true, data: { isFavorited: true/false } }
    const data = response.data?.data || response.data
    isFavorited.value = data?.isFavorited === true
    console.log('检查收藏状态结果:', animeId, '已收藏:', isFavorited.value)
  } catch (error) {
    console.error('检查收藏状态失败:', error)
    isFavorited.value = false
  }
}

// 切换收藏状态
const toggleFavorite = async () => {
  // 检查 localStorage 是否有 token
  const hasToken = localStorage.getItem('accessToken')
  if (!hasToken) {
    ElMessage.warning('请先登录后再收藏')
    router.push('/login')
    return
  }

  try {
    favoriteLoading.value = true
    const animeId = route.params.id
    console.log('切换收藏状态 - 动漫ID:', animeId, '当前状态:', isFavorited.value)

    if (isFavorited.value) {
      // 取消收藏
      await removeFavorite(animeId)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      await addFavorite({
        externalAnimeId: String(animeId),  // 确保是字符串
        externalAnimeTitle: animeData.value.title,
        externalAnimeCover: animeData.value.poster,
        externalAnimeDesc: animeData.value.description?.substring(0, 200),
        sourceKey: 'lzzy',
        sourceName: '量子资源'
      })
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    favoriteLoading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAnimeDetail()
  checkIsFavorited()
})
</script>

<style scoped>
/* 页面整体样式 - 主题适配 */
.anime-detail-page {
  min-height: 100vh;
  background: var(--theme-background-soft); /* 使用主题背景色 */
  padding: 0;
}

.loading-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 8px;
  box-shadow: var(--theme-shadow-light); /* 使用主题阴影 */
}

/* 顶部导航栏 - 主题适配 */
.detail-header {
  background: var(--theme-background); /* 使用主题背景色 */
  padding: 15px 0;
  border-bottom: 1px solid var(--theme-border); /* 使用主题边框色 */
}

.breadcrumb {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  font-size: 14px;
  color: var(--theme-text-secondary); /* 使用主题文字颜色 */
}

.breadcrumb-item {
  color: var(--theme-primary); /* 使用主题主色 */
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb-item:hover {
  text-decoration: underline;
  color: var(--theme-primary-dark); /* 悬停时使用深色主题色 */
}

.separator {
  margin: 0 8px;
  color: var(--theme-text-placeholder); /* 使用主题占位符颜色 */
}

.current {
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
}

/* 主要内容区域 - 主题适配 */
.detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  gap: 40px;
  background: var(--theme-background); /* 使用主题背景色 */
  margin-top: 20px;
  border-radius: 8px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
}

/* 左侧海报区域 */
.poster-section {
  flex-shrink: 0;
}

.poster-container {
  width: 280px;
}

.anime-poster {
  width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  border: 1px solid var(--theme-border-light); /* 添加主题边框 */
}

/* 右侧信息区域 */
.info-section {
  flex: 1;
}

.title-section {
  margin-bottom: 30px;
}

.anime-title {
  font-size: 28px;
  font-weight: bold;
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
  margin: 0 0 10px 0;
}

.anime-subtitle {
  font-size: 16px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  margin: 0;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 15px;
}

.action-buttons .el-button {
  padding: 10px 20px;
}

/* 详细信息网格 - 主题适配 */
.details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px 40px;
  margin-bottom: 30px;
}

.detail-row {
  display: flex;
  align-items: center;
}

.detail-label {
  width: 60px;
  color: var(--theme-primary); /* 使用主题主色 */
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
  margin-left: 10px;
}

/* 简介区域 - 主题适配 */
.description-section {
  margin-top: 30px;
}

.section-title {
  font-size: 18px;
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
  margin: 0 0 15px 0;
  font-weight: 600;
}

.description-text {
  line-height: 1.8;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  margin: 0;
}

/* 剧集选择区域 - 主题适配 */
.episodes-section {
  max-width: 1200px;
  margin: 20px auto;
  padding: 30px 20px;
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 8px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
}

.episodes-header {
  display: flex;
  align-items: center;
  /* 暂时去掉 justify-content: space-between，因为隐藏了播放源按钮 */
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--theme-primary); /* 使用主题主色 */
}

.episodes-title {
  font-size: 20px;
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
  margin: 0;
  font-weight: 600;
}

.episodes-nav {
  display: flex;
  gap: 20px;
}

.nav-item {
  padding: 8px 16px;
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
}

.nav-item.active {
  background: var(--theme-primary); /* 使用主题主色 */
  color: white;
  border-color: var(--theme-primary); /* 使用主题主色边框 */
}

.nav-item:hover:not(.active) {
  background: var(--theme-background-mute); /* 使用主题静音背景色 */
  border-color: var(--theme-primary-light); /* 使用主题浅色边框 */
}

.episodes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.episode-item {
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  border: 1px solid var(--theme-border); /* 使用主题边框色 */
  border-radius: 4px;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
  font-weight: 500;
}

.episode-item:hover {
  background: var(--theme-primary); /* 使用主题主色 */
  color: white;
  border-color: var(--theme-primary); /* 使用主题主色边框 */
  transform: translateY(-2px); /* 添加悬停效果 */
  box-shadow: var(--theme-shadow-light); /* 添加主题阴影 */
}

/* 剧情简介区域 - 主题适配 */
.plot-section {
  max-width: 1200px;
  margin: 20px auto;
  padding: 30px 20px;
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 8px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
}

.plot-title {
  font-size: 18px;
  color: var(--theme-text-primary); /* 使用主题主要文字颜色 */
  margin: 0 0 15px 0;
  font-weight: 600;
}

.plot-text {
  line-height: 1.8;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  margin: 0;
  text-indent: 2em;
}

/* 评分和评论区域 */
.interaction-section {
  max-width: 1200px;
  margin: 20px auto;
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

.rating-wrapper {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.comment-wrapper {
  min-width: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
    gap: 20px;
    padding: 20px 15px;
  }

  .poster-container {
    width: 200px;
    margin: 0 auto;
  }

  .details-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .episodes-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
  }

  .episodes-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  /* 评分和评论区域响应式 */
  .interaction-section {
    grid-template-columns: 1fr;
    padding: 0 15px;
  }

  .rating-wrapper {
    position: static;
  }
}
</style>
