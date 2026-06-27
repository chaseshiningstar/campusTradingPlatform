<template>
  <div class="comment-list-page">
    <el-card>
      <h3>留言管理</h3>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 380px"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="所属物品" min-width="140">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goToItem(row.itemId)">
              {{ row.itemTitle || '未知物品' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="留言用户" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="24" :src="row.avatar || '/uploads/avatars/default.jpg'" />
              <span>{{ row.nickname || row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="回复目标" width="180">
          <template #default="{ row }">
            <template v-if="row.replyToUserNickname">
              <el-popover placement="top" :width="300" trigger="hover">
                <template #reference>
                  <span class="reply-tag">回复 @{{ row.replyToUserNickname }}</span>
                </template>
                <div class="parent-preview">
                  <div class="parent-label">被回复的原话：</div>
                  <div class="parent-text">{{ row.parentContent || '(内容已删除)' }}</div>
                </div>
              </el-popover>
            </template>
            <span v-else class="top-level-tag">一级留言</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="留言时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminComments, deleteComment } from '@/api/comment'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  startTime: '',
  endTime: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: queryParams.current,
      size: queryParams.size,
      startTime: queryParams.startTime || undefined,
      endTime: queryParams.endTime || undefined
    }
    const res = await getAdminComments(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('加载留言列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0]
    queryParams.endTime = dateRange.value[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
  queryParams.current = 1
  loadData()
}

const handleReset = () => {
  dateRange.value = null
  queryParams.startTime = ''
  queryParams.endTime = ''
  queryParams.current = 1
  loadData()
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条留言吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteComment(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除失败:', error)
    }
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const goToItem = (itemId) => {
  if (itemId) {
    router.push(`/item/${itemId}`)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.comment-list-page h3 {
  margin-bottom: 20px;
  color: #303133;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.reply-tag {
  font-size: 12px;
  color: #409eff;
}

.top-level-tag {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.parent-preview {
  font-size: 13px;
}

.parent-label {
  color: #909399;
  margin-bottom: 6px;
  font-size: 12px;
}

.parent-text {
  color: #303133;
  line-height: 1.6;
  word-break: break-word;
  padding: 8px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}
</style>
