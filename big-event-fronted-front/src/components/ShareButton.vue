<template>
  <div class="share-button-group">
    <el-tooltip content="分享到微信" placement="top">
      <el-button 
        circle 
        size="small" 
        class="share-btn wechat"
        @click="shareToWeChat">
        <svg viewBox="0 0 24 24" width="18" height="18">
          <path fill="currentColor" d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.496-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.179c0-.651.52-1.18 1.162-1.18zm6.673 3.24c-2.016 0-3.673 1.476-4.106 3.378-.339 1.496-.054 2.984.747 4.182a6.915 6.915 0 0 0 3.359 2.43.59.59 0 0 1 .312.663l-.107.406c-.013.05-.03.099-.05.147a.24.24 0 0 0-.01.097c0 .12.097.217.217.217a.3.3 0 0 0 .14-.04l.687-.402a.81.81 0 0 1 .674-.092 9.88 9.88 0 0 0 2.422.31c3.8 0 6.691-3.288 6.691-7.342 0-4.055-2.891-7.343-6.691-7.343zm-3.357 4.852c.53 0 .96.437.96.976a.97.97 0 0 1-.96.976.97.97 0 0 1-.96-.976c0-.539.43-.976.96-.976zm3.357 0c.53 0 .96.437.96.976a.97.97 0 0 1-.96.976.97.97 0 0 1-.96-.976c0-.539.43-.976.96-.976z"/>
        </svg>
      </el-button>
    </el-tooltip>
    
    <el-tooltip content="分享到微博" placement="top">
      <el-button 
        circle 
        size="small" 
        class="share-btn weibo"
        @click="shareToWeibo">
        <svg viewBox="0 0 24 24" width="18" height="18">
          <path fill="currentColor" d="M9.839 10.872c-.285 0-.516-.231-.516-.516s.231-.516.516-.516.516.231.516.516-.23.516-.516.516zm5.322 0c-.285 0-.516-.231-.516-.516s.231-.516.516-.516.516.231.516.516-.231.516-.516.516zm2.581-2.323c-.678-.678-1.548-1.032-2.581-1.032-1.033 0-1.903.354-2.581 1.032-.678.678-1.032 1.548-1.032 2.581 0 1.033.354 1.903 1.032 2.581.678.678 1.548 1.032 2.581 1.032 1.033 0 1.903-.354 2.581-1.032.678-.678 1.032-1.548 1.032-2.581 0-1.033-.354-1.903-1.032-2.581zm-1.161 4.001c-.484.484-1.126.75-1.806.75s-1.322-.266-1.806-.75c-.484-.484-.75-1.126-.75-1.806s.266-1.322.75-1.806c.484-.484 1.126-.75 1.806-.75s1.322.266 1.806.75c.484.484.75 1.126.75 1.806s-.266 1.322-.75 1.806z"/>
        </svg>
      </el-button>
    </el-tooltip>
    
    <el-tooltip content="复制链接" placement="top">
      <el-button 
        circle 
        size="small" 
        class="share-btn link"
        @click="copyLink">
        <el-icon><Link /></el-icon>
      </el-button>
    </el-tooltip>
    
    <el-tooltip content="生成二维码" placement="top">
      <el-button 
        circle 
        size="small" 
        class="share-btn qrcode"
        @click="showQRCode">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <path d="M3 3h8v8H3V3zm2 2v4h4V5H5zm8-2h8v8h-8V3zm2 2v4h4V5h-4zM3 13h8v8H3v-8zm2 2v4h4v-4H5zm13-2h3v2h-3v-2zm0 4h3v2h-3v-2zm-2-4h2v3h-2v-3zm2 6h3v2h-3v-2zm-2-2h2v3h-2v-3zm4 0h3v2h-3v-2zm0 4h3v2h-3v-2z"/>
        </svg>
      </el-button>
    </el-tooltip>
  </div>
  
  <!-- 二维码对话框 -->
  <el-dialog v-model="qrCodeVisible" title="分享二维码" width="300px" center>
    <div class="qrcode-container">
      <div ref="qrcodeRef" class="qrcode-canvas"></div>
      <p class="qrcode-tip">扫描二维码分享文章</p>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Link } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  url: {
    type: String,
    required: true
  }
})

const qrCodeVisible = ref(false)
const qrcodeRef = ref(null)

const shareToWeChat = () => {
  // 微信分享需要配置JS-SDK
  ElMessage.info('请使用微信内置浏览器打开')
}

const shareToWeibo = () => {
  const shareUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(props.url)}&title=${encodeURIComponent(props.title)}`
  window.open(shareUrl, '_blank', 'width=600,height=400')
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(props.url)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = props.url
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('链接已复制到剪贴板')
    } catch {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(textarea)
  }
}

const showQRCode = async () => {
  qrCodeVisible.value = true
  await nextTick()
  if (qrcodeRef.value) {
    try {
      // 使用在线API生成二维码（无需安装依赖）
      const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(props.url)}`
      const img = document.createElement('img')
      img.src = qrCodeUrl
      img.style.width = '200px'
      img.style.height = '200px'
      qrcodeRef.value.innerHTML = ''
      qrcodeRef.value.appendChild(img)
    } catch (err) {
      console.error('生成二维码失败', err)
      ElMessage.error('生成二维码失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.share-button-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.share-btn {
  transition: all 0.3s;
  
  &:hover {
    transform: scale(1.1);
  }
  
  &.wechat {
    background: #07c160;
    border-color: #07c160;
    color: white;
    
    &:hover {
      background: #06ad56;
      border-color: #06ad56;
    }
  }
  
  &.weibo {
    background: #e6162d;
    border-color: #e6162d;
    color: white;
    
    &:hover {
      background: #c91428;
      border-color: #c91428;
    }
  }
  
  &.link {
    background: #667eea;
    border-color: #667eea;
    color: white;
    
    &:hover {
      background: #5568d3;
      border-color: #5568d3;
    }
  }
  
  &.qrcode {
    background: #764ba2;
    border-color: #764ba2;
    color: white;
    
    &:hover {
      background: #653a8a;
      border-color: #653a8a;
    }
  }
}

.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  
  .qrcode-canvas {
    margin-bottom: 16px;
  }
  
  .qrcode-tip {
    margin: 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
}
</style>

