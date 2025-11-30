import '@/assets/main.scss'
import 'element-plus/dist/index.css'
import '@/styles/modern-theme.scss'
import '@/styles/design-system.scss'
import '@/styles/enhancements.scss'
import '@/styles/dark-mode-fix.css'

import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import router from "@/router";
import { createPinia } from "pinia";
import { createPersistedState } from "pinia-persistedstate-plugin";
import locale from 'element-plus/dist/locale/zh-cn'

const app = createApp(App)
const pinia = createPinia()
const persistedState = createPersistedState();
pinia.use(persistedState)

// 注册所有 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale });

// 全局主题初始化与系统偏好监听
(() => {
  const saved = localStorage.getItem('darkMode')
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
  const isDark = saved !== null ? saved === 'true' : prefersDark
  const root = document.documentElement
  if (isDark) {
    root.setAttribute('data-theme', 'dark')
    root.classList.add('dark')
  } else {
    root.removeAttribute('data-theme')
    root.classList.remove('dark')
  }
  // 仅当未设置用户偏好时，跟随系统
  if (saved === null && window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (e.matches) {
        root.setAttribute('data-theme', 'dark')
        root.classList.add('dark')
      } else {
        root.removeAttribute('data-theme')
        root.classList.remove('dark')
      }
    })
  }
})()

app.mount('#app')
