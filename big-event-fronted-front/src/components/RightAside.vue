<script setup>
import { ref, onMounted } from 'vue'
import { fetchCategories } from '../api/category'
const keyword = ref('')
const categories = ref([])
const emit = defineEmits(['search','tag'])

onMounted(async () => {
  try {
    const { data } = await fetchCategories()
    categories.value = data || []
  } catch {
    // ignore
  }
})
</script>

<template>
  <aside class="right-aside">
    <el-card class="block">
      <div class="search">
        <el-input v-model="keyword" placeholder="输入关键字搜索" @keyup.enter="emit('search', keyword)">
          <template #prefix>
            <el-icon><svg viewBox="0 0 24 24"><path fill="currentColor" d="M9.5 3a6.5 6.5 0 0 1 5.168 10.498l5.91 5.91l-1.414 1.414l-5.91-5.91A6.5 6.5 0 1 1 9.5 3m0 2a4.5 4.5 0 1 0 0 9a4.5 4.5 0 0 0 0-9Z"/></svg></el-icon>
          </template>
          <template #suffix>
            <el-button type="primary" size="small" plain @click="emit('search', keyword)">搜索</el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-card class="block">
      <h3 class="block-title">热门推荐</h3>
      <ul class="hot-list">
        <li>常见开发工具精选</li>
        <li>旅行游记 · 大湾区风景路线</li>
        <li>免费无版权图片素材推荐</li>
      </ul>
    </el-card>

    <el-card class="block">
      <h3 class="block-title">订阅</h3>
      <div style="display:flex;gap:8px">
        <el-input placeholder="输入邮箱订阅"></el-input>
        <el-button type="primary">订阅</el-button>
      </div>
    </el-card>

    <el-card class="block">
      <h3 class="block-title">标签云</h3>
      <div class="tags">
        <el-tag v-for="t in categories" :key="t.id" effect="plain" round style="margin:6px" @click="emit('tag', t.categoryName)">{{ t.categoryName }}</el-tag>
      </div>
    </el-card>
  </aside>
</template>

<style scoped>
.right-aside { position: sticky; top: 16px }
.block { margin-bottom: 12px }
.block-title { font-size: 14px; margin-bottom: 8px }
.hot-list { padding-left: 16px; color: var(--el-text-color-secondary) }
.search :deep(.el-input__wrapper) { border-radius: 999px }
</style>
