<script setup>
import {computed, onBeforeUnmount, onMounted, ref, shallowRef, watch} from "vue"
import {ElMessage} from "element-plus"
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from "@wangeditor/editor-for-vue"
import TurndownService from "turndown"
import {articleAddService, articleCategoryListService} from "@/api/article"
import {useTokenStore} from "@/stores/token"
import {PictureFilled, UploadFilled} from "@element-plus/icons-vue"
import {debounce} from "@/utils/debounce"
import {clearApiCache} from "@/utils/request"
import {useRouter} from "vue-router"

const tokenStore = useTokenStore()
const router = useRouter()

const createDefaultArticle = () => ({
  title: '',
  categoryId: '',
  coverImg: '',
  content: '',
  state: '已发布'
})

const categories = ref([])
const articleModel = ref(createDefaultArticle())
const loading = ref(false)
const categoryLoading = ref(false)
const coverUploading = ref(false)
const editorRef = shallowRef(null)

const stripHtml = (val = '') => val
  .replace(/<style[\s\S]*?<\/style>/gi, ' ')
  .replace(/<script[\s\S]*?<\/script>/gi, ' ')
  .replace(/<[^>]+>/g, ' ')
  .replace(/&nbsp;/g, ' ')
  .replace(/\s+/g, ' ')
  .trim()

const contentLength = computed(() => {
  const editor = editorRef.value
  if (editor && typeof editor.getText === 'function') {
    const txt = editor.getText().trim()
    return txt.length
  }
  return stripHtml(articleModel.value.content).length
})

const canPublish = computed(() => Boolean(
  articleModel.value.title.trim() &&
  articleModel.value.categoryId &&
  articleModel.value.coverImg &&
  contentLength.value >= 10
))

const fetchCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await articleCategoryListService()
    const data = (res && res.code >= 200 && res.code < 300) ? res.data : []
    categories.value = data
  } catch (err) {
    ElMessage.error('获取文章类型失败，请稍后重试')
  } finally {
    categoryLoading.value = false
  }
}

const handleUploadSuccess = (response) => {
  // 文件上传的响应格式可能是 {code, message, data} 或直接是 {data}
  // 兼容两种格式
  if (response && typeof response === 'object') {
    if (response.code !== undefined) {
      // 标准响应格式
      if (response.code >= 200 && response.code < 300 && response.data) {
        articleModel.value.coverImg = response.data
        coverUploading.value = false
        ElMessage.success(response.message || '封面上传成功')
      } else {
        coverUploading.value = false
        ElMessage.error(response.message || '封面上传失败')
      }
    } else if (response.data) {
      // 直接数据格式（兼容旧格式）
      articleModel.value.coverImg = response.data
      coverUploading.value = false
      ElMessage.success('封面上传成功')
    }
  }
}

const handleUploadError = () => {
  coverUploading.value = false
  ElMessage.error('封面上传失败，请重试')
}

const beforeUpload = (rawFile) => {
  const isImage = ['image/jpeg', 'image/png'].includes(rawFile.type)
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('仅支持 JPG/PNG 格式')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('封面大小需小于 2MB')
    return false
  }
  coverUploading.value = true
  return true
}

const handleCoverRemove = () => {
  articleModel.value.coverImg = ''
  coverUploading.value = false
}

const createUploadConfig = ({maxFileSize, allowedFileTypes}) => ({
  server: '/api/upload',
  fieldName: 'file',
  timeout: 15000,
  headers: {
    Authorization: tokenStore.token
  },
  maxFileSize,
  allowedFileTypes,
  customInsert(res, insertFn) {
    if (res && res.data) {
      insertFn(res.data)
      return
    }
    ElMessage.error('内容上传失败，请重试')
  },
  onError(file, err) {
    console.error('editor upload failed', err)
    ElMessage.error('上传失败，请检查网络')
  }
})

const toolbarConfig = {
  toolbarKeys: [
    'headerSelect', 'bold', 'italic', 'underline', 'through',
    'color', 'bgColor', 'fontSize', 'fontFamily', 'lineHeight', '|',
    'bulletedList', 'numberedList', 'todo', 'blockquote', 'codeBlock', '|',
    'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyJustify', '|',
    'insertLink', 'insertTable', 'divider', 'emotion', 'insertImage', 'uploadVideo', 'fullScreen'
  ]
}

