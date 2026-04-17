package com.expertlink.dto.engagement;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReleaseScoreRequest {

    /** 行管微调后的总分（可选） */
    private BigDecimal finalScore;

    private String releaseNote;
}
