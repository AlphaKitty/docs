<template>
  <div class="add-skill">
    <div class="page-header">
      <h2>添加技能</h2>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

interface SkillForm {
  name: string
  category: string
  description: string
  demandLevel: number
  tags: string[]
  relatedSkills: number[]
  documentation: string
  enabled: boolean
}

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<SkillForm>({
  name: '',
  category: '',
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

const availableSkills = ref([
  { id: 1, name: '机器学习' },
  { id: 2, name: '深度学习' },
  { id: 3, name: 'Python编程' },
  { id: 4, name: 'Java开发' },
  { id: 5, name: 'Vue.js开发' },
  { id: 6, name: 'React开发' },
  { id: 7, name: 'Node.js开发' },
  { id: 8, name: 'Spring Boot开发' },
  { id: 9, name: 'MySQL数据库' },
  { id: 10, name: 'Redis缓存' },
  { id: 11, name: 'Docker容器化' },
  { id: 12, name: 'Kubernetes编排' },
  { id: 13, name: 'AWS云服务' },
  { id: 14, name: 'UI界面设计' },
  { id: 15, name: 'UX用户体验' },
  { id: 16, name: '产品需求分析' },
  { id: 17, name: '项目管理' },
  { id: 18, name: '数据分析' },
  { id: 19, name: '数据可视化' },
  { id: 20, name: '市场营销策略' }
])

const goBack = () => {
  router.back()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('技能添加成功')
    
    // 返回技能列表页
    router.push('/skills')
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 页面加载时的初始化操作
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