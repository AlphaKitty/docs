<template>
  <div class="skill-detail">
    <div class="page-header">
      <h2>技能详情</h2>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="editSkill">编辑</el-button>
        <el-button type="danger" @click="deleteSkill">删除</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧信息卡片 -->
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <h3>{{ skill.name }}</h3>
              <div class="skill-tags">
                <el-tag :type="getCategoryType(skill.category)" size="large">
                  {{ getCategoryText(skill.category) }}
                </el-tag>
                <el-tag type="info" size="large">
                  需求等级: {{ skill.demandLevel }}/5
                </el-tag>
                <el-tag v-if="skill.enabled" type="success" size="large">
                  已启用
                </el-tag>
                <el-tag v-else type="danger" size="large">
                  已禁用
                </el-tag>
              </div>
            </div>
          </template>

          <div class="skill-info">
            <div class="info-section">
              <h4>技能描述</h4>
              <p>{{ skill.description }}</p>
            </div>

            <div class="info-section">
              <h4>技能标签</h4>
              <div class="tags-container">
                <el-tag
                  v-for="tag in skill.tags"
                  :key="tag"
                  class="skill-tag"
                  size="medium"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>

            <div class="info-section">
              <h4>相关技能</h4>
              <div class="related-skills">
                <el-tag
                  v-for="relatedSkill in relatedSkills"
                  :key="relatedSkill.id"
                  class="related-skill-tag"
                  size="medium"
                  @click="viewRelatedSkill(relatedSkill.id)"
                >
                  {{ relatedSkill.name }}
                </el-tag>
              </div>
            </div>

            <div class="info-section">
              <h4>技能文档</h4>
              <div class="documentation">
                <p v-if="skill.documentation">{{ skill.documentation }}</p>
                <p v-else class="no-documentation">暂无文档</p>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 统计信息卡片 -->
        <el-card class="stats-card">
          <template #header>
            <h3>统计信息</h3>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">关联专家</div>
                <div class="stat-value">{{ skill.expertCount }}</div>
                <div class="stat-trend">
                  <el-icon color="#67C23A"><TrendCharts /></el-icon>
                  <span>+12%</span>
                </div>
              </div>
            </el-col>
            
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">关联项目</div>
                <div class="stat-value">{{ skill.projectCount }}</div>
                <div class="stat-trend">
                  <el-icon color="#67C23A"><TrendCharts /></el-icon>
                  <span>+8%</span>
                </div>
              </div>
            </el-col>
            
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">匹配率</div>
                <div class="stat-value">{{ skill.matchRate }}%</div>
                <div class="stat-trend">
                  <el-icon color="#67C23A"><TrendCharts /></el-icon>
                  <span>+5%</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 右侧操作卡片 -->
      <el-col :span="8">
        <el-card class="action-card">
          <template #header>
            <h3>快速操作</h3>
          </template>
          
          <div class="action-buttons">
            <el-button type="primary" icon="User" @click="viewExperts">
              查看关联专家
            </el-button>
            <el-button type="success" icon="Document" @click="viewProjects">
              查看关联项目
            </el-button>
            <el-button type="warning" icon="Edit" @click="editSkill">
              编辑技能信息
            </el-button>
            <el-button type="info" icon="Setting" @click="manageConfig">
              技能配置
            </el-button>
          </div>

          <div class="action-section">
            <h4>技能状态</h4>
            <el-switch
              v-model="skill.enabled"
              active-text="启用"
              inactive-text="禁用"
              @change="toggleSkillStatus"
            />
          </div>

          <div class="action-section">
            <h4>创建信息</h4>
            <div class="creation-info">
              <p><strong>创建时间:</strong> {{ skill.createdAt }}</p>
              <p><strong>更新时间:</strong> {{ skill.updatedAt }}</p>
              <p><strong>创建人:</strong> {{ skill.creator }}</p>
            </div>
          </div>
        </el-card>

        <!-- 相关技能卡片 -->
        <el-card class="related-card">
          <template #header>
            <h3>同分类技能</h3>
          </template>
          
          <div class="related-list">
            <div
              v-for="related in sameCategorySkills"
              :key="related.id"
              class="related-item"
              @click="viewRelatedSkill(related.id)"
            >
              <div class="related-name">{{ related.name }}</div>
              <div class="related-stats">
                <span class="expert-count">{{ related.expertCount }} 专家</span>
                <span class="project-count">{{ related.projectCount }} 项目</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Edit, Setting, TrendCharts } from '@element-plus/icons-vue'

