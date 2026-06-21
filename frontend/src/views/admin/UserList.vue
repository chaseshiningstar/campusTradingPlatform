<template>
  <div class="user-list-page">
    <el-card>
      <h3>用户管理</h3>

      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadUserList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, toggleUserStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await getUserList({
      current: currentPage.value,
      size: pageSize.value
    })
    userList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 封禁/解封用户
const handleToggleStatus = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要${user.status === 1 ? '封禁' : '解封'}该用户吗?`, '提示', { type: 'warning' })
    await toggleUserStatus(user.id)
    ElMessage.success('操作成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-list-page h3 { margin-bottom: 20px; color: #303133; }
</style>
