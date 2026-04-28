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
      <div class="phase-row">
        <el-tag type="primary">调用阶段：{{ callPhaseText }}</el-tag>
        <el-tag type="success">积分阶段：{{ pointsPhaseText }}</el-tag>
      </div>

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

      <el-card v-if="fusionSummary.length || fusionExtraText" class="mt" shadow="never">
        <template #header>融合申请信息（专家调用 + 积分自提）</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item
            v-for="item in fusionSummary"
            :key="item.label"
            :label="item.label"
          >
            {{ item.value }}
          </el-descriptions-item>
          <el-descriptions-item v-if="fusionExtraText" label="补充描述" :span="2">
            {{ fusionExtraText }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card v-if="canViewProgressLogs" class="mt" shadow="never">
        <template #header>执行协同记录（过程证据）</template>
        <p class="hint">执行阶段由申请人和被指派专家持续补充过程材料，作为积分放分依据之一。</p>
        <template v-if="canEditProgressLogs">
          <el-input
            v-model="progressLogDraft"
            type="textarea"
            :rows="3"
            placeholder="填写本次进展、问题、方案、结论等"
          />
          <el-upload :http-request="onProgressUpload" :limit="6" :show-file-list="true" class="mt-row">
            <el-button type="primary" plain>上传过程附件</el-button>
          </el-upload>
          <div v-if="progressDraftAttachments.length" class="mt-row">
            <el-tag
              v-for="(p, i) in progressDraftAttachments"
              :key="`${p}-${i}`"
              closable
              class="tag"
              @close="removeProgressAttachment(i)"
            >
              {{ p }}
            </el-tag>
          </div>
          <div class="mt-row">
            <el-button type="primary" :loading="postingProgress" @click="submitProgressLog">发布过程记录</el-button>
          </div>
        </template>
        <el-empty v-if="!progressLogs.length" description="暂无过程记录" />
        <div v-else class="progress-list">
          <el-card v-for="item in progressLogs" :key="item.id" shadow="hover" class="progress-item">
            <div class="progress-meta">
              <span class="progress-author">{{ item.authorName }}（{{ progressRoleText(item.authorRole) }}）</span>
              <span>{{ formatDateTimeDisplay(item.createdAt) }}</span>
            </div>
            <div class="progress-content">{{ item.content }}</div>
            <div v-if="item.attachments?.length" class="mt-row">
              <el-space wrap>
                <el-button
                  v-for="(path, idx) in item.attachments"
                  :key="`${item.id}-${idx}`"
                  link
                  type="primary"
                  @click="downloadAttachment(path)"
                >
                  附件 {{ idx + 1 }}
                </el-button>
              </el-space>
            </div>
          </el-card>
        </div>
      </el-card>

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
        <template #header>任务结束评价（触发积分审核）</template>
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
        <template #header>积分放分（结项）</template>
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
import type { EngagementProgressLog, EngagementRequestRow } from '@/api/types/engagement'
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
const progressLogs = ref<EngagementProgressLog[]>([])
const postingProgress = ref(false)
const progressLogDraft = ref('')
const progressDraftAttachments = ref<string[]>([])

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
const canViewProgressLogs = computed(() => {
  if (!row.value) return false
  if (canStewardRole.value) return true
  if (isApplicant.value) return true
  if (canExpertRole.value && ['PENDING_EXPERT_CONFIRM', 'IN_PROGRESS', 'PENDING_STEWARD_SCORE_RELEASE'].includes(row.value.status)) {
    return true
  }
  return Boolean(row.value.viewerAmongAssignedExperts)
})
const canEditProgressLogs = computed(() => {
  if (!row.value || row.value.status !== 'IN_PROGRESS') return false
  if (isApplicant.value) return true
  if (canExpertRole.value) return true
  return Boolean(row.value.viewerAmongAssignedExperts)
})
const canReleaseByProgressRule = computed(() => {
  if (!canStewardRelease.value) return false
  return progressLogs.value.length > 0
})

const callPhaseText = computed(() => {
  const status = row.value?.status
  if (!status) return '未开始'
  if (status === 'DRAFT') return '草稿'
  if (status === 'PENDING_STEWARD_ASSIGN') return '待分派专家'
  if (status === 'PENDING_EXPERT_CONFIRM') return '待专家确认'
  if (status === 'IN_PROGRESS') return '执行中'
  if (status === 'PENDING_STEWARD_SCORE_RELEASE') return '执行完成'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'CANCELLED') return '已取消'
  if (status === 'REJECTED') return '已驳回'
  return status
})

const pointsPhaseText = computed(() => {
  const status = row.value?.status
  if (!status) return '未开始'
  if (status === 'DRAFT' || status === 'PENDING_STEWARD_ASSIGN' || status === 'PENDING_EXPERT_CONFIRM') {
    return '未开始'
  }
  if (status === 'IN_PROGRESS') return '待申请人评价'
  if (status === 'PENDING_STEWARD_SCORE_RELEASE') return '待积分审核/放分'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'CANCELLED' || status === 'REJECTED') return '已终止'
  return status
})

