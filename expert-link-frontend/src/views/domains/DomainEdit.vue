<template>
  <div v-loading="loading" class="domain-edit">
    <div class="page-header">
      <h2>{{ isEditMode ? '编辑领域' : '新增领域' }}</h2>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEditMode ? '保存' : '创建' }}</el-button>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      :title="loadError"
      type="error"
      show-icon
      :closable="false"
      class="page-alert"
    />

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
          <el-col :xs="24" :md="12">
            <el-form-item label="领域名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入领域名称" clearable />
            </el-form-item>
          </el-col>

          <el-col :xs="24" :md="12">
            <el-form-item label="父领域" prop="parentId">
              <el-select
                v-model="form.parentId"
                placeholder="请选择父领域"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="option in parentOptions"
                  :key="option.id"
                  :label="option.name"
                  :value="option.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="领域描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入领域描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="是否启用" prop="isActive">
          <el-switch v-model="form.isActive" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { DomainService } from '@/api/services'

interface DomainForm {
  name: string
  description: string
  parentId?: number
  isActive: boolean
}

interface DomainOption {
  id: number
  name: string
}

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const loadError = ref('')
const currentDomainId = computed(() => Number(route.params.id))
const isEditMode = computed(() => route.name === 'DomainEdit' && Number.isFinite(currentDomainId.value))
const parentOptions = ref<DomainOption[]>([])

const form = reactive<DomainForm>({
  name: '',
  description: '',
  parentId: undefined,
  isActive: true
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入领域名称', trigger: 'blur' },
    { min: 2, max: 100, message: '领域名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入领域描述', trigger: 'blur' },
    { min: 2, max: 500, message: '领域描述长度在 2 到 500 个字符', trigger: 'blur' }
  ]
}

const goBack = () => {
  router.back()
}

const loadParentOptions = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const domainPage = await DomainService.getDomains({ page: 0, size: 200 })
    parentOptions.value = domainPage.content
      .map((item: any) => ({
        id: Number(item.id),
        name: item.name || `领域 ${item.id}`
      }))
      .filter((item: DomainOption) => !isEditMode.value || item.id !== currentDomainId.value)
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载领域数据失败'
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}

const loadExistingDomain = async () => {
  if (!isEditMode.value) return

  loading.value = true
  loadError.value = ''

  try {
    const detail = await DomainService.getDomainById(currentDomainId.value)
    form.name = detail.name || ''
    form.description = detail.description || ''
    form.parentId = detail.parentId ?? undefined
    form.isActive = detail.isActive ?? true
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载领域编辑数据失败'
    ElMessage.error(loadError.value)
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
      description: form.description.trim(),
      parentId: form.parentId,
      isActive: form.isActive
    }

    if (isEditMode.value) {
      await DomainService.updateDomain(currentDomainId.value, {
        id: currentDomainId.value,
        ...payload
      } as any)
      ElMessage.success('领域更新成功')
      router.push(`/domains/${currentDomainId.value}`)
    } else {
      const created = await DomainService.createDomain(payload as any)
      ElMessage.success('领域创建成功')
      router.push(`/domains/${created.id}`)
    }
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadParentOptions()
  void loadExistingDomain()
})
</script>

<style scoped>
.domain-edit {
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
}

.header-actions {
  display: flex;
  gap: 10px;
}

.page-alert {
  margin-bottom: 20px;
}

.form-card {
  margin-top: 20px;
}
</style>
