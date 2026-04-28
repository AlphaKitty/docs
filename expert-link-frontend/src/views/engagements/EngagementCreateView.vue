<template>
  <div v-loading="loading" class="page">
    <div class="head">
      <h2>新建专家调用申请表（草稿）</h2>
      <div class="head-actions">
        <span class="test-label">测试填充</span>
        <el-switch v-model="testMode" />
      </div>
    </div>

    <el-alert
      class="mb"
      type="info"
      :closable="false"
      title="仅可申请已配置领域行管的领域。页面新增字段会自动拼装进任务描述，保持后端兼容。"
    />

    <el-form :model="form" label-width="96px" class="form" @submit.prevent>
      <el-card shadow="never" class="section">
        <template #header>申请信息</template>
        <div class="grid four">
          <el-form-item label="申请类别">
            <el-select v-model="extraForm.applyCategory">
              <el-option label="专家调用" value="专家调用" />
              <el-option label="专家评审" value="专家评审" />
            </el-select>
          </el-form-item>
          <el-form-item label="积分大类">
            <el-input v-model="extraForm.pointsCategory" placeholder="例如：评估评审" />
          </el-form-item>
          <el-form-item label="积分项目">
            <el-input v-model="extraForm.pointsItem" placeholder="例如：技术评审" />
          </el-form-item>
          <el-form-item label="调用模式" required>
            <el-radio-group v-model="form.mode">
              <el-radio label="NAMED">点名</el-radio>
              <el-radio label="STEWARD_ASSIGN">行管指派</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>需求方信息</template>
        <div class="grid four">
          <el-form-item label="需求人">
            <el-input v-model="extraForm.requester" placeholder="输入需求人" />
          </el-form-item>
          <el-form-item label="需求部门">
            <el-input v-model="extraForm.requestDept" placeholder="输入部门" />
          </el-form-item>
          <el-form-item label="需求人职位">
            <el-input v-model="extraForm.requestPosition" placeholder="输入职位" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="extraForm.contact" placeholder="手机/邮箱" />
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>项目信息</template>
        <div class="grid four">
          <el-form-item label="项目部门">
            <el-input v-model="extraForm.projectDept" placeholder="输入项目部门" />
          </el-form-item>
          <el-form-item label="项目名称">
            <el-input v-model="extraForm.projectName" placeholder="输入项目名称" />
          </el-form-item>
          <el-form-item label="项目级别">
            <el-input v-model="extraForm.projectLevel" placeholder="如 S/A/B" />
          </el-form-item>
          <el-form-item label="客户代码">
            <el-input v-model="extraForm.customerCode" placeholder="输入客户代码" />
          </el-form-item>
          <el-form-item label="产品线">
            <el-input v-model="extraForm.productLine" placeholder="输入产品线" />
          </el-form-item>
          <el-form-item label="当前阶段">
            <el-input v-model="extraForm.currentStage" placeholder="如 EVT/DVT" />
          </el-form-item>
          <el-form-item label="是否KDW">
            <el-select v-model="extraForm.isKdw">
              <el-option label="是" value="是" />
              <el-option label="否" value="否" />
            </el-select>
          </el-form-item>
          <el-form-item label="是否迭代产品">
            <el-select v-model="extraForm.isIterative">
              <el-option label="是" value="是" />
              <el-option label="否" value="否" />
            </el-select>
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>活动与任务</template>
        <div class="grid four">
          <el-form-item label="任务类型" required>
            <el-select v-model="form.taskType">
              <el-option label="问题解决" value="PROBLEM_SOLVING" />
              <el-option label="评审" value="REVIEW" />
              <el-option label="知识管理" value="KNOWLEDGE_MANAGEMENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动名称">
            <el-input v-model="extraForm.activityName" placeholder="输入活动名称" />
          </el-form-item>
          <el-form-item label="活动地点">
            <el-input v-model="extraForm.activityLocation" placeholder="输入地点" />
          </el-form-item>
          <el-form-item label="贡献范围">
            <el-input v-model="extraForm.contributionScope" placeholder="如 跨BG" />
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
        </div>
        <el-form-item label="活动主要信息">
          <el-input
            v-model="extraForm.activityInfo"
            type="textarea"
            :rows="4"
            placeholder="问题/现象、可行方案、无法突破点等"
          />
        </el-form-item>
        <el-form-item label="成果提交简述">
          <el-input v-model="extraForm.resultSummary" type="textarea" :rows="3" placeholder="成果内容简述" />
        </el-form-item>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>领域与专家</template>
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
            placeholder="不选则使用父领域"
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
        <div class="grid four">
          <el-form-item label="专家价值">
            <el-input v-model="extraForm.expertValue" placeholder="输入价值评估" />
          </el-form-item>
          <el-form-item label="需求人数">
            <el-input-number v-model="extraForm.requiredCount" :min="1" :max="20" style="width: 100%" />
          </el-form-item>
          <el-form-item label="技术标签">
            <el-input v-model="extraForm.techTags" placeholder="例如：热管理、结构强度" />
          </el-form-item>
        </div>
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
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>补充说明</template>
        <el-form-item label="任务描述">
          <el-input v-model="form.taskDescription" type="textarea" :rows="5" placeholder="可补充业务背景，至少 8 字" />
        </el-form-item>
      </el-card>

      <div class="actions">
        <el-button type="primary" :loading="saving" @click="onSave">保存草稿</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </div>
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
const testMode = ref(false)

