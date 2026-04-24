package com.expertlink.dto.engagement;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EngagementRequestResponse(
        Long id,
        String referenceCode,
        String status,
        String mode,
        String taskType,
        Long domainId,
        String domainName,
        List<Long> domainStewardIds,
        List<String> domainStewardNames,
        Long applicantId,
        String applicantUsername,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String taskDescription,
        List<Long> designatedExpertIds,
        List<String> designatedExpertNames,
        Long designatedExpertId,
        String designatedExpertName,
        List<Long> assignedExpertIds,
        List<String> assignedExpertNames,
        Boolean viewerExpertConfirmPending,
        Boolean viewerAmongAssignedExperts,
        Long assignedExpertId,
        String assignedExpertName,
        Long assignedByStewardId,
        String assignmentNote,
        LocalDateTime assignedAt,
        Boolean expertAccepted,
        String expertResponseNote,
        LocalDateTime expertRespondedAt,
        Integer evalProfessional,
        Integer evalTimeliness,
        Integer evalAttitude,
        Boolean evalResolved,
        String evalComment,
        LocalDateTime evaluationSubmittedAt,
        BigDecimal suggestedScore,
        List<String> evaluationAttachmentUrls,
        String evaluationRevisionNote,
        List<ReassignmentLogEntryResponse> reassignmentLog,
        BigDecimal stewardFinalScore,
        String stewardReleaseNote,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
