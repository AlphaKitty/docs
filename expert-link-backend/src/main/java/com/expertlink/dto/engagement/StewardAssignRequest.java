package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StewardAssignRequest {

    @NotEmpty
    private List<Long> expertIds;

    private String assignmentNote;
}
