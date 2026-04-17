package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "experts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expert extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "english_name", length = 100)
    private String englishName;

    @Column(length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Column(length = 50)
    private String nationality;

    @Column(name = "highest_degree", length = 50)
    private String highestDegree;

    @Column(name = "graduation_school", length = 200)
    private String graduationSchool;

    @Column(name = "major_field", length = 200)
    private String majorField;

    @Column(name = "current_position", length = 200)
    private String currentPosition;

    @Column(name = "current_company", length = 200)
    private String currentCompany;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "research_interests", columnDefinition = "TEXT")
    private String researchInterests;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "project_rate", precision = 10, scale = 2)
    private BigDecimal projectRate;

    @Column(name = "availability_status", length = 50)
    private String availabilityStatus;

    @Column(name = "preferred_contact_method", length = 50)
    private String preferredContactMethod;

    @Column(length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "wechat_id", length = 100)
    private String wechatId;

    @Column(length = 500)
    private String avatar;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_level")
    private Integer verificationLevel = 0;

    @Column(name = "overall_rating", precision = 3, scale = 2)
    private BigDecimal overallRating;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "project_count")
    private Integer projectCount = 0;

    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;

    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_domain_id")
    private Domain primaryDomain;

    /** 专家对外称谓/岗位（从「称谓库」选择，与系统用户账号解耦展示用） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ExpertDesignation designation;

    @ManyToMany
    @JoinTable(
        name = "expert_skills",
        joinColumns = @JoinColumn(name = "expert_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "expert_domains",
        joinColumns = @JoinColumn(name = "expert_id"),
        inverseJoinColumns = @JoinColumn(name = "domain_id")
    )
    @Builder.Default
    private Set<Domain> domains = new HashSet<>();

    @OneToMany(mappedBy = "expert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<ProjectExpert> projectExperiences = new HashSet<>();

    @OneToMany(mappedBy = "expert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Review> reviews = new HashSet<>();

    // Helper methods
    public void addSkill(Skill skill) {
        this.skills.add(skill);
        skill.getExperts().add(this);
    }

    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
        skill.getExperts().remove(this);
    }

    public void addDomain(Domain domain) {
        this.domains.add(domain);
        domain.getExperts().add(this);
    }

    public void removeDomain(Domain domain) {
        this.domains.remove(domain);
        domain.getExperts().remove(this);
    }

    @JsonProperty("ownerId")
    public Long getOwnerId() {
        return owner == null ? null : owner.getId();
    }

    @JsonProperty("designationId")
    public Long getDesignationId() {
        return designation == null ? null : designation.getId();
    }
}