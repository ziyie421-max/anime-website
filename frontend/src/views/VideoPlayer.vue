<!-- 视频播放页面 - 播放动漫视频 -->
<template>
  <div class="video-player-page">
    <!-- 加载状态 -->
    <div v-if="loading || !animeData" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 播放页面内容 -->
    <div class="video-player-page-content" v-else>
      <!-- 顶部导航栏 -->
      <div class="breadcrumb">
        <span @click="$router.push('/')" class="breadcrumb-item">首页</span>
        <span class="separator">></span>
        <span @click="$router.push('/external')" class="breadcrumb-item">动漫列表</span>
        <span class="separator">></span>
        <span @click="$router.push(`/anime/${animeData.id}`)" class="breadcrumb-item">{{ animeData.title }}</span>
        <span class="separator">></span>
        <span class="current">第{{ currentEpisode }}集</span>
      </div>

      <!-- 视频播放区域 -->
      <div class="video-container">
        <div class="video-wrapper">
          <iframe
            :src="videoUrl"
            width="100%"
            height="100%"
            frameborder="0"
            allowfullscreen
            class="video-frame"
          ></iframe>
        </div>
      </div>

      <!-- 剧集选择 -->
      <div class="episodes-section">
        <div class="episodes-header">
          <h3 class="episodes-title">选集</h3>
        </div>
        <div class="episodes-grid">
          <div
            v-for="i in 20"
            :key="i"
            :class="['episode-item', { active: currentEpisode === i }]"
            @click="changeEpisode(i)"
          >
            第{{ String(i).padStart(2, '0') }}集
          </div>
        </div>
      </div>

      <!-- 评分和评论区域 -->
      <div class="interaction-section">
        <!-- 评分面板 -->
        <div class="rating-wrapper">
          <RatingPanel
            :external-anime-id="String(animeData.id)"
            :external-anime-title="animeData.title"
          />
        </div>

        <!-- 评论区 -->
        <div class="comment-wrapper">
          <CommentSection
            :external-anime-id="String(animeData.id)"
            :external-anime-title="animeData.title"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { animeAPI, externalAPI } from '@/services/api'
// 导入评论和评分组件
import CommentSection from '@/components/CommentSection.vue'
import RatingPanel from '@/components/RatingPanel.vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const animeData = ref(null)
const loading = ref(false)
const videoUrl = ref('')
const currentEpisode = ref(1)

// 获取动漫详情
const fetchAnimeDetail = async () => {
  loading.value = true
  try {
    const animeId = route.params.id
    const response = await externalAPI.getDetail(animeId)
    if (response.data.success) {
      animeData.value = response.data.data
      // 构建视频 URL (使用外部播放源)
      updateVideoUrl()
    }
  } catch (error) {
    console.error('获取动漫详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新视频 URL
const updateVideoUrl = () => {
  if (animeData.value && animeData.value.videoUrl) {
    // 替换集数
    videoUrl.value = animeData.value.videoUrl.replace(/\d+/, String(currentEpisode.value))
  } else {
    // 默认使用空 URL
    videoUrl.value = 'about:blank'
  }
}

// 切换集数
const changeEpisode = (episode) => {
  currentEpisode.value = episode
  updateVideoUrl()
}

// 初始化
onMounted(() => {
  fetchAnimeDetail()
})
</script>

<style scoped>
.video-player-page {
  min-height: 100vh;
  background: var(--theme-background);
  padding: 20px;
}

.loading-container {
  padding: 40px;
}

/* 面包屑导航 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: var(--theme-text-secondary);
}

.breadcrumb-item {
  cursor: pointer;
  transition: color 0.3s;
}

.breadcrumb-item:hover {
  color: var(--theme-primary);
}

.separator {
  color: var(--theme-text-secondary);
}

.current {
  color: var(--theme-text-primary);
}

/* 视频容器 */
.video-container {
  width: 100%;
  height: 600px;
  background: var(--theme-background);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--theme-border);
  margin-bottom: 20px;
}

.video-wrapper {
  width: 100%;
  height: 100%;
}

.video-frame {
  width: 100%;
  height: 100%;
}

/* 剧集选择 */
.episodes-section {
  background: var(--theme-background);
  border: 1px solid var(--theme-border);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.episodes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.episodes-title {
  margin: 0;
  font-size: 18px;
  color: var(--theme-text-primary);
}

.episodes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.episode-item {
  padding: 12px;
  background: var(--theme-background-soft);
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--theme-text-primary);
}

.episode-item:hover {
  background: var(--theme-primary);
  color: white;
  border-color: var(--theme-primary);
  transform: translateY(-2px);
}

.episode-item.active {
  background: var(--theme-primary);
  color: white;
  border-color: var(--theme-primary);
}

/* 互动区域 */
.interaction-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rating-wrapper,
.comment-wrapper {
  width: 100%;
}

/* 响应式 - 移动端适配 */
@media (max-width: 768px) {
  .video-player-page {
    padding: 12px;
  }

  /* 面包屑可能折行，允许换行并收紧 */
  .breadcrumb {
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 12px;
    font-size: 13px;
  }

  /* 固定 600px 高度在竖屏手机上比例失调，改为 16:9 自适应 */
  .video-container {
    height: auto;
    aspect-ratio: 16 / 9;
    margin-bottom: 14px;
  }

  .episodes-section {
    padding: 14px;
    margin-bottom: 14px;
  }

  .episodes-header {
    margin-bottom: 12px;
  }

  .episodes-title {
    font-size: 16px;
  }

  /* minmax(100px) 偏大，窄屏收紧到约 80px 保证一行至少 3 个 */
  .episodes-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 8px;
  }

  .episode-item {
    padding: 10px 8px;
    font-size: 13px;
  }

  .interaction-section {
    gap: 14px;
  }
}
</style>
