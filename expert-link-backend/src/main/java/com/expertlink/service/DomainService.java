package com.expertlink.service;

import com.expertlink.domain.Domain;
import com.expertlink.domain.Expert;
import com.expertlink.domain.User;
import com.expertlink.dto.domain.DomainStatsResponse;
import com.expertlink.dto.importing.ImportErrorRow;
import com.expertlink.dto.importing.ImportResultResponse;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.ProjectRepository;
import com.expertlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final String[] DOMAIN_IMPORT_HEADERS_ZH = {"领域名称", "父领域名称", "层级", "启用状态", "描述"};
    private static final String[] DOMAIN_ACTIVE_OPTIONS_ZH = {"启用", "停用"};

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

    public Set<Long> collectSubtreeDomainIds(Set<Long> rootDomainIds) {
        Set<Long> roots = rootDomainIds == null ? Set.of() : rootDomainIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (roots.isEmpty()) {
            return Set.of();
        }
        List<Domain> all = domainRepository.findAll();
        Map<Long, List<Long>> childrenMap = new LinkedHashMap<>();
        for (Domain d : all) {
            if (d.getParentId() == null) {
                continue;
            }
            childrenMap.computeIfAbsent(d.getParentId(), k -> new ArrayList<>()).add(d.getId());
        }
        Set<Long> out = new LinkedHashSet<>();
        for (Long rootId : roots) {
            collectSubtreeIds(rootId, childrenMap, out);
        }
        return out;
    }

    /**
     * 将领域 ID 扩展为「自身 + 所有祖先领域」，用于专家关联子领域时默认同时写入父域链。
     */
    public Set<Long> collectWithAncestorDomainIds(Set<Long> domainIds) {
        if (domainIds == null || domainIds.isEmpty()) {
            return Set.of();
        }
        List<Domain> all = domainRepository.findAll();
        Map<Long, Long> parentByChild = new HashMap<>();
        Set<Long> known = new HashSet<>();
        for (Domain d : all) {
            if (d.getId() == null) {
                continue;
            }
            known.add(d.getId());
            Long pid = d.getParentId();
            parentByChild.put(d.getId(), (pid != null && pid > 0) ? pid : null);
        }
        LinkedHashSet<Long> out = new LinkedHashSet<>();
        for (Long start : domainIds) {
            if (start == null || start <= 0 || !known.contains(start)) {
                continue;
            }
            Long cur = start;
            int guard = 0;
            while (cur != null && cur > 0 && guard++ < 128) {
                if (!out.add(cur)) {
                    break;
                }
                cur = parentByChild.get(cur);
            }
        }
        return out;
    }

    /**
     * 根据名称查找领域
     */
    public Optional<Domain> findByName(String name) {
        return domainRepository.findByName(name);
    }

    public byte[] buildImportTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("domains");
            Row header = sheet.createRow(0);
            for (int i = 0; i < DOMAIN_IMPORT_HEADERS_ZH.length; i++) {
                header.createCell(i).setCellValue(DOMAIN_IMPORT_HEADERS_ZH[i]);
                sheet.setColumnWidth(i, 22 * 256);
            }
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("智能制造");
            sample.createCell(1).setCellValue("");
            sample.createCell(2).setCellValue("1");
            sample.createCell(3).setCellValue("启用");
            sample.createCell(4).setCellValue("制造业数字化相关领域");
            // 枚举字段下拉：启用状态
            addExplicitDropdown(sheet, 1, 5000, 3, DOMAIN_ACTIVE_OPTIONS_ZH);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成领域导入模板失败", e);
        }
    }

    public byte[] buildExportWorkbook() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("domains");
            Row header = sheet.createRow(0);
            for (int i = 0; i < DOMAIN_IMPORT_HEADERS_ZH.length; i++) {
                header.createCell(i).setCellValue(DOMAIN_IMPORT_HEADERS_ZH[i]);
                sheet.setColumnWidth(i, 22 * 256);
            }
            List<Domain> domains = domainRepository.findAll();
            int rowIdx = 1;
            for (Domain domain : domains) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(defaultString(domain.getName()));
                String parentName = "";
                if (domain.getParentId() != null) {
                    parentName = domainRepository.findById(domain.getParentId()).map(Domain::getName).orElse("");
                }
                row.createCell(1).setCellValue(parentName);
                row.createCell(2).setCellValue(domain.getLevel() == null ? "" : String.valueOf(domain.getLevel()));
                row.createCell(3).setCellValue(Boolean.TRUE.equals(domain.getIsActive()) ? "启用" : "停用");
                row.createCell(4).setCellValue(defaultString(domain.getDescription()));
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出领域失败", e);
        }
    }

    @Transactional
    public ImportResultResponse importDomains(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传 domains.xlsx 文件");
        }
        List<ImportErrorRow> errors = new ArrayList<>();
        int total = 0;
        int success = 0;
        int skipped = 0;
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("文件中不存在工作表");
            }
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row, 5)) {
                    continue;
                }
                total++;
                try {
                    String name = readString(row.getCell(0));
                    if (!StringUtils.hasText(name)) {
                        throw new IllegalArgumentException("name 为必填");
                    }
                    if (domainRepository.existsByName(name.trim())) {
                        skipped++;
                        continue;
                    }
                    Domain domain = new Domain();
                    domain.setName(name.trim());
                    String parentName = readString(row.getCell(1));
                    if (StringUtils.hasText(parentName)) {
                        Domain parent = domainRepository.findByName(parentName.trim())
                                .orElseThrow(() -> new IllegalArgumentException("父领域不存在: " + parentName));
                        domain.setParent(parent);
                        domain.setParentId(parent.getId());
                        domain.setLevel(parent.getLevel() + 1);
                    } else {
                        Integer level = readInteger(row.getCell(2));
                        domain.setLevel(level == null || level < 1 ? 1 : level);
                    }

                    String activeText = readString(row.getCell(3));
                    domain.setIsActive(parseActive(activeText));
                    domain.setDescription(readString(row.getCell(4)));
                    domain.setDisplayOrder(0);
                    domain.setExpertCount(0);
                    domain.setProjectCount(0);
                    domain.setCreatedAt(LocalDateTime.now());
                    domain.setUpdatedAt(LocalDateTime.now());
                    domainRepository.save(domain);
                    success++;
                } catch (Exception ex) {
                    errors.add(ImportErrorRow.builder()
                            .row(i + 1)
                            .message(ex.getMessage())
                            .build());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("解析 domains.xlsx 失败", e);
        }

        return ImportResultResponse.builder()
                .total(total)
                .success(success)
                .failed(errors.size())
                .skipped(skipped)
                .errors(errors)
                .build();
    }

    private static String readString(Cell cell) {
        if (cell == null) return null;
        CellType type = cell.getCellType();
        if (type == CellType.STRING) return cell.getStringCellValue();
        if (type == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        if (type == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        return null;
    }

    private static Integer readInteger(Cell cell) {
        String text = readString(cell);
        if (!StringUtils.hasText(text)) return null;
        return Integer.parseInt(text.trim());
    }

    private static boolean isBlankRow(Row row, int expectedCols) {
        for (int i = 0; i < expectedCols; i++) {
            if (StringUtils.hasText(readString(row.getCell(i)))) return false;
        }
        return true;
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static Boolean parseActive(String text) {
        if (!StringUtils.hasText(text)) {
            return true;
        }
        String t = text.trim();
        if ("启用".equals(t) || "true".equalsIgnoreCase(t)) {
            return true;
        }
        if ("停用".equals(t) || "false".equalsIgnoreCase(t)) {
            return false;
        }
        throw new IllegalArgumentException("启用状态仅支持：启用/停用");
    }

    private static void addExplicitDropdown(Sheet sheet, int firstRow, int lastRow, int col, String[] values) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, col, col);
        DataValidation validation = helper.createValidation(constraint, regions);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.setShowErrorBox(true);
        validation.createErrorBox("输入不合法", "请从下拉选项中选择");
        sheet.addValidationData(validation);
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