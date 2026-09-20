<template>
  <div class="analysis-container">
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span>财报分析</span>
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
            <el-form-item label="报告年份">
              <el-select 
                v-model="searchForm.reportYear" 
                placeholder="请选择年份"
                :disabled="!matchedReports.length"
                style="width: 100%"
              >
                <el-option
                  v-for="report in matchedReports"
                  :key="report.reportYear"
                  :label="report.reportYear + '年'"
                  :value="report.reportYear"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: center;">
            <el-button type="primary" @click="handleAnalyze" :loading="analyzing" :disabled="!canAnalyze">
              开始分析
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
        
        <!-- 动态进度条 -->
        <el-row v-if="progressVisible" style="margin-top: 20px;">
          <el-col :span="24">
            <div class="progress-container">
              <div class="progress-header">
                <span class="progress-icon">🔄</span>
                <span class="progress-title">{{ progressText }}</span>
              </div>
              <div class="progress-bar-wrapper">
                <div class="progress-bar" :style="{ width: progressValue + '%' }">
                  <div class="progress-glow"></div>
                </div>
              </div>
              <div class="progress-percent">{{ progressValue }}%</div>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card v-if="analysisResult" class="result-card">
      <template #header>
        <div class="card-header">
          <span>分析结果 - {{ searchForm.stockName }} {{ searchForm.reportYear }}年报</span>
          <el-button type="success" @click="handleDownload">下载原始PDF</el-button>
        </div>
      </template>
      
      <div class="result-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="财务指标" name="metrics">
            <!-- 基础财务数据 -->
            <el-card class="metric-card main-card">
              <h3>主要财务数据</h3>
              <div class="metrics-grid">
                <div class="metric-item-large" v-if="hasValue(analysisResult.metrics?.revenue)">
                  <span class="metric-label">营业收入</span>
                  <span class="metric-value-large">{{ analysisResult.metrics?.revenue }}亿元</span>
                </div>
                <div class="metric-item-large" v-if="hasValue(analysisResult.metrics?.netProfit)">
                  <span class="metric-label">净利润</span>
                  <span class="metric-value-large highlight">{{ analysisResult.metrics?.netProfit }}亿元</span>
                </div>
                <div class="metric-item-large" v-if="hasValue(analysisResult.metrics?.deductNonRecurringProfit)">
                  <span class="metric-label">扣非净利润</span>
                  <span class="metric-value-large">{{ analysisResult.metrics?.deductNonRecurringProfit }}亿元</span>
                </div>
                <div class="metric-item-large" v-if="hasValue(analysisResult.metrics?.operatingCashFlow)">
                  <span class="metric-label">经营现金流</span>
                  <span class="metric-value-large">{{ analysisResult.metrics?.operatingCashFlow }}亿元</span>
                </div>
              </div>
            </el-card>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-card class="metric-card" v-if="hasValue(analysisResult.metrics?.totalAssets) || hasValue(analysisResult.metrics?.netAssets) || hasValue(analysisResult.metrics?.eps)">
                  <h4>资产负债</h4>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.totalAssets)">
                    <span>总资产</span>
                    <span class="metric-value">{{ analysisResult.metrics?.totalAssets }}亿元</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.netAssets)">
                    <span>净资产</span>
                    <span class="metric-value">{{ analysisResult.metrics?.netAssets }}亿元</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.eps)">
                    <span>每股收益 (EPS)</span>
                    <span class="metric-value">{{ analysisResult.metrics?.eps }}元</span>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card class="metric-card" v-if="hasValue(analysisResult.metrics?.roe) || hasValue(analysisResult.metrics?.roa) || hasValue(analysisResult.metrics?.grossMargin) || hasValue(analysisResult.metrics?.netMargin)">
                  <h4>盈利能力分析</h4>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.roe)">
                    <span>净资产收益率 (ROE)</span>
                    <span class="metric-value">{{ analysisResult.metrics?.roe }}</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.roa)">
                    <span>总资产收益率 (ROA)</span>
                    <span class="metric-value">{{ analysisResult.metrics?.roa }}</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.grossMargin)">
                    <span>毛利率</span>
                    <span class="metric-value">{{ analysisResult.metrics?.grossMargin }}</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.netMargin)">
                    <span>净利率</span>
                    <span class="metric-value">{{ analysisResult.metrics?.netMargin }}</span>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="12">
                <el-card class="metric-card" v-if="hasValue(analysisResult.metrics?.revenueGrowth) || hasValue(analysisResult.metrics?.profitGrowth) || hasValue(analysisResult.metrics?.roeChange) || hasValue(analysisResult.metrics?.assetGrowth) || hasValue(analysisResult.metrics?.cashFlowChange)">
                  <h4>成长能力分析</h4>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.revenueGrowth)">
                    <span>营收增长率</span>
                    <span class="metric-value" :class="{ positive: isPositive(analysisResult.metrics?.revenueGrowth), negative: isNegative(analysisResult.metrics?.revenueGrowth) }">
                      {{ analysisResult.metrics?.revenueGrowth }}
                    </span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.profitGrowth)">
                    <span>净利润增长率</span>
                    <span class="metric-value" :class="{ positive: isPositive(analysisResult.metrics?.profitGrowth), negative: isNegative(analysisResult.metrics?.profitGrowth) }">
                      {{ analysisResult.metrics?.profitGrowth }}
                    </span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.roeChange)">
                    <span>ROE变化</span>
                    <span class="metric-value" :class="{ positive: isPositive(analysisResult.metrics?.roeChange), negative: isNegative(analysisResult.metrics?.roeChange) }">
                      {{ analysisResult.metrics?.roeChange }}
                    </span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.assetGrowth)">
                    <span>资产增长率</span>
                    <span class="metric-value" :class="{ positive: isPositive(analysisResult.metrics?.assetGrowth), negative: isNegative(analysisResult.metrics?.assetGrowth) }">
                      {{ analysisResult.metrics?.assetGrowth }}
                    </span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.cashFlowChange)">
                    <span>现金流变化</span>
                    <span class="metric-value" :class="{ positive: isPositive(analysisResult.metrics?.cashFlowChange), negative: isNegative(analysisResult.metrics?.cashFlowChange) }">
                      {{ analysisResult.metrics?.cashFlowChange }}
                    </span>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card class="metric-card" v-if="hasValue(analysisResult.metrics?.currentRatio) || hasValue(analysisResult.metrics?.quickRatio) || hasValue(analysisResult.metrics?.debtRatio)">
                  <h4>偿债能力分析</h4>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.currentRatio)">
                    <span>流动比率</span>
                    <span class="metric-value">{{ analysisResult.metrics?.currentRatio }}</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.quickRatio)">
                    <span>速动比率</span>
                    <span class="metric-value">{{ analysisResult.metrics?.quickRatio }}</span>
                  </div>
                  <div class="metric-item" v-if="hasValue(analysisResult.metrics?.debtRatio)">
                    <span>资产负债率</span>
                    <span class="metric-value">{{ analysisResult.metrics?.debtRatio }}</span>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>
          
          <el-tab-pane label="AI财报分析" name="ai">
            <el-card class="ai-analysis-card">
              <div class="ai-content" v-html="analysisResult.aiAnalysis"></div>
              
              <div class="chat-section">
                <div class="chat-header">
                  <h4>💬 与AI对话</h4>
                  <p class="chat-hint">基于该财报数据，您可以向AI提问</p>
                </div>
                
                <div class="chat-messages" ref="chatMessagesRef">
                  <div v-if="chatMessages.length === 0" class="chat-empty">
                    <p>开始提问吧！比如：</p>
                    <div class="suggestions">
                      <el-button size="small" @click="askSuggestion('该公司的盈利能力如何？')">该公司的盈利能力如何？</el-button>
                      <el-button size="small" @click="askSuggestion('分析该公司的成长能力')">分析该公司的成长能力</el-button>
                      <el-button size="small" @click="askSuggestion('该公司的主要财务数据怎么样？')">该公司的主要财务数据怎么样？</el-button>
                    </div>
                  </div>
                  
                  <div v-for="(msg, index) in chatMessages" :key="index" 
                       :class="['chat-message', msg.role]">
                    <div class="message-avatar">
                      <span v-if="msg.role === 'user'">👤</span>
                      <span v-else>🤖</span>
                    </div>
                    <div class="message-content">
                      <div class="message-text">{{ msg.content }}</div>
                    </div>
                  </div>
                  
                  <div v-if="chatLoading" class="chat-message ai">
                    <div class="message-avatar">🤖</div>
                    <div class="message-content">
                      <div class="message-text typing">
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                        <span class="typing-dot"></span>
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="chat-input-area">
                  <el-input 
                    v-model="chatInput" 
                    placeholder="请输入您的问题..." 
                    @keyup.enter="sendChatMessage"
                    :disabled="chatLoading"
                    rows="2"
                    type="textarea"
                    resize="none"
                  />
                  <el-button 
                    type="primary" 
                    @click="sendChatMessage"
                    :loading="chatLoading"
                    :disabled="!chatInput.trim()"
                  >
                    发送
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <el-card v-if="!analysisResult && searched" class="no-data-card">
      <div class="no-data">
        <el-empty description="暂无分析数据，请输入股票代码查询" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const searchForm = ref({
  stockCode: '',
  stockName: '',
  reportYear: null
})

