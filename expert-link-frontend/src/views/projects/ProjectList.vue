<template>
  <div v-loading="projectStore.loading" class="project-list">
    <div class="page-header">
      <h2>项目管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="goToAddProject">新建项目</el-button>
        <el-button icon="Refresh" @click="refreshProjects">刷新</el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <div class="search-controls">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目名称或描述"
          clearable
          @keyup.enter="handleSearch"
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
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
    
    <div class="project-table">
      <el-table :data="paginatedProjects" style="width: 100%" table-layout="fixed">
        <el-table-column prop="name" label="项目名称" min-width="200" align="center" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="周期" width="180" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.startDate || '-' }} ~ {{ row.endDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="120" align="center" show-overflow-tooltip />
        <el-table-column label="预算" width="110" align="center">
          <template #default="{ row }">
            ¥{{ Number(row.budget || 0).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="进度" width="140" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.progress" :status="getProgressStatus(row.progress)" />
          </template>
        </el-table-column>
        <el-table-column label="关键技能" min-width="180" align="center">
          <template #default="{ row }">
            <el-tag
              v-for="skill in row.requiredSkills.slice(0, 3)"
              :key="skill"
              size="small"
              type="info"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <span v-if="row.requiredSkills.length > 3" class="more-skills">+{{ row.requiredSkills.length - 3 }}</span>
            <span v-if="row.requiredSkills.length === 0" class="more-skills">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip align="center" />
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" size="small" :icon="View" circle @click="viewProject(row)" />
              <el-button type="warning" size="small" :icon="Edit" circle @click="editProject(row)" />
              <el-button type="danger" size="small" :icon="Delete" circle @click="deleteProject(row)" />
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!projectStore.loading && filteredProjects.length === 0" description="暂无项目数据" />
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
import { Search, Plus, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useProjectStore, type Project } from '@/stores/project'

const router = useRouter()
const projectStore = useProjectStore()

const searchKeyword = ref('')
const filterStatus = ref('')
const dateRange = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

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
const paginatedProjects = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredProjects.value.slice(start, start + pageSize.value)
})

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
  if (progress >= 70) return ''
  return 'warning'
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
  projectStore.fetchProjects()
    .then(() => ElMessage.success('数据已刷新'))
    .catch(() => ElMessage.error('刷新项目列表失败'))
}

const viewProject = (project: Project) => {
  router.push(`/projects/${project.id}`)
}

const editProject = (project: Project) => {
  router.push(`/projects/${project.id}/edit`)
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
  ).then(async () => {
    await projectStore.deleteProject(project.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 取消删除
  })
}

onMounted(async () => {
  try {
    await projectStore.fetchProjects()
  } catch (error: unknown) {
    ElMessage.error((error as Error)?.message || '加载项目列表失败')
  }
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
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.search-controls {
  display: flex;
  gap: 12px;
  flex: 1;
  align-items: center;
  min-width: 0;
}

.search-controls :deep(.el-input) {
  width: 320px;
}

.search-controls :deep(.el-select) {
  width: 180px;
}

.search-controls :deep(.el-date-editor) {
  width: 340px;
}

.project-table {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.skill-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.more-skills {
  color: #999;
  font-size: 12px;
  margin-left: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.table-actions {
  display: inline-flex;
  gap: 6px;
  align-items: center;
}

.table-actions :deep(.el-button) {
  margin: 0;
}
</style>