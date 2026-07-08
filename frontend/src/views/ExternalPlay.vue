<!-- 外部动漫播放页面 - 播放豪华资源的动漫视频 -->
<template>
  <div class="external-play-page">
    <!-- 返回按钮和动漫标题 -->
    <div class="header-section">
      <el-button @click="goBack" :icon="ArrowLeft" class="back-button">
        返回列表
      </el-button>
      <h2 class="anime-title" v-if="animeTitle">{{ animeTitle }}</h2>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-section">
      <el-skeleton :rows="5" animated />
      <div class="loading-text">正在获取播放信息...</div>
    </div>

    <!-- 播放区域 -->
    <div v-else-if="playData" class="play-section">
      <!-- 播放器区域 -->
      <div class="player-section" v-if="currentPlayUrl">
        <!-- 代理模式开关：直连快但受网络限制；代理走服务器（无视VPN） -->
        <div class="proxy-toggle-bar">
          <el-switch
            v-model="useProxy"
            active-text="代理加速"
            inactive-text="直连模式"
            @change="onProxyToggleChange"
          />
          <span v-if="proxyAutoSwitched" class="proxy-auto-hint">
            ⚡ 检测到网络限制，已自动切换代理
          </span>
          <span v-if="useProxy" class="proxy-status-badge">
            🔒 通过服务器代理播放
          </span>
        </div>
        <div class="player-container">
          <!-- DPlayer播放器容器 -->
          <div ref="dplayerContainer" class="dplayer-container"></div>
        </div>

        <!-- 播放信息区域已隐藏，保持界面简洁 -->


      </div>



      <!-- 剧集选择 - 移到播放器下方 -->
      <div class="episode-selector" v-if="currentEpisodes.length > 0">
        <h3>选择剧集</h3>
        <div class="episode-list">
          <el-button
            v-for="(episode, index) in currentEpisodes"
            :key="index"
            :type="currentEpisodeIndex === index ? 'primary' : 'default'"
            size="small"
            @click="selectEpisode(index)"
            class="episode-btn"
          >
            {{ episode.name }}
          </el-button>
        </div>
      </div>

      <!-- 评分和评论区域 -->
      <div class="interaction-section">
        <!-- 评分面板 -->
        <div class="rating-wrapper">
          <RatingPanel
            :external-anime-id="String(vodId)"
            :external-anime-title="animeTitle"
          />
        </div>

        <!-- 评论区 -->
        <div class="comment-wrapper">
          <CommentSection
            :external-anime-id="String(vodId)"
            :external-anime-title="animeTitle"
          />
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else class="error-section">
      <el-result
        icon="error"
        title="获取播放信息失败"
        sub-title="请检查网络连接或稍后重试"
      >
        <template #extra>
          <el-button type="primary" @click="retryLoad">
            重新加载
          </el-button>
          <el-button @click="goBack">
            返回列表
          </el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { externalAPI } from '@/services/api'
import { addWatchHistory } from '@/api/user'
// 导入评论和评分组件
import CommentSection from '@/components/CommentSection.vue'
import RatingPanel from '@/components/RatingPanel.vue'

const route = useRoute()
const router = useRouter()
// authStore 暂不使用，直接从 localStorage 检查登录状态

// 响应式数据
const isLoading = ref(true)
const isPlaying = ref(false)
const playData = ref(null)
const vodId = ref('')  // 动漫 ID
const currentEpisodeIndex = ref(0)
const currentPlayUrl = ref('')
const dplayerContainer = ref(null) // DPlayer 容器引用
const dplayer = ref(null) // DPlayer 实例
const animeTitle = ref(route.query.title || '未知动漫')
const animeCover = ref('')  // 动漫封面
const playerStatus = ref(null)
const watchHistoryId = ref(null)  // 当前观看历史记录 ID
const useProxy = ref(false) // 是否通过后端代理播放（VPN/网络受限时开启）
const proxyAutoSwitched = ref(false) // 是否自动切换到了代理模式

// 计算属性 - 固定使用lzm3u8播放源
const currentEpisodes = computed(() => {
  if (!playData.value) return []
  return playData.value.playUrls['lzm3u8'] || []
})

