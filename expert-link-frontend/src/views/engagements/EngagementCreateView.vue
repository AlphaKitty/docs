<template>
  <div v-loading="loading" class="page">
    <div class="head">
      <h2>新建专家活动申请（草稿）</h2>
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
          <el-form-item label="申请类别" required>
            <el-select v-model="extraForm.applyCategory">
              <el-option v-for="item in applyCategoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="积分大类" required>
            <el-select v-model="extraForm.pointsCategory">
              <el-option v-for="item in pointsCategoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="积分项目" required>
            <el-select v-model="extraForm.pointsItem" filterable>
              <el-option v-for="item in pointsItemOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="!isSelfPick" label="调用模式" required>
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
          <el-form-item label="需求人" required>
            <el-select v-model="extraForm.requester" filterable placeholder="搜索选择需求人（默认当前用户）">
              <el-option
                v-for="emp in mockEmployeeOptions"
                :key="emp.id"
                :label="`${emp.name} (${emp.id})`"
                :value="emp.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="需求人部门" required>
            <el-input v-model="extraForm.requestDept" placeholder="输入部门" />
          </el-form-item>
          <el-form-item label="需求人职位" required>
            <el-input v-model="extraForm.requestPosition" placeholder="输入职位" />
          </el-form-item>
          <el-form-item label="联系方式" required>
            <el-input v-model="extraForm.contact" placeholder="手机/邮箱" />
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>项目基本信息</template>
        <div class="grid four">
          <el-form-item v-if="showProjectInfoFields" label="项目部门" :required="showProjectInfoFields">
            <el-select v-model="extraForm.projectDept" filterable placeholder="默认需求人部门，可改选">
              <el-option v-for="d in mockDeptOptions" :key="d" :label="d" :value="d" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="项目名称" :required="showProjectInfoFields">
            <el-select
              v-model="extraForm.projectName"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入项目名称"
            >
              <el-option v-for="n in mockProjectNameOptions" :key="n" :label="n" :value="n" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="项目级别" :required="showProjectInfoFields">
            <el-select v-model="extraForm.projectLevel" placeholder="选择项目级别">
              <el-option v-for="l in mockProjectLevelOptions" :key="l" :label="l" :value="l" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="客户代码" :required="showProjectInfoFields">
            <el-select
              v-model="extraForm.customerCode"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入客户代码"
            >
              <el-option v-for="c in mockCustomerCodeOptions" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="产品线" :required="showProjectInfoFields">
            <el-select
              v-model="extraForm.productLine"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入产品线"
            >
              <el-option v-for="p in mockProductLineOptions" :key="p" :label="p" :value="p" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="当前阶段" :required="showProjectInfoFields">
            <el-select v-model="extraForm.currentStage" placeholder="选择项目阶段">
              <el-option v-for="s in mockProjectStageOptions" :key="s" :label="s" :value="s" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="是否KDW" :required="showProjectInfoFields">
            <el-select v-model="extraForm.isKdw">
              <el-option label="是" value="是" />
              <el-option label="否" value="否" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="showProjectInfoFields" label="是否迭代产品" :required="showProjectInfoFields">
            <el-select v-model="extraForm.isIterative">
              <el-option label="是" value="是" />
              <el-option label="否" value="否" />
            </el-select>
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header>活动信息</template>
        <div class="grid four">
          <el-form-item label="活动名称" required>
            <el-input v-model="extraForm.activityName" :placeholder="activityNamePlaceholder" />
          </el-form-item>
          <el-form-item label="活动时间" required>
            <el-date-picker
              v-model="activityTimeRange"
              type="datetimerange"
              range-separator="~"
              start-placeholder="开始"
              end-placeholder="结束"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="活动地点" :required="activityLocationRequired">
            <div class="location-cascade">
              <el-select v-model="extraForm.locationRegion" placeholder="区域" @change="onLocationRegionChange">
                <el-option v-for="r in mockLocationRegions" :key="r" :label="r" :value="r" />
              </el-select>
              <el-select v-model="extraForm.locationZone" placeholder="厂区" :disabled="!extraForm.locationRegion">
                <el-option v-for="z in mockLocationZones" :key="z" :label="z" :value="z" />
              </el-select>
              <el-select v-model="extraForm.locationBuilding" placeholder="楼栋" :disabled="!extraForm.locationZone">
                <el-option v-for="b in mockLocationBuildings" :key="b" :label="b" :value="b" />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item v-if="showContributionScope" label="贡献范围" :required="showContributionScope">
            <el-select v-model="extraForm.contributionScope">
              <el-option
                v-for="item in contributionScopeOptions"
                :key="item"
                :label="item"
                :value="item"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="活动需求信息" required>
          <el-input
            v-model="extraForm.activityInfo"
            type="textarea"
            :rows="4"
            :placeholder="activityInfoPlaceholder"
          />
        </el-form-item>
        <el-form-item v-if="showResultSummary" label="成果提交简述" :required="showResultSummary">
          <el-input
            v-model="extraForm.resultSummary"
            type="textarea"
            :rows="3"
            :placeholder="resultSummaryPlaceholder"
          />
        </el-form-item>
        <div class="grid four">
          <el-form-item label="专家价值" required>
            <el-input v-model="extraForm.expertValue" placeholder="需求专家参与活动给公司/BG带来的期望价值评估" />
          </el-form-item>
          <el-form-item label="附件" required>
            <el-upload
              :http-request="onAttachmentUpload"
              :limit="5"
              :show-file-list="true"
            >
              <el-button type="primary" plain>上传附件</el-button>
              <template #tip>
                <div class="hint">PDF/JPEG/PNG/Word，尽量可供预览</div>
              </template>
            </el-upload>
            <div v-if="attachmentPaths.length" class="mt-row">
              <el-tag
                v-for="(p, i) in attachmentPaths"
                :key="i"
                closable
                class="tag"
                @close="removeAttachment(i)"
              >
                {{ p }}
              </el-tag>
            </div>
          </el-form-item>
        </div>
      </el-card>

      <el-card v-if="!isSelfPick" shadow="never" class="section">
        <template #header>专家需求</template>
        <el-form-item label="需求领域" required>
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
          <el-form-item label="需求人数">
            <el-input-number v-model="extraForm.requiredCount" :min="1" :max="20" style="width: 100%" />
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
import type { UploadRequestOptions } from 'element-plus'
import { DomainService } from '@/api/services/domain.service'
import { EngagementRequestService } from '@/api/services/engagement-request.service'
import { ExpertService } from '@/api/services/expert.service'
import type { EngagementMode, EngagementTaskType } from '@/api/types/engagement'
import type { ExpertDetail } from '@/api/types/expert'
import {
  ACTIVITY_INFO_PLACEHOLDER_BY_ITEM,
  ACTIVITY_LOCATION_REQUIRED_ITEMS,
  ACTIVITY_NAME_PLACEHOLDER_BY_APPLY_TYPE,
  APPLY_CATEGORY_OPTIONS,
  CONTRIBUTION_SCOPE_BY_ITEM,
  POINTS_CATEGORY_BY_APPLY_CATEGORY,
  POINTS_ITEM_BY_CATEGORY,
  shouldShowProjectInfoFields,
  RESULT_SUMMARY_HIDDEN_ITEMS,
  RESULT_SUMMARY_PLACEHOLDER_BY_ITEM,
} from './engagement-form-config'

