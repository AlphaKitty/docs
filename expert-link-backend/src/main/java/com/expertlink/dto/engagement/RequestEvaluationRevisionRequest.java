package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestEvaluationRevisionRequest {

    @NotBlank
    private String reason;
}
