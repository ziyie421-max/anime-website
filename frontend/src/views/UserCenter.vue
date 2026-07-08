<!-- 用户中心页面 - 用户个人信息管理 -->
<template>
  <div class="user-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>个人中心</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 用户信息卡片 -->
    <div v-else class="content-container">
      <!-- 用户基本信息 -->
      <el-card class="user-card">
        <div class="user-header">
          <el-avatar :size="80" :src="userProfile?.avatar">
            <el-icon :size="40"><User /></el-icon>
          </el-avatar>
          <div class="user-info">
            <h2>{{ userProfile?.nickname || userProfile?.username }}</h2>
            <p class="email">{{ userProfile?.email }}</p>
            <p class="bio">{{ userProfile?.bio || '这个人很懒，什么都没写~' }}</p>
          </div>
          <el-button type="primary" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon> 编辑资料
          </el-button>
        </div>

        <!-- 统计数据和操作区 -->
        <div class="stats-row">
          <div class="stat-item clickable" @click="$router.push('/user/favorites')">
            <span class="stat-value">{{ userProfile?.favoriteCount || 0 }}</span>
            <span class="stat-label">收藏</span>
          </div>
          <div class="stat-item clickable" @click="$router.push('/user/history')">
            <span class="stat-value">{{ userProfile?.historyCount || 0 }}</span>
            <span class="stat-label">观看历史</span>
          </div>
          <div class="stat-item clickable" @click="showPasswordDialog = true">
            <el-icon class="stat-icon"><Lock /></el-icon>
            <span class="stat-label">修改密码</span>
          </div>
        </div>
      </el-card>

      <!-- 最近观看 -->
      <el-card v-if="recentWatched.length > 0" class="recent-watched">
        <template #header>
          <div class="card-header">
            <span>最近观看</span>
            <el-button type="primary" plain @click="$router.push('/user/history')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <div class="anime-list">
          <div
            v-for="item in recentWatched"
            :key="item.id"
            class="anime-item"
            @click="goToAnime(item)"
          >
            <el-image :src="item.externalAnimeCover || item.animeCover" fit="cover" class="anime-cover">
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="anime-info">
              <h4>{{ item.externalAnimeTitle || item.animeTitle }}</h4>
              <p>{{ item.externalEpisodeName || '第' + item.episodeNumber + '集' }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑资料" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatar" placeholder="请输入头像图片URL" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己吧"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="updating">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.currentPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="updating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Star, Clock, Lock, ArrowRight, Picture } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, changePassword, getRecentWatched } from '@/api/user'

const router = useRouter()

// 响应式数据
const loading = ref(true)
const updating = ref(false)
const userProfile = ref(null)
const recentWatched = ref([])
const showEditDialog = ref(false)
const showPasswordDialog = ref(false)

// 编辑表单
const editForm = reactive({
  nickname: '',
  avatar: '',
  bio: ''
})

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 获取用户资料
const fetchUserProfile = async () => {
  try {
    loading.value = true
    const response = await getUserProfile()
    userProfile.value = response.data?.data || response.data

    // 初始化编辑表单
    editForm.nickname = userProfile.value?.nickname || ''
    editForm.avatar = userProfile.value?.avatar || ''
    editForm.bio = userProfile.value?.bio || ''
  } catch (error) {
    console.error('获取用户资料失败:', error)
    ElMessage.error('获取用户资料失败')
  } finally {
    loading.value = false
  }
}

// 获取最近观看
const fetchRecentWatched = async () => {
  try {
    const response = await getRecentWatched(5)
    recentWatched.value = response.data?.data || response.data || []
  } catch (error) {
    console.error('获取最近观看失败:', error)
  }
}

// 更新用户资料
const handleUpdateProfile = async () => {
  try {
    updating.value = true
    await updateUserProfile(editForm)
    ElMessage.success('资料更新成功')
    showEditDialog.value = false
    fetchUserProfile()
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新资料失败')
  } finally {
    updating.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('新密码长度至少6位')
    return
  }

  try {
    updating.value = true
    await changePassword(passwordForm)
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    // 重置表单
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.response?.data?.message || '修改密码失败')
  } finally {
    updating.value = false
  }
}

// 跳转到动漫详情
const goToAnime = (item) => {
  if (item.externalAnimeId) {
    router.push(`/anime/${item.externalAnimeId}`)
  } else if (item.animeId) {
    router.push(`/anime/${item.animeId}`)
  }
}

// 初始化
onMounted(() => {
  fetchUserProfile()
  fetchRecentWatched()
})
</script>

