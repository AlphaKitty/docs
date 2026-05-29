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
        <el-step v-for="s in stepDefs" :key="s" :title="s" />
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
        <el-descriptions-item label="任务描述（原文）" :span="2">{{ row.taskDescription || '—' }}</el-descriptions-item>
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
        <!-- 系统建议分：按专家分开展示 -->
        <el-descriptions-item v-if="expertSuggestedScoreRows.length > 0" label="系统建议分（按专家）" :span="2">
          <div v-for="item in expertSuggestedScoreRows" :key="item.expertId" class="score-row">
            <span class="score-expert-name">{{ item.expertName || `专家 #${item.expertId}` }}：</span>
            <span class="score-value">{{ item.suggestedScore ?? '—' }} 分</span>
          </div>
          <span class="hint" style="margin-top:4px;display:inline-block;">评价维度预计算，供放分参考</span>
        </el-descriptions-item>
        <el-descriptions-item v-else-if="row.suggestedScore != null" label="系统建议分">
          {{ row.suggestedScore }}（评价维度预计算，供放分参考）
        </el-descriptions-item>
        <!-- 行管确认分：按专家分开展示 -->
        <el-descriptions-item v-if="expertStewardScoreRows.length > 0" label="行管确认分（按专家）" :span="2">
          <div v-for="item in expertStewardScoreRows" :key="item.expertId" class="score-row">
            <span class="score-expert-name">{{ item.expertName || `专家 #${item.expertId}` }}：</span>
            <span class="score-value">{{ item.finalScore }} 分</span>
            <el-tag v-if="item.reason" type="info" size="small" class="score-reason">{{ item.reason }}</el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-else-if="row.stewardFinalScore != null" label="行管确认分">
          {{ row.stewardFinalScore }}
        </el-descriptions-item>
      </el-descriptions>

      <el-card v-if="structuredInfoGroups.length || fusionExtraText" class="mt" shadow="never">
        <template #header>申请表详情（融合字段）</template>
        <div class="structured-groups">
          <el-card
            v-for="group in structuredInfoGroups"
            :key="group.title"
            shadow="never"
            class="structured-group"
          >
            <template #header>{{ group.title }}</template>
            <el-descriptions :column="2" border>
              <el-descriptions-item
                v-for="item in group.items"
                :key="`${group.title}-${item.label}`"
                :label="item.required ? `${item.label}（必填）` : item.label"
              >
                {{ item.value }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
        <div v-if="parsedAttachments.length" class="mt-row attachments-row">
          <span class="attachments-label">附件：</span>
          <el-space wrap>
            <el-button
              v-for="(p, idx) in parsedAttachments"
              :key="idx"
              link
              type="primary"
              @click="downloadAttachment(p)"
            >
              附件 {{ idx + 1 }}
            </el-button>
          </el-space>
        </div>
        <el-descriptions v-if="fusionExtraText" :column="1" border class="mt-row">
          <el-descriptions-item label="补充描述">
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
        <p class="hint">须为已关联本领域或其子领域的专家（后端按领域子树校验）；可多选，所选专家均需确认接受后进入执行中。</p>
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
        <p class="hint">待确认或执行中可改派；可选本领域及子领域下专家（与指派一致）；改派后新名单需重新确认（可多选）。</p>
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
          <el-card v-if="pointsEvalRulesDisplay" shadow="never" class="points-eval-rules mb">
            <template #header>
              <span>积分规则说明</span>
              <el-tag v-if="currentPointsItem" type="info" size="small" class="ml-sm">{{ currentPointsItem }}</el-tag>
            </template>
            <div v-if="pointsEvalRulesDisplay.hint" class="rule-block">
              <div class="rule-title">积分提示</div>
              <p class="rule-body">{{ pointsEvalRulesDisplay.hint }}</p>
            </div>
            <div v-if="pointsEvalRulesDisplay.standard" class="rule-block">
              <div class="rule-title">积分标准</div>
              <p class="rule-body pre-line">{{ pointsEvalRulesDisplay.standard }}</p>
            </div>
            <div v-if="pointsEvalRulesDisplay.scopeLines" class="rule-block">
              <div class="rule-title">贡献范围与标准分（本单对照）</div>
              <p class="rule-body pre-line">{{ pointsEvalRulesDisplay.scopeLines }}</p>
            </div>
          </el-card>
          <el-alert
            type="info"
            :closable="false"
            class="mb"
            title="请对每位专家分别评价（按积分项目规则）"
            :description="`当前积分项目：${currentPointsItem || '未识别'}，请补充贡献范围、评分等级和评语。`"
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
            <el-form-item label="贡献范围">
              <el-select v-model="item.contributionScope" :disabled="!currentContributionScopeOptions.length">
                <el-option
                  v-for="scope in currentContributionScopeOptions"
                  :key="scope"
                  :label="scope"
                  :value="scope"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="标准分">
              <el-input :model-value="item.baseScore.toFixed(2)" disabled />
            </el-form-item>
            <el-form-item label="申请人评分等级">
              <el-select v-model="item.applicantLevel" :disabled="!currentApplicantLevelOptions.length">
                <el-option
                  v-for="level in currentApplicantLevelOptions"
                  :key="level.label"
                  :label="level.label"
                  :value="level.label"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="评分系数">
              <el-input :model-value="item.applicantCoefficient.toFixed(2)" disabled />
            </el-form-item>
            <el-form-item label="建议分">
              <el-input :model-value="item.applicantSuggestedScore.toFixed(2)" disabled />
            </el-form-item>
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
        <el-card v-if="pointsEvalRulesDisplay" shadow="never" class="points-eval-rules mb">
          <template #header>
            <span>积分规则说明</span>
            <el-tag v-if="currentPointsItem" type="info" size="small" class="ml-sm">{{ currentPointsItem }}</el-tag>
          </template>
          <div v-if="pointsEvalRulesDisplay.hint" class="rule-block">
            <div class="rule-title">积分提示</div>
            <p class="rule-body">{{ pointsEvalRulesDisplay.hint }}</p>
          </div>
          <div v-if="pointsEvalRulesDisplay.standard" class="rule-block">
            <div class="rule-title">积分标准</div>
            <p class="rule-body pre-line">{{ pointsEvalRulesDisplay.standard }}</p>
          </div>
          <div v-if="pointsEvalRulesDisplay.scopeLines" class="rule-block">
            <div class="rule-title">贡献范围与标准分（本单对照）</div>
            <p class="rule-body pre-line">{{ pointsEvalRulesDisplay.scopeLines }}</p>
          </div>
        </el-card>
        <p v-if="row.suggestedScore != null" class="hint">系统建议分：{{ row.suggestedScore }}，可直接作为放分参考。</p>
        <el-alert
          v-if="isExpertCallApply"
          type="info"
          :closable="false"
          class="mb"
          title="专家调用：可按积分项目规则调整每位专家的评价等级，或直接修改最终分；说明可选填。"
        />
        <el-alert
          v-else
          type="info"
          :closable="false"
          class="mb"
          title="积分自提：请选择认可或不认可申请人提交的建议分；不认可须填写理由。"
        />
        <el-card v-for="item in expertEvalForms" :key="`steward-${item.expertId}`" class="mt" shadow="never">
          <template #header>
            <span>专家：{{ item.expertName || `#${item.expertId}` }}</span>
          </template>
          <el-form v-if="isExpertCallApply" label-width="120px">
            <el-form-item label="申请人评分等级">
              <el-input :model-value="item.applicantLevel || '—'" disabled />
            </el-form-item>
            <el-form-item label="申请人建议分">
              <el-input :model-value="item.applicantSuggestedScore.toFixed(2)" disabled />
            </el-form-item>
            <el-form-item v-if="currentApplicantLevelOptions.length" label="行管评分等级">
              <el-select v-model="item.stewardLevel" placeholder="选择等级" style="width: 100%">
                <el-option
                  v-for="level in currentApplicantLevelOptions"
                  :key="level.label"
                  :label="level.label"
                  :value="level.label"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="按等级折算分">
              <el-input :model-value="stewardLevelDerivedScore(item).toFixed(2)" disabled />
            </el-form-item>
            <el-form-item label="最终分">
              <el-input-number v-model="item.stewardFinalScore" :precision="2" :step="0.5" :min="0" />
            </el-form-item>
            <el-form-item label="说明">
              <el-input v-model="item.stewardReason" type="textarea" :rows="2" placeholder="可选" />
            </el-form-item>
          </el-form>
          <el-form v-else label-width="120px">
            <el-form-item label="申请人建议分">
              <el-input :model-value="item.applicantSuggestedScore.toFixed(2)" disabled />
            </el-form-item>
            <el-form-item label="审核结果">
              <el-select v-model="item.stewardDecision">
                <el-option label="认可" value="APPROVE" />
                <el-option label="不认可" value="REJECT" />
              </el-select>
            </el-form-item>
            <el-form-item label="最终分">
              <el-input-number
                v-model="item.stewardFinalScore"
                :precision="2"
                :step="0.5"
                :min="0"
                :disabled="item.stewardDecision === 'APPROVE' || item.stewardDecision === 'REJECT'"
              />
            </el-form-item>
            <el-form-item label="评分理由">
              <el-input
                v-model="item.stewardReason"
                type="textarea"
                :rows="2"
                :placeholder="item.stewardDecision === 'APPROVE' ? '认可可选填；不认可必填' : '请填写不认可理由'"
              />
            </el-form-item>
          </el-form>
        </el-card>
        <el-form-item label="确认总分" class="mt-row">
          <el-input-number v-model="finalScore" :precision="2" :step="0.5" placeholder="自动汇总，可手动改" />
        </el-form-item>
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
import {
  APPLICANT_LEVELS_BY_ITEM,
  BASE_SCORE_BY_ITEM_SCOPE,
  CONTRIBUTION_SCOPE_BY_ITEM,
  RESULT_SUMMARY_HIDDEN_ITEMS,
  shouldShowProjectInfoFields,
} from './engagement-form-config'
import { getPointsEvalRulesDisplay } from './engagement-eval-display-rules'

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
  contributionScope: string
  baseScore: number
  applicantLevel: string
  applicantCoefficient: number
  applicantSuggestedScore: number
  professional: number
  timeliness: number
  attitude: number
  resolved: boolean
  comment: string
  /** 专家调用放分：行管调整后的评价等级（与申请人等级同一套选项） */
  stewardLevel: string
  /** 积分自提放分：仅认可 / 不认可 */
  stewardDecision: 'APPROVE' | 'ADJUST' | 'REJECT'
  stewardFinalScore: number
  stewardReason: string
}

