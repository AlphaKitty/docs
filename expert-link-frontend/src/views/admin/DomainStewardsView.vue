<template>
  <div class="page">
    <div class="page-header">
      <h2>领域行管配置</h2>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="domains" row-key="id" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="领域" min-width="140" />
      <el-table-column label="行管用户" min-width="280">
        <template #default="{ row }">
          <el-select
            :model-value="stewardIds(row)"
            multiple
            filterable
            remote
            clearable
            reserve-keyword
            :remote-method="debouncedRemoteSearchUsers"
            :loading="usersLoading"
            collapse-tags
            placeholder="输入姓名/邮箱搜索用户"
            style="width: 100%"
            @update:model-value="(v) => onPick(row, v as number[])"
          >
            <el-option v-for="u in users" :key="u.id" :label="userLabel(u)" :value="u.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" :loading="savingId === row.id" @click="save(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DomainService } from '@/api/services/domain.service'
import { UserAdminService, type AdminUserRow } from '@/api/services/user-admin.service'
import type { DomainDetail } from '@/api/types'

const loading = ref(false)
const domains = ref<DomainDetail[]>([])
const users = ref<AdminUserRow[]>([])
const usersLoading = ref(false)
let userSearchTimer: ReturnType<typeof setTimeout> | null = null
const pending = ref<Record<number, number[]>>({})
const savingId = ref<number | null>(null)

const stewardIds = (row: DomainDetail) => pending.value[row.id] ?? (row as any).stewards?.map((s: any) => s.id) ?? []

const userLabel = (u: AdminUserRow) => `${u.username}${u.fullName ? '（' + u.fullName + '）' : ''}`

const onPick = (row: DomainDetail, ids: number[]) => {
  pending.value = { ...pending.value, [row.id]: ids }
}

const load = async () => {
  loading.value = true
  try {
    const [dRes, uRes] = await Promise.all([
      DomainService.getDomains({ page: 0, size: 200 }),
      UserAdminService.searchUsers('', 0, 50),
    ])
    domains.value = dRes.content
    users.value = uRes.content
    pending.value = {}
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const remoteSearchUsers = async (keyword: string) => {
  usersLoading.value = true
  try {
    const res = await UserAdminService.searchUsers(keyword || '', 0, 50)
    users.value = res.content || []
  } catch (e: any) {
    users.value = []
    ElMessage.error(e?.message || '加载用户失败')
  } finally {
    usersLoading.value = false
  }
}

const debouncedRemoteSearchUsers = (keyword: string) => {
  if (userSearchTimer) clearTimeout(userSearchTimer)
  userSearchTimer = setTimeout(() => {
    void remoteSearchUsers(keyword)
  }, 300)
}

const save = async (row: DomainDetail) => {
  savingId.value = row.id
  try {
    const ids = pending.value[row.id] ?? stewardIds(row)
    await DomainService.replaceDomainStewards(row.id, ids)
    ElMessage.success('已保存')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    savingId.value = null
  }
}

onMounted(() => {
  void load()
})

onBeforeUnmount(() => {
  if (userSearchTimer) {
    clearTimeout(userSearchTimer)
    userSearchTimer = null
  }
})
</script>

<style scoped>
.page {
  padding: 8px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
