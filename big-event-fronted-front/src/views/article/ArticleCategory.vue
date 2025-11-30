<script setup>
import {Delete, Edit} from '@element-plus/icons-vue'
import {ref} from 'vue'
import {
  articleCategoryAddService,
  articleCategoryDeleteService,
  articleCategoryListService,
  articleCategoryUpdateService
} from "@/api/article";
import {ElMessage, ElMessageBox} from "element-plus";

const categories = ref([
  // {
  //   "id": 3,
  //   "categoryName": "美食",
  //   "categoryAlias": "my",
  //   "createTime": "2023-09-02 12:06:59",
  //   "updateTime": "2023-09-02 12:06:59"
  // },
  // {
  //   "id": 4,
  //   "categoryName": "娱乐",
  //   "categoryAlias": "yl",
  //   "createTime": "2023-09-02 12:08:16",
  //   "updateTime": "2023-09-02 12:08:16"
  // },
  // {
  //   "id": 5,
  //   "categoryName": "军事",
  //   "categoryAlias": "js",
  //   "createTime": "2023-09-02 12:08:33",
  //   "updateTime": "2023-09-02 12:08:33"
  // }
])


const articleCategoryList = async () => {
  const result = await articleCategoryListService();
  categories.value = result.data
}

articleCategoryList()

//控制添加分类弹窗
const dialogVisible = ref(false)

//添加分类数据模型
const categoryModel = ref({
  categoryName: '',
  categoryAlias: ''
})
//添加分类表单校验
const rules = {
  categoryName: [
    {required: true, message: '请输入分类名称', trigger: 'blur'},
  ],
  categoryAlias: [
    {required: true, message: '请输入分类别名', trigger: 'blur'},
  ]
}
const title = ref('')

const form = ref(null)
const addCategory = async () => {
  const valid = await form.value.validate();
  if (valid) {
    await articleCategoryAddService(categoryModel.value);
    ElMessage.success("添加成功")
    await articleCategoryList()
    dialogVisible.value = false
  }
}

// Show Edit Dialog
const showDialog = (row) => {
  dialogVisible.value = true
  title.value = '编辑分类'
  categoryModel.value = {
    categoryName: row.categoryName,
    categoryAlias: row.categoryAlias,
    id: row.id
  }
}

const updateCategory = async () => {
  const valid = await form.value.validate();
  if (valid) {
    await articleCategoryUpdateService(categoryModel.value);
    ElMessage.success("添加成功")
    await articleCategoryList()
    dialogVisible.value = false
  }
}

const clearData = () => {
  categoryModel.value = {
    categoryName: '',
    categoryAlias: '',
  }
}

const deleteCategory = (row) => {
  ElMessageBox.confirm(
      '你确认要删除该分类信息吗？',
      '温馨提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(
      async () => {
        await articleCategoryDeleteService(row.id)
        ElMessage.success("删除成功")
        await articleCategoryList()
      }
  )
}
</script>
<template>
  <div role="main" aria-label="文章分类管理页面">
    <el-card class="page-container">
      <template #header>
        <header class="header">
          <h1 class="header-title">文章分类</h1>
          <div class="extra">
            <el-button 
              type="primary" 
              aria-label="添加新分类"
              @click="dialogVisible=true;title='添加分类';clearData()">
              添加分类
            </el-button>
          </div>
        </header>
      </template>
      <el-table 
        :data="categories" 
        style="width: 100%"
        role="table"
        aria-label="分类列表">
        <el-table-column label="序号" width="100" type="index" aria-label="序号"></el-table-column>
        <el-table-column label="分类名称" prop="categoryName" aria-label="分类名称"></el-table-column>
        <el-table-column label="分类别名" prop="categoryAlias" aria-label="分类别名"></el-table-column>
        <el-table-column label="操作" width="100" aria-label="操作">
          <template #default="{ row }">
            <el-button 
              :icon="Edit" 
              circle 
              plain 
              type="primary" 
              :aria-label="`编辑分类：${row.categoryName}`"
              title="编辑"
              @click="showDialog(row)" />
            <el-button 
              :icon="Delete" 
              circle 
              plain 
              type="danger" 
              :aria-label="`删除分类：${row.categoryName}`"
              title="删除"
              @click="deleteCategory(row)" />
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="没有数据" role="status" />
        </template>
      </el-table>

    <!-- 添加分类弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="title" 
      width="30%"
      role="dialog"
      :aria-labelledby="title">
      <el-form 
        ref="form" 
        :model="categoryModel" 
        :rules="rules" 
        label-width="100px" 
        style="padding-right: 30px"
        role="form"
        aria-label="分类表单">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input 
            v-model="categoryModel.categoryName" 
            minlength="1" 
            maxlength="10"
            aria-label="分类名称"
            aria-required="true" />
        </el-form-item>
        <el-form-item label="分类别名" prop="categoryAlias">
          <el-input 
            v-model="categoryModel.categoryAlias" 
            minlength="1" 
            maxlength="15"
            aria-label="分类别名"
            aria-required="true" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer" role="group" aria-label="对话框操作">
            <el-button 
              aria-label="取消操作"
              @click="dialogVisible = false">
              取消
            </el-button>
            <el-button 
              type="primary" 
              :aria-label="title==='添加分类'?'确认添加':'确认更新'"
              @click="title==='添加分类'?addCategory():updateCategory()">
              确认
            </el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
  </div>
</template>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;
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

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .header-title {
      margin: 0;
      font-size: var(--text-xl, 18px);
      font-weight: var(--font-semibold, 600);
      color: #1f2937;
      
      [data-theme="dark"] & {
        color: #f1f5f9;
      }
    }
  }
  
  :deep(.el-button--primary) {
    color: white !important;
    
    &:hover {
      color: white !important;
    }
  }
  
  :deep(.el-table) {
    .el-table__row {
      background-color: transparent;
    }
    
    .el-table__row--striped {
      background-color: rgba(0, 0, 0, 0.02);
      
      [data-theme="dark"] & {
        background-color: rgba(255, 255, 255, 0.02);
      }
    }
  }
  
  :deep(.el-card__header) {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
    border-bottom: 1px solid var(--surface-border);
    padding: 20px 24px;
    
    [data-theme="dark"] & {
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
    }
  }
  
  :deep(.el-table) {
    background-color: transparent;
    
    .el-table__header-wrapper {
      .el-table__header {
        th {
          background: transparent;
        }
      }
    }
  }
  
  :deep(.el-button--primary) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    transition: all 0.2s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
    }
    
    [data-theme="dark"] & {
      background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
    }
  }
}
</style>
