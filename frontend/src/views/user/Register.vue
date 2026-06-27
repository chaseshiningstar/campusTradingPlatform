<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>用户注册</h2>
      <el-form :model="registerForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名(4-20位,字母数字下划线)" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码(6-20位,不含空格)" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="registerForm.email" placeholder="邮箱(必填,用于接收验证码)" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="emailCode">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input v-model="registerForm.emailCode" placeholder="邮箱验证码" prefix-icon="Key" maxlength="6" />
            <el-button
              type="primary"
              plain
              :disabled="codeCooldown > 0 || sendingCode"
              :loading="sendingCode"
              @click="handleSendCode"
              style="width: 140px;"
            >
              {{ codeCooldown > 0 ? `${codeCooldown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="手机号(可选)" prefix-icon="Phone" maxlength="11" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <div class="links">
          <router-link to="/login">已有账号?立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { sendRegisterCode } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const codeCooldown = ref(0)
let cooldownTimer = null

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  emailCode: '',
  phone: ''
})

const validatePass = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateEmailCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入邮箱验证码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('验证码为6位数字'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在4-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6-20个字符', trigger: 'blur' },
    { pattern: /^\S+$/, message: '密码不能包含空格', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, validator: validateEmailCode, trigger: 'blur' }
  ],
  phone: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const startCooldown = (seconds = 60) => {
  codeCooldown.value = seconds
  if (cooldownTimer) clearInterval(cooldownTimer)
  cooldownTimer = setInterval(() => {
    codeCooldown.value--
    if (codeCooldown.value <= 0) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请先填写邮箱')
    return
  }
  // 简单格式校验
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  sendingCode.value = true
  try {
    await sendRegisterCode(registerForm.email)
    ElMessage.success('验证码已发送,请查收邮件(开发模式验证码统一为123456)')
    startCooldown(60)
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const { confirmPassword, ...data } = registerForm
    await userStore.register(data)
    ElMessage.success('注册成功,请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (cooldownTimer) clearInterval(cooldownTimer)
})
</script>

<style scoped>
.register-page {
  min-height: calc(100vh - 140px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

.register-card {
  width: 400px;
  padding: 20px;
}

.register-card h2 {
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