type DomainOption = { id: number; name: string; parentId?: number; hasSteward: boolean }

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const allDomains = ref<DomainOption[]>([])
const domainExpertOptions = ref<ExpertDetail[]>([])
const selectedParentIds = ref<number[]>([])
const selectedSubdomainIdsByParent = reactive<Record<number, number[]>>({})
const testMode = ref(false)

// ====== mock 选项数据（后续对接真实字典服务） ======
const mockEmployeeOptions = [
  { id: 'EMP001', name: '张三' },
  { id: 'EMP002', name: '李四' },
  { id: 'EMP003', name: '王五' },
  { id: 'EMP004', name: '赵六' },
  { id: 'EMP005', name: '钱七' },
]
const mockDeptOptions = [
  'BG1-BU1', 'BG1-BU2', 'BG1-BU3',
  'BG2-BU1', 'BG2-BU2',
  'BG3-BU1', 'BG3-BU2', 'BG3-BU3',
]
const mockProjectNameOptions = ['XR-光学模组', 'XR-结构件', 'TWS-降噪', '智能穿戴-心率', '车载-ARHUD']
const mockCustomerCodeOptions = ['CUS-001', 'CUS-002', 'CUS-003', 'CUS-004', 'CUS-005']
const mockProductLineOptions = ['XR产品线', 'TWS产品线', '智能穿戴产品线', '车载产品线', '音频产品线']
const mockProjectLevelOptions = ['公司级', '部门级', 'S级', 'A级', 'B级']
const mockProjectStageOptions = ['P1', 'EVT', 'DVT', 'PVT', 'MP']

