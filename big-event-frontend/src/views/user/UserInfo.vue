<script setup>
import {ref} from 'vue'
import {useUserInfoStore} from "@/stores/userInfo";
import {userInfoUpdateService} from "@/api/user";
import {ElMessage} from "element-plus";

const userInfoStore = useUserInfoStore();


const userInfo = ref({
  ...userInfoStore.userInfo
})
const formRef = ref(null)

const rules = {
  nickname: [
    {required: true, message: '请输入用户昵称', trigger: 'blur'},
    {
      pattern: /^\S{2,10}$/,
      message: '昵称必须是2-10位的非空字符串',
      trigger: 'blur'
    }
  ],
  email: [
    {required: true, message: '请输入用户邮箱', trigger: 'blur'},
    {type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
  ]
}

const updateUserInfo = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate()
  
  try {
    const res = await userInfoUpdateService(userInfo.value)
    // 成功状态码范围：200-299
    if (res && res.code >= 200 && res.code < 300) {
      ElMessage.success(res?.message || '修改成功')
    userInfoStore.setUserInfo(userInfo.value)
    } else {
      ElMessage.error(res?.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error(error?.message || '修改失败')
  }
}
</script>
<template>
  <el-card class="page-container enhanced-card">
    <template #header>
      <div class="header">
        <span>基本资料</span>
      </div>
    </template>
    <el-row>
      <el-col :span="12">
        <el-form ref="formRef" :model="userInfo" :rules="rules" label-width="100px" size="large">
          <el-form-item label="登录名称">
            <el-input v-model="userInfo.username" disabled></el-input>
          </el-form-item>
          <el-form-item label="用户昵称" prop="nickname">
            <el-input v-model="userInfo.nickname"></el-input>
          </el-form-item>
          <el-form-item label="用户邮箱" prop="email">
            <el-input v-model="userInfo.email"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="updateUserInfo">提交修改</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </el-card>
</template>
