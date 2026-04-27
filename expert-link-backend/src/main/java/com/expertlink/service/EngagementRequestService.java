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
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return buildResponse(e, userId);
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
        if (e.getAssignedExperts() != null) {
            for (Expert ex : e.getAssignedExperts()) {
                if (ex.getId() != null && isExpertOwner(userId, ex.getId())) {
                    return;
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权查看该申请单");
    }

    private boolean isExpertOwner(Long userId, Long expertId) {
        return expertRepository.findById(expertId)
                .map(ex -> ex.getOwner() != null && userId.equals(ex.getOwner().getId()))
                .orElse(false);
    }

    private boolean isAssignedExpertOwner(Long userId, EngagementRequest e) {
        if (e.getAssignedExperts() == null || e.getAssignedExperts().isEmpty()) {
            return false;
        }
        for (Expert ex : e.getAssignedExperts()) {
            if (ex.getId() != null && isExpertOwner(userId, ex.getId())) {
                return true;
            }
        }
        return false;
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

    private Set<Expert> resolveDesignatedExperts(List<Long> expertIds, Long domainId) {
        if (expertIds == null || expertIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Expert> experts = new LinkedHashSet<>();
        for (Long expertId : expertIds) {
            if (expertId == null) {
                continue;
            }
            Expert expert = expertRepository.findById(expertId)
                    .orElseThrow(() -> new IllegalArgumentException("指定专家不存在"));
            assertExpertCoversRequestDomain(expert, domainId);
            experts.add(expert);
        }
        return experts;
    }

    private Set<Expert> resolveAssignedExperts(List<Long> expertIds, long domainId) {
        if (expertIds == null || expertIds.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一名专家");
        }
        LinkedHashSet<Long> dedup = new LinkedHashSet<>();
        for (Long id : expertIds) {
            if (id != null && id > 0) {
                dedup.add(id);
            }
        }
        if (dedup.isEmpty()) {
            throw new IllegalArgumentException("请至少选择一名专家");
        }
        Set<Expert> experts = new LinkedHashSet<>();
        for (Long expertId : dedup) {
            Expert expert = expertRepository.findById(expertId)
                    .orElseThrow(() -> new IllegalArgumentException("专家不存在"));
            assertExpertCoversRequestDomain(expert, domainId);
            experts.add(expert);
        }
        return experts;
    }

    private List<Map<String, Object>> readAssignmentDecisions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private String writeAssignmentDecisions(List<Map<String, Object>> rows) throws JsonProcessingException {
        return objectMapper.writeValueAsString(rows);
    }

    private String initDecisionsJson(Set<Expert> experts) {
        if (experts == null || experts.isEmpty()) {
            return null;
        }
        try {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Expert ex : experts.stream().sorted(Comparator.comparing(Expert::getId)).toList()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("expertId", ex.getId());
                m.put("accepted", null);
                m.put("note", null);
                m.put("at", null);
                rows.add(m);
            }
            return writeAssignmentDecisions(rows);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("初始化专家确认状态失败", e);
        }
    }

    private static Boolean asBooleanObject(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(o.toString());
    }

    private Boolean decisionAcceptedForExpert(EngagementRequest e, Long expertId) {
        for (Map<String, Object> m : readAssignmentDecisions(e.getAssignmentExpertDecisions())) {
            Long id = asLong(m.get("expertId"));
            if (id != null && id.equals(expertId)) {
                return asBooleanObject(m.get("accepted"));
            }
        }
        return null;
    }

    private boolean allExpertsAccepted(EngagementRequest e) {
        if (e.getAssignedExperts() == null || e.getAssignedExperts().isEmpty()) {
            return false;
        }
        List<Map<String, Object>> rows = readAssignmentDecisions(e.getAssignmentExpertDecisions());
        for (Expert ex : e.getAssignedExperts()) {
            boolean ok = false;
            for (Map<String, Object> m : rows) {
                if (ex.getId().equals(asLong(m.get("expertId"))) && Boolean.TRUE.equals(asBooleanObject(m.get("accepted")))) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private void applyExpertDecisionRow(EngagementRequest e, Long expertId, boolean accepted, String note) {
        List<Map<String, Object>> rows = new ArrayList<>(readAssignmentDecisions(e.getAssignmentExpertDecisions()));
        boolean found = false;
        for (Map<String, Object> m : rows) {
            Long id = asLong(m.get("expertId"));
            if (id != null && id.equals(expertId)) {
                if (asBooleanObject(m.get("accepted")) != null) {
                    throw new IllegalArgumentException("该专家已确认过，不能重复操作");
                }
                m.put("accepted", accepted);
                m.put("note", note);
                m.put("at", LocalDateTime.now().toString());
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("未找到该专家的确认记录");
        }
        try {
            e.setAssignmentExpertDecisions(writeAssignmentDecisions(rows));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("更新专家确认状态失败", ex);
        }
    }

    private void appendBatchReassignmentLog(EngagementRequest e, Set<Expert> oldExperts, Set<Expert> newExperts, User steward, String reason) {
        try {
            String fromNames = oldExperts == null || oldExperts.isEmpty()
                    ? "—"
                    : oldExperts.stream().sorted(Comparator.comparing(Expert::getId)).map(Expert::getName).collect(Collectors.joining("、"));
            String toNames = newExperts.stream().sorted(Comparator.comparing(Expert::getId)).map(Expert::getName).collect(Collectors.joining("、"));
            List<Map<String, Object>> list = new ArrayList<>(readReassignmentLogRaw(e.getReassignmentLog()));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("fromExpertId", null);
            row.put("fromExpertName", fromNames);
            row.put("toExpertId", null);
            row.put("toExpertName", toNames);
            row.put("byStewardId", steward.getId());
            row.put("reason", reason);
            row.put("at", LocalDateTime.now().toString());
            list.add(row);
            e.setReassignmentLog(objectMapper.writeValueAsString(list));
        } catch (Exception ex) {
            throw new IllegalStateException("改派记录写入失败", ex);
        }
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

    private static List<BigDecimal> splitCreditEvenly(BigDecimal credit, int n) {
        if (n <= 0 || credit == null) {
            return List.of();
        }
        long cents = credit.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValue();
        long each = cents / n;
        long rem = cents % n;
        List<BigDecimal> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            long c = each + (i < rem ? 1L : 0L);
            out.add(BigDecimal.valueOf(c).movePointLeft(2));
        }
        return out;
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

    private static BigDecimal computeSuggestedScore(SubmitEvaluationRequest.ExpertEvaluationItem dto) {
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
        return buildResponse(e, null);
    }

    private EngagementRequestResponse buildResponse(EngagementRequest e, Long viewerUserId) {
        List<Expert> designatedExperts = e.getDesignatedExperts() == null
                ? List.of()
                : e.getDesignatedExperts().stream()
                .sorted(Comparator.comparing(Expert::getId))
                .toList();
        Expert firstDesignated = designatedExperts.isEmpty() ? null : designatedExperts.get(0);

        List<Expert> assignedExperts = e.getAssignedExperts() == null
                ? List.of()
                : e.getAssignedExperts().stream()
                .sorted(Comparator.comparing(Expert::getId))
                .toList();
        Expert firstAssigned = assignedExperts.isEmpty() ? null : assignedExperts.get(0);

        Boolean viewerPending = null;
        Boolean viewerAmongAssigned = null;
        if (viewerUserId != null) {
            Expert mine = expertRepository.findByOwnerId(viewerUserId).orElse(null);
            boolean amongAssigned = mine != null && mine.getId() != null && e.getAssignedExperts() != null
                    && e.getAssignedExperts().stream().anyMatch(x -> mine.getId().equals(x.getId()));
            viewerAmongAssigned = amongAssigned;
            if (e.getStatus() == EngagementRequestStatus.PENDING_EXPERT_CONFIRM && amongAssigned) {
                Boolean st = decisionAcceptedForExpert(e, mine.getId());
                viewerPending = st == null;
            } else {
                viewerPending = false;
            }
        }

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
                .designatedExpertIds(designatedExperts.stream().map(Expert::getId).toList())
                .designatedExpertNames(designatedExperts.stream().map(Expert::getName).toList())
                .designatedExpertId(firstDesignated != null ? firstDesignated.getId() : null)
                .designatedExpertName(firstDesignated != null ? firstDesignated.getName() : null)
                .assignedExpertIds(assignedExperts.stream().map(Expert::getId).toList())
                .assignedExpertNames(assignedExperts.stream().map(Expert::getName).toList())
                .viewerExpertConfirmPending(viewerPending)
                .viewerAmongAssignedExperts(viewerAmongAssigned)
                .assignedExpertId(firstAssigned != null ? firstAssigned.getId() : null)
                .assignedExpertName(firstAssigned != null ? firstAssigned.getName() : null)
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
                .rollbackNote(e.getRollbackNote())
                .rolledBackAt(e.getRolledBackAt())
                .reassignmentLog(parseReassignmentLogForResponse(e.getReassignmentLog()))
                .cancelReason(e.getCancelReason())
                .cancelledAt(e.getCancelledAt())
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
            ).map(e -> buildResponse(e, userId));
        }
        Expert expert = expertRepository.findByOwnerId(userId).orElse(null);
        if (expert == null) {
            return Page.empty(pageable);
        }
        return engagementRequestRepository.findByAssignedExpertMemberAndStatusOrderByCreatedAtDesc(
                        expert.getId(), EngagementRequestStatus.PENDING_EXPERT_CONFIRM, pageable)
                .map(e -> buildResponse(e, userId));
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
        e.setDesignatedExperts(resolveDesignatedExperts(dto.getDesignatedExpertIds(), domain.getId()));
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
        if (dto.getDesignatedExpertIds() != null) {
            e.setDesignatedExperts(resolveDesignatedExperts(dto.getDesignatedExpertIds(), e.getDomain().getId()));
        }
        if (e.getDesignatedExperts() != null) {
            for (Expert expert : e.getDesignatedExperts()) {
                assertExpertCoversRequestDomain(expert, e.getDomain().getId());
            }
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
        if (e.getDesignatedExperts() != null) {
            for (Expert expert : e.getDesignatedExperts()) {
                assertExpertCoversRequestDomain(expert, e.getDomain().getId());
            }
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
        Set<Expert> experts = resolveAssignedExperts(dto.getExpertIds(), e.getDomain().getId());
        User steward = userRepository.findById(userId).orElseThrow();
        e.setAssignedExperts(new LinkedHashSet<>(experts));
        e.setAssignedBySteward(steward);
        e.setAssignmentNote(dto.getAssignmentNote());
        e.setAssignedAt(LocalDateTime.now());
        e.setAssignmentExpertDecisions(initDecisionsJson(experts));
        e.setExpertAccepted(null);
        e.setExpertResponseNote(null);
        e.setExpertRespondedAt(null);
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
        Set<Expert> oldExperts = e.getAssignedExperts() == null || e.getAssignedExperts().isEmpty()
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(e.getAssignedExperts());
        if (oldExperts.isEmpty()) {
            throw new IllegalStateException("尚未指派专家，请使用指派接口");
        }
        Set<Expert> newExperts = resolveAssignedExperts(dto.getExpertIds(), e.getDomain().getId());
        Set<Long> oldIds = oldExperts.stream().map(Expert::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> newIds = newExperts.stream().map(Expert::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        if (oldIds.equals(newIds)) {
            throw new IllegalArgumentException("新专家名单与当前指派相同");
        }
        User steward = userRepository.findById(userId).orElseThrow();
        appendBatchReassignmentLog(e, oldExperts, newExperts, steward, dto.getReason());
        e.setAssignedExperts(new LinkedHashSet<>(newExperts));
        e.setAssignedBySteward(steward);
        e.setAssignedAt(LocalDateTime.now());
        e.setAssignmentExpertDecisions(initDecisionsJson(newExperts));
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
        long targetExpertId = resolveTargetExpertIdForDecision(userId, e, dto);
        boolean accepted = Boolean.TRUE.equals(dto.getAccepted());
        String note = dto.getNote() == null ? "" : dto.getNote().trim();
        if (!accepted && note.isBlank()) {
            throw new IllegalArgumentException("拒绝时请填写备注说明");
        }
        applyExpertDecisionRow(e, targetExpertId, accepted, dto.getNote());
        e.setExpertRespondedAt(LocalDateTime.now());
        e.setExpertResponseNote(note.isBlank() ? null : note);
        if (!accepted) {
            e.setExpertAccepted(false);
            e.setStatus(EngagementRequestStatus.PENDING_STEWARD_ASSIGN);
            return toResponse(engagementRequestRepository.save(e));
        }
        if (allExpertsAccepted(e)) {
            e.setExpertAccepted(true);
            e.setStatus(EngagementRequestStatus.IN_PROGRESS);
        } else {
            e.setExpertAccepted(null);
        }
        return toResponse(engagementRequestRepository.save(e));
    }

    private long resolveTargetExpertIdForDecision(Long userId, EngagementRequest e, ExpertDecisionRequest dto) {
        if (e.getAssignedExperts() == null || e.getAssignedExperts().isEmpty()) {
            throw new IllegalStateException("未指派专家");
        }
        if (isSuperAdmin(userId)) {
            if (dto.getExpertId() != null) {
                long tid = dto.getExpertId();
                boolean in = e.getAssignedExperts().stream().anyMatch(x -> x.getId() != null && x.getId() == tid);
                if (!in) {
                    throw new IllegalArgumentException("expertId 不在当前指派名单中");
                }
                return tid;
            }
            Expert selfExpert = expertRepository.findByOwnerId(userId).orElse(null);
            if (selfExpert != null
                    && selfExpert.getId() != null
                    && e.getAssignedExperts().stream().anyMatch(x -> selfExpert.getId().equals(x.getId()))) {
                return selfExpert.getId();
            }
            throw new IllegalArgumentException("超级管理员代为确认时请传 expertId");
        }
        Expert mine = expertRepository.findByOwnerId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "仅专家账号可操作"));
        if (e.getAssignedExperts().stream().noneMatch(x -> mine.getId().equals(x.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅被指派的专家本人可操作");
        }
        return mine.getId();
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

        List<SubmitEvaluationRequest.ExpertEvaluationItem> byExpert = dto.getExpertEvaluations();
        if (byExpert != null && !byExpert.isEmpty()) {
            if (e.getAssignedExperts() == null || e.getAssignedExperts().isEmpty()) {
                throw new IllegalArgumentException("当前申请单未指派专家，无法按专家评价");
            }
            Set<Long> assignedIds = e.getAssignedExperts().stream()
                    .map(Expert::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            LinkedHashMap<Long, SubmitEvaluationRequest.ExpertEvaluationItem> evalMap = new LinkedHashMap<>();
            for (SubmitEvaluationRequest.ExpertEvaluationItem item : byExpert) {
                if (item == null || item.getExpertId() == null) {
                    continue;
                }
                if (!assignedIds.contains(item.getExpertId())) {
                    throw new IllegalArgumentException("存在不在指派名单内的专家评价");
                }
                if (evalMap.containsKey(item.getExpertId())) {
                    throw new IllegalArgumentException("同一专家存在重复评价");
                }
                evalMap.put(item.getExpertId(), item);
            }
            if (evalMap.size() != assignedIds.size()) {
                throw new IllegalArgumentException("请对每位被指派专家分别评价后再提交");
            }

            int sumProfessional = 0;
            int sumTimeliness = 0;
            int sumAttitude = 0;
            boolean allResolved = true;
            BigDecimal scoreSum = BigDecimal.ZERO;
            List<Map<String, Object>> detailRows = new ArrayList<>();

            for (Expert ex : e.getAssignedExperts().stream().sorted(Comparator.comparing(Expert::getId)).toList()) {
                SubmitEvaluationRequest.ExpertEvaluationItem item = evalMap.get(ex.getId());
                if (item == null) {
                    throw new IllegalArgumentException("请对每位被指派专家分别评价后再提交");
                }
                sumProfessional += item.getProfessional();
                sumTimeliness += item.getTimeliness();
                sumAttitude += item.getAttitude();
                allResolved = allResolved && Boolean.TRUE.equals(item.getResolved());
                BigDecimal singleScore = computeSuggestedScore(item);
                scoreSum = scoreSum.add(singleScore);

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("expertId", ex.getId());
                row.put("expertName", ex.getName());
                row.put("professional", item.getProfessional());
                row.put("timeliness", item.getTimeliness());
                row.put("attitude", item.getAttitude());
                row.put("resolved", item.getResolved());
                row.put("comment", item.getComment());
                row.put("suggestedScore", singleScore);
                detailRows.add(row);
            }

            int size = evalMap.size();
            e.setEvalProfessional((int) Math.round(sumProfessional * 1.0 / size));
            e.setEvalTimeliness((int) Math.round(sumTimeliness * 1.0 / size));
            e.setEvalAttitude((int) Math.round(sumAttitude * 1.0 / size));
            e.setEvalResolved(allResolved);
            e.setSuggestedScore(scoreSum.divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP));
            try {
                e.setEvalComment(objectMapper.writeValueAsString(detailRows));
            } catch (JsonProcessingException ex) {
                throw new IllegalArgumentException("评价详情序列化失败");
            }
        } else {
            e.setEvalProfessional(dto.getProfessional());
            e.setEvalTimeliness(dto.getTimeliness());
            e.setEvalAttitude(dto.getAttitude());
            e.setEvalResolved(dto.getResolved());
            e.setEvalComment(dto.getComment());
            e.setSuggestedScore(computeSuggestedScore(dto));
        }
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
        if (credit != null && credit.compareTo(BigDecimal.ZERO) > 0
                && saved.getAssignedExperts() != null
                && !saved.getAssignedExperts().isEmpty()) {
            List<Expert> list = saved.getAssignedExperts().stream()
                    .sorted(Comparator.comparing(Expert::getId))
                    .toList();
            List<BigDecimal> portions = splitCreditEvenly(credit, list.size());
            for (int i = 0; i < list.size(); i++) {
                Expert ex = list.get(i);
                BigDecimal portion = portions.get(i);
                if (portion.compareTo(BigDecimal.ZERO) > 0 && ex.getOwner() != null) {
                    pointsService.creditFromEngagement(ex.getOwner().getId(), portion, saved.getId());
                }
            }
        }
        return toResponse(saved);
    }

    @Transactional
    public EngagementRequestResponse rollbackToPreviousNode(Long userId, Long id, RollbackEngagementRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        String reason = dto.getReason() == null ? "" : dto.getReason().trim();
        if (reason.isBlank()) {
            throw new IllegalArgumentException("退回说明不能为空");
        }
        EngagementRequestStatus current = e.getStatus();
        EngagementRequestStatus target;
        switch (current) {
            case PENDING_EXPERT_CONFIRM -> {
                if (!isSuperAdmin(userId) && !isAssignedExpertOwner(userId, e)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅当前节点责任专家可退回");
                }
                target = EngagementRequestStatus.PENDING_STEWARD_ASSIGN;
            }
            case IN_PROGRESS -> {
                if (!isSuperAdmin(userId) && !isAssignedExpertOwner(userId, e)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅当前节点责任专家可退回");
                }
                target = EngagementRequestStatus.PENDING_EXPERT_CONFIRM;
            }
            case PENDING_STEWARD_SCORE_RELEASE -> {
                if (!isSuperAdmin(userId) && !domainService.isUserStewardOfDomain(userId, e.getDomain().getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅当前节点责任行管可退回");
                }
                target = EngagementRequestStatus.IN_PROGRESS;
            }
            default -> throw new IllegalArgumentException("当前状态不支持退回上一节点");
        }
        e.setStatus(target);
        e.setRollbackNote(reason);
        e.setRolledBackAt(LocalDateTime.now());
        return toResponse(engagementRequestRepository.save(e));
    }

    @Transactional
    public EngagementRequestResponse cancelByApplicant(Long userId, Long id, CancelEngagementRequest dto) {
        assertCanUseEngagement(userId);
        EngagementRequest e = requireEntity(id);
        if (!e.getApplicant().getId().equals(userId) && !isSuperAdmin(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅申请人可取消申请");
        }
        if (e.getStatus() == EngagementRequestStatus.COMPLETED
                || e.getStatus() == EngagementRequestStatus.REJECTED
                || e.getStatus() == EngagementRequestStatus.CANCELLED) {
            throw new IllegalArgumentException("当前状态不可取消");
        }
        e.setStatus(EngagementRequestStatus.CANCELLED);
        String reason = dto.getReason() == null ? null : dto.getReason().trim();
        e.setCancelReason((reason == null || reason.isBlank()) ? null : reason);
        e.setCancelledAt(LocalDateTime.now());
        return toResponse(engagementRequestRepository.save(e));
    }
}
