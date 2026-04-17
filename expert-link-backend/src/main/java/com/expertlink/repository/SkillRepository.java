package com.expertlink.repository;

import com.expertlink.domain.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    
    Optional<Skill> findByName(String name);
    
    Optional<Skill> findByEnglishName(String englishName);
    
    List<Skill> findByCategory(String category);
    
    List<Skill> findByDomainId(Long domainId);
    
    List<Skill> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM Skill s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.englishName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Skill> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT s FROM Skill s WHERE s.domain.id = :domainId AND s.isActive = true")
    List<Skill> findActiveSkillsByDomainId(@Param("domainId") Long domainId);
    
    @Query("SELECT s FROM Skill s WHERE s.demandLevel = :demandLevel")
    List<Skill> findByDemandLevel(@Param("demandLevel") String demandLevel);

    Page<Skill> findByDemandLevel(String demandLevel, Pageable pageable);
    
    @Query("SELECT s FROM Skill s WHERE s.expertCount >= :minExpertCount")
    List<Skill> findByMinExpertCount(@Param("minExpertCount") Integer minExpertCount);
    
    @Query("SELECT s FROM Skill s WHERE s.expertCount BETWEEN :minCount AND :maxCount")
    List<Skill> findByExpertCountRange(
            @Param("minCount") Integer minCount,
            @Param("maxCount") Integer maxCount);
    
    @Query("SELECT s FROM Skill s ORDER BY s.expertCount DESC")
    Page<Skill> findTopSkillsByExpertCount(Pageable pageable);
    
    @Query("SELECT s FROM Skill s WHERE s.category IN :categories")
    List<Skill> findByCategories(@Param("categories") List<String> categories);
    
    @Query("SELECT COUNT(s) FROM Skill s WHERE s.isActive = true")
    long countActiveSkills();
    
    @Query("SELECT s FROM Skill s WHERE s.isActive = true ORDER BY s.displayOrder ASC, s.name ASC")
    List<Skill> findAllActiveSkillsOrdered();
    
    boolean existsByName(String name);
    
    boolean existsByEnglishName(String englishName);
}