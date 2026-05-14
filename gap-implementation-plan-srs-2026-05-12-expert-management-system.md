# 实现方案

> **来源清单**：gap-checklist-srs-2026-05-12-expert-management-system-checked.md
> **目标代码目录**：expert-link-backend/, expert-link-frontend/
> **生成时间**：2026-05-13T15:00:00+08:00
> **统计**：总任务 24 | 🟢 低 2 | 🟡 中 12 | 🔴 高 10

---

## 前置准备

### 依赖安装

| 库 | 版本 | 用途 | 对应任务 |
|----|------|------|----------|
| `larksuite-oapi` (飞书开放平台 SDK) | latest | 飞书消息/机器人/交互式卡片 | T7, T11 |
| `itextpdf` 或 `openpdf` | latest | PDF 报告生成 | T21 |
| `spring-boot-starter-cache` | (内置) | 搜索模板/配置缓存 | T18 |

### 配置项

| 配置键 | 类型 | 默认值 | 说明 | 对应任务 |
|--------|------|--------|------|----------|
| `feishu.app-id` | String | — | 飞书应用 ID | T7 |
| `feishu.app-secret` | String | — | 飞书应用密钥 | T7 |
| `feishu.notification.enabled` | Boolean | false | 飞书通知开关 | T7 |
| `ehr.base-url` | String | — | e-HR 系统基础 URL | T6 |
| `ehr.timeout-seconds` | Integer | 5 | e-HR 接口超时 | T6 |
| `points.ceiling.enabled` | Boolean | false | 积分上限规则默认状态 | T12 |
| `app.file.upload.max-size-mb` | Integer | 50 | 文件上传大小限制 | T5 |

---

## 修改任务

### T1: 扩展 EngagementRequest 实体字段（FORM-017~024, FORM-037, FORM-039）

- **类型**: 🆕 新增
- **来源**: §3.2.1
- **当前状态**: `EngagementRequest` 缺少项目信息、客户代码、产品线、阶段、KDW/迭代标记、活动地点等字段
- **难度**: 🟡 中 — 跨后端实体 + DTO + 前端表单
- **置信度**: 🟢 高 — 现有实体扩展模式清晰

#### 实现方案

**后端**:
1. `EngagementRequest.java` 新增 11 个字段
2. `CreateEngagementDraftRequest.java` 和 `EngagementRequestResponse` 同步新增对应字段
3. 新增 `ActivityLocation.java` 值对象（region → factory → building 三级）

**前端**:
1. `EngagementCreateView.vue` 表单新增对应输入组件
2. 活动地点三级联动组件（见下文）

#### 关键代码骨架

```java
// EngagementRequest.java 新增字段
@Column(name = "activity_name", length = 200)
private String activityName;  // FORM-037: 活动名称

@Column(name = "project_department", length = 200)
private String projectDepartment;  // FORM-017: 项目部门

@Column(name = "project_name", length = 500)
private String projectName;  // FORM-018: 项目名称（逗号分隔多值）

@Column(name = "project_level", length = 50)
private String projectLevel;  // FORM-019: 项目级别

@Column(name = "customer_code", length = 500)
private String customerCode;  // FORM-020: 客户代码

@Column(name = "product_line", length = 500)
private String productLine;  // FORM-021: 产品线

@Column(name = "current_phase", length = 50)
private String currentPhase;  // FORM-022: 当前阶段

@Column(name = "is_kdw")
private Boolean isKdw;  // FORM-023

@Column(name = "is_iterative_product")
private Boolean isIterativeProduct;  // FORM-024

// FORM-039: 活动地点三级
@Column(name = "location_region", length = 50)
private String locationRegion;

@Column(name = "location_factory", length = 100)
private String locationFactory;

@Column(name = "location_building", length = 100)
private String locationBuilding;
```

```typescript
// 前端：地点三级联动数据
export const LOCATION_CASCADE = [
  { value: '潍坊', children: [
    { value: '光电园', children: ['A栋','B栋','C栋'] },
    { value: '科技园', children: ['1号楼','2号楼'] },
  ]},
  { value: '越南', children: [
    { value: '北宁厂区', children: ['A1','A2'] },
  ]},
  // ...
] as const;
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

`POST /api/engagement-requests/create` 传入含新字段的请求体，检查数据库写入和 `GET /api/engagement-requests/{id}` 回读一致。

---

### T2: 扩展 EngagementMode 和 EngagementTaskType 枚举，后端积分分类与项目映射（FORM-002, FORM-003, FORM-008, FORM-009, EVAL-003, EVAL-008, EVAL-009）

- **类型**: 🔄 修改
- **来源**: §3.2.1, §3.2.4
- **当前状态**: 后端 `EngagementMode` 仅 `NAMED/STEWARD_ASSIGN`，无积分大类/项目字段；评估使用 P/T/A 三维而非系数计算
- **难度**: 🔴 高 — 核心领域模型变更，影响状态机、评分逻辑、现有数据迁移
- **置信度**: 🟡 中 — 前端 `engagement-form-config.ts` 提供了完整规则参考，但后端需同时支持新旧评估模型

#### 实现方案

**后端**:
1. `EngagementRequest.java` 新增 `pointsCategory` (积分大类)、`pointsItem` (积分项目)、`contributionScope` (贡献范围) 字段
2. 新增 `computePointsScore(pointsItem, contributionScope, applicantLevel)` 方法替代 `computeSuggestedScore()`
3. 在 `submit()` / `stewardAssign()` 前置校验中增加按 mode 的必填字段校验：
   - 专家调用 → domain 必填、designatedExperts 必填
   - 积分自提 → pointsCategory + pointsItem 必填
4. `EngagementRequestResponse` 增加按 mode 的条件字段返回

**前端**:
1. `engagement-form-config.ts` 已完整实现规则，无需额外修改
2. 后端 DTO 与前端 `EngagementCreateView.vue` 字段对齐

#### 关键代码骨架

```java
// EngagementRequest.java 新增字段
@Column(name = "points_category", length = 50)
private String pointsCategory;  // 积分大类: 评估评审|问题解决|成果贡献|知识沉淀|团队成长

