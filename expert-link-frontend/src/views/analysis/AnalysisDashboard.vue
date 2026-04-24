<template>
  <div v-loading="pageLoading" class="analysis-dashboard">
    <div class="page-header">
      <h2>统计分析</h2>
      <div class="header-actions">
        <el-button-group>
          <el-button :type="timeRange === 'week' ? 'primary' : ''" @click="timeRange = 'week'">近7天</el-button>
          <el-button :type="timeRange === 'month' ? 'primary' : ''" @click="timeRange = 'month'">近1月</el-button>
          <el-button :type="timeRange === 'quarter' ? 'primary' : ''" @click="timeRange = 'quarter'">近3月</el-button>
          <el-button :type="timeRange === 'year' ? 'primary' : ''" @click="timeRange = 'year'">近1年</el-button>
        </el-button-group>
        <el-date-picker
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="onCustomRange"
        />
      </div>
    </div>

    <p class="range-hint">
      匹配相关图表可按分配开始日期筛选（选择日期范围后生效）。专家/项目/技能卡片数据为全量汇总。
    </p>

    <el-alert
      v-if="loadFailures.length"
      :title="`以下数据源加载失败：${loadFailures.join('、')}。其余区块仍展示可用数据。`"
      type="warning"
      show-icon
      :closable="false"
      class="page-alert"
    />

    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409eff">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.expertCount }}</div>
                <div class="stat-label">专家总数</div>
              </div>
            </div>
            <div class="stat-foot">来自 /api/experts 全量分页汇总</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67c23a">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.projectCount }}</div>
                <div class="stat-label">进行中项目</div>
              </div>
            </div>
            <div class="stat-foot">状态为 IN_PROGRESS 的项目数</div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6a23c">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.skillCount }}</div>
                <div class="stat-label">技能条目</div>
              </div>
            </div>
            <div class="stat-foot">
              领域数 {{ stats.domainCount }}
              <span v-if="domainsFailed" class="warn">（领域数据加载失败）</span>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f56c6c">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.matchRateDisplay }}</div>
                <div class="stat-label">项目分配覆盖率</div>
              </div>
            </div>
            <div class="stat-foot">已有分配的项目数 / 项目总数</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>专家分布统计</h3>
                <el-select v-model="expertChartType" size="small" style="width: 120px">
                  <el-option label="按技能" value="skill" />
                  <el-option label="按状态" value="status" />
                  <el-option label="按公司" value="company" />
                </el-select>
              </div>
            </template>
            <div ref="expertChartEl" class="chart-host" />
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>项目进度统计</h3>
                <el-select v-model="projectChartType" size="small" style="width: 120px">
                  <el-option label="按状态" value="status" />
                  <el-option label="按预算" value="budget" />
                  <el-option label="按时长" value="duration" />
                </el-select>
              </div>
            </template>
            <div ref="projectChartEl" class="chart-host" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mt-20">
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>技能需求分布</h3>
                <el-select v-model="skillChartType" size="small" style="width: 120px">
                  <el-option label="按热度" value="hot" />
                  <el-option label="按需求等级" value="demand" />
                  <el-option label="按领域" value="domain" />
                </el-select>
              </div>
            </template>
            <div ref="skillChartEl" class="chart-host" />
            <p v-if="skillChartType === 'domain' && domainsFailed" class="chart-footnote">领域维度已降级：仅使用技能上嵌套的领域名称。</p>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="chart-header">
                <h3>项目匹配分布</h3>
                <el-select v-model="matchChartType" size="small" style="width: 130px">
                  <el-option label="覆盖率" value="coverage" />
                  <el-option label="按状态" value="status" />
                  <el-option label="完成度分段" value="completion" />
                </el-select>
              </div>
            </template>
            <div ref="matchChartEl" class="chart-host" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="tables-section">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="12">
          <el-card class="table-card" shadow="hover">
            <template #header>
              <h3>热门技能排行榜</h3>
            </template>
            <el-empty
              v-if="skillsFailed || topSkills.length === 0"
              :description="skillsFailed ? '技能数据加载失败' : '暂无技能数据'"
            />
            <el-table v-else :data="topSkills" style="width: 100%" height="300">
              <el-table-column prop="rank" label="排名" width="60" align="center">
                <template #default="{ row }">
                  <span class="rank-badge" :class="getRankClass(row.rank)">
                    {{ row.rank }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="name" label="技能名称" align="center" />
              <el-table-column prop="expertCount" label="专家数量" width="100" align="center" />
              <el-table-column prop="projectCount" label="项目数量" width="100" align="center" />
              <el-table-column prop="demandLevel" label="需求等级" width="100" align="center">
                <template #default="{ row }">
                  <el-rate :model-value="row.demandLevel" disabled size="small" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="table-card" shadow="hover">
            <template #header>
              <h3>最近匹配记录</h3>
            </template>
            <el-empty v-if="recentMatches.length === 0" description="暂无项目-专家分配记录" />
            <el-table v-else :data="recentMatches" style="width: 100%" height="300">
              <el-table-column prop="projectName" label="项目名称" align="center" />
              <el-table-column prop="expertName" label="专家姓名" width="120" align="center" />
              <el-table-column prop="scoreDisplay" label="匹配度" width="110" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.scoreIsPercent" :type="scoreTagType(row.scoreDisplay)">
                    {{ row.scoreDisplay }}
                  </el-tag>
                  <el-tag v-else type="info">{{ row.scoreDisplay }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="matchDate" label="匹配时间" width="120" align="center" />
              <el-table-column prop="statusLabel" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="assignmentTagType(row.statusRaw)" size="small">
                    {{ row.statusLabel }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { User, Document, Star, TrendCharts } from '@element-plus/icons-vue'
import type { ExpertDetail } from '@/api/types/expert'
import type { ProjectDetail } from '@/api/types/project'
import type { SkillDetail } from '@/api/types/skill'
import type { DomainDetail } from '@/api/types/domain'
import { ExpertService } from '@/api/services/expert.service'
import { ProjectService } from '@/api/services/project.service'
import { SkillService } from '@/api/services/skill.service'
import { DomainService } from '@/api/services/domain.service'
import { ProjectExpertService, type ProjectExpertAssignment } from '@/api/services/project-expert.service'
import {
  fetchAllPaginated,
  dedupeAssignments,
  buildTopSkills,
  buildRecentMatches,
  aggregateExpertSkillChart,
  aggregateExpertStatusChart,
  aggregateExpertCompanyChart,
  aggregateProjectStatusChart,
  aggregateProjectBudgetChart,
  aggregateProjectDurationChart,
  aggregateSkillHotChart,
  aggregateSkillDemandChart,
  aggregateSkillDomainChart,
  aggregateMatchCoverageChart,
  aggregateMatchStatusChart,
  aggregateMatchCompletionChart,
  filterAssignmentsByTimeRange,
} from './analysis-dashboard-aggregate'

const timeRange = ref<'week' | 'month' | 'quarter' | 'year' | 'custom'>('month')
const customDateRange = ref<string[]>([])

const expertChartType = ref<'skill' | 'status' | 'company'>('skill')
const projectChartType = ref<'status' | 'budget' | 'duration'>('status')
const skillChartType = ref<'hot' | 'demand' | 'domain'>('hot')
const matchChartType = ref<'coverage' | 'status' | 'completion'>('coverage')

const pageLoading = ref(false)
const loadFailures = ref<string[]>([])
const skillsFailed = ref(false)
const domainsFailed = ref(false)

const experts = ref<ExpertDetail[]>([])
const projects = ref<ProjectDetail[]>([])
const skills = ref<SkillDetail[]>([])
const domains = ref<DomainDetail[]>([])
const allAssignments = ref<ProjectExpertAssignment[]>([])

const dedupedAssignments = computed(() => dedupeAssignments(allAssignments.value))

const timeFilteredAssignments = computed(() => {
  const mode = timeRange.value === 'custom' && customDateRange.value?.length === 2 ? 'custom' : timeRange.value
  return filterAssignmentsByTimeRange(dedupedAssignments.value, mode, customDateRange.value)
})

const stats = computed(() => {
  const totalProjects = projects.value.length
  const assigned = new Set(dedupedAssignments.value.map((a) => a.projectId)).size
  const matchRate =
    totalProjects > 0 ? Math.round((assigned / totalProjects) * 1000) / 10 : null
  return {
    expertCount: experts.value.length,
    projectCount: projects.value.filter((p) => p.status === 'IN_PROGRESS').length,
    skillCount: skills.value.length,
    domainCount: domains.value.length,
    matchRateDisplay: matchRate == null ? '--' : `${matchRate}%`,
  }
})

const topSkills = computed(() => (skillsFailed.value ? [] : buildTopSkills(skills.value, experts.value, projects.value)))

const recentMatches = computed(() => {
  const pMap = new Map(projects.value.map((p) => [p.id, p]))
  const eMap = new Map(experts.value.map((e) => [e.id, e]))
  return buildRecentMatches(timeFilteredAssignments.value, pMap, eMap)
})

function onCustomRange() {
  if (customDateRange.value?.length === 2) {
    timeRange.value = 'custom'
  }
}

function getRankClass(rank: number) {
  if (rank <= 3) return 'rank-gold'
  if (rank <= 6) return 'rank-silver'
  return 'rank-bronze'
}

function scoreTagType(display: string) {
  const n = parseFloat(display.replace('%', ''))
  if (Number.isNaN(n)) return 'info'
  if (n >= 90) return 'success'
  if (n >= 80) return 'primary'
  if (n >= 70) return 'warning'
  return 'danger'
}

function assignmentTagType(status: string) {
  const s = status.toUpperCase()
  if (s === 'COMPLETED' || s === 'SUCCESS' || s === 'CLOSED') return 'success'
  if (s === 'PENDING' || s === 'ACTIVE' || s === 'IN_PROGRESS') return 'warning'
  if (s === 'CANCELLED' || s === 'REJECTED' || s === 'FAILED') return 'danger'
  return 'info'
}

const expertChartEl = ref<HTMLDivElement | null>(null)
const projectChartEl = ref<HTMLDivElement | null>(null)
const skillChartEl = ref<HTMLDivElement | null>(null)
const matchChartEl = ref<HTMLDivElement | null>(null)

let expertChart: ECharts | null = null
let projectChart: ECharts | null = null
let skillChart: ECharts | null = null
let matchChart: ECharts | null = null

function disposeCharts() {
  expertChart?.dispose()
  projectChart?.dispose()
  skillChart?.dispose()
  matchChart?.dispose()
  expertChart = null
  projectChart = null
  skillChart = null
  matchChart = null
}

function barOption(categories: string[], values: number[], yName = '数量') {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, bottom: 72, top: 28 },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { rotate: 28, interval: 0, fontSize: 11 },
    },
    yAxis: { type: 'value', name: yName },
    series: [{ type: 'bar', data: values, itemStyle: { color: '#409EFF' } }],
  }
}

