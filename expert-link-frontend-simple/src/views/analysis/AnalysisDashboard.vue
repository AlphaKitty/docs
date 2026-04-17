<template>
  <div class="analysis-dashboard">
    <div class="page-header">
      <h2>统计分析</h2>
      <div class="header-actions">
        <el-button-group>
          <el-button :type="timeRange === 'week' ? 'primary' : ''" @click="timeRange = 'week'">本周</el-button>
          <el-button :type="timeRange === 'month' ? 'primary' : ''" @click="timeRange = 'month'">本月</el-button>
          <el-button :type="timeRange === 'quarter' ? 'primary' : ''" @click="timeRange = 'quarter'">本季度</el-button>
          <el-button :type="timeRange === 'year' ? 'primary' : ''" @click="timeRange = 'year'">本年</el-button>
        </el-button-group>
        <el-date-picker
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleCustomDateRange"
        />
      </div>
    </div>
    
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409eff;">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.expertCount }}</div>
                <div class="stat-label">专家总数</div>
              </div>
            </div>
            <div class="stat-trend">
              <span class="trend-up">+{{ stats.expertGrowth }}%</span>
              <span>较上月</span>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67c23a;">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.projectCount }}</div>
                <div class="stat-label">进行中项目</div>
              </div>
            </div>
            <div class="stat-trend">
              <span class="trend-up">+{{ stats.projectGrowth }}%</span>
              <span>较上月</span>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6a23c;">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.skillCount }}</div>
                <div class="stat-label">技能领域</div>
              </div>
            </div>
            <div class="stat-trend">
              <span class="trend-up">+{{ stats.skillGrowth }}%</span>
              <span>较上月</span>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f56c6c;">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.matchRate }}%</div>
                <div class="stat-label">匹配成功率</div>
              </div>
            </div>
            <div class="stat-trend">
              <span class="trend-up">+{{ stats.matchGrowth }}%</span>
              <span>较上月</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>专家分布统计</h3>
                <el-select v-model="expertChartType" size="small" style="width: 120px">
                  <el-option label="按技能" value="skill" />
                  <el-option label="按状态" value="status" />
                  <el-option label="按公司" value="company" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里可以放置图表组件 -->
              <div class="chart-placeholder">
                <el-icon><PieChart /></el-icon>
                <p>专家分布图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>项目进度统计</h3>
                <el-select v-model="projectChartType" size="small" style="width: 120px">
                  <el-option label="按状态" value="status" />
                  <el-option label="按预算" value="budget" />
                  <el-option label="按时长" value="duration" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里可以放置图表组件 -->
              <div class="chart-placeholder">
                <el-icon><Histogram /></el-icon>
                <p>项目进度图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" class="mt-20">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>技能需求趋势</h3>
                <el-select v-model="skillChartType" size="small" style="width: 120px">
                  <el-option label="按热度" value="hot" />
                  <el-option label="按增长" value="growth" />
                  <el-option label="按匹配" value="match" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里可以放置图表组件 -->
              <div class="chart-placeholder">
                <el-icon><TrendCharts /></el-icon>
                <p>技能需求趋势图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>匹配成功率趋势</h3>
                <el-select v-model="matchChartType" size="small" style="width: 120px">
                  <el-option label="按周" value="week" />
                  <el-option label="按月" value="month" />
                  <el-option label="按季度" value="quarter" />
                </el-select>
              </div>
            </template>
            <div class="chart-container">
              <!-- 这里可以放置图表组件 -->
              <div class="chart-placeholder">
                <el-icon><DataLine /></el-icon>
                <p>匹配成功率趋势图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="tables-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card class="table-card" shadow="hover">
            <template #header>
              <h3>热门技能排行榜</h3>
            </template>
            <el-table :data="topSkills" style="width: 100%" height="300">
              <el-table-column prop="rank" label="排名" width="60">
                <template #default="{ row }">
                  <span class="rank-badge" :class="getRankClass(row.rank)">
                    {{ row.rank }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="技能名称" />
              <el-table-column prop="expertCount" label="专家数量" width="100" />
              <el-table-column prop="projectCount" label="项目数量" width="100" />
              <el-table-column prop="demandLevel" label="需求等级" width="100">
                <template #default="{ row }">
                  <el-rate v-model="row.demandLevel" disabled size="small" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="12">
          <el-card class="table-card" shadow="hover">
            <template #header>
              <h3>最近匹配记录</h3>
            </template>
            <el-table :data="recentMatches" style="width: 100%" height="300">
              <el-table-column prop="projectName" label="项目名称" />
              <el-table-column prop="expertName" label="专家姓名" width="120" />
              <el-table-column prop="matchScore" label="匹配度" width="100">
                <template #default="{ row }">
                  <el-tag :type="getMatchScoreType(row.matchScore)">
                    {{ row.matchScore }}%
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="matchDate" label="匹配时间" width="120" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getMatchStatusType(row.status)" size="small">
                    {{ getMatchStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { User, Document, Star, TrendCharts, PieChart, Histogram, DataLine } from '@element-plus/icons-vue'

const timeRange = ref('month')
const customDateRange = ref<string[]>([])

const expertChartType = ref('skill')
const projectChartType = ref('status')
const skillChartType = ref('hot')
const matchChartType = ref('month')

const stats = ref({
  expertCount: 156,
  expertGrowth: 12,
  projectCount: 28,
  projectGrowth: 8,
  skillCount: 45,
  skillGrowth: 5,
  matchRate: 85,
  matchGrowth: 3
})

const topSkills = ref([
  { rank: 1, name: '机器学习', expertCount: 25, projectCount: 18, demandLevel: 5 },
  { rank: 2, name: '深度学习', expertCount: 20, projectCount: 15, demandLevel: 5 },
  { rank: 3, name: '数据分析', expertCount: 18, projectCount: 15, demandLevel: 5 },
  { rank: 4, name: 'Vue.js', expertCount: 18, projectCount: 22, demandLevel: 4 },
  { rank: 5, name: 'React', expertCount: 14, projectCount: 20, demandLevel: 4 },
  { rank: 6, name: 'Java', expertCount: 22, projectCount: 18, demandLevel: 4 },
  { rank: 7, name: 'Python', expertCount: 30, projectCount: 25, demandLevel: 4 },
  { rank: 8, name: 'TypeScript', expertCount: 15, projectCount: 12, demandLevel: 4 },
  { rank: 9, name: '产品设计', expertCount: 12, projectCount: 10, demandLevel: 3 },
  { rank: 10, name: '市场营销', expertCount: 10, projectCount: 8, demandLevel: 3 }
])

const recentMatches = ref([
  { projectName: 'AI智能客服系统', expertName: '张明', matchScore: 92, matchDate: '2024-04-10', status: 'success' },
  { projectName: '大数据分析平台', expertName: '李华', matchScore: 88, matchDate: '2024-04-09', status: 'success' },
  { projectName: '移动端应用开发', expertName: '王强', matchScore: 76, matchDate: '2024-04-08', status: 'pending' },
  { projectName: '区块链金融系统', expertName: '赵敏', matchScore: 85, matchDate: '2024-04-07', status: 'success' },
  { projectName: '电商平台优化', expertName: '刘芳', matchScore: 81, matchDate: '2024-04-06', status: 'success' },
  { projectName: '智能家居系统', expertName: '陈伟', matchScore: 69, matchDate: '2024-04-05', status: 'failed' },
  { projectName: '医疗数据分析', expertName: '孙丽', matchScore: 94, matchDate: '2024-04-04', status: 'success' },
  { projectName: '教育平台开发', expertName: '周涛', matchScore: 78, matchDate: '2024-04-03', status: 'pending' }
])

const getRankClass = (rank: number) => {
  if (rank <= 3) return 'rank-gold'
  if (rank <= 6) return 'rank-silver'
  return 'rank-bronze'
}

const getMatchScoreType = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

const getMatchStatusType = (status: string) => {
  switch (status) {
    case 'success': return 'success'
    case 'pending': return 'warning'
    case 'failed': return 'danger'
    default: return 'info'
  }
}

const getMatchStatusText = (status: string) => {
  switch (status) {
    case 'success': return '成功'
    case 'pending': return '待确认'
    case 'failed': return '失败'
    default: return '未知'
  }
}

const handleCustomDateRange = () => {
  timeRange.value = 'custom'
  // 这里可以触发数据重新加载
}

watch(timeRange, (newRange) => {
  // 这里可以根据时间范围重新加载数据
  console.log('时间范围改变:', newRange)
})

watch(expertChartType, (newType) => {
  console.log('专家图表类型改变:', newType)
})

watch(projectChartType, (newType) => {
  console.log('项目图表类型改变:', newType)
})

watch(skillChartType, (newType) => {
  console.log('技能图表类型改变:', newType)
})

watch(matchChartType, (newType) => {
  console.log('匹配图表类型改变:', newType)
})
</script>

<style scoped>
.analysis-dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.trend-up {
  color: #67c23a;
  font-weight: 500;
}

.trend-down {
  color: #f56c6c;
  font-weight: 500;
}

.charts-section {
  margin-bottom: 30px;
}

.chart-card {
  margin-bottom: 20px;
  height: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #999;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #dcdfe6;
}

.chart-placeholder p {
  margin: 0;
  font-size: 14px;
}

.tables-section {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
  height: 100%;
}

.table-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.rank-gold {
  background: #ffd700;
  color: #333;
}

.rank-silver {
  background: #c0c0c0;
  color: #333;
}

.rank-bronze {
  background: #cd7f32;
  color: white;
}

.mt-20 {
  margin-top: 20px;
}
</style>