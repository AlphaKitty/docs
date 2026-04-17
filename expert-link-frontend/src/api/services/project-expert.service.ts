import { apiClient, handleApiError } from '../utils'
import type { ApiResponse } from '../types'

export interface ProjectExpertAssignment {
  id: number
  projectId: number
  expertId: number
  role?: string
  responsibilities?: string
  startDate?: string
  endDate?: string
  totalHours?: number
  hourlyRate?: number
  status?: string
  performanceRating?: number
  feedback?: string
  isLead?: boolean
  isPrimaryContact?: boolean
  completionPercentage?: number
  hoursLogged?: number
  expert?: {
    id: number
    name: string
    currentPosition?: string
    currentCompany?: string
    email?: string
    phoneNumber?: string
    avatar?: string
    skills?: Array<{ id: number; name: string }>
  }
}

export interface CreateProjectExpertAssignmentRequest {
  projectId: number
  expertId: number
  role: string
  responsibilities?: string
  startDate?: string
  endDate?: string
  totalHours?: number
  hourlyRate?: number
  status?: string
}

function toAssignmentPayload(data: CreateProjectExpertAssignmentRequest) {
  return {
    role: data.role,
    responsibilities: data.responsibilities,
    startDate: data.startDate || undefined,
    endDate: data.endDate || undefined,
    totalHours: data.totalHours,
    hourlyRate: data.hourlyRate,
    status: data.status || 'PENDING',
  }
}

export class ProjectExpertService {
  static async getAssignmentsByProjectId(projectId: number): Promise<ProjectExpertAssignment[]> {
    try {
      const response = await apiClient.get<ApiResponse<ProjectExpertAssignment[]>>(`/project-experts/project/${projectId}`)
      return response.data
    } catch (error) {
      throw handleApiError(error, '获取项目分配专家失败')
    }
  }

  static async getAssignmentsByExpertId(expertId: number): Promise<ProjectExpertAssignment[]> {
    try {
      const response = await apiClient.get<ApiResponse<ProjectExpertAssignment[]>>(`/project-experts/expert/${expertId}`)
      return response.data
    } catch (error) {
      throw handleApiError(error, '获取专家参与项目失败')
    }
  }

  static async exists(projectId: number, expertId: number): Promise<boolean> {
    try {
      const response = await apiClient.get<ApiResponse<boolean>>('/project-experts/exists', {
        params: { projectId, expertId },
      })
      return response.data
    } catch (error) {
      throw handleApiError(error, '检查分配关系失败')
    }
  }

  static async createAssignment(data: CreateProjectExpertAssignmentRequest): Promise<ProjectExpertAssignment> {
    try {
      const response = await apiClient.post<ApiResponse<ProjectExpertAssignment>>(
        '/project-experts',
        toAssignmentPayload(data),
        {
          params: {
            projectId: data.projectId,
            expertId: data.expertId,
          },
        }
      )
      return response.data
    } catch (error) {
      throw handleApiError(error, '创建项目专家分配失败')
    }
  }
}
