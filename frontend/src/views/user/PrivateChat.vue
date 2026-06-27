<template>
  <div class="chat-page">
    <el-card class="chat-card" body-style="padding: 0;">
      <!-- 顶部对方信息 -->
      <div class="chat-header">
        <el-button text @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <el-avatar :size="36" :src="otherAvatar || '/uploads/avatars/default.jpg'" />
        <div class="other-info">
          <span class="other-name">{{ otherNickname || otherUsername || '加载中...' }}</span>
          <span class="other-status" :class="{ online: isOtherOnline }">
            {{ isOtherOnline ? '在线' : '离线' }}
          </span>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef" v-loading="loading">
        <div v-if="messages.length === 0 && !loading" class="empty-chat">
          <el-empty description="暂无消息,发送第一条私信吧" :image-size="80" />
        </div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="{ mine: msg.senderId === currentUserId }"
        >
          <el-avatar :size="36" :src="getMsgAvatar(msg) || '/uploads/avatars/default.jpg'" />
          <div class="msg-bubble-wrap">
            <div class="msg-bubble">{{ msg.content }}</div>
            <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入消息,Enter发送,Shift+Enter换行"
          maxlength="500"
          show-word-limit
          resize="none"
          @keydown.enter.exact.prevent="handleSend"
        />
        <el-button
          type="primary"
          :loading="sending"
          :disabled="!inputText.trim()"
          @click="handleSend"
          style="margin-left: 10px; align-self: flex-end;"
        >
          发送
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMessageHistory, sendMessage, markConversationRead } from '@/api/message'
import { getPublicUserInfo } from '@/api/user'
import { chatWebSocket } from '@/utils/websocket'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const sending = ref(false)
const otherNickname = ref('')
const otherUsername = ref('')
const otherAvatar = ref('')
const isOtherOnline = ref(false)
const messageListRef = ref(null)

const currentUserId = computed(() => userStore.userInfo?.id)
const otherUserId = computed(() => Number(route.params.userId))

let unsubscribe = null

// 加载历史消息
const loadHistory = async () => {
  if (!otherUserId.value) return
  loading.value = true
  try {
    const res = await getMessageHistory(otherUserId.value)
    messages.value = res.data || []
    // 进入会话即标记已读
    await markConversationRead(otherUserId.value)
    // 刷新全局未读数
    userStore.refreshUnreadCount()
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载历史消息失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载对方信息
const loadOtherInfo = async () => {
  if (!otherUserId.value) return
  try {
    const res = await getPublicUserInfo(otherUserId.value)
    otherNickname.value = res.data?.nickname || ''
    otherUsername.value = res.data?.username || ''
    otherAvatar.value = res.data?.avatar || ''
  } catch (error) {
    console.error('加载对方信息失败:', error)
  }
}

// 发送消息
const handleSend = async () => {
  const content = inputText.value.trim()
  if (!content) return
  if (content.length > 500) {
    ElMessage.warning('消息不能超过500个字符')
    return
  }

  sending.value = true
  try {
    const res = await sendMessage(otherUserId.value, content)
    // 将返回的消息加入列表(若未通过WebSocket回环)
    const exists = messages.value.some(m => m.id === res.data.id)
    if (!exists) {
      messages.value.push(res.data)
    }
    inputText.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('发送失败:', error)
  } finally {
    sending.value = false
  }
}

// WebSocket消息回调
const onWsMessage = (msg) => {
  if (msg.type === 'PRIVATE_MESSAGE' && msg.senderId === otherUserId.value) {
    // 收到对方发来的新消息
    const exists = messages.value.some(m => m.id === msg.id)
    if (!exists) {
      messages.value.push(msg)
    }
    // 标记已读
    markConversationRead(otherUserId.value).then(() => userStore.refreshUnreadCount())
    nextTick(() => scrollToBottom())
  } else if (msg.type === 'MESSAGE_SENT' && msg.receiverId === otherUserId.value) {
    // 自己在其他设备发送的消息,同步到当前会话
    const exists = messages.value.some(m => m.id === msg.id)
    if (!exists) {
      messages.value.push(msg)
      nextTick(() => scrollToBottom())
    }
  } else if (msg.type === 'PRIVATE_MESSAGE') {
    // 其他会话的消息,仅刷新未读数
    userStore.incrementUnread()
  }
}

const getMsgAvatar = (msg) => {
  if (msg.senderId === currentUserId.value) {
    return userStore.userInfo?.avatar
  }
  return otherAvatar.value
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM-DD HH:mm')
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

onMounted(() => {
  loadOtherInfo()
  loadHistory()
  unsubscribe = chatWebSocket.onMessage(onWsMessage)
})

onUnmounted(() => {
  if (unsubscribe) unsubscribe()
})
</script>

<style scoped>
.chat-page {
  max-width: 800px;
  margin: 0 auto;
}

.chat-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 160px);
  min-height: 500px;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.other-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.other-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.other-status {
  font-size: 12px;
  color: #909399;
}

.other-status.online {
  color: #67c23a;
}

.other-status.online::before {
  content: '●';
  margin-right: 4px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.empty-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-row.mine {
  flex-direction: row-reverse;
}

.msg-bubble-wrap {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-row.mine .msg-bubble-wrap {
  align-items: flex-end;
}

.msg-bubble {
  padding: 10px 14px;
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-row.mine .msg-bubble {
  background: #409eff;
  color: #fff;
}

.msg-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  padding: 0 4px;
}

.input-area {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  align-items: flex-end;
}

.input-area :deep(.el-textarea) {
  flex: 1;
}
</style>
