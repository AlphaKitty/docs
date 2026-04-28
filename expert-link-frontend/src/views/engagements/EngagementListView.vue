<template>
  <div v-loading="loading" class="page">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-button v-if="kind === 'mine'" type="primary" @click="$router.push('/engagements/new')">新建申请</el-button>
    </div>
    <div class="table-container">
      <el-table :data="rows" style="width: 100%" table-layout="fixed">
        <el-table-column prop="referenceCode" label="申请编号" min-width="180" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{ row.referenceCode || '—' }}
        </template>
      </el-table-column>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="domainName" label="领域" width="140" show-overflow-tooltip align="center" />
        <el-table-column prop="status" label="状态" width="160" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
        <el-table-column prop="taskType" label="类型" width="140" show-overflow-tooltip align="center">
          <template #default="{ row }">
            {{ taskTypeLabel(row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantUsername" v-if="kind !== 'mine'" label="申请人" width="120" show-overflow-tooltip align="center" />
        <el-table-column label="指派专家" min-width="220" show-overflow-tooltip align="center">
        <template #default="{ row }">
          {{
            row.assignedExpertNames?.length
              ? row.assignedExpertNames.join('、')
              : row.assignedExpertName || '—'
          }}
        </template>
      </el-table-column>
        <el-table-column v-if="kind === 'expert'" label="执行记录" min-width="280" align="center">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-tag size="small" type="info">共 {{ getProgressCount(row.id) }} 条</el-tag>
              <span class="progress-snippet">{{ getProgressSnippet(row.id) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTimeDisplay(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/engagements/${row.id}`)">详情</el-button>
        </template>
        </el-table-column>
      </el-table>
    </div>
    <el-empty v-if="!loading && rows.length === 0" :description="emptyText" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EngagementRequestService } from '@/api/services/engagement-request.service'
import type { EngagementProgressLog, EngagementRequestRow } from '@/api/types/engagement'
import { formatDateTimeDisplay, taskTypeLabel } from '@/utils/display-format'

const route = useRoute()
const kind = computed(() => (route.meta.engagementList as 'mine' | 'steward' | 'expert') || 'mine')

const title = computed(() => {
  if (kind.value === 'steward') return '行管待办'
  if (kind.value === 'expert') return '专家任务'
  return '我的调⽤申请'
})

const emptyText = computed(() => {
  if (kind.value === 'steward') return '暂无行管待办'
  if (kind.value === 'expert') return '暂无待确认/执行中任务'
  return '暂无申请，点击右上角新建'
})

const loading = ref(false)
const rows = ref<EngagementRequestRow[]>([])
const progressDigestByRequestId = ref<Record<number, { count: number; latest: string }>>({})

function toTimestamp(value?: string) {
  if (!value) return 0
  const t = new Date(value).getTime()
  return Number.isFinite(t) ? t : 0
}

function sortByLatest(items: EngagementRequestRow[]) {
  return [...items].sort((a, b) => {
    const bt = toTimestamp(b.updatedAt || b.createdAt)
    const at = toTimestamp(a.updatedAt || a.createdAt)
    if (bt !== at) return bt - at
    return Number(b.id || 0) - Number(a.id || 0)
  })
}

function statusText(s: string) {
  const m: Record<string, string> = {
    DRAFT: '草稿',
    PENDING_STEWARD_ASSIGN: '待行管指派',
    PENDING_EXPERT_CONFIRM: '待专家确认',
    IN_PROGRESS: '执行中',
    PENDING_STEWARD_SCORE_RELEASE: '待行管放分',
    COMPLETED: '已结项',
    REJECTED: '已驳回',
    CANCELLED: '已取消',
  }
  return m[s] || s
}

function progressStorageKey(requestId: number): string {
  return `engagement-progress-logs:${requestId}`
}

function readLocalProgressLogs(requestId: number): EngagementProgressLog[] {
  if (typeof localStorage === 'undefined') return []
  const raw = localStorage.getItem(progressStorageKey(requestId))
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw) as EngagementProgressLog[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function getProgressCount(requestId: number): number {
  return progressDigestByRequestId.value[requestId]?.count || 0
}

function getProgressSnippet(requestId: number): string {
  return progressDigestByRequestId.value[requestId]?.latest || '暂无过程记录'
}

function makeLatestSnippet(logs: EngagementProgressLog[]): string {
  if (!logs.length) return '暂无过程记录'
  const latest = [...logs].sort((a, b) => {
    const bt = toTimestamp(b.createdAt)
    const at = toTimestamp(a.createdAt)
    if (bt !== at) return bt - at
    return b.id - a.id
  })[0]
  const text = latest?.content?.trim() || '（无内容）'
  return text.length > 30 ? `${text.slice(0, 30)}...` : text
}

async function loadProgressDigest(list: EngagementRequestRow[]): Promise<void> {
  if (kind.value !== 'expert' || !list.length) {
    progressDigestByRequestId.value = {}
    return
  }
  const pairs = await Promise.all(
    list.map(async (item) => {
      try {
        const logs = await EngagementRequestService.listProgressLogs(item.id)
        return [item.id, { count: logs.length, latest: makeLatestSnippet(logs) }] as const
      } catch {
        const localLogs = readLocalProgressLogs(item.id)
        return [item.id, { count: localLogs.length, latest: makeLatestSnippet(localLogs) }] as const
      }
    })
  )
  progressDigestByRequestId.value = Object.fromEntries(pairs)
}

async function load() {
  loading.value = true
  try {
    const page =
      kind.value === 'steward'
        ? await EngagementRequestService.stewardQueue(0, 50)
        : kind.value === 'expert'
          ? await EngagementRequestService.expertPending(0, 50, ['PENDING_EXPERT_CONFIRM', 'IN_PROGRESS'])
          : await EngagementRequestService.listMine(0, 50)
    rows.value = sortByLatest(page.content || [])
    await loadProgressDigest(rows.value)
  } catch (e: unknown) {
    rows.value = []
    progressDigestByRequestId.value = {}
    ElMessage.error((e as Error)?.message || '加载申请列表失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => kind.value,
  () => {
    void load()
  },
  { immediate: true }
)
</script>

<style scoped>
.page {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
}
.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.progress-snippet {
  color: #606266;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
