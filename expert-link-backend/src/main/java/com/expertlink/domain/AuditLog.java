package com.expertlink.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(length = 100)
    private String username;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    /** 类名.方法名 */
    @Column(name = "action", length = 300)
    private String action;

    @Column(nullable = false)
    private boolean success;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "client_ip", length = 64)
    private String clientIp;
}
