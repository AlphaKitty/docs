<template>
  <div class="project-list">
    <div class="page-header">
      <h2>项目管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="goToAddProject">新建项目</el-button>
        <el-button icon="Refresh" @click="refreshProjects">刷新</el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索项目名称或描述"
        clearable
        style="width: 300px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleFilter">
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
        <el-option label="待开始" value="pending" />
      </el-select>
      
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="handleDateFilter"
      />
    </div>
    
    <div class="project-cards">
      <el-row :gutter="20">
        <el-col
          v-for="project in filteredProjects"
          :key="project.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="project-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <h3 class="project-title">{{ project.name }}</h3>
                <el-tag :type="getStatusType(project.status)" size="small">
                  {{ getStatusText(project.status) }}
                </el-tag>
              </div>
            </template>
            
            <div class="project-info">
              <p class="project-desc">{{ project.description }}</p>
              
              <div class="project-meta">
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ project.startDate }} - {{ project.endDate }}</span>
                </div>
                
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span>负责人: {{ project.manager }}</span>
                </div>
                
                <div class="meta-item">
                  <el-icon><Money /></el-icon>
                  <span>预算: ¥{{ project.budget.toLocaleString() }}</span>
                </div>
              </div>
              
              <div class="progress-section">
                <div class="progress-header">
                  <span>进度</span>
                  <span>{{ project.progress }}%</span>
                </div>
                <el-progress :percentage="project.progress" :status="getProgressStatus(project.progress)" />
              </div>
              
              <div class="skill-tags">
                <el-tag
                  v-for="skill in project.requiredSkills.slice(0, 3)"
                  :key="skill"
                  size="small"
                  type="info"
                >
                  {{ skill }}
                </el-tag>
                <span v-if="project.requiredSkills.length > 3" class="more-skills">
                  +{{ project.requiredSkills.length - 3 }}
                </span>
              </div>
            </div>
            
            <template #footer>
              <div class="card-footer">
                <el-button type="primary" size="small" @click="viewProject(project)">查看详情</el-button>
                <el-button type="warning" size="small" @click="editProject(project)">编辑</el-button>
                <el-button type="danger" size="small" @click="deleteProject(project)">删除</el-button>
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div v-if="filteredProjects.length === 0" class="empty-state">
      <el-empty description="暂无项目数据" />
    </div>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
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
import { Search, Plus, Refresh, Calendar, User, Money } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useProjectStore, type Project } from '@/stores/project'

const router = useRouter()
const projectStore = useProjectStore()

const searchKeyword = ref('')
const filterStatus = ref('')
const dateRange = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(8)

const filteredProjects = computed(() => {
  let filtered = [...projectStore.projects]

  // 搜索过滤
  if (searchKeyword.value) {
    filtered = filtered.filter(project =>
      project.name.includes(searchKeyword.value) ||
      project.description.includes(searchKeyword.value) ||
      project.manager.includes(searchKeyword.value)
    )
  }

  // 状态过滤
  if (filterStatus.value) {
    filtered = filtered.filter(project => project.status === filterStatus.value)
  }

  // 日期过滤
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    filtered = filtered.filter(project =>
      project.startDate >= start && project.endDate <= end
    )
  }

  return filtered
})

const total = computed(() => filteredProjects.value.length)

const getStatusType = (status: string) => {
  switch (status) {
    case 'in_progress': return 'primary'
    case 'completed': return 'success'
    case 'cancelled': return 'danger'
    case 'pending': return 'warning'
    case 'delayed': return 'info'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'in_progress': return '进行中'
    case 'completed': return '已完成'
    case 'cancelled': return '已取消'
    case 'pending': return '待开始'
    case 'delayed': return '已延期'
    default: return '未知'
  }
}

const getProgressStatus = (progress: number) => {
  if (progress >= 100) return 'success'
  if (progress >= 70) return 'warning'
  return 'exception'
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleDateFilter = () => {
  currentPage.value = 1
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

const goToAddProject = () => {
  router.push('/projects/add')
}

const refreshProjects = () => {
  ElMessage.success('数据已刷新')
}

const viewProject = (project: Project) => {
  ElMessage.info(`查看项目: ${project.name}`)
  // 这里可以跳转到项目详情页
}

const editProject = (project: Project) => {
  ElMessage.info(`编辑项目: ${project.name}`)
  // 这里可以跳转到编辑页面
}

const deleteProject = (project: Project) => {
  ElMessageBox.confirm(
    `确定要删除项目 "${project.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    projectStore.deleteProject(project.id)
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
.project-list {
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
  flex-wrap: wrap;
}

.project-cards {
  margin-bottom: 20px;
}

.project-card {
  margin-bottom: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.project-info {
  min-height: 200px;
}

.project-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-meta {
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #666;
  font-size: 13px;
}

.meta-item .el-icon {
  margin-right: 6px;
  color: #999;
}

.progress-section {
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #666;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.more-skills {
  color: #999;
  font-size: 12px;
  margin-left: 4px;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-start;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>