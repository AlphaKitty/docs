package com.expertlink.service;

import com.expertlink.domain.AuditLog;
import com.expertlink.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(String username, Long userId, String httpMethod, String requestUri, String action,
                       boolean success, String detail, String clientIp) {
        AuditLog row = AuditLog.builder()
                .username(username)
                .userId(userId)
                .httpMethod(httpMethod)
                .requestUri(truncate(requestUri, 500))
                .action(truncate(action, 300))
                .success(success)
                .detail(truncate(detail, 8000))
                .clientIp(truncate(clientIp, 64))
                .build();
        auditLogRepository.save(row);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
