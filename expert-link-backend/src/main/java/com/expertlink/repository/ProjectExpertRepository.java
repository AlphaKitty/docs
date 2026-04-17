package com.expertlink.repository;

import com.expertlink.domain.ProjectExpert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectExpertRepository extends JpaRepository<ProjectExpert, Long> {
    
    List<ProjectExpert> findByProjectId(Long projectId);
    
    List<ProjectExpert> findByExpertId(Long expertId);
    
    Optional<ProjectExpert> findByProjectIdAndExpertId(Long projectId, Long expertId);
    
    List<ProjectExpert> findByStatus(String status);
    
    List<ProjectExpert> findByProjectIdAndStatus(Long projectId, String status);
    
    List<ProjectExpert> findByExpertIdAndStatus(Long expertId, String status);
    
    List<ProjectExpert> findByRole(String role);
    
    List<ProjectExpert> findByProjectIdAndRole(Long projectId, String role);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.isLead = true")
    List<ProjectExpert> findAllLeadAssignments();
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.isPrimaryContact = true")
    List<ProjectExpert> findAllPrimaryContactAssignments();
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.isLead = true AND pe.project.id = :projectId")
    List<ProjectExpert> findLeadAssignmentsByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.isPrimaryContact = true AND pe.project.id = :projectId")
    List<ProjectExpert> findPrimaryContactAssignmentsByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.startDate >= :startDate AND pe.endDate <= :endDate")
    List<ProjectExpert> findAssignmentsBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.project.id = :projectId AND " +
           "pe.startDate >= :startDate AND pe.endDate <= :endDate")
    List<ProjectExpert> findAssignmentsByProjectIdBetweenDates(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.expert.id = :expertId AND " +
           "pe.startDate >= :startDate AND pe.endDate <= :endDate")
    List<ProjectExpert> findAssignmentsByExpertIdBetweenDates(
            @Param("expertId") Long expertId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE " +
           "pe.hourlyRate >= :minRate AND pe.hourlyRate <= :maxRate")
    List<ProjectExpert> findByHourlyRateRange(
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.performanceRating >= :minRating")
    List<ProjectExpert> findByMinPerformanceRating(@Param("minRating") BigDecimal minRating);
    
    @Query("SELECT COUNT(pe) FROM ProjectExpert pe WHERE pe.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(pe) FROM ProjectExpert pe WHERE pe.expert.id = :expertId")
    long countByExpertId(@Param("expertId") Long expertId);
    
    @Query("SELECT COUNT(pe) FROM ProjectExpert pe WHERE pe.project.id = :projectId AND pe.status = :status")
    long countByProjectIdAndStatus(
            @Param("projectId") Long projectId,
            @Param("status") String status);
    
    @Query("SELECT COUNT(pe) FROM ProjectExpert pe WHERE pe.expert.id = :expertId AND pe.status = :status")
    long countByExpertIdAndStatus(
            @Param("expertId") Long expertId,
            @Param("status") String status);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE " +
           "pe.status IN :statuses ORDER BY pe.startDate DESC")
    List<ProjectExpert> findByStatusIn(@Param("statuses") List<String> statuses);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.completionPercentage >= :minPercentage")
    List<ProjectExpert> findByMinCompletionPercentage(@Param("minPercentage") Integer minPercentage);
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.hoursLogged > 0")
    List<ProjectExpert> findWithHoursLogged();
    
    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.expert.id = :expertId AND pe.status IN ('ACTIVE', 'IN_PROGRESS')")
    List<ProjectExpert> findActiveAssignmentsByExpertId(@Param("expertId") Long expertId);

    @Query("SELECT pe FROM ProjectExpert pe WHERE pe.project.id = :projectId AND pe.status IN ('ACTIVE', 'IN_PROGRESS')")
    List<ProjectExpert> findActiveAssignmentsByProjectId(@Param("projectId") Long projectId);
    
    boolean existsByProjectIdAndExpertId(Long projectId, Long expertId);
}