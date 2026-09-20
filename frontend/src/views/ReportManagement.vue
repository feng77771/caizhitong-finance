<template>
  <div class="report-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>财报管理</span>
          <el-button type="primary" @click="dialogVisible = true">上传财报</el-button>
        </div>
      </template>
      
      <el-table :data="reports" stripe style="width: 100%">
        <el-table-column prop="stockCode" label="股票代码" width="120" />
        <el-table-column prop="stockName" label="股票名称" width="150" />
        <el-table-column prop="reportYear" label="报告年份" width="120" />
        <el-table-column prop="pdfPath" label="PDF文件" min-width="200">
          <template #default="{ row }">
            <span>{{ getFileName(row.pdfPath) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="上传财报" width="500px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="股票代码">
          <el-input v-model="uploadForm.stockCode" placeholder="如: 002594" />
        </el-form-item>
        <el-form-item label="股票名称">
          <el-input v-model="uploadForm.stockName" placeholder="如: 比亚迪" />
        </el-form-item>
        <el-form-item label="报告年份">
          <el-date-picker
            v-model="uploadForm.reportYear"
            type="year"
            placeholder="选择年份"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="选择PDF">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".pdf"
            :on-change="handleFileChange"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传pdf文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const reports = ref([])
const dialogVisible = ref(false)
const uploadRef = ref(null)
const uploadForm = ref({
  stockCode: '',
  stockName: '',
  reportYear: '',
  file: null
})

const fetchReports = async () => {
  try {
    const res = await request.get('/reports/list')
    if (res.code === 200) {
      reports.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取财报列表失败')
  }
}

const handleFileChange = (file) => {
  uploadForm.value.file = file.raw
}

const handleUpload = async () => {
  if (!uploadForm.value.stockCode) {
    ElMessage.warning('请输入股票代码')
    return
  }
  if (!uploadForm.value.stockName) {
    ElMessage.warning('请输入股票名称')
    return
  }
  if (!uploadForm.value.reportYear) {
    ElMessage.warning('请选择报告年份')
    return
  }
  if (!uploadForm.value.file) {
    ElMessage.warning('请选择PDF文件')
    return
  }

  const formData = new FormData()
  formData.append('file', uploadForm.value.file)
  formData.append('stockCode', uploadForm.value.stockCode)
  formData.append('stockName', uploadForm.value.stockName)
  formData.append('reportYear', new Date(uploadForm.value.reportYear).getFullYear())

  try {
    const res = await request.post('/reports/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      ElMessage.success('上传成功')
      dialogVisible.value = false
      uploadForm.value = { stockCode: '', stockName: '', reportYear: '', file: null }
      fetchReports()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleDelete = async (id) => {
  try {
    const res = await request.delete(`/reports/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchReports()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const getFileName = (path) => {
  if (!path) return ''
  return path.split('/').pop().split('\\').pop()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.report-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
