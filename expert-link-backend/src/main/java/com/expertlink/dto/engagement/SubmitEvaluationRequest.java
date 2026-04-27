package com.expertlink.dto.engagement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitEvaluationRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer professional;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer timeliness;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer attitude;

    @NotNull
    private Boolean resolved;

    private String comment;

    /** 评价附件相对路径列表（先调上传接口获得 path） */
    private List<String> attachmentUrls;

    /** 按专家分别评价（前端分专家填写时使用） */
    private List<ExpertEvaluationItem> expertEvaluations;

    @Data
    public static class ExpertEvaluationItem {
        @NotNull
        private Long expertId;

        @NotNull
        @Min(1)
        @Max(5)
        private Integer professional;

        @NotNull
        @Min(1)
        @Max(5)
        private Integer timeliness;

        @NotNull
        @Min(1)
        @Max(5)
        private Integer attitude;

        @NotNull
        private Boolean resolved;

        private String comment;
    }
}
