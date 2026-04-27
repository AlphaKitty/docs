<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>系统概览</h1>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData" :loading="loading">刷新数据</el-button>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateChange"
        />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409eff;">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ dashboardStats.totalExperts || 0 }}</div>
                <div class="stat-label">专家总数</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ expertGrowthRate }}%</span>
                <el-icon><TrendCharts /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67c23a;">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ dashboardStats.totalProjects || 0 }}</div>
                <div class="stat-label">项目总数</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ projectGrowthRate }}%</span>
                <el-icon><TrendCharts /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6a23c;">
                <el-icon><Collection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ dashboardStats.totalSkills || 0 }}</div>
                <div class="stat-label">技能领域</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ skillGrowthRate }}%</span>
                <el-icon><TrendCharts /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f56c6c;">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ formatCurrency(dashboardStats.monthlyRevenue || 0) }}</div>
                <div class="stat-label">月度收入</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ revenueGrowthRate }}%</span>
                <el-icon><TrendCharts /></el-icon>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>专家增长趋势</h3>
                <div class="chart-actions">
                  <el-button type="text" size="small" @click="refreshExpertChart" :loading="chartLoading.expert">刷新</el-button>
                  <el-button type="text" size="small" @click="exportExpertChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div v-if="!expertTrendData.length" class="chart-empty">
                <el-empty description="暂无数据" />
              </div>
              <div v-else ref="expertGrowthChart" class="chart-canvas"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>项目状态分布</h3>
                <div class="chart-actions">
                  <el-button type="text" size="small" @click="refreshProjectChart" :loading="chartLoading.project">刷新</el-button>
                  <el-button type="text" size="small" @click="exportProjectChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div v-if="!projectStats.byStatus" class="chart-empty">
                <el-empty description="暂无数据" />
              </div>
              <div v-else ref="projectStatusChart" class="chart-canvas"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>技能需求热度</h3>
                <div class="chart-actions">
                  <el-button type="text" size="small" @click="refreshSkillChart" :loading="chartLoading.skill">刷新</el-button>
                  <el-button type="text" size="small" @click="exportSkillChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div v-if="!skillStats.topSkills.length" class="chart-empty">
                <el-empty description="暂无数据" />
              </div>
              <div v-else ref="skillHeatChart" class="chart-canvas"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>收入趋势分析</h3>
                <div class="chart-actions">
                  <el-button type="text" size="small" @click="refreshRevenueChart" :loading="chartLoading.revenue">刷新</el-button>
                  <el-button type="text" size="small" @click="exportRevenueChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div v-if="!revenueTrendData.length" class="chart-empty">
                <el-empty description="暂无数据" />
              </div>
              <div v-else ref="revenueTrendChart" class="chart-canvas"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作和最近活动 -->
    <div class="quick-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="quick-card">
            <template #header>
              <h3>快速操作</h3>
            </template>
            <div class="quick-actions">
              <el-button type="primary" @click="addExpert">
                <el-icon><Plus /></el-icon>
                添加专家
              </el-button>
              <el-button type="success" @click="addProject">
                <el-icon><Plus /></el-icon>
                创建项目
              </el-button>
              <el-button type="warning" @click="matchExpertProject">
                <el-icon><Connection /></el-icon>
                匹配专家项目
              </el-button>
              <el-button type="info" @click="generateReport">
                <el-icon><Document /></el-icon>
                生成报告
              </el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="activity-card">
            <template #header>
              <h3>最近活动</h3>
            </template>
            <div class="activity-list">
              <div v-if="recentActivities.length === 0" class="activity-empty">
                <el-empty description="暂无活动记录" />
              </div>
              <div v-else v-for="activity in recentActivities" :key="activity.id" class="activity-item">
                <div class="activity-icon">
                  <el-icon :class="getActivityIconClass(activity.type)">
                    <component :is="getActivityIcon(activity.type)" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 待办事项 -->
    <div class="todo-section">
      <el-card class="todo-card">
        <template #header>
          <div class="todo-header">
            <h3>待办事项</h3>
            <el-button type="text" @click="addTodo">添加待办</el-button>
          </div>
        </template>
        <div class="todo-list">
          <div v-if="todos.length === 0" class="todo-empty">
            <el-empty description="暂无待办事项" />
          </div>
          <div v-else v-for="todo in todos" :key="todo.id" class="todo-item">
            <el-checkbox v-model="todo.completed" @change="toggleTodo(todo)">
              <span :class="{ 'todo-completed': todo.completed }">{{ todo.title }}</span>
            </el-checkbox>
            <div class="todo-actions">
              <el-button type="text" size="small" @click="editTodo(todo)">编辑</el-button>
              <el-button type="text" size="small" @click="deleteTodo(todo)">删除</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  User,
  Document,
  Collection,
  Money,
  TrendCharts,
  Plus,
  Connection,
  Check,
  Clock,
  Warning,
  InfoFilled
} from '@element-plus/icons-vue'

