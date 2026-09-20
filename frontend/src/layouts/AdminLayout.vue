<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon :size="28"><Setting /></el-icon>
          <span>管理控制台</span>
        </div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        background-color="#1a1a2e"
        text-color="#a0a0b0"
        active-text-color="#02e5b4"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/reports">
          <el-icon><Document /></el-icon>
          <span>财报管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/posts">
          <el-icon><ChatDotRound /></el-icon>
          <span>发帖管理</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/bar-manager-review">
          <el-icon><UserFilled /></el-icon>
          <span>主理人审核</span>
        </el-menu-item>
        
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <el-button type="danger" @click="handleLogout" plain>
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>
    
    <div class="admin-main">
      <div class="admin-header">
        <div class="header-left">
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <div class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ username }}</span>
          </div>
        </div>
      </div>
      
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Setting, User, Document, ChatDotRound, SwitchButton, UserFilled, Bell } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const username = computed(() => userStore.user?.username || '管理员')

const pageTitle = computed(() => {
  const titleMap = {
    '/admin/users': '用户管理',
    '/admin/reports': '财报管理',
    '/admin/posts': '发帖管理',
    '/admin/bar-manager-review': '主理人审核',
    '/admin/announcements': '公告管理'
  }
  return titleMap[route.path] || '管理控制台'
})

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #0a0a0f;
}

.admin-sidebar {
  width: 240px;
  background: #1a1a2e;
  border-right: 1px solid rgba(2, 229, 180, 0.1);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(2, 229, 180, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #02e5b4;
  font-size: 20px;
  font-weight: 600;
}

.sidebar-menu {
  flex: 1;
  border: none;
  padding: 20px 0;
}

.sidebar-menu .el-menu-item {
  margin: 0 12px;
  border-radius: 8px;
  padding: 0 16px;
  height: 60px;
  line-height: 60px;
  margin-bottom: 8px;
  transition: all 0.3s;
  font-size: 20px;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(2, 229, 180, 0.1);
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.2) 0%, rgba(2, 229, 180, 0.1) 100%);
  border: 1px solid rgba(2, 229, 180, 0.3);
}

.sidebar-menu .el-menu-item .el-icon {
  margin-right: 10px;
}

.sidebar-menu .el-menu-item span {
  font-size: 20px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(2, 229, 180, 0.1);
}

.sidebar-footer .el-button {
  width: 100%;
  justify-content: center;
}

.admin-main {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: rgba(26, 26, 46, 0.95);
  padding: 20px 30px;
  border-bottom: 1px solid rgba(2, 229, 180, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 99;
  backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #a0a0b0;
  font-size: 20px;
  padding: 8px 16px;
  background: rgba(2, 229, 180, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(2, 229, 180, 0.2);
}

.admin-content {
  flex: 1;
  padding: 30px;
  background: #0a0a0f;
}
</style>