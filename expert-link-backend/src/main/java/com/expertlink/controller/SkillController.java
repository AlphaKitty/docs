package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.dto.skill.CreateSkillRequest;
import com.expertlink.domain.Skill;
import com.expertlink.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /**
     * 获取所有技能（分页）
     * GET /api/skills
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Skill>>> getAllSkills(Pageable pageable) {
        Page<Skill> skills = skillService.findAll(pageable);
        return ApiResponses.okPage(skills);
    }

    /**
     * 根据ID获取技能
     * GET /api/skills/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Skill>> getSkillById(@PathVariable Long id) {
        Skill skill = skillService.findById(id);
        return ApiResponses.ok(skill);
    }

    /**
     * 根据名称获取技能
     * GET /api/skills/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Skill>> getSkillByName(@PathVariable String name) {
        Skill skill = skillService.findByName(name);
        return ApiResponses.ok(skill);
    }

    /**
     * 根据名称关键词搜索技能
     * GET /api/skills/search/name
     */
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<Skill>>> searchSkillsByName(@RequestParam String keyword) {
        List<Skill> skills = skillService.findByNameContaining(keyword);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据类别获取技能
     * GET /api/skills/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByCategory(@PathVariable String category) {
        List<Skill> skills = skillService.findByCategory(category);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据领域ID获取技能
     * GET /api/skills/domain/{domainId}
     */
    @GetMapping("/domain/{domainId}")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByDomainId(@PathVariable Long domainId) {
        List<Skill> skills = skillService.findByDomainId(domainId);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据需求等级获取技能
     * GET /api/skills/demand-level/{demandLevel}
     */
    @GetMapping("/demand-level/{demandLevel}")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByDemandLevel(@PathVariable String demandLevel) {
        List<Skill> skills = skillService.findByDemandLevel(demandLevel);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据活跃状态获取技能
     * GET /api/skills/status/{isActive}
     */
    @GetMapping("/status/{isActive}")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByStatus(@PathVariable Boolean isActive) {
        List<Skill> skills = skillService.findByIsActive(isActive);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据关键词搜索技能（分页）
     * GET /api/skills/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Skill>>> searchSkills(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<Skill> skills = skillService.searchByKeyword(keyword, pageable);
        return ApiResponses.okPage(skills);
    }

    /**
     * 按专家数量排序查找热门技能（分页）
     * GET /api/skills/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<PaginatedResponse<Skill>>> getTopSkillsByExpertCount(Pageable pageable) {
        Page<Skill> skills = skillService.findTopSkillsByExpertCount(pageable);
        return ApiResponses.okPage(skills);
    }

    /**
     * 查找高需求技能（分页）
     * GET /api/skills/high-demand
     */
    @GetMapping("/high-demand")
    public ResponseEntity<ApiResponse<PaginatedResponse<Skill>>> getSkillsWithHighDemand(Pageable pageable) {
        Page<Skill> skills = skillService.findSkillsWithHighDemand(pageable);
        return ApiResponses.okPage(skills);
    }

    /**
     * 根据专家ID查找技能
     * GET /api/skills/expert/{expertId}
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByExpertId(@PathVariable Long expertId) {
        List<Skill> skills = skillService.findByExpertId(expertId);
        return ApiResponses.ok(skills);
    }

    /**
     * 获取活跃技能数量
     * GET /api/skills/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> countActiveSkills() {
        long count = skillService.countActiveSkills();
        return ApiResponses.ok(count);
    }

    /**
     * 获取所有活跃技能（按显示顺序和名称排序）
     * GET /api/skills/active/ordered
     */
    @GetMapping("/active/ordered")
    public ResponseEntity<ApiResponse<List<Skill>>> getAllActiveSkillsOrdered() {
        List<Skill> skills = skillService.findAllActiveSkillsOrdered();
        return ApiResponses.ok(skills);
    }

    /**
     * 根据领域ID查找活跃技能
     * GET /api/skills/active/domain/{domainId}
     */
    @GetMapping("/active/domain/{domainId}")
    public ResponseEntity<ApiResponse<List<Skill>>> getActiveSkillsByDomainId(@PathVariable Long domainId) {
        List<Skill> skills = skillService.findActiveSkillsByDomainId(domainId);
        return ApiResponses.ok(skills);
    }

    /**
     * 验证技能名称是否已存在
     * GET /api/skills/validate/name
     */
    @GetMapping("/validate/name")
    public ResponseEntity<ApiResponse<Boolean>> validateSkillName(@RequestParam String name) {
        boolean exists = skillService.existsByName(name);
        return ApiResponses.ok(exists);
    }

    /**
     * 验证英文技能名称是否已存在
     * GET /api/skills/validate/english-name
     */
    @GetMapping("/validate/english-name")
    public ResponseEntity<ApiResponse<Boolean>> validateEnglishSkillName(@RequestParam String englishName) {
        boolean exists = skillService.existsByEnglishName(englishName);
        return ApiResponses.ok(exists);
    }

    /**
     * 根据专家数量范围查找技能
     * GET /api/skills/expert-count-range
     */
    @GetMapping("/expert-count-range")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByExpertCountRange(
            @RequestParam Integer minCount,
            @RequestParam Integer maxCount) {
        List<Skill> skills = skillService.findByExpertCountRange(minCount, maxCount);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据最小专家数量查找技能
     * GET /api/skills/min-expert-count
     */
    @GetMapping("/min-expert-count")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByMinExpertCount(@RequestParam Integer minExpertCount) {
        List<Skill> skills = skillService.findByMinExpertCount(minExpertCount);
        return ApiResponses.ok(skills);
    }

    /**
     * 根据类别列表查找技能
     * POST /api/skills/by-categories
     */
    @PostMapping("/by-categories")
    public ResponseEntity<ApiResponse<List<Skill>>> getSkillsByCategories(@RequestBody List<String> categories) {
        List<Skill> skills = skillService.findByCategories(categories);
        return ApiResponses.ok(skills);
    }

    /**
     * 创建技能
     * POST /api/skills
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Skill>> createSkill(@RequestBody CreateSkillRequest request) {
        Skill createdSkill = skillService.create(request);
        return ApiResponses.created(createdSkill);
    }

    /**
     * 更新技能
     * PUT /api/skills/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Skill>> updateSkill(
            @PathVariable Long id,
            @RequestBody CreateSkillRequest request) {
        Skill updatedSkill = skillService.update(id, request);
        return ApiResponses.ok(updatedSkill);
    }

    /**
     * 删除技能
     * DELETE /api/skills/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        skillService.delete(id);
        return ApiResponses.noContent("技能删除成功");
    }

    /**
     * 增加技能专家计数
     * PUT /api/skills/{id}/increment-expert
     */
    @PutMapping("/{id}/increment-expert")
    public ResponseEntity<ApiResponse<Skill>> incrementExpertCount(@PathVariable Long id) {
        Skill skill = skillService.incrementExpertCount(id);
        return ApiResponses.ok(skill);
    }

    /**
     * 减少技能专家计数
     * PUT /api/skills/{id}/decrement-expert
     */
    @PutMapping("/{id}/decrement-expert")
    public ResponseEntity<ApiResponse<Skill>> decrementExpertCount(@PathVariable Long id) {
        Skill skill = skillService.decrementExpertCount(id);
        return ApiResponses.ok(skill);
    }
}