<template>
  <div class="reports-management">
    <div class="page-header">
      <h3>财报管理</h3>
      <div class="header-right">
        <div class="user-info">
          <span class="user-label">管理员</span>
        </div>
        <el-button type="primary" @click="showUploadDialog = true" class="upload-btn">
          <el-icon><Upload /></el-icon>
          上传财报
        </el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索公司名称或代码"
        prefix-icon="Search"
        style="width: 300px"
        @input="handleSearch"
      />
      <el-select v-model="filterYear" placeholder="选择年份" style="width: 150px; margin-left: 10px" @change="handleFilter">
        <el-option label="全部年份" value="" />
        <el-option label="2025年" value="2025" />
        <el-option label="2024年" value="2024" />
        <el-option label="2023年" value="2023" />
        <el-option label="2022年" value="2022" />
        <el-option label="2021年" value="2021" />
      </el-select>
    </div>
    
    <el-table :data="filteredReports" style="width: 100%" stripe>
      <el-table-column prop="id" label="ID" min-width="80" />
      <el-table-column prop="stockName" label="公司名称" min-width="200" />
      <el-table-column prop="stockCode" label="股票代码" min-width="120" />
      <el-table-column prop="reportType" label="财报类型" min-width="120">
        <template #default="{ row }">
          <el-tag :type="getReportTypeColor(row.reportType)">
            {{ row.reportType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reportYear" label="年份" min-width="100" />
      <el-table-column prop="uploadTime" label="上传时间" min-width="180" />
      <el-table-column prop="analyzedAt" label="分析时间" min-width="180">
        <template #default="{ row }">
          {{ row.analyzedAt || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'analyzed' ? 'success' : 'warning'">
            {{ row.status === 'analyzed' ? '已分析' : '待分析' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="380">
        <template #default="{ row }">
          <el-button size="small" @click="handleViewPdf(row)">查看PDF</el-button>
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 上传财报对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传财报" width="600px">
      <el-form :model="uploadForm" label-width="100px">
        <el-form-item label="公司名称">
          <el-input v-model="uploadForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="股票代码">
          <el-input v-model="uploadForm.companyCode" placeholder="请输入股票代码" />
        </el-form-item>
        <el-form-item label="财报类型">
          <el-select v-model="uploadForm.reportType" placeholder="请选择财报类型">
            <el-option label="年报" value="年报" />
            <el-option label="半年报" value="半年报" />
            <el-option label="季报" value="季报" />
          </el-select>
        </el-form-item>
        <el-form-item label="财报年份">
          <el-select v-model="uploadForm.reportYear" placeholder="请选择年份">
            <el-option label="2025年" value="2025" />
            <el-option label="2024年" value="2024" />
            <el-option label="2023年" value="2023" />
            <el-option label="2022年" value="2022" />
            <el-option label="2021年" value="2021" />
          </el-select>
        </el-form-item>
        <el-form-item label="财报文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".pdf,.xlsx,.xls"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持PDF、Excel格式，文件大小不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpload">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑财报对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑财报" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="公司名称">
          <el-input v-model="editForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="股票代码">
          <el-input v-model="editForm.companyCode" placeholder="请输入股票代码" />
        </el-form-item>
        <el-form-item label="财报类型">
          <el-select v-model="editForm.reportType" placeholder="请选择财报类型">
            <el-option label="年报" value="年报" />
            <el-option label="半年报" value="半年报" />
            <el-option label="季报" value="季报" />
          </el-select>
        </el-form-item>
        <el-form-item label="财报年份">
          <el-select v-model="editForm.reportYear" placeholder="请选择年份">
            <el-option label="2025年" value="2025" />
            <el-option label="2024年" value="2024" />
            <el-option label="2023年" value="2023" />
            <el-option label="2022年" value="2022" />
            <el-option label="2021年" value="2021" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传PDF">
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :show-file-list="true"
            :file-list="editFileList"
            :on-change="handleEditFileChange"
            :before-upload="beforeUpload"
            accept=".pdf"
          >
            <el-button size="small" type="primary">点击上传PDF</el-button>
            <template #tip>
              <div class="el-upload__tip">仅支持PDF格式文件，选填，不填则保持原文件</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 查看PDF对话框 -->
    <el-dialog v-model="showPdfDialog" title="查看PDF" width="90%" height="85%">
      <div class="pdf-container">
        <iframe :src="currentPdfUrl" width="100%" height="650px" frameborder="0"></iframe>
      </div>
      <template #footer>
        <el-button @click="showPdfDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import request from '@/utils/request'

const reports = ref([])
const searchKeyword = ref('')
const filterYear = ref('')
const showUploadDialog = ref(false)
const uploadForm = ref({
  companyName: '',
  companyCode: '',
  reportType: '',
  reportYear: '',
  file: null
})

const showEditDialog = ref(false)
const editForm = ref({
  id: '',
  companyName: '',
  companyCode: '',
  reportType: '',
  reportYear: '',
  file: null
})

const showPdfDialog = ref(false)
const currentPdfUrl = ref('')
const editFileList = ref([])

const filteredReports = computed(() => {
  let result = reports.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(report => 
      report.stockName.toLowerCase().includes(keyword) || 
      report.stockCode.toLowerCase().includes(keyword)
    )
  }
  
  if (filterYear.value) {
    result = result.filter(report => report.reportYear === filterYear.value)
  }
  
  return result
})

const getReportTypeColor = (type) => {
  const colorMap = {
    '年报': 'danger',
    '半年报': 'warning',
    '季报': 'primary'
  }
  return colorMap[type] || 'info'
}

const loadReports = async () => {
  try {
    const response = await request.get('/admin/reports')
    reports.value = response.data
  } catch (error) {
    ElMessage.error('加载财报列表失败')
  }
}

const handleSearch = () => {
  // 搜索逻辑由 computed 自动处理
}

const handleFilter = () => {
  // 筛选逻辑由 computed 自动处理
}

const handleFileChange = (file) => {
  uploadForm.value.file = file.raw
}

const handleUpload = async () => {
  try {
    const formData = new FormData()
    formData.append('companyName', uploadForm.value.companyName)
    formData.append('companyCode', uploadForm.value.companyCode)
    formData.append('reportType', uploadForm.value.reportType)
    formData.append('reportYear', uploadForm.value.reportYear)
    formData.append('file', uploadForm.value.file)
    
    await request.post('/admin/reports/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    ElMessage.success('上传财报成功')
    showUploadDialog.value = false
    uploadForm.value = { companyName: '', companyCode: '', reportType: '', reportYear: '', file: null }
    loadReports()
  } catch (error) {
    ElMessage.error('上传财报失败')
  }
}

const handleViewPdf = async (report) => {
  try {
    const response = await request.get(`/admin/reports/${report.id}/pdf`, {
      responseType: 'blob'
    })
    // 响应拦截器返回的是完整的response对象
    const blob = new Blob([response.data], { type: 'application/pdf' })
    currentPdfUrl.value = URL.createObjectURL(blob)
    showPdfDialog.value = true
  } catch (error) {
    ElMessage.error('加载PDF失败')
  }
}

const handleEdit = (report) => {
  editForm.value = {
    id: report.id,
    companyName: report.stockName,
    companyCode: report.stockCode,
    reportType: report.reportType,
    reportYear: String(report.reportYear),
    file: null
  }
  editFileList.value = []
  showEditDialog.value = true
}

const handleEditFileChange = (file) => {
  editForm.value.file = file.raw
  editFileList.value = [file]
}

const handleSaveEdit = async () => {
  try {
    const formData = new FormData()
    formData.append('companyName', editForm.value.companyName)
    formData.append('companyCode', editForm.value.companyCode)
    formData.append('reportType', editForm.value.reportType)
    formData.append('reportYear', editForm.value.reportYear)
    if (editForm.value.file) {
      formData.append('file', editForm.value.file)
    }
    
    await request.post(`/admin/reports/${editForm.value.id}/update`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    ElMessage.success('编辑财报成功')
    showEditDialog.value = false
    editFileList.value = []
    loadReports()
  } catch (error) {
    ElMessage.error('编辑财报失败')
  }
}

const handleDelete = async (report) => {
  try {
    await ElMessageBox.confirm('确定要删除该财报吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/reports/${report.id}`)
    ElMessage.success('删除财报成功')
    loadReports()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除财报失败')
    }
  }
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.reports-management {
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

:deep(.el-form-item__label) {
  color: #a0a0b0;
  font-size: 20px;
}

:deep(.el-input__wrapper) {
  background: rgba(2, 229, 180, 0.05);
  border: 1px solid rgba(2, 229, 180, 0.2);
}

:deep(.el-input__inner) {
  color: #a0a0b0;
  font-size: 20px;
}

:deep(.el-select__input) {
  font-size: 20px;
}

:deep(.el-upload__tip) {
  color: #666;
  font-size: 16px;
  margin-top: 8px;
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
</style>