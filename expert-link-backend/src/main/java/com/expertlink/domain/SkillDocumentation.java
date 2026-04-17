package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_documentation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDocumentation extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "skill_id", nullable = false, unique = true)
    @JsonIgnore
    private Skill skill;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "document_url", length = 500)
    private String url;
}
