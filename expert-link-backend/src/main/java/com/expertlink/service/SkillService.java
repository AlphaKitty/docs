package com.expertlink.service;

import com.expertlink.dto.skill.CreateSkillRequest;
import com.expertlink.domain.Skill;
import com.expertlink.domain.Domain;
import com.expertlink.domain.Expert;
import com.expertlink.domain.SkillDocumentation;
import com.expertlink.domain.Tag;
import com.expertlink.repository.SkillRepository;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.SkillDocumentationRepository;
import com.expertlink.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillService {

    private final SkillRepository skillRepository;
    private final DomainRepository domainRepository;
    private final ExpertRepository expertRepository;
    private final TagRepository tagRepository;
    private final SkillDocumentationRepository skillDocumentationRepository;

    /**
     * 分页获取所有技能
     */
    public Page<Skill> findAll(Pageable pageable) {
        return skillRepository.findAll(Objects.requireNonNull(pageable));
    }

    /**
     * 根据ID查找技能
     */
    public Skill findById(Long id) {
        return skillRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("技能不存在，ID: " + id));
    }

    /**
     * 根据名称查找技能
     */
    public Skill findByName(String name) {
        return skillRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("技能不存在，名称: " + name));
    }

    /**
     * 根据名称关键词查找技能
     */
    public List<Skill> findByNameContaining(String keyword) {
        // 使用searchByKeyword方法实现，但只返回名称匹配的结果
        Page<Skill> result = skillRepository.searchByKeyword(keyword, Pageable.unpaged());
        return result.getContent();
    }

    /**
     * 根据类别查找技能
     */
    public List<Skill> findByCategory(String category) {
        return skillRepository.findByCategory(category);
    }

    /**
     * 根据领域ID查找技能
     */
    public List<Skill> findByDomainId(Long domainId) {
        return skillRepository.findByDomainId(domainId);
    }

    /**
     * 根据需求等级查找技能
     */
    public List<Skill> findByDemandLevel(String demandLevel) {
        return skillRepository.findByDemandLevel(demandLevel);
    }

    /**
     * 根据活跃状态查找技能
     */
    public List<Skill> findByIsActive(Boolean isActive) {
        return skillRepository.findByIsActive(isActive);
    }

    /**
     * 创建新技能
     */
    @Transactional
    public Skill create(Skill skill, Long domainId) {
        // 验证技能名称唯一性
        if (skillRepository.existsByName(skill.getName())) {
            throw new RuntimeException("技能名称已存在: " + skill.getName());
        }

        // 验证领域存在
        Domain domain = domainRepository.findById(Objects.requireNonNull(domainId))
                .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + domainId));
        skill.setDomain(domain);

        // 设置默认值
        if (skill.getIsActive() == null) {
            skill.setIsActive(true);
        }
        if (skill.getDisplayOrder() == null) {
            skill.setDisplayOrder(0);
        }
        if (skill.getExpertCount() == null) {
            skill.setExpertCount(0);
        }

        // 设置时间戳
        skill.setCreatedAt(LocalDateTime.now());
        skill.setUpdatedAt(LocalDateTime.now());

        Skill skillToSave = Objects.requireNonNull(skill);
        Skill savedSkill = skillRepository.save(skillToSave);
        log.info("创建新技能: {} (ID: {})", savedSkill.getName(), savedSkill.getId());
        return savedSkill;
    }

    @Transactional
    public Skill create(CreateSkillRequest request) {
        String skillName = normalizeText(request.getName());
        if (skillName == null || skillName.length() < 2) {
            throw new RuntimeException("技能名称不能为空");
        }
        if (skillRepository.existsByName(skillName)) {
            throw new RuntimeException("技能名称已存在: " + skillName);
        }
        if (request.getDomainId() == null) {
            throw new RuntimeException("请选择所属领域");
        }

        Domain domain = domainRepository.findById(Objects.requireNonNull(request.getDomainId()))
                .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + request.getDomainId()));

        Skill skill = Skill.builder()
                .name(skillName)
                .category(normalizeText(request.getCategory()))
                .description(normalizeText(request.getDescription()))
                .demandLevel(normalizeText(request.getDemandLevel()))
                .isActive(request.getEnabled() == null ? true : request.getEnabled())
                .displayOrder(0)
                .expertCount(0)
                .domain(domain)
                .build();

        Skill savedSkill = skillRepository.save(skill);

        savedSkill.setTags(resolveTags(request.getTags()));
        savedSkill.setRelatedSkills(resolveRelatedSkills(savedSkill.getId(), request.getRelatedSkillIds()));

        SkillDocumentation documentation = buildDocumentation(savedSkill, request);
        if (documentation != null) {
            savedSkill.setDocumentation(skillDocumentationRepository.save(documentation));
        }

        Skill finalSkill = skillRepository.save(savedSkill);
        log.info("结构化创建技能: {} (ID: {})", finalSkill.getName(), finalSkill.getId());
        return finalSkill;
    }

    @Transactional
    public Skill update(Long id, CreateSkillRequest request) {
        Skill existingSkill = findById(id);

        String newName = normalizeText(request.getName());
        if (newName != null && !newName.equals(existingSkill.getName())) {
            if (skillRepository.existsByName(newName)) {
                throw new RuntimeException("技能名称已存在: " + newName);
            }
            existingSkill.setName(newName);
        }

        if (request.getCategory() != null) {
            existingSkill.setCategory(normalizeText(request.getCategory()));
        }
        if (request.getDescription() != null) {
            existingSkill.setDescription(normalizeText(request.getDescription()));
        }
        if (request.getDemandLevel() != null) {
            existingSkill.setDemandLevel(normalizeText(request.getDemandLevel()));
        }
        if (request.getEnabled() != null) {
            existingSkill.setIsActive(request.getEnabled());
        }
        if (request.getDomainId() != null) {
            Domain domain = domainRepository.findById(Objects.requireNonNull(request.getDomainId()))
                    .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + request.getDomainId()));
            existingSkill.setDomain(domain);
        }

        if (request.getTags() != null) {
            existingSkill.setTags(resolveTags(request.getTags()));
        }
        if (request.getRelatedSkillIds() != null) {
            existingSkill.setRelatedSkills(resolveRelatedSkills(existingSkill.getId(), request.getRelatedSkillIds()));
        }
        if (request.getDocumentation() != null) {
            SkillDocumentation documentation = mergeDocumentation(existingSkill, request);
            existingSkill.setDocumentation(documentation);
        }

        existingSkill.setUpdatedAt(LocalDateTime.now());
        Skill updatedSkill = skillRepository.save(existingSkill);
        log.info("结构化更新技能: {} (ID: {})", updatedSkill.getName(), updatedSkill.getId());
        return updatedSkill;
    }

    /**
     * 更新技能信息
     */
    @Transactional
    public Skill update(Long id, Skill skillDetails, Long domainId) {
        Skill existingSkill = findById(id);

        // 更新基本信息
        if (skillDetails.getName() != null && !skillDetails.getName().equals(existingSkill.getName())) {
            // 验证新名称唯一性
            if (skillRepository.existsByName(skillDetails.getName())) {
                throw new RuntimeException("技能名称已存在: " + skillDetails.getName());
            }
            existingSkill.setName(skillDetails.getName());
        }
        if (skillDetails.getEnglishName() != null) {
            existingSkill.setEnglishName(skillDetails.getEnglishName());
        }
        if (skillDetails.getDescription() != null) {
            existingSkill.setDescription(skillDetails.getDescription());
        }
        if (skillDetails.getCategory() != null) {
            existingSkill.setCategory(skillDetails.getCategory());
        }
        if (skillDetails.getProficiencyLevels() != null) {
            existingSkill.setProficiencyLevels(skillDetails.getProficiencyLevels());
        }
        if (skillDetails.getDisplayOrder() != null) {
            existingSkill.setDisplayOrder(skillDetails.getDisplayOrder());
        }
        if (skillDetails.getIsActive() != null) {
            existingSkill.setIsActive(skillDetails.getIsActive());
        }
        if (skillDetails.getIconUrl() != null) {
            existingSkill.setIconUrl(skillDetails.getIconUrl());
        }
        if (skillDetails.getDemandLevel() != null) {
            existingSkill.setDemandLevel(skillDetails.getDemandLevel());
        }

        // 更新领域关联
        if (domainId != null) {
            Domain domain = domainRepository.findById(Objects.requireNonNull(domainId))
                    .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + domainId));
            existingSkill.setDomain(domain);
        }

        // 更新专家计数（只能通过increment/decrement方法修改）
        if (skillDetails.getExpertCount() != null) {
            log.warn("专家计数不能直接修改，请使用incrementExpertCount或decrementExpertCount方法");
        }

        existingSkill.setUpdatedAt(LocalDateTime.now());

        Skill updatedSkill = skillRepository.save(existingSkill);
        log.info("更新技能: {} (ID: {})", updatedSkill.getName(), updatedSkill.getId());
        return updatedSkill;
    }

    /**
     * 删除技能
     */
    @Transactional
    public void delete(Long id) {
        Skill skill = findById(id);
        
        // 检查是否有专家关联
        if (!skill.getExperts().isEmpty()) {
            throw new RuntimeException("无法删除技能，该技能有关联专家");
        }
        
        skillRepository.delete(skill);
        log.info("删除技能: {} (ID: {})", skill.getName(), skill.getId());
    }

    /**
     * 根据关键词搜索技能
     */
    public Page<Skill> searchByKeyword(String keyword, Pageable pageable) {
        return skillRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 按专家数量排序查找热门技能
     */
    public Page<Skill> findTopSkillsByExpertCount(Pageable pageable) {
        return skillRepository.findTopSkillsByExpertCount(pageable);
    }

    /**
     * 查找高需求技能（需求等级为HIGH）
     */
    public Page<Skill> findSkillsWithHighDemand(Pageable pageable) {
        // 使用searchByKeyword方法，但只筛选需求等级为HIGH的技能
        // 在实际项目中，可以在SkillRepository中添加专门的查询方法
        // 这里我们暂时使用所有技能然后过滤的方式
        return skillRepository.findByDemandLevel("HIGH", pageable);
    }

    /**
     * 根据专家ID查找技能
     */
    public List<Skill> findByExpertId(Long expertId) {
        // 查找专家
        Expert expert = expertRepository.findById(Objects.requireNonNull(expertId))
                .orElseThrow(() -> new RuntimeException("专家不存在，ID: " + expertId));
        
        // 获取专家的技能集合
        Set<Skill> skills = expert.getSkills();
        return List.copyOf(skills);
    }

    /**
     * 增加技能专家计数
     */
    @Transactional
    public Skill incrementExpertCount(Long id) {
        Skill skill = findById(id);
        skill.incrementExpertCount();
        skill.setUpdatedAt(LocalDateTime.now());
        return skillRepository.save(skill);
    }

    /**
     * 减少技能专家计数
     */
    @Transactional
    public Skill decrementExpertCount(Long id) {
        Skill skill = findById(id);
        skill.decrementExpertCount();
        skill.setUpdatedAt(LocalDateTime.now());
        return skillRepository.save(skill);
    }

    /**
     * 获取活跃技能数量
     */
    public long countActiveSkills() {
        return skillRepository.countActiveSkills();
    }

    /**
     * 获取所有活跃技能（按显示顺序和名称排序）
     */
    public List<Skill> findAllActiveSkillsOrdered() {
        return skillRepository.findAllActiveSkillsOrdered();
    }

    /**
     * 根据领域ID查找活跃技能
     */
    public List<Skill> findActiveSkillsByDomainId(Long domainId) {
        return skillRepository.findActiveSkillsByDomainId(domainId);
    }

    /**
     * 验证技能名称是否已存在
     */
    public boolean existsByName(String name) {
        return skillRepository.existsByName(name);
    }

    /**
     * 验证英文技能名称是否已存在
     */
    public boolean existsByEnglishName(String englishName) {
        return skillRepository.existsByEnglishName(englishName);
    }

    /**
     * 根据专家数量范围查找技能
     */
    public List<Skill> findByExpertCountRange(Integer minCount, Integer maxCount) {
        return skillRepository.findByExpertCountRange(minCount, maxCount);
    }

    /**
     * 根据最小专家数量查找技能
     */
    public List<Skill> findByMinExpertCount(Integer minExpertCount) {
        return skillRepository.findByMinExpertCount(minExpertCount);
    }

    /**
     * 根据类别列表查找技能
     */
    public List<Skill> findByCategories(List<String> categories) {
        return skillRepository.findByCategories(categories);
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return tagNames.stream()
                .map(this::normalizeTagName)
                .filter(Objects::nonNull)
                .distinct()
                .map(tagName -> tagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> tagRepository.save(Objects.requireNonNull(Tag.builder().name(tagName).build()))))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Skill> resolveRelatedSkills(Long currentSkillId, List<Long> relatedSkillIds) {
        if (relatedSkillIds == null || relatedSkillIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return relatedSkillIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .peek(relatedId -> {
                    if (relatedId.equals(currentSkillId)) {
                        throw new RuntimeException("相关技能不能关联自身");
                    }
                })
                .map(this::findById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private SkillDocumentation buildDocumentation(Skill skill, CreateSkillRequest request) {
        if (request.getDocumentation() == null) {
            return null;
        }

        String content = normalizeText(request.getDocumentation().getContent());
        String url = normalizeText(request.getDocumentation().getUrl());
        if (content == null && url == null) {
            return null;
        }

        return SkillDocumentation.builder()
                .skill(skill)
                .content(content)
                .url(url)
                .build();
    }

    private SkillDocumentation mergeDocumentation(Skill skill, CreateSkillRequest request) {
        SkillDocumentation incoming = buildDocumentation(skill, request);
        if (incoming == null) {
            return null;
        }

        SkillDocumentation existingDocumentation = skill.getDocumentation();
        if (existingDocumentation == null) {
            return incoming;
        }

        existingDocumentation.setContent(incoming.getContent());
        existingDocumentation.setUrl(incoming.getUrl());
        existingDocumentation.setUpdatedAt(LocalDateTime.now());
        return existingDocumentation;
    }

    private String normalizeTagName(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}