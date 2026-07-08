<!-- 用户登录页面 -->
<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <!-- 标题 -->
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录您的动漫网站账户</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="usernameOrEmail">
            <el-input
              v-model="loginForm.usernameOrEmail"
              placeholder="用户名或邮箱"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-button"
              :loading="authStore.isLoading"
              @click="handleLogin"
            >
              {{ authStore.isLoading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 注册链接 -->
        <div class="register-link">
          <span>还没有账户？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { loginLimiter, sanitizeInput } from '@/utils/security'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref()

// 登录表单数据
const loginForm = reactive({
  usernameOrEmail: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  usernameOrEmail: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, max: 100, message: '长度在 3 到 100 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 验证表单
    await loginFormRef.value.validate()

    // 执行登录
    await authStore.login(loginForm)

    // 登录成功，跳转到首页或之前的页面
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)

  } catch (error) {
    console.error('登录处理失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--theme-gradient); /* 使用主题渐变背景 */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 12px;
  padding: 40px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0 0 10px 0;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  font-size: 28px;
  font-weight: 600;
}

.login-header p {
  margin: 0;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.register-link {
  text-align: center;
  margin-bottom: 20px;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  font-size: 14px;
}

.register-link span {
  margin-right: 8px;
}

/* 响应式设计 - 平板及移动端 */
@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  /* 卡片内边距过大，减小；max-width:400px 保留 */
  .login-card {
    padding: 28px 20px;
  }

  .login-header {
    margin-bottom: 20px;
  }

  .login-header h2 {
    font-size: 22px;
    margin-bottom: 8px;
  }

  .login-header p {
    font-size: 13px;
  }

  .login-form {
    margin-bottom: 16px;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 24px 16px;
  }

  .login-header h2 {
    font-size: 20px;
  }
}

/* Element Plus 输入框样式 - 适配暗色主题 */
:deep(.el-input__wrapper) {
  background-color: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
  transition: all 0.3s ease;
}

:deep(.el-input__inner) {
  color: var(--theme-text-primary) !important;
}

:deep(.el-input__inner::placeholder) {
  color: var(--theme-text-placeholder) !important;
}

:deep(.el-input__icon) {
  color: var(--theme-text-secondary) !important;
}

:deep(.el-input__wrapper:focus-within) {
  border-color: var(--theme-primary) !important;
  box-shadow: 0 0 0 1px var(--theme-primary) !important;
}

/* Element Plus 复选框样式 - 适配暗色主题 */
:deep(.el-checkbox__input) {
  background-color: var(--theme-background) !important;
  border: 1px solid var(--theme-border) !important;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--theme-primary) !important;
  border-color: var(--theme-primary) !important;
}

:deep(.el-checkbox__inner) {
  background-color: var(--theme-background) !important;
}

:deep(.el-checkbox__label) {
  color: var(--theme-text-regular) !important;
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
