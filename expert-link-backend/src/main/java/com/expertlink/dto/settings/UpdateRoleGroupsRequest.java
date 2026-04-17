package com.expertlink.dto.settings;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleGroupsRequest {
    private List<String> steward;
    private List<String> expert;
    private List<String> superAdmin;
}

