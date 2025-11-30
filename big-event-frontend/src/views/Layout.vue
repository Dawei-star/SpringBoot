<script setup>
import {
  CaretBottom,
  Crop,
  Document,
  EditPen,
  Fold,
  FullScreen,
  Grid,
  Management,
  Message,
  Moon,
  Position,
  Refresh,
  Search,
  Sunny,
  SwitchButton,
  User,
  Menu
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'
import {userInfoService} from "@/api/user";
import {useUserInfoStore} from "@/stores/userInfo";
import {useRouter, useRoute} from "vue-router";
import {ElMessage, ElMessageBox} from "element-plus";
import {useTokenStore} from "@/stores/token";
import {ref, onMounted, computed, shallowRef} from 'vue'

const isCollapse = ref(false)
const searchKeyword = ref('')
const mobileSidebarOpen = ref(false)
const isDarkMode = ref(false)

const userInfoStore = useUserInfoStore();
const getUserInfo = async () => {
  try {
    const result = await userInfoService();
    // 成功状态码范围：200-299
    if (result && result.code >= 200 && result.code < 300 && result.data) {
      userInfoStore.setUserInfo(result.data)
    }
  } catch (e) {
    // 后端异常时不阻断页面渲染，提示并保持可用
    console.error('加载用户信息失败:', e)
  }
}

const router = useRouter();
const route = useRoute();
const tokenStore = useTokenStore();

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm(
        '你确认要退出吗？',
        '温馨提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
    ).then(
        async () => {
          userInfoStore.removeUserInfo()
          tokenStore.removeToken()
          ElMessage.success("退出成功")
          await router.push('/login')
        }
    )
  } else {
    router.push('/user/' + command)
  }
}

// 使用 shallowRef 优化性能
const menuItems = shallowRef([
  { path: '/home', icon: Grid, label: '首页', badge: null },
  { path: '/article/manage', icon: Document, label: '文章列表', badge: null },
  { path: '/article/archive', icon: Grid, label: '文章归档', badge: null },
  { path: '/article/category', icon: Management, label: '文章分类', badge: null },
  { path: '/article/publish', icon: Position, label: '文章发布', badge: null },
  { path: '/message/manage', icon: Message, label: '留言管理', badge: null },
  { path: '/user/manage', icon: User, label: '用户管理', badge: null },
  { path: '/album/manage', icon: Grid, label: '相册管理', badge: null },
  { path: '/operation/log', icon: Document, label: '操作日志', badge: null },
])

// 优化计算属性，缓存结果
const activeMenu = computed(() => {
  const currentPath = route.path
  return menuItems.value.find(item => 
    currentPath === item.path || currentPath.startsWith(item.path + '/')
  )?.path || ''
})

// 缓存当前菜单标签
const currentMenuLabel = computed(() => {
  return menuItems.value.find(m => m.path === activeMenu.value)?.label || '首页'
})

const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  // 使用 requestAnimationFrame 优化 DOM 操作
  requestAnimationFrame(() => {
  if (isDarkMode.value) {
    document.documentElement.setAttribute('data-theme', 'dark')
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
    document.documentElement.classList.remove('dark')
  }
  localStorage.setItem('darkMode', isDarkMode.value)
  })
}

const refreshPage = () => {
  location.reload()
}

const toggleFullscreen = async () => {
  try {
    if (!document.fullscreenElement) {
      await document.documentElement.requestFullscreen()
    } else {
      await document.exitFullscreen()
    }
  } catch (e) {
    ElMessage.info('浏览器不支持全屏或被阻止')
  }
}

// 防抖搜索
let searchTimer = null
const performSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    if (!searchKeyword.value || !searchKeyword.value.trim()) {
      ElMessage.info('请输入关键词')
      return
    }
    
    const keyword = searchKeyword.value.trim()
    
    // 根据当前路由执行不同的搜索
    const currentPath = route.path
    
    // 如果是文章管理页面，跳转并传递搜索关键词
    if (currentPath.startsWith('/article/manage') || currentPath.startsWith('/article')) {
      // 使用query参数传递搜索关键词
      router.push({
        path: '/article/manage',
        query: { keyword: keyword }
      })
      ElMessage.success(`正在搜索：${keyword}`)
    } else {
      // 其他页面，跳转到文章管理页面并搜索
      router.push({
        path: '/article/manage',
        query: { keyword: keyword }
      })
      ElMessage.success(`正在搜索：${keyword}`)
    }
  }, 300)
}

const toggleMobileSidebar = () => {
  mobileSidebarOpen.value = !mobileSidebarOpen.value
}

const initDarkMode = () => {
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode === 'true') {
    isDarkMode.value = true
    requestAnimationFrame(() => {
      document.documentElement.setAttribute('data-theme', 'dark')
    })
  }
}

onMounted(() => {
  initDarkMode()
  getUserInfo()
})
</script>

