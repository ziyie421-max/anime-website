<template>
  <div class="password-strength-meter">
    <div class="strength-bar">
      <div 
        class="strength-fill" 
        :style="{ width: `${(score / 5) * 100}%`, backgroundColor: color }"
      ></div>
    </div>
    <div class="strength-text" :style="{ color }">
      {{ message }}
    </div>
    <div class="strength-tips" v-if="showTips && score < 4">
      <p class="tips-title">密码强度建议：</p>
      <ul class="tips-list">
        <li v-if="!checks.length" :class="{ completed: checks.length }">
          <el-icon><Check v-if="checks.length" /><Close v-else /></el-icon>
          至少8个字符
        </li>
        <li v-if="!checks.lowercase" :class="{ completed: checks.lowercase }">
          <el-icon><Check v-if="checks.lowercase" /><Close v-else /></el-icon>
          包含小写字母
        </li>
        <li v-if="!checks.uppercase" :class="{ completed: checks.uppercase }">
          <el-icon><Check v-if="checks.uppercase" /><Close v-else /></el-icon>
          包含大写字母
        </li>
        <li v-if="!checks.number" :class="{ completed: checks.number }">
          <el-icon><Check v-if="checks.number" /><Close v-else /></el-icon>
          包含数字
        </li>
        <li v-if="!checks.special" :class="{ completed: checks.special }">
          <el-icon><Check v-if="checks.special" /><Close v-else /></el-icon>
          包含特殊字符
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Check, Close } from '@element-plus/icons-vue'
import { checkPasswordStrength } from '@/utils/security'

const props = defineProps({
  password: {
    type: String,
    default: ''
  },
  showTips: {
    type: Boolean,
    default: true
  }
})

const strengthInfo = computed(() => {
  if (!props.password) {
    return {
      strength: 'weak',
      message: '',
      color: '#dcdfe6',
      score: 0,
      checks: {}
    }
  }
  return checkPasswordStrength(props.password)
})

const { strength, message, color, score, checks } = strengthInfo.value
</script>

<style scoped>
.password-strength-meter {
  margin-top: 8px;
}

.strength-bar {
  width: 100%;
  height: 4px;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-text {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

.strength-tips {
  margin-top: 8px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #e6a23c;
}

.tips-title {
  font-size: 12px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #606266;
}

.tips-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.tips-list li {
  display: flex;
  align-items: center;
  font-size: 12px;
  margin: 2px 0;
  color: #909399;
}

.tips-list li.completed {
  color: #67c23a;
}

.tips-list li .el-icon {
  margin-right: 4px;
  font-size: 14px;
}
</style>
