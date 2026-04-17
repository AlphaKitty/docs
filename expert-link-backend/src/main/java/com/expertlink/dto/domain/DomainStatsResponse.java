package com.expertlink.dto.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record DomainStatsResponse(
        Long domainId,
        int subtreeDomainCount,
        long expertCount,
        long projectCount,
        List<Long> subtreeDomainIds
) {
}

