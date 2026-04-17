<template>
  <div class="add-skill">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑技能' : '添加技能' }}</h2>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </div>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="技能名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入技能名称"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="技能分类" prop="category">
              <el-select
                v-model="form.category"
                placeholder="请选择技能分类"
                clearable
                style="width: 100%"
              >
                <el-option label="技术开发" value="tech" />
                <el-option label="设计创意" value="design" />
                <el-option label="产品管理" value="product" />
                <el-option label="市场营销" value="marketing" />
                <el-option label="数据分析" value="data" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="所属领域" prop="domainId">
              <el-select
                v-model="form.domainId"
                placeholder="请选择所属领域"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="domain in availableDomains"
                  :key="domain.id"
                  :label="domain.name"
                  :value="domain.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="技能描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入技能详细描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="需求等级" prop="demandLevel">
              <el-rate
                v-model="form.demandLevel"
                :max="5"
                show-text
                :texts="['低', '较低', '中等', '较高', '高']"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="关联标签" prop="tags">
              <el-select
                v-model="form.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="请输入或选择标签"
                style="width: 100%"
              >
                <el-option
                  v-for="tag in commonTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="相关技能" prop="relatedSkills">
          <el-select
            v-model="form.relatedSkills"
            multiple
            filterable
            placeholder="请选择相关技能"
            style="width: 100%"
          >
            <el-option
              v-for="skill in availableSkills"
              :key="skill.id"
              :label="skill.name"
              :value="skill.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="技能文档" prop="documentation">
          <el-input
            v-model="form.documentation"
            type="textarea"
            :rows="3"
            placeholder="请输入技能相关的文档链接或说明"
          />
        </el-form-item>

        <el-form-item label="是否启用" prop="enabled">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { DomainService, SkillService } from '@/api/services'
import type { SkillDetail } from '@/api/types'

interface SkillForm {
  name: string
  category: string
  domainId?: number
  description: string
  demandLevel: number
  tags: string[]
  relatedSkills: number[]
  documentation: string
  enabled: boolean
}

interface SkillOption {
  id: number
  name: string
}

interface DomainOption {
  id: number
  name: string
}

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(false)
const skillId = computed(() => Number(route.params.id))
const isEditMode = computed(() => route.name === 'SkillEdit' && Number.isFinite(skillId.value))

const form = reactive<SkillForm>({
  name: '',
  category: '',
  domainId: undefined,
  description: '',
  demandLevel: 3,
  tags: [],
  relatedSkills: [],
  documentation: '',
  enabled: true
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入技能名称', trigger: 'blur' },
    { min: 2, max: 50, message: '技能名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择技能分类', trigger: 'change' }
  ],
  domainId: [
    { required: true, message: '请选择所属领域', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入技能描述', trigger: 'blur' },
    { min: 10, max: 500, message: '技能描述长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

const commonTags = ref([
  '人工智能', '机器学习', '深度学习', 'Python', 'Java', 'JavaScript',
  'Vue.js', 'React', 'Node.js', 'Spring Boot', 'MySQL', 'Redis',
  'Docker', 'Kubernetes', 'AWS', 'Azure', 'UI设计', 'UX设计',
  '产品规划', '项目管理', '数据分析', '数据可视化', '市场营销',
  'SEO', '内容营销', '社交媒体'
])

const availableSkills = ref<SkillOption[]>([])
const availableDomains = ref<DomainOption[]>([])

const demandLevelMap: Record<number, string> = {
  1: 'LOW',
  2: 'LOW',
  3: 'MEDIUM',
  4: 'HIGH',
  5: 'CRITICAL'
}

const goBack = () => {
  router.back()
}

const mapDemandLevelToScore = (demandLevel?: string) => {
  switch (String(demandLevel || '').toUpperCase()) {
    case 'LOW':
      return 2
    case 'MEDIUM':
      return 3
    case 'HIGH':
      return 4
    case 'CRITICAL':
      return 5
    default:
      return 3
  }
}

const loadFormOptions = async () => {
  try {
    const [domains, skills] = await Promise.all([
      DomainService.getDomains({ page: 0, size: 200 }),
      SkillService.getSkills({ page: 0, size: 200 })
    ])

    availableDomains.value = domains.content.map((domain: any) => ({
      id: domain.id,
      name: domain.name || `领域 ${domain.id}`
    }))

    availableSkills.value = skills.content.map((skill: any) => ({
      id: skill.id,
      name: skill.name || `技能 ${skill.id}`
    })).filter((skill: SkillOption) => skill.id !== skillId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载技能表单数据失败')
  }
}

const fillForm = (skill: SkillDetail) => {
  form.name = skill.name || ''
  form.category = skill.category || ''
  form.domainId = skill.domain?.id
  form.description = skill.description || ''
  form.demandLevel = mapDemandLevelToScore(skill.demandLevel)
  form.tags = Array.isArray(skill.tags) ? skill.tags.map((tag) => tag.name) : []
  form.relatedSkills = Array.isArray(skill.relatedSkills) ? skill.relatedSkills.map((item) => item.id) : []
  form.documentation = skill.documentation?.content || skill.documentation?.url || ''
  form.enabled = skill.isActive ?? true
}

const loadExistingSkill = async () => {
  if (!isEditMode.value) return

  loading.value = true
  try {
    const skill = await SkillService.getSkillById(skillId.value)
    fillForm(skill)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载技能详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      name: form.name.trim(),
      category: form.category,
      domainId: Number(form.domainId),
      description: form.description.trim(),
      demandLevel: demandLevelMap[form.demandLevel] || 'MEDIUM',
      enabled: form.enabled,
      tags: form.tags.map((tag) => tag.trim()).filter(Boolean),
      relatedSkillIds: form.relatedSkills,
      documentation: form.documentation.trim()
        ? { content: form.documentation.trim() }
        : undefined
    }

    const savedSkill = isEditMode.value
      ? await SkillService.updateSkill(skillId.value, { ...payload, id: skillId.value })
      : await SkillService.createSkill(payload)

    ElMessage.success(isEditMode.value ? '技能更新成功' : '技能添加成功')

    router.push(`/skills/${savedSkill.id}`)
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadFormOptions()
  void loadExistingSkill()
})
</script>

<style scoped>
.add-skill {
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
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.form-card {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-rate {
  display: flex;
  align-items: center;
}
</style>