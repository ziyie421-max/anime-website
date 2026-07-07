<template>
  <div class="social-login">
    <div class="divider">
      <span class="divider-text">或使用以下方式登录</span>
    </div>
    
    <div class="social-buttons">
      <el-button 
        class="social-btn github-btn"
        @click="handleSocialLogin('github')"
        :loading="loading.github"
      >
        <svg class="social-icon" viewBox="0 0 24 24">
          <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
        </svg>
        GitHub
      </el-button>
      
      <el-button 
        class="social-btn google-btn"
        @click="handleSocialLogin('google')"
        :loading="loading.google"
      >
        <svg class="social-icon" viewBox="0 0 24 24">
          <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
          <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
          <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
          <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
        </svg>
        Google
      </el-button>
      
      <el-button 
        class="social-btn qq-btn"
        @click="handleSocialLogin('qq')"
        :loading="loading.qq"
      >
        <svg class="social-icon" viewBox="0 0 24 24">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.16 1.04-.8 1.52-1.44 1.68-.64.16-1.36-.16-1.6-.8-.24-.64.16-1.36.8-1.6.64-.24 1.36.16 1.6.8.08.24.08.48-.36.92zm-9.28 0c-.44-.44-.44-.68-.36-.92.24-.64.96-.96 1.6-.8.64.24 1.04.96.8 1.6-.24.64-.96.96-1.6.8-.64-.16-1.28-.64-1.44-1.68zm4.64 9.2c-2.24 0-4.16-1.44-4.8-3.36.8.48 2.24.8 4.8.8s4-.32 4.8-.8c-.64 1.92-2.56 3.36-4.8 3.36z" fill="#12B7F5"/>
        </svg>
        QQ
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const loading = reactive({
  github: false,
  google: false,
  qq: false
})

const handleSocialLogin = async (provider) => {
  loading[provider] = true
  
  try {
    // 这里实现社交登录逻辑
    switch (provider) {
      case 'github':
        await handleGitHubLogin()
        break
      case 'google':
        await handleGoogleLogin()
        break
      case 'qq':
        await handleQQLogin()
        break
    }
  } catch (error) {
    console.error(`${provider} 登录失败:`, error)
    ElMessage.error(`${provider} 登录失败`)
  } finally {
    loading[provider] = false
  }
}

const handleGitHubLogin = async () => {
  // GitHub OAuth 登录
  const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID
  const redirectUri = `${window.location.origin}/auth/github/callback`
  const scope = 'user:email'
  
  const authUrl = `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}`
  
  // 打开新窗口进行授权
  const popup = window.open(authUrl, 'github-login', 'width=600,height=600')
  
  // 监听授权结果
  const checkClosed = setInterval(() => {
    if (popup.closed) {
      clearInterval(checkClosed)
      // 检查登录状态
      authStore.checkAuthStatus()
    }
  }, 1000)
}

const handleGoogleLogin = async () => {
  // Google OAuth 登录
  ElMessage.info('Google 登录功能开发中...')
}

const handleQQLogin = async () => {
  // QQ 登录
  ElMessage.info('QQ 登录功能开发中...')
}
</script>

<style scoped>
.social-login {
  margin-top: 24px;
}

.divider {
  position: relative;
  text-align: center;
  margin: 24px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: #e4e7ed;
}

.divider-text {
  background-color: #fff;
  padding: 0 16px;
  color: #909399;
  font-size: 14px;
}

.social-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.social-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.social-icon {
  width: 20px;
  height: 20px;
}

.github-btn {
  background-color: #24292e;
  border-color: #24292e;
  color: white;
}

.github-btn:hover {
  background-color: #1a1e22;
  border-color: #1a1e22;
}

.google-btn {
  background-color: white;
  border-color: #dadce0;
  color: #3c4043;
}

.google-btn:hover {
  background-color: #f8f9fa;
  border-color: #dadce0;
}

.qq-btn {
  background-color: #12B7F5;
  border-color: #12B7F5;
  color: white;
}

.qq-btn:hover {
  background-color: #0ea5e9;
  border-color: #0ea5e9;
}

@media (max-width: 768px) {
  .social-buttons {
    flex-direction: column;
  }
  
  .social-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
