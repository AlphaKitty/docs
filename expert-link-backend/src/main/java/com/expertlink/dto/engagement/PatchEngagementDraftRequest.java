package com.expertlink.dto.engagement;

import com.expertlink.domain.EngagementMode;
import com.expertlink.domain.EngagementTaskType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PatchEngagementDraftRequest {

    private Long domainId;
    private EngagementMode mode;
    private String applyCategory;
    private String pointsCategory;
    private String pointsItem;
    private EngagementTaskType taskType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String taskDescription;
    private List<Long> designatedExpertIds;
}
