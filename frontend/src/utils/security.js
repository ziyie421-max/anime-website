/**
 * 安全工具函数
 */

// 密码强度检查
export function checkPasswordStrength(password) {
  const checks = {
    length: password.length >= 8,
    lowercase: /[a-z]/.test(password),
    uppercase: /[A-Z]/.test(password),
    number: /\d/.test(password),
    special: /[!@#$%^&*(),.?":{}|<>]/.test(password)
  }
  
  const score = Object.values(checks).filter(Boolean).length
  
  let strength = 'weak'
  let message = '密码强度：弱'
  let color = '#f56c6c'
  
  if (score >= 4) {
    strength = 'strong'
    message = '密码强度：强'
    color = '#67c23a'
  } else if (score >= 3) {
    strength = 'medium'
    message = '密码强度：中等'
    color = '#e6a23c'
  }
  
  return {
    strength,
    message,
    color,
    score,
    checks
  }
}

// 防止暴力破解 - 登录尝试限制
export class LoginAttemptLimiter {
  constructor(maxAttempts = 5, lockoutTime = 15 * 60 * 1000) { // 15分钟
    this.maxAttempts = maxAttempts
    this.lockoutTime = lockoutTime
    this.attempts = new Map()
  }
  
  // 记录失败尝试
  recordFailedAttempt(identifier) {
    const now = Date.now()
    const key = identifier.toLowerCase()
    
    if (!this.attempts.has(key)) {
      this.attempts.set(key, { count: 0, lastAttempt: now })
    }
    
    const record = this.attempts.get(key)
    
    // 如果超过锁定时间，重置计数
    if (now - record.lastAttempt > this.lockoutTime) {
      record.count = 0
    }
    
    record.count++
    record.lastAttempt = now
  }
  
  // 检查是否被锁定
  isLocked(identifier) {
    const key = identifier.toLowerCase()
    const record = this.attempts.get(key)
    
    if (!record) return false
    
    const now = Date.now()
    
    // 如果超过锁定时间，解除锁定
    if (now - record.lastAttempt > this.lockoutTime) {
      this.attempts.delete(key)
      return false
    }
    
    return record.count >= this.maxAttempts
  }
  
  // 获取剩余锁定时间
  getRemainingLockTime(identifier) {
    const key = identifier.toLowerCase()
    const record = this.attempts.get(key)
    
    if (!record || record.count < this.maxAttempts) return 0
    
    const elapsed = Date.now() - record.lastAttempt
    return Math.max(0, this.lockoutTime - elapsed)
  }
  
  // 清除记录（登录成功时调用）
  clearAttempts(identifier) {
    const key = identifier.toLowerCase()
    this.attempts.delete(key)
  }
}

// 创建全局实例
export const loginLimiter = new LoginAttemptLimiter()

// 输入验证和清理
export function sanitizeInput(input) {
  if (typeof input !== 'string') return ''
  
  return input
    .trim()
    .replace(/[<>]/g, '') // 移除潜在的XSS字符
    .substring(0, 1000) // 限制长度
}

// 邮箱格式验证
export function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 用户名格式验证
export function validateUsername(username) {
  // 只允许字母、数字、下划线，3-20位
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/
  return usernameRegex.test(username)
}
