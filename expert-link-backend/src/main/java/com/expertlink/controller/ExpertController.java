package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.dto.importing.ImportResultResponse;
import com.expertlink.dto.expert.UserPickerDto;
import com.expertlink.domain.Expert;
import com.expertlink.security.AuthPrincipal;
import com.expertlink.service.ExpertPrivacyService;
import com.expertlink.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/experts")
@RequiredArgsConstructor
public class ExpertController {

    private final ExpertService expertService;
    private final ExpertPrivacyService expertPrivacyService;

    /**
     * 获取所有专家（分页）
     * GET /api/experts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Expert>>> getAllExperts(Pageable pageable, Authentication authentication) {
        Page<Expert> experts = expertPrivacyService.maskPage(expertService.findAll(pageable), authentication);
        return ApiResponses.okPage(experts);
    }

    /**
     * 尚未建立专家档案的用户（用于从用户库拉取专家）
     * GET /api/experts/user-candidates
     */
    @GetMapping("/user-candidates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','DEPT_ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserPickerDto>>> userCandidates(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserPickerDto> page = expertService.listUserCandidates(keyword, pageable);
        return ApiResponses.okPage(page);
    }

    /**
     * 根据ID获取专家
     * GET /api/experts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Expert>> getExpertById(@PathVariable Long id, Authentication authentication) {
        Expert expert = expertPrivacyService.maskForRead(expertService.findById(id), authentication);
        return ApiResponses.ok(expert);
    }

    /**
     * 根据邮箱获取专家
     * GET /api/experts/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Expert>> getExpertByEmail(@PathVariable String email, Authentication authentication) {
        Expert expert = expertPrivacyService.maskForRead(
                expertService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("专家不存在，邮箱: " + email)),
                authentication);
        return ApiResponses.ok(expert);
    }

    /**
     * 根据姓名查找专家
     * GET /api/experts/search/name?name={name}
     */
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<Expert>>> searchExpertsByName(@RequestParam String name, Authentication authentication) {
        List<Expert> experts = expertService.findByNameContaining(name).stream()
                .map(e -> expertPrivacyService.maskForRead(e, authentication))
                .toList();
        return ApiResponses.ok(experts);
    }

    /**
     * 创建专家
     * POST /api/experts
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','DEPT_ADMIN','EXPERT_USER','REGULAR_USER')")
    public ResponseEntity<ApiResponse<Expert>> createExpert(
            @RequestBody Expert expert,
            @RequestParam Long ownerId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(required = false) Long primaryDomainId,
            @RequestParam(required = false) Set<Long> skillIds,
            @RequestParam(required = false) Set<Long> domainIds,
            Authentication authentication) {
        assertCanActForOwner(authentication, ownerId);
        Expert createdExpert = expertService.create(expert, ownerId, primaryDomainId, skillIds, domainIds, designationId);
        return ApiResponses.created(createdExpert);
    }

    private static void assertCanActForOwner(Authentication authentication, Long ownerId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal ap)) {
            throw new AccessDeniedException("未认证");
        }
        if (ap.userId() == ownerId) {
            return;
        }
        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a) || "ROLE_DEPT_ADMIN".equals(a));
        if (!admin) {
            throw new AccessDeniedException("只能为自己创建专家档案，或需管理员操作");
        }
    }

    /**
     * 更新专家信息
     * PUT /api/experts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Expert>> updateExpert(
            @PathVariable Long id,
            @RequestBody Expert expertDetails,
            @RequestParam(required = false) Long primaryDomainId,
            @RequestParam(required = false) Set<Long> skillIds,
            @RequestParam(required = false) Set<Long> domainIds) {
        Expert updatedExpert = expertService.update(id, expertDetails, primaryDomainId, skillIds, domainIds);
        return ApiResponses.ok(updatedExpert);
    }

    /**
     * 删除专家
     * DELETE /api/experts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpert(@PathVariable Long id) {
        expertService.delete(id);
        return ApiResponses.noContent("专家删除成功");
    }

    /**
     * 搜索专家（关键词）
     * GET /api/experts/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Expert>>> searchExperts(
            @RequestParam(required = false) String keyword,
            Pageable pageable,
            Authentication authentication) {
        Page<Expert> experts = expertPrivacyService.maskPage(expertService.searchByKeyword(keyword, pageable), authentication);
        return ApiResponses.okPage(experts);
    }

    /**
     * 根据领域查找专家
     * GET /api/experts/domain/{domainId}
     */
    @GetMapping("/domain/{domainId}")
    public ResponseEntity<ApiResponse<List<Expert>>> getExpertsByDomain(@PathVariable Long domainId, Authentication authentication) {
        List<Expert> experts = expertService.findByDomainId(domainId).stream()
                .map(e -> expertPrivacyService.maskForRead(e, authentication))
                .toList();
        return ApiResponses.ok(experts);
    }

    /**
     * 根据多个领域（含各自子领域）查找专家
     * GET /api/experts/by-domains?domainIds=1&domainIds=2
     */
    @GetMapping("/by-domains")
    public ResponseEntity<ApiResponse<List<Expert>>> getExpertsByDomains(
            @RequestParam(name = "domainIds", required = false) Set<Long> domainIds,
            @RequestParam(name = "domainIds[]", required = false) Set<Long> bracketDomainIds,
            Authentication authentication) {
        Set<Long> deduped = new LinkedHashSet<>();
        if (domainIds != null) {
            deduped.addAll(domainIds);
        }
        if (bracketDomainIds != null) {
            deduped.addAll(bracketDomainIds);
        }
        List<Expert> experts = expertService.findByDomainIds(deduped).stream()
                .map(e -> expertPrivacyService.maskForRead(e, authentication))
                .toList();
        return ApiResponses.ok(experts);
    }

