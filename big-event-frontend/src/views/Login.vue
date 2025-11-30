<script setup>

import {Lock, User} from "@element-plus/icons-vue"
import {ref} from "vue";
import {userLoginService, userRegisterService} from "@/api/user";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import {useTokenStore} from "@/stores/token";

// Register page and Login page use the same view.
// By default, show login.
const isRegister = ref(false);
const rememberMe = ref(false);
const registerData = ref({
  username: '',
  password: '',
  rePassword: ''
});

const checkRePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== registerData.value.password) {
    callback(new Error('两次密码不一样'))
  } else {
    callback()
  }
}
const rules = {
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 5, max: 16, message: '长度为5-16位', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 5, max: 16, message: '长度为5-16位', trigger: 'blur'},
  ],
  rePassword: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {validator: checkRePassword, trigger: 'blur'},
  ]
}

const register = async () => {
  const valid = await form.value.validate();
  if (valid) {
    try {
    const result = await userRegisterService(registerData.value);
      // 成功状态码范围：200-299
      if (result && result.code >= 200 && result.code < 300) {
        ElMessage.success(result.message || '注册成功')
        // 注册成功后切换到登录页面
        isRegister.value = false
        clearRegisterData()
      } else {
        ElMessage.error(result?.message || '注册失败')
      }
    } catch (error) {
      ElMessage.error(error?.message || '注册失败')
    }
  }
}

const router = useRouter()
const tokenStore = useTokenStore();
const form = ref(null)
const login = async () => {
  const valid = await form.value.validate();
  if (valid) {
    try {
    // 添加rememberMe参数
    const loginParams = {
      ...registerData.value,
      rememberMe: rememberMe.value
    };
    const result = await userLoginService(loginParams);
      // 成功状态码范围：200-299
      if (result && result.code >= 200 && result.code < 300 && result.data) {
        ElMessage.success(result.message || '登录成功')
    tokenStore.setToken(result.data)
    router.push('/')
      } else {
        ElMessage.error(result?.message || '登录失败')
      }
    } catch (error) {
      ElMessage.error(error?.message || '登录失败')
    }
  }
}

const clearRegisterData = () => {
  registerData.value = {
    username: '',
    password: '',
    rePassword: ''
  }
}


</script>

<template>
  <div class="login-page login-enhanced">
    <div class="login-background"></div>
    
    <el-row class="login-container">
      <el-col :span="12" class="bg"></el-col>
      <el-col :span="6" :offset="3" class="form-wrapper">
        <!-- 注册表单 -->
        <el-form ref="form" size="large" autocomplete="on" v-if="isRegister" :model="registerData" :rules="rules" class="login-form-container">
          <el-form-item>
            <h1 class="title gradient-text">注册</h1>
          </el-form-item>
          <el-form-item prop="username">
            <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.username" class="input-enhanced"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input :prefix-icon="Lock" type="password" placeholder="请输入密码"
                      v-model="registerData.password" class="input-enhanced"></el-input>
          </el-form-item>
          <el-form-item prop="rePassword">
            <el-input :prefix-icon="Lock" type="password" placeholder="请输入再次密码"
                      v-model="registerData.rePassword" class="input-enhanced"></el-input>
          </el-form-item>
          <!-- 注册按钮 -->
          <el-form-item>
            <el-button class="button btn-enhanced" type="primary" auto-insert-space @click="register">
              注册
            </el-button>
          </el-form-item>
          <el-form-item class="flex">
            <el-link type="info" :underline="false" @click="isRegister = false;clearRegisterData()">
              ← 返回
            </el-link>
          </el-form-item>
        </el-form>

        <!-- 登录表单 -->
        <el-form ref="form" size="large" autocomplete="off" v-else :model="registerData" :rules="rules" class="login-form-container">
          <el-form-item>
            <h1 class="title gradient-text">登录</h1>
          </el-form-item>
          <el-form-item prop="username">
            <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.username" class="input-enhanced"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码"
                      v-model="registerData.password" class="input-enhanced"></el-input>
          </el-form-item>
          <el-form-item class="flex">
            <div class="flex-between w-full">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>
          </el-form-item>
          <!-- 登录按钮 -->
          <el-form-item>
            <el-button class="button btn-enhanced" type="primary" auto-insert-space @click="login">
              登录
            </el-button>
          </el-form-item>
          <el-form-item class="flex">
            <el-link type="info" :underline="false" @click="isRegister = true;clearRegisterData()">
              注册 →
            </el-link>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  position: relative;
  overflow: hidden;
  
  .login-container {
    position: relative;
    z-index: 1;
    height: 100%;
    align-items: center;
  }
}

.bg {
  background: url('@/assets/logo2.png') no-repeat 60% center / 240px auto,
  url('@/assets/login_bg.jpg') no-repeat center / cover;
  border-radius: 0 20px 20px 0;
  height: 100%;
  box-shadow: 20px 0 40px rgba(0,0,0,0.1);
}

.form-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.title {
  margin: 0 auto 20px;
  font-size: 2rem;
  font-weight: bold;
}

.button {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.flex {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.w-full {
  width: 100%;
}

// Animation keyframes
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}
</style>
