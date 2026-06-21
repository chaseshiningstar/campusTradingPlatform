<template>
  <div class="audit-page">
    <el-card>
      <h3>物品审核</h3>

      <el-table :data="auditList" style="width: 100%">
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <img :src="'/uploads/items/default.jpg'" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="卖家" width="120">
          <template #default="{ row }">用户ID: {{ row.sellerId }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleAudit(row, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="showRejectDialog(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadAuditList"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="500px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuditItemList, auditItem } from '@/api/admin'
import { ElMessage } from 'element-plus'

const auditList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const rejectDialogVisible = ref(false)
const currentItem = ref(null)
const rejectReason = ref('')

// 加载待审核列表
const loadAuditList = async () => {
  try {
    const res = await getAuditItemList({
      current: currentPage.value,
      size: pageSize.value
    })
    auditList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载失败:', error)
  }
}

// 通过审核
const handleAudit = async (item, action) => {
  try {
    await auditItem({ itemId: item.id, action })
    ElMessage.success(action === 1 ? '审核通过' : '已驳回')
    loadAuditList()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

// 显示驳回对话框
const showRejectDialog = (item) => {
  currentItem.value = item
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 执行驳回
const handleReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await auditItem({
      itemId: currentItem.value.id,
      action: 2,
      reason: rejectReason.value
    })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    loadAuditList()
  } catch (error) {
    console.error('驳回失败:', error)
  }
}

onMounted(() => {
  loadAuditList()
})
</script>

<style scoped>
.audit-page h3 { margin-bottom: 20px; color: #303133; }
</style>
