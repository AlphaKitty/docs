package com.expertlink.dto.skill;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSkillRequest {
    private String name;
    private String category;
    private String description;
    private Long domainId;
    private String demandLevel;
    private Boolean enabled;
    private List<String> tags;
    private List<Long> relatedSkillIds;
    private SkillDocumentationDto documentation;
}
