<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, ZoomIn, Download, Calendar, Close, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchAlbums, fetchAlbumDetail } from '../api/album'
import { formatDate } from '../utils/date'

const router = useRouter()
const loading = ref(false)
const albums = ref([])
const selectedAlbum = ref(null)
const previewVisible = ref(false)
const previewIndex = ref(0)

// 转换后端数据格式为前端需要的格式
const transformAlbum = (album) => {
  // 后端Album实体使用title字段，前端使用name
  const albumName = album.name || album.title || '未命名相册'
  // 获取封面图片
  const coverImg = album.coverImg || (album.images && album.images.length > 0 ? album.images[0].imageUrl : '')
  // 转换图片列表
  const photos = (album.images || []).map(img => ({
    id: img.id,
    url: img.imageUrl,
    title: img.imageDesc || img.description || `图片 ${img.id}`
  }))
  
  return {
    id: album.id,
    name: albumName,
    description: album.description || '',
    coverImage: coverImg,
    photoCount: photos.length,
    createTime: formatDate(album.createTime),
    photos: photos
  }
}

const loadAlbums = async () => {
  try {
    loading.value = true
    const res = await fetchAlbums()
    if (res && res.data) {
      albums.value = res.data.map(transformAlbum)
    }
  } catch (e) {
    console.error('加载相册失败:', e)
    ElMessage.error('加载相册失败: ' + (e?.message || '未知错误'))
    // 如果API失败，使用空数组而不是模拟数据
    albums.value = []
  } finally {
    loading.value = false
  }
}

const openAlbum = async (album) => {
  try {
    loading.value = true
    // 如果相册已经有图片数据，直接使用
    if (album.photos && album.photos.length > 0) {
      selectedAlbum.value = album
    } else {
      // 否则重新获取相册详情
      const res = await fetchAlbumDetail(album.id)
      if (res && res.data) {
        selectedAlbum.value = transformAlbum(res.data)
      } else {
        ElMessage.warning('相册暂无图片')
      }
    }
  } catch (e) {
    console.error('加载相册详情失败:', e)
    ElMessage.error('加载相册详情失败')
  } finally {
    loading.value = false
  }
}

const closeAlbum = () => {
  selectedAlbum.value = null
  previewIndex.value = 0
  previewVisible.value = false
}

// 处理图片加载错误
const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/400x300?text=Image+Error'
}

const openPreview = (index) => {
  previewIndex.value = index
  previewVisible.value = true
}

const closePreview = () => {
  previewVisible.value = false
}

const prevImage = () => {
  if (previewIndex.value > 0) {
    previewIndex.value--
  }
}

const nextImage = () => {
  if (selectedAlbum.value && previewIndex.value < selectedAlbum.value.photos.length - 1) {
    previewIndex.value++
  }
}

const downloadImage = (url) => {
  const link = document.createElement('a')
  link.href = url
  link.download = 'photo.jpg'
  link.click()
  ElMessage.success('开始下载')
}

onMounted(() => {
  loadAlbums()
})
</script>

