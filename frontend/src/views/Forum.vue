<template>
  <div class="forum-container">
    <div class="forum-header">
      <div class="header-content">
        <h1 class="header-title">📢 论坛中心</h1>
        <p class="header-desc">分享观点，交流心得，共同成长</p>
      </div>
      <div class="header-actions">
        <el-dropdown trigger="click" class="manager-dropdown">
          <span class="el-dropdown-link">
            <el-icon><Setting /></el-icon> 主理人管理
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click.native="goToApply">申请成为主理人</el-dropdown-item>
              <el-dropdown-item @click.native="goToPostManage">帖子管理</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button type="primary" class="publish-btn" @click="showPublishDialog = true">
          <el-icon><ChatDotRound /></el-icon> 发布帖子
        </el-button>
      </div>
    </div>
    
    <div class="main-content">
      <div class="tabs-wrapper">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="最新帖子" name="latest">
            <div class="filter-bar">
              <el-select 
                v-model="selectedCategory" 
                placeholder="全部分类" 
                class="category-select"
                @change="loadPosts"
              >
                <el-option label="全部分类" value="" />
                <el-option label="财报分析" value="财报分析" />
                <el-option label="投资心得" value="投资心得" />
                <el-option label="行业讨论" value="行业讨论" />
                <el-option label="风险提示" value="风险提示" />
                <el-option label="其他" value="其他" />
              </el-select>
            </div>
            <div class="post-list">
              <div v-for="post in posts" :key="post.id" class="post-card">
                <div class="post-card-inner">
                  <div class="post-header">
                    <div class="post-author">
                      <div class="author-name">{{ post.displayName || post.username || '匿名用户' }}</div>
                      <div class="post-time">
                        <el-icon><Clock /></el-icon>
                        {{ formatTime(post.createTime) }}
                      </div>
                    </div>
                  </div>
                  <h3 class="post-title" @click="viewPost(post)">{{ post.title }}</h3>
                  <p class="post-content">{{ post.content }}</p>
                  <div class="post-footer">
                    <el-tag :type="getCategoryType(post.category)" size="small">{{ post.category }}</el-tag>
                    <div class="post-stats">
                      <span class="stat-item" @click.stop="viewPost(post)">
                        <el-icon><View /></el-icon> {{ post.views || 0 }}
                      </span>
                      <span class="stat-item" @click.stop="handleComment(post)">
                        <el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}
                      </span>
                      <span class="stat-item" @click.stop="handleLike(post)">
                        <el-icon><Star :class="{ active: post.liked }" /></el-icon> {{ post.likes || 0 }}
                      </span>
                    </div>
                  </div>
                  <!-- 评论展示区域 -->
                  <div v-if="post.comments && post.comments.length > 0" class="comments-section">
                    <div 
                      v-for="(comment, index) in (post.expandedComments ? post.comments : post.comments.slice(0, 2))" 
                      :key="comment.id" 
                      class="comment-item"
                    >
                      <div class="comment-header">
                        <span class="comment-author">{{ comment.displayName || comment.username || '匿名用户' }}</span>
                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                      </div>
                      <div class="comment-text">{{ comment.content }}</div>
                    </div>
                    <div v-if="post.comments.length > 2" class="more-comments">
                      <span @click="toggleComments(post)" class="more-link">
                        {{ post.expandedComments ? '收起评论' : `查看全部${post.comments.length}条评论` }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="热门帖子" name="hot">
            <div class="filter-bar">
              <el-select 
                v-model="selectedCategory" 
                placeholder="全部分类" 
                class="category-select"
                @change="loadPosts"
              >
                <el-option label="全部分类" value="" />
                <el-option label="财报分析" value="财报分析" />
                <el-option label="投资心得" value="投资心得" />
                <el-option label="行业讨论" value="行业讨论" />
                <el-option label="风险提示" value="风险提示" />
                <el-option label="其他" value="其他" />
              </el-select>
            </div>
            <div class="post-list">
              <div v-for="post in posts" :key="post.id" class="post-card">
                <div class="post-card-inner">
                  <div class="post-header">
                    <div class="post-author">
                      <div class="author-name">{{ post.displayName || post.username || '匿名用户' }}</div>
                      <div class="post-time">
                        <el-icon><Clock /></el-icon>
                        {{ formatTime(post.createTime) }}
                      </div>
                    </div>
                  </div>
                  <h3 class="post-title" @click="viewPost(post)">{{ post.title }}</h3>
                  <p class="post-content">{{ post.content }}</p>
                  <div class="post-footer">
                    <el-tag :type="getCategoryType(post.category)" size="small">{{ post.category }}</el-tag>
                    <div class="post-stats">
                      <span class="stat-item" @click.stop="viewPost(post)">
                        <el-icon><View /></el-icon> {{ post.views || 0 }}
                      </span>
                      <span class="stat-item" @click.stop="handleComment(post)">
                        <el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}
                      </span>
                      <span class="stat-item" @click.stop="handleLike(post)">
                        <el-icon><Star :class="{ active: post.liked }" /></el-icon> {{ post.likes || 0 }}
                      </span>
                    </div>
                  </div>
                  <!-- 评论展示区域 -->
                  <div v-if="post.comments && post.comments.length > 0" class="comments-section">
                    <div 
                      v-for="(comment, index) in (post.expandedComments ? post.comments : post.comments.slice(0, 2))" 
                      :key="comment.id" 
                      class="comment-item"
                    >
                      <div class="comment-header">
                        <span class="comment-author">{{ comment.displayName || comment.username || '匿名用户' }}</span>
                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                      </div>
                      <div class="comment-text">{{ comment.content }}</div>
                    </div>
                    <div v-if="post.comments.length > 2" class="more-comments">
                      <span @click="toggleComments(post)" class="more-link">
                        {{ post.expandedComments ? '收起评论' : `查看全部${post.comments.length}条评论` }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 发布帖子对话框 -->
    <el-dialog v-model="showPublishDialog" title="发布帖子" width="600px">
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="帖子标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="帖子分类">
          <el-select v-model="postForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="财报分析" value="财报分析" />
            <el-option label="投资心得" value="投资心得" />
            <el-option label="行业讨论" value="行业讨论" />
            <el-option label="风险提示" value="风险提示" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="帖子内容">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入帖子内容"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="postForm.isAnonymous">匿名发布</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>

    <!-- 评论对话框 -->
    <el-dialog v-model="showCommentDialog" title="发表评论" width="600px">
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="评论内容">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入评论内容"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="commentForm.isAnonymous">匿名评论</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCommentDialog = false">取消</el-button>
        <el-button type="primary" @click="submitComment">发表评论</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View, ChatDotRound, Star, Clock, Setting, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

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

const activeTab = ref('latest')
const selectedCategory = ref('')
const showPublishDialog = ref(false)
const showCommentDialog = ref(false)
const posts = ref([])
const currentPost = ref(null)

const postForm = reactive({
  title: '',
  category: '',
  content: '',
  isAnonymous: false
})

const commentForm = reactive({
  content: '',
  isAnonymous: false
})

const loadPosts = async () => {
  try {
    const url = activeTab.value === 'latest' 
      ? '/forum/posts/latest' 
      : '/forum/posts/hot'
    const params = selectedCategory.value ? { category: selectedCategory.value } : {}
    const response = await request.get(url, { params })
    if (response.code === 200) {
      posts.value = response.data.map(post => ({
        ...post,
        liked: false,
        expandedComments: false
      }))
      
      // 为每个帖子加载评论
      for (const post of posts.value) {
        await loadComments(post)
      }
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('加载帖子失败')
    console.error(error)
  }
}

const handleTabChange = () => {
  loadPosts()
}

const loadComments = async (post) => {
  try {
    const response = await request.get(`/forum/posts/${post.id}/comments`)
    if (response.code === 200) {
      post.comments = response.data
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

const toggleComments = (post) => {
  post.expandedComments = !post.expandedComments
}

const viewPost = async (post) => {
  try {
    const response = await request.post(`/forum/posts/${post.id}/view`)
    if (response.code === 200) {
      post.views = (post.views || 0) + 1
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    console.error(error)
  }
}

const handleComment = (post) => {
  currentPost.value = post
  commentForm.content = ''
  showCommentDialog.value = true
}

const handleLike = async (post) => {
  try {
    const response = await request.post(`/forum/posts/${post.id}/like`)
    if (response.code === 200) {
      if (post.liked) {
        post.likes = (post.likes || 0) - 1
        post.liked = false
        ElMessage.info('已取消点赞')
      } else {
        post.likes = (post.likes || 0) + 1
        post.liked = true
        ElMessage.success('点赞成功')
      }
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('点赞失败')
    console.error(error)
  }
}

const submitComment = async () => {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  try {
    const response = await request.post(`/forum/posts/${currentPost.value.id}/comments`, {
      content: commentForm.content,
      isAnonymous: commentForm.isAnonymous ? 1 : 0
    })
    
    if (response.code === 200) {
      currentPost.value.commentCount = (currentPost.value.commentCount || 0) + 1
      showCommentDialog.value = false
      commentForm.content = ''
      commentForm.isAnonymous = false
      ElMessage.success('评论成功')
      
      // 刷新评论列表
      await loadComments(currentPost.value)
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('评论失败')
    console.error(error)
  }
}

const handlePublish = async () => {
  if (!postForm.title || !postForm.category || !postForm.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const response = await request.post('/forum/posts', {
      title: postForm.title,
      category: postForm.category,
      content: postForm.content,
      isAnonymous: postForm.isAnonymous ? 1 : 0
    })
    
    if (response.code === 200) {
      ElMessage.success('发布成功')
      showPublishDialog.value = false
      
      postForm.title = ''
      postForm.category = ''
      postForm.content = ''
      postForm.isAnonymous = false
      
      loadPosts()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('发布失败')
    console.error(error)
  }
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goToApply = () => {
  window.location.href = '/bar-manager-apply'
}

const goToPostManage = () => {
  window.location.href = '/bar-manager-post-manage'
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.forum-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 100px);
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 28px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-content {
  color: white;
}

.header-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
}

.header-desc {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.manager-dropdown {
  color: white;
  font-size: 14px;
  font-weight: 500;
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.manager-dropdown:hover {
  background: rgba(255, 255, 255, 0.3);
}

.manager-dropdown .el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 6px;
}

.publish-btn {
  background: white;
  color: #667eea;
  border: none;
  font-weight: 600;
  padding: 12px 28px;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  background: #f0f2ff;
  color: #667eea;
}

.main-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.tabs-wrapper {
  padding: 24px;
}

.filter-bar {
  padding: 0 0 20px 0;
  border-bottom: 2px solid #f0f2f5;
  margin-bottom: 24px;
}

.category-select {
  width: 180px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 12px;
  transition: all 0.3s;
  border: 1px solid #f0f2f5;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);
  border-color: #e0e6ed;
}

.post-card-inner {
  padding: 24px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.post-author {
  margin-left: 0;
}

.author-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 15px;
  margin-bottom: 4px;
}

.post-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-title {
  margin: 0 0 12px 0;
  color: #1a1a1a;
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  line-height: 1.4;
}

.post-title:hover {
  color: #667eea;
}

.post-content {
  color: #606266;
  line-height: 1.7;
  margin-bottom: 18px;
  font-size: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.post-stats {
  display: flex;
  gap: 24px;
  color: #909399;
}

.post-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s;
  font-size: 14px;
}

.post-stats .stat-item:hover {
  background: linear-gradient(135deg, #f0f2ff 0%, #e8ecff 100%);
  color: #667eea;
}

.post-stats .stat-item .active {
  color: #f56c6c;
}

/* 评论区域样式 */
.comments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px dashed #f0f2f5;
}

.comment-item {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 12px;
  border-left: 4px solid #667eea;
  transition: all 0.3s;
}

.comment-item:hover {
  background: linear-gradient(135deg, #f0f4ff 0%, #e8edff 100%);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  color: #667eea;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.comment-text {
  color: #4a5568;
  font-size: 14px;
  line-height: 1.6;
  padding: 4px 0;
}

.more-comments {
  text-align: center;
  margin-top: 12px;
}

.more-link {
  font-size: 14px;
  color: #667eea;
  cursor: pointer;
  padding: 8px 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f0f2ff 0%, #e8ecff 100%);
  transition: all 0.3s;
  font-weight: 500;
  display: inline-block;
}

.more-link:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}
</style>
