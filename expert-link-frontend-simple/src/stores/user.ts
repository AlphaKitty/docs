import { defineStore } from 'pinia'

interface User {
  id: number
  name: string
  email: string
  avatar: string
  role: 'admin' | 'manager' | 'user'
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: {
      id: 1,
      name: '管理员',
      email: 'admin@expertlink.com',
      avatar: '',
      role: 'admin' as const
    },
    token: '',
    isAuthenticated: true
  }),

  actions: {
    login(credentials: { email: string; password: string }) {
      // 模拟登录
      this.isAuthenticated = true
      this.token = 'mock-jwt-token'
    },
    
    logout() {
      this.isAuthenticated = false
      this.token = ''
    },
    
    updateProfile(data: Partial<User>) {
      this.user = { ...this.user, ...data }
    }
  },

  getters: {
    isAdmin: (state) => state.user.role === 'admin',
    userInfo: (state) => state.user
  }
})