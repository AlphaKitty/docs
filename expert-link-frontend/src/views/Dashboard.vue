<template>
  <div class="dashboard" v-loading="expertStore.loading">
    <div class="page-header">
      <h2>专家库</h2>
    </div>

    <el-card class="filter-card">
      <div class="filter-grid">
        <div v-for="item in filterFields" :key="item.key" class="filter-item">
          <label>{{ item.label }}</label>
          <el-input v-model="filters[item.key]" placeholder="输入关键字" clearable />
        </div>

        <div class="filter-item">
          <label>BG</label>
          <el-select v-model="filters.bg" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option label="A组" value="A组" />
            <el-option label="B组" value="B组" />
          </el-select>
        </div>
      </div>

      <div class="filter-actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" @click="triggerSearch">搜索</el-button>
      </div>
    </el-card>

    <div class="content-grid">
      <el-card class="group-card">
        <template #header>
          <div class="card-header">
            <span>专家库（按领域分组）</span>
            <span class="sub-text">默认折叠，点击展开查看</span>
          </div>
        </template>

        <el-collapse v-model="activeGroups">
          <el-collapse-item
            v-for="group in groupedExperts"
            :key="group.name"
            :name="group.name"
          >
            <template #title>
              <div class="group-title">
                <span>{{ group.name }}</span>
                <el-tag size="small" type="info">{{ group.experts.length }} 人</el-tag>
              </div>
            </template>

            <div class="expert-card-list">
              <div
                v-for="expert in group.experts"
                :key="expert.id"
                class="expert-card"
                @click="goToExpertProfile(expert.id)"
              >
                <div class="expert-left">
                  <el-avatar :size="42" :src="expert.avatar" />
                  <div class="expert-main">
                    <div class="name-row">
                      <span class="name">{{ expert.name }}</span>
                      <el-tag :type="getStatusType(expert.status)" size="small">
                        {{ getStatusText(expert.status) }}
                      </el-tag>
                    </div>
                    <div class="meta">{{ expert.title || '未填写职位' }}</div>
                    <div class="skills">
                      <el-tag
                        v-for="skill in expert.skills.slice(0, 4)"
                        :key="skill"
                        size="small"
                        class="skill-tag"
                      >
                        {{ skill }}
                      </el-tag>
                    </div>
                  </div>
                </div>
                <div class="expert-right">
                  <span class="score-label">评分</span>
                  <span class="score">{{ Number(expert.rating || 0).toFixed(1) }}</span>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>

        <el-empty v-if="groupedExperts.length === 0" description="暂无匹配专家" />
      </el-card>

      <el-card class="rank-card">
        <template #header>
          <div class="card-header">
            <span>积分排名</span>
          </div>
        </template>

        <div v-if="rankingExperts.length" class="ranking-list">
          <div v-for="(item, index) in rankingExperts" :key="item.id" class="ranking-item">
            <div class="rank-index" :class="{ top3: index < 3 }">{{ index + 1 }}</div>
            <div class="rank-main">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-meta">{{ item.title || '未填写职位' }}</div>
            </div>
            <div class="rank-score">{{ item.score }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无排名数据" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useExpertStore } from '@/stores/expert'

type FilterKey =
  | 'jobNo'
  | 'domain'
  | 'direction'
  | 'project'
  | 'region'
  | 'name'
  | 'tech'
  | 'department'
  | 'client'

const router = useRouter()
const expertStore = useExpertStore()
const activeGroups = ref<string[]>([])

const filterFields: Array<{ label: string; key: FilterKey }> = [
  { label: '工号', key: 'jobNo' },
  { label: '领域', key: 'domain' },
  { label: '方向', key: 'direction' },
  { label: '项目', key: 'project' },
  { label: '地域', key: 'region' },
  { label: '姓名', key: 'name' },
  { label: '技术', key: 'tech' },
  { label: '部门', key: 'department' },
  { label: '客户', key: 'client' }
]

const filters = reactive<Record<FilterKey | 'bg', string>>({
  jobNo: '',
  domain: '',
  direction: '',
  project: '',
  region: '',
  name: '',
  tech: '',
  department: '',
  client: '',
  bg: ''
})

const normalizedExperts = computed(() => {
  return expertStore.experts.filter((expert) => {
    const fullText = [
      expert.name,
      expert.title,
      expert.company,
      expert.email,
      expert.phone,
      ...(expert.skills || []),
      ...(expert.domains || [])
    ]
      .join(' ')
      .toLowerCase()

    return filterFields.every((field) => {
      const keyword = filters[field.key].trim().toLowerCase()
      return !keyword || fullText.includes(keyword)
    })
  })
})

const groupedExperts = computed(() => {
  const groupMap = new Map<string, typeof normalizedExperts.value>()
  normalizedExperts.value.forEach((expert) => {
    const domains = expert.domains?.length ? expert.domains : ['未分配领域']
    domains.forEach((domain) => {
      const list = groupMap.get(domain) || []
      list.push(expert)
      groupMap.set(domain, list)
    })
  })

  return [...groupMap.entries()]
    .map(([name, experts]) => ({ name, experts }))
    .sort((a, b) => b.experts.length - a.experts.length)
})

const rankingExperts = computed(() => {
  return [...expertStore.experts]
    .map((item) => ({
      id: item.id,
      name: item.name,
      title: item.title,
      score: Math.round(Number(item.rating || 0) * 20 + Number(item.experience || 0) * 2)
    }))
    .sort((a, b) => b.score - a.score)
    .slice(0, 8)
})

const triggerSearch = () => {
  activeGroups.value = []
}

const resetFilters = () => {
  filters.jobNo = ''
  filters.domain = ''
  filters.direction = ''
  filters.project = ''
  filters.region = ''
  filters.name = ''
  filters.tech = ''
  filters.department = ''
  filters.client = ''
  filters.bg = ''
  activeGroups.value = []
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'available':
      return 'success'
    case 'busy':
      return 'warning'
    case 'unavailable':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'available':
      return '可用'
    case 'busy':
      return '忙碌'
    case 'unavailable':
      return '不可用'
    default:
      return '未知'
  }
}

const goToExpertProfile = (expertId: number) => {
  router.push(`/dashboard/expert-profile/${expertId}`)
}

onMounted(async () => {
  if (expertStore.experts.length) return
  try {
    await expertStore.fetchExperts()
  } catch {
    ElMessage.error('加载专家库数据失败')
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px 12px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  min-width: 34px;
  color: #606266;
  font-size: 13px;
}

.filter-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.sub-text {
  color: #909399;
  font-size: 12px;
  font-weight: 400;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.expert-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.expert-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.expert-card:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  border-color: #dcdfe6;
}

.expert-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.expert-main {
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  color: #303133;
  font-weight: 600;
}

.meta {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.skills {
  margin-top: 6px;
}

.skill-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.expert-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
}

.score-label {
  font-size: 12px;
  color: #909399;
}

.score {
  color: #409eff;
  font-size: 22px;
  line-height: 1.1;
  font-weight: 700;
}

.rank-card {
  height: fit-content;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  background: #f5f7fa;
}

.rank-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #dcdfe6;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.rank-index.top3 {
  background: #f56c6c;
  color: #fff;
}

.rank-main {
  min-width: 0;
  flex: 1;
}

.rank-name {
  font-weight: 600;
  color: #303133;
}

.rank-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.rank-score {
  color: #409eff;
  font-weight: 700;
}

@media (max-width: 1200px) {
  .filter-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .expert-card-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
