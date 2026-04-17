package com.expertlink.repository;

import com.expertlink.domain.SkillDocumentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillDocumentationRepository extends JpaRepository<SkillDocumentation, Long> {
}
