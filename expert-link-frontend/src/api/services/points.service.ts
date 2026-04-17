import { apiClient, handleApiError } from '../utils'
import type { ApiResponse, PaginatedResponse } from '../types'
import type { PaginatedPointsLedger, PointsLedgerEntryRow } from '../types/points'

function unwrap<T>(body: ApiResponse<T>): T {
  return body.data
}

export class PointsService {
  static async myLedger(page = 0, size = 20): Promise<PaginatedPointsLedger> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<PointsLedgerEntryRow>>>(
        '/points/ledger',
        { params: { page, size } }
      )
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取积分流水失败')
    }
  }
}
