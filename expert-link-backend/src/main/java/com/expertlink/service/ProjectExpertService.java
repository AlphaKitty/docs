package com.expertlink.service;

import com.expertlink.domain.ProjectExpert;
import com.expertlink.domain.Project;
import com.expertlink.domain.Expert;
import com.expertlink.repository.ProjectExpertRepository;
import com.expertlink.repository.ProjectRepository;
import com.expertlink.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectExpertService {

    private final ProjectExpertRepository projectExpertRepository;
    private final ProjectRepository projectRepository;
    private final ExpertRepository expertRepository;

    /**
     * 分页获取所有项目专家关联
     */
    public Page<ProjectExpert> findAll(Pageable pageable) {
        return projectExpertRepository.findAll(pageable);
    }

    /**
     * 根据ID查找项目专家关联
     */
    public ProjectExpert findById(Long id) {
        return projectExpertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目专家关联不存在，ID: " + id));
    }

    /**
     * 根据项目ID查找关联
     */
    public List<ProjectExpert> findByProjectId(Long projectId) {
        return projectExpertRepository.findByProjectId(projectId);
    }

    /**
     * 根据专家ID查找关联
     */
    public List<ProjectExpert> findByExpertId(Long expertId) {
        return projectExpertRepository.findByExpertId(expertId);
    }

    /**
     * 根据项目和专家ID查找关联
     */
    public ProjectExpert findByProjectIdAndExpertId(Long projectId, Long expertId) {
        return projectExpertRepository.findByProjectIdAndExpertId(projectId, expertId)
                .orElseThrow(() -> new RuntimeException("项目专家关联不存在，项目ID: " + projectId + ", 专家ID: " + expertId));
    }

    /**
     * 根据状态查找关联
     */
    public List<ProjectExpert> findByStatus(String status) {
        return projectExpertRepository.findByStatus(status);
    }

    /**
     * 根据角色查找关联
     */
    public List<ProjectExpert> findByRole(String role) {
        return projectExpertRepository.findByRole(role);
    }

    /**
     * 根据时薪范围查找关联
     */
    public List<ProjectExpert> findByHourlyRateBetween(BigDecimal minRate, BigDecimal maxRate) {
        return projectExpertRepository.findByHourlyRateRange(minRate, maxRate);
    }

    /**
     * 根据绩效评分范围查找关联
     */
    public List<ProjectExpert> findByPerformanceRatingBetween(BigDecimal minRating, BigDecimal maxRating) {
        // 由于Repository没有直接的between方法，我们使用自定义查询
        // 这里我们使用findByMinPerformanceRating并手动过滤
        List<ProjectExpert> experts = projectExpertRepository.findByMinPerformanceRating(minRating);
        return experts.stream()
                .filter(pe -> pe.getPerformanceRating() != null && pe.getPerformanceRating().compareTo(maxRating) <= 0)
                .toList();
    }

    /**
     * 创建新关联
     */
    @Transactional
    public ProjectExpert create(ProjectExpert projectExpert, Long projectId, Long expertId) {
        // 验证项目存在
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
        
        // 验证专家存在
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("专家不存在，ID: " + expertId));
        
        // 检查是否已存在关联
        if (projectExpertRepository.existsByProjectIdAndExpertId(projectId, expertId)) {
            throw new RuntimeException("项目专家关联已存在，项目ID: " + projectId + ", 专家ID: " + expertId);
        }
        
        // 设置关联
        projectExpert.setProject(project);
        projectExpert.setExpert(expert);
        
        // 设置默认值
        if (projectExpert.getStatus() == null) {
            projectExpert.setStatus("PENDING");
        }
        if (projectExpert.getCompletionPercentage() == null) {
            projectExpert.setCompletionPercentage(0);
        }
        if (projectExpert.getHoursLogged() == null) {
            projectExpert.setHoursLogged(0);
        }
        if (projectExpert.getIsLead() == null) {
            projectExpert.setIsLead(false);
        }
        if (projectExpert.getIsPrimaryContact() == null) {
            projectExpert.setIsPrimaryContact(false);
        }
        
        // 计算总成本
        projectExpert.calculateTotalCost();
        
        // 设置创建时间
        projectExpert.setCreatedAt(LocalDateTime.now());
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 更新关联信息
     */
    @Transactional
    public ProjectExpert update(Long id, ProjectExpert projectExpertDetails, Long projectId, Long expertId) {
        ProjectExpert existingProjectExpert = findById(id);
        
        // 如果提供了新的项目ID，更新项目关联
        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + projectId));
            existingProjectExpert.setProject(project);
        }
        
        // 如果提供了新的专家ID，更新专家关联
        if (expertId != null) {
            Expert expert = expertRepository.findById(expertId)
                    .orElseThrow(() -> new RuntimeException("专家不存在，ID: " + expertId));
            existingProjectExpert.setExpert(expert);
        }
        
        // 更新基本信息
        if (projectExpertDetails.getRole() != null) {
            existingProjectExpert.setRole(projectExpertDetails.getRole());
        }
        if (projectExpertDetails.getResponsibilities() != null) {
            existingProjectExpert.setResponsibilities(projectExpertDetails.getResponsibilities());
        }
        if (projectExpertDetails.getStartDate() != null) {
            existingProjectExpert.setStartDate(projectExpertDetails.getStartDate());
        }
        if (projectExpertDetails.getEndDate() != null) {
            existingProjectExpert.setEndDate(projectExpertDetails.getEndDate());
        }
        if (projectExpertDetails.getHourlyRate() != null) {
            existingProjectExpert.setHourlyRate(projectExpertDetails.getHourlyRate());
        }
        if (projectExpertDetails.getTotalHours() != null) {
            existingProjectExpert.setTotalHours(projectExpertDetails.getTotalHours());
        }
        if (projectExpertDetails.getStatus() != null) {
            existingProjectExpert.setStatus(projectExpertDetails.getStatus());
        }
        if (projectExpertDetails.getPerformanceRating() != null) {
            existingProjectExpert.setPerformanceRating(projectExpertDetails.getPerformanceRating());
        }
        if (projectExpertDetails.getFeedback() != null) {
            existingProjectExpert.setFeedback(projectExpertDetails.getFeedback());
        }
        if (projectExpertDetails.getReviewedById() != null) {
            existingProjectExpert.setReviewedById(projectExpertDetails.getReviewedById());
        }
        if (projectExpertDetails.getReviewedAt() != null) {
            existingProjectExpert.setReviewedAt(projectExpertDetails.getReviewedAt());
        }
        if (projectExpertDetails.getIsLead() != null) {
            existingProjectExpert.setIsLead(projectExpertDetails.getIsLead());
        }
        if (projectExpertDetails.getIsPrimaryContact() != null) {
            existingProjectExpert.setIsPrimaryContact(projectExpertDetails.getIsPrimaryContact());
        }
        if (projectExpertDetails.getCompletionPercentage() != null) {
            existingProjectExpert.setCompletionPercentage(projectExpertDetails.getCompletionPercentage());
        }
        if (projectExpertDetails.getHoursLogged() != null) {
            existingProjectExpert.setHoursLogged(projectExpertDetails.getHoursLogged());
        }
        
        // 重新计算总成本
        existingProjectExpert.calculateTotalCost();
        
        // 更新修改时间
        existingProjectExpert.setUpdatedAt(LocalDateTime.now());
        
        return projectExpertRepository.save(existingProjectExpert);
    }

    /**
     * 删除关联
     */
    @Transactional
    public void delete(Long id) {
        ProjectExpert projectExpert = findById(id);
        
        // 检查状态，如果项目正在进行中，不能删除
        if (projectExpert.isActive()) {
            throw new RuntimeException("无法删除活跃的项目专家关联，ID: " + id);
        }
        
        projectExpertRepository.delete(projectExpert);
        log.info("删除项目专家关联，ID: {}", id);
    }

    /**
     * 计算总成本（时薪×总小时数）
     */
    @Transactional
    public BigDecimal calculateTotalCost(Long id) {
        ProjectExpert projectExpert = findById(id);
        projectExpert.calculateTotalCost();
        projectExpertRepository.save(projectExpert);
        return projectExpert.getTotalCost();
    }

    /**
     * 更新状态
     */
    @Transactional
    public ProjectExpert updateStatus(Long id, String status) {
        ProjectExpert projectExpert = findById(id);
        
        // 验证状态转换
        validateStatusTransition(projectExpert.getStatus(), status);
        
        projectExpert.setStatus(status);
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        // 如果状态变为COMPLETED，设置完成百分比为100%
        if ("COMPLETED".equals(status) || "CLOSED".equals(status)) {
            projectExpert.setCompletionPercentage(100);
        }
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 更新绩效评分
     */
    @Transactional
    public ProjectExpert updatePerformanceRating(Long id, BigDecimal rating) {
        ProjectExpert projectExpert = findById(id);
        
        // 验证评分范围
        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5.00")) > 0) {
            throw new RuntimeException("绩效评分必须在0到5之间");
        }
        
        projectExpert.setPerformanceRating(rating);
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 更新完成百分比
     */
    @Transactional
    public ProjectExpert updateCompletionPercentage(Long id, Integer percentage) {
        ProjectExpert projectExpert = findById(id);
        
        // 验证百分比范围
        if (percentage < 0 || percentage > 100) {
            throw new RuntimeException("完成百分比必须在0到100之间");
        }
        
        projectExpert.setCompletionPercentage(percentage);
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        // 如果完成百分比达到100%，自动更新状态为COMPLETED
        if (percentage == 100 && !projectExpert.isCompleted()) {
            projectExpert.setStatus("COMPLETED");
        }
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 增加已记录小时数
     */
    @Transactional
    public ProjectExpert addHours(Long id, Integer hours) {
        if (hours <= 0) {
            throw new RuntimeException("增加的小时数必须大于0");
        }
        
        ProjectExpert projectExpert = findById(id);
        
        // 验证项目状态
        if (!projectExpert.isActive()) {
            throw new RuntimeException("无法为非活跃项目增加小时数");
        }
        
        int newHoursLogged = projectExpert.getHoursLogged() + hours;
        projectExpert.setHoursLogged(newHoursLogged);
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算总成本
        projectExpert.calculateTotalCost();
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 根据项目ID和状态查找关联
     */
    public List<ProjectExpert> findByProjectIdAndStatus(Long projectId, String status) {
        return projectExpertRepository.findByProjectIdAndStatus(projectId, status);
    }

    /**
     * 根据专家ID和状态查找关联
     */
    public List<ProjectExpert> findByExpertIdAndStatus(Long expertId, String status) {
        return projectExpertRepository.findByExpertIdAndStatus(expertId, status);
    }

    /**
     * 查找项目的活跃分配
     */
    public List<ProjectExpert> findActiveAssignmentsByProjectId(Long projectId) {
        return projectExpertRepository.findActiveAssignmentsByProjectId(projectId);
    }

    /**
     * 查找专家的活跃分配
     */
    public List<ProjectExpert> findActiveAssignmentsByExpertId(Long expertId) {
        return projectExpertRepository.findActiveAssignmentsByExpertId(expertId);
    }

    /**
     * 统计项目的专家数量
     */
    public long countByProjectId(Long projectId) {
        return projectExpertRepository.countByProjectId(projectId);
    }

    /**
     * 统计专家的项目数量
     */
    public long countByExpertId(Long expertId) {
        return projectExpertRepository.countByExpertId(expertId);
    }

    /**
     * 根据开始日期范围查找
     */
    public List<ProjectExpert> findByStartDateBetween(LocalDate start, LocalDate end) {
        // 使用Repository中的findAssignmentsBetweenDates方法
        return projectExpertRepository.findAssignmentsBetweenDates(start, end);
    }

    /**
     * 验证状态转换
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // 定义允许的状态转换
        if ("PENDING".equals(currentStatus)) {
            if (!"ACTIVE".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                throw new RuntimeException("PENDING状态只能转换为ACTIVE或CANCELLED");
            }
        } else if ("ACTIVE".equals(currentStatus)) {
            if (!"IN_PROGRESS".equals(newStatus) && !"COMPLETED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                throw new RuntimeException("ACTIVE状态只能转换为IN_PROGRESS、COMPLETED或CANCELLED");
            }
        } else if ("IN_PROGRESS".equals(currentStatus)) {
            if (!"COMPLETED".equals(newStatus) && !"CANCELLED".equals(newStatus)) {
                throw new RuntimeException("IN_PROGRESS状态只能转换为COMPLETED或CANCELLED");
            }
        } else if ("COMPLETED".equals(currentStatus) || "CLOSED".equals(currentStatus)) {
            throw new RuntimeException("COMPLETED或CLOSED状态不能再转换");
        } else if ("CANCELLED".equals(currentStatus)) {
            throw new RuntimeException("CANCELLED状态不能再转换");
        }
    }

    /**
     * 查找项目负责人分配
     */
    public List<ProjectExpert> findLeadAssignmentsByProjectId(Long projectId) {
        return projectExpertRepository.findLeadAssignmentsByProjectId(projectId);
    }

    /**
     * 查找项目主要联系人分配
     */
    public List<ProjectExpert> findPrimaryContactAssignmentsByProjectId(Long projectId) {
        return projectExpertRepository.findPrimaryContactAssignmentsByProjectId(projectId);
    }

    /**
     * 获取所有项目负责人分配
     */
    public List<ProjectExpert> findAllLeadAssignments() {
        return projectExpertRepository.findAllLeadAssignments();
    }

    /**
     * 获取所有主要联系人分配
     */
    public List<ProjectExpert> findAllPrimaryContactAssignments() {
        return projectExpertRepository.findAllPrimaryContactAssignments();
    }

    /**
     * 根据最小完成百分比查找关联
     */
    public List<ProjectExpert> findByMinCompletionPercentage(Integer minPercentage) {
        return projectExpertRepository.findByMinCompletionPercentage(minPercentage);
    }

    /**
     * 查找有小时记录的关联
     */
    public List<ProjectExpert> findWithHoursLogged() {
        return projectExpertRepository.findWithHoursLogged();
    }

    /**
     * 检查项目专家关联是否存在
     */
    public boolean existsByProjectIdAndExpertId(Long projectId, Long expertId) {
        return projectExpertRepository.existsByProjectIdAndExpertId(projectId, expertId);
    }

    /**
     * 根据状态列表查找关联
     */
    public List<ProjectExpert> findByStatusIn(List<String> statuses) {
        return projectExpertRepository.findByStatusIn(statuses);
    }

    /**
     * 更新反馈信息
     */
    @Transactional
    public ProjectExpert updateFeedback(Long id, String feedback, Long reviewedById) {
        ProjectExpert projectExpert = findById(id);
        
        projectExpert.setFeedback(feedback);
        projectExpert.setReviewedById(reviewedById);
        projectExpert.setReviewedAt(LocalDateTime.now());
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 设置项目负责人
     */
    @Transactional
    public ProjectExpert setAsLead(Long id, boolean isLead) {
        ProjectExpert projectExpert = findById(id);
        
        // 如果设置为负责人，检查项目是否已有负责人
        if (isLead) {
            List<ProjectExpert> existingLeads = findLeadAssignmentsByProjectId(projectExpert.getProject().getId());
            if (!existingLeads.isEmpty() && !existingLeads.get(0).getId().equals(id)) {
                throw new RuntimeException("项目已存在负责人");
            }
        }
        
        projectExpert.setIsLead(isLead);
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        return projectExpertRepository.save(projectExpert);
    }

    /**
     * 设置主要联系人
     */
    @Transactional
    public ProjectExpert setAsPrimaryContact(Long id, boolean isPrimaryContact) {
        ProjectExpert projectExpert = findById(id);
        
        // 如果设置为主要联系人，检查项目是否已有主要联系人
        if (isPrimaryContact) {
            List<ProjectExpert> existingContacts = findPrimaryContactAssignmentsByProjectId(projectExpert.getProject().getId());
            if (!existingContacts.isEmpty() && !existingContacts.get(0).getId().equals(id)) {
                throw new RuntimeException("项目已存在主要联系人");
            }
        }
        
        projectExpert.setIsPrimaryContact(isPrimaryContact);
        projectExpert.setUpdatedAt(LocalDateTime.now());
        
        return projectExpertRepository.save(projectExpert);
    }
}