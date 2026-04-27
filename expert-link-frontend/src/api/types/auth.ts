/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * 登录响应（与后端 LoginResponse 对齐）
 */
export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresInMs: number
  userId: number
  username: string
  roles: string[]
}

/**
 * GET /auth/me
 */
export interface AuthMe {
  userId: number
  username: string
  email: string
  fullName: string
  roles: string[]
  /** 积分余额（后端 BigDecimal，JSON 为 number） */
  pointsBalance?: number | null
}

export interface UserProfile {
  userId: number
  username: string
  email: string
  fullName: string
  roles: string[]
  pointsBalance?: number | null
  phoneNumber?: string | null
  bio?: string | null
  avatar?: string | null
  expertProfile?: ExpertProfile | null
}

export interface UpdateMyProfileRequest {
  phoneNumber?: string
  bio?: string
  avatar?: string
  expertProfile?: UpdateMyExpertProfileRequest
}

export interface ExpertProfile {
  expertId: number
  name?: string | null
  currentPosition?: string | null
  currentCompany?: string | null
  wechatId?: string | null
  yearsOfExperience?: number | null
  hourlyRate?: number | null
  availabilityStatus?: string | null
  biography?: string | null
}

export interface UpdateMyExpertProfileRequest {
  currentPosition?: string
  currentCompany?: string
  wechatId?: string
  yearsOfExperience?: number
  hourlyRate?: number
  availabilityStatus?: 'AVAILABLE' | 'UNAVAILABLE'
  biography?: string
}

/**
 * 注册请求（预留）
 */
export interface RegisterRequest {
  username: string
  password: string
  email: string
  confirmPassword: string
}

/**
 * 修改密码请求（预留）
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/**
 * 重置密码请求（预留）
 */
export interface ResetPasswordRequest {
  email: string
  token: string
  newPassword: string
}
