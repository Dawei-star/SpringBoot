<!-- 增强版登录页面组件 -->
<template>
  <div class="enhanced-login-container">
    <!-- 背景动画 -->
    <div class="background-animation">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="circle circle-4"></div>
    </div>
    
    <!-- 粒子效果 -->
    <div class="particles" id="particles"></div>
    
    <el-row class="login-page">
      <el-col :span="12" class="bg">
        <div class="login-brand">
          <div class="brand-logo">
            <img src="@/assets/logo2.png" alt="Logo" />
          </div>
          <div class="brand-content">
            <h1 class="brand-title">BigEvent</h1>
            <p class="brand-subtitle">现代化博客管理系统</p>
            <div class="brand-features">
              <div class="feature-item">
                <el-icon><Check /></el-icon>
                <span>简洁高效的文章管理</span>
              </div>
              <div class="feature-item">
                <el-icon><Check /></el-icon>
                <span>强大的分类系统</span>
              </div>
              <div class="feature-item">
                <el-icon><Check /></el-icon>
                <span>实时留言互动</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6" :offset="3" class="form">
        <div class="form-container">
          <!-- 注册表单 -->
          <el-form 
            ref="formRef"
            size="large" 
            autocomplete="on" 
            v-if="isRegister" 
            :model="registerData" 
            :rules="rules"
            class="enhanced-form">
            
            <div class="form-header">
              <div class="form-icon">
                <el-icon><UserFilled /></el-icon>
              </div>
              <h2>创建账户</h2>
              <p>欢迎加入我们，开始您的博客之旅</p>
            </div>
            
            <el-form-item prop="username">
              <el-input 
                :prefix-icon="User" 
                placeholder="请输入用户名" 
                v-model="registerData.username"
                class="enhanced-input">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                :prefix-icon="Lock" 
                type="password" 
                placeholder="请输入密码"
                v-model="registerData.password"
                class="enhanced-input"
                show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="rePassword">
              <el-input 
                :prefix-icon="Lock" 
                type="password" 
                placeholder="请再次输入密码"
                v-model="registerData.rePassword"
                class="enhanced-input"
                show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <!-- 注册按钮 -->
            <el-form-item>
              <el-button 
                class="enhanced-button" 
                type="primary" 
                @click="handleRegister"
                :loading="loading">
                <span>创建账户</span>
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-form-item>
            
            <el-form-item class="form-footer">
              <span>已有账户？</span>
              <el-link type="primary" :underline="false" @click="switchToLogin">
                立即登录
              </el-link>
            </el-form-item>
          </el-form>

          <!-- 登录表单 -->
          <el-form 
            ref="formRef"
            size="large" 
            autocomplete="off" 
            v-else 
            :model="registerData" 
            :rules="rules"
            class="enhanced-form">
            
            <div class="form-header">
              <div class="form-icon">
                <el-icon><Avatar /></el-icon>
              </div>
              <h2>欢迎回来</h2>
              <p>请登录您的账户以继续</p>
            </div>
            
            <el-form-item prop="username">
              <el-input 
                :prefix-icon="User" 
                placeholder="请输入用户名" 
                v-model="registerData.username"
                class="enhanced-input">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                name="password" 
                :prefix-icon="Lock" 
                type="password" 
                placeholder="请输入密码"
                v-model="registerData.password"
                class="enhanced-input"
                show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item class="form-options">
              <div class="remember-me">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              </div>
              <el-link type="primary" :underline="false" @click="handleForgotPassword">
                忘记密码？
              </el-link>
            </el-form-item>
            
            <!-- 登录按钮 -->
            <el-form-item>
              <el-button 
                class="enhanced-button" 
                type="primary" 
                @click="handleLogin"
                :loading="loading">
                <span>登录</span>
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-form-item>
            
            <el-form-item class="form-footer">
              <span>还没有账户？</span>
              <el-link type="primary" :underline="false" @click="switchToRegister">
                立即注册
              </el-link>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { 
  User, 
  Lock, 
  UserFilled, 
  Avatar, 
  ArrowRight, 
  Check,
  Key,
  Iphone
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { userLoginService, userRegisterService } from '@/api/user'

// 状态管理
const router = useRouter()
const tokenStore = useTokenStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const isRegister = ref(false)

// 表单数据
const registerData = reactive({
  username: '',
  password: '',
  rePassword: ''
})

// 表单验证规则
const checkRePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== registerData.password) {
    callback(new Error('两次密码不一样'))
  } else {
    callback()
  }
}