// 活动地点三级：区域 → 厂区 → 楼栋
const mockLocationTree: Record<string, Record<string, string[]>> = {
  '潍坊': {
    '光电园一期': ['A栋', 'B栋', 'C栋'],
    '光电园二期': ['D栋', 'E栋'],
    '综合保税区': ['1号厂房', '2号厂房'],
  },
  '青岛': {
    '崂山研发中心': ['A座', 'B座'],
    '黄岛厂区': ['1栋', '2栋', '3栋'],
  },
  '深圳': {
    '南山研发中心': ['A栋', 'B栋'],
  },
  '越南': {
    '北宁厂区': ['A1栋', 'A2栋', 'B1栋'],
  },
}
const mockLocationRegions = Object.keys(mockLocationTree)
const mockLocationZones = computed(() => {
  const region = extraForm.locationRegion
  if (!region) return []
  return Object.keys(mockLocationTree[region] || {})
})
const mockLocationBuildings = computed(() => {
  const region = extraForm.locationRegion
  const zone = extraForm.locationZone
  if (!region || !zone) return []
  return mockLocationTree[region]?.[zone] || []
})

const attachmentPaths = ref<string[]>([])

const activityTimeRange = ref<[string, string] | null>(null)

watch(activityTimeRange, (val) => {
  if (val) {
    form.startAt = val[0]
    form.endAt = val[1]
  } else {
    form.startAt = ''
    form.endAt = null
  }
})

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
  projectName: [] as string[],
  projectLevel: '',
  customerCode: [] as string[],
  productLine: [] as string[],
  currentStage: '',
  isKdw: '否',
  isIterative: '否',
  activityName: '',
  activityLocation: '',
  locationRegion: '',
  locationZone: '',
  locationBuilding: '',
  contributionScope: '',
  activityInfo: '',
  resultSummary: '',
  expertValue: '',
  requiredCount: 2,
  techTags: '',
})

const applyCategoryOptions = APPLY_CATEGORY_OPTIONS
const isSelfPick = computed(() => extraForm.applyCategory === '积分自提')
const pointsCategoryOptions = computed(() => POINTS_CATEGORY_BY_APPLY_CATEGORY[extraForm.applyCategory] || [])
const pointsItemOptions = computed(() => POINTS_ITEM_BY_CATEGORY[extraForm.pointsCategory] || [])
const contributionScopeOptions = computed(() => CONTRIBUTION_SCOPE_BY_ITEM[extraForm.pointsItem] || [])
const showContributionScope = computed(() => contributionScopeOptions.value.length > 0)
const showProjectInfoFields = computed(() =>
  shouldShowProjectInfoFields(extraForm.pointsCategory, extraForm.pointsItem)
)
const showResultSummary = computed(() => !RESULT_SUMMARY_HIDDEN_ITEMS.has(extraForm.pointsItem))
const activityNamePlaceholder = computed(
  () => ACTIVITY_NAME_PLACEHOLDER_BY_APPLY_TYPE[extraForm.applyCategory] || '请输入活动名称'
)
const activityInfoPlaceholder = computed(
  () => ACTIVITY_INFO_PLACEHOLDER_BY_ITEM[extraForm.pointsItem] || '请填写活动背景、目标、过程与结果'
)
const activityLocationRequired = computed(() => ACTIVITY_LOCATION_REQUIRED_ITEMS.has(extraForm.pointsItem))
const resultSummaryPlaceholder = computed(
  () => RESULT_SUMMARY_PLACEHOLDER_BY_ITEM[extraForm.pointsItem] || '请填写成果提交简述'
)

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

