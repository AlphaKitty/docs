<template>
  <div v-loading="loading" class="page">
    <template v-if="row">
      <div class="head">
        <h2>{{ row.referenceCode || `申请单 #${row.id}` }}</h2>
        <el-button @click="$router.push('/engagements/mine')">返回列表</el-button>
      </div>

      <el-steps
        class="mb"
        align-center
        finish-status="success"
        :active="stepMeta.active"
        :status="stepMeta.stepsStatus"
      >
        <el-step title="草稿" />
        <el-step title="待行管指派" />
        <el-step title="专家确认" />
        <el-step title="执行中" />
        <el-step title="待放分" />
        <el-step title="已结项" />
      </el-steps>

      <el-tag class="mb">{{ statusText(row.status) }}</el-tag>

      <el-alert
        v-if="row.evaluationRevisionNote"
        type="warning"
        :closable="false"
        class="mb"
        title="行管退回说明"
        :description="row.evaluationRevisionNote"
      />
      <el-alert
        v-if="row.rollbackNote"
        type="warning"
        :closable="false"
        class="mb"
        title="节点退回说明"
        :description="row.rollbackNote"
      />
      <el-alert
        v-if="row.expertResponseNote"
        type="warning"
        :closable="false"
        class="mb"
        title="专家拒绝说明"
        :description="row.expertResponseNote"
      />
      <el-alert
        v-if="row.cancelReason"
        type="info"
        :closable="false"
        class="mb"
        title="取消说明"
        :description="row.cancelReason"
      />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="正式编号">{{ row.referenceCode || '提交后生成' }}</el-descriptions-item>
        <el-descriptions-item label="领域">{{ row.domainName }} ({{ row.domainId }})</el-descriptions-item>
        <el-descriptions-item label="领域行管">
          {{ row.domainStewardNames?.length ? row.domainStewardNames.join('、') : '未配置' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请人">{{ row.applicantUsername }}</el-descriptions-item>
        <el-descriptions-item label="模式">{{ row.mode }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ taskTypeLabel(row.taskType) }}</el-descriptions-item>
        <el-descriptions-item label="开始">{{ formatDateTimeDisplay(row.startAt) }}</el-descriptions-item>
        <el-descriptions-item label="结束">{{ formatDateTimeDisplay(row.endAt) }}</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">{{ row.taskDescription || '—' }}</el-descriptions-item>
        <el-descriptions-item label="指定专家">
          {{
            row.designatedExpertNames?.length
              ? row.designatedExpertNames.join('、')
              : (row.designatedExpertName || '—')
          }}
        </el-descriptions-item>
        <el-descriptions-item label="指派专家">
          {{
            row.assignedExpertNames?.length
              ? row.assignedExpertNames.join('、')
              : row.assignedExpertName || '—'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="指派说明" :span="2">{{ row.assignmentNote || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="row.suggestedScore != null" label="系统建议分">
          {{ row.suggestedScore }}（评价维度预计算，供放分参考）
        </el-descriptions-item>
        <el-descriptions-item v-if="row.stewardFinalScore != null" label="行管确认分">
          {{ row.stewardFinalScore }}
        </el-descriptions-item>
      </el-descriptions>

      <el-card v-if="row.reassignmentLog?.length" class="mt" shadow="never">
        <template #header>改派记录</template>
        <el-table :data="row.reassignmentLog" border size="small">
            <el-table-column prop="at" label="时间" width="170" align="center">
              <template #default="{ row: logRow }">
                {{ formatDateTimeDisplay(logRow.at) }}
              </template>
            </el-table-column>
          <el-table-column prop="fromExpertName" label="原专家" align="center" />
          <el-table-column prop="toExpertName" label="新专家" align="center" />
          <el-table-column prop="reason" label="原因" show-overflow-tooltip align="center" />
        </el-table>
      </el-card>

      <el-card
        v-if="row.evaluationAttachmentUrls?.length && row.status !== 'IN_PROGRESS'"
        class="mt"
        shadow="never"
      >
        <template #header>评价附件</template>
        <el-space wrap>
          <el-button
            v-for="(p, idx) in row.evaluationAttachmentUrls"
            :key="idx"
            link
            type="primary"
            @click="downloadAttachment(p)"
          >
            附件 {{ idx + 1 }}
          </el-button>
        </el-space>
      </el-card>

      <el-card v-if="isApplicant && row.status === 'DRAFT'" class="mt" shadow="never">
        <template #header>编辑并提交</template>
        <el-input v-model="draftDesc" type="textarea" :rows="4" placeholder="至少 8 字" />
        <div class="mt-row">
          <el-button type="success" :loading="acting" @click="submitReq">提交申请</el-button>
        </div>
      </el-card>

      <el-alert
        v-if="
          row.status === 'PENDING_EXPERT_CONFIRM' &&
          row.viewerAmongAssignedExperts &&
          row.viewerExpertConfirmPending === false
        "
        type="info"
        :closable="false"
        class="mb"
        title="您已确认，待其他被指派的专家确认后方可进入执行中"
      />

      <el-card v-if="canStewardAssign" class="mt" shadow="never">
        <template #header>行管指派专家</template>
        <p class="hint">须为已关联本领域的专家（后端校验）；可多选，所选专家均需确认接受后进入执行中。</p>
        <el-select
          v-model="assignExpertIds"
          multiple
          filterable
          collapse-tags
          collapse-tags-tooltip
          clearable
          placeholder="从本领域专家中选择（可多选）"
          style="width: 100%"
        >
          <el-option
            v-for="ex in domainExpertOptions"
            :key="ex.id"
            :label="`${ex.name} (#${ex.id})`"
            :value="ex.id"
          />
        </el-select>
        <el-input v-model="assignNote" class="mt-row" placeholder="指派说明（可选）" />
        <el-button type="primary" class="mt-row" :loading="acting" @click="doAssign">确认指派</el-button>
      </el-card>

      <el-card v-if="canStewardReassign" class="mt" shadow="never">
        <template #header>改派专家</template>
        <p class="hint">待确认或执行中可改派；改派后新名单需重新确认（可多选）。</p>
        <el-select
          v-model="reassignExpertIds"
          multiple
          filterable
          collapse-tags
          collapse-tags-tooltip
          clearable
          placeholder="选择专家（可多选）"
          style="width: 100%"
        >
          <el-option
            v-for="ex in domainExpertOptions"
            :key="ex.id"
            :label="`${ex.name} (#${ex.id})`"
            :value="ex.id"
          />
        </el-select>
        <el-input v-model="reassignReason" class="mt-row" type="textarea" :rows="2" placeholder="改派原因" />
        <el-button type="warning" class="mt-row" :loading="acting" @click="doReassign">确认改派</el-button>
      </el-card>

      <el-card v-if="canExpertConfirm" class="mt" shadow="never">
        <template #header>专家确认（需被指派的专家账号登录）</template>
        <el-input v-model="expertNote" placeholder="备注（拒绝时必填）" />
        <div class="mt-row">
          <el-button type="success" :loading="acting" @click="expertDecision(true)">接受</el-button>
          <el-button type="danger" :loading="acting" @click="expertDecision(false)">拒绝</el-button>
        </div>
      </el-card>

      <el-card v-if="isApplicant && row.status === 'IN_PROGRESS'" class="mt" shadow="never">
        <template #header>任务结束评价</template>
        <el-form label-width="100px">
          <el-alert
            type="info"
            :closable="false"
            class="mb"
            title="请对每位专家分别评价"
            description="每位被指派专家都需要独立填写：专业度、时效、态度、是否解决和评语。"
          />
          <el-card
            v-for="item in expertEvalForms"
            :key="item.expertId"
            class="mt"
            shadow="never"
          >
            <template #header>
              <span>专家：{{ item.expertName || `#${item.expertId}` }}</span>
            </template>
            <el-form-item label="专业度 (1-5)">
              <el-input-number v-model="item.professional" :min="1" :max="5" />
            </el-form-item>
            <el-form-item label="时效 (1-5)">
              <el-input-number v-model="item.timeliness" :min="1" :max="5" />
            </el-form-item>
            <el-form-item label="态度 (1-5)">
              <el-input-number v-model="item.attitude" :min="1" :max="5" />
            </el-form-item>
            <el-form-item label="是否解决">
              <el-switch v-model="item.resolved" />
            </el-form-item>
            <el-form-item label="评语">
              <el-input v-model="item.comment" type="textarea" :rows="3" placeholder="请输入该专家的评价" />
            </el-form-item>
          </el-card>
          <el-form-item label="附件">
            <el-upload :http-request="onEvalUpload" :limit="8" :show-file-list="true">
              <el-button type="primary">上传文件</el-button>
              <template #tip>
                <div class="hint">已选路径将随评价一并提交（最多 8 个）。</div>
              </template>
            </el-upload>
            <div v-if="evalAttachmentPaths.length" class="mt-row">
              <el-tag v-for="(p, i) in evalAttachmentPaths" :key="i" closable class="tag" @close="removeAttachment(i)">
                {{ p }}
              </el-tag>
            </div>
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="acting" @click="doEval">提交评价</el-button>
      </el-card>

      <el-card v-if="canStewardRelease" class="mt" shadow="never">
        <template #header>行管放分（结项）</template>
        <p v-if="row.suggestedScore != null" class="hint">系统建议分：{{ row.suggestedScore }}，可直接作为放分参考。</p>
        <el-input-number v-model="finalScore" :precision="2" :step="0.5" placeholder="确认分（默认用建议分）" />
        <el-input v-model="releaseNote" class="mt-row" placeholder="说明（可选）" />
        <el-button type="success" class="mt-row" :loading="acting" @click="doRelease">确认放分并结项</el-button>
      </el-card>

      <el-card v-if="canRequestRevision" class="mt" shadow="never">
        <template #header>退回申请人重评</template>
        <p class="hint">将清空当前评价与附件，申请单回到执行中，由申请人重新提交评价。</p>
        <el-input v-model="revisionReason" type="textarea" :rows="3" placeholder="退回原因（必填）" />
        <el-button type="danger" class="mt-row" :loading="acting" @click="doRequestRevision">确认退回</el-button>
      </el-card>

      <el-card v-if="canRollback" class="mt" shadow="never">
        <template #header>退回上一节点</template>
        <p class="hint">将由当前节点责任人退回到流程上一节点，需填写退回说明。</p>
        <el-input v-model="rollbackReason" type="textarea" :rows="3" placeholder="退回说明（必填）" />
        <el-button type="warning" class="mt-row" :loading="acting" @click="doRollback">确认退回</el-button>
      </el-card>

      <el-card v-if="canCancel" class="mt" shadow="never">
        <template #header>取消申请</template>
        <p class="hint">申请人可取消当前申请（不可取消已结项/已驳回/已取消）。</p>
        <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="取消说明（可选）" />
        <el-button type="danger" class="mt-row" :loading="acting" @click="doCancel">确认取消</el-button>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { hasAnyRole } from '@/constants/role-policy'
import { useSystemSettingsStore } from '@/stores/system-settings'
import { EngagementRequestService } from '@/api/services/engagement-request.service'
import { ExpertService } from '@/api/services/expert.service'
import type { ExpertDetail } from '@/api/types/expert'
import type { EngagementRequestRow } from '@/api/types/engagement'
import { formatDateTimeDisplay, taskTypeLabel } from '@/utils/display-format'

const route = useRoute()
const auth = useAuthStore()
const settings = useSystemSettingsStore()
void settings.ensureHydrated()

const loading = ref(false)
const acting = ref(false)
const row = ref<EngagementRequestRow | null>(null)
const draftDesc = ref('')
const assignExpertIds = ref<number[]>([])
const assignNote = ref('')
const domainExpertOptions = ref<ExpertDetail[]>([])
const expertNote = ref('')
const finalScore = ref<number | undefined>()
const releaseNote = ref('')
const reassignExpertIds = ref<number[]>([])
const reassignReason = ref('')
const revisionReason = ref('')
const rollbackReason = ref('')
const cancelReason = ref('')
const evalAttachmentPaths = ref<string[]>([])

type ExpertEvalFormItem = {
  expertId: number
  expertName: string
  professional: number
  timeliness: number
  attitude: number
  resolved: boolean
  comment: string
}

const expertEvalForms = ref<ExpertEvalFormItem[]>([])

const id = computed(() => Number(route.params.id))

const isApplicant = computed(() => row.value && auth.userId === row.value.applicantId)
const canStewardRole = computed(() => hasAnyRole(auth.roles, settings.rolesFor('STEWARD')))
const canExpertRole = computed(() => hasAnyRole(auth.roles, settings.rolesFor('EXPERT')))

const canStewardAssign = computed(() => {
  if (!row.value || row.value.status !== 'PENDING_STEWARD_ASSIGN') return false
  return canStewardRole.value
})

const canStewardReassign = computed(() => {
  if (!row.value) return false
  if (row.value.status !== 'PENDING_EXPERT_CONFIRM' && row.value.status !== 'IN_PROGRESS') return false
  return canStewardRole.value
})

const canStewardRelease = computed(() => {
  if (!row.value || row.value.status !== 'PENDING_STEWARD_SCORE_RELEASE') return false
  return canStewardRole.value
})

const canExpertConfirm = computed(() => {
  if (!row.value || row.value.status !== 'PENDING_EXPERT_CONFIRM') return false
  if (!canExpertRole.value) return false
  return row.value.viewerExpertConfirmPending === true
})

const canRequestRevision = computed(() => canStewardRelease.value)
const isSuperAdmin = computed(() => auth.roles.includes('SUPER_ADMIN'))
const canRollback = computed(() => {
  if (!row.value) return false
  if (row.value.status === 'PENDING_EXPERT_CONFIRM' || row.value.status === 'IN_PROGRESS') {
    return Boolean(row.value.viewerAmongAssignedExperts) || isSuperAdmin.value
  }
  if (row.value.status === 'PENDING_STEWARD_SCORE_RELEASE') {
    return canStewardRole.value
  }
  return false
})
const canCancel = computed(() => {
  if (!row.value || !isApplicant.value) return false
  return !['COMPLETED', 'REJECTED', 'CANCELLED'].includes(row.value.status)
})

const stepMeta = computed(() => {
  if (!row.value) return { active: 0, stepsStatus: undefined as 'error' | 'process' | 'wait' | 'finish' | 'success' | undefined }
  const s = row.value.status
  if (s === 'REJECTED') return { active: 2, stepsStatus: 'error' as const }
  if (s === 'CANCELLED') return { active: 1, stepsStatus: 'error' as const }
  const order = [
    'DRAFT',
    'PENDING_STEWARD_ASSIGN',
    'PENDING_EXPERT_CONFIRM',
    'IN_PROGRESS',
    'PENDING_STEWARD_SCORE_RELEASE',
    'COMPLETED',
  ] as const
  const i = order.indexOf(s as (typeof order)[number])
  if (s === 'COMPLETED') return { active: 6, stepsStatus: undefined }
  return { active: i < 0 ? 0 : i, stepsStatus: undefined }
})

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

async function load() {
  loading.value = true
  try {
    const r = await EngagementRequestService.getById(id.value)
    row.value = r
    draftDesc.value = r.taskDescription || ''
    evalAttachmentPaths.value = []
    if (r.status === 'PENDING_STEWARD_SCORE_RELEASE' && r.suggestedScore != null) {
      finalScore.value = Number(r.suggestedScore)
    } else {
      finalScore.value = undefined
    }
    revisionReason.value = ''
    rollbackReason.value = ''
    cancelReason.value = ''
    reassignExpertIds.value = r.assignedExpertIds?.length ? [...r.assignedExpertIds] : []
    reassignReason.value = ''
    assignExpertIds.value = []
    initExpertEvalForms(r)
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '加载失败')
    row.value = null
  } finally {
    loading.value = false
  }
}

function initExpertEvalForms(r: EngagementRequestRow) {
  const ids = r.assignedExpertIds?.length
    ? r.assignedExpertIds
    : (r.assignedExpertId ? [r.assignedExpertId] : [])
  const names = r.assignedExpertNames?.length
    ? r.assignedExpertNames
    : (r.assignedExpertName ? [r.assignedExpertName] : [])

  expertEvalForms.value = ids.map((expertId, idx) => ({
    expertId,
    expertName: names[idx] || '',
    professional: 5,
    timeliness: 5,
    attitude: 5,
    resolved: true,
    comment: '',
  }))
}

watch(
  () => route.params.id,
  () => {
    void load()
  }
)

watch(
  () => (row.value ? `${row.value.domainId}-${row.value.status}-${canStewardAssign.value}-${canStewardReassign.value}` : ''),
  async () => {
    domainExpertOptions.value = []
    if (!row.value) return
    const st = row.value.status
    const needList =
      st === 'PENDING_STEWARD_ASSIGN' ||
      ((st === 'PENDING_EXPERT_CONFIRM' || st === 'IN_PROGRESS') && canStewardRole.value)
    if (!needList) return
    try {
      domainExpertOptions.value = await ExpertService.getExpertsByDomain(row.value.domainId)
    } catch (e: unknown) {
      domainExpertOptions.value = []
      ElMessage.error((e as Error)?.message || '加载领域专家失败')
    }
  },
  { immediate: true }
)

onMounted(() => {
  void load()
})

async function saveDraft() {
  if (!row.value) return
  acting.value = true
  try {
    const r = await EngagementRequestService.patchDraft(row.value.id, { taskDescription: draftDesc.value })
    row.value = r
    ElMessage.success('已保存')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '保存失败')
  } finally {
    acting.value = false
  }
}

async function submitReq() {
  if (!row.value) return
  acting.value = true
  try {
    await EngagementRequestService.patchDraft(row.value.id, { taskDescription: draftDesc.value })
    const r = await EngagementRequestService.submit(row.value.id)
    row.value = r
    ElMessage.success('已提交')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '提交失败')
  } finally {
    acting.value = false
  }
}

async function doAssign() {
  if (!row.value || !assignExpertIds.value.length) {
    ElMessage.warning('请至少选择一名专家')
    return
  }
  acting.value = true
  try {
    const r = await EngagementRequestService.assign(row.value.id, assignExpertIds.value, assignNote.value)
    row.value = r
    ElMessage.success('已指派')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '指派失败')
  } finally {
    acting.value = false
  }
}

async function doReassign() {
  if (!row.value || !reassignExpertIds.value.length) {
    ElMessage.warning('请至少选择一名专家')
    return
  }
  acting.value = true
  try {
    const r = await EngagementRequestService.reassign(row.value.id, reassignExpertIds.value, reassignReason.value)
    row.value = r
    ElMessage.success('已改派')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '改派失败')
  } finally {
    acting.value = false
  }
}

