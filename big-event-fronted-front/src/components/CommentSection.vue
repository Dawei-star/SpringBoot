<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { fetchComments, addComment, likeComment } from '../api/comment'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star, StarFilled } from '@element-plus/icons-vue'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  }
})

const comments = ref([])
const loading = ref(false)
const submitting = ref(false)

// 评论表单
const commentForm = ref({
  nickname: localStorage.getItem('comment_nickname') || '',
  content: ''
})

// 回复状态
const replyingTo = ref(null)
const replyForm = ref({
  nickname: localStorage.getItem('comment_nickname') || '',
  content: ''
})

// 已点赞的评论ID集合
const likedComments = ref(new Set(JSON.parse(localStorage.getItem('liked_comments') || '[]')))

// 加载评论
const loadComments = async () => {
  try {
    loading.value = true
    const res = await fetchComments(props.articleId)
    // 确保数据是数组格式，并处理评论树结构
    const commentData = res.data || []
    let newComments = []
    if (Array.isArray(commentData)) {
      newComments = commentData
    } else if (commentData.items) {
      newComments = commentData.items
    }
    
    // 确保每个评论都有children数组
    newComments = newComments.map(comment => ({
      ...comment,
      children: comment.children || []
    }))
    
    // 直接替换，确保使用服务器返回的最新数据
    comments.value = newComments
    // 强制触发响应式更新
    comments.value = [...comments.value]
  } catch (e) {
    console.error('加载评论失败:', e)
    // 如果加载失败，保留现有评论（不置空）
    if (comments.value.length === 0) {
      comments.value = []
    }
  } finally {
    loading.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentForm.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const nickname = commentForm.value.nickname.trim()
  const content = commentForm.value.content.trim()
  
  // 创建临时评论对象（乐观更新）
  const tempComment = {
    id: `temp_${Date.now()}`,
    nickname: nickname,
    content: content,
    createTime: new Date().toISOString(),
    likeCount: 0,
    children: []
  }
  
  // 立即添加到列表顶部（乐观更新）
  comments.value = [tempComment, ...comments.value]
  // 强制触发响应式更新
  comments.value = [...comments.value]
  
  // 保存昵称
  localStorage.setItem('comment_nickname', nickname)
  
  // 清空表单
  commentForm.value.content = ''
  
  try {
    submitting.value = true
    await addComment({
      articleId: props.articleId,
      nickname: nickname,
      content: content
    })
    
    ElMessage.success('评论成功')
    submitting.value = false
    
    // 延迟刷新以确保后端数据已保存（静默刷新，不显示loading）
    await nextTick()
    setTimeout(async () => {
      // 静默刷新，不显示加载状态
      try {
        const res = await fetchComments(props.articleId)
        const commentData = res.data || []
        let newComments = []
        if (Array.isArray(commentData)) {
          newComments = commentData
        } else if (commentData.items) {
          newComments = commentData.items
        }
        newComments = newComments.map(comment => ({
          ...comment,
          children: comment.children || []
        }))
        
        // 检查新评论是否已存在于服务器数据中（通过内容和昵称匹配）
        const commentExists = newComments.some(c => 
          c.content === content && c.nickname === nickname
        )
        
        // 如果服务器数据中还没有新评论，保留临时评论
        if (!commentExists) {
          // 保留临时评论，但将其放在列表顶部
          comments.value = [tempComment, ...newComments]
        } else {
          // 服务器数据已包含新评论，直接使用服务器数据
          comments.value = newComments
        }
        comments.value = [...comments.value]
      } catch {
        console.error('刷新评论失败')
        // 刷新失败时保留临时评论
      }
    }, 800)
  } catch {
    // 如果提交失败，移除临时评论
    comments.value = comments.value.filter(c => c.id !== tempComment.id)
    comments.value = [...comments.value]
    ElMessage.error('评论失败，请重试')
    submitting.value = false
  }
}

// 回复评论
const submitReply = async () => {
  if (!replyForm.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  const nickname = replyForm.value.nickname.trim()
  const content = replyForm.value.content.trim()
  const parentId = replyingTo.value.id
  const replyToNickname = replyingTo.value.nickname
  
  // 查找父评论
  const targetParent = findCommentById(comments.value, parentId)
  if (!targetParent) {
    ElMessage.error('找不到父评论')
    return
  }
  
  // 创建临时回复对象（乐观更新）
  const tempReply = {
    id: `temp_reply_${Date.now()}`,
    nickname: nickname,
    content: content,
    replyNickname: replyToNickname,
    createTime: new Date().toISOString(),
    likeCount: 0
  }
  
  // 立即添加到父评论的children中（乐观更新）
  if (!targetParent.children) {
    targetParent.children = []
  }
  targetParent.children.push(tempReply)
  // 强制触发响应式更新
  comments.value = [...comments.value]
  
  // 保存昵称
  localStorage.setItem('comment_nickname', nickname)
  
  // 清空表单并关闭回复框
  replyForm.value.content = ''
  replyingTo.value = null
  
  try {
    submitting.value = true
    await addComment({
      articleId: props.articleId,
      nickname: nickname,
      content: content,
      replyId: parentId
    })
    
    ElMessage.success('回复成功')
    submitting.value = false
    
    // 延迟刷新以确保后端数据已保存（静默刷新，不显示loading）
    await nextTick()
    setTimeout(async () => {
      // 静默刷新，不显示加载状态
      try {
        const res = await fetchComments(props.articleId)
        const commentData = res.data || []
        let newComments = []
        if (Array.isArray(commentData)) {
          newComments = commentData
        } else if (commentData.items) {
          newComments = commentData.items
        }
        newComments = newComments.map(comment => ({
          ...comment,
          children: comment.children || []
        }))
        
        // 检查新回复是否已存在于服务器数据中（通过内容和昵称匹配）
        const targetParentInServer = findCommentById(newComments, parentId)
        const replyExists = targetParentInServer && targetParentInServer.children && 
          targetParentInServer.children.some(r => 
            r.content === content && r.nickname === nickname
          )
        
        // 如果服务器数据中还没有新回复，保留临时回复
        if (!replyExists && targetParentInServer) {
          // 将临时回复添加到服务器父评论的children中
          if (!targetParentInServer.children) {
            targetParentInServer.children = []
          }
          // 检查是否已存在（避免重复）
          const alreadyExists = targetParentInServer.children.some(r => 
            r.content === content && r.nickname === nickname
          )
          if (!alreadyExists) {
            targetParentInServer.children.push(tempReply)
          }
        }
        
        comments.value = newComments
        comments.value = [...comments.value]
      } catch {
        console.error('刷新评论失败')
        // 刷新失败时保留临时回复
      }
    }, 800)
  } catch {
    // 如果提交失败，移除临时回复
    if (targetParent.children) {
      targetParent.children = targetParent.children.filter(r => r.id !== tempReply.id)
      comments.value = [...comments.value]
    }
    ElMessage.error('回复失败，请重试')
    submitting.value = false
  }
}

// 点赞评论
const handleLike = async (comment) => {
  if (likedComments.value.has(comment.id)) {
    ElMessage.warning('您已经点赞过了')
    return
  }

  try {
    await likeComment(comment.id)
    comment.likeCount = (comment.likeCount || 0) + 1
    likedComments.value.add(comment.id)
    localStorage.setItem('liked_comments', JSON.stringify([...likedComments.value]))
  } catch {
    ElMessage.error('点赞失败')
  }
}

// 开始回复
const startReply = (comment) => {
  replyingTo.value = comment
  replyForm.value.content = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  replyForm.value.content = ''
}

// 查找评论（递归查找）
const findCommentById = (commentList, id) => {
  for (const comment of commentList) {
    if (comment.id === id) {
      return comment
    }
    if (comment.children && comment.children.length > 0) {
      const found = findCommentById(comment.children, id)
      if (found) return found
    }
  }
  return null
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return time.split('T')[0] || time.split(' ')[0]
}

// 计算总评论数
const totalComments = computed(() => {
  let count = comments.value.length
  comments.value.forEach(c => {
    if (c.children) count += c.children.length
  })
  return count
})

onMounted(() => {
  loadComments()
})
</script>

<template>
  <div class="comment-section">
    <div class="comment-header">
      <el-icon><ChatDotRound /></el-icon>
      <h3>评论区</h3>
      <span class="comment-count">{{ totalComments }} 条评论</span>
    </div>

    <!-- 评论输入框 -->
    <div class="comment-form">
      <div class="form-row">
        <el-input
          v-model="commentForm.nickname"
          placeholder="您的昵称"
          class="nickname-input"
          maxlength="20"
        />
      </div>
      <div class="form-row">
        <el-input
          v-model="commentForm.content"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
          maxlength="500"
          show-word-limit
        />
      </div>
      <div class="form-actions">
        <el-button 
          type="primary" 
          :loading="submitting"
          @click="submitComment">
          发表评论
        </el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <el-skeleton :loading="loading" animated :count="3">
        <template #default>
          <div v-if="comments.length === 0" class="no-comments">
            <el-empty description="暂无评论，快来发表第一条评论吧~" />
          </div>
          
          <div v-else>
            <div 
              v-for="comment in comments" 
              :key="comment.id" 
              class="comment-item">
              <!-- 主评论 -->
              <div class="comment-main">
                <div class="comment-avatar">
                  {{ comment.nickname?.charAt(0) || '匿' }}
                </div>
                <div class="comment-body">
                  <div class="comment-meta">
                    <span class="comment-author">{{ comment.nickname }}</span>
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <span 
                      class="action-item" 
                      :class="{ liked: likedComments.has(comment.id) }"
                      @click="handleLike(comment)">
                      <el-icon>
                        <StarFilled v-if="likedComments.has(comment.id)" />
                        <Star v-else />
                      </el-icon>
                      {{ comment.likeCount || 0 }}
                    </span>
                    <span class="action-item" @click="startReply(comment)">
                      回复
                    </span>
                  </div>

                  <!-- 回复输入框 -->
                  <div v-if="replyingTo?.id === comment.id" class="reply-form">
                    <el-input
                      v-model="replyForm.nickname"
                      placeholder="您的昵称"
                      class="nickname-input"
                      maxlength="20"
                    />
                    <el-input
                      v-model="replyForm.content"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 @${comment.nickname}`"
                      maxlength="500"
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button 
                        type="primary" 
                        size="small" 
                        :loading="submitting"
                        @click="submitReply(comment)">
                        回复
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 子评论 -->
              <div v-if="comment.children?.length" class="comment-children">
                <div 
                  v-for="child in comment.children" 
                  :key="child.id" 
                  class="comment-child">
                  <div class="comment-avatar small">
                    {{ child.nickname?.charAt(0) || '匿' }}
                  </div>
                  <div class="comment-body">
                    <div class="comment-meta">
                      <span class="comment-author">{{ child.nickname }}</span>
                      <span v-if="child.replyNickname" class="reply-to">
                        回复 <span class="reply-target">@{{ child.replyNickname }}</span>
                      </span>
                      <span class="comment-time">{{ formatTime(child.createTime) }}</span>
                    </div>
                    <div class="comment-content">{{ child.content }}</div>
                    <div class="comment-actions">
                      <span 
                        class="action-item" 
                        :class="{ liked: likedComments.has(child.id) }"
                        @click="handleLike(child)">
                        <el-icon>
                          <StarFilled v-if="likedComments.has(child.id)" />
                          <Star v-else />
                        </el-icon>
                        {{ child.likeCount || 0 }}
                      </span>
                      <span class="action-item" @click="startReply(child)">
                        回复
                      </span>
                    </div>

                    <!-- 子评论的回复输入框 -->
                    <div v-if="replyingTo?.id === child.id" class="reply-form">
                      <el-input
                        v-model="replyForm.nickname"
                        placeholder="您的昵称"
                        class="nickname-input"
                        maxlength="20"
                      />
                      <el-input
                        v-model="replyForm.content"
                        type="textarea"
                        :rows="2"
                        :placeholder="`回复 @${child.nickname}`"
                        maxlength="500"
                      />
                      <div class="reply-actions">
                        <el-button size="small" @click="cancelReply">取消</el-button>
                        <el-button 
                          type="primary" 
                          size="small" 
                          :loading="submitting"
                          @click="submitReply(child)">
                          回复
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </el-skeleton>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.comment-section {
  margin-top: 60px;
  padding-top: 40px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);

  [data-theme="dark"] & {
    border-top-color: rgba(255, 255, 255, 0.08);
  }
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;

  .el-icon {
    font-size: 24px;
    color: var(--primary-color, #667eea);
  }

  h3 {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0;
  }

  .comment-count {
    color: #6b7280;
    font-size: 0.9rem;
  }
}

.comment-form {
  background: rgba(102, 126, 234, 0.05);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 40px;

  [data-theme="dark"] & {
    background: rgba(102, 126, 234, 0.1);
  }
}

.form-row {
  margin-bottom: 16px;
}

.nickname-input {
  max-width: 200px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  .no-comments {
    padding: 40px 0;
  }
}

.comment-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);

  &:last-child {
    border-bottom: none;
  }

  [data-theme="dark"] & {
    border-bottom-color: rgba(255, 255, 255, 0.06);
  }
}

.comment-main,
.comment-child {
  display: flex;
  gap: 16px;
}

.comment-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.1rem;
  flex-shrink: 0;

  &.small {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;
  }
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #1f2937;

  [data-theme="dark"] & {
    color: #f1f5f9;
  }
}

.reply-to {
  color: #6b7280;
  font-size: 0.9rem;
}

.reply-target {
  color: var(--primary-color, #667eea);
}

.comment-time {
  color: #9ca3af;
  font-size: 0.85rem;
}

.comment-content {
  color: #374151;
  line-height: 1.7;
  word-break: break-word;

  [data-theme="dark"] & {
    color: #cbd5e1;
  }
}

.comment-actions {
  display: flex;
  gap: 20px;
  margin-top: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 0.9rem;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: var(--primary-color, #667eea);
  }

  &.liked {
    color: #f59e0b;
  }

  .el-icon {
    font-size: 16px;
  }
}

.reply-form {
  margin-top: 16px;
  padding: 16px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 12px;

  [data-theme="dark"] & {
    background: rgba(255, 255, 255, 0.02);
  }

  .nickname-input {
    margin-bottom: 12px;
  }
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.comment-children {
  margin-left: 60px;
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid rgba(102, 126, 234, 0.2);
}

.comment-child {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

@media (max-width: 768px) {
  .comment-children {
    margin-left: 30px;
    padding-left: 12px;
  }

  .comment-avatar {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;

    &.small {
      width: 30px;
      height: 30px;
      font-size: 0.8rem;
    }
  }
}
</style>