const editorConfig = {
  placeholder: '请直接开始创作，支持粘贴排版与多媒体内容',
  scroll: false,
  MENU_CONF: {
    uploadImage: createUploadConfig({
      maxFileSize: 2 * 1024 * 1024,
      allowedFileTypes: ['image/jpeg', 'image/png', 'image/gif']
    }),
    uploadVideo: createUploadConfig({
      maxFileSize: 20 * 1024 * 1024,
      allowedFileTypes: ['video/mp4', 'video/quicktime']
    })
  }
}

const handleEditorCreated = (editor) => {
  editorRef.value = editor
}

// 自动保存草稿（防抖）
const autoSaveDraft = debounce(async () => {
  if (!articleModel.value.title.trim() && !articleModel.value.content.trim()) {
    return // 没有内容不保存
  }
  try {
    // 使用localStorage保存草稿
    const draft = {
      title: articleModel.value.title,
      categoryId: articleModel.value.categoryId,
      coverImg: articleModel.value.coverImg,
      content: articleModel.value.content,
      savedAt: new Date().toISOString()
    }
    localStorage.setItem('article_draft', JSON.stringify(draft))
  } catch (e) {
    console.warn('自动保存草稿失败', e)
  }
}, 2000) // 2秒后自动保存

// 加载草稿
const loadDraft = () => {
  try {
    const draftStr = localStorage.getItem('article_draft')
    if (draftStr) {
      const draft = JSON.parse(draftStr)
      // 检查草稿是否过期（7天）
      const savedAt = new Date(draft.savedAt)
      const now = new Date()
      const daysDiff = (now - savedAt) / (1000 * 60 * 60 * 24)
      
      if (daysDiff < 7) {
        articleModel.value.title = draft.title || ''
        articleModel.value.categoryId = draft.categoryId || ''
        articleModel.value.coverImg = draft.coverImg || ''
        articleModel.value.content = draft.content || ''
        ElMessage.info('已恢复上次的草稿内容')
      } else {
        localStorage.removeItem('article_draft')
      }
    }
  } catch (e) {
    console.warn('加载草稿失败', e)
  }
}

const handlePublish = async (state) => {
  if (!articleModel.value.title.trim()) {
    ElMessage.error('请先填写文章标题')
    return
  }
  if (!articleModel.value.categoryId) {
    ElMessage.error('请选择文章类型')
    return
  }
  if (state === '已发布' && !canPublish.value) {
    ElMessage.error('请完善封面与正文内容，至少 10 字')
    return
  }
  articleModel.value.state = state
  loading.value = true
  try {
    // 等待服务器响应（同步）
    const res = await articleAddService(articleModel.value)
    // 清除草稿和缓存
    localStorage.removeItem('article_draft')
    clearApiCache('/api/article')
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || (state === '已发布' ? '发布成功' : '已保存到草稿箱'))
    // 跳转到文章管理页面（会自动刷新列表）
    router.push('/article/manage')
  } catch (e) {
    ElMessage.error(e?.message || '发布失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 监听内容变化，自动保存草稿
watch(
  () => [articleModel.value.title, articleModel.value.content, articleModel.value.categoryId, articleModel.value.coverImg],
  () => {
    autoSaveDraft()
  },
  { deep: true }
)

onMounted(() => {
  fetchCategories()
  loadDraft()
})

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
    editorRef.value = null
  }
})
</script>

