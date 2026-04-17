<template>
  <div class="expert-list">
    <div class="page-header">
      <h2>专家管理</h2>
      <div class="header-actions">
        <el-button icon="Download">导出数据</el-button>
      </div>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索专家姓名、技能或公司"
        clearable
        style="width: 300px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="handleFilter">
        <el-option label="可用" value="available" />
        <el-option label="忙碌" value="busy" />
        <el-option label="不可用" value="unavailable" />
      </el-select>
    </div>
    
    <div class="table-container">
      <el-table :data="filteredExperts" style="width: 100%">
        <el-table-column label="头像" width="60" align="center">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" align="center">
          <template #default="{ row }">
            <span class="expert-name">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职位" width="150" align="center" />
        
        <!-- <el-table-column prop="company" label="公司" width="150" align="center" /> -->
        
        <el-table-column prop="skills" label="技能" align="center">
          <template #default="{ row }">
            <el-tag
              v-for="skill in row.skills.slice(0, 3)"
              :key="skill"
              size="small"
              class="skill-tag"
            >
              {{ skill }}
            </el-tag>
            <span v-if="row.skills.length > 3" class="more-skills">+{{ row.skills.length - 3 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="domains" label="领域" min-width="180" align="center">
          <template #default="{ row }">
            <el-tag
              v-for="d in (row.domains || []).slice(0, 3)"
              :key="d"
              size="small"
              class="skill-tag"
              type="info"
            >
              {{ d }}
            </el-tag>
            <span v-if="(row.domains || []).length > 3" class="more-skills">+{{ (row.domains || []).length - 3 }}</span>
            <span v-if="(row.domains || []).length === 0" class="more-skills">—</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="experience" label="经验" width="80" align="center">
          <template #default="{ row }">
            {{ row.experience }}年
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="rating" label="评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="80" fixed="right" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" size="small" :icon="View" @click="viewExpert(row)"></el-button>
              <!-- <el-button type="warning" size="small" :icon="Edit" @click="editExpert(row)"></el-button> -->
              <!-- <el-button type="danger" size="small" :icon="Delete" @click="deleteExpert(row)"></el-button> -->
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
import { useExpertStore, type Expert } from '@/stores/expert'
import { Search, Download, View, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const expertStore = useExpertStore()

const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredExperts = computed(() => {
  let experts = expertStore.experts
  
  // 搜索过滤
  if (searchKeyword.value) {
    experts = experts.filter(expert => 
      expert.name.includes(searchKeyword.value) ||
      expert.skills.some(skill => skill.includes(searchKeyword.value)) ||
      (expert.domains || []).some((domain) => domain.includes(searchKeyword.value)) ||
      expert.company.includes(searchKeyword.value)
    )
  }
  
  // 状态过滤
  if (filterStatus.value) {
    experts = experts.filter(expert => expert.status === filterStatus.value)
  }
  
  return experts
})

const total = computed(() => filteredExperts.value.length)

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
    case 'available': return '可用'
    case 'busy': return '忙碌'
    case 'unavailable': return '不可用'
    default: return '未知'
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

const viewExpert = (expert: Expert) => {
  router.push(`/experts/${expert.id}`)
}

const editExpert = (expert: Expert) => {
  router.push(`/experts/${expert.id}/edit`)
}

const deleteExpert = (expert: Expert) => {
  ElMessageBox.confirm(
    `确定要删除专家 "${expert.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await expertStore.deleteExpert(expert.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 取消删除
  })
}

onMounted(async () => {
  try {
    await expertStore.fetchExperts()
  } catch (error) {
    ElMessage.error('加载专家列表失败')
  }
})
</script>

<style scoped>
.expert-list {
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

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.expert-info {
  display: flex;
  align-items: center;
}

.expert-name {
  margin-left: 8px;
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
  flex-wrap: nowrap;
  gap: 8px;
  align-items: center;
  vertical-align: middle;
}

.table-actions :deep(.el-button) {
  margin: 0;
}
</style>