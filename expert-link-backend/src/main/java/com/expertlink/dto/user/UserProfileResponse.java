package com.expertlink.dto.user;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;

@Value
@Builder
public class UserProfileResponse {
    long userId;
    String username;
    String fullName;
    String email;
    Set<String> roles;
    BigDecimal pointsBalance;
    String phoneNumber;
    String bio;
    String avatar;
    ExpertProfileResponse expertProfile;
}
