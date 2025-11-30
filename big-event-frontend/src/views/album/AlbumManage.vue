<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAlbumList, deleteAlbum } from '@/api/album'
import { Plus, Delete, View } from '@element-plus/icons-vue'
import { clearCacheByPrefix } from '@/utils/cache'

const loading = ref(false)
const albums = ref([])

const loadAlbums = async (forceRefresh = false) => {
  try {
    loading.value = true
    // 如果需要强制刷新，先清除缓存
    if (forceRefresh) {
      clearCacheByPrefix('/api/album')
    }
    // 强制刷新时禁用缓存（forceRefresh=true时，useCache=false）
    const res = await getAlbumList(!forceRefresh)
    // 确保响应式更新 - 使用新数组触发响应式
    if (res && res.data) {
      albums.value = [...res.data]
    } else {
      albums.value = []
    }
  } catch (e) {
    ElMessage.error('加载相册列表失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (album) => {
  try {
    await ElMessageBox.confirm('确定要删除这个相册吗？', '提示', {
      type: 'warning'
    })
    // 等待服务器响应（同步）
    const res = await deleteAlbum(album.id)
    // 清除缓存
    clearCacheByPrefix('/api/album')
    // 等待刷新列表完成（同步，强制刷新）
    await loadAlbums(true)
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || '相册删除成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadAlbums()
})

// 当路由激活时（从其他页面返回时）自动刷新
onActivated(() => {
  loadAlbums(true)
})
</script>

<template>
  <div class="album-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>相册管理</h2>
          <el-button type="primary" :icon="Plus" @click="$router.push('/album/create')">
            新建相册
          </el-button>
        </div>
      </template>

      <el-table :data="albums" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image 
              :src="row.coverImg || 'https://via.placeholder.com/100'" 
              fit="cover"
              style="width: 100px; height: 100px; border-radius: 8px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="图片数量" width="120">
          <template #default="{ row }">
            {{ row.images?.length || 0 }} 张
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" :icon="View" @click="$router.push(`/album/edit/${row.id}`)">
              查看/编辑
            </el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.album-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
  }
}
</style>

