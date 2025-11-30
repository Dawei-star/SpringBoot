import { createRouter, createWebHistory } from 'vue-router'
import { useTokenStore } from '../stores/token'

const Home = () => import('../views/Home.vue')
const ArticleList = () => import('../views/ArticleList.vue')
const ArticleDetail = () => import('../views/ArticleDetail.vue')
const Archive = () => import('../views/Archive.vue')
const Gallery = () => import('../views/Gallery.vue')
const Login = () => import('../views/Login.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/articles', name: 'articles', component: ArticleList },
    { path: '/articles/:id', name: 'article-detail', component: ArticleDetail },
    { path: '/archive', name: 'archive', component: Archive },
    { path: '/gallery', name: 'gallery', component: Gallery },
    { path: '/login', name: 'login', component: Login, meta: { hideHeader: true } },
  ],
})

router.beforeEach((to, from, next) => {
  const tokenStore = useTokenStore()
  // Public routes that don't require login
  const publicNames = ['home', 'articles', 'article-detail', 'archive', 'gallery', 'login']
  if (publicNames.includes(to.name)) return next()

  if (tokenStore.token) return next()
  next('/login')
})

export default router
