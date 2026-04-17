import { defineStore } from 'pinia'

export interface Expert {
  id: number
  name: string
  title: string
  company: string
  email: string
  phone: string
  skills: string[]
  experience: number
  status: 'available' | 'busy' | 'unavailable'
  rating: number
  avatar: string
}

export const useExpertStore = defineStore('expert', {
  state: () => ({
    experts: [
      {
        id: 1,
        name: '张明',
        title: '高级AI工程师',
        company: '腾讯科技',
        email: 'zhangming@tencent.com',
        phone: '13800138001',
        skills: ['机器学习', '深度学习', 'Python'],
        experience: 8,
        status: 'available' as const,
        rating: 4.8,
        avatar: ''
      },
      {
        id: 2,
        name: '李华',
        title: '数据科学家',
        company: '阿里巴巴',
        email: 'lihua@alibaba.com',
        phone: '13800138002',
        skills: ['数据分析', '统计学', 'R语言'],
        experience: 6,
        status: 'busy' as const,
        rating: 4.5,
        avatar: ''
      },
      {
        id: 3,
        name: '王强',
        title: '前端架构师',
        company: '字节跳动',
        email: 'wangqiang@bytedance.com',
        phone: '13800138003',
        skills: ['Vue.js', 'React', 'TypeScript'],
        experience: 7,
        status: 'available' as const,
        rating: 4.7,
        avatar: ''
      }
    ]
  }),

  actions: {
    async fetchExperts() {
      return Promise.resolve()
    },

    addExpert(expert: Omit<Expert, 'id'>) {
      const newExpert = {
        ...expert,
        id: this.experts.length + 1
      }
      this.experts.push(newExpert)
    },
    
    updateExpert(id: number, data: Partial<Expert>) {
      const index = this.experts.findIndex(expert => expert.id === id)
      if (index !== -1) {
        this.experts[index] = { ...this.experts[index], ...data }
      }
    },
    
    deleteExpert(id: number) {
      this.experts = this.experts.filter(expert => expert.id !== id)
    },
    
    searchExperts(keyword: string) {
      return this.experts.filter(expert => 
        expert.name.includes(keyword) ||
        expert.skills.some(skill => skill.includes(keyword)) ||
        expert.company.includes(keyword)
      )
    }
  },

  getters: {
    availableExperts: (state) => state.experts.filter(expert => expert.status === 'available'),
    expertCount: (state) => state.experts.length,
    topRatedExperts: (state) => [...state.experts].sort((a, b) => b.rating - a.rating).slice(0, 5)
  }
})