package com.expertlink.security;

import com.expertlink.domain.User;
import com.expertlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("用户已禁用: " + username);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(mapAuthorities(user.getRoles()))
                .build();
    }

    static Set<SimpleGrantedAuthority> mapAuthorities(Set<String> roles) {
        return roles.stream()
                .map(CustomUserDetailsService::normalizeRole)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }

    /** Legacy DB may contain USER — treat as REGULAR_USER. */
    public static String normalizeRoleForToken(String r) {
        if (r == null || r.isBlank()) {
            return UserRole.REGULAR_USER.name();
        }
        if ("USER".equalsIgnoreCase(r)) {
            return UserRole.REGULAR_USER.name();
        }
        return r;
    }

    private static String normalizeRole(String r) {
        return normalizeRoleForToken(r);
    }
}
