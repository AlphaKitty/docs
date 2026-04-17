package com.expertlink.dto.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReplaceDomainStewardsRequest {
    private List<Long> userIds = new ArrayList<>();
}
