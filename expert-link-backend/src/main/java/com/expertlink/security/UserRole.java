package com.expertlink.security;

/**
 * Application roles (stored without ROLE_ prefix in DB; Spring adds ROLE_ for authorities).
 */
public enum UserRole {
    SUPER_ADMIN,
    /** 领域行管 — 调度与审核 */
    DOMAIN_STEWARD,
    /** 部门管理员 */
    DEPT_ADMIN,
    /** 专家用户 — 维护本人专家信息 */
    EXPERT_USER,
    /** 普通用户 — 发起需求等 */
    REGULAR_USER,
    /** 访客 — 只读 */
    VISITOR
}
