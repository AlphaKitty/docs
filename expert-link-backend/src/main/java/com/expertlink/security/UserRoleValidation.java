package com.expertlink.security;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserRoleValidation {

    private UserRoleValidation() {
    }

    public static void validateRoleSet(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("至少分配一个角色");
        }
        for (String raw : roles) {
            String normalized = CustomUserDetailsService.normalizeRoleForToken(raw);
            try {
                UserRole.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("非法角色: " + raw);
            }
        }
    }

    public static Set<String> normalizeRoleSet(Set<String> roles) {
        return roles.stream()
                .map(CustomUserDetailsService::normalizeRoleForToken)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
