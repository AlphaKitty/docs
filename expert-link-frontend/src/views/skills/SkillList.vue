<template>
  <div v-loading="loading" class="skill-list">
    <div class="page-header">
      <h2>技能领域管理</h2>
      <div class="header-actions">
        <el-button type="success" icon="Plus" @click="goToAddDomain">新增领域</el-button>
        <el-button type="primary" icon="Plus" @click="goToAddSkill">添加技能</el-button>
        <el-button icon="Setting" @click="showSkillConfig">技能配置</el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <div class="search-controls">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索技能名称或描述"
          clearable
          @keyup.enter="handleSearch"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="filterCategory" placeholder="分类筛选" clearable @change="handleFilter">
          <el-option label="技术开发" value="tech" />
          <el-option label="设计创意" value="design" />
          <el-option label="产品管理" value="product" />
          <el-option label="市场营销" value="marketing" />
          <el-option label="数据分析" value="data" />
        </el-select>
      </div>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-alert
      v-if="loadError"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
      class="page-alert"
    />
    
    <!-- <div class="skill-categories">
      <el-row :gutter="20">
        <el-col
          v-for="category in skillCategories"
          :key="category.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="category-card" shadow="hover">
            <template #header>
              <div class="category-header">
                <h3 class="category-title">{{ category.name }}</h3>
                <el-tag :type="getCategoryType(category.type)" size="small">
                  {{ getCategoryText(category.type) }}
                </el-tag>
              </div>
            </template>
            
            <div class="category-info">
              <p class="category-desc">{{ category.description }}</p>
              
              <div class="skill-stats">
                <div class="stat-item">
                  <span class="stat-label">专家数量</span>
                  <span class="stat-value">{{ category.expertCount }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">项目数量</span>
                  <span class="stat-value">{{ category.projectCount }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">匹配率</span>
                  <span class="stat-value">{{ category.matchRate }}%</span>
                </div>
              </div>
              
              <div class="top-skills">
                <h4>热门技能</h4>
                <div class="skill-tags">
                  <el-tag
                    v-for="skill in category.topSkills"
                    :key="skill"
                    size="small"
                    class="skill-tag"
                  >
                    {{ skill }}
                  </el-tag>
                </div>
              </div>
            </div>
            
            <template #footer>
              <div class="card-footer">
                <el-button type="primary" size="small" @click="viewCategory(category)">查看详情</el-button>
                <el-button type="warning" size="small" @click="editCategory(category)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteCategory(category)">删除</el-button>
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-if="!loading && skillCategories.length === 0" description="暂无领域分类数据" />
    </div> -->
    
    <div class="skill-table">
      <el-table :data="paginatedSkills" style="width: 100%" table-layout="fixed">
        <el-table-column prop="name" label="技能名称" min-width="160" align="center" show-overflow-tooltip />
        
        <el-table-column prop="category" label="分类" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small" align="center">
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" min-width="220" align="center" show-overflow-tooltip />

        <el-table-column label="标签" min-width="150" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="vertical-tag-list" style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
              <el-tag
                v-for="tag in row.visibleTags"
                :key="tag"
                size="small"
                class="skill-tag"
                style="margin-bottom: 4px; display: flex; justify-content: center;"
              >
                {{ tag }}
              </el-tag>
              <el-tag
                v-if="row.hiddenTagCount > 0"
                size="small"
                type="info"
                style="display: flex; justify-content: center;"
              >
                +{{ row.hiddenTagCount }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="expertCount" label="专家数量" width="110" align="center">
          <template #default="{ row }">
            <span class="count-badge">{{ row.expertCount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectCount" label="项目数量" width="110" align="center">
          <template #default="{ row }">
            <span class="count-badge">{{ row.projectCount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="demandLevel" label="需求等级" width="160" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.demandLevel" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>

        <el-table-column label="相关技能" width="120" align="center">
          <template #default="{ row }">
            <el-tooltip
              :content="row.relatedSkillNames.length ? row.relatedSkillNames.join('、') : '暂无相关技能'"
              placement="top"
            >
              <span class="count-badge">{{ row.relatedSkillCount }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="文档" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hasDocumentation ? 'success' : 'info'" size="small">
              {{ row.hasDocumentation ? '有文档' : '无文档' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <!-- <div class="table-actions" style="display: flex; flex-direction: column; gap: 4px;"> -->
            <div class="table-actions">
              <!-- <el-button size="small" @click="previewDocumentation(row)">文档</el-button> -->
              <el-button type="primary" size="small" :icon="View" @click="viewSkill(row)"></el-button>
              <el-button type="warning" size="small" :icon="Edit" @click="editSkill(row)"></el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="deleteSkill(row)"></el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredSkills.length === 0" description="暂无技能数据" />
    </div>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Setting, View, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DomainService, SkillService } from '@/api/services'

interface RawDomain {
  id: number
  name?: string
  description?: string
  expertCount?: number
  projectCount?: number
}

interface RawSkill {
  id: number
  name?: string
  description?: string
  category?: string
  expertCount?: number
  projectCount?: number
  demandLevel?: string
  domain?: {
    id?: number
    name?: string
  }
  tags?: Array<{ id: number; name?: string }>
  relatedSkills?: Array<{ id: number; name?: string }>
  documentation?: {
    content?: string
    url?: string
  } | null
}

interface SkillCategory {
  id: number
  name: string
  type: string
  description: string
  expertCount: number
  projectCount: number
  matchRate: number
  topSkills: string[]
}

interface SkillRow {
  id: number
  name: string
  category: string
  description: string
  expertCount: number
  projectCount: number
  demandLevel: number
  domainId?: number
  domainName?: string
  tags: string[]
  visibleTags: string[]
  hiddenTagCount: number
  relatedSkillNames: string[]
  relatedSkillCount: number
  documentation: string
  hasDocumentation: boolean
}

const router = useRouter()

const loading = ref(false)
const loadError = ref('')
const searchKeyword = ref('')
const filterCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const allSkills = ref<SkillRow[]>([])
const allDomains = ref<RawDomain[]>([])

const demandLevelMap: Record<string, number> = {
  LOW: 2,
  MEDIUM: 3,
  HIGH: 4,
  CRITICAL: 5
}

const normalizeCategory = (category?: string) => String(category || 'other').toLowerCase()

const adaptSkill = (skill: RawSkill): SkillRow => ({
  id: skill.id,
  name: skill.name || `技能 ${skill.id}`,
  category: normalizeCategory(skill.category),
  description: skill.description || '暂无描述',
  expertCount: Number(skill.expertCount || 0),
  projectCount: Number(skill.projectCount || 0),
  demandLevel: demandLevelMap[String(skill.demandLevel || '').toUpperCase()] || 3,
  domainId: skill.domain?.id,
  domainName: skill.domain?.name,
  tags: Array.isArray(skill.tags) ? skill.tags.map((tag) => tag.name).filter(Boolean) as string[] : [],
  visibleTags: Array.isArray(skill.tags)
    ? (skill.tags.map((tag) => tag.name).filter(Boolean) as string[]).slice(0, 3)
    : [],
  hiddenTagCount: Array.isArray(skill.tags) ? Math.max(skill.tags.length - 3, 0) : 0,
  relatedSkillNames: Array.isArray(skill.relatedSkills)
    ? (skill.relatedSkills.map((related) => related.name).filter(Boolean) as string[])
    : [],
  relatedSkillCount: Array.isArray(skill.relatedSkills) ? skill.relatedSkills.length : 0,
  documentation: skill.documentation?.content || skill.documentation?.url || '',
  hasDocumentation: Boolean(skill.documentation?.content || skill.documentation?.url)
})

const buildCategoryCards = (domains: RawDomain[], skills: SkillRow[]): SkillCategory[] =>
  domains.map((domain) => {
    const domainSkills = skills.filter((skill) => skill.domainId === domain.id)
    const uniqueTopSkills = Array.from(new Set(domainSkills.map((skill) => skill.name))).slice(0, 5)
    const matchRateBase = uniqueTopSkills.length > 0 ? 70 : 60

    return {
      id: domain.id,
      name: domain.name || `领域 ${domain.id}`,
      type: domainSkills[0]?.category || 'other',
      description: domain.description || '暂无领域描述',
      expertCount: Number(domain.expertCount || 0),
      projectCount: Number(domain.projectCount || 0),
      matchRate: Math.min(95, matchRateBase + uniqueTopSkills.length * 5),
      topSkills: uniqueTopSkills
    }
  })

const filteredSkills = computed(() => {
  const keyword = searchKeyword.value.trim()

  return allSkills.value.filter((skill) => {
    const matchesKeyword =
      !keyword ||
      skill.name.includes(keyword) ||
      skill.description.includes(keyword) ||
      (skill.domainName || '').includes(keyword)

    const matchesCategory = !filterCategory.value || skill.category === filterCategory.value
    return matchesKeyword && matchesCategory
  })
})

const paginatedSkills = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredSkills.value.slice(start, start + pageSize.value)
})

const skillCategories = computed(() => buildCategoryCards(allDomains.value, allSkills.value))
const total = computed(() => filteredSkills.value.length)

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

const loadData = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const [skillPage, domainPage] = await Promise.all([
      SkillService.getSkills({ page: 0, size: 200 }),
      DomainService.getDomains({ page: 0, size: 200 })
    ])

    allSkills.value = (skillPage.content as unknown as RawSkill[]).map(adaptSkill)
    allDomains.value = domainPage.content as unknown as RawDomain[]
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载技能领域数据失败'
    allSkills.value = []
    allDomains.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

const goToAddSkill = () => {
  router.push('/skills/add')
}

const goToAddDomain = () => {
  router.push('/domains/add')
}

const showSkillConfig = () => {
  const first = filteredSkills.value[0]
  if (!first) {
    ElMessage.warning('当前列表为空，请先新增技能')
    return
  }
  ElMessageBox.confirm(
    '「技能配置」将打开当前筛选结果中第一条技能的编辑页。如需配置其他技能，请在列表中使用对应行的「编辑」。',
    '技能配置',
    {
      confirmButtonText: '打开编辑',
      cancelButtonText: '取消',
      type: 'info'
    }
  )
    .then(() => {
      router.push(`/skills/${first.id}/edit`)
    })
    .catch(() => undefined)
}

const viewCategory = (category: SkillCategory) => {
  router.push(`/domains/${category.id}`)
}

const editCategory = (category: SkillCategory) => {
  router.push(`/domains/${category.id}/edit`)
}

const deleteCategory = (category: SkillCategory) => {
  ElMessageBox.confirm(`确定要删除分类 "${category.name}" 吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await DomainService.deleteDomain(category.id)
      ElMessage.success('领域删除成功')
      await loadData()
    })
    .catch(() => undefined)
}

const viewSkill = (skill: SkillRow) => {
  router.push(`/skills/${skill.id}`)
}

const editSkill = (skill: SkillRow) => {
  router.push(`/skills/${skill.id}/edit`)
}

const deleteSkill = (skill: SkillRow) => {
  ElMessageBox.confirm(`确定要删除技能 "${skill.name}" 吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await SkillService.deleteSkill(skill.id)
      ElMessage.success('技能删除成功')
      await loadData()
    })
    .catch(() => undefined)
}

const previewDocumentation = (skill: SkillRow) => {
  ElMessageBox.alert(skill.documentation || '暂无文档', `${skill.name} 文档`, {
    confirmButtonText: '关闭'
  })
}

onMounted(() => {
  void loadData()
})
</script>

<style scoped>
.skill-list {
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
  color: #333;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
}

.search-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.search-controls :deep(.el-input) {
  width: 320px;
}

.search-controls :deep(.el-select) {
  width: 220px;
}

.page-alert {
  margin-bottom: 20px;
}

.skill-categories {
  margin-bottom: 30px;
}

.category-card {
  margin-bottom: 20px;
  height: 100%;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.category-info {
  min-height: 180px;
}

.category-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
}

.skill-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.top-skills h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #333;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.skill-tag {
  margin-bottom: 4px;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-start;
}

.skill-table {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.count-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #f0f2f5;
  border-radius: 10px;
  font-size: 12px;
  color: #333;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.inline-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.table-actions {
  display: inline-flex;
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
  vertical-align: middle;
}

.table-actions :deep(.el-button) {
  margin: 0;
}
</style>