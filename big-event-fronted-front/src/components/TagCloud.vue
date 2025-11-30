<script setup>
import { ref, onMounted, computed } from 'vue'
import { fetchCategories } from '../api/category'
import { fetchArticles } from '../api/article'
import { useRouter } from 'vue-router'

const router = useRouter()
const categories = ref([])
const articles = ref([])
const loading = ref(false)

const categoryStats = computed(() => {
  const stats = {}
  articles.value.forEach(article => {
    const catId = article.categoryId
    if (catId) {
      stats[catId] = (stats[catId] || 0) + 1
    }
  })
  return stats
})

const tagCloudData = computed(() => {
  return categories.value.map(cat => {
    const count = categoryStats.value[cat.id] || 0
    return {
      ...cat,
      count,
      size: Math.max(12, Math.min(24, 12 + count * 2))
    }
  }).filter(cat => cat.count > 0)
})

const loadData = async () => {
  try {
    loading.value = true
    const [catRes, artRes] = await Promise.all([
      fetchCategories(),
      fetchArticles({ pageNum: 1, pageSize: 100 })
    ])
    categories.value = catRes.data || []
    articles.value = artRes.data?.items || []
  } finally {
    loading.value = false
  }
}

const handleTagClick = (categoryId) => {
  router.push({ path: '/articles', query: { categoryId } })
}

onMounted(loadData)
</script>

<template>
  <div class="tag-cloud">
    <h3 class="tag-cloud-title">Categories</h3>
    <div v-loading="loading" class="tag-container">
      <span
        v-for="tag in tagCloudData"
        :key="tag.id"
        class="tag-item"
        :style="{ fontSize: tag.size + 'px' }"
        @click="handleTagClick(tag.id)"
      >
        {{ tag.categoryName }}
        <span class="tag-count">({{ tag.count }})</span>
      </span>
      <el-empty v-if="!loading && tagCloudData.length === 0" description="No categories" />
    </div>
  </div>
</template>

<style scoped>
.tag-cloud {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.tag-cloud-title {
  margin: 0 0 20px 0;
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  min-height: 100px;
}

.tag-item {
  display: inline-block;
  padding: 6px 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--el-text-color-primary);
  font-weight: 500;
  line-height: 1.5;
}

.tag-item:hover {
  background: var(--el-color-primary);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.tag-count {
  font-size: 0.85em;
  opacity: 0.7;
  margin-left: 4px;
}
</style>
