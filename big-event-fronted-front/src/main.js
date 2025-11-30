import { createApp } from 'vue'
// 移除默认 Vite 模板样式，避免与主题冲突
import './styles/design-system.scss'
import './styles/enhancements.scss'
import './styles/code-highlight.css'
import './styles/theme-comfortable.scss'
import App from './App.vue'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import { createPinia } from 'pinia'
import { useThemeStore } from './stores/theme'
import router from './router'

// 使用 Pinia 主题仓库初始化主题

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
const theme = useThemeStore()
theme.applyInitial()
app.mount('#app')
