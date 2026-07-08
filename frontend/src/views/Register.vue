<!-- 用户注册页面 -->
<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <!-- 标题 -->
        <div class="register-header">
          <h2>创建账户</h2>
          <p>加入我们的动漫社区</p>
        </div>

        <!-- 注册表单 -->
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
              clearable
              @blur="checkUsernameAvailability"
            />
            <div v-if="usernameCheckResult" class="field-hint">
              <span :class="usernameCheckResult.available ? 'success' : 'error'">
                {{ usernameCheckResult.message }}
              </span>
            </div>
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              type="email"
              placeholder="邮箱地址"
              size="large"
              :prefix-icon="Message"
              clearable
              @blur="checkEmailAvailability"
            />
            <div v-if="emailCheckResult" class="field-hint">
              <span :class="emailCheckResult.available ? 'success' : 'error'">
                {{ emailCheckResult.message }}
              </span>
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item prop="emailCode">
            <div class="email-code-container">
              <el-input
                v-model="registerForm.emailCode"
                placeholder="邮箱验证码"
                size="large"
                :prefix-icon="Message"
                clearable
                maxlength="6"
              />
              <el-button
                type="primary"
                size="large"
                class="send-code-btn"
                :loading="sendingCode"
                :disabled="!canSendCode"
                @click="sendEmailCode"
              >
                {{ sendCodeButtonText }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称（可选）"
              size="large"
              :prefix-icon="Avatar"
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="register-button"
              :loading="authStore.isLoading"
              @click="handleRegister"
            >
              {{ authStore.isLoading ? '注册中...' : '注册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 登录链接 -->
        <div class="login-link">
          <span>已有账户？</span>
          <el-link type="primary" @click="goToLogin">立即登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Avatar } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authAPI } from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const registerFormRef = ref()

// 注册表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  emailCode: '',
  nickname: ''
})

// 验证码相关状态
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 字段检查结果
const usernameCheckResult = ref(null)
const emailCheckResult = ref(null)

// 计算属性
const canSendCode = computed(() => {
  return registerForm.email &&
         /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email) &&
         countdown.value === 0 &&
         !sendingCode.value
})

const sendCodeButtonText = computed(() => {
  if (sendingCode.value) return '发送中...'
  if (countdown.value > 0) return `${countdown.value}s后重发`
  return '发送验证码'
})

// 自定义验证器
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ]
}

// 检查用户名可用性
const checkUsernameAvailability = async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameCheckResult.value = null
    return
  }

  try {
    const available = await authStore.checkUsername(registerForm.username)
    usernameCheckResult.value = {
      available,
      message: available ? '✓ 用户名可用' : '✗ 用户名已被使用'
    }
  } catch (error) {
    usernameCheckResult.value = {
      available: false,
      message: '检查失败，请稍后重试'
    }
  }
}

// 检查邮箱可用性
const checkEmailAvailability = async () => {
  if (!registerForm.email || !/\S+@\S+\.\S+/.test(registerForm.email)) {
    emailCheckResult.value = null
    return
  }

  try {
    const available = await authStore.checkEmail(registerForm.email)
    emailCheckResult.value = {
      available,
      message: available ? '✓ 邮箱可用' : '✗ 邮箱已被注册'
    }
  } catch (error) {
    emailCheckResult.value = {
      available: false,
      message: '检查失败，请稍后重试'
    }
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    // 验证表单
    await registerFormRef.value.validate()

    // 检查用户名和邮箱可用性
    if (usernameCheckResult.value && !usernameCheckResult.value.available) {
      ElMessage.error('用户名不可用')
      return
    }

    if (emailCheckResult.value && !emailCheckResult.value.available) {
      ElMessage.error('邮箱不可用')
      return
    }

    // 执行注册
    await authStore.register(registerForm)

    // 注册成功，跳转到首页
    router.push('/')

  } catch (error) {
    console.error('注册处理失败:', error)
    if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }

  sendingCode.value = true

  try {
    await authAPI.sendVerificationCode({
      email: registerForm.email,
      type: 'register'
    })

    ElMessage.success('验证码已发送到您的邮箱')

    // 开始倒计时
    startCountdown()

  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}

// 组件卸载时清理定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: var(--theme-gradient); /* 使用主题渐变背景 */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
  background: var(--theme-background); /* 使用主题背景色 */
  border-radius: 12px;
  padding: 40px;
  box-shadow: var(--theme-shadow); /* 使用主题阴影 */
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0 0 10px 0;
  color: var(--theme-text-primary); /* 使用主题文字颜色 */
  font-size: 28px;
  font-weight: 600;
}

.register-header p {
  margin: 0;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.email-code-container {
  display: flex;
  gap: 12px;
}

.email-code-container .el-input {
  flex: 1;
}

.send-code-btn {
  min-width: 120px;
  white-space: nowrap;
}

.field-hint {
  margin-top: 5px;
  font-size: 12px;
}

.field-hint .success {
  color: #67c23a;
}

.field-hint .error {
  color: #f56c6c;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-link {
  text-align: center;
  color: var(--theme-text-regular); /* 使用主题常规文字颜色 */
  font-size: 14px;
}

.login-link span {
  margin-right: 8px;
}

/* 响应式设计 - 平板及移动端 */
@media (max-width: 768px) {
  .register-page {
    padding: 16px;
  }

  .register-card {
    padding: 28px 20px;
  }

  .register-header {
    margin-bottom: 20px;
  }

  .register-header h2 {
    font-size: 22px;
    margin-bottom: 8px;
  }

  .register-header p {
    font-size: 13px;
  }

  .register-form {
    margin-bottom: 16px;
  }

  /* 验证码输入 + 发送按钮横向排列，确保按钮可收缩不溢出 */
  .email-code-container {
    gap: 8px;
  }

  .send-code-btn {
    min-width: 100px;
    flex-shrink: 0;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-card {
    padding: 24px 16px;
  }

  .register-header h2 {
    font-size: 20px;
  }

  /* 极窄屏按钮文案缩短空间，进一步收紧 */
  .send-code-btn {
    min-width: 88px;
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

/* 字段提示样式 */
.field-hint .success {
  color: var(--theme-success) !important;
}

.field-hint .error {
  color: var(--theme-danger) !important;
}
</style>
