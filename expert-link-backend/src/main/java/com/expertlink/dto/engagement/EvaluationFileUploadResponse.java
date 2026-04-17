package com.expertlink.dto.engagement;

import lombok.Builder;

@Builder
public record EvaluationFileUploadResponse(
        /** 相对路径，提交评价时放入 attachmentUrls */
        String path,
        String originalFilename
) {
}