async function expertDecision(accepted: boolean) {
  if (!row.value) return
  const trimmedNote = expertNote.value.trim()
  if (!accepted && !trimmedNote) {
    ElMessage.warning('拒绝时请填写备注说明')
    return
  }
  acting.value = true
  try {
    const r = await EngagementRequestService.expertDecision(
      row.value.id,
      accepted,
      trimmedNote || undefined
    )
    row.value = r
    expertNote.value = ''
    ElMessage.success(accepted ? '已接受' : '已拒绝')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '操作失败')
  } finally {
    acting.value = false
  }
}

async function onEvalUpload(opt: UploadRequestOptions) {
  if (!row.value) {
    opt.onError?.(new Error('no row') as never)
    return
  }
  try {
    const file = opt.file as File
    const res = await EngagementRequestService.uploadEvaluationFile(row.value.id, file)
    evalAttachmentPaths.value.push(res.path)
    opt.onSuccess?.({} as never)
    ElMessage.success('已上传')
  } catch (e: unknown) {
    opt.onError?.(e as never)
    ElMessage.error((e as Error)?.message || '上传失败')
  }
}

function removeAttachment(i: number) {
  evalAttachmentPaths.value.splice(i, 1)
}

async function doEval() {
  if (!row.value) return
  if (!expertEvalForms.value.length) {
    ElMessage.warning('当前没有可评价的专家')
    return
  }
  const missingComment = expertEvalForms.value.some((item) => !item.comment.trim())
  if (missingComment) {
    ElMessage.warning('请为每位专家填写评语')
    return
  }

  const professionalAvg = Math.round(
    expertEvalForms.value.reduce((sum, item) => sum + item.professional, 0) / expertEvalForms.value.length
  )
  const timelinessAvg = Math.round(
    expertEvalForms.value.reduce((sum, item) => sum + item.timeliness, 0) / expertEvalForms.value.length
  )
  const attitudeAvg = Math.round(
    expertEvalForms.value.reduce((sum, item) => sum + item.attitude, 0) / expertEvalForms.value.length
  )
  const allResolved = expertEvalForms.value.every((item) => item.resolved)
  const mergedComment = expertEvalForms.value
    .map((item) => `${item.expertName || `专家#${item.expertId}`}: ${item.comment.trim()}`)
    .join('\n')

  acting.value = true
  try {
    const r = await EngagementRequestService.submitEvaluation(row.value.id, {
      professional: professionalAvg,
      timeliness: timelinessAvg,
      attitude: attitudeAvg,
      resolved: allResolved,
      comment: mergedComment || undefined,
      attachmentUrls: evalAttachmentPaths.value.length ? [...evalAttachmentPaths.value] : undefined,
      expertEvaluations: expertEvalForms.value.map((item) => ({
        expertId: item.expertId,
        professional: item.professional,
        timeliness: item.timeliness,
        attitude: item.attitude,
        resolved: item.resolved,
        comment: item.comment.trim() || undefined,
      })),
    })
    row.value = r
    ElMessage.success('评价已提交')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '提交失败')
  } finally {
    acting.value = false
  }
}

