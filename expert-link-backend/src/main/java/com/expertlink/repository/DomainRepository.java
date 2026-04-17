package com.expertlink.repository;

import com.expertlink.domain.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
    
    Optional<Domain> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Domain> findByParentId(Long parentId);
    
    List<Domain> findByLevel(Integer level);
    
    List<Domain> findByIsActive(Boolean isActive);
    
    Page<Domain> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT d FROM Domain d WHERE d.parent IS NULL")
    List<Domain> findRootDomains();
    
    @Query("SELECT d FROM Domain d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Domain> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(d) FROM Domain d")
    long countAllDomains();
    
    @Query("SELECT COUNT(d) FROM Domain d WHERE d.parent IS NOT NULL")
    long countSubDomains();
    
    @Query("SELECT d FROM Domain d WHERE d.parent IS NULL")
    Page<Domain> findRootDomainsPage(Pageable pageable);
    
    @Query("SELECT d FROM Domain d WHERE d.parent.id = :parentId")
    Page<Domain> findByParentIdPage(@Param("parentId") Long parentId, Pageable pageable);
}