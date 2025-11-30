<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from './stores/token'
import { useThemeStore } from './stores/theme'
import { ElMessage } from 'element-plus'
import BackToTop from './components/BackToTop.vue'
import ChatWidget from './components/ChatWidget.vue'
import { 
  Menu, 
  Close, 
  Moon, 
  Sunny,
  User,
  SwitchButton,
  House,
  Document,
  Folder,
  Picture,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const themeStore = useThemeStore()

const isMobileMenuOpen = ref(false)

const navItems = [
  { path: '/', name: 'home', label: '首页', icon: House },
  { path: '/articles', name: 'articles', label: '文章', icon: Document },
  { path: '/archive', name: 'archive', label: '归档', icon: Folder },
  { path: '/gallery', name: 'gallery', label: '相册', icon: Picture },
]

const activeNav = computed(() => {
  return navItems.find(item => route.path === item.path || route.path.startsWith(item.path + '/'))?.path || ''
})

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const logout = () => {
  tokenStore.clear()
  ElMessage.success('已退出登录')
  router.push('/login')
}

const toggleTheme = () => {
  themeStore.toggle()
}

const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  }
}

onMounted(() => {
  themeStore.applyInitial()
})
</script>

<template>
  <div class="app-wrapper" :class="{ 'dark-mode': themeStore.isDark }">
    <!-- 顶部导航栏 -->
    <header v-if="!route.meta?.hideHeader" class="app-header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon"></div>
          <span class="logo-text">BigEvent</span>
        </div>

        <!-- 桌面端导航 -->
        <nav class="nav-desktop">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: activeNav === item.path }"
            @click="isMobileMenuOpen = false">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>

        <!-- 右侧操作区 -->
        <div class="header-actions">
          <el-button 
            circle 
            text 
            class="theme-toggle"
            @click="toggleTheme">
            <el-icon><Moon v-if="!themeStore.isDark" /><Sunny v-else /></el-icon>
          </el-button>

          <template v-if="!tokenStore.token">
            <el-button type="primary" @click="router.push('/login')">
              登录
            </el-button>
          </template>
          <template v-else>
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-menu">
                <el-avatar :size="32" class="user-avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout" :icon="SwitchButton">
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <!-- 移动端菜单按钮 -->
          <el-button 
            circle 
            text 
            class="mobile-menu-btn"
            @click="toggleMobileMenu">
            <el-icon><Menu v-if="!isMobileMenuOpen" /><Close v-else /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 移动端导航菜单 -->
      <transition name="slide-down">
        <nav v-if="isMobileMenuOpen" class="nav-mobile">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item-mobile"
            :class="{ active: activeNav === item.path }"
            @click="toggleMobileMenu">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>
      </transition>
    </header>

    <!-- 主内容区 -->
    <main class="app-main" :class="{ 'no-header': route.meta?.hideHeader }">
      <router-view :key="route.fullPath" />
    </main>

    <!-- 页脚 -->
    <footer v-if="!route.meta?.hideHeader" class="app-footer">
      <div class="footer-container">
        <p>&copy; 2024 BigEvent Blog. All rights reserved.</p>
      </div>
    </footer>

    <!-- 回到顶部按钮 -->
    <BackToTop v-if="!route.meta?.hideHeader" />
    
    <!-- 聊天组件 -->
    <ChatWidget v-if="!route.meta?.hideHeader" />
  </div>
</template>


<style lang="scss" scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fafaf9;
  color: var(--text-primary, #292524);
  transition: background 0.3s ease;
  position: relative;

  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 50%, rgba(14, 165, 233, 0.03) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(251, 146, 60, 0.03) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  @media (max-width: 768px) {
    padding: 0 16px;
    height: 60px;
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 4px 0;

  &:hover {
    transform: translateY(-2px);
  }

  .logo-icon {
    width: 42px;
    height: 42px;
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    border-radius: 14px;
    box-shadow: 0 4px 16px rgba(14, 165, 233, 0.25);
    transition: all 0.3s ease;
    
    .logo:hover & {
      box-shadow: 0 6px 24px rgba(14, 165, 233, 0.35);
      transform: rotate(5deg);
    }
  }

  .logo-text {
    font-size: 26px;
    font-weight: 800;
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: -0.02em;
    transition: all 0.3s ease;
    
    @media (max-width: 768px) {
      font-size: 22px;
    }
  }
}

.nav-desktop {
  display: flex;
  gap: 8px;
  flex: 1;
  justify-content: center;

  @media (max-width: 768px) {
    display: none;
  }
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--el-text-color-regular);
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  font-size: 15px;

  &::before {
    content: '';
    position: absolute;
    bottom: 6px;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 60%;
    height: 2px;
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    border-radius: 2px;
    transition: transform 0.3s ease;
  }

  &:hover {
    background: rgba(14, 165, 233, 0.08);
    color: #0ea5e9;
    transform: translateY(-1px);
    
    &::before {
      transform: translateX(-50%) scaleX(1);
    }
  }

  &.active {
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    color: white;
    box-shadow: 0 4px 16px rgba(14, 165, 233, 0.25);
    
    &::before {
      display: none;
    }
  }

  .el-icon {
    font-size: 18px;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle {
  color: var(--el-text-color-regular);
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;

  &:hover {
    background: rgba(102, 126, 234, 0.1);
  }

  .user-avatar {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  .dropdown-icon {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.mobile-menu-btn {
  display: none;

  @media (max-width: 768px) {
    display: flex;
  }
}

.nav-mobile {
  display: none;
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.98);

  .dark-mode & {
    background: rgba(15, 23, 42, 0.98);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }

  @media (max-width: 768px) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.nav-item-mobile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--el-text-color-regular);
  font-weight: 500;
  transition: all 0.2s;

  &:hover {
    background: rgba(102, 126, 234, 0.1);
    color: var(--el-color-primary);
  }

  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
}

.app-main {
  flex: 1;
  min-height: calc(100vh - 140px);
  position: relative;
  z-index: 1;

  &.no-header {
    min-height: 100vh;
  }
}

.app-footer {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding: 32px 0;
  margin-top: auto;
  position: relative;
  z-index: 1;

  .dark-mode & {
    background: rgba(15, 23, 42, 0.6);
    border-top: 1px solid rgba(255, 255, 255, 0.08);
  }
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

// 过渡动画
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
  max-height: 300px;
  overflow: hidden;
}

.slide-down-enter-from,
.slide-down-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
