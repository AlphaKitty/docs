package com.expertlink.dto.settings;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateMenuPermissionsRequest {
    private Map<String, List<String>> roleMenus;
}