const form = reactive({
  mode: 'STEWARD_ASSIGN' as EngagementMode,
  taskType: 'PROBLEM_SOLVING' as EngagementTaskType,
  startAt: '',
  endAt: '' as string | null,
  taskDescription: '',
  designatedExpertIds: [] as number[],
})

const extraForm = reactive({
  applyCategory: '专家调用',
  pointsCategory: '',
  pointsItem: '',
  requester: '',
  requestDept: '',
  requestPosition: '',
  contact: '',
  projectDept: '',
  projectName: '',
  projectLevel: '',
  customerCode: '',
  productLine: '',
  currentStage: '',
  isKdw: '否',
  isIterative: '否',
  activityName: '',
  activityLocation: '',
  contributionScope: '',
  activityInfo: '',
  resultSummary: '',
  expertValue: '',
  requiredCount: 2,
  techTags: '',
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

function getErrorMessage(error: unknown, fallback: string): string {
  return (error as Error)?.message || fallback
}

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
  } catch (e: unknown) {
    ElMessage.error(getErrorMessage(e, '加载领域失败'))
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
  } catch (e: unknown) {
    domainExpertOptions.value = []
    ElMessage.error(getErrorMessage(e, '加载领域专家失败'))
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

watch(testMode, (enabled) => {
  if (!enabled) return
  fillTestData()
})

function fillTestData() {
  const firstEnabledParent = parentDomains.value.find((d) => d.hasSteward)
  if (firstEnabledParent) {
    selectedParentIds.value = [firstEnabledParent.id]
    const children = (childrenByParent.value.get(firstEnabledParent.id) || []).filter((c) => c.hasSteward)
    selectedSubdomainIdsByParent[firstEnabledParent.id] = children.length ? [children[0].id] : []
  }
  form.mode = 'NAMED'
  form.taskType = 'REVIEW'
  form.startAt = '2026-05-05T09:00:00'
  form.endAt = '2026-05-14T18:00:00'
  form.taskDescription = '用于联调的测试申请，请勿用于正式流程。'
  extraForm.applyCategory = '专家调用'
  extraForm.pointsCategory = '评估评审'
  extraForm.pointsItem = '技术评审'
  extraForm.requester = '张三'
  extraForm.requestDept = 'e-HR带出'
  extraForm.requestPosition = '项目经理'
  extraForm.contact = 'zhangsan@example.com'
  extraForm.projectDept = '带出/下拉'
  extraForm.projectName = '多选/输入示例项目'
  extraForm.projectLevel = 'A'
  extraForm.customerCode = 'CUS-001'
  extraForm.productLine = 'XR产品线'
  extraForm.currentStage = 'EVT'
  extraForm.isKdw = '否'
  extraForm.isIterative = '是'
  extraForm.activityName = '结构件技术评审（测试）'
  extraForm.activityLocation = '潍坊 光电园二期'
  extraForm.contributionScope = '跨BG'
  extraForm.activityInfo = '问题/不良现象：结构干涉。\n已尝试方案：调整装配间隙。\n无法突破点：强度与间隙冲突。'
  extraForm.resultSummary = '1) 评审建议输出；2) 风险清单；3) 后续验证项。'
  extraForm.expertValue = '提供结构强度与可制造性评估。'
  extraForm.requiredCount = 2
  extraForm.techTags = '结构设计, 热管理, 装配工艺'
}

function buildTaskDescription(): string {
  const lines = [
    `申请类别：${extraForm.applyCategory}`,
    `积分大类：${extraForm.pointsCategory || '—'}`,
    `积分项目：${extraForm.pointsItem || '—'}`,
    `需求人：${extraForm.requester || '—'} / ${extraForm.requestDept || '—'} / ${extraForm.requestPosition || '—'}`,
    `联系方式：${extraForm.contact || '—'}`,
    `项目：${extraForm.projectName || '—'}（${extraForm.projectLevel || '—'}）`,
    `项目部门：${extraForm.projectDept || '—'}，客户代码：${extraForm.customerCode || '—'}`,
    `产品线：${extraForm.productLine || '—'}，当前阶段：${extraForm.currentStage || '—'}`,
    `是否KDW：${extraForm.isKdw}，是否迭代产品：${extraForm.isIterative}`,
    `活动名称：${extraForm.activityName || '—'}，地点：${extraForm.activityLocation || '—'}，贡献范围：${extraForm.contributionScope || '—'}`,
    `活动主要信息：${extraForm.activityInfo || '—'}`,
    `成果提交简述：${extraForm.resultSummary || '—'}`,
    `专家价值：${extraForm.expertValue || '—'}，需求人数：${extraForm.requiredCount}`,
    `技术标签：${extraForm.techTags || '—'}`,
    '',
    `补充描述：${form.taskDescription || '—'}`,
  ]
  return lines.join('\n')
}

async function onSave() {
  if (!effectiveDomainIds.value.length || !form.startAt) {
    ElMessage.warning('请填写领域与开始时间')
    return
  }
  saving.value = true
  try {
    const ok: number[] = []
    const failed: number[] = []
    const failureMessages: string[] = []
    for (const domainId of effectiveDomainIds.value) {
      try {
        const created = await EngagementRequestService.createDraft({
          domainId,
          mode: form.mode,
          taskType: form.taskType,
          startAt: form.startAt,
          endAt: form.endAt || undefined,
          taskDescription: buildTaskDescription(),
          ...(form.designatedExpertIds.length > 0 ? { designatedExpertIds: form.designatedExpertIds } : {}),
        })
        ok.push(created.id)
      } catch (e: unknown) {
        failed.push(domainId)
        failureMessages.push(getErrorMessage(e, `领域 ${domainId} 创建失败`))
      }
    }
    if (!ok.length) {
      ElMessage.error(failureMessages[0] || '草稿创建失败')
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
      ElMessage.warning(
        `已创建 ${ok.length} 个草稿，${failed.length} 个领域创建失败：${failureMessages[0] || '请检查领域配置'}`
      )
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
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.head h2 {
  margin: 0;
}
.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.test-label {
  color: #606266;
  font-size: 13px;
}
.form {
  margin-top: 16px;
}
.form :deep(.el-form-item__content) {
  min-width: 0;
}
.form :deep(.el-input),
.form :deep(.el-select),
.form :deep(.el-date-editor),
.form :deep(.el-input-number) {
  width: 100%;
}
.mb {
  margin-bottom: 12px;
}
.section {
  margin-bottom: 12px;
}
.grid {
  display: grid;
  gap: 10px 12px;
}
.grid.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.actions {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-top: 8px;
}

@media (max-width: 1200px) {
  .grid.four {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .grid.four {
    grid-template-columns: 1fr;
  }
}
</style>