type EvalCommentDetailRow = {
  expertId: number
  expertName?: string
  professional?: number
  timeliness?: number
  attitude?: number
  resolved?: boolean
  comment?: string
  suggestedScore?: number
}

function parseEvalCommentRows(raw: string | null | undefined): EvalCommentDetailRow[] | null {
  if (!raw?.trim()) return null
  try {
    const p = JSON.parse(raw) as unknown
    if (!Array.isArray(p)) return null
    return p.filter((x) => x && typeof (x as EvalCommentDetailRow).expertId === 'number') as EvalCommentDetailRow[]
  } catch {
    return null
  }
}

function parseEmbeddedEvalComment(comment: string | undefined): {
  text?: string
  contributionScope?: string
  baseScore?: number
  applicantLevel?: string
  applicantCoefficient?: number
  applicantSuggestedScore?: number
} {
  if (!comment?.trim()) return {}
  try {
    const o = JSON.parse(comment) as Record<string, unknown>
    return {
      text: typeof o.text === 'string' ? o.text : undefined,
      contributionScope: typeof o.contributionScope === 'string' ? o.contributionScope : undefined,
      baseScore: typeof o.baseScore === 'number' ? o.baseScore : undefined,
      applicantLevel: typeof o.applicantLevel === 'string' ? o.applicantLevel : undefined,
      applicantCoefficient: typeof o.applicantCoefficient === 'number' ? o.applicantCoefficient : undefined,
      applicantSuggestedScore: typeof o.applicantSuggestedScore === 'number' ? o.applicantSuggestedScore : undefined,
    }
  } catch {
    return { text: comment }
  }
}

