import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { hasAnyRole } from '@/constants/role-policy'
import { useSystemSettingsStore } from '@/stores/system-settings'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { menuKey: 'dashboard' },
    },
    {
      path: '/experts',
      name: 'Experts',
      component: () => import('@/views/experts/ExpertList.vue'),
      meta: { menuKey: 'experts' },
    },
    {
      path: '/experts/add',
      name: 'ExpertAdd',
      component: () => import('@/views/experts/AddExpert.vue'),
      meta: { menuKey: 'experts' },
    },
    {
      path: '/experts/:id/edit',
      name: 'ExpertEdit',
      component: () => import('@/views/experts/AddExpert.vue'),
      meta: { menuKey: 'experts' },
    },
    {
      path: '/experts/:id',
      name: 'ExpertDetail',
      component: () => import('@/views/experts/ExpertDetail.vue'),
      meta: { menuKey: 'experts' },
    },
    {
      path: '/dashboard/expert-profile/:id',
      name: 'DashboardExpertProfile',
      component: () => import('@/views/experts/DashboardExpertProfile.vue'),
      meta: { menuKey: 'dashboard' },
    },
    {
      path: '/projects',
      name: 'Projects',
      component: () => import('@/views/projects/ProjectList.vue'),
      meta: { menuKey: 'projects' },
    },
    {
      path: '/projects/add',
      name: 'ProjectAdd',
      component: () => import('@/views/projects/AddProject.vue'),
      meta: { menuKey: 'projects' },
    },
    {
      path: '/projects/:id/edit',
      name: 'ProjectEdit',
      component: () => import('@/views/projects/AddProject.vue'),
      meta: { menuKey: 'projects' },
    },
    {
      path: '/projects/:id',
      name: 'ProjectDetail',
      component: () => import('@/views/projects/ProjectDetail.vue'),
      meta: { menuKey: 'projects' },
    },
    {
      path: '/skills',
      name: 'Skills',
      component: () => import('@/views/skills/SkillList.vue'),
      meta: { menuKey: 'skills' },
    },
    {
      path: '/domains',
      name: 'Domains',
      component: () => import('@/views/domains/DomainList.vue'),
      meta: { menuKey: 'domains' },
    },
    {
      path: '/domains/add',
      name: 'DomainAdd',
      component: () => import('@/views/domains/DomainEdit.vue'),
      meta: { menuKey: 'domains' },
    },
    {
      path: '/domains/:id/edit',
      name: 'DomainEdit',
      component: () => import('@/views/domains/DomainEdit.vue'),
      meta: { menuKey: 'domains' },
    },
    {
      path: '/domains/:id',
      name: 'DomainDetail',
      component: () => import('@/views/domains/DomainDetail.vue'),
      meta: { menuKey: 'domains' },
    },
    {
      path: '/skills/add',
      name: 'SkillAdd',
      component: () => import('@/views/skills/AddSkill.vue'),
      meta: { menuKey: 'skills' },
    },
    {
      path: '/skills/:id/edit',
      name: 'SkillEdit',
      component: () => import('@/views/skills/AddSkill.vue'),
      meta: { menuKey: 'skills' },
    },
    {
      path: '/skills/:id',
      name: 'SkillDetail',
      component: () => import('@/views/skills/SkillDetail.vue'),
      meta: { menuKey: 'skills' },
    },
    {
      path: '/analysis',
      name: 'Analysis',
      component: () => import('@/views/analysis/AnalysisDashboard.vue'),
      meta: { menuKey: 'analysis' },
    },
    {
      path: '/engagements/mine',
      name: 'EngagementMine',
      component: () => import('@/views/engagements/EngagementListView.vue'),
      meta: { engagementList: 'mine', menuKey: 'engagements.mine' },
    },
    {
      path: '/engagements/steward-queue',
      name: 'EngagementStewardQueue',
      component: () => import('@/views/engagements/EngagementListView.vue'),
      meta: { engagementList: 'steward', roleGroup: 'STEWARD', menuKey: 'engagements.stewardQueue' },
    },
    {
      path: '/engagements/expert-pending',
      name: 'EngagementExpertPending',
      component: () => import('@/views/engagements/EngagementListView.vue'),
      meta: { engagementList: 'expert', roleGroup: 'EXPERT', menuKey: 'engagements.expertPending' },
    },
    {
      path: '/engagements/points-ledger',
      name: 'EngagementPointsLedger',
      component: () => import('@/views/engagements/PointsLedgerView.vue'),
      meta: { menuKey: 'engagements.pointsLedger' },
    },
    {
      path: '/engagements/new',
      name: 'EngagementNew',
      component: () => import('@/views/engagements/EngagementCreateView.vue'),
      // 与「我的申请」同一权限：新建入口不在侧栏单独占键，避免仅 SUPER_ADMIN 默认含 engagements.new 时被重定向到仪表盘
      meta: { menuKey: 'engagements.mine' },
    },
    {
      path: '/engagements/:id',
      name: 'EngagementDetail',
      component: () => import('@/views/engagements/EngagementDetailView.vue'),
      meta: { menuKey: 'engagements.mine' },
    },
    {
      path: '/admin/users',
      name: 'UsersAdmin',
      component: () => import('@/views/admin/UsersAdminView.vue'),
      meta: { roleGroup: 'SUPER_ADMIN', menuKey: 'admin.users' },
    },
    {
      path: '/admin/domain-stewards',
      name: 'DomainStewards',
      component: () => import('@/views/admin/DomainStewardsView.vue'),
      meta: { roleGroup: 'SUPER_ADMIN', menuKey: 'admin.domainStewards' },
    },
    {
      path: '/admin/audit-logs',
      name: 'AuditLogs',
      component: () => import('@/views/admin/AuditLogsView.vue'),
      meta: { roleGroup: 'SUPER_ADMIN', menuKey: 'admin.auditLogs' },
    },
    {
      path: '/admin/access-control',
      name: 'AccessControl',
      component: () => import('@/views/admin/AccessControlView.vue'),
      meta: { roleGroup: 'SUPER_ADMIN', menuKey: 'admin.accessControl' },
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/settings/SystemSettings.vue'),
      meta: { menuKey: 'settings' },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const settings = useSystemSettingsStore()
  if (!auth.hydrated) {
    await auth.restoreSession()
  }
  await settings.ensureHydrated()
  if (to.meta.public) {
    if (auth.isAuthenticated && to.name === 'Login') {
      return { path: '/' }
    }
    return true
  }
  if (!auth.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const roleGroup = to.meta.roleGroup as 'STEWARD' | 'EXPERT' | 'SUPER_ADMIN' | undefined
  const roles = roleGroup ? settings.rolesFor(roleGroup) : ((to.meta.roles as string[] | undefined) || undefined)
  if (roles?.length && !hasAnyRole(auth.roles, roles)) {
    return { path: '/' }
  }
  const menuKey = to.meta.menuKey as string | undefined
  if (menuKey && !settings.canAccessMenu(auth.roles, menuKey)) {
    const fallbackCandidates = [
      { path: '/', menuKey: 'dashboard' },
      { path: '/engagements/mine', menuKey: 'engagements.mine' },
      { path: '/experts', menuKey: 'experts' },
      { path: '/projects', menuKey: 'projects' },
      { path: '/settings', menuKey: 'settings' },
    ]
    const firstAllowed = fallbackCandidates.find((item) =>
      settings.canAccessMenu(auth.roles, item.menuKey)
    )
    // 避免“跳回自身”触发 infinite redirect
    if (firstAllowed && firstAllowed.path !== to.path) {
      return { path: firstAllowed.path }
    }
    // 配置缺失时允许通过，避免登录后卡死
    return true
  }
  return true
})

export default router
