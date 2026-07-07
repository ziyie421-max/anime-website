<!-- 动漫周表组件 -->
<template>
  <div class="anime-weekly-schedule">
    <!-- 标题区域 -->
    <div class="schedule-header">
      <h2 class="schedule-title">
        <el-icon><Calendar /></el-icon>
        追番周表
      </h2>
      <div class="schedule-actions">
        <el-button 
          @click="refreshSchedule" 
          :loading="isLoading"
          size="small"
          type="primary"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 周表内容 -->
    <div class="schedule-content" v-loading="isLoading">
      <!-- 星期标签 -->
      <div class="weekday-tabs">
        <div 
          v-for="(day, index) in weekdays" 
          :key="index"
          :class="['weekday-tab', { active: currentDay === index }]"
          @click="currentDay = index"
        >
          <span class="weekday-name">{{ day.name }}</span>
          <span class="weekday-date">{{ day.date }}</span>
          <span class="anime-count">{{ getAnimeCountForDay(index) }}部</span>
        </div>
      </div>

      <!-- 动漫列表 -->
      <div class="anime-list">
        <div 
          v-for="anime in getCurrentDayAnime()" 
          :key="anime.vod_id"
          class="anime-item"
          @click="viewAnimeDetail(anime)"
        >
          <div class="anime-cover">
            <img 
              :src="anime.vod_pic || '/placeholder-anime.jpg'" 
              :alt="anime.vod_name"
              @error="handleImageError"
            />
            <div class="anime-overlay">
              <div class="anime-year">{{ anime.vod_year || '2024' }}</div>
            </div>
          </div>
          <div class="anime-info">
            <h3 class="anime-title">{{ anime.vod_name }}</h3>
            <p class="anime-episode">{{ formatEpisodeInfo(anime.vod_remarks) }}</p>
            <div class="anime-meta">
              <span class="anime-time">{{ formatUpdateTime(anime) }}</span>
              <span class="anime-status" :class="getStatusClass(anime.vod_remarks)">
                {{ getStatusText(anime.vod_remarks) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="getCurrentDayAnime().length === 0" class="empty-state">
        <el-icon><VideoCamera /></el-icon>
        <p>今天暂无更新的动漫</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Calendar, Refresh, VideoCamera } from '@element-plus/icons-vue'
import { externalAPI } from '@/services/api'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const isLoading = ref(false)
const currentDay = ref(new Date().getDay()) // 当前星期几 (0=周日, 1=周一, ...)
const animeData = ref([])

// 星期数据
const weekdays = computed(() => {
  const today = new Date()
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  return days.map((name, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - today.getDay() + index)
    return {
      name,
      date: `${date.getMonth() + 1}/${date.getDate()}`,
      index
    }
  })
})

// 智能分配动漫到不同的星期几
const getAnimeDayIndex = (anime) => {
  // 基于动漫名称生成一个稳定的哈希值
  const name = anime.vod_name || ''
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    const char = name.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // 转换为32位整数
  }

  // 根据动漫ID和名称的组合来分配，确保分配更均匀
  const id = anime.vod_id || 0
  const combinedHash = Math.abs(hash + id)

  return combinedHash % 7
}

// 获取当前选中日期的动漫
const getCurrentDayAnime = () => {
  return animeData.value.filter(anime => {
    return getAnimeDayIndex(anime) === currentDay.value
  })
}

// 获取指定日期的动漫数量
const getAnimeCountForDay = (dayIndex) => {
  return animeData.value.filter(anime => {
    return getAnimeDayIndex(anime) === dayIndex
  }).length
}

// 获取动漫周表数据
const fetchWeeklySchedule = async () => {
  try {
    isLoading.value = true
    console.log('📅 获取动漫周表数据...')

    // 使用量子资源API获取动漫数据
    const promises = [
      externalAPI.getAnimeList('日本动漫', 1),
      externalAPI.getAnimeList('日本动漫', 2)
    ]

    const responses = await Promise.all(promises)
    let allAnime = []

    responses.forEach(response => {
      if (response && response.code === 1 && response.list) {
        allAnime = allAnime.concat(response.list)
      }
    })

    if (allAnime.length > 0) {
      // 过滤出最新的动漫（通常是连载中的）
      animeData.value = allAnime
        .filter(anime => {
          // 过滤条件：包含"更新"、"集"等关键词，或者是最近的动漫
          const remarks = anime.vod_remarks || ''
          const name = anime.vod_name || ''

          // 优先选择连载中的动漫
          const isOngoing = remarks.includes('更新') ||
                           remarks.includes('集') ||
                           remarks.includes('话') ||
                           remarks.includes('完结') ||
                           name.includes('第') ||
                           anime.vod_year === '2024' ||
                           anime.vod_year === '2023'

          // 排除一些明显不是动漫的内容
          const isAnime = !name.includes('电影') &&
                         !name.includes('真人') &&
                         !name.includes('纪录片')

          return isAnime && isOngoing
        })
        .slice(0, 70) // 增加数量，确保每天都有足够的动漫

      console.log('📅 周表数据获取成功:', animeData.value.length, '部动漫')
    } else {
      console.error('❌ 周表数据获取失败: 没有获取到数据')
    }
  } catch (error) {
    console.error('❌ 获取周表数据时出错:', error)
  } finally {
    isLoading.value = false
  }
}