async function doRelease() {
  if (!row.value) return
  acting.value = true
  try {
    const r = await EngagementRequestService.releaseScore(
      row.value.id,
      finalScore.value,
      releaseNote.value || undefined
    )
    row.value = r
    ElMessage.success('已结项')
    void auth.restoreSession()
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '放分失败')
  } finally {
    acting.value = false
  }
}

async function doRequestRevision() {
  if (!row.value) return
  if (!revisionReason.value.trim()) {
    ElMessage.warning('请填写退回原因')
    return
  }
  acting.value = true
  try {
    const r = await EngagementRequestService.requestEvaluationRevision(row.value.id, revisionReason.value.trim())
    row.value = r
    ElMessage.success('已退回，申请人可重评')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '操作失败')
  } finally {
    acting.value = false
  }
}

async function doRollback() {
  if (!row.value) return
  if (!rollbackReason.value.trim()) {
    ElMessage.warning('请填写退回说明')
    return
  }
  acting.value = true
  try {
    const r = await EngagementRequestService.rollback(row.value.id, rollbackReason.value.trim())
    row.value = r
    ElMessage.success('已退回上一节点')
    rollbackReason.value = ''
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '退回失败')
  } finally {
    acting.value = false
  }
}

async function doCancel() {
  if (!row.value) return
  acting.value = true
  try {
    const r = await EngagementRequestService.cancel(row.value.id, cancelReason.value.trim() || undefined)
    row.value = r
    ElMessage.success('申请已取消')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '取消失败')
  } finally {
    acting.value = false
  }
}

function fileNameFromPath(path: string) {
  const parts = path.split('/').filter(Boolean)
  return parts[parts.length - 1] || 'file'
}

async function downloadAttachment(path: string) {
  const fileName = fileNameFromPath(path)
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('auth_token') : null
  try {
    const res = await fetch(
      `/api/engagement-requests/${id.value}/evaluation-files/${encodeURIComponent(fileName)}`,
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      }
    )
    if (!res.ok) {
      let backendMessage = ''
      try {
        const errJson = await res.json()
        backendMessage = errJson?.message || ''
      } catch {
        backendMessage = ''
      }
      ElMessage.error(backendMessage || '下载失败')
      return
    }
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    URL.revokeObjectURL(url)
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '下载失败')
  }
}
</script>

<style scoped>
.page {
  padding: 16px;
  max-width: 900px;
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
.mb {
  margin-bottom: 12px;
}
.mt {
  margin-top: 16px;
}
.mt-row {
  margin-top: 12px;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin: 0 0 8px;
}
.tag {
  margin-right: 8px;
  margin-bottom: 4px;
}
</style>
