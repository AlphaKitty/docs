package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.domain.Domain;
import com.expertlink.dto.expert.BulkAddExpertsByDomainRequest;
import com.expertlink.dto.expert.BulkAddExpertsByDomainResponse;
import com.expertlink.dto.domain.DomainStatsResponse;
import com.expertlink.dto.domain.DomainStatsBatchRequest;
import com.expertlink.dto.domain.ReplaceDomainStewardsRequest;
import com.expertlink.service.DomainService;
import com.expertlink.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;
    private final ExpertService expertService;

    /**
     * 获取所有领域（分页）
     * GET /api/domains
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Domain>>> getAllDomains(Pageable pageable) {
        Page<Domain> domains = domainService.findAll(pageable);
        return ApiResponses.okPage(domains);
    }

    /**
     * 根据ID获取领域
     * GET /api/domains/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Domain>> getDomainById(@PathVariable Long id) {
        Domain domain = domainService.findById(id);
        return ApiResponses.ok(domain);
    }

    /**
     * 获取领域子树统计（本领域 + 全部子领域）
     * GET /api/domains/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<DomainStatsResponse>> getDomainStats(@PathVariable Long id) {
        return ApiResponses.ok(domainService.getSubtreeStats(id));
    }

    /**
     * 批量获取领域子树统计（本领域 + 全部子领域）
     * POST /api/domains/stats
     */
    @PostMapping("/stats")
    public ResponseEntity<ApiResponse<List<DomainStatsResponse>>> getDomainStatsBatch(
            @RequestBody(required = false) DomainStatsBatchRequest body) {
        List<Long> ids = body == null ? null : body.getDomainIds();
        return ApiResponses.ok(domainService.getSubtreeStatsBatch(ids));
    }

    /**
     * 根据名称获取领域
     * GET /api/domains/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Domain>> getDomainByName(@PathVariable String name) {
        Domain domain = domainService.findByName(name)
                .orElseThrow(() -> new RuntimeException("领域不存在，名称: " + name));
        return ApiResponses.ok(domain);
    }

    /**
     * 根据名称关键词搜索领域（分页）
     * GET /api/domains/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Domain>>> searchDomains(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<Domain> domains = domainService.findByNameContaining(keyword, pageable);
        return ApiResponses.okPage(domains);
    }

    /**
     * 根据父级ID获取子领域
     * GET /api/domains/parent/{parentId}
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<Domain>>> getDomainsByParentId(@PathVariable Long parentId) {
        List<Domain> domains = domainService.findByParentId(parentId);
        return ApiResponses.ok(domains);
    }

    /**
     * 根据父级ID获取子领域（分页）
     * GET /api/domains/parent/{parentId}/page
     */
    @GetMapping("/parent/{parentId}/page")
    public ResponseEntity<ApiResponse<PaginatedResponse<Domain>>> getDomainsByParentIdPage(
            @PathVariable Long parentId,
            Pageable pageable) {
        Page<Domain> domains = domainService.findByParentIdPage(parentId, pageable);
        return ApiResponses.okPage(domains);
    }

    /**
     * 根据层级获取领域
     * GET /api/domains/level/{level}
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<Domain>>> getDomainsByLevel(@PathVariable Integer level) {
        List<Domain> domains = domainService.findByLevel(level);
        return ApiResponses.ok(domains);
    }

    /**
     * 根据活跃状态获取领域
     * GET /api/domains/status/{isActive}
     */
    @GetMapping("/status/{isActive}")
    public ResponseEntity<ApiResponse<List<Domain>>> getDomainsByStatus(@PathVariable Boolean isActive) {
        List<Domain> domains = domainService.findByIsActive(isActive);
        return ApiResponses.ok(domains);
    }

    /**
     * 获取活跃领域（分页）
     * GET /api/domains/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PaginatedResponse<Domain>>> getActiveDomains(Pageable pageable) {
        Page<Domain> domains = domainService.findActiveDomains(pageable);
        return ApiResponses.okPage(domains);
    }

    /**
     * 获取根领域（parentId为null）
     * GET /api/domains/root
     */
    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<Domain>>> getRootDomains() {
        List<Domain> domains = domainService.findRootDomains();
        return ApiResponses.ok(domains);
    }

    /**
     * 获取根领域（分页）
     * GET /api/domains/root/page
     */
    @GetMapping("/root/page")
    public ResponseEntity<ApiResponse<PaginatedResponse<Domain>>> getRootDomainsPage(Pageable pageable) {
        Page<Domain> domains = domainService.findRootDomainsPage(pageable);
        return ApiResponses.okPage(domains);
    }

    /**
     * 根据专家ID获取关联领域
     * GET /api/domains/expert/{expertId}
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<ApiResponse<List<Domain>>> getDomainsByExpertId(@PathVariable Long expertId) {
        List<Domain> domains = domainService.findByExpertId(expertId);
        return ApiResponses.ok(domains);
    }

    /**
     * 创建领域
     * POST /api/domains
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Domain>> createDomain(@RequestBody Domain domain) {
        Domain createdDomain = domainService.create(domain);
        return ApiResponses.created(createdDomain);
    }

    /**
     * 更新领域
     * PUT /api/domains/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Domain>> updateDomain(@PathVariable Long id, @RequestBody Domain domainDetails) {
        Domain updatedDomain = domainService.update(id, domainDetails);
        return ApiResponses.ok(updatedDomain);
    }

    /**
     * 配置领域行管（全量覆盖 userIds）
     * PUT /api/domains/{id}/stewards
     */
    @PutMapping("/{id}/stewards")
    public ResponseEntity<ApiResponse<Domain>> replaceStewards(
            @PathVariable Long id,
            @RequestBody ReplaceDomainStewardsRequest body) {
        Domain domain = domainService.replaceStewards(id, new HashSet<>(body.getUserIds()));
        return ApiResponses.ok(domain);
    }

    /**
     * 批量将用户纳入某领域专家（从用户转专家）。
     * POST /api/domains/{id}/experts/batch-add
     */
    @PostMapping("/{id}/experts/batch-add")
    public ResponseEntity<ApiResponse<BulkAddExpertsByDomainResponse>> batchAddExperts(
            @PathVariable Long id,
            @RequestBody BulkAddExpertsByDomainRequest body) {
        return ApiResponses.ok(expertService.bulkAddExpertsByDomain(id, body.getOwnerIds()));
    }

    /**
     * 删除领域
     * DELETE /api/domains/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDomain(@PathVariable Long id) {
        domainService.delete(id);
        return ApiResponses.noContent("领域删除成功");
    }

    /**
     * 增加领域专家计数
     * PUT /api/domains/{id}/increment-expert
     */
    @PutMapping("/{id}/increment-expert")
    public ResponseEntity<ApiResponse<Domain>> incrementExpertCount(@PathVariable Long id) {
        Domain domain = domainService.incrementExpertCount(id);
        return ApiResponses.ok(domain);
    }

    /**
     * 减少领域专家计数
     * PUT /api/domains/{id}/decrement-expert
     */
    @PutMapping("/{id}/decrement-expert")
    public ResponseEntity<ApiResponse<Domain>> decrementExpertCount(@PathVariable Long id) {
        Domain domain = domainService.decrementExpertCount(id);
        return ApiResponses.ok(domain);
    }

    /**
     * 增加领域项目计数
     * PUT /api/domains/{id}/increment-project
     */
    @PutMapping("/{id}/increment-project")
    public ResponseEntity<ApiResponse<Domain>> incrementProjectCount(@PathVariable Long id) {
        Domain domain = domainService.incrementProjectCount(id);
        return ApiResponses.ok(domain);
    }

    /**
     * 减少领域项目计数
     * PUT /api/domains/{id}/decrement-project
     */
    @PutMapping("/{id}/decrement-project")
    public ResponseEntity<ApiResponse<Domain>> decrementProjectCount(@PathVariable Long id) {
        Domain domain = domainService.decrementProjectCount(id);
        return ApiResponses.ok(domain);
    }

    /**
     * 统计所有领域数量
     * GET /api/domains/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countAllDomains() {
        long count = domainService.countAllDomains();
        return ApiResponses.ok(count);
    }

    /**
     * 统计子领域数量
     * GET /api/domains/sub-count
     */
    @GetMapping("/sub-count")
    public ResponseEntity<ApiResponse<Long>> countSubDomains() {
        long count = domainService.countSubDomains();
        return ApiResponses.ok(count);
    }

    /**
     * 通过关键词搜索领域（统一搜索接口）
     * GET /api/domains/search-by-keyword
     */
    @GetMapping("/search-by-keyword")
    public ResponseEntity<ApiResponse<PaginatedResponse<Domain>>> searchByKeyword(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Domain> domains = domainService.searchByKeyword(keyword, pageable);
        return ApiResponses.okPage(domains);
    }
}