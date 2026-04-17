import { apiClient, handleApiError } from '../utils'
import type { ApiResponse, PaginatedResponse } from '../types'

export interface AdminUserRow {
  id: number
  username: string
  email: string
  fullName?: string
  roles?: string[]
  isActive?: boolean
}

export interface CreateUserPayload {
  username: string
  email: string
  password: string
  fullName?: string
  roles: string[]
}

/**
 * 用户管理（需 SUPER_ADMIN）
 */
export class UserAdminService {
  static async getUsers(page = 0, size = 100): Promise<PaginatedResponse<AdminUserRow>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<AdminUserRow>>>('/users', {
        params: { page, size },
      })
      return response.data
    } catch (error) {
      throw handleApiError(error, '获取用户列表失败')
    }
  }

  static async createUser(data: CreateUserPayload): Promise<AdminUserRow> {
    try {
      const response = await apiClient.post<ApiResponse<AdminUserRow>>('/users', data)
      return response.data
    } catch (error) {
      throw handleApiError(error, '创建用户失败')
    }
  }

  static async replaceRoles(userId: number, roles: string[]): Promise<AdminUserRow> {
    try {
      const response = await apiClient.put<ApiResponse<AdminUserRow>>(`/users/${userId}/roles`, { roles })
      return response.data
    } catch (error) {
      throw handleApiError(error, '更新角色失败')
    }
  }

  static async deleteUser(userId: number): Promise<void> {
    try {
      await apiClient.delete<ApiResponse<void>>(`/users/${userId}`)
    } catch (error) {
      throw handleApiError(error, '删除用户失败')
    }
  }
}
