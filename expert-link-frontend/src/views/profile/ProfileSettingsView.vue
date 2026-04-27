<template>
  <div class="profile-settings">
    <div class="page-header">
      <h2>个人信息配置</h2>
      <p>支持按分组查看字段，灰色字段为只读，手机号/个人简介/头像可编辑。</p>
    </div>

    <el-card class="section-card">
      <template #header>
        <h3>账户信息（只读）</h3>
      </template>
      <el-form label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input :model-value="profile.username" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input :model-value="profile.fullName || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input :model-value="profile.email" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="积分余额">
              <el-input :model-value="pointsText" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="角色">
              <div class="roles-wrap">
                <el-tag v-for="role in profile.roles" :key="role" type="info" effect="plain">
                  {{ roleLabel(role) }}
                </el-tag>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card v-if="isExpert" class="section-card">
      <template #header>
        <h3>专家专属信息</h3>
      </template>
      <el-form :model="expertForm" :rules="expertRules" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="专家ID">
              <el-input :model-value="profile.expertProfile?.expertId || '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="专家姓名">
              <el-input :model-value="profile.expertProfile?.name || profile.fullName || profile.username" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前职位">
              <el-input v-model.trim="expertForm.currentPosition" placeholder="请输入当前职位" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前公司">
              <el-input v-model.trim="expertForm.currentCompany" placeholder="请输入当前公司" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="微信号">
              <el-input v-model.trim="expertForm.wechatId" placeholder="请输入微信号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="从业年限">
              <el-input-number v-model="expertForm.yearsOfExperience" :min="0" :max="80" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="小时费率">
              <el-input-number v-model="expertForm.hourlyRate" :min="0" :precision="2" :step="50" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="可用状态">
              <el-select v-model="expertForm.availabilityStatus" style="width: 100%">
                <el-option label="可用" value="AVAILABLE" />
                <el-option label="不可用" value="UNAVAILABLE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="专家简介" prop="biography">
              <el-input
                v-model.trim="expertForm.biography"
                type="textarea"
                :rows="4"
                maxlength="4000"
                show-word-limit
                placeholder="请输入专家简介"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <h3>可编辑信息</h3>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model.trim="form.phoneNumber" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model.trim="form.bio"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            placeholder="请输入个人简介"
          />
        </el-form-item>

        <el-form-item label="头像地址" prop="avatar">
          <el-input v-model.trim="form.avatar" placeholder="请输入头像 URL 地址" clearable />
        </el-form-item>

        <el-form-item v-if="form.avatar" label="头像预览">
          <el-avatar :size="64" :src="form.avatar">{{ displayInitial }}</el-avatar>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :disabled="!hasChanges" :loading="saving" @click="onSave">
            保存修改
          </el-button>
          <el-button :disabled="!hasChanges || saving" @click="resetEditableFields">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { AuthService } from '@/api/services/auth.service'
import type { UserProfile } from '@/api/types'
import { roleLabel } from '@/constants/user-roles'

type EditableProfileForm = {
  phoneNumber: string
  bio: string
  avatar: string
}
type EditableExpertForm = {
  currentPosition: string
  currentCompany: string
  wechatId: string
  yearsOfExperience: number | undefined
  hourlyRate: number | undefined
  availabilityStatus: 'AVAILABLE' | 'UNAVAILABLE'
  biography: string
}

const formRef = ref<FormInstance>()
const saving = ref(false)

const profile = reactive<UserProfile>({
  userId: 0,
  username: '',
  email: '',
  fullName: '',
  roles: [],
  pointsBalance: 0,
  phoneNumber: '',
  bio: '',
  avatar: '',
})

const form = reactive<EditableProfileForm>({
  phoneNumber: '',
  bio: '',
  avatar: '',
})

const baseline = reactive<EditableProfileForm>({
  phoneNumber: '',
  bio: '',
  avatar: '',
})
const expertForm = reactive<EditableExpertForm>({
  currentPosition: '',
  currentCompany: '',
  wechatId: '',
  yearsOfExperience: 0,
  hourlyRate: 0,
  availabilityStatus: 'AVAILABLE',
  biography: '',
})
const expertBaseline = reactive<EditableExpertForm>({
  currentPosition: '',
  currentCompany: '',
  wechatId: '',
  yearsOfExperience: 0,
  hourlyRate: 0,
  availabilityStatus: 'AVAILABLE',
  biography: '',
})

const rules: FormRules<EditableProfileForm> = {
  phoneNumber: [
    {
      pattern: /^$|^[+]?[\d\-() ]{6,20}$/,
      message: '手机号格式不正确',
      trigger: 'blur',
    },
  ],
  bio: [{ max: 2000, message: '个人简介最多 2000 个字符', trigger: 'blur' }],
  avatar: [
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback()
          return
        }
        try {
          const url = new URL(value)
          if (url.protocol !== 'http:' && url.protocol !== 'https:') {
            callback(new Error('头像地址需为 http/https'))
            return
          }
          callback()
        } catch {
          callback(new Error('请输入合法的 URL'))
        }
      },
      trigger: 'blur',
    },
  ],
}
const expertRules: FormRules<EditableExpertForm> = {
  biography: [{ max: 4000, message: '专家简介最多 4000 个字符', trigger: 'blur' }],
}

