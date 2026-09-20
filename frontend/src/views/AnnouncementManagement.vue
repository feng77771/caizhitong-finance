<template>
  <div class="announcement-management">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button class="add-btn" @click="handleAdd">
        <el-icon :icon="Plus" /> 添加公告
      </el-button>
    </div>
    
    <el-table :data="announcements" border class="announcement-table">
      <el-table-column prop="formattedPublishDate" label="发布时间" width="180" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="url" label="链接" width="300">
        <template #default="scope">
          <a v-if="scope.row.url" :href="scope.row.url" target="_blank" class="url-link">
            {{ scope.row.url.length > 50 ? scope.row.url.substring(0, 50) + '...' : scope.row.url }}
          </a>
          <span v-else class="no-url">无</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center">
        <template #default="scope">
          <div class="action-buttons">
            <el-button size="small" class="edit-btn" @click="editAnnouncement(scope.row)">
              <el-icon :icon="Edit" /> 编辑
            </el-button>
            <el-button size="small" class="delete-btn" @click="deleteAnnouncement(scope.row.id)">
              <el-icon :icon="Delete" /> 删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加/编辑弹窗 -->
    <el-dialog :title="isEdit ? '编辑公告' : '添加公告'" v-model="showAddDialog" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" placeholder="请输入公告内容" :rows="4" />
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.url" placeholder="请输入超链接地址（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ isEdit ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>import { ref, onMounted } from 'vue';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncementApi } from '../api/announcement';
const announcements = ref([]);
const showAddDialog = ref(false);
const isEdit = ref(false);
const form = ref({
 title: '',
 content: '',
 url: ''
});
const currentId = ref(null);
const loadAnnouncements = async () => {
 try {
 const res = await getAnnouncements();
 if (res.code === 200) {
 announcements.value = res.data;
 }
 }
 catch (error) {
 console.error('获取公告列表失败:', error);
 ElMessage.error('获取公告列表失败');
 }
};
const handleAdd = () => {
 resetForm();
 showAddDialog.value = true;
};

const editAnnouncement = (row) => {
 isEdit.value = true;
 currentId.value = row.id;
 form.value = {
 title: row.title,
 content: row.content,
 url: row.url || ''
 };
 showAddDialog.value = true;
};
const deleteAnnouncement = async (id) => {
 try {
 await ElMessageBox.confirm('确定要删除这条公告吗？', '提示', {
 type: 'warning'
 });
 const res = await deleteAnnouncementApi(id);
 if (res.code === 200) {
 ElMessage.success('删除成功');
 loadAnnouncements();
 }
 else {
 ElMessage.error(res.message || '删除失败');
 }
 }
 catch (error) {
 if (error !== 'cancel') {
 ElMessage.error('删除失败');
 }
 }
};
const submitForm = async () => {
 if (!form.value.title || !form.value.content) {
 ElMessage.warning('请填写标题和内容');
 return;
 }
 try {
 let res;
 if (isEdit.value) {
 res = await updateAnnouncement(currentId.value, form.value);
 }
 else {
 res = await createAnnouncement(form.value);
 }
 if (res.code === 200) {
 ElMessage.success(isEdit.value ? '更新成功' : '添加成功');
 showAddDialog.value = false;
 loadAnnouncements();
 resetForm();
 }
 else {
 ElMessage.error(res.message || (isEdit.value ? '更新失败' : '添加失败'));
 }
 }
 catch (error) {
 console.error('提交失败:', error);
 ElMessage.error(isEdit.value ? '更新失败' : '添加失败');
 }
};
const resetForm = () => {
 form.value = {
 title: '',
 content: '',
 url: ''
 };
 isEdit.value = false;
 currentId.value = null;
};
onMounted(() => {
 loadAnnouncements();
});
</script>

<style scoped>
.announcement-management {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  backdrop-filter: blur(10px);
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #ffffff;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.add-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #ffffff;
  font-weight: 600;
  padding: 12px 28px;
  border-radius: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.add-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #8b5cf6 100%);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.announcement-table {
  width: 100%;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  overflow: hidden;
  border: 2px solid rgba(102, 126, 234, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 60px rgba(102, 126, 234, 0.15);
}

.announcement-table :deep(.el-table__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.4) 0%, rgba(118, 75, 162, 0.4) 100%);
}

.announcement-table :deep(.el-table__header th) {
  color: #333333;
  font-weight: 700;
  font-size: 18px;
  padding: 20px 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.5);
  text-align: center;
}

.announcement-table :deep(.el-table__body td) {
  color: #333333;
  font-size: 16px;
  padding: 18px 16px;
  border-bottom: 1px solid rgba(102, 126, 234, 0.2);
  text-align: center !important;
  vertical-align: middle !important;
  line-height: 1.6;
}

.announcement-table :deep(.el-table__row:hover td) {
  background: rgba(102, 126, 234, 0.2);
}

.announcement-table :deep(.el-table__body) {
  background: rgba(0, 0, 0, 0.1);
}

.action-buttons {
  display: flex;
  gap: 20px;
  align-items: center;
  justify-content: center;
  padding: 8px 0;
  width: 100%;
  box-sizing: border-box;
  text-align: center;
}

.edit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #ffffff;
  border-radius: 8px;
  padding: 8px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  min-width: 80px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #8b5cf6 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.delete-btn {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  border: none;
  color: #ffffff;
  border-radius: 8px;
  padding: 8px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.2);
  min-width: 80px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.url-link {
  color: #667eea;
  text-decoration: none;
  word-break: break-all;
  font-size: 14px;
}

.url-link:hover {
  text-decoration: underline;
  color: #764ba2;
}

.no-url {
  color: #888;
  font-style: italic;
}

:deep(.el-dialog) {
  background: #ffffff !important;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  border-bottom: 1px solid #e0e0e0;
  border-radius: 16px 16px 0 0;
}

:deep(.el-dialog__title) {
  color: #333333 !important;
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  color: #333333 !important;
  background: #ffffff !important;
}

:deep(.el-input__inner) {
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 8px;
  color: #333333 !important;
}

:deep(.el-input__inner:focus) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

:deep(.el-textarea__inner) {
  background: #ffffff !important;
  border: 1px solid #dcdfe6 !important;
  border-radius: 8px;
  color: #333333 !important;
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

:deep(.el-form-item__label) {
  color: #333333 !important;
}

:deep(.el-form-item__label::before) {
  color: #ef4444 !important;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #764ba2 0%, #8b5cf6 100%);
}
</style>