<template>
  <div class="modern-layout" :class="{'dark-mode': isDarkMode}">
    <!-- 移动端遮罩 -->
    <transition name="fade">
    <div 
      v-if="mobileSidebarOpen" 
        class="mobile-overlay"
      @click="toggleMobileSidebar">
    </div>
    </transition>
    
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{'collapsed': isCollapse, 'mobile-open': mobileSidebarOpen}">
      <div class="sidebar-header">
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon"></div>
          <span v-if="!isCollapse" class="logo-text">大事件</span>
        </div>
        <el-button 
          v-if="!isCollapse" 
          text 
          circle 
          class="collapse-btn"
          @click="isCollapse = !isCollapse">
          <el-icon><Fold /></el-icon>
        </el-button>
      </div>

      <nav class="sidebar-nav">
        <div 
          v-for="item in menuItems" 
          :key="item.path"
          class="nav-item"
          :class="{'active': activeMenu === item.path}"
          @click="router.push(item.path); mobileSidebarOpen = false">
          <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
          <span v-if="!isCollapse" class="nav-label">{{ item.label }}</span>
          <span v-if="item.badge && !isCollapse" class="nav-badge">{{ item.badge }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card" @click="router.push('/user/info')">
          <el-avatar :src="userInfoStore.userInfo.userPic || avatar" :size="isCollapse ? 32 : 40" />
          <div v-if="!isCollapse" class="user-info">
            <div class="user-name">{{ userInfoStore.userInfo.nickname || '未命名用户' }}</div>
            <div class="user-email">{{ userInfoStore.userInfo.email || '点击补充邮箱' }}</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header v-if="!route.meta?.hideHeader" class="top-header">
        <div class="header-left">
          <el-button 
            circle 
            text 
            class="mobile-menu-btn"
            @click="toggleMobileSidebar">
            <el-icon><Menu /></el-icon>
          </el-button>
          <el-button 
            v-if="isCollapse"
            circle 
            text 
            class="expand-btn"
            @click="isCollapse = !isCollapse">
            <el-icon><Grid /></el-icon>
          </el-button>
          <div class="breadcrumb">
            <span class="breadcrumb-item">{{ currentMenuLabel }}</span>
          </div>
        </div>

        <div class="header-center">
          <el-input
              v-model="searchKeyword"
            placeholder="搜索文章、分类..."
              class="header-search"
            :prefix-icon="Search"
              @keyup.enter="performSearch"
            clearable>
          </el-input>
        </div>

        <div class="header-right">
          <el-tooltip content="刷新" placement="bottom">
            <el-button circle text @click="refreshPage">
              <el-icon><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="isDarkMode ? '切换为亮色' : '切换为夜间模式'" placement="bottom">
            <el-button circle text @click="toggleDarkMode">
              <el-icon><Moon v-if="!isDarkMode"/><Sunny v-else/></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="全屏" placement="bottom">
            <el-button circle text @click="toggleFullscreen">
              <el-icon><FullScreen /></el-icon>
              </el-button>
            </el-tooltip>
          <el-dropdown placement="bottom-end" @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <el-avatar :src="userInfoStore.userInfo.userPic || avatar" :size="32" />
              <el-icon class="dropdown-icon"><CaretBottom/></el-icon>
              </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="info" :icon="User">基本资料</el-dropdown-item>
                <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
                <el-dropdown-item command="resetPassword" :icon="EditPen">重置密码</el-dropdown-item>
                <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="content-area">
        <router-view :key="$route.fullPath"/>
      </main>
    </div>
      </div>
</template>

<style lang="scss" scoped>
.modern-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-app, #f7fafc);
  position: relative;
  overflow: hidden;

  &.dark-mode {
    background: #1a202c;
  }
}

// 淡入淡出过渡
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.mobile-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
  // GPU 加速
  transform: translateZ(0);
  
  @media (max-width: 768px) {
    display: block;
  }
}

.sidebar {
  width: 260px;
  background: rgba(255, 255, 255, 0.98);
  // 减少 backdrop-filter 的使用，改用半透明背景
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  // 优化过渡，只过渡必要的属性
  transition: width 0.2s ease;
  z-index: 999;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.05);
  // GPU 加速
  will-change: width;
  transform: translateZ(0);

  &.collapsed {
    width: 80px;
  }

  @media (max-width: 768px) {
    position: fixed;
    left: -260px;
  height: 100vh;
    box-shadow: 2px 0 30px rgba(0, 0, 0, 0.3);
    transition: left 0.3s ease;
    
    &.mobile-open {
      left: 0;
    }
  }
}

.dark-mode .sidebar {
  background: #2d3748;
  backdrop-filter: none;
  border-right: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.2);
}

.sidebar-header {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .sidebar-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.15s;
  // 避免不必要的重绘
  will-change: transform;

  &:hover {
    transform: scale(1.02);
  }

  .logo-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
    transform: translateZ(0);
  }

  .logo-text {
    font-size: 20px;
    font-weight: 700;
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.collapse-btn {
  color: var(--text-secondary);
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
  // 优化滚动性能
  -webkit-overflow-scrolling: touch;
  transform: translateZ(0);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 6px;
  border-radius: 12px;
  cursor: pointer;
  // 只过渡必要的属性
  transition: background-color 0.15s, transform 0.15s;
  position: relative;
  color: var(--text-secondary);
  // GPU 加速
  will-change: transform, background-color;
  transform: translateZ(0);

  .nav-icon {
    font-size: 20px;
    flex-shrink: 0;
  }

  .nav-label {
    flex: 1;
    font-weight: 500;
  }

  .nav-badge {
    background: var(--el-color-danger);
    color: white;
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 10px;
  }

  &:hover {
    background: rgba(14, 165, 233, 0.08);
    color: var(--primary-color);
    transform: translateX(2px);
  }

  &.active {
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);

    .nav-icon {
      color: white;
    }
  }
}

