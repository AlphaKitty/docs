package com.expertlink.service;

import com.expertlink.domain.Domain;
import com.expertlink.domain.EngagementMode;
import com.expertlink.domain.EngagementRequest;
import com.expertlink.domain.EngagementRequestStatus;
import com.expertlink.domain.EngagementTaskType;
import com.expertlink.domain.Expert;
import com.expertlink.domain.User;
import com.expertlink.dto.engagement.ExpertDecisionRequest;
import com.expertlink.dto.engagement.EngagementRequestResponse;
import com.expertlink.repository.EngagementRequestRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.UserRepository;
import com.expertlink.security.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EngagementRequestServiceTest {

    private final EngagementRequestRepository engagementRequestRepository = mock(EngagementRequestRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ExpertRepository expertRepository = mock(ExpertRepository.class);
    private final PointsService pointsService = mock(PointsService.class);

    private final EngagementRequestService service = new EngagementRequestService(
            engagementRequestRepository,
            userRepository,
            domainService,
            expertRepository,
            new ObjectMapper(),
            pointsService
    );

    @Test
    void expertRejectShouldReturnToPendingStewardAssign() {
        Long userId = 100L;
        Long requestId = 10L;
        Long expertId = 200L;

        User expertUser = User.builder().username("expert-user").roles(Set.of(UserRole.EXPERT_USER.name())).build();
        expertUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expertUser));

        User applicant = User.builder().username("applicant").roles(Set.of(UserRole.REGULAR_USER.name())).build();
        applicant.setId(300L);

        Domain domain = Domain.builder().name("测试领域").build();
        domain.setId(1L);

        Expert assignedExpert = Expert.builder().name("专家A").build();
        assignedExpert.setId(expertId);

        Expert ownerExpert = Expert.builder().name("专家A").build();
        ownerExpert.setId(expertId);

        EngagementRequest request = EngagementRequest.builder()
                .status(EngagementRequestStatus.PENDING_EXPERT_CONFIRM)
                .mode(EngagementMode.STEWARD_ASSIGN)
                .taskType(EngagementTaskType.PROBLEM_SOLVING)
                .domain(domain)
                .applicant(applicant)
                .startAt(LocalDateTime.now())
                .assignedExperts(new LinkedHashSet<>(Set.of(assignedExpert)))
                .assignmentExpertDecisions("[{\"expertId\":200,\"accepted\":null,\"note\":null,\"at\":null}]")
                .build();
        request.setId(requestId);

        when(engagementRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(expertRepository.findByOwnerId(userId)).thenReturn(Optional.of(ownerExpert));
        when(engagementRequestRepository.save(any(EngagementRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpertDecisionRequest dto = new ExpertDecisionRequest();
        dto.setAccepted(false);
        dto.setNote("时间冲突，无法参与");

        EngagementRequestResponse response = service.expertDecision(userId, requestId, dto);

        assertEquals(EngagementRequestStatus.PENDING_STEWARD_ASSIGN.name(), response.status());
        assertFalse(Boolean.TRUE.equals(response.expertAccepted()));
    }

    @Test
    void expertRejectShouldRequireNote() {
        Long userId = 100L;
        Long requestId = 11L;
        Long expertId = 201L;

        User expertUser = User.builder().username("expert-user").roles(Set.of(UserRole.EXPERT_USER.name())).build();
        expertUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expertUser));

        User applicant = User.builder().username("applicant").roles(Set.of(UserRole.REGULAR_USER.name())).build();
        applicant.setId(301L);

        Domain domain = Domain.builder().name("测试领域").build();
        domain.setId(1L);

        Expert assignedExpert = Expert.builder().name("专家B").build();
        assignedExpert.setId(expertId);
        Expert ownerExpert = Expert.builder().name("专家B").build();
        ownerExpert.setId(expertId);

        EngagementRequest request = EngagementRequest.builder()
                .status(EngagementRequestStatus.PENDING_EXPERT_CONFIRM)
                .mode(EngagementMode.STEWARD_ASSIGN)
                .taskType(EngagementTaskType.PROBLEM_SOLVING)
                .domain(domain)
                .applicant(applicant)
                .startAt(LocalDateTime.now())
                .assignedExperts(new LinkedHashSet<>(Set.of(assignedExpert)))
                .assignmentExpertDecisions("[{\"expertId\":201,\"accepted\":null,\"note\":null,\"at\":null}]")
                .build();
        request.setId(requestId);

        when(engagementRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(expertRepository.findByOwnerId(userId)).thenReturn(Optional.of(ownerExpert));

        ExpertDecisionRequest dto = new ExpertDecisionRequest();
        dto.setAccepted(false);
        dto.setNote("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.expertDecision(userId, requestId, dto));
        assertEquals("拒绝时请填写备注说明", ex.getMessage());
    }
}
