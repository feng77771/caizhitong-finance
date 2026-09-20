<template>
  <div class="main-layout">
    <el-container>
      <el-header class="custom-header">
        <div class="header-content">
          <div class="logo">
            <img src="/logo.png" alt="财报智析" class="logo-image" />
            <span class="logo-text">财报智析</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            router
            background-color="transparent"
            text-color="#a0a0b0"
            active-text-color="#02e5b4"
            class="nav-menu"
          >
            <el-menu-item index="/home">
              <template #default>
                <span class="menu-text">首页</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/analysis">
              <template #default>
                <span class="menu-text">财报分析</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/profile">
              <template #default>
                <span class="menu-text">公司画像</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/risk">
              <template #default>
                <span class="menu-text">风险分析</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/forum">
              <template #default>
                <span class="menu-text">论坛中心</span>
              </template>
            </el-menu-item>
            <el-menu-item index="/user">
              <template #default>
                <span class="menu-text">个人中心</span>
              </template>
            </el-menu-item>
          </el-menu>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-icon class="user-icon"><User /></el-icon>
                <span>{{ userStore.userInfo.username }}</span>
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu class="dropdown-menu">
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background-color: #0a0a0a;
}

.custom-header {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #0a0a0a 100%);
  padding: 0;
  border-bottom: 1px solid rgba(2, 229, 180, 0.3);
  height: 80px !important;
  box-shadow: 0 4px 20px rgba(2, 229, 180, 0.15),
              0 0 40px rgba(2, 229, 180, 0.08);
  position: relative;
}

.custom-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(2, 229, 180, 0.5), transparent);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 30px;
}

.logo {
  margin-right: 50px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-image {
  height: 50px;
  width: auto;
  filter: drop-shadow(0 0 10px rgba(2, 229, 180, 0.5));
  transition: transform 0.3s ease, filter 0.3s ease;
}

.logo:hover .logo-image {
  transform: scale(1.05);
  filter: drop-shadow(0 0 20px rgba(2, 229, 180, 0.8));
}

.logo-text {
  font-size: 28px;
  font-weight: 700;
  color: #02e5b4;
  text-shadow: 0 0 20px rgba(2, 229, 180, 0.6);
  letter-spacing: 2px;
}

.nav-menu {
  flex: 1;
  border: none;
}

:deep(.el-menu--horizontal .el-menu-item:not(:last-child)::after) {
  content: none;
}

:deep(.el-menu-item) {
  font-size: 28px !important;
  height: 80px;
  line-height: 80px;
  margin: 0 10px;
  border-radius: 8px;
  position: relative;
  transition: all 0.3s ease;
  background: transparent;
}

:deep(.el-menu-item:hover) {
  background: rgba(2, 229, 180, 0.1);
  transform: translateY(-2px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.15) 0%, rgba(2, 229, 180, 0.05) 100%);
  box-shadow: 0 4px 15px rgba(2, 229, 180, 0.2);
}

:deep(.el-menu-item.is-active)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #02e5b4, transparent);
  border-radius: 2px;
}

.menu-text {
  font-size: 28px !important;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover .menu-text) {
  color: #02e5b4;
  text-shadow: 0 0 10px rgba(2, 229, 180, 0.8);
}

:deep(.el-menu-item.is-active .menu-text) {
  color: #02e5b4;
  text-shadow: 0 0 15px rgba(2, 229, 180, 0.8);
}

.user-info {
  color: #02e5b4;
  margin-left: 20px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #02e5b4;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 26px;
  padding: 10px 20px;
  border-radius: 8px;
  background: rgba(2, 229, 180, 0.1);
  border: 1px solid rgba(2, 229, 180, 0.2);
  transition: all 0.3s ease;
}

.el-dropdown-link:hover {
  background: rgba(2, 229, 180, 0.15);
  box-shadow: 0 0 20px rgba(2, 229, 180, 0.3);
}

.user-icon {
  font-size: 28px;
}

:deep(.dropdown-menu) {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(2, 229, 180, 0.3);
  border-radius: 10px;
}

:deep(.dropdown-menu .el-dropdown-menu__item) {
  font-size: 24px;
  color: #a0a0b0;
  padding: 15px 25px;
  transition: all 0.3s ease;
}

:deep(.dropdown-menu .el-dropdown-menu__item:hover) {
  background: rgba(2, 229, 180, 0.2);
  color: #02e5b4;
}

.el-main {
  padding: 20px;
  background-color: #0a0a0a;
  min-height: calc(100vh - 80px);
}
</style>