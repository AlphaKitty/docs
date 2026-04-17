package com.expertlink.repository;

import com.expertlink.domain.Expert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {

    boolean existsByOwner_Id(Long ownerId);
    
    // 根据邮箱查找专家
    Optional<Expert> findByEmail(String email);
    
    // 根据姓名模糊查找（不区分大小写）
    List<Expert> findByNameContainingIgnoreCase(String name);
    
    // 根据所有者ID查找专家
    @Query("SELECT e FROM Expert e WHERE e.owner.id = :ownerId")
    Optional<Expert> findByOwnerId(@Param("ownerId") Long ownerId);
    
    // 根据主要领域ID查找专家
    @Query("SELECT e FROM Expert e WHERE e.primaryDomain.id = :domainId")
    List<Expert> findByPrimaryDomainId(@Param("domainId") Long domainId);

    // 根据领域查找专家：同时覆盖主领域与多对多关联领域，去重
    @Query("""
            SELECT DISTINCT e
            FROM Expert e
            LEFT JOIN e.domains d
            WHERE e.primaryDomain.id = :domainId OR d.id = :domainId
            """)
    List<Expert> findByDomainId(@Param("domainId") Long domainId);
    
    // 根据技能ID查找专家（通过关联表）
    @Query("SELECT e FROM Expert e JOIN e.skills s WHERE s.id = :skillId")
    List<Expert> findBySkillId(@Param("skillId") Long skillId);

    @Query("""
            SELECT COUNT(DISTINCT e.id)
            FROM Expert e
            LEFT JOIN e.domains d
            WHERE e.primaryDomain.id IN :domainIds OR d.id IN :domainIds
            """)
    long countDistinctByAnyDomainIds(@Param("domainIds") Collection<Long> domainIds);
    
    // 根据可用状态查找专家（分页）
    Page<Expert> findByAvailabilityStatus(String availabilityStatus, Pageable pageable);
    
    // 综合关键词搜索（姓名、英文名、职位、公司、简介、研究方向）
    @Query("SELECT e FROM Expert e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.englishName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.currentPosition) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.currentCompany) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.biography) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.researchInterests) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Expert> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 根据小时费率范围查找专家
    @Query("SELECT e FROM Expert e WHERE e.hourlyRate BETWEEN :minRate AND :maxRate")
    List<Expert> findByHourlyRateBetween(
            @Param("minRate") BigDecimal minRate,
            @Param("maxRate") BigDecimal maxRate);
    
    // 统计可用专家数量（availabilityStatus = 'AVAILABLE'）
    @Query("SELECT COUNT(e) FROM Expert e WHERE e.availabilityStatus = 'AVAILABLE'")
    long countAvailableExperts();
    
    // 获取评分最高的专家（分页）
    @Query("SELECT e FROM Expert e WHERE e.overallRating IS NOT NULL ORDER BY e.overallRating DESC")
    List<Expert> findTopRatedExperts(Pageable pageable);
    
    // 根据验证状态查找专家
    Page<Expert> findByIsVerified(Boolean isVerified, Pageable pageable);
    
    // 根据验证等级查找专家
    List<Expert> findByVerificationLevel(Integer verificationLevel);
    
    // 根据国籍查找专家
    List<Expert> findByNationality(String nationality);
    
    // 根据最高学历查找专家
    List<Expert> findByHighestDegree(String highestDegree);
}