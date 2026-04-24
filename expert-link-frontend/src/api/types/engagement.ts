import type { PaginatedResponse } from './common'

export type EngagementStatus =
  | 'DRAFT'
  | 'PENDING_STEWARD_ASSIGN'
  | 'PENDING_EXPERT_CONFIRM'
  | 'IN_PROGRESS'
  | 'PENDING_STEWARD_SCORE_RELEASE'
  | 'COMPLETED'
  | 'REJECTED'
  | 'CANCELLED'

export type EngagementMode = 'NAMED' | 'STEWARD_ASSIGN'

export type EngagementTaskType = 'PROBLEM_SOLVING' | 'REVIEW' | 'KNOWLEDGE_MANAGEMENT'

export interface ReassignmentLogEntry {
  fromExpertId?: number | null
  fromExpertName?: string | null
  toExpertId?: number | null
  toExpertName?: string | null
  byStewardId?: number | null
  reason?: string | null
  at?: string | null
}

export interface EngagementRequestRow {
  id: number
  referenceCode?: string | null
  status: EngagementStatus
  mode: EngagementMode
  taskType: EngagementTaskType
  domainId: number
  domainName: string
  domainStewardIds?: number[] | null
  domainStewardNames?: string[] | null
  applicantId: number
  applicantUsername: string
  startAt: string
  endAt?: string | null
  taskDescription?: string | null
  designatedExpertIds?: number[] | null
  designatedExpertNames?: string[] | null
  designatedExpertId?: number | null
  designatedExpertName?: string | null
  assignedExpertIds?: number[] | null
  assignedExpertNames?: string[] | null
  viewerExpertConfirmPending?: boolean | null
  viewerAmongAssignedExperts?: boolean | null
  assignedExpertId?: number | null
  assignedExpertName?: string | null
  assignedByStewardId?: number | null
  assignmentNote?: string | null
  assignedAt?: string | null
  expertAccepted?: boolean | null
  expertResponseNote?: string | null
  expertRespondedAt?: string | null
  evalProfessional?: number | null
  evalTimeliness?: number | null
  evalAttitude?: number | null
  evalResolved?: boolean | null
  evalComment?: string | null
  evaluationSubmittedAt?: string | null
  suggestedScore?: number | null
  evaluationAttachmentUrls?: string[] | null
  evaluationRevisionNote?: string | null
  rollbackNote?: string | null
  rolledBackAt?: string | null
  reassignmentLog?: ReassignmentLogEntry[] | null
  cancelReason?: string | null
  cancelledAt?: string | null
  stewardFinalScore?: number | null
  stewardReleaseNote?: string | null
  completedAt?: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateEngagementDraftPayload {
  domainId: number
  mode: EngagementMode
  taskType: EngagementTaskType
  startAt: string
  endAt?: string | null
  taskDescription?: string | null
  designatedExpertIds?: number[] | null
}

export type PaginatedEngagements = PaginatedResponse<EngagementRequestRow>
