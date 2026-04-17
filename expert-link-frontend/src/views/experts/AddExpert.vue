<template>
  <div class="add-expert">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑专家' : '新建专家档案' }}</h2>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <!-- 新建：先维护称谓，再批量将用户入库为专家 -->
    <div v-if="!isEditMode" class="form-container">
      <el-alert type="info" :closable="false" show-icon class="mb">
        新增称谓/岗位只维护称谓本身，不绑定用户。请先选择称谓，再批量选择系统用户入库为专家。
      </el-alert>

      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="120px" label-position="top">
        <el-form-item label="选择用户（可多选）" prop="ownerIds" v-if="canPickUsers">
          <el-select
            v-model="createForm.ownerIds"
            multiple
            filterable
            remote
            clearable
            reserve-keyword
            placeholder="输入姓名、用户名或邮箱搜索"
            :remote-method="remoteSearchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="u in pickerOptions"
              :key="u.id"
              :label="`${u.fullName || u.username} (${u.email})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          v-else
          type="warning"
          :closable="false"
          show-icon
          class="mb"
          title="仅超级管理员/部门管理员可批量入库专家"
        />

        <el-form-item label="称谓/岗位" prop="designationId">
          <div class="designation-row">
            <el-select
              v-model="createForm.designationId"
              placeholder="请选择称谓"
              filterable
              style="flex: 1; min-width: 0"
            >
              <el-option v-for="d in designations" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
            <el-button v-if="canManageDesignations" type="primary" @click="openDesigDialog">新增称谓</el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitCreate">批量入库专家</el-button>
        </el-form-item>
      </el-form>

      <el-dialog v-model="desigDialogVisible" title="新增称谓/岗位" width="480px" destroy-on-close>
        <el-form :model="desigForm" label-position="top">
          <el-form-item label="名称" required>
            <el-input v-model="desigForm.name" maxlength="100" show-word-limit placeholder="如：首席专家、外聘顾问" />
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="desigForm.description" type="textarea" :rows="2" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="desigDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="desigSaving" @click="submitNewDesignation">确定</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- 编辑：沿用表单（与接口字段映射在提交时处理） -->
    <div v-else class="form-container">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="专家姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位" prop="title">
              <el-input v-model="form.title" placeholder="职位/称谓展示" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公司" prop="company">
              <el-input v-model="form.company" placeholder="公司或部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作经验" prop="experience">
              <el-input-number v-model="form.experience" :min="0" :max="50" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="技能" prop="skills">
          <el-select v-model="form.skills" multiple filterable allow-create default-first-option placeholder="技能" style="width: 100%">
            <el-option v-for="skill in skillOptions" :key="skill" :label="skill" :value="skill" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="状态" style="width: 100%">
                <el-option label="可用" value="available" />
                <el-option label="忙碌" value="busy" />
                <el-option label="不可用" value="unavailable" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评分" prop="rating">
              <el-rate v-model="form.rating" show-score />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useExpertStore } from '@/stores/expert'
import { useAuthStore } from '@/stores/auth'
import { ExpertService } from '@/api/services/expert.service'
import { ExpertDesignationService } from '@/api/services/expert-designation.service'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { UserPickerItem } from '@/api/types'
import type { ExpertDesignation } from '@/api/types/expert-designation'

const router = useRouter()
const route = useRoute()
const expertStore = useExpertStore()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const createFormRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const userSearchLoading = ref(false)
const userOptions = ref<UserPickerItem[]>([])
const designations = ref<ExpertDesignation[]>([])
const desigDialogVisible = ref(false)
const desigSaving = ref(false)

const desigForm = reactive({ name: '', description: '' })

const expertId = computed(() => Number(route.params.id))
const isEditMode = computed(() => route.name === 'ExpertEdit' && Number.isFinite(expertId.value))

const canPickUsers = computed(
  () => auth.roles.includes('SUPER_ADMIN') || auth.roles.includes('DEPT_ADMIN')
)
const canManageDesignations = computed(() => canPickUsers.value)

const createForm = reactive({
  ownerIds: [] as number[],
  designationId: undefined as number | undefined,
})

const createRules = computed<FormRules>(() => ({
  ...(canPickUsers.value
    ? { ownerIds: [{ required: true, message: '请至少选择一个用户', trigger: 'change' }] as const }
    : {}),
  designationId: [{ required: true, message: '请选择称谓/岗位', trigger: 'change' }],
}))

const pickerOptions = computed(() => userOptions.value)

const form = reactive({
  name: '',
  title: '',
  company: '',
  email: '',
  phone: '',
  skills: [] as string[],
  experience: 0,
  status: 'available' as 'available' | 'busy' | 'unavailable',
  rating: 5,
  avatar: '',
})

