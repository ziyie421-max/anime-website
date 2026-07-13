<!-- 首页组件 - 展示热门动漫、最新更新等内容 -->
<template>
  <div class="home">
    <!-- 轮播图区域 - 海报在左侧，文字在右侧 -->
    <el-carousel ref="bannerCarouselRef" height="500px" class="banner-carousel" indicator-position="outside" arrow="hover">
      <el-carousel-item v-for="item in bannerList" :key="item.id">
        <div class="banner-item">
          <!-- 整个轮播区域的遮罩 -->
          <div class="banner-mask"></div>

          <!-- 海报区域 -->
          <div class="banner-poster">
            <img :src="item.image" :alt="item.title" fetchpriority="high" decoding="async" />
          </div>

          <!-- 文字内容区域 -->
          <div class="banner-content">
            <div class="banner-text">
              <h1>{{ item.title }}</h1>
              <p class="banner-description">{{ item.description }}</p>
              <div class="banner-actions">
                <el-button type="primary" size="large" @click="goToAnime(item)" class="info-btn">
                  <!-- <el-icon><InfoFilled /></el-icon> -->
                  详细信息
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 内容区域 - 优化PC端布局 -->
    <div class="content-container">

      <!-- 动漫周表 -->
      <AnimeWeeklySchedule />

      <!-- 热门推荐 -->
      <section class="section">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="section-icon"><Star /></el-icon>
            <h3>热门推荐</h3>
          </div>
          <!-- <el-button text type="primary" @click="$router.push('/anime?sort=popular')" class="more-btn">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button> -->
        </div>
        <div class="anime-grid">
          <router-link v-for="anime in popularAnime" :key="anime.id" class="anime-card" :to="{ name: 'NewAnimeDetail', params: { id: anime.id } }">
            <div class="anime-poster">
              <img :src="anime.poster" :alt="anime.title" loading="lazy" decoding="async" />
              <div class="anime-overlay">
                <el-icon class="play-icon"><VideoPlay /></el-icon>
                <!-- 悬停时显示年份 - 位置在底部 -->
                <div class="anime-year" v-if="anime.year">
                  {{ anime.year }}年
                </div>
              </div>
              <div class="anime-badge" v-if="anime.isFeatured">推荐</div>
              <!-- 隐藏播放源标识，不显示量子资源等信息 -->
              <!-- <div class="source-badge" v-if="anime.sourceName">{{ anime.sourceName }}</div> -->
            </div>
            <div class="anime-info">
              <h4 class="anime-title">{{ anime.title }}</h4>
              <p class="anime-meta">{{ anime.year }} · {{ anime.category }}</p>
              <!-- 移除星级评分显示 -->
            </div>
          </router-link>
        </div>
      </section>

      <!-- 最新更新 -->
      <section class="section">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="section-icon"><Refresh /></el-icon>
            <h3>最新更新</h3>
          </div>
          <!-- <el-button text type="primary" @click="$router.push('/anime?sort=latest')" class="more-btn">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button> -->
        </div>
        <div class="anime-grid">
          <router-link v-for="anime in latestAnime" :key="anime.id" class="anime-card" :to="{ name: 'NewAnimeDetail', params: { id: anime.id } }">
            <div class="anime-poster">
              <img :src="anime.poster" :alt="anime.title" loading="lazy" decoding="async" />
              <div class="anime-overlay">
                <el-icon class="play-icon"><VideoPlay /></el-icon>
                <!-- 悬停时显示年份 - 位置在底部 -->
                <div class="anime-year" v-if="anime.year">
                  {{ anime.year }}年
                </div>
              </div>
              <div class="episode-badge">{{ anime.latestEpisode }}</div>
            </div>
            <div class="anime-info">
              <h4 class="anime-title">{{ anime.title }}</h4>
              <p class="anime-meta">{{ anime.latestEpisode || '暂无更新' }}</p>
              <!-- 移除星级评分显示 -->
            </div>
          </router-link>
        </div>
      </section>


    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { animeAPI, externalAPI, handleApiError } from '@/services/api'
