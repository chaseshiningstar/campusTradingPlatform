<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :md="16">
        <el-card>
          <h3>个人信息</h3>
          <el-form :model="userForm" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userForm.nickname" maxlength="50" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="userForm.gender">
                <el-radio :value="0">保密</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card style="margin-top: 20px">
          <h3>修改密码</h3>
          <el-form :model="passwordForm" ref="pwdFormRef" label-width="100px" :rules="pwdRules">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 头像上传 -->
      <el-col :md="8">
        <el-card>
          <h3>头像设置</h3>
          <div class="avatar-preview">
            <el-avatar :size="150" :src="userForm.avatar || '/uploads/avatars/default.jpg'" />
          </div>
          <el-upload
            action="#"
            :auto-upload="true"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            accept="image/*"
            style="margin-top: 20px; text-align: center;"
          >
            <el-button type="primary" :loading="uploading">
              {{ uploading ? '上传中...' : '选择头像' }}
            </el-button>
            <template #tip>
              <div class="avatar-tip">支持 JPG/PNG/GIF,文件不超过 2MB<br />选择后自动上传,只保留最新头像</div>
            </template>
          </el-upload>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo, updateUserInfo, changePassword, uploadAvatar } from '@/api/user'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const pwdFormRef = ref(null)
const loading = ref(false)
const uploading = ref(false)

const userForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 0,
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过50', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6-20个字符', trigger: 'blur' },
    { pattern: /^\S+$/, message: '密码不能包含空格', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    Object.assign(userForm, res.data)
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 更新用户信息
const handleUpdate = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    await updateUserInfo(userForm)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    loading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await pwdFormRef.value.validate()
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  }
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isImage) {
    ElMessage.error('头像只能为 JPG/PNG/GIF/WEBP 格式')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义上传(选择后自动上传,只保留最新一个)
const handleAvatarUpload = async (options) => {
  const file = options.file
  uploading.value = true
  try {
    const res = await uploadAvatar(file)
    userForm.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page h3 {
  margin-bottom: 20px;
  color: #303133;
}

.avatar-preview {
  text-align: center;
  padding: 20px 0;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.6;
}
</style>