const skillOptions = [
  '机器学习',
  '深度学习',
  'Python',
  '数据分析',
  'Vue.js',
  'React',
  'TypeScript',
  'Java',
  'Spring Boot',
]

const rules: FormRules = {
  name: [{ required: true, message: '请输入专家姓名', trigger: 'blur' }],
  title: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  company: [{ required: true, message: '请输入公司', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  skills: [{ required: true, message: '请至少选择一个技能', trigger: 'change' }],
}

async function loadDesignations() {
  try {
    designations.value = await ExpertDesignationService.list()
  } catch {
    designations.value = []
  }
}

async function remoteSearchUsers(query: string) {
  if (!canPickUsers.value) return
  userSearchLoading.value = true
  try {
    const page = await ExpertService.getUserCandidates(query || '', 0, 30)
    userOptions.value = page.content
  } catch {
    userOptions.value = []
  } finally {
    userSearchLoading.value = false
  }
}

function openDesigDialog() {
  desigForm.name = ''
  desigForm.description = ''
  desigDialogVisible.value = true
}

async function submitNewDesignation() {
  if (!desigForm.name.trim()) {
    ElMessage.warning('请输入称谓名称')
    return
  }
  desigSaving.value = true
  try {
    const d = await ExpertDesignationService.create({
      name: desigForm.name.trim(),
      description: desigForm.description.trim() || undefined,
    })
    await loadDesignations()
    createForm.designationId = d.id
    desigDialogVisible.value = false
    ElMessage.success('已新增称谓')
  } catch (e: any) {
    ElMessage.error(e?.message || '新增失败')
  } finally {
    desigSaving.value = false
  }
}

async function submitCreate() {
  if (!createFormRef.value) return
  if (!canPickUsers.value) {
    ElMessage.error('仅管理员可批量添加专家')
    return
  }
  if (!createForm.designationId) {
    ElMessage.warning('请选择称谓/岗位')
    return
  }
  if (!createForm.ownerIds.length) {
    ElMessage.warning('请至少选择一个用户')
    return
  }
  try {
    await createFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const result = await ExpertDesignationService.batchAddExperts(createForm.designationId, {
      ownerIds: createForm.ownerIds,
    })
    ElMessage.success(
      `批量入库完成：请求 ${result.requestedCount}，成功 ${result.createdCount}，跳过 ${result.skippedCount}`
    )
    await router.push('/experts')
  } catch (e: any) {
    ElMessage.error(e?.message || '批量入库失败')
  } finally {
    submitting.value = false
  }
}

const fillForm = (detail: Record<string, any>) => {
  form.name = detail.name || ''
  form.title = detail.title || detail.currentPosition || ''
  form.company = detail.company || detail.currentCompany || ''
  form.email = detail.email || ''
  form.phone = detail.phone || detail.phoneNumber || ''
  form.skills = Array.isArray(detail.skills)
    ? detail.skills.map((skill: any) => skill.skillName || skill.name).filter(Boolean)
    : []
  form.experience = Number(detail.experienceYears ?? detail.yearsOfExperience ?? 0)
  form.status =
    detail.availability === false || detail.availabilityStatus === 'UNAVAILABLE'
      ? 'unavailable'
      : detail.status === 'PENDING'
        ? 'busy'
        : 'available'
  form.rating = Number(detail.rating ?? detail.overallRating ?? 5)
  form.avatar = detail.avatar || ''
}

const loadExistingExpert = async () => {
  if (!isEditMode.value) return
  loading.value = true
  try {
    const detail = await ExpertService.getExpertById(expertId.value)
    fillForm(detail as Record<string, any>)
  } catch (error: any) {
    ElMessage.error(error?.message || '加载专家详情失败')
  } finally {
    loading.value = false
  }
}

const submitEdit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    await expertStore.updateExpert(expertId.value, { ...form })
    ElMessage.success('专家更新成功')
    await router.push(`/experts/${expertId.value}`)
  } catch {
    ElMessage.error('请检查表单')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.skills = []
  form.experience = 0
  form.rating = 5
  form.status = 'available'
}

const goBack = () => {
  void router.push('/experts')
}

onMounted(async () => {
  if (!isEditMode.value) {
    await loadDesignations()
    if (canPickUsers.value) {
      void remoteSearchUsers('')
    }
  } else {
    await loadExistingExpert()
  }
})
</script>

<style scoped>
.add-expert {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.form-container {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.mb {
  margin-bottom: 16px;
}

.designation-row {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}
</style>
