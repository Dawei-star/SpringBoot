<script setup>
import {Calendar, CollectionTag, Delete, Edit, Plus, Search, Filter, Document} from '@element-plus/icons-vue'
import {computed, ref, onMounted, onActivated, watch} from 'vue'
import {useRouter, useRoute} from "vue-router"
import {
  articleAddService,
  articleCategoryListService,
  articleDeleteService,
  articleListService,
  articleUpdateService
} from "@/api/article"
import {QuillEditor} from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import {useTokenStore} from "@/stores/token"
import {ElMessage, ElMessageBox} from "element-plus"
import defaultCover from '@/assets/cover.jpg'
import {debounce} from "@/utils/debounce"
import LoadingSkeleton from "@/components/LoadingSkeleton.vue"
import {clearApiCache} from "@/utils/request"

const categories = ref([])
const categoryId = ref('')
const state = ref('')
const articles = ref([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const pageSize = ref(12)
const searchKeyword = ref('')
const activeYear = ref('全部')
const yearOptions = ref([])
const showFilters = ref(false)

const visibleDrawer = ref(false)
const drawerTitle = ref('')
const articleModel = ref({
  title: '',
  categoryId: '',
  coverImg: '',
  content: '',
  state: ''
})

const tokenStore = useTokenStore()
const isAuthenticated = computed(() => !!tokenStore.token)
const router = useRouter()
const route = useRoute()

const onSizeChange = (size) => {
  pageSize.value = size
  articleList()
}

const onCurrentChange = (num) => {
  pageNum.value = num
  articleList()
}

const articleCategoryList = async () => {
  try {
    const result = await articleCategoryListService()
    // 成功状态码范围：200-299
    if (result && result.code >= 200 && result.code < 300 && result.data) {
      categories.value = result.data || []
    } else {
      categories.value = []
    }
  } catch (e) {
    categories.value = []
  }
}
articleCategoryList()

const updateYearOptions = () => {
  const years = new Set()
  articles.value.forEach(item => {
    const year = extractYear(item.createTime)
    if (year) years.add(year)
  })
  yearOptions.value = Array.from(years).sort((a, b) => Number(b) - Number(a))
}

const articleList = async (forceRefresh = false) => {
  loading.value = true
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    categoryId: categoryId.value || null,
    state: state.value || null
  }
  try {
    // 强制刷新时禁用缓存
    const result = await articleListService(params, !forceRefresh)
    // 成功状态码范围：200-299
    // 确保响应式更新 - 使用新数组触发响应式
    if (result && result.code >= 200 && result.code < 300 && result.data) {
      articles.value = [...(result.data.items || [])]
      total.value = result.data.total || 0
    } else {
      articles.value = []
      total.value = 0
    }
    articles.value.forEach(article => {
      const matchCategory = categories.value.find(cat => cat.id === article.categoryId)
      if (matchCategory) {
        article.categoryName = matchCategory.categoryName
      }
    })
    updateYearOptions()
  } catch (e) {
    ElMessage.error('文章列表加载失败，请确认后端服务已启动并可访问')
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 使用防抖优化搜索
const debouncedArticleList = debounce(articleList, 300)

// 监听筛选条件变化，使用防抖
watch([categoryId, state], () => {
  pageNum.value = 1
  debouncedArticleList()
})

// 监听路由参数变化，支持从顶部搜索栏跳转
watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword && typeof newKeyword === 'string') {
    searchKeyword.value = newKeyword
  }
}, { immediate: true })

onMounted(() => {
  // 如果路由中有搜索关键词，设置到搜索框
  if (route.query.keyword && typeof route.query.keyword === 'string') {
    searchKeyword.value = route.query.keyword
  }
  articleList()
})

// 当路由激活时（从其他页面返回时）自动刷新
onActivated(() => {
  // 清除缓存并强制刷新列表
  clearApiCache('/api/article')
  articleList(true)
})

const uploadSuccess = (response) => {
  // 文件上传的响应格式可能是 {code, message, data} 或直接是 {data}
  // 兼容两种格式
  if (response && typeof response === 'object') {
    if (response.code !== undefined) {
      // 标准响应格式
      if (response.code >= 200 && response.code < 300 && response.data) {
        articleModel.value.coverImg = response.data
      } else {
        ElMessage.error(response.message || '封面上传失败')
      }
    } else if (response.data) {
      // 直接数据格式（兼容旧格式）
      articleModel.value.coverImg = response.data
    }
  }
}

