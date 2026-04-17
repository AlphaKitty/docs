package com.expertlink.repository;

import com.expertlink.domain.ExpertDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpertDesignationRepository extends JpaRepository<ExpertDesignation, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<ExpertDesignation> findAllByOrderByNameAsc();
}