function stewardLevelDerivedScore(item: ExpertEvalFormItem): number {
  const pointsItem = currentPointsItem.value
  const level = item.stewardLevel || item.applicantLevel
  const coeff = getApplicantCoefficient(pointsItem, level)
  return Number((item.baseScore * coeff).toFixed(2))
}

const expertEvalForms = ref<ExpertEvalFormItem[]>([])

/** 解析 evalComment 中的每位专家建议分 */
const expertSuggestedScoreRows = computed<EvalCommentDetailRow[]>(() => {
  if (!row.value?.evalComment) return []
  return parseEvalCommentRows(row.value.evalComment) || []
})

/** 解析 stewardReleaseNote 中的每位专家确认分 */
const expertStewardScoreRows = computed<Array<{
  expertId: number
  expertName: string
  finalScore: number
  reason?: string
}>>(() => {
  const releaseNote = row.value?.stewardReleaseNote
  if (!releaseNote?.trim()) return []
  // stewardReleaseNote 可能是 "说明文字\n{json}" 或直接 "{json}"
  const jsonPart = releaseNote.includes('\n')
    ? releaseNote.slice(releaseNote.indexOf('\n') + 1)
    : releaseNote
  try {
    const parsed = JSON.parse(jsonPart) as {
      items?: Array<{
        expertId: number
        finalScore?: number
        suggestedScore?: number
        reason?: string
        [key: string]: unknown
      }>
    }
    if (!parsed.items?.length) return []
    const names = row.value?.assignedExpertNames || []
    const ids = row.value?.assignedExpertIds || []
    return parsed.items.map((item) => ({
      expertId: item.expertId,
      expertName: names[ids.indexOf(item.expertId)] || `专家 #${item.expertId}`,
      finalScore: item.finalScore ?? item.suggestedScore ?? 0,
      reason: item.reason,
    }))
  } catch {
    return []
  }
})

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

