import { apiClient, handleApiError } from '../utils'
import type { ApiResponse } from '../types'
import type {
  BatchAddExpertsToDesignationBody,
  BatchAddExpertsToDesignationResult,
  CreateExpertDesignationBody,
  ExpertDesignation,
} from '../types/expert-designation'

export class ExpertDesignationService {
  static async list(): Promise<ExpertDesignation[]> {
    try {
      const res = await apiClient.get<ApiResponse<ExpertDesignation[]>>('/expert-designations')
      return res.data
    } catch (e) {
      throw handleApiError(e, '获取称谓列表失败')
    }
  }

  static async create(body: CreateExpertDesignationBody): Promise<ExpertDesignation> {
    try {
      const res = await apiClient.post<ApiResponse<ExpertDesignation>>('/expert-designations', body)
      return res.data
    } catch (e) {
      throw handleApiError(e, '新增称谓失败')
    }
  }

  static async batchAddExperts(
    designationId: number,
    body: BatchAddExpertsToDesignationBody
  ): Promise<BatchAddExpertsToDesignationResult> {
    try {
      const res = await apiClient.post<ApiResponse<BatchAddExpertsToDesignationResult>>(
        `/expert-designations/${designationId}/experts/batch-add`,
        body
      )
      return res.data
    } catch (e) {
      throw handleApiError(e, '批量添加专家失败')
    }
  }
}