const addArticle = async (clickState) => {
  try {
    articleModel.value.state = clickState
    // 等待服务器响应（同步）
    const res = await articleAddService(articleModel.value)
    // 清除文章列表缓存
    clearApiCache('/api/article')
    // 等待刷新列表完成（同步，强制刷新）
    await articleList(true)
    // 关闭对话框并重置表单
    visibleDrawer.value = false
    clearData()
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || '文章发布成功')
  } catch (e) {
    ElMessage.error(e?.message || '添加文章失败')
  }
}

const showEditDialog = (row, title) => {
  visibleDrawer.value = true
  drawerTitle.value = title
  articleModel.value = {...row}
}

const showAddDialog = (title) => {
  clearData()
  drawerTitle.value = title
  visibleDrawer.value = true
}

const clearData = () => {
  articleModel.value = {
    title: '',
    categoryId: '',
    coverImg: '',
    content: '',
    state: ''
  }
}

const updateArticle = async (clickState) => {
  try {
    articleModel.value.state = clickState
    // 等待服务器响应（同步）
    const res = await articleUpdateService(articleModel.value)
    // 清除文章列表和详情缓存
    clearApiCache('/api/article')
    // 等待刷新列表完成（同步，强制刷新）
    await articleList(true)
    // 关闭对话框并重置表单
    visibleDrawer.value = false
    clearData()
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || "文章更新成功")
  } catch (e) {
    ElMessage.error(e?.message || '修改文章失败')
  }
}

const deleteArticle = (row) => {
  ElMessageBox.confirm(
      '你确认要删除该文章信息吗？',
      '温馨提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(
      async () => {
        try {
          // 等待服务器响应（同步）
          const res = await articleDeleteService(row.id)
          // 清除文章列表和详情缓存
          clearApiCache('/api/article')
          // 等待刷新列表完成（同步，强制刷新）
          await articleList(true)
          // 显示后端返回的成功消息
          ElMessage.success(res?.message || "文章删除成功")
        } catch (e) {
          ElMessage.error(e?.message || '删除文章失败')
        }
      }
  )
}

const extractYear = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return isNaN(date) ? '' : date.getFullYear().toString()
}

const formatDate = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  if (isNaN(date)) return time
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getDescription = (content) => {
  if (!content) return '这篇文章暂时没有描述。'
  const text = content
      .replace(/<style[\s\S]*?<\/style>/gi, ' ')
      .replace(/<script[\s\S]*?<\/script>/gi, ' ')
      .replace(/<[^>]+>/g, ' ')
      .replace(/&nbsp;/g, ' ')
      .replace(/&amp;/g, '&')
      .replace(/\s+/g, ' ')
      .trim()
  return text.slice(0, 80) + (text.length > 80 ? '...' : '')
}

// 优化计算属性，使用记忆化
const filteredArticles = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  const year = activeYear.value
  if (!keyword && year === '全部') {
    return articles.value
  }
  return articles.value.filter(article => {
    const matchKeyword = !keyword || (article.title || '').toLowerCase().includes(keyword)
    const matchYear = year === '全部' || extractYear(article.createTime) === year
    return matchKeyword && matchYear
  })
})
</script>

