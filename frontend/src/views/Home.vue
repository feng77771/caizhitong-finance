<template>
  <div class="home-container">
    <div class="main-section">
      <!-- 标题区域 -->
      <div class="welcome-header">
        <h1>欢迎使用多模态财报深度解析平台</h1>
        <p>专业的财务数据分析与风险评估平台</p>
      </div>
      
      <!-- 轮播图区域 -->
      <el-carousel height="520px" class="home-carousel">
        <el-carousel-item>
          <img src="/l1.png" alt="轮播图1" class="carousel-image" />
        </el-carousel-item>
        <el-carousel-item>
          <img src="/l2.png" alt="轮播图2" class="carousel-image" />
        </el-carousel-item>
        <el-carousel-item>
          <img src="/l3.png" alt="轮播图3" class="carousel-image" />
        </el-carousel-item>
        <el-carousel-item>
          <img src="/l4.png" alt="轮播图4" class="carousel-image" />
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 统计卡片区域（独立框） -->
    <div class="statistics-section">
      <div class="statistics-cards">
        <div class="stat-card stock-card">
          <div class="stat-icon">
            <el-icon :size="40"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.stockCount || 0 }}</div>
            <div class="stat-label">股票数量</div>
            <div class="stat-desc">共收录</div>
          </div>
        </div>
        <div class="stat-card data-card">
          <div class="stat-icon">
            <el-icon :size="40"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.recordCount || 0 }}</div>
            <div class="stat-label">数据条数</div>
            <div class="stat-desc">财务指标</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 行业分布统计区域 -->
    <div class="industry-section">
      <div class="section-title">
        <h2>行业分布统计</h2>
      </div>
      <div class="industry-chart-container">
        <div ref="pieChartRef" class="pie-chart"></div>
      </div>
    </div>

    <!-- 最新公告区域 -->
    <div class="announcement-section">
      <div class="section-title">
        <h2>最新公告</h2>
      </div>
      <div class="announcement-list">
        <div class="announcement-item" v-for="(item, index) in announcements" :key="index">
          <div class="announcement-date">{{ item.formattedPublishDate || item.publishDate }}</div>
          <div class="announcement-content">
            <a v-if="item.url" :href="item.url" target="_blank" class="announcement-link">
              {{ item.content }}
            </a>
            <span v-else>{{ item.content }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { TrendCharts, Document } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const statistics = ref({
  stockCount: 0,
  recordCount: 0
})

const industryData = ref([])
const pieChartRef = ref(null)
let pieChart = null

const announcements = ref([])

const fetchStatistics = async () => {
  try {
    const res = await request.get('/financial/statistics')
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchAnnouncements = async () => {
  try {
    const res = await request.get('/announcements/latest')
    if (res.code === 200) {
      announcements.value = res.data
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

const fetchIndustryDistribution = async () => {
  try {
    const res = await request.get('/financial/industry-distribution')
    if (res.code === 200) {
      // 过滤掉"其他"分类
      industryData.value = res.data.filter(item => item.name !== '其他')
      renderPieChart()
    }
  } catch (error) {
    console.error('获取行业分布失败:', error)
  }
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  
  if (pieChart) {
    pieChart.dispose()
  }
  
  pieChart = echarts.init(pieChartRef.value)
  
  const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      textStyle: {
        fontSize: 20
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        color: '#fff',
        fontSize: 20
      }
    },
    series: [
      {
        name: '行业分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#0a0a0a',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%',
          color: '#fff',
          fontSize: 20
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 24,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true,
          lineStyle: {
            color: '#888'
          }
        },
        data: industryData.value.map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: colors[index % colors.length]
          }
        }))
      }
    ]
  }
  
  pieChart.setOption(option)
}

const handleResize = () => {
  if (pieChart) {
    pieChart.resize()
  }
}

