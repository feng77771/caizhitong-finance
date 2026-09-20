<template>
  <div class="bar-manager-review">
    <el-card>
      <template #default>
        <div v-if="loadingData" class="loading-container">
          <el-spinner size="large" />
          <span class="loading-text">加载中...</span>
        </div>
        <template v-else>
      
      <div class="applications-table-container">
        <table class="applications-table">
          <thead>
            <tr>
              <th style="width: 120px">申请人</th>
              <th style="width: 120px">申请贴吧</th>
              <th>申请理由</th>
              <th>相关经验</th>
              <th style="width: 180px">申请时间</th>
              <th style="width: 200px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pendingApplications" :key="row.id">
              <td>{{ row.username }}</td>
              <td>{{ row.category }}</td>
              <td class="text-overflow">{{ row.reason }}</td>
              <td class="text-overflow">{{ row.experience }}</td>
              <td>{{ row.createTime }}</td>
              <td>
                <button class="btn-success" @click="showReviewDialog(row, 'approved')">通过</button>
                <button class="btn-danger" @click="showReviewDialog(row, 'rejected')">拒绝</button>
              </td>
            </tr>
            <tr v-if="pendingApplications.length === 0">
              <td colspan="6" class="no-data">暂无待审核申请</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <el-divider />
      
      <div>
        <h3>所有主理人列表</h3>
        <div class="managers-table-container">
          <table class="managers-table">
          <thead>
            <tr>
              <th>主理人</th>
              <th>管理贴吧</th>
              <th>开始时间</th>
              <th style="width: 100px">状态</th>
              <th style="width: 120px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in allManagers" :key="row.id">
              <td>{{ row.username }}</td>
              <td>{{ row.category }}</td>
              <td>{{ row.startTime }}</td>
              <td><span class="status-tag">在职</span></td>
              <td>
                <button class="btn-danger btn-delete" @click="removeManager(row.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
        </template>
      </template>
    </el-card>
    
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewAction === 'approved' ? '审核通过' : '审核拒绝'"
      width="500px"
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请填写审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="loading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const pendingApplications = ref([])
const allManagers = ref([])
const reviewDialogVisible = ref(false)
const loading = ref(false)
const currentApplication = ref(null)
const reviewAction = ref('')
const loadingData = ref(true)

const reviewForm = reactive({
  reviewComment: ''
})

const loadPendingApplications = async () => {
  try {
    const response = await request.get('/bar-manager/pending-applications')
    pendingApplications.value = response.data
  } catch (error) {
    console.error(error)
  }
}

const loadAllManagers = async () => {
  try {
    const response = await request.get('/bar-manager/all')
    allManagers.value = response.data
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loadingData.value = true
  await Promise.all([loadPendingApplications(), loadAllManagers()])
  loadingData.value = false
}

const showReviewDialog = (application, action) => {
  currentApplication.value = application
  reviewAction.value = action
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  try {
    loading.value = true
    
    const response = await request.post('/bar-manager/review', {
      applicationId: currentApplication.value.id,
      status: reviewAction.value,
      reviewComment: reviewForm.reviewComment
    })
    
    ElMessage.success(response.message)
    reviewDialogVisible.value = false
    loadPendingApplications()
    loadAllManagers()
  } catch (error) {
    ElMessage.error('审核失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const removeManager = async (managerId) => {
  if (!confirm('确定要删除该主理人吗？')) return
  
  try {
    await request.delete('/bar-manager/' + managerId)
    ElMessage.success('删除成功')
    loadAllManagers()
  } catch (error) {
    ElMessage.error('删除失败')
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.bar-manager-review {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  gap: 16px;
}

.loading-text {
  color: #b8c4d4;
  font-size: 18px;
}

.el-card {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.el-card__body {
  padding: 24px;
}

.applications-table-container,
.managers-table-container {
  margin-bottom: 20px;
  width: 100%;
  min-height: 200px;
}

.applications-table,
.managers-table {
  width: 100%;
  border-collapse: collapse;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  table-layout: fixed;
  min-height: 200px;
}

.applications-table thead,
.managers-table thead {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
}

.applications-table th,
.managers-table th {
  color: #ffffff;
  font-weight: 600;
  font-size: 16px;
  padding: 16px 12px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.applications-table td,
.managers-table td {
  color: #e0e0e0;
  font-size: 15px;
  padding: 14px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  vertical-align: middle;
}

.applications-table tbody tr:hover td,
.managers-table tbody tr:hover td {
  background: rgba(102, 126, 234, 0.1);
  transition: none;
}

.text-overflow {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #666;
}

.btn-success,
.btn-danger {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  margin-right: 8px;
}

.btn-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.25) 0%, rgba(52, 211, 153, 0.15) 100%);
  border: 1px solid rgba(16, 185, 129, 0.5);
  border-radius: 20px;
  color: #34d399;
  font-size: 14px;
}

.el-divider {
  margin: 24px 0;
  background: #667eea;
  height: 1px;
  border: none;
}

h3 {
  color: #ffffff;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.5);
  display: inline-block;
}

.el-dialog {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(20px);
}

.el-dialog__header {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px 16px 0 0;
}

.el-dialog__title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
}

.el-dialog__body {
  color: #e0e0e0;
}

.el-input__inner {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  color: #ffffff;
}

.el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.el-textarea__inner {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  color: #ffffff;
}

.el-textarea__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.el-form-item__label {
  color: #b8c4d4;
}

.el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}
</style>
