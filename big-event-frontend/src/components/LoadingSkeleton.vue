<template>
  <div class="loading-skeleton" :class="type">
    <el-skeleton :loading="true" animated :count="count">
      <template #template>
        <div v-if="type === 'article-card'" class="skeleton-article-card">
          <el-skeleton-item variant="image" style="width: 100%; height: 200px; border-radius: 12px 12px 0 0;" />
          <div style="padding: 20px;">
            <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 12px;" />
            <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
            <el-skeleton-item variant="text" style="width: 60%;" />
            <div style="display: flex; gap: 16px; margin-top: 16px;">
              <el-skeleton-item variant="text" style="width: 100px;" />
              <el-skeleton-item variant="text" style="width: 100px;" />
            </div>
          </div>
        </div>
        <div v-else-if="type === 'article-list'" class="skeleton-article-list">
          <el-skeleton-item variant="h3" style="width: 60%; margin-bottom: 16px;" />
          <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
          <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 8px;" />
          <el-skeleton-item variant="text" style="width: 90%;" />
        </div>
        <div v-else-if="type === 'table'" class="skeleton-table">
          <el-skeleton-item variant="rect" style="width: 100%; height: 40px; margin-bottom: 8px;" />
          <el-skeleton-item variant="rect" style="width: 100%; height: 40px; margin-bottom: 8px;" />
          <el-skeleton-item variant="rect" style="width: 100%; height: 40px;" />
        </div>
        <div v-else class="skeleton-default">
          <el-skeleton-item variant="rect" style="width: 100%; height: 200px;" />
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'article-card', 'article-list', 'table'].includes(value)
  },
  count: {
    type: Number,
    default: 1
  }
})
</script>

<style lang="scss" scoped>
.loading-skeleton {
  width: 100%;
}

.skeleton-article-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

[data-theme="dark"] .skeleton-article-card {
  background: rgba(15, 23, 42, 0.8);
}

.skeleton-article-list {
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  margin-bottom: 16px;
}

[data-theme="dark"] .skeleton-article-list {
  background: rgba(15, 23, 42, 0.8);
}

.skeleton-table {
  padding: 20px;
}
</style>

