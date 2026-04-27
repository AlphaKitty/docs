<template>
  <div id="app">
    <router-view v-if="isLoginRoute" />
    <el-container v-else class="app-container">
      <el-header class="app-header">
        <div class="header-left">
          <el-button
            class="menu-toggle-btn"
            type="primary"
            link
            :icon="isSidebarCollapsed ? Expand : Fold"
            @click="toggleSidebar"
          />
          <h1 class="app-title">Expert Link 专家管理系统</h1>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="onUserMenu">
            <span class="user-info">
              <el-avatar :size="32">{{ displayInitial }}</el-avatar>
              <span class="user-name">{{ userName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ auth.email }}</el-dropdown-item>
                <el-dropdown-item disabled>积分余额：{{ auth.pointsBalance.toFixed(2) }}</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <el-aside :width="isSidebarCollapsed ? '64px' : '220px'" class="app-sidebar">
          <el-menu
            :default-active="activeMenu"
            :default-openeds="isSidebarCollapsed ? [] : sidebarOpeneds"
            :collapse="isSidebarCollapsed"
            class="sidebar-menu"
            router
          >
            <template v-for="item in visibleMenus" :key="item.key">
              <el-sub-menu v-if="item.children" :index="item.key">
                <template #title>
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ item.label }}</span>
                </template>
                <el-menu-item
                  v-for="child in item.children"
                  :key="child.path"
                  :index="child.path"
                >
                  {{ child.label }}
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item v-else :index="item.path!">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </el-menu-item>
            </template>
          </el-menu>
        </el-aside>

        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSystemSettingsStore } from '@/stores/system-settings'
import {
  Expand,
  Fold,
  House,
  User,
  Document,
  DataAnalysis,
  Tickets,
  Setting,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const settings = useSystemSettingsStore()
void settings.ensureHydrated()
const isSidebarCollapsed = ref(false)

type MenuLeaf = {
  key: string
  label: string
  path: string
}

type MenuGroup = {
  key: string
  label: string
  icon: typeof House
  path?: string
  children?: MenuLeaf[]
}

const menuConfig: MenuGroup[] = [
  { key: 'dashboard', label: '仪表盘', icon: House, path: '/' },
  {
    key: 'engagements',
    label: '调用申请',
    icon: Tickets,
    children: [
      { key: 'engagements.mine', label: '我的申请', path: '/engagements/mine' },
      { key: 'engagements.stewardQueue', label: '行管待办', path: '/engagements/steward-queue' },
      { key: 'engagements.expertPending', label: '专家待确认', path: '/engagements/expert-pending' },
      { key: 'engagements.pointsLedger', label: '积分台账', path: '/engagements/points-ledger' },
    ],
  },
  {
    key: 'experts',
    label: '专家管理',
    icon: User,
    path: '/experts',
  },
  { key: 'domains', label: '专家入库', icon: User, path: '/domains' },
  { key: 'skills', label: '技能管理', icon: User, path: '/skills' },
  { key: 'projects', label: '项目列表', icon: Document, path: '/projects' },
  { key: 'analysis', label: '统计分析', icon: DataAnalysis, path: '/analysis' },
  {
    key: 'system',
    label: '系统',
    icon: Setting,
    children: [
      { key: 'admin.users', label: '用户与角色', path: '/admin/users' },
      { key: 'admin.domainStewards', label: '领域行管配置', path: '/admin/domain-stewards' },
      { key: 'admin.auditLogs', label: '审计日志', path: '/admin/audit-logs' },
      { key: 'admin.accessControl', label: '菜单授权', path: '/admin/access-control' },
      { key: 'settings', label: '系统设置', path: '/settings' },
    ],
  },
]

const canViewLeaf = (leaf: MenuLeaf) => settings.canAccessMenu(auth.roles, leaf.key)

const visibleMenus = computed(() =>
  menuConfig
    .map((group) => {
      if (!group.children) return settings.canAccessMenu(auth.roles, group.key) ? group : null
      const children = group.children.filter(canViewLeaf)
      return children.length > 0 ? { ...group, children } : null
    })
    .filter((x): x is MenuGroup => x !== null)
)

const isLoginRoute = computed(() => route.name === 'Login')
/** 子路由高亮到对应侧栏项（与 el-menu-item index 一致） */
const activeMenu = computed(() => {
  const p = route.path
  if (p === '/' || p === '') return '/'

  if (p.startsWith('/engagements')) {
    if (p.startsWith('/engagements/steward-queue')) return '/engagements/steward-queue'
    if (p.startsWith('/engagements/expert-pending')) return '/engagements/expert-pending'
    if (p.startsWith('/engagements/points-ledger')) return '/engagements/points-ledger'
    if (p.startsWith('/engagements/mine')) return '/engagements/mine'
    // 详情等：/engagements/:id（兼容数字或 UUID）
    if (/^\/engagements\/[^/]+\/?$/.test(p)) {
      return '/engagements/mine'
    }
    return '/engagements/mine'
  }

  if (p.startsWith('/experts')) return '/experts'
  if (p.startsWith('/domains')) return '/domains'
  if (p.startsWith('/skills')) return '/skills'
  if (p.startsWith('/projects')) return '/projects'
  if (p.startsWith('/analysis')) return '/analysis'
  if (p.startsWith('/admin')) return p
  if (p.startsWith('/settings')) return '/settings'

  return p
})

/** 进入某模块时自动展开对应子菜单，避免高亮项藏在折叠组里 */
const sidebarOpeneds = computed(() => {
  const p = route.path
  const keys: string[] = []
  if (p.startsWith('/engagements')) keys.push('engagements')
  if (p.startsWith('/admin') || p.startsWith('/settings')) keys.push('system')
  return keys
})
const userName = computed(() => auth.displayName)
const displayInitial = computed(() => (auth.username || '?').slice(0, 1).toUpperCase())

const onUserMenu = (cmd: string) => {
  if (cmd === 'logout') {
    auth.logout()
    void router.push('/login')
  }
}

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

watch(
  () => route.path,
  (path) => {
    if (path === '/') {
      isSidebarCollapsed.value = true
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  color: white;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left .app-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-toggle-btn {
  color: #fff;
}

.header-right .user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: white;
}

.user-name {
  margin-left: 8px;
  font-size: 14px;
}

.app-sidebar {
  background-color: #f8f9fa;
  border-right: 1px solid #e4e7ed;
  transition: width 0.2s ease;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.app-main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
