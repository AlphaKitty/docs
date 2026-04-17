<template>
  <div class="expert-detail">
    <div class="page-header">
      <div class="header-left">
        <h2>专家详情</h2>
        <div class="expert-status">
          <el-tag :type="getStatusType(expert.status)" size="large">
            {{ getStatusText(expert.status) }}
          </el-tag>
        </div>
      </div>
      <div class="header-right">
        <el-button-group>
          <el-button type="primary" @click="editExpert">编辑专家</el-button>
          <el-button type="success" @click="matchProjects">匹配项目</el-button>
          <el-button type="warning" @click="exportExpert">导出资料</el-button>
          <!-- <el-button type="danger" @click="deleteExpert">删除</el-button> -->
        </el-button-group>
      </div>
    </div>

    <div class="expert-content">
      <el-row :gutter="20">
        <!-- 左侧专家基本信息 -->
        <el-col :span="16">
          <!-- 个人信息卡片 -->
          <el-card class="info-card">
            <template #header>
              <h3>个人信息</h3>
            </template>
            <div class="personal-info">
              <div class="avatar-section">
                <el-avatar :size="120" :src="expert.avatar" />
                <div class="avatar-actions">
                  <el-button type="text" size="small" @click="changeAvatar">更换头像</el-button>
                  <input
                    ref="avatarFileInputRef"
                    type="file"
                    accept="image/*"
                    class="avatar-file-input"
                    @change="onAvatarFileChange"
                  />
                </div>
              </div>
              <div class="info-section">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="label">姓名:</span>
                    <span class="value">{{ expert.name }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">性别:</span>
                    <span class="value">{{ expert.gender }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">年龄:</span>
                    <span class="value">{{ expert.age }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">职称:</span>
                    <span class="value">{{ expert.title }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">学历:</span>
                    <span class="value">{{ expert.education }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">毕业院校:</span>
                    <span class="value">{{ expert.school }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">工作年限:</span>
                    <span class="value">{{ expert.workYears }} 年</span>
                  </div>
                  <div class="info-item">
                    <span class="label">联系电话:</span>
                    <span class="value">{{ expert.phone }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">电子邮箱:</span>
                    <span class="value">{{ expert.email }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">所在城市:</span>
                    <span class="value">{{ expert.city }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">期望薪资:</span>
                    <span class="value">{{ formatCurrency(expert.expectedSalary) }}/月</span>
                  </div>
                  <div class="info-item">
                    <span class="label">可工作时间:</span>
                    <span class="value">{{ expert.availability }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 专业技能 -->
          <el-card class="skills-card">
            <template #header>
              <div class="skills-header">
                <h3>专业技能</h3>
                <div class="skills-actions">
                  <el-button type="text" size="small" @click="refreshRadarChart">刷新</el-button>
                  <el-button type="text" size="small" @click="exportRadarChart">导出</el-button>
                </div>
              </div>
            </template>
            <div class="skills-content">
              <div class="skills-layout">
                <!-- 技能雷达图 -->
                <div class="radar-chart-section">
                  <div class="chart-header">
                    <h4>技能雷达图</h4>
                    <span class="chart-subtitle">各技能领域能力评估</span>
                  </div>
                  <div class="chart-container">
                    <div ref="skillRadarChart" class="chart-canvas"></div>
                  </div>
                </div>
                
                <!-- 技能分类列表 -->
                <div class="skill-categories-section">
                  <div class="categories-header">
                    <h4>技能分类</h4>
                    <span class="categories-count">{{ expert.skillCategories.length }} 个分类</span>
                  </div>
                  <div class="skill-categories">
                    <div v-for="category in expert.skillCategories" :key="category.name" class="category-item">
                      <div class="category-header">
                        <h5>{{ category.name }}</h5>
                        <span class="category-level">{{ category.level }}</span>
                      </div>
                      <div class="skill-tags">
                        <el-tag
                          v-for="skill in category.skills"
                          :key="skill.name"
                          :type="getSkillType(skill.level)"
                          size="default"
                          class="skill-tag"
                        >
                          {{ skill.name }}
                          <span class="skill-level">({{ skill.level }})</span>
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 工作经历 -->
          <el-card class="experience-card">
            <template #header>
              <h3>工作经历</h3>
            </template>
            <div class="experience-list">
              <div v-for="exp in expert.experiences" :key="exp.id" class="experience-item">
                <div class="experience-header">
                  <div class="company-info">
                    <span class="company-name">{{ exp.company }}</span>
                    <span class="company-position">{{ exp.position }}</span>
                  </div>
                  <div class="experience-period">
                    {{ formatDate(exp.startDate) }} - {{ exp.endDate ? formatDate(exp.endDate) : '至今' }}
                  </div>
                </div>
                <div class="experience-content">
                  <div class="experience-description">{{ exp.description }}</div>
                  <div class="experience-projects">
                    <div class="projects-title">参与项目:</div>
                    <div class="projects-list">
                      <el-tag
                        v-for="project in exp.projects"
                        :key="project"
                        size="small"
                        class="project-tag"
                      >
                        {{ project }}
                      </el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧专家状态和匹配项目 -->
        <el-col :span="8">
          <!-- 专家状态 -->
          <el-card class="status-card">
            <template #header>
              <h3>专家状态</h3>
            </template>
            <div class="status-info">
              <div class="status-stats">
                <div class="stat-item">
                  <span class="stat-label">匹配成功率:</span>
                  <div class="stat-value">
                    <el-progress
                      :percentage="expert.matchRate"
                      :stroke-width="12"
                      :color="getMatchRateColor(expert.matchRate)"
                    />
                    <span class="percentage">{{ expert.matchRate }}%</span>
                  </div>
                </div>
                <div class="stat-item">
                  <span class="stat-label">项目完成数:</span>
                  <span class="stat-value">{{ expert.completedProjects }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均评分:</span>
                  <div class="stat-value">
                    <el-rate
                      v-model="expert.averageRating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template="{value} 分"
                    />
                  </div>
                </div>
                <div class="stat-item">
                  <span class="stat-label">活跃度:</span>
                  <div class="stat-value">
                    <el-progress
                      :percentage="expert.activityLevel"
                      :stroke-width="12"
                      :color="getActivityColor(expert.activityLevel)"
                    />
                    <span class="percentage">{{ expert.activityLevel }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 匹配项目 -->
          <el-card class="matched-projects-card">
            <template #header>
              <div class="card-header">
                <h3>匹配项目</h3>
                <el-button type="primary" size="small" @click="matchProjects">重新匹配</el-button>
              </div>
            </template>
            <div class="projects-list">
              <div v-for="project in matchedProjects" :key="project.id" class="project-item">
                <div class="project-info">
                  <div class="project-name">{{ project.name }}</div>
                  <div class="project-meta">
                    <span class="project-client">{{ project.client }}</span>
                    <span class="project-budget">{{ formatCurrency(project.budget) }}</span>
                  </div>
                </div>
                <div class="project-match">
                  <div class="match-score">
                    <el-progress
                      :percentage="project.matchScore"
                      :stroke-width="8"
                      :show-text="false"
                      :color="getMatchScoreColor(project.matchScore)"
                    />
                    <span class="score-text">{{ project.matchScore }}%</span>
                  </div>
                  <div class="project-actions">
                    <el-button type="text" size="small" @click="viewProject(project)">查看</el-button>
                    <el-button type="text" size="small" @click="applyProject(project)">申请</el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 证书和资质 -->
          <el-card class="certificates-card">
            <template #header>
              <h3>证书和资质</h3>
            </template>
            <div class="certificates-list">
              <div v-for="cert in expert.certificates" :key="cert.id" class="certificate-item">
                <div class="certificate-info">
                  <el-icon class="certificate-icon"><Trophy /></el-icon>
                  <div class="certificate-details">
                    <div class="certificate-name">{{ cert.name }}</div>
                    <div class="certificate-meta">{{ cert.issuer }} · {{ formatDate(cert.issueDate) }}</div>
                  </div>
                </div>
                <div class="certificate-actions">
                  <el-button type="text" size="small" @click="viewCertificate(cert)">查看</el-button>
                </div>
              </div>
            </div>
            <div class="upload-section">
              <el-upload
                class="upload-demo"
                action="#"
                :on-change="handleCertificateUpload"
                :auto-upload="false"
                :show-file-list="false"
              >
                <el-button type="text" icon="Upload">上传证书</el-button>
              </el-upload>
            </div>
          </el-card>

          <!-- 评价记录 -->
          <el-card class="reviews-card">
            <template #header>
              <h3>评价记录</h3>
            </template>
            <div class="reviews-list">
              <div v-for="review in expert.reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="reviewer-info">
                    <el-avatar :size="32" :src="review.reviewerAvatar" />
                    <div class="reviewer-details">
                      <div class="reviewer-name">{{ review.reviewer }}</div>
                      <div class="review-project">{{ review.project }}</div>
                    </div>
                  </div>
                  <div class="review-rating">
                    <el-rate
                      v-model="review.rating"
                      disabled
                      size="small"
                    />
                  </div>
                </div>
                <div class="review-content">{{ review.content }}</div>
                <div class="review-date">{{ formatDate(review.date) }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="assignProjectDialogVisible" title="分配项目" width="480px">
      <el-form label-width="100px">
        <el-form-item label="选择项目">
          <el-select v-model="assignProjectForm.projectId" placeholder="请选择项目" style="width: 100%">
            <el-option
              v-for="project in assignableProjects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
          <div v-if="assignableProjects.length === 0" class="dialog-help-text">当前没有可分配的新项目。</div>
        </el-form-item>
        <el-form-item label="项目角色">
          <el-input v-model="assignProjectForm.role" placeholder="如：顾问、项目成员、负责人" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="assignProjectForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择开始日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="总工时">
          <el-input-number v-model="assignProjectForm.totalHours" :min="1" :step="8" style="width: 100%" />
        </el-form-item>
        <el-form-item label="职责说明">
          <el-input v-model="assignProjectForm.responsibilities" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignProjectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigningProject" :disabled="assignableProjects.length === 0" @click="submitAssignProject">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { Trophy } from '@element-plus/icons-vue'
import { ExpertService } from '@/api/services/expert.service'
import { ProjectService } from '@/api/services/project.service'
import { ProjectExpertService } from '@/api/services/project-expert.service'
import type { UpdateExpertRequest } from '@/api/types'

// 导入ECharts配置
import {
  getExpertSkillRadarOption,
  initChart,
  resizeChart,
  disposeChart
} from '@/utils/echartsConfig'

const route = useRoute()
const router = useRouter()
const avatarFileInputRef = ref<HTMLInputElement | null>(null)
const allProjects = ref<Array<{ id: number; name: string }>>([])
const assignProjectDialogVisible = ref(false)
const assigningProject = ref(false)
const assignProjectForm = ref({
  projectId: undefined as number | undefined,
  role: '项目成员',
  startDate: '',
  totalHours: 40,
  responsibilities: ''
})

// 图表引用
const skillRadarChart = ref<HTMLElement>()

// 图表实例
let radarChartInstance: echarts.ECharts | null = null

// 专家数据
const expert = ref({
  id: 1,
  name: '',
  gender: '-',
  age: 0,
  title: '',
  education: '-',
  school: '-',
  workYears: 0,
  phone: '',
  email: '',
  city: '-',
  expectedSalary: 0,
  availability: '待确认',
  status: 'available',
  avatar: '',
  matchRate: 0,
  completedProjects: 0,
  averageRating: 0,
  activityLevel: 0,
  skillCategories: [] as Array<{ name: string; level: string; skills: Array<{ name: string; level: string }> }>,
  experiences: [] as Array<{ id: number; company: string; position: string; startDate: string; endDate: string; description: string; projects: string[] }>,
  certificates: [] as Array<{ id: number; name: string; issuer: string; issueDate: string }>,
  reviews: [] as Array<{ id: number; reviewer: string; reviewerAvatar: string; project: string; rating: number; content: string; date: string }>
})

// 匹配项目数据
const matchedProjects = ref<Array<{ id: number; name: string; client: string; budget: number; matchScore: number; status: string }>>([])
const assignableProjects = computed(() => {
  const assignedProjectIds = new Set(matchedProjects.value.map((project) => project.id))
  return allProjects.value.filter((project) => !assignedProjectIds.has(project.id))
})

const toNumber = (value: unknown, fallback = 0) => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim() !== '') {
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : fallback
  }

  return fallback
}

const normalizeExpertStatus = (detail: Record<string, any>) => {
  if (detail.availability === false || detail.availabilityStatus === 'UNAVAILABLE') {
    return 'unavailable'
  }

  return 'available'
}

const toSkillLevelLabel = (value: unknown) => {
  const level = toNumber(value)
  if (level >= 4) return '精通'
  if (level >= 2) return '熟练'
  return '了解'
}

const syncExpertFromApi = async () => {
  const expertId = Number(route.params.id)
  if (!expertId) return

  const expertDetail = await ExpertService.getExpertById(expertId) as Record<string, any>
  expert.value = {
    ...expert.value,
    id: expertDetail.id,
    name: expertDetail.name,
    title: expertDetail.title || expertDetail.currentPosition || '',
    phone: expertDetail.phone || expertDetail.phoneNumber || '',
    email: expertDetail.email,
    workYears: toNumber(expertDetail.experienceYears ?? expertDetail.yearsOfExperience),
    expectedSalary: expertDetail.hourlyRate || 0,
    avatar: expertDetail.avatar || '',
    status: normalizeExpertStatus(expertDetail),
    averageRating: toNumber(expertDetail.rating ?? expertDetail.overallRating),
    completedProjects: toNumber(expertDetail.completedProjects ?? expertDetail.projectCount),
    matchRate: toNumber(expertDetail.successRate, expertDetail.projectCount ? 80 : 0),
    activityLevel: expertDetail.availability === false || expertDetail.availabilityStatus === 'UNAVAILABLE' ? 40 : 90,
    availability: expertDetail.availability === false || expertDetail.availabilityStatus === 'UNAVAILABLE' ? '暂不可用' : '可安排',
    skillCategories: Array.isArray(expertDetail.skills) && expertDetail.skills.length
      ? [{
          name: '核心技能',
          level: '已同步',
          skills: expertDetail.skills.map((skill: any) => ({
            name: skill.skillName || skill.name,
            level: toSkillLevelLabel(skill.proficiency ?? skill.proficiencyLevel ?? 3)
          }))
        }]
      : [],
    experiences: Array.isArray(expertDetail.projects) ? expertDetail.projects.map((project: any) => ({
      id: project.id,
      company: expertDetail.company || expertDetail.currentCompany || '未填写公司',
      position: project.role || expertDetail.position || expertDetail.currentPosition || expertDetail.title || '未填写岗位',
      startDate: project.startDate,
      endDate: project.endDate || '',
      description: `参与项目 ${project.projectName}`,
      projects: [project.projectName]
    })) : []
  }
}

const syncMatchedProjects = async () => {
  const expertId = Number(route.params.id)
  if (!expertId) return

  const assignments = await ProjectExpertService.getAssignmentsByExpertId(expertId)
  const projects = await Promise.all(
    assignments.map(async (assignment, index) => {
      const projectDetail = await ProjectService.getProjectById(assignment.projectId)
      return {
        id: projectDetail.id,
        name: projectDetail.name,
        client: projectDetail.clientName || '未填写客户',
        budget: toNumber(projectDetail.budget),
        matchScore: assignment.completionPercentage ?? (assignment.status === 'COMPLETED' ? 100 : Math.max(70, 95 - index * 5)),
        status: String(assignment.status || projectDetail.status)
      }
    })
  )

  matchedProjects.value = projects
}

const syncAssignableProjects = async () => {
  const projects = await ProjectService.getProjects({ page: 0, size: 100 })
  allProjects.value = projects.content.map((project) => ({
    id: project.id,
    name: project.name
  }))
}

// 状态类型映射
const getStatusType = (status: string) => {
  switch (status) {
    case 'available': return 'success'
    case 'busy': return 'warning'
    case 'unavailable': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'available': return '可接单'
    case 'busy': return '忙碌中'
    case 'unavailable': return '不可用'
    default: return '未知'
  }
}

// 技能类型映射
const getSkillType = (level: string) => {
  switch (level) {
    case '精通': return 'success'
    case '熟练': return 'primary'
    case '了解': return 'info'
    default: return 'info'
  }
}

// 匹配率颜色
const getMatchRateColor = (percentage: number) => {
  if (percentage < 60) return '#f56c6c'
  if (percentage < 80) return '#e6a23c'
  return '#67c23a'
}

// 活跃度颜色
const getActivityColor = (percentage: number) => {
  if (percentage < 60) return '#f56c6c'
  if (percentage < 80) return '#e6a23c'
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
  if (!dateStr) return '未设置'
  const date = new Date(dateStr)
  return Number.isNaN(date.getTime()) ? dateStr : date.toLocaleDateString('zh-CN')
}

// 初始化雷达图
const initRadarChart = () => {
  if (skillRadarChart.value) {
    // 准备雷达图数据
    const radarData = expert.value.skillCategories.map(category => {
      // 根据技能等级计算分数
      const skillScores = category.skills.map(skill => {
        switch (skill.level) {
          case '精通': return 100
          case '熟练': return 75
          case '了解': return 50
          default: return 25
        }
      })
      const averageScore = skillScores.reduce((sum, score) => sum + score, 0) / skillScores.length
      
      return {
        name: category.name,
        max: 100
      }
    })
    
    // 准备实际技能值
    const skillValues = expert.value.skillCategories.map(category => {
      const skillScores = category.skills.map(skill => {
        switch (skill.level) {
          case '精通': return 100
          case '熟练': return 75
          case '了解': return 50
          default: return 25
        }
      })
      return skillScores.reduce((sum, score) => sum + score, 0) / skillScores.length
    })
    
    // 创建雷达图选项
    const radarOption: echarts.EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: function(params: any) {
          const category = expert.value.skillCategories[params.dataIndex]
          return `
            <div style="margin: 0px 0 0; line-height: 1;">
              <div style="font-size: 14px; color: #666; font-weight: 400; margin-bottom: 4px;">
                ${category.name}
              </div>
              <div style="margin: 10px 0 0; padding: 10px 0 0; border-top: 1px solid #eee;">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px;">
                  <span style="font-size: 14px; color: #666;">技能评分</span>
                  <span style="font-size: 14px; color: #333; font-weight: 600;">${params.value.toFixed(1)}</span>
                </div>
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <span style="font-size: 14px; color: #666;">技能数量</span>
                  <span style="font-size: 14px; color: #333; font-weight: 600;">${category.skills.length}</span>
                </div>
                <div style="display: flex; align-items: center; justify-content: space-between;">
                  <span style="font-size: 14px; color: #666;">最高等级</span>
                  <span style="font-size: 14px; color: #333; font-weight: 600;">${category.level}</span>
                </div>
              </div>
            </div>
          `
        }
      },
      radar: {
        indicator: radarData,
        shape: 'circle' as const,
        splitNumber: 5,
        axisName: {
          color: '#606266',
          fontSize: 12
        },
        splitLine: {
          lineStyle: {
            color: ['#e4e7ed', '#dcdfe6', '#c0c4cc', '#b1b3b8', '#a6a9ad']
          }
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: ['rgba(250, 250, 250, 0.8)', 'rgba(245, 245, 245, 0.8)', 'rgba(240, 240, 240, 0.8)']
          }
        },
        axisLine: {
          lineStyle: {
            color: '#dcdfe6'
          }
        }
      },
      series: [
        {
          name: '技能分布',
          type: 'radar',
          data: [
            {
              value: skillValues,
              name: '技能评分',
              symbol: 'circle',
              symbolSize: 8,
              lineStyle: {
                width: 3,
                color: '#409EFF'
              },
              areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: 'rgba(64, 158, 255, 0.8)' },
                  { offset: 1, color: 'rgba(64, 158, 255, 0.2)' }
                ])
              },
              itemStyle: {
                color: '#409EFF',
                borderColor: '#fff',
                borderWidth: 2
              }
            }
          ]
        }
      ]
    }
    
    radarChartInstance = initChart(skillRadarChart.value, radarOption)
  }
}

// 刷新雷达图
const refreshRadarChart = () => {
  if (radarChartInstance) {
    // 重新计算技能分数
    const radarData = expert.value.skillCategories.map(category => {
      return {
        name: category.name,
        max: 100
      }
    })
    
    const skillValues = expert.value.skillCategories.map(category => {
      const skillScores = category.skills.map(skill => {
        switch (skill.level) {
          case '精通': return 100
          case '熟练': return 75
          case '了解': return 50
          default: return 25
        }
      })
      return skillScores.reduce((sum, score) => sum + score, 0) / skillScores.length
    })
    
    // 更新雷达图选项
    const radarOption = {
      radar: {
        indicator: radarData
      },
      series: [
        {
          data: [
            {
              value: skillValues,
              name: '技能评分'
            }
          ]
        }
      ]
    }
    
    radarChartInstance.setOption(radarOption)
    ElMessage.success('技能雷达图已刷新')
  }
}

// 导出雷达图
const exportRadarChart = () => {
  if (radarChartInstance) {
    const url = radarChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    downloadImage(url, '专家技能雷达图.png')
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

// 编辑专家
const editExpert = () => {
  router.push(`/experts/${expert.value.id}/edit`)
}

// 匹配项目
const matchProjects = () => {
  assignProjectForm.value = {
    projectId: assignableProjects.value[0]?.id,
    role: '项目成员',
    startDate: '',
    totalHours: 40,
    responsibilities: ''
  }
  assignProjectDialogVisible.value = true
}

const submitAssignProject = async () => {
  if (!assignProjectForm.value.projectId) {
    ElMessage.warning('请先选择项目')
    return
  }

  assigningProject.value = true
  try {
    await ProjectExpertService.createAssignment({
      projectId: assignProjectForm.value.projectId,
      expertId: expert.value.id,
      role: assignProjectForm.value.role || '项目成员',
      startDate: assignProjectForm.value.startDate || undefined,
      totalHours: assignProjectForm.value.totalHours,
      responsibilities: assignProjectForm.value.responsibilities || undefined
    })
    assignProjectDialogVisible.value = false
    await syncMatchedProjects()
    ElMessage.success('项目分配成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '项目分配失败')
  } finally {
    assigningProject.value = false
  }
}

// 导出专家资料（当前页已加载数据，含匹配项目）
const exportExpert = () => {
  try {
    const payload = {
      ...expert.value,
      matchedProjects: matchedProjects.value
    }
    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${expert.value.name || 'expert'}-detail.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('已导出当前专家详情数据')
  } catch (error: any) {
    ElMessage.error(error?.message || '导出失败')
  }
}

// 删除专家
const deleteExpert = () => {
  ElMessageBox.confirm(
    '确定要删除这个专家吗？此操作不可恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await ExpertService.deleteExpert(expert.value.id)
    ElMessage.success('专家已删除')
    router.push('/experts')
  }).catch(() => {
    // 取消删除
  })
}

// 更换头像：后端 avatar 字段较短，仅适合保存 URL；本地选图为 blob 预览
const onAvatarFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  const nextUrl = URL.createObjectURL(file)
  if (expert.value.avatar?.startsWith('blob:')) {
    URL.revokeObjectURL(expert.value.avatar)
  }
  expert.value.avatar = nextUrl
  ElMessage.success('已应用本地头像预览（未上传服务器，刷新后失效）')
}

const changeAvatar = async () => {
  try {
    await ElMessageBox.confirm(
      '保存到服务器仅支持「图片 URL」（外链），且需符合后端字段长度。本地选图仅当前页预览，刷新后失效。',
      '更换头像',
      {
        confirmButtonText: '输入 URL 并保存',
        cancelButtonText: '本地选图预览',
        distinguishCancelAndClose: true,
        type: 'info'
      }
    )
  } catch (action) {
    if (action === 'cancel') {
      avatarFileInputRef.value?.click()
    }
    return
  }

  try {
    const { value } = await ElMessageBox.prompt('请输入头像图片 URL', '头像 URL', {
      confirmButtonText: '保存',
      inputPlaceholder: 'https://example.com/avatar.png'
    })
    const url = value?.trim()
    if (!url) {
      ElMessage.warning('未输入 URL')
      return
    }
    if (url.length > 500) {
      ElMessage.warning('URL 过长，请使用更短链接或图床（后端头像字段长度有限）')
      return
    }
    await ExpertService.updateExpert(expert.value.id, { id: expert.value.id, avatar: url } as UpdateExpertRequest)
    if (expert.value.avatar?.startsWith('blob:')) {
      URL.revokeObjectURL(expert.value.avatar)
    }
    expert.value.avatar = url
    ElMessage.success('头像已保存')
  } catch {
    // 用户取消输入
  }
}

// 查看项目详情
const viewProject = (project: any) => {
  router.push(`/projects/${project.id}`)
}

// 申请项目：与「匹配项目」一致，走项目-专家分配
const applyProject = (project: any) => {
  assignProjectForm.value = {
    projectId: project.id,
    role: '项目成员',
    startDate: '',
    totalHours: 40,
    responsibilities: ''
  }
  assignProjectDialogVisible.value = true
}

// 查看证书
const viewCertificate = (cert: any) => {
  ElMessageBox.alert(
    `证书名称：${cert.name}\n颁发机构：${cert.issuer}\n颁发日期：${formatDate(cert.issueDate)}`,
    '证书信息',
    { confirmButtonText: '关闭' }
  )
}

// 上传证书（仅当前页列表，无后端证书实体时不在此假持久化）
const handleCertificateUpload = (file: any) => {
  const uploadedName = file?.name || file?.raw?.name || '未命名证书'
  expert.value.certificates = [
    {
      id: Date.now(),
      name: uploadedName,
      issuer: '本地上传（未持久化）',
      issueDate: new Date().toISOString().slice(0, 10)
    },
    ...expert.value.certificates
  ]
  ElMessage.success(`证书「${uploadedName}」已加入列表（仅当前页，刷新后不会保留）`)
}

// 响应式调整图表大小
const handleResize = () => {
  if (radarChartInstance) resizeChart(radarChartInstance)
}

onMounted(async () => {
  try {
    await Promise.all([syncExpertFromApi(), syncMatchedProjects(), syncAssignableProjects()])
  } catch (error) {
    ElMessage.error('加载专家详情失败')
  }

  nextTick(() => {
    initRadarChart()
  })

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 销毁图表实例
  if (radarChartInstance) disposeChart(radarChartInstance)
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.expert-detail {
  padding: 20px;
}

.avatar-file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
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

.expert-content {
  margin-top: 20px;
}

.info-card,
.skills-card,
.experience-card,
.status-card,
.matched-projects-card,
.certificates-card,
.reviews-card {
  margin-bottom: 20px;
}

.info-card h3,
.skills-card h3,
.experience-card h3,
.status-card h3,
.matched-projects-card h3,
.certificates-card h3,
.reviews-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.skills-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skills-actions {
  display: flex;
  gap: 8px;
}

.personal-info {
  display: flex;
  gap: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-actions {
  text-align: center;
}

.info-section {
  flex: 1;
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

.info-item .label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 14px;
  color: #333;
}

.skills-content {
  padding: 16px 0;
}

.skills-layout {
  display: flex;
  gap: 24px;
}

.radar-chart-section {
  flex: 1;
  min-width: 300px;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.chart-subtitle {
  font-size: 12px;
  color: #999;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

.skill-categories-section {
  flex: 1;
  min-width: 300px;
}

.categories-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.categories-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.categories-count {
  font-size: 12px;
  color: #999;
  padding: 2px 8px;
  background: #f0f2f5;
  border-radius: 4px;
}

.skill-categories {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-header h5 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.category-level {
  font-size: 11px;
  color: #666;
  padding: 2px 8px;
  background: #e4e7ed;
  border-radius: 4px;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin-right: 4px;
}

.skill-level {
  font-size: 11px;
  color: #666;
  margin-left: 2px;
}

.experience-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.experience-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.experience-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.company-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.company-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.company-position {
  font-size: 13px;
  color: #666;
}

.experience-period {
  font-size: 12px;
  color: #999;
}

.experience-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.experience-description {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.experience-projects {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.projects-title {
  font-size: 12px;
  color: #999;
}

.projects-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.project-tag {
  margin-right: 4px;
}

.status-info {
  padding: 16px 0;
}

.status-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

.stat-value {
  display: flex;
  align-items: center;
  gap: 12px;
}

.percentage {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  min-width: 40px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.projects-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.project-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.project-info {
  flex: 1;
}

.project-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.project-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.project-match {
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

.project-actions {
  display: flex;
  gap: 8px;
}

.certificates-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.certificate-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.certificate-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.certificate-icon {
  font-size: 20px;
  color: #e6a23c;
}

.certificate-details {
  flex: 1;
}

.certificate-name {
  font-size: 13px;
  color: #333;
}

.certificate-meta {
  font-size: 11px;
  color: #999;
}

.certificate-actions {
  display: flex;
  gap: 8px;
}

.upload-section {
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reviewer-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.reviewer-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.review-project {
  font-size: 12px;
  color: #666;
}

.review-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.review-date {
  font-size: 11px;
  color: #999;
  text-align: right;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .skills-layout {
    flex-direction: column;
  }
  
  .radar-chart-section,
  .skill-categories-section {
    min-width: 100%;
  }
}
</style>