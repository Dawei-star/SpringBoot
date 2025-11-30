<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useTokenStore } from '../stores/token'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Promotion, User, Refresh } from '@element-plus/icons-vue'
import { sendChatMessage, getChatHistory } from '../api/chat'
import { clearApiCache } from '../api/request'

const tokenStore = useTokenStore()
const isOpen = ref(false)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesEnd = ref(null)

// 快速回复选项
const quickReplies = [
  '你好',
  '如何发布文章？',
  '怎么创建相册？',
  '有哪些功能？',
  '帮助'
]

// 检查是否登录
const isLoggedIn = computed(() => !!tokenStore.token)

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesEnd.value) {
      messagesEnd.value.scrollIntoView({ behavior: 'smooth' })
    }
  })
}

// 加载聊天历史
const loadHistory = async () => {
  if (!isLoggedIn.value) return
  
  try {
    loading.value = true
    const res = await getChatHistory()
    if (res && res.data) {
      // 确保消息按时间排序
      const historyMessages = (res.data || []).sort((a, b) => {
        const timeA = new Date(a.createTime || 0).getTime()
        const timeB = new Date(b.createTime || 0).getTime()
        return timeA - timeB
      })
      messages.value = historyMessages
      scrollToBottom()
    }
  } catch (e) {
    console.error('加载聊天历史失败:', e)
    ElMessage.error('加载聊天历史失败')
  } finally {
    loading.value = false
  }
}

// 刷新消息（手动恢复）
const refreshMessages = async () => {
  await loadHistory()
  ElMessage.success('消息已刷新')
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再使用聊天功能')
    return
  }
  
  const messageText = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息到列表（乐观更新）
  const tempUserMessage = {
    id: Date.now(),
    content: messageText,
    sender: 'user',
    createTime: new Date().toISOString()
  }
  messages.value.push(tempUserMessage)
  scrollToBottom()
  
  try {
    sending.value = true
    const res = await sendChatMessage(messageText)
    
    if (res && res.code === 0) {
      // 清理聊天相关缓存并拉取最新历史
      clearApiCache('/chat')
      await loadHistory()
    } else {
      // 发送失败，移除临时消息
      const index = messages.value.findIndex(m => m.id === tempUserMessage.id)
      if (index > -1) {
        messages.value.splice(index, 1)
      }
      ElMessage.error(res?.message || '发送失败')
    }
  } catch (e) {
    console.error('发送消息失败:', e)
    // 发送失败，移除临时消息
    const index = messages.value.findIndex(m => m.id === tempUserMessage.id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }
    ElMessage.error('发送消息失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

// 处理回车发送
const handleKeyPress = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 切换聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && isLoggedIn.value) {
    loadHistory()
  }
}

// 格式化消息内容（支持换行、列表等）
function formatMessage(content) {
  if (!content) return ''
  // 转义HTML，防止XSS
  let formatted = String(content)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 支持换行
  formatted = formatted.replace(/\n/g, '<br>')
  
  // 支持列表（• 开头）
  formatted = formatted.replace(/•\s*(.+?)(?=\n|$)/g, '<div class="list-item">• $1</div>')
  
  // 支持数字列表（1. 开头）
  formatted = formatted.replace(/(\d+)\.\s*(.+?)(?=\n|$)/g, '<div class="list-item">$1. $2</div>')
  
  // 支持加粗（**文本**）
  formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  
  return formatted
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  // 如果已登录，可以预加载历史记录
  if (isLoggedIn.value) {
    // 延迟加载，避免影响页面加载速度
    setTimeout(() => {
      if (isOpen.value) {
        loadHistory()
      }
    }, 1000)
  }
})
</script>