// 获取播放数据
const fetchPlayData = async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('缺少动漫 ID 参数')
    goBack()
    return
  }

  // 设置 vodId 供评分和评论组件使用
  vodId.value = id

  try {
    isLoading.value = true
    console.log('获取播放数据:', id)

    const response = await externalAPI.getPlayUrls(id)

    if (response && response.playUrls) {
      playData.value = response
      animeTitle.value = response.title || animeTitle.value
      animeCover.value = response.cover || ''  // 保存封面

      // 检查是否有lzm3u8播放源
      if (response.playUrls['lzm3u8']) {
        console.log('找到lzm3u8播放源')

        // 等待下一个tick确保计算属性更新，然后选择第一集
        await nextTick()
        const episodes = response.playUrls['lzm3u8']
        if (episodes && episodes.length > 0) {
          // 检查URL参数中是否有指定的剧集
          const queryEpisode = parseInt(route.query.episode) || 0
          const startIndex = queryEpisode > 0 ? Math.min(queryEpisode - 1, episodes.length - 1) : 0
          console.log('选择剧集:', episodes[startIndex].name)
          selectEpisode(startIndex)
        }
      } else {
        console.warn('未找到lzm3u8播放源')
        ElMessage.warning('未找到支持的播放源')
      }

      console.log('播放数据获取成功:', response)
    } else {
      throw new Error('播放数据格式错误')
    }

  } catch (error) {
    console.error('获取播放数据失败:', error)
    ElMessage.error('获取播放信息失败: ' + error.message)
  } finally {
    isLoading.value = false
  }
}

// 代理模式切换：关闭时如果之前是自动切换的，重置标记；切换时重建播放器
const onProxyToggleChange = (val) => {
  console.log('代理模式切换为:', val)
  if (!val) proxyAutoSwitched.value = false
  // 重建播放器以应用新的 URL
  nextTick(() => setupVideoPlayer())
}

/**
 * 生成代理 URL：后端替前端请求视频 CDN，绕过用户本地 IP（VPN/校园网）限制
 * 未挂 VPN 的用户走直连（currentPlayUrl 原值），不受影响
 */
const getProxyUrl = (directUrl) => {
  try {
    const encoded = btoa(unescape(encodeURIComponent(directUrl)))
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
    return `/api/video/proxy?url=${encoded}`
  } catch {
    return directUrl
  }
}

// 当前实际播放的 URL（直连或代理，根据 useProxy 和是否有错误自动选择）
const actualPlayUrl = computed(() => {
  if (!currentPlayUrl.value) return ''
  return useProxy.value ? getProxyUrl(currentPlayUrl.value) : currentPlayUrl.value
})

// 选择剧集
const selectEpisode = async (index) => {
  if (index < 0 || index >= currentEpisodes.value.length) return

  currentEpisodeIndex.value = index
  const episode = currentEpisodes.value[index]
  currentPlayUrl.value = episode.url

  // 切换剧集时重置错误触发的自动代理标记（新剧集可以重新尝试直连）
  proxyAutoSwitched.value = false

  console.log('选择剧集:', episode.name, episode.url, 'proxy:', useProxy.value)

  // 记录观看历史（用户已登录时）
  recordWatchHistory(index, episode.name)

  // 等待DOM更新后设置视频源
  await nextTick()

  // 确保DPlayer容器元素存在
  if (!dplayerContainer.value) {
    console.error('DPlayer容器元素未找到，等待DOM渲染...')
    // 再等待一个tick
    await nextTick()
  }

  if (dplayerContainer.value) {
    console.log('DPlayer容器元素已准备，开始设置播放器')
    setupVideoPlayer()


  } else {
    console.error('DPlayer容器元素仍未找到')
  }
}

// 记录观看历史
const recordWatchHistory = async (episodeIndex, episodeName) => {
  // 检查是否有 accessToken（直接从 localStorage 检查，因为 authStore.isLoggedIn 可能还没初始化）
  const hasToken = localStorage.getItem('accessToken')
  if (!hasToken) {
    console.log('用户未登录（无token），不记录观看历史')
    return
  }

  try {
    const vodId = route.query.id
    console.log('记录观看历史 - 动漫ID:', vodId, '剧集:', episodeName)

    const response = await addWatchHistory({
      externalAnimeId: String(vodId),  // 确保是字符串
      externalAnimeTitle: animeTitle.value,
      externalAnimeCover: animeCover.value || playData.value?.cover || '',
      externalEpisodeName: episodeName,
      externalEpisodeIndex: episodeIndex + 1,  // 转换为1-based索引
      sourceKey: 'lzzy',
      sourceName: '量子资源',
      watchProgress: 0,
      totalDuration: 0
    })

    // 保存历史记录ID用于后续更新进度
    watchHistoryId.value = response.data?.data?.id || response.data?.id
    console.log('观看历史已记录:', watchHistoryId.value)
  } catch (error) {
    console.error('记录观看历史失败:', error)
    // 不影响用户观看，静默失败
  }
}

