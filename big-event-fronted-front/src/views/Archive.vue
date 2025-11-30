<script setup>
import { ref, onMounted, computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { fetchArticles } from '../api/article'
import { fetchCategories } from '../api/category'

const loading = ref(false)
const items = ref([])
const categories = ref([])
const keyword = ref('')
const selectedYear = ref('')



const formatDateTime = (d) => {
  if (!d) return ''
  const s = Array.isArray(d) ? d[0] : d
  const dt = new Date(s)
  const y = dt.getFullYear()
  const m = String(dt.getMonth()+1).padStart(2,'0')
  const day = String(dt.getDate()).padStart(2,'0')
  const h = String(dt.getHours()).padStart(2,'0')
  const min = String(dt.getMinutes()).padStart(2,'0')
  const sec = String(dt.getSeconds()).padStart(2,'0')
  return `${y}-${m}-${day} ${h}:${min}:${sec}`
}

const yearMonthGroups = computed(() => {
  const map = new Map()
  const k = keyword.value.trim().toLowerCase()
  items.value
    .filter(i => !k || (i.title||'').toLowerCase().includes(k))
    .forEach(a => {
      const dt = a.createTime ? new Date(Array.isArray(a.createTime) ? a.createTime[0] : a.createTime) : new Date()
      const year = String(dt.getFullYear())
      const month = String(dt.getMonth()+1).padStart(2,'0')
      if (selectedYear.value && selectedYear.value !== year) return
      if (!map.has(year)) map.set(year, new Map())
      const ym = map.get(year)
      if (!ym.has(month)) ym.set(month, [])
      ym.get(month).push(a)
    })
  // sort years desc and months desc
  return Array.from(map.entries()).sort((a,b) => b[0].localeCompare(a[0])).map(([y, mMap]) => [y, Array.from(mMap.entries()).sort((a,b)=> b[0].localeCompare(a[0]))])
})



const load = async () => {
  try {
    loading.value = true
    const res = await fetchArticles({ pageNum: 1, pageSize: 500 })
    const list = res.data.items || res.data.rows || []
    items.value = list
    const { data } = await fetchCategories()
    categories.value = data || []
    // map categoryName
    items.value.forEach(a => {
      const c = categories.value.find(x => x.id === a.categoryId)
      if (c) a.categoryName = c.categoryName
    })
  } finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <div class="archive-page" role="main" aria-label="文章归档页面">
    <!-- Hero Banner -->
    <div class="archive-hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">时间轴</h1>
        <p class="hero-subtitle">记录每一段时光</p>
      </div>
    </div>

    <!-- Main Content -->
    <div class="archive-container">
      <div class="archive-content">
        <el-skeleton :loading="loading" animated :count="5" role="status" aria-live="polite">
          <template #default>
            <el-empty v-if="yearMonthGroups.length === 0" description="暂无归档" role="status" />
            <div v-else class="timeline-wrapper">
              <!-- Timeline -->
              <div class="timeline" role="list" aria-label="文章归档时间线">
                <div 
                  v-for="([year, months]) in yearMonthGroups" 
                  :key="year" 
                  class="year-section"
                  :aria-label="`${year}年文章`">
                  <!-- Year Marker -->
                  <div class="year-marker">
                    <div class="year-circle"></div>
                    <div class="year-label">{{ year }}</div>
                  </div>
                  
                  <!-- Articles -->
                  <div class="articles-group">
                    <div 
                      v-for="([month, list]) in months" 
                      :key="month" 
                      class="month-group"
                      :aria-label="`${year}年${month}月文章`">
                      <div 
                        v-for="article in list" 
                        :key="article.id" 
                        class="article-item"
                        role="listitem"
                        @click="$router.push(`/articles/${article.id}`)">
                        <div class="article-image">
                          <el-image 
                            :src="article.coverImg || 'https://picsum.photos/200/200?random=' + article.id" 
                            fit="cover"
                            loading="lazy"
                            :alt="article.title"
                            class="cover-img">
                            <template #error>
                              <div class="image-placeholder">
                                <el-icon><Document /></el-icon>
                              </div>
                            </template>
                          </el-image>
                        </div>
                        <div class="article-info">
                          <h3 class="article-title">{{ article.title || 'Untitled' }}</h3>
                          <div class="article-meta">
                            <span class="article-date">{{ formatDateTime(article.createTime) }}</span>
                            <el-tag v-if="article.categoryName" size="small" effect="plain" class="category-tag">
                              {{ article.categoryName }}
                            </el-tag>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.archive-page {
  min-height: 100vh;
  background: #f8fafc;
  
  [data-theme="dark"] & {
    background: #0f172a;
  }
}

// Hero Banner
.archive-hero {
  position: relative;
  height: 300px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 300"><path d="M0,150 Q300,100 600,150 T1200,150 L1200,300 L0,300 Z" fill="rgba(255,255,255,0.1)"/></svg>');
    background-size: cover;
    background-position: bottom;
    opacity: 0.3;
  }
  
  [data-theme="dark"] & {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  }
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
  letter-spacing: -0.02em;
  
  @media (max-width: 768px) {
    font-size: 2.5rem;
  }
}

.hero-subtitle {
  font-size: 1.25rem;
  margin: 0;
  opacity: 0.95;
  font-weight: 400;
  
  @media (max-width: 768px) {
    font-size: 1rem;
  }
}

// Main Content
.archive-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 24px;
  
  @media (max-width: 768px) {
    padding: 40px 16px;
  }
}