function onLocationRegionChange() {
  extraForm.locationZone = ''
  extraForm.locationBuilding = ''
}

/** 三级地点同步到 activityLocation 字符串 */
watch(
  () => [extraForm.locationRegion, extraForm.locationZone, extraForm.locationBuilding],
  () => {
    const parts = [extraForm.locationRegion, extraForm.locationZone, extraForm.locationBuilding].filter(Boolean)
    extraForm.activityLocation = parts.join(' / ')
  }
)

function removeAttachment(i: number) {
  attachmentPaths.value.splice(i, 1)
}

async function onAttachmentUpload(opt: UploadRequestOptions) {
  try {
    const file = opt.file as File
    const res = await EngagementRequestService.uploadFile(file)
    attachmentPaths.value.push(res.path)
    opt.onSuccess?.({} as never)
    ElMessage.success('附件已上传')
  } catch (e: unknown) {
    opt.onError?.(e as never)
    ElMessage.error((e as Error)?.message || '上传失败')
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

watch(
  () => extraForm.applyCategory,
  (next) => {
    const categories = POINTS_CATEGORY_BY_APPLY_CATEGORY[next] || []
    if (!categories.includes(extraForm.pointsCategory)) {
      extraForm.pointsCategory = categories[0] || ''
    }
  },
  { immediate: true }
)

watch(
  () => extraForm.pointsCategory,
  (next) => {
    const items = POINTS_ITEM_BY_CATEGORY[next] || []
    if (!items.includes(extraForm.pointsItem)) {
      extraForm.pointsItem = items[0] || ''
    }
  },
  { immediate: true }
)

watch(
  () => extraForm.pointsItem,
  (next) => {
    const scopes = CONTRIBUTION_SCOPE_BY_ITEM[next] || []
    if (!scopes.includes(extraForm.contributionScope)) {
      extraForm.contributionScope = scopes[0] || ''
    }
  },
  { immediate: true }
)

/** 根据积分大类自动推导 taskType */
const TASK_TYPE_BY_CATEGORY: Record<string, EngagementTaskType> = {
  评估评审: 'REVIEW',
  问题解决: 'PROBLEM_SOLVING',
  成果贡献: 'KNOWLEDGE_MANAGEMENT',
  知识沉淀: 'KNOWLEDGE_MANAGEMENT',
  团队成长: 'KNOWLEDGE_MANAGEMENT',
}
watch(
  () => extraForm.pointsCategory,
  (next) => {
    const mapped = TASK_TYPE_BY_CATEGORY[next]
    if (mapped) form.taskType = mapped
  },
  { immediate: true }
)

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
  extraForm.projectName = ['多选/输入示例项目']
  extraForm.projectLevel = 'A'
  extraForm.customerCode = ['CUS-001']
  extraForm.productLine = ['XR产品线']
  extraForm.currentStage = 'EVT'
  extraForm.isKdw = '否'
  extraForm.isIterative = '是'
  extraForm.activityName = '结构件技术评审（测试）'
  extraForm.locationRegion = '潍坊'
  extraForm.locationZone = '光电园二期'
  extraForm.locationBuilding = 'D栋'
  extraForm.activityLocation = '潍坊 / 光电园二期 / D栋'
  extraForm.contributionScope = '跨BG'
  extraForm.activityInfo = '问题/不良现象：结构干涉。\n已尝试方案：调整装配间隙。\n无法突破点：强度与间隙冲突。'
  extraForm.resultSummary = '1) 评审建议输出；2) 风险清单；3) 后续验证项。'
  extraForm.expertValue = '提供结构强度与可制造性评估。'
  extraForm.requiredCount = 2
}

function buildTaskDescription(): string {
  const lines = [
    `申请类别：${extraForm.applyCategory}`,
    `积分大类：${extraForm.pointsCategory || '—'}`,
    `积分项目：${extraForm.pointsItem || '—'}`,
    `需求人：${extraForm.requester || '—'} / ${extraForm.requestDept || '—'} / ${extraForm.requestPosition || '—'}`,
    `联系方式：${extraForm.contact || '—'}`,
    `项目部门：${extraForm.projectDept || '—'}`,
    `项目名称：${extraForm.projectName.length ? extraForm.projectName.join('、') : '—'}`,
    `项目级别：${extraForm.projectLevel || '—'}`,
    `客户代码：${extraForm.customerCode.length ? extraForm.customerCode.join('、') : '—'}`,
    `产品线：${extraForm.productLine.length ? extraForm.productLine.join('、') : '—'}`,
    `当前阶段：${extraForm.currentStage || '—'}`,
    `是否KDW：${extraForm.isKdw || '—'}`,
    `是否迭代产品：${extraForm.isIterative || '—'}`,
    `活动名称：${extraForm.activityName || '—'}`,
    `活动地点：${extraForm.activityLocation || '—'}`,
    `贡献范围：${extraForm.contributionScope || '—'}`,
    `活动需求信息：${extraForm.activityInfo || '—'}`,
    `成果提交简述：${extraForm.resultSummary || '—'}`,
    `专家价值：${extraForm.expertValue || '—'}`,
    `需求人数：${extraForm.requiredCount}`,
    `附件：${attachmentPaths.value.join('; ') || '—'}`,
    '',
    `补充描述：${form.taskDescription || '—'}`,
  ]
  return lines.join('\n')
}

function validateRequiredFields(): string | null {
  if (!extraForm.applyCategory) return '请先选择申请类别'
  if (!extraForm.pointsCategory) return '请先选择积分大类'
  if (!extraForm.pointsItem) return '请先选择积分项目'
  if (!extraForm.requester || !extraForm.requestDept || !extraForm.requestPosition || !extraForm.contact) {
    return '请完整填写需求方信息'
  }
  if (showProjectInfoFields.value) {
    const requiredProjectValues = [
      extraForm.projectDept,
      extraForm.projectName,
      extraForm.projectLevel,
      extraForm.customerCode,
      extraForm.productLine,
      extraForm.currentStage,
      extraForm.isKdw,
      extraForm.isIterative,
    ]
    if (requiredProjectValues.some((item) => !String(item || '').trim())) {
      return '当前积分大类下，项目信息为必填'
    }
  }
  if (!extraForm.activityName) {
    return '请填写活动名称'
  }
  if (activityLocationRequired.value && (!extraForm.locationRegion || !extraForm.locationZone || !extraForm.locationBuilding)) {
    return '请完整填写活动地点（区域 / 厂区 / 楼栋）'
  }
  if (!extraForm.activityInfo) {
    return '请完整填写活动需求信息'
  }
  if (showContributionScope.value && !extraForm.contributionScope) {
    return '请填写贡献范围'
  }
  if (showResultSummary.value && !extraForm.resultSummary.trim()) {
    return '请填写成果提交简述'
  }
  if (!extraForm.expertValue.trim()) {
    return '请填写专家价值'
  }
  if (!attachmentPaths.value.length) {
    return '请上传附件'
  }
  return null
}

async function onSave() {
  if (!effectiveDomainIds.value.length || !form.startAt) {
    ElMessage.warning('请填写领域与开始时间')
    return
  }
  const validationError = validateRequiredFields()
  if (validationError) {
    ElMessage.warning(validationError)
    return
  }
  saving.value = true
  try {
    const ok: number[] = []
    const failed: number[] = []
    const failureMessages: string[] = []
    const domainIds = isSelfPick.value
      ? [effectiveDomainIds.value[0]].filter(Boolean)
      : effectiveDomainIds.value
    if (!domainIds.length) {
      ElMessage.warning('请选择需求领域')
      return
    }
    for (const domainId of domainIds) {
      try {
        const created = await EngagementRequestService.createDraft({
          domainId,
          mode: isSelfPick.value ? 'SELF' : form.mode,
          applyCategory: extraForm.applyCategory || undefined,
          pointsCategory: extraForm.pointsCategory || undefined,
          pointsItem: extraForm.pointsItem || undefined,
          taskType: form.taskType,
          startAt: form.startAt,
          endAt: form.endAt || undefined,
          taskDescription: buildTaskDescription(),
          ...(!isSelfPick.value && form.designatedExpertIds.length > 0
            ? { designatedExpertIds: form.designatedExpertIds }
            : {}),
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
.location-cascade {
  display: flex;
  gap: 8px;
  width: 100%;
}
.location-cascade .el-select {
  flex: 1;
  min-width: 0;
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
