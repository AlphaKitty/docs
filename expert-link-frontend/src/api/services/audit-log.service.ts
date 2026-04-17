import { apiClient, handleApiError } from '../utils'
import type { ApiResponse, PaginatedResponse } from '../types'

export interface AuditLogRow {
  id: number
  username?: string
  userId?: number
  httpMethod?: string
  requestUri?: string
  action?: string
  success: boolean
  detail?: string
  clientIp?: string
  createdAt?: string
}

export class AuditLogService {
  static async list(page = 0, size = 20): Promise<PaginatedResponse<AuditLogRow>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<AuditLogRow>>>('/audit-logs', {
        params: { page, size },
      })
      return response.data
    } catch (error) {
      throw handleApiError(error, '获取审计日志失败')
    }
  }
}
