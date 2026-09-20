import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UsersManagement.vue')
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/ReportsManagement.vue')
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('@/views/admin/PostsManagement.vue')
      },
      {
        path: 'bar-manager-review',
        name: 'BarManagerReview',
        component: () => import('@/views/BarManagerReview.vue')
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('@/views/AnnouncementManagement.vue')
      }
    ]
  },
  {
    path: '/report-management',
    name: 'ReportManagement',
    component: () => import('@/views/ReportManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/Analysis.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'risk',
        name: 'Risk',
        component: () => import('@/views/RiskCenter.vue')
      },
      {
        path: 'forum',
        name: 'Forum',
        component: () => import('@/views/Forum.vue')
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/UserCenter.vue')
      },
      {
        path: 'bar-manager-apply',
        name: 'BarManagerApply',
        component: () => import('@/views/BarManagerApplication.vue')
      },
      {
        path: 'bar-manager-post-manage',
        name: 'BarManagerPostManage',
        component: () => import('@/views/BarManagerPostManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated
  const user = userStore.userInfo
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresAdmin && (!user || user.role !== 'admin')) {
    ElMessage.error('需要管理员权限')
    next('/home')
  } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    next('/home')
  } else {
    next()
  }
})

export default router