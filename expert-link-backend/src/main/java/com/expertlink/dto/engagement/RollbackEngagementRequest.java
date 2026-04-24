package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RollbackEngagementRequest {
    @NotBlank(message = "退回说明不能为空")
    private String reason;
}
