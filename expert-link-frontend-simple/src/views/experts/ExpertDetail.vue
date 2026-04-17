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
          <el-button type="danger" @click="deleteExpert">删除</el-button>
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
                          size="medium"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { Trophy } from '@element-plus/icons-vue'

// 导入ECharts配置
import {
  getExpertSkillRadarOption,
  initChart,
  resizeChart,
  disposeChart
} from '@/utils/echartsConfig'

const route = useRoute()
const router = useRouter()

// 图表引用
const skillRadarChart = ref<HTMLElement>()

// 图表实例
let radarChartInstance: echarts.ECharts | null = null

// 专家数据
const expert = ref({
  id: 1,
  name: '王专家',
  gender: '男',
  age: 35,
  title: '自然语言处理专家',
  education: '博士',
  school: '清华大学',
  workYears: 10,
  phone: '13800138000',
  email: 'wang.expert@example.com',
  city: '北京',
  expectedSalary: 50000,
  availability: '全职',
  status: 'available',
  avatar: '',
  matchRate: 85,
  completedProjects: 24,
  averageRating: 4.7,
  activityLevel: 90,
  skillCategories: [
    {
      name: '自然语言处理',
      level: '专家级',
      skills: [
        { name: '中文分词', level: '精通' },
        { name: '词性标注', level: '精通' },
        { name: '命名实体识别', level: '精通' },
        { name: '情感分析', level: '熟练' },
        { name: '文本分类', level: '熟练' }
      ]
    },
    {
      name: '深度学习',
      level: '高级',
      skills: [
        { name: 'TensorFlow', level: '精通' },
        { name: 'PyTorch', level: '精通' },
        { name: '神经网络', level: '精通' },
        { name: '模型训练', level: '熟练' },
        { name: '模型优化', level: '熟练' }
      ]
    },
    {
      name: '编程语言',
      level: '高级',
      skills: [
        { name: 'Python', level: '精通' },
        { name: 'Java', level: '熟练' },
        { name: 'C++', level: '熟练' },
        { name: 'JavaScript', level: '熟练' }
      ]
    },
    {
      name: '数据库',
      level: '中级',
      skills: [
        { name: 'MySQL', level: '熟练' },
        { name: 'Redis', level: '熟练' },
        { name: 'MongoDB', level: '了解' }
      ]
    },
    {
      name: '项目管理',
      level: '中级',
      skills: [
        { name: '敏捷开发', level: '熟练' },
        { name: '团队协作', level: '熟练' },
        { name: '需求分析', level: '了解' }
      ]
    }
  ],
  experiences: [
    {
      id: 1,
      company: '某科技公司',
      position: '高级算法工程师',
      startDate: '2020-03-01',
      endDate: '2024-01-31',
      description: '负责自然语言处理相关算法的研发和优化，主导多个AI项目的技术方案设计。',
      projects: ['智能客服系统', '文本分析平台', '知识图谱构建']
    },
    {
      id: 2,
      company: '某互联网公司',
      position: '算法工程师',
      startDate: '2016-07-01',
      endDate: '2020-02-29',
      description: '参与推荐系统和搜索算法的开发，负责文本处理模块的设计和实现。',
      projects: ['个性化推荐系统', '智能搜索', '用户画像系统']
    },
    {
      id: 3,
      company: '某研究院',
      position: '研究员',
      startDate: '2014-09-01',
      endDate: '2016-06-30',
      description: '从事自然语言处理基础研究，发表多篇学术论文，参与国家级科研项目。',
      projects: ['中文信息处理', '机器翻译', '语义理解']
    }
  ],
  certificates: [
    { id: 1, name: '人工智能高级工程师', issuer: '中国人工智能学会', issueDate: '2023-06-01' },
    { id: 2, name: 'TensorFlow开发者认证', issuer: 'Google', issueDate: '2022-12-01' },
    { id: 3, name: '项目管理专业人士', issuer: 'PMI', issueDate: '2021-09-01' }
  ],
  reviews: [
    {
      id: 1,
      reviewer: '张经理',
      reviewerAvatar: '',
      project: '智能客服系统开发',
      rating: 5,
      content: '王专家在项目中表现出色，技术能力强，解决问题迅速，沟通顺畅。',
      date: '2024-03-15'
    },
    {
      id: 2,
      reviewer: '李总监',
      reviewerAvatar: '',
      project: '文本分析平台',
      rating: 4,
      content: '专业水平很高，项目交付质量优秀，期待再次合作。',
      date: '2023-11-20'
    },
    {
      id: 3,
      reviewer: '王主管',
      reviewerAvatar: '',
      project: '知识图谱构建',
      rating: 5,
      content: '技术功底深厚，对复杂问题有独到见解，项目进展顺利。',
      date: '2023-08-10'
    }
  ]
})

// 匹配项目数据
const matchedProjects = ref([
  {
    id: 1,
    name: 'AI智能客服系统开发',
    client: '某科技公司',
    budget: 500000,
    matchScore: 92,
    status: 'in_progress'
  },
  {
    id: 2,
    name: '智能文本分析平台',
    client: '某金融公司',
    budget: 350000,
    matchScore: 88,
    status: 'planning'
  },
  {
    id: 3,
    name: '知识图谱构建项目',
    client: '某教育机构',
    budget: 280000,
    matchScore: 85,
    status: 'planning'
  },
  {
    id: 4,
    name: '智能搜索系统优化',
    client: '某电商平台',
    budget: 420000,
    matchScore: 82,
    status: 'in_progress'
  }
])

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
  return new Date(dateStr).toLocaleDateString('zh-CN')
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
    const radarOption = {
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
        shape: 'circle',
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
  router.push(`/experts/edit/${expert.value.id}`)
}

// 匹配项目
const matchProjects = () => {
  ElMessage.info('开始匹配项目...')
  // 这里可以调用匹配算法
}

// 导出专家资料
const exportExpert = () => {
  ElMessage.success('专家资料导出成功')
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
  ).then(() => {
    ElMessage.success('专家已删除')
    router.push('/experts')
  }).catch(() => {
    // 取消删除
  })
}

// 更换头像
const changeAvatar = () => {
  ElMessage.info('更换头像功能')
}

// 查看项目详情
const viewProject = (project: any) => {
  router.push(`/projects/${project.id}`)
}

// 申请项目
const applyProject = (project: any) => {
  ElMessage.info(`申请项目: ${project.name}`)
}

// 查看证书
const viewCertificate = (cert: any) => {
  ElMessage.info(`查看证书: ${cert.name}`)
}

// 上传证书
const handleCertificateUpload = (file: any) => {
  ElMessage.success(`证书 ${file.name} 上传成功`)
  // 这里可以添加上传逻辑
}

// 响应式调整图表大小
const handleResize = () => {
  if (radarChartInstance) resizeChart(radarChartInstance)
}

onMounted(() => {
  // 初始化雷达图
  nextTick(() => {
    initRadarChart()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  
  // 这里可以加载专家数据
  const expertId = route.params.id
  console.log('加载专家数据:', expertId)
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