function pieOption(items: { name: string; value: number }[]) {
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['36%', '62%'],
        data: items.map((d) => ({ name: d.name, value: d.value })),
      },
    ],
  }
}

function renderCharts() {
  const eList = experts.value
  const pList = projects.value
  const sList = skills.value
  const dList = domains.value
  const matchRows = timeFilteredAssignments.value

  if (expertChartEl.value) {
    if (!expertChart) expertChart = echarts.init(expertChartEl.value)
    let rows: { name: string; value: number }[] = []
    if (expertChartType.value === 'skill') rows = aggregateExpertSkillChart(eList)
    else if (expertChartType.value === 'status') rows = aggregateExpertStatusChart(eList)
    else rows = aggregateExpertCompanyChart(eList)
    expertChart.setOption(barOption(rows.map((r) => r.name), rows.map((r) => r.value)), true)
  }

  if (projectChartEl.value) {
    if (!projectChart) projectChart = echarts.init(projectChartEl.value)
    let rows: { name: string; value: number }[] = []
    if (projectChartType.value === 'status') rows = aggregateProjectStatusChart(pList)
    else if (projectChartType.value === 'budget') rows = aggregateProjectBudgetChart(pList)
    else rows = aggregateProjectDurationChart(pList)
    projectChart.setOption(barOption(rows.map((r) => r.name), rows.map((r) => r.value)), true)
  }

  if (skillChartEl.value) {
    if (!skillChart) skillChart = echarts.init(skillChartEl.value)
    let rows: { name: string; value: number }[] = []
    if (skillChartType.value === 'hot') rows = aggregateSkillHotChart(sList, eList, pList)
    else if (skillChartType.value === 'demand') rows = aggregateSkillDemandChart(sList)
    else rows = aggregateSkillDomainChart(sList, dList)
    skillChart.setOption(barOption(rows.map((r) => r.name), rows.map((r) => r.value), '指标'), true)
  }

  if (matchChartEl.value) {
    if (!matchChart) matchChart = echarts.init(matchChartEl.value)
    if (matchChartType.value === 'coverage') {
      matchChart.setOption(pieOption(aggregateMatchCoverageChart(pList, matchRows)), true)
    } else if (matchChartType.value === 'status') {
      const rows = aggregateMatchStatusChart(matchRows)
      matchChart.setOption(barOption(rows.map((r) => r.name), rows.map((r) => r.value), '分配数'), true)
    } else {
      const rows = aggregateMatchCompletionChart(matchRows)
      matchChart.setOption(barOption(rows.map((r) => r.name), rows.map((r) => r.value), '分配数'), true)
    }
  }
}

