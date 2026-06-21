<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :md="16">
        <el-card>
          <h3>个人信息</h3>
          <el-form :model="userForm" ref="formRef" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" />
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
          <el-form :model="passwordForm" ref="pwdFormRef" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" />
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
            :auto-upload="false"
            :on-change="handleAvatarChange"
            accept="image/*"
            style="margin-top: 20px"
          >
            <el-button type="primary">选择头像</el-button>
          </el-upload>
          <el-button type="success" @click="handleUploadAvatar" :loading="uploading" style="margin-top: 10px; width: 100%">
            上传头像
          </el-button>
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
const avatarFile = ref(null)

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
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }

  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败:', error)
  }
}

// 处理头像选择
const handleAvatarChange = (file) => {
  avatarFile.value = file.raw
}

// 上传头像
const handleUploadAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  try {
    uploading.value = true
    const res = await uploadAvatar(avatarFile.value)
    userForm.avatar = res.data
    ElMessage.success('上传成功')
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
</style>
