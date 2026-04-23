package com.expertlink.service;

import com.expertlink.domain.Expert;
import com.expertlink.domain.ExpertDesignation;
import com.expertlink.domain.Skill;
import com.expertlink.domain.Domain;
import com.expertlink.domain.User;
import com.expertlink.dto.expert.UserPickerDto;
import com.expertlink.dto.expert.BulkAddExpertsByDomainResponse;
import com.expertlink.dto.importing.ImportErrorRow;
import com.expertlink.dto.importing.ImportResultResponse;
import com.expertlink.repository.ExpertDesignationRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.UserRepository;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;
    private final ExpertDesignationRepository expertDesignationRepository;

    /**
     * 获取所有专家（分页）
     */
    public Page<Expert> findAll(Pageable pageable) {
        return expertRepository.findAll(pageable);
    }

    /**
     * 根据ID查找专家
     */
    public Expert findById(Long id) {
        return expertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("专家不存在，ID: " + id));
    }

    /**
     * 根据邮箱查找专家
     */
    public Optional<Expert> findByEmail(String email) {
        return expertRepository.findByEmail(email);
    }

    /**
     * 根据姓名查找专家
     */
    public List<Expert> findByNameContaining(String name) {
        return expertRepository.findByNameContainingIgnoreCase(name);
    }

    public Page<UserPickerDto> listUserCandidates(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return userRepository.searchActiveUsersWithoutExpertProfile(kw, pageable)
                .map(u -> new UserPickerDto(
                        u.getId(),
                        u.getUsername(),
                        u.getFullName() != null ? u.getFullName() : "",
                        u.getEmail()
                ));
    }

    /**
     * 创建新专家：必须从系统用户拉取主体信息，并选择已维护的称谓/岗位；同一用户仅允许一条专家档案。
     */
    @Transactional
    public Expert create(Expert expert, Long ownerId, Long primaryDomainId, Set<Long> skillIds, Set<Long> domainIds, Long designationId) {
        if (expertRepository.existsByOwner_Id(ownerId)) {
            throw new IllegalArgumentException("该用户已有关联的专家档案，不能重复创建");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + ownerId));
        expert.setOwner(owner);
        ExpertDesignation designation = null;
        if (designationId != null) {
            designation = expertDesignationRepository.findById(designationId)
                    .orElseThrow(() -> new IllegalArgumentException("称谓不存在，ID: " + designationId));
        }
        applyUserProfile(owner, expert);
        if (designation != null) {
            applyDesignation(expert, designation);
        }

        // 设置主要领域
        if (primaryDomainId != null) {
            Domain primaryDomain = domainRepository.findById(primaryDomainId)
                    .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + primaryDomainId));
            expert.setPrimaryDomain(primaryDomain);
        }

        // 添加技能
        if (skillIds != null && !skillIds.isEmpty()) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillIds));
            expert.setSkills(skills);
        }

        // 添加领域
        if (domainIds != null && !domainIds.isEmpty()) {
            Set<Domain> domains = new HashSet<>(domainRepository.findAllById(domainIds));
            expert.setDomains(domains);
        }

        // 设置默认值
        if (expert.getIsVerified() == null) {
            expert.setIsVerified(false);
        }
        if (expert.getVerificationLevel() == null) {
            expert.setVerificationLevel(0);
        }
        if (expert.getReviewCount() == null) {
            expert.setReviewCount(0);
        }
        if (expert.getProjectCount() == null) {
            expert.setProjectCount(0);
        }
        if (expert.getOverallRating() == null) {
            expert.setOverallRating(BigDecimal.ZERO);
        }
        if (expert.getSuccessRate() == null) {
            expert.setSuccessRate(BigDecimal.ZERO);
        }

        expert.setCreatedAt(LocalDateTime.now());
        expert.setUpdatedAt(LocalDateTime.now());
        expert.setLastActiveTime(LocalDateTime.now());

        return expertRepository.save(expert);
    }

    /**
     * 按“系统用户 + 称谓”快速创建专家档案（用于批量入库）。
     */
    @Transactional
    public Expert createByOwnerAndDesignation(Long ownerId, Long designationId) {
        return create(new Expert(), ownerId, null, null, null, designationId);
    }

    public byte[] buildImportTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("experts");
            Row header = sheet.createRow(0);
            String[] columns = {
                    "name", "email", "phone", "wechat", "company", "position", "introduction",
                    "experienceYears", "hourlyRate", "availability", "primaryDomainName",
                    "domainNames", "skillNames"
            };
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
                sheet.setColumnWidth(i, 20 * 256);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成专家导入模板失败", e);
        }
    }

    @Transactional
    public ImportResultResponse importExperts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传 experts.xlsx 文件");
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
                if (row == null || isBlankRow(row, 13)) {
                    continue;
                }
                total++;
                try {
                    String name = readString(row.getCell(0));
                    String email = readString(row.getCell(1));
                    if (!StringUtils.hasText(name) || !StringUtils.hasText(email)) {
                        throw new IllegalArgumentException("name/email 为必填");
                    }
                    if (expertRepository.findByEmail(email).isPresent()) {
                        skipped++;
                        continue;
                    }
                    Expert expert = new Expert();
                    expert.setName(name.trim());
                    expert.setEmail(email.trim());
                    expert.setPhoneNumber(readString(row.getCell(2)));
                    expert.setWechatId(readString(row.getCell(3)));
                    expert.setCurrentCompany(readString(row.getCell(4)));
                    expert.setCurrentPosition(readString(row.getCell(5)));
                    expert.setBiography(readString(row.getCell(6)));
                    Integer years = readInteger(row.getCell(7));
                    if (years != null) {
                        expert.setYearsOfExperience(years);
                    }
                    BigDecimal hourlyRate = readDecimal(row.getCell(8));
                    if (hourlyRate != null) {
                        expert.setHourlyRate(hourlyRate);
                    }
                    String availability = readString(row.getCell(9));
                    expert.setAvailabilityStatus(
                            "UNAVAILABLE".equalsIgnoreCase(availability) ? "UNAVAILABLE" : "AVAILABLE");

                    String primaryDomainName = readString(row.getCell(10));
                    if (StringUtils.hasText(primaryDomainName)) {
                        Domain domain = domainRepository.findByName(primaryDomainName.trim())
                                .orElseThrow(() -> new IllegalArgumentException("主领域不存在: " + primaryDomainName));
                        expert.setPrimaryDomain(domain);
                    }

                    String domainNames = readString(row.getCell(11));
                    if (StringUtils.hasText(domainNames)) {
                        Set<Domain> domains = Arrays.stream(domainNames.split(","))
                                .map(String::trim)
                                .filter(StringUtils::hasText)
                                .map(nameText -> domainRepository.findByName(nameText)
                                        .orElseThrow(() -> new IllegalArgumentException("领域不存在: " + nameText)))
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                        expert.setDomains(domains);
                    }

                    String skillNames = readString(row.getCell(12));
                    if (StringUtils.hasText(skillNames)) {
                        Set<Skill> skills = Arrays.stream(skillNames.split(","))
                                .map(String::trim)
                                .filter(StringUtils::hasText)
                                .map(nameText -> skillRepository.findByName(nameText)
                                        .orElseThrow(() -> new IllegalArgumentException("技能不存在: " + nameText)))
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                        expert.setSkills(skills);
                    }

                    expert.setIsVerified(false);
                    expert.setVerificationLevel(0);
                    expert.setReviewCount(0);
                    expert.setProjectCount(0);
                    expert.setOverallRating(BigDecimal.ZERO);
                    expert.setSuccessRate(BigDecimal.ZERO);
                    expert.setCreatedAt(LocalDateTime.now());
                    expert.setUpdatedAt(LocalDateTime.now());
                    expert.setLastActiveTime(LocalDateTime.now());
                    expertRepository.save(expert);
                    success++;
                } catch (Exception ex) {
                    errors.add(ImportErrorRow.builder()
                            .row(i + 1)
                            .message(ex.getMessage())
                            .build());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("解析 experts.xlsx 失败", e);
        }

        return ImportResultResponse.builder()
                .total(total)
                .success(success)
                .skipped(skipped)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private static String readString(Cell cell) {
        if (cell == null) return null;
        CellType type = cell.getCellType();
        if (type == CellType.STRING) return cell.getStringCellValue();
        if (type == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
        if (type == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        return null;
    }

    private static Integer readInteger(Cell cell) {
        String text = readString(cell);
        if (!StringUtils.hasText(text)) return null;
        return Integer.parseInt(text.trim());
    }

    private static BigDecimal readDecimal(Cell cell) {
        String text = readString(cell);
        if (!StringUtils.hasText(text)) return null;
        return new BigDecimal(text.trim());
    }

    private static boolean isBlankRow(Row row, int expectedCols) {
        for (int i = 0; i < expectedCols; i++) {
            if (StringUtils.hasText(readString(row.getCell(i)))) return false;
        }
        return true;
    }

    private static void applyUserProfile(User owner, Expert expert) {
        String displayName = StringUtils.hasText(owner.getFullName()) ? owner.getFullName().trim() : owner.getUsername();
        expert.setName(displayName);
        expert.setEmail(owner.getEmail());
        if (StringUtils.hasText(owner.getPhoneNumber())) {
            expert.setPhoneNumber(owner.getPhoneNumber());
        }
        if (StringUtils.hasText(owner.getDepartment())) {
            expert.setCurrentCompany(owner.getDepartment());
        }
    }

    private static void applyDesignation(Expert expert, ExpertDesignation designation) {
        expert.setDesignation(designation);
        expert.setCurrentPosition(designation.getName());
    }

    @Transactional
    public BulkAddExpertsByDomainResponse bulkAddExpertsByDomain(Long domainId, List<Long> ownerIds) {
        if (ownerIds == null || ownerIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个用户");
        }
        domainRepository.findById(domainId)
                .orElseThrow(() -> new IllegalArgumentException("领域不存在，ID: " + domainId));

        Set<Long> deduped = new LinkedHashSet<>();
        for (Long ownerId : ownerIds) {
            if (ownerId != null && ownerId > 0) {
                deduped.add(ownerId);
            }
        }
        if (deduped.isEmpty()) {
            throw new IllegalArgumentException("有效用户为空");
        }

        List<Long> createdExpertIds = new ArrayList<>();
        List<Long> skippedOwnerIds = new ArrayList<>();
        for (Long ownerId : deduped) {
            try {
                Expert created = create(new Expert(), ownerId, domainId, null, Set.of(domainId), null);
                createdExpertIds.add(created.getId());
            } catch (IllegalArgumentException ex) {
                skippedOwnerIds.add(ownerId);
            }
        }

        return BulkAddExpertsByDomainResponse.builder()
                .requestedCount(deduped.size())
                .createdCount(createdExpertIds.size())
                .skippedCount(skippedOwnerIds.size())
                .createdExpertIds(createdExpertIds)
                .skippedOwnerIds(skippedOwnerIds)
                .build();
    }

    /**
     * 更新专家信息
     */
    @Transactional
    public Expert update(Long id, Expert expertDetails, Long primaryDomainId, Set<Long> skillIds, Set<Long> domainIds) {
        Expert existingExpert = findById(id);

        // 更新基本信息
        if (expertDetails.getName() != null) {
            existingExpert.setName(expertDetails.getName());
        }
        if (expertDetails.getEnglishName() != null) {
            existingExpert.setEnglishName(expertDetails.getEnglishName());
        }
        if (expertDetails.getGender() != null) {
            existingExpert.setGender(expertDetails.getGender());
        }
        if (expertDetails.getDateOfBirth() != null) {
            existingExpert.setDateOfBirth(expertDetails.getDateOfBirth());
        }
        if (expertDetails.getIdNumber() != null) {
            existingExpert.setIdNumber(expertDetails.getIdNumber());
        }
        if (expertDetails.getNationality() != null) {
            existingExpert.setNationality(expertDetails.getNationality());
        }
        if (expertDetails.getHighestDegree() != null) {
            existingExpert.setHighestDegree(expertDetails.getHighestDegree());
        }
        if (expertDetails.getGraduationSchool() != null) {
            existingExpert.setGraduationSchool(expertDetails.getGraduationSchool());
        }
        if (expertDetails.getMajorField() != null) {
            existingExpert.setMajorField(expertDetails.getMajorField());
        }
        if (expertDetails.getCurrentPosition() != null) {
            existingExpert.setCurrentPosition(expertDetails.getCurrentPosition());
        }
        if (expertDetails.getCurrentCompany() != null) {
            existingExpert.setCurrentCompany(expertDetails.getCurrentCompany());
        }
        if (expertDetails.getYearsOfExperience() != null) {
            existingExpert.setYearsOfExperience(expertDetails.getYearsOfExperience());
        }
        if (expertDetails.getBiography() != null) {
            existingExpert.setBiography(expertDetails.getBiography());
        }
        if (expertDetails.getAchievements() != null) {
            existingExpert.setAchievements(expertDetails.getAchievements());
        }
        if (expertDetails.getResearchInterests() != null) {
            existingExpert.setResearchInterests(expertDetails.getResearchInterests());
        }
        if (expertDetails.getHourlyRate() != null) {
            existingExpert.setHourlyRate(expertDetails.getHourlyRate());
        }
        if (expertDetails.getDailyRate() != null) {
            existingExpert.setDailyRate(expertDetails.getDailyRate());
        }
        if (expertDetails.getProjectRate() != null) {
            existingExpert.setProjectRate(expertDetails.getProjectRate());
        }
        if (expertDetails.getAvailabilityStatus() != null) {
            existingExpert.setAvailabilityStatus(expertDetails.getAvailabilityStatus());
        }
        if (expertDetails.getPreferredContactMethod() != null) {
            existingExpert.setPreferredContactMethod(expertDetails.getPreferredContactMethod());
        }
        if (expertDetails.getEmail() != null) {
            existingExpert.setEmail(expertDetails.getEmail());
        }
        if (expertDetails.getPhoneNumber() != null) {
            existingExpert.setPhoneNumber(expertDetails.getPhoneNumber());
        }
        if (expertDetails.getWechatId() != null) {
            existingExpert.setWechatId(expertDetails.getWechatId());
        }
        if (expertDetails.getAvatar() != null) {
            existingExpert.setAvatar(expertDetails.getAvatar());
        }
        if (expertDetails.getIsVerified() != null) {
            existingExpert.setIsVerified(expertDetails.getIsVerified());
        }
        if (expertDetails.getVerificationLevel() != null) {
            existingExpert.setVerificationLevel(expertDetails.getVerificationLevel());
        }

        // 更新主要领域
        if (primaryDomainId != null) {
            Domain primaryDomain = domainRepository.findById(primaryDomainId)
                    .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + primaryDomainId));
            existingExpert.setPrimaryDomain(primaryDomain);
        }

        // 更新技能
        if (skillIds != null) {
            Set<Skill> skills = new HashSet<>(skillRepository.findAllById(skillIds));
            existingExpert.getSkills().clear();
            existingExpert.getSkills().addAll(skills);
        }

        // 更新领域
        if (domainIds != null) {
            Set<Domain> domains = new HashSet<>(domainRepository.findAllById(domainIds));
            existingExpert.getDomains().clear();
            existingExpert.getDomains().addAll(domains);
        }

        existingExpert.setUpdatedAt(LocalDateTime.now());
        existingExpert.setLastActiveTime(LocalDateTime.now());

        return expertRepository.save(existingExpert);
    }

    /**
     * 删除专家
     */
    @Transactional
    public void delete(Long id) {
        Expert expert = findById(id);
        // 检查是否有关联项目
        if (!expert.getProjectExperiences().isEmpty()) {
            throw new RuntimeException("无法删除专家，该专家有关联项目");
        }
        
        expertRepository.delete(expert);
        log.info("删除专家，ID: {}", id);
    }

    /**
     * 搜索专家
     */
    public Page<Expert> searchByKeyword(String keyword, Pageable pageable) {
        return expertRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 根据领域查找专家
     */
    public List<Expert> findByDomainId(Long domainId) {
        return expertRepository.findByDomainId(domainId);
    }

    /**
     * 根据技能查找专家
     */
    public List<Expert> findBySkillId(Long skillId) {
        return expertRepository.findBySkillId(skillId);
    }

    /**
     * 根据费率范围查找专家
     */
    public List<Expert> findByHourlyRateBetween(BigDecimal minRate, BigDecimal maxRate) {
        return expertRepository.findByHourlyRateBetween(minRate, maxRate);
    }

    /**
     * 获取活跃专家数量
     */
    public long countActiveExperts() {
        return expertRepository.countAvailableExperts();
    }

    /**
     * 获取评分最高的专家
     */
    public List<Expert> findTopRatedExperts(Pageable pageable) {
        return expertRepository.findTopRatedExperts(pageable);
    }

    /**
     * 验证专家
     */
    @Transactional
    public Expert verifyExpert(Long id, Integer verificationLevel) {
        Expert expert = findById(id);
        expert.setIsVerified(true);
        expert.setVerificationLevel(verificationLevel);
        expert.setUpdatedAt(LocalDateTime.now());
        return expertRepository.save(expert);
    }

    /**
     * 更新专家评分
     */
    @Transactional
    public Expert updateRating(Long id, BigDecimal newRating) {
        Expert expert = findById(id);
        
        // 计算新的平均评分
        int currentReviewCount = expert.getReviewCount();
        BigDecimal currentOverallRating = expert.getOverallRating();
        
        if (currentReviewCount == 0) {
            expert.setOverallRating(newRating);
        } else {
            BigDecimal totalRating = currentOverallRating.multiply(BigDecimal.valueOf(currentReviewCount));
            totalRating = totalRating.add(newRating);
            BigDecimal updatedRating = totalRating.divide(BigDecimal.valueOf(currentReviewCount + 1), 2, BigDecimal.ROUND_HALF_UP);
            expert.setOverallRating(updatedRating);
        }
        
        expert.setReviewCount(currentReviewCount + 1);
        expert.setUpdatedAt(LocalDateTime.now());
        
        return expertRepository.save(expert);
    }

    /**
     * 更新项目数量
     */
    @Transactional
    public Expert incrementProjectCount(Long id) {
        Expert expert = findById(id);
        int currentCount = expert.getProjectCount() != null ? expert.getProjectCount() : 0;
        expert.setProjectCount(currentCount + 1);
        expert.setUpdatedAt(LocalDateTime.now());
        return expertRepository.save(expert);
    }

    /**
     * 更新成功率
     */
    @Transactional
    public Expert updateSuccessRate(Long id, BigDecimal successRate) {
        Expert expert = findById(id);
        expert.setSuccessRate(successRate);
        expert.setUpdatedAt(LocalDateTime.now());
        return expertRepository.save(expert);
    }

    /**
     * 添加技能到专家
     */
    @Transactional
    public Expert addSkill(Long expertId, Long skillId) {
        Expert expert = findById(expertId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("技能不存在，ID: " + skillId));
        
        expert.addSkill(skill);
        return expertRepository.save(expert);
    }

    /**
     * 从专家移除技能
     */
    @Transactional
    public Expert removeSkill(Long expertId, Long skillId) {
        Expert expert = findById(expertId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("技能不存在，ID: " + skillId));
        
        expert.removeSkill(skill);
        return expertRepository.save(expert);
    }

    /**
     * 添加领域到专家
     */
    @Transactional
    public Expert addDomain(Long expertId, Long domainId) {
        Expert expert = findById(expertId);
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + domainId));
        
        expert.addDomain(domain);
        return expertRepository.save(expert);
    }

    /**
     * 从专家移除领域
     */
    @Transactional
    public Expert removeDomain(Long expertId, Long domainId) {
        Expert expert = findById(expertId);
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("领域不存在，ID: " + domainId));
        
        expert.removeDomain(domain);
        return expertRepository.save(expert);
    }
}