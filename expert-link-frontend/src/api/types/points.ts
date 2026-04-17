import type { PaginatedResponse } from './common'

export interface PointsLedgerEntryRow {
  id: number
  pointsDelta: number
  balanceAfter: number
  reasonCode: string
  engagementRequestId?: number | null
  createdAt: string
}

export type PaginatedPointsLedger = PaginatedResponse<PointsLedgerEntryRow>