const parsedTaskDescription = computed(() => parseStructuredTaskDescription(row.value?.taskDescription || ''))

const structuredInfoGroups = computed(() => {
  const all = parsedTaskDescription.value.map
  const groups: Array<{ title: string; items: Array<{ label: string; value: string; required: boolean }> }> = []
  const pickItems = (keys: string[]) =>
    keys
      .map((key) => ({ label: key, value: all[key], required: isFieldRequired(key, all) }))
      .filter((item) => Boolean(item.value))

  const applyInfo = pickItems(['申请类别', '积分大类', '积分项目'])
  if (applyInfo.length) groups.push({ title: '申请信息', items: applyInfo })

  const requesterInfo = pickItems(['需求人', '需求人部门', '需求人职位', '联系方式'])
  if (requesterInfo.length) groups.push({ title: '需求方信息', items: requesterInfo })

  const projectInfo = pickItems(['项目部门', '项目名称', '项目级别', '客户代码', '产品线', '当前阶段', '是否KDW', '是否迭代产品'])
  if (projectInfo.length) groups.push({ title: '项目基本信息', items: projectInfo })

  const activityInfo = pickItems(['活动名称', '活动地点', '贡献范围', '活动需求信息', '成果提交简述', '专家价值'])
  if (activityInfo.length) groups.push({ title: '活动信息', items: activityInfo })

  const expertInfo = pickItems(['需求人数'])
  if (expertInfo.length) groups.push({ title: '专家需求', items: expertInfo })

  return groups
})

