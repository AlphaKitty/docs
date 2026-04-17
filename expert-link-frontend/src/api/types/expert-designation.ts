export interface ExpertDesignation {
  id: number
  name: string
  description?: string | null
}

export interface CreateExpertDesignationBody {
  name: string
  description?: string
}

export interface BatchAddExpertsToDesignationBody {
  ownerIds: number[]
}

export interface BatchAddExpertsToDesignationResult {
  requestedCount: number
  createdCount: number
  skippedCount: number
  createdExpertIds: number[]
  skippedOwnerIds: number[]
}
