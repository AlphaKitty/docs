package com.expertlink.repository;

import com.expertlink.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // 基础查询方法
    Optional<Project> findByProjectCode(String projectCode);
    
    List<Project> findByNameContainingIgnoreCase(String name);
    
    Page<Project> findByProjectType(String projectType, Pageable pageable);
    
    Page<Project> findByClientNameContainingIgnoreCase(String clientName, Pageable pageable);
    
    Page<Project> findByStatus(String status, Pageable pageable);
    
    List<Project> findByStatusAndEndDateBefore(String status, LocalDateTime endDate);
    
    // 复合查询
    @Query("SELECT p FROM Project p WHERE p.domain.id = :domainId")
    List<Project> findByDomainId(@Param("domainId") Long domainId);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.domain.id IN :domainIds")
    long countByDomainIds(@Param("domainIds") Collection<Long> domainIds);
    
    @Query("SELECT p FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Project> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.clientName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.projectType) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Project p WHERE p.budget BETWEEN :minBudget AND :maxBudget")
    List<Project> findByBudgetBetween(
            @Param("minBudget") BigDecimal minBudget,
            @Param("maxBudget") BigDecimal maxBudget);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.endDate < :deadline")
    List<Project> findOverdueProjects(
            @Param("status") String status,
            @Param("deadline") LocalDateTime deadline);
    
    @Query("SELECT p FROM Project p JOIN p.projectExperts pe WHERE pe.expert.id = :expertId")
    Page<Project> findByExpertId(@Param("expertId") Long expertId, Pageable pageable);
    
    @Query("SELECT p FROM Project p JOIN p.projectExperts pe WHERE pe.expert.id = :expertId AND p.status = :status")
    List<Project> findByExpertIdAndStatus(
            @Param("expertId") Long expertId,
            @Param("status") String status);
    
    @Query("SELECT p FROM Project p WHERE p.status IN :statuses")
    List<Project> findByStatusIn(@Param("statuses") List<String> statuses);
    
    // 统计查询
    @Query("SELECT SUM(p.budget) FROM Project p WHERE p.status = :status")
    BigDecimal sumBudgetByStatus(@Param("status") String status);
    
    @Query("SELECT AVG(p.completionPercentage) FROM Project p WHERE p.status = :status")
    Double avgCompletionPercentageByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status AND p.priority = :priority")
    long countByStatusAndPriority(
            @Param("status") String status,
            @Param("priority") String priority);
    
    // 基于时间的查询
    @Query("SELECT p FROM Project p WHERE p.endDate BETWEEN :startDate AND :endDate")
    List<Project> findByEndDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // 复杂度和风险级别查询
    List<Project> findByComplexityLevel(String complexityLevel);
    
    List<Project> findByRiskLevel(String riskLevel);
    
    // 项目负责人查询
    Page<Project> findByProjectManagerContainingIgnoreCase(String projectManager, Pageable pageable);
    
    Page<Project> findByTechnicalLeadContainingIgnoreCase(String technicalLead, Pageable pageable);
}