<style scoped>
.user-center {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

.loading-container {
  padding: 40px;
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 用户卡片 */
.user-card {
  background: var(--theme-background); /* 使用主题背景色 */
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
}

.user-info {
  flex: 1;
}

.user-info h2 {
  margin: 0 0 5px 0;
  font-size: 20px;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

.user-info .email {
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
  font-size: 14px;
  margin: 0 0 5px 0;
}

.user-info .bio {
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  font-size: 14px;
  margin: 0;
}

/* 统计数据和操作区 */
.stats-row {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--theme-border); /* 使用主题边框 */
}

.stat-item {
  text-align: center;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

/* 可点击的统计项 */
.stat-item.clickable {
  cursor: pointer;
}

/* 悬停时使用主题色背景 */
.stat-item.clickable:hover {
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  transform: translateY(-2px);
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: var(--theme-primary); /* 使用主题主色 */
}

/* 修改密码按钮的图标样式 */
.stat-icon {
  font-size: 24px;
  color: var(--theme-primary); /* 使用主题主色 */
}

.stat-label {
  font-size: 14px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

/* 最近观看 - 适配主题 */
.recent-watched {
  background: var(--theme-background); /* 使用主题背景色 */
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.recent-watched :deep(.el-card__header) {
  background: var(--theme-background); /* 卡片头部背景 */
  border-bottom: 1px solid var(--theme-border); /* 卡片头部边框 */
}

.recent-watched :deep(.el-card__body) {
  background: var(--theme-background); /* 卡片内容区域背景 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 修复查看全部按钮颜色 - 使用 plain 类型按钮 */
.card-header :deep(.el-button--primary.is-plain) {
  color: var(--theme-primary) !important;
  background-color: transparent !important;
  border-color: var(--theme-primary) !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 8px 16px !important;
  transition: all 0.3s ease !important;
}

/* 确保按钮内的所有元素都有颜色 */
.card-header :deep(.el-button--primary.is-plain *),
.card-header :deep(.el-button--primary.is-plain .el-text),
.card-header :deep(.el-button--primary.is-plain span),
.card-header :deep(.el-button--primary.is-plain .el-icon) {
  color: var(--theme-primary) !important;
}

.card-header :deep(.el-button--primary.is-plain:hover) {
  background-color: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

/* 悬停时确保按钮内的所有元素都变白色 */
.card-header :deep(.el-button--primary.is-plain:hover *),
.card-header :deep(.el-button--primary.is-plain:hover .el-text),
.card-header :deep(.el-button--primary.is-plain:hover span),
.card-header :deep(.el-button--primary.is-plain:hover .el-icon) {
  color: white !important;
}

.anime-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

/* 动漫卡片 - 适配主题背景色 */
.anime-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
  transition: transform 0.3s;
  border: 1px solid var(--theme-border); /* 使用主题边框 */
}

.anime-item:hover {
  transform: translateY(-5px);
}

.anime-cover {
  width: 100%;
  height: 200px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--theme-background-mute); /* 使用主题 muted 背景 */
  color: var(--theme-text-placeholder); /* 使用主题占位符颜色 */
}

.anime-info {
  padding: 10px;
  background: var(--theme-background-soft); /* 使用主题浅色背景 */
}

.anime-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
}

.anime-info p {
  margin: 0;
  font-size: 12px;
  color: var(--theme-text-secondary); /* 使用主题次要文字颜色 */
}

/* 响应式 */
@media (max-width: 768px) {
  .user-center {
    padding: 12px;
  }

  .page-header h1 {
    font-size: 20px;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
    gap: 14px;
  }

  .stats-row {
    flex-wrap: wrap;
    gap: 10px;
    justify-content: space-between;
  }

  .stat-item {
    flex: 1 1 30%;
    padding: 10px 8px;
  }

  .stat-value {
    font-size: 20px;
  }

  .anime-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .anime-cover {
    height: 160px;
  }

  /* 对话框移动端自适应宽度，避免固定 450px 溢出 */
  :deep(.el-dialog) {
    width: 92% !important;
    max-width: 450px;
  }
}

/* Element Plus 对话框样式 - 适配暗色主题 */
:deep(.el-dialog) {
  background: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
}

:deep(.el-dialog__title) {
  color: var(--theme-text-primary) !important;
}

:deep(.el-dialog__body) {
  color: var(--theme-text-regular) !important;
}

/* Element Plus 输入框样式 - 适配暗色主题 */
:deep(.el-input__wrapper) {
  background-color: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
}

:deep(.el-input__inner) {
  color: var(--theme-text-primary) !important;
}

:deep(.el-input__inner::placeholder) {
  color: var(--theme-text-placeholder) !important;
}

/* Element Plus 按钮样式 */
:deep(.el-button--primary) {
  background-color: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

:deep(.el-button--primary:hover) {
  background-color: var(--theme-primary-light) !important;
  border-color: var(--theme-primary-light) !important;
}

/* Element Plus 链接样式 */
:deep(.el-link.el-link--primary) {
  color: var(--theme-primary) !important;
}

:deep(.el-link.el-link--primary:hover) {
  color: var(--theme-primary-light) !important;
}
</style>
