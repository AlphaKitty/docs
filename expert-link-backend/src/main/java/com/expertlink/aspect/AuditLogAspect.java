package com.expertlink.aspect;

import com.expertlink.security.AuthPrincipal;
import com.expertlink.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private static final List<String> MUTATING = List.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditLogService auditLogService;

    @Around("execution(* com.expertlink.controller..*(..)) && !execution(* com.expertlink.controller.AuthController.*(..))")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest req = attrs.getRequest();
        if (!MUTATING.contains(req.getMethod())) {
            return pjp.proceed();
        }

        String action = pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();
        String username = null;
        Long userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal ap) {
            username = ap.username();
            userId = ap.userId();
        } else if (auth != null) {
            username = auth.getName();
        }

        long start = System.currentTimeMillis();
        try {
            Object out = pjp.proceed();
            auditLogService.record(
                    username,
                    userId,
                    req.getMethod(),
                    req.getRequestURI(),
                    action,
                    true,
                    "OK " + (System.currentTimeMillis() - start) + "ms",
                    clientIp(req)
            );
            return out;
        } catch (Throwable t) {
            auditLogService.record(
                    username,
                    userId,
                    req.getMethod(),
                    req.getRequestURI(),
                    action,
                    false,
                    truncate(t.getMessage(), 2000),
                    clientIp(req)
            );
            throw t;
        }
    }

    private static String clientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
