<template>
  <div class="page">
    <div class="page-header">
      <h2>审计日志</h2>
      <el-button @click="load">刷新</el-button>
    </div>
    <div class="table-container">
      <el-table v-loading="loading" :data="rows" border table-layout="fixed">
        <el-table-column prop="createdAt" label="时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTimeDisplay(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="140" show-overflow-tooltip align="center" />
        <el-table-column prop="httpMethod" label="方法" width="90" align="center" />
        <el-table-column prop="requestUri" label="URI" min-width="260" show-overflow-tooltip align="center" />
        <el-table-column prop="action" label="操作" min-width="240" show-overflow-tooltip align="center" />
        <el-table-column prop="success" label="成功" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip align="center" />
        <el-table-column prop="clientIp" label="IP" width="140" align="center" />
      </el-table>
    </div>
    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { AuditLogService, type AuditLogRow } from '@/api/services/audit-log.service'
import { formatDateTimeDisplay } from '@/utils/display-format'

const loading = ref(false)
const rows = ref<AuditLogRow[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const load = async () => {
  loading.value = true
  try {
    const res = await AuditLogService.list(page.value - 1, size.value)
    rows.value = res.content
    total.value = res.totalElements
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
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