// 设置DPlayer播放器
const setupVideoPlayer = () => {
  console.log('setupVideoPlayer 被调用')
  console.log('dplayerContainer.value:', dplayerContainer.value)
  console.log('currentPlayUrl.value:', currentPlayUrl.value)
  console.log('window.DPlayer 是否存在:', !!window.DPlayer)
  console.log('window.Hls 是否存在:', !!window.Hls)

  if (!dplayerContainer.value) {
    console.error('DPlayer容器元素不存在')
    ElMessage.error('播放器容器未找到')
    return
  }

  if (!currentPlayUrl.value) {
    console.error('播放URL不存在')
    ElMessage.error('播放链接不存在')
    return
  }

  if (!window.DPlayer) {
    console.error('DPlayer库未加载')
    ElMessage.error('播放器组件未加载，请刷新页面重试')
    return
  }

  try {
    console.log('设置DPlayer播放器，URL:', currentPlayUrl.value)

    // 清理之前的播放器实例
    if (dplayer.value) {
      console.log('清理之前的播放器实例')
      try {
        dplayer.value.destroy()
      } catch (e) {
        console.warn('清理播放器实例时出错:', e)
      }
      dplayer.value = null
    }

    // 清空容器内容
    if (dplayerContainer.value) {
      dplayerContainer.value.innerHTML = ''
    }

    // 检查视频URL格式
    const isHLS = currentPlayUrl.value.includes('.m3u8')
    console.log('视频格式:', isHLS ? 'HLS' : '普通视频', 'via proxy:', useProxy.value)

    // 创建DPlayer实例 - 使用自定义控制栏配置
    console.log('开始创建DPlayer实例...')
    // 最简化的播放器配置
    const playerConfig = {
      container: dplayerContainer.value,
      volume: 0.8,
      theme: '#4EABE6',
      video: {
        url: actualPlayUrl.value
      }
    }

    console.log('DPlayer配置:', playerConfig)
    dplayer.value = new window.DPlayer(playerConfig)
    console.log('DPlayer实例创建成功:', dplayer.value)

    // 添加自定义控制按钮
    addCustomControls()

    // 绑定播放器事件 - 简化版本
    try {
      dplayer.value.on('loadedmetadata', () => {
        console.log('视频元数据加载完成')
        setPlayerStatus('success', '加载完成', '视频已准备就绪')
        setTimeout(() => {
          playerStatus.value = null
        }, 3000)
      })

      dplayer.value.on('play', () => {
        console.log('开始播放')
        isPlaying.value = true
      })

      dplayer.value.on('pause', () => {
        console.log('暂停播放')
        isPlaying.value = false
      })
    } catch (error) {
      console.error('绑定播放器事件失败:', error)
    }

    dplayer.value.on('ended', () => {
      console.log('视频播放结束')
      isPlaying.value = false

      // 自动播放下一集
      if (currentEpisodeIndex.value < currentEpisodes.value.length - 1) {
        ElMessage.success('自动播放下一集')
        setTimeout(() => {
          // 播放下一集
          selectEpisode(currentEpisodeIndex.value + 1)
        }, 2000)
      }
    })

    dplayer.value.on('error', (info) => {
      console.warn('DPlayer播放事件:', info)

      // 检查是否是真正的致命错误
      const videoEl = dplayer.value?.video
      const hasFatalError = videoEl && videoEl.error

      // 如果是网络错误但视频仍在播放，不显示错误
      if (info && typeof info === 'object' && !hasFatalError) {
        console.log('检测到非致命错误，视频仍可正常播放，忽略错误提示')
        return
      }

      // 真正的播放错误
      if (hasFatalError || info) {
        // 智能回退：如果当前是直连且未尝试过代理，自动切换到代理重试
        if (!useProxy.value && currentPlayUrl.value) {
          console.log('⚠️ 直连失败，自动切换到代理模式重试...')
          useProxy.value = true
          proxyAutoSwitched.value = true
          setPlayerStatus('info', '网络受限', '检测到网络限制，正在通过服务器代理重试...')
          ElMessage.warning('视频直连失败，已自动切换代理模式')

          // 销毁当前播放器，用代理 URL 重建
          setTimeout(() => {
            if (dplayer.value) {
              try { dplayer.value.destroy() } catch(e) {}
              dplayer.value = null
            }
            if (dplayerContainer.value) dplayerContainer.value.innerHTML = ''
            setupVideoPlayer()
          }, 500)
          return
        }

        // 已经在使用代理或无法重试，显示具体错误
        let errorMessage = '视频播放出错'
        if (hasFatalError) {
          switch (videoEl.error.code) {
            case 1: errorMessage = '视频播放被中止'; break
            case 2: errorMessage = '网络错误，请检查网络连接'; break
            case 3: errorMessage = '视频解码错误，请尝试其他播放源'; break
            case 4: errorMessage = '视频格式不支持，请尝试其他播放源'; break
            default: errorMessage = '未知播放错误'
          }
        }
        console.error('视频播放致命错误:', errorMessage)
        setPlayerStatus('error', '播放错误', errorMessage)
        ElMessage.error(errorMessage)
      }
    })

    dplayer.value.on('loadstart', () => {
      console.log('开始加载视频')
      setPlayerStatus('info', '正在加载', '视频正在加载中，请稍候...')
    })

    // 添加更多事件监听以便调试
    dplayer.value.on('canplay', () => {
      console.log('视频可以开始播放')
      setPlayerStatus('success', '加载完成', '视频已准备就绪')
      setTimeout(() => {
        playerStatus.value = null
      }, 2000)
    })

    dplayer.value.on('waiting', () => {
      console.log('视频缓冲中...')
      setPlayerStatus('info', '缓冲中', '视频正在缓冲，请稍候...')
    })

    dplayer.value.on('playing', () => {
      console.log('视频正在播放')
      // 清除之前的状态提示
      if (playerStatus.value && playerStatus.value.type !== 'error') {
        playerStatus.value = null
      }
    })

    // 监听网络状态变化
    dplayer.value.on('progress', () => {
      // 视频正在下载，说明网络连接正常
      console.log('视频下载进度更新')
    })

    // 如果是HLS格式，等待HLS.js加载完成
    if (currentPlayUrl.value.includes('.m3u8')) {
      console.log('检测到HLS格式，使用DPlayer的HLS支持')
    }

    // 启动播放器健康检查
    startPlayerHealthCheck()

    // 添加状态提示到DPlayer内部，确保全屏时也显示
    setTimeout(() => {
      addStatusOverlayToDPlayer()
    }, 1000)

    ElMessage.success('播放器初始化成功')

  } catch (error) {
    console.error('设置DPlayer失败:', error)
    ElMessage.error('播放器设置失败: ' + error.message)
  }
}

