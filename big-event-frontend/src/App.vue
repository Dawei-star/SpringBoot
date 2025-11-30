<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  // 检查系统主题偏好
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  if (prefersDark) {
    document.documentElement.setAttribute('data-theme', 'dark')
  }
})
</script>

<template>
  <router-view :key="$route.fullPath" />
</template>

<style lang="scss">
#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
