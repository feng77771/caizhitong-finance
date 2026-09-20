<template>
  <div class="user-center-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="menu-card">
          <div class="user-info-header">
            <el-avatar :size="80" :src="userAvatar" />
            <h3>{{ userStore.userInfo.username }}</h3>
            <p>{{ userStore.userInfo.email }}</p>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="vertical"
            @select="handleMenuSelect"
          >
            <el-menu-item index="basic">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>

            <el-menu-item index="favorites">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="content-card">
          <!-- 基本信息 -->
          <div v-if="activeMenu === 'basic'">
            <h3>基本信息</h3>
            <el-form :model="userForm" label-width="100px" style="margin-top: 20px">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userForm.email" disabled />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="userForm.phone" disabled />
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 安全设置 -->
          <div v-if="activeMenu === 'security'">
            <h3>安全设置</h3>
            <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" style="margin-top: 20px">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdatePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 我的收藏 -->
          <div v-if="activeMenu === 'favorites'">
            <h3>我的收藏</h3>
            <div v-if="likedPosts.length === 0" class="empty-state">
              <el-empty description="暂无收藏的帖子" />
            </div>
            <div v-else class="liked-posts-list">
              <div v-for="post in likedPosts" :key="post.id" class="post-card">
                <div class="post-header">
                  <span class="post-category">{{ post.category }}</span>
                  <span class="post-time">{{ post.createTime }}</span>
                </div>
                <h4 class="post-title">{{ post.title }}</h4>
                <p class="post-content">{{ truncateContent(post.content) }}</p>
                <div class="post-footer">
                  <span class="post-author">{{ post.displayName || post.username }}</span>
                  <span class="post-stats">浏览 {{ post.views }} | 点赞 {{ post.likes }}</span>
                  <el-button type="text" @click="handleRemoveLike(post.id)" class="unlike-btn">取消收藏</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getLikedPosts, likePost } from '@/api/forum'
import request from '@/utils/request'

const userStore = useUserStore()
const activeMenu = ref('basic')
const passwordFormRef = ref(null)
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

const userForm = reactive({
  username: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const likedPosts = ref([])

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 100 ? content.substring(0, 100) + '...' : content
}

const loadLikedPosts = async () => {
  try {
    const res = await getLikedPosts()
    // request 拦截器返回的是 ApiResponse 对象，code=200 时直接返回
    // 所以 res 的结构是 { code, message, data: [...] }
    if (res && res.code === 200 && Array.isArray(res.data)) {
      likedPosts.value = res.data
    } else {
      likedPosts.value = []
    }
  } catch (error) {
    likedPosts.value = []
    console.error('获取收藏帖子失败:', error)
  }
}

const handleRemoveLike = async (postId) => {
  try {
    await likePost(postId)
    likedPosts.value = likedPosts.value.filter(post => post.id !== postId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    ElMessage.error('取消收藏失败')
  }
}

onMounted(() => {
  userForm.username = userStore.userInfo.username
  userForm.email = userStore.userInfo.email
  userForm.phone = userStore.userInfo.phone
})

const handleMenuSelect = (key) => {
  activeMenu.value = key
  if (key === 'favorites') {
    loadLikedPosts()
  }
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    const valid = await passwordFormRef.value.validate()
    if (valid) {
      const response = await request.post('/auth/change-password', {
        userId: userStore.userInfo.userId,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      if (response.code === 200) {
        ElMessage.success('密码修改成功')
        // 重置表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } else {
        ElMessage.error(response.message || '密码修改失败')
      }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '密码修改失败')
  }
}

</script>

<style scoped>
.user-center-container {
  padding: 20px;
}

.menu-card {
  height: 100%;
}

.user-info-header {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.user-info-header h3 {
  margin: 15px 0 5px 0;
  color: #333;
}

.user-info-header p {
  color: #999;
  font-size: 14px;
}

.content-card {
  min-height: 600px;
}

.content-card h3 {
  color: #333;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.liked-posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.post-category {
  background: #e8f4fd;
  color: #409EFF;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.post-time {
  color: #999;
  font-size: 13px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.post-content {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.post-author {
  color: #999;
  font-size: 13px;
}

.post-stats {
  color: #999;
  font-size: 13px;
}

.unlike-btn {
  color: #F56C6C;
  font-size: 13px;
}

.unlike-btn:hover {
  color: #E6A23C;
}
</style>