<template>
  <div class="modern-article-manage" role="main" aria-label="文章管理页面">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">文章管理</h1>
          <p class="page-subtitle">管理和编辑您的所有文章</p>
        </div>
        <div class="header-right">
          <el-button 
            v-if="isAuthenticated"
            type="primary" 
            :icon="Plus"
            @click="showAddDialog('添加文章')"
            class="add-btn"
            aria-label="新建文章">
            新建文章
          </el-button>
        </div>
      </div>
    </header>

    <!-- 搜索和筛选栏 -->
    <section class="filter-bar" aria-label="搜索和筛选">
      <div class="search-section" role="search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章标题..."
          class="search-input"
          clearable
          aria-label="搜索文章标题"
          role="searchbox">
          <template #prefix>
            <el-icon aria-hidden="true"><Search/></el-icon>
          </template>
        </el-input>
        <el-button 
          :icon="Filter"
          @click="showFilters = !showFilters"
          :type="showFilters ? 'primary' : 'default'"
          :aria-expanded="showFilters"
          aria-label="显示高级筛选"
          aria-controls="advanced-filters">
          筛选
        </el-button>
      </div>

      <div class="year-filter">
        <el-button
          round
          size="small"
          :type="activeYear === '全部' ? 'primary' : 'default'"
          @click="activeYear = '全部'">
          全部
        </el-button>
        <el-button
          v-for="year in yearOptions"
          :key="year"
          round
          size="small"
          :type="activeYear === year ? 'primary' : 'default'"
          @click="activeYear = year">
          {{ year }}
        </el-button>
      </div>

      <!-- 高级筛选 -->
      <transition name="slide-down">
        <div 
          v-if="showFilters" 
          id="advanced-filters"
          class="advanced-filters"
          role="region"
          aria-label="高级筛选选项">
          <el-form :inline="true" class="filter-form" role="form">
            <el-form-item label="分类">
              <el-select 
                v-model="categoryId" 
                placeholder="选择分类" 
                clearable
                style="width: 200px"
                aria-label="按分类筛选">
                <el-option
                  v-for="c in categories"
                  :key="c.id"
                  :label="c.categoryName"
                  :value="c.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select 
                v-model="state" 
                placeholder="选择状态" 
                clearable
                style="width: 150px"
                aria-label="按状态筛选">
                <el-option label="已发布" value="已发布"></el-option>
                <el-option label="草稿" value="草稿"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="articleList"
                aria-label="应用筛选条件">
                应用筛选
              </el-button>
              <el-button 
                @click="categoryId='';state='';articleList()"
                aria-label="重置筛选条件">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </transition>
    </section>

    <!-- 文章网格 -->
    <section class="articles-container" aria-label="文章列表">
      <LoadingSkeleton 
        v-if="loading" 
        type="article-card" 
        :count="6" 
        role="status" 
        aria-live="polite"
        aria-label="正在加载文章" />
      <div 
        v-else-if="filteredArticles.length" 
        class="articles-grid" 
        role="list"
        aria-label="文章列表">
        <div 
          v-for="item in filteredArticles" 
          :key="item.id" 
          class="article-card"
          role="listitem"
          :aria-label="`文章：${item.title}`"
          tabindex="0"
          @click="router.push(`/article/detail/${item.id}`)"
          @keyup.enter="router.push(`/article/detail/${item.id}`)">
          <div class="card-image">
            <el-image 
              :src="item.coverImg || defaultCover" 
              fit="cover"
              class="cover-image"
              loading="lazy"
              :alt="`${item.title}的封面图`">
              <template #error>
                <div class="image-error" role="img" aria-label="图片加载失败">
                  <el-icon aria-hidden="true"><Document /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="card-overlay">
              <div class="overlay-content">
                <el-tag 
                  :type="item.state === '已发布' ? 'success' : 'info'"
                  class="state-tag">
                  {{ item.state || '草稿' }}
                </el-tag>
                <el-tag class="category-tag">
                  {{ item.categoryName || '未分类' }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-description">{{ getDescription(item.content) }}</p>
            <div class="card-meta">
              <div class="meta-item">
                <el-icon><Calendar/></el-icon>
                <span>{{ formatDate(item.createTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><CollectionTag/></el-icon>
                <span>{{ item.categoryName || '未分类' }}</span>
              </div>
            </div>
            <div 
              v-if="isAuthenticated" 
              class="card-actions" 
              @click.stop
              role="group"
              aria-label="文章操作">
              <el-button 
                text 
                type="primary" 
                :icon="Edit"
                @click.stop="showEditDialog(item, '编辑文章')"
                :aria-label="`编辑文章：${item.title}`">
                编辑
              </el-button>
              <el-button 
                text 
                type="danger" 
                :icon="Delete"
                @click.stop="deleteArticle(item)"
                :aria-label="`删除文章：${item.title}`">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无文章数据" role="status" />
    </section>

    <!-- 分页 -->
    <nav v-if="total > 0" class="pagination-wrapper" aria-label="文章分页">
      <el-pagination 
        v-model:current-page="pageNum" 
        v-model:page-size="pageSize" 
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper" 
        :total="total"
        @size-change="onSizeChange"
        @current-change="onCurrentChange" />
    </nav>

    <!-- 编辑抽屉 -->
    <el-drawer 
      v-if="isAuthenticated"
      v-model="visibleDrawer" 
      :title="drawerTitle" 
      direction="rtl" 
      size="50%"
      class="article-drawer">
      <el-form :model="articleModel" label-width="100px" class="article-form">
        <el-form-item label="文章标题">
          <el-input v-model="articleModel.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="文章分类">
          <el-select placeholder="请选择" v-model="articleModel.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="文章封面">
          <el-upload 
            class="avatar-uploader" 
            :auto-upload="true" 
            :show-file-list="false"
            action="/api/upload"
            name="file"
            :headers="{'Authorization':tokenStore.token}"
            :on-success="uploadSuccess">
            <el-image v-if="articleModel.coverImg" :src="articleModel.coverImg" class="avatar"/>
            <el-icon v-else class="avatar-uploader-icon">
              <Plus/>
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="文章内容">
          <div class="editor">
            <QuillEditor
              theme="snow"
              v-model:content="articleModel.content"
              contentType="html">
            </QuillEditor>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="drawerTitle==='添加文章'?addArticle('已发布'):updateArticle('已发布')">
            发布
          </el-button>
          <el-button @click="drawerTitle==='添加文章'?addArticle('草稿'):updateArticle('草稿')">
            保存草稿
          </el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.modern-article-manage {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  // GPU 加速
  transform: translateZ(0);
  will-change: background;
}

[data-theme="dark"] .page-header {
  background: rgba(15, 23, 42, 0.8);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 14px;
}

.add-btn {
  height: 44px;
  padding: 0 24px;
  font-weight: 600;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  }
}

.filter-bar {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  // GPU 加速
  transform: translateZ(0);
}

[data-theme="dark"] .filter-bar {
  background: rgba(15, 23, 42, 0.8);
}

.search-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  max-width: 400px;

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
}

.year-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.advanced-filters {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--surface-border);
}

.filter-form {
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

.articles-container {
  min-height: 400px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-lg);
}

.article-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  will-change: transform, box-shadow;
  transform: translateZ(0);

  &:hover {
    transform: translateY(-4px) translateZ(0);
    box-shadow: var(--shadow-xl);
  }
}

