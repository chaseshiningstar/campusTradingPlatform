<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff"><el-icon><Goods /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.itemCount }}</div>
            <div class="stat-label">物品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c"><el-icon><DocumentChecked /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingAudit }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c"><el-icon><ChatDotRound /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.commentCount }}</div>
            <div class="stat-label">评论总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <h3>快捷操作</h3>
      <el-button type="primary" @click="$router.push('/admin/audit')">去审核物品</el-button>
      <el-button type="success" @click="$router.push('/admin/users')">用户管理</el-button>
      <el-button type="warning" @click="$router.push('/admin/items')">物品管理</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/admin'

const stats = ref({
  itemCount: 0,
  pendingAudit: 0,
  userCount: 0,
  commentCount: 0
})

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 30px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

h3 {
  margin-bottom: 20px;
  color: #303133;
}
</style>
