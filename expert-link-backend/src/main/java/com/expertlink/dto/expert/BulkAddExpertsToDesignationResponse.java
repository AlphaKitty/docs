package com.expertlink.dto.expert;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkAddExpertsToDesignationResponse {
    private int requestedCount;
    private int createdCount;
    private int skippedCount;
    private List<Long> createdExpertIds;
    private List<Long> skippedOwnerIds;
}
