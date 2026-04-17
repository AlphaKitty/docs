package com.expertlink.dto.engagement;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReassignmentLogEntryResponse(
        Long fromExpertId,
        String fromExpertName,
        Long toExpertId,
        String toExpertName,
        Long byStewardId,
        String reason,
        LocalDateTime at
) {
}
