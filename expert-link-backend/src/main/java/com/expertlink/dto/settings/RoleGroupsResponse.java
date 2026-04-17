package com.expertlink.dto.settings;

import lombok.Builder;

import java.util.List;

@Builder
public record RoleGroupsResponse(
        List<String> steward,
        List<String> expert,
        List<String> superAdmin
) {
}

