<template>
  <div class="page">
    <div class="page-header">
      <h2>菜单授权</h2>
      <div>
        <el-button @click="reload">刷新</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存授权</el-button>
      </div>
    </div>
    <el-alert
      class="mb"
      type="info"
      :closable="false"
      title="仅超级管理员可见；授权精确到菜单，影响侧栏和路由访问。"
    />
    <div class="section-title">顶级菜单</div>
    <el-table v-loading="loading" :data="topLevelRows" border size="small" class="mb">
      <el-table-column prop="menuKey" label="菜单" min-width="220" fixed="left" align="center">
        <template #default="{ row }">
          {{ menuLabel(row.menuKey) }}
        </template>
      </el-table-column>
      <el-table-column
        v-for="role in roles"
        :key="role"
        :label="roleLabel(role)"
        width="160"
        align="center"
      >
        <template #default="{ row }">
          <el-checkbox v-model="row.permissions[role]" />
        </template>
      </el-table-column>
    </el-table>

    <el-collapse v-model="openedGroups" class="menu-collapse">
      <el-collapse-item
        v-for="group in collapsibleGroups"
        :key="group.key"
        :name="group.key"
      >
        <template #title>
          <span class="group-title">{{ group.label }}</span>
          <el-tag size="small" type="info">{{ group.rows.length }}</el-tag>
        </template>
        <el-table v-loading="loading" :data="group.rows" border size="small">
          <el-table-column prop="menuKey" label="菜单" min-width="220" fixed="left" align="center">
            <template #default="{ row }">
              {{ menuLabel(row.menuKey) }}
            </template>
          </el-table-column>
          <el-table-column
            v-for="role in roles"
            :key="role"
            :label="roleLabel(role)"
            width="160"
            align="center"
          >
            <template #default="{ row }">
              <el-checkbox v-model="row.permissions[role]" />
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { USER_ROLE_OPTIONS, roleLabel } from '@/constants/user-roles'
import { useSystemSettingsStore } from '@/stores/system-settings'

type MenuRow = {
  menuKey: string
  permissions: Record<string, boolean>
}

const settings = useSystemSettingsStore()
const loading = ref(false)
const saving = ref(false)
const menuRows = ref<MenuRow[]>([])
const openedGroups = ref<string[]>(['engagements', 'system'])

const menuLabelMap: Record<string, string> = {
  dashboard: '仪表盘',
  expertLibrary: '专家库',
  'engagements.mine': '我的申请',
  'engagements.new': '新建申请',
  'engagements.stewardQueue': '行管待办',
  'engagements.expertPending': '专家待确认',
  'engagements.pointsLedger': '积分台账',
  experts: '专家管理',
  domains: '专家入库',
  skills: '技能管理',
  analysis: '统计分析',
  settings: '系统设置',
  'admin.users': '用户与角色',
  'admin.domainStewards': '领域行管配置',
  'admin.auditLogs': '审计日志',
  'admin.accessControl': '菜单授权',
}

const menuKeys = computed(() => settings.menuKeys)
const roles = USER_ROLE_OPTIONS.map((o) => o.value).filter((r) => r !== 'VISITOR')

const topLevelMenuOrder = ['dashboard', 'expertLibrary', 'experts', 'domains', 'skills', 'analysis'] as const
const collapsibleGroupOrder = ['engagements', 'system'] as const
const groupLabelMap: Record<(typeof collapsibleGroupOrder)[number], string> = {
  engagements: '调用申请',
  system: '系统',
}

function menuLabel(k: string) {
  return menuLabelMap[k] || k
}

function groupKeyForMenu(menuKey: string): (typeof collapsibleGroupOrder)[number] | null {
  if (menuKey.startsWith('engagements.')) return 'engagements'
  if (menuKey.startsWith('admin.') || menuKey === 'settings') return 'system'
  return null
}

const topLevelRows = computed(() => {
  const map = new Map(menuRows.value.map((r) => [r.menuKey, r]))
  return topLevelMenuOrder.map((key) => map.get(key)).filter((r): r is MenuRow => !!r)
})

const collapsibleGroups = computed(() => {
  const buckets = new Map<(typeof collapsibleGroupOrder)[number], MenuRow[]>()
  for (const row of menuRows.value) {
    const key = groupKeyForMenu(row.menuKey)
    if (!key) continue
    const list = buckets.get(key) || []
    list.push(row)
    buckets.set(key, list)
  }
  return collapsibleGroupOrder
    .map((key) => ({
      key,
      label: groupLabelMap[key],
      rows: buckets.get(key) || [],
    }))
    .filter((g) => g.rows.length > 0)
})

function buildRows() {
  menuRows.value = menuKeys.value.map((menuKey) => {
    const permissions: Record<string, boolean> = {}
    for (const role of roles) {
      const allowed = new Set(settings.allowedMenusForRole(role))
      permissions[role] = allowed.has(menuKey)
    }
    return { menuKey, permissions }
  })
}

async function reload() {
  loading.value = true
  try {
    await settings.reloadMenuPermissions()
    buildRows()
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const roleMenus: Record<string, string[]> = {}
    for (const role of roles) {
      roleMenus[role] = menuRows.value
        .filter((row) => row.permissions[role])
        .map((row) => row.menuKey)
    }
    settings.roleMenus = roleMenus
    await settings.saveMenuPermissions()
    buildRows()
    ElMessage.success('授权已保存')
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

void reload()
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
.mb {
  margin-bottom: 12px;
}
.menu-collapse {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px 12px;
}
.group-title {
  margin-right: 8px;
  font-weight: 600;
}
.section-title {
  font-weight: 600;
  margin: 6px 0 10px;
  color: #606266;
}
</style>

