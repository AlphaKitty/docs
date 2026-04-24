package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReassignEngagementRequest {

    @NotEmpty
    private List<Long> expertIds;

    /** 改派原因 */
    private String reason;
}
