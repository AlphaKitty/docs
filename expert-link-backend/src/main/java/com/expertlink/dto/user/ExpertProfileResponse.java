package com.expertlink.dto.user;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ExpertProfileResponse {
    long expertId;
    String name;
    String currentPosition;
    String currentCompany;
    String wechatId;
    Integer yearsOfExperience;
    BigDecimal hourlyRate;
    String availabilityStatus;
    String biography;
}
