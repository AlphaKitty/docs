<template>
  <div v-loading="loading" class="skill-detail">
    <div class="page-header">
      <h2>技能详情</h2>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="editSkill">编辑</el-button>
        <el-button type="danger" @click="deleteSkill">删除</el-button>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
      class="page-alert"
    />

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
                  size="default"
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
                  size="default"
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
import { onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Edit, Setting, TrendCharts } from '@element-plus/icons-vue'
import { SkillService } from '@/api/services'
import type { UpdateSkillRequest } from '@/api/types'

interface RawSkill {
  id: number
  name?: string
  description?: string
  category?: string
  demandLevel?: string
  expertCount?: number
  projectCount?: number
  isActive?: boolean
  createdAt?: string
  updatedAt?: string
  domain?: {
    id?: number
    name?: string
  }
  tags?: Array<{ id: number; name?: string }>
  relatedSkills?: Array<{
    id: number
    name?: string
    category?: string
    expertCount?: number
    projectCount?: number
    domain?: { id?: number }
  }>
  documentation?: {
    content?: string
    url?: string
  } | null
}

interface SkillViewModel {
  id: number
  name: string
  category: string
  description: string
  demandLevel: number
  tags: string[]
  documentation: string
  enabled: boolean
  expertCount: number
  projectCount: number
  matchRate: number
  createdAt: string
  updatedAt: string
  creator: string
  domainId?: number
  domainName?: string
}

interface RelatedSkill {
  id: number
  name: string
  expertCount: number
  projectCount: number
  category: string
  domainId?: number
}

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const loadError = ref('')
const allSkills = ref<RelatedSkill[]>([])
const relatedSkills = ref<RelatedSkill[]>([])
const sameCategorySkills = ref<RelatedSkill[]>([])
const detailRelatedSkillIds = ref<number[]>([])

const skill = ref<SkillViewModel>({
  id: 0,
  name: '加载中',
  category: 'other',
  description: '暂无描述',
  demandLevel: 3,
  tags: [],
  documentation: '',
  enabled: true,
  expertCount: 0,
  projectCount: 0,
  matchRate: 60,
  createdAt: '-',
  updatedAt: '-',
  creator: '系统'
})

const demandLevelMap: Record<string, number> = {
  LOW: 2,
  MEDIUM: 3,
  HIGH: 4,
  CRITICAL: 5
}

const normalizeCategory = (category?: string) => String(category || 'other').toLowerCase()

const adaptRelatedSkill = (rawSkill: RawSkill): RelatedSkill => ({
  id: rawSkill.id,
  name: rawSkill.name || `技能 ${rawSkill.id}`,
  expertCount: Number(rawSkill.expertCount || 0),
  projectCount: Number(rawSkill.projectCount || 0),
  category: normalizeCategory(rawSkill.category),
  domainId: rawSkill.domain?.id
})

const adaptSkill = (rawSkill: RawSkill): SkillViewModel => {
  const category = normalizeCategory(rawSkill.category)
  const domainName = rawSkill.domain?.name
  const tags =
    Array.isArray(rawSkill.tags) && rawSkill.tags.length > 0
      ? rawSkill.tags.map((tag) => tag.name).filter(Boolean) as string[]
      : ([rawSkill.name, category, domainName].filter(Boolean) as string[])
  const uniqueTags = Array.from(new Set(tags))
  const documentationText = rawSkill.documentation?.content || rawSkill.documentation?.url || ''

  return {
    id: rawSkill.id,
    name: rawSkill.name || `技能 ${rawSkill.id}`,
    category,
    description: rawSkill.description || '暂无描述',
    demandLevel: demandLevelMap[String(rawSkill.demandLevel || '').toUpperCase()] || 3,
    tags: uniqueTags,
    documentation: documentationText,
    enabled: rawSkill.isActive ?? true,
    expertCount: Number(rawSkill.expertCount || 0),
    projectCount: Number(rawSkill.projectCount || 0),
    matchRate: Math.min(95, 60 + Number(rawSkill.expertCount || 0) * 5 + Number(rawSkill.projectCount || 0) * 3),
    createdAt: rawSkill.createdAt || '-',
    updatedAt: rawSkill.updatedAt || '-',
    creator: '系统',
    domainId: rawSkill.domain?.id,
    domainName
  }
}

const getCategoryType = (category: string) => {
  switch (category) {
    case 'tech':
      return 'primary'
    case 'design':
      return 'success'
    case 'product':
      return 'warning'
    case 'marketing':
      return 'danger'
    case 'data':
      return 'info'
    default:
      return 'info'
  }
}

const getCategoryText = (category: string) => {
  switch (category) {
    case 'tech':
      return '技术开发'
    case 'design':
      return '设计创意'
    case 'product':
      return '产品管理'
    case 'marketing':
      return '市场营销'
    case 'data':
      return '数据分析'
    default:
      return '其他'
  }
}

const populateRelatedLists = () => {
  const currentSkill = skill.value
  const explicitRelated = allSkills.value.filter((item) =>
    detailRelatedSkillIds.value.includes(item.id)
  )
  const candidates = allSkills.value.filter((item) => item.id !== currentSkill.id)

  relatedSkills.value = (explicitRelated.length > 0 ? explicitRelated : candidates
    .filter((item) => item.domainId === currentSkill.domainId || item.category === currentSkill.category))
    .slice(0, 6)

  sameCategorySkills.value = candidates
    .filter((item) => item.category === currentSkill.category)
    .slice(0, 5)
}

const loadSkillDetail = async () => {
  const skillId = Number(route.params.id)
  if (!skillId) {
    loadError.value = '技能 ID 无效'
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    const [detail, skillPage] = await Promise.all([
      SkillService.getSkillById(skillId),
      SkillService.getSkills({ page: 0, size: 200 })
    ])

    skill.value = adaptSkill(detail as unknown as RawSkill)
    detailRelatedSkillIds.value = Array.isArray((detail as any).relatedSkills)
      ? (detail as any).relatedSkills.map((item: any) => Number(item.id)).filter(Boolean)
      : []
    allSkills.value = (skillPage.content as unknown as RawSkill[]).map(adaptRelatedSkill)
    populateRelatedLists()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载技能详情失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const editSkill = () => {
  router.push(`/skills/${skill.value.id}/edit`)
}

const deleteSkill = () => {
  ElMessageBox.confirm('确定要删除此技能吗？删除后无法恢复。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await SkillService.deleteSkill(skill.value.id)
      ElMessage.success('技能已删除')
      router.push('/skills')
    })
    .catch(() => undefined)
}

const viewExperts = () => {
  router.push('/experts')
}

const viewProjects = () => {
  router.push('/projects')
}

const manageConfig = () => {
  router.push(`/skills/${skill.value.id}/edit`)
}

const toggleSkillStatus = async () => {
  const nextEnabled = skill.value.enabled
  try {
    await SkillService.updateSkill(skill.value.id, {
      id: skill.value.id,
      enabled: nextEnabled
    } as UpdateSkillRequest)
    ElMessage.success(nextEnabled ? '已启用技能' : '已禁用技能')
    await loadSkillDetail()
  } catch (error: any) {
    skill.value.enabled = !nextEnabled
    ElMessage.error(error?.message || '更新技能状态失败')
  }
}

const viewRelatedSkill = (skillId: number) => {
  router.push(`/skills/${skillId}`)
}

onMounted(() => {
  void loadSkillDetail()
})

watch(
  () => route.params.id,
  () => {
    void loadSkillDetail()
  }
)
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

.page-alert {
  margin-bottom: 20px;
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