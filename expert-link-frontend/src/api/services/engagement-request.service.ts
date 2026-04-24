import { apiClient, handleApiError } from '../utils'
import type { ApiResponse, PaginatedResponse } from '../types'
import type {
  CreateEngagementDraftPayload,
  EngagementRequestRow,
  PaginatedEngagements,
} from '../types/engagement'

function unwrap<T>(response: ApiResponse<T>): T {
  return response.data
}

export class EngagementRequestService {
  static async listMine(page = 0, size = 20): Promise<PaginatedEngagements> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<EngagementRequestRow>>>(
        '/engagement-requests/mine',
        { params: { page, size } }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取我的申请失败')
    }
  }

  static async stewardQueue(page = 0, size = 20): Promise<PaginatedEngagements> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<EngagementRequestRow>>>(
        '/engagement-requests/steward-queue',
        { params: { page, size } }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取行管待办失败')
    }
  }

  static async expertPending(page = 0, size = 20): Promise<PaginatedEngagements> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<EngagementRequestRow>>>(
        '/engagement-requests/expert-pending',
        { params: { page, size } }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取专家待确认失败')
    }
  }

  static async getById(id: number): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.get<ApiResponse<EngagementRequestRow>>(`/engagement-requests/${id}`)
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取申请单详情失败')
    }
  }

  static async createDraft(body: CreateEngagementDraftPayload): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>('/engagement-requests', body)
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '创建草稿失败')
    }
  }

  static async patchDraft(
    id: number,
    body: Partial<CreateEngagementDraftPayload> & { designatedExpertIds?: number[] | null }
  ): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.put<ApiResponse<EngagementRequestRow>>(`/engagement-requests/${id}`, body)
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '更新草稿失败')
    }
  }

  static async submit(id: number): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(`/engagement-requests/${id}/submit`)
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '提交申请失败')
    }
  }

  static async assign(
    id: number,
    expertIds: number[],
    assignmentNote?: string
  ): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/assign`,
        { expertIds, assignmentNote }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '指派专家失败')
    }
  }

  static async expertDecision(id: number, accepted: boolean, note?: string): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/expert-decision`,
        { accepted, note }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '确认失败')
    }
  }

  static async uploadEvaluationFile(id: number, file: File): Promise<{ path: string; originalFilename: string }> {
    const form = new FormData()
    form.append('file', file)
    try {
      const response = await apiClient.post<ApiResponse<{ path: string; originalFilename: string }>>(
        `/engagement-requests/${id}/evaluation-files`,
        form
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '上传附件失败')
    }
  }

  static async submitEvaluation(
    id: number,
    body: {
      professional: number
      timeliness: number
      attitude: number
      resolved: boolean
      comment?: string
      attachmentUrls?: string[]
    }
  ): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/submit-evaluation`,
        body
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '提交评价失败')
    }
  }

  static async reassign(id: number, expertIds: number[], reason?: string): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/reassign`,
        { expertIds, reason }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '改派失败')
    }
  }

  static async requestEvaluationRevision(id: number, reason: string): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/request-evaluation-revision`,
        { reason }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '退回重评失败')
    }
  }

  static async releaseScore(id: number, finalScore?: number, releaseNote?: string): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/release-score`,
        { finalScore, releaseNote }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '放分失败')
    }
  }

  static async rollback(id: number, reason: string): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/rollback`,
        { reason }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '退回失败')
    }
  }

  static async cancel(id: number, reason?: string): Promise<EngagementRequestRow> {
    try {
      const response = await apiClient.post<ApiResponse<EngagementRequestRow>>(
        `/engagement-requests/${id}/cancel`,
        { reason }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '取消申请失败')
    }
  }
}