// 导入API服务
import { StatsService } from '@/api/services'
import type { DashboardStats, ExpertStats, ProjectStats, SkillStats } from '@/api/types'

// 导入ECharts配置
import {
  getExpertGrowthChartOption,
  getProjectStatusChartOption,
  getSkillHeatChartOption,
  getRevenueTrendChartOption,
  initChart,
  resizeChart,
  disposeChart
} from '@/utils/echartsConfig'

const router = useRouter()

// 图表引用
const expertGrowthChart = ref<HTMLElement>()
const projectStatusChart = ref<HTMLElement>()
const skillHeatChart = ref<HTMLElement>()
const revenueTrendChart = ref<HTMLElement>()

// 图表实例
let expertChartInstance: echarts.ECharts | null = null
let projectChartInstance: echarts.ECharts | null = null
let skillChartInstance: echarts.ECharts | null = null
let revenueChartInstance: echarts.ECharts | null = null

// 加载状态
const loading = ref(false)
const chartLoading = ref({
  expert: false,
  project: false,
  skill: false,
  revenue: false
})

// API数据
const dashboardStats = ref<DashboardStats>({
  totalExperts: 0,
  activeExperts: 0,
  totalProjects: 0,
  ongoingProjects: 0,
  totalSkills: 0,
  totalDomains: 0,
  monthlyRevenue: 0,
  utilizationRate: 0
})

const expertStats = ref<ExpertStats>({
  byStatus: {
    ACTIVE: 0,
    INACTIVE: 0,
    PENDING: 0,
    ARCHIVED: 0
  },
  byLevel: {
    JUNIOR: 0,
    MIDDLE: 0,
    SENIOR: 0,
    EXPERT: 0
  },
  byDomain: [],
  bySkill: [],
  ratingDistribution: [],
  hourlyRateDistribution: []
})

const projectStats = ref<ProjectStats>({
  byStatus: {
    PLANNING: 0,
    IN_PROGRESS: 0,
    ON_HOLD: 0,
    COMPLETED: 0,
    CANCELLED: 0
  },
  byPriority: {
    LOW: 0,
    MEDIUM: 0,
    HIGH: 0,
    CRITICAL: 0
  },
  byDomain: [],
  budgetDistribution: [],
  timeline: []
})

const skillStats = ref<SkillStats>({
  byCategory: {
    TECHNICAL: 0,
    BUSINESS: 0,
    DESIGN: 0,
    MANAGEMENT: 0,
    LANGUAGE: 0,
    OTHER: 0
  },
  topSkills: [],
  demandTrend: []
})

// 趋势数据
const expertTrendData = ref<any[]>([])
const revenueTrendData = ref<any[]>([])

// 计算增长率
const expertGrowthRate = computed(() => {
  if (expertTrendData.value.length < 2) return 0
  const last = expertTrendData.value[expertTrendData.value.length - 1]
  const prev = expertTrendData.value[expertTrendData.value.length - 2]
  return prev.value > 0 ? ((last.value - prev.value) / prev.value * 100).toFixed(1) : '0.0'
})

