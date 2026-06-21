import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  // 用户端路由
  {
    path: '/',
    component: () => import('@/views/user/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'item/:id',
        name: 'ItemDetail',
        component: () => import('@/views/user/ItemDetail.vue'),
        meta: { title: '物品详情' }
      },
      {
        path: 'publish',
        name: 'Publish',
        component: () => import('@/views/user/Publish.vue'),
        meta: { title: '发布物品', requiresAuth: true }
      },
      {
        path: 'my-items',
        name: 'MyItems',
        component: () => import('@/views/user/MyItems.vue'),
        meta: { title: '我的发布', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'category/:id',
        name: 'CategoryItems',
        component: () => import('@/views/user/CategoryItems.vue'),
        meta: { title: '分类浏览' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/user/Search.vue'),
        meta: { title: '搜索结果' }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/user/Messages.vue'),
        meta: { title: '我的消息', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '注册', guest: true }
  },

  // 管理端路由
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'audit',
        name: 'AuditList',
        component: () => import('@/views/admin/AuditList.vue'),
        meta: { title: '物品审核' }
      },
      {
        path: 'items',
        name: 'AdminItems',
        component: () => import('@/views/admin/ItemList.vue'),
        meta: { title: '物品管理' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/admin/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'comments',
        name: 'CommentList',
        component: () => import('@/views/admin/CommentList.vue'),
        meta: { title: '留言管理' }
      },
      {
        path: 'categories',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园二手交易平台` : '校园二手交易平台'

  // 需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }

    // 需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/')
      return
    }
  }

  // 已登录用户不能访问登录/注册页
  if (to.meta.guest && userStore.isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
