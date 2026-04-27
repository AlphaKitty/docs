import { defineStore } from 'pinia'
import type { AppRole } from '@/constants/role-policy'
import { SettingsService } from '@/api/services/settings.service'

export type RoleGroupKey = 'STEWARD' | 'EXPERT' | 'SUPER_ADMIN'
type RoleMenus = Record<string, string[]>

const DEFAULT_ROLE_GROUPS: Record<RoleGroupKey, AppRole[]> = {
  STEWARD: ['DOMAIN_STEWARD', 'SUPER_ADMIN'],
  EXPERT: ['EXPERT_USER', 'SUPER_ADMIN'],
  SUPER_ADMIN: ['SUPER_ADMIN'],
}

const DEFAULT_BASE_MENUS = [
  'dashboard',
  'expertLibrary',
  'engagements.mine',
  'engagements.pointsLedger',
  'experts',
  'domains',
  'skills',
  'projects',
  'analysis',
  'settings',
] as const

const DEFAULT_ROLE_MENUS: RoleMenus = {
  REGULAR_USER: [...DEFAULT_BASE_MENUS],
  DEPT_ADMIN: [...DEFAULT_BASE_MENUS],
  EXPERT_USER: [...DEFAULT_BASE_MENUS, 'engagements.expertPending'],
  DOMAIN_STEWARD: [...DEFAULT_BASE_MENUS, 'engagements.stewardQueue'],
  SUPER_ADMIN: [
    'dashboard',
    'expertLibrary',
    'engagements.mine',
    'engagements.new',
    'engagements.stewardQueue',
    'engagements.expertPending',
    'engagements.pointsLedger',
    'experts',
    'domains',
    'skills',
    'projects',
    'analysis',
    'settings',
    'admin.users',
    'admin.domainStewards',
    'admin.auditLogs',
    'admin.accessControl',
  ],
}

function sanitizeRoleGroups(raw: unknown): Record<RoleGroupKey, AppRole[]> {
  const fallback = { ...DEFAULT_ROLE_GROUPS }
  if (!raw || typeof raw !== 'object') return fallback
  const obj = raw as Record<string, unknown>
  const asList = (v: unknown, d: AppRole[]) => {
    if (!Array.isArray(v)) return d
    return v.filter((x): x is AppRole => typeof x === 'string') as AppRole[]
  }
  return {
    STEWARD: asList(obj.STEWARD, fallback.STEWARD),
    EXPERT: asList(obj.EXPERT, fallback.EXPERT),
    SUPER_ADMIN: asList(obj.SUPER_ADMIN, fallback.SUPER_ADMIN),
  }
}

export const useSystemSettingsStore = defineStore('system-settings', {
  state: () => ({
    roleGroups: { ...DEFAULT_ROLE_GROUPS } as Record<RoleGroupKey, AppRole[]>,
    hydrated: false,
    loading: false,
    menuKeys: [] as string[],
    roleMenus: {} as RoleMenus,
  }),
  getters: {
    rolesFor: (state) => (key: RoleGroupKey) => state.roleGroups[key],
    allowedMenusForRole: (state) => (role: string) => state.roleMenus[role] || DEFAULT_ROLE_MENUS[role] || [],
    canAccessMenu: (state) => (roles: string[], menuKey: string) =>
      roles.some((r) => (state.roleMenus[r] || DEFAULT_ROLE_MENUS[r] || []).includes(menuKey)),
  },
  actions: {
    async ensureHydrated() {
      if (this.hydrated || this.loading) return
      this.loading = true
      try {
        const res = await SettingsService.getRoleGroups()
        this.roleGroups = sanitizeRoleGroups({
          STEWARD: res.steward,
          EXPERT: res.expert,
          SUPER_ADMIN: res.superAdmin,
        })
        const menuRes = await SettingsService.getMenuPermissions()
        this.menuKeys = menuRes.menuKeys || []
        this.roleMenus = menuRes.roleMenus || {}
      } catch {
        this.roleGroups = { ...DEFAULT_ROLE_GROUPS }
        this.menuKeys = []
        this.roleMenus = { ...DEFAULT_ROLE_MENUS }
      } finally {
        this.hydrated = true
        this.loading = false
      }
    },
    setRoleGroup(key: RoleGroupKey, roles: AppRole[]) {
      this.roleGroups[key] = [...roles]
    },
    async saveRoleGroups() {
      const res = await SettingsService.updateRoleGroups({
        steward: [...this.roleGroups.STEWARD],
        expert: [...this.roleGroups.EXPERT],
        superAdmin: [...this.roleGroups.SUPER_ADMIN],
      })
      this.roleGroups = sanitizeRoleGroups({
        STEWARD: res.steward,
        EXPERT: res.expert,
        SUPER_ADMIN: res.superAdmin,
      })
    },
    async resetRoleGroups() {
      this.roleGroups = { ...DEFAULT_ROLE_GROUPS }
      await this.saveRoleGroups()
    },
    async saveMenuPermissions() {
      const res = await SettingsService.updateMenuPermissions({
        roleMenus: this.roleMenus,
      })
      this.menuKeys = res.menuKeys || []
      this.roleMenus = res.roleMenus || {}
    },
    async reloadMenuPermissions() {
      const res = await SettingsService.getMenuPermissions()
      this.menuKeys = res.menuKeys || []
      this.roleMenus = res.roleMenus || {}
    },
  },
})