onMounted(() => {
  fetchStatistics()
  fetchIndustryDistribution()
  fetchAnnouncements()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) {
    pieChart.dispose()
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-size: 24px;
}

.main-section {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #0a0a0a 100%);
  border-radius: 12px;
  border: 1px solid rgba(2, 229, 180, 0.2);
  box-shadow: 0 0 30px rgba(2, 229, 180, 0.15);
  overflow: hidden;
}

.welcome-header {
  color: #02e5b4;
  padding: 30px 40px;
  text-align: center;
  border-bottom: 1px solid rgba(2, 229, 180, 0.2);
}

.welcome-header h1 {
  font-size: 40px;
  margin-bottom: 10px;
}

.welcome-header p {
  font-size: 24px;
  color: #888;
}

.statistics-section {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #0a0a0a 100%);
  border-radius: 12px;
  border: 1px solid rgba(2, 229, 180, 0.2);
  box-shadow: 0 0 30px rgba(2, 229, 180, 0.15);
  padding: 30px 40px;
}

.statistics-cards {
  display: flex;
  gap: 30px;
  justify-content: center;
  flex-wrap: wrap;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px 40px;
  border-radius: 12px;
  min-width: 280px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stock-card {
  background: linear-gradient(135deg, #1e3a5f 0%, #0d1f33 100%);
  border: 1px solid rgba(3, 169, 244, 0.3);
  box-shadow: 0 8px 24px rgba(3, 169, 244, 0.2);
}

.stock-card:hover {
  box-shadow: 0 12px 32px rgba(3, 169, 244, 0.3);
}

.data-card {
  background: linear-gradient(135deg, #1e3f35 0%, #0d231f 100%);
  border: 1px solid rgba(2, 229, 180, 0.3);
  box-shadow: 0 8px 24px rgba(2, 229, 180, 0.2);
}

.data-card:hover {
  box-shadow: 0 12px 32px rgba(2, 229, 180, 0.3);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.stock-card .stat-icon {
  color: #03a9f4;
}

.data-card .stat-icon {
  color: #02e5b4;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 48px;
  font-weight: bold;
  color: #fff;
  line-height: 1.2;
}

.stock-card .stat-value {
  color: #03a9f4;
}

.data-card .stat-value {
  color: #02e5b4;
}

.stat-label {
  font-size: 24px;
  color: #fff;
  margin-top: 5px;
}

.stat-desc {
  font-size: 24px;
  color: #888;
  margin-top: 3px;
}

.home-carousel {
  border-radius: 0;
  border: none;
  box-shadow: none;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
}

:deep(.el-carousel__indicator) {
  background-color: rgba(2, 229, 180, 0.3);
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

:deep(.el-carousel__indicator.is-active) {
  background-color: #02e5b4;
}

:deep(.el-carousel__arrow) {
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 40px;
  height: 40px;
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(2, 229, 180, 0.5);
}

.industry-section {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #0a0a0a 100%);
  border-radius: 12px;
  border: 1px solid rgba(2, 229, 180, 0.2);
  box-shadow: 0 0 30px rgba(2, 229, 180, 0.15);
  padding: 30px 40px;
}

.section-title {
  text-align: center;
  margin-bottom: 20px;
}

.section-title h2 {
  color: #02e5b4;
  font-size: 28px;
  margin: 0;
}

.industry-chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.pie-chart {
  width: 100%;
  max-width: 700px;
  height: 450px;
}

.announcement-section {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #0a0a0a 100%);
  border-radius: 12px;
  border: 1px solid rgba(2, 229, 180, 0.2);
  box-shadow: 0 0 30px rgba(2, 229, 180, 0.15);
  padding: 30px 40px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border-left: 3px solid #02e5b4;
  transition: background 0.3s;
}

.announcement-item:hover {
  background: rgba(2, 229, 180, 0.1);
}

.announcement-date {
  color: #02e5b4;
  font-size: 24px;
  font-weight: bold;
  min-width: 120px;
}

.announcement-content {
  color: #fff;
  font-size: 24px;
  flex: 1;
}

.announcement-link {
  color: #02e5b4;
  text-decoration: none;
  transition: color 0.3s;
}

.announcement-link:hover {
  color: #00ffcc;
  text-decoration: underline;
}
</style>
