<template>
  <div v-loading="loading" class="page">
    <div class="head">
      <h2>积分台账</h2>
      <el-button @click="reload">刷新</el-button>
    </div>
    <p class="hint">结项放分时按行管确认分入账专家账号关联用户，以下为本人流水。</p>
    <div class="table-container">
      <el-table :data="rows" border stripe style="width: 100%" table-layout="fixed">
        <el-table-column prop="createdAt" label="时间" min-width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTimeDisplay(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="pointsDelta" label="变动" width="120" align="center" />
        <el-table-column prop="balanceAfter" label="余额" width="120" align="center" />
        <el-table-column prop="reasonCode" label="原因" min-width="220" show-overflow-tooltip align="center" />
        <el-table-column prop="engagementRequestId" label="申请单" width="140" align="center">
        <template #default="{ row }">
          <el-link
            v-if="row.engagementRequestId"
            type="primary"
            @click="$router.push(`/engagements/${row.engagementRequestId}`)"
          >
            #{{ row.engagementRequestId }}
          </el-link>
          <span v-else>—</span>
        </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="total, prev, pager, next"
        :total="total"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { PointsService } from '@/api/services/points.service'
import type { PointsLedgerEntryRow } from '@/api/types/points'
import { formatDateTimeDisplay } from '@/utils/display-format'

const loading = ref(false)
const rows = ref<PointsLedgerEntryRow[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

async function reload() {
  loading.value = true
  try {
    const p = await PointsService.myLedger(page.value - 1, size.value)
    rows.value = p.content
    total.value = p.totalElements
  } catch {
    ElMessage.error('加载失败')
    rows.value = []
  } finally {
    loading.value = false
  }
}

watch(
  [page, size],
  () => {
    void reload()
  },
  { immediate: true }
)

watch(size, () => {
  page.value = 1
})
</script>

<style scoped>
.page {
  padding: 20px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.head h2 {
  margin: 0;
}
.hint {
  font-size: 13px;
  color: #606266;
  margin: 0 0 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