@Column(name = "points_item", length = 100)
private String pointsItem;      // 积分项目: 15种之一

@Column(name = "contribution_scope", length = 50)
private String contributionScope;  // 贡献范围: 跨BG|BG内 等

@Column(name = "applicant_level", length = 20)
private String applicantLevel;  // 申请人自评等级: 卓越|优秀|良好|一般|无贡献

// EngagementRequestService.java 新评分方法
private BigDecimal computePointsScore(String pointsItem, String contributionScope, String applicantLevel) {
    // 从 PointsScoringConfig 查找标准分值和系数
    BigDecimal baseScore = pointsScoringConfig.getBaseScore(pointsItem, contributionScope);
    BigDecimal coefficient = pointsScoringConfig.getCoefficient(pointsItem, applicantLevel);
    return baseScore.multiply(coefficient).setScale(2, RoundingMode.HALF_UP);
}
```

```java
// PointsScoringConfig.java — 将前端 TS 规则落成可配置 Java 服务
@Service
public class PointsScoringConfig {
    // 标准分值映射: items → {scope → score}
    // 等级系数映射: items → [{level, coefficient}]
    // 来源: engagement-form-config.ts BASE_SCORE_BY_ITEM_SCOPE + APPLICANT_LEVELS_BY_ITEM

    public BigDecimal getBaseScore(String pointsItem, String scope) { ... }
    public BigDecimal getCoefficient(String pointsItem, String level) { ... }
}
```

#### 依赖

- 前置任务: T1（新增字段）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

1. 创建专家调用申请，`pointsCategory`/`pointsItem` 应为空，domain 必填
2. 创建积分自提申请，domain 应为空，pointsCategory/pointsItem 必填
3. 结项时验证 new scoring 结果与前端计算一致

---

### T3: 后端级联约束与字段显隐校验（FORM-007）

- **类型**: 🆕 新增
- **来源**: §3.2.1
- **当前状态**: 前端仅有数据映射，后端无约束
- **难度**: 🟢 低 — 纯校验逻辑
- **置信度**: 🟢 高 — 规则明确

#### 实现方案

在 `EngagementRequestService.submit()` 中新增校验：

```java
// submit() 方法内新增
if (request.getMode() == EngagementMode.SELF_CLAIM) {
    if (isBlank(request.getPointsCategory())) {
        throw new IllegalArgumentException("积分自提模式下积分大类为必填");
    }
    if (isBlank(request.getPointsItem())) {
        throw new IllegalArgumentException("必须先选择积分大类再选择积分项目");
    }
}
```

#### 依赖

- 前置任务: T2（积分字段）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

`POST /api/engagement-requests/{id}/submit` — 积分自提模式下不传 pointsItem → 400 错误

---

### T4: 结构化模板字段（FORM-026, FORM-027）

- **类型**: 🔄 修改
- **来源**: §3.2.1
- **当前状态**: `taskDescription` 为单个 TEXT，前端以 placeholder 提示
- **难度**: 🟡 中 — 需改动实体 + DTO + 前端表单
- **置信度**: 🟡 中 — 两种方案可选：(A) 独立字段 (B) JSON 字段；推荐 JSON 以保持灵活性

#### 实现方案

1. `EngagementRequest.java` 新增 `taskTemplateFields` JSON 字段存储结构化内容
2. 前端按 pointsItem 动态渲染 8 个（技术支持）或 4 个（积分自提）独立 textarea
3. 保留 `taskDescription` 作为兼容旧数据的降级文本

```java
// EngagementRequest.java
@Column(name = "task_template_fields", columnDefinition = "JSON")
private String taskTemplateFields;  // JSON: {"关联背景":"...","具体交付物":"...","量化数据":"...","实际影响":"..."}
```

```typescript
// 前端模板字段配置
export const TEMPLATE_FIELDS_BY_ITEM: Record<string, string[]> = {
  '技术支持': ['问题背景','技术方案','解决过程','关键技术点','应用效果','经验总结','可复用性','后续建议'],
  // 积分自提类默认 4 域
};
export const SELF_CLAIM_DEFAULT_FIELDS = ['关联背景','具体交付物','量化数据','实际影响描述'];
```

#### 依赖

- 前置任务: T2（积分项目字段）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

选择"技术支持"积分项目 → 8 个独立文本域渲染 → 填写提交 → 回读验证字段完整性

---

### T5: 增强文件上传系统（FORM-029, FORM-030, FORM-031, FORM-032）

- **类型**: 🆕 新增
- **来源**: §3.2.1
- **当前状态**: 仅有评价附件单文件上传，无 MIME 校验、无预览、无大小限制
- **难度**: 🟡 中 — 涉及文件服务抽取 + MIME 校验 + 预览端点
- **置信度**: 🟢 高 — Spring Boot 文件上传为标准模式

#### 实现方案

**后端**:
1. 新增 `FileStorageService.java` 统一文件存储逻辑
2. 新增 `POST /api/engagement-requests/{id}/attachments` 支持多文件上传
3. 新增 `GET /api/files/{id}/preview` 返回 inline 预览（PDF/图片设置 Content-Type）
4. `application.yml` 新增 `app.file.allowed-extensions` 白名单

```java
// FileStorageService.java
@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_FILES_PER_REQUEST = 5;

    public List<String> storeFiles(Long engagementId, MultipartFile[] files) {
        if (files.length > MAX_FILES_PER_REQUEST) throw new IllegalArgumentException("单次最多上传5个文件");
        for (MultipartFile f : files) {
            if (f.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("单文件不超过50MB: " + f.getOriginalFilename());
            String ext = safeFileExtension(f.getOriginalFilename());
            if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) throw new IllegalArgumentException("不支持的文件格式: " + ext);
        }
        // write to disk + return paths
    }
}
```

**前端**:
1. `EngagementCreateView.vue` 新增 `<el-upload>` 组件，`multiple`、`accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"`、`:limit="5"`
2. `EngagementDetailView.vue` 附件列表 + PDF/图片 inline 预览

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: `app.file.upload.max-size-mb`, `app.file.allowed-extensions`

