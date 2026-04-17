package com.expertlink.service;

import com.expertlink.domain.ExpertDesignation;
import com.expertlink.dto.expert.BulkAddExpertsToDesignationResponse;
import com.expertlink.repository.ExpertDesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertDesignationService {

    private final ExpertDesignationRepository expertDesignationRepository;
    private final ExpertService expertService;

    public List<ExpertDesignation> listAll() {
        return expertDesignationRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public ExpertDesignation create(String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("称谓名称不能为空");
        }
        String n = name.trim();
        if (expertDesignationRepository.existsByNameIgnoreCase(n)) {
            throw new IllegalArgumentException("该称谓已存在: " + n);
        }
        ExpertDesignation d = ExpertDesignation.builder()
                .name(n)
                .description(StringUtils.hasText(description) ? description.trim() : null)
                .build();
        return expertDesignationRepository.save(d);
    }

    @Transactional
    public BulkAddExpertsToDesignationResponse bulkAddExperts(Long designationId, List<Long> ownerIds) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个用户");
        }
        // 先校验称谓存在
        expertDesignationRepository.findById(designationId)
                .orElseThrow(() -> new IllegalArgumentException("称谓不存在，ID: " + designationId));

        Set<Long> deduped = new LinkedHashSet<>();
        for (Long ownerId : ownerIds) {
            if (ownerId != null && ownerId > 0) {
                deduped.add(ownerId);
            }
        }
        if (deduped.isEmpty()) {
            throw new IllegalArgumentException("有效用户为空");
        }

        List<Long> createdExpertIds = new ArrayList<>();
        List<Long> skippedOwnerIds = new ArrayList<>();
        for (Long ownerId : deduped) {
            try {
                createdExpertIds.add(expertService.createByOwnerAndDesignation(ownerId, designationId).getId());
            } catch (IllegalArgumentException ex) {
                // 已有专家档案等可预期业务错误，记为跳过
                skippedOwnerIds.add(ownerId);
            }
        }

        return BulkAddExpertsToDesignationResponse.builder()
                .requestedCount(deduped.size())
                .createdCount(createdExpertIds.size())
                .skippedCount(skippedOwnerIds.size())
                .createdExpertIds(createdExpertIds)
                .skippedOwnerIds(skippedOwnerIds)
                .build();
    }
}