// 刷新周表
const refreshSchedule = () => {
  fetchWeeklySchedule()
}

// 查看动漫详情
const viewAnimeDetail = (anime) => {
  console.log('🎬 查看动漫详情:', anime.vod_name)
  // 跳转到动漫详情页面，使用正确的路由
  router.push({
    name: 'NewAnimeDetail',
    params: { id: anime.vod_id }
  })
}

// 格式化剧集信息
const formatEpisodeInfo = (remarks) => {
  if (!remarks) return '更新中'

  // 提取集数信息
  if (remarks.includes('集')) {
    return remarks
  } else if (remarks.includes('话')) {
    return remarks
  } else if (remarks.includes('更新')) {
    return remarks
  } else if (remarks.includes('完结')) {
    return remarks
  }

  return remarks || '更新中'
}

// 格式化更新时间
const formatUpdateTime = (anime) => {
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const dayIndex = getAnimeDayIndex(anime)
  return `${dayNames[dayIndex]} 更新`
}

// 获取状态样式类 - 统一使用主题色
const getStatusClass = (remarks) => {
  return 'status-theme' // 统一使用主题色样式
}

// 获取状态文本
const getStatusText = (remarks) => {
  if (!remarks) return '连载中'

  if (remarks.includes('完结')) return '已完结'
  if (remarks.includes('更新')) return '连载中'
  if (remarks.includes('集') || remarks.includes('话')) return '连载中'

  return '连载中'
}

// 处理图片加载错误
const handleImageError = (event) => {
  event.target.src = '/placeholder-anime.jpg'
}

// 组件挂载时获取数据
onMounted(() => {
  fetchWeeklySchedule()
})
</script>

<style scoped>
.anime-weekly-schedule {
  margin-bottom: 40px;
  background: var(--theme-background-soft);
  border-radius: 12px;
  border: 1px solid var(--theme-border);
  box-shadow: var(--theme-shadow-light);
  overflow: hidden;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: var(--theme-background);
  border-bottom: 1px solid var(--theme-border);
}

.schedule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--theme-text-primary);
}

.schedule-title .el-icon {
  color: var(--theme-primary);
}

.schedule-content {
  padding: 0;
}

.weekday-tabs {
  display: flex;
  background: var(--theme-background);
  border-bottom: 1px solid var(--theme-border);
  overflow-x: auto;
}

.weekday-tab {
  flex: 1;
  min-width: 120px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border-right: 1px solid var(--theme-border);
}

.weekday-tab:last-child {
  border-right: none;
}

.weekday-tab:hover {
  background: var(--theme-background-soft);
}

.weekday-tab.active {
  background: var(--theme-primary);
  color: white;
}

.weekday-name {
  display: block;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.weekday-date {
  display: block;
  font-size: 0.8rem;
  opacity: 0.8;
  margin-bottom: 4px;
}

.anime-count {
  display: block;
  font-size: 0.75rem;
  opacity: 0.7;
}

.anime-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  padding: 20px;
}

.anime-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  background: var(--theme-background);
  border: 1px solid var(--theme-border);
}

.anime-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--theme-shadow);
}

.anime-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.anime-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.anime-item:hover .anime-cover img {
  transform: scale(1.05);
}

.anime-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--theme-primary);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.anime-info {
  padding: 12px;
}

.anime-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--theme-text-primary);
  margin: 0 0 8px 0;
  line-height: 1.3;
  /* 改为单行显示，超出省略号 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.anime-episode {
  font-size: 0.8rem;
  color: var(--theme-primary);
  margin: 0 0 8px 0;
  font-weight: 500;
}

.anime-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.anime-time {
  font-size: 0.75rem;
  color: var(--theme-text-secondary);
  flex: 1;
}

.anime-status {
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
  white-space: nowrap;
}

/* 统一使用主题色的状态样式 */
.status-theme {
  background: rgba(var(--theme-primary-rgb), 0.1);
  color: var(--theme-primary);
  border: 1px solid rgba(var(--theme-primary-rgb), 0.2);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--theme-text-secondary);
}

.empty-state .el-icon {
  font-size: 3rem;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .schedule-header {
    padding: 16px;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .weekday-tabs {
    overflow-x: auto;
  }

  .weekday-tab {
    min-width: 80px;
    padding: 12px 8px;
  }

  .anime-list {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
    padding: 16px;
  }

  .anime-cover {
    height: 180px;
  }
}
</style>