// 添加状态提示到DPlayer容器内
const addStatusOverlayToDPlayer = () => {
  if (!dplayer.value || !dplayer.value.container) {
    console.log('DPlayer容器不存在，跳过状态提示添加')
    return
  }

  try {
    // 创建状态提示元素
    const statusOverlay = document.createElement('div')
    statusOverlay.className = 'dplayer-status-overlay'
    statusOverlay.style.cssText = `
      position: absolute;
      top: 20px;
      left: 20px;
      z-index: 9999;
      pointer-events: none;
      display: none;
    `

    // 添加到DPlayer容器
    dplayer.value.container.appendChild(statusOverlay)
    console.log('状态提示已添加到DPlayer容器')

    // 监听状态变化并更新显示
    const updateStatusDisplay = () => {
      try {
        if (playerStatus.value && statusOverlay) {
          statusOverlay.innerHTML = `
            <div style="
              padding: 8px 16px;
              border-radius: 8px;
              background: rgba(0, 0, 0, 0.7);
              color: white;
              font-size: 14px;
              font-weight: 500;
              backdrop-filter: blur(10px);
            ">
              ${playerStatus.value.title}
            </div>
          `
          statusOverlay.style.display = 'block'
        } else if (statusOverlay) {
          statusOverlay.style.display = 'none'
        }
      } catch (error) {
        console.error('更新状态显示失败:', error)
      }
    }

    // 使用watch监听状态变化
    watch(playerStatus, updateStatusDisplay, { immediate: true })
  } catch (error) {
    console.error('添加状态提示失败:', error)
  }
}

// 直接修改DPlayer控制栏HTML，内置剧集控制按钮
const addCustomControls = () => {
  if (!dplayer.value) return

  // 等待DPlayer完全初始化
  setTimeout(() => {
    // 直接修改DPlayer的控制栏HTML结构
    const controller = dplayer.value.template.controller
    if (!controller) return

    // 找到右侧图标区域
    const rightIcons = controller.querySelector('.dplayer-icons-right')
    if (!rightIcons) return

    // 找到播放按钮
    const playButton = controller.querySelector('.dplayer-play-icon')
    if (!playButton) return

    // 创建上一集按钮（插入到播放按钮左侧）
    const prevEpisodeHTML = `
      <div class="dplayer-icon dplayer-prev-episode" title="上一集">
        <svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 32 32">
          <path d="M7 7h3v18h-3v-18zM12 16l10 7v-14z"></path>
        </svg>
      </div>
    `

    // 创建下一集按钮（插入到播放按钮右侧）
    const nextEpisodeHTML = `
      <div class="dplayer-icon dplayer-next-episode" title="下一集">
        <svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 32 32">
          <path d="M22 7h3v18h-3v-18zM10 9v14l10-7z"></path>
        </svg>
      </div>
    `

    // 创建剧集列表按钮（插入到右侧图标区域）
    const episodeListHTML = `
      <div class="dplayer-icon dplayer-episode-list" title="剧集列表">
        <svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 32 32">
          <path d="M4 10h4v4h-4zM10 10h18v4h-18zM4 16h4v4h-4zM10 16h18v4h-18zM4 22h4v4h-4zM10 22h18v4h-18z"></path>
        </svg>
      </div>
    `

    // 插入按钮到指定位置
    playButton.insertAdjacentHTML('beforebegin', prevEpisodeHTML) // 上一集按钮在播放按钮左侧
    playButton.insertAdjacentHTML('afterend', nextEpisodeHTML)   // 下一集按钮在播放按钮右侧
    rightIcons.insertAdjacentHTML('afterbegin', episodeListHTML) // 剧集列表按钮在右侧区域开头

    // 绑定事件监听器
    const prevBtn = controller.querySelector('.dplayer-prev-episode')
    const nextBtn = controller.querySelector('.dplayer-next-episode')
    const listBtn = controller.querySelector('.dplayer-episode-list')

    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        if (currentEpisodeIndex.value > 0) {
          selectEpisode(currentEpisodeIndex.value - 1)
        } else {
          ElMessage.info('已经是第一集了')
        }
      })
    }

    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        if (currentEpisodeIndex.value < currentEpisodes.value.length - 1) {
          selectEpisode(currentEpisodeIndex.value + 1)
        } else {
          ElMessage.info('已经是最后一集了')
        }
      })
    }

    if (listBtn) {
      listBtn.addEventListener('click', () => {
        showEpisodeList()
      })
    }

    console.log('✅ 剧集控制按钮已内置到DPlayer控制栏 - 上一集/下一集在播放键左右，剧集列表在右侧')
  }, 1000)
}



