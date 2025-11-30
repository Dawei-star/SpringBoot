<script setup>
import {onMounted, onBeforeUnmount, ref, computed} from "vue";
import {useRoute, useRouter} from "vue-router";
import {articleDetailService} from "@/api/article";
import {ElMessage} from "element-plus";
import {useTokenStore} from "@/stores/token";
import {ArrowLeft} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const article = ref(null)
const progress = ref(0)
const tokenStore = useTokenStore()
const isAuthenticated = computed(() => !!tokenStore.token)

const fetchDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await articleDetailService(id)
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300 && res.data) {
      article.value = res.data
    } else {
      const errorMsg = res?.message || "文章获取失败"
      ElMessage.error(errorMsg)
      article.value = null
      console.error('[ArticleDetail] 获取文章失败:', res)
    }
  } catch (e) {
    // 401错误可能是未登录，但不影响查看文章（后端允许匿名访问）
    if (e?.response?.status === 401) {
      console.warn('[ArticleDetail] 未登录访问文章详情，尝试匿名访问')
      // 清除token后重试（如果有token但无效）
      if (tokenStore.token) {
        tokenStore.removeToken()
      }
      // 不显示错误消息，因为允许匿名访问
      try {
        const id = route.params.id
        const res = await articleDetailService(id)
        if (res && res.code >= 200 && res.code < 300 && res.data) {
          article.value = res.data
        }
      } catch (retryError) {
        ElMessage.error('文章获取失败，请稍后重试')
        console.error('[ArticleDetail] 重试失败:', retryError)
      }
    } else {
      const errorMsg = e?.response?.data?.message || e?.message || "文章获取失败"
      ElMessage.error(errorMsg)
      console.error('[ArticleDetail] 获取文章异常:', e)
    }
    article.value = null
  } finally {
    loading.value = false
  }
}

const formatDate = computed(() => {
  if (!article.value?.createTime) return "--"
  const date = new Date(article.value.createTime)
  if (isNaN(date)) return article.value.createTime
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
})

const updateProgress = () => {
  const el = document.querySelector('.detail-content')
  if (!el) {
    progress.value = 0
    return
  }
  const start = el.offsetTop
  const max = Math.max(1, el.scrollHeight - window.innerHeight)
  const scrolled = Math.max(0, window.scrollY - start)
  progress.value = Math.max(0, Math.min(100, (scrolled / max) * 100))
}

onMounted(() => {
  fetchDetail()
  updateProgress()
  window.addEventListener('scroll', updateProgress, {passive: true})
  window.addEventListener('resize', updateProgress)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateProgress)
  window.removeEventListener('resize', updateProgress)
})
</script>

<template>
  <div class="modern-article-detail" v-loading="loading">
    <!-- 阅读进度条 -->
    <div class="reading-progress">
      <div class="progress-bar" :style="{ width: progress + '%' }"></div>
    </div>

    <!-- 文章容器 -->
    <div class="article-wrapper" v-if="article">
      <!-- 返回按钮 -->
      <el-button 
        :icon="ArrowLeft" 
        text 
        class="back-button"
        @click="router.back()">
        返回
      </el-button>

      <!-- 文章头部 -->
      <header class="article-header">
        <div class="article-meta">
          <el-tag class="category-tag">{{ article.categoryName || '未分类' }}</el-tag>
          <span class="meta-separator">·</span>
          <span class="meta-date">{{ formatDate }}</span>
          <span class="meta-separator">·</span>
          <el-tag 
            :type="article.state === '已发布' ? 'success' : 'info'"
            size="small">
            {{ article.state || '草稿' }}
          </el-tag>
        </div>
        <h1 class="article-title">{{ article.title }}</h1>
      </header>

      <!-- 文章内容 -->
      <div class="article-content">
        <div class="detail-content" v-html="article.content || '暂无内容'"></div>
      </div>

      <!-- 文章底部 -->
      <footer class="article-footer">
        <div class="footer-divider"></div>
        <div class="footer-content">
          <div class="footer-meta">
            <span>发布于 {{ formatDate }}</span>
            <span v-if="article.categoryName">· {{ article.categoryName }}</span>
          </div>
          <el-button 
            type="primary" 
            :icon="ArrowLeft"
            @click="router.back()">
            返回列表
          </el-button>
        </div>
      </footer>
    </div>
  </div>
