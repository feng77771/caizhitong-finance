<template>
  <div class="users-management">
    <div class="page-header">
      <h3>用户管理</h3>
      <div class="header-right">
        <div class="user-info">
          <span class="user-label">管理员</span>
        </div>
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名或邮箱"
        prefix-icon="Search"
        style="width: 300px"
        @input="handleSearch"
      />
    </div>
    
    <el-table :data="filteredUsers" style="width: 100%" stripe>
      <el-table-column prop="id" label="ID" min-width="80" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="role" label="角色" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'">
            {{ row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="150" />
      <el-table-column prop="registerTime" label="注册时间" min-width="180" />
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
            {{ row.status === 'active' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加用户对话框 -->
    <el-dialog v-model="showAddDialog" title="添加用户" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑用户对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择状态">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const users = ref([])
const searchKeyword = ref('')
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const userForm = ref({
  username: '',
  email: '',
  phone: '',
  password: '',
  role: 'user'
})
const editForm = ref({
  id: null,
  username: '',
  email: '',
  phone: '',
  role: 'user',
  status: 'active'
})

const filteredUsers = computed(() => {
  if (!searchKeyword.value) return users.value
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(user => 
    user.username.toLowerCase().includes(keyword) || 
    user.email.toLowerCase().includes(keyword)
  )
})

const loadUsers = async () => {
  try {
    const response = await request.get('/admin/users')
    users.value = response.data
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const handleSearch = () => {
  // 搜索逻辑由 computed 自动处理
}

const handleAddUser = async () => {
  try {
    await request.post('/admin/users', userForm.value)
    ElMessage.success('添加用户成功')
    showAddDialog.value = false
    userForm.value = { username: '', email: '', password: '', role: 'user' }
    loadUsers()
  } catch (error) {
    ElMessage.error('添加用户失败')
  }
}

const handleEdit = (user) => {
  editForm.value = { ...user }
  showEditDialog.value = true
}

const handleUpdateUser = async () => {
  try {
    await request.put(`/admin/users/${editForm.value.id}`, editForm.value)
    ElMessage.success('更新用户成功')
    showEditDialog.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('更新用户失败')
  }
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/admin/users/${user.id}`)
    ElMessage.success('删除用户成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-management {
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
}

.search-bar :deep(.el-input__inner) {
  font-size: 20px;
  height: 48px;
  line-height: 48px;
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
</style>