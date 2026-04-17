package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "english_name", length = 200)
    private String englishName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_code", length = 50, unique = true)
    private String projectCode;

    @Column(name = "project_type", length = 50)
    private String projectType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(name = "client_industry", length = 100)
    private String clientIndustry;

    @Column(name = "client_contact", length = 100)
    private String clientContact;

    @Column(name = "client_email", length = 100)
    private String clientEmail;

    @Column(name = "client_phone", length = 20)
    private String clientPhone;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "complexity_level", length = 20)
    private String complexityLevel;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "success_criteria", columnDefinition = "TEXT")
    private String successCriteria;

    @Column(name = "key_deliverables", columnDefinition = "TEXT")
    private String keyDeliverables;

    @Column(name = "milestones", columnDefinition = "TEXT")
    private String milestones;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Column(name = "project_manager", length = 100)
    private String projectManager;

    @Column(name = "technical_lead", length = 100)
    private String technicalLead;

    @Column(name = "qa_lead", length = 100)
    private String qaLead;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "last_updated_by_id")
    private Long lastUpdatedById;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @Column(name = "quality_score", precision = 3, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "client_satisfaction_score", precision = 3, scale = 2)
    private BigDecimal clientSatisfactionScore;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    @JsonIgnore
    private User createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<ProjectExpert> projectExperts = new HashSet<>();

    // Helper methods
    public boolean isActive() {
        return "IN_PROGRESS".equals(status) || "PLANNING".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status) || "CLOSED".equals(status);
    }

    public boolean isOverdue() {
        if (endDate == null) return false;
        return LocalDate.now().isAfter(endDate) && !isCompleted();
    }
}