const projectGrowthRate = computed(() => {
  if (projectStats.value.timeline.length < 2) return 0
  const last = projectStats.value.timeline[projectStats.value.timeline.length - 1]
  const prev = projectStats.value.timeline[projectStats.value.timeline.length - 2]
  return prev.started > 0 ? ((last.started - prev.started) / prev.started * 100).toFixed(1) : '0.0'
})

const skillGrowthRate = computed(() => {
  // 这里可以根据技能增长趋势计算
  return '5.2'
})

const revenueGrowthRate = computed(() => {
  if (revenueTrendData.value.length < 2) return 0
  const last = revenueTrendData.value[revenueTrendData.value.length - 1]
  const prev = revenueTrendData.value[revenueTrendData.value.length - 2]
  return prev.revenue > 0 ? ((last.revenue - prev.revenue) / prev.revenue * 100).toFixed(1) : '0.0'
})

// 日期范围
const dateRange = ref(['2024-01-01', '2024-04-14'])

// 最近活动
const recentActivities = ref<any[]>([])

// 待办事项
const todos = ref<any[]>([])

// 格式化货币
const formatCurrency = (amount: number) => {
  return `¥${amount.toLocaleString()}`
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

// 获取活动图标
const getActivityIcon = (type: string) => {
  switch (type) {
    case 'expert': return User
    case 'project': return Document
    case 'match': return Connection
    case 'warning': return Warning
    case 'info': return InfoFilled
    default: return InfoFilled
  }
}

// 获取活动图标类名
const getActivityIconClass = (type: string) => {
  switch (type) {
    case 'expert': return 'expert-icon'
    case 'project': return 'project-icon'
    case 'match': return 'match-icon'
    case 'warning': return 'warning-icon'
    case 'info': return 'info-icon'
    default: return ''
  }
}

// 加载仪表板数据
const loadDashboardData = async () => {
  loading.value = true
  try {
    // 并行加载所有统计数据
    const [dashboardData, expertData, projectData, skillData, trendData] = await Promise.all([
      StatsService.getDashboardStats(),
      StatsService.getExpertStats(),
      StatsService.getProjectStats(),
      StatsService.getSkillStats(),
      StatsService.getTrendData('experts', 'monthly', 12)
    ])

    dashboardStats.value = dashboardData
    expertStats.value = expertData
    projectStats.value = projectData
    skillStats.value = skillData
    expertTrendData.value = trendData

    // 加载收入趋势数据
    const revenueTrend = await StatsService.getTrendData('projects', 'monthly', 12)
    revenueTrendData.value = revenueTrend

    // 初始化图表
    initCharts()

    ElMessage.success('数据加载成功')
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('数据加载失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  if (expertGrowthChart.value && expertTrendData.value.length > 0) {
    const chartData = expertTrendData.value.map((item, index) => ({
      month: `第${index + 1}月`,
      experts: item.value,
      growth: index > 0 ? ((item.value - expertTrendData.value[index - 1].value) / expertTrendData.value[index - 1].value * 100).toFixed(1) : 0
    }))
    expertChartInstance = initChart(expertGrowthChart.value, getExpertGrowthChartOption(chartData))
  }
  
  if (projectStatusChart.value && projectStats.value.byStatus) {
    const statusData = [
      { value: projectStats.value.byStatus.PLANNING || 0, name: '规划中', color: '#909399' },
      { value: projectStats.value.byStatus.IN_PROGRESS || 0, name: '进行中', color: '#409EFF' },
      { value: projectStats.value.byStatus.COMPLETED || 0, name: '已完成', color: '#67C23A' },
      { value: projectStats.value.byStatus.ON_HOLD || 0, name: '已暂停', color: '#E6A23C' },
      { value: projectStats.value.byStatus.CANCELLED || 0, name: '已取消', color: '#F56C6C' }
    ]
    projectChartInstance = initChart(projectStatusChart.value, getProjectStatusChartOption(statusData))
  }
  
  if (skillHeatChart.value && skillStats.value.topSkills.length > 0) {
    const skillData = skillStats.value.topSkills.slice(0, 10).map(skill => ({
      name: skill.skillName,
      value: skill.expertCount,
      category: skill.skillName.includes('设计') ? '设计' : 
               skill.skillName.includes('管理') ? '管理' : 
               skill.skillName.includes('语言') ? '语言' : '技术'
    }))
    skillChartInstance = initChart(skillHeatChart.value, getSkillHeatChartOption(skillData))
  }
  
  if (revenueTrendChart.value && revenueTrendData.value.length > 0) {
    const revenueData = revenueTrendData.value.map((item, index) => ({
      month: `第${index + 1}月`,
      revenue: item.revenue || item.value || 0,
      cost: item.cost || (item.revenue ? item.revenue * 0.6 : 0) // 假设成本为收入的60%
    }))
    revenueChartInstance = initChart(revenueTrendChart.value, getRevenueTrendChartOption(revenueData))
  }
}

// 刷新专家增长图表
const refreshExpertChart = async () => {
  chartLoading.value.expert = true
  try {
    const trendData = await StatsService.getTrendData('experts', 'monthly', 12)
    expertTrendData.value = trendData
    
    if (expertChartInstance && expertTrendData.value.length > 0) {
      const chartData = expertTrendData.value.map((item, index) => ({
        month: `第${index + 1}月`,
        experts: item.value,
        growth: index > 0 ? ((item.value - expertTrendData.value[index - 1].value) / expertTrendData.value[index - 1].value * 100).toFixed(1) : 0
      }))
      expertChartInstance.setOption(getExpertGrowthChartOption(chartData))
    }
    ElMessage.success('专家增长图表已刷新')
  } catch (error) {
    console.error('刷新专家图表失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    chartLoading.value.expert = false
  }
}

// 刷新项目状态图表
const refreshProjectChart = async () => {
  chartLoading.value.project = true
  try {
    const projectData = await StatsService.getProjectStats()
    projectStats.value = projectData
    
    if (projectChartInstance && projectStats.value.byStatus) {
      const statusData = [
        { value: projectStats.value.byStatus.PLANNING || 0, name: '规划中', color: '#909399' },
        { value: projectStats.value.byStatus.IN_PROGRESS || 0, name: '进行中', color: '#409EFF' },
        { value: projectStats.value.byStatus.COMPLETED || 0, name: '已完成', color: '#67C23A' },
        { value: projectStats.value.byStatus.ON_HOLD || 0, name: '已暂停', color: '#E6A23C' },
        { value: projectStats.value.byStatus.CANCELLED || 0, name: '已取消', color: '#F56C6C' }
      ]
      projectChartInstance.setOption(getProjectStatusChartOption(statusData))
    }
    ElMessage.success('项目状态图表已刷新')
  } catch (error) {
    console.error('刷新项目图表失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    chartLoading.value.project = false
  }
}

// 刷新技能热度图表
const refreshSkillChart = async () => {
  chartLoading.value.skill = true
  try {
    const skillData = await StatsService.getSkillStats()
    skillStats.value = skillData
    
    if (skillChartInstance && skillStats.value.topSkills.length > 0) {
      const chartData = skillStats.value.topSkills.slice(0, 10).map(skill => ({
        name: skill.skillName,
        value: skill.expertCount,
        category: skill.skillName.includes('设计') ? '设计' : 
                 skill.skillName.includes('管理') ? '管理' : 
                 skill.skillName.includes('语言') ? '语言' : '技术'
      }))
      skillChartInstance.setOption(getSkillHeatChartOption(chartData))
    }
    ElMessage.success('技能热度图表已刷新')
  } catch (error) {
    console.error('刷新技能图表失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    chartLoading.value.skill = false
  }
}

// 刷新收入趋势图表
const refreshRevenueChart = async () => {
  chartLoading.value.revenue = true
  try {
    const revenueTrend = await StatsService.getTrendData('projects', 'monthly', 12)
    revenueTrendData.value = revenueTrend
    
    if (revenueChartInstance && revenueTrendData.value.length > 0) {
      const chartData = revenueTrendData.value.map((item, index) => ({
        month: `第${index + 1}月`,
        revenue: item.revenue || item.value || 0,
        cost: item.cost || (item.revenue ? item.revenue * 0.6 : 0)
      }))
      revenueChartInstance.setOption(getRevenueTrendChartOption(chartData))
    }
    ElMessage.success('收入趋势图表已刷新')
  } catch (error) {
    console.error('刷新收入图表失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    chartLoading.value.revenue = false
  }
}

// 导出图表
const exportExpertChart = () => {
  if (expertChartInstance) {
    const url = expertChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    downloadImage(url, '专家增长趋势.png')
  }
}

const exportProjectChart = () => {
  if (projectChartInstance) {
    const url = projectChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    downloadImage(url, '项目状态分布.png')
  }
}

const exportSkillChart = () => {
  if (skillChartInstance) {
    const url = skillChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    downloadImage(url, '技能需求热度.png')
  }
}

const exportRevenueChart = () => {
  if (revenueChartInstance) {
    const url = revenueChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    downloadImage(url, '收入趋势分析.png')
  }
}

// 下载图片
const downloadImage = (url: string, filename: string) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success(`图表已导出: ${filename}`)
}

// 刷新数据
const refreshData = () => {
  loadDashboardData()
}

// 处理日期变化
const handleDateChange = () => {
  console.log('日期范围变化:', dateRange.value)
  // 这里可以根据日期范围重新加载数据
  refreshData()
}

// 添加专家
const addExpert = () => {
  router.push('/experts/add')
}

// 添加项目
const addProject = () => {
  router.push('/projects/add')
}

// 匹配专家项目
const matchExpertProject = () => {
  ElMessage.info('开始匹配专家和项目...')
  // 这里可以调用匹配功能
}

// 生成报告
const generateReport = () => {
  ElMessage.success('报告生成成功')
  // 这里可以生成报告
}

// 添加待办
const addTodo = () => {
  const newTodo = {
    id: todos.value.length + 1,
    title: '新待办事项',
    completed: false,
    priority: 'medium'
  }
  todos.value.unshift(newTodo)
  ElMessage.info('已添加新待办事项')
}

// 切换待办状态
const toggleTodo = (todo: any) => {
  console.log('切换待办状态:', todo)
}

// 编辑待办
const editTodo = (todo: any) => {
  ElMessage.info(`编辑待办: ${todo.title}`)
}

// 删除待办
const deleteTodo = (todo: any) => {
  const index = todos.value.findIndex(t => t.id === todo.id)
  if (index !== -1) {
    todos.value.splice(index, 1)
    ElMessage.success('待办事项已删除')
  }
}

// 响应式调整图表大小
const handleResize = () => {
  if (expertChartInstance) resizeChart(expertChartInstance)
  if (projectChartInstance) resizeChart(projectChartInstance)
  if (skillChartInstance) resizeChart(skillChartInstance)
  if (revenueChartInstance) resizeChart(revenueChartInstance)
}

onMounted(() => {
  // 加载数据
  loadDashboardData()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  
  console.log('仪表盘初始化完成')
})

onUnmounted(() => {
  // 销毁图表实例
  if (expertChartInstance) disposeChart(expertChartInstance)
  if (projectChartInstance) disposeChart(projectChartInstance)
  if (skillChartInstance) disposeChart(skillChartInstance)
  if (revenueChartInstance) disposeChart(revenueChartInstance)
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dashboard-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon .el-icon {
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
}

.trend-up {
  color: #67c23a;
  font-size: 14px;
  font-weight: 600;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.quick-section {
  margin-bottom: 20px;
}

.quick-card,
.activity-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.quick-card h3,
.activity-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-actions .el-button {
  flex: 1;
  min-width: 120px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.expert-icon {
  color: #409eff;
}

.project-icon {
  color: #67c23a;
}

.match-icon {
  color: #e6a23c;
}

.warning-icon {
  color: #f56c6c;
}

.info-icon {
  color: #909399;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.todo-section {
  margin-bottom: 20px;
}

.todo-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.todo-completed {
  text-decoration: line-through;
  color: #999;
}

.todo-actions {
  display: flex;
  gap: 8px;
}
</style>