package com.expertlink.config;

import com.expertlink.domain.User;
import com.expertlink.repository.UserRepository;
import com.expertlink.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class BootstrapAdminRunner implements ApplicationRunner {

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (userRepository.existsByUsername(properties.getUsername())) {
            return;
        }
        User admin = User.builder()
                .username(properties.getUsername())
                .email(properties.getEmail())
                .password(passwordEncoder.encode(properties.getPassword()))
                .fullName("系统管理员")
                .isActive(true)
                .roles(new HashSet<>(Set.of(UserRole.SUPER_ADMIN.name())))
                .build();
        userRepository.save(admin);
        log.warn("已创建默认超级管理员用户 '{}'，请尽快修改密码（app.bootstrap-admin / 环境变量）", properties.getUsername());
    }
}
