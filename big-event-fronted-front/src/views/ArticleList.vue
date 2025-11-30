<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchArticles } from '../api/article'
import { fetchCategories } from '../api/category'
import ArticleCard from '../components/ArticleCard.vue'
import ArticleCardSkeleton from '../components/ArticleCardSkeleton.vue'
import { Search } from '@element-plus/icons-vue'
import { debounce } from '../utils/debounce'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)
const items = ref([])
const categories = ref([])
const keyword = ref('')
const activeCategoryId = ref(null)

const load = async () => {
  try {
    loading.value = true
    errorMsg.value = ''
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      state: '已发布'
    }
    if (activeCategoryId.value) {
      params.categoryId = activeCategoryId.value
    }
    const res = await fetchArticles(params)
    const page = res.data
    items.value = page.items || page.rows || []
    if (!categories.value.length) {
      const { data } = await fetchCategories()
      categories.value = data || []
    }
    items.value.forEach(a => {
      const c = categories.value.find(x => x.id === a.categoryId)
      if (c) a.categoryName = c.categoryName
    })
    total.value = page.total || 0
  } catch (e) {
    errorMsg.value = e?.message || '文章列表加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await load()
  const { data } = await fetchCategories()
  categories.value = data || []
})

const handlePageChange = (p) => {
  pageNum.value = p
  load()
}

const handleSizeChange = (s) => {
  pageSize.value = s
  pageNum.value = 1
  load()
}

const handleCategoryChange = () => {
  pageNum.value = 1
  load()
}

// 使用防抖优化搜索
const debouncedLoad = debounce(() => {
  pageNum.value = 1
  load()
}, 300)

// 监听搜索关键词变化
watch(keyword, () => {
  debouncedLoad()
})

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return items.value
  return items.value.filter(i => (i.title || '').toLowerCase().includes(k))
})
</script>

<template>
  <div class="article-list-page" role="main" aria-label="文章列表页面">
    <div class="page-container">
      <!-- 页面头部 -->
      <header class="page-header">
        <h1 class="page-title">文章列表</h1>
        <p class="page-subtitle">浏览所有发布的文章</p>
      </header>

      <!-- 搜索和筛选 -->
      <section class="filter-section" aria-label="搜索和筛选">
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索文章标题..."
            class="search-input"
            clearable
            aria-label="搜索文章"
            role="searchbox">
            <template #prefix>
              <el-icon aria-hidden="true"><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <nav v-if="categories.length" class="category-filter" aria-label="分类筛选">
          <el-button
            round
            size="small"
            :type="!activeCategoryId ? 'primary' : 'default'"
            :aria-pressed="!activeCategoryId"
            @click="activeCategoryId = null; handleCategoryChange()">
            全部
          </el-button>
          <el-button
            v-for="c in categories"
            :key="c.id"
            round
            size="small"
            :type="activeCategoryId === c.id ? 'primary' : 'default'"
            :aria-pressed="activeCategoryId === c.id"
            :aria-label="`筛选分类：${c.categoryName}`"
            @click="activeCategoryId = c.id; handleCategoryChange()">
            {{ c.categoryName }}
          </el-button>
        </nav>
      </section>

      <!-- 文章列表 -->
      <section class="articles-section" aria-label="文章列表">
        <div v-if="loading" class="articles-grid" role="status" aria-live="polite" aria-label="正在加载文章">
          <ArticleCardSkeleton v-for="i in 6" :key="i" />
        </div>
            <el-result 
          v-else-if="errorMsg" 
              icon="error" 
              title="加载失败" 
              :sub-title="errorMsg"
              role="alert">
              <template #extra>
                <el-button type="primary" aria-label="重试加载" @click="load">重试</el-button>
                <el-button aria-label="返回首页" @click="router.push('/')">返回首页</el-button>
              </template>
            </el-result>
            <el-empty 
              v-else-if="filtered.length === 0" 
              description="暂无文章"
              class="empty-state"
              role="status" />
            <div v-else class="articles-grid" role="list" aria-label="文章列表">
              <ArticleCard
                v-for="article in filtered"
                :key="article.id"
                :item="article"
                class="article-item"
                role="listitem"
                :aria-label="`文章：${article.title}`"
                tabindex="0"
                @click="router.push(`/articles/${article.id}`)"
                @keyup.enter="router.push(`/articles/${article.id}`)"
              />
            </div>
      </section>

      <!-- 分页 -->
      <div v-if="total > 0 && !errorMsg" class="pagination-wrapper">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-size="pageSize"
          :current-page="pageNum"
          :page-sizes="[12, 24, 36, 48]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.article-list-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-xl, 40px) var(--spacing-lg, 24px);
  min-height: calc(100vh - 140px);
}

.page-container {
  animation: fadeIn var(--transition-base, 0.6s ease-out);
}

.page-header {
  margin-bottom: var(--spacing-xl, 40px);
  text-align: center;
}

.page-title {
  font-size: var(--text-5xl, 3rem);
  font-weight: var(--font-extrabold, 800);
  margin: 0 0 var(--spacing-md, 12px) 0;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  
  @media (max-width: 768px) {
    font-size: var(--text-3xl, 1.875rem);
  }
}

.page-subtitle {
  color: var(--text-secondary, var(--el-text-color-secondary));
  font-size: var(--text-lg, 1.1rem);
  margin: 0;
}

.filter-section {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl, 20px);
  padding: var(--spacing-lg, 24px);
  margin-bottom: var(--spacing-xl, 40px);
  box-shadow: var(--shadow-md, 0 4px 20px rgba(0, 0, 0, 0.05));
  border: 1px solid rgba(255, 255, 255, 0.2);

  [data-theme="dark"] & {
    background: rgba(15, 23, 42, 0.8);
    border-color: rgba(148, 163, 184, 0.1);
  }
}

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  max-width: 500px;

  :deep(.el-input__wrapper) {
    border-radius: var(--radius-md, 12px);
    box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.05));
    transition: all var(--transition-fast, 0.2s);

    &:hover {
      box-shadow: var(--shadow-md, 0 4px 12px rgba(0, 0, 0, 0.1));
    }

    &.is-focus {
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      outline: 2px solid transparent;
      outline-offset: 2px;
    }
  }
}

.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md, 12px);
}

.articles-section {
  margin-bottom: var(--spacing-xl, 40px);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--spacing-xl, 30px);
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg, 24px);
  }
}

.article-item {
  cursor: pointer;
  transition: transform var(--transition-fast, 0.2s);
  outline: none;
  
  &:focus-visible {
    outline: 2px solid var(--primary-500, #667eea);
    outline-offset: 4px;
    border-radius: var(--radius-md, 12px);
  }

  &:hover, &:focus {
    transform: translateY(-4px);
  }
}

.empty-state {
  padding: 60px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg, 24px);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl, 20px);
  box-shadow: var(--shadow-md, 0 4px 20px rgba(0, 0, 0, 0.05));
  border: 1px solid rgba(255, 255, 255, 0.2);

  [data-theme="dark"] & {
    background: rgba(15, 23, 42, 0.8);
    border-color: rgba(148, 163, 184, 0.1);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .article-list-page {
    padding: var(--spacing-lg, 20px) var(--spacing-md, 16px);
  }

  .search-input {
    max-width: 100%;
  }
}
</style>
