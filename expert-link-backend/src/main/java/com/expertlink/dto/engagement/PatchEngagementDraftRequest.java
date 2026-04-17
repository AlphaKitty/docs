package com.expertlink.dto.engagement;

import com.expertlink.domain.EngagementMode;
import com.expertlink.domain.EngagementTaskType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatchEngagementDraftRequest {

    private Long domainId;
    private EngagementMode mode;
    private EngagementTaskType taskType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String taskDescription;
    private Long designatedExpertId;
}
