<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageListService, messageAddService, messageLikeService, messageDeleteService } from '@/api/message'
import { clearCacheByPrefix } from '@/utils/cache'

const loading = ref(false)
const messages = ref([])
const total = ref(0)

const query = ref({
  pageNum: 1,
  pageSize: 20
})

const formVisible = ref(false)
const formModel = ref({
  nickname: '',
  content: ''
})

const formRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }]
}

const formRef = ref(null)

const loadMessages = async (forceRefresh = false) => {
  loading.value = true
  try {
    // 如果需要强制刷新，先清除缓存
    if (forceRefresh) {
      clearCacheByPrefix('/api/message')
    }
    // 强制刷新时禁用缓存（forceRefresh=true时，useCache=false）
    const res = await messageListService({
      pageNum: query.value.pageNum,
      pageSize: query.value.pageSize
    }, !forceRefresh)
    // 成功状态码范围：200-299
    // 确保响应式更新 - 使用新数组触发响应式
    if (res && res.code >= 200 && res.code < 300 && res.data) {
      messages.value = [...(res.data.items || [])]
      total.value = res.data.total || 0
    } else {
      messages.value = []
      total.value = 0
    }
  } catch (e) {
    ElMessage.error('获取留言失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  query.value.pageNum = page
  loadMessages()
}

const openCreate = () => {
  formModel.value = {
    nickname: '',
    content: ''
  }
  formVisible.value = true
}

const handleCreate = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return
  try {
    // 等待服务器响应（同步）
    const res = await messageAddService(formModel.value)
    // 关闭对话框
    formVisible.value = false
    // 重置到第一页并等待刷新完成（同步，强制刷新）
    query.value.pageNum = 1
    await loadMessages(true)
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || '留言发布成功')
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const handleLike = async (item) => {
  try {
    const res = await messageLikeService(item.id)
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300) {
      item.likeCount = (item.likeCount || 0) + 1
    }
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这条留言吗？', '提示', {
      type: 'warning'
    })
    // 等待服务器响应（同步）
    const res = await messageDeleteService(item.id)
    // 等待刷新列表完成（同步，强制刷新）
    await loadMessages(true)
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || '留言删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

const getCardClass = (index) => {
  return `message-card-${index % 6}`
}

onMounted(loadMessages)

// 当路由激活时（从其他页面返回时）自动刷新
onActivated(() => {
  loadMessages(true)
})
</script>

<template>
  <div class="message-manage" role="main" aria-label="留言管理页面">
    <header class="message-header">
      <div>
        <h1 class="message-title">留言墙</h1>
        <p class="sub-title">每一条留言都是你的心情片段，也为我们提供了宝贵的反馈</p>
      </div>
      <el-button 
        type="primary" 
        @click="openCreate"
        aria-label="写一条新留言">
        写一条新留言
      </el-button>
    </header>

    <el-skeleton :loading="loading" animated :rows="4" role="status" aria-live="polite">
      <template #default>
        <div class="message-grid" role="list" aria-label="留言列表">
          <div
            v-for="(item, index) in messages"
            :key="item.id"
            class="message-card-wrapper"
            role="listitem">
            <div 
              class="message-card" 
              :class="getCardClass(index)"
              :aria-label="`留言：${item.content.substring(0, 30)}...`"
              tabindex="0">
              <div class="message-date">
                {{ (item.createTime || '').split(' ')[0] || '——' }}
              </div>
              <div class="message-content">
                {{ item.content }}
              </div>
              <div class="message-footer">
                <span class="nickname" :aria-label="`留言者：${item.nickname || '匿名'}`">
                  {{ item.nickname || '匿名' }}
                </span>
                <div class="actions" role="group" aria-label="留言操作">
                  <el-button 
                    link 
                    type="primary" 
                    size="small" 
                    @click="handleLike(item)"
                    :aria-label="`点赞留言，当前${item.likeCount || 0}个赞`">
                    ❤️ {{ item.likeCount || 0 }}
                  </el-button>
                  <el-button 
                    link 
                    type="danger" 
                    size="small" 
                    @click="handleDelete(item)"
                    :aria-label="`删除留言`">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!messages.length" class="empty-wrapper">
          <el-empty description="暂时还没有留言，快来写第一条吧~" role="status" />
        </div>

        <div class="pagination-wrapper">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="query.pageSize"
            :current-page="query.pageNum"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </el-skeleton>

    <el-dialog 
      v-model="formVisible" 
      title="写留言" 
      width="480px"
      role="dialog"
      aria-labelledby="留言表单">
      <el-form 
        ref="formRef" 
        :model="formModel" 
        :rules="formRules" 
        label-width="80px"
        role="form"
        aria-label="留言表单">
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            v-model="formModel.nickname" 
            placeholder="请输入你的昵称"
            aria-label="留言昵称"
            aria-required="true" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formModel.content"
            type="textarea"
            :rows="4"
            placeholder="今天想分享些什么？"
            aria-label="留言内容"
            aria-required="true" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer" role="group" aria-label="对话框操作">
          <el-button 
            @click="formVisible = false"
            aria-label="取消留言">
            取 消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleCreate"
            aria-label="发布留言">
            发 布
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.message-manage {
  padding: 24px;
  background: var(--surface-bg);
  border: 1px solid var(--surface-border);
  border-radius: 16px;
  min-height: 60vh;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md, 16px);
  
  .message-title {
    margin: 0;
    font-size: var(--text-3xl, 24px);
    font-weight: var(--font-bold, 700);
    color: #1f2937;
    
    [data-theme="dark"] & {
      color: #f1f5f9;
    }
  }
  
  .sub-title {
    margin-top: 4px;
    color: #6b7280;
    font-size: 13px;
    
    [data-theme="dark"] & {
      color: #cbd5e1;
    }
  }
  
  :deep(.el-button--primary) {
    color: white !important;
    
    &:hover {
      color: white !important;
    }
  }
}

