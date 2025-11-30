import { createRouter, createWebHistory } from "vue-router";
// 路由懒加载
import { useTokenStore } from "@/stores/token";

const routes = [
  { path: '/login', component: () => import('@/components/EnhancedLogin.vue') },
  {
    path: '/',
    redirect: '/home',
    component: () => import('@/views/Layout.vue'),
    children: [
      { path: '/home', component: () => import('@/views/Home.vue') },
      { path: '/article/category', component: () => import('@/views/article/ArticleCategory.vue') },
      { path: '/article/manage', component: () => import('@/views/article/ArticleManage.vue'), meta: { hideHeader: true } },
      { path: '/article/detail/:id', component: () => import('@/views/article/ArticleDetail.vue'), meta: { hideHeader: true } },
      { path: '/article/archive', component: () => import('@/views/article/Archive.vue'), meta: { hideHeader: true } },
      { path: '/article/publish', component: () => import('@/views/article/ArticlePublish.vue') },
      { path: '/message/manage', component: () => import('@/views/message/MessageManage.vue') },
      { path: '/operation/log', component: () => import('@/views/operation/OperationLog.vue') },
      { path: '/user/info', component: () => import('@/views/user/UserInfo.vue') },
      { path: '/user/avatar', component: () => import('@/views/user/UserAvatar.vue') },
      { path: '/user/resetPassword', component: () => import('@/views/user/UserResetPassword.vue') },
      { path: '/user/manage', component: () => import('@/views/user/UserManage.vue') },
      { path: '/album/manage', component: () => import('@/views/album/AlbumManage.vue') },
      { path: '/album/create', component: () => import('@/views/album/AlbumForm.vue') },
      { path: '/album/edit/:id', component: () => import('@/views/album/AlbumForm.vue') },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: routes
})

// 路由守卫：根据 token 决定是否允许访问
router.beforeEach((to, from, next) => {
  const tokenStore = useTokenStore()
  
  // 获取token（优先使用cleanToken getter，确保获取的是清理后的token）
  const token = (tokenStore.cleanToken || tokenStore.token || '').trim()
  const hasToken = token.length > 0
  
  // 登录页面
  if (to.path === '/login') {
    // 如果已登录，重定向到首页
    if (hasToken) {
      next('/home')
    } else {
      next()
    }
    return
  }
  
  // 公共访问页面（文章浏览）不需要登录
  const isPublicArticle = to.path === '/article/manage' 
    || to.path.startsWith('/article/detail/') 
    || to.path === '/article/archive'
  
  if (isPublicArticle) {
    next()
    return
  }

  // 需要登录的页面
  if (hasToken) {
    next()
  } else {
    // 未登录，跳转到登录页
    next('/login')
  }
})

export default router