import AnimeWeeklySchedule from '@/components/AnimeWeeklySchedule.vue'
import {
  ArrowRight,
  VideoPlay,
  InfoFilled,
  Star,
  Refresh
} from '@element-plus/icons-vue'

const router = useRouter()

// 清理简介内容 - 直接去掉HTML实体编码乱码
const cleanDescription = (text) => {
  if (!text) return text

  // 去掉常见的HTML实体编码乱码
  return text
    .replace(/&amp;/g, '') // 去掉 &amp;
    .replace(/&lt;/g, '') // 去掉 &lt;
    .replace(/&gt;/g, '') // 去掉 &gt;
    .replace(/&quot;/g, '') // 去掉 &quot;
    .replace(/&#39;/g, '') // 去掉 &#39;
    .replace(/&nbsp;/g, ' ') // 将 &nbsp; 替换为空格
    .replace(/&[a-zA-Z0-9#]+;/g, '') // 去掉其他HTML实体
    .replace(/\s*;\s*/g, '') // 去掉多余的分号和周围空格
    .replace(/\s+/g, ' ') // 将多个空格合并为一个
    .trim() // 去掉首尾空格
}

// 响应式数据
const bannerList = ref([])
const popularAnime = ref([])
const latestAnime = ref([])
const loading = ref(false)

// 跳转到动漫详情页
const goToAnime = (anime) => {
  // 统一跳转到动漫详情页面
  const animeId = typeof anime === 'object' ? anime.id : anime
  router.push(`/anime/${animeId}`)
}



// 获取首页数据
const fetchHomeData = async () => {
  try {
    loading.value = true

    // 优先获取外部资源数据（索尼资源优先），如果失败则使用本地数据
    try {
      // 获取外部热门动漫数据 - 只获取8部
      const externalPopularResponse = await externalAPI.getPopularAnime(1, 8)

      if (externalPopularResponse && externalPopularResponse.code === 1 && externalPopularResponse.list) {
        // 设置热门动漫数据（来自外部资源）- 只取8部
        popularAnime.value = externalPopularResponse.list.slice(0, 8).map(anime => ({
          id: anime.vod_id,
          title: anime.vod_name,
          poster: anime.vod_pic,
          year: anime.vod_year,
          category: anime.type_name || '动漫',
          rating: parseFloat(anime.vod_score) || 0,
          isFeatured: anime.sourceKey === 'lzzy', // 量子资源标记为推荐
          sourceKey: anime.sourceKey,
          // sourceName: anime.sourceName, // 隐藏播放源名称
          description: cleanDescription(anime.vod_blurb || anime.vod_content) // 清理HTML实体乱码
        }))

        // 设置轮播图数据（使用前3个热门动漫）
        bannerList.value = popularAnime.value.slice(0, 3).map(anime => ({
          id: anime.id,
          title: anime.title,
          description: cleanDescription(anime.description) || '精彩动漫推荐，不容错过！', // 清理HTML实体乱码
          image: anime.poster
        }))

        // 设置最新动漫数据（使用外部资源数据）
        const externalLatestResponse = await externalAPI.getAnimeList('日韩动漫', 1)
        if (externalLatestResponse && externalLatestResponse.code === 1 && externalLatestResponse.list) {
          latestAnime.value = externalLatestResponse.list.slice(0, 8).map(anime => ({
            id: anime.vod_id,
            title: anime.vod_name,
            poster: anime.vod_pic,
            latestEpisode: anime.vod_remarks || '更新中',
            rating: parseFloat(anime.vod_score) || 0,
            sourceKey: anime.sourceKey,
            // sourceName: anime.sourceName // 隐藏播放源名称
          }))
        }

        console.log('成功获取外部资源数据')
      } else {
        throw new Error('外部资源数据获取失败')
      }
    } catch (externalError) {
      console.warn('外部资源获取失败，使用本地数据:', externalError)

      // 如果外部资源失败，使用本地API数据
      const [featuredResponse, popularResponse, latestResponse] = await Promise.all([
        animeAPI.getFeatured(),
        animeAPI.getPopular(12),
        animeAPI.getLatest(12)
      ])

      // 设置轮播图数据（使用推荐动漫）
      bannerList.value = featuredResponse.slice(0, 3).map(anime => ({
        id: anime.id,
        title: anime.title,
        description: cleanDescription(anime.description), // 清理HTML实体乱码
        image: anime.banner || anime.poster
      }))

      // 设置热门动漫数据 - 只取8部
      popularAnime.value = popularResponse.slice(0, 8).map(anime => ({
        id: anime.id,
        title: anime.title,
        poster: anime.poster,
        year: anime.year,
        category: anime.status,
        rating: anime.rating || 0,
        isFeatured: anime.isFeatured
      }))

      // 设置最新动漫数据 - 只取8部
      latestAnime.value = latestResponse.slice(0, 8).map(anime => ({
        id: anime.id,
        title: anime.title,
        poster: anime.poster,
        latestEpisode: anime.currentEpisodes || 1,
        rating: anime.rating || 0
      }))
    }



  } catch (error) {
    console.error('获取首页数据失败:', error)
    ElMessage.error(handleApiError(error))

    // 如果所有API都失败，使用备用数据
    setFallbackData()
  } finally {
    loading.value = false
  }
}

// 备用数据（API失败时使用）
const setFallbackData = () => {
  bannerList.value = [
    {
      id: 1,
      title: '进击的巨人 最终季',
      description: '人类与巨人的终极对决，真相即将揭晓',
      image: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=500&fit=crop'
    },
    {
      id: 2,
      title: '鬼灭之刃 刀匠村篇',
      description: '炭治郎前往刀匠村，新的冒险即将开始',
      image: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=1200&h=500&fit=crop'
    }
  ]

  popularAnime.value = Array.from({ length: 8 }, (_, i) => ({
    id: i + 1,
    title: `热门动漫 ${i + 1}`,
    poster: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=200&h=280&fit=crop',
    year: 2023,
    category: '动作',
    rating: 4.5,
    isFeatured: i < 3
  }))

  latestAnime.value = Array.from({ length: 8 }, (_, i) => ({
    id: i + 13,
    title: `最新动漫 ${i + 1}`,
    poster: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=200&h=280&fit=crop',
    latestEpisode: i + 1,
    rating: 4.0
  }))


}

// 轮播图触摸滑动支持（移动端）：记录起手坐标，松手时按水平位移方向切换
const bannerCarouselRef = ref(null)
let carouselTouchStartX = 0
let carouselTouchStartY = 0

const onCarouselTouchStart = (e) => {
  carouselTouchStartX = e.touches[0].clientX
  carouselTouchStartY = e.touches[0].clientY
}

const onCarouselTouchEnd = (e) => {
  const dx = e.changedTouches[0].clientX - carouselTouchStartX
  const dy = e.changedTouches[0].clientY - carouselTouchStartY
  // 仅在明显的水平滑动时切换，避免与页面纵向滚动冲突
  if (Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(dy) * 1.5) {
    if (dx < 0) {
      bannerCarouselRef.value?.next() // 向左滑 → 下一张
    } else {
      bannerCarouselRef.value?.prev() // 向右滑 → 上一张
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchHomeData()
  // 绑定轮播图触摸事件（用于移动端左右滑动切换）
  nextTick(() => {
    const el = bannerCarouselRef.value?.$el
    if (el) {
      el.addEventListener('touchstart', onCarouselTouchStart, { passive: true })
      el.addEventListener('touchend', onCarouselTouchEnd, { passive: true })
    }
  })
})

onUnmounted(() => {
  const el = bannerCarouselRef.value?.$el
  if (el) {
    el.removeEventListener('touchstart', onCarouselTouchStart)
    el.removeEventListener('touchend', onCarouselTouchEnd)
  }
})
</script>

<style scoped>
/* PC端优化样式 */
.home {
  min-height: 100vh;
  background: var(--theme-background-soft); /* 使用主题背景色 */
}

/* 轮播图样式优化 */
.banner-carousel {
  margin-bottom: 60px;
  border-radius: 0 0 20px 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

/* 覆盖Element Plus轮播图的默认背景 */
.banner-carousel :deep(.el-carousel__container) {
  background: transparent !important;
}

.banner-carousel :deep(.el-carousel__item) {
  background: transparent !important;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  position: relative;
  background: var(--theme-background-soft); /* 使用主题背景色，暗色主题下为深色 */
  overflow: hidden;
}

/* 整个轮播区域的遮罩 */
.banner-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    rgba(0, 0, 0, 0.2) 0%,
    rgba(0, 0, 0, 0.4) 50%,
    rgba(0, 0, 0, 0.6) 100%
  );
  z-index: 1;
}

/* 海报区域样式 - 右移避免遮挡切换按钮 */
.banner-poster {
  width: 380px;
  height: 100%;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
  padding: 25px;
  margin-left: 50px; /* 右移50px避免遮挡左侧切换按钮 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-poster img {
  width: 300px;
  height: 450px;
  object-fit: cover;
  border-radius: 15px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.4);
  transition: transform 0.3s ease;
}

.banner-poster img:hover {
  transform: scale(1.05);
}

.banner-content {
  flex: 1;
  padding: 40px 60px 40px 40px;
  color: var(--theme-banner-text); /* 使用轮播图专用颜色 */
  z-index: 2;
  position: relative;
  display: flex;
  align-items: center;
}

.banner-text h1 {
  font-size: clamp(2.25rem, 4vw, 3.5rem);
  font-weight: 700;
  margin: 0 0 1.25rem;
  color: var(--theme-banner-text); /* 使用轮播图专用颜色 */
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  line-height: 1.2;
}

.banner-description {
  font-size: 1.4rem;
  margin: 0 0 2rem;
  color: var(--theme-banner-text-secondary) !important; /* 使用轮播图专用次要颜色，加强优先级 */
  line-height: 1.6;
  max-width: 60ch;
  word-wrap: break-word; /* 确保长单词能够换行 */
}

.banner-actions {
  display: flex;
  gap: 20px;
}

.info-btn {
  min-height: 44px;
  padding: 12px 30px;
  font-size: 1.1rem;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  background: var(--theme-gradient) !important; /* 使用主题渐变色，支持主题切换 */
  border: none !important;
  color: white !important;
  box-shadow: var(--theme-shadow) !important; /* 使用主题阴影 */
}

.info-btn:hover {
  background: var(--theme-gradient-reverse) !important; /* 悬停时使用反向渐变 */
  transform: translateY(-2px); /* 悬停时轻微上移 */
  box-shadow: var(--theme-shadow) !important; /* 悬停时阴影 */
}

/* 内容区域样式 */
.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

/* 快速导航样式 */
.quick-nav-section {
  margin-bottom: 60px;
}

.quick-nav {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
  background: var(--theme-background); /* 使用主题背景色 */
  padding: 40px;
  border-radius: 20px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--theme-background-soft); /* 使用主题背景色 */
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
}

.nav-item:hover {
  transform: translateY(-5px);
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
  background: var(--theme-gradient); /* 使用主题渐变 */
  color: white;
}

.nav-icon {
  font-size: 2.5rem;
  margin-bottom: 15px;
  color: var(--theme-primary); /* 使用主题主色调 */
  transition: color 0.3s ease;
}

.nav-item:hover .nav-icon {
  color: white;
}

.nav-item span {
  font-size: 1.1rem;
  font-weight: 600;
}

/* 区块样式 */
.section {
  margin-bottom: 80px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding: 0 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.section-icon {
  font-size: 2rem;
  color: var(--theme-primary); /* 使用主题主色调 */
}

.section-header h3 {
  font-size: 2.2rem;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  font-weight: 700;
  margin: 0;
}

.more-btn {
  font-size: 1.1rem;
  font-weight: 600;
}

/* 动漫网格样式 - 一行显示4部 */
.anime-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 25px;
  padding: 20px;
}

.anime-card {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--theme-shadow-light); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
  color: inherit;
  text-decoration: none;
}

.anime-card:focus-visible {
  outline: 3px solid var(--theme-primary);
  outline-offset: 3px;
}

.anime-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
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

/* 暗色主题下的年份标签 */
.theme-dark .anime-year {
  background: var(--theme-primary); /* 暗色主题也使用主题主色 */
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 推荐标签样式 */
.anime-badge {
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

/* 集数标签样式 */
.episode-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--theme-primary); /* 使用主题主色，与推荐标签一致 */
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  box-shadow: var(--theme-shadow-light);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 暗色主题下的推荐标签 */
.theme-dark .anime-badge {
  background: var(--theme-primary);
  border: 1px solid rgba(0, 0, 0, 0.2);
}

/* 暗色主题下的集数标签 */
.theme-dark .episode-badge {
  background: var(--theme-primary);
  border: 1px solid rgba(0, 0, 0, 0.2);
}

.source-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  z-index: 3;
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

.anime-rating {
  display: flex;
  align-items: center;
  justify-content: space-between;
}



/* 响应式设计 */
@media (max-width: 1200px) {
  .banner-poster {
    width: 300px;
    margin-left: 20px;
    padding: 20px;
  }

  .banner-poster img {
    width: 230px;
    height: 345px;
  }

  .banner-content {
    padding: 32px 36px 32px 24px;
  }

  .banner-description {
    font-size: 1.1rem;
  }

  .content-container {
    max-width: 1000px;
    padding: 0 30px;
  }

  .anime-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .content-container {
    padding: 0 14px;
  }

  /* 轮播图移动端：降低高度，重置 PC 端负 margin/padding hack */
  .banner-carousel {
    margin-bottom: 30px;
  }

  .banner-carousel :deep(.el-carousel__container),
  .banner-carousel :deep(.el-carousel__item) {
    height: 460px !important;
  }

  .banner-item {
    flex-direction: column;
    padding: 0;
    align-items: center;
  }

  .banner-poster {
    width: 100%;
    height: 250px;
    padding: 18px 0 0;
    margin-left: 0;
  }

  .banner-poster img {
    width: 150px;
    height: 232px;
  }

  /* 移动端遮罩改为自上而下加深，文字更易读 */
  .banner-mask {
    background: linear-gradient(
      180deg,
      rgba(0, 0, 0, 0.25) 0%,
      rgba(0, 0, 0, 0.55) 60%,
      rgba(0, 0, 0, 0.7) 100%
    );
  }

  .banner-content {
    flex: 1;
    padding: 10px 18px 18px;
    text-align: center;
  }

  .banner-text h1 {
    font-size: 1.5rem;
    margin-top: 0; /* 重置 PC 端 -400px */
    margin-bottom: 0.6rem;
    line-height: 1.25;
  }

  .banner-description {
    font-size: 0.9rem;
    margin: 0 auto 0.8rem auto;
    margin-top: 0; /* 重置 PC 端 60px */
    max-width: 100%;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .banner-actions {
    position: static; /* 重置 PC 端绝对定位 */
    transform: none;
    left: auto;
    bottom: auto;
    justify-content: center;
  }

  .info-btn {
    padding: 8px 22px;
    font-size: 0.95rem;
  }

  .anime-grid {
    display: flex; /* 移动端改为横向滑动，不再使用网格 */
    overflow-x: auto;
    scroll-snap-type: x mandatory;
    -webkit-overflow-scrolling: touch;
    gap: 12px;
    padding: 8px 4px 16px;
    scrollbar-width: none; /* Firefox 隐藏滚动条 */
  }

  .anime-grid::-webkit-scrollbar {
    display: none; /* Chrome/Safari 隐藏滚动条 */
  }

  .anime-card {
    flex: 0 0 142px; /* 固定卡片宽度，确保可横向滑动 */
    scroll-snap-align: start;
  }

  .anime-poster {
    height: 196px;
  }

  .anime-info {
    padding: 10px 12px;
  }

  .anime-title {
    font-size: 0.92rem;
  }

  .section {
    margin-bottom: 40px;
  }

  .section-header {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    margin-bottom: 18px;
    padding: 0 4px;
    gap: 8px;
  }

  .section-header h3 {
    font-size: 1.25rem;
  }

  .section-icon {
    font-size: 1.5rem;
  }

  .more-btn {
    font-size: 0.9rem;
  }
}
</style>
