package com.expertlink.domain;

/**
 * 调⽤申请单状态（对齐 docs/03.md 最小节点）。
 */
public enum EngagementRequestStatus {
    /** 草稿，可编辑 */
    DRAFT,
    /** 已提交，待领域行管指派专家 */
    PENDING_STEWARD_ASSIGN,
    /** 已指派，待专家确认 */
    PENDING_EXPERT_CONFIRM,
    /** 专家已接受，执行中 */
    IN_PROGRESS,
    /** 申请人已提交评价，待行管放分 */
    PENDING_STEWARD_SCORE_RELEASE,
    /** 已结项 */
    COMPLETED,
    /** 已驳回（专家拒绝等） */
    REJECTED,
    /** 申请人已取消 */
    CANCELLED
}
