package com.expertlink.dto.settings;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record MenuPermissionsResponse(
        List<String> menuKeys,
        Map<String, List<String>> roleMenus
) {
}

