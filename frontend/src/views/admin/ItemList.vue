<template>
  <div class="item-list-page">
    <el-card>
      <h3>物品管理</h3>
      <el-table :data="itemList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/item/${row.id}`)">查看</el-button>
            <el-button size="small" type="danger" @click="handleOffline(row)" v-if="row.status === 1">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getItemList } from '@/api/item'
import { adminOfflineItem } from '@/api/admin'
import { ElMessage } from 'element-plus'

const itemList = ref([])

const loadItems = async () => {
  try {
    const res = await getItemList({ current: 1, size: 20 })
    itemList.value = res.data.records
  } catch (error) {
    console.error('加载失败:', error)
  }
}

const handleOffline = async (item) => {
  try {
    await adminOfflineItem(item.id, '违规物品')
    ElMessage.success('已下架')
    loadItems()
  } catch (error) {
    console.error('下架失败:', error)
  }
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info', 3: '', 4: 'danger' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已发布', 2: '已下架', 3: '已售出', 4: '审核驳回' }
  return map[status] || '未知'
}

onMounted(() => { loadItems() })
</script>

<style scoped>
.item-list-page h3 { margin-bottom: 20px; color: #303133; }
</style>