.sub-title {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 13px;
}

.message-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-md, 16px);
  margin-top: var(--spacing-sm, 8px);
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.message-card-wrapper {
  width: 100%;
}

.message-card {
  border-radius: var(--radius-lg, 16px);
  padding: var(--spacing-lg, 18px) var(--spacing-md, 16px);
  box-shadow: var(--shadow-md, 0 6px 18px rgba(15, 23, 42, 0.06));
  display: flex;
  flex-direction: column;
  min-height: 150px;
  transition: all var(--transition-fast, 0.15s ease);
  border: 1px solid var(--border-light, var(--surface-border));
  outline: none;
  cursor: pointer;
  
  [data-theme="dark"] & {
    border-color: var(--border-light, rgba(148, 163, 184, 0.12));
  }
  
  &:hover, &:focus {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg, 0 10px 24px rgba(15, 23, 42, 0.12));
  }
  
  &:focus-visible {
    outline: 2px solid var(--primary-500, #667eea);
    outline-offset: 2px;
  }
}

.message-card-0 { background-color: #f3e8ff; }
.message-card-1 { background-color: #e0f2fe; }
.message-card-2 { background-color: #fee2e2; }
.message-card-3 { background-color: #dcfce7; }
.message-card-4 { background-color: #fef9c3; }
.message-card-5 { background-color: #e5e7eb; }

/* 深色模式下的卡片颜色 */
[data-theme="dark"] .message-card-0 { background-color: #581c87; border-color: #6b21a8; }
[data-theme="dark"] .message-card-1 { background-color: #1e3a8a; border-color: #1e40af; }
[data-theme="dark"] .message-card-2 { background-color: #7f1d1d; border-color: #991b1b; }
[data-theme="dark"] .message-card-3 { background-color: #14532d; border-color: #166534; }
[data-theme="dark"] .message-card-4 { background-color: #713f12; border-color: #854d0e; }
[data-theme="dark"] .message-card-5 { background-color: #374151; border-color: #4b5563; }

.message-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.message-date {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.message-content {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.nickname {
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 4px;
  align-items: center;
}

.empty-wrapper {
  margin-top: 40px;
}

.pagination-wrapper {
  margin-top: 16px;
  text-align: right;
}
</style>
