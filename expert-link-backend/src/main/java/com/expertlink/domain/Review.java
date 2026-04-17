package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;

    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    @Column(name = "communication_rating", precision = 3, scale = 2)
    private BigDecimal communicationRating;

    @Column(name = "technical_rating", precision = 3, scale = 2)
    private BigDecimal technicalRating;

    @Column(name = "timeliness_rating", precision = 3, scale = 2)
    private BigDecimal timelinessRating;

    @Column(name = "overall_experience_rating", precision = 3, scale = 2)
    private BigDecimal overallExperienceRating;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_expert_id")
    @JsonIgnore
    private ProjectExpert projectExpert;

    // Helper methods
    public void incrementHelpfulCount() {
        if (this.helpfulCount == null) {
            this.helpfulCount = 0;
        }
        this.helpfulCount++;
    }

    public void decrementHelpfulCount() {
        if (this.helpfulCount == null || this.helpfulCount <= 0) {
            this.helpfulCount = 0;
        } else {
            this.helpfulCount--;
        }
    }

    public void incrementReplyCount() {
        if (this.replyCount == null) {
            this.replyCount = 0;
        }
        this.replyCount++;
    }

    public void decrementReplyCount() {
        if (this.replyCount == null || this.replyCount <= 0) {
            this.replyCount = 0;
        } else {
            this.replyCount--;
        }
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(status) || "APPROVED".equals(status);
    }
}