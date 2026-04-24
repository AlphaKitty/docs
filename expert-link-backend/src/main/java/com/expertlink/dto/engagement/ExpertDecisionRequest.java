package com.expertlink.dto.engagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExpertDecisionRequest {

    @NotNull
    private Boolean accepted;

    private String note;

    /** 超级管理员代为某被指派的专家确认时必传 */
    private Long expertId;
}
