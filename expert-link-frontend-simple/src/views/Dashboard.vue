<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>系统概览</h1>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">刷新数据</el-button>
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
                <div class="stat-value">{{ stats.totalExperts }}</div>
                <div class="stat-label">专家总数</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ stats.expertGrowth }}%</span>
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
                <div class="stat-value">{{ stats.totalProjects }}</div>
                <div class="stat-label">项目总数</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ stats.projectGrowth }}%</span>
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
                <div class="stat-value">{{ stats.totalSkills }}</div>
                <div class="stat-label">技能领域</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ stats.skillGrowth }}%</span>
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
                <div class="stat-value">{{ formatCurrency(stats.totalRevenue) }}</div>
                <div class="stat-label">累计收入</div>
              </div>
              <div class="stat-trend">
                <span class="trend-up">+{{ stats.revenueGrowth }}%</span>
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
                  <el-button type="text" size="small" @click="refreshExpertChart">刷新</el-button>
                  <el-button type="text" size="small" @click="exportExpertChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div ref="expertGrowthChart" class="chart-canvas"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>项目状态分布</h3>
                <div class="chart-actions">
                  <el-button type="text" size="small" @click="refreshProjectChart">刷新</el-button>
                  <el-button type="text" size="small" @click="exportProjectChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div ref="projectStatusChart" class="chart-canvas"></div>
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
                  <el-button type="text" size="small" @click="refreshSkillChart">刷新</el-button>
                  <el-button type="text" size="small" @click="exportSkillChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div ref="skillHeatChart" class="chart-canvas"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>收入趋势分析</h3>
                <div class="chart-actions">
                  <el-button type="text" size="small" @click="refreshRevenueChart">刷新</el-button>
                  <el-button type="text" size="small" @click="exportRevenueChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="chart-container">
              <div ref="revenueTrendChart" class="chart-canvas"></div>
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
              <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
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
          <div v-for="todo in todos" :key="todo.id" class="todo-item">
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
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
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

// 统计数据
const stats = ref({
  totalExperts: 156,
  expertGrowth: 12.5,
  totalProjects: 48,
  projectGrowth: 8.3,
  totalSkills: 23,
  skillGrowth: 5.2,
  totalRevenue: 2850000,
  revenueGrowth: 15.7
})

// 日期范围
const dateRange = ref(['2024-01-01', '2024-04-14'])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'expert',
    title: '新专家加入',
    description: '王专家已加入系统',
    time: '2024-04-14 10:30:00'
  },
  {
    id: 2,
    type: 'project',
    title: '项目状态更新',
    description: 'AI智能客服系统项目进度更新为50%',
    time: '2024-04-13 15:20:00'
  },
  {
    id: 3,
    type: 'match',
    title: '专家匹配成功',
    description: '李专家成功匹配到智能文本分析平台项目',
    time: '2024-04-12 09:45:00'
  },
  {
    id: 4,
    type: 'warning',
    title: '项目延期提醒',
    description: '知识图谱构建项目即将到期',
    time: '2024-04-11 14:10:00'
  },
  {
    id: 5,
    type: 'info',
    title: '系统更新',
    description: '系统已完成月度维护',
    time: '2024-04-10 18:00:00'
  }
])

// 待办事项
const todos = ref([
  {
    id: 1,
    title: '审核新专家资料',
    completed: false,
    priority: 'high'
  },
  {
    id: 2,
    title: '跟进项目进度',
    completed: false,
    priority: 'medium'
  },
  {
    id: 3,
    title: '更新技能分类',
    completed: true,
    priority: 'low'
  },
  {
    id: 4,
    title: '准备月度报告',
    completed: false,
    priority: 'high'
  },
  {
    id: 5,
    title: '联系客户确认需求',
    completed: false,
    priority: 'medium'
  }
])

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