<template>
  <div class="publish-page">
    <div class="publish-container">
      <div class="page-header">
        <h2>文章发布</h2>
        <p>创作优质内容，分享你的想法</p>
      </div>

      <el-card class="publish-card">
        <div class="form-section">
          <el-form :model="articleModel" label-position="top">
            <el-form-item label="文章标题" required>
              <el-input
                  v-model="articleModel.title"
                  maxlength="100"
                  placeholder="请输入文章标题（最多100个字符）"
                  size="large"
              />
            </el-form-item>

            <el-form-item label="文章分类" required>
              <el-select
                  v-model="articleModel.categoryId"
                  placeholder="请选择文章类型"
                  size="large"
                  clearable
                  :loading="categoryLoading"
                  style="width: 100%"
              >
                <el-option
                    v-for="item in categories"
                    :key="item.id"
                    :label="item.categoryName"
                    :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="文章封面" required>
              <div class="cover-upload">
                <el-upload
                    class="cover-uploader"
                    :auto-upload="true"
                    :show-file-list="false"
                    action="/api/upload"
                    name="file"
                    :headers="{'Authorization': tokenStore.token}"
                    :on-success="handleUploadSuccess"
                    :on-error="handleUploadError"
                    :before-upload="beforeUpload"
                    :disabled="coverUploading || loading"
                >
                  <div class="cover-inner" v-if="!articleModel.coverImg">
                    <el-icon>
                      <UploadFilled/>
                    </el-icon>
                    <p>{{ coverUploading ? '上传中...' : '点击上传封面' }}</p>
                    <small>支持 jpg/png，大小不超过 2MB</small>
                  </div>
                  <el-image
                      v-else
                      :src="articleModel.coverImg"
                      fit="cover"
                      class="cover-preview"
                  />
                </el-upload>
                <div class="cover-actions" v-if="articleModel.coverImg">
                  <el-button text type="primary" @click="handleCoverRemove">重新上传</el-button>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="文章内容" required>
              <div class="editor-wrapper">
                <Toolbar
                    class="editor-toolbar"
                    :editor="editorRef"
                    :default-config="toolbarConfig"
                    mode="default"
                />
                <Editor
                    class="rich-editor"
                    v-model="articleModel.content"
                    :default-config="editorConfig"
                    mode="default"
                    @onCreated="handleEditorCreated"
                />
              </div>
            </el-form-item>
          </el-form>
        </div>

        <div class="action-bar">
          <div class="action-left">
            <el-icon><PictureFilled/></el-icon>
            <span>支持拖拽图片至编辑器自动上传</span>
            <span class="auto-save-hint" v-if="articleModel.title || articleModel.content">
              <el-icon><UploadFilled/></el-icon>
              内容已自动保存
            </span>
          </div>
          <div class="action-right">
            <el-button @click="handlePublish('草稿')" :loading="loading">保存草稿</el-button>
            <el-button
                type="primary"
                @click="handlePublish('已发布')"
                :loading="loading"
                :disabled="loading || !canPublish"
            >
              发布文章
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.publish-page {
  padding: 24px;
  min-height: 100vh;
  background: var(--gray-50);
}

.publish-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
  }

  p {
    margin: 0;
    color: var(--text-secondary);
    font-size: 14px;
  }
}

.publish-card {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid var(--surface-border);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.form-section {
  padding: 24px;
}

.cover-upload {
  .cover-uploader {
    width: 100%;

    :deep(.el-upload) {
      width: 100%;
      border: 1px dashed var(--surface-border);
      border-radius: 8px;
      background: var(--gray-50);
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: border-color 0.2s;
      cursor: pointer;

      &:hover {
        border-color: var(--primary-color);
      }
    }
  }

  .cover-inner {
    text-align: center;
    color: var(--text-secondary);

    .el-icon {
      font-size: 40px;
      color: var(--primary-color);
      margin-bottom: 8px;
    }

    p {
      margin: 0;
      font-size: 16px;
      color: var(--text-primary);
    }

    small {
      display: block;
      margin-top: 4px;
      font-size: 12px;
    }
  }

  .cover-preview {
    width: 100%;
    height: 200px;
    border-radius: 8px;
  }

  .cover-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
  }
}

.editor-wrapper {
  border: 1px solid var(--surface-border);
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  border-bottom: 1px solid var(--surface-border);
  background: #ffffff;
}

.rich-editor {
  :deep(.w-e-scroll) {
    min-height: 400px;
    max-height: 600px;
  }

  :deep(.w-e-text-container) {
    background-color: transparent;
    color: var(--text-primary);
  }

  :deep(.w-e-text-placeholder) {
    color: var(--text-secondary);
    opacity: 0.85;
  }
}

.action-bar {
  padding: 16px 24px;
  background: var(--gray-50);
  border-top: 1px solid var(--surface-border);
  display: flex;
  justify-content: space-between;
  align-items: center;

  .action-left {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--text-secondary);
    font-size: 14px;
    
    .auto-save-hint {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--success-color, #67c23a);
      font-size: 12px;
      margin-left: 12px;
      
      .el-icon {
        font-size: 14px;
      }
    }
  }

  .action-right {
    display: flex;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .publish-page {
    padding: 12px;
  }

  .form-section {
    padding: 16px;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .action-left {
      justify-content: center;
    }

    .action-right {
      width: 100%;

      button {
        flex: 1;
      }
    }
  }
}
</style>
