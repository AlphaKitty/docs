package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "engagement_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EngagementRequest extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private EngagementRequestStatus status = EngagementRequestStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EngagementMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 40)
    private EngagementTaskType taskType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    /** 点名模式下申请人可选指定的专家（可多选） */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "engagement_request_designated_experts",
            joinColumns = @JoinColumn(name = "engagement_request_id"),
            inverseJoinColumns = @JoinColumn(name = "expert_id")
    )
    @Builder.Default
    private Set<Expert> designatedExperts = new LinkedHashSet<>();

    /** 行管指派后的专家 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_expert_id")
    private Expert assignedExpert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_steward_id")
    private User assignedBySteward;

    @Column(name = "assignment_note", columnDefinition = "TEXT")
    private String assignmentNote;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "expert_accepted")
    private Boolean expertAccepted;

    @Column(name = "expert_response_note", columnDefinition = "TEXT")
    private String expertResponseNote;

    @Column(name = "expert_responded_at")
    private LocalDateTime expertRespondedAt;

    @Column(name = "eval_professional")
    private Integer evalProfessional;

    @Column(name = "eval_timeliness")
    private Integer evalTimeliness;

    @Column(name = "eval_attitude")
    private Integer evalAttitude;

    @Column(name = "eval_resolved")
    private Boolean evalResolved;

    @Column(name = "eval_comment", columnDefinition = "TEXT")
    private String evalComment;

    @Column(name = "evaluation_submitted_at")
    private LocalDateTime evaluationSubmittedAt;

    @Column(name = "steward_final_score", precision = 6, scale = 2)
    private BigDecimal stewardFinalScore;

    @Column(name = "steward_release_note", columnDefinition = "TEXT")
    private String stewardReleaseNote;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** 正式编号，在首次提交时生成（如 ER-20260417-000042） */
    @Column(name = "reference_code", unique = true, length = 40)
    private String referenceCode;

    /** 根据评价维度预计算的建议分（0–100） */
    @Column(name = "suggested_score", precision = 8, scale = 2)
    private BigDecimal suggestedScore;

    /** 评价附件相对路径 JSON 数组，如 ["/engagement-requests/1/evaluation-files/uuid.pdf"] */
    @Column(name = "evaluation_attachment_urls", columnDefinition = "TEXT")
    private String evaluationAttachmentUrls;

    /** 行管退回重评时的说明（申请人可见） */
    @Column(name = "evaluation_revision_note", columnDefinition = "TEXT")
    private String evaluationRevisionNote;

    /** 改派记录 JSON 数组 */
    @Column(name = "reassignment_log", columnDefinition = "TEXT")
    private String reassignmentLog;
}
