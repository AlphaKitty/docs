import { defineStore } from 'pinia'
import { AuthService } from '@/api/services/auth.service'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: (typeof localStorage !== 'undefined' && localStorage.getItem('auth_token')) || '',
    userId: 0,
    username: '',
    email: '',
    fullName: '',
    roles: [] as string[],
    pointsBalance: 0 as number,
    hydrated: false,
  }),

  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isSuperAdmin: (state) => state.roles.includes('SUPER_ADMIN'),
    displayName: (state) => state.fullName || state.username || '用户',
  },

  actions: {
    async restoreSession() {
      const stored = typeof localStorage !== 'undefined' ? localStorage.getItem('auth_token') : null
      this.token = stored || ''
      if (!stored) {
        this.resetProfile()
        this.hydrated = true
        return
      }
      try {
        const me = await AuthService.getCurrentUser()
        this.userId = me.userId
        this.username = me.username
        this.email = me.email
        this.fullName = me.fullName || ''
        this.roles = [...(me.roles || [])]
        const pb = me.pointsBalance
        this.pointsBalance = pb == null || Number.isNaN(Number(pb)) ? 0 : Number(pb)
      } catch {
        this.token = ''
        this.resetProfile()
        if (typeof localStorage !== 'undefined') {
          localStorage.removeItem('auth_token')
        }
      } finally {
        this.hydrated = true
      }
    },

    resetProfile() {
      this.userId = 0
      this.username = ''
      this.email = ''
      this.fullName = ''
      this.roles = []
      this.pointsBalance = 0
    },

    async login(username: string, password: string) {
      const res = await AuthService.login({ username, password })
      this.token = res.accessToken
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('auth_token', this.token)
      }
      this.userId = res.userId
      this.username = res.username
      this.roles = [...(res.roles || [])]
      await this.restoreSession()
    },

    logout() {
      this.token = ''
      this.resetProfile()
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem('auth_token')
      }
      this.hydrated = true
    },
  },
})
