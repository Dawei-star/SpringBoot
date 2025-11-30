<script setup>
import { storeToRefs } from 'pinia'
import { useThemeStore } from '../stores/theme'
import { ElMessage } from 'element-plus'

const theme = useThemeStore()
const { isDark } = storeToRefs(theme)
const onChange = (val) => { theme.setDark(val); ElMessage.success(val ? '切换到深色模式' : '切换到浅色模式') }
</script>

<template>
  <div class="theme-switch">
    <span class="label">浅色</span>
    <el-switch v-model="isDark" size="large" @change="onChange" />
    <span class="label">深色</span>
  </div>
</template>

<style scoped>
.theme-switch { position: fixed; left: 20px; bottom: 20px; z-index: 1000; background: var(--el-fill-color-blank); padding: 12px 16px; border-radius: 12px; box-shadow: var(--el-box-shadow-light); display:flex; align-items:center; gap:10px; font-size:16px }
.label { color: var(--el-text-color-regular) }
.theme-switch :deep(.el-switch) { transform: scale(1.15) }
</style>
