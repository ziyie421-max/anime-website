// 路由配置文件 - 定义应用的所有路由
import { createRouter, createWebHashHistory } from 'vue-router'

// 导入页面组件
const Home = () => import('../views/Home.vue')
const NewAnimeDetail = () => import('../views/NewAnimeDetail.vue')
const VideoPlayer = () => import('../views/VideoPlayer.vue')
const UserCenter = () => import('../views/UserCenter.vue')
const UserFavorites = () => import('../views/UserFavorites.vue')  // 用户收藏页面
const UserHistory = () => import('../views/UserHistory.vue')      // 用户观看历史页面
const UserNotifications = () => import('../views/UserNotifications.vue')  // 用户通知页面
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const AdminDashboard = () => import('../views/admin/Dashboard.vue')
const ApiTest = () => import('../views/ApiTest.vue')
const SimpleAnimeDetail = () => import('../views/SimpleAnimeDetail.vue')
// 使用静态导入来避免动态导入问题
import SearchResults from '../views/SearchResults.vue'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: 'AnimeS~' }
  },

  {
    path: '/search',
    name: 'SearchResults',
    component: SearchResults,
    meta: { title: '搜索结果' }
  },
  {
    path: '/anime/:id',
    name: 'NewAnimeDetail',
    component: NewAnimeDetail,
    meta: { title: '动漫详情' }
  },
  {
    path: '/watch/:id/:episode?',
    name: 'VideoPlayer',
    component: VideoPlayer,
    meta: { title: '观看视频' }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: UserCenter,
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    // 用户收藏页面路由
    path: '/user/favorites',
    name: 'UserFavorites',
    component: UserFavorites,
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    // 用户观看历史页面路由
    path: '/user/history',
    name: 'UserHistory',
    component: UserHistory,
    meta: { title: '观看历史', requiresAuth: true }
  },
  {
    // 用户通知页面路由
    path: '/user/notifications',
    name: 'UserNotifications',
    component: UserNotifications,
    meta: { title: '我的通知', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', requiresGuest: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/api-test',
    name: 'ApiTest',
    component: ApiTest,
    meta: { title: 'API测试' }
  },
  {
    path: '/external',
    name: 'ExternalAnime',
    component: () => import('@/views/ExternalAnime.vue'),
    meta: { title: '在线动漫' }
  },
  {
    path: '/external/play',
    name: 'ExternalPlay',
    component: () => import('@/views/ExternalPlay.vue'),
    meta: { title: '在线播放' }
  },
  // 新增三个独立的动漫分类页面
  {
    path: '/japanese-anime',
    name: 'JapaneseAnime',
    component: () => import('@/views/JapaneseAnime.vue'),
    meta: { title: '日本动漫' }
  },
  {
    path: '/chinese-anime',
    name: 'ChineseAnime',
    component: () => import('@/views/ChineseAnime.vue'),
    meta: { title: '国产动漫' }
  },
  {
    path: '/western-anime',
    name: 'WesternAnime',
    component: () => import('@/views/WesternAnime.vue'),
    meta: { title: '欧美动漫' }
  },
  {
    path: '/simple-anime/:id',
    name: 'SimpleAnimeDetail',
    component: SimpleAnimeDetail,
    meta: { title: '简化动漫详情' }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫 - 认证和权限控制
router.beforeEach(async (to, from, next) => {
  // 设置页面标题 - 首页显示AnimeS~，其他页面显示AnimeS~  页面名称
  if (to.meta.title === 'AnimeS~') {
    document.title = 'AnimeS~'
  } else {
    document.title = to.meta.title ? `AnimeS~  ${to.meta.title}` : 'AnimeS~'
  }

  // 动态导入认证store（避免循环依赖）
  const { useAuthStore } = await import('@/stores/auth')
  const authStore = useAuthStore()

  // 初始化认证状态（仅在首次访问时）
  if (!authStore.user && authStore.accessToken) {
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      console.warn('初始化用户信息失败:', error)
    }
  }

  // 检查需要认证的路由
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      // 未登录，重定向到登录页面
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 检查管理员权限
    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      // 非管理员，重定向到首页
      next('/')
      return
    }
  }

  // 检查访客专用路由（如登录、注册页面）
  if (to.meta.requiresGuest && authStore.isLoggedIn) {
    // 已登录用户访问登录/注册页面，重定向到首页
    next('/')
    return
  }

  next()
})

export default router
