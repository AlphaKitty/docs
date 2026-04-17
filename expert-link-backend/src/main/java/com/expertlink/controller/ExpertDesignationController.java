package com.expertlink.controller;

import com.expertlink.domain.ExpertDesignation;
import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.expert.BulkAddExpertsToDesignationRequest;
import com.expertlink.dto.expert.BulkAddExpertsToDesignationResponse;
import com.expertlink.dto.expert.CreateExpertDesignationRequest;
import com.expertlink.service.ExpertDesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expert-designations")
@RequiredArgsConstructor
public class ExpertDesignationController {

    private final ExpertDesignationService expertDesignationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpertDesignation>>> list() {
        return ApiResponses.ok(expertDesignationService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','DEPT_ADMIN')")
    public ResponseEntity<ApiResponse<ExpertDesignation>> create(@Valid @RequestBody CreateExpertDesignationRequest body) {
        ExpertDesignation created = expertDesignationService.create(body.getName(), body.getDescription());
        return ApiResponses.created(created);
    }

    /**
     * 批量将用户挂到称谓下创建专家档案。
     * 路径使用 {@code /experts/batch-add}，避免 {@code experts:batch-add} 中的冒号在 Spring 6
     * {@link org.springframework.web.util.pattern.PathPattern} 下无法匹配、落到静态资源 404/500 的问题。
     */
    @PostMapping("/{designationId}/experts/batch-add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','DEPT_ADMIN')")
    public ResponseEntity<ApiResponse<BulkAddExpertsToDesignationResponse>> bulkAddExperts(
            @PathVariable Long designationId,
            @Valid @RequestBody BulkAddExpertsToDesignationRequest body) {
        return ApiResponses.ok(expertDesignationService.bulkAddExperts(designationId, body.getOwnerIds()));
    }
}
