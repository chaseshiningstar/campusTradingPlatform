<template>
  <div class="messages-page">
    <el-card>
      <div class="header">
        <div>
          <h3>私信</h3>
          <p class="subtitle">与其他用户的即时私信</p>
        </div>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-icon :size="20" color="#409eff"><ChatDotRound /></el-icon>
        </el-badge>
      </div>

      <div v-loading="loading">
        <div v-if="conversations.length === 0" class="empty-state">
          <el-empty description="暂无私信会话,去物品详情页发起私信吧" />
        </div>

        <div v-else class="conversation-list">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ unread: isUnread(conv) }"
            @click="goToChat(getOtherUserId(conv))"
          >
            <div class="conv-avatar">
              <el-avatar :size="48" :src="getOtherAvatar(conv) || '/uploads/avatars/default.jpg'" />
              <span v-if="isUnread(conv)" class="unread-dot"></span>
            </div>
            <div class="conv-body">
              <div class="conv-header">
                <span class="conv-name">{{ getOtherName(conv) }}</span>
                <span class="conv-time">{{ formatTime(conv.createTime) }}</span>
              </div>
              <p class="conv-preview">{{ conv.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getConversations } from '@/api/message'
import { chatWebSocket } from '@/utils/websocket'
import { ChatDotRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

const conversations = ref([])
const loading = ref(false)
const unreadCount = computed(() => userStore.unreadCount)

let unsubscribe = null

const loadConversations = async () => {
  loading.value = true
  try {
    const res = await getConversations()
    conversations.value = res.data || []
    // 同步未读数
    userStore.refreshUnreadCount()
  } catch (error) {
    console.error('加载会话失败:', error)
  } finally {
    loading.value = false
  }
}

// 判断某条会话是否未读(我是接收者且is_read=0)
const isUnread = (conv) => {
  return conv.receiverId === userStore.userInfo?.id && conv.isRead === 0
}

// 获取对方用户ID
const getOtherUserId = (conv) => {
  const myId = userStore.userInfo?.id
  return conv.senderId === myId ? conv.receiverId : conv.senderId
}

// 获取对方头像
const getOtherAvatar = (conv) => {
  const myId = userStore.userInfo?.id
  if (conv.senderId === myId) {
    return conv.receiverAvatar
  }
  return conv.senderAvatar
}

// 获取对方昵称
const getOtherName = (conv) => {
  const myId = userStore.userInfo?.id
  if (conv.senderId === myId) {
    return conv.receiverNickname || conv.receiverUsername || '未知用户'
  }
  return conv.senderNickname || conv.senderUsername || '未知用户'
}

const formatTime = (time) => {
  if (!time) return ''
  const now = dayjs()
  const t = dayjs(time)
  if (now.diff(t, 'day') === 0) {
    return t.format('HH:mm')
  }
  if (now.diff(t, 'day') < 7) {
    return t.format('MM-DD HH:mm')
  }
  return t.format('YYYY-MM-DD')
}

const goToChat = (userId) => {
  router.push(`/chat/${userId}`)
}

// 监听WebSocket新消息,实时刷新会话列表
const onWsMessage = (msg) => {
  if (msg.type === 'PRIVATE_MESSAGE') {
    // 收到新私信,刷新会话列表并增加未读
    loadConversations()
    userStore.incrementUnread()
  } else if (msg.type === 'MESSAGE_SENT') {
    // 自己在其他设备发送了消息,刷新会话列表
    loadConversations()
  }
}

onMounted(() => {
  loadConversations()
  // 订阅WebSocket消息
  unsubscribe = chatWebSocket.onMessage(onWsMessage)
})

onUnmounted(() => {
  if (unsubscribe) unsubscribe()
})
</script>

<style scoped>
.messages-page {
  max-width: 800px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.messages-page h3 {
  margin-bottom: 4px;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.empty-state {
  padding: 40px 0;
}

.conversation-list {
  display: flex;
  flex-direction: column;
}

.conversation-item {
  display: flex;
  gap: 14px;
  padding: 14px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.conversation-item:hover {
  background: #f5f7fa;
}

.conversation-item.unread {
  background: #ecf5ff;
}

.conversation-item.unread:hover {
  background: #e0efff;
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #f56c6c;
  border-radius: 50%;
  border: 2px solid #fff;
}

.conv-body {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.conv-time {
  font-size: 12px;
  color: #909399;
}

.conv-preview {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