.dark-mode .nav-item {
  color: #e2e8f0;
  
  &:hover {
    background: rgba(56, 189, 248, 0.15);
    color: #ffffff;
    transform: translateX(4px);
  }
  
  &.active {
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    color: white;
    box-shadow: 0 4px 16px rgba(14, 165, 233, 0.4);
  }
}

.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode .sidebar-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background-color 0.15s;

  &:hover {
    background: rgba(102, 126, 234, 0.08);
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .user-name {
      font-weight: 600;
      font-size: 14px;
      color: var(--text-primary);
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-email {
      font-size: 12px;
      color: var(--text-secondary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.dark-mode .user-card {
  &:hover {
    background: rgba(99, 102, 241, 0.15);
  }
  
  .user-name {
    color: var(--text-primary);
  }
  
  .user-email {
    color: var(--text-secondary);
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  // GPU 加速
  transform: translateZ(0);
}

.top-header {
  height: 70px;
  background: rgba(255, 255, 255, 0.98);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  z-index: 100;
  // GPU 加速
  will-change: background;
  transform: translateZ(0);
}

.dark-mode .top-header {
  background: #2d3748;
  backdrop-filter: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  
  * {
    color: inherit;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.mobile-menu-btn {
  display: none;
  color: #4b5563;
  
  &:hover {
    color: #667eea;
    background-color: rgba(102, 126, 234, 0.1);
  }
  
  .dark-mode & {
    color: #cbd5e1;
    
    &:hover {
      color: #ffffff;
      background-color: rgba(99, 102, 241, 0.2);
    }
  }
  
  @media (max-width: 768px) {
    display: flex;
  }
}

.breadcrumb {
  .breadcrumb-item {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    
    .dark-mode & {
      color: #f1f5f9;
    }
  }
}

.header-center {
  flex: 1;
  max-width: 500px;
  margin: 0 24px;
  
  @media (max-width: 768px) {
    display: none;
  }
}

.header-search {
  :deep(.el-input__wrapper) {
    border-radius: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: box-shadow 0.2s;
    background-color: rgba(255, 255, 255, 0.9);
    border-color: rgba(0, 0, 0, 0.1);

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      border-color: rgba(102, 126, 234, 0.3);
    }

    &.is-focus {
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      border-color: #667eea;
    }
  }
  
  :deep(.el-input__inner) {
    color: #1f2937;
    
    &::placeholder {
      color: #9ca3af;
    }
  }
}

.dark-mode .header-search {
  :deep(.el-input__wrapper) {
    background-color: rgba(30, 41, 59, 0.8);
    border-color: rgba(148, 163, 184, 0.2);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    
    &:hover {
      border-color: rgba(99, 102, 241, 0.6);
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
      background-color: rgba(30, 41, 59, 0.9);
    }
    
    &.is-focus {
      border-color: #6366f1;
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.3);
      background-color: rgba(30, 41, 59, 0.95);
    }
  }
  
  :deep(.el-input__inner) {
    color: #f1f5f9;
    
    &::placeholder {
      color: rgba(203, 213, 225, 0.6);
    }
  }
  
  :deep(.el-input__prefix) {
    .el-icon {
      color: #cbd5e1;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  
  :deep(.el-button) {
    color: #4b5563;
    
    &:hover {
      color: #667eea;
      background-color: rgba(102, 126, 234, 0.1);
    }
  }
  
  .dark-mode & {
    :deep(.el-button) {
      color: #cbd5e1;
      
      &:hover {
        color: #ffffff;
        background-color: rgba(99, 102, 241, 0.2);
      }
    }
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.15s;

  &:hover {
    background: rgba(102, 126, 234, 0.08);
  }

  .dropdown-icon {
    font-size: 12px;
    color: var(--text-secondary);
  }
}

.dark-mode .user-dropdown {
  &:hover {
    background: rgba(99, 102, 241, 0.15);
  }
  
  .dropdown-icon {
    color: var(--text-secondary);
  }
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg, 24px);
  position: relative;
  // 优化滚动性能
  -webkit-overflow-scrolling: touch;
  transform: translateZ(0);
  background: var(--bg-secondary, #f9fafb);
  
  [data-theme="dark"] & {
    background: #1a202c;
  }

  @media (max-width: 768px) {
    padding: var(--spacing-md, 16px);
  }
}

// 优化滚动条样式
.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-track {
  background: transparent;
}

.content-area::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 4px;

  &:hover {
    background: rgba(0, 0, 0, 0.25);
  }
}

.dark-mode .content-area::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.15);

  &:hover {
    background: rgba(255, 255, 255, 0.25);
  }
}
</style>
