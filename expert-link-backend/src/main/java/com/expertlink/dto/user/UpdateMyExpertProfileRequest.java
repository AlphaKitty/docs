package com.expertlink.dto.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMyExpertProfileRequest {

    @Size(max = 200)
    private String currentPosition;

    @Size(max = 200)
    private String currentCompany;

    @Size(max = 100)
    private String wechatId;

    private Integer yearsOfExperience;

    @DecimalMin(value = "0", message = "小时费率不能为负数")
    private BigDecimal hourlyRate;

    @Size(max = 50)
    private String availabilityStatus;

    @Size(max = 4000)
    private String biography;
}
