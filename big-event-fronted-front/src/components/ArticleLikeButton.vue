<template>
  <div class="like-button-wrapper">
    <el-button 
      :type="isLiked ? 'primary' : 'default'"
      :icon="isLiked ? StarFilled : Star"
      circle
      :loading="loading"
      :disabled="loading"
      @click="handleLike">
      {{ likeCount }}
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { likeArticle } from '../api/article'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  },
  initialLikeCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:likeCount'])

const loading = ref(false)
const isLiked = ref(false)
const likeCount = ref(props.initialLikeCount)

// 检查是否已点赞（从localStorage）
const checkLikedStatus = () => {
  const likedArticles = JSON.parse(localStorage.getItem('liked_articles') || '[]')
  return likedArticles.includes(props.articleId)
}

const handleLike = async () => {
  if (loading.value) return
  
  // 检查是否已点赞
  if (isLiked.value) {
    ElMessage.warning('您已经点赞过了')
    return
  }

  try {
    loading.value = true
    await likeArticle(props.articleId)
    
    // 更新状态
    isLiked.value = true
    likeCount.value += 1
    
    // 保存到localStorage
    const likedArticles = JSON.parse(localStorage.getItem('liked_articles') || '[]')
    likedArticles.push(props.articleId)
    localStorage.setItem('liked_articles', JSON.stringify(likedArticles))
    
    // 触发事件
    emit('update:likeCount', likeCount.value)
    
    ElMessage.success('点赞成功')
  } catch {
    ElMessage.error('点赞失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  isLiked.value = checkLikedStatus()
})
</script>

<style lang="scss" scoped>
.like-button-wrapper {
  display: inline-flex;
}
</style>

