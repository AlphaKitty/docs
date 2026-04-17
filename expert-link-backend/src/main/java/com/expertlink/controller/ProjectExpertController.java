package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.domain.ProjectExpert;
import com.expertlink.service.ProjectExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/project-experts")
@RequiredArgsConstructor
public class ProjectExpertController {

    private final ProjectExpertService projectExpertService;

    /**
     * 获取所有项目专家关联（分页）
     * GET /api/project-experts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProjectExpert>>> getAllProjectExperts(Pageable pageable) {
        Page<ProjectExpert> projectExperts = projectExpertService.findAll(pageable);
        return ApiResponses.okPage(projectExperts);
    }

    /**
     * 根据ID获取项目专家关联
     * GET /api/project-experts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectExpert>> getProjectExpertById(@PathVariable Long id) {
        ProjectExpert projectExpert = projectExpertService.findById(id);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 根据项目ID获取关联
     * GET /api/project-experts/project/{projectId}
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByProjectId(@PathVariable Long projectId) {
        List<ProjectExpert> projectExperts = projectExpertService.findByProjectId(projectId);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据专家ID获取关联
     * GET /api/project-experts/expert/{expertId}
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByExpertId(@PathVariable Long expertId) {
        List<ProjectExpert> projectExperts = projectExpertService.findByExpertId(expertId);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据项目和专家ID获取关联
     * GET /api/project-experts/project/{projectId}/expert/{expertId}
     */
    @GetMapping("/project/{projectId}/expert/{expertId}")
    public ResponseEntity<ApiResponse<ProjectExpert>> getProjectExpertByProjectIdAndExpertId(
            @PathVariable Long projectId,
            @PathVariable Long expertId) {
        ProjectExpert projectExpert = projectExpertService.findByProjectIdAndExpertId(projectId, expertId);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 根据状态获取关联
     * GET /api/project-experts/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByStatus(@PathVariable String status) {
        List<ProjectExpert> projectExperts = projectExpertService.findByStatus(status);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据角色获取关联
     * GET /api/project-experts/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByRole(@PathVariable String role) {
        List<ProjectExpert> projectExperts = projectExpertService.findByRole(role);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据时薪范围获取关联
     * GET /api/project-experts/hourly-rate-range
     */
    @GetMapping("/hourly-rate-range")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByHourlyRateRange(
            @RequestParam BigDecimal minRate,
            @RequestParam BigDecimal maxRate) {
        List<ProjectExpert> projectExperts = projectExpertService.findByHourlyRateBetween(minRate, maxRate);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据绩效评分范围获取关联
     * GET /api/project-experts/performance-rating-range
     */
    @GetMapping("/performance-rating-range")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByPerformanceRatingRange(
            @RequestParam BigDecimal minRating,
            @RequestParam BigDecimal maxRating) {
        List<ProjectExpert> projectExperts = projectExpertService.findByPerformanceRatingBetween(minRating, maxRating);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据项目ID和状态获取关联
     * GET /api/project-experts/project/{projectId}/status/{status}
     */
    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByProjectIdAndStatus(
            @PathVariable Long projectId,
            @PathVariable String status) {
        List<ProjectExpert> projectExperts = projectExpertService.findByProjectIdAndStatus(projectId, status);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据专家ID和状态获取关联
     * GET /api/project-experts/expert/{expertId}/status/{status}
     */
    @GetMapping("/expert/{expertId}/status/{status}")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByExpertIdAndStatus(
            @PathVariable Long expertId,
            @PathVariable String status) {
        List<ProjectExpert> projectExperts = projectExpertService.findByExpertIdAndStatus(expertId, status);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取项目的活跃分配
     * GET /api/project-experts/project/{projectId}/active
     */
    @GetMapping("/project/{projectId}/active")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getActiveAssignmentsByProjectId(@PathVariable Long projectId) {
        List<ProjectExpert> projectExperts = projectExpertService.findActiveAssignmentsByProjectId(projectId);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取专家的活跃分配
     * GET /api/project-experts/expert/{expertId}/active
     */
    @GetMapping("/expert/{expertId}/active")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getActiveAssignmentsByExpertId(@PathVariable Long expertId) {
        List<ProjectExpert> projectExperts = projectExpertService.findActiveAssignmentsByExpertId(expertId);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 统计项目的专家数量
     * GET /api/project-experts/project/{projectId}/count
     */
    @GetMapping("/project/{projectId}/count")
    public ResponseEntity<ApiResponse<Long>> countByProjectId(@PathVariable Long projectId) {
        long count = projectExpertService.countByProjectId(projectId);
        return ApiResponses.ok(count);
    }

    /**
     * 统计专家的项目数量
     * GET /api/project-experts/expert/{expertId}/count
     */
    @GetMapping("/expert/{expertId}/count")
    public ResponseEntity<ApiResponse<Long>> countByExpertId(@PathVariable Long expertId) {
        long count = projectExpertService.countByExpertId(expertId);
        return ApiResponses.ok(count);
    }

    /**
     * 根据开始日期范围获取关联
     * GET /api/project-experts/date-range
     */
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByStartDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<ProjectExpert> projectExperts = projectExpertService.findByStartDateBetween(startDate, endDate);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取项目负责人分配
     * GET /api/project-experts/project/{projectId}/lead
     */
    @GetMapping("/project/{projectId}/lead")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getLeadAssignmentsByProjectId(@PathVariable Long projectId) {
        List<ProjectExpert> projectExperts = projectExpertService.findLeadAssignmentsByProjectId(projectId);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取项目主要联系人分配
     * GET /api/project-experts/project/{projectId}/primary-contact
     */
    @GetMapping("/project/{projectId}/primary-contact")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getPrimaryContactAssignmentsByProjectId(@PathVariable Long projectId) {
        List<ProjectExpert> projectExperts = projectExpertService.findPrimaryContactAssignmentsByProjectId(projectId);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取所有项目负责人分配
     * GET /api/project-experts/lead/all
     */
    @GetMapping("/lead/all")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getAllLeadAssignments() {
        List<ProjectExpert> projectExperts = projectExpertService.findAllLeadAssignments();
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取所有主要联系人分配
     * GET /api/project-experts/primary-contact/all
     */
    @GetMapping("/primary-contact/all")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getAllPrimaryContactAssignments() {
        List<ProjectExpert> projectExperts = projectExpertService.findAllPrimaryContactAssignments();
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 根据最小完成百分比获取关联
     * GET /api/project-experts/completion-percentage
     */
    @GetMapping("/completion-percentage")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByMinCompletionPercentage(
            @RequestParam Integer minPercentage) {
        List<ProjectExpert> projectExperts = projectExpertService.findByMinCompletionPercentage(minPercentage);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 获取有小时记录的关联
     * GET /api/project-experts/with-hours
     */
    @GetMapping("/with-hours")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsWithHoursLogged() {
        List<ProjectExpert> projectExperts = projectExpertService.findWithHoursLogged();
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 检查项目专家关联是否存在
     * GET /api/project-experts/exists
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> existsByProjectIdAndExpertId(
            @RequestParam Long projectId,
            @RequestParam Long expertId) {
        boolean exists = projectExpertService.existsByProjectIdAndExpertId(projectId, expertId);
        return ApiResponses.ok(exists);
    }

    /**
     * 根据状态列表获取关联
     * POST /api/project-experts/by-statuses
     */
    @PostMapping("/by-statuses")
    public ResponseEntity<ApiResponse<List<ProjectExpert>>> getProjectExpertsByStatuses(@RequestBody List<String> statuses) {
        List<ProjectExpert> projectExperts = projectExpertService.findByStatusIn(statuses);
        return ApiResponses.ok(projectExperts);
    }

    /**
     * 创建项目专家关联
     * POST /api/project-experts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectExpert>> createProjectExpert(
            @RequestBody ProjectExpert projectExpert,
            @RequestParam Long projectId,
            @RequestParam Long expertId) {
        ProjectExpert createdProjectExpert = projectExpertService.create(projectExpert, projectId, expertId);
        return ApiResponses.created(createdProjectExpert);
    }

    /**
     * 更新项目专家关联
     * PUT /api/project-experts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectExpert>> updateProjectExpert(
            @PathVariable Long id,
            @RequestBody ProjectExpert projectExpertDetails,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long expertId) {
        ProjectExpert updatedProjectExpert = projectExpertService.update(id, projectExpertDetails, projectId, expertId);
        return ApiResponses.ok(updatedProjectExpert);
    }

    /**
     * 删除项目专家关联
     * DELETE /api/project-experts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProjectExpert(@PathVariable Long id) {
        projectExpertService.delete(id);
        return ApiResponses.noContent("项目专家关联删除成功");
    }

    /**
     * 计算总成本
     * PUT /api/project-experts/{id}/calculate-total-cost
     */
    @PutMapping("/{id}/calculate-total-cost")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateTotalCost(@PathVariable Long id) {
        BigDecimal totalCost = projectExpertService.calculateTotalCost(id);
        return ApiResponses.ok(totalCost);
    }

    /**
     * 更新状态
     * PUT /api/project-experts/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProjectExpert>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        ProjectExpert projectExpert = projectExpertService.updateStatus(id, status);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 更新绩效评分
     * PUT /api/project-experts/{id}/performance-rating
     */
    @PutMapping("/{id}/performance-rating")
    public ResponseEntity<ApiResponse<ProjectExpert>> updatePerformanceRating(
            @PathVariable Long id,
            @RequestParam BigDecimal rating) {
        ProjectExpert projectExpert = projectExpertService.updatePerformanceRating(id, rating);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 更新完成百分比
     * PUT /api/project-experts/{id}/completion-percentage
     */
    @PutMapping("/{id}/completion-percentage")
    public ResponseEntity<ApiResponse<ProjectExpert>> updateCompletionPercentage(
            @PathVariable Long id,
            @RequestParam Integer percentage) {
        ProjectExpert projectExpert = projectExpertService.updateCompletionPercentage(id, percentage);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 增加已记录小时数
     * PUT /api/project-experts/{id}/add-hours
     */
    @PutMapping("/{id}/add-hours")
    public ResponseEntity<ApiResponse<ProjectExpert>> addHours(
            @PathVariable Long id,
            @RequestParam Integer hours) {
        ProjectExpert projectExpert = projectExpertService.addHours(id, hours);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 更新反馈信息
     * PUT /api/project-experts/{id}/feedback
     */
    @PutMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<ProjectExpert>> updateFeedback(
            @PathVariable Long id,
            @RequestParam String feedback,
            @RequestParam Long reviewedById) {
        ProjectExpert projectExpert = projectExpertService.updateFeedback(id, feedback, reviewedById);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 设置项目负责人
     * PUT /api/project-experts/{id}/set-lead
     */
    @PutMapping("/{id}/set-lead")
    public ResponseEntity<ApiResponse<ProjectExpert>> setAsLead(
            @PathVariable Long id,
            @RequestParam boolean isLead) {
        ProjectExpert projectExpert = projectExpertService.setAsLead(id, isLead);
        return ApiResponses.ok(projectExpert);
    }

    /**
     * 设置主要联系人
     * PUT /api/project-experts/{id}/set-primary-contact
     */
    @PutMapping("/{id}/set-primary-contact")
    public ResponseEntity<ApiResponse<ProjectExpert>> setAsPrimaryContact(
            @PathVariable Long id,
            @RequestParam boolean isPrimaryContact) {
        ProjectExpert projectExpert = projectExpertService.setAsPrimaryContact(id, isPrimaryContact);
        return ApiResponses.ok(projectExpert);
    }
}