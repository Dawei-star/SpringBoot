<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {articleListService, articleCategoryListService} from '@/api/article'
import {debounce} from '@/utils/debounce'
import LoadingSkeleton from '@/components/LoadingSkeleton.vue'
import {clearApiCache} from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const items = ref([])
const categories = ref([])
const years = ref([])
const activeYear = ref('全部')

const fetchCategories = async () => {
  try {
    const res = await articleCategoryListService()
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300 && res.data) {
      categories.value = res.data || []
    } else {
      categories.value = []
    }
  } catch (e) {
    categories.value = []
  }
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 200, state: '已发布' }
    const res = await articleListService(params)
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300 && res.data) {
      items.value = (res.data?.items || []).map(a => ({
      ...a,
      categoryName: (categories.value.find(c => c.id === a.categoryId)?.categoryName) || '未分类'
    }))
    const ys = Array.from(new Set(items.value.map(i => extractYear(i.createTime)).filter(Boolean)))
    years.value = ys.sort((a, b) => Number(b) - Number(a))
    } else {
      items.value = []
      ElMessage.error(res?.message || '归档数据加载失败，请稍后重试')
    }
  } catch (e) {
    items.value = []
    ElMessage.error(e?.message || '归档数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const extractYear = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return isNaN(d) ? '' : String(d.getFullYear())
}

const extractDate = (time) => {
  if (!time) return '--'
  const d = new Date(time)
  if (isNaN(d)) return time
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

const grouped = computed(() => {
  const map = new Map()
  items.value.forEach(i => {
    const y = extractYear(i.createTime) || '未知'
    if (!map.has(y)) map.set(y, [])
    map.get(y).push(i)
  })
  const entries = Array.from(map.entries()).sort((a,b) => Number(b[0]) - Number(a[0]))
  return entries
})

// 过滤后的分组数据（根据选中的年份）
const filteredGrouped = computed(() => {
  if (activeYear.value === '全部') {
    return grouped.value
  }
  return grouped.value.filter(([year]) => year === activeYear.value)
})

// 使用防抖优化年份切换
const handleYearChange = debounce(() => {
  // 年份切换不需要重新请求，只需要过滤显示
}, 100)

watch(activeYear, handleYearChange)

onMounted(async () => {
  await fetchCategories()
  await fetchArticles()
})
</script>

<template>
  <div class="archive-page">
    <div class="archive-header">
      <div class="header-left">
      <h2>文章归档</h2>
        <p class="header-subtitle">按时间查看所有已发布的文章</p>
      </div>
      <div class="year-filter">
        <el-button 
          round 
          size="small" 
          :type="activeYear==='全部'?'primary':'default'" 
          @click="activeYear='全部'">
          全部
        </el-button>
        <el-button 
          v-for="y in years" 
          :key="y" 
          round 
          size="small" 
          :type="activeYear===y?'primary':'default'" 
          @click="activeYear=y">
          {{ y }}
        </el-button>
      </div>
    </div>

    <div class="archive-list">
      <LoadingSkeleton v-if="loading" type="article-list" :count="5" />
      <template v-else>
        <div v-for="([year, list]) in filteredGrouped" :key="year" class="archive-year">
          <div class="year-badge">
            <span class="year-number">{{ year }}</span>
            <span class="year-count">（{{ list.length }} 篇）</span>
          </div>
        <div class="year-items">
            <div 
              v-for="item in list" 
              :key="item.id" 
              class="archive-item" 
              @click="router.push(`/article/detail/${item.id}`)">
            <span class="date">{{ extractDate(item.createTime) }}</span>
            <span class="title">{{ item.title }}</span>
              <el-tag class="category" size="small" effect="plain">{{ item.categoryName }}</el-tag>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && !items.length" description="暂无文章" />
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.archive-page {
  padding: 24px;
  min-height: calc(100vh - 140px);
  max-width: 1400px;
  margin: 0 auto;
}

.archive-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 24px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  
  [data-theme="dark"] & {
    background: rgba(15, 23, 42, 0.8);
  }
  
  .header-left {
    flex: 1;
    
    h2 {
      margin: 0 0 8px 0;
      font-size: 28px;
      font-weight: 700;
      color: #1f2937;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      
      [data-theme="dark"] & {
        color: #f1f5f9;
      }
    }
    
    .header-subtitle {
      margin: 0;
      color: #6b7280;
      font-size: 14px;
      
      [data-theme="dark"] & {
        color: #cbd5e1;
      }
    }
  }
}

.year-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.archive-list {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  min-height: 400px;
  
  [data-theme="dark"] & {
    background: rgba(15, 23, 42, 0.8);
    border-color: rgba(148, 163, 184, 0.12);
  }
}

.archive-year {
  margin-bottom: 32px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.year-badge {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.2);
  
  .year-number {
    font-size: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .year-count {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-secondary);
  }
}

.year-items {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.archive-item {
  display: grid;
  grid-template-columns: 140px 1fr auto;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  
  &:hover {
    background: rgba(102, 126, 234, 0.05);
    border-color: rgba(102, 126, 234, 0.2);
    transform: translateX(4px);
  }
  
  .date {
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 500;
  }
  
  .title {
    color: var(--text-primary);
    font-weight: 500;
    font-size: 15px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .category {
    flex-shrink: 0;
  }
}

@media (max-width: 768px) {
  .archive-page {
    padding: 16px;
  }
  
  .archive-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .archive-item {
    grid-template-columns: 100px 1fr;
    
    .category {
      display: none;
    }
  }
  
  .year-badge .year-number {
    font-size: 20px;
  }
}
</style>

