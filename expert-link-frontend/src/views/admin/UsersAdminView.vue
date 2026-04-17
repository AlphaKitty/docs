<template>
  <div class="page">
    <div class="page-header">
      <h2>用户与角色</h2>
      <div>
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" @click="openCreate">新建用户</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="fullName" label="姓名" width="120" />
      <el-table-column label="角色" min-width="200">
        <template #default="{ row }">
          <el-tag v-for="r in normalizeRoles(row)" :key="r" size="small" class="role-tag">{{ roleLabel(r) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isActive" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'" size="small">{{ row.isActive ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openRoles(row)">角色</el-button>
          <el-button type="danger" link size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>

    <el-dialog v-model="createVisible" title="新建用户" width="480px" @closed="resetCreate">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="88px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" autocomplete="off" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" autocomplete="off" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="姓名" prop="fullName">
          <el-input v-model="createForm.fullName" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="createForm.roles" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rolesVisible" title="调整角色" width="480px">
      <p class="muted">用户：{{ rolesTarget?.username }}</p>
      <el-select v-model="rolesEdit" multiple placeholder="选择角色" style="width: 100%">
        <el-option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <template #footer>
        <el-button @click="rolesVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserAdminService, type AdminUserRow } from '@/api/services/user-admin.service'
import { USER_ROLE_OPTIONS, roleLabel } from '@/constants/user-roles'

const loading = ref(false)
const rows = ref<AdminUserRow[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const createVisible = ref(false)
const createFormRef = ref<FormInstance>()
const submitting = ref(false)
const createForm = reactive({
  username: '',
  email: '',
  password: '',
  fullName: '',
  roles: [] as string[],
})

const createRules: FormRules = {
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  email: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [
    { required: true, message: '必填', trigger: 'blur' },
    { min: 8, message: '至少 8 位', trigger: 'blur' },
  ],
  roles: [{ type: 'array', required: true, min: 1, message: '至少选一个角色', trigger: 'change' }],
}

const rolesVisible = ref(false)
const rolesTarget = ref<AdminUserRow | null>(null)
const rolesEdit = ref<string[]>([])

const normalizeRoles = (row: AdminUserRow) => {
  const raw = row.roles as unknown
  if (Array.isArray(raw)) return raw as string[]
  if (raw && typeof raw === 'object') return Object.values(raw) as string[]
  return [] as string[]
}

const load = async () => {
  loading.value = true
  try {
    const res = await UserAdminService.getUsers(page.value - 1, size.value)
    rows.value = res.content
    total.value = res.totalElements
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetCreate()
  createVisible.value = true
}

const resetCreate = () => {
  createForm.username = ''
  createForm.email = ''
  createForm.password = ''
  createForm.fullName = ''
  createForm.roles = ['REGULAR_USER']
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate().catch(() => Promise.reject())
  submitting.value = true
  try {
    await UserAdminService.createUser({
      username: createForm.username.trim(),
      email: createForm.email.trim(),
      password: createForm.password,
      fullName: createForm.fullName.trim() || undefined,
      roles: createForm.roles,
    })
    ElMessage.success('创建成功')
    createVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const openRoles = (row: AdminUserRow) => {
  rolesTarget.value = row
  rolesEdit.value = [...normalizeRoles(row)]
  rolesVisible.value = true
}

const submitRoles = async () => {
  if (!rolesTarget.value) return
  if (!rolesEdit.value.length) {
    ElMessage.warning('至少保留一个角色')
    return
  }
  submitting.value = true
  try {
    await UserAdminService.replaceRoles(rolesTarget.value.id, rolesEdit.value)
    ElMessage.success('已保存')
    rolesVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const remove = async (row: AdminUserRow) => {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '确认', { type: 'warning' })
  } catch {
    return
  }
  submitting.value = true
  try {
    await UserAdminService.deleteUser(row.id)
    ElMessage.success('已删除')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  } finally {
    submitting.value = false
  }
}

void load()
</script>

<style scoped>
.page {
  padding: 8px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.role-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}
.muted {
  color: #909399;
  font-size: 13px;
  margin-top: 0;
}
</style>
