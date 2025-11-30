<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticleDetail, fetchArticles } from '../api/article'
import { fetchCategories } from '../api/category'
import { recordView, getViewCount } from '../api/statistics'
import { renderMarkdown, extractTOC } from '../utils/markdown'
import ReadingProgress from '../components/ReadingProgress.vue'
import CommentSection from '../components/CommentSection.vue'
import ShareButton from '../components/ShareButton.vue'
import ArticleLikeButton from '../components/ArticleLikeButton.vue'
import { updateSEO, resetSEO } from '../utils/seo'
import { ElMessage } from 'element-plus'
import { View } from '@element-plus/icons-vue'

const route = useRoute()
const article = ref(null)
const categories = ref([])
const loading = ref(false)
const toc = ref([])
const relatedArticles = ref([])
const contentRoot = ref(null)
const viewCount = ref(0)
let destroyed = false

// 获取当前页面URL
const getCurrentUrl = computed(() => {
  if (typeof window !== 'undefined' && article.value) {
    return `${window.location.origin}/articles/${article.value.id}`
  }
  return ''
})

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return renderMarkdown(article.value.content)
})

const load = async () => {
  try {
    loading.value = true
    const res = await fetchArticleDetail(route.params.id)
    if (destroyed) return
    
    // 检查响应格式（兼容新旧状态码系统）
    const isSuccess = (res.code >= 200 && res.code < 300) || res.code === 0
    if (isSuccess && res.data) {
      article.value = res.data
      
      // Extract TOC
      if (!destroyed && article.value?.content) {
        toc.value = extractTOC(article.value.content)
      }

      if (!destroyed && !categories.value.length) {
        try {
          const { data } = await fetchCategories()
          categories.value = data || []
        } catch (e) {
          console.warn('获取分类失败:', e)
        }
      }
      const c = categories.value.find(x => x.id === article.value?.categoryId)
      if (!destroyed && c && article.value) article.value.categoryName = c.categoryName

      // Fetch related articles
      if (!destroyed && article.value?.categoryId) {
        try {
          const relatedRes = await fetchArticles({ 
            pageNum: 1, 
            pageSize: 6, 
            categoryId: article.value.categoryId,
            state: '已发布'
          })
          if (!destroyed) {
            const relatedData = (relatedRes.data?.items || relatedRes.data || [])
            relatedArticles.value = relatedData
              .filter(a => a.id !== article.value.id)
              .slice(0, 3)
              .map(a => {
                a.categoryName = c?.categoryName
                return a
              })
          }
        } catch (e) {
          console.warn('获取相关文章失败:', e)
        }
      }
    } else {
      // 响应格式错误或没有数据
      console.error('获取文章失败:', res)
      article.value = null
    }
  } catch (e) {
    // 401错误可能是未登录，但不影响查看文章（后端允许匿名访问）
    if (e?.response?.status === 401) {
      console.warn('未登录访问文章详情，尝试匿名访问')
      // 不显示错误消息，因为允许匿名访问
      article.value = null
    } else {
      console.error('加载文章失败:', e)
      article.value = null
    }
  } finally {
    if (!destroyed) loading.value = false
  }
}

const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
    // 添加焦点以便屏幕阅读器可以跟踪
    element.setAttribute('tabindex', '-1')
    element.focus()
    // 移除tabindex以避免影响正常导航
    setTimeout(() => element.removeAttribute('tabindex'), 1000)
  }
}

const addCopyButtons = () => {
  nextTick(() => {
    if (destroyed) return
    const root = contentRoot.value || document
    const codeBlocks = root.querySelectorAll('.article-content pre')
    codeBlocks.forEach((block) => {
      if (block.querySelector('.copy-btn')) return
      
      const button = document.createElement('button')
      button.className = 'copy-btn'
      button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>'
      button.title = 'Copy code'
      
      button.addEventListener('click', () => {
        const code = block.querySelector('code')
        if (code) {
          navigator.clipboard.writeText(code.textContent || '').then(() => {
            ElMessage.success('Code copied!')
            button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"></polyline></svg>'
            setTimeout(() => {
              button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>'
            }, 2000)
          })
        }
      })
      
      block.style.position = 'relative'
      block.appendChild(button)
    })
  })
}

