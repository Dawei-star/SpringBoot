<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { fetchArticles } from '../api/article'
import { fetchCategories } from '../api/category'
import ArticleCard from '../components/ArticleCard.vue'
import { TrendCharts, Document, View, ChatDotRound, Picture, Folder } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const featuredArticles = ref([])
const latestArticles = ref([])
const categories = ref([])
const stats = ref({
  totalArticles: 0,
  totalViews: 0,
  totalComments: 0,
  totalPhotos: 0
})

const currentBannerIndex = ref(0)
let bannerTimer = null

const banners = [
  {
    title: '欢迎来到 BigEvent',
    subtitle: '分享技术，记录生活',
    image: 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1200',
    gradient: 'linear-gradient(135deg, rgba(14, 165, 233, 0.85), rgba(2, 132, 199, 0.85))'
  },
  {
    title: '探索精彩内容',
    subtitle: '发现更多有趣的文章',
    image: 'https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=1200',
    gradient: 'linear-gradient(135deg, rgba(251, 146, 60, 0.85), rgba(249, 115, 22, 0.85))'
  },
  {
    title: '记录美好瞬间',
    subtitle: '用相册保存珍贵回忆',
    image: 'https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=1200',
    gradient: 'linear-gradient(135deg, rgba(168, 162, 158, 0.85), rgba(120, 113, 108, 0.85))'
  }
]

const loadData = async () => {
  try {
    loading.value = true
    
    // 获取特色文章（最新的3篇）
    const featuredRes = await fetchArticles({
      pageNum: 1,
      pageSize: 3,
      state: '已发布'
    })
    featuredArticles.value = featuredRes.data.items || featuredRes.data.rows || []
    
    // 获取最新文章（接下来的6篇）
    const latestRes = await fetchArticles({
      pageNum: 1,
      pageSize: 9,
      state: '已发布'
    })
    const allArticles = latestRes.data.items || latestRes.data.rows || []
    latestArticles.value = allArticles.slice(3, 9)
    
    // 获取分类
    const { data } = await fetchCategories()
    categories.value = data || []
    
    // 模拟统计数据
    stats.value = {
      totalArticles: latestRes.data.total || 0,
      totalViews: Math.floor(Math.random() * 10000) + 5000,
      totalComments: Math.floor(Math.random() * 500) + 100,
      totalPhotos: Math.floor(Math.random() * 200) + 50
    }
  } catch (e) {
    // 加载数据失败，静默处理
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  
  // 轮播图自动切换
  bannerTimer = setInterval(() => {
    currentBannerIndex.value = (currentBannerIndex.value + 1) % banners.length
  }, 5000)
})

onBeforeUnmount(() => {
  if (bannerTimer) {
    clearInterval(bannerTimer)
    bannerTimer = null
  }
})

const currentBanner = computed(() => banners[currentBannerIndex.value])

const goToBanner = (index) => {
  currentBannerIndex.value = index
}

const navigateToArticle = (id) => {
  router.push(`/articles/${id}`)
}

const navigateToCategory = (id) => {
  router.push({ path: '/articles', query: { categoryId: id } })
}
</script>

<template>
  <div class="home-page">
    <!-- 轮播横幅 -->
    <section class="hero-banner">
      <transition name="fade" mode="out-in">
        <div 
          :key="currentBannerIndex" 
          class="banner-slide"
          :style="{ 
            backgroundImage: `${currentBanner.gradient}, url(${currentBanner.image})` 
          }">
          <div class="banner-content">
            <h1 class="banner-title">{{ currentBanner.title }}</h1>
            <p class="banner-subtitle">{{ currentBanner.subtitle }}</p>
            <div class="banner-actions">
              <el-button type="primary" size="large" @click="router.push('/articles')">
                <el-icon><Document /></el-icon>
                浏览文章
              </el-button>
              <el-button size="large" @click="router.push('/gallery')">
                <el-icon><Picture /></el-icon>
                查看相册
              </el-button>
            </div>
          </div>
          <div class="banner-indicators">
            <span 
              v-for="(_, index) in banners" 
              :key="index"
              class="indicator"
              :class="{ active: index === currentBannerIndex }"
              @click="goToBanner(index)" />
          </div>
        </div>
      </transition>
    </section>

    <div class="home-container">
      <!-- 统计卡片 -->
      <section class="stats-section">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%)">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalArticles }}</div>
              <div class="stat-label">文章总数</div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fb923c 0%, #f97316 100%)">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalViews }}</div>
              <div class="stat-label">总浏览量</div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #a8a29e 0%, #78716c 100%)">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalComments }}</div>
              <div class="stat-label">评论数量</div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #10b981 0%, #059669 100%)">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalPhotos }}</div>
              <div class="stat-label">相册照片</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 特色文章 -->
      <section v-if="featuredArticles.length" class="featured-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><TrendCharts /></el-icon>
            特色推荐
          </h2>
          <el-button text type="primary" @click="router.push('/articles')">
            查看更多 →
          </el-button>
        </div>
        <div class="featured-grid">
          <ArticleCard
            v-for="article in featuredArticles"
            :key="article.id"
            :item="article"
            class="featured-card"
            @click="navigateToArticle(article.id)" />
        </div>
      </section>

      <!-- 最新文章 -->
      <section v-if="latestArticles.length" class="latest-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Document /></el-icon>
            最新文章
          </h2>
        </div>
        <div class="latest-grid">
          <ArticleCard
            v-for="article in latestArticles"
            :key="article.id"
            :item="article"
            @click="navigateToArticle(article.id)" />
        </div>
      </section>

      <!-- 分类导航 -->
      <section v-if="categories.length" class="categories-section">
        <div class="section-header">
          <h2 class="section-title">文章分类</h2>
        </div>
        <div class="categories-grid">
          <div 
            v-for="category in categories" 
            :key="category.id"
            class="category-card"
            @click="navigateToCategory(category.id)">
            <div class="category-icon">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="category-name">{{ category.categoryName }}</div>
            <div class="category-count">{{ category.articleCount || 0 }} 篇</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
}

.hero-banner {
  position: relative;
  height: 500px;
  overflow: hidden;
  margin-bottom: 60px;
  
  @media (max-width: 768px) {
    height: 400px;
  }
}

.banner-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.banner-content {
  text-align: center;
  color: white;
  z-index: 2;
  padding: 0 20px;
}

.banner-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin: 0 0 16px 0;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: slideInDown 0.8s ease-out;
  
  @media (max-width: 768px) {
    font-size: 2rem;
  }
}

.banner-subtitle {
  font-size: 1.5rem;
  margin: 0 0 32px 0;
  opacity: 0.95;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  animation: slideInUp 0.8s ease-out 0.2s both;
  
  @media (max-width: 768px) {
    font-size: 1.1rem;
  }
}

.banner-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  animation: fadeIn 0.8s ease-out 0.4s both;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
  }
}

.banner-indicators {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 3;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    background: white;
    width: 32px;
    border-radius: 6px;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
  }
}

.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 60px;
}

.stats-section {
  margin-bottom: 60px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.stat-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  
  .el-icon {
    font-size: 2rem;
    color: #0ea5e9;
  }
}

.featured-section {
  margin-bottom: 60px;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.featured-card {
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover {
    transform: translateY(-8px);
  }
}

.latest-section {
  margin-bottom: 60px;
}

.latest-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.categories-section {
  margin-bottom: 60px;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.category-card {
  background: white;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    
    .category-icon {
      transform: scale(1.1);
    }
  }
}

.category-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  transition: transform 0.3s;
}

.category-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.category-count {
  font-size: 0.9rem;
  color: #666;
}

// 动画
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
