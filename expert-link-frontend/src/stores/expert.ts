import { defineStore } from 'pinia'
import { ExpertService } from '@/api/services/expert.service'
import { useAuthStore } from '@/stores/auth'
import { ExpertLevel, ExpertStatus } from '@/api/types'
import type { CreateExpertRequest, ExpertDetail, UpdateExpertRequest } from '@/api/types'

export interface Expert {
  id: number
  name: string
  title: string
  company: string
  email: string
  phone: string
  skills: string[]
  domains?: string[]
  experience: number
  status: 'available' | 'busy' | 'unavailable'
  rating: number
  avatar: string
}

interface ExpertFormPayload extends Omit<Expert, 'id'> {
  ownerId?: number
  designationId?: number
}

const mapApiStatusToStore = (status: ExpertDetail['status'], availability: boolean): Expert['status'] => {
  if (!availability || status === ExpertStatus.INACTIVE || status === ExpertStatus.ARCHIVED) {
    return 'unavailable'
  }
  if (status === ExpertStatus.PENDING) {
    return 'busy'
  }
  return 'available'
}

const mapStoreStatusToApi = (status: Expert['status']): ExpertStatus => {
  switch (status) {
    case 'busy':
      return ExpertStatus.PENDING
    case 'unavailable':
      return ExpertStatus.INACTIVE
    default:
      return ExpertStatus.ACTIVE
  }
}

const toNumber = (value: unknown, fallback = 0): number => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim() !== '') {
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : fallback
  }

  return fallback
}

const adaptExpertFromApi = (expert: ExpertDetail & Record<string, any>): Expert => ({
  id: toNumber(expert.id),
  name: expert.name || '未命名专家',
  title: expert.title || expert.currentPosition || '',
  company: expert.company || expert.currentCompany || '',
  email: expert.email || '',
  phone: expert.phone || expert.phoneNumber || '',
  skills: Array.isArray(expert.skills)
    ? expert.skills.map((skill: any) => skill.skillName || skill.name).filter(Boolean)
    : [],
  domains: Array.isArray(expert.domains)
    ? expert.domains.map((d: any) => d.domainName || d.name).filter(Boolean)
    : (expert.primaryDomain?.name ? [expert.primaryDomain.name] : []),
  experience: toNumber(expert.experienceYears ?? expert.yearsOfExperience),
  status: mapApiStatusToStore(
    expert.status ?? (expert.availabilityStatus === 'UNAVAILABLE' ? ExpertStatus.INACTIVE : ExpertStatus.ACTIVE),
    expert.availability ?? expert.availabilityStatus !== 'UNAVAILABLE'
  ),
  rating: toNumber(expert.rating ?? expert.overallRating),
  avatar: expert.avatar || ''
})

const basePayload = (expert: ExpertFormPayload) => ({
  name: expert.name,
  title: expert.title,
  email: expert.email,
  phone: expert.phone,
  company: expert.company,
  position: expert.title,
  introduction: `${expert.title} - ${expert.company}`,
  experienceYears: expert.experience,
  hourlyRate: 0,
  status: mapStoreStatusToApi(expert.status),
  level: ExpertLevel.MIDDLE,
  availability: expert.status === 'available',
  tags: expert.skills,
  skillIds: [] as number[],
  domainIds: [] as number[],
})

const adaptExpertToCreateRequest = (expert: ExpertFormPayload): CreateExpertRequest => {
  const auth = useAuthStore()
  const ownerId = expert.ownerId ?? auth.userId
  const designationId = expert.designationId
  if (designationId == null || designationId < 1) {
    throw new Error('请选择称谓/岗位')
  }
  return {
    ownerId: ownerId || 1,
    designationId,
    ...basePayload(expert),
  }
}

const adaptExpertToUpdateRequest = (id: number, expert: ExpertFormPayload): UpdateExpertRequest => ({
  id,
  ownerId: expert.ownerId,
  ...(expert.designationId != null && expert.designationId > 0 ? { designationId: expert.designationId } : {}),
  ...basePayload(expert),
})

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
    ] as Expert[],
    loading: false
  }),

  actions: {
    async fetchExperts() {
      this.loading = true
      try {
        const response = await ExpertService.getExperts({ page: 0, size: 100 })
        this.experts = response.content.map(adaptExpertFromApi)
      } finally {
        this.loading = false
      }
    },

    async addExpert(expert: ExpertFormPayload) {
      const createdExpert = await ExpertService.createExpert(adaptExpertToCreateRequest(expert))
      const adaptedExpert = adaptExpertFromApi(createdExpert)
      this.experts.unshift(adaptedExpert)
      return adaptedExpert
    },
    
    async updateExpert(id: number, expert: ExpertFormPayload) {
      const updatedExpert = await ExpertService.updateExpert(id, adaptExpertToUpdateRequest(id, expert))
      const adaptedExpert = adaptExpertFromApi(updatedExpert)
      const index = this.experts.findIndex(expert => expert.id === id)
      if (index !== -1) {
        this.experts[index] = adaptedExpert
      }
      return adaptedExpert
    },
    
    async deleteExpert(id: number) {
      await ExpertService.deleteExpert(id)
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