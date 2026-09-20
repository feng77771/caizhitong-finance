<template>
  <div class="risk-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>风险分析</span>
          <el-button type="primary" @click="handleAnalyze">开始分析</el-button>
        </div>
      </template>
      
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="公司代码">
              <el-input v-model="searchForm.stockCode" placeholder="请输入股票代码" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="公司名称">
              <el-input v-model="searchForm.companyName" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分析类型">
              <el-select v-model="searchForm.analysisType" placeholder="请选择分析类型" style="width: 100%">
                <el-option label="全面风险分析" value="comprehensive" />
                <el-option label="财务风险分析" value="financial" />
                <el-option label="经营风险分析" value="operational" />
                <el-option label="市场风险分析" value="market" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider />

      <div class="risk-content" v-if="riskData">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="risk-card">
              <div class="risk-header">
                <h4>综合风险评级</h4>
                <el-tag :type="getRiskTagType(riskData.overallRisk.level)" size="large">
                  {{ riskData.overallRisk.level }}
                </el-tag>
              </div>
              <div class="risk-score">
                <span class="score-label">风险评分</span>
                <span class="score-value" :class="getScoreClass(riskData.overallRisk.score)">
                  {{ riskData.overallRisk.score }}
                </span>
              </div>
            </el-card>
          </el-col>
          <el-col :span="16">
            <el-card class="risk-detail-card">
              <h4>风险详情</h4>
              <el-table :data="riskData.riskItems" style="width: 100%">
                <el-table-column prop="category" label="风险类别" width="150" />
                <el-table-column prop="item" label="风险项目" width="200" />
                <el-table-column prop="description" label="风险描述" />
                <el-table-column prop="level" label="风险等级" width="120">
                  <template #default="scope">
                    <el-tag :type="getRiskTagType(scope.row.level)" size="small">
                      {{ scope.row.level }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-card style="margin-top: 20px">
          <h4>风险建议</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(suggestion, index) in riskData.suggestions"
              :key="index"
              :timestamp="suggestion.date"
              placement="top"
            >
              <el-card>
                <h5>{{ suggestion.title }}</h5>
                <p>{{ suggestion.content }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>

      <el-empty v-else description="请输入公司信息并点击开始分析" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const searchForm = reactive({
  stockCode: '',
  companyName: '',
  analysisType: 'comprehensive'
})

const riskData = ref(null)

const getRiskTagType = (level) => {
  const typeMap = {
    '低风险': 'success',
    '中风险': 'warning',
    '高风险': 'danger'
  }
  return typeMap[level] || 'info'
}

const getScoreClass = (score) => {
  if (score >= 80) return 'high-risk'
  if (score >= 60) return 'medium-risk'
  return 'low-risk'
}

const handleAnalyze = () => {
  if (!searchForm.stockCode && !searchForm.companyName) {
    ElMessage.warning('请输入公司代码或名称')
    return
  }
  
  // 模拟数据
  riskData.value = {
    overallRisk: {
      level: '中风险',
      score: 65
    },
    riskItems: [
      {
        category: '财务风险',
        item: '流动性风险',
        description: '公司流动比率偏低，短期偿债能力存在一定压力',
        level: '中风险'
      },
      {
        category: '财务风险',
        item: '债务风险',
        description: '资产负债率处于合理区间，债务结构较为稳定',
        level: '低风险'
      },
      {
        category: '经营风险',
        item: '盈利风险',
        description: '净利润增长率有所下降，需关注盈利能力变化',
        level: '中风险'
      },
      {
        category: '经营风险',
        item: '运营风险',
        description: '存货周转率正常，运营效率良好',
        level: '低风险'
      },
      {
        category: '市场风险',
        item: '行业风险',
        description: '行业竞争加剧，市场份额面临挑战',
        level: '中风险'
      }
    ],
    suggestions: [
      {
        date: '2024-01-15',
        title: '加强流动性管理',
        content: '建议公司优化现金流管理，提高资金使用效率，确保短期偿债能力。'
      },
      {
        date: '2024-01-10',
        title: '关注盈利能力',
        content: '建议公司关注成本控制，提升产品附加值，改善盈利状况。'
      },
      {
        date: '2024-01-05',
        title: '市场竞争力提升',
        content: '建议公司加大研发投入，提升产品竞争力，巩固市场地位。'
      }
    ]
  }
}
</script>

<style scoped>
.risk-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.risk-content {
  margin-top: 20px;
}

.risk-card {
  height: 100%;
}

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.risk-header h4 {
  margin: 0;
  color: #333;
}

.risk-score {
  text-align: center;
  padding: 20px 0;
}

.score-label {
  display: block;
  color: #666;
  margin-bottom: 10px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
}

.score-value.low-risk {
  color: #67C23A;
}

.score-value.medium-risk {
  color: #E6A23C;
}

.score-value.high-risk {
  color: #F56C6C;
}

.risk-detail-card h4 {
  margin-bottom: 20px;
  color: #333;
}
</style>