// 更新SEO信息
watch(() => article.value, (newArticle) => {
  if (newArticle) {
    const seoTitle = newArticle.seoTitle || newArticle.title
    const seoDescription = newArticle.seoDescription || 
      (newArticle.content ? newArticle.content.replace(/<[^>]+>/g, '').substring(0, 150) : '')
    const seoKeywords = newArticle.seoKeywords || (newArticle.categoryName || '')
    
    updateSEO({
      title: `${seoTitle} - BigEvent`,
      description: seoDescription,
      keywords: seoKeywords,
      image: newArticle.coverImg || '',
      url: getCurrentUrl.value
    })
  }
}, { immediate: true })

onMounted(async () => {
  await load()
  addCopyButtons()
  
  // 记录访问量并获取最新访问量
  try {
    await recordView(route.params.id)
    const res = await getViewCount(route.params.id)
    viewCount.value = res.data || 0
  } catch (e) {
    console.error('记录访问量失败:', e)
  }
})

onBeforeUnmount(() => {
  destroyed = true
  const root = contentRoot.value || document
  root.querySelectorAll('.article-content .copy-btn').forEach(btn => btn.remove())
  // 重置SEO
  resetSEO()
})
</script>

<template>
  <ReadingProgress />
  <div class="page-layout" role="main" aria-label="文章详情页面">
    <div class="container with-toc">
      <article ref="contentRoot" class="main-column" :aria-label="article?.title || '文章内容'">
        <el-card class="article-card">
          <div v-loading="loading" role="status" aria-live="polite">
            <template v-if="article">
              <header class="article-header">
                <div class="meta-tags" role="list" aria-label="文章标签">
                  <el-tag v-if="article.categoryName" effect="dark" role="listitem" :aria-label="`分类：${article.categoryName}`">
                    {{ article.categoryName }}
                  </el-tag>
                  <el-tag type="info" effect="plain" role="listitem" :aria-label="`状态：${article.state || 'Draft'}`">
                    {{ article.state || 'Draft' }}
                  </el-tag>
                </div>
                <h1 class="article-title">{{ article.title }}</h1>
                <div class="article-meta">
                  <span>{{ article.createTime ? new Date(article.createTime).toLocaleDateString() : '' }}</span>
                  <span class="meta-divider">·</span>
                  <span class="view-count">
                    <el-icon><View /></el-icon>
                    {{ viewCount }} 次阅读
                  </span>
                </div>
                <div class="article-actions">
                  <ArticleLikeButton 
                    :article-id="article.id"
                    :initial-like-count="article.likeCount || 0"
                    @update:like-count="article.likeCount = $event"
                  />
                  <ShareButton 
                    :title="article.title" 
                    :url="getCurrentUrl" 
                  />
                </div>
              </header>
              
              <el-divider />
              
                <!-- eslint-disable-next-line vue/no-v-html -->
                <div 
                  class="article-content markdown-body" 
                  role="article"
                  :aria-label="`文章内容：${article.title}`"
                  v-html="renderedContent" /> <!-- eslint-disable-line vue/no-v-html -->
            </template>
            <el-empty v-else description="Article not found" role="alert" />
          </div>
        </el-card>

        <!-- 评论区 -->
        <section aria-label="评论区">
          <CommentSection 
            v-if="article" 
            :article-id="Number(route.params.id)" 
          />
        </section>

        <!-- Related Articles -->
        <section v-if="relatedArticles.length > 0" class="related-section" aria-label="相关文章">
          <h2 class="related-title">相关文章</h2>
          <div class="related-grid" role="list">
            <div 
              v-for="item in relatedArticles" 
              :key="item.id" 
              class="related-card"
              role="listitem"
              :aria-label="`相关文章：${item.title}`"
              tabindex="0"
              @click="$router.push(`/articles/${item.id}`)"
              @keyup.enter="$router.push(`/articles/${item.id}`)">
              <el-image 
                :src="item.coverImg" 
                fit="cover" 
                class="related-img" 
                loading="lazy"
                :alt="`${item.title}的封面图`" />
              <div class="related-content">
                <h3 class="related-card-title">{{ item.title }}</h3>
                <p class="related-date">{{ new Date(item.createTime).toLocaleDateString() }}</p>
              </div>
            </div>
          </div>
        </section>
      </article>

      <aside v-if="toc.length > 0" class="toc-column" aria-label="目录导航">
        <nav class="toc-wrapper">
          <h2 class="toc-title">目录</h2>
          <ul class="toc-list" role="list">
            <li 
              v-for="(item, index) in toc" 
              :key="index"
              :class="['toc-item', `level-${item.level}`]"
              role="listitem"
              tabindex="0"
              :aria-label="`跳转到：${item.text}`"
              @click="scrollToHeading(item.id)"
              @keyup.enter="scrollToHeading(item.id)">
              {{ item.text }}
            </li>
          </ul>
        </nav>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.page-layout {
  padding: 40px 0;
  min-height: 80vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.container.with-toc {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.main-column {
  flex: 1;
  min-width: 0;
}

.toc-column {
  width: 280px;
  position: sticky;
  top: 80px;
  flex-shrink: 0;
}

.toc-wrapper {
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.toc-title {
  margin: 0 0 16px 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.toc-item {
  padding: 6px 0;
  cursor: pointer;
  color: var(--el-text-color-regular);
  font-size: 0.9rem;
  transition: color 0.3s;
  line-height: 1.4;
}

.toc-item:hover {
  color: var(--el-color-primary);
}

.toc-item.level-1 { font-weight: 600; margin-top: 8px; }
.toc-item.level-2 { padding-left: 12px; }
.toc-item.level-3 { padding-left: 24px; font-size: 0.85rem; }
.toc-item.level-4 { padding-left: 36px; font-size: 0.85rem; }

.article-card {
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: none;
}

.article-header {
  text-align: center;
  margin-bottom: 30px;
}

.meta-tags {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 20px;
}

.article-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0 0 20px 0;
  line-height: 1.3;
  color: var(--el-text-color-primary);
}

.article-meta {
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.meta-divider {
  color: var(--el-border-color);
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.article-content {
  line-height: 1.8;
  font-size: 1.1rem;
  color: var(--el-text-color-regular);
}

/* Markdown Styles */
.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin-top: 1.5em;
  margin-bottom: 0.8em;
  font-weight: 700;
  line-height: 1.3;
  color: var(--el-text-color-primary);
}

.article-content :deep(h1) { font-size: 2rem; border-bottom: 1px solid var(--el-border-color-lighter); padding-bottom: 0.3em; }
.article-content :deep(h2) { font-size: 1.6rem; }
.article-content :deep(h3) { font-size: 1.3rem; }

.article-content :deep(p) {
  margin-bottom: 1.2em;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 1.2em;
}

.article-content :deep(li) {
  margin-bottom: 0.4em;
}

.article-content :deep(blockquote) {
  margin: 1.2em 0;
  padding: 0.8em 1.2em;
  border-left: 4px solid var(--el-color-primary);
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  border-radius: 4px;
}

.article-content :deep(code) {
  background-color: var(--el-fill-color);
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9em;
  color: var(--el-color-danger);
}

.article-content :deep(pre) {
  background-color: #282c34;
  padding: 1.2em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1.2em 0;
}

.article-content :deep(pre code) {
  background-color: transparent;
  color: #abb2bf;
  padding: 0;
  font-size: 0.95em;
}

.article-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 1.2em 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.article-content :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s;
}

.article-content :deep(a:hover) {
  border-bottom-color: var(--el-color-primary);
}

@media (max-width: 1024px) {
  .container.with-toc {
    flex-direction: column;
  }
  
  .toc-column {
    width: 100%;
    position: static;
    margin-bottom: 30px;
  }
  
  .toc-list {
    max-height: 300px;
  }
}

/* Related Articles */
.related-section {
  margin-top: 40px;
}

.related-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--el-text-color-primary);
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.related-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.related-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.12);
}

.related-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.related-content {
  padding: 16px;
}

.related-card-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-date {
  font-size: 0.85rem;
  color: var(--el-text-color-secondary);
  margin: 0;
}

/* Copy Code Button */
.article-content :deep(pre .copy-btn) {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: #abb2bf;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.article-content :deep(pre .copy-btn:hover) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}
</style>