// 显示播放器内置剧集列表
const showEpisodeList = () => {
  if (!dplayer.value) return

  // 检查是否已经有剧集列表面板
  const existingPanel = dplayer.value.container.querySelector('.dplayer-episode-panel')
  if (existingPanel) {
    // 如果已存在，则切换显示/隐藏
    existingPanel.style.display = existingPanel.style.display === 'none' ? 'block' : 'none'
    return
  }

  // 创建剧集列表面板
  const episodePanel = document.createElement('div')
  episodePanel.className = 'dplayer-episode-panel'
  episodePanel.innerHTML = `
    <div class="dplayer-episode-panel-header">
      <span class="dplayer-episode-panel-title">${animeTitle.value}</span>
    </div>
    <div class="dplayer-episode-panel-content">
      ${currentEpisodes.value.map((episode, index) => {
        const isActive = index === currentEpisodeIndex.value
        return `
          <div class="dplayer-episode-item ${isActive ? 'active' : ''}" data-index="${index}">
            <span class="dplayer-episode-name">${episode.name}</span>
          </div>
        `
      }).join('')}
    </div>
  `

  // 添加到播放器容器中
  dplayer.value.container.appendChild(episodePanel)



  // 绑定剧集点击事件
  const episodeItems = episodePanel.querySelectorAll('.dplayer-episode-item')
  episodeItems.forEach(item => {
    item.addEventListener('click', () => {
      const index = parseInt(item.dataset.index)
      selectEpisode(index)
      episodePanel.style.display = 'none' // 选择后隐藏面板
    })
  })

  console.log('✅ 播放器内置剧集列表已显示')
}

// 播放器健康检查 - 定期检查播放状态，避免误报错误
let healthCheckInterval = null

const startPlayerHealthCheck = () => {
  // 清除之前的检查
  if (healthCheckInterval) {
    clearInterval(healthCheckInterval)
  }

  healthCheckInterval = setInterval(() => {
    if (!dplayer.value || !dplayer.value.video) {
      return
    }

    const video = dplayer.value.video

    // 检查视频是否真的有错误
    if (video.error) {
      console.log('健康检查：发现视频错误', video.error)
    } else if (video.readyState >= 2) { // HAVE_CURRENT_DATA
      // 视频数据可用，播放器健康
      console.log('健康检查：播放器状态正常')
    }
  }, 10000) // 每10秒检查一次，避免过于频繁
}

const stopPlayerHealthCheck = () => {
  if (healthCheckInterval) {
    clearInterval(healthCheckInterval)
    healthCheckInterval = null
  }
}





// DPlayer相关的事件处理已经在setupVideoPlayer中定义

// 设置播放器状态
const setPlayerStatus = (type, title, message) => {
  playerStatus.value = {
    type,
    title,
    message
  }
}



// 返回列表
const goBack = () => {
  router.push('/external')
}

// 重新加载
const retryLoad = () => {
  fetchPlayData()
}



// 组件挂载
onMounted(async () => {
  // 确保DPlayer和HLS.js加载完成
  await loadDPlayer()

  // 获取播放数据
  fetchPlayData()
})