const fusionExtraText = computed(() => parsedTaskDescription.value.extraText)
const currentPointsItem = computed(() => parsedTaskDescription.value.map['积分项目'] || '')
const currentContributionScopeOptions = computed(() => {
  return CONTRIBUTION_SCOPE_BY_ITEM[currentPointsItem.value] || []
})
const currentApplicantLevelOptions = computed(() => {
  return APPLICANT_LEVELS_BY_ITEM[currentPointsItem.value] || []
})

/** 评价打分 / 放分页顶部：积分提示、积分标准、贡献范围与标准分 */
const pointsEvalRulesDisplay = computed(() => getPointsEvalRulesDisplay(currentPointsItem.value))

const applyCategory = computed(() => parsedTaskDescription.value.map['申请类别'] || '')
const isExpertCallApply = computed(() => applyCategory.value === '专家调用')

const parsedAttachments = computed(() => {
  const attachmentLine = parsedTaskDescription.value.map['附件'] || ''
  if (!attachmentLine.trim()) return []
  return attachmentLine.split(';').map((s: string) => s.trim()).filter(Boolean)
})

const selfPickStepOrder = [
  'DRAFT',
  'IN_PROGRESS',
  'PENDING_STEWARD_SCORE_RELEASE',
  'COMPLETED',
] as const

const normalStepOrder = [
  'DRAFT',
  'PENDING_STEWARD_ASSIGN',
  'PENDING_EXPERT_CONFIRM',
  'IN_PROGRESS',
  'PENDING_STEWARD_SCORE_RELEASE',
  'COMPLETED',
] as const

const selfPickStepDefs = ['草稿', '执行中', '待放分', '已结项']
const normalStepDefs = ['草稿', '待行管指派', '专家确认', '执行中', '待放分', '已结项']

const stepDefs = computed(() => (row.value?.isSelfPick ? selfPickStepDefs : normalStepDefs))

