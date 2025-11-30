<script setup>
import { defineProps } from 'vue'
import { Calendar, View } from '@element-plus/icons-vue'
import { formatDate } from '../utils/date'

defineProps({ item: { type: Object, required: true } })
// 使用公共工具函数 formatDate
</script>

<template>
  <el-card 
    class="article-card" 
    :body-style="{ padding: '0px' }" 
    shadow="hover"
    role="article"
    :aria-label="`文章：${item.title}`">
    <div 
      class="card-content" 
      tabindex="0"
      role="link"
      :aria-label="`查看文章：${item.title}`"
      @click="$router.push(`/articles/${item.id}`)"
      @keyup.enter="$router.push(`/articles/${item.id}`)">
      <div class="thumb">
        <el-image 
          :src="item.coverImg || 'https://picsum.photos/640/360?blur=1'" 
          fit="cover" 
          loading="lazy"
          :alt="`${item.title}的封面图`"
          :lazy="true" />
        <div v-if="item.categoryName" class="category-badge" aria-label="文章分类">
          <el-tag effect="dark" size="small">{{ item.categoryName }}</el-tag>
        </div>
      </div>
      <div class="info">
        <h3 class="title">{{ item.title }}</h3>
        <p class="desc">{{ (item.content||'').replace(/<[^>]+>/g,' ').trim().slice(0,80) }}...</p>
        <div class="footer">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(item.createTime) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ item.views || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.article-card {
  border: none;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  
  [data-theme="dark"] & {
    background: rgba(30, 41, 59, 0.7);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  }
}

.article-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.3);
  
  [data-theme="dark"] & {
    box-shadow: 0 12px 40px rgba(102, 126, 234, 0.3);
    border-color: rgba(102, 126, 234, 0.4);
  }
}

.card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.thumb {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  
  [data-theme="dark"] & {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.2) 100%);
    opacity: 0;
    transition: opacity 0.4s ease;
    z-index: 1;
  }
}

.article-card:hover .thumb::after {
  opacity: 1;
}

.thumb .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  object-fit: cover;
}

.article-card:hover .thumb .el-image {
  transform: scale(1.12);
}

.category-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 2;
  
  :deep(.el-tag) {
    background: rgba(102, 126, 234, 0.9);
    backdrop-filter: blur(10px);
    border: none;
    font-weight: 600;
    font-size: 12px;
    padding: 6px 14px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
}

.info {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0 0 14px 0;
  font-size: 1.3rem;
  font-weight: 700;
  line-height: 1.5;
  color: var(--el-text-color-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
  letter-spacing: -0.01em;
  
  [data-theme="dark"] & {
    color: #f1f5f9;
  }
}

.article-card:hover .title {
  color: #667eea;
  
  [data-theme="dark"] & {
    color: #818cf8;
  }
}

.desc {
  color: var(--el-text-color-secondary);
  font-size: 0.95rem;
  line-height: 1.7;
  margin: 0 0 20px 0;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  
  [data-theme="dark"] & {
    color: #94a3b8;
  }
}

.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--el-text-color-placeholder);
  font-size: 0.875rem;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding-top: 18px;
  margin-top: auto;
  
  [data-theme="dark"] & {
    border-top-color: rgba(255, 255, 255, 0.1);
    color: #64748b;
  }
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.2s ease;
  
  :deep(.el-icon) {
    font-size: 16px;
  }
  
  &:hover {
    color: #667eea;
    
    [data-theme="dark"] & {
      color: #818cf8;
    }
  }
}
</style>