// 加载DPlayer和相关依赖
const loadDPlayer = () => {
  return new Promise((resolve) => {
    if (window.DPlayer) {
      console.log('DPlayer已存在')
      resolve()
      return
    }

    console.log('开始加载DPlayer和依赖...')

    // 设置超时机制
    const timeout = setTimeout(() => {
      console.error('DPlayer加载超时')
      ElMessage.error('播放器加载超时，请检查网络连接')
      resolve()
    }, 30000) // 30秒超时

    // 加载DPlayer CSS
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://cdn.jsdelivr.net/npm/dplayer@1.27.1/dist/DPlayer.min.css'
    document.head.appendChild(link)

    // 先加载HLS.js
    const hlsScript = document.createElement('script')
    hlsScript.src = 'https://cdn.jsdelivr.net/npm/hls.js@1.4.12/dist/hls.min.js' // 使用稳定版本

    hlsScript.onload = () => {
      console.log('HLS.js加载完成，版本:', window.Hls ? window.Hls.version : '未知')

      // 再加载DPlayer
      const dplayerScript = document.createElement('script')
      dplayerScript.src = 'https://cdn.jsdelivr.net/npm/dplayer@1.27.1/dist/DPlayer.min.js'

      dplayerScript.onload = () => {
        clearTimeout(timeout)
        console.log('DPlayer加载完成')

        // 验证DPlayer是否正确加载
        if (window.DPlayer) {
          console.log('DPlayer验证成功')
          ElMessage.success('播放器组件加载完成')
        } else {
          console.error('DPlayer加载后验证失败')
          ElMessage.error('播放器组件验证失败')
        }
        resolve()
      }

      dplayerScript.onerror = (error) => {
        clearTimeout(timeout)
        console.error('DPlayer脚本加载失败:', error)
        ElMessage.error('播放器组件加载失败，请刷新页面重试')
        resolve()
      }

      document.head.appendChild(dplayerScript)
    }

    hlsScript.onerror = (error) => {
      clearTimeout(timeout)
      console.error('HLS.js脚本加载失败:', error)
      ElMessage.error('播放器依赖加载失败，请检查网络连接')
      resolve()
    }

    document.head.appendChild(hlsScript)
  })
}

// 组件卸载
onUnmounted(() => {
  // 停止健康检查
  stopPlayerHealthCheck()

  // 清理DPlayer实例
  if (dplayer.value) {
    dplayer.value.destroy()
    dplayer.value = null
  }
})
</script>

<style scoped>
.external-play-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: var(--theme-background); /* 使用主题背景色 */
  min-height: 100vh; /* 确保全屏覆盖 */
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

/* 头部区域 - 返回按钮和动漫标题 */
.header-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
}

.back-button {
  flex-shrink: 0;
}

/* 返回按钮主题样式 */
.header-section :deep(.el-button) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
}

.header-section :deep(.el-button:hover) {
  background: var(--theme-background-light) !important;
  border-color: var(--theme-primary) !important;
}

.anime-title {
  color: var(--theme-text-primary);
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  flex: 1;
}

.loading-section {
  text-align: center;
  padding: 50px;
}

.loading-text {
  margin-top: 20px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  font-size: 1.1rem;
}

.episode-selector {
  margin-top: 25px;
  margin-bottom: 20px;
  padding: 20px;
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 12px;
  box-shadow: var(--theme-shadow-light); /* 使用主题阴影 */
  border: 1px solid var(--theme-border); /* 使用主题边框色 */
}

.episode-selector h3 {
  margin: 0 0 18px 0;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
}



.episode-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding: 5px;
}

.episode-btn {
  min-width: 80px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.episode-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
}

/* 剧集按钮主题样式 */
.episode-selector :deep(.el-button) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  color: var(--theme-text-primary) !important;
}

.episode-selector :deep(.el-button:hover) {
  border-color: var(--theme-primary) !important;
  background: var(--theme-background-light) !important;
}

.episode-selector :deep(.el-button--primary) {
  background: var(--theme-gradient) !important;
  border: none !important;
  color: white !important;
}



.player-section {
  margin-bottom: 30px;
}

.proxy-toggle-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  margin-bottom: 10px;
  background: var(--theme-background);
  border-radius: 8px;
  border: 1px solid var(--theme-border);
  font-size: 13px;
}

.proxy-auto-hint {
  color: #e6a23c;
  font-weight: 500;
}

.proxy-status-badge {
  color: #67c23a;
  font-weight: 500;
  padding: 2px 8px;
  background: rgba(103, 194, 58, 0.1);
  border-radius: 4px;
}

.player-container {
  margin-bottom: 20px;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  position: relative; /* 为状态提示定位 */
}

.dplayer-container {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
}