const fusionSummary = computed(() => {
  const parsed = parseStructuredTaskDescription(row.value?.taskDescription || '')
  const allItems: Array<{ label: string; value: string }> = []
  const keysInOrder = [
    '申请类别',
    '积分大类',
    '积分项目',
    '需求人',
    '联系方式',
    '项目',
    '项目部门',
    '产品线',
    '是否KDW',
    '活动名称',
    '活动主要信息',
    '成果提交简述',
    '专家价值',
    '技术标签',
  ]
  for (const key of keysInOrder) {
    const value = parsed.map[key]
    if (value) {
      allItems.push({ label: key, value })
    }
  }
  return allItems
})

const fusionExtraText = computed(() => {
  const parsed = parseStructuredTaskDescription(row.value?.taskDescription || '')
  return parsed.extraText
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

function parseStructuredTaskDescription(taskDescription: string): {
  map: Record<string, string>
  extraText: string
} {
  const result: Record<string, string> = {}
  const lines = (taskDescription || '').split('\n')
  let extraText = ''
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue
    const idx = trimmed.indexOf('：')
    if (idx > 0) {
      const key = trimmed.slice(0, idx).trim()
      const value = trimmed.slice(idx + 1).trim()
      if (key) result[key] = value
    }
  }
  if (result['补充描述']) {
    extraText = result['补充描述']
  } else if (taskDescription.trim()) {
    extraText = taskDescription.trim()
  }
  return { map: result, extraText }
}

function progressStorageKey(requestId: number): string {
  return `engagement-progress-logs:${requestId}`
}

function progressRoleText(role: EngagementProgressLog['authorRole']): string {
  if (role === 'APPLICANT') return '申请人'
  if (role === 'EXPERT') return '专家'
  if (role === 'STEWARD') return '行管'
  return '系统'
}

function inferAuthorRole(): EngagementProgressLog['authorRole'] {
  if (isApplicant.value) return 'APPLICANT'
  if (canStewardRole.value) return 'STEWARD'
  return 'EXPERT'
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

function writeLocalProgressLogs(requestId: number, logs: EngagementProgressLog[]): void {
  if (typeof localStorage === 'undefined') return
  localStorage.setItem(progressStorageKey(requestId), JSON.stringify(logs))
}

async function loadProgressLogs(requestId: number): Promise<void> {
  try {
    progressLogs.value = await EngagementRequestService.listProgressLogs(requestId)
  } catch {
    progressLogs.value = readLocalProgressLogs(requestId)
  }
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
    await loadProgressLogs(r.id)
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

async function onProgressUpload(opt: UploadRequestOptions) {
  if (!row.value) {
    opt.onError?.(new Error('no row') as never)
    return
  }
  try {
    const file = opt.file as File
    const res = await EngagementRequestService.uploadEvaluationFile(row.value.id, file)
    progressDraftAttachments.value.push(res.path)
    opt.onSuccess?.({} as never)
    ElMessage.success('过程附件已上传')
  } catch (e: unknown) {
    opt.onError?.(e as never)
    ElMessage.error((e as Error)?.message || '上传失败')
  }
}

function removeProgressAttachment(index: number): void {
  progressDraftAttachments.value.splice(index, 1)
}

async function submitProgressLog(): Promise<void> {
  if (!row.value || !canEditProgressLogs.value) return
  const content = progressLogDraft.value.trim()
  if (!content) {
    ElMessage.warning('请填写过程记录内容')
    return
  }
  postingProgress.value = true
  try {
    try {
      const created = await EngagementRequestService.createProgressLog(row.value.id, {
        content,
        attachments: progressDraftAttachments.value.length ? [...progressDraftAttachments.value] : undefined,
      })
      progressLogs.value = [created, ...progressLogs.value]
    } catch {
      const localCreated: EngagementProgressLog = {
        id: Date.now(),
        requestId: row.value.id,
        authorId: auth.userId || 0,
        authorName: auth.username || '当前用户',
        authorRole: inferAuthorRole(),
        content,
        attachments: progressDraftAttachments.value.length ? [...progressDraftAttachments.value] : [],
        createdAt: new Date().toISOString(),
      }
      progressLogs.value = [localCreated, ...progressLogs.value]
      writeLocalProgressLogs(row.value.id, progressLogs.value)
      ElMessage.info('已保存过程记录（本地模式）')
    }
    progressLogDraft.value = ''
    progressDraftAttachments.value = []
    ElMessage.success('过程记录已发布')
  } finally {
    postingProgress.value = false
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
  if (!canReleaseByProgressRule.value) {
    ElMessage.warning('请先补充至少 1 条执行协同记录，再进行积分放分')
    return
  }
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
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
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
.phase-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
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
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}
.progress-item {
  border: 1px solid #ebeef5;
}
.progress-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  color: #909399;
  font-size: 12px;
}
.progress-author {
  color: #303133;
  font-weight: 600;
}
.progress-content {
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(.el-descriptions__cell) {
  word-break: break-word;
  white-space: pre-wrap;
}
</style>
