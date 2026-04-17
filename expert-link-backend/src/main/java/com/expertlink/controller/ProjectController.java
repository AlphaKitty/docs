package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.domain.Project;
import com.expertlink.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 获取所有项目（分页）
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<Project>>> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectService.findAll(pageable);
        return ApiResponses.okPage(projects);
    }

    /**
     * 根据ID获取项目
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Long id) {
        Project project = projectService.findById(id);
        return ApiResponses.ok(project);
    }

    /**
     * 根据项目编码获取项目
     * GET /api/projects/code/{projectCode}
     */
    @GetMapping("/code/{projectCode}")
    public ResponseEntity<ApiResponse<Project>> getProjectByCode(@PathVariable String projectCode) {
        Project project = projectService.findByProjectCode(projectCode)
                .orElseThrow(() -> new RuntimeException("项目不存在，编码: " + projectCode));
        return ApiResponses.ok(project);
    }

    /**
     * 根据项目名称查找项目
     * GET /api/projects/search/name?name={name}
     */
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<Project>>> searchProjectsByName(@RequestParam String name) {
        List<Project> projects = projectService.findByNameContaining(name);
        return ApiResponses.ok(projects);
    }

    /**
     * 根据状态获取项目
     * GET /api/projects/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PaginatedResponse<Project>>> getProjectsByStatus(@PathVariable String status, Pageable pageable) {
        Page<Project> projects = projectService.findByStatus(status, pageable);
        return ApiResponses.okPage(projects);
    }

    /**
     * 创建项目
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Project>> createProject(
            @RequestBody Project project,
            @RequestParam(required = false) Long domainId,
            @RequestParam(required = false) Long createdById) {
        Project createdProject = projectService.create(project, domainId, createdById);
        return ApiResponses.created(createdProject);
    }

    /**
     * 更新项目信息
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @PathVariable Long id,
            @RequestBody Project projectDetails,
            @RequestParam(required = false) Long domainId,
            @RequestParam(required = false) Long lastUpdatedById) {
        Project updatedProject = projectService.update(id, projectDetails, domainId, lastUpdatedById);
        return ApiResponses.ok(updatedProject);
    }

    /**
     * 删除项目
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ApiResponses.noContent("项目删除成功");
    }

    /**
     * 搜索项目（关键词）
     * GET /api/projects/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<Project>>> searchProjects(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<Project> projects = projectService.searchByKeyword(keyword, pageable);
        return ApiResponses.okPage(projects);
    }

    /**
     * 根据领域查找项目
     * GET /api/projects/domain/{domainId}
     */
    @GetMapping("/domain/{domainId}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByDomain(@PathVariable Long domainId) {
        List<Project> projects = projectService.findByDomainId(domainId);
        return ApiResponses.ok(projects);
    }

    /**
     * 根据预算范围查找项目
     * GET /api/projects/budget-range
     */
    @GetMapping("/budget-range")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByBudgetRange(
            @RequestParam BigDecimal minBudget,
            @RequestParam BigDecimal maxBudget) {
        List<Project> projects = projectService.findByBudgetBetween(minBudget, maxBudget);
        return ApiResponses.ok(projects);
    }

    /**
     * 根据创建时间范围查找项目
     * GET /api/projects/created-between
     */
    @GetMapping("/created-between")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByCreatedAtRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<Project> projects = projectService.findByCreatedAtBetween(startDate, endDate);
        return ApiResponses.ok(projects);
    }

    /**
     * 根据状态统计项目数量
     * GET /api/projects/count/status/{status}
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<ApiResponse<Long>> countProjectsByStatus(@PathVariable String status) {
        long count = projectService.countByStatus(status);
        return ApiResponses.ok(count);
    }

    /**
     * 查找逾期项目
     * GET /api/projects/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<Project>>> getOverdueProjects(@RequestParam(required = false) String status) {
        List<Project> projects = projectService.findOverdueProjects(status != null ? status : "IN_PROGRESS");
        return ApiResponses.ok(projects);
    }

    /**
     * 更新项目状态
     * PUT /api/projects/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Project>> updateProjectStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Project project = projectService.updateStatus(id, status);
        return ApiResponses.ok(project);
    }

    /**
     * 更新项目进度
     * PUT /api/projects/{id}/completion
     */
    @PutMapping("/{id}/completion")
    public ResponseEntity<ApiResponse<Project>> updateProjectCompletionPercentage(
            @PathVariable Long id,
            @RequestParam Integer completionPercentage) {
        Project project = projectService.updateCompletionPercentage(id, completionPercentage);
        return ApiResponses.ok(project);
    }

    /**
     * 更新项目质量评分
     * PUT /api/projects/{id}/quality
     */
    @PutMapping("/{id}/quality")
    public ResponseEntity<ApiResponse<Project>> updateProjectQualityScore(
            @PathVariable Long id,
            @RequestParam BigDecimal qualityScore) {
        Project project = projectService.updateQualityScore(id, qualityScore);
        return ApiResponses.ok(project);
    }

    /**
     * 更新客户满意度评分
     * PUT /api/projects/{id}/client-satisfaction
     */
    @PutMapping("/{id}/client-satisfaction")
    public ResponseEntity<ApiResponse<Project>> updateProjectClientSatisfactionScore(
            @PathVariable Long id,
            @RequestParam BigDecimal satisfactionScore) {
        Project project = projectService.updateClientSatisfactionScore(id, satisfactionScore);
        return ApiResponses.ok(project);
    }

    /**
     * 更新项目预算
     * PUT /api/projects/{id}/budget
     */
    @PutMapping("/{id}/budget")
    public ResponseEntity<ApiResponse<Project>> updateProjectBudget(
            @PathVariable Long id,
            @RequestParam BigDecimal budget) {
        Project project = projectService.updateBudget(id, budget);
        return ApiResponses.ok(project);
    }

    /**
     * 更新实际成本
     * PUT /api/projects/{id}/actual-cost
     */
    @PutMapping("/{id}/actual-cost")
    public ResponseEntity<ApiResponse<Project>> updateProjectActualCost(
            @PathVariable Long id,
            @RequestParam BigDecimal actualCost) {
        Project project = projectService.updateActualCost(id, actualCost);
        return ApiResponses.ok(project);
    }

    /**
     * 检查项目是否活跃
     * GET /api/projects/{id}/active
     */
    @GetMapping("/{id}/active")
    public ResponseEntity<ApiResponse<Boolean>> isProjectActive(@PathVariable Long id) {
        boolean isActive = projectService.isProjectActive(id);
        return ApiResponses.ok(isActive);
    }

    /**
     * 检查项目是否已完成
     * GET /api/projects/{id}/completed
     */
    @GetMapping("/{id}/completed")
    public ResponseEntity<ApiResponse<Boolean>> isProjectCompleted(@PathVariable Long id) {
        boolean isCompleted = projectService.isProjectCompleted(id);
        return ApiResponses.ok(isCompleted);
    }

    /**
     * 检查项目是否逾期
     * GET /api/projects/{id}/overdue
     */
    @GetMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<Boolean>> isProjectOverdue(@PathVariable Long id) {
        boolean isOverdue = projectService.isProjectOverdue(id);
        return ApiResponses.ok(isOverdue);
    }

    /**
     * 获取活跃项目数量
     * GET /api/projects/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> countActiveProjects() {
        long count = projectService.countActiveProjects();
        return ApiResponses.ok(count);
    }

    /**
     * 获取已完成项目数量
     * GET /api/projects/count/completed
     */
    @GetMapping("/count/completed")
    public ResponseEntity<ApiResponse<Long>> countCompletedProjects() {
        long count = projectService.countCompletedProjects();
        return ApiResponses.ok(count);
    }

    /**
     * 获取逾期项目数量
     * GET /api/projects/count/overdue
     */
    @GetMapping("/count/overdue")
    public ResponseEntity<ApiResponse<Long>> countOverdueProjects() {
        long count = projectService.countOverdueProjects();
        return ApiResponses.ok(count);
    }
}