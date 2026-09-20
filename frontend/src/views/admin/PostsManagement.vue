<template>
  <div class="posts-management">
    <div class="page-header">
      <h3>发帖管理</h3>
      <div class="header-right">
        <div class="user-info">
          <span class="user-label">管理员</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="handleBatchDelete" :disabled="selectedPosts.length === 0">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索帖子标题或内容"
        prefix-icon="Search"
        style="width: 300px"
        @input="handleSearch"
      />
      <el-select v-model="filterCategory" placeholder="选择分类" style="width: 150px; margin-left: 10px" @change="handleFilter">
        <el-option label="全部分类" value="" />
        <el-option label="财报分析" value="财报分析" />
        <el-option label="投资心得" value="投资心得" />
        <el-option label="行业讨论" value="行业讨论" />
        <el-option label="风险提示" value="风险提示" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="选择状态" style="width: 150px; margin-left: 10px" @change="handleFilter">
        <el-option label="全部状态" value="" />
        <el-option label="正常" value="normal" />
        <el-option label="已删除" value="deleted" />
        <el-option label="已隐藏" value="hidden" />
      </el-select>
    </div>
    
    <el-table 
      :data="filteredPosts" 
      style="width: 100%" 
      stripe 
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" min-width="55" />
      <el-table-column prop="id" label="ID" min-width="80" />
      <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" min-width="120" />
      <el-table-column prop="category" label="分类" min-width="120">
        <template #default="{ row }">
          <el-tag :type="getCategoryType(row.category)">
            {{ row.category }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="views" label="浏览" min-width="80" />
      <el-table-column prop="likes" label="点赞" min-width="80" />
      <el-table-column prop="commentCount" label="评论" min-width="80" />
      <el-table-column prop="createTime" label="发布时间" min-width="180" />
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="350">
        <template #default="{ row }">
          <el-button size="small" @click="handleView(row)">查看</el-button>
          <el-button size="small" type="warning" @click="handleHide(row)">
            {{ row.status === 'hidden' ? '显示' : '隐藏' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 查看帖子对话框 -->
    <el-dialog v-model="showViewDialog" title="帖子详情" width="900px">
      <div v-if="currentPost" class="post-detail">
        <h3 class="post-title">{{ currentPost.title }}</h3>
        <div class="post-meta">
          <span class="author">作者: {{ currentPost.author }}</span>
          <span class="category">分类: {{ currentPost.category }}</span>
          <span class="time">发布时间: {{ currentPost.createTime }}</span>
        </div>
        <div class="post-content">{{ currentPost.content }}</div>
        <div class="post-stats">
          <span>浏览: {{ currentPost.views }}</span>
          <span>点赞: {{ currentPost.likes }}</span>
          <span>评论: {{ currentPost.commentCount }}</span>
        </div>
        
        <!-- 评论列表 -->
        <div class="comments-section">
          <h4 class="comments-title">评论列表</h4>
          <div v-if="comments.length > 0" class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-author">{{ comment.displayName || comment.username }}</span>
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
          <div v-else class="no-comments">
            <p>暂无评论</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const posts = ref([])
const searchKeyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')
const selectedPosts = ref([])
const showViewDialog = ref(false)
const currentPost = ref(null)
const comments = ref([])

const filteredPosts = computed(() => {
  let result = posts.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(post => 
      post.title.toLowerCase().includes(keyword) || 
      post.content.toLowerCase().includes(keyword)
    )
  }
  
  if (filterCategory.value) {
    result = result.filter(post => post.category === filterCategory.value)
  }
  
  if (filterStatus.value) {
    result = result.filter(post => post.status === filterStatus.value)
  }
  
  return result
})

const getCategoryType = (category) => {
  const typeMap = {
    '财报分析': 'success',
    '投资心得': 'primary',
    '行业讨论': 'warning',
    '风险提示': 'danger',
    '其他': 'info'
  }
  return typeMap[category] || 'info'
}

const getStatusType = (status) => {
  const typeMap = {
    'normal': 'success',
    'deleted': 'danger',
    'hidden': 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'normal': '正常',
    'deleted': '已删除',
    'hidden': '已隐藏'
  }
  return textMap[status] || '未知'
}

const loadPosts = async () => {
  try {
    const response = await request.get('/admin/posts')
    posts.value = response.data
  } catch (error) {
    ElMessage.error('加载帖子列表失败')
  }
}

const handleSearch = () => {
  // 搜索逻辑由 computed 自动处理
}

const handleFilter = () => {
  // 筛选逻辑由 computed 自动处理
}

const handleSelectionChange = (selection) => {
  selectedPosts.value = selection
}

const handleView = async (post) => {
  currentPost.value = post
  showViewDialog.value = true
  // 加载评论
  await loadComments(post.id)
}

const loadComments = async (postId) => {
  try {
    const response = await request.get(`/forum/posts/${postId}/comments`, {
      params: { limit: 20 }
    })
    comments.value = response.data
  } catch (error) {
    comments.value = []
  }
}

const handleHide = async (post) => {
  try {
    const newStatus = post.status === 'hidden' ? 'normal' : 'hidden'
    await request.put(`/admin/posts/${post.id}/status`, { status: newStatus })
    ElMessage.success(newStatus === 'hidden' ? '隐藏成功' : '显示成功')
    loadPosts()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/posts/${post.id}`)
    ElMessage.success('删除帖子成功')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除帖子失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedPosts.value.length} 个帖子吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedPosts.value.map(post => post.id)
    await request.post('/admin/posts/batch-delete', { ids })
    ElMessage.success('批量删除成功')
    selectedPosts.value = []
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.posts-management {
  background: rgba(26, 26, 46, 0.95);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(2, 229, 180, 0.1);
  width: calc(100% - 20px);
  max-width: calc(100% - 20px);
  margin: 0 auto;
  box-sizing: border-box;
  font-size: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(2, 229, 180, 0.1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  min-width: 120px;
  background: rgba(2, 229, 180, 0.1);
  border-radius: 8px;
}

.user-label {
  color: #02e5b4;
  font-size: 18px;
  font-weight: 600;
}

.page-header h3 {
  color: #02e5b4;
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  margin-bottom: 24px;
  display: flex;
  gap: 10px;
}

.search-bar :deep(.el-input__inner) {
  font-size: 20px;
  height: 48px;
  line-height: 48px;
}

.search-bar :deep(.el-select__input) {
  font-size: 20px;
}

:deep(.el-table) {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(2, 229, 180, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 60px rgba(2, 229, 180, 0.08);
  border-radius: 12px;
  overflow: hidden;
  width: 100% !important;
  font-size: 20px;
}

:deep(.el-table th) {
  background: rgba(26, 26, 46, 0.95);
  color: #02e5b4;
  font-weight: 600;
  font-size: 20px;
  height: 52px;
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(2, 229, 180, 0.05);
  color: #a0a0b0;
  font-size: 20px;
  height: 50px;
}

:deep(.el-table tr:hover > td) {
  background: rgba(26, 26, 46, 0.95);
}

:deep(.el-tag) {
  font-size: 18px;
  padding: 6px 14px;
}

:deep(.el-button) {
  font-size: 18px;
  padding: 10px 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

:deep(.el-table td .el-button) {
  display: inline-flex;
  margin-right: 8px;
  margin-bottom: 0;
}

:deep(.el-dialog) {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(2, 229, 180, 0.2);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(2, 229, 180, 0.1);
}

:deep(.el-dialog__title) {
  color: #02e5b4;
  font-size: 22px;
}

.post-detail {
  color: #a0a0b0;
}

.post-title {
  color: #02e5b4;
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
}

.post-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(2, 229, 180, 0.1);
  font-size: 20px;
}

.post-content {
  line-height: 1.8;
  margin-bottom: 20px;
  white-space: pre-wrap;
  font-size: 20px;
}

.post-stats {
  display: flex;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(2, 229, 180, 0.1);
  font-size: 20px;
}

.comments-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(2, 229, 180, 0.1);
}

.comments-title {
  color: #02e5b4;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
}

.comments-list {
  max-height: 400px;
  overflow-y: auto;
}

.comment-item {
  background: rgba(2, 229, 180, 0.05);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.comment-author {
  color: #02e5b4;
  font-weight: 600;
  font-size: 18px;
}

.comment-time {
  color: #666;
  font-size: 16px;
}

.comment-content {
  color: #a0a0b0;
  font-size: 18px;
  line-height: 1.6;
}

.no-comments {
  text-align: center;
  padding: 30px;
  color: #666;
}

.no-comments p {
  margin: 0;
  font-size: 18px;
}
</style>