#### 验证方式

上传 5 个混合格式文件（pdf+jpg+png+doc+docx）→ 检查存储 → 预览 PDF → 上传第 6 个文件 → 应拒绝

---

### T6: e-HR 集成（FORM-013, FORM-014, FORM-015, FORM-016, FORM-017）

- **类型**: 🆕 新增
- **来源**: §3.2.1
- **当前状态**: 无 e-HR 集成代码
- **难度**: 🔴 高 — 外部系统集成，需 e-HR 团队提供 API
- **置信度**: 🔴 低 — 依赖外部 API 规格未确认

#### 前置澄清事项

1. e-HR 系统是否提供 REST API？端点规格如何？
2. 部门树数据的完整层级是多少级？
3. e-HR 是否支持按工号搜索？
4. e-HR 接口响应时间 SLA 是多少？

#### 实现方案

**假设 e-HR 提供标准 REST API**:

```java
// EhrClient.java — Feign 或 RestTemplate 封装
@Service
public class EhrClient {
    private final RestTemplate restTemplate;

    public EhrEmployee getCurrentEmployee(String username) { ... }  // GET /api/employee?username=
    public List<EhrEmployee> searchByJobNumber(String jobNumber) { ... }  // GET /api/employee?jobNumber=
    public List<DepartmentNode> getDepartmentTree() { ... }  // GET /api/departments/tree

    // 超时降级
    public EhrResult<EhrEmployee> getCurrentEmployeeWithFallback(String username) {
        try {
            return EhrResult.success(restTemplate.getForObject(...));
        } catch (TimeoutException e) {
            return EhrResult.timeout();  // 前端展示"信息加载中"，允许手动输入
        }
    }
}
```

```java
// DepartmentService.java — 部门截取 2~3 级
public String truncateDepartment(String fullPath, int maxLevel) {
    // "BG1/BU1/Dept1/Team1" → "BG1/BU1" (2级) 或 "BG1/BU1/Dept1" (3级)
    String[] parts = fullPath.split("/");
    int levels = Math.min(maxLevel, parts.length);
    return String.join("/", Arrays.copyOf(parts, levels));
}
```

**前端**:
- 申请表单加载时自动调用 `GET /api/ehr/current-employee` 填充工号/姓名/部门
- 需求人搜索组件改为支持工号的远程搜索
- 超时时显示"信息加载中，可手动填写"

#### 依赖

- 前置任务: 无（独立集成）
- 依赖引入: 可能需要 `spring-cloud-starter-openfeign`
- 配置项: `ehr.base-url`, `ehr.timeout-seconds`

#### 验证方式

Mock e-HR 端点 → 打开新建申请页 → 字段自动填充 → 模拟超时 → 验证手动输入可用

---

### T7: 飞书通知集成（DISP-004, DISP-010, DISP-012, DISP-013, CONF-004, FLOW-004, FLOW-005, FLOW-006, FLOW-007, LIFE-005）

- **类型**: 🆕 新增
- **来源**: §3.2.2, §3.2.3, §3.2.10, §3.2.9
- **当前状态**: 无飞书集成；消息实体 `Message` 支持站内信但无外部推送
- **难度**: 🔴 高 — 跨切面事件驱动架构 + 外部 API 集成
- **置信度**: 🟡 中 — 飞书 SDK 成熟，但通知模板设计需产品确认

#### 实现方案

采用**事件驱动**模式，业务方法发布事件，通知监听器异步发送飞书消息，失败不影响主流程：

```java
// 通知事件
public record EngagementNotificationEvent(
    Long engagementRequestId,
    NotificationTrigger trigger,  // SUBMITTED|STEWARD_ASSIGNED|STEWARD_REASSIGNED|EXPERT_REJECTED|EVALUATED|SCORE_RELEASED|URGED
    Set<Long> targetUserIds
) {}

// EngagementNotificationListener.java
@Component
public class EngagementNotificationListener {
    private final FeishuMessageService feishuService;
    private final MessageService messageService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEngagementEvent(EngagementNotificationEvent event) {
        try {
            feishuService.sendInteractiveCard(buildCard(event));
        } catch (Exception e) {
            // FLOW-007: 发送失败不影响业务
            log.error("飞书通知发送失败: engagementId={}, trigger={}", event.engagementRequestId(), event.trigger(), e);
            messageService.createSystemMessage(event.targetUserIds(), "通知发送失败: " + e.getMessage());
        }
    }
}
```

```java
// FeishuMessageService.java
@Service
public class FeishuMessageService {
    // 发送交互式卡片（DISP-013: 含"接受""拒绝"按钮）
    public void sendInteractiveCard(FeishuCard card) { ... }

    // 发送普通消息
    public void sendTextMessage(String openId, String content) { ... }
}
```

**通知触发点**（在各 Service 方法中发布事件）:

| 触发器 | 触发位置 | 接收人 |
|--------|----------|--------|
| SUBMITTED | `submit()` | 领域行管 |
| STEWARD_ASSIGNED | `stewardAssign()` | 被指派专家 |
| STEWARD_REASSIGNED | `stewardReassign()` | 申请人 |
| EXPERT_REJECTED | `expertDecision()` | 行管 |
| EVALUATED | `submitEvaluation()` | 行管 |
| SCORE_RELEASED | `releaseScore()` | 专家 |

#### 依赖

- 前置任务: 无（独立集成）
- 依赖引入: `larksuite-oapi`
- 配置项: `feishu.app-id`, `feishu.app-secret`, `feishu.notification.enabled`

#### 验证方式

Mock 飞书回调 → 提交申请 → 验证事件发布 → 验证飞书 API 被调用 → Mock 飞书失败 → 验证业务流程不受影响且消息记录写入

---

### T8: 工作负荷热力图（DISP-005, DISP-006, DISP-007, DISP-008）

- **类型**: 🆕 新增
- **来源**: §3.2.2
- **当前状态**: 无工作量统计相关代码
- **难度**: 🔴 高 — 新页面 + 新后端统计 + ECharts heatmap 渲染
- **置信度**: 🟡 中 — ECharts 已有依赖，数据聚合逻辑需设计

