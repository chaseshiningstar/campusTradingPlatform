<template>
  <div class="my-items-page">
    <el-card>
      <h2>我的发布</h2>

      <el-table :data="itemList" style="width: 100%">
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <img :src="row.coverImage || '/uploads/items/default.jpg'" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/item/${row.id}`)">查看</el-button>
            <el-button size="small" type="warning" @click="handleOffline(row)" v-if="row.status === 1">下架</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadItems"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyItems, offlineItem, deleteItem } from '@/api/item'
import { ElMessage, ElMessageBox } from 'element-plus'

const itemList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载我的物品
const loadItems = async () => {
  try {
    const res = await getMyItems({
      current: currentPage.value,
      size: pageSize.value
    })
    itemList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 下架物品
const handleOffline = async (item) => {
  try {
    await ElMessageBox.confirm('确定要下架该物品吗?', '提示', { type: 'warning' })
    await offlineItem(item.id)
    ElMessage.success('下架成功')
    loadItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

// 删除物品
const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除该物品吗?此操作不可恢复!', '警告', { type: 'error' })
    await deleteItem(item.id)
    ElMessage.success('删除成功')
    loadItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }
  return map[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已下架', 3: '已售出', 4: '审核驳回' }
  return map[status] || '未知'
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.my-items-page h2 {
  margin-bottom: 20px;
  color: #303133;
}
</style>
