<template>
  <div class="system-settings">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>
    
    <div class="settings-container">
      <el-tabs v-model="activeTab" class="settings-tabs">
        <el-tab-pane label="基本设置" name="basic">
          <el-card class="settings-card">
            <template #header>
              <h3>系统基本信息</h3>
            </template>
            <el-form :model="basicForm" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              
              <el-form-item label="系统Logo">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :on-change="handleLogoChange"
                  :auto-upload="false"
                >
                  <img v-if="basicForm.logoUrl" :src="basicForm.logoUrl" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              
              <el-form-item label="版权信息">
                <el-input v-model="basicForm.copyright" placeholder="请输入版权信息" />
              </el-form-item>
              
              <el-form-item label="系统描述">
                <el-input
                  v-model="basicForm.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入系统描述"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
                <el-button @click="resetBasicSettings">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="用户管理" name="users">
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <h3>用户管理</h3>
                <el-button type="primary" icon="Plus" @click="showAddUserDialog">添加用户</el-button>
              </div>
            </template>
            <el-table :data="users" style="width: 100%">
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="role" label="角色" width="120">
                <template #default="{ row }">
                  <el-tag :type="getRoleType(row.role)">
                    {{ getRoleText(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="'active'"
                    :inactive-value="'inactive'"
                    @change="toggleUserStatus(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button type="primary" size="small" @click="editUser(row)">编辑</el-button>
                  <el-button type="danger" size="small" @click="deleteUser(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="权限设置" name="permissions">
          <el-card class="settings-card">
            <template #header>
              <h3>权限管理</h3>
            </template>
            <div class="permissions-container">
              <div class="roles-section">
                <h4>角色列表</h4>
                <div class="roles-list">
                  <el-tag
                    v-for="role in roles"
                    :key="role.id"
                    :type="role.id === selectedRoleId ? 'primary' : 'info'"
                    class="role-tag"
                    @click="selectRole(role.id)"
                  >
                    {{ role.name }}
                  </el-tag>
                </div>
              </div>
              
              <div class="permissions-section">
                <h4>权限配置</h4>
                <div v-if="selectedRole" class="permissions-list">
                  <div class="permission-category" v-for="category in permissionCategories" :key="category.id">
                    <h5>{{ category.name }}</h5>
                    <div class="permission-items">
                      <el-checkbox
                        v-for="permission in category.permissions"
                        :key="permission.id"
                        v-model="selectedRole.permissions[permission.id]"
                        :label="permission.name"
                      />
                    </div>
                  </div>
                </div>
                <div v-else class="no-role-selected">
                  <el-empty description="请选择一个角色进行配置" />
                </div>
              </div>
            </div>
            
            <div class="permissions-actions">
              <el-button type="primary" :disabled="!selectedRole" @click="savePermissions">保存权限</el-button>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="通知设置" name="notifications">
          <el-card class="settings-card">
            <template #header>
              <h3>通知设置</h3>
            </template>
            <el-form :model="notificationForm" label-width="200px">
              <el-form-item label="新专家注册通知">
                <el-switch v-model="notificationForm.newExpert" />
                <span class="form-help">当有新专家注册时发送通知</span>
              </el-form-item>
              
              <el-form-item label="新项目创建通知">
                <el-switch v-model="notificationForm.newProject" />
                <span class="form-help">当有新项目创建时发送通知</span>
              </el-form-item>
              
              <el-form-item label="匹配成功通知">
                <el-switch v-model="notificationForm.matchSuccess" />
                <span class="form-help">当专家与项目匹配成功时发送通知</span>
              </el-form-item>
              
              <el-form-item label="项目进度更新通知">
                <el-switch v-model="notificationForm.projectUpdate" />
                <span class="form-help">当项目进度更新时发送通知</span>
              </el-form-item>
              
              <el-form-item label="系统维护通知">
                <el-switch v-model="notificationForm.systemMaintenance" />
                <span class="form-help">系统维护前发送通知</span>
              </el-form-item>
              
              <el-form-item label="通知方式">
                <el-checkbox-group v-model="notificationForm.methods">
                  <el-checkbox label="email">邮件</el-checkbox>
                  <el-checkbox label="sms">短信</el-checkbox>
                  <el-checkbox label="push">推送</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="数据备份" name="backup">
          <el-card class="settings-card">
            <template #header>
              <h3>数据备份与恢复</h3>
            </template>
            <div class="backup-container">
              <div class="backup-info">
                <div class="info-item">
                  <span class="label">上次备份时间:</span>
                  <span class="value">{{ backupInfo.lastBackup }}</span>
                </div>
                <div class="info-item">
                  <span class="label">备份文件大小:</span>
                  <span class="value">{{ backupInfo.backupSize }}</span>
                </div>
                <div class="info-item">
                  <span class="label">备份位置:</span>
                  <span class="value">{{ backupInfo.backupLocation }}</span>
                </div>
              </div>
              
              <div class="backup-actions">
                <el-button type="primary" icon="Download" @click="backupNow">立即备份</el-button>
                <el-button type="warning" icon="Upload" @click="showRestoreDialog">恢复数据</el-button>
                <el-button type="info" icon="Setting" @click="showBackupConfig">备份配置</el-button>
              </div>
              
              <div class="backup-history">
                <h4>备份历史</h4>
                <el-table :data="backupHistory" style="width: 100%">
                  <el-table-column prop="date" label="备份时间" width="180" />
                  <el-table-column prop="size" label="文件大小" width="120" />
                  <el-table-column prop="type" label="备份类型" width="120">
                    <template #default="{ row }">
                      <el-tag :type="row.type === 'full' ? 'primary' : 'success'">
                        {{ row.type === 'full' ? '完整备份' : '增量备份' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                        {{ row.status === 'success' ? '成功' : '失败' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                      <el-button type="text" @click="downloadBackup(row)">下载</el-button>
                      <el-button type="text" @click="deleteBackup(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('basic')

// 基本设置
const basicForm = reactive({
  systemName: 'Expert Link 专家管理系统',
  logoUrl: '',
  copyright: '© 2024 Expert Link. All rights reserved.',
  description: '专业的专家资源管理系统，帮助企业高效匹配专家与项目需求。'
})

const saveBasicSettings = () => {
  ElMessage.success('基本设置已保存')
}

const resetBasicSettings = () => {
  basicForm.systemName = 'Expert Link 专家管理系统'
  basicForm.logoUrl = ''
  basicForm.copyright = '© 2024 Expert Link. All rights reserved.'
  basicForm.description = '专业的专家资源管理系统，帮助企业高效匹配专家与项目需求。'
  ElMessage.info('设置已重置')
}

const handleLogoChange = (file: any) => {
  // 这里可以处理文件上传逻辑
  const reader = new FileReader()
  reader.onload = (e) => {
    basicForm.logoUrl = e.target?.result as string
  }
  reader.readAsDataURL(file.raw)
}

// 用户管理
const users = ref([
  { id: 1, username: 'admin', name: '管理员', email: 'admin@expertlink.com', role: 'admin', status: 'active' },
  { id: 2, username: 'manager1', name: '项目经理', email: 'manager1@expertlink.com', role: 'manager', status: 'active' },
  { id: 3, username: 'viewer1', name: '查看员', email: 'viewer1@expertlink.com', role: 'viewer', status: 'active' },
  { id: 4, username: 'editor1', name: '编辑员', email: 'editor1@expertlink.com', role: 'editor', status: 'inactive' }
])

const getRoleType = (role: string) => {
  switch (role) {
    case 'admin': return 'danger'
    case 'manager': return 'warning'
    case 'editor': return 'primary'
    case 'viewer': return 'success'
    default: return 'info'
  }
}

const getRoleText = (role: string) => {
  switch (role) {
    case 'admin': return '管理员'
    case 'manager': return '项目经理'
    case 'editor': return '编辑员'
    case 'viewer': return '查看员'
    default: return '未知'
  }
}

const showAddUserDialog = () => {
  ElMessage.info('打开添加用户对话框')
}

const toggleUserStatus = (user: any) => {
  ElMessage.success(`用户 ${user.name} 状态已${user.status === 'active' ? '启用' : '禁用'}`)
}

const editUser = (user: any) => {
  ElMessage.info(`编辑用户: ${user.name}`)
}

const deleteUser = (user: any) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    users.value = users.value.filter(u => u.id !== user.id)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 取消删除
  })
}

// 权限设置
const roles = ref([
  { id: 'admin', name: '管理员', permissions: {} as Record<string, boolean> },
  { id: 'manager', name: '项目经理', permissions: {} as Record<string, boolean> },
  { id: 'editor', name: '编辑员', permissions: {} as Record<string, boolean> },
  { id: 'viewer', name: '查看员', permissions: {} as Record<string, boolean> }
])

const selectedRoleId = ref('admin')

const selectedRole = computed(() => {
  return roles.value.find(role => role.id === selectedRoleId.value)
})

const permissionCategories = ref([
  {
    id: 'expert',
    name: '专家管理',
    permissions: [
      { id: 'expert_view', name: '查看专家' },
      { id: 'expert_add', name: '添加专家' },
      { id: 'expert_edit', name: '编辑专家' },
      { id: 'expert_delete', name: '删除专家' }
    ]
  },
  {
    id: 'project',
    name: '项目管理',
    permissions: [
      { id: 'project_view', name: '查看项目' },
      { id: 'project_add', name: '添加项目' },
      { id: 'project_edit', name: '编辑项目' },
      { id: 'project_delete', name: '删除项目' }
    ]
  },
  {
    id: 'skill',
    name: '技能管理',
    permissions: [
      { id: 'skill_view', name: '查看技能' },
      { id: 'skill_add', name: '添加技能' },
      { id: 'skill_edit', name: '编辑技能' },
      { id: 'skill_delete', name: '删除技能' }
    ]
  },
  {
    id: 'system',
    name: '系统管理',
    permissions: [
      { id: 'user_manage', name: '用户管理' },
      { id: 'role_manage', name: '角色管理' },
      { id: 'system_config', name: '系统配置' },
      { id: 'data_backup', name: '数据备份' }
    ]
  }
])

const selectRole = (roleId: string) => {
  selectedRoleId.value = roleId
}

const savePermissions = () => {
  ElMessage.success('权限设置已保存')
}

// 通知设置
const notificationForm = reactive({
  newExpert: true,
  newProject: true,
  matchSuccess: true,
  projectUpdate: true,
  systemMaintenance: true,
  methods: ['email', 'push']
})

const saveNotificationSettings = () => {
  ElMessage.success('通知设置已保存')
}

// 数据备份
const backupInfo = reactive({
  lastBackup: '2024-04-10 14:30:00',
  backupSize: '256 MB',
  backupLocation: '/backup/expertlink'
})

const backupHistory = ref([
  { date: '2024-04-10 14:30:00', size: '256 MB', type: 'full', status: 'success' },
  { date: '2024-04-03 14:30:00', size: '128 MB', type: 'incremental', status: 'success' },
  { date: '2024-03-27 14:30:00', size: '512 MB', type: 'full', status: 'success' },
  { date: '2024-03-20 14:30:00', size: '64 MB', type: 'incremental', status: 'failed' }
])

const backupNow = () => {
  ElMessageBox.confirm(
    '确定要立即备份数据吗？',
    '备份确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    ElMessage.success('数据备份已开始，请稍后查看备份历史')
  }).catch(() => {
    // 取消备份
  })
}

const showRestoreDialog = () => {
  ElMessage.info('打开数据恢复对话框')
}

const showBackupConfig = () => {
  ElMessage.info('打开备份配置对话框')
}

const downloadBackup = (backup: any) => {
  ElMessage.info(`下载备份: ${backup.date}`)
}

const deleteBackup = (backup: any) => {
  ElMessageBox.confirm(
    `确定要删除备份文件吗？\n备份时间: ${backup.date}`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    backupHistory.value = backupHistory.value.filter(b => b.date !== backup.date)
    ElMessage.success('备份文件已删除')
  }).catch(() => {
    // 取消删除
  })
}
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.settings-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.settings-tabs {
  min-height: 500px;
}

.settings-card {
  margin-bottom: 20px;
}

.settings-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.form-help {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

.permissions-container {
  display: flex;
  gap: 30px;
}

.roles-section {
  flex: 0 0 200px;
}

.roles-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.roles-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-tag {
  cursor: pointer;
  padding: 8px 12px;
  font-size: 14px;
}

.permissions-section {
  flex: 1;
}

.permissions-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.permission-category {
  margin-bottom: 24px;
}

.permission-category h5 {
  margin: 0 0 12px 0;
  font-size: 13px;
  font-weight: 600;
  color: #666;
}

.permission-items {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.no-role-selected {
  text-align: center;
  padding: 40px 0;
}

.permissions-actions {
  margin-top: 20px;
  text-align: right;
}

.backup-container {
  padding: 20px 0;
}

.backup-info {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  flex: 0 0 120px;
  color: #666;
  font-size: 14px;
}

.info-item .value {
  flex: 1;
  color: #333;
  font-size: 14px;
}

.backup-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.backup-history h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
</style>