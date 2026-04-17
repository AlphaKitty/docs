package com.expertlink.dto.points;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointsLedgerEntryResponse(
        Long id,
        BigDecimal pointsDelta,
        BigDecimal balanceAfter,
        String reasonCode,
        Long engagementRequestId,
        LocalDateTime createdAt
) {
}
