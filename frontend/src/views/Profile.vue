<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>公司画像</h1>
      <p>基于财务指标的多维度企业分析</p>
    </div>

    <!-- 股票选择区域 -->
    <div class="stock-selector">
      <el-input
        v-model="stockCode"
        placeholder="请输入股票代码（如：002594）"
        style="width: 550px; margin-right: 10px"
        @keyup.enter="searchStock"
      >
        <template #append>
          <el-button @click="searchStock" :loading="loading">搜索</el-button>
        </template>
      </el-input>
      <el-select
        v-if="stockOptions.length > 0"
        v-model="selectedStock"
        placeholder="请选择股票"
        style="width: 300px"
        @change="onStockChange"
      >
        <el-option
          v-for="stock in stockOptions"
          :key="stock.stock_code"
          :label="`${stock.stock_code} - ${stock.stock_name}`"
          :value="stock.stock_code"
        />
      </el-select>
      <el-select
        v-if="availableYears.length > 0"
        v-model="selectedYears"
        multiple
        placeholder="请选择年份"
        style="width: 350px"
        @change="loadStockData"
      >
        <el-option
          v-for="year in availableYears"
          :key="year"
          :label="year + '年'"
          :value="year"
        />
      </el-select>
    </div>

    <!-- 公司画像内容 -->
    <div v-if="stockData.length > 0" class="profile-content">
      <!-- 公司基本信息 -->
      <div class="company-info">
        <el-card>
          <div class="info-header">
            <h2>{{ stockData[0].stock_name }} ({{ stockData[0].stock_code }})</h2>
            <span class="industry-tag">{{ stockData[0].industry }}</span>
          </div>
          <div class="info-stats">
            <div class="stat-item">
              <div class="stat-label">数据年份</div>
              <div class="stat-value">{{ stockData.length }} 年</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">最新年份</div>
              <div class="stat-value">{{ stockData[stockData.length - 1].report_date }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">风险等级</div>
              <div class="stat-value" :class="getRiskClass(stockData[stockData.length - 1].risk)">
                {{ getRiskText(stockData[stockData.length - 1].risk) }}
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 盈利能力画像 -->
      <div class="profitability-section">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>盈利能力画像</span>
              <el-tag :type="getProfitabilityLevel().type">{{ getProfitabilityLevel().text }}</el-tag>
            </div>
          </template>
          <div class="profitability-content">
            <div class="profitability-score">
              <div class="score-circle">
                <div class="score-value">{{ getProfitabilityScore() }}</div>
                <div class="score-label">盈利能力评分</div>
              </div>
            </div>
            <div class="profitability-indicators">
              <div class="indicator-item">
                <div class="indicator-label">ROE (净资产收益率)</div>
                <div class="indicator-value">{{ formatPercent(stockData[stockData.length - 1].roe) }}</div>
                <div class="indicator-trend" :class="getTrendClass('roe')">
                  {{ getTrendIcon('roe') }} {{ formatPercent(getTrendValue('roe')) }}
                </div>
              </div>
              <div class="indicator-item">
                <div class="indicator-label">毛利率</div>
                <div class="indicator-value">{{ formatPercent(stockData[stockData.length - 1].gross_margin) }}</div>
                <div class="indicator-trend" :class="getTrendClass('gross_margin')">
                  {{ getTrendIcon('gross_margin') }} {{ formatPercent(getTrendValue('gross_margin')) }}
                </div>
              </div>
              <div class="indicator-item">
                <div class="indicator-label">EPS (每股收益)</div>
                <div class="indicator-value">{{ formatNumber(stockData[stockData.length - 1].eps) }}</div>
                <div class="indicator-trend" :class="getTrendClass('eps')">
                  {{ getTrendIcon('eps') }} {{ formatNumber(getTrendValue('eps')) }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 成长能力画像 -->
      <div class="growth-section">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成长能力画像</span>
              <el-tag :type="getGrowthLevel().type">{{ getGrowthLevel().text }}</el-tag>
            </div>
          </template>
          <div class="growth-content">
            <div class="growth-score">
              <div class="score-circle">
                <div class="score-value">{{ getGrowthScore() }}</div>
                <div class="score-label">成长能力评分</div>
              </div>
            </div>
            <div class="growth-indicators">
              <div class="indicator-item">
                <div class="indicator-label">营收增长率</div>
                <div class="indicator-value">{{ formatPercent(stockData[stockData.length - 1].revenue_growth) }}</div>
                <div class="indicator-trend" :class="getTrendClass('revenue_growth')">
                  {{ getTrendIcon('revenue_growth') }} {{ formatPercent(getTrendValue('revenue_growth')) }}
                </div>
              </div>
              <div class="indicator-item">
                <div class="indicator-label">净利润增长率</div>
                <div class="indicator-value">{{ formatPercent(stockData[stockData.length - 1].profit_growth) }}</div>
                <div class="indicator-trend" :class="getTrendClass('profit_growth')">
                  {{ getTrendIcon('profit_growth') }} {{ formatPercent(getTrendValue('profit_growth')) }}
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 现金流画像 -->
      <div class="cashflow-section">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>现金流画像</span>
              <el-tag :type="getCashflowLevel().type">{{ getCashflowLevel().text }}</el-tag>
            </div>
          </template>
          <div class="cashflow-content">
            <div class="cashflow-score">
              <div class="score-circle">
                <div class="score-value">{{ getCashflowScore() }}</div>
                <div class="score-label">现金流健康度</div>
              </div>
            </div>
            <div class="cashflow-indicators">
              <div class="indicator-item">
                <div class="indicator-label">经营现金流</div>
                <div class="indicator-value">{{ formatNumber(stockData[stockData.length - 1].cash_flow) }} 亿元</div>
                <div class="indicator-trend" :class="getTrendClass('cash_flow')">
                  {{ getTrendIcon('cash_flow') }} {{ formatNumber(getTrendValue('cash_flow')) }} 亿元
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 企业标签画像 -->
      <div class="tags-section">
        <el-card>
          <template #header>
            <span>企业标签画像</span>
          </template>
          <div class="tags-content">
            <el-tag
              v-for="tag in getCompanyTags()"
              :key="tag"
              :type="getTagType(tag)"
              size="large"
              class="company-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
        </el-card>
      </div>

      <!-- 多年趋势分析 -->
      <div class="trend-section">
        <el-card>
          <template #header>
            <span>多年趋势分析</span>
          </template>
          <div class="trend-content">
            <div class="trend-tabs">
              <el-radio-group v-model="activeTrend" @change="updateTrendChart">
                <el-radio-button value="eps">EPS趋势</el-radio-button>
                <el-radio-button value="roe">ROE趋势</el-radio-button>
                <el-radio-button value="gross_margin">毛利率趋势</el-radio-button>
                <el-radio-button value="revenue_growth">营收增长率趋势</el-radio-button>
                <el-radio-button value="profit_growth">净利润增长率趋势</el-radio-button>
              </el-radio-group>
            </div>
            <div ref="trendChart" class="trend-chart"></div>
          </div>
        </el-card>
      </div>

      <!-- AI公司画像 -->
      <div class="ai-section">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI公司画像</span>
              <el-button type="primary" size="small" @click="generateAIProfile" :loading="aiLoading">
                <el-icon><Refresh /></el-icon>
                重新生成
              </el-button>
            </div>
          </template>
          <div class="ai-content">
            <div v-if="aiProfile" class="ai-result">
              <div class="ai-text">{{ aiProfile }}</div>
            </div>
            <div v-else class="ai-loading">
              <el-icon class="is-loading" :size="40"><Loading /></el-icon>
              <p>正在生成AI公司画像...</p>
            </div>
          </div>
        </el-card>
      </div>

      <!-- AI问答区域 -->
      <div class="ai-chat-section">
        <el-card>
          <template #header>
            <span>AI问答</span>
          </template>
          <div class="chat-container">
            <!-- 对话历史 -->
            <div class="chat-history">
              <div
                v-for="(message, index) in chatMessages"
                :key="index"
                class="chat-message"
                :class="{ 'user-message': message.isUser, 'ai-message': !message.isUser }"
              >
                <div class="message-avatar">
                  <el-icon v-if="message.isUser" size="24">User</el-icon>
                  <el-icon v-else size="24">Bot</el-icon>
                </div>
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ message.time }}</div>
                </div>
              </div>
            </div>
            
            <!-- 输入区域 -->
            <div class="chat-input">
              <el-input
                v-model="chatInput"
                placeholder="请输入您的问题..."
                style="flex: 1"
                @keyup.enter="sendChatMessage"
              />
              <el-button type="primary" @click="sendChatMessage" :loading="chatLoading">
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-empty description="请输入股票代码开始分析" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import axios from 'axios'
import { Refresh, Loading } from '@element-plus/icons-vue'