async function loadAssignments(projectIds: number[]) {
  const chunk = 6
  const merged: ProjectExpertAssignment[] = []
  for (let i = 0; i < projectIds.length; i += chunk) {
    const slice = projectIds.slice(i, i + chunk)
    const settled = await Promise.allSettled(slice.map((id) => ProjectExpertService.getAssignmentsByProjectId(id)))
    for (const s of settled) {
      if (s.status === 'fulfilled') merged.push(...s.value)
    }
  }
  return merged
}

async function loadAll() {
  pageLoading.value = true
  loadFailures.value = []
  skillsFailed.value = false
  domainsFailed.value = false

  const fail = (label: string) => {
    if (!loadFailures.value.includes(label)) loadFailures.value.push(label)
  }

  try {
    const ex = await fetchAllPaginated((page, size) => ExpertService.getExperts({ page, size }))
    experts.value = ex.items as ExpertDetail[]
  } catch {
    experts.value = []
    fail('专家')
  }

  try {
    const pr = await fetchAllPaginated((page, size) => ProjectService.getProjects({ page, size }))
    projects.value = pr.items as ProjectDetail[]
  } catch {
    projects.value = []
    fail('项目')
  }

  try {
    const sk = await fetchAllPaginated((page, size) => SkillService.getSkills({ page, size }))
    skills.value = sk.items as SkillDetail[]
  } catch {
    skillsFailed.value = true
    skills.value = []
    fail('技能')
  }

  try {
    const dm = await fetchAllPaginated((page, size) => DomainService.getDomains({ page, size }))
    domains.value = dm.items as DomainDetail[]
  } catch {
    domainsFailed.value = true
    domains.value = []
    fail('领域')
  }

  try {
    const ids = projects.value.map((p) => p.id)
    allAssignments.value = ids.length ? await loadAssignments(ids) : []
  } catch {
    allAssignments.value = []
    fail('项目分配')
  }

  pageLoading.value = false
  await nextTick()
  renderCharts()
}

watch(
  [expertChartType, projectChartType, skillChartType, matchChartType, timeRange, customDateRange, experts, projects, skills, domains, dedupedAssignments],
  () => {
    void nextTick().then(() => renderCharts())
  }
)

onMounted(() => {
  void loadAll()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})

function resizeCharts() {
  expertChart?.resize()
  projectChart?.resize()
  skillChart?.resize()
  matchChart?.resize()
}
</script>

<style scoped>
.analysis-dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.range-hint {
  margin: 0 0 16px;
  font-size: 12px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.page-alert {
  margin-bottom: 16px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.stat-foot {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.stat-foot .warn {
  color: #e6a23c;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  margin-bottom: 20px;
  height: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-host {
  height: 300px;
  width: 100%;
}

.chart-footnote {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.tables-section {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
  height: 100%;
}

.table-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.rank-gold {
  background: #ffd700;
  color: #333;
}

.rank-silver {
  background: #c0c0c0;
  color: #333;
}

.rank-bronze {
  background: #cd7f32;
  color: white;
}

.mt-20 {
  margin-top: 20px;
}
</style>
