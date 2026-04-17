package com.expertlink.dto.auth;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String tokenType;
    long expiresInMs;
    long userId;
    String username;
    Set<String> roles;
}
