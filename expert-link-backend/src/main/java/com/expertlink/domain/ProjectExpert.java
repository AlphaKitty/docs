package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_experts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectExpert extends BaseEntity {

    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @Column(name = "expert_id", insertable = false, updatable = false)
    private Long expertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @Column(name = "role", length = 100)
    private String role;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "performance_rating", precision = 3, scale = 2)
    private BigDecimal performanceRating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "reviewed_by_id")
    private Long reviewedById;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "is_lead")
    private Boolean isLead = false;

    @Column(name = "is_primary_contact")
    private Boolean isPrimaryContact = false;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @Column(name = "hours_logged")
    private Integer hoursLogged = 0;

    // Relationship to User (reviewer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id", insertable = false, updatable = false)
    @JsonIgnore
    private User reviewedBy;

    // Helper methods
    public void calculateTotalCost() {
        if (hourlyRate != null && totalHours != null) {
            totalCost = hourlyRate.multiply(new BigDecimal(totalHours));
        }
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) || "IN_PROGRESS".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status) || "CLOSED".equals(status);
    }
}