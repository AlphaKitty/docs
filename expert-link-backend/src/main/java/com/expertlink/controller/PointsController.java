package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.dto.points.PointsLedgerEntryResponse;
import com.expertlink.security.AuthPrincipal;
import com.expertlink.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/ledger")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginatedResponse<PointsLedgerEntryResponse>>> myLedger(
            @AuthenticationPrincipal AuthPrincipal principal,
            Pageable pageable) {
        return ApiResponses.okPage(pointsService.ledgerForUser(principal.userId(), pageable));
    }
}
