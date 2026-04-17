package com.expertlink.dto.auth;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;

@Value
@Builder
public class AuthMeResponse {
    long userId;
    String username;
    String email;
    String fullName;
    Set<String> roles;
    BigDecimal pointsBalance;
}
