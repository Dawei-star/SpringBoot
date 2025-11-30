<template>
  <div class="skeleton-card">
    <el-skeleton animated>
      <template #template>
        <div class="skeleton-image">
          <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
        </div>
        <div class="skeleton-content">
          <el-skeleton-item variant="h3" style="width: 80%" />
          <el-skeleton-item variant="text" style="width: 60%; margin-top: 8px" />
          <div class="skeleton-meta">
            <el-skeleton-item variant="text" style="width: 30%" />
          </div>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.skeleton-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.skeleton-image {
  width: 100%;
}

.skeleton-content {
  padding: 20px;
}

.skeleton-meta {
  margin-top: 16px;
}
</style>
