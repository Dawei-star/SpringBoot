<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addAlbum, updateAlbum, getAlbumById } from '@/api/album'
import { Plus, Delete, Upload } from '@element-plus/icons-vue'
import { useTokenStore } from '@/stores/token'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()

const loading = ref(false)
const uploading = ref(false)
const isEdit = ref(false)
const albumId = ref(null)

const albumForm = ref({
  title: '',
  description: '',
  coverImg: '',
  images: []
})

const formRules = {
  title: [
    { required: true, message: '请输入相册标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入相册描述', trigger: 'blur' },
    { min: 5, max: 200, message: '描述长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  coverImg: [
    { required: true, message: '请上传封面图片', trigger: 'change' }
  ]
}

const formRef = ref(null)

// 加载相册数据（编辑模式）
const loadAlbum = async () => {
  try {
    loading.value = true
    const res = await getAlbumById(albumId.value)
    const album = res.data
    // 转换后端数据格式为前端格式
    const images = (album.images || []).map(img => ({
      url: img.imageUrl || img.url,
      title: img.imageName || img.title || '',
      description: img.imageDesc || img.description || ''
    }))
    albumForm.value = {
      title: album.title || '',
      description: album.description || '',
      coverImg: album.coverImg || '',
      images: images
    }
  } catch (e) {
    ElMessage.error('加载相册失败')
    router.push('/album/manage')
  } finally {
    loading.value = false
  }
}

// 封面上传成功
const handleCoverSuccess = (response) => {
  // 文件上传的响应格式可能是 {code, message, data} 或直接是 {data}
  // 兼容两种格式
  if (response && typeof response === 'object') {
    if (response.code !== undefined) {
      // 标准响应格式
      if (response.code >= 200 && response.code < 300 && response.data) {
        albumForm.value.coverImg = response.data
        ElMessage.success(response.message || '封面上传成功')
      } else {
        ElMessage.error(response.message || '封面上传失败')
      }
    } else if (response.data) {
      // 直接数据格式（兼容旧格式）
      albumForm.value.coverImg = response.data
      ElMessage.success('封面上传成功')
    }
  }
}

// 封面上传前验证
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 相册图片上传成功
const handleImageSuccess = (response) => {
  // 文件上传的响应格式可能是 {code, message, data} 或直接是 {data}
  // 兼容两种格式
  if (response && typeof response === 'object') {
    let imageUrl = null
    if (response.code !== undefined) {
      // 标准响应格式
      if (response.code >= 200 && response.code < 300 && response.data) {
        imageUrl = response.data
        ElMessage.success(response.message || '图片上传成功')
      } else {
        ElMessage.error(response.message || '图片上传失败')
        return
      }
    } else if (response.data) {
      // 直接数据格式（兼容旧格式）
      imageUrl = response.data
      ElMessage.success('图片上传成功')
    }
    
    if (imageUrl) {
      albumForm.value.images.push({
        url: imageUrl,
        title: '',
        description: ''
      })
    }
  }
}

// 删除相册图片
const handleRemoveImage = (index) => {
  albumForm.value.images.splice(index, 1)
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (albumForm.value.images.length === 0) {
      ElMessage.warning('请至少上传一张照片')
      return
    }

    loading.value = true
    
    // 转换图片数据格式，确保与后端实体类匹配
    const images = albumForm.value.images.map(img => ({
      imageUrl: img.url,
      imageName: img.title || '',
      imageDesc: img.description || ''
    }))
    
    const albumData = {
      title: albumForm.value.title,
      description: albumForm.value.description,
      coverImg: albumForm.value.coverImg,
      images: images
    }
    
    if (isEdit.value) {
      albumData.id = albumId.value
      // 等待服务器响应（同步）
      const res = await updateAlbum(albumData)
      ElMessage.success(res?.message || '相册更新成功')
    } else {
      // 等待服务器响应（同步）
      const res = await addAlbum(albumData)
      ElMessage.success(res?.message || '相册创建成功')
    }
    
    // 跳转到管理页面（管理页面会自动刷新）
    router.push('/album/manage')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/album/manage')
}

onMounted(() => {
  // 检查是否为编辑模式
  if (route.params.id) {
    isEdit.value = true
    albumId.value = route.params.id
    loadAlbum()
  }
})
</script>

<template>
  <div class="album-form-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '编辑相册' : '新建相册' }}</h2>
        </div>
      </template>

      <el-form 
        ref="formRef"
        :model="albumForm" 
        :rules="formRules"
        label-width="100px"
        label-position="top">
        
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="相册标题" prop="title">
          <el-input 
            v-model="albumForm.title" 
            placeholder="请输入相册标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="相册描述" prop="description">
          <el-input 
            v-model="albumForm.description" 
            type="textarea"
            :rows="4"
            placeholder="请输入相册描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 封面图片 -->
        <el-divider content-position="left">封面图片</el-divider>
        
        <el-form-item label="封面" prop="coverImg">
          <div class="cover-upload-wrapper">
            <el-upload
              class="cover-uploader"
              action="/api/upload"
              :headers="{ Authorization: tokenStore.token }"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
              :before-upload="beforeCoverUpload">
              <img v-if="albumForm.coverImg" :src="albumForm.coverImg" class="cover-image" />
              <div v-else class="cover-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传封面</div>
                <div class="upload-hint">建议尺寸 800x600，不超过 2MB</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 相册图片 -->
        <el-divider content-position="left">相册图片</el-divider>
        
        <el-form-item label="照片">
          <div class="images-grid">
            <!-- 已上传的图片 -->
            <div 
              v-for="(image, index) in albumForm.images" 
              :key="index"
              class="image-item">
              <img :src="image.url" class="image-preview" />
              <div class="image-overlay">
                <el-button 
                  circle 
                  type="danger" 
                  :icon="Delete"
                  @click="handleRemoveImage(index)" />
              </div>
              <el-input 
                v-model="image.title"
                placeholder="图片标题（可选）"
                size="small"
                class="image-title-input"
              />
            </div>

            <!-- 上传按钮 -->
            <el-upload
              class="image-uploader"
              action="/api/upload"
              :headers="{ Authorization: tokenStore.token }"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeCoverUpload"
              multiple>
              <div class="upload-box">
                <el-icon class="upload-icon"><Upload /></el-icon>
                <div class="upload-text">上传照片</div>
              </div>
            </el-upload>
          </div>
          <div class="upload-tips">
            <el-icon><InfoFilled /></el-icon>
            支持批量上传，单张图片不超过 2MB
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建相册' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.album-form-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  h2 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
    color: #1a1a1a;
  }
}

.cover-upload-wrapper {
  width: 100%;
}

.cover-uploader {
  :deep(.el-upload) {
    border: 2px dashed #d9d9d9;
    border-radius: 12px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    width: 400px;
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #0ea5e9;
    }
  }
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  text-align: center;
  padding: 40px;
}

.upload-icon {
  font-size: 48px;
  color: #8c939d;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  width: 100%;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

    .image-overlay {
      opacity: 1;
    }
  }
}

.image-preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 50px;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-title-input {
  margin-top: 8px;
}

.image-uploader {
  :deep(.el-upload) {
    width: 100%;
  }
}

.upload-box {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #0ea5e9;
    background: #f0f9ff;
  }

  .upload-icon {
    font-size: 32px;
    color: #8c939d;
    margin-bottom: 8px;
  }

  .upload-text {
    font-size: 14px;
    color: #606266;
  }
}

.upload-tips {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #1a1a1a;
}
</style>