#### 实现方案

**后端**:
1. 新增 `GET /api/stats/workload?domainId=` 返回本领域专家月度任务统计
2. 按月聚合每位专家在 `IN_PROGRESS` 状态下的 EngagementRequest 数量

```java
// StatsController.java 新增
@GetMapping("/workload")
public List<ExpertWorkloadRow> getWorkload(@RequestParam Long domainId, @RequestParam(defaultValue = "3") int months) {
    // 返回本领域最近 N 个月每位专家的任务数量
}
```

**前端**:
1. 新建 `ExpertWorkloadHeatmap.vue` 组件，以弹窗形式嵌入行管工作台
2. 使用 ECharts heatmap 类型渲染，行=专家、列=月份、颜色深浅=任务数
3. 红色标记过载专家（月任务 > 阈值，默认 5）
4. 点击专家行展开当前任务列表

#### 依赖

- 前置任务: 无
- 依赖引入: 无（ECharts 已安装）
- 配置项: 无

#### 验证方式

行管工作台 → 点击"查看负荷图" → 弹窗渲染 heatmap → 验证过载行红色标记 → 点击专家行查看任务列表

---

### T9: 流程超时与计划完成时间（FLOW-002, FLOW-003, DISP-014）

- **类型**: 🔄 修改
- **来源**: §3.2.10, §3.2.2
- **当前状态**: 后端有操作时间记录但无计划完成时间和超时计算
- **难度**: 🟡 中 — 实体字段新增 + 超时判定计算
- **置信度**: 🟢 高 — 规则明确

#### 实现方案

1. `EngagementRequest.java` 新增 `plannedCompletionAt` 字段
2. 各状态转移时设置对应节点的计划完成时间（当前时间 + 1 工作日）
3. 查询时动态计算超时状态

```java
// EngagementRequest.java 新增
@Column(name = "planned_completion_at")
private LocalDateTime plannedCompletionAt;

// EngagementRequestService.java
private static final int TIMEOUT_HOURS_DISPATCH = 24;  // DISP-014

public boolean isNodeTimeout(EngagementRequest er) {
    if (er.getPlannedCompletionAt() == null) return false;
    return LocalDateTime.now().isAfter(er.getPlannedCompletionAt());
}

public boolean isDispatchTimeout(EngagementRequest er) {
    // DISP-014: 指派后 24h 专家未响应
    return er.getStatus() == PENDING_EXPERT_CONFIRM
        && er.getAssignedAt() != null
        && ChronoUnit.HOURS.between(er.getAssignedAt(), LocalDateTime.now()) > TIMEOUT_HOURS_DISPATCH;
}
```

**前端**: 流程节点组件中，超时节点添加红色样式 (`el-steps` 的 `process-status="error"`)

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

1. 创建申请并指派 → 检查 `plannedCompletionAt` 已设置
2. 等 24h（或修改时间为过去）→ 验证行管工作台显示"待确认"标记

---

### T10: 专家审核工作流（LIFE-001, LIFE-002, LIFE-003, LIFE-004, LIFE-013）

- **类型**: 🆕 新增
- **来源**: §3.2.9
- **当前状态**: 专家入库无审核流，导入直接创建记录
- **难度**: 🔴 高 — 全新审批状态机 + 前端审核看板
- **置信度**: 🟡 中 — 可参考 `EngagementRequestStatus` 状态机模式

#### 实现方案

1. 新增 `ExpertOnboardingRequest` 实体（替代直接创建 Expert）
2. 状态流转: `DRAFT → PENDING_DEPT_REVIEW → PENDING_STEWARD_REVIEW → APPROVED → EXPERT_CREATED`
3. 批量导入上限 500 条校验

```java
// ExpertOnboardingRequest.java
@Entity
public class ExpertOnboardingRequest extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private OnboardingStatus status;  // DRAFT, PENDING_DEPT_REVIEW, PENDING_STEWARD_REVIEW, APPROVED, REJECTED, EXPERT_CREATED

    @Column(columnDefinition = "JSON")
    private String expertData;  // 待入库专家数据快照

    private Long submittedBy;
    private Long reviewedByDept;
    private Long reviewedBySteward;
    private String reviewComment;
    private LocalDateTime submittedAt;
    private LocalDateTime deadlineAt;  // 审核截止时间
}
```

```java
// 超时标记（LIFE-003）
public boolean isOverdue(ExpertOnboardingRequest o) {
    return o.getStatus().name().startsWith("PENDING_")
        && o.getDeadlineAt() != null
        && LocalDateTime.now().isAfter(o.getDeadlineAt());
}
```

**前端**: 新建 `ExpertReviewDashboard.vue` 审核看板

#### 依赖

- 前置任务: T7（飞书催办）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

Excel 导入 500 条 → 确认 → 查看审核看板 → 逐级审批 → 通过后 Expert 记录自动创建

---

### T11: 专家出库管理（LIFE-006, LIFE-007, LIFE-008, LIFE-009）

- **类型**: 🆕 新增
- **来源**: §3.2.9
- **当前状态**: 无出库管理
- **难度**: 🟡 中 — 新实体 + 审批流 + 定时任务
- **置信度**: 🟢 高

#### 实现方案

1. 新增 `ExpertDischargeRequest` 实体
2. `@Scheduled` 定时任务检查生效日期到达的出库申请，自动变更 Expert 状态
3. 搜索默认过滤已出库专家

```java
// ExpertDischargeRequest.java
@Entity
public class ExpertDischargeRequest extends BaseEntity {
    @ManyToOne private Expert expert;
    @Enumerated private DischargeReason reason;  // RESIGNED, RETIRED, VOLUNTARY_EXIT, FAILED_EVALUATION, VIOLATION
    @Enumerated private DischargeStatus status;  // PENDING_APPROVAL, APPROVED, REJECTED, EFFECTIVE
    private LocalDate effectiveDate;
    private String remark;
}
```