.archive-content {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  
  [data-theme="dark"] & {
    background: rgba(30, 41, 59, 0.6);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  }
  
  @media (max-width: 768px) {
    padding: 24px;
    border-radius: 16px;
  }
}

// Timeline
.timeline-wrapper {
  position: relative;
}

.timeline {
  position: relative;
  padding-left: 80px;
  
  @media (max-width: 768px) {
    padding-left: 50px;
  }
  
  &::before {
    content: '';
    position: absolute;
    left: 20px;
    top: 0;
    bottom: 0;
    width: 3px;
    background: linear-gradient(to bottom, #667eea, #764ba2);
    border-radius: 2px;
    
    [data-theme="dark"] & {
      background: linear-gradient(to bottom, #818cf8, #a78bfa);
    }
    
    @media (max-width: 768px) {
      left: 12px;
      width: 2px;
    }
  }
}

.year-section {
  position: relative;
  margin-bottom: 60px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.year-marker {
  position: absolute;
  left: -80px;
  top: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  
  @media (max-width: 768px) {
    left: -50px;
  }
}

.year-circle {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 4px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.4);
  position: relative;
  z-index: 2;
  
  [data-theme="dark"] & {
    border-color: rgba(15, 23, 42, 0.9);
    background: linear-gradient(135deg, #818cf8 0%, #a78bfa 100%);
  }
  
  @media (max-width: 768px) {
    width: 20px;
    height: 20px;
    border-width: 3px;
  }
}

.year-label {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  
  [data-theme="dark"] & {
    color: #f1f5f9;
  }
  
  @media (max-width: 768px) {
    font-size: 1.25rem;
  }
}

.articles-group {
  padding-top: 10px;
}

.month-group {
  margin-bottom: 30px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.article-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  [data-theme="dark"] & {
    background: rgba(51, 65, 85, 0.6);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  &:hover {
    transform: translateX(8px);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
    border-color: rgba(102, 126, 234, 0.3);
    
    [data-theme="dark"] & {
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
    }
  }
  
  &:last-child {
    margin-bottom: 0;
  }
  
  @media (max-width: 768px) {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
}

.article-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: #f3f4f6;
  
  [data-theme="dark"] & {
    background: #1e293b;
  }
  
  @media (max-width: 768px) {
    width: 100%;
    height: 200px;
  }
}

.cover-img {
  width: 100%;
  height: 100%;
  transition: transform 0.6s ease;
  
  .article-item:hover & {
    transform: scale(1.1);
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #9ca3af;
  
  [data-theme="dark"] & {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    color: #64748b;
  }
  
  .el-icon {
    font-size: 40px;
  }
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.article-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: var(--el-text-color-primary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
  
  [data-theme="dark"] & {
    color: #f1f5f9;
  }
  
  .article-item:hover & {
    color: #667eea;
    
    [data-theme="dark"] & {
      color: #818cf8;
    }
  }
  
  @media (max-width: 768px) {
    font-size: 1.1rem;
  }
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.article-date {
  font-size: 0.9rem;
  color: var(--el-text-color-secondary);
  
  [data-theme="dark"] & {
    color: #94a3b8;
  }
}

.category-tag {
  font-size: 0.85rem;
}
</style>
