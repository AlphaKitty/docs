import { apiClient, handleApiError } from '../utils'
import type { ApiResponse } from '../types'
import type {
  MenuPermissionsResponse,
  RoleGroupsResponse,
  UpdateMenuPermissionsRequest,
  UpdateRoleGroupsRequest,
} from '../types/settings'

function unwrap<T>(response: ApiResponse<T>): T {
  return response.data
}

export class SettingsService {
  static async getRoleGroups(): Promise<RoleGroupsResponse> {
    try {
      const response = await apiClient.get<ApiResponse<RoleGroupsResponse>>('/settings/role-groups')
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取角色策略失败')
    }
  }

  static async updateRoleGroups(payload: UpdateRoleGroupsRequest): Promise<RoleGroupsResponse> {
    try {
      const response = await apiClient.put<ApiResponse<RoleGroupsResponse>>('/settings/role-groups', payload)
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '保存角色策略失败')
    }
  }

  static async getMenuPermissions(): Promise<MenuPermissionsResponse> {
    try {
      const response = await apiClient.get<ApiResponse<MenuPermissionsResponse>>('/settings/menu-permissions')
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '获取菜单授权失败')
    }
  }

  static async updateMenuPermissions(payload: UpdateMenuPermissionsRequest): Promise<MenuPermissionsResponse> {
    try {
      const response = await apiClient.put<ApiResponse<MenuPermissionsResponse>>('/settings/menu-permissions', payload)
      return unwrap(response)
    } catch (e) {
      throw handleApiError(e, '保存菜单授权失败')
    }
  }
}