const pointsText = computed(() => Number(profile.pointsBalance || 0).toFixed(2))
const isExpert = computed(
  () =>
    profile.roles.includes('EXPERT_USER') ||
    profile.roles.includes('SUPER_ADMIN') ||
    Boolean(profile.expertProfile)
)
const hasChanges = computed(
  () =>
    form.phoneNumber !== baseline.phoneNumber ||
    form.bio !== baseline.bio ||
    form.avatar !== baseline.avatar ||
    (isExpert.value &&
      (expertForm.currentPosition !== expertBaseline.currentPosition ||
        expertForm.currentCompany !== expertBaseline.currentCompany ||
        expertForm.wechatId !== expertBaseline.wechatId ||
        expertForm.yearsOfExperience !== expertBaseline.yearsOfExperience ||
        expertForm.hourlyRate !== expertBaseline.hourlyRate ||
        expertForm.availabilityStatus !== expertBaseline.availabilityStatus ||
        expertForm.biography !== expertBaseline.biography))
)
const displayInitial = computed(() => (profile.username || '?').slice(0, 1).toUpperCase())

const applyProfile = (data: UserProfile) => {
  profile.userId = data.userId
  profile.username = data.username
  profile.fullName = data.fullName || ''
  profile.email = data.email
  profile.roles = [...(data.roles || [])]
  profile.pointsBalance = data.pointsBalance ?? 0
  profile.phoneNumber = data.phoneNumber || ''
  profile.bio = data.bio || ''
  profile.avatar = data.avatar || ''
  profile.expertProfile = data.expertProfile || null

  baseline.phoneNumber = profile.phoneNumber || ''
  baseline.bio = profile.bio || ''
  baseline.avatar = profile.avatar || ''

  form.phoneNumber = baseline.phoneNumber
  form.bio = baseline.bio
  form.avatar = baseline.avatar

  expertBaseline.currentPosition = data.expertProfile?.currentPosition || ''
  expertBaseline.currentCompany = data.expertProfile?.currentCompany || ''
  expertBaseline.wechatId = data.expertProfile?.wechatId || ''
  expertBaseline.yearsOfExperience = data.expertProfile?.yearsOfExperience ?? 0
  expertBaseline.hourlyRate = data.expertProfile?.hourlyRate ?? 0
  expertBaseline.availabilityStatus = data.expertProfile?.availabilityStatus === 'UNAVAILABLE' ? 'UNAVAILABLE' : 'AVAILABLE'
  expertBaseline.biography = data.expertProfile?.biography || ''

  expertForm.currentPosition = expertBaseline.currentPosition
  expertForm.currentCompany = expertBaseline.currentCompany
  expertForm.wechatId = expertBaseline.wechatId
  expertForm.yearsOfExperience = expertBaseline.yearsOfExperience
  expertForm.hourlyRate = expertBaseline.hourlyRate
  expertForm.availabilityStatus = expertBaseline.availabilityStatus
  expertForm.biography = expertBaseline.biography
}

const resetEditableFields = () => {
  form.phoneNumber = baseline.phoneNumber
  form.bio = baseline.bio
  form.avatar = baseline.avatar
  expertForm.currentPosition = expertBaseline.currentPosition
  expertForm.currentCompany = expertBaseline.currentCompany
  expertForm.wechatId = expertBaseline.wechatId
  expertForm.yearsOfExperience = expertBaseline.yearsOfExperience
  expertForm.hourlyRate = expertBaseline.hourlyRate
  expertForm.availabilityStatus = expertBaseline.availabilityStatus
  expertForm.biography = expertBaseline.biography
  formRef.value?.clearValidate()
}

const onSave = async () => {
  if (!formRef.value || !hasChanges.value) return
  await formRef.value.validate().catch(() => Promise.reject())
  saving.value = true
  try {
    const updated = await AuthService.updateMyProfile({
      phoneNumber: form.phoneNumber || '',
      bio: form.bio || '',
      avatar: form.avatar || '',
      expertProfile: isExpert.value
        ? {
            currentPosition: expertForm.currentPosition || '',
            currentCompany: expertForm.currentCompany || '',
            wechatId: expertForm.wechatId || '',
            yearsOfExperience: Number(expertForm.yearsOfExperience || 0),
            hourlyRate: Number(expertForm.hourlyRate || 0),
            availabilityStatus: expertForm.availabilityStatus,
            biography: expertForm.biography || '',
          }
        : undefined,
    })
    applyProfile(updated)
    ElMessage.success('个人信息已更新')
  } catch (e: any) {
    ElMessage.error(e?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

const load = async () => {
  try {
    const data = await AuthService.getMyProfile()
    applyProfile(data)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载个人信息失败')
  }
}

void load()
</script>

<style scoped>
.profile-settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0 0 6px;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.section-card {
  margin-bottom: 16px;
}

.section-card h3 {
  margin: 0;
  font-size: 16px;
}

.roles-wrap {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
