<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Top } from '@element-plus/icons-vue'

const show = ref(false)
const onScroll = () => { 
  show.value = window.scrollY > 300 
}

const backToTop = () => { 
  window.scrollTo({ top: 0, behavior: 'smooth' }) 
}

onMounted(() => {
  window.addEventListener('scroll', onScroll)
  onScroll() // 初始检查
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <transition name="fade">
    <el-button 
      v-show="show" 
      type="primary" 
      :icon="Top"
      circle 
      class="back-to-top" 
      aria-label="返回顶部" 
      @click="backToTop">
    </el-button>
  </transition>
</template>

<style scoped>
.back-to-top {
  position: fixed;
  right: 24px;
  bottom: 100px;
  z-index: 1000;
  width: 48px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  
  &:hover {
    transform: translateY(-4px) scale(1.1);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  }
  
  &:active {
    transform: translateY(-2px) scale(1.05);
  }
  
  [data-theme="dark"] & {
    background: linear-gradient(135deg, #818cf8 0%, #a78bfa 100%);
  }
  
  @media (max-width: 768px) {
    right: 16px;
    bottom: 80px;
    width: 44px;
    height: 44px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
