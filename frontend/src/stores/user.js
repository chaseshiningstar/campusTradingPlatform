import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { getUserInfo } from '@/api/user'

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

export const useUserStore = defineStore('user', () => {
  // 初始化时检查token是否过期,过期则清除
  const storedToken = localStorage.getItem('token')
  if (storedToken && isTokenExpired(storedToken)) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const token = ref(isTokenExpired(storedToken) ? '' : (storedToken || ''))
  const userInfo = ref(token.value ? JSON.parse(localStorage.getItem('userInfo') || 'null') : null)

  const isLoggedIn = computed(() => {
    return !!token.value && !isTokenExpired(token.value)
  })
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  // 登录
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const { token: newToken, userInfo: info } = res.data

    token.value = newToken
    userInfo.value = info

    localStorage.setItem('token', newToken)
    localStorage.setItem('userInfo', JSON.stringify(info))

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
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
