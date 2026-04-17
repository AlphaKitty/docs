package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StewardAssignRequest {

    @NotNull
    private Long expertId;

    private String assignmentNote;
}
