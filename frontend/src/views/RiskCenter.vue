<template>
  <div class="risk-container">
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span>风险评估中心</span>
        </div>
      </template>
      
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="股票代码">
              <el-input 
                v-model="searchForm.stockCode" 
                placeholder="请输入股票代码"
                @input="handleStockCodeInput"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="公司名称">
              <el-input 
                v-model="searchForm.stockName" 
                placeholder="自动匹配"
                readonly
                class="stock-name-input"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" @click="predict" :loading="loading">
                开始评估
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <el-card v-if="result" class="result-card">
      <template #header>
        <div class="result-header">
          <span>评估结果</span>
        </div>
      </template>
      
      <div class="result-content">
        <div class="company-info">
          <div class="info-item">
            <span class="label">股票代码</span>
            <span class="value">{{ result.stockCode }}</span>
          </div>
          <div class="info-item">
            <span class="label">公司名称</span>
            <span class="value">{{ result.companyName }}</span>
          </div>
        </div>
        
        <div class="risk-display">
          <div class="risk-chart">
            <el-progress 
              type="dashboard" 
              :percentage="result.riskProb"
              :color="getRiskColor(result.riskProb)"
              :stroke-width="16"
              :show-info="true"
              info-format="percent"
            />
          </div>
          
          <div class="risk-level">
            <span class="level-label">风险等级</span>
            <span class="level-value" :class="getRiskLevelClass(result.riskLevel)">
              {{ result.riskLevel }}
            </span>
          </div>
        </div>
        
        <div class="risk-description">
          <p v-if="result.riskLevel === '低风险'">
            🟢 该公司财务状况良好，各项指标健康，投资风险较低。
          </p>
          <p v-else-if="result.riskLevel === '中风险'">
            🟡 该公司存在一定的风险因素，建议谨慎投资，密切关注财务变化。
          </p>
          <p v-else>
            🔴 该公司风险较高，建议深入研究后再做投资决策。
          </p>
        </div>
        
        <!-- 指标风险评分展示 -->
        <div class="indicators-section">
          <h3 class="section-title">财务指标风险评分</h3>
          <div class="indicators-grid">
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">ROE</span>
                <span class="indicator-weight">权重: 25%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.roe?.value ?? '-' }}%</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.roe?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.roe?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.roe?.score ?? '-' }}%</div>
            </div>
            
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">EPS</span>
                <span class="indicator-weight">权重: 5%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.eps?.value ?? '-' }}</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.eps?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.eps?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.eps?.score ?? '-' }}%</div>
            </div>
            
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">毛利率</span>
                <span class="indicator-weight">权重: 10%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.gross_margin?.value ?? '-' }}%</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.gross_margin?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.gross_margin?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.gross_margin?.score ?? '-' }}%</div>
            </div>
            
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">营收增长率</span>
                <span class="indicator-weight">权重: 15%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.revenue_growth?.value ?? '-' }}%</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.revenue_growth?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.revenue_growth?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.revenue_growth?.score ?? '-' }}%</div>
            </div>
            
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">利润增长率</span>
                <span class="indicator-weight">权重: 20%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.profit_growth?.value ?? '-' }}%</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.profit_growth?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.profit_growth?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.profit_growth?.score ?? '-' }}%</div>
            </div>
            
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">现金流</span>
                <span class="indicator-weight">权重: 20%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.cash_flow?.value ?? '-' }}</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.cash_flow?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.cash_flow?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.cash_flow?.score ?? '-' }}%</div>
            </div>
            
            <div class="indicator-item">
              <div class="indicator-header">
                <span class="indicator-name">BPS</span>
                <span class="indicator-weight">权重: 5%</span>
              </div>
              <div class="indicator-value">{{ result.indicators?.bps?.value ?? '-' }}</div>
              <div class="indicator-score-bar">
                <div 
                  class="score-fill" 
                  :style="{ width: (result.indicators?.bps?.score ?? 0) + '%', backgroundColor: getScoreColor(result.indicators?.bps?.score ?? 0) }"
                ></div>
              </div>
              <div class="indicator-score">风险评分: {{ result.indicators?.bps?.score ?? '-' }}%</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card v-if="error" class="error-card" style="display: none;">
      <div class="error-message">
        {{ error }}
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const searchForm = reactive({
  stockCode: '',
  stockName: ''
})

