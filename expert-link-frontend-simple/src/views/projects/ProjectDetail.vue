<template>
  <div class="project-detail">
    <div class="page-header">
      <div class="header-left">
        <h2>项目详情</h2>
        <div class="project-status">
          <el-tag :type="getStatusType(project.status)" size="large">
            {{ getStatusText(project.status) }}
          </el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button-group>
          <el-button type="primary" @click="editProject">编辑项目</el-button>
          <el-button type="warning" @click="matchExperts">匹配专家</el-button>
          <el-button type="success" @click="exportProject">导出</el-button>
          <el-button type="danger" @click="deleteProject">删除</el-button>
        </el-button-group>
      </div>
    </div>

    <div class="project-content">
      <el-row :gutter="20">
        <!-- 左侧项目基本信息 -->
        <el-col :span="16">
          <el-card class="info-card">
            <template #header>
              <h3>项目基本信息</h3>
            </template>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">项目名称:</span>
                <span class="value">{{ project.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">项目编号:</span>
                <span class="value">{{ project.code }}</span>
              </div>
              <div class="info-item">
                <span class="label">客户名称:</span>
                <span class="value">{{ project.client }}</span>
              </div>
              <div class="info-item">
                <span class="label">项目类型:</span>
                <span class="value">{{ project.type }}</span>
              </div>
              <div class="info-item">
                <span class="label">预算金额:</span>
                <span class="value">{{ formatCurrency(project.budget) }}</span>
              </div>
              <div class="info-item">
                <span class="label">开始日期:</span>
                <span class="value">{{ formatDate(project.startDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">结束日期:</span>
                <span class="value">{{ formatDate(project.endDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">项目经理:</span>
                <span class="value">{{ project.manager }}</span>
              </div>
              <div class="info-item full-width">
                <span class="label">项目描述:</span>
                <div class="value description">{{ project.description }}</div>
              </div>
            </div>
          </el-card>

          <!-- 项目需求 -->
          <el-card class="requirements-card">
            <template #header>
              <h3>项目需求</h3>
            </template>
            <div class="requirements-list">
              <div v-for="(requirement, index) in project.requirements" :key="index" class="requirement-item">
                <div class="requirement-header">
                  <span class="requirement-title">{{ requirement.title }}</span>
                  <el-tag :type="getPriorityType(requirement.priority)" size="small">
                    {{ getPriorityText(requirement.priority) }}
                  </el-tag>
                </div>
                <div class="requirement-content">{{ requirement.description }}</div>
                <div class="requirement-skills">
                  <el-tag
                    v-for="skill in requirement.skills"
                    :key="skill"
                    size="small"
                    class="skill-tag"
                  >
                    {{ skill }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧项目状态和匹配专家 -->
        <el-col :span="8">
          <!-- 项目进度 -->
          <el-card class="progress-card">
            <template #header>
              <h3>项目进度</h3>
            </template>
            <div class="progress-info">
              <div class="progress-bar-container">
                <el-progress
                  :percentage="project.progress"
                  :stroke-width="16"
                  :color="getProgressColor(project.progress)"
                />
                <div class="progress-text">{{ project.progress }}%</div>
              </div>
              <div class="progress-stats">
                <div class="stat-item">
                  <span class="stat-label">已用时间:</span>
                  <span class="stat-value">{{ project.elapsedDays }} 天</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">剩余时间:</span>
                  <span class="stat-value">{{ project.remainingDays }} 天</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">已用预算:</span>
                  <span class="stat-value">{{ formatCurrency(project.spentBudget) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">剩余预算:</span>
                  <span class="stat-value">{{ formatCurrency(project.remainingBudget) }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 匹配专家 -->
          <el-card class="matched-experts-card">
            <template #header>
              <div class="card-header">
                <h3>匹配专家</h3>
                <el-button type="primary" size="small" @click="matchExperts">重新匹配</el-button>
              </div>
            </template>
            <div class="experts-list">
              <div v-for="expert in matchedExperts" :key="expert.id" class="expert-item">
                <div class="expert-info">
                  <el-avatar :size="40" :src="expert.avatar" />
                  <div class="expert-details">
                    <div class="expert-name">{{ expert.name }}</div>
                    <div class="expert-title">{{ expert.title }}</div>
                  </div>
                </div>
                <div class="expert-match">
                  <div class="match-score">
                    <el-progress
                      :percentage="expert.matchScore"
                      :stroke-width="8"
                      :show-text="false"
                      :color="getMatchScoreColor(expert.matchScore)"
                    />
                    <span class="score-text">{{ expert.matchScore }}%</span>
                  </div>
                  <div class="expert-actions">
                    <el-button type="text" size="small" @click="viewExpert(expert)">查看</el-button>
                    <el-button type="text" size="small" @click="contactExpert(expert)">联系</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 项目文档 -->
          <el-card class="documents-card">
            <template #header>
              <h3>项目文档</h3>
            </template>
            <div class="documents-list">
              <div v-for="doc in project.documents" :key="doc.id" class="document-item">
                <div class="document-info">
                  <el-icon class="document-icon"><Document /></el-icon>
                  <div class="document-details">
                    <div class="document-name">{{ doc.name }}</div>
                    <div class="document-meta">{{ formatDate(doc.uploadDate) }} · {{ doc.size }}</div>
                  </div>
                </div>
                <div class="document-actions">
                  <el-button type="text" size="small" @click="downloadDocument(doc)">下载</el-button>
                  <el-button type="text" size="small" @click="previewDocument(doc)">预览</el-button>
                </div>
              </div>
            </div>
            <div class="upload-section">
              <el-upload
                class="upload-demo"
                action="#"
                :on-change="handleDocumentUpload"
                :auto-upload="false"
                :show-file-list="false"
              >
                <el-button type="text" icon="Upload">上传文档</el-button>
              </el-upload>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 项目数据
const project = ref({
  id: 1,
  name: 'AI智能客服系统开发',
  code: 'PROJ-2024-001',
  client: '某科技公司',
  type: '软件开发',
  budget: 500000,
  spentBudget: 250000,
  remainingBudget: 250000,
  startDate: '2024-01-15',
  endDate: '2024-06-30',
  manager: '张经理',
  status: 'in_progress',
  progress: 50,
  elapsedDays: 90,
  remainingDays: 60,
  description: '开发一套基于人工智能的智能客服系统，包含自然语言处理、意图识别、对话管理等功能模块。',
  requirements: [
    {
      title: '自然语言处理模块',
      description: '实现中文分词、词性标注、命名实体识别等基础NLP功能',
      priority: 'high',
      skills: ['自然语言处理', 'Python', 'TensorFlow', '中文分词']
    },
    {
      title: '意图识别模块',
      description: '基于深度学习模型实现用户意图分类和识别',
      priority: 'high',
      skills: ['深度学习', '意图识别', '分类算法', 'PyTorch']
    },
    {
      title: '对话管理系统',
      description: '设计对话状态跟踪和对话策略管理',
      priority: 'medium',
      skills: ['对话系统', '状态管理', '策略设计', '强化学习']
    },
    {
      title: '前端界面开发',
      description: '开发用户友好的客服对话界面',
      priority: 'medium',
      skills: ['Vue.js', 'TypeScript', 'Element Plus', 'WebSocket']
    }
  ],
  documents: [
    { id: 1, name: '项目需求文档.pdf', uploadDate: '2024-01-10', size: '2.5 MB' },
    { id: 2, name: '技术方案设计.docx', uploadDate: '2024-01-20', size: '1.8 MB' },
    { id: 3, name: '项目进度报告.xlsx', uploadDate: '2024-04-01', size: '850 KB' }
  ]
})

// 匹配专家数据
const matchedExperts = ref([
  {
    id: 1,
    name: '王专家',
    title: '自然语言处理专家',
    avatar: '',
    matchScore: 92,
    skills: ['自然语言处理', 'Python', 'TensorFlow', '中文分词']
  },
  {
    id: 2,
    name: '李专家',
    title: '深度学习工程师',
    avatar: '',
    matchScore: 88,
    skills: ['深度学习', '意图识别', 'PyTorch', '分类算法']
  },
  {
    id: 3,
    name: '张专家',
    title: '对话系统架构师',
    avatar: '',
    matchScore: 85,
    skills: ['对话系统', '状态管理', '强化学习', '策略设计']
  },
  {
    id: 4,
    name: '陈专家',
    title: '前端开发专家',
    avatar: '',
    matchScore: 82,
    skills: ['Vue.js', 'TypeScript', 'Element Plus', 'WebSocket']
  }
])

// 状态类型映射
const getStatusType = (status: string) => {
  switch (status) {
    case 'planning': return 'info'
    case 'in_progress': return 'primary'
    case 'completed': return 'success'
    case 'cancelled': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'planning': return '规划中'
    case 'in_progress': return '进行中'
    case 'completed': return '已完成'
    case 'cancelled': return '已取消'
    default: return '未知'
  }
}

// 优先级类型映射
const getPriorityType = (priority: string) => {
  switch (priority) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'success'
    default: return 'info'
  }
}

const getPriorityText = (priority: string) => {
  switch (priority) {
    case 'high': return '高优先级'
    case 'medium': return '中优先级'
    case 'low': return '低优先级'
    default: return '未知'
  }
}

// 进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage < 30) return '#f56c6c'
  if (percentage < 70) return '#e6a23c'
  return '#67c23a'
}

// 匹配分数颜色
const getMatchScoreColor = (score: number) => {
  if (score < 60) return '#f56c6c'
  if (score < 80) return '#e6a23c'
  return '#67c23a'
}

// 格式化货币
const formatCurrency = (amount: number) => {
  return `¥${amount.toLocaleString()}`
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 编辑项目
const editProject = () => {
  router.push(`/projects/edit/${project.value.id}`)
}

// 匹配专家
const matchExperts = () => {
  ElMessage.info('开始匹配专家...')
  // 这里可以调用匹配算法
}

// 导出项目
const exportProject = () => {
  ElMessage.success('项目数据导出成功')
}

// 删除项目
const deleteProject = () => {
  ElMessageBox.confirm(
    '确定要删除这个项目吗？此操作不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    ElMessage.success('项目已删除')
    router.push('/projects')
  }).catch(() => {
    // 取消删除
  })
}

// 查看专家详情
const viewExpert = (expert: any) => {
  router.push(`/experts/${expert.id}`)
}

// 联系专家
const contactExpert = (expert: any) => {
  ElMessage.info(`联系专家: ${expert.name}`)
}

// 下载文档
const downloadDocument = (doc: any) => {
  ElMessage.info(`下载文档: ${doc.name}`)
}

// 预览文档
const previewDocument = (doc: any) => {
  ElMessage.info(`预览文档: ${doc.name}`)
}

// 上传文档
const handleDocumentUpload = (file: any) => {
  ElMessage.success(`文档 ${file.name} 上传成功`)
  // 这里可以添加上传逻辑
}

onMounted(() => {
  // 这里可以加载项目数据
  const projectId = route.params.id
  console.log('加载项目数据:', projectId)
})
</script>

<style scoped>
.project-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.project-content {
  margin-top: 20px;
}

.info-card,
.requirements-card,
.progress-card,
.matched-experts-card,
.documents-card {
  margin-bottom: 20px;
}

.info-card h3,
.requirements-card h3,
.progress-card h3,
.matched-experts-card h3,
.documents-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item .label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 14px;
  color: #333;
}

.info-item .description {
  line-height: 1.6;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-top: 4px;
}

.requirements-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.requirement-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.requirement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.requirement-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.requirement-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 12px;
}

.requirement-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin-right: 4px;
}

.progress-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-bar-container {
  position: relative;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.experts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.expert-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.expert-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.expert-details {
  flex: 1;
}

.expert-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.expert-title {
  font-size: 12px;
  color: #666;
}

.expert-match {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.match-score {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-text {
  font-size: 12px;
  font-weight: 600;
  color: #333;
  min-width: 40px;
}

.expert-actions {
  display: flex;
  gap: 8px;
}

.documents-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.document-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.document-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.document-icon {
  font-size: 20px;
  color: #409eff;
}

.document-details {
  flex: 1;
}

.document-name {
  font-size: 13px;
  color: #333;
}

.document-meta {
  font-size: 11px;
  color: #999;
}

.document-actions {
  display: flex;
  gap: 8px;
}

.upload-section {
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}
</style>