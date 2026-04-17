<template>
  <div class="add-project">
    <div class="page-header">
      <h2>添加项目</h2>
      <el-button @click="goBack">返回列表</el-button>
    </div>
    
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="项目编号" prop="code">
              <el-input v-model="form.code" placeholder="请输入项目编号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择项目类型" style="width: 100%">
                <el-option label="技术咨询" value="technical" />
                <el-option label="市场调研" value="market" />
                <el-option label="产品开发" value="product" />
                <el-option label="战略规划" value="strategy" />
                <el-option label="培训服务" value="training" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预算" prop="budget">
              <el-input-number
                v-model="form.budget"
                :min="0"
                :step="1000"
                placeholder="请输入预算金额"
                style="width: 100%"
              >
                <template #append>元</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="待启动" value="pending" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已延期" value="delayed" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="所需技能" prop="requiredSkills">
          <el-select
            v-model="form.requiredSkills"
            multiple
            filterable
            placeholder="请选择所需技能"
            style="width: 100%"
          >
            <el-option
              v-for="skill in skillOptions"
              :key="skill"
              :label="skill"
              :value="skill"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="项目负责人" prop="manager">
          <el-select
            v-model="form.manager"
            filterable
            placeholder="请选择项目负责人"
            style="width: 100%"
          >
            <el-option
              v-for="expert in expertOptions"
              :key="expert.id"
              :label="expert.name"
              :value="expert.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore, type ProjectStatus } from '@/stores/project'
import { useExpertStore } from '@/stores/expert'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const router = useRouter()
const projectStore = useProjectStore()
const expertStore = useExpertStore()
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  code: '',
  description: '',
  type: 'technical',
  priority: 'medium',
  startDate: '',
  endDate: '',
  budget: 0,
  status: 'pending' as ProjectStatus,
  requiredSkills: [] as string[],
  manager: undefined as number | undefined
})

const skillOptions = [
  '机器学习',
  '深度学习',
  'Python',
  '数据分析',
  '统计学',
  'R语言',
  'Vue.js',
  'React',
  'TypeScript',
  'Java',
  'Spring Boot',
  '数据库设计',
  '项目管理',
  '产品设计',
  '市场营销'
]

const expertOptions = ref<Array<{ id: number; name: string }>>([])

const rules: FormRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入项目编号', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  budget: [
    { required: true, message: '请输入预算金额', trigger: 'blur' }
  ],
  requiredSkills: [
    { required: true, message: '请至少选择一个所需技能', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请选择项目负责人', trigger: 'change' }
  ]
}

onMounted(async () => {
  // 加载专家列表
  await expertStore.fetchExperts()
  expertOptions.value = expertStore.experts.map((expert) => ({
    id: expert.id,
    name: expert.name
  }))
})

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 添加项目到store
    projectStore.addProject({
      ...form,
      manager: form.manager as number
    })
    
    ElMessage.success('项目添加成功')
    
    // 重置表单
    resetForm()
    
    // 返回列表页
    setTimeout(() => {
      router.push('/projects')
    }, 1000)
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入')
  }
}

const resetForm = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.requiredSkills = []
  form.budget = 0
  form.status = 'pending'
  form.type = 'technical'
  form.priority = 'medium'
  form.manager = undefined
}

const goBack = () => {
  router.push('/projects')
}
</script>

<style scoped>
.add-project {
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
</style>