package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private java.time.LocalDateTime readAt;

    @Column(name = "is_archived_by_sender")
    private Boolean isArchivedBySender = false;

    @Column(name = "is_archived_by_receiver")
    private Boolean isArchivedByReceiver = false;

    @Column(name = "is_starred_by_sender")
    private Boolean isStarredBySender = false;

    @Column(name = "is_starred_by_receiver")
    private Boolean isStarredByReceiver = false;

    @Column(name = "has_attachments")
    private Boolean hasAttachments = false;

    @Column(name = "attachments_json", columnDefinition = "TEXT")
    private String attachmentsJson;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id", insertable = false, updatable = false)
    @JsonIgnore
    private Message replyTo;

    // Helper methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = java.time.LocalDateTime.now();
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    public boolean isSystemMessage() {
        return "SYSTEM".equals(messageType);
    }

    public boolean isProjectRelated() {
        return "PROJECT".equals(relatedEntityType);
    }

    public boolean isExpertRelated() {
        return "EXPERT".equals(relatedEntityType);
    }
}