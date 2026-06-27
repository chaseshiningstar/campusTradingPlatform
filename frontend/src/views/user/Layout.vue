<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <h2>校园二手交易平台</h2>
        </div>

        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索物品..."
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="nav-menu">
          <el-menu mode="horizontal" :ellipsis="false">
            <el-menu-item @click="$router.push('/')">首页</el-menu-item>
            <el-menu-item @click="$router.push('/publish')" v-if="isLoggedIn">发布物品</el-menu-item>
            <el-menu-item @click="$router.push('/my-items')" v-if="isLoggedIn">我的发布</el-menu-item>

            <!-- 私信入口(带未读徽标) -->
            <el-menu-item v-if="isLoggedIn" @click="$router.push('/messages')">
              私信
              <el-badge
                :value="unreadCount"
                :hidden="unreadCount === 0"
                :max="99"
                class="nav-badge"
              />
            </el-menu-item>

            <!-- 留言通知入口(独立模块) -->
            <el-menu-item v-if="isLoggedIn" @click="$router.push('/comments')">
              留言
            </el-menu-item>

            <el-sub-menu index="user" v-if="isLoggedIn">
              <template #title>{{ userInfo?.nickname || '用户' }}</template>
              <el-menu-item @click="$router.push('/profile')">个人中心</el-menu-item>
              <el-menu-item @click="$router.push('/admin')" v-if="isAdmin">管理后台</el-menu-item>
              <el-menu-item @click="handleLogout">退出登录</el-menu-item>
            </el-sub-menu>

            <el-menu-item v-else @click="$router.push('/login')">登录</el-menu-item>
          </el-menu>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="footer">
      <p>&copy; 2026 校园二手交易平台 - 让闲置流动起来</p>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { chatWebSocket } from '@/utils/websocket'
import { Search } from '@element-plus/icons-vue'
import { ElMessageBox, ElNotification } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)
const userInfo = computed(() => userStore.userInfo)
const unreadCount = computed(() => userStore.unreadCount)

let unsubscribe = null

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/')
  })
}

// 全局WebSocket消息监听:接收非当前会话的新私信时弹出通知
const onWsMessage = (msg) => {
  if (msg.type === 'PRIVATE_MESSAGE') {
    // 检查是否在当前聊天页面(如果在,该页面自己处理,不重复弹通知)
    const currentPath = router.currentRoute.value.path
    const isOnChatPage = currentPath.startsWith('/chat/') &&
      router.currentRoute.value.params.userId == msg.senderId
    if (!isOnChatPage) {
      userStore.incrementUnread()
      const senderName = msg.senderNickname || msg.senderUsername || '新消息'
      ElNotification({
        title: '收到新私信',
        message: `${senderName}: ${msg.content}`.slice(0, 60),
        type: 'info',
        duration: 4000,
        onClick: () => {
          router.push(`/chat/${msg.senderId}`)
        }
      })
    }
  }
}

onMounted(() => {
  // 订阅全局WebSocket消息
  unsubscribe = chatWebSocket.onMessage(onWsMessage)
  // 刷新未读数
  if (isLoggedIn.value) {
    userStore.refreshUnreadCount()
  }
})

onUnmounted(() => {
  if (unsubscribe) unsubscribe()
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
}

.logo {
  cursor: pointer;
  color: #409eff;
}

.search-bar {
  flex: 1;
  margin: 0 40px;
  max-width: 400px;
}

.nav-menu {
  flex-shrink: 0;
}

.nav-badge {
  margin-left: 6px;
  margin-top: -8px;
}

.main-content {
  flex: 1;
  background: #f5f7fa;
  padding: 20px;
}

.footer {
  text-align: center;
  padding: 20px;
  background: #fff;
  color: #909399;
}
</style>
