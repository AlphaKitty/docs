<template>
  <div class="add-expert">
    <div class="page-header">
      <h2>添加专家</h2>
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
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入专家姓名" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="职位" prop="title">
              <el-input v-model="form.title" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公司" prop="company">
              <el-input v-model="form.company" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入电话号码" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="工作经验" prop="experience">
              <el-input-number
                v-model="form.experience"
                :min="0"
                :max="50"
                placeholder="请输入工作经验年限"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="技能" prop="skills">
          <el-select
            v-model="form.skills"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入技能"
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
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
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
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useExpertStore } from '@/stores/expert'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const router = useRouter()
const expertStore = useExpertStore()
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  title: '',
  company: '',
  email: '',
  phone: '',
  skills: [] as string[],
  experience: 0,
  status: 'available',
  rating: 5,
  avatar: ''
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

const rules: FormRules = {
  name: [
    { required: true, message: '请输入专家姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  title: [
    { required: true, message: '请输入职位', trigger: 'blur' }
  ],
  company: [
    { required: true, message: '请输入公司名称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  skills: [
    { required: true, message: '请至少选择一个技能', trigger: 'change' }
  ]
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 添加专家到store
    expertStore.addExpert(form)
    
    ElMessage.success('专家添加成功')
    
    // 重置表单
    resetForm()
    
    // 返回列表页
    setTimeout(() => {
      router.push('/experts')
    }, 1000)
  } catch (error) {
    ElMessage.error('表单验证失败，请检查输入')
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
  router.push('/experts')
}
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
</style>