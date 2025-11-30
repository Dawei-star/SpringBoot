<script setup>
import {computed, onBeforeUnmount, onMounted, ref, shallowRef, watch} from "vue"
import {ElMessage} from "element-plus"
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from "@wangeditor/editor-for-vue"
import {articleAddService, articleCategoryListService} from "@/api/article"
import {useTokenStore} from "@/stores/token"
import {PictureFilled, UploadFilled} from "@element-plus/icons-vue"
import {debounce} from "@/utils/debounce"
import {clearApiCache} from "@/utils/request"

const tokenStore = useTokenStore()

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
    const {data} = await articleCategoryListService()
    categories.value = data
  } catch {
    ElMessage.error('获取文章类型失败，请稍后重试')
  } finally {
    categoryLoading.value = false
  }
}

const handleUploadSuccess = (res) => {
  coverUploading.value = false
  articleModel.value.coverImg = res.data
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
    await articleAddService(articleModel.value)
    ElMessage.success(state === '已发布' ? '发布成功' : '已保存到草稿箱')
    // 清除草稿和缓存
    localStorage.removeItem('article_draft')
    clearApiCache('/api/article')
    articleModel.value = createDefaultArticle()
    coverUploading.value = false
    // 清空编辑器内容
    if (editorRef.value) {
      editorRef.value.clear()
    }
  } catch {
    ElMessage.error('发布失败，请稍后重试')
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
                  <div v-if="!articleModel.coverImg" class="cover-inner">
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
                <div v-if="articleModel.coverImg" class="cover-actions">
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
                    v-model="articleModel.content"
                    class="rich-editor"
                    :default-config="editorConfig"
                    mode="default"
                    @on-created="handleEditorCreated"
                />
              </div>
            </el-form-item>
          </el-form>
        </div>

        <div class="action-bar">
          <div class="action-left">
            <el-icon><PictureFilled/></el-icon>
            <span>支持拖拽图片至编辑器自动上传</span>
            <span v-if="articleModel.title || articleModel.content" class="auto-save-hint">
              <el-icon><UploadFilled/></el-icon>
              内容已自动保存
            </span>
          </div>
          <div class="action-right">
            <el-button :loading="loading" @click="handlePublish('草稿')">保存草稿</el-button>
            <el-button
                type="primary"
                :loading="loading"
                :disabled="loading || !canPublish"
                @click="handlePublish('已发布')"
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
  background: transparent; /* Let body background show through */
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
    font-family: var(--sci-font-heading);
    color: var(--sci-primary);
    text-transform: uppercase;
    letter-spacing: 1px;
    text-shadow: 0 0 10px rgba(0, 243, 255, 0.3);
  }

  p {
    margin: 0;
    color: var(--sci-text-dim);
    font-size: 14px;
  }
}

.publish-card {
  background: var(--sci-bg-panel);
  backdrop-filter: var(--sci-glass);
  border: var(--sci-border);
  border-radius: 16px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.5);
  color: var(--sci-text-main);
  
  :deep(.el-card__body) {
    padding: 0;
  }
}

.form-section {
  padding: 24px;
}

:deep(.el-form-item__label) {
  color: var(--sci-text-main);
  font-family: var(--sci-font-heading);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  background-color: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 1px rgba(0, 243, 255, 0.2) inset;
  color: var(--sci-text-main);
  
  &.is-focus {
    box-shadow: 0 0 0 1px var(--sci-primary) inset !important;
  }
}

:deep(.el-input__inner) {
  color: var(--sci-text-main);
}

.cover-upload {
  .cover-uploader {
    width: 100%;

    :deep(.el-upload) {
      width: 100%;
      border: 1px dashed rgba(0, 243, 255, 0.3);
      border-radius: 8px;
      background: rgba(0, 0, 0, 0.2);
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      cursor: pointer;

      &:hover {
        border-color: var(--sci-primary);
        background: rgba(0, 243, 255, 0.05);
        box-shadow: var(--sci-glow-primary);
      }
    }
  }

  .cover-inner {
    text-align: center;
    color: var(--sci-text-dim);

    .el-icon {
      font-size: 40px;
      color: var(--sci-primary);
      margin-bottom: 8px;
    }

    p {
      margin: 0;
      font-size: 16px;
      color: var(--sci-text-main);
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
    border: 1px solid var(--sci-primary);
  }

  .cover-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 8px;
  }
}

.editor-wrapper {
  border: 1px solid rgba(0, 243, 255, 0.2);
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  border-bottom: 1px solid rgba(0, 243, 255, 0.2);
  background: rgba(0, 0, 0, 0.4);
  color: var(--sci-text-main);
  
  :deep(.w-e-bar-item button) {
    color: var(--sci-text-main);
    &:hover {
      background: rgba(0, 243, 255, 0.1);
    }
  }
}

.rich-editor {
  background: rgba(0, 0, 0, 0.2);
  
  :deep(.w-e-scroll) {
    min-height: 400px;
    max-height: 600px;
  }

  :deep(.w-e-text-container) {
    background-color: transparent;
    color: var(--sci-text-main);
  }

  :deep(.w-e-text-placeholder) {
    color: var(--sci-text-dim);
    opacity: 0.85;
  }
}

.action-bar {
  padding: 16px 24px;
  background: rgba(0, 0, 0, 0.3);
  border-top: 1px solid rgba(0, 243, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;

  .action-left {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--sci-text-dim);
    font-size: 14px;
    
    .auto-save-hint {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--sci-primary);
      font-size: 12px;
      margin-left: 12px;
      text-shadow: 0 0 5px rgba(0, 243, 255, 0.5);
      
      .el-icon {
        font-size: 14px;
      }
    }
  }

  .action-right {
    display: flex;
    gap: 12px;
    
    .el-button {
      background: transparent;
      border: 1px solid var(--sci-primary);
      color: var(--sci-primary);
      font-family: var(--sci-font-heading);
      
      &:hover {
        background: var(--sci-primary);
        color: var(--sci-bg-dark);
        box-shadow: var(--sci-glow-primary);
      }
      
      &.el-button--primary {
        background: rgba(0, 243, 255, 0.1);
        
        &:hover {
          background: var(--sci-primary);
          color: var(--sci-bg-dark);
        }
      }
    }
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