</template>

<style scoped lang="scss">
.modern-article-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 0;
  position: relative;
}

[data-theme="dark"] .modern-article-detail {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.reading-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.05);
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 0 10px rgba(102, 126, 234, 0.5);
  transition: width 0.1s ease;
}

.article-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 60px 40px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  margin-top: 40px;
  margin-bottom: 40px;
  animation: fadeInUp 0.6s ease-out;
}

[data-theme="dark"] .article-wrapper {
  background: rgba(15, 23, 42, 0.9);
}

.back-button {
  margin-bottom: 24px;
  color: var(--text-secondary);
  font-weight: 500;
  transition: all 0.2s;

  &:hover {
    color: var(--primary-color);
    transform: translateX(-4px);
  }
}

.article-header {
  margin-bottom: 40px;
  padding-bottom: 24px;
  border-bottom: 2px solid var(--surface-border);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.category-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  font-weight: 500;
  padding: 6px 16px;
  border-radius: 20px;
}

.meta-separator {
  color: var(--text-secondary);
  font-size: 14px;
}

.meta-date {
  color: var(--text-secondary);
  font-size: 14px;
}

.article-title {
  font-size: 42px;
  font-weight: 800;
  line-height: 1.2;
  margin: 0;
  background: linear-gradient(135deg, #1f2937 0%, #4b5563 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

[data-theme="dark"] .article-title {
  background: linear-gradient(135deg, #ffffff 0%, #e5e7eb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.article-content {
  margin: 40px 0;
}

.detail-content {
  font-size: 18px;
  line-height: 1.9;
  color: var(--text-primary);
  word-wrap: break-word;

  :deep(p) {
    margin: 20px 0;
    color: var(--text-primary);
  }

  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin: 32px 0 16px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.3;
  }

  :deep(h1) {
    font-size: 32px;
  }

  :deep(h2) {
    font-size: 28px;
  }

  :deep(h3) {
    font-size: 24px;
  }

  :deep(ul), :deep(ol) {
    padding-left: 24px;
    margin: 20px 0;
  }

  :deep(li) {
    margin: 8px 0;
    line-height: 1.8;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    margin: 24px 0;
    display: block;
  }

  :deep(blockquote) {
    margin: 24px 0;
    padding: 20px 24px;
    border-left: 4px solid var(--primary-color);
    background: rgba(102, 126, 234, 0.05);
    border-radius: 12px;
    font-style: italic;
    color: var(--text-secondary);
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 24px 0;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  :deep(th), :deep(td) {
    border: 1px solid var(--surface-border);
    padding: 12px 16px;
    text-align: left;
  }

  :deep(th) {
    background: var(--gray-100);
    font-weight: 600;
    color: var(--text-primary);
  }

  :deep(pre) {
    background: #1e293b;
    color: #e2e8f0;
    padding: 24px;
    border-radius: 12px;
    overflow-x: auto;
    margin: 24px 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(code) {
    background: rgba(30, 41, 59, 0.8);
    color: #e2e8f0;
    padding: 4px 8px;
    border-radius: 6px;
    font-family: 'Fira Code', Consolas, monospace;
    font-size: 0.9em;
  }

  :deep(pre code) {
    background: transparent;
    padding: 0;
  }

  :deep(a) {
    color: var(--primary-color);
    text-decoration: none;
    border-bottom: 1px solid transparent;
    transition: all 0.2s;

    &:hover {
      border-bottom-color: var(--primary-color);
    }
  }

  :deep(hr) {
    border: none;
    height: 2px;
    background: linear-gradient(90deg, transparent, var(--surface-border), transparent);
    margin: 32px 0;
  }
}

.article-footer {
  margin-top: 60px;
  padding-top: 32px;
}

.footer-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--surface-border), transparent);
  margin-bottom: 24px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-meta {
  color: var(--text-secondary);
  font-size: 14px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

@media (max-width: 768px) {
  .article-wrapper {
    padding: 32px 20px;
    margin: 20px;
    border-radius: 16px;
  }

  .article-title {
    font-size: 28px;
  }

  .detail-content {
    font-size: 16px;

    :deep(h1) {
      font-size: 24px;
    }

    :deep(h2) {
      font-size: 20px;
    }

    :deep(h3) {
      font-size: 18px;
    }
  }

  .footer-content {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
