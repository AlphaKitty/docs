package com.expertlink.service;

import com.expertlink.domain.Project;
import com.expertlink.domain.Domain;
import com.expertlink.domain.User;
import com.expertlink.repository.ProjectRepository;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.UserRepository;
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
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DomainRepository domainRepository;
    private final UserRepository userRepository;

    /**
     * 获取所有项目（分页）
     */
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    /**
     * 根据ID查找项目
     */
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在，ID: " + id));
    }

    /**
     * 根据项目编码查找项目
     */
    public Optional<Project> findByProjectCode(String projectCode) {
        return projectRepository.findByProjectCode(projectCode);
    }

    /**
     * 根据项目名称查找项目
     */
    public List<Project> findByNameContaining(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * 根据状态查找项目
     */
    public Page<Project> findByStatus(String status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable);
    }

    /**
     * 创建新项目
     */
    @Transactional
    public Project create(Project project, Long domainId, Long createdById) {
        // 验证领域存在
        if (domainId != null) {
            Domain domain = domainRepository.findById(domainId)
                    .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + domainId));
            project.setDomain(domain);
        }

        // 验证创建者存在
        if (createdById != null) {
            User createdBy = userRepository.findById(createdById)
                    .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + createdById));
            project.setCreatedById(createdById);
        }

        // 设置默认值
        if (project.getStatus() == null) {
            project.setStatus("DRAFT");
        }
        if (project.getCompletionPercentage() == null) {
            project.setCompletionPercentage(0);
        }
        if (project.getPriority() == null) {
            project.setPriority("MEDIUM");
        }
        if (project.getComplexityLevel() == null) {
            project.setComplexityLevel("MEDIUM");
        }
        if (project.getRiskLevel() == null) {
            project.setRiskLevel("MEDIUM");
        }
        
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    /**
     * 更新项目信息
     */
    @Transactional
    public Project update(Long id, Project projectDetails, Long domainId, Long lastUpdatedById) {
        Project existingProject = findById(id);

        // 更新基本信息
        if (projectDetails.getName() != null) {
            existingProject.setName(projectDetails.getName());
        }
        if (projectDetails.getEnglishName() != null) {
            existingProject.setEnglishName(projectDetails.getEnglishName());
        }
        if (projectDetails.getDescription() != null) {
            existingProject.setDescription(projectDetails.getDescription());
        }
        if (projectDetails.getProjectCode() != null) {
            existingProject.setProjectCode(projectDetails.getProjectCode());
        }
        if (projectDetails.getProjectType() != null) {
            existingProject.setProjectType(projectDetails.getProjectType());
        }
        if (projectDetails.getClientName() != null) {
            existingProject.setClientName(projectDetails.getClientName());
        }
        if (projectDetails.getClientIndustry() != null) {
            existingProject.setClientIndustry(projectDetails.getClientIndustry());
        }
        if (projectDetails.getClientContact() != null) {
            existingProject.setClientContact(projectDetails.getClientContact());
        }
        if (projectDetails.getClientEmail() != null) {
            existingProject.setClientEmail(projectDetails.getClientEmail());
        }
        if (projectDetails.getClientPhone() != null) {
            existingProject.setClientPhone(projectDetails.getClientPhone());
        }
        if (projectDetails.getStartDate() != null) {
            existingProject.setStartDate(projectDetails.getStartDate());
        }
        if (projectDetails.getEndDate() != null) {
            existingProject.setEndDate(projectDetails.getEndDate());
        }
        if (projectDetails.getEstimatedDurationDays() != null) {
            existingProject.setEstimatedDurationDays(projectDetails.getEstimatedDurationDays());
        }
        if (projectDetails.getActualStartDate() != null) {
            existingProject.setActualStartDate(projectDetails.getActualStartDate());
        }
        if (projectDetails.getActualEndDate() != null) {
            existingProject.setActualEndDate(projectDetails.getActualEndDate());
        }
        if (projectDetails.getBudget() != null) {
            existingProject.setBudget(projectDetails.getBudget());
        }
        if (projectDetails.getActualCost() != null) {
            existingProject.setActualCost(projectDetails.getActualCost());
        }
        if (projectDetails.getStatus() != null) {
            existingProject.setStatus(projectDetails.getStatus());
        }
        if (projectDetails.getPriority() != null) {
            existingProject.setPriority(projectDetails.getPriority());
        }
        if (projectDetails.getComplexityLevel() != null) {
            existingProject.setComplexityLevel(projectDetails.getComplexityLevel());
        }
        if (projectDetails.getRiskLevel() != null) {
            existingProject.setRiskLevel(projectDetails.getRiskLevel());
        }
        if (projectDetails.getSuccessCriteria() != null) {
            existingProject.setSuccessCriteria(projectDetails.getSuccessCriteria());
        }
        if (projectDetails.getKeyDeliverables() != null) {
            existingProject.setKeyDeliverables(projectDetails.getKeyDeliverables());
        }
        if (projectDetails.getMilestones() != null) {
            existingProject.setMilestones(projectDetails.getMilestones());
        }
        if (projectDetails.getDocumentationUrl() != null) {
            existingProject.setDocumentationUrl(projectDetails.getDocumentationUrl());
        }
        if (projectDetails.getProjectManager() != null) {
            existingProject.setProjectManager(projectDetails.getProjectManager());
        }
        if (projectDetails.getTechnicalLead() != null) {
            existingProject.setTechnicalLead(projectDetails.getTechnicalLead());
        }
        if (projectDetails.getQaLead() != null) {
            existingProject.setQaLead(projectDetails.getQaLead());
        }
        if (projectDetails.getCompletionPercentage() != null) {
            existingProject.setCompletionPercentage(projectDetails.getCompletionPercentage());
        }
        if (projectDetails.getQualityScore() != null) {
            existingProject.setQualityScore(projectDetails.getQualityScore());
        }
        if (projectDetails.getClientSatisfactionScore() != null) {
            existingProject.setClientSatisfactionScore(projectDetails.getClientSatisfactionScore());
        }

        // 更新领域
        if (domainId != null) {
            Domain domain = domainRepository.findById(domainId)
                    .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + domainId));
            existingProject.setDomain(domain);
        }

        // 更新最后更新者
        if (lastUpdatedById != null) {
            existingProject.setLastUpdatedById(lastUpdatedById);
        }

        existingProject.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(existingProject);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void delete(Long id) {
        Project project = findById(id);
        // 检查是否有关联专家
        if (!project.getProjectExperts().isEmpty()) {
            throw new RuntimeException("无法删除项目，该项目有关联专家");
        }
        
        projectRepository.delete(project);
        log.info("删除项目，ID: {}", id);
    }

    /**
     * 搜索项目
     */
    public Page<Project> searchByKeyword(String keyword, Pageable pageable) {
        return projectRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 根据领域查找项目
     */
    public List<Project> findByDomainId(Long domainId) {
        return projectRepository.findByDomainId(domainId);
    }

    /**
     * 根据预算范围查找项目
     */
    public List<Project> findByBudgetBetween(BigDecimal minBudget, BigDecimal maxBudget) {
        return projectRepository.findByBudgetBetween(minBudget, maxBudget);
    }

    /**
     * 根据创建时间范围查找项目
     */
    public List<Project> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return projectRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * 根据状态统计项目数量
     */
    public long countByStatus(String status) {
        return projectRepository.countByStatus(status);
    }

    /**
     * 查找逾期项目
     */
    public List<Project> findOverdueProjects(String status) {
        // 使用当前日期作为截止日期
        LocalDateTime deadline = LocalDateTime.now();
        return projectRepository.findOverdueProjects(status, deadline);
    }

    /**
     * 更新项目状态
     */
    @Transactional
    public Project updateStatus(Long id, String status) {
        Project project = findById(id);
        project.setStatus(status);
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * 更新项目进度
     */
    @Transactional
    public Project updateCompletionPercentage(Long id, Integer completionPercentage) {
        Project project = findById(id);
        if (completionPercentage < 0 || completionPercentage > 100) {
            throw new RuntimeException("完成百分比必须在0-100之间");
        }
        project.setCompletionPercentage(completionPercentage);
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * 更新项目质量评分
     */
    @Transactional
    public Project updateQualityScore(Long id, BigDecimal qualityScore) {
        Project project = findById(id);
        if (qualityScore.compareTo(BigDecimal.ZERO) < 0 || qualityScore.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new RuntimeException("质量评分必须在0-5之间");
        }
        project.setQualityScore(qualityScore);
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * 更新客户满意度评分
     */
    @Transactional
    public Project updateClientSatisfactionScore(Long id, BigDecimal satisfactionScore) {
        Project project = findById(id);
        if (satisfactionScore.compareTo(BigDecimal.ZERO) < 0 || satisfactionScore.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new RuntimeException("客户满意度评分必须在0-5之间");
        }
        project.setClientSatisfactionScore(satisfactionScore);
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * 更新项目预算
     */
    @Transactional
    public Project updateBudget(Long id, BigDecimal budget) {
        Project project = findById(id);
        project.setBudget(budget);
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * 更新实际成本
     */
    @Transactional
    public Project updateActualCost(Long id, BigDecimal actualCost) {
        Project project = findById(id);
        project.setActualCost(actualCost);
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    /**
     * 检查项目是否活跃
     */
    public boolean isProjectActive(Long id) {
        Project project = findById(id);
        return project.isActive();
    }

    /**
     * 检查项目是否已完成
     */
    public boolean isProjectCompleted(Long id) {
        Project project = findById(id);
        return project.isCompleted();
    }

    /**
     * 检查项目是否逾期
     */
    public boolean isProjectOverdue(Long id) {
        Project project = findById(id);
        return project.isOverdue();
    }

    /**
     * 获取活跃项目数量
     */
    public long countActiveProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().filter(Project::isActive).count();
    }

    /**
     * 获取已完成项目数量
     */
    public long countCompletedProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().filter(Project::isCompleted).count();
    }

    /**
     * 获取逾期项目数量
     */
    public long countOverdueProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().filter(Project::isOverdue).count();
    }
}