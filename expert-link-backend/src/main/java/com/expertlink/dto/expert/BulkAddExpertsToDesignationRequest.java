package com.expertlink.dto.expert;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkAddExpertsToDesignationRequest {

    @NotEmpty
    private List<@NotNull Long> ownerIds;
}