// 登录表单验证规则（不包含rePassword）
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 16, message: '长度为5-16位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 16, message: '长度为5-16位', trigger: 'blur' },
  ]
}

// 密码强度验证器
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  
  if (value.length < 8 || value.length > 20) {
    callback(new Error('密码长度为8-20位'))
    return
  }
  
  const hasUpper = /[A-Z]/.test(value)
  const hasLower = /[a-z]/.test(value)
  const hasNumber = /\d/.test(value)
  const hasSpecial = /[@$!%*?&]/.test(value)
  
  if (!hasUpper || !hasLower || !hasNumber || !hasSpecial) {
    callback(new Error('密码必须包含大小写字母、数字和特殊字符(@$!%*?&)'))
    return
  }
  
  callback()
}

// 注册表单验证规则（包含rePassword）
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 16, message: '长度为5-16位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' },
  ],
  rePassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: checkRePassword, trigger: 'blur' },
  ]
}

// 计算属性：根据当前模式返回对应的验证规则
const rules = computed(() => {
  return isRegister.value ? registerRules : loginRules
})

// 方法
/**
 * 登录处理函数
 * 重构后的版本：逻辑清晰，错误处理完善
 */
const handleLogin = async () => {
  // 1. 表单验证
  try {
  const valid = await formRef.value.validate()
    if (!valid) {
      return
    }
  } catch (validateError) {
    console.error('[Login] 表单验证失败:', validateError)
    return
  }

  // 2. 准备登录参数
  const loginParams = {
    username: registerData.username.trim(),
    password: registerData.password,
    rememberMe: rememberMe.value
  }

  // 3. 开始登录流程
    loading.value = true
  
  try {
    // 4. 调用登录接口
    let result
    try {
      result = await userLoginService(loginParams)
    } catch (serviceError) {
      // 如果服务调用抛出异常（如网络错误、500错误等），直接处理
      await animateError()
      const errorMsg = serviceError?.message 
        || serviceError?.response?.data?.message 
        || '登录失败，请稍后重试'
      ElMessage.error(errorMsg)
      console.error('[Login] 登录服务调用失败:', serviceError)
      return
    }
    
    // 5. 验证响应格式
    if (!result || typeof result !== 'object') {
      await animateError()
      ElMessage.error('登录响应格式错误')
      console.error('[Login] 响应格式错误:', result)
      return
    }

    // 6. 严格检查：必须是成功状态码（200-299）且有有效的token
    const isSuccess = result.code >= 200 && result.code < 300
    const hasToken = result.data && typeof result.data === 'string' && result.data.trim().length > 0
    
    // 开发环境日志
    if (import.meta.env.DEV) {
      console.log('[Login] 登录响应检查:', {
        code: result.code,
        isSuccess,
        hasToken,
        message: result.message,
        dataType: typeof result.data,
        dataLength: result.data?.length || 0
      })
    }
    
    if (!isSuccess || !hasToken) {
      // 登录失败：状态码错误或没有token
      await animateError()
      const errorMsg = result.message || '登录失败，请检查用户名和密码'
      ElMessage.error(errorMsg)
      console.error('[Login] 登录失败:', { 
        code: result.code, 
        message: result.message, 
        hasToken,
        data: result.data 
      })
      return
    }

    // 7. 保存token（同步操作，立即生效）
    const token = result.data.trim()
    tokenStore.setToken(token)
    
    // 8. 验证token是否保存成功
    const savedToken = tokenStore.token || tokenStore.cleanToken
    if (!savedToken || savedToken.trim() !== token) {
      await animateError()
      ElMessage.error('Token保存失败，请重试')
      console.error('[Login] Token保存失败:', { 
        expected: token.substring(0, 20) + '...', 
        actual: savedToken?.substring(0, 20) + '...' 
      })
      return
    }

    // 9. 保存"记住我"信息
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('username', registerData.username)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('username')
    }

    // 10. 显示成功消息和动画
    await animateSuccess()
    ElMessage.success(result.message || '登录成功')

    // 11. 等待动画完成，然后跳转
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 12. 跳转到首页
    try {
      await router.replace('/home')
    } catch (routerError) {
      console.error('[Login] 路由跳转失败:', routerError)
      // 如果跳转失败，尝试强制刷新页面
      window.location.href = '/home'
    }
    
  } catch (error) {
    // 处理所有错误情况
    await animateError()
    
    let errorMsg = '登录失败，请稍后重试'
    
    if (error?.response) {
      // HTTP错误响应
      const status = error.response.status
      if (status >= 500) {
        errorMsg = error.response.data?.message || '服务器错误，请稍后重试'
      } else if (status === 401) {
        errorMsg = error.response.data?.message || '用户名或密码错误'
      } else if (status === 400) {
        errorMsg = error.response.data?.message || '请求参数错误'
      } else {
        errorMsg = error.response.data?.message || error.message || errorMsg
      }
    } else if (error?.message) {
      // 其他错误
      errorMsg = error.message
    }
    
    ElMessage.error(errorMsg)
    console.error('[Login] 登录异常:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  
  try {
    loading.value = true
    const result = await userRegisterService(registerData)
    
    // 成功状态码范围：200-299
    if (result && result.code >= 200 && result.code < 300) {
    // 添加成功动画
    await animateSuccess()
    
    ElMessage.success(result.message || '注册成功')
    
      // 切换到登录页面
      isRegister.value = false
      clearRegisterData()
    } else {
      await animateError()
      ElMessage.error(result?.message || '注册失败')
    }
  } catch (error) {
    // 添加错误动画
    await animateError()
    ElMessage.error(error?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const switchToRegister = () => {
  isRegister.value = true
  clearForm()
  animateFormSwitch()
}

const switchToLogin = () => {
  isRegister.value = false
  clearForm()
  animateFormSwitch()
}

const clearForm = () => {
  registerData.username = ''
  registerData.password = ''
  registerData.rePassword = ''
  formRef.value?.clearValidate()
}

const handleForgotPassword = () => {
  ElMessage.info('密码重置功能开发中...')
}

// 动画效果
const animateSuccess = async () => {
  const formElement = formRef.value.$el
  formElement.classList.add('form-success')
  await new Promise(resolve => setTimeout(resolve, 500))
  formElement.classList.remove('form-success')
}

const animateError = async () => {
  const formElement = formRef.value.$el
  formElement.classList.add('form-error')
  await new Promise(resolve => setTimeout(resolve, 500))
  formElement.classList.remove('form-error')
}

const animateFormSwitch = async () => {
  await nextTick()
  const formElement = formRef.value.$el
  formElement.classList.add('form-switch')
  await new Promise(resolve => setTimeout(resolve, 300))
  formElement.classList.remove('form-switch')
}

// 创建粒子效果
const createParticles = () => {
  const particlesContainer = document.getElementById('particles')
  const particleCount = 50
  
  for (let i = 0; i < particleCount; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + '%'
    particle.style.top = Math.random() * 100 + '%'
    particle.style.animationDelay = Math.random() * 2 + 's'
    particle.style.animationDuration = (Math.random() * 3 + 2) + 's'
    particlesContainer.appendChild(particle)
  }
}

// 生命周期
onMounted(() => {
  createParticles()
  
  // 检查是否记住用户名
  if (localStorage.getItem('rememberMe') === 'true') {
    rememberMe.value = true
    registerData.username = localStorage.getItem('username') || ''
  }
})
</script>

<style lang="scss" scoped>
// 变量定义
$primary-color: #4f6ef2;
$primary-light: #eef4ff;
$primary-dark: #3b5bdb;
$success-color: #10b981;
$error-color: #ef4444;
$gray-50: #f9fafb;
$gray-100: #f3f4f6;
$gray-200: #e5e7eb;
$gray-300: #d1d5db;
$gray-400: #9ca3af;
$gray-500: #6b7280;
$gray-600: #4b5563;
$gray-700: #374151;
$gray-800: #1f2937;
$gray-900: #111827;

// 主容器
.enhanced-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

// 背景动画
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
  
  &.circle-1 {
    width: 80px;
    height: 80px;
    top: 20%;
    left: 10%;
    animation-delay: 0s;
  }
  
  &.circle-2 {
    width: 120px;
    height: 120px;
    top: 60%;
    right: 10%;
    animation-delay: 2s;
  }
  
  &.circle-3 {
    width: 60px;
    height: 60px;
    top: 40%;
    left: 80%;
    animation-delay: 4s;
  }
  
  &.circle-4 {
    width: 100px;
    height: 100px;
    bottom: 20%;
    left: 30%;
    animation-delay: 1s;
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

// 粒子效果
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: particle-float 3s ease-in-out infinite;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0px) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) scale(1.2);
    opacity: 0.8;
  }
}

// 登录页面布局
.login-page {
  position: relative;
  z-index: 10;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

// 品牌展示区域
.bg {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .login-brand {
    text-align: center;
    color: white;
    animation: fadeInUp 1s ease-out;
    
    .brand-logo {
      margin-bottom: 32px;
      
      img {
        width: 120px;
        height: 120px;
        border-radius: 24px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
        animation: pulse 2s ease-in-out infinite;
      }
    }
    
    .brand-title {
      font-size: 3rem;
      font-weight: 700;
      margin-bottom: 16px;
      background: linear-gradient(135deg, white 0%, #f0f0f0 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .brand-subtitle {
      font-size: 1.25rem;
      margin-bottom: 48px;
      opacity: 0.9;
    }
    
    .brand-features {
      .feature-item {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;
        margin-bottom: 16px;
        font-size: 1rem;
        opacity: 0.8;
        
        .el-icon {
          color: #10b981;
          font-size: 1.25rem;
        }
      }
    }
  }
}

// 表单区域
.form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.form-container {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 0.8s ease-out;
  
  .enhanced-form {
    .form-header {
      text-align: center;
      margin-bottom: 32px;
      
      .form-icon {
        width: 64px;
        height: 64px;
        background: linear-gradient(135deg, $primary-color 0%, #764ba2 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 16px;
        
        .el-icon {
          font-size: 2rem;
          color: white;
        }
      }
      
      h2 {
        font-size: 1.75rem;
        font-weight: 600;
        color: $gray-800;
        margin-bottom: 8px;
      }
      
      p {
        color: $gray-500;
        font-size: 1rem;
      }
    }
    
    .enhanced-input {
      .el-input__wrapper {
        background: $gray-50;
        border: 2px solid transparent;
        border-radius: 12px;
        padding: 12px 16px;
        transition: all 0.3s ease;
        
        &:hover {
          background: white;
          border-color: $primary-color;
          box-shadow: 0 0 0 3px rgba(79, 110, 242, 0.1);
        }
        
        &.is-focus {
          background: white;
          border-color: $primary-color;
          box-shadow: 0 0 0 3px rgba(79, 110, 242, 0.1);
        }
        
        .el-input__prefix {
          color: $gray-400;
          margin-right: 8px;
        }
      }
    }
    
    .enhanced-button {
      width: 100%;
      height: 48px;
      border-radius: 12px;
      font-size: 1rem;
      font-weight: 500;
      background: linear-gradient(135deg, $primary-color 0%, #764ba2 100%);
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        transition: left 0.5s ease;
      }
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 20px rgba(79, 110, 242, 0.3);
        
        &::before {
          left: 100%;
        }
      }
      
      &:active {
        transform: translateY(0);
      }
      
      .el-icon {
        font-size: 1.25rem;
      }
    }
    
    .form-options {
      margin-bottom: 24px;
      
      .remember-me {
        .el-checkbox {
          color: $gray-600;
        }
      }
    }
    
    .form-footer {
      text-align: center;
      margin-top: 24px;
      
      span {
        color: $gray-500;
        font-size: 0.875rem;
      }
      
      .el-link {
        font-weight: 500;
      }
    }
  }
}

// 表单动画
.form-success {
  animation: formSuccess 0.5s ease-out;
}

@keyframes formSuccess {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

.form-error {
  animation: formError 0.5s ease-in-out;
}

@keyframes formError {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

.form-switch {
  animation: formSwitch 0.3s ease-out;
}

@keyframes formSwitch {
  0% { opacity: 0; transform: translateX(20px); }
  100% { opacity: 1; transform: translateX(0); }
}

// 响应式设计
@media (max-width: 1200px) {
  .login-page {
    .bg {
      display: none;
    }
    
    .form {
      width: 100%;
      max-width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .enhanced-login-container {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  .form-container {
    padding: 32px 24px;
    border-radius: 20px;
    margin: 20px;
    max-width: none;
  }
  
  .login-page {
    padding: 0;
    
    .form {
      padding: 0;
    }
  }
}

// 加载动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>