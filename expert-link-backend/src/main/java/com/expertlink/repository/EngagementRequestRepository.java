package com.expertlink.repository;

import com.expertlink.domain.EngagementRequest;
import com.expertlink.domain.EngagementRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface EngagementRequestRepository extends JpaRepository<EngagementRequest, Long> {

    Page<EngagementRequest> findByApplicant_IdOrderByCreatedAtDesc(Long applicantId, Pageable pageable);

    Page<EngagementRequest> findByStatusOrderByCreatedAtDesc(EngagementRequestStatus status, Pageable pageable);

    Page<EngagementRequest> findByStatusInOrderByCreatedAtDesc(Collection<EngagementRequestStatus> statuses, Pageable pageable);

    @Query("""
            select distinct e from EngagementRequest e
            join e.assignedExperts ae
            where ae.id = :expertId and e.status = :status
            order by e.createdAt desc
            """)
    Page<EngagementRequest> findByAssignedExpertMemberAndStatusOrderByCreatedAtDesc(
            @Param("expertId") Long expertId,
            @Param("status") EngagementRequestStatus status,
            Pageable pageable);

    @Query("""
            select distinct e from EngagementRequest e
            join e.domain d
            join d.stewards st
            where st.id = :userId and e.status in :statuses
            """)
    Page<EngagementRequest> findPendingForSteward(
            @Param("userId") Long userId,
            @Param("statuses") Collection<EngagementRequestStatus> statuses,
            Pageable pageable);
}