// 初始化图表
const initCharts = () => {
  if (expertGrowthChart.value) {
    expertChartInstance = initChart(expertGrowthChart.value, getExpertGrowthChartOption())
  }
  
  if (projectStatusChart.value) {
    projectChartInstance = initChart(projectStatusChart.value, getProjectStatusChartOption())
  }
  
  if (skillHeatChart.value) {
    skillChartInstance = initChart(skillHeatChart.value, getSkillHeatChartOption())
  }
  
  if (revenueTrendChart.value) {
    revenueChartInstance = initChart(revenueTrendChart.value, getRevenueTrendChartOption())
  }
}

// 刷新专家增长图表
const refreshExpertChart = () => {
  if (expertChartInstance) {
    // 这里可以调用API获取最新数据
    const newData = [
      { month: '1月', experts: 120, growth: 5 },
      { month: '2月', experts: 125, growth: 4.2 },
      { month: '3月', experts: 132, growth: 5.6 },
      { month: '4月', experts: 142, growth: 7.6 },
      { month: '5月', experts: 148, growth: 4.2 },
      { month: '6月', experts: 156, growth: 5.4 },
      { month: '7月', experts: 162, growth: 3.8 },
      { month: '8月', experts: 170, growth: 4.9 },
      { month: '9月', experts: 178, growth: 4.7 },
      { month: '10月', experts: 185, growth: 3.9 },
      { month: '11月', experts: 192, growth: 3.8 },
      { month: '12月', experts: 200, growth: 4.2 }
    ]
    expertChartInstance.setOption(getExpertGrowthChartOption(newData))
    ElMessage.success('专家增长图表已刷新')
  }
}

// 刷新项目状态图表
const refreshProjectChart = () => {
  if (projectChartInstance) {
    // 这里可以调用API获取最新数据
    const newData = [
      { value: 15, name: '规划中', color: '#909399' },
      { value: 22, name: '进行中', color: '#409EFF' },
      { value: 12, name: '已完成', color: '#67C23A' },
      { value: 6, name: '已暂停', color: '#E6A23C' },
      { value: 4, name: '已取消', color: '#F56C6C' }
    ]
    projectChartInstance.setOption(getProjectStatusChartOption(newData))
    ElMessage.success('项目状态图表已刷新')
  }
}

// 刷新技能热度图表
const refreshSkillChart = () => {
  if (skillChartInstance) {
    // 这里可以调用API获取最新数据
    const newData = [
      { name: 'Vue.js', value: 98, category: '前端开发' },
      { name: 'React', value: 92, category: '前端开发' },
      { name: 'Spring Boot', value: 95, category: '后端开发' },
      { name: 'Python', value: 88, category: '后端开发' },
      { name: 'Docker', value: 82, category: 'DevOps' },
      { name: 'Kubernetes', value: 78, category: 'DevOps' },
      { name: 'MySQL', value: 85, category: '数据库' },
      { name: 'Redis', value: 80, category: '数据库' },
      { name: 'UI设计', value: 72, category: '设计' },
      { name: '产品管理', value: 68, category: '管理' }
    ]
    skillChartInstance.setOption(getSkillHeatChartOption(newData))
    ElMessage.success('技能热度图表已刷新')
  }
}

// 刷新收入趋势图表
const refreshRevenueChart = () => {
  if (revenueChartInstance) {
    // 这里可以调用API获取最新数据
    const newData = [
      { month: '1月', revenue: 180000, cost: 120000 },
      { month: '2月', revenue: 210000, cost: 135000 },
      { month: '3月', revenue: 240000, cost: 150000 },
      { month: '4月', revenue: 285000, cost: 165000 },
      { month: '5月', revenue: 320000, cost: 180000 },
      { month: '6月', revenue: 350000, cost: 195000 },
      { month: '7月', revenue: 380000, cost: 210000 },
      { month: '8月', revenue: 410000, cost: 225000 }
    ]
    revenueChartInstance.setOption(getRevenueTrendChartOption(newData))
    ElMessage.success('收入趋势图表已刷新')
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
  refreshExpertChart()
  refreshProjectChart()
  refreshSkillChart()
  refreshRevenueChart()
  ElMessage.success('所有图表数据已刷新')
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
  // 初始化图表
  nextTick(() => {
    initCharts()
  })
  
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