const result = ref(null)
const loading = ref(false)
const error = ref('')

const handleStockCodeInput = async (val) => {
  if (val && val.length === 6) {
    try {
      const res = await request.get(`/risk/stock-info/${val}`)
      if (res && res.data) {
        searchForm.stockName = res.data.name
      } else {
        searchForm.stockName = ''
      }
    } catch (error) {
      searchForm.stockName = ''
    }
  } else {
    searchForm.stockName = ''
  }
}

const predict = async () => {
  if (!searchForm.stockCode || searchForm.stockCode.length !== 6) {
    ElMessage.warning('请输入正确的股票代码')
    return
  }
  
  loading.value = true
  result.value = null
  error.value = ''
  
  try {
    const res = await request.get(`/risk/${searchForm.stockCode}`)
    if (res && res.data) {
      result.value = res.data
    } else {
      error.value = '评估失败，请稍后重试'
      ElMessage.error('评估失败，请稍后重试')
    }
  } catch (err) {
    // 判断是否是"未找到该股票的财务数据"错误
    const errorMessage = err.message || ''
    if (errorMessage.includes('未找到该股票')) {
      error.value = errorMessage
      ElMessage.error(errorMessage)
    } else {
      error.value = '评估失败，请稍后重试'
      ElMessage.error('评估失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const getRiskColor = (prob) => {
  if (prob < 30) return '#67c23a'
  if (prob < 60) return '#e6a23c'
  return '#f56c6c'
}

const getRiskLevelClass = (level) => {
  if (level === '低风险') return 'risk-low'
  if (level === '中风险') return 'risk-medium'
  return 'risk-high'
}

const getScoreColor = (score) => {
  if (score < 30) return '#67c23a'
  if (score < 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.risk-container {
  padding: 24px;
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(2, 229, 180, 0.03) 100%);
  border: 1px solid rgba(2, 229, 180, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: #e4e7ed;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header span::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #02e5b4, #03a9f4);
  border-radius: 2px;
}

.stock-name-input :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.1);
}

.result-card {
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(2, 229, 180, 0.03) 100%);
  border: 1px solid rgba(2, 229, 180, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.result-header span {
  font-size: 17px;
  font-weight: 600;
  color: #02e5b4;
}

.result-content {
  padding: 24px;
}

.company-info {
  display: flex;
  gap: 40px;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item .label {
  font-size: 14px;
  color: #909399;
}

.info-item .value {
  font-size: 18px;
  font-weight: 600;
  color: #e4e7ed;
}

.risk-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 80px;
  margin-bottom: 32px;
}

.risk-chart {
  width: 200px;
  height: 200px;
}

.risk-level {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.level-label {
  font-size: 14px;
  color: #909399;
}

.level-value {
  font-size: 36px;
  font-weight: 700;
  padding: 12px 32px;
  border-radius: 12px;
}

.level-value.risk-low {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.15);
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.level-value.risk-medium {
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.15);
  border: 1px solid rgba(230, 162, 60, 0.3);
}

.level-value.risk-high {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.15);
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.risk-description {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.risk-description p {
  margin: 0;
  font-size: 15px;
  color: #b4bccc;
  line-height: 1.8;
}

.error-card {
  border-color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.error-message {
  color: #f56c6c;
  text-align: center;
  padding: 20px;
}

/* 指标展示区域样式 */
.indicators-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #02e5b4;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, #02e5b4, #03a9f4);
  border-radius: 2px;
}

.indicators-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1200px) {
  .indicators-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .indicators-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .indicators-grid {
    grid-template-columns: 1fr;
  }
}

.indicator-item {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.indicator-item:hover {
  border-color: rgba(2, 229, 180, 0.3);
  transform: translateY(-2px);
}

.indicator-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.indicator-name {
  font-size: 14px;
  font-weight: 600;
  color: #e4e7ed;
}

.indicator-weight {
  font-size: 12px;
  color: #909399;
  background: rgba(255, 255, 255, 0.08);
  padding: 2px 8px;
  border-radius: 10px;
}

.indicator-value {
  font-size: 20px;
  font-weight: 700;
  color: #02e5b4;
  margin-bottom: 12px;
}

.indicator-score-bar {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.score-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.indicator-score {
  font-size: 12px;
  color: #909399;
}
</style>