const matchedReports = ref([])
const analysisResult = ref(null)
const analyzing = ref(false)
const activeTab = ref('metrics')
const searched = ref(false)

// 进度条相关
const progressVisible = ref(false)
const progressValue = ref(0)
const progressText = ref('')

// 聊天相关
const chatMessages = ref([])
const chatInput = ref('')
const chatLoading = ref(false)
const chatMessagesRef = ref(null)

const canAnalyze = computed(() => {
  return searchForm.value.stockCode && searchForm.value.stockName && searchForm.value.reportYear
})

// 判断值是否有效（不是空或'-'）
const hasValue = (value) => {
  return value !== undefined && value !== null && value !== '-' && value !== ''
}

const handleStockCodeInput = async () => {
  if (!searchForm.value.stockCode) {
    searchForm.value.stockName = ''
    matchedReports.value = []
    return
  }
  
  try {
    const res = await request.get(`/reports/stock/${searchForm.value.stockCode}`)
    if (res.code === 200 && res.data.length > 0) {
      searchForm.value.stockName = res.data[0].stockName
      matchedReports.value = res.data
      if (res.data.length === 1) {
        searchForm.value.reportYear = res.data[0].reportYear
      }
    } else {
      searchForm.value.stockName = ''
      matchedReports.value = []
    }
  } catch (error) {
    console.error('查询财报失败', error)
    ElMessage.error('查询财报失败，请检查网络连接')
  }
}

