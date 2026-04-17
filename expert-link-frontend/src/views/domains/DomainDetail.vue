<template>
  <div v-loading="loading" class="domain-detail">
    <div class="page-header">
      <div>
        <h2>{{ domain.name || '领域详情' }}</h2>
        <p class="page-subtitle">查看领域基础信息、层级关系和关联技能</p>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="warning" @click="goToEdit">编辑</el-button>
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
      <el-col :xs="24" :lg="16">
        <el-card class="info-card">
          <template #header>
            <span>基础信息</span>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="领域名称">{{ domain.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="domain.isActive ? 'success' : 'info'">
                {{ domain.isActive ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="父领域">
              {{ domain.parentName || '顶级领域' }}
            </el-descriptions-item>
            <el-descriptions-item label="层级">{{ domain.level || 1 }}</el-descriptions-item>
            <el-descriptions-item label="专家数量">{{ domain.expertCount }}</el-descriptions-item>
            <el-descriptions-item label="项目数量">{{ domain.projectCount }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDate(domain.updatedAt) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(domain.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">
              {{ domain.description || '暂无描述' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <span>关联技能</span>
          </template>

          <el-empty v-if="relatedSkills.length === 0" description="该领域下暂无技能" />
          <div v-else class="skill-list">
            <el-tag
              v-for="skill in relatedSkills"
              :key="skill.id"
              class="skill-tag"
              size="default"
              @click="goToSkill(skill.id)"
            >
              {{ skill.name }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card class="info-card">
          <template #header>
            <span>子领域</span>
          </template>

          <el-empty v-if="childDomains.length === 0" description="暂无子领域" />
          <div v-else class="child-list">
            <div
              v-for="child in childDomains"
              :key="child.id"
              class="child-item"
              @click="goToDomain(child.id)"
            >
              <div class="child-name">{{ child.name }}</div>
              <div class="child-desc">{{ child.description || '暂无描述' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DomainService, SkillService } from '@/api/services'

interface DomainViewModel {
  id: number
  name: string
  description: string
  parentId?: number
  parentName?: string
  level: number
  isActive: boolean
  expertCount: number
  projectCount: number
  createdAt?: string
  updatedAt?: string
}

interface DomainSummary {
  id: number
  name: string
  description: string
  parentId?: number
}

interface SkillSummary {
  id: number
  name: string
  domainId?: number
}

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const loadError = ref('')
const allDomains = ref<DomainSummary[]>([])
const allSkills = ref<SkillSummary[]>([])
const domain = ref<DomainViewModel>({
  id: 0,
  name: '',
  description: '',
  parentId: undefined,
  parentName: '',
  level: 1,
  isActive: true,
  expertCount: 0,
  projectCount: 0,
  createdAt: '',
  updatedAt: ''
})

const domainId = computed(() => Number(route.params.id))

const childDomains = computed(() =>
  allDomains.value.filter((item) => item.parentId === domain.value.id)
)

const relatedSkills = computed(() =>
  allSkills.value.filter((item) => item.domainId === domain.value.id)
)

const adaptDomain = (raw: any, domains: DomainSummary[]): DomainViewModel => {
  const parent = domains.find((item) => item.id === raw.parentId)

  return {
    id: Number(raw.id || 0),
    name: raw.name || `领域 ${raw.id}`,
    description: raw.description || '',
    parentId: raw.parentId ?? undefined,
    parentName: parent?.name || '',
    level: Number(raw.level || 1),
    isActive: raw.isActive ?? true,
    expertCount: Number(raw.expertCount || 0),
    projectCount: Number(raw.projectCount || 0),
    createdAt: raw.createdAt || '',
    updatedAt: raw.updatedAt || ''
  }
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 19)
}

const goBack = () => {
  router.back()
}

const goToEdit = () => {
  router.push(`/domains/${domainId.value}/edit`)
}

const goToDomain = (id: number) => {
  router.push(`/domains/${id}`)
}

const goToSkill = (id: number) => {
  router.push(`/skills/${id}`)
}

const loadData = async () => {
  if (!Number.isFinite(domainId.value)) {
    loadError.value = '领域 ID 无效'
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    const [detail, stats, domainPage, skillPage] = await Promise.all([
      DomainService.getDomainById(domainId.value),
      DomainService.getDomainStats(domainId.value),
      DomainService.getDomains({ page: 0, size: 200 }),
      SkillService.getSkills({ page: 0, size: 200 }),
    ])

    allDomains.value = domainPage.content.map((item: any) => ({
      id: Number(item.id),
      name: item.name || `领域 ${item.id}`,
      description: item.description || '',
      parentId: item.parentId ?? undefined
    }))

    allSkills.value = skillPage.content.map((item: any) => ({
      id: Number(item.id),
      name: item.name || `技能 ${item.id}`,
      domainId: item.domain?.id ?? undefined
    }))
    const vm = adaptDomain(detail, allDomains.value)
    vm.expertCount = Number(stats.expertCount || 0)
    vm.projectCount = Number(stats.projectCount || 0)
    domain.value = vm
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载领域详情失败'
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadData()
})

watch(
  () => route.params.id,
  () => {
    void loadData()
  }
)
</script>

<style scoped>
.domain-detail {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.page-subtitle {
  margin: 0;
  color: #666;
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

.skill-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tag {
  cursor: pointer;
}

.child-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.child-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.child-item:hover {
  border-color: #409eff;
  background: #f5f9ff;
}

.child-name {
  font-weight: 600;
  margin-bottom: 6px;
}

.child-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
}
</style>
