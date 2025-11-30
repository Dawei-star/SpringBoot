<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { House, Document } from '@element-plus/icons-vue'
const router = useRouter()
const active = computed(() => router.currentRoute.value.path)
</script>

<template>
<aside class="side-nav" role="navigation" aria-label="主导航">
    <div class="profile">
      <el-avatar size="large">D</el-avatar>
      <div class="profile-info">
        <div class="name">Debug客栈风格</div>
        <small class="desc">分享开发与生活的故事</small>
      </div>
    </div>
    <el-menu :default-active="active" class="menu" router aria-label="页面导航菜单">
      <el-menu-item index="/" @click="router.push('/')">
        <el-icon><House /></el-icon>
        首页
      </el-menu-item>
      <el-menu-item index="/articles" @click="router.push('/articles')">
        <el-icon><Document /></el-icon>
        文章列表
      </el-menu-item>
      <el-menu-item index="/archive" @click="router.push('/archive')">
        <el-icon><Document /></el-icon>
        归档
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<style scoped>
.side-nav { position: sticky; top: 16px; padding: 12px; border-radius: 12px; background: var(--el-bg-color-overlay); box-shadow: var(--el-box-shadow-light) }
.profile { display:flex; gap:12px; align-items:center; margin-bottom:12px }
.profile-info .name { font-weight:600 }
.menu { border-right: none }
</style>
