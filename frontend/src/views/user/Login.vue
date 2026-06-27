<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <div class="links">
          <router-link to="/register">还没有账号?立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    await userStore.login(loginForm)
    ElMessage.success('登录成功')

    // 使用 await 等待导航完成,并捕获导航失败
    await router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    // 登录或导航失败时,重置 loading 状态
    loading.value = false
    return
  }
  // 登录成功并跳转后才重置 loading
  loading.value = false
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 140px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.login-card {
  width: 400px;
  padding: 20px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.links {
  text-align: center;
  margin-top: 10px;
}

.links a {
  color: #409eff;
  text-decoration: none;
}
</style>