const handleAnalyze = async () => {
  if (!canAnalyze.value) {
    ElMessage.warning('请完整填写股票代码、公司名称和报告年份')
    return
  }
  
  analyzing.value = true
  searched.value = true
  analysisResult.value = null
  chatMessages.value = []
  chatInput.value = ''
  
  // 初始化进度条
  progressVisible.value = true
  progressValue.value = 0
  progressText.value = '正在准备分析...'
  
  // 模拟进度更新
  const progressSteps = [
    { value: 10, text: '正在查询财报数据...' },
    { value: 25, text: '正在提取财务指标...' },
    { value: 40, text: '正在验证数据完整性...' },
    { value: 55, text: '正在调用AI分析模型...' },
    { value: 70, text: '正在生成分析报告...' },
    { value: 85, text: '正在整理分析结果...' },
    { value: 95, text: '即将完成...' }
  ]
  
  // 启动进度更新定时器
  let stepIndex = 0
  const progressInterval = setInterval(() => {
    if (stepIndex < progressSteps.length) {
      progressValue.value = progressSteps[stepIndex].value
      progressText.value = progressSteps[stepIndex].text
      stepIndex++
    } else {
      // 保持进度在95%直到完成
      progressValue.value = 95
    }
  }, 800)
  
  try {
    const res = await request.get(`/reports/analyze/${searchForm.value.stockCode}/${searchForm.value.reportYear}`)
    if (res.code === 200 && res.data) {
      // 清理AI分析结果中的代码块标记
      if (res.data.aiAnalysis) {
        res.data.aiAnalysis = res.data.aiAnalysis.replace(/```html\s*/gi, '')
        res.data.aiAnalysis = res.data.aiAnalysis.replace(/```\s*/gi, '')
        res.data.aiAnalysis = res.data.aiAnalysis.replace(/\n{3,}/g, '\n\n').trim()
      }
      analysisResult.value = res.data
      
      // 完成进度
      clearInterval(progressInterval)
      progressValue.value = 100
      progressText.value = '分析完成！'
      
      setTimeout(() => {
        progressVisible.value = false
      }, 500)
      
      ElMessage.success('分析完成')
    } else {
      clearInterval(progressInterval)
      progressVisible.value = false
      ElMessage.warning(res.message || '分析失败')
    }
  } catch (error) {
    clearInterval(progressInterval)
    progressVisible.value = false
    console.error('分析失败', error)
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

const handleReset = () => {
  searchForm.value = {
    stockCode: '',
    stockName: '',
    reportYear: null
  }
  matchedReports.value = []
  analysisResult.value = null
  searched.value = false
  chatMessages.value = []
  chatInput.value = ''
}

const handleDownload = async () => {
  if (analysisResult.value?.reportId) {
    try {
      const response = await fetch(`/api/reports/download/file/${analysisResult.value.reportId}`)
      if (!response.ok) {
        throw new Error('网络请求失败')
      }
      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${searchForm.value.stockCode}_${searchForm.value.reportYear}_年报.pdf`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
      ElMessage.success('下载成功')
    } catch (error) {
      console.error('下载失败', error)
      ElMessage.error('下载失败，请稍后重试')
    }
  }
}

const isPositive = (value) => {
  if (!value || value === '-') return false
  return value.startsWith('+') || (!value.startsWith('-') && parseFloat(value) > 0)
}

const isNegative = (value) => {
  if (!value || value === '-') return false
  return value.startsWith('-') && parseFloat(value) < 0
}

// 聊天相关函数
const scrollChatToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

const sendChatMessage = async () => {
  if (!chatInput.value.trim()) return
  if (!analysisResult.value) {
    ElMessage.warning('请先分析财报')
    return
  }
  
  const userMessage = chatInput.value.trim()
  chatMessages.value.push({
    role: 'user',
    content: userMessage
  })
  
  chatInput.value = ''
  chatLoading.value = true
  scrollChatToBottom()
  
  try {
    const formData = new URLSearchParams()
    formData.append('stockCode', searchForm.value.stockCode)
    formData.append('reportYear', searchForm.value.reportYear)
    formData.append('question', userMessage)
    
    const res = await request.post('/reports/chat', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
    
    if (res.code === 200 && res.data) {
      chatMessages.value.push({
        role: 'ai',
        content: res.data
      })
    } else {
      ElMessage.error(res.message || '回答失败')
    }
  } catch (error) {
    console.error('对话失败', error)
    ElMessage.error('对话失败，请稍后重试')
  } finally {
    chatLoading.value = false
    scrollChatToBottom()
  }
}

const askSuggestion = (question) => {
  chatInput.value = question
  sendChatMessage()
}

onMounted(() => {
  const stockCode = new URLSearchParams(window.location.search).get('stockCode')
  if (stockCode) {
    searchForm.value.stockCode = stockCode
    handleStockCodeInput()
  }
})
</script>

<style scoped>
.analysis-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(2, 229, 180, 0.03) 100%);
  border: 1px solid rgba(2, 229, 180, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  margin-bottom: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(2, 229, 180, 0.03) 100%);
  border: 1px solid rgba(2, 229, 180, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.result-content {
  padding: 24px 0;
}

.metric-card {
  height: 100%;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(2, 229, 180, 0.03) 100%);
  border: 1px solid rgba(2, 229, 180, 0.15);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(2, 229, 180, 0.15);
  border-color: rgba(2, 229, 180, 0.3);
}

.metric-card.main-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.08) 0%, rgba(2, 229, 180, 0.03) 100%);
  border: 1px solid rgba(2, 229, 180, 0.25);
}

.metric-card h3 {
  margin: 0 0 20px 0;
  color: #02e5b4;
  border-bottom: 2px solid rgba(2, 229, 180, 0.4);
  padding-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.metric-card h3::before {
  content: '';
  width: 4px;
  height: 22px;
  background: linear-gradient(180deg, #02e5b4, #03a9f4);
  border-radius: 2px;
}

.metric-card h4 {
  margin: 0 0 16px 0;
  color: #02e5b4;
  border-bottom: 1px solid rgba(2, 229, 180, 0.3);
  padding-bottom: 10px;
  font-size: 15px;
  font-weight: 500;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  min-height: 120px;
}

.metric-item-large {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.12) 0%, rgba(2, 229, 180, 0.05) 100%);
  padding: 24px 20px;
  border-radius: 14px;
  text-align: center;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(2, 229, 180, 0.25);
  transition: all 0.3s ease;
}

.metric-item-large:hover {
  background: rgba(2, 229, 180, 0.15);
  transform: translateY(-2px);
}

.metric-item-large .metric-label {
  display: block;
  font-size: 13px;
  color: #8892a6;
  margin-bottom: 8px;
  font-weight: 400;
}

.metric-item-large .metric-value-large {
  font-size: 24px;
  font-weight: 700;
  color: #03a9f4;
  line-height: 1.3;
}

.metric-item-large .metric-value-large.highlight {
  color: #02e5b4;
}

.metric-row {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  transition: background-color 0.2s ease;
}

.metric-item:hover {
  background: rgba(255, 255, 255, 0.03);
}

.metric-item:first-child {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.metric-item:last-child {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.metric-item span:first-child {
  color: #b4bccc;
  font-size: 14px;
  font-weight: 400;
  min-width: 120px;
}

.metric-value {
  font-weight: 600;
  color: #03a9f4;
  font-size: 15px;
  text-align: right;
  min-width: 80px;
}

.metric-value.positive {
  color: #67c23a;
}

.metric-value.negative {
  color: #f56c6c;
}

.ai-analysis-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(2, 229, 180, 0.03) 100%);
  border-radius: 16px;
  border: 1px solid rgba(2, 229, 180, 0.15);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.ai-content {
  line-height: 1.9;
  color: #e4e7ed;
  font-size: 15px;
  padding: 24px;
}

.ai-content p {
  margin: 20px 0;
  text-indent: 2em;
  text-align: justify;
}

.ai-content ul {
  padding-left: 32px;
  margin: 16px 0;
}

.ai-content li {
  margin: 12px 0;
  list-style-type: none;
  position: relative;
  padding-left: 20px;
}

.ai-content li::before {
  content: '▸';
  position: absolute;
  left: 0;
  color: #02e5b4;
  font-weight: bold;
}

.ai-content h2 {
  color: #02e5b4;
  font-size: 20px;
  margin: 30px 0 20px 0;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(2, 229, 180, 0.3);
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-content h2::before {
  content: '';
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #02e5b4, #03a9f4);
  border-radius: 2px;
}

.ai-content h3 {
  color: #02e5b4;
  font-size: 16px;
  margin: 24px 0 16px 0;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-content h3::before {
  content: '';
  width: 8px;
  height: 8px;
  background: #02e5b4;
  border-radius: 50%;
}

.ai-content strong {
  color: #02e5b4;
  font-weight: 600;
}

.ai-content em {
  color: #90caf9;
  font-style: italic;
}

.ai-content .highlight {
  background: rgba(2, 229, 180, 0.15);
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #02e5b4;
  margin: 12px 0;
}

.ai-content code {
  background: rgba(0, 0, 0, 0.3);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #f0f0f0;
}

.chat-section {
  margin-top: 0;
  padding: 24px;
  background: rgba(0, 0, 0, 0.1);
  border-top: 1px solid rgba(2, 229, 180, 0.1);
}

.chat-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header h4 {
  margin: 0;
  color: #e4e7ed;
  font-size: 17px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-header h4::before {
  content: '💬';
  font-size: 20px;
}

.chat-hint {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.chat-messages {
  max-height: 450px;
  overflow-y: auto;
  padding: 20px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.chat-empty {
  text-align: center;
  color: #909399;
  padding: 40px 20px;
}

.chat-empty p {
  margin-bottom: 20px;
  font-size: 14px;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.suggestions .el-button {
  border-color: rgba(2, 229, 180, 0.4);
  color: #02e5b4;
  background: rgba(2, 229, 180, 0.08);
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 13px;
  transition: all 0.3s ease;
}

.suggestions .el-button:hover {
  border-color: #02e5b4;
  color: #fff;
  background: rgba(2, 229, 180, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(2, 229, 180, 0.3);
}

.chat-message {
  display: flex;
  gap: 14px;
  margin-bottom: 20px;
  align-items: flex-start;
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.chat-message.user .message-avatar {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.3), rgba(2, 229, 180, 0.1));
}

.message-content {
  flex: 1;
  max-width: 75%;
}

.chat-message.user .message-content {
  display: flex;
  justify-content: flex-end;
}

.message-text {
  padding: 14px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  color: #e4e7ed;
  line-height: 1.7;
  word-break: break-word;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-message.user .message-text {
  background: linear-gradient(135deg, rgba(2, 229, 180, 0.25), rgba(2, 229, 180, 0.15));
  border: 1px solid rgba(2, 229, 180, 0.3);
}

.message-text.typing {
  display: flex;
  gap: 6px;
  padding: 20px 18px;
}

.typing-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #02e5b4;
  animation: typing 1.4s infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input-area {
  display: flex;
  gap: 14px;
  align-items: flex-end;
}

.chat-input-area .el-textarea {
  flex: 1;
}

.chat-input-area .el-textarea textarea {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #e4e7ed;
  font-size: 14px;
  padding: 12px 16px;
  resize: none;
}

.chat-input-area .el-textarea textarea::placeholder {
  color: #666;
}

.chat-input-area .el-button {
  height: 76px;
  border-radius: 12px;
  padding: 0 32px;
  font-weight: 600;
  background: linear-gradient(135deg, #02e5b4, #03a9f4);
  border: none;
  color: #000;
  transition: all 0.3s ease;
}

.chat-input-area .el-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(2, 229, 180, 0.4);
}

.chat-input-area .el-button:disabled {
  background: rgba(255, 255, 255, 0.1);
  color: #666;
}

.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(2, 229, 180, 0.4);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(2, 229, 180, 0.6);
}

.raw-text-card {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.raw-text {
  white-space: pre-wrap;
  word-break: break-all;
  color: #c0c4cc;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.7;
  padding: 16px;
}

.no-data-card {
  min-height: 300px;
  border-radius: 12px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__nav-wrap) {
  padding-bottom: 16px;
}

/* 进度条样式 */
.progress-container {
  background: rgba(2, 229, 180, 0.08);
  border: 1px solid rgba(2, 229, 180, 0.2);
  border-radius: 12px;
  padding: 20px 24px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.progress-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.progress-icon {
  font-size: 20px;
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.progress-title {
  font-size: 15px;
  color: #02e5b4;
  font-weight: 500;
}

.progress-bar-wrapper {
  height: 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(135deg, #02e5b4, #03a9f4);
  border-radius: 6px;
  transition: width 0.5s ease;
  position: relative;
  overflow: hidden;
}

.progress-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.progress-glow {
  position: absolute;
  top: -50%;
  left: 0;
  right: 0;
  height: 200%;
  background: linear-gradient(
    0deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  animation: glow 2s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% {
    opacity: 0.3;
    transform: translateY(-25%);
  }
  50% {
    opacity: 0.8;
    transform: translateY(25%);
  }
}

.progress-percent {
  text-align: right;
  margin-top: 8px;
  font-size: 14px;
  color: #02e5b4;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 20px;
}

:deep(.el-tabs__item.is-active) {
  color: #02e5b4;
}

:deep(.el-tabs__active-bar) {
  background-color: #02e5b4;
}

@media screen and (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .metric-item span:first-child {
    min-width: 100px;
  }
}

@media screen and (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .analysis-container {
    padding: 16px;
  }
  
  .metric-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .metric-value {
    min-width: auto;
    text-align: left;
  }
}
</style>