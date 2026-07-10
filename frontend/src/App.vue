<!-- 主应用组件 - 包含导航栏和路由出口 -->
<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { Search, User, Setting, VideoPlay, Close, Sunny, Moon, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { externalAPI } from '@/services/api'
// 导入动漫筛选工具
import { filterAnimeResponse } from '@/utils/animeFilter'
// 导入favicon工具
import { updateFavicon } from '@/utils/favicon.js'
// 导入通知API
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const authStore = useAuthStore()

// 搜索相关数据
const searchKeyword = ref('')
const searchResults = ref([])
const isSearching = ref(false)
const showSearchResults = ref(false)
const isSearchExpanded = ref(false) // 搜索框是否展开（默认隐藏，点击搜索按钮后展开）

// 主题切换
const isDarkTheme = ref(false)

// 通知相关
const unreadNotificationCount = ref(0)
let notificationTimer = null

// 计算属性
const isLoggedIn = computed(() => authStore.isLoggedIn)
const userInfo = computed(() => authStore.user)

// 获取未读通知数量
const fetchUnreadCount = async () => {
  if (!isLoggedIn.value) {
    unreadNotificationCount.value = 0
    return
  }
  try {
    const response = await getUnreadCount()
    if (response.data.success) {
      unreadNotificationCount.value = response.data.data.count
    }
  } catch (error) {
    console.error('获取未读通知数量失败:', error)
  }
}

// 启动通知轮询
const startNotificationPolling = () => {
  fetchUnreadCount()
  // 每60秒检查一次未读通知
  notificationTimer = setInterval(fetchUnreadCount, 60000)
}

// 停止通知轮询
const stopNotificationPolling = () => {
  if (notificationTimer) {
    clearInterval(notificationTimer)
    notificationTimer = null
  }
}

// 监听登录状态变化
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    startNotificationPolling()
  } else {
    stopNotificationPolling()
    unreadNotificationCount.value = 0
  }
})

// 实时搜索功能 - 输入时自动搜索
const performRealTimeSearch = async (keyword) => {
  if (!keyword || keyword.length < 2) {
    searchResults.value = []
    showSearchResults.value = false
    return
  }

  try {
    isSearching.value = true
    console.log('搜索开始，关键词:', keyword)

    // 调用量子资源API搜索
    const response = await externalAPI.searchAnime(keyword, 1)
    console.log('🔍 搜索API原始响应:', response)

    if (response && response.code === 1 && response.list) {
      // 应用动漫内容筛选
      const filteredResponse = filterAnimeResponse(response)
      console.log('🔍 筛选后响应:', filteredResponse)

      searchResults.value = (filteredResponse.list || []).slice(0, 8) // 只显示前8个结果
      showSearchResults.value = true
      console.log('🔍 搜索成功，筛选后找到', searchResults.value.length, '个结果')
    } else {
      searchResults.value = []
      showSearchResults.value = keyword.length >= 2 // 只有输入了内容才显示"未找到"
    }
  } catch (error) {
    console.error('🔍 搜索失败:', error)
    searchResults.value = []
    showSearchResults.value = false
  } finally {
    isSearching.value = false
  }
}

// 防抖搜索 - 避免频繁API调用
let searchTimeout = null
const debouncedSearch = (keyword) => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    performRealTimeSearch(keyword)
  }, 300) // 300ms延迟
}

// 处理搜索按钮点击或回车 - 跳转到搜索结果页面
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  // 关闭搜索结果下拉框
  closeSearchResults()

  // 跳转到搜索结果页面
  router.push(`/search?search=${encodeURIComponent(keyword)}`)
  console.log('🔍 跳转到搜索结果页面，关键词:', keyword)
}

// 关闭搜索结果
const closeSearchResults = () => {
  showSearchResults.value = false
  searchKeyword.value = ''
  searchResults.value = []
}

