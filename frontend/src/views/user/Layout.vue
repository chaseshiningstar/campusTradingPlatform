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
            <el-menu-item @click="$router.push('/messages')" v-if="isLoggedIn">消息</el-menu-item>

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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)
const userInfo = computed(() => userStore.userInfo)

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
