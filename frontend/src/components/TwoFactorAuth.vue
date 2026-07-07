<template>
  <div class="two-factor-auth">
    <div class="auth-header">
      <el-icon class="auth-icon"><Lock /></el-icon>
      <h3>两步验证</h3>
      <p>请输入您手机上收到的验证码</p>
    </div>

    <el-form 
      ref="codeFormRef"
      :model="codeForm"
      :rules="codeRules"
      class="code-form"
    >
      <el-form-item prop="code">
        <div class="code-input-container">
          <el-input
            v-for="(digit, index) in codeDigits"
            :key="index"
            v-model="codeDigits[index]"
            class="code-digit"
            maxlength="1"
            @input="handleDigitInput(index, $event)"
            @keydown="handleKeyDown(index, $event)"
            @paste="handlePaste"
            :ref="el => setDigitRef(el, index)"
          />
        </div>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          size="large"
          class="verify-button"
          :loading="loading"
          :disabled="!isCodeComplete"
          @click="handleVerify"
        >
          {{ loading ? '验证中...' : '验证' }}
        </el-button>
      </el-form-item>
    </el-form>

    <div class="resend-section">
      <span v-if="countdown > 0" class="countdown-text">
        {{ countdown }}秒后可重新发送
      </span>
      <el-button
        v-else
        type="text"
        @click="handleResend"
        :loading="resendLoading"
      >
        重新发送验证码
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'

const props = defineProps({
  phone: {
    type: String,
    required: true
  },
  onVerify: {
    type: Function,
    required: true
  },
  onResend: {
    type: Function,
    required: true
  }
})

const codeFormRef = ref()
const digitRefs = ref([])
const loading = ref(false)
const resendLoading = ref(false)
const countdown = ref(60)
let countdownTimer = null

// 验证码数字
const codeDigits = reactive(['', '', '', '', '', ''])

// 表单数据
const codeForm = reactive({
  code: ''
})

// 验证规则
const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须是6位数字', trigger: 'blur' }
  ]
}

// 验证码是否完整
const isCodeComplete = computed(() => {
  return codeDigits.every(digit => digit !== '') && codeDigits.join('').length === 6
})

// 设置数字输入框引用
const setDigitRef = (el, index) => {
  if (el) {
    digitRefs.value[index] = el
  }
}

// 处理数字输入
const handleDigitInput = (index, value) => {
  // 只允许数字
  const numericValue = value.replace(/\D/g, '')
  codeDigits[index] = numericValue

  // 更新完整验证码
  codeForm.code = codeDigits.join('')

  // 自动跳转到下一个输入框
  if (numericValue && index < 5) {
    digitRefs.value[index + 1]?.focus()
  }

  // 如果验证码完整，自动验证
  if (isCodeComplete.value) {
    handleVerify()
  }
}

// 处理键盘事件
const handleKeyDown = (index, event) => {
  if (event.key === 'Backspace' && !codeDigits[index] && index > 0) {
    // 退格键：如果当前输入框为空，跳转到前一个输入框
    digitRefs.value[index - 1]?.focus()
  } else if (event.key === 'ArrowLeft' && index > 0) {
    // 左箭头：跳转到前一个输入框
    digitRefs.value[index - 1]?.focus()
  } else if (event.key === 'ArrowRight' && index < 5) {
    // 右箭头：跳转到下一个输入框
    digitRefs.value[index + 1]?.focus()
  }
}

// 处理粘贴事件
const handlePaste = (event) => {
  event.preventDefault()
  const pastedData = event.clipboardData.getData('text')
  const numericData = pastedData.replace(/\D/g, '').slice(0, 6)
  
  if (numericData.length === 6) {
    for (let i = 0; i < 6; i++) {
      codeDigits[i] = numericData[i] || ''
    }
    codeForm.code = numericData
    
    // 自动验证
    if (isCodeComplete.value) {
      handleVerify()
    }
  }
}

// 验证验证码
const handleVerify = async () => {
  if (!isCodeComplete.value) {
    ElMessage.warning('请输入完整的验证码')
    return
  }

  loading.value = true
  
  try {
    await props.onVerify(codeForm.code)
  } catch (error) {
    console.error('验证失败:', error)
    // 清空验证码
    codeDigits.fill('')
    codeForm.code = ''
    digitRefs.value[0]?.focus()
  } finally {
    loading.value = false
  }
}

// 重新发送验证码
const handleResend = async () => {
  resendLoading.value = true
  
  try {
    await props.onResend()
    startCountdown()
    ElMessage.success('验证码已重新发送')
  } catch (error) {
    console.error('重新发送失败:', error)
    ElMessage.error('重新发送失败，请稍后重试')
  } finally {
    resendLoading.value = false
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

onMounted(() => {
  // 自动聚焦第一个输入框
  digitRefs.value[0]?.focus()
  // 开始倒计时
  startCountdown()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.two-factor-auth {
  max-width: 400px;
  margin: 0 auto;
  padding: 32px;
  text-align: center;
}

.auth-header {
  margin-bottom: 32px;
}

.auth-icon {
  font-size: 48px;
  color: var(--el-color-primary);
  margin-bottom: 16px;
}

.auth-header h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.auth-header p {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.code-input-container {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 24px;
}

.code-digit {
  width: 48px;
  height: 48px;
}

.code-digit :deep(.el-input__inner) {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  border-radius: 8px;
}

.verify-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.resend-section {
  margin-top: 24px;
}

.countdown-text {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

@media (max-width: 480px) {
  .code-input-container {
    gap: 8px;
  }
  
  .code-digit {
    width: 40px;
    height: 40px;
  }
  
  .code-digit :deep(.el-input__inner) {
    font-size: 18px;
  }
}
</style>
