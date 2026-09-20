<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1 class="admin-title">
        <Settings :size="32" />
        管理员控制台
      </h1>
      <p class="admin-subtitle">多模态财报深度解析平台 - 管理中心</p>
    </div>
    
    <div class="admin-content">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">
            <Users :size="28" />
          </div>
          <div class="stat-info">
            <p class="stat-value">128</p>
            <p class="stat-label">用户总数</p>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <FileText :size="28" />
          </div>
          <div class="stat-info">
            <p class="stat-value">512</p>
            <p class="stat-label">财报分析</p>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <Building :size="28" />
          </div>
          <div class="stat-info">
            <p class="stat-value">64</p>
            <p class="stat-label">公司画像</p>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <AlertTriangle :size="28" />
          </div>
          <div class="stat-info">
            <p class="stat-value">23</p>
            <p class="stat-label">风险预警</p>
          </div>
        </div>
      </div>
      
      <div class="admin-actions">
        <h3 class="actions-title">快捷操作</h3>
        <div class="action-buttons">
          <el-button type="primary" class="action-btn">
            <UserPlus :size="20" />
            <span>添加用户</span>
          </el-button>
          <el-button type="primary" class="action-btn">
            <Upload :size="20" />
            <span>导入财报</span>
          </el-button>
          <el-button type="primary" class="action-btn">
            <BarChart :size="20" />
            <span>数据统计</span>
          </el-button>
          <el-button type="primary" class="action-btn">
            <Settings :size="20" />
            <span>系统设置</span>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Settings, Users, FileText, Building, AlertTriangle, UserPlus, Upload, BarChart } from '@element-plus/icons-vue'
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #0d1117 100%);
  padding: 30px;
}

.admin-header {
  text-align: center;
  margin-bottom: 40px;
}

.admin-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  color: #02e5b4;
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 0 30px rgba(2, 229, 180, 0.5);
}

.admin-subtitle {
  color: #666;
  font-size: 14px;
}

.admin-content {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 25px;
  background: rgba(15, 15, 20, 0.9);
  border-radius: 15px;
  border: 1px solid rgba(2, 229, 180, 0.2);
  box-shadow: 0 0 30px rgba(2, 229, 180, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 40px rgba(2, 229, 180, 0.2);
}

.stat-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.2) 0%, rgba(2, 229, 180, 0.1) 100%);
  border-radius: 12px;
  color: #02e5b4;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #fff;
  margin: 0 0 5px 0;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin: 0;
}

.admin-actions {
  background: rgba(15, 15, 20, 0.9);
  border-radius: 15px;
  border: 1px solid rgba(2, 229, 180, 0.2);
  padding: 30px;
}

.actions-title {
  color: #02e5b4;
  font-size: 18px;
  margin: 0 0 20px 0;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(2, 229, 180, 0.15);
}

.action-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 30px;
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.2) 0%, rgba(2, 229, 180, 0.1) 100%);
  border: 1px solid rgba(2, 229, 180, 0.5);
  border-radius: 10px;
  color: #02e5b4;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.3) 0%, rgba(2, 229, 180, 0.15) 100%);
  box-shadow: 0 0 25px rgba(2, 229, 180, 0.3);
  transform: translateY(-3px);
}
</style>