```java
// ExpertDischargeScheduler.java
@Component
public class ExpertDischargeScheduler {
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨 2 点
    public void processEffectiveDischarges() {
        // LIFE-007: 查询 effectiveDate <= today 的已批准出库申请
        // 将 Expert.availabilityStatus 设为 DISCHARGED
    }
}
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无（Spring Boot 内置 `@Scheduled`）
- 配置项: 无

#### 验证方式

1. 发起出库申请（生效日期=明天）→ 审核通过
2. 第二天 → 检查专家状态已变为 DISCHARGED
3. 专家搜索 → 默认不显示已出库专家

---

### T12: 积分年度上限管理（SCOR-001, SCOR-002, SCOR-003, SCOR-004）

- **类型**: 🆕 新增
- **来源**: §3.2.5
- **当前状态**: 积分系统无上限概念
- **难度**: 🟡 中 — 新实体 + 裁剪逻辑
- **置信度**: 🟢 高 — 业务规则明确

#### 实现方案

1. 新增 `PointsCeilingConfig` 实体存储积分项目上限值
2. 新增年末核算 API `POST /api/points/annual-settlement`
3. 裁剪逻辑：对比专家各类积分项目年度累计与上限，超出部分裁剪

```java
// PointsCeilingConfig.java
@Entity
@Table(name = "points_ceiling_configs")
public class PointsCeilingConfig extends BaseEntity {
    private String pointsItem;  // 积分项目（null=全局默认）
    private BigDecimal ceilingValue;  // 上限值
    private Boolean isActive;
}
```

```java
// PointsAnnualService.java
@Transactional
public AnnualSettlementResult settle(Long userId, int year, boolean enableCeiling) {
    // 汇总该用户当年所有积分
    // 如果 enableCeiling: 逐项目对比上限并裁剪
    // 写入裁剪日志 → PointsCeilingLogEntry
}
```

#### 依赖

- 前置任务: T2（积分项目字段，用于按项目汇总）
- 依赖引入: 无
- 配置项: `points.ceiling.enabled`

#### 验证方式

为专家设置积分上限 → 当年积分超出上限 → 执行年末核算 → 验证裁剪后积分 ≤ 上限 → 检查裁剪日志

---

### T13: 年度任期折算与津贴判定（SCOR-005, SCOR-006, SCOR-007, SCOR-008）

- **类型**: 🆕 新增
- **来源**: §3.2.5
- **当前状态**: Expert 无 `appointmentDate`/`expertLevel` 字段
- **难度**: 🟡 中 — 字段新增 + 计算逻辑 + 手工覆盖
- **置信度**: 🟡 中 — 专家级别（17/18/19 级）的映射需业务确认

#### 实现方案

1. `Expert.java` 新增 `appointmentDate`, `expertLevel` 字段
2. 折算逻辑 + 津贴判定

```java
// Expert.java 新增
@Column(name = "appointment_date")
private LocalDate appointmentDate;

@Column(name = "expert_level")
private Integer expertLevel;  // 17, 18, 19

// AnnualSubsidyService.java
public SubsidyResult calculateSubsidy(Expert expert, BigDecimal annualPoints) {
    long days = ChronoUnit.DAYS.between(expert.getAppointmentDate(), LocalDate.of(LocalDate.now().getYear(), 12, 31));
    BigDecimal threshold;
    if (days >= 360) {
        threshold = getOriginalThreshold(expert.getExpertLevel());  // 17→10, 18→12, 19→15
    } else {
        threshold = getOriginalThreshold(expert.getExpertLevel())
            .multiply(BigDecimal.valueOf(days))
            .divide(BigDecimal.valueOf(360), 2, RoundingMode.HALF_UP);
    }
    boolean eligible = annualPoints.compareTo(threshold) >= 0;
    return new SubsidyResult(threshold, eligible, eligible ? 1 : 0);
}
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

1. 设置专家 appointmentDate=今年 6/1（不足 360 天）→ 验证折算后的门槛
2. 设置 appointmentDate=去年 1/1（≥360 天）→ 验证原门槛
3. 积分不达标 → 津贴=0；达标 → 津贴=1

---

### T14: 知识资产管理（ASSET-005, ASSET-006, ASSET-007, ASSET-008, ASSET-009）

- **类型**: 🆕 新增
- **来源**: §3.2.6
- **当前状态**: 无知识资产模块
- **难度**: 🔴 高 — 全新模块：实体 + CRUD + 审核流 + 权限过滤 + 门户展示
- **置信度**: 🟡 中 — 模式与已有 EngagementRequest 审核流类似

#### 实现方案

1. 新增 `KnowledgeAsset` 实体
2. 审核通过后调用 `PointsService.creditFromEngagement()` 发放积分

```java
// KnowledgeAsset.java
@Entity
public class KnowledgeAsset extends BaseEntity {
    private String title;
    @Enumerated(EnumType.STRING) private AssetType type;  // DOCUMENT, SLIDE, VIDEO, CODE, OTHER
    @Enumerated(EnumType.STRING) private ClassificationLevel classification;  // PUBLIC, INTERNAL, SECRET
    private String filePath;
    private Long uploadedBy;
    @ManyToOne private Domain domain;
    @Enumerated(EnumType.STRING) private AssetStatus status;  // PENDING_REVIEW, APPROVED, REJECTED, FEATURED
    private Boolean isFeatured;
    private BigDecimal pointsAwarded;
}
```

```java
// KnowledgeAssetService.java — 密级过滤
public Page<KnowledgeAsset> listVisible(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId).orElseThrow();
    if (SECRET.equals(asset.getClassification())) {
        // ASSET-006: 仅本领域行管和上传者可见
        return repository.findByClassificationNotSecretOrUploaderOrSteward(userId, pageable);
    }
    return repository.findByClassificationPublicOrInternal(pageable);
}
```

#### 依赖

- 前置任务: T5（文件上传）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

1. 上传知识资产（密级=秘密）→ 同领域行管可见，其他用户不可见
2. 行管审核通过 → 积分发放 + 全域展示
3. 行管标记"典型案例" → 门户置顶

---

### T15: 项目案例自动生成（ASSET-010, ASSET-011）

