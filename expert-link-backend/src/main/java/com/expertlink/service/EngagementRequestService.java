package com.expertlink.service;

import com.expertlink.domain.*;
import com.expertlink.dto.engagement.*;
import com.expertlink.repository.EngagementRequestRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.UserRepository;
import com.expertlink.security.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EngagementRequestService {

    private static final int MIN_TASK_DESC_LEN = 8;

    private static final Set<String> ENGAGEMENT_ACTOR_ROLES = Set.of(
            UserRole.SUPER_ADMIN.name(),
            UserRole.DOMAIN_STEWARD.name(),
            UserRole.DEPT_ADMIN.name(),
            UserRole.REGULAR_USER.name(),
            UserRole.EXPERT_USER.name()
    );

    private final EngagementRequestRepository engagementRequestRepository;
    private final UserRepository userRepository;
    private final DomainService domainService;
    private final ExpertRepository expertRepository;
    private final ObjectMapper objectMapper;
    private final PointsService pointsService;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    private boolean isSuperAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRoles().contains(UserRole.SUPER_ADMIN.name()))
                .orElse(false);
    }

    private void assertCanUseEngagement(Long userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (u.getRoles().stream().noneMatch(ENGAGEMENT_ACTOR_ROLES::contains)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前角色不可使用调⽤申请");
        }
    }

    private EngagementRequest requireEntity(Long id) {
        return engagementRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("申请单不存在，ID: " + id));
    }

    public EngagementRequestResponse getByIdForViewer(Long userId, Long id) {
        EngagementRequest e = requireEntity(id);
        assertCanView(userId, e);
        return toResponse(e);
    }

    private void assertCanView(Long userId, EngagementRequest e) {
        if (isSuperAdmin(userId)) {
            return;
        }
        if (e.getApplicant().getId().equals(userId)) {
            return;
        }
        if (domainService.isUserStewardOfDomain(userId, e.getDomain().getId())) {
            return;
        }
        if (e.getAssignedExpert() != null && isExpertOwner(userId, e.getAssignedExpert().getId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看该申请单");
    }

    private boolean isExpertOwner(Long userId, Long expertId) {
        return expertRepository.findById(expertId)
                .map(ex -> ex.getOwner() != null && userId.equals(ex.getOwner().getId()))
                .orElse(false);
    }

    /**
     * 专家主领域或关联领域须覆盖申请单领域（超管指派仍校验数据一致性）。
     */
    private void assertExpertCoversRequestDomain(Expert expert, long domainId) {
        Expert loaded = expertRepository.findById(expert.getId())
                .orElseThrow(() -> new IllegalArgumentException("专家不存在"));
        if (loaded.getPrimaryDomain() != null && Objects.equals(loaded.getPrimaryDomain().getId(), domainId)) {
            return;
        }
        if (loaded.getDomains() != null
                && loaded.getDomains().stream().anyMatch(d -> d.getId() != null && Objects.equals(d.getId(), domainId))) {
            return;
        }
        throw new IllegalArgumentException("专家未关联该申请领域，不能指定或指派该专家");
    }

    private static String newReferenceCode(long id) {
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("ER-%s-%06d", day, id);
    }

    private List<String> readAttachmentUrlList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<Map<String, Object>> readReassignmentLogRaw(String json) throws JsonProcessingException {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    private List<ReassignmentLogEntryResponse> parseReassignmentLogForResponse(String json) {
        try {
            List<Map<String, Object>> raw = readReassignmentLogRaw(json);
            List<ReassignmentLogEntryResponse> out = new ArrayList<>();
            for (Map<String, Object> m : raw) {
                out.add(ReassignmentLogEntryResponse.builder()
                        .fromExpertId(asLong(m.get("fromExpertId")))
                        .fromExpertName(asString(m.get("fromExpertName")))
                        .toExpertId(asLong(m.get("toExpertId")))
                        .toExpertName(asString(m.get("toExpertName")))
                        .byStewardId(asLong(m.get("byStewardId")))
                        .reason(asString(m.get("reason")))
                        .at(parseAt(m.get("at")))
                        .build());
            }
            return out;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static Long asLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private static LocalDateTime parseAt(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (o instanceof List<?> list && list.size() >= 3) {
            // Jackson 可能反序列化为数组
            try {
                int y = ((Number) list.get(0)).intValue();
                int mo = ((Number) list.get(1)).intValue();
                int d = ((Number) list.get(2)).intValue();
                int h = list.size() > 3 ? ((Number) list.get(3)).intValue() : 0;
                int mi = list.size() > 4 ? ((Number) list.get(4)).intValue() : 0;
                int s = list.size() > 5 ? ((Number) list.get(5)).intValue() : 0;
                int nano = list.size() > 6 ? ((Number) list.get(6)).intValue() : 0;
                return LocalDateTime.of(y, mo, d, h, mi, s, nano);
            } catch (Exception e) {
                return null;
            }
        }
        try {
            return LocalDateTime.parse(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void appendReassignmentLog(EngagementRequest e, Expert fromExpert, Expert toExpert, User steward, String reason) {
        try {
            List<Map<String, Object>> list = new ArrayList<>(readReassignmentLogRaw(e.getReassignmentLog()));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("fromExpertId", fromExpert != null ? fromExpert.getId() : null);
            row.put("fromExpertName", fromExpert != null ? fromExpert.getName() : null);
            row.put("toExpertId", toExpert.getId());
            row.put("toExpertName", toExpert.getName());
            row.put("byStewardId", steward.getId());
            row.put("reason", reason);
            row.put("at", LocalDateTime.now().toString());
            list.add(row);
            e.setReassignmentLog(objectMapper.writeValueAsString(list));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("改派记录写入失败", ex);
        }
    }

    private Path evaluationFilesDir(long engagementId) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        return root.resolve("engagement-requests").resolve(String.valueOf(engagementId)).resolve("evaluation-files").normalize();
    }

    private static String safeFileExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int i = originalFilename.lastIndexOf('.');
        if (i < 0 || i == originalFilename.length() - 1) {
            return "";
        }
        String ext = originalFilename.substring(i).toLowerCase();
        if (ext.length() > 12 || !ext.matches("\\.[a-z0-9]+")) {
            return "";
        }
        return ext;
    }

    private static BigDecimal computeSuggestedScore(SubmitEvaluationRequest dto) {
        double p = dto.getProfessional();
        double t = dto.getTimeliness();
        double a = dto.getAttitude();
        double avg = (p + t + a) / 3.0;
        BigDecimal base = BigDecimal.valueOf(avg / 5.0 * 100).setScale(2, RoundingMode.HALF_UP);
        if (Boolean.FALSE.equals(dto.getResolved())) {
            base = base.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal cap = new BigDecimal("100");
        return base.compareTo(cap) > 0 ? cap : base;
    }

    private void assertDomainHasSteward(Domain domain) {
        if (domain == null || domain.getStewards() == null || domain.getStewards().isEmpty()) {
            throw new IllegalArgumentException("该领域未配置行管，暂不可发起申请");
        }
    }

    public EngagementRequestResponse toResponse(EngagementRequest e) {
        return EngagementRequestResponse.builder()
                .id(e.getId())
                .referenceCode(e.getReferenceCode())
                .status(e.getStatus().name())
                .mode(e.getMode().name())
                .taskType(e.getTaskType().name())
                .domainId(e.getDomain().getId())
                .domainName(e.getDomain().getName())
                .domainStewardIds(
                        e.getDomain().getStewards() == null
                                ? List.of()
                                : e.getDomain().getStewards().stream()
                                .map(User::getId)
                                .filter(Objects::nonNull)
                                .sorted()
                                .toList()
                )
                .domainStewardNames(
                        e.getDomain().getStewards() == null
                                ? List.of()
                                : e.getDomain().getStewards().stream()
                                .map(u -> u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername())
                                .filter(Objects::nonNull)
                                .sorted(Comparator.naturalOrder())
                                .toList()
                )
                .applicantId(e.getApplicant().getId())
                .applicantUsername(e.getApplicant().getUsername())
                .startAt(e.getStartAt())
                .endAt(e.getEndAt())
                .taskDescription(e.getTaskDescription())
                .designatedExpertId(e.getDesignatedExpert() != null ? e.getDesignatedExpert().getId() : null)
                .designatedExpertName(e.getDesignatedExpert() != null ? e.getDesignatedExpert().getName() : null)
                .assignedExpertId(e.getAssignedExpert() != null ? e.getAssignedExpert().getId() : null)
                .assignedExpertName(e.getAssignedExpert() != null ? e.getAssignedExpert().getName() : null)
                .assignedByStewardId(e.getAssignedBySteward() != null ? e.getAssignedBySteward().getId() : null)
                .assignmentNote(e.getAssignmentNote())
                .assignedAt(e.getAssignedAt())
                .expertAccepted(e.getExpertAccepted())
                .expertResponseNote(e.getExpertResponseNote())
                .expertRespondedAt(e.getExpertRespondedAt())
                .evalProfessional(e.getEvalProfessional())
                .evalTimeliness(e.getEvalTimeliness())
                .evalAttitude(e.getEvalAttitude())
                .evalResolved(e.getEvalResolved())
                .evalComment(e.getEvalComment())
                .evaluationSubmittedAt(e.getEvaluationSubmittedAt())
                .suggestedScore(e.getSuggestedScore())
                .evaluationAttachmentUrls(readAttachmentUrlList(e.getEvaluationAttachmentUrls()))
                .evaluationRevisionNote(e.getEvaluationRevisionNote())
                .reassignmentLog(parseReassignmentLogForResponse(e.getReassignmentLog()))
                .stewardFinalScore(e.getStewardFinalScore())
                .stewardReleaseNote(e.getStewardReleaseNote())
                .completedAt(e.getCompletedAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public Page<EngagementRequestResponse> listMine(Long userId, Pageable pageable) {
        assertCanUseEngagement(userId);
        return engagementRequestRepository.findByApplicant_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public Page<EngagementRequestResponse> listStewardQueue(Long userId, Pageable pageable) {
        assertCanUseEngagement(userId);
        Set<EngagementRequestStatus> stewardTodoStatuses = EnumSet.of(
                EngagementRequestStatus.PENDING_STEWARD_ASSIGN,
                EngagementRequestStatus.PENDING_EXPERT_CONFIRM,
                EngagementRequestStatus.PENDING_STEWARD_SCORE_RELEASE
        );
        Page<EngagementRequest> page;
        if (isSuperAdmin(userId)) {
            page = engagementRequestRepository.findByStatusInOrderByCreatedAtDesc(stewardTodoStatuses, pageable);
        } else {
            if (!userRepository.findById(userId).map(u -> u.getRoles().contains(UserRole.DOMAIN_STEWARD.name())).orElse(false)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要领域行管角色");
            }
            page = engagementRequestRepository.findPendingForSteward(
                    userId, stewardTodoStatuses, pageable);
        }
        return page.map(this::toResponse);
    }

    public Page<EngagementRequestResponse> listExpertPending(Long userId, Pageable pageable) {
        assertCanUseEngagement(userId);
        if (isSuperAdmin(userId)) {
            return engagementRequestRepository.findByStatusOrderByCreatedAtDesc(
                    EngagementRequestStatus.PENDING_EXPERT_CONFIRM, pageable
            ).map(this::toResponse);
        }
        Expert expert = expertRepository.findByOwnerId(userId).orElse(null);
        if (expert == null) {
            return Page.empty(pageable);
        }
        return engagementRequestRepository.findByAssignedExpert_IdAndStatusOrderByCreatedAtDesc(
                        expert.getId(), EngagementRequestStatus.PENDING_EXPERT_CONFIRM, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public EngagementRequestResponse createDraft(Long userId, CreateEngagementDraftRequest dto) {
        assertCanUseEngagement(userId);
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Domain domain = domainService.findById(dto.getDomainId());
        assertDomainHasSteward(domain);
        EngagementRequest e = EngagementRequest.builder()
                .mode(dto.getMode())
                .taskType(dto.getTaskType())
                .domain(domain)
                .applicant(applicant)
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .taskDescription(dto.getTaskDescription())
                .status(EngagementRequestStatus.DRAFT)
                .build();
        if (dto.getDesignatedExpertId() != null) {
            Expert designated = expertRepository.findById(dto.getDesignatedExpertId())
                    .orElseThrow(() -> new IllegalArgumentException("指定专家不存在"));
            assertExpertCoversRequestDomain(designated, domain.getId());
            e.setDesignatedExpert(designated);
        }
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse patchDraft(Long userId, Long id, PatchEngagementDraftRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (!e.getApplicant().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅申请人可编辑草稿");
        }
        if (e.getStatus() != EngagementRequestStatus.DRAFT) {
            throw new IllegalArgumentException("仅草稿状态可编辑");
        }
        if (dto.getDomainId() != null) {
            Domain domain = domainService.findById(dto.getDomainId());
            assertDomainHasSteward(domain);
            e.setDomain(domain);
        }
        if (dto.getMode() != null) {
            e.setMode(dto.getMode());
        }
        if (dto.getTaskType() != null) {
            e.setTaskType(dto.getTaskType());
        }
        if (dto.getStartAt() != null) {
            e.setStartAt(dto.getStartAt());
        }
        if (dto.getEndAt() != null) {
            e.setEndAt(dto.getEndAt());
        }
        if (dto.getTaskDescription() != null) {
            e.setTaskDescription(dto.getTaskDescription());
        }
        if (dto.getDesignatedExpertId() != null) {
            Expert designated = expertRepository.findById(dto.getDesignatedExpertId())
                    .orElseThrow(() -> new IllegalArgumentException("指定专家不存在"));
            assertExpertCoversRequestDomain(designated, e.getDomain().getId());
            e.setDesignatedExpert(designated);
        }
        if (e.getDesignatedExpert() != null) {
            assertExpertCoversRequestDomain(e.getDesignatedExpert(), e.getDomain().getId());
        }
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse submit(Long userId, Long id) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (!e.getApplicant().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅申请人可提交");
        }
        if (e.getStatus() != EngagementRequestStatus.DRAFT) {
            throw new IllegalArgumentException("仅草稿可提交");
        }
        if (e.getTaskDescription() == null || e.getTaskDescription().trim().length() < MIN_TASK_DESC_LEN) {
            throw new IllegalArgumentException("任务描述至少 " + MIN_TASK_DESC_LEN + " 个字符");
        }
        assertDomainHasSteward(e.getDomain());
        if (e.getEndAt() != null && e.getEndAt().isBefore(e.getStartAt())) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }
        if (e.getDesignatedExpert() != null) {
            assertExpertCoversRequestDomain(e.getDesignatedExpert(), e.getDomain().getId());
        }
        e.setStatus(EngagementRequestStatus.PENDING_STEWARD_ASSIGN);
        EngagementRequest saved = engagementRequestRepository.save(e);
        if (saved.getReferenceCode() == null || saved.getReferenceCode().isBlank()) {
            saved.setReferenceCode(newReferenceCode(saved.getId()));
            saved = engagementRequestRepository.save(saved);
        }
        return toResponse(saved);
    }

    @Transactional
    public EngagementRequestResponse stewardAssign(Long userId, Long id, StewardAssignRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (e.getStatus() != EngagementRequestStatus.PENDING_STEWARD_ASSIGN) {
            throw new IllegalArgumentException("当前状态不可指派");
        }
        if (!isSuperAdmin(userId) && !domainService.isUserStewardOfDomain(userId, e.getDomain().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅该领域行管或超级管理员可指派");
        }
        Expert expert = expertRepository.findById(dto.getExpertId())
                .orElseThrow(() -> new IllegalArgumentException("专家不存在"));
        assertExpertCoversRequestDomain(expert, e.getDomain().getId());
        User steward = userRepository.findById(userId).orElseThrow();
        e.setAssignedExpert(expert);
        e.setAssignedBySteward(steward);
        e.setAssignmentNote(dto.getAssignmentNote());
        e.setAssignedAt(LocalDateTime.now());
        e.setStatus(EngagementRequestStatus.PENDING_EXPERT_CONFIRM);
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse stewardReassign(Long userId, Long id, ReassignEngagementRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (e.getStatus() != EngagementRequestStatus.PENDING_EXPERT_CONFIRM
                && e.getStatus() != EngagementRequestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("当前状态不可改派");
        }
        if (!isSuperAdmin(userId) && !domainService.isUserStewardOfDomain(userId, e.getDomain().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅该领域行管或超级管理员可改派");
        }
        if (e.getAssignedExpert() == null) {
            throw new IllegalStateException("尚未指派专家，请使用指派接口");
        }
        Expert newExpert = expertRepository.findById(dto.getExpertId())
                .orElseThrow(() -> new IllegalArgumentException("专家不存在"));
        assertExpertCoversRequestDomain(newExpert, e.getDomain().getId());
        if (newExpert.getId().equals(e.getAssignedExpert().getId())) {
            throw new IllegalArgumentException("新专家与当前专家相同");
        }
        Expert old = e.getAssignedExpert();
        User steward = userRepository.findById(userId).orElseThrow();
        appendReassignmentLog(e, old, newExpert, steward, dto.getReason());
        e.setAssignedExpert(newExpert);
        e.setAssignedBySteward(steward);
        e.setAssignedAt(LocalDateTime.now());
        if (dto.getReason() != null && !dto.getReason().isBlank()) {
            String prev = e.getAssignmentNote() != null ? e.getAssignmentNote() : "";
            e.setAssignmentNote(prev.isBlank() ? "[改派] " + dto.getReason() : prev + "\n[改派] " + dto.getReason());
        }
        e.setExpertAccepted(null);
        e.setExpertResponseNote(null);
        e.setExpertRespondedAt(null);
        e.setStatus(EngagementRequestStatus.PENDING_EXPERT_CONFIRM);
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse requestEvaluationRevision(Long userId, Long id, RequestEvaluationRevisionRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (e.getStatus() != EngagementRequestStatus.PENDING_STEWARD_SCORE_RELEASE) {
            throw new IllegalArgumentException("仅待行管放分时可退回重评");
        }
        if (!isSuperAdmin(userId) && !domainService.isUserStewardOfDomain(userId, e.getDomain().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅该领域行管或超级管理员可操作");
        }
        e.setEvalProfessional(null);
        e.setEvalTimeliness(null);
        e.setEvalAttitude(null);
        e.setEvalResolved(null);
        e.setEvalComment(null);
        e.setEvaluationSubmittedAt(null);
        e.setSuggestedScore(null);
        e.setEvaluationAttachmentUrls(null);
        e.setEvaluationRevisionNote(dto.getReason());
        e.setStatus(EngagementRequestStatus.IN_PROGRESS);
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse expertDecision(Long userId, Long id, ExpertDecisionRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (e.getStatus() != EngagementRequestStatus.PENDING_EXPERT_CONFIRM) {
            throw new IllegalArgumentException("当前状态不需要专家确认");
        }
        if (e.getAssignedExpert() == null) {
            throw new IllegalStateException("未指派专家");
        }
        if (!isSuperAdmin(userId) && !isExpertOwner(userId, e.getAssignedExpert().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅被指派的专家本人可操作");
        }
        e.setExpertRespondedAt(LocalDateTime.now());
        e.setExpertResponseNote(dto.getNote());
        if (Boolean.TRUE.equals(dto.getAccepted())) {
            e.setExpertAccepted(true);
            e.setStatus(EngagementRequestStatus.IN_PROGRESS);
        } else {
            e.setExpertAccepted(false);
            e.setStatus(EngagementRequestStatus.REJECTED);
        }
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse submitEvaluation(Long userId, Long id, SubmitEvaluationRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (!e.getApplicant().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅申请人可提交评价");
        }
        if (e.getStatus() != EngagementRequestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("仅执行中可申请评价");
        }
        e.setEvalProfessional(dto.getProfessional());
        e.setEvalTimeliness(dto.getTimeliness());
        e.setEvalAttitude(dto.getAttitude());
        e.setEvalResolved(dto.getResolved());
        e.setEvalComment(dto.getComment());
        e.setSuggestedScore(computeSuggestedScore(dto));
        e.setEvaluationRevisionNote(null);
        try {
            if (dto.getAttachmentUrls() != null && !dto.getAttachmentUrls().isEmpty()) {
                e.setEvaluationAttachmentUrls(objectMapper.writeValueAsString(dto.getAttachmentUrls()));
            } else {
                e.setEvaluationAttachmentUrls(null);
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("附件列表序列化失败");
        }
        e.setEvaluationSubmittedAt(LocalDateTime.now());
        e.setStatus(EngagementRequestStatus.PENDING_STEWARD_SCORE_RELEASE);
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EvaluationFileUploadResponse uploadEvaluationFile(Long userId, Long id, MultipartFile file) throws IOException {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (!e.getApplicant().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅申请人可上传评价附件");
        }
        if (e.getStatus() != EngagementRequestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("仅执行中可上传评价附件");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = safeFileExtension(original);
        String stored = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : ext);
        Path dir = evaluationFilesDir(id);
        Files.createDirectories(dir);
        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir)) {
            throw new IllegalStateException("非法路径");
        }
        file.transferTo(target.toFile());
        String rel = "/engagement-requests/" + id + "/evaluation-files/" + stored;
        return EvaluationFileUploadResponse.builder()
                .path(rel)
                .originalFilename(original)
                .build();
    }

    public Resource readEvaluationFile(Long userId, Long id, String fileName) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        assertCanView(userId, e);
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法文件名");
        }
        Path dir = evaluationFilesDir(id);
        Path file = dir.resolve(fileName).normalize();
        if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        try {
            return new UrlResource(file.toUri());
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
    }

    @Transactional
    public EngagementRequestResponse releaseScore(Long userId, Long id, ReleaseScoreRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (e.getStatus() != EngagementRequestStatus.PENDING_STEWARD_SCORE_RELEASE) {
            throw new IllegalArgumentException("当前状态不可放分");
        }
        if (!isSuperAdmin(userId) && !domainService.isUserStewardOfDomain(userId, e.getDomain().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅该领域行管或超级管理员可放分");
        }
        e.setStewardFinalScore(dto.getFinalScore());
        e.setStewardReleaseNote(dto.getReleaseNote());
        e.setCompletedAt(LocalDateTime.now());
        e.setStatus(EngagementRequestStatus.COMPLETED);
        EngagementRequest saved = engagementRequestRepository.save(e);

        BigDecimal credit = dto.getFinalScore();
        if (credit == null) {
            credit = saved.getSuggestedScore();
        }
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0 && saved.getAssignedExpert() != null) {
            Expert ex = expertRepository.findById(saved.getAssignedExpert().getId()).orElse(null);
            if (ex != null && ex.getOwner() != null) {
                pointsService.creditFromEngagement(ex.getOwner().getId(), credit, saved.getId());
            }
        }
        return toResponse(saved);
    }
}
