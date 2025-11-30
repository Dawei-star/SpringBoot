<script setup>
import { ref, onMounted, onActivated, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserRole, updateUserByAdmin, userInfoService } from '@/api/user'
import { User, Edit, Lock, Unlock } from '@element-plus/icons-vue'
import { clearCacheByPrefix } from '@/utils/cache'

const loading = ref(false)
const users = ref([])
const currentUser = ref(null)
const isAdmin = computed(() => currentUser.value?.role === 'admin')

const loadUsers = async (forceRefresh = false) => {
  try {
    loading.value = true
    // 如果需要强制刷新，先清除缓存
    if (forceRefresh) {
      clearCacheByPrefix('/api/user')
    }
    // 先获取当前用户信息
    const userRes = await userInfoService()
    if (userRes && userRes.data) {
      currentUser.value = userRes.data
    }
    
    // 再获取用户列表（强制刷新时禁用缓存，forceRefresh=true时，useCache=false）
    try {
      const res = await getUserList(!forceRefresh)
      
      // 确保响应式更新 - 使用新数组触发响应式
      // 成功状态码范围：200-299
      if (res && res.code >= 200 && res.code < 300 && res.data) {
        users.value = [...res.data]
      } else {
        users.value = []
        if (res && res.code < 200 || res.code >= 300) {
          ElMessage.warning(res.message || '获取用户列表失败')
        }
      }
    } catch (error) {
      // 处理被reject的响应（code不为0的情况）
      if (error && typeof error === 'object' && 'code' in error) {
        users.value = []
        ElMessage.warning(error.message || '获取用户列表失败')
      } else {
        throw error // 重新抛出其他错误
      }
    }
  } catch (e) {
    // 401错误会在request拦截器中处理，这里只处理其他错误
    if (e?.response?.status !== 401) {
      ElMessage.error('加载用户列表失败: ' + (e?.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleRoleChange = async (user, newRole) => {
  if (!isAdmin.value) {
    ElMessage.warning('无权限操作')
    return
  }
  
  try {
    // 等待服务器响应（同步）
    const res = await updateUserRole(user.id, newRole)
    // 等待刷新列表完成（同步，强制刷新）
    await loadUsers(true)
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || '角色更新成功')
  } catch (e) {
    ElMessage.error('角色更新失败: ' + (e?.message || '未知错误'))
  }
}

const handleEdit = async (user) => {
  if (!isAdmin.value) {
    ElMessage.warning('无权限操作')
    return
  }
  
  try {
    await ElMessageBox.prompt('请输入新昵称', '编辑用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: user.nickname || '',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '昵称不能为空'
        }
        if (value.length > 10) {
          return '昵称不能超过10个字符'
        }
        return true
      }
    })
    
    const updateData = {
      id: user.id,
      nickname: user.nickname,
      email: user.email || '',
      role: user.role
    }
    
    // 等待服务器响应（同步）
    const res = await updateUserByAdmin(updateData)
    // 等待刷新列表完成（同步，强制刷新）
    await loadUsers(true)
    // 显示后端返回的成功消息
    ElMessage.success(res?.message || '用户信息更新成功')
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('更新失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})

// 当路由激活时（从其他页面返回时）自动刷新
onActivated(() => {
  loadUsers(true)
})
</script>

<template>
  <div class="user-manage" role="main" aria-label="用户管理页面">
    <el-card class="enhanced-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2 class="page-title">用户管理</h2>
            <p class="page-subtitle">管理系统用户和权限</p>
          </div>
          <div class="header-right">
            <el-button 
              type="primary" 
              :icon="User"
              @click="loadUsers" 
              :loading="loading"
              class="refresh-btn">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table 
        :data="users" 
        v-loading="loading"
        class="enhanced-table"
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无用户数据'">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150">
          <template #default="{ row }">
            {{ row.nickname || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200">
          <template #default="{ row }">
            {{ row.email || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">
              {{ row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" v-if="isAdmin">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              :icon="Edit"
              @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="row.role === 'admin' ? 'warning' : 'success'"
              :icon="row.role === 'admin' ? Unlock : Lock"
              @click="handleRoleChange(row, row.role === 'admin' ? 'user' : 'admin')">
              {{ row.role === 'admin' ? '设为普通用户' : '设为管理员' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!isAdmin" class="permission-tip">
        <el-alert
          title="权限提示"
          type="info"
          :closable="false"
          show-icon>
          <template #default>
            <p>您当前是普通用户，只能查看普通用户列表。如需管理所有用户，请联系管理员。</p>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.user-manage {
  padding: var(--spacing-lg);
  min-height: calc(100vh - 200px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
  
  .header-left {
    flex: 1;
    
    .page-title {
      margin: 0 0 var(--spacing-xs) 0;
      font-size: var(--text-2xl);
      font-weight: var(--font-bold);
      color: var(--text-primary);
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .page-subtitle {
      margin: 0;
      font-size: var(--text-sm);
      color: var(--text-secondary);
    }
  }
  
  .header-right {
    display: flex;
    gap: var(--spacing-sm);
  }
}

.refresh-btn {
  border-radius: var(--radius-full);
  font-weight: var(--font-medium);
  transition: all var(--transition-base);
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
}

.enhanced-card {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-base);
  transition: all var(--transition-base);
  overflow: hidden;
  
  &:hover {
    box-shadow: var(--shadow-lg);
  }
  
  :deep(.el-card__header) {
    background: linear-gradient(135deg, var(--gray-50) 0%, var(--gray-100) 100%);
    border-bottom: 1px solid var(--border-light);
    padding: var(--spacing-lg);
  }
  
  :deep(.el-card__body) {
    padding: var(--spacing-lg);
  }
}

.enhanced-table {
  :deep(.el-table__header) {
    th {
      background: linear-gradient(135deg, var(--gray-50) 0%, var(--gray-100) 100%);
      color: var(--text-primary);
      font-weight: var(--font-semibold);
      border-bottom: 2px solid var(--border-light);
    }
  }
  
  :deep(.el-table__row) {
    transition: all var(--transition-base);
    
    &:hover {
      background-color: var(--primary-light) !important;
      transform: scale(1.01);
    }
  }
  
  :deep(.el-table__cell) {
    padding: var(--spacing-md) var(--spacing-sm) !important;
  }
}

.permission-tip {
  margin-top: var(--spacing-lg);
  
  :deep(.el-alert) {
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-light);
  }
}

// 深色模式适配
[data-theme="dark"] {
  .card-header {
    .page-title {
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .enhanced-card {
    background: rgba(30, 41, 59, 0.6);
    border-color: rgba(148, 163, 184, 0.12);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    
    :deep(.el-card__header) {
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
      border-bottom: 1px solid rgba(148, 163, 184, 0.12);
    }
  }
  
  .enhanced-table {
    :deep(.el-table__header) {
      th {
        background: linear-gradient(135deg, rgba(30, 41, 59, 0.9) 0%, rgba(15, 23, 42, 0.7) 100%);
        border-bottom: 2px solid rgba(148, 163, 184, 0.12);
      }
    }
    
    :deep(.el-table__row:hover) {
      background-color: rgba(99, 102, 241, 0.12) !important;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .user-manage {
    padding: var(--spacing-md);
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
    
    .header-right {
      width: 100%;
      
      .refresh-btn {
        width: 100%;
      }
    }
  }
  
  .enhanced-table {
    :deep(.el-table__cell) {
      padding: var(--spacing-sm) var(--spacing-xs) !important;
      font-size: var(--text-sm);
    }
  }
}
</style>