<template>
  <div class="gallery-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <header class="page-header">
        <h1 class="page-title">
          <el-icon><Picture /></el-icon>
          相册集
        </h1>
        <p class="page-subtitle">记录生活中的美好瞬间</p>
      </header>

      <!-- 相册列表 -->
      <div v-if="!selectedAlbum" class="albums-view">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="albums.length === 0" class="empty-state">
          <el-empty description="暂无相册" />
        </div>
        
        <div v-else class="albums-grid">
          <div 
            v-for="album in albums" 
            :key="album.id"
            class="album-card"
            @click="openAlbum(album)">
            <div class="album-cover">
              <img 
                :src="album.coverImage || 'https://via.placeholder.com/400x300?text=No+Image'" 
                :alt="album.name"
                loading="lazy"
                @error="handleImageError" />
              <div class="album-overlay">
                <div class="photo-count">
                  <el-icon><Picture /></el-icon>
                  {{ album.photoCount }} 张照片
                </div>
              </div>
            </div>
            <div class="album-info">
              <h3 class="album-name">{{ album.name }}</h3>
              <p class="album-description">{{ album.description }}</p>
              <div class="album-meta">
                <el-icon><Calendar /></el-icon>
                {{ album.createTime }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 相册详情 -->
      <div v-else class="album-detail">
        <div class="detail-header">
          <el-button @click="closeAlbum">
            ← 返回相册列表
          </el-button>
          <div class="detail-title">
            <h2>{{ selectedAlbum.name }}</h2>
            <p>{{ selectedAlbum.description }}</p>
          </div>
        </div>

        <div class="photos-grid">
          <div 
            v-for="(photo, index) in selectedAlbum.photos" 
            :key="photo.id"
            class="photo-item"
            @click="openPreview(index)">
            <img 
              :src="photo.url" 
              :alt="photo.title" 
              loading="lazy"
              @error="handleImageError" />
            <div class="photo-overlay">
              <el-icon class="zoom-icon"><ZoomIn /></el-icon>
              <div class="photo-title">{{ photo.title }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 图片预览 -->
      <el-dialog
        v-model="previewVisible"
        :show-close="false"
        fullscreen
        class="preview-dialog">
        <template #header>
          <div class="preview-header">
            <span class="preview-title">
              {{ selectedAlbum?.photos[previewIndex]?.title }}
            </span>
            <div class="preview-actions">
              <el-button 
                circle 
                @click="downloadImage(selectedAlbum?.photos[previewIndex]?.url)">
                <el-icon><Download /></el-icon>
              </el-button>
              <el-button circle @click="closePreview">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </template>
        
        <div class="preview-content">
          <el-button 
            v-if="previewIndex > 0"
            circle 
            class="nav-btn prev-btn"
            @click="prevImage">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          
          <img 
            :src="selectedAlbum?.photos[previewIndex]?.url || 'https://via.placeholder.com/800x600?text=Image+Error'" 
            :alt="selectedAlbum?.photos[previewIndex]?.title || '图片'"
            class="preview-image"
            @error="handleImageError" />
          
          <el-button 
            v-if="selectedAlbum && previewIndex < selectedAlbum.photos.length - 1"
            circle 
            class="nav-btn next-btn"
            @click="nextImage">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        
        <div class="preview-footer">
          {{ previewIndex + 1 }} / {{ selectedAlbum?.photos.length }}
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.gallery-page {
  min-height: calc(100vh - 140px);
  padding: 40px 0;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 60px;
}

.page-title {
  font-size: 3rem;
  font-weight: 800;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  
  .el-icon {
    font-size: 3rem;
    background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  @media (max-width: 768px) {
    font-size: 2rem;
    
    .el-icon {
      font-size: 2rem;
    }
  }
}

.page-subtitle {
  font-size: 1.2rem;
  color: #666;
  margin: 0;
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 32px;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

.album-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    
    .album-overlay {
      opacity: 1;
    }
  }
}

.album-cover {
  position: relative;
  height: 240px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }
  
  &:hover img {
    transform: scale(1.1);
  }
}

.album-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  display: flex;
  align-items: flex-end;
  padding: 20px;
  opacity: 0;
  transition: opacity 0.3s;
}

.photo-count {
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.album-info {
  padding: 24px;
}

.album-name {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: #1a1a1a;
}

.album-description {
  color: #666;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.album-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 0.9rem;
}

.album-detail {
  animation: fadeIn 0.3s;
}

.detail-header {
  margin-bottom: 40px;
}

.detail-title {
  margin-top: 24px;
  
  h2 {
    font-size: 2.5rem;
    font-weight: 700;
    margin: 0 0 12px 0;
    color: #1a1a1a;
  }
  
  p {
    font-size: 1.1rem;
    color: #666;
    margin: 0;
  }
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

.photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    
    .photo-overlay {
      opacity: 1;
    }
    
    img {
      transform: scale(1.1);
    }
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
  }
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.zoom-icon {
  font-size: 3rem;
  color: white;
  margin-bottom: 12px;
}

.photo-title {
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
}

.preview-dialog {
  :deep(.el-dialog__header) {
    background: rgba(0, 0, 0, 0.9);
    padding: 20px 30px;
    margin: 0;
  }
  
  :deep(.el-dialog__body) {
    background: rgba(0, 0, 0, 0.95);
    padding: 0;
    display: flex;
    flex-direction: column;
    height: calc(100vh - 120px);
  }
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.preview-title {
  color: white;
  font-size: 1.3rem;
  font-weight: 600;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

.preview-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 40px;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
  font-size: 24px;
  
  &.prev-btn {
    left: 40px;
  }
  
  &.next-btn {
    right: 40px;
  }
}

.preview-footer {
  background: rgba(0, 0, 0, 0.9);
  color: white;
  text-align: center;
  padding: 20px;
  font-size: 1.1rem;
}

.loading-state {
  padding: 60px 0;
}

.empty-state {
  padding: 100px 0;
  text-align: center;
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
</style>
