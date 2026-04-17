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
