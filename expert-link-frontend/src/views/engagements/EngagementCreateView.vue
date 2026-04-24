<template>
  <div v-loading="loading" class="page">
    <h2>新建调⽤申请（草稿）</h2>
    <el-form :model="form" label-width="120px" class="form" @submit.prevent>
      <el-alert
        class="mb"
        type="info"
        :closable="false"
        title="仅可申请已配置领域行管的领域。"
      />
      <el-form-item label="父领域" required>
        <el-select
          v-model="selectedParentIds"
          multiple
          filterable
          collapse-tags
          clearable
          placeholder="选择父领域（可多选）"
          style="width: 100%"
        >
          <el-option
            v-for="d in parentDomains"
            :key="d.id"
            :label="domainLabel(d)"
            :value="d.id"
            :disabled="!d.hasSteward"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-for="pid in selectedParentIds"
        :key="pid"
        :label="`${domainNameById.get(pid) || pid} / 子领域`"
      >
        <el-select
          v-model="selectedSubdomainIdsByParent[pid]"
          multiple
          filterable
          collapse-tags
          clearable
          placeholder="不选则使用父领域（自动覆盖其全部子领域专家）"
          style="width: 100%"
        >
          <el-option
            v-for="child in childrenByParent.get(pid) || []"
            :key="child.id"
            :label="domainLabel(child)"
            :value="child.id"
            :disabled="!child.hasSteward"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="模式" required>
        <el-radio-group v-model="form.mode">
          <el-radio label="NAMED">点名</el-radio>
          <el-radio label="STEWARD_ASSIGN">行管指派</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="类型" required>
        <el-select v-model="form.taskType" style="width: 100%">
          <el-option label="问题解决" value="PROBLEM_SOLVING" />
          <el-option label="评审" value="REVIEW" />
          <el-option label="知识管理" value="KNOWLEDGE_MANAGEMENT" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间" required>
        <el-date-picker
          v-model="form.startAt"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          placeholder="开始"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="form.endAt"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          placeholder="可选"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="任务描述">
        <el-input v-model="form.taskDescription" type="textarea" :rows="5" placeholder="提交前需填写至少 8 字" />
      </el-form-item>
      <el-form-item label="指定专家" v-if="form.mode === 'NAMED'">
        <el-select
          v-model="form.designatedExpertIds"
          multiple
          filterable
          clearable
          collapse-tags
          placeholder="请选择指定专家（可多选）"
          style="width: 100%"
        >
          <el-option
            v-for="ex in domainExpertOptions"
            :key="ex.id"
            :label="`${ex.name} (#${ex.id})`"
            :value="ex.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSave">保存草稿</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DomainService } from '@/api/services/domain.service'
import { EngagementRequestService } from '@/api/services/engagement-request.service'
import { ExpertService } from '@/api/services/expert.service'
import type { EngagementMode, EngagementTaskType } from '@/api/types/engagement'
import type { ExpertDetail } from '@/api/types/expert'

type DomainOption = { id: number; name: string; parentId?: number; hasSteward: boolean }

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const allDomains = ref<DomainOption[]>([])
const domainExpertOptions = ref<ExpertDetail[]>([])
const selectedParentIds = ref<number[]>([])
const selectedSubdomainIdsByParent = reactive<Record<number, number[]>>({})

const form = reactive({
  mode: 'STEWARD_ASSIGN' as EngagementMode,
  taskType: 'PROBLEM_SOLVING' as EngagementTaskType,
  startAt: '',
  endAt: '' as string | null,
  taskDescription: '',
  designatedExpertIds: [] as number[],
})

const parentDomains = computed(() => allDomains.value.filter((d) => d.parentId == null))

const domainNameById = computed(() => {
  const map = new Map<number, string>()
  for (const d of allDomains.value) {
    map.set(d.id, d.name)
  }
  return map
})