// 切换搜索框展开/收起
const toggleSearch = () => {
  isSearchExpanded.value = !isSearchExpanded.value
  if (isSearchExpanded.value) {
    // 展开后自动聚焦输入框
    setTimeout(() => {
      const input = document.querySelector('.search-box .search-input .el-input__inner')
      input && input.focus()
    }, 100)
  }
}

// 查看动漫详情
const viewAnimeDetail = (anime) => {
  console.log('查看动漫详情:', anime.vod_name)
  // 关闭搜索结果
  closeSearchResults()
  // 跳转到动漫详情页面
  router.push({
    name: 'NewAnimeDetail',
    params: { id: anime.vod_id }
  })
}

// 点击外部关闭搜索结果
const handleClickOutside = (event) => {
  const searchBox = event.target.closest('.search-box')
  if (!searchBox && showSearchResults.value) {
    closeSearchResults()
  }
}

// 组件挂载时添加全局点击事件监听
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

// 监听搜索关键词变化，实现实时搜索
watch(searchKeyword, (newKeyword) => {
  console.log('🔍 搜索关键词变化:', newKeyword)
  debouncedSearch(newKeyword.trim())
}, { immediate: false })

// 组件卸载时移除事件监听和清理定时器
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  // 停止通知轮询
  stopNotificationPolling()
})

// 用户登出
const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}

// 主题切换功能
const toggleTheme = async () => {
  isDarkTheme.value = !isDarkTheme.value
  document.body.className = isDarkTheme.value ? 'theme-dark' : ''

  // 更新Element Plus主题变量
  const root = document.documentElement
  if (isDarkTheme.value) {
    // 暗色粉色主题 (234,122,153)
    root.style.setProperty('--el-color-primary', '#EA7A99')
    root.style.setProperty('--el-color-primary-light-3', '#F09BB3')
    root.style.setProperty('--el-color-primary-dark-2', '#D65A7F')
    root.style.setProperty('--el-bg-color', '#1a1a1a')
    root.style.setProperty('--el-bg-color-page', '#2d2d2d')
    root.style.setProperty('--el-text-color-primary', '#c8c8c8')
    root.style.setProperty('--el-text-color-regular', '#b8b8b8')
    root.style.setProperty('--el-border-color', '#4a4a4a')
  } else {
    // 蓝白色主题 (78,171,230)
    root.style.setProperty('--el-color-primary', '#4EABE6')
    root.style.setProperty('--el-color-primary-light-3', '#7BC2ED')
    root.style.setProperty('--el-color-primary-dark-2', '#3A8BB8')
    root.style.setProperty('--el-bg-color', '#ffffff')
    root.style.setProperty('--el-bg-color-page', '#f8f9fa')
    root.style.setProperty('--el-text-color-primary', '#303133')
    root.style.setProperty('--el-text-color-regular', '#606266')
    root.style.setProperty('--el-border-color', '#dcdfe6')
  }

  localStorage.setItem('theme', isDarkTheme.value ? 'dark' : 'light')

  // 主题切换后立即更新favicon
  try {
    await updateFavicon(isDarkTheme.value)
  } catch (error) {
    console.error('❌ 主题切换时favicon更新失败:', error)
  }

  ElMessage.success(`已切换到${isDarkTheme.value ? '暗色粉色' : '蓝白色'}主题`)
}