const stepMeta = computed(() => {
  if (!row.value) return { active: 0, stepsStatus: undefined as 'error' | 'process' | 'wait' | 'finish' | 'success' | undefined }
  const s = row.value.status
  const order: readonly string[] = row.value.isSelfPick ? selfPickStepOrder : normalStepOrder
  if (s === 'REJECTED') return { active: 2, stepsStatus: 'error' as const }
  if (s === 'CANCELLED') return { active: 1, stepsStatus: 'error' as const }
  const i = order.indexOf(s)
  if (s === 'COMPLETED') return { active: order.length, stepsStatus: undefined }
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
  // 拆分需求人：张三 / e-HR带出 / 项目经理 → 需求人 + 需求人部门 + 需求人职位
  const requesterRaw = result['需求人']
  if (requesterRaw) {
    const parts = requesterRaw.split(' / ').map((s: string) => s.trim())
    if (parts.length >= 3) {
      result['需求人'] = parts[0]
      result['需求人部门'] = parts[1]
      result['需求人职位'] = parts[2]
    }
  }
  if (result['补充描述']) {
    extraText = result['补充描述']
  } else if (taskDescription.trim()) {
    extraText = taskDescription.trim()
  }
  return { map: result, extraText }
}

function isFieldRequired(label: string, all: Record<string, string>): boolean {
  const baseRequired = new Set([
    '申请类别',
    '积分大类',
    '积分项目',
    '需求人',
    '联系方式',
    '活动名称',
    '活动需求信息',
    '附件',
    '专家价值',
  ])
  if (baseRequired.has(label)) return true

  const pointsCategory = all['积分大类'] || ''
  const pointsItem = all['积分项目'] || ''
  const showProjectInfo = shouldShowProjectInfoFields(pointsCategory, pointsItem)
  if (
    showProjectInfo &&
    ['项目部门', '项目名称', '项目级别', '客户代码', '产品线', '当前阶段', '是否KDW', '是否迭代产品'].includes(label)
  ) {
    return true
  }
  if (label === '贡献范围') {
    return (CONTRIBUTION_SCOPE_BY_ITEM[pointsItem] || []).length > 0
  }
  if (label === '成果提交简述') {
    return !RESULT_SUMMARY_HIDDEN_ITEMS.has(pointsItem)
  }
  return false
}

function getBaseScore(pointsItem: string, contributionScope: string): number {
  const map = BASE_SCORE_BY_ITEM_SCOPE[pointsItem] || {}
  if (contributionScope && typeof map[contributionScope] === 'number') return map[contributionScope]
  if (typeof map['默认'] === 'number') return map['默认']
  return 0
}

function getApplicantCoefficient(pointsItem: string, applicantLevel: string): number {
  const options = APPLICANT_LEVELS_BY_ITEM[pointsItem] || []
  return options.find((item) => item.label === applicantLevel)?.coefficient ?? 1
}

function syncApplicantEvalDerivedScores(item: ExpertEvalFormItem): void {
  const pointsItem = currentPointsItem.value
  item.baseScore = getBaseScore(pointsItem, item.contributionScope)
  item.applicantCoefficient = getApplicantCoefficient(pointsItem, item.applicantLevel)
  item.applicantSuggestedScore = Number((item.baseScore * item.applicantCoefficient).toFixed(2))
}

function syncStewardSelfApproveReject(item: ExpertEvalFormItem): void {
  if (item.stewardDecision === 'APPROVE') {
    item.stewardFinalScore = item.applicantSuggestedScore
  } else if (item.stewardDecision === 'REJECT') {
    item.stewardFinalScore = 0
  }
}

function syncStewardExpertCallScoreFromLevel(item: ExpertEvalFormItem): void {
  item.stewardFinalScore = stewardLevelDerivedScore(item)
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
    : r.assignedExpertId
      ? [r.assignedExpertId]
      : []
  const names = r.assignedExpertNames?.length
    ? r.assignedExpertNames
    : r.assignedExpertName
      ? [r.assignedExpertName]
      : []
  const pointsItem = currentPointsItem.value
  const scopeOptions = CONTRIBUTION_SCOPE_BY_ITEM[pointsItem] || []
  const levelOptions = APPLICANT_LEVELS_BY_ITEM[pointsItem] || []
  const defaultScope = scopeOptions[0] || ''
  const defaultLevel = levelOptions[0]?.label || ''

  const hydrateRows =
    r.status === 'PENDING_STEWARD_SCORE_RELEASE' && r.evalComment ? parseEvalCommentRows(r.evalComment) : null
  const byExpertId = new Map((hydrateRows || []).map((row) => [row.expertId, row]))
  const expertCall = isExpertCallApply.value

  expertEvalForms.value = ids.map((expertId, idx) => {
    const detail = byExpertId.get(expertId)
    const emb = parseEmbeddedEvalComment(detail?.comment)
    const contributionScope = emb.contributionScope ?? defaultScope
    const applicantLevel = emb.applicantLevel ?? defaultLevel
    const baseFromEmb = typeof emb.baseScore === 'number' ? emb.baseScore : undefined
    const baseScore =
      baseFromEmb !== undefined ? baseFromEmb : getBaseScore(pointsItem, contributionScope)
    const applicantCoefficient =
      typeof emb.applicantCoefficient === 'number'
        ? emb.applicantCoefficient
        : getApplicantCoefficient(pointsItem, applicantLevel)
    let applicantSuggestedScore =
      typeof emb.applicantSuggestedScore === 'number'
        ? emb.applicantSuggestedScore
        : Number((baseScore * applicantCoefficient).toFixed(2))
    if (Number.isNaN(applicantSuggestedScore)) {
      applicantSuggestedScore = Number((baseScore * applicantCoefficient).toFixed(2))
    }
    if (
      r.status === 'PENDING_STEWARD_SCORE_RELEASE' &&
      !expertCall &&
      typeof emb.applicantSuggestedScore !== 'number' &&
      detail?.suggestedScore != null
    ) {
      applicantSuggestedScore = Number(Number(detail.suggestedScore).toFixed(2))
    }

    const professional = detail?.professional ?? 5
    const timeliness = detail?.timeliness ?? 5
    const attitude = detail?.attitude ?? 5
    const resolved = detail?.resolved ?? true
    const comment =
      emb.text ||
      (detail?.comment && !detail.comment.trim().startsWith('{') ? detail.comment : '') ||
      ''

    const stewardLevel = expertCall ? applicantLevel : ''
    let stewardDecision: ExpertEvalFormItem['stewardDecision'] = 'APPROVE'
    let stewardFinalScore = applicantSuggestedScore
    if (expertCall && r.status === 'PENDING_STEWARD_SCORE_RELEASE') {
      const coeff = getApplicantCoefficient(pointsItem, stewardLevel || applicantLevel)
      stewardFinalScore = Number((baseScore * coeff).toFixed(2))
    } else if (!expertCall) {
      stewardFinalScore = applicantSuggestedScore
    }

    return {
      expertId,
      expertName: names[idx] || detail?.expertName || '',
      contributionScope,
      baseScore,
      applicantLevel,
      applicantCoefficient,
      applicantSuggestedScore,
      professional,
      timeliness,
      attitude,
      resolved,
      comment,
      stewardLevel,
      stewardDecision,
      stewardFinalScore,
      stewardReason: '',
    }
  })
}

watch(
  () => route.params.id,
  () => {
    void load()
  }
)

watch(
  () => currentPointsItem.value,
  () => {
    if (row.value?.status !== 'IN_PROGRESS') return
    for (const item of expertEvalForms.value) {
      const scopeOptions = currentContributionScopeOptions.value
      const levelOptions = currentApplicantLevelOptions.value
      if (scopeOptions.length && !scopeOptions.includes(item.contributionScope)) {
        item.contributionScope = scopeOptions[0]
      }
      if (levelOptions.length && !levelOptions.some((opt) => opt.label === item.applicantLevel)) {
        item.applicantLevel = levelOptions[0]?.label || ''
      }
      syncApplicantEvalDerivedScores(item)
      syncStewardSelfApproveReject(item)
    }
  }
)

watch(
  () =>
    expertEvalForms.value.map((item) => ({
      scope: item.contributionScope,
      applicantLevel: item.applicantLevel,
      stewardLevel: item.stewardLevel,
      decision: item.stewardDecision,
      suggested: item.applicantSuggestedScore,
      final: item.stewardFinalScore,
    })),
  () => {
    const st = row.value?.status
    if (!row.value) return
    if (st === 'IN_PROGRESS') {
      for (const item of expertEvalForms.value) {
        syncApplicantEvalDerivedScores(item)
        syncStewardSelfApproveReject(item)
      }
    } else if (st === 'PENDING_STEWARD_SCORE_RELEASE' && !isExpertCallApply.value) {
      for (const item of expertEvalForms.value) {
        if (item.stewardDecision === 'ADJUST') item.stewardDecision = 'APPROVE'
        syncStewardSelfApproveReject(item)
      }
    }
    if (expertEvalForms.value.length) {
      const sum = expertEvalForms.value.reduce((acc, item) => acc + Number(item.stewardFinalScore || 0), 0)
      finalScore.value = Number(sum.toFixed(2))
    }
  },
  { deep: true }
)

watch(
  () =>
    row.value?.status === 'PENDING_STEWARD_SCORE_RELEASE' && isExpertCallApply.value
      ? expertEvalForms.value.map((i) => `${i.expertId}:${i.stewardLevel}:${i.baseScore}`)
      : '',
  () => {
    if (row.value?.status !== 'PENDING_STEWARD_SCORE_RELEASE' || !isExpertCallApply.value) return
    for (const item of expertEvalForms.value) {
      syncStewardExpertCallScoreFromLevel(item)
    }
    if (expertEvalForms.value.length) {
      finalScore.value = Number(
        expertEvalForms.value.reduce((acc, item) => acc + Number(item.stewardFinalScore || 0), 0).toFixed(2)
      )
    }
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
  const missingScope = currentContributionScopeOptions.value.length
    ? expertEvalForms.value.some((item) => !item.contributionScope)
    : false
  if (missingScope) {
    ElMessage.warning('请为每位专家选择贡献范围')
    return
  }
  const missingLevel = currentApplicantLevelOptions.value.length
    ? expertEvalForms.value.some((item) => !item.applicantLevel)
    : false
  if (missingLevel) {
    ElMessage.warning('请为每位专家选择申请人评分等级')
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
    .map(
      (item) =>
        `${item.expertName || `专家#${item.expertId}`}: 范围=${item.contributionScope || '—'}，等级=${item.applicantLevel || '—'}，建议分=${item.applicantSuggestedScore.toFixed(2)}，评语=${item.comment.trim()}`
    )
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
        comment:
          JSON.stringify({
            text: item.comment.trim() || undefined,
            pointsItem: currentPointsItem.value,
            contributionScope: item.contributionScope || undefined,
            baseScore: item.baseScore,
            applicantLevel: item.applicantLevel || undefined,
            applicantCoefficient: item.applicantCoefficient,
            applicantSuggestedScore: item.applicantSuggestedScore,
          }) || undefined,
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
  const missingStewardReason = expertEvalForms.value.some(
    (item) => !isExpertCallApply.value && item.stewardDecision === 'REJECT' && !item.stewardReason.trim()
  )
  if (missingStewardReason) {
    ElMessage.warning('不认可时请填写评分理由')
    return
  }
  const computedFinalScore = Number(
    expertEvalForms.value.reduce((acc, item) => acc + Number(item.stewardFinalScore || 0), 0).toFixed(2)
  )
  if (finalScore.value == null || Number.isNaN(Number(finalScore.value))) {
    finalScore.value = computedFinalScore
  }
  const structuredReleaseNote = JSON.stringify({
    pointsItem: currentPointsItem.value,
    applyCategory: applyCategory.value,
    items: expertEvalForms.value.map((item) =>
      isExpertCallApply.value
        ? {
            expertId: item.expertId,
            stewardLevel: item.stewardLevel || item.applicantLevel,
            applicantLevel: item.applicantLevel,
            applicantSuggestedScore: item.applicantSuggestedScore,
            finalScore: item.stewardFinalScore,
            reason: item.stewardReason.trim() || undefined,
          }
        : {
            expertId: item.expertId,
            decision: item.stewardDecision,
            suggestedScore: item.applicantSuggestedScore,
            finalScore: item.stewardFinalScore,
            reason: item.stewardReason.trim() || undefined,
          }
    ),
  })
  acting.value = true
  try {
    const r = await EngagementRequestService.releaseScore(
      row.value.id,
      finalScore.value,
      releaseNote.value
        ? `${releaseNote.value}\n${structuredReleaseNote}`
        : structuredReleaseNote
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
    const downloadUrl = path.startsWith('/files/')
      ? `/api/files/${encodeURIComponent(fileName)}`
      : `/api/engagement-requests/${id.value}/evaluation-files/${encodeURIComponent(fileName)}`
    const res = await fetch(downloadUrl, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
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
.structured-groups {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.structured-group {
  border: 1px solid #ebeef5;
}

.attachments-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.attachments-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  flex-shrink: 0;
}

:deep(.el-descriptions__cell) {
  word-break: break-word;
  white-space: pre-wrap;
}

.points-eval-rules {
  border: 1px solid var(--el-border-color-lighter);
}
.points-eval-rules :deep(.el-card__header) {
  padding: 10px 14px;
}
.points-eval-rules :deep(.el-card__body) {
  padding: 12px 14px 14px;
}
.ml-sm {
  margin-left: 8px;
  vertical-align: middle;
}
.rule-block + .rule-block {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--el-border-color-lighter);
}
.rule-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
}
.rule-body {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.55;
}
.rule-body.pre-line {
  white-space: pre-line;
}
.score-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0;
}
.score-row + .score-row {
  margin-top: 2px;
}
.score-expert-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}
.score-value {
  color: var(--el-color-primary);
  font-weight: 700;
}
.score-reason {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