    /**
     * 根据技能查找专家
     * GET /api/experts/skill/{skillId}
     */
    @GetMapping("/skill/{skillId}")
    public ResponseEntity<ApiResponse<List<Expert>>> getExpertsBySkill(@PathVariable Long skillId, Authentication authentication) {
        List<Expert> experts = expertService.findBySkillId(skillId).stream()
                .map(e -> expertPrivacyService.maskForRead(e, authentication))
                .toList();
        return ApiResponses.ok(experts);
    }

    /**
     * 根据费率范围查找专家
     * GET /api/experts/rate-range
     */
    @GetMapping("/rate-range")
    public ResponseEntity<ApiResponse<List<Expert>>> getExpertsByRateRange(
            @RequestParam BigDecimal minRate,
            @RequestParam BigDecimal maxRate,
            Authentication authentication) {
        List<Expert> experts = expertService.findByHourlyRateBetween(minRate, maxRate).stream()
                .map(e -> expertPrivacyService.maskForRead(e, authentication))
                .toList();
        return ApiResponses.ok(experts);
    }

    /**
     * 获取活跃专家数量
     * GET /api/experts/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> countActiveExperts() {
        long count = expertService.countActiveExperts();
        return ApiResponses.ok(count);
    }

    /**
     * 获取评分最高的专家
     * GET /api/experts/top-rated
     */
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<List<Expert>>> getTopRatedExperts(Pageable pageable, Authentication authentication) {
        List<Expert> experts = expertService.findTopRatedExperts(pageable).stream()
                .map(e -> expertPrivacyService.maskForRead(e, authentication))
                .toList();
        return ApiResponses.ok(experts);
    }

    /**
     * 验证专家
     * PUT /api/experts/{id}/verify
     */
    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Expert>> verifyExpert(
            @PathVariable Long id,
            @RequestParam Integer verificationLevel) {
        Expert expert = expertService.verifyExpert(id, verificationLevel);
        return ApiResponses.ok(expert);
    }

    /**
     * 更新专家评分
     * PUT /api/experts/{id}/rating
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<Expert>> updateExpertRating(
            @PathVariable Long id,
            @RequestParam BigDecimal rating) {
        Expert expert = expertService.updateRating(id, rating);
        return ApiResponses.ok(expert);
    }

    /**
     * 更新专家项目数量（增加）
     * PUT /api/experts/{id}/increment-project
     */
    @PutMapping("/{id}/increment-project")
    public ResponseEntity<ApiResponse<Expert>> incrementProjectCount(@PathVariable Long id) {
        Expert expert = expertService.incrementProjectCount(id);
        return ApiResponses.ok(expert);
    }

    /**
     * 更新专家成功率
     * PUT /api/experts/{id}/success-rate
     */
    @PutMapping("/{id}/success-rate")
    public ResponseEntity<ApiResponse<Expert>> updateSuccessRate(
            @PathVariable Long id,
            @RequestParam BigDecimal successRate) {
        Expert expert = expertService.updateSuccessRate(id, successRate);
        return ApiResponses.ok(expert);
    }

    /**
     * 添加技能到专家
     * POST /api/experts/{expertId}/skills/{skillId}
     */
    @PostMapping("/{expertId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<Expert>> addSkillToExpert(
            @PathVariable Long expertId,
            @PathVariable Long skillId) {
        Expert expert = expertService.addSkill(expertId, skillId);
        return ApiResponses.ok(expert);
    }

    /**
     * 从专家移除技能
     * DELETE /api/experts/{expertId}/skills/{skillId}
     */
    @DeleteMapping("/{expertId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<Expert>> removeSkillFromExpert(
            @PathVariable Long expertId,
            @PathVariable Long skillId) {
        Expert expert = expertService.removeSkill(expertId, skillId);
        return ApiResponses.ok(expert);
    }

    /**
     * 添加领域到专家
     * POST /api/experts/{expertId}/domains/{domainId}
     */
    @PostMapping("/{expertId}/domains/{domainId}")
    public ResponseEntity<ApiResponse<Expert>> addDomainToExpert(
            @PathVariable Long expertId,
            @PathVariable Long domainId) {
        Expert expert = expertService.addDomain(expertId, domainId);
        return ApiResponses.ok(expert);
    }

    /**
     * 从专家移除领域
     * DELETE /api/experts/{expertId}/domains/{domainId}
     */
    @DeleteMapping("/{expertId}/domains/{domainId}")
    public ResponseEntity<ApiResponse<Expert>> removeDomainFromExpert(
            @PathVariable Long expertId,
            @PathVariable Long domainId) {
        Expert expert = expertService.removeDomain(expertId, domainId);
        return ApiResponses.ok(expert);
    }

    @GetMapping("/import-template")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        byte[] data = expertService.buildImportTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=experts-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExperts() {
        byte[] data = expertService.buildExportWorkbook();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=experts-export.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importExperts(@RequestParam("file") MultipartFile file) {
        return ApiResponses.ok(expertService.importExperts(file));
    }
}