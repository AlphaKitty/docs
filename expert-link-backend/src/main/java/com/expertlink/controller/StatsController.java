package com.expertlink.controller;

import com.expertlink.domain.Expert;
import com.expertlink.domain.Project;
import com.expertlink.domain.Skill;
import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.ProjectRepository;
import com.expertlink.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ExpertRepository expertRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final DomainRepository domainRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        long totalExperts = expertRepository.count();
        long activeExperts = expertRepository.countAvailableExperts();
        long totalProjects = projectRepository.count();
        long ongoingProjects = projectRepository.countByStatus("IN_PROGRESS");
        long totalSkills = skillRepository.count();
        long totalDomains = domainRepository.count();

        List<Project> projects = projectRepository.findAll();
        BigDecimal monthlyRevenue = projects.stream()
                .map(Project::getBudget)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal utilizationRate = totalExperts == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(activeExperts * 100.0 / totalExperts).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("totalExperts", totalExperts);
        body.put("activeExperts", activeExperts);
        body.put("totalProjects", totalProjects);
        body.put("ongoingProjects", ongoingProjects);
        body.put("totalSkills", totalSkills);
        body.put("totalDomains", totalDomains);
        body.put("monthlyRevenue", monthlyRevenue);
        body.put("utilizationRate", utilizationRate);
        return ApiResponses.ok(body);
    }

    @GetMapping("/experts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> experts() {
        List<Expert> experts = expertRepository.findAll();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("ACTIVE", 0L);
        byStatus.put("INACTIVE", 0L);
        byStatus.put("PENDING", 0L);
        byStatus.put("ARCHIVED", 0L);
        for (Expert expert : experts) {
            String status = normalizeUpper(expert.getAvailabilityStatus(), "ACTIVE");
            if (!byStatus.containsKey(status)) {
                status = "ACTIVE";
            }
            byStatus.put(status, byStatus.get(status) + 1);
        }

        Map<String, Long> byLevel = new LinkedHashMap<>();
        byLevel.put("JUNIOR", 0L);
        byLevel.put("MIDDLE", 0L);
        byLevel.put("SENIOR", 0L);
        byLevel.put("EXPERT", 0L);
        for (Expert expert : experts) {
            int level = expert.getVerificationLevel() == null ? 0 : expert.getVerificationLevel();
            String bucket = level >= 4 ? "EXPERT" : level >= 3 ? "SENIOR" : level >= 2 ? "MIDDLE" : "JUNIOR";
            byLevel.put(bucket, byLevel.get(bucket) + 1);
        }

        Map<Long, Long> domainCounts = experts.stream()
                .filter(e -> e.getPrimaryDomain() != null && e.getPrimaryDomain().getId() != null)
                .collect(Collectors.groupingBy(e -> e.getPrimaryDomain().getId(), Collectors.counting()));
        List<Map<String, Object>> byDomain = new ArrayList<>();
        domainCounts.forEach((domainId, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("domainId", domainId);
            item.put("domainName", experts.stream()
                    .map(Expert::getPrimaryDomain)
                    .filter(Objects::nonNull)
                    .filter(d -> domainId.equals(d.getId()))
                    .map(d -> d.getName() == null ? "" : d.getName())
                    .findFirst()
                    .orElse(""));
            item.put("count", count);
            byDomain.add(item);
        });

        Map<Long, Map<String, Object>> skillAccumulator = new LinkedHashMap<>();
        for (Expert expert : experts) {
            if (expert.getSkills() == null) continue;
            for (Skill skill : expert.getSkills()) {
                if (skill.getId() == null) continue;
                Map<String, Object> item = skillAccumulator.computeIfAbsent(skill.getId(), k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("skillId", skill.getId());
                    m.put("skillName", skill.getName());
                    m.put("count", 0L);
                    return m;
                });
                item.put("count", ((Long) item.get("count")) + 1);
            }
        }
        List<Map<String, Object>> bySkill = new ArrayList<>(skillAccumulator.values());

        Map<Integer, Long> ratingBuckets = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) ratingBuckets.put(i, 0L);
        for (Expert expert : experts) {
            int bucket = expert.getOverallRating() == null ? 1 : Math.max(1, Math.min(5, expert.getOverallRating().intValue()));
            ratingBuckets.put(bucket, ratingBuckets.get(bucket) + 1);
        }
        List<Map<String, Object>> ratingDistribution = ratingBuckets.entrySet().stream().map(entry -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rating", entry.getKey());
            item.put("count", entry.getValue());
            return item;
        }).toList();

        Map<String, Long> rateBuckets = new LinkedHashMap<>();
        rateBuckets.put("0-200", 0L);
        rateBuckets.put("200-500", 0L);
        rateBuckets.put("500-1000", 0L);
        rateBuckets.put("1000+", 0L);
        for (Expert expert : experts) {
            BigDecimal hourlyRate = expert.getHourlyRate() == null ? BigDecimal.ZERO : expert.getHourlyRate();
            String bucket;
            if (hourlyRate.compareTo(BigDecimal.valueOf(200)) < 0) bucket = "0-200";
            else if (hourlyRate.compareTo(BigDecimal.valueOf(500)) < 0) bucket = "200-500";
            else if (hourlyRate.compareTo(BigDecimal.valueOf(1000)) < 0) bucket = "500-1000";
            else bucket = "1000+";
            rateBuckets.put(bucket, rateBuckets.get(bucket) + 1);
        }
        List<Map<String, Object>> hourlyRateDistribution = rateBuckets.entrySet().stream().map(entry -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("range", entry.getKey());
            item.put("count", entry.getValue());
            return item;
        }).toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("byStatus", byStatus);
        body.put("byLevel", byLevel);
        body.put("byDomain", byDomain);
        body.put("bySkill", bySkill);
        body.put("ratingDistribution", ratingDistribution);
        body.put("hourlyRateDistribution", hourlyRateDistribution);
        return ApiResponses.ok(body);
    }

    @GetMapping("/projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> projects() {
        List<Project> projects = projectRepository.findAll();

        Map<String, Long> byStatus = initCounter("PLANNING", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "CANCELLED");
        Map<String, Long> byPriority = initCounter("LOW", "MEDIUM", "HIGH", "CRITICAL");
        for (Project project : projects) {
            String status = normalizeUpper(project.getStatus(), "PLANNING");
            if (!byStatus.containsKey(status)) status = "PLANNING";
            byStatus.put(status, byStatus.get(status) + 1);

            String priority = normalizeUpper(project.getPriority(), "MEDIUM");
            if (!byPriority.containsKey(priority)) priority = "MEDIUM";
            byPriority.put(priority, byPriority.get(priority) + 1);
        }

        Map<Long, Map<String, Object>> byDomainMap = new LinkedHashMap<>();
        for (Project project : projects) {
            if (project.getDomain() == null || project.getDomain().getId() == null) continue;
            long domainId = project.getDomain().getId();
            Map<String, Object> item = byDomainMap.computeIfAbsent(domainId, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("domainId", domainId);
                m.put("domainName", project.getDomain().getName());
                m.put("count", 0L);
                m.put("totalBudget", BigDecimal.ZERO);
                return m;
            });
            item.put("count", ((Long) item.get("count")) + 1);
            BigDecimal b = project.getBudget() == null ? BigDecimal.ZERO : project.getBudget();
            item.put("totalBudget", ((BigDecimal) item.get("totalBudget")).add(b));
        }
        List<Map<String, Object>> byDomain = new ArrayList<>(byDomainMap.values());

        Map<String, Map<String, Object>> budgetDist = new LinkedHashMap<>();
        budgetDist.put("0-50w", budgetBucket("0-50w"));
        budgetDist.put("50w-200w", budgetBucket("50w-200w"));
        budgetDist.put("200w+", budgetBucket("200w+"));
        for (Project project : projects) {
            BigDecimal budget = project.getBudget() == null ? BigDecimal.ZERO : project.getBudget();
            String key;
            if (budget.compareTo(BigDecimal.valueOf(500_000)) < 0) key = "0-50w";
            else if (budget.compareTo(BigDecimal.valueOf(2_000_000)) < 0) key = "50w-200w";
            else key = "200w+";
            Map<String, Object> item = budgetDist.get(key);
            item.put("count", ((Long) item.get("count")) + 1);
            item.put("totalBudget", ((BigDecimal) item.get("totalBudget")).add(budget));
        }

        List<Map<String, Object>> timeline = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i).withDayOfMonth(1);
            String label = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", label);
            item.put("started", 0);
            item.put("completed", 0);
            item.put("revenue", BigDecimal.ZERO);
            timeline.add(item);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("byStatus", byStatus);
        body.put("byPriority", byPriority);
        body.put("byDomain", byDomain);
        body.put("budgetDistribution", new ArrayList<>(budgetDist.values()));
        body.put("timeline", timeline);
        return ApiResponses.ok(body);
    }

    @GetMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> skills() {
        List<Skill> skills = skillRepository.findAll();
        Map<String, Long> byCategory = initCounter("TECHNICAL", "BUSINESS", "DESIGN", "MANAGEMENT", "LANGUAGE", "OTHER");
        for (Skill skill : skills) {
            String category = normalizeUpper(skill.getCategory(), "OTHER");
            if (!byCategory.containsKey(category)) category = "OTHER";
            byCategory.put(category, byCategory.get(category) + 1);
        }

        List<Map<String, Object>> topSkills = skills.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getExpertCount() == null ? 0 : b.getExpertCount(),
                        a.getExpertCount() == null ? 0 : a.getExpertCount()))
                .limit(10)
                .map(skill -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("skillId", skill.getId());
                    item.put("skillName", skill.getName());
                    item.put("expertCount", skill.getExpertCount() == null ? 0 : skill.getExpertCount());
                    item.put("projectCount", 0);
                    item.put("averageProficiency", 0);
                    return item;
                })
                .toList();

        List<Map<String, Object>> demandTrend = new ArrayList<>();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("byCategory", byCategory);
        body.put("topSkills", topSkills);
        body.put("demandTrend", demandTrend);
        return ApiResponses.ok(body);
    }

    private static String normalizeUpper(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static Map<String, Long> initCounter(String... keys) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (String key : keys) map.put(key, 0L);
        return map;
    }

    private static Map<String, Object> budgetBucket(String range) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("range", range);
        item.put("count", 0L);
        item.put("totalBudget", BigDecimal.ZERO);
        return item;
    }
}