- **类型**: 🆕 新增
- **来源**: §3.2.6
- **当前状态**: `releaseScore()` 仅发放积分
- **难度**: 🟢 低 — 结项 hook + 单表
- **置信度**: 🟢 高

#### 实现方案

1. 新增 `ProjectCase` 实体
2. `releaseScore()` 方法末尾自动创建案例记录

```java
// ProjectCase.java
@Entity
public class ProjectCase extends BaseEntity {
    @ManyToOne private EngagementRequest engagementRequest;
    private String projectName;
    private String expertRole;
    private String outputSummary;
    private Boolean isFeatured;  // ASSET-011
}

// EngagementRequestService.releaseScore() 末尾
ProjectCase projectCase = ProjectCase.builder()
    .engagementRequest(er)
    .projectName(er.getProjectName())
    .expertRole(buildExpertRoleSummary(er))
    .outputSummary(er.getTaskDescription())
    .isFeatured(false)
    .build();
projectCaseRepository.save(projectCase);
```

#### 依赖

- 前置任务: T1（projectName 字段）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

结项一个任务 → 查询 `project_cases` 表 → 确认自动生成记录 → 行管标记典型案例 → 验证专家画像突出展示

---

### T16: 专家画像自助维护与审核（ASSET-002, ASSET-004, PERM-007）

- **类型**: 🆕 新增
- **来源**: §3.2.6, §3.2.8
- **当前状态**: 标签通过管理员操作 skill 关联；画像变更无审核
- **难度**: 🟡 中 — 审核流 + 脱敏规则增强
- **置信度**: 🟢 高

#### 实现方案

1. 专家可新增/删除自己的 Skill 关联（ASSET-002）
2. 画像变更提交审核，行管审批后生效（ASSET-004）
3. 脱敏规则增加"指派状态"维度（PERM-007）

```java
// ExpertPrivacyService.java — PERM-007 增强
public boolean shouldMask(User viewer, Expert expert, EngagementRequest er) {
    if (ELEVATED.contains(viewer.getHighestRole())) return false;
    // 如果该用户是该次调用的申请人且专家已被指派，展示真实姓名
    if (er != null && er.getApplicant().getId().equals(viewer.getId())
        && er.getAssignedExperts().contains(expert)) {
        return false;
    }
    return true;
}
```

```java
// ExpertProfileChangeRequest.java — ASSET-004
@Entity
public class ExpertProfileChangeRequest extends BaseEntity {
    @ManyToOne private Expert expert;
    @Column(columnDefinition = "JSON") private String changes;  // 变更快照
    @Enumerated private ChangeRequestStatus status;  // PENDING, APPROVED, REJECTED
    private Long reviewedBy;
    private String reviewNote;
}
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

1. 专家登录 → 编辑技能标签 → 提交 → 行管审核 → 通过后标签生效
2. 申请人查看指派后详情页 → 专家姓名不脱敏

---

### T17: 多维度专家组合检索（SRCH-005）

- **类型**: 🔄 修改
- **来源**: §3.2.7
- **当前状态**: 仅支持 keyword/domain/skill 三种维度
- **难度**: 🟡 中 — 扩展 Repository 查询方法
- **置信度**: 🟢 高 — Spring Data JPA Specification 模式成熟

#### 实现方案

使用 JPA Specification 构建动态组合查询：

```java
// ExpertRepository.java 新增
Page<Expert> findAll(Specification<Expert> spec, Pageable pageable);

