package com.expertlink.dto.expert;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkAddExpertsByDomainRequest {
    @NotEmpty
    private List<@NotNull Long> ownerIds;
}

