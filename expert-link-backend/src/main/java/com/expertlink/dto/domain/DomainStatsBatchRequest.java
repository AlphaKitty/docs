package com.expertlink.dto.domain;

import lombok.Data;

import java.util.List;

@Data
public class DomainStatsBatchRequest {
    /** 可选：只统计指定领域；为空时统计全部领域 */
    private List<Long> domainIds;
}

