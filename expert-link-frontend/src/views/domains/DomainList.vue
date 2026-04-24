<template>
  <div class="domain-list">
    <el-card class="header-card" shadow="never">
      <div class="header-row">
        <div>
          <h2>专家入库</h2>
          <p>维护领域层级、状态及专家/项目关联概况。</p>
        </div>
        <div class="header-actions">
          <el-button @click="downloadTemplate">下载导入模板</el-button>
          <el-button @click="exportData">导出领域数据</el-button>
          <el-button type="primary" @click="triggerImport">批量导入领域</el-button>
          <el-button type="primary" plain @click="goCreate">新建领域</el-button>
          <input
            ref="importInputRef"
            type="file"
            accept=".xlsx"
            style="display: none"
            @change="onImportFileChange"
          />
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="treeRows"
        table-layout="fixed"
        border
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="false"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="领域名称" min-width="260" align="center" show-overflow-tooltip />
        <el-table-column prop="level" label="层级" width="90" align="center" />
        <el-table-column prop="expertCount" label="专家数" width="100" align="center" />
        <el-table-column prop="projectCount" label="项目数" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openBatchAdd(row)">纳入专家</el-button>
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-button link type="primary" @click="goEdit(row.id)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">共 {{ total }} 个领域（按层级展示，默认折叠）</div>
    </el-card>

    <el-dialog v-model="batchDialogVisible" title="按领域批量纳入专家" width="560px">
      <div class="dialog-hint">
        领域：<strong>{{ currentDomain?.name || '-' }}</strong>
      </div>
      <el-select
        v-model="selectedOwnerIds"
        multiple
        filterable
        remote
        clearable
        reserve-keyword
        :remote-method="debouncedRemoteSearchUsers"
        :loading="userSearchLoading"
        collapse-tags
        placeholder="输入姓名/邮箱搜索用户（已是专家会自动跳过）"
        style="width: 100%"
      >
        <el-option
          v-for="u in users"
          :key="u.id"
          :label="`${u.fullName || u.username} (${u.email})`"
          :value="u.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="submitBatchAdd">保存并纳入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DomainService } from '@/api/services/domain.service'
import { ExpertService } from '@/api/services/expert.service'
import type { UserPickerItem } from '@/api/types'
import type { DomainDetail } from '@/api/types'

const router = useRouter()
const loading = ref(false)
const rows = ref<DomainDetail[]>([])
const total = ref(0)
const users = ref<UserPickerItem[]>([])
const userSearchLoading = ref(false)
let userSearchTimer: ReturnType<typeof setTimeout> | null = null
const batchDialogVisible = ref(false)
const batchSubmitting = ref(false)
const currentDomain = ref<DomainDetail | null>(null)
const selectedOwnerIds = ref<number[]>([])
const statsByDomain = ref<Map<number, { expertCount: number; projectCount: number }>>(new Map())
const importInputRef = ref<HTMLInputElement | null>(null)

type DomainTreeNode = DomainDetail & { children: DomainTreeNode[] }

const treeRows = computed<DomainTreeNode[]>(() => {
  const byId = new Map<number, DomainTreeNode>()
  for (const d of rows.value) {
    byId.set(d.id, {
      ...d,
      expertCount: statsByDomain.value.get(d.id)?.expertCount || 0,
      projectCount: statsByDomain.value.get(d.id)?.projectCount || 0,
      children: []
    })
  }
  const roots: DomainTreeNode[] = []
  for (const node of byId.values()) {
    if (node.parentId != null && byId.has(node.parentId)) {
      byId.get(node.parentId)!.children.push(node)
    } else {
      roots.push(node)
    }
  }
  return roots
})

const fetchRows = async () => {
  loading.value = true
  try {
    const [domainsRes, statsRes] = await Promise.all([
      DomainService.getDomains({ page: 0, size: 500 }),
      DomainService.getDomainStatsBatch()
    ])
    rows.value = domainsRes.content || []
    total.value = domainsRes.totalElements || rows.value.length
    const statsMap = new Map<number, { expertCount: number; projectCount: number }>()
    for (const s of statsRes || []) {
      statsMap.set(Number(s.domainId), {
        expertCount: Number(s.expertCount || 0),
        projectCount: Number(s.projectCount || 0),
      })
    }
    statsByDomain.value = statsMap
  } catch (error: unknown) {
    ElMessage.error((error as Error)?.message || '加载领域列表失败')
  } finally {
    loading.value = false
  }
}

