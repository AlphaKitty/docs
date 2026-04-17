package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "english_name", length = 100)
    private String englishName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @Column(name = "proficiency_levels", length = 200)
    private String proficiencyLevels;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "expert_count")
    @Builder.Default
    private Integer expertCount = 0;

    @Column(name = "demand_level", length = 20)
    private String demandLevel;

    @ManyToMany
    @JoinTable(
            name = "skill_tags",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "skill_related_skills",
            joinColumns = @JoinColumn(name = "skill_id"),
            inverseJoinColumns = @JoinColumn(name = "related_skill_id")
    )
    @JsonIgnoreProperties({"relatedSkills", "relatedToSkills", "tags", "documentation", "experts"})
    @Builder.Default
    private Set<Skill> relatedSkills = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "relatedSkills")
    @JsonIgnore
    @Builder.Default
    private Set<Skill> relatedToSkills = new LinkedHashSet<>();

    @OneToOne(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"skill", "hibernateLazyInitializer", "handler"})
    private SkillDocumentation documentation;

    // Relationships
    @ManyToMany(mappedBy = "skills")
    @JsonIgnore
    @Builder.Default
    private Set<Expert> experts = new HashSet<>();

    // Helper methods
    public void incrementExpertCount() {
        if (this.expertCount == null) {
            this.expertCount = 0;
        }
        this.expertCount++;
    }

    public void decrementExpertCount() {
        if (this.expertCount == null || this.expertCount <= 0) {
            this.expertCount = 0;
        } else {
            this.expertCount--;
        }
    }
}