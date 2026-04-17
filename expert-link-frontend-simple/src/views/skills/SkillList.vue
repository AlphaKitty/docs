<template>
  <div class="skill-list">
    <div class="page-header">
      <h2>技能领域管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="goToAddSkill">添加技能</el-button>
        <el-button icon="Setting" @click="showSkillConfig">技能配置</el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索技能名称或描述"
        clearable
        style="width: 300px"
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
    
    <div class="skill-categories">
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
    </div>
    
    <div class="skill-table">
      <el-table :data="filteredSkills" style="width: 100%">
        <el-table-column prop="name" label="技能名称" width="150" />
        
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small">
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" />
        
        <el-table-column prop="expertCount" label="专家数量" width="100">
          <template #default="{ row }">
            <span class="count-badge">{{ row.expertCount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="projectCount" label="项目数量" width="100">
          <template #default="{ row }">
            <span class="count-badge">{{ row.projectCount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="demandLevel" label="需求等级" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.demandLevel" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="80" fixed="right" align="left">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" size="small" :icon="View" @click="viewSkill(row)">查看</el-button>
              <el-button type="warning" size="small" :icon="Edit" @click="editSkill(row)">编辑</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="deleteSkill(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Setting, View, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

interface Skill {
  id: number
  name: string
  category: string
  description: string
  expertCount: number
  projectCount: number
  demandLevel: number
}

const router = useRouter()

const skillCategories = ref<SkillCategory[]>([
  {
    id: 1,
    name: '人工智能',
    type: 'tech',
    description: '机器学习、深度学习、自然语言处理等相关技能',
    expertCount: 45,
    projectCount: 28,
    matchRate: 85,
    topSkills: ['机器学习', '深度学习', 'Python', 'TensorFlow', 'PyTorch']
  },
  {
    id: 2,
    name: '前端开发',
    type: 'tech',
    description: 'Web前端开发相关技能',
    expertCount: 32,
    projectCount: 42,
    matchRate: 78,
    topSkills: ['Vue.js', 'React', 'TypeScript', 'JavaScript', 'CSS']
  },
  {
    id: 3,
    name: '后端开发',
    type: 'tech',
    description: '服务器端开发相关技能',
    expertCount: 38,
    projectCount: 35,
    matchRate: 82,
    topSkills: ['Java', 'Spring Boot', 'Python', 'Node.js', '数据库']
  },
  {
    id: 4,
    name: '数据分析',
    type: 'data',
    description: '数据分析和可视化相关技能',
    expertCount: 28,
    projectCount: 25,
    matchRate: 90,
    topSkills: ['数据分析', '统计学', 'R语言', '数据可视化', 'SQL']
  }
])

const skills = ref<Skill[]>([
  { id: 1, name: '机器学习', category: 'tech', description: '机器学习算法和应用', expertCount: 25, projectCount: 18, demandLevel: 5 },
  { id: 2, name: '深度学习', category: 'tech', description: '深度学习框架和应用', expertCount: 20, projectCount: 15, demandLevel: 5 },
  { id: 3, name: 'Vue.js', category: 'tech', description: 'Vue.js前端框架', expertCount: 18, projectCount: 22, demandLevel: 4 },
  { id: 4, name: 'React', category: 'tech', description: 'React前端框架', expertCount: 14, projectCount: 20, demandLevel: 4 },
  { id: 5, name: 'Java', category: 'tech', description: 'Java后端开发', expertCount: 22, projectCount: 18, demandLevel: 4 },
  { id: 6, name: '数据分析', category: 'data', description: '数据分析和处理', expertCount: 18, projectCount: 15, demandLevel: 5 },
  { id: 7, name: '产品设计', category: 'product', description: '产品设计和规划', expertCount: 12, projectCount: 10, demandLevel: 3 },
  { id: 8, name: '市场营销', category: 'marketing', description: '市场营销策略', expertCount: 10, projectCount: 8, demandLevel: 3 }
])

const searchKeyword = ref('')
const filterCategory = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredSkills = computed(() => {
  let filtered = skills.value
  
  // 搜索过滤
  if (searchKeyword.value) {
    filtered = filtered.filter(skill => 
      skill.name.includes(searchKeyword.value) ||
      skill.description.includes(searchKeyword.value)
    )
  }
  
  // 分类过滤
  if (filterCategory.value) {
    filtered = filtered.filter(skill => skill.category === filterCategory.value)
  }
  
  return filtered
})

const total = computed(() => filteredSkills.value.length)

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

const showSkillConfig = () => {
  ElMessage.info('打开技能配置面板')
}

const viewCategory = (category: SkillCategory) => {
  ElMessage.info(`查看分类: ${category.name}`)
}

const editCategory = (category: SkillCategory) => {
  ElMessage.info(`编辑分类: ${category.name}`)
}

const deleteCategory = (category: SkillCategory) => {
  ElMessageBox.confirm(
    `确定要删除分类 "${category.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    skillCategories.value = skillCategories.value.filter(c => c.id !== category.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 取消删除
  })
}

const viewSkill = (skill: Skill) => {
  router.push(`/skills/${skill.id}`)
}

const editSkill = (skill: Skill) => {
  ElMessage.info(`编辑技能: ${skill.name}`)
}

const deleteSkill = (skill: Skill) => {
  ElMessageBox.confirm(
    `确定要删除技能 "${skill.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    skills.value = skills.value.filter(s => s.id !== skill.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 取消删除
  })
}

onMounted(() => {
  // 可以在这里加载数据
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
  gap: 16px;
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