<template>
  <div class="chat-widget">
    <!-- 聊天按钮 -->
    <transition name="scale">
      <el-button
        v-show="!isOpen"
        type="primary"
        :icon="ChatDotRound"
        circle
        class="chat-button"
        aria-label="打开聊天"
        @click="toggleChat">
      </el-button>
    </transition>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div v-show="isOpen" class="chat-window">
        <!-- 窗口头部 -->
        <div class="chat-header">
          <div class="header-left">
            <el-icon class="chat-icon"><ChatDotRound /></el-icon>
            <span class="chat-title">在线客服</span>
          </div>
          <div class="header-actions">
            <el-button
              v-if="isLoggedIn"
              text
              circle
              :icon="Refresh"
              class="refresh-button"
              :loading="loading"
              @click="refreshMessages"
              title="刷新消息">
            </el-button>
            <el-button
              text
              circle
              :icon="Close"
              class="close-button"
              @click="toggleChat">
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" v-loading="loading">
          <div v-if="!isLoggedIn" class="login-prompt">
            <el-icon><User /></el-icon>
            <p>请先登录后再使用聊天功能</p>
            <el-button type="primary" size="small" @click="$router.push('/login')">
              去登录
            </el-button>
          </div>
          
          <template v-else>
            <div v-if="messages.length === 0 && !loading" class="empty-state">
              <el-icon><ChatDotRound /></el-icon>
              <p>还没有消息，开始聊天吧~</p>
            </div>
            
            <div
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="{ 'message-user': message.sender === 'user' }">
              <div class="message-avatar">
                <el-avatar :size="32">
                  <el-icon v-if="message.sender === 'user'"><User /></el-icon>
                  <el-icon v-else><ChatDotRound /></el-icon>
                </el-avatar>
              </div>
              <div class="message-content">
                <div class="message-bubble" v-html="formatMessage(message.content)"></div>
                <div class="message-time">
                  {{ formatTime(message.createTime) }}
                </div>
              </div>
            </div>
          </template>
          
          <div ref="messagesEnd"></div>
        </div>

        <!-- 输入区域 -->
        <div v-if="isLoggedIn" class="chat-input">
          <div class="quick-replies" v-if="messages.length === 0">
            <span class="quick-label">快速提问：</span>
            <el-button 
              v-for="quick in quickReplies" 
              :key="quick"
              size="small"
              text
              @click="inputMessage = quick; sendMessage()">
              {{ quick }}
            </el-button>
          </div>
          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="输入消息，按Enter发送，Shift+Enter换行..."
              :disabled="sending"
              @keydown="handleKeyPress"
              class="input-box">
            </el-input>
            <el-button
              type="primary"
              :icon="Promotion"
              :loading="sending"
              :disabled="!inputMessage.trim()"
              class="send-button"
              @click="sendMessage">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style lang="scss" scoped>
.chat-widget {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 999;
  
  @media (max-width: 768px) {
    right: 16px;
    bottom: 16px;
  }
}

.chat-button {
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px) scale(1.05);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  }
  
  [data-theme="dark"] & {
    background: linear-gradient(135deg, #818cf8 0%, #a78bfa 100%);
  }
  
  @media (max-width: 768px) {
    width: 48px;
    height: 48px;
  }
}

.chat-window {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 380px;
  height: 600px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
  
  [data-theme="dark"] & {
    background: rgba(15, 23, 42, 0.98);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  @media (max-width: 768px) {
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .chat-icon {
    font-size: 20px;
  }
  
  .chat-title {
    font-size: 16px;
    font-weight: 600;
  }
  
  .refresh-button,
  .close-button {
    color: white;
    
    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
    
    [data-theme="dark"] & {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.login-prompt,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 12px;
  color: var(--el-text-color-secondary);
  
  .el-icon {
    font-size: 48px;
    opacity: 0.5;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  
  &.message-user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }
  }
}

.message-avatar {
  flex-shrink: 0;
  
  :deep(.el-avatar) {
    background: var(--el-color-info-light-9);
    
    [data-theme="dark"] & {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--el-color-info-light-9);
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
  
  :deep(.list-item) {
    margin: 4px 0;
    padding-left: 8px;
  }
  
  :deep(strong) {
    font-weight: 600;
    color: var(--el-color-primary);
  }
  
  [data-theme="dark"] & {
    background: rgba(255, 255, 255, 0.1);
    color: #f1f5f9;
    
    :deep(strong) {
      color: #818cf8;
    }
  }
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  padding: 0 4px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  [data-theme="dark"] & {
    border-top-color: rgba(255, 255, 255, 0.1);
  }
  
  .quick-replies {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    
    .quick-label {
      margin-right: 4px;
      font-weight: 500;
    }
    
    :deep(.el-button) {
      padding: 4px 12px;
      font-size: 12px;
      border-radius: 16px;
      transition: all 0.2s;
      
      &:hover {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }
    }
  }
  
  .input-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-end;
  }
  
  .input-box {
    flex: 1;
  }
  
  .send-button {
    flex-shrink: 0;
    height: 40px;
  }
}

// 过渡动画
.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>