[data-theme="dark"] .article-card {
  background: rgba(15, 23, 42, 0.8);
  border-color: rgba(148, 163, 184, 0.12);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  
  &:hover {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  }
}

.card-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.cover-image {
  width: 100%;
  height: 100%;
  // 优化图片缩放动画
  transition: transform 0.3s ease;
  will-change: transform;
  transform: translateZ(0);

  :deep(img) {
    width: 100%;
    height: 100%;
    object-fit: cover;
    // 优化图片渲染
    image-rendering: -webkit-optimize-contrast;
  }
}

.article-card:hover .cover-image {
  transform: scale(1.05) translateZ(0);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.5) 100%);
  display: flex;
  align-items: flex-end;
  padding: 16px;
  opacity: 0;
  transition: opacity 0.2s ease;
  // GPU 加速
  will-change: opacity;
  transform: translateZ(0);
}

.article-card:hover .card-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.state-tag,
.category-tag {
  backdrop-filter: blur(10px);
  border: none;
}

.card-content {
  padding: var(--spacing-lg);
}

.card-title {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-primary);
  line-height: var(--leading-tight);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-description {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
  margin: 0 0 var(--spacing-md) 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 13px;

  .el-icon {
    font-size: 16px;
  }
}

.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination-wrapper {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

[data-theme="dark"] .pagination-wrapper {
  background: rgba(15, 23, 42, 0.8);
  border-color: rgba(148, 163, 184, 0.12);
}

.article-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 24px;
    padding: 24px;
    border-bottom: 1px solid var(--surface-border);
  }

  :deep(.el-drawer__body) {
    padding: 24px;
  }
}

.article-form {
  :deep(.el-form-item) {
    margin-bottom: var(--spacing-lg);
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--radius-md);
    transition: all var(--transition-base);
    
    &:hover {
      box-shadow: var(--shadow-sm);
    }
    
    &.is-focus {
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }
  }
}

.avatar-uploader {
  :deep(.avatar) {
    width: 178px;
    height: 178px;
    display: block;
    border-radius: 12px;
  }

  :deep(.el-upload) {
    border: 2px dashed var(--surface-border);
    border-radius: 12px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: var(--primary-color);
    }
  }

  :deep(.el-icon.avatar-uploader-icon) {
    font-size: 28px;
    color: var(--text-secondary);
    width: 178px;
    height: 178px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.editor {
  width: 100%;

  :deep(.ql-editor) {
    min-height: 300px;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: opacity 0.2s ease, max-height 0.2s ease;
  max-height: 200px;
  overflow: hidden;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  max-height: 0;
}

@media (max-width: 768px) {
  .modern-article-manage {
    padding: var(--spacing-md);
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .header-left {
    width: 100%;
  }
  
  .add-btn {
    width: 100%;
  }

  .search-section {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    max-width: 100%;
    width: 100%;
  }
  
  .filter-bar {
    padding: var(--spacing-md);
  }
  
  .page-header {
    padding: var(--spacing-md);
  }
  
  .card-content {
    padding: var(--spacing-md);
  }
}
</style>
