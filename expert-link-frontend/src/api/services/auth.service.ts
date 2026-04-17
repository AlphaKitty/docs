import { apiClient, handleApiError } from '../utils'
import type { LoginRequest, LoginResponse, AuthMe, ApiResponse } from '../types'

/**
 * 认证服务（P0：登录 / 当前用户）
 */
export class AuthService {
  static async login(credentials: LoginRequest): Promise<LoginResponse> {
    try {
      const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', credentials)
      return response.data
    } catch (error) {
      throw handleApiError(error, '登录失败')
    }
  }

  static async getCurrentUser(): Promise<AuthMe> {
    try {
      const response = await apiClient.get<ApiResponse<AuthMe>>('/auth/me')
      return response.data
    } catch (error) {
      throw handleApiError(error, '获取用户信息失败')
    }
  }
}
