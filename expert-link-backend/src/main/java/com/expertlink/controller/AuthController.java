package com.expertlink.controller;

import com.expertlink.config.JwtProperties;
import com.expertlink.domain.User;
import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.auth.AuthMeResponse;
import com.expertlink.dto.auth.LoginRequest;
import com.expertlink.dto.auth.LoginResponse;
import com.expertlink.repository.UserRepository;
import com.expertlink.security.AuthPrincipal;
import com.expertlink.security.CustomUserDetailsService;
import com.expertlink.security.JwtService;
import com.expertlink.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("账号已禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        userService.updateLastLogin(user.getId());
        User fresh = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Set<String> roles = normalizeRoles(fresh.getRoles());
        int te = fresh.getTokenEpoch() == null ? 0 : fresh.getTokenEpoch();
        String token = jwtService.generateToken(fresh.getId(), fresh.getUsername(), roles, te);
        LoginResponse body = LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(jwtProperties.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .roles(roles)
                .build();
        return ApiResponses.ok(body);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthMeResponse>> me(@AuthenticationPrincipal AuthPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        BigDecimal bal = user.getPointsBalance() != null ? user.getPointsBalance() : BigDecimal.ZERO;
        AuthMeResponse body = AuthMeResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(normalizeRoles(user.getRoles()))
                .pointsBalance(bal)
                .build();
        return ApiResponses.ok(body);
    }

    private static Set<String> normalizeRoles(Set<String> raw) {
        return raw.stream()
                .map(CustomUserDetailsService::normalizeRoleForToken)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
