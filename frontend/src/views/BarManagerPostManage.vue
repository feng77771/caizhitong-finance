<template>
  <div class="bar-manager-post-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>贴吧主理人 - 帖子管理</span>
        </div>
      </template>

      <div class="category-select">
        <span>选择管理的贴吧：</span>
        <el-select v-model="selectedCategory" placeholder="请选择贴吧" style="width: 200px; margin-left: 10px" @change="loadPosts">
          <el-option v-for="cat in managedCategories" :key="cat.category" :label="cat.category" :value="cat.category" />
        </el-select>
      </div>

      <el-table v-if="selectedCategory" :data="posts" style="margin-top: 20px">
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="username" label="作者" width="100" />
        <el-table-column prop="views" label="浏览量" width="80" />
        <el-table-column prop="likes" label="点赞数" width="80" />
        <el-table-column prop="commentCount" label="评论数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' || row.status === 'visible' ? 'success' : 'warning'">
              {{ row.status === 'normal' || row.status === 'visible' ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 'normal' || row.status === 'visible'" type="warning" size="small" @click="updateStatus(row.id, 'hidden')">隐藏</el-button>
            <el-button v-else type="success" size="small" @click="updateStatus(row.id, 'normal')">显示</el-button>
            <el-button type="danger" size="small" @click="deletePost(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!selectedCategory" class="empty-hint">
        <el-icon size="48" color="#ccc">
          <Document />
        </el-icon>
        <p>请先选择要管理的贴吧</p>
      </div>

      <div v-if="selectedCategory && posts.length === 0" class="empty-hint">
        <el-icon size="48" color="#ccc">
          <Document />
        </el-icon>
        <p>该贴吧暂无帖子</p>
      </div>
    </el-card>

    <el-dialog
      v-model="editDialogVisible"
      title="编辑帖子"
      width="600px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入帖子内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="loading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import request from '@/utils/request'

const managedCategories = ref([])
const selectedCategory = ref('')
const posts = ref([])
const loading = ref(false)

const editDialogVisible = ref(false)
const editForm = reactive({
  title: '',
  content: ''
})
let currentPostId = null

const loadManagedCategories = async () => {
  try {
    const response = await request.get('/bar-manager/my-managed-categories')
    managedCategories.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('获取管理的贴吧失败')
  }
}

const loadPosts = async () => {
  if (!selectedCategory.value) return
  
  try {
    loading.value = true
    const response = await request.get(`/bar-manager/posts/${selectedCategory.value}`)
    posts.value = response.data
  } catch (error) {
    console.error(error)
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

const showEditDialog = (row) => {
  currentPostId = row.id
  editForm.title = row.title
  editForm.content = row.content
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!currentPostId) return
  
  try {
    loading.value = true
    await request.put('/bar-manager/post/' + currentPostId, {
      title: editForm.title,
      content: editForm.content
    })
    
    ElMessage.success('编辑成功')
    editDialogVisible.value = false
    loadPosts()
  } catch (error) {
    console.error(error)
    ElMessage.error('编辑失败')
  } finally {
    loading.value = false
  }
}

const updateStatus = async (postId, status) => {
  try {
    await request.put('/bar-manager/post/' + postId + '/status', { status })
    ElMessage.success('状态更新成功')
    loadPosts()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const deletePost = async (postId) => {
  if (!confirm('确定要删除该帖子吗？删除后将无法恢复。')) return
  
  try {
    await request.delete('/bar-manager/post/' + postId)
    ElMessage.success('删除成功')
    loadPosts()
  } catch (error) {
    console.error(error)
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadManagedCategories()
})
</script>

<style scoped>
.bar-manager-post-manage {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.category-select {
  margin-bottom: 20px;
  font-size: 14px;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #999;
}

.empty-hint p {
  margin-top: 10px;
}
</style>