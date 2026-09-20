<template>
  <div class="bar-manager-application">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>贴吧主理人申请</span>
        </div>
      </template>
      
      <el-form :model="applicationForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="申请贴吧" prop="category">
          <el-select v-model="applicationForm.category" placeholder="请选择要申请的贴吧" style="width: 100%">
            <el-option label="财报分析" value="财报分析" />
            <el-option label="投资心得" value="投资心得" />
            <el-option label="行业讨论" value="行业讨论" />
            <el-option label="风险提示" value="风险提示" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="applicationForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明您申请成为该贴吧主理人的理由"
          />
        </el-form-item>
        
        <el-form-item label="相关经验" prop="experience">
          <el-input
            v-model="applicationForm.experience"
            type="textarea"
            :rows="4"
            placeholder="请介绍您在相关领域的经验或背景（选填）"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitApplication" :loading="loading">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <div>
        <h3>我的申请记录</h3>
        <el-table :data="myApplications" style="margin-top: 20px">
          <el-table-column prop="category" label="申请贴吧" width="150" />
          <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reviewComment" label="审核意见" show-overflow-tooltip />
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column prop="reviewTime" label="审核时间" width="180" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const formRef = ref(null)
const loading = ref(false)
const myApplications = ref([])

const applicationForm = reactive({
  category: '',
  reason: '',
  experience: ''
})

const rules = {
  category: [{ required: true, message: '请选择要申请的贴吧', trigger: 'change' }],
  reason: [{ required: true, message: '请填写申请理由', trigger: 'blur' }]
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return textMap[status] || status
}

const loadMyApplications = async () => {
  try {
    const response = await request.get('/bar-manager/my-applications')
    myApplications.value = response.data
  } catch (error) {
    console.error(error)
  }
}

const submitApplication = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const response = await request.post('/bar-manager/apply', applicationForm)
    
    ElMessage.success(response.message)
    resetForm()
    loadMyApplications()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('提交申请失败')
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  loadMyApplications()
})
</script>

<style scoped>
.bar-manager-application {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
