<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h1 class="title">Expert Link</h1>
      <p class="subtitle">请登录以继续</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" native-type="submit" @click="onSubmit">
          登录
        </el-button>
      </el-form>
      <p class="hint">默认管理员：<code>admin</code> / 见后端 <code>app.bootstrap-admin.password</code></p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate().catch(() => Promise.reject())
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect || '/')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef5ff 0%, #f5f7fa 100%);
}
.login-card {
  width: 400px;
  padding: 8px 8px 16px;
}
.title {
  margin: 0 0 8px;
  text-align: center;
  font-size: 22px;
  color: #303133;
}
.subtitle {
  margin: 0 0 24px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
.submit {
  width: 100%;
  margin-top: 8px;
}
.hint {
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
