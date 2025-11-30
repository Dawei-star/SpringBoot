<script setup>
import { ref, onMounted, watch } from 'vue'
import { operationLogListService } from '@/api/operationLog'
import { ElMessage } from 'element-plus'
import { debounce } from '@/utils/debounce'

const loading = ref(false)
const logs = ref([])
const total = ref(0)

const query = ref({
  pageNum: 1,
  pageSize: 10,
  username: '',
  module: '',
  timeRange: []
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: query.value.pageNum,
      pageSize: query.value.pageSize,
      username: query.value.username?.trim() || undefined,
      module: query.value.module?.trim() || undefined
    }
    if (query.value.timeRange?.length === 2) {
      params.beginTime = query.value.timeRange[0]
      params.endTime = query.value.timeRange[1]
    }
    const res = await operationLogListService(params)
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300 && res.data) {
      logs.value = res.data.items || []
      total.value = res.data.total || 0
    } else {
      logs.value = []
      total.value = 0
      ElMessage.error(res?.message || '获取操作日志失败')
    }
  } catch (e) {
    ElMessage.error('获取操作日志失败')
    logs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 使用防抖优化搜索
const debouncedSearch = debounce(() => {
  query.value.pageNum = 1
  fetchLogs()
}, 300)

const handleSearch = () => {
  debouncedSearch()
}

const handleReset = () => {
  query.value = {
    pageNum: 1,
    pageSize: 10,
    username: '',
    module: '',
    timeRange: []
  }
  fetchLogs()
}

// 监听筛选条件变化，自动搜索
watch([() => query.value.username, () => query.value.module, () => query.value.timeRange], () => {
  if (query.value.pageNum === 1) {
    debouncedSearch()
  }
}, { deep: true })

const handlePageChange = (page) => {
  query.value.pageNum = page
  fetchLogs()
}

const handleSizeChange = (size) => {
  query.value.pageSize = size
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>操作日志</span>
      </div>
    </template>

    <el-form :inline="true" :model="query" class="filter-form" size="default">
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="模块">
        <el-input v-model="query.module" placeholder="如：用户管理" clearable />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="query.timeRange"
          type="datetimerange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table 
      :data="logs" 
      v-loading="loading" 
      border 
      style="width: 100%; margin-top: 12px"
      :stripe="false"
      :default-sort="{ prop: 'createTime', order: 'descending' }"
      class="operation-log-table">
      <el-table-column prop="createTime" label="时间" width="170" sortable />
      <el-table-column prop="username" label="用户" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.username" size="small" effect="plain">{{ row.username }}</el-tag>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="140">
        <template #default="{ row }">
          <el-tag v-if="row.module" size="small" type="info">{{ row.module }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="operation" label="操作" width="160" />
      <el-table-column prop="requestMethod" label="方法" width="80">
        <template #default="{ row }">
          <el-tag 
            :type="row.requestMethod === 'GET' ? 'success' : row.requestMethod === 'POST' ? 'info' : 'warning'"
            size="small">
            {{ row.requestMethod }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUri" label="请求路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="resultCode" label="结果码" width="80">
        <template #default="{ row }">
          <el-tag 
            :type="row.resultCode >= 200 && row.resultCode < 300 ? 'success' : 'danger'"
            size="small">
            {{ row.resultCode }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="resultMessage" label="结果信息" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="140" />
    </el-table>

    <el-pagination
      style="margin-top: 16px; text-align: right"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page="query.pageNum"
      :page-size="query.pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </el-card>
</template>

<style lang="scss" scoped>
.page-container {
  min-height: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  [data-theme="dark"] & {
    background: rgba(30, 41, 59, 0.6);
    border-color: rgba(148, 163, 184, 0.12);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  }
  
  :deep(.el-card__header) {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
    border-bottom: 1px solid var(--surface-border);
    padding: 20px 24px;
    
    [data-theme="dark"] & {
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
    }
    
    .header span {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
    }
  }
}

.filter-form {
  margin-bottom: 4px;
  
  :deep(.el-form-item__label) {
    color: #6b7280;
    font-weight: 500;
    
    [data-theme="dark"] & {
      color: #cbd5e1;
    }
  }
  
  :deep(.el-button--primary) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    color: white !important;
    
    [data-theme="dark"] & {
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
      color: white !important;
    }
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
      color: white !important;
    }
  }
}

// 修复表格行颜色问题
:deep(.operation-log-table) {
  .el-table__body {
    tr {
      background-color: transparent !important;
      
      &:hover {
        background-color: rgba(102, 126, 234, 0.05) !important;
        
        [data-theme="dark"] & {
          background-color: rgba(99, 102, 241, 0.1) !important;
        }
      }
    }
    
    td {
      background-color: transparent !important;
      border-color: rgba(0, 0, 0, 0.06);
      
      [data-theme="dark"] & {
        border-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
  
  .el-table__header {
    th {
      background-color: rgba(102, 126, 234, 0.05) !important;
      color: #1f2937;
      
      [data-theme="dark"] & {
        background-color: rgba(99, 102, 241, 0.1) !important;
        color: #f1f5f9;
      }
    }
  }
}
</style>


