package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExpertDecisionRequest {

    @NotNull
    private Boolean accepted;

    private String note;
}
