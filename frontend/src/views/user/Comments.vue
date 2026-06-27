<template>
  <div class="comments-page">
    <el-card>
      <h3>留言通知</h3>
      <p class="subtitle">您发布物品收到的留言</p>

      <div v-loading="loading">
        <div v-if="messages.length === 0" class="empty-state">
          <el-empty description="暂无留言消息" />
        </div>

        <div v-else class="message-list">
          <div v-for="msg in messages" :key="msg.id" class="message-item" @click="goToItem(msg.itemId)">
            <div class="msg-avatar">
              <el-avatar :size="44" :src="msg.avatar || '/uploads/avatars/default.jpg'" />
            </div>
            <div class="msg-body">
              <div class="msg-header">
                <span class="msg-user">{{ msg.nickname || msg.username }}</span>
                <span v-if="msg.replyToUserNickname" class="msg-reply-to">
                  <el-popover placement="top" :width="260" trigger="hover">
                    <template #reference>
                      <span class="reply-link"><el-icon><Right /></el-icon> 回复 @{{ msg.replyToUserNickname }}</span>
                    </template>
                    <div class="parent-preview">{{ msg.parentContent || '(内容已删除)' }}</div>
                  </el-popover>
                </span>
                <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <p class="msg-content">{{ msg.content }}</p>
              <div class="msg-item-title">
                <el-icon><Goods /></el-icon>
                <span>{{ msg.itemTitle || '未知物品' }}</span>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyMessages } from '@/api/comment'
import { Right, Goods, ArrowRight } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const messages = ref([])
const loading = ref(false)

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getMyMessages()
    messages.value = res.data || []
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const goToItem = (itemId) => {
  router.push({ path: `/item/${itemId}`, hash: '#comments' })
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.comments-page {
  max-width: 800px;
  margin: 0 auto;
}

.comments-page h3 {
  margin-bottom: 4px;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin-bottom: 20px;
}

.empty-state {
  padding: 40px 0;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.message-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.2s;
}

.message-item:hover {
  background: #f5f7fa;
  margin: 0 -16px;
  padding-left: 16px;
  padding-right: 16px;
  border-radius: 6px;
}

.msg-avatar {
  flex-shrink: 0;
  padding-top: 2px;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.msg-user {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.msg-reply-to {
  font-size: 12px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 2px;
}

.msg-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.msg-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 0 8px 0;
  word-break: break-word;
}

.msg-item-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  padding: 6px 10px;
  background: #ecf5ff;
  border-radius: 4px;
  display: inline-flex;
}

.msg-item-title span {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reply-link {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.reply-link:hover {
  text-decoration: underline;
}

.parent-preview {
  font-size: 13px;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
}
</style>