interface Skill {
  id: number
  name: string
  category: string
  description: string
  demandLevel: number
  tags: string[]
  relatedSkills: number[]
  documentation: string
  enabled: boolean
  expertCount: number
  projectCount: number
  matchRate: number
  createdAt: string
  updatedAt: string
  creator: string
}

interface RelatedSkill {
  id: number
  name: string
  expertCount: number
  projectCount: number
}

const router = useRouter()
const route = useRoute()

const skill = reactive<Skill>({
  id: 1,
  name: '机器学习',
  category: 'tech',
  description: '机器学习是人工智能的核心领域，涉及算法和统计模型，使计算机系统能够从数据中学习并做出预测或决策，而无需显式编程。',
  demandLevel: 5,
  tags: ['人工智能', '深度学习', 'Python', 'TensorFlow', 'PyTorch', '数据分析'],
  relatedSkills: [2, 3, 4],
  documentation: 'https://scikit-learn.org/stable/documentation.html',
  enabled: true,
  expertCount: 45,
  projectCount: 28,
  matchRate: 85,
  createdAt: '2024-01-15 10:30:00',
  updatedAt: '2024-03-20 14:45:00',
  creator: '管理员'
})

const relatedSkills = ref<RelatedSkill[]>([
  { id: 2, name: '深度学习', expertCount: 32, projectCount: 21 },
  { id: 3, name: 'Python编程', expertCount: 78, projectCount: 56 },
  { id: 4, name: '数据分析', expertCount: 42, projectCount: 38 }
])

const sameCategorySkills = ref<RelatedSkill[]>([
  { id: 5, name: 'Java开发', expertCount: 65, projectCount: 48 },
  { id: 6, name: 'Vue.js开发', expertCount: 38, projectCount: 42 },
  { id: 7, name: 'React开发', expertCount: 42, projectCount: 39 },
  { id: 8, name: 'Node.js开发', expertCount: 35, projectCount: 31 },
  { id: 9, name: 'Spring Boot开发', expertCount: 52, projectCount: 45 }
])

const getCategoryType = (category: string) => {
  switch (category) {
    case 'tech': return 'primary'
    case 'design': return 'success'
    case 'product': return 'warning'
    case 'marketing': return 'danger'
    case 'data': return 'info'
    default: return 'info'
  }
}

const getCategoryText = (category: string) => {
  switch (category) {
    case 'tech': return '技术开发'
    case 'design': return '设计创意'
    case 'product': return '产品管理'
    case 'marketing': return '市场营销'
    case 'data': return '数据分析'
    default: return '其他'
  }
}

const goBack = () => {
  router.back()
}

const editSkill = () => {
  ElMessage.info('编辑技能功能开发中')
}

const deleteSkill = () => {
  ElMessageBox.confirm(
    '确定要删除此技能吗？删除后无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    ElMessage.success('删除成功')
    router.push('/skills')
  }).catch(() => {
    // 取消删除
  })
}

const viewExperts = () => {
  router.push('/experts')
}

const viewProjects = () => {
  router.push('/projects')
}

const manageConfig = () => {
  ElMessage.info('技能配置功能开发中')
}

const toggleSkillStatus = () => {
  const action = skill.enabled ? '启用' : '禁用'
  ElMessage.success(`技能已${action}`)
}

const viewRelatedSkill = (skillId: number) => {
  router.push(`/skills/${skillId}`)
}

onMounted(() => {
  // 从路由参数获取技能ID
  const skillId = route.params.id
  console.log('加载技能详情，ID:', skillId)
  
  // 这里应该调用API获取技能详情数据
  // 暂时使用模拟数据
})
</script>

<style scoped>
.skill-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
}

.skill-tags {
  display: flex;
  gap: 8px;
}

.info-section {
  margin-bottom: 24px;
}

.info-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.info-section p {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  cursor: default;
}

.related-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.related-skill-tag {
  cursor: pointer;
}

.related-skill-tag:hover {
  opacity: 0.8;
}

.no-documentation {
  color: #999;
  font-style: italic;
}

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: #67C23A;
}

.action-card {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.action-buttons .el-button {
  width: 100%;
  justify-content: flex-start;
}

.action-section {
  margin-bottom: 20px;
}

.action-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.creation-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.creation-info strong {
  color: #333;
  margin-right: 8px;
}

.related-card {
  margin-bottom: 20px;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  padding: 12px;
  border-radius: 6px;
  background: #f5f7fa;
  cursor: pointer;
  transition: background-color 0.3s;
}

.related-item:hover {
  background: #e4e7ed;
}

.related-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.related-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.expert-count {
  color: #409EFF;
}

.project-count {
  color: #67C23A;
}
</style>