<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useTokenStore } from '../stores/token'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'

const form = ref({ username: '', password: '' })
const loading = ref(false)
const router = useRouter()
const tokenStore = useTokenStore()

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  try {
    loading.value = true
    const params = new URLSearchParams()
    params.append('username', form.value.username)
    params.append('password', form.value.password)
    const res = await request.post('/user/login', params)
    tokenStore.setToken(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 错误提示在拦截器中已处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-background">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-section">
            <div class="logo-icon"></div>
            <h1 class="logo-title">BigEvent</h1>
          </div>
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录您的账户以继续</p>
        </div>

        <el-form 
          label-position="top" 
          size="large"
          class="login-form">
          <el-form-item label="用户名">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              class="form-input"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              show-password 
              :prefix-icon="Lock"
              class="form-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              class="login-button" 
              @click="handleLogin">
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <p>还没有账户？<span class="link">请联系管理员</span></p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  padding: 20px;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;

  &.circle-1 {
    width: 300px;
    height: 300px;
    top: -100px;
    left: -100px;
    animation-delay: 0s;
  }

  &.circle-2 {
    width: 200px;
    height: 200px;
    bottom: -50px;
    right: -50px;
    animation-delay: 2s;
  }

  &.circle-3 {
    width: 150px;
    height: 150px;
    top: 50%;
    right: 10%;
    animation-delay: 4s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 450px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.6s ease-out;

  [data-theme="dark"] & {
    background: rgba(15, 23, 42, 0.95);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.logo-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.login-subtitle {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;

  :deep(.el-form-item__label) {
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 8px;
  }
}

.form-input {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &.is-focus {
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
  margin-top: 8px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--el-text-color-secondary);

  .link {
    color: var(--el-color-primary);
    cursor: pointer;
    font-weight: 500;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .login-card {
    padding: 32px 24px;
    border-radius: 20px;
  }

  .login-title {
    font-size: 24px;
  }

  .logo-title {
    font-size: 28px;
  }
}
</style>
