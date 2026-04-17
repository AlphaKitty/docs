package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.settings.MenuPermissionsResponse;
import com.expertlink.dto.settings.RoleGroupsResponse;
import com.expertlink.dto.settings.UpdateMenuPermissionsRequest;
import com.expertlink.dto.settings.UpdateRoleGroupsRequest;
import com.expertlink.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping("/role-groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RoleGroupsResponse>> getRoleGroups() {
        return ApiResponses.ok(systemSettingService.getRoleGroups());
    }

    @PutMapping("/role-groups")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleGroupsResponse>> updateRoleGroups(
            @RequestBody(required = false) UpdateRoleGroupsRequest body) {
        UpdateRoleGroupsRequest req = body != null ? body : new UpdateRoleGroupsRequest();
        return ApiResponses.ok(systemSettingService.updateRoleGroups(req));
    }

    @GetMapping("/menu-permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MenuPermissionsResponse>> getMenuPermissions() {
        return ApiResponses.ok(systemSettingService.getMenuPermissions());
    }

    @PutMapping("/menu-permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<MenuPermissionsResponse>> updateMenuPermissions(
            @RequestBody(required = false) UpdateMenuPermissionsRequest body) {
        UpdateMenuPermissionsRequest req = body != null ? body : new UpdateMenuPermissionsRequest();
        return ApiResponses.ok(systemSettingService.updateMenuPermissions(req));
    }
}

