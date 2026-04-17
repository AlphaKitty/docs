package com.expertlink.service;

import com.expertlink.domain.Domain;
import com.expertlink.domain.Expert;
import com.expertlink.domain.User;
import com.expertlink.dto.domain.DomainStatsResponse;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.ProjectRepository;
import com.expertlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainService {

    private final DomainRepository domainRepository;
    private final ExpertRepository expertRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 分页获取所有领域
     */
    public Page<Domain> findAll(Pageable pageable) {
        return domainRepository.findAll(pageable);
    }

    /**
     * 根据ID查找领域
     */
    public Domain findById(Long id) {
        return domainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + id));
    }

    public DomainStatsResponse getSubtreeStats(Long domainId) {
        Domain root = findById(domainId);
        List<Domain> all = domainRepository.findAll();
        Map<Long, List<Long>> childrenMap = new LinkedHashMap<>();
        for (Domain d : all) {
            if (d.getParentId() == null) continue;
            childrenMap.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d.getId());
        }
        Set<Long> subtree = new LinkedHashSet<>();
        collectSubtreeIds(root.getId(), childrenMap, subtree);
        long expertCount = expertRepository.countDistinctByAnyDomainIds(subtree);
        long projectCount = projectRepository.countByDomainIds(subtree);
        return DomainStatsResponse.builder()
                .domainId(root.getId())
                .subtreeDomainCount(subtree.size())
                .expertCount(expertCount)
                .projectCount(projectCount)
                .subtreeDomainIds(List.copyOf(subtree))
                .build();
    }

    public List<DomainStatsResponse> getSubtreeStatsBatch(List<Long> domainIds) {
        List<Domain> all = domainRepository.findAll();
        Map<Long, Domain> byId = new LinkedHashMap<>();
        Map<Long, List<Long>> childrenMap = new LinkedHashMap<>();
        for (Domain d : all) {
            byId.put(d.getId(), d);
            if (d.getParentId() != null) {
                childrenMap.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d.getId());
            }
        }
        List<Long> roots;
        if (domainIds == null || domainIds.isEmpty()) {
            roots = new ArrayList<>(byId.keySet());
        } else {
            roots = domainIds.stream().filter(byId::containsKey).toList();
        }
        List<DomainStatsResponse> out = new ArrayList<>();
        for (Long rootId : roots) {
            Set<Long> subtree = new LinkedHashSet<>();
            collectSubtreeIds(rootId, childrenMap, subtree);
            long expertCount = expertRepository.countDistinctByAnyDomainIds(subtree);
            long projectCount = projectRepository.countByDomainIds(subtree);
            out.add(DomainStatsResponse.builder()
                    .domainId(rootId)
                    .subtreeDomainCount(subtree.size())
                    .expertCount(expertCount)
                    .projectCount(projectCount)
                    .subtreeDomainIds(List.copyOf(subtree))
                    .build());
        }
        return out;
    }

    private void collectSubtreeIds(Long id, Map<Long, List<Long>> childrenMap, Set<Long> out) {
        if (id == null || out.contains(id)) return;
        out.add(id);
        for (Long childId : childrenMap.getOrDefault(id, List.of())) {
            collectSubtreeIds(childId, childrenMap, out);
        }
    }

    /**
     * 根据名称查找领域
     */
    public Optional<Domain> findByName(String name) {
        return domainRepository.findByName(name);
    }

    /**
     * 根据名称关键词查找领域
     */
    public Page<Domain> findByNameContaining(String keyword, Pageable pageable) {
        return domainRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 根据父级ID查找子领域
     */
    public List<Domain> findByParentId(Long parentId) {
        return domainRepository.findByParentId(parentId);
    }

    /**
     * 根据层级查找领域
     */
    public List<Domain> findByLevel(Integer level) {
        return domainRepository.findByLevel(level);
    }

    /**
     * 根据活跃状态查找领域
     */
    public List<Domain> findByIsActive(Boolean isActive) {
        return domainRepository.findByIsActive(isActive);
    }

    /**
     * 创建新领域
     */
    @Transactional
    public Domain create(Domain domain) {
        // 验证名称唯一性
        if (domainRepository.existsByName(domain.getName())) {
            throw new RuntimeException("领域名称已存在: " + domain.getName());
        }

        // 设置父级关系和层级
        if (domain.getParentId() != null) {
            Domain parent = findById(domain.getParentId());
            domain.setParent(parent);
            domain.setLevel(parent.getLevel() + 1);
        } else {
            domain.setParent(null);
            domain.setLevel(1); // 根领域层级为1
        }

        // 设置默认值
        if (domain.getIsActive() == null) {
            domain.setIsActive(true);
        }
        if (domain.getLevel() == null) {
            domain.setLevel(1);
        }
        if (domain.getDisplayOrder() == null) {
            domain.setDisplayOrder(0);
        }
        if (domain.getExpertCount() == null) {
            domain.setExpertCount(0);
        }
        if (domain.getProjectCount() == null) {
            domain.setProjectCount(0);
        }

        // 设置时间戳（BaseEntity会自动处理，但这里显式设置以确保）
        domain.setCreatedAt(LocalDateTime.now());
        domain.setUpdatedAt(LocalDateTime.now());

        Domain savedDomain = domainRepository.save(domain);
        
        // 如果设置了父级，更新父级的子领域关系
        if (domain.getParent() != null) {
            domain.getParent().addChild(savedDomain);
        }

        log.info("创建领域: {}", savedDomain.getName());
        return savedDomain;
    }

    /**
     * 更新领域信息
     */
    @Transactional
    public Domain update(Long id, Domain domainDetails) {
        Domain existingDomain = findById(id);

        // 更新基本信息
        if (domainDetails.getName() != null && !domainDetails.getName().equals(existingDomain.getName())) {
            // 验证新名称的唯一性
            if (domainRepository.existsByName(domainDetails.getName())) {
                throw new RuntimeException("领域名称已存在: " + domainDetails.getName());
            }
            existingDomain.setName(domainDetails.getName());
        }
        
        if (domainDetails.getEnglishName() != null) {
            existingDomain.setEnglishName(domainDetails.getEnglishName());
        }
        
        if (domainDetails.getDescription() != null) {
            existingDomain.setDescription(domainDetails.getDescription());
        }
        
        if (domainDetails.getParentId() == null && existingDomain.getParentId() != null) {
            if (existingDomain.getParent() != null) {
                existingDomain.getParent().removeChild(existingDomain);
            } else {
                existingDomain.setParent(null);
                existingDomain.setParentId(null);
                existingDomain.setLevel(1);
            }

            updateChildrenLevel(existingDomain, existingDomain.getLevel() + 1);
        } else if (domainDetails.getParentId() != null && !domainDetails.getParentId().equals(existingDomain.getParentId())) {
            // 更新父级关系
            Domain newParent = findById(domainDetails.getParentId());

            // 防止循环引用：不能将自己设为父级
            if (newParent.getId().equals(id)) {
                throw new RuntimeException("不能将领域设为自己的父级");
            }

            // 检查是否形成循环引用（例如：A->B->C->A）
            if (isCircularReference(newParent, id)) {
                throw new RuntimeException("更新父级会导致循环引用");
            }

            // 移除旧的父级关系
            if (existingDomain.getParent() != null) {
                existingDomain.getParent().removeChild(existingDomain);
            }

            // 设置新的父级关系
            existingDomain.setParentId(domainDetails.getParentId());
            existingDomain.setParent(newParent);
            existingDomain.setLevel(newParent.getLevel() + 1);
            newParent.addChild(existingDomain);

            // 更新所有子领域的层级
            updateChildrenLevel(existingDomain, existingDomain.getLevel() + 1);
        }
        
        if (domainDetails.getDisplayOrder() != null) {
            existingDomain.setDisplayOrder(domainDetails.getDisplayOrder());
        }
        
        if (domainDetails.getIsActive() != null) {
            existingDomain.setIsActive(domainDetails.getIsActive());
        }
        
        if (domainDetails.getIconUrl() != null) {
            existingDomain.setIconUrl(domainDetails.getIconUrl());
        }
        
        if (domainDetails.getColorCode() != null) {
            existingDomain.setColorCode(domainDetails.getColorCode());
        }

        existingDomain.setUpdatedAt(LocalDateTime.now());

        return domainRepository.save(existingDomain);
    }

    /**
     * 删除领域
     */
    @Transactional
    public void delete(Long id) {
        Domain domain = findById(id);
        
        // 检查是否有子领域
        if (!domain.getChildren().isEmpty()) {
            throw new RuntimeException("无法删除领域，该领域有子领域");
        }
        
        // 检查是否有专家关联
        if (!domain.getExperts().isEmpty()) {
            throw new RuntimeException("无法删除领域，该领域有关联专家");
        }
        
        // 检查是否有项目关联
        if (!domain.getProjects().isEmpty()) {
            throw new RuntimeException("无法删除领域，该领域有关联项目");
        }
        
        // 从父级中移除（如果存在）
        if (domain.getParent() != null) {
            domain.getParent().removeChild(domain);
        }

        domain.getStewards().clear();

        domainRepository.delete(domain);
        log.info("删除领域，ID: {}", id);
    }

    /**
     * 获取活跃领域（分页）
     */
    public Page<Domain> findActiveDomains(Pageable pageable) {
        return domainRepository.findByIsActive(true, pageable);
    }

    /**
     * 获取根领域（parentId为null）
     */
    public List<Domain> findRootDomains() {
        return domainRepository.findRootDomains();
    }

    /**
     * 增加领域专家计数
     */
    @Transactional
    public Domain incrementExpertCount(Long id) {
        Domain domain = findById(id);
        int currentCount = domain.getExpertCount() != null ? domain.getExpertCount() : 0;
        domain.setExpertCount(currentCount + 1);
        domain.setUpdatedAt(LocalDateTime.now());
        return domainRepository.save(domain);
    }

    /**
     * 减少领域专家计数
     */
    @Transactional
    public Domain decrementExpertCount(Long id) {
        Domain domain = findById(id);
        int currentCount = domain.getExpertCount() != null ? domain.getExpertCount() : 0;
        if (currentCount > 0) {
            domain.setExpertCount(currentCount - 1);
            domain.setUpdatedAt(LocalDateTime.now());
            return domainRepository.save(domain);
        }
        return domain;
    }

    /**
     * 增加领域项目计数
     */
    @Transactional
    public Domain incrementProjectCount(Long id) {
        Domain domain = findById(id);
        int currentCount = domain.getProjectCount() != null ? domain.getProjectCount() : 0;
        domain.setProjectCount(currentCount + 1);
        domain.setUpdatedAt(LocalDateTime.now());
        return domainRepository.save(domain);
    }

    /**
     * 减少领域项目计数
     */
    @Transactional
    public Domain decrementProjectCount(Long id) {
        Domain domain = findById(id);
        int currentCount = domain.getProjectCount() != null ? domain.getProjectCount() : 0;
        if (currentCount > 0) {
            domain.setProjectCount(currentCount - 1);
            domain.setUpdatedAt(LocalDateTime.now());
            return domainRepository.save(domain);
        }
        return domain;
    }

    /**
     * 根据专家ID查找领域
     */
    public List<Domain> findByExpertId(Long expertId) {
        // 首先检查专家是否存在
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("专家不存在，ID: " + expertId));
        
        // 返回专家关联的所有领域
        return expert.getDomains().stream().toList();
    }

    /**
     * 检查循环引用
     */
    private boolean isCircularReference(Domain parent, Long childId) {
        Domain current = parent;
        while (current != null) {
            if (current.getId().equals(childId)) {
                return true; // 发现循环引用
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * 递归更新子领域层级
     */
    private void updateChildrenLevel(Domain parent, int baseLevel) {
        for (Domain child : parent.getChildren()) {
            child.setLevel(baseLevel);
            updateChildrenLevel(child, baseLevel + 1);
        }
    }

    /**
     * 根据父级ID分页查找子领域
     */
    public Page<Domain> findByParentIdPage(Long parentId, Pageable pageable) {
        return domainRepository.findByParentIdPage(parentId, pageable);
    }

    /**
     * 搜索领域（分页）
     */
    public Page<Domain> searchByKeyword(String keyword, Pageable pageable) {
        return domainRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 统计所有领域数量
     */
    public long countAllDomains() {
        return domainRepository.countAllDomains();
    }

    /**
     * 统计子领域数量
     */
    public long countSubDomains() {
        return domainRepository.countSubDomains();
    }

    /**
     * 获取根领域（分页）
     */
    public Page<Domain> findRootDomainsPage(Pageable pageable) {
        return domainRepository.findRootDomainsPage(pageable);
    }

    /**
     * 替换领域行管绑定（全量覆盖）
     */
    @Transactional
    public Domain replaceStewards(Long domainId, Set<Long> userIds) {
        Domain domain = findById(domainId);
        domain.getStewards().clear();
        if (userIds != null) {
            for (Long uid : userIds) {
                User user = userRepository.findById(uid)
                        .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + uid));
                domain.getStewards().add(user);
            }
        }
        domain.setUpdatedAt(LocalDateTime.now());
        return domainRepository.save(domain);
    }

    /** 当前用户是否为该领域配置的行管之一 */
    public boolean isUserStewardOfDomain(Long userId, Long domainId) {
        Domain domain = findById(domainId);
        if (domain.getStewards() == null || domain.getStewards().isEmpty()) {
            return false;
        }
        return domain.getStewards().stream().anyMatch(u -> u.getId().equals(userId));
    }
}