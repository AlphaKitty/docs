package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.dto.engagement.*;
import com.expertlink.security.AuthPrincipal;
import com.expertlink.service.EngagementRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/engagement-requests")
@RequiredArgsConstructor
public class EngagementRequestController {

    private final EngagementRequestService engagementRequestService;

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','DOMAIN_STEWARD','EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<EngagementRequestResponse>>> listMine(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable) {
        return ApiResponses.okPage(engagementRequestService.listMine(principal.userId(), pageable));
    }

    @GetMapping("/steward-queue")
    @PreAuthorize("hasAnyRole('DOMAIN_STEWARD','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<EngagementRequestResponse>>> stewardQueue(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable) {
        return ApiResponses.okPage(engagementRequestService.listStewardQueue(principal.userId(), pageable));
    }

    @GetMapping("/expert-pending")
    @PreAuthorize("hasAnyRole('EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<EngagementRequestResponse>>> expertPending(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable) {
        return ApiResponses.okPage(engagementRequestService.listExpertPending(principal.userId(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> getOne(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        return ApiResponses.ok(engagementRequestService.getByIdForViewer(principal.userId(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','DOMAIN_STEWARD','EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> create(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateEngagementDraftRequest body) {
        return ApiResponses.created(engagementRequestService.createDraft(principal.userId(), body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> patchDraft(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody PatchEngagementDraftRequest body) {
        return ApiResponses.ok(engagementRequestService.patchDraft(principal.userId(), id, body));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> submit(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {
        return ApiResponses.ok(engagementRequestService.submit(principal.userId(), id));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DOMAIN_STEWARD','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> assign(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody StewardAssignRequest body) {
        return ApiResponses.ok(engagementRequestService.stewardAssign(principal.userId(), id, body));
    }

    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAnyRole('DOMAIN_STEWARD','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> reassign(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReassignEngagementRequest body) {
        return ApiResponses.ok(engagementRequestService.stewardReassign(principal.userId(), id, body));
    }

    @PostMapping("/{id}/request-evaluation-revision")
    @PreAuthorize("hasAnyRole('DOMAIN_STEWARD','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> requestEvaluationRevision(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RequestEvaluationRevisionRequest body) {
        return ApiResponses.ok(engagementRequestService.requestEvaluationRevision(principal.userId(), id, body));
    }

    @PostMapping(value = "/{id}/evaluation-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','DOMAIN_STEWARD','EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EvaluationFileUploadResponse>> uploadEvaluationFile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            return ApiResponses.ok(engagementRequestService.uploadEvaluationFile(principal.userId(), id, file));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件上传失败", e);
        }
    }

    @GetMapping("/{id}/evaluation-files/{fileName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadEvaluationFile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @PathVariable String fileName) {
        Resource resource = engagementRequestService.readEvaluationFile(principal.userId(), id, fileName);
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(resource);
    }

    @PostMapping("/{id}/expert-decision")
    @PreAuthorize("hasAnyRole('EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> expertDecision(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ExpertDecisionRequest body) {
        return ApiResponses.ok(engagementRequestService.expertDecision(principal.userId(), id, body));
    }

    @PostMapping("/{id}/submit-evaluation")
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> submitEvaluation(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SubmitEvaluationRequest body) {
        return ApiResponses.ok(engagementRequestService.submitEvaluation(principal.userId(), id, body));
    }

    @PostMapping("/{id}/release-score")
    @PreAuthorize("hasAnyRole('DOMAIN_STEWARD','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> releaseScore(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) ReleaseScoreRequest body) {
        ReleaseScoreRequest req = body != null ? body : new ReleaseScoreRequest();
        return ApiResponses.ok(engagementRequestService.releaseScore(principal.userId(), id, req));
    }

    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasAnyRole('DOMAIN_STEWARD','EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> rollbackToPreviousNode(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RollbackEngagementRequest body) {
        return ApiResponses.ok(engagementRequestService.rollbackToPreviousNode(principal.userId(), id, body));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('REGULAR_USER','DEPT_ADMIN','DOMAIN_STEWARD','EXPERT_USER','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<EngagementRequestResponse>> cancelByApplicant(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @RequestBody(required = false) CancelEngagementRequest body) {
        CancelEngagementRequest req = body != null ? body : new CancelEngagementRequest();
        return ApiResponses.ok(engagementRequestService.cancelByApplicant(principal.userId(), id, req));
    }
}
