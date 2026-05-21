package com.expertlink.dto.engagement;

import com.expertlink.domain.EngagementMode;
import com.expertlink.domain.EngagementTaskType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEngagementDraftRequest {

    @NotNull
    private Long domainId;

    @NotNull
    private EngagementMode mode;

    /** 申请类别：专家调用 / 积分自提 */
    private String applyCategory;

    /** 积分大类 */
    private String pointsCategory;

    /** 积分项目 */
    private String pointsItem;

    @NotNull
    private EngagementTaskType taskType;

    @NotNull
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    /** 草稿阶段可空，提交前必填 */
    private String taskDescription;

    /** 点名模式下可选（支持多选） */
    private List<Long> designatedExpertIds;
}
