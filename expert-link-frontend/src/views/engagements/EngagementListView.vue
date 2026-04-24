<template>
  <div v-loading="loading" class="page">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-button v-if="kind === 'mine'" type="primary" @click="$router.push('/engagements/new')">新建申请</el-button>
    </div>
    <el-table :data="rows" style="width: 100%">
      <el-table-column prop="referenceCode" label="申请编号" width="160">
        <template #default="{ row }">
          {{ row.referenceCode || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column prop="domainName" label="领域" width="140" />
      <el-table-column prop="status" label="状态" width="160">
        <template #default="{ row }">
          <el-tag size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="taskType" label="类型" width="140" />
      <el-table-column prop="applicantUsername" v-if="kind !== 'mine'" label="申请人" width="120" />
      <el-table-column label="指派专家" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{
            row.assignedExpertNames?.length
              ? row.assignedExpertNames.join('、')
              : row.assignedExpertName || '—'
          }}
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push(`/engagements/${row.id}`)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && rows.length === 0" :description="emptyText" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { EngagementRequestService } from '@/api/services/engagement-request.service'
import type { EngagementRequestRow } from '@/api/types/engagement'

const route = useRoute()
const kind = computed(() => (route.meta.engagementList as 'mine' | 'steward' | 'expert') || 'mine')

const title = computed(() => {
  if (kind.value === 'steward') return '行管待办'
  if (kind.value === 'expert') return '专家待确认'
  return '我的调⽤申请'
})

const emptyText = computed(() => {
  if (kind.value === 'steward') return '暂无行管待办'
  if (kind.value === 'expert') return '暂无待确认任务'
  return '暂无申请，点击右上角新建'
})

const loading = ref(false)
const rows = ref<EngagementRequestRow[]>([])

function statusText(s: string) {
  const m: Record<string, string> = {
    DRAFT: '草稿',
    PENDING_STEWARD_ASSIGN: '待行管指派',
    PENDING_EXPERT_CONFIRM: '待专家确认',
    IN_PROGRESS: '执行中',
    PENDING_STEWARD_SCORE_RELEASE: '待行管放分',
    COMPLETED: '已结项',
    REJECTED: '已驳回',
  }
  return m[s] || s
}

async function load() {
  loading.value = true
  try {
    const page =
      kind.value === 'steward'
        ? await EngagementRequestService.stewardQueue(0, 50)
        : kind.value === 'expert'
          ? await EngagementRequestService.expertPending(0, 50)
          : await EngagementRequestService.listMine(0, 50)
    rows.value = page.content || []
  } catch (e: unknown) {
    rows.value = []
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
  padding: 16px;
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
</style>