// 初始化主题
const initTheme = async () => {
  const savedTheme = localStorage.getItem('theme')
  const root = document.documentElement

  if (savedTheme === 'dark') {
    isDarkTheme.value = true
    document.body.className = 'theme-dark'

    // 设置暗色粉色主题的Element Plus变量
    root.style.setProperty('--el-color-primary', '#EA7A99')
    root.style.setProperty('--el-color-primary-light-3', '#F09BB3')
    root.style.setProperty('--el-color-primary-dark-2', '#D65A7F')
    root.style.setProperty('--el-bg-color', '#1a1a1a')
    root.style.setProperty('--el-bg-color-page', '#2d2d2d')
    root.style.setProperty('--el-text-color-primary', '#c8c8c8')
    root.style.setProperty('--el-text-color-regular', '#b8b8b8')
    root.style.setProperty('--el-border-color', '#4a4a4a')
  } else {
    isDarkTheme.value = false
    document.body.className = ''

    // 设置蓝白色主题的Element Plus变量
    root.style.setProperty('--el-color-primary', '#4EABE6')
    root.style.setProperty('--el-color-primary-light-3', '#7BC2ED')
    root.style.setProperty('--el-color-primary-dark-2', '#3A8BB8')
    root.style.setProperty('--el-bg-color', '#ffffff')
    root.style.setProperty('--el-bg-color-page', '#f8f9fa')
    root.style.setProperty('--el-text-color-primary', '#303133')
    root.style.setProperty('--el-text-color-regular', '#606266')
    root.style.setProperty('--el-border-color', '#dcdfe6')
  }

  // 主题设置完成后，立即更新favicon
  try {
    await updateFavicon(isDarkTheme.value)
    console.log('🎨 主题初始化时favicon已同步更新')
  } catch (error) {
    console.error('❌ 主题初始化时favicon更新失败:', error)
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user')
      break
    case 'favorites':
      router.push('/user/favorites')
      break
    case 'history':
      router.push('/user/history')
      break
    case 'notifications':
      router.push('/user/notifications')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 导航菜单 - 已移除日漫/国漫/美漫分类入口，用户可通过点击 Logo 回到首页
const menuItems = []

// 初始化认证状态和主题
onMounted(async () => {
  await authStore.initAuth()
  await initTheme() // 等待主题初始化完成（包括favicon更新）
  // 如果已登录，启动通知轮询
  if (isLoggedIn.value) {
    startNotificationPolling()
  }
})
</script>

<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <h1>AnimeS~</h1>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            active-class="active"
          >
            {{ item.name }}
          </router-link>
        </nav>

        <!-- 搜索框：默认只显示搜索按钮(和主题/登录挨在一起)，点击后输入框原地展开 -->
        <div class="search-box" :class="{ 'search-expanded': isSearchExpanded }">
          <div class="search-input-wrapper">
            <el-input
              v-model="searchKeyword"
              placeholder="输入动漫名称..."
              @keyup.enter="handleSearch"
              class="search-input"
              clearable
            >
              <template #suffix>
                <!-- 关闭按钮在搜索框右侧 -->
                <el-icon
                  @click="toggleSearch"
                  class="search-close-icon"
                  title="收起搜索"
                >
                  <Close />
                </el-icon>
              </template>
            </el-input>
          </div>
          <!-- 搜索触发按钮：位于最右侧，和主题/登录按钮挨在一起 -->
          <el-button
            @click="toggleSearch"
            circle
            size="default"
            :icon="Search"
            class="search-toggle-btn"
            title="搜索"
          />

          <!-- 搜索结果下拉框（PC/移动端统一：展开后才显示） -->
          <div v-if="showSearchResults && searchKeyword.trim() && isSearchExpanded" class="search-results-dropdown">
            <div class="search-results-header">
              <span v-if="isSearching">正在搜索...</span>
              <span v-else>搜索结果 ({{ searchResults.length }})</span>
              <el-icon @click="closeSearchResults" class="close-icon">
                <Close />
              </el-icon>
            </div>

            <div v-if="!isSearching && searchResults.length === 0" class="no-results">
              <p>未找到相关动漫</p>
              <p class="no-results-tip">试试其他关键词吧</p>
            </div>

            <div v-else class="search-results-list">
              <div
                v-for="anime in searchResults"
                :key="anime.vod_id"
                class="search-result-item"
                @click="viewAnimeDetail(anime)"
              >
                <div class="result-poster">
                  <img
                    v-if="anime.vod_pic"
                    :src="anime.vod_pic"
                    :alt="anime.vod_name"
                    @error="$event.target.style.display='none'"
                  />
                  <div v-else class="placeholder-poster">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                </div>
                <div class="result-info">
                  <h4 class="result-title">{{ anime.vod_name }}</h4>
                  <p class="result-meta">
                    <span v-if="anime.vod_year">{{ anime.vod_year }}年</span>
                    <span v-if="anime.type_name">{{ anime.type_name }}</span>
                  </p>
                  <p class="result-desc" v-if="anime.vod_blurb">{{ anime.vod_blurb }}</p>
                </div>
              </div>
            </div>

            <div v-if="searchResults.length > 0" class="search-results-footer">
              <el-button
                text
                type="primary"
                @click="handleSearch"
                :disabled="isSearching"
              >
                查看完整搜索结果 ({{ searchResults.length }}+)
              </el-button>
            </div>
          </div>
        </div>

        <!-- 主题切换按钮 -->
        <div class="theme-toggle">
          <el-button
            @click="toggleTheme"
            :icon="isDarkTheme ? Sunny : Moon"
            circle
            size="default"
            class="theme-btn"
            :title="isDarkTheme ? '切换到蓝白色主题' : '切换到暗粉色主题'"
          />
        </div>

        <!-- 用户区域 -->
        <div class="user-area">
          <template v-if="isLoggedIn">
            <!-- 通知铃铛 -->
            <div class="notification-bell" @click="$router.push('/user/notifications')">
              <el-badge :value="unreadNotificationCount" :hidden="unreadNotificationCount === 0" :max="99">
                <el-icon :size="22"><Bell /></el-icon>
              </el-badge>
            </div>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userInfo?.avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="username">{{ authStore.userDisplayName }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="history">观看历史</el-dropdown-item>
                  <el-dropdown-item command="notifications">我的通知</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button
              @click="$router.push('/login')"
              circle
              size="default"
              :icon="User"
              class="theme-btn"
              title="登录"
            />
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="footer-container">
        <p>&copy; AnimeS~. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 全局样式 */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏样式 - 使用纯正蓝色 */
.header {
  background: var(--theme-primary);
  box-shadow: var(--theme-shadow);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  width: 100%;
  padding: 0 40px;
  display: flex;
  align-items: center;
  height: 70px;
  box-sizing: border-box;
}

.logo {
  cursor: pointer;
  margin-left: 48px; /* 往右挪一点，不再紧贴屏幕左边缘 */
  margin-right: 24px;
  flex-shrink: 0;
}

.logo h1 {
  margin: 0;
  font-size: 1.8rem;
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.nav-menu {
  display: flex;
  gap: 40px;
  margin-right: 0;
}

.nav-item {
  text-decoration: none;
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
  font-weight: 600;
  font-size: 1rem;
  padding: 10px 20px;
  border-radius: 25px;
  transition: all 0.3s ease;
  position: relative;
  white-space: nowrap; /* 防止文字换行导致竖立显示 */
  display: inline-block; /* 确保正常水平显示 */
  writing-mode: horizontal-tb; /* 强制水平书写模式 */
}

.nav-item:hover {
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  transform: translateY(-2px);
}

.nav-item.active {
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
}

.search-box {
  margin-left: auto; /* 靠右，和主题/登录按钮挨在一起 */
  margin-right: 20px; /* 和主题按钮之间留出间距，与主题-登录按钮间距一致 */
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 搜索展开/收起按钮：和主题/登录按钮同风格正圆 */
.search-toggle-btn {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: var(--theme-nav-text) !important;
  border-radius: 50% !important;
  padding: 0 !important;
  width: 38px !important;
  height: 38px !important;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.search-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: scale(1.1);
}

/* 输入框容器：默认折叠，展开时滑出 */
.search-input-wrapper {
  overflow: hidden;
  max-width: 0;
  opacity: 0;
  transition: max-width 0.3s ease, opacity 0.2s ease;
  flex-shrink: 0;
}

.search-box.search-expanded .search-input-wrapper {
  max-width: 350px;
  opacity: 1;
}

.search-input {
  width: 350px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.2); /* 稍微增加背景透明度 */
  border: none !important; /* 完全去掉边框 */
  backdrop-filter: blur(15px); /* 增强毛玻璃效果 */
  border-radius: 25px;
  box-shadow: none !important; /* 去掉突兀阴影 */
  transition: all 0.3s ease; /* 添加过渡动画 */
}

.search-input :deep(.el-input__inner) {
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
}

/* 去掉输入框选中/聚焦时的彩色边框（图2问题） */
.search-input :deep(.el-input__inner:focus) {
  outline: none !important;
  box-shadow: none !important;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: var(--theme-nav-text-secondary); /* 使用导航栏专用次要颜色 */
}

/* 搜索框悬停时的样式：只变背景，不加阴影 */
.search-input :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.25) !important; /* 悬停时背景稍微变亮 */
}

/* 搜索框聚焦时的样式：去掉彩色方框/光晕，只保留背景变化 */
.search-input :deep(.el-input__wrapper:focus-within) {
  background: rgba(255, 255, 255, 0.3) !important; /* 聚焦时背景更亮 */
  box-shadow: none !important; /* 去掉聚焦时的彩色方框 */
  outline: none !important;
}

.search-icon {
  cursor: pointer;
  color: var(--theme-nav-text-secondary); /* 使用导航栏专用次要颜色 */
  transition: color 0.3s ease;
}

.search-icon:hover {
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
}

/* 搜索框内关闭按钮（右侧 ✕） */
.search-close-icon {
  cursor: pointer;
  color: var(--theme-nav-text-secondary);
  transition: color 0.3s ease;
}

.search-close-icon:hover {
  color: var(--theme-nav-text);
}

.search-icon.searching {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 搜索结果下拉框样式 */
.search-results-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 2000;
  max-height: 500px;
  overflow: hidden;
  margin-top: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  font-weight: 600;
  color: #333;
  min-height: 50px;
}

.search-results-header span {
  display: flex;
  align-items: center;
}

.search-results-header span:first-child::before {
  content: '';
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #667eea;
  margin-right: 8px;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.close-icon {
  cursor: pointer;
  color: #666;
  transition: color 0.3s ease;
}

.close-icon:hover {
  color: #333;
}

.no-results {
  padding: 40px 20px;
  text-align: center;
  color: #666;
}

.no-results p {
  margin: 0 0 8px 0;
}

.no-results-tip {
  font-size: 0.9rem;
  color: #999;
}

.search-results-list {
  max-height: 350px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  padding: 15px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.search-result-item:hover {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  transform: translateX(5px);
}

.search-result-item:last-child {
  border-bottom: none;
}

.result-poster {
  width: 60px;
  height: 80px;
  flex-shrink: 0;
  margin-right: 15px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--theme-gradient);
}

.result-poster img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-poster {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-meta {
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 8px 0;
}

.result-meta span {
  margin-right: 10px;
}

.result-desc {
  font-size: 0.8rem;
  color: #888;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-results-footer {
  padding: 15px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  text-align: center;
}

/* 主题切换按钮样式 */
.theme-toggle {
  margin-right: 20px;
}

.theme-btn {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: var(--theme-nav-text) !important; /* 使用导航栏专用颜色 */
  border-radius: 50% !important; /* 主题/登录按钮统一纯圆形 */
  padding: 0 !important; /* 覆盖 user-area 的横向 padding，防止圆形被撑成椭圆 */
  /* ElPlus 自带 .el-button.is-circle{width:32px}，特异性相同但源顺序靠后会覆盖我的 width，
     导致按钮渲染成 32×38 椭圆。用 !important 锁死宽高相等，保证正圆 */
  width: 38px !important;
  height: 38px !important;
  transition: all 0.3s ease;
}

.theme-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: scale(1.1);
}

/* 通知铃铛样式 */
.notification-bell {
  cursor: pointer;
  padding: 10px; /* 增加内边距，确保圆形 */
  border-radius: 50%;
  transition: all 0.3s ease;
  color: var(--theme-nav-text);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px; /* 固定宽度 */
  height: 42px; /* 固定高度 */
}

.notification-bell:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.notification-bell :deep(.el-badge__content) {
  background-color: #ff4757;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-area :deep(.el-button:not(.is-circle)) {
  border-radius: 20px;
  font-weight: 600;
  padding: 10px 20px;
}

.user-area :deep(.el-button:not(.el-button--primary)) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
  backdrop-filter: blur(10px);
}

.user-area :deep(.el-button:not(.el-button--primary):hover) {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
}



.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 25px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.username {
  font-weight: 600;
  color: var(--theme-nav-text); /* 使用导航栏专用颜色 */
  font-size: 0.95rem;
}

/* 主内容区域 */
.main-content {
  flex: 1;
}

/* 底部样式 */
.footer {
  background: var(--theme-background-mute);
  padding: 40px 0;
  text-align: center;
  color: var(--theme-text-secondary);
  margin-top: auto;
  border-top: 1px solid var(--theme-border);
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

.footer p {
  margin: 0;
  font-size: 0.95rem;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-container {
    padding: 0 30px;
  }

  .search-input {
    width: 280px;
  }
}

@media (max-width: 768px) {
  /* 单行紧凑导航：logo + 搜索框(占满中间) + 主题 + 用户区 */
  .header-container {
    height: 54px;
    padding: 0 12px;
    gap: 8px;
  }

  .logo {
    margin-right: 0;
    flex-shrink: 0;
  }

  .logo h1 {
    font-size: 1.2rem;
  }

  .nav-menu {
    display: none;
  }

  /* 搜索框占满中间剩余空间；完整清掉 PC 端的 margin:auto，
     避免残留在 flex:1 上把布局撑乱；justify-content 让折叠按钮靠右 */
  .search-box {
    flex: 1;
    margin: 0;
    min-width: 0;
    justify-content: flex-end;
    position: relative; /* 作为移动端输入框绝对定位的参考 */
  }

  /* 移动端 logo 左距改小，避免偏左 */
  .logo {
    margin-left: 12px;
  }

  /* 移动端搜索框展开时：绝对定位脱离 flex 流悬浮在按钮左侧，
     往左展开，不挤压 logo；未折叠时隐藏 */
  .search-input-wrapper {
    position: absolute;
    right: 46px; /* 贴近搜索按钮(38px)左侧，留 8px gap */
    top: 50%;
    transform: translateY(-50%);
    max-width: 0;
    opacity: 0;
    overflow: hidden;
    transition: max-width 0.3s ease, opacity 0.2s ease;
    z-index: 10;
  }

  .search-box.search-expanded .search-input-wrapper {
    max-width: 200px;
    opacity: 1;
  }

  .search-input {
    width: 200px;
  }

  /* 搜索结果下拉框在窄屏下保证最小可读宽度 */
  .search-results-dropdown {
    min-width: 260px;
    right: auto;
  }

  .theme-toggle {
    margin-right: 0;
    flex-shrink: 0;
  }

  .user-area {
    gap: 6px;
    flex-shrink: 0;
  }

  .user-area :deep(.el-button) {
    padding: 6px 12px;
    font-size: 0.85rem;
  }

  /* 移动端隐藏用户名，只保留头像 */
  .username {
    display: none;
  }

  .notification-bell {
    width: 34px;
    height: 34px;
    padding: 6px;
  }

  .user-info {
    padding: 4px 6px;
    gap: 4px;
  }
}
</style>
