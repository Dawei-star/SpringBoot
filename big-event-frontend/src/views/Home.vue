<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { getDashboardData } from '@/api/statistics'
import { Document, Management, Message, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const loading = ref(false)
const stats = ref({
  articleCount: 0,
  publishedArticleCount: 0,
  draftArticleCount: 0,
  categoryCount: 0,
  messageCount: 0,
  userCount: 0,
  adminCount: 0
})

const loadData = async () => {
  try {
    loading.value = true
    const res = await getDashboardData()
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300 && res.data) {
      stats.value = res.data
      // 初始化图表
      await nextTick()
      initCharts()
    }
  } catch (e) {
    console.error('加载数据失败:', e)
    // 如果是401错误，会在request拦截器中处理
    if (e?.response?.status === 401) {
      // Token过期，已由拦截器处理跳转登录
      return
    }
  } finally {
    loading.value = false
  }
}

let articleChartInstance = null
let dataChartInstance = null

const initCharts = async () => {
  await nextTick()
  
  // 文章状态饼图
  const articleChartDom = document.getElementById('article-chart')
  if (!articleChartDom) return
  
  // 如果已存在实例，先销毁
  if (articleChartInstance) {
    articleChartInstance.dispose()
  }
  
  articleChartInstance = echarts.init(articleChartDom)
  articleChartInstance.setOption({
    title: {
      text: '文章状态分布',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [{
      name: '文章状态',
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: stats.value.publishedArticleCount, name: '已发布' },
        { value: stats.value.draftArticleCount, name: '草稿' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })

  // 数据统计柱状图
  const dataChartDom = document.getElementById('data-chart')
  if (!dataChartDom) return
  
  // 如果已存在实例，先销毁
  if (dataChartInstance) {
    dataChartInstance.dispose()
  }
  
  dataChartInstance = echarts.init(dataChartDom)
  dataChartInstance.setOption({
    title: {
      text: '数据统计',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['文章', '分类', '留言', '用户']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '数量',
      type: 'bar',
      data: [
        stats.value.articleCount,
        stats.value.categoryCount,
        stats.value.messageCount,
        stats.value.userCount
      ],
      itemStyle: {
        color: echarts.graphic ? new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ]) : '#188df0'
      }
    }]
  })
}

onMounted(() => {
  loadData()
})

onBeforeUnmount(() => {
  // 清理图表实例
  if (articleChartInstance) {
    articleChartInstance.dispose()
    articleChartInstance = null
  }
  if (dataChartInstance) {
    dataChartInstance.dispose()
    dataChartInstance = null
  }
})
</script>

<template>
  <div class="dashboard" role="main" aria-label="数据概览页面">
    <el-card v-loading="loading" class="enhanced-card">
      <template #header>
        <div class="dashboard-header">
          <div class="header-left">
            <h2 class="page-title">数据概览</h2>
            <p class="page-subtitle">实时查看系统数据统计</p>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.articleCount }}</div>
              <div class="stat-label">文章总数</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon size="32"><Management /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.categoryCount }}</div>
              <div class="stat-label">分类数量</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon size="32"><Message /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.messageCount }}</div>
              <div class="stat-label">留言数量</div>
            </div>
          </div>
        </el-card>

        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
              <el-icon size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 图表区域 -->
      <div class="charts-grid">
        <el-card class="chart-card">
          <div id="article-chart" style="width: 100%; height: 300px;"></div>
        </el-card>
        <el-card class="chart-card">
          <div id="data-chart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  padding: var(--spacing-lg);
  min-height: calc(100vh - 200px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    .page-title {
      margin: 0 0 var(--spacing-xs) 0;
      font-size: var(--text-2xl);
      font-weight: var(--font-bold);
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .page-subtitle {
      margin: 0;
      font-size: var(--text-sm);
      color: var(--text-secondary);
    }
  }
}

.enhanced-card {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-base);
  transition: all var(--transition-base);
  overflow: hidden;
  
  &:hover {
    box-shadow: var(--shadow-lg);
  }
  
  :deep(.el-card__header) {
    background: linear-gradient(135deg, var(--gray-50) 0%, var(--gray-100) 100%);
    border-bottom: 1px solid var(--border-light);
    padding: var(--spacing-lg);
  }
  
  :deep(.el-card__body) {
    padding: var(--spacing-lg);
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
  overflow: hidden;
  border: 1px solid var(--border-light);
  background: var(--bg-primary);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
  
  :deep(.el-card__body) {
    padding: var(--spacing-lg);
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: var(--shadow-md);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: var(--spacing-lg);
}

.chart-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  background: var(--bg-primary);
  overflow: hidden;
  transition: all var(--transition-base);
  
  &:hover {
    box-shadow: var(--shadow-md);
  }
  
  :deep(.el-card__body) {
    padding: var(--spacing-lg);
  }
}

// 深色模式适配
[data-theme="dark"] {
  .dashboard-header {
    .page-title {
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .enhanced-card {
    background: rgba(30, 41, 59, 0.6);
    border-color: rgba(148, 163, 184, 0.12);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    
    :deep(.el-card__header) {
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
      border-bottom: 1px solid rgba(148, 163, 184, 0.12);
    }
  }
  
  .stat-card {
    background: rgba(30, 41, 59, 0.6);
    border-color: rgba(148, 163, 184, 0.12);
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
    }
  }
  
  .chart-card {
    background: rgba(30, 41, 59, 0.6);
    border-color: rgba(148, 163, 184, 0.12);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard {
    padding: var(--spacing-md);
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .stat-content {
    gap: var(--spacing-sm);
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
  }
  
  .stat-value {
    font-size: var(--text-2xl);
  }
}
</style>