@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* DPlayer自定义样式 */
.dplayer-container :deep(.dplayer-controller) {
  background: linear-gradient(0deg, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
}

.dplayer-container :deep(.dplayer-controller-mask) {
  background: linear-gradient(0deg, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
}

.dplayer-container :deep(.dplayer-icons-left .dplayer-icon),
.dplayer-container :deep(.dplayer-icons-right .dplayer-icon) {
  color: #fff;
  transition: all 0.3s ease;
}

.dplayer-container :deep(.dplayer-icons-left .dplayer-icon:hover),
.dplayer-container :deep(.dplayer-icons-right .dplayer-icon:hover) {
  color: #4EABE6; /* 使用新的蓝色 (78,171,230) */
  transform: scale(1.1);
}

.dplayer-container :deep(.dplayer-bar-wrap) {
  background: rgba(255, 255, 255, 0.2);
}

.dplayer-container :deep(.dplayer-played) {
  background: linear-gradient(90deg, #4EABE6 0%, #67C23A 100%); /* 使用新的蓝色 */
}

.dplayer-container :deep(.dplayer-thumb) {
  background: #4EABE6; /* 使用新的蓝色 (78,171,230) */
  box-shadow: 0 2px 8px rgba(78, 171, 230, 0.5);
}

.play-info {
  padding: 15px;
  background: var(--theme-background); /* 使用主题背景色 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
  border-radius: 8px;
  margin-bottom: 15px;
}

.info-item {
  margin-bottom: 10px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

.play-url-info {
  margin-top: 15px;
}

.play-controls {
  text-align: center;
  margin: 25px 0;
  padding: 20px;
  background: var(--theme-background); /* 使用主题背景色 */
  border: 1px solid var(--theme-border); /* 添加主题边框 */
  border-radius: 15px;
  box-shadow: var(--theme-shadow-light); /* 使用主题阴影 */
}

.control-btn {
  margin: 0 15px;
  padding: 12px 24px;
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.control-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.play-btn {
  background: linear-gradient(135deg, #4EABE6 0%, #67C23A 100%); /* 使用新的蓝色 */
  border: none;
  color: white;
  min-width: 120px;
}

.play-btn:hover {
  background: linear-gradient(135deg, #3A8BB8 0%, #529b2e 100%); /* 使用新的蓝色深色调 */
}

.prev-btn, .next-btn {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
  border: none;
  color: white;
  min-width: 100px;
}

.prev-btn:hover, .next-btn:hover {
  background: linear-gradient(135deg, #73767a 0%, #4c4d4f 100%);
}

.prev-btn:disabled, .next-btn:disabled {
  background: var(--theme-background-light); /* 使用主题浅色背景 */
  color: var(--theme-text-placeholder); /* 使用主题占位符文字颜色 */
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}



.play-tips {
  margin-top: 30px;
}

.play-tips ul {
  margin: 10px 0 0 20px;
  color: #666;
}

.play-tips li {
  margin-bottom: 5px;
}

.error-section {
  text-align: center;
  padding: 50px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .external-play-page {
    padding: 10px;
  }

  .episode-list {
    justify-content: center;
  }

  .play-controls .el-button {
    margin: 5px;
  }
}

/* 互动区域 */
.interaction-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
  background: var(--theme-background);
  border-radius: 12px;
  border: 1px solid var(--theme-border);
}

.rating-wrapper,
.comment-wrapper {
  width: 100%;
}

/* DPlayer内置剧集列表面板样式 - 主题化 */
:deep(.dplayer-episode-panel) {
  position: absolute;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  width: 320px;
  max-height: 400px;
  background: var(--theme-background) !important; /* 使用主题背景色 */
  border-radius: 12px; /* 增加圆角 */
  box-shadow: var(--theme-shadow) !important; /* 使用主题阴影 */
  z-index: 1000;
  backdrop-filter: blur(15px); /* 增强模糊效果 */
  border: 1px solid var(--theme-border) !important; /* 使用主题边框色 */
}

:deep(.dplayer-episode-panel-header) {
  display: flex;
  justify-content: center; /* 居中对齐 */
  align-items: center;
  padding: 15px 20px; /* 增加内边距 */
  border-bottom: 1px solid var(--theme-border) !important; /* 使用主题边框色 */
  background: var(--theme-gradient) !important; /* 使用主题渐变色作为头部背景 */
  border-radius: 12px 12px 0 0; /* 匹配面板圆角 */
}

:deep(.dplayer-episode-panel-title) {
  color: white !important; /* 确保在渐变背景上文字清晰 */
  font-size: 15px; /* 稍微增大字体 */
  font-weight: 600; /* 增加字重 */
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3); /* 添加文字阴影增强可读性 */
}



:deep(.dplayer-episode-panel-content) {
  max-height: 320px;
  overflow-y: auto;
  padding: 12px 16px; /* 增加内边距 */
  background: var(--theme-background) !important; /* 确保内容区域使用主题背景 */
  border-radius: 0 0 12px 12px; /* 添加下方圆角，匹配面板整体圆角 */
}

:deep(.dplayer-episode-item) {
  display: flex;
  align-items: center;
  justify-content: center; /* 居中对齐 */
  padding: 12px 14px; /* 增加内边距 */
  margin: 6px 0; /* 增加间距 */
  background: var(--theme-background-light) !important; /* 使用主题浅色背景 */
  border-radius: 8px; /* 增加圆角 */
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--theme-border) !important; /* 使用主题边框色 */
  color: var(--theme-text-primary) !important; /* 使用主题文字颜色 */
  position: relative; /* 为播放指示器定位 */
}

:deep(.dplayer-episode-item:hover) {
  background: var(--theme-background-hover) !important; /* 使用主题悬停背景色 */
  border-color: var(--theme-primary) !important; /* 悬停时使用主题主色 */
  transform: translateX(6px); /* 增加悬停位移效果 */
  box-shadow: var(--theme-shadow-light) !important; /* 添加悬停阴影 */
}

:deep(.dplayer-episode-item.active) {
  background: var(--theme-gradient) !important; /* 使用主题渐变色 */
  border-color: var(--theme-primary) !important; /* 使用主题主色 */
  box-shadow: var(--theme-shadow) !important; /* 使用主题阴影 */
  color: white !important; /* 激活状态文字为白色 */
  transform: translateX(8px); /* 激活状态更大的位移 */
}



:deep(.dplayer-episode-name) {
  flex: 1;
  font-size: 14px; /* 稍微增大字体 */
  color: var(--theme-text-primary) !important; /* 使用主题文字颜色 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500; /* 增加字重 */
  text-align: center; /* 剧集名称居中显示 */
}

/* 激活状态的剧集名称 */
:deep(.dplayer-episode-item.active .dplayer-episode-name) {
  color: white !important; /* 激活状态文字为白色 */
  font-weight: 600; /* 激活状态字重更大 */
}



@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 滚动条样式 - 主题化 */
:deep(.dplayer-episode-panel-content::-webkit-scrollbar) {
  width: 8px; /* 稍微增加宽度 */
}

:deep(.dplayer-episode-panel-content::-webkit-scrollbar-track) {
  background: var(--theme-background-light) !important; /* 使用主题浅色背景 */
  border-radius: 4px; /* 增加圆角 */
}

:deep(.dplayer-episode-panel-content::-webkit-scrollbar-thumb) {
  background: var(--theme-primary) !important; /* 使用主题主色 */
  border-radius: 4px; /* 增加圆角 */
  transition: background 0.3s ease; /* 添加过渡效果 */
}

:deep(.dplayer-episode-panel-content::-webkit-scrollbar-thumb:hover) {
  background: var(--theme-primary-dark) !important; /* 悬停时使用深色主题色 */
}

/* DPlayer内置剧集控制按钮样式 - 完全融入原生控制栏 */
/* 上一集/下一集按钮位于播放键左右两侧，剧集列表按钮位于右侧控制区域 */

/* DPlayer播放器控制条主题色 */
:deep(.dplayer-bar-wrap) {
  background: rgba(0, 0, 0, 0.6) !important;
}

/* 进度条背景 */
:deep(.dplayer-bar) {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 进度条已播放部分 - 使用主题色 */
:deep(.dplayer-played) {
  background: var(--theme-primary) !important;
}

/* 进度条缓冲部分 */
:deep(.dplayer-loaded) {
  background: rgba(255, 255, 255, 0.4) !important;
}

/* 进度条拖拽按钮 - 使用主题色 */
:deep(.dplayer-thumb) {
  background: var(--theme-primary) !important;
  border: 2px solid white !important;
}

/* 音量条背景 */
:deep(.dplayer-volume-bar) {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 音量条已设置部分 - 使用主题色 */
:deep(.dplayer-volume-bar-inner) {
  background: var(--theme-primary) !important;
}

/* 音量条拖拽按钮 - 使用主题色 */
:deep(.dplayer-volume-bar-inner .dplayer-thumb) {
  background: var(--theme-primary) !important;
}

/* 控制按钮悬停效果 */
:deep(.dplayer-icon:hover) {
  color: var(--theme-primary) !important;
}

/* 时间显示文字 */
:deep(.dplayer-ptime) {
  color: white !important;
}

/* 修复进度球和音量球的显示和对齐问题 */
/* 进度条悬停时才显示进度球 */
:deep(.dplayer-bar-wrap:hover .dplayer-thumb) {
  opacity: 1 !important;
  visibility: visible !important;
}

/* 默认隐藏进度球 */
:deep(.dplayer-thumb) {
  opacity: 0 !important;
  visibility: hidden !important;
  transition: opacity 0.2s ease !important;
}

/* 音量条悬停时才显示音量球 */
:deep(.dplayer-volume:hover .dplayer-volume-bar .dplayer-thumb) {
  opacity: 1 !important;
  visibility: visible !important;
}

/* 默认隐藏音量球 */
:deep(.dplayer-volume-bar .dplayer-thumb) {
  opacity: 0 !important;
  visibility: hidden !important;
  transition: opacity 0.2s ease !important;
}

/* 进度球垂直居中对齐 */
:deep(.dplayer-bar-wrap .dplayer-thumb) {
  top: 50% !important;
  transform: translateY(-50%) !important;
  margin-top: 0 !important;
}

/* 音量球垂直居中对齐 */
:deep(.dplayer-volume-bar .dplayer-thumb) {
  top: 50% !important;
  transform: translateY(-50%) !important;
  margin-top: 0 !important;
}
</style>