const stockCode = ref('')
const stockOptions = ref([])
const selectedStock = ref('')
const availableYears = ref([])
const selectedYears = ref([])
const stockData = ref([])
const loading = ref(false)
const aiLoading = ref(false)
const aiProfile = ref('')
const activeTrend = ref('eps')
const trendChart = ref(null)
let chartInstance = null

// AI问答相关状态
const chatInput = ref('')
const chatMessages = ref([])
const chatLoading = ref(false)

const searchStock = async () => {
  if (!stockCode.value) {
    ElMessage.warning('请输入股票代码')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE || '/api'}/financial/stock-history/${stockCode.value}`)
    if (response.data.code === 200 && response.data.data.length > 0) {
      const data = response.data.data
      const firstItem = data[0]
      stockOptions.value = [{
        stock_code: firstItem.stock_code,
        stock_name: firstItem.stock_name,
        industry: firstItem.industry
      }]
      selectedStock.value = stockCode.value
      onStockChange()
    } else {
      ElMessage.warning('未找到该股票数据')
      stockOptions.value = []
      stockData.value = []
    }
  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const onStockChange = async () => {
  if (!selectedStock.value) return

  loading.value = true
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE || '/api'}/financial/stock-history/${selectedStock.value}`)
    if (response.data.code === 200) {
      const allData = response.data.data
      const years = [...new Set(allData.map(item => item.report_date))].sort((a, b) => a - b)
      availableYears.value = years
      selectedYears.value = years.slice(-4)
      loadStockData()
    }
  } catch (error) {
    ElMessage.error('获取年份失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadStockData = async () => {
  if (!selectedStock.value) return

  loading.value = true
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE || '/api'}/financial/stock-history/${selectedStock.value}`)
    if (response.data.code === 200) {
      let data = response.data.data
      if (selectedYears.value.length > 0) {
        data = data.filter(item => selectedYears.value.includes(item.report_date))
      }
      stockData.value = data.sort((a, b) => a.report_date - b.report_date)
      nextTick(() => {
        updateTrendChart()
        generateAIProfile()
      })
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const formatPercent = (value) => {
  if (value === null || value === undefined) return '-'
  return (value * 100).toFixed(2) + '%'
}

const formatNumber = (value) => {
  if (value === null || value === undefined) return '-'
  return value.toFixed(2)
}

const getRiskClass = (risk) => {
  if (risk === 1) return 'risk-low'
  if (risk === 2) return 'risk-medium'
  return 'risk-high'
}

const getRiskText = (risk) => {
  if (risk === 1) return '低风险'
  if (risk === 2) return '中风险'
  return '高风险'
}

const getProfitabilityLevel = () => {
  const latest = stockData.value[stockData.value.length - 1]
  const roe = latest.roe || 0
  const grossMargin = latest.gross_margin || 0

  if (roe > 0.2 && grossMargin > 0.3) {
    return { type: 'success', text: '优秀' }
  } else if (roe > 0.1 && grossMargin > 0.2) {
    return { type: 'warning', text: '良好' }
  } else {
    return { type: 'danger', text: '一般' }
  }
}

const getProfitabilityScore = () => {
  const latest = stockData.value[stockData.value.length - 1]
  const roe = latest.roe || 0
  const grossMargin = latest.gross_margin || 0
  const eps = latest.eps || 0

  const score = (roe * 30 + grossMargin * 40 + Math.min(eps / 10, 1) * 30).toFixed(0)
  return score
}

const getGrowthLevel = () => {
  const latest = stockData.value[stockData.value.length - 1]
  const revenueGrowth = latest.revenue_growth || 0
  const profitGrowth = latest.profit_growth || 0

  if (revenueGrowth > 0.2 && profitGrowth > 0.2) {
    return { type: 'success', text: '优秀' }
  } else if (revenueGrowth > 0.1 && profitGrowth > 0.1) {
    return { type: 'warning', text: '良好' }
  } else {
    return { type: 'danger', text: '一般' }
  }
}

const getGrowthScore = () => {
  const latest = stockData.value[stockData.value.length - 1]
  const revenueGrowth = latest.revenue_growth || 0
  const profitGrowth = latest.profit_growth || 0

  const score = (Math.min(revenueGrowth / 0.5, 1) * 50 + Math.min(profitGrowth / 0.5, 1) * 50).toFixed(0)
  return score
}

const getCashflowLevel = () => {
  const latest = stockData.value[stockData.value.length - 1]
  const cashFlow = latest.cash_flow || 0

  if (cashFlow > 10) {
    return { type: 'success', text: '健康' }
  } else if (cashFlow > 0) {
    return { type: 'warning', text: '一般' }
  } else {
    return { type: 'danger', text: '较差' }
  }
}

const getCashflowScore = () => {
  const latest = stockData.value[stockData.value.length - 1]
  const cashFlow = latest.cash_flow || 0

  const score = Math.min(Math.max(cashFlow / 20 * 100, 0), 100).toFixed(0)
  return score
}

const getTrendValue = (field) => {
  if (stockData.value.length < 2) return 0
  const latest = stockData.value[stockData.value.length - 1][field]
  const previous = stockData.value[stockData.value.length - 2][field]
  return latest - previous
}

const getTrendClass = (field) => {
  const trend = getTrendValue(field)
  if (trend > 0) return 'trend-up'
  if (trend < 0) return 'trend-down'
  return 'trend-stable'
}

const getTrendIcon = (field) => {
  const trend = getTrendValue(field)
  if (trend > 0) return '↑'
  if (trend < 0) return '↓'
  return '→'
}

const getCompanyTags = () => {
  const tags = []
  const latest = stockData.value[stockData.value.length - 1]

  if (latest.roe > 0.2) tags.push('高盈利')
  if (latest.revenue_growth > 0.1) tags.push('高成长')
  if (latest.cash_flow > 0) tags.push('现金流充裕')
  if (latest.gross_margin > 0.3) tags.push('高毛利企业')
  if (latest.risk === 1) tags.push('低风险')
  if (latest.profit_growth > 0.2) tags.push('利润高增长')

  if (tags.length === 0) tags.push('一般企业')

  return tags
}

const getTagType = (tag) => {
  const successTags = ['高盈利', '高成长', '现金流充裕', '高毛利企业', '低风险', '利润高增长']
  return successTags.includes(tag) ? 'success' : 'info'
}

const updateTrendChart = () => {
  if (!trendChart.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(trendChart.value)

  const years = stockData.value.map(item => item.report_date)
  const data = stockData.value.map(item => item[activeTrend.value])

  const option = {
    title: {
      text: getTrendTitle(activeTrend.value),
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: years,
      name: '年份'
    },
    yAxis: {
      type: 'value',
      name: getTrendUnit(activeTrend.value),
      axisLabel: {
        formatter: (value) => {
          if (activeTrend.value === 'eps' || activeTrend.value === 'cash_flow') {
            return value.toFixed(2)
          }
          return (value * 100).toFixed(1) + '%'
        }
      }
    },
    series: [
      {
        data: data,
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const getTrendTitle = (trend) => {
  const titles = {
    eps: 'EPS趋势',
    roe: 'ROE趋势',
    gross_margin: '毛利率趋势',
    revenue_growth: '营收增长率趋势',
    profit_growth: '净利润增长率趋势'
  }
  return titles[trend] || trend
}

const getTrendUnit = (trend) => {
  if (trend === 'eps' || trend === 'cash_flow') {
    return '亿元'
  }
  return '百分比'
}

const generateAIProfile = async () => {
  if (stockData.value.length === 0) return

  aiLoading.value = true
  aiProfile.value = ''

  try {
    const latest = stockData.value[stockData.value.length - 1]
    const prompt = `请根据以下财务指标生成企业画像分析：

公司名称：${latest.stock_name} (${latest.stock_code})
行业：${latest.industry}

财务指标：
- EPS（每股收益）：${latest.eps}
- ROE（净资产收益率）：${formatPercent(latest.roe)}
- 毛利率：${formatPercent(latest.gross_margin)}
- 营收增长率：${formatPercent(latest.revenue_growth)}
- 净利润增长率：${formatPercent(latest.profit_growth)}
- 经营现金流：${latest.cash_flow} 亿元
- 每股净资产（BPS）：${latest.bps}
- 风险等级：${getRiskText(latest.risk)}

请从以下维度进行分析：
1. 盈利能力分析
2. 成长能力分析
3. 现金流状况分析
4. 整体财务健康状况
5. 投资价值评估

请用简洁专业的语言进行分析，每部分不超过50字。`

    const response = await axios.post(`${import.meta.env.VITE_API_BASE || '/api'}/ai/analyze`, {
      prompt: prompt
    })

    if (response.data.code === 200) {
      aiProfile.value = response.data.data
    } else {
      aiProfile.value = 'AI分析暂时不可用，请稍后再试。'
    }
  } catch (error) {
    console.error('AI分析失败:', error)
    aiProfile.value = 'AI分析服务暂时不可用，请稍后再试。'
  } finally {
    aiLoading.value = false
  }
}

// AI问答发送消息
const sendChatMessage = async () => {
  if (!chatInput.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }

  const userMessage = chatInput.value.trim()
  chatInput.value = ''
  
  // 添加用户消息
  chatMessages.value.push({
    isUser: true,
    content: userMessage,
    time: new Date().toLocaleTimeString('zh-CN')
  })

  chatLoading.value = true

  try {
    // 构建包含股票信息的prompt
    let prompt = userMessage
    
    // 如果有股票数据，添加到prompt中
    if (stockData.value.length > 0) {
      const latest = stockData.value[stockData.value.length - 1]
      prompt = `
请参考以下公司数据回答问题：

公司名称：${latest.stock_name} (${latest.stock_code})
行业：${latest.industry}

财务指标：
- EPS（每股收益）：${latest.eps}
- ROE（净资产收益率）：${formatPercent(latest.roe)}
- 毛利率：${formatPercent(latest.gross_margin)}
- 营收增长率：${formatPercent(latest.revenue_growth)}
- 净利润增长率：${formatPercent(latest.profit_growth)}
- 经营现金流：${latest.cash_flow} 亿元
- 每股净资产（BPS）：${latest.bps}
- 风险等级：${getRiskText(latest.risk)}

用户问题：${userMessage}

请用简洁专业的语言回答。
      `.trim()
    }

    const response = await axios.post(`${import.meta.env.VITE_API_BASE || '/api'}/ai/analyze`, {
      prompt: prompt
    })

    if (response.data.code === 200) {
      chatMessages.value.push({
        isUser: false,
        content: response.data.data,
        time: new Date().toLocaleTimeString('zh-CN')
      })
    } else {
      chatMessages.value.push({
        isUser: false,
        content: 'AI服务暂时不可用，请稍后再试。',
        time: new Date().toLocaleTimeString('zh-CN')
      })
    }
  } catch (error) {
    console.error('AI问答失败:', error)
    chatMessages.value.push({
      isUser: false,
      content: 'AI服务暂时不可用，请稍后再试。',
      time: new Date().toLocaleTimeString('zh-CN')
    })
  } finally {
    chatLoading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
})
</script>

<style scoped>
.profile-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  font-size: 24px;
}

.profile-header {
  text-align: center;
  margin-bottom: 30px;
}

.profile-header h1 {
  font-size: 40px;
  color: #ffffff;
  margin-bottom: 10px;
}

.profile-header p {
  font-size: 24px;
  color: #ffffff;
}

.stock-selector {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 30px;
  gap: 15px;
  flex-wrap: wrap;
}

.stock-selector :deep(.el-input) {
  width: 550px;
  height: 72px;
}

.stock-selector :deep(.el-select) {
  width: 420px;
  height: 72px;
}

.stock-selector :deep(.el-input-group) {
  height: 72px;
}

.stock-selector :deep(.el-input-group__append) {
  height: 72px;
  padding: 0;
}

.stock-selector :deep(.el-input__inner) {
  font-size: 28px;
  height: 72px;
  line-height: 72px;
}

.stock-selector :deep(.el-select__input) {
  font-size: 28px;
  height: 72px;
  line-height: 72px;
}

.stock-selector :deep(.el-select__selection-item) {
  font-size: 28px;
  height: 68px;
  line-height: 68px;
  margin-top: 2px;
  padding: 0 15px;
}

.stock-selector :deep(.el-select__tags) {
  flex-wrap: nowrap;
  overflow: hidden;
  height: 72px;
  align-items: center;
}

.stock-selector :deep(.el-select__wrapper) {
  height: 72px;
}

.stock-selector :deep(.el-select-dropdown__item) {
  font-size: 28px;
}

.stock-selector :deep(.el-button) {
  font-size: 28px;
  height: 72px;
  padding: 0 40px;
  line-height: 72px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.company-info {
  margin-bottom: 10px;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.info-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.industry-tag {
  background: #f0f9ff;
  color: #409eff;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 24px;
}

.info-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 24px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.risk-low {
  color: #67c23a;
}

.risk-medium {
  color: #e6a23c;
}

.risk-high {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profitability-content,
.growth-content,
.cashflow-content {
  display: flex;
  gap: 50px;
  align-items: center;
}

.profitability-score,
.growth-score,
.cashflow-score {
  flex-shrink: 0;
}

.score-circle {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.4),
              inset 0 0 60px rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.score-circle::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
}

.score-value {
  font-size: 64px;
  font-weight: bold;
  line-height: 1.1;
  position: relative;
  z-index: 1;
}

.score-label {
  font-size: 24px;
  margin-top: 10px;
  opacity: 0.95;
  text-align: center;
  padding: 0 15px;
  position: relative;
  z-index: 1;
}

.profitability-indicators,
.growth-indicators,
.cashflow-indicators {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.indicator-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.indicator-item:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(2, 229, 180, 0.2);
}

.indicator-label {
  font-size: 26px;
  color: #b8c4d4;
  flex: 1;
}

.indicator-value {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  margin: 0 20px;
}

.indicator-trend {
  font-size: 24px;
  font-weight: bold;
  min-width: 140px;
  text-align: right;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-stable {
  color: #8892a6;
}

.tags-content {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.company-tag {
  font-size: 24px;
  padding: 12px 24px;
  background: rgba(2, 229, 180, 0.1);
  color: #02e5b4;
  border: 1px solid rgba(2, 229, 180, 0.2);
  border-radius: 25px;
  transition: all 0.3s ease;
}

.company-tag:hover {
  background: rgba(2, 229, 180, 0.2);
  box-shadow: 0 0 20px rgba(2, 229, 180, 0.3);
}

.trend-content {
  padding: 20px 0;
}

.trend-tabs {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

:deep(.trend-tabs .el-tabs__item) {
  font-size: 26px;
  color: #8892a6;
  padding: 12px 30px;
  border-radius: 10px;
  margin: 0 5px;
}

:deep(.trend-tabs .el-tabs__item:hover) {
  color: #02e5b4;
}

:deep(.trend-tabs .el-tabs__item.is-active) {
  color: #02e5b4;
  background: rgba(2, 229, 180, 0.15);
}

.trend-chart {
  width: 100%;
  height: 450px;
}

.ai-content {
  min-height: 200px;
}

.ai-result {
  padding: 30px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  line-height: 2;
  color: #000000;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.ai-text {
  white-space: pre-wrap;
  font-size: 26px;
}

.ai-loading {
  text-align: center;
  padding: 40px;
}

.ai-loading p {
  font-size: 26px;
  color: #8892a6;
}

.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #909399;
}

.ai-loading .el-icon {
  margin-bottom: 15px;
}

.ai-loading p {
  font-size: 24px;
}

.empty-state {
  margin-top: 100px;
}

/* AI问答样式 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-message {
  display: flex;
  gap: 15px;
  max-width: 85%;
}

.user-message {
  align-self: flex-end;
}

.ai-message {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-message .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-message .message-avatar {
  background: linear-gradient(135deg, #02e5b4 0%, #00d4aa 100%);
}

.message-content {
  background: rgba(255, 255, 255, 0.05);
  padding: 16px 20px;
  border-radius: 16px;
  max-width: calc(100% - 79px);
}

.user-message .message-content {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.ai-message .message-content {
  background: rgba(2, 229, 180, 0.1);
  border: 1px solid rgba(2, 229, 180, 0.2);
}

.message-text {
  font-size: 26px;
  color: #000000;
  line-height: 1.6;
  white-space: pre-wrap;
}

.message-time {
  font-size: 20px;
  color: #6b7280;
  margin-top: 8px;
  text-align: right;
}

.chat-input {
  display: flex;
  gap: 15px;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.chat-input :deep(.el-input__inner) {
  font-size: 26px;
  height: 56px;
  line-height: 56px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #b8c4d4;
}

.chat-input :deep(.el-button) {
  font-size: 26px;
  padding: 14px 30px;
}

/* 滚动条样式 */
.chat-history::-webkit-scrollbar {
  width: 8px;
}

.chat-history::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.chat-history::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.chat-history::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>