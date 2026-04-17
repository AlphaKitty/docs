package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReassignEngagementRequest {

    @NotNull
    private Long expertId;

    /** 改派原因 */
    private String reason;
}
