package com.expertlink.dto.expert;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkAddExpertsByDomainResponse(
        int requestedCount,
        int createdCount,
        int skippedCount,
        List<Long> createdExpertIds,
        List<Long> skippedOwnerIds
) {
}

