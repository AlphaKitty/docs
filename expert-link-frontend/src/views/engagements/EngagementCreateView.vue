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
      <el-form-item label="领域" required>
        <el-select v-model="form.domainId" placeholder="选择领域" filterable style="width: 100%">
          <el-option
            v-for="d in domains"
            :key="d.id"
            :label="d.hasSteward ? d.name : `${d.name}（未配置行管）`"
            :value="d.id"
            :disabled="!d.hasSteward"
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
          v-model="form.designatedExpertId"
          filterable
          clearable
          placeholder="请选择指定专家（可选）"
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
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DomainService } from '@/api/services/domain.service'
import { EngagementRequestService } from '@/api/services/engagement-request.service'
import { ExpertService } from '@/api/services/expert.service'
import type { EngagementMode, EngagementTaskType } from '@/api/types/engagement'
import type { ExpertDetail } from '@/api/types/expert'

type DomainOption = { id: number; name: string; hasSteward: boolean }

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const domains = ref<DomainOption[]>([])
const domainExpertOptions = ref<ExpertDetail[]>([])

const form = reactive({
  domainId: undefined as number | undefined,
  mode: 'STEWARD_ASSIGN' as EngagementMode,
  taskType: 'PROBLEM_SOLVING' as EngagementTaskType,
  startAt: '',
  endAt: '' as string | null,
  taskDescription: '',
  designatedExpertId: undefined as number | undefined,
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await DomainService.getDomains({ page: 0, size: 200 })
    domains.value = (res.content || []).map((d: any) => ({
      id: d.id,
      name: d.name,
      hasSteward: Array.isArray(d.stewards) && d.stewards.length > 0,
    }))
    const firstEnabled = domains.value.find((d) => d.hasSteward)
    if (firstEnabled && form.domainId == null) {
      form.domainId = firstEnabled.id
    } else if (!firstEnabled) {
      form.domainId = undefined
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
  if (!form.domainId) {
    domainExpertOptions.value = []
    return
  }
  try {
    domainExpertOptions.value = await ExpertService.getExpertsByDomain(form.domainId)
  } catch {
    domainExpertOptions.value = []
  }
}

watch(
  () => form.domainId,
  async () => {
    form.designatedExpertId = undefined
    await loadDomainExperts()
  }
)

async function onSave() {
  if (!form.domainId || !form.startAt) {
    ElMessage.warning('请填写领域与开始时间')
    return
  }
  saving.value = true
  try {
    const created = await EngagementRequestService.createDraft({
      domainId: form.domainId,
      mode: form.mode,
      taskType: form.taskType,
      startAt: form.startAt,
      endAt: form.endAt || undefined,
      taskDescription: form.taskDescription || undefined,
      ...(form.designatedExpertId != null ? { designatedExpertId: form.designatedExpertId } : {}),
    })
    ElMessage.success('草稿已创建')
    await router.push(`/engagements/${created.id}`)
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
