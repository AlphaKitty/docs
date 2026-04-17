export interface RoleGroupsResponse {
  steward: string[]
  expert: string[]
  superAdmin: string[]
}

export interface UpdateRoleGroupsRequest {
  steward: string[]
  expert: string[]
  superAdmin: string[]
}

export interface MenuPermissionsResponse {
  menuKeys: string[]
  roleMenus: Record<string, string[]>
}

export interface UpdateMenuPermissionsRequest {
  roleMenus: Record<string, string[]>
}

