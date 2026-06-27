import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { getUserInfo } from '@/api/user'
import { getUnreadCount } from '@/api/message'
import { chatWebSocket, getBackendBaseURL } from '@/utils/websocket'

/**
 * 解码JWT并检查是否过期
 */
function isTokenExpired(token) {
  if (!token) return true
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    // exp 是秒级时间戳,乘以1000转毫秒
    return payload.exp * 1000 < Date.now()
  } catch {
    return true // 解码失败视为过期
  }
}

/**
 * 用于多标签页账号同步的广播通道
 * 当某个标签页登录/登出/切换账号时,其他标签页会收到通知并同步状态
 */
const broadcastChannel = typeof BroadcastChannel !== 'undefined'
  ? new BroadcastChannel('campus_trading_auth')
  : null

// 当前登录会话的唯一标识,用于区分"自己发起的变更"和"其他标签页发起的变更"
const SESSION_ID = Date.now() + '-' + Math.random().toString(36).slice(2)

export const useUserStore = defineStore('user', () => {
  // 初始化时检查token是否过期,过期则清除
  const storedToken = localStorage.getItem('token')
  if (storedToken && isTokenExpired(storedToken)) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const token = ref(isTokenExpired(storedToken) ? '' : (storedToken || ''))
  const userInfo = ref(token.value ? JSON.parse(localStorage.getItem('userInfo') || 'null') : null)
  const unreadCount = ref(0)

  const isLoggedIn = computed(() => {
    return !!token.value && !isTokenExpired(token.value)
  })
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  /**
   * 建立WebSocket连接并拉取未读数
   */
  function connectWebSocket() {
    if (!token.value || isTokenExpired(token.value)) return
    const baseURL = getBackendBaseURL()
    chatWebSocket.connect(baseURL, token.value)
    // 拉取未读消息数
    refreshUnreadCount()
  }

  /**
   * 拉取最新未读消息数
   */
  async function refreshUnreadCount() {
    if (!isLoggedIn.value) {
      unreadCount.value = 0
      return
    }
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data?.unreadCount || 0
    } catch (e) {
      // 静默失败
    }
  }

  /**
   * 增加未读数(收到新私信时调用)
   */
  function incrementUnread() {
    unreadCount.value++
  }

  /**
   * 通知其他标签页账号状态变更
   */
  function broadcastAuthChange(action) {
    if (broadcastChannel) {
      broadcastChannel.postMessage({
        action, // 'login' | 'logout' | 'switch'
        sessionId: SESSION_ID,
        token: token.value,
        userInfo: userInfo.value
      })
    }
  }

  // 监听其他标签页的账号变更通知
  if (broadcastChannel) {
    broadcastChannel.onmessage = (event) => {
      const { action, sessionId, token: newToken, userInfo: newUserInfo } = event.data || {}
      // 忽略自己发出的通知
      if (sessionId === SESSION_ID) return

      if (action === 'logout') {
        // 其他标签页登出了,本标签页也同步登出
        token.value = ''
        userInfo.value = null
        unreadCount.value = 0
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        chatWebSocket.disconnect()
      } else if (action === 'login' || action === 'switch') {
        // 其他标签页登录或切换了账号,本标签页同步更新
        const currentUserId = userInfo.value?.id
        const newUserId = newUserInfo?.id
        token.value = newToken || ''
        userInfo.value = newUserInfo || null
        if (newToken) {
          localStorage.setItem('token', newToken)
          localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
        }
        // 如果是切换了账号(不同的用户ID),需要重连WebSocket
        if (currentUserId !== newUserId) {
          chatWebSocket.disconnect()
          connectWebSocket()
        }
      }
    }
  }

  // 监听localStorage的storage事件(兼容不支持BroadcastChannel的环境,以及跨标签页直接修改localStorage)
  if (typeof window !== 'undefined') {
    window.addEventListener('storage', (e) => {
      if (e.key === 'token' || e.key === 'userInfo') {
        const newToken = localStorage.getItem('token') || ''
        const newUserInfoStr = localStorage.getItem('userInfo') || 'null'
        const newUserInfo = JSON.parse(newUserInfoStr)

        const oldUserId = userInfo.value?.id
        const newUserId = newUserInfo?.id

        token.value = isTokenExpired(newToken) ? '' : newToken
        userInfo.value = token.value ? newUserInfo : null

        // 账号变更(登录/登出/切换)时,重连WebSocket
        if (oldUserId !== newUserId) {
          chatWebSocket.disconnect()
          if (token.value) {
            connectWebSocket()
          } else {
            unreadCount.value = 0
          }
        }
      }
    })
  }

  // 登录
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const { token: newToken, userInfo: info } = res.data

    token.value = newToken
    userInfo.value = info

    localStorage.setItem('token', newToken)
    localStorage.setItem('userInfo', JSON.stringify(info))

    // 建立WebSocket连接
    connectWebSocket()
    // 通知其他标签页
    broadcastAuthChange('login')

    return res
  }

  // 注册
  async function register(registerForm) {
    return await registerApi(registerForm)
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    unreadCount.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    chatWebSocket.disconnect()
    // 通知其他标签页
    broadcastAuthChange('logout')
  }

  // 初始化:若已登录,自动连接WebSocket
  if (isLoggedIn.value) {
    // 延迟连接,避免阻塞store初始化
    setTimeout(() => connectWebSocket(), 0)
  }

  return {
    token,
    userInfo,
    unreadCount,
    isLoggedIn,
    isAdmin,
    login,
    register,
    fetchUserInfo,
    logout,
    refreshUnreadCount,
    incrementUnread,
    connectWebSocket
  }
})