const childrenByParent = computed(() => {
  const map = new Map<number, DomainOption[]>()
  for (const d of allDomains.value) {
    if (d.parentId == null) continue
    const arr = map.get(d.parentId) || []
    arr.push(d)
    map.set(d.parentId, arr)
  }
  return map
})

const effectiveDomainIds = computed(() => {
  const ids: number[] = []
  for (const pid of selectedParentIds.value) {
    const selectedChildren = (selectedSubdomainIdsByParent[pid] || []).filter((id) => id > 0)
    if (selectedChildren.length) {
      ids.push(...selectedChildren)
    } else {
      ids.push(pid)
    }
  }
  return Array.from(new Set(ids))
})

function domainLabel(d: DomainOption): string {
  return d.hasSteward ? d.name : `${d.name}（未配置行管）`
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await DomainService.getDomains({ page: 0, size: 200 })
    allDomains.value = (res.content || []).map((d: any) => ({
      id: d.id,
      name: d.name,
      parentId: d.parentId,
      hasSteward: Array.isArray(d.stewards) && d.stewards.length > 0,
    }))
    const firstEnabledParent = parentDomains.value.find((d) => d.hasSteward)
    if (firstEnabledParent) {
      selectedParentIds.value = [firstEnabledParent.id]
    } else {
      ElMessage.warning('当前没有已配置行管的领域，暂不可新建申请')
    }
  } catch {
    ElMessage.error('加载领域失败')
  } finally {
    loading.value = false
  }
  await loadDomainExperts()
})

async function loadDomainExperts() {
  if (!effectiveDomainIds.value.length) {
    domainExpertOptions.value = []
    return
  }
  try {
    domainExpertOptions.value = await ExpertService.getExpertsByDomains(effectiveDomainIds.value)
  } catch {
    domainExpertOptions.value = []
  }
}

watch(
  () => [...selectedParentIds.value],
  (ids) => {
    const keep = new Set(ids)
    for (const key of Object.keys(selectedSubdomainIdsByParent)) {
      const pid = Number(key)
      if (!keep.has(pid)) {
        delete selectedSubdomainIdsByParent[pid]
      }
    }
    for (const pid of ids) {
      if (!Array.isArray(selectedSubdomainIdsByParent[pid])) {
        selectedSubdomainIdsByParent[pid] = []
      }
    }
    form.designatedExpertIds = []
    void loadDomainExperts()
  },
  { immediate: true }
)

watch(
  () => effectiveDomainIds.value.join(','),
  () => {
    form.designatedExpertIds = []
    void loadDomainExperts()
  }
)

async function onSave() {
  if (!effectiveDomainIds.value.length || !form.startAt) {
    ElMessage.warning('请填写领域与开始时间')
    return
  }
  saving.value = true
  try {
    const ok: number[] = []
    const failed: number[] = []
    for (const domainId of effectiveDomainIds.value) {
      try {
        const created = await EngagementRequestService.createDraft({
          domainId,
          mode: form.mode,
          taskType: form.taskType,
          startAt: form.startAt,
          endAt: form.endAt || undefined,
          taskDescription: form.taskDescription || undefined,
          ...(form.designatedExpertIds.length > 0 ? { designatedExpertIds: form.designatedExpertIds } : {}),
        })
        ok.push(created.id)
      } catch {
        failed.push(domainId)
      }
    }
    if (!ok.length) {
      ElMessage.error('草稿创建失败')
      return
    }
    if (ok.length === 1 && failed.length === 0) {
      ElMessage.success('草稿已创建')
      await router.push(`/engagements/${ok[0]}`)
      return
    }
    if (!failed.length) {
      ElMessage.success(`已创建 ${ok.length} 个草稿`)
    } else {
      ElMessage.warning(`已创建 ${ok.length} 个草稿，${failed.length} 个领域创建失败`)
    }
    await router.push('/engagements/mine')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 16px;
  max-width: 720px;
}
.form {
  margin-top: 16px;
}
.mb {
  margin-bottom: 12px;
}
</style>