const goCreate = () => {
  void router.push('/domains/add')
}

const goEdit = (id: number) => {
  void router.push(`/domains/${id}/edit`)
}

const goDetail = (id: number) => {
  void router.push(`/domains/${id}`)
}

const openBatchAdd = async (domain: DomainDetail) => {
  currentDomain.value = domain
  selectedOwnerIds.value = []
  batchDialogVisible.value = true
  await remoteSearchUsers('')
}

const remoteSearchUsers = async (keyword: string) => {
  userSearchLoading.value = true
  try {
    const res = await ExpertService.getUserCandidates(keyword || '', 0, 50)
    users.value = res.content || []
  } catch (error: unknown) {
    users.value = []
    ElMessage.error((error as Error)?.message || '加载待纳入用户失败')
  } finally {
    userSearchLoading.value = false
  }
}

const debouncedRemoteSearchUsers = (keyword: string) => {
  if (userSearchTimer) clearTimeout(userSearchTimer)
  userSearchTimer = setTimeout(() => {
    void remoteSearchUsers(keyword)
  }, 300)
}

const submitBatchAdd = async () => {
  if (!currentDomain.value) return
  if (selectedOwnerIds.value.length === 0) {
    ElMessage.warning('请至少选择一个用户')
    return
  }
  batchSubmitting.value = true
  try {
    const result = await DomainService.batchAddExperts(currentDomain.value.id, selectedOwnerIds.value)
    if (result.createdCount > 0) {
      ElMessage.success(`已处理 ${result.requestedCount} 人，新增 ${result.createdCount} 人，跳过 ${result.skippedCount} 人`)
    } else {
      ElMessage.warning(`未新增专家（共跳过 ${result.skippedCount} 人），请检查是否已是专家或数据不满足创建条件`)
    }
    batchDialogVisible.value = false
    await fetchRows()
  } catch (error: unknown) {
    ElMessage.error((error as Error)?.message || '纳入专家失败')
  } finally {
    batchSubmitting.value = false
  }
}

const saveBlob = (blob: Blob, filename: string) => {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

const downloadTemplate = async () => {
  try {
    const blob = await DomainService.downloadImportTemplate()
    saveBlob(blob, 'domains-template.xlsx')
  } catch (error: unknown) {
    ElMessage.error((error as Error)?.message || '下载模板失败')
  }
}

const exportData = async () => {
  try {
    const blob = await DomainService.exportDomains()
    saveBlob(blob, '领域导出.xlsx')
  } catch (error: unknown) {
    ElMessage.error((error as Error)?.message || '导出失败')
  }
}

const triggerImport = () => {
  importInputRef.value?.click()
}

const showImportErrors = async (errors: Array<{ row: number; message: string }>) => {
  if (!errors?.length) return
  const lines = errors.map((item, index) => `${index + 1}. 第 ${item.row} 行：${item.message}`)
  await ElMessageBox.alert(lines.join('<br/>'), '导入失败明细', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '我知道了',
    type: 'warning',
  })
}

const onImportFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    const result = await DomainService.importDomains(file)
    ElMessage.success(`导入完成：总计${result.data.total}，成功${result.data.success}，跳过${result.data.skipped}，失败${result.data.failed}`)
    await showImportErrors(result.data.errors || [])
    await fetchRows()
  } catch (error: unknown) {
    ElMessage.error((error as Error)?.message || '导入失败')
  } finally {
    input.value = ''
  }
}

onMounted(() => {
  void fetchRows()
})

onBeforeUnmount(() => {
  if (userSearchTimer) {
    clearTimeout(userSearchTimer)
    userSearchTimer = null
  }
})
</script>

<style scoped>
.domain-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.header-row h2 {
  margin: 0 0 4px;
}

.header-row p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.dialog-hint {
  margin-bottom: 12px;
  color: #606266;
}
</style>