// ExpertSpecification.java
public class ExpertSpecification {
    public static Specification<Expert> build(ExpertSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            criteria.getJobNumber().ifPresent(jn -> predicates.add(cb.equal(root.get("jobNumber"), jn)));   // 工号精确
            criteria.getNameKeyword().ifPresent(kw -> predicates.add(  // 姓名模糊 + 拼音
                cb.or(cb.like(root.get("name"), "%" + kw + "%"),
                      cb.like(root.get("pinyinName"), "%" + kw + "%"))));
            criteria.getDepartment().ifPresent(dept -> predicates.add(cb.like(root.get("department"), "%" + dept + "%")));
            criteria.getExperienceTypes().ifPresent(types -> predicates.add(root.get("experienceType").in(types)));
            criteria.getRegions().ifPresent(regions -> predicates.add(root.get("region").in(regions)));
            criteria.getHighestDegree().ifPresent(deg -> predicates.add(cb.equal(root.get("highestDegree"), deg)));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

**注意**: Expert 实体目前缺少 `jobNumber`、`pinyinName`、`department`、`experienceType`、`region` 字段，需在 T17 中同时新增。

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

`GET /api/experts/search?keyword=zhang&degree=博士&region=潍坊` → 验证组合过滤结果

---

### T18: 搜索条件模板（SRCH-007）

- **类型**: 🆕 新增
- **来源**: §3.2.7
- **当前状态**: 无搜索模板功能
- **难度**: 🟢 低 — 单实体 CRUD
- **置信度**: 🟢 高

#### 实现方案

```java
// SearchTemplate.java
@Entity
public class SearchTemplate extends BaseEntity {
    private Long userId;
    private String name;  // 模板名称
    @Column(columnDefinition = "JSON")
    private String criteria;  // JSON 序列化的搜索条件
}

// SearchTemplateController.java
@RestController
@RequestMapping("/api/search-templates")
public class SearchTemplateController {
    @GetMapping  // 列出当前用户的模板
    @PostMapping  // 保存模板
    @DeleteMapping("/{id}")  // 删除模板
}
```

#### 依赖

- 前置任务: T17（搜索条件结构定义）
- 依赖引入: 无
- 配置项: 无

#### 验证方式

设置搜索条件 → 保存模板 → 刷新页面 → 加载模板 → 条件自动填充

---

### T19: 统计看板增强 — 权限过滤 + 下钻 + 导出（STAT-003, STAT-004, STAT-005）

- **类型**: 🆕 新增
- **来源**: §3.2.11
- **当前状态**: `/stats/dashboard` 无权限过滤和导出
- **难度**: 🔴 高 — 权限 scope 注入 + 图表下钻 + Excel/PDF 导出
- **置信度**: 🟡 中 — 权限注入模式参考现有行管过滤逻辑

#### 实现方案

```java
// StatsService.java
public DashboardStats getDashboardWithScope(Long userId, StatsTimeRange range) {
    User user = userRepository.findById(userId).orElseThrow();
    return switch (user.getHighestRole()) {
        case SUPER_ADMIN -> buildStats(null, range);          // 全量
        case DOMAIN_STEWARD -> buildStats(user.getStewardDomainIds(), range);  // 本领域
        case DEPT_ADMIN -> buildStats(user.getDepartment(), range);  // 本部门
        default -> throw new AccessDeniedException("无权限");
    };
}
```

```java
// StatsController.java 新增
@GetMapping("/drill-down")  // STAT-004: 图表下钻
public Page<?> drillDown(@RequestParam String chartType, @RequestParam String dimension, Pageable pageable) { ... }

@GetMapping("/export")  // STAT-005: Excel/PDF 导出
public ResponseEntity<Resource> exportReport(@RequestParam String format) { ... }
```

#### 依赖

- 前置任务: 无
- 依赖引入: `itextpdf` 或 `openpdf`（PDF 导出）
- 配置项: 无

#### 验证方式

1. 部门管理员登录 → 看板只显示本部门数据
2. 点击图表 → 弹出明细列表
3. 点击"导出 Excel" → 下载 `.xlsx` 文件

---

### T20: 积分排名（STAT-006, STAT-007, STAT-009）

- **类型**: 🆕 新增
- **来源**: §3.2.11
- **当前状态**: `User.pointsBalance` 存在但无排名查询
- **难度**: 🟡 中 — SQL 排名查询 + 历史快照 + 前端图表
- **置信度**: 🟢 高

#### 实现方案

1. 新增排名查询端点
2. 新增每月排名快照表用于趋势图
3. 前端新增 `PointsRankingView.vue`

```java
// PointsController.java 新增
@GetMapping("/ranking")
public Page<PointsRankRow> getRanking(@RequestParam(required = false) Long domainId, Pageable pageable) {
    // 按 pointsBalance DESC 排序
    // 可选按领域过滤（通过 Expert.primaryDomain）
}
```

```java
// PointsRankSnapshot.java — 月度快照
@Entity
public class PointsRankSnapshot extends BaseEntity {
    private int year;
    private int month;
    private Long userId;
    private BigDecimal totalPoints;
    private int rank;
}
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无（ECharts 已有）
- 配置项: 无

#### 验证方式

1. `GET /api/points/ranking` → 验证按积分降序
2. `GET /api/points/ranking?domainId=1` → 验证领域过滤
3. 查看排名趋势图 → 验证 6 个月折线

---

### T21: 履职报告生成（STAT-010, STAT-011, STAT-012）

- **类型**: 🆕 新增
- **来源**: §3.2.11
- **当前状态**: 无报告生成
- **难度**: 🔴 高 — PDF 生成 + 模板系统
- **置信度**: 🟡 中 — PDF 库选择需权衡（openpdf 轻量但中文复杂，itext 企业级）

#### 实现方案

```java
// PerformanceReportService.java
public byte[] generateReport(Long engagementRequestId) {
    EngagementRequest er = engagementRequestRepository.findById(engagementRequestId).orElseThrow();
    // 聚合: 专家信息 + 参与时长 + 贡献度评分 + 甲方评价
    // 使用 openpdf + 模板引擎渲染 PDF
    return pdfRenderer.render(buildReportData(er));
}
```

```java
// ReportTemplate.java
@Entity
public class ReportTemplate extends BaseEntity {
    private String name;
    private Long ownerId;
    @Column(columnDefinition = "TEXT") private String templateContent;  // 模板定义（如 HTML）
    private Boolean isDefault;
}
```

#### 依赖

- 前置任务: T2（评分数据）
- 依赖引入: `com.github.librepdf:openpdf` 或 `com.itextpdf:itext7-core`
- 配置项: 无

#### 验证方式

结项后 → 点击"生成履职报告" → 下载 PDF → 验证含专家信息、参与时长、评分、评价

---

### T22: 表单路由参数传递（FORM-035）

- **类型**: 🆕 新增
- **来源**: §3.2.1
- **当前状态**: 前端路由无 query 参数预填充
- **难度**: 🟢 低 — 前端路由改动
- **置信度**: 🟢 高

#### 实现方案

前端路由跳转传递参数，创建表单页解析 query 参数自动填充：

```typescript
// router/index.ts — 无需改动

// ExpertLibrary.vue / ExpertDetail.vue — 跳转时带参数
router.push({ name: 'EngagementNew', query: { domain: domainId, experts: expertIds.join(',') } });

// EngagementCreateView.vue — onMounted 读取 query
const route = useRoute();
onMounted(() => {
  if (route.query.domain) form.domainId = Number(route.query.domain);
  if (route.query.experts) form.designatedExpertIds = route.query.experts.split(',').map(Number);
});
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

专家库页 → 点击"申请调用"按钮 → 跳转到新建申请页 → 验证领域和专家已自动填充

---

### T23: 改派时排除已拒绝专家（CONF-008）

- **类型**: 🔄 修改
- **来源**: §3.2.3
- **当前状态**: `stewardReassign()` 全量替换专家
- **难度**: 🟢 低 — 单方法改动
- **置信度**: 🟢 高

#### 实现方案

```java
// EngagementRequestService.stewardReassign() 中
// 从 assignmentExpertDecisions JSON 解析出已拒绝的 expertId 列表
Set<Long> rejectedExpertIds = parseRejectedExpertIds(er.getAssignmentExpertDecisions());
// 候选专家筛选时排除
List<Expert> candidates = expertRepository.findByDomainId(er.getDomain().getId())
    .stream()
    .filter(e -> !rejectedExpertIds.contains(e.getId()))
    .collect(Collectors.toList());
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

1. 指派专家 A → A 拒绝 → 行管改派 → 搜索候选人 → A 不在列表中
2. 确认新指派后，A 仍不在专家列表中

---

### T24: 前端 UI 增强（FORM-004, FORM-012, EVAL-010, SRCH-008, SRCH-009, FORM-038）

- **类型**: 🔄 修改（部分 🔧）
- **来源**: §3.2.1, §3.2.4, §3.2.7
- **当前状态**: ❓ 无法判定 — 需实际运行确认
- **难度**: 🟡 中 — 多项小改动
- **置信度**: 🟡 中 — 需实际运行验证

#### 实现方案

按项处理：

1. **FORM-004/012**: 检查 `EngagementCreateView.vue` 中切换申请类别/积分项目时的表单状态管理逻辑 — 确保隐藏字段值保留
2. **EVAL-010**: 在 `ReleaseScoreRequest` 中新增 `approved` (Boolean) 字段区分认可/不认可
3. **SRCH-008**: `ExpertList.vue` 搜索区新增"重置"按钮
4. **SRCH-009**: `ExpertList.vue` 新增视图切换 toggle（list/card）
5. **FORM-038**: 在 `submit()` 校验中增加"提交日期 < 开始时间至少 1 天"

```java
// FORM-038 补充校验
if (!request.getStartAt().minusDays(1).isAfter(LocalDateTime.now())) {
    throw new IllegalArgumentException("提交日期应早于活动开始时间至少一天");
}
```

```java
// ReleaseScoreRequest.java — EVAL-010
@Data
public class ReleaseScoreRequest {
    private BigDecimal score;      // 分数
    private Boolean approved;      // 积分自提类：认可=true, 不认可=false
    private String releaseNote;    // 评分理由
    private LocalDateTime completedAt;
}
```

#### 依赖

- 前置任务: 无
- 依赖引入: 无
- 配置项: 无

#### 验证方式

在浏览器中逐项验证上述 6 个功能点。

---

## 执行顺序

```
Phase 1: Foundation
  T1 (字段扩展) ──→ T2 (积分映射+评分) ──→ T3 (级联约束) ──→ T4 (模板字段)
                     ↘
                       T5 (文件上传) ──→ T14 (知识资产)
  T9 (超时) ──→ 独立
  T15 (案例生成) ──→ 依赖 T1

Phase 2: Integration
  T6 (e-HR) ──→ 独立（需外部 API）
  T7 (飞书通知) ──→ 独立（需外部 API）

Phase 3: New Modules
  T8 (负荷热力图) ──→ 独立
  T10 (审核工作流) ──→ 依赖 T7
  T11 (出库管理) ──→ 独立
  T12 (积分上限) ──→ 依赖 T2
  T13 (津贴判定) ──→ 独立
  T16 (画像自助) ──→ 独立

Phase 4: Search & Analytics
  T17 (多维检索) ──→ T18 (搜索模板)
  T19 (统计增强) ──→ 独立
  T20 (积分排名) ──→ 独立
  T21 (履职报告) ──→ 依赖 T2

Phase 5: Polish
  T22 (路由参数) ──→ 独立
  T23 (排除已拒绝) ──→ 独立
  T24 (UI增强) ──→ 独立
```

### 建议实施批次

**第一批（核心数据模型）**: T1 → T2 → T3 → T4
> 完成后方可推进后续功能，预计 5-7 人天

**第二批（独立基础设施）**: T5, T9, T15, T23
> 可与第一批并行，预计 3-4 人天

**第三批（外部集成）**: T6, T7
> 需等待外部 API 确认，预计 5-10 人天（取决于外部依赖就绪时间）

**第四批（新模块）**: T8, T10, T11, T12, T13, T14, T16
> 依赖前两批完成，预计 15-20 人天

**第五批（搜索与分析）**: T17, T18, T19, T20, T21
> 部分依赖 T2，预计 10-15 人天

**第六批（收尾）**: T22, T24
> 预计 2-3 人天

**总计估算**: 40-60 人天（不含外部依赖等待时间）

---

## 验收清单

- [ ] T1: FORM-017~024,037,039 — 新字段在 API 请求/响应中正确往返
- [ ] T2: EVAL-003,008,009 — 系数评分结果与前端 `engagement-form-config.ts` 一致
- [ ] T3: FORM-007 — 积分自提模式下不选积分项目应拒绝提交
- [ ] T4: FORM-026,027 — 技术支持 8 个独立字段渲染和存储正确
- [ ] T5: FORM-029~032 — 5 文件上传、50MB 限制、PDF/图片预览
- [ ] T6: FORM-013~016 — e-HR 自动填充 + 超时降级
- [ ] T7: 飞书通知 — 7 个节点通知发送，失败不影响业务
- [ ] T8: DISP-005~008 — 工作负荷热力图 + 过载红色标记
- [ ] T9: FLOW-002,003; DISP-014 — 超时红色标记 + 24h 待确认提示
- [ ] T10: LIFE-001~004,013 — 专家审核看板 + 500 条导入上限
- [ ] T11: LIFE-006~009 — 出库审批 + 定时生效 + 搜索过滤
- [ ] T12: SCOR-001~004 — 年底核算 + 上限裁剪 + 日志
- [ ] T13: SCOR-005~008 — 任期折算 + 津贴判定 + 手工覆盖
- [ ] T14: ASSET-005~009 — 知识资产 CRUD + 密级过滤 + 审核 + 典型案例置顶
- [ ] T15: ASSET-010,011 — 结项自动生成案例 + 典型案例标记
- [ ] T16: ASSET-002,004; PERM-007 — 标签自助 + 画像审核 + 指派后脱敏豁免
- [ ] T17: SRCH-005 — 工号/拼音/部门/经验类型/地域/学历组合检索
- [ ] T18: SRCH-007 — 搜索条件保存/加载/删除
- [ ] T19: STAT-003~005 — 权限过滤看板 + 图表下钻 + Excel/PDF 导出
- [ ] T20: STAT-006,007,009 — 积分排名列表 + 月度趋势图 + 领域筛选
- [ ] T21: STAT-010~012 — 履职报告生成 PDF + 模板保存
- [ ] T22: FORM-035 — URL 参数自动填充表单
- [ ] T23: CONF-008 — 改派候选人列表排除已拒绝专家
- [ ] T24: 6 项 UI 增强在浏览器中验证通过
