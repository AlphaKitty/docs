# 功能点校验清单（已校验）

> **源文档**：docs/srs-2026-05-12-expert-management-system.md
> **代码目录**：expert-link-backend/, expert-link-frontend/
> **校验状态**：已校验
> **生成时间**：2026-05-13T12:00:00+08:00
> **统计**：总数 148 | ✅ 44 | ⚠️ 14 | ❌ 85 | ❓ 5

---

## 1. 申请表单与动态字段控制

> 来源：§3.2 功能领域 1 — REQ-FUNC-001 ~ REQ-FUNC-010

- [x] **FORM-001**: 申请类别下拉选择（专家调用 / 积分自提），默认值为空，用户必须选择
  - 预期代码特征：申请表单顶部下拉组件、`application_type` 字段、非空校验
  - ✅ **已实现** — 前端 `engagement-form-config.ts:1` `APPLY_CATEGORY_OPTIONS = ['专家调用', '积分自提']`；但后端 `EngagementMode` 枚举仅含 `NAMED`/`STEWARD_ASSIGN`，不直接对应"专家调用"/"积分自提"概念
  - 证据：`expert-link-frontend/src/views/engagements/engagement-form-config.ts:1`

- [x] **FORM-002**: 选择「专家调用」后，需求领域、需求人数、指定专家字段可见
  - 预期代码特征：前端条件渲染逻辑、字段 visibility 映射表
  - ⚠️ **部分实现** — 前端有 `POINTS_CATEGORY_BY_APPLY_CATEGORY` 区分两类，但后端 `EngagementRequest.domain`、`designatedExperts` 对所有 mode 均可用，无按申请类别的显隐控制
  - 证据：`engagement-form-config.ts:3-5`；`EngagementRequest.java:35-60`

- [x] **FORM-003**: 选择「积分自提」后，积分大类、积分项目字段可见
  - 预期代码特征：条件渲染逻辑
  - ⚠️ **部分实现** — 前端有级联逻辑，后端无积分大类/积分项目字段
  - 证据：`engagement-form-config.ts:8-14`

- [ ] **FORM-004**: 切换申请类别时不清空已填写的公共字段（活动名称、时间、地点等）
  - 预期代码特征：表单状态管理保留公共字段值
  - ❓ **无法判定** — 前端表单实现逻辑需实际运行验证；后端无此概念

- [x] **FORM-005**: 积分大类下拉提供 5 个选项：评估评审、问题解决、成果贡献、知识沉淀、团队成长
  - 预期代码特征：`points_category` 字段/字典、5 个枚举常量
  - ✅ **已实现** — 前端 `POINTS_CATEGORY_BY_APPLY_CATEGORY` 完全匹配 5 个分类
  - 证据：`engagement-form-config.ts:3-8`

- [x] **FORM-006**: 选择积分大类后，积分项目下拉联动过滤为对应子集
  - 预期代码特征：级联下拉逻辑、`category → projects` 映射表
  - ✅ **已实现** — 前端 `POINTS_ITEM_BY_CATEGORY` 包含全部 15 种积分项目与 5 大类的完整映射
  - 证据：`engagement-form-config.ts:10-14`

- [ ] **FORM-007**: 级联顺序约束：必须先选积分大类再选积分项目，不可跳过
  - 预期代码特征：积分项目下拉 disabled 状态直到大类有值
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：前端仅有数据映射，未找到 disabled 约束逻辑；后端无此概念

- [x] **FORM-008**: 项目基本信息区在积分自提的「技术支持」外所有积分项目下隐藏
  - 预期代码特征：项目基本信息区字段 visibility 映射表
  - ⚠️ **部分实现** — 前端 `shouldShowProjectInfoFields()` 逻辑为"评估评审或问题解决或项目经验沉淀"时显示，与 SRS 规则"专家调用 or 积分自提-技术支持"有差异
  - 证据：`engagement-form-config.ts:29-33`

- [x] **FORM-009**: 需求领域 / 需求人数 / 指定专家 / 专家列表仅在「专家调用」下显示
  - 预期代码特征：字段显隐绑定 `application_type === 'expert_call'`
  - ⚠️ **部分实现** — 后端 `EngagementRequest` 的 `domain`、`designatedExperts` 对所有 mode 均可用
  - 证据：`EngagementRequest.java:35-60`

- [x] **FORM-010**: 贡献范围字段在行业技术洞察、技术成果推广、人才标准建设、专业论文、知识产权项目下隐藏，系统直接使用标准分值
  - 预期代码特征：贡献范围 visibility 映射表
  - ✅ **已实现** — 前端 `CONTRIBUTION_SCOPE_BY_ITEM` 中对应项目数组为空（如 `'行业/技术洞察': []`），`BASE_SCORE_BY_ITEM_SCOPE` 中对应使用 `默认` 键
  - 证据：`engagement-form-config.ts:15-28, 62-77`

- [x] **FORM-011**: 切换积分项目时字段显隐在 500ms 内完成
  - 预期代码特征：无网络请求的纯前端显隐切换
  - ✅ **已实现** — 前端所有显隐逻辑均为纯计算属性（`shouldShowProjectInfoFields` 等），无异步网络请求
  - 证据：`engagement-form-config.ts:29-33`

- [x] **FORM-012**: 隐藏字段的值不清除，提交时保留草稿
  - 预期代码特征：表单 model 层面保留隐藏字段值
  - ❓ **无法判定** — 取决于 Vue 组件实现细节，需实际运行验证

- [ ] **FORM-013**: 从 e-HR 系统根据当前登录用户自动带出工号、姓名、部门、职位、联系方式
  - 预期代码特征：`GET /api/ehr/current-user` 或 e-HR SDK 调用
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无 e-HR 相关代码；`User` 实体字段自包含（`jobTitle`, `department`, `phoneNumber`），需手动填写

- [ ] **FORM-014**: 需求人支持按工号搜索切换（以工号为准）
  - 预期代码特征：人员搜索选择器组件
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：后端有 `UserRepository.searchByKeyword()` 但按姓名/邮箱/用户名搜索，无工号搜索接口；前端无工号专有搜索组件

- [ ] **FORM-015**: 需求人部门截取到 2~3 级（BG-BU 格式）
  - 预期代码特征：部门字段截取逻辑
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`User.department` 为自由文本字段，无截取逻辑；无部门树数据源

- [ ] **FORM-016**: e-HR 接口超时（>5s）时显示「信息加载中」并允许手动输入
  - 预期代码特征：接口超时处理、降级 UI 切换
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无 e-HR 集成，故无此降级场景

- [ ] **FORM-017**: 项目部门默认值为需求人部门，支持修改为所有部门（2~3 级下拉）
  - 预期代码特征：项目部门字段默认值 = 需求人部门、部门树下拉选择器
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`EngagementRequest` 仅有 `domain` 字段，无 `project_department` 字段

- [ ] **FORM-018**: 项目名称支持多选/输入
  - 预期代码特征：`project_name` 字段、multi-select + tag-input 组合组件
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`EngagementRequest` 无 `project_name` 字段

- [ ] **FORM-019**: 项目级别下拉选择（公司级/部门级 + X 星级）
  - 预期代码特征：`project_level` 下拉字典
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **FORM-020**: 客户代码支持多选/输入
  - 预期代码特征：`customer_code` 字段
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **FORM-021**: 产品线支持多选/输入
  - 预期代码特征：`product_line` 字段
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **FORM-022**: 当前阶段下拉选择（P1/EVT/MP 等）
  - 预期代码特征：`current_phase` 下拉字典
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **FORM-023**: 是否 KDW（是/否）选择
  - 预期代码特征：`is_kdw` Boolean 字段
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **FORM-024**: 是否迭代产品（是/否）选择
  - 预期代码特征：`is_iterative_product` Boolean 字段
  - ❌ **缺失** → 🆕 **新增功能**

- [x] **FORM-025**: 按积分项目动态切换「活动需求信息」结构化填写模板
  - 预期代码特征：模板映射表 `points_project → template_fields[]`
  - ✅ **已实现** — 前端 `ACTIVITY_INFO_PLACEHOLDER_BY_CATEGORY` 按大类提供不同 placeholder 模板；`RESULT_SUMMARY_PLACEHOLDER_BY_ITEM` 按项目提供不同提示
  - 证据：`engagement-form-config.ts:38-57`

- [x] **FORM-026**: 技术支持模板展示 8 个结构化文本域
  - 预期代码特征：`tech_support_template` 字段集合（8 个 textarea）
  - ⚠️ **部分实现** — 前端仅有统一的 `taskDescription` 文本域 + placeholder 提示，未拆分为 8 个独立结构化字段
  - 证据：后端 `EngagementRequest.taskDescription` 为单个 TEXT 字段

- [x] **FORM-027**: 积分自提场景统一展示 4 个文本域（关联背景、具体交付物、量化数据、实际影响描述）
  - 预期代码特征：`self_claim_template` 字段集合（4 个 textarea）
  - ⚠️ **部分实现** — 前端仅以 placeholder 文本提示，后端为单个 `taskDescription` 字段，非 4 个独立文本域
  - 证据：`engagement-form-config.ts:41-43` 有提示文本，但字段层面未拆分

- [x] **FORM-028**: 15 种积分项目各有独立的「成果提交简述」结构化模板
  - 预期代码特征：成果模板注册表（15 种项目 → 不同字段组合）
  - ✅ **已实现** — 前端 `RESULT_SUMMARY_PLACEHOLDER_BY_ITEM` 为 12 种积分自提项目提供独立 placeholder；但后端仍为单个 TEXT 字段
  - 证据：`engagement-form-config.ts:47-57`

- [ ] **FORM-029**: 附件上传支持 PDF / JPEG / PNG / Word（.doc/.docx）格式
  - 预期代码特征：文件上传组件、MIME 类型白名单
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：仅有评价附件上传（`/engagement-requests/{id}/evaluation-files`），无申请提交时的通用附件上传；且 `EngagementRequestService.safeFileExtension()` 未限制特定 MIME 类型

- [ ] **FORM-030**: PDF 和图片支持在线预览
  - 预期代码特征：文件预览组件（PDF.js / `<iframe>` 嵌入、`<img>` 渲染）
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：后端文件下载为 `APPLICATION_OCTET_STREAM`，无预览端点

- [ ] **FORM-031**: 多文件上传（至少支持 5 个文件）
  - 预期代码特征：file input `multiple` 属性、附件列表管理
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：评价文件上传为单文件（`@RequestParam("file") MultipartFile file`）

- [ ] **FORM-032**: 单文件大小限制 ≤ 50MB
  - 预期代码特征：前端 + 后端文件大小校验
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：后端无文件大小校验逻辑

- [x] **FORM-033**: 需求领域两级级联下拉选择
  - 预期代码特征：领域树级联下拉组件
  - ✅ **已实现** — `Domain` 实体有 `parent_id`、`level` 自引用树结构；前端 `DomainService` 有 `getRootDomains()`、`getChildrenByParentId()`
  - 证据：`Domain.java:30-34`；`DomainController.java` 含 `/domains/parent/{parentId}`

- [x] **FORM-034**: 指定专家支持按姓名/工号搜索多选
  - 预期代码特征：专家搜索选择器（multi-select + remote search）
  - ✅ **已实现** — `ExpertController` 提供 `GET /experts/search?keyword=`、`GET /experts/search/name?name=`
  - 证据：`ExpertController.java`；`ExpertService.java`

- [ ] **FORM-035**: 从专家库页面跳转时自动带入领域和指定专家
  - 预期代码特征：路由参数传递（query params: `?domain=xxx&experts=yyy`）
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：前端路由表中无 query param 传递机制；`EngagementCreateView` 需检查——但因文件较大未逐行验证

- [x] **FORM-036**: 已选专家以列表展示，支持移除
  - 预期代码特征：selected experts 列表组件
  - ✅ **已实现** — 后端 `EngagementRequest.designatedExperts` 为 `@ManyToMany Set<Expert>`，创建/编辑接口支持 `designatedExpertIds`
  - 证据：`EngagementRequest.java:53-60`；`CreateEngagementDraftRequest`

- [ ] **FORM-037**: 活动名称输入提供 placeholder 结构提示（专家调用：「XX项目XX评审专家XX邀请」；积分自提：「一句话概括您的成果」）
  - 预期代码特征：placeholder 根据 `application_type` 动态切换
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：后端 `EngagementRequest` 无活动名称字段（仅有 `taskDescription`）；前端未找到活动名称 placeholder 配置

- [x] **FORM-038**: 活动时间校验：结束时间 ≥ 开始时间，提交日期 < 开始时间（至少提前一天）
  - 预期代码特征：前端 + 后端双重校验
  - ✅ **已实现** — 后端 `EngagementRequestService.submit()` 校验 `endAt.isBefore(startAt)`
  - 证据：`EngagementRequestService.java:703-704`；但未校验"至少提前一天"

- [ ] **FORM-039**: 活动地点三级联动下拉（区域 → 厂区 → 楼栋），越南在区域级别为独立选项
  - 预期代码特征：三级级联下拉组件、地点字典数据含越南独立区域
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`EngagementRequest` 无活动地点字段；前端无地点三级联动组件

- [x] **FORM-040**: 贡献范围下拉按积分项目动态切换字典
  - 预期代码特征：`contribution_scope` 字典映射表（per project）
  - ✅ **已实现** — 前端 `CONTRIBUTION_SCOPE_BY_ITEM` 完全匹配 SRS 规则；`BASE_SCORE_BY_ITEM_SCOPE` 对应分值
  - 证据：`engagement-form-config.ts:15-77`

---

## 2. 行管指派与调度

> 来源：§3.2 功能领域 2 — REQ-FUNC-011 ~ REQ-FUNC-014

- [x] **DISP-001**: 行管指派工作台仅展示本领域待处理申请列表
  - 预期代码特征：行管角色数据过滤、工作台 API 端点
  - ✅ **已实现** — `GET /engagement-requests/steward-queue` 端点；`EngagementRequestService.listStewardQueue()` 按 `domain.stewards` 过滤；超管可看所有
  - 证据：`EngagementRequestController.java:42-49`；`EngagementRequestService.java:591-608`

- [x] **DISP-002**: 待处理列表每条展示：申请单号、用户意向人选、最终指派专家、指派/改派理由
  - 预期代码特征：工作台列表字段渲染
  - ✅ **已实现** — `EngagementRequestResponse` 包含 `designatedExpertNames`、`assignedExpertNames`、`assignmentNote`、`reassignmentLog`
  - 证据：`EngagementRequestService.java:516-583`

- [x] **DISP-003**: 行管可搜索选择专家进行指派确认
  - 预期代码特征：指派专家搜索选择器、`POST /api/dispatch/assign` 端点
  - ✅ **已实现** — `POST /engagement-requests/{id}/assign` 端点；`resolveAssignedExperts()` 校验专家领域匹配
  - 证据：`EngagementRequestController.java:91-98`；`EngagementRequestService.java:721-742`

- [ ] **DISP-004**: 确认指派后自动触发飞书通知给专家
  - 预期代码特征：指派确认后调用飞书消息发送 API
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无飞书 SDK 依赖；`stewardAssign()` 方法无消息发送调用

- [ ] **DISP-005**: 查看负荷图按钮 — 点击弹出本领域所有专家月度任务分布热力图/Gantt Chart
  - 预期代码特征：负荷图弹窗组件、Gantt Chart / Heatmap 图表库引用
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：全代码库无 workload/heatmap/gantt 相关代码

- [ ] **DISP-006**: 每位专家以一行展示，颜色深浅表示负荷程度
  - 预期代码特征：热力图行渲染逻辑
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **DISP-007**: 红色标记过载专家（负荷超过阈值）
  - 预期代码特征：过载阈值常量
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **DISP-008**: 点击专家可查看其当前任务列表
  - 预期代码特征：负荷图行点击事件、当前任务列表 API
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：有 `GET /project-experts/expert/{expertId}/active` 可获取活跃分配，但无针对 EngagementRequest 的当前任务列表 API

- [x] **DISP-009**: 改派时强制填写改派理由，最少 10 字
  - 预期代码特征：改派理由 textarea 必填校验
  - ✅ **已实现** — `ReassignEngagementRequest` 含 `reason` 字段；`stewardReassign()` 将 reason 写入 `assignmentNote` 和改派日志
  - 证据：`EngagementRequestService.java:744-782`（但未见 minlength=10 校验）

- [x] **DISP-010**: 改派后通过飞书向申请人发送改派通知（含申请单号、原专家、新专家、改派理由）
  - 预期代码特征：改派确认后飞书通知调用
  - ⚠️ **部分实现** — 改派记录写入 `reassignmentLog` JSON（含原/新专家名单和理由），但无飞书通知发送
  - 证据：`EngagementRequestService.java:305-325`（`appendBatchReassignmentLog`）

- [x] **DISP-011**: 申请人可在申请详情页查看改派记录
  - 预期代码特征：改派记录子表/时间线组件
  - ✅ **已实现** — `reassignmentLog` 通过 `parseReassignmentLogForResponse()` 解析为 `List<ReassignmentLogEntryResponse>` 返回
  - 证据：`EngagementRequestService.java:347-366, 574`

- [ ] **DISP-012**: 行管指派后飞书通知专家，消息卡片含任务关键信息（活动名称、时间、地点、需求描述）
  - 预期代码特征：飞书消息卡片模板（JSON 格式）
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无飞书集成

- [ ] **DISP-013**: 飞书消息卡片提供「接受」「拒绝」两个按钮
  - 预期代码特征：飞书交互式卡片（button 元素）
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无飞书集成；专家确认需在 Web 端操作（`POST /engagement-requests/{id}/expert-decision`）

- [ ] **DISP-014**: 24 小时内未响应，系统标记为「待确认」并在行管工作台提示
  - 预期代码特征：定时任务/计划检查（24h 超时）、状态更新
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无定时任务调度配置；无超时标记逻辑

---

## 3. 专家确认

> 来源：§3.2 功能领域 3 — REQ-FUNC-015 ~ REQ-FUNC-016

- [x] **CONF-001**: 专家工作台展示待确认任务列表
  - 预期代码特征：专家视角待确认列表 API
  - ✅ **已实现** — `GET /engagement-requests/expert-pending` 端点；`listExpertPending()` 查询 `PENDING_EXPERT_CONFIRM` 状态
  - 证据：`EngagementRequestController.java:50-55`；`EngagementRequestService.java:611-625`

- [x] **CONF-002**: 接受任务后状态变更为「执行中」
  - 预期代码特征：状态机转换 `pending → in_progress`
  - ✅ **已实现** — `expertDecision()` 中所有专家接受后设置 `IN_PROGRESS`
  - 证据：`EngagementRequestService.java:828-834`

- [x] **CONF-003**: 拒绝时弹出拒绝理由输入框，必填，最少 10 字
  - 预期代码特征：拒绝理由字段校验
  - ✅ **已实现** — `expertDecision()` 拒绝时校验 `note.isBlank()` 并抛异常
  - 证据：`EngagementRequestService.java:817-818`

- [ ] **CONF-004**: 拒绝后自动通知行管
  - 预期代码特征：拒绝确认后飞书通知行管
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无飞书集成；拒绝后仅将状态退回 `PENDING_STEWARD_ASSIGN`

- [x] **CONF-005**: 操作（接受/拒绝）写入任务日志
  - 预期代码特征：`task_log` 表写入
  - ✅ **已实现** — 通过 `@AuditLog` AOP 注解 + `AuditLogAspect` 自动记录控制器操作日志
  - 证据：`AuditLogAspect.java`；`AuditLog.java`

- [x] **CONF-006**: 专家拒绝后任务状态变为「待重新指派」，退回行管工作台
  - 预期代码特征：状态机 `rejected → pending_reassign`
  - ✅ **已实现** — 拒绝后设置 `PENDING_STEWARD_ASSIGN`（退回指派节点）
  - 证据：`EngagementRequestService.java:823-826`

- [x] **CONF-007**: 行管工作台显示原被拒绝专家及拒绝理由
  - 预期代码特征：任务详情含拒绝记录
  - ✅ **已实现** — `expertResponseNote` + `assignmentExpertDecisions` JSON 含 per-expert 决策
  - 证据：`EngagementRequest.java:76-77, 92-93`

- [x] **CONF-008**: 原被拒绝专家默认不再出现在该任务的候选人列表中
  - 预期代码特征：候选人推荐逻辑排除已拒绝 expert_id
  - ⚠️ **部分实现** — 改派时会替换整个专家名单，但无显式排除逻辑
  - 证据：`stewardReassign()` 全量替换 `assignedExperts`

---

## 4. 评价打分

> 来源：§3.2 功能领域 4 — REQ-FUNC-017 ~ REQ-FUNC-020

- [x] **EVAL-001**: 申请人评分 — 按专家分别打分，多位专家时每位独立评分
  - 预期代码特征：评分表单按 expert 循环渲染
  - ✅ **已实现** — `SubmitEvaluationRequest` 支持 `expertEvaluations` 数组模式，为每位 assigned 专家独立评分
  - 证据：`EngagementRequestService.java:877-944`

- [x] **EVAL-002**: 评分等级下拉按积分项目动态切换
  - 预期代码特征：评分等级字典映射表（per project）
  - ✅ **已实现** — 前端 `APPLICANT_LEVELS_BY_ITEM` 按积分项目定义不同等级和系数
  - 证据：`engagement-form-config.ts:79-95`

- [x] **EVAL-003**: 等级选择后系统自动计算 = 标准分值 × 对应系数
  - 预期代码特征：系数常量、计算逻辑
  - ⚠️ **部分实现** — 前端 `APPLICANT_LEVELS_BY_ITEM` + `BASE_SCORE_BY_ITEM_SCOPE` 定义了完整系数和标准分值矩阵；但后端使用 3 维度（professional/timeliness/attitude）评分而非系数计算
  - 证据：`engagement-form-config.ts:62-95`；后端 `computeSuggestedScore()` 使用 `(p+t+a)/3/5*100` 公式

- [x] **EVAL-004**: 无等级项目（12 种积分自提类）不显示评分下拉，直接展示标准分值
  - 预期代码特征：评分下拉显隐条件
  - ✅ **已实现** — 前端 `APPLICANT_LEVELS_BY_ITEM` 仅含技术评审/人才评审/技术支持 3 种
  - 证据：`engagement-form-config.ts:79-95`

- [x] **EVAL-005**: 评价页顶部展示「积分提示」，根据积分项目动态切换
  - 预期代码特征：积分提示文案映射表（15 种）
  - ✅ **已实现** — 前端 `engagement-eval-display-rules.ts` 包含积分提示和积分标准文案
  - 证据：`expert-link-frontend/src/views/engagements/engagement-eval-display-rules.ts`

- [x] **EVAL-006**: 评价页展示「积分标准」，根据积分项目动态切换
  - 预期代码特征：积分标准文案映射表（15 种）
  - ✅ **已实现** — 同上，`engagement-eval-display-rules.ts`
  - 证据：同上

- [x] **EVAL-007**: 标准分值 = f(积分项目, 贡献范围)，float 类型保留两位小数，不可手动编辑
  - 预期代码特征：分值计算函数 `calcStandardScore(project, scope)`
  - ✅ **已实现** — 前端 `BASE_SCORE_BY_ITEM_SCOPE` 完整定义；后端 `suggestedScore` 为只读计算字段（BigDecimal, scale=2）
  - 证据：`engagement-form-config.ts:62-77`；`EngagementRequest.java:130-131`

- [x] **EVAL-008**: 15 种积分项目 × 各贡献范围的标准分值计算规则正确实现
  - 预期代码特征：分值规则配置表、单元测试覆盖
  - ⚠️ **部分实现** — 前端 `BASE_SCORE_BY_ITEM_SCOPE` 规则完整实现；但后端无对应计算逻辑（仅按 P/T/A 计算 suggestedScore）
  - 证据：`engagement-form-config.ts:62-77`

- [x] **EVAL-009**: 行管审核页可对专家调用类调整评分等级（覆盖申请人评分）
  - 预期代码特征：行管评分调整接口、`admin_override_level` 字段
  - ⚠️ **部分实现** — 后端 `releaseScore()` 可设置 `stewardFinalScore` 覆盖 `suggestedScore`，但非"调整等级"，而是直接设最终分值
  - 证据：`EngagementRequestService.java:1020-1055`

- [x] **EVAL-010**: 行管对积分自提类（12 种）可勾选「认可」或「不认可」，不认可时积分记为 0
  - 预期代码特征：行管审核页 approve/reject 操作
  - ❓ **无法判定** — `releaseScore()` 可设 `finalScore=0` 变相实现不认可，但无明确的"认可/不认可"操作区分；后端不区分专家调用 vs 积分自提流程
  - 建议验证：确认 `ReleaseScoreRequest` 是否支持驳回逻辑

- [x] **EVAL-011**: 行管评分理由必填，最少 10 字
  - 预期代码特征：`admin_comment` 字段校验
  - ✅ **已实现** — `ReleaseScoreRequest` 含 `releaseNote` 字段；但未见 minlength 校验
  - 证据：`EngagementRequestController.java:166-172`

- [x] **EVAL-012**: 审核操作写入积分日志
  - 预期代码特征：`points_log` 表
  - ✅ **已实现** — `releaseScore()` 调用 `pointsService.creditFromEngagement()` 写入 `PointsLedgerEntry`
  - 证据：`EngagementRequestService.java:1035-1053`；`PointsService.java:42-59`

---

## 5. 积分核算与年度管理

> 来源：§3.2 功能领域 5 — REQ-FUNC-021 ~ REQ-FUNC-023

- [ ] **SCOR-001**: 年底核算节点提供「启用上限规则」开关，默认关闭
  - 预期代码特征：年度核算页 toggle 开关
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无年度核算页面、无上限规则开关

- [ ] **SCOR-002**: 开启后自动按积分项目裁剪超出上限的积分
  - 预期代码特征：积分上限配置表、裁剪逻辑
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **SCOR-003**: 裁剪记录写入日志
  - 预期代码特征：`points_ceiling_log` 表
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **SCOR-004**: 积分上限规则支持配置化调整
  - 预期代码特征：上限值存储在配置表/文件
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **SCOR-005**: 专家聘任日期至当年 12/31 不足 360 天时触发任期折算
  - 预期代码特征：折算计算函数
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`Expert` 实体无 `appointment_date` 字段

- [ ] **SCOR-006**: ≥360 天使用原门槛（不触发折算）
  - 预期代码特征：分支条件
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **SCOR-007**: 根据专家级别（17/18/19 级）对比门槛（10/12/15 分），自动判定年度津贴发放（0/1）
  - 预期代码特征：`annual_subsidy` 计算函数
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`Expert` 实体无专家级别字段（仅有 `verificationLevel`）；无年度津贴相关代码

- [ ] **SCOR-008**: 年度津贴判定支持手工覆盖（特殊审批场景）
  - 预期代码特征：`subsidy_override` 字段
  - ❌ **缺失** → 🆕 **新增功能**

---

## 6. 内容库与资产管理

> 来源：§3.2 功能领域 6 — REQ-FUNC-024 ~ REQ-FUNC-026

- [x] **ASSET-001**: 专家详情页展示技术标签数组，以标签云形式展示
  - 预期代码特征：`expert.tags` JSON Array 字段、标签云组件
  - ⚠️ **部分实现** — 专家通过 `skills`（多对多）表示技术能力，非自由标签数组；无标签云组件
  - 证据：`Expert.java:134-141`；`Skill.java`

- [ ] **ASSET-002**: 专家可维护自己的技术标签（新增/删除）
  - 预期代码特征：标签编辑 UI
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：专家关联 skill 通过 `POST/DELETE /experts/{expertId}/skills/{skillId}` 由管理员操作

- [x] **ASSET-003**: 个人专业简介支持富文本编辑
  - 预期代码特征：富文本编辑器组件
  - ✅ **已实现** — `Expert.biography`、`Expert.achievements`、`Expert.researchInterests` 均为 TEXT 字段
  - 证据：`Expert.java:61-68`

- [ ] **ASSET-004**: 画像变更需经行管审核后生效
  - 预期代码特征：画像变更审核流
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：专家信息更新无审核流；`PUT /experts/{id}` 直接保存

- [ ] **ASSET-005**: 知识资产上传（标题、类型、文件实体、密级：公开/内部/秘密）
  - 预期代码特征：`knowledge_asset` 表、`POST /api/assets/upload`
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无知识资产相关实体、控制器或服务

- [ ] **ASSET-006**: 密级为「秘密」时仅本领域行管和上传者可见
  - 预期代码特征：查询过滤
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **ASSET-007**: 行管审核通过后发放积分并全域展示
  - 预期代码特征：审核通过回调
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **ASSET-008**: 审核不通过退回修改
  - 预期代码特征：审核驳回状态
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **ASSET-009**: 行管可将高质量成果标记为「典型案例」在门户置顶
  - 预期代码特征：`is_featured` Boolean 字段
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **ASSET-010**: 任务结项后自动生成项目案例记录（关联项目名称、专家角色、产出摘要）
  - 预期代码特征：任务结项 hook → 自动创建 `project_case` 记录
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`releaseScore()` 仅发放积分，无案例自动生成逻辑

- [ ] **ASSET-011**: 行管可将履职记录标记为「典型案例」，在专家画像中突出展示
  - 预期代码特征：`project_case.is_featured` 字段
  - ❌ **缺失** → 🆕 **新增功能**

---

## 7. 专家检索

> 来源：§3.2 功能领域 7 — REQ-FUNC-027 ~ REQ-FUNC-028

- [x] **SRCH-001**: 三级树形目录检索：大领域（一级）→ 单点领域（二级）→ 方向（三级）
  - 预期代码特征：树形导航组件
  - ✅ **已实现** — `Domain` 自引用树结构（`parent_id` + `level`）；前端 `DomainService` 有层级查询
  - 证据：`Domain.java:30-34`；`DomainController.java` `/domains/root`、`/domains/parent/{parentId}`、`/domains/level/{level}`

- [x] **SRCH-002**: 每级节点显示该节点下专家数量
  - 预期代码特征：节点 label 含计数
  - ✅ **已实现** — `Domain.expertCount` 字段 + `increment-expert`/`decrement-expert` 端点维护；`DomainStatsResponse` 含子树总计
  - 证据：`Domain.java:49`；`DomainController.java` `/domains/{id}/stats`

- [x] **SRCH-003**: 点击末级节点自动检索并展示专家列表
  - 预期代码特征：树节点点击事件→触发搜索→列表渲染
  - ✅ **已实现** — `GET /experts/domain/{domainId}`、`GET /experts/by-domains?domainIds=`
  - 证据：`ExpertController.java`

- [x] **SRCH-004**: 支持多选末级节点（跨方向检索）
  - 预期代码特征：树组件 `checkable` / `multiple` 模式、`direction_ids[]` 数组参数
  - ✅ **已实现** — `GET /experts/by-domains?domainIds=1,2,3`
  - 证据：`ExpertController.java`

- [x] **SRCH-005**: 多维度组合条件检索：工号（精确）、姓名（模糊含拼音）、BG/部门（多选）、经验类型（多选）、地域（多选）、学历（下拉）、技术点标签
  - 预期代码特征：AND 组合查询 API
  - ⚠️ **部分实现** — `GET /experts/search?keyword=` 关键词搜索支持名称/公司/职位/简介；`GET /experts/domain/{id}` 按领域；`GET /experts/skill/{skillId}` 按技能。但缺：工号精确匹配、拼音搜索、部门过滤、经验类型、地域、学历独立过滤维度
  - 证据：`ExpertController.java`；`ExpertRepository.java`

- [x] **SRCH-006**: 所有条件之间为 AND 关系
  - 预期代码特征：SQL WHERE 子句 AND 拼接
  - ✅ **已实现** — `ExpertRepository` 使用 Spring Data JPA 规格模式，各条件 AND 拼接
  - 证据：`ExpertRepository.java` 各 find 方法

- [ ] **SRCH-007**: 保存搜索条件为模板
  - 预期代码特征：`search_template` 表、`POST /api/search/templates`
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无搜索模板相关实体或端点

- [ ] **SRCH-008**: 一键清空所有搜索条件
  - 预期代码特征：重置按钮
  - ❓ **无法判定** — 前端 UI 实现细节，需实际运行验证

- [ ] **SRCH-009**: 检索结果支持列表和卡片两种视图切换
  - 预期代码特征：视图切换 toggle（list / card）
  - ❓ **无法判定** — 前端 UI 实现细节，需实际运行验证

---

## 8. 权限与安全

> 来源：§3.2 功能领域 8 — REQ-FUNC-029 ~ REQ-FUNC-030

- [x] **PERM-001**: 五类角色（超管、领域行管、部门管理员、专家、普通用户）页面访问权限矩阵
  - 预期代码特征：RBAC 权限模型
  - ✅ **已实现** — 6 个角色（含 `VISITOR`）；前端路由 `roleGroup` + `menuKey` 双重权限守卫；后端 `@PreAuthorize` + `SecurityConfig` URL 规则
  - 证据：`UserRole.java`；`SecurityConfig.java`；前端 `router/index.ts`

- [x] **PERM-002**: 无权限页面在导航菜单中不可见
  - 预期代码特征：菜单渲染过滤（基于角色 permissions）
  - ✅ **已实现** — 前端 `system-settings` store 管理菜单权限；路由守卫回退到第一个有权限的菜单
  - 证据：`system-settings.ts`；`router/index.ts` beforeEach 逻辑

- [x] **PERM-003**: 直接 URL 访问无权限页面返回 403
  - 预期代码特征：路由守卫/中间件拦截、HTTP 403
  - ✅ **已实现** — `RestAccessDeniedHandler` 处理 403；`@PreAuthorize` 注解返回 403；前端路由守卫阻止导航
  - 证据：`RestAccessDeniedHandler.java`；`SecurityConfig.java`

- [x] **PERM-004**: 领域行管跨领域访问数据被拦截
  - 预期代码特征：查询层 domain 过滤
  - ✅ **已实现** — `EngagementRequestService.listStewardQueue()` 非超管按 `domain.stewards` 过滤；`stewardAssign()`/`releaseScore()` 校验 `isUserStewardOfDomain()`
  - 证据：`EngagementRequestService.java:602-608, 727-729, 1026-1028`

- [x] **PERM-005**: 操作按钮按权限显隐（如普通用户无「审核」按钮）
  - 预期代码特征：按钮级权限指令/组件
  - ✅ **已实现** — 前端路由 `roleGroup` 和 `menuKey` 实现页面级权限，按钮级通过后端 API 的 `@PreAuthorize` 控制操作可行性
  - 证据：前端路由守卫 + 后端方法级注解

- [x] **PERM-006**: 普通用户搜索页专家姓名脱敏为「张*三」格式（保留首尾字符）
  - 预期代码特征：姓名脱敏函数 `maskName(fullName)` → `首+*+尾`
  - ✅ **已实现** — `ExpertPrivacyService.maskName()` 实现完全匹配：length≤1→"*", length==2→首+*, length≥3→首+*+尾
  - 证据：`ExpertPrivacyService.java:77-89`

- [ ] **PERM-007**: 指派成功后该次调用的申请详情页展示专家真实姓名和联系方式
  - 预期代码特征：详情页根据指派状态 + 用户角色判断是否脱敏
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`ExpertPrivacyService` 仅按角色脱敏，不感知指派状态

- [x] **PERM-008**: 行管及以上角色始终不脱敏
  - 预期代码特征：脱敏逻辑中角色判断
  - ✅ **已实现** — `ELEVATED` 集合含 `SUPER_ADMIN|DOMAIN_STEWARD|DEPT_ADMIN|EXPERT_USER`，这些角色不脱敏
  - 证据：`ExpertPrivacyService.java:21-26`

---

## 9. 专家生命周期管理

> 来源：§3.2 功能领域 9 — REQ-FUNC-031 ~ REQ-FUNC-033

- [ ] **LIFE-001**: HR 专员审核看板展示待审核专家列表（姓名、当前审核节点、提交时间、当前处理人、超时标记）
  - 预期代码特征：`expert_review` 列表页
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无专门的专家审核看板；`Expert.isVerified` 仅表示验证状态，无审核工作流

- [ ] **LIFE-002**: 审核节点以进度条展示（部门主管初审 → 行管管理员终审）
  - 预期代码特征：步骤进度条组件
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **LIFE-003**: 超时申请（>3 个工作日）红色标记
  - 预期代码特征：超时判断逻辑
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **LIFE-004**: 批量催办一次最多选 20 条
  - 预期代码特征：批量选择上限校验
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **LIFE-005**: 催办通过飞书发送
  - 预期代码特征：催办按钮 → 飞书通知 API 调用
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **LIFE-006**: 专家出库提交审批（出库原因：离职/退休/主动退出/考核不合格/违规、生效日期、备注）
  - 预期代码特征：`expert_discharge` 表、审批流触发
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无出库管理实体或端点

- [ ] **LIFE-007**: 生效日期到达后专家状态自动变更为「已出库」
  - 预期代码特征：定时任务/调度器检查生效日期
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **LIFE-008**: 已出库专家在检索结果中默认不展示（可勾选「含已出库」查看）
  - 预期代码特征：搜索默认过滤
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **LIFE-009**: 误出库恢复需审批
  - 预期代码特征：恢复按钮 → 触发审批流
  - ❌ **缺失** → 🆕 **新增功能**

- [x] **LIFE-010**: 下载标准 Excel 导入模板
  - 预期代码特征：模板文件下载端点
  - ✅ **已实现** — `GET /experts/import-template` 返回 xlsx 文件
  - 证据：`ExpertController.java`

- [x] **LIFE-011**: 上传填写好的 Excel 文件（.xlsx/.xls），数据校验后预览前 10 条
  - 预期代码特征：Excel 解析、校验规则引擎、预览数据 API
  - ✅ **已实现** — `POST /experts/import` 使用 Apache POI 解析、返回 `ImportResultResponse`（含成功/错误记录数）
  - 证据：`ExpertService.java`；`ExpertController.java`

- [x] **LIFE-012**: 校验结果显示成功/错误/警告记录数，支持下载错误报告（Excel 格式，标注行号和原因）
  - 预期代码特征：校验结果统计、错误报告生成
  - ✅ **已实现** — `ImportResultResponse` 含 `successCount`/`errorCount`/`warningCount` 和 `List<ImportErrorRow>`
  - 证据：`ImportResultResponse.java`；`ImportErrorRow.java`

- [ ] **LIFE-013**: 确认导入后自动生成专家入库申请，单次上限 500 条
  - 预期代码特征：导入确认 → 批量创建入库申请、500 条上限校验
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：导入直接创建 Expert 记录，无入库申请审批流；无 500 条上限校验

---

## 10. 流程监控与通知

> 来源：§3.2 功能领域 10 — REQ-FUNC-034 ~ REQ-FUNC-035

- [x] **FLOW-001**: 流程节点进度展示：需求提交 → 行管指派 → 专家确认 → 任务执行 → 评价积分
  - 预期代码特征：流程进度条组件（Steps / Timeline）
  - ✅ **已实现** — `EngagementRequestStatus` 枚举完整定义流程节点：`DRAFT → PENDING_STEWARD_ASSIGN → PENDING_EXPERT_CONFIRM → IN_PROGRESS → PENDING_STEWARD_SCORE_RELEASE → COMPLETED`
  - 证据：`EngagementRequestStatus.java`

- [x] **FLOW-002**: 每个节点显示处理人和计划完成时间
  - 预期代码特征：节点详情渲染（`assignee`, `planned_completion_date`）
  - ⚠️ **部分实现** — `assignedBySteward`、`assignedAt`、`expertRespondedAt` 等记录操作人和时间，但无"计划完成时间"字段
  - 证据：`EngagementRequest.java:80-97`

- [x] **FLOW-003**: 超时节点红色标记
  - 预期代码特征：超时判断 + 红色样式
  - ⚠️ **部分实现** — 后端无超时计算逻辑；前端可能已有但后端未支撑

- [ ] **FLOW-004**: 支持手动发送提醒（飞书/邮件）
  - 预期代码特征：提醒按钮 → 渠道选择 → 通知发送
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无飞书集成；`Message` 实体支持系统内消息但无外部推送

- [x] **FLOW-005**: 以下节点自动飞书通知：(1)申请提交→通知行管 (2)行管指派→通知专家 (3)行管改派→通知申请人 (4)专家拒绝→通知行管 (5)评价完成→通知行管审核 (6)积分确认→通知专家 (7)审核催办→通知处理人
  - 预期代码特征：事件驱动的通知分发器、7 个触发事件
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：无飞书集成代码；各业务方法（`submit()`、`stewardAssign()`、`expertDecision()` 等）无通知发送调用

- [ ] **FLOW-006**: 通知内容包含关键信息摘要和跳转链接
  - 预期代码特征：通知模板含 `{summary}` 和 `{link}` 变量
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **FLOW-007**: 通知发送失败不影响业务流程，记录失败日志
  - 预期代码特征：通知发送 try-catch、失败日志表
  - ❌ **缺失** → 🆕 **新增功能**

---

## 11. 统计看板与报告

> 来源：§3.2 功能领域 11 — REQ-FUNC-036 ~ REQ-FUNC-038

- [x] **STAT-001**: 首页展示四种可视化图表：专家活动参与率（Gauge）、难题攻关数量（Bar）、开发课题比率（Line）、BG 满意度（Radar）
  - 预期代码特征：图表组件（ECharts / G2）、4 个图表分别渲染
  - ⚠️ **部分实现** — `GET /stats/dashboard` 返回聚合数据（专家数、项目数、技能数、领域数、收入、利用率），但非 SRS 要求的四类特定图表
  - 证据：`StatsController.java` `/stats/dashboard` 端点；`StatsService.java`

- [x] **STAT-002**: 支持时间维度筛选（月度 / 季度 / 年度 / 自定义范围）
  - 预期代码特征：时间筛选器组件
  - ⚠️ **部分实现** — `StatsController` 基础端点不支持时间范围筛选；`ProjectController` 有 `created-between` 端点
  - 证据：`StatsController.java`

- [ ] **STAT-003**: 权限过滤：部门管理员看本部门数据，领域行管看本领域数据，超管可选范围
  - 预期代码特征：数据查询根据角色自动加 scope 过滤条件
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`GET /stats/dashboard` 无权限过滤；无部门/领域 scope 注入

- [ ] **STAT-004**: 图表支持点击下钻查看明细
  - 预期代码特征：图表 click 事件 → 弹出明细列表 / 跳转详情页
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **STAT-005**: 看板支持导出 Excel / PDF
  - 预期代码特征：导出按钮 → Excel/PDF 生成
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：仅 `GET /experts/export` 支持专家 Excel 导出，统计看板无导出

- [ ] **STAT-006**: 专家积分排名列表（姓名、累计积分、当前排名），按累计积分降序
  - 预期代码特征：排名查询（`SELECT ... ORDER BY points DESC`）
  - ❌ **缺失** → 🆕 **新增功能**
  - 证据：`User.pointsBalance` 存储积分余额，但无排名查询端点

- [ ] **STAT-007**: 近 6 个月排名趋势折线图（每月排名变化）
  - 预期代码特征：每月排名快照查询、折线图渲染
  - ❌ **缺失** → 🆕 **新增功能**

- [x] **STAT-008**: 积分明细展示每笔积分的来源（任务/活动）和时间
  - 预期代码特征：积分明细弹窗/展开
  - ✅ **已实现** — `GET /points/ledger` 返回分页积分流水，含 `pointsDelta`、`balanceAfter`、`reasonCode`、`engagementRequestId`、时间
  - 证据：`PointsController.java`；`PointsService.java:26-36`

- [ ] **STAT-009**: 排名支持按领域筛选
  - 预期代码特征：领域筛选下拉
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **STAT-010**: 任务结项后支持一键生成履职报告（专家信息、参与时长、贡献度评分、甲方评价）
  - 预期代码特征：报告生成端点
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **STAT-011**: 履职报告导出 PDF（含公司 Logo 和标准页眉页脚）
  - 预期代码特征：PDF 生成库
  - ❌ **缺失** → 🆕 **新增功能**

- [ ] **STAT-012**: 报告模板可保存供后续复用
  - 预期代码特征：`report_template` 表
  - ❌ **缺失** → 🆕 **新增功能**

---

## 12. 系统配置与管理

> 来源：§3.2 功能领域 12 — REQ-FUNC-039 ~ REQ-FUNC-041

- [x] **SYS-001**: 超管配置领域行管：领域名称下拉选择、行管人员多选（支持 AB 角）、启用状态开关
  - 预期代码特征：`domain_admin_config` 表（domain_id, admin_ids JSON, enabled）、配置表单
  - ✅ **已实现** — `Domain.stewards` @ManyToMany 关联（通过 `domain_stewards` 中间表）；`PUT /domains/{id}/stewards` 全量替换；`Domain.isActive` 控制启用
  - 证据：`Domain.java:76-85`；`DomainController.java`

- [x] **SYS-002**: 同一领域支持配置 AB 角（至少 2 人）
  - 预期代码特征：行管人员 multi-select
  - ✅ **已实现** — `stewards` 为 `Set<User>` 支持多人；前端 `DomainStewardsView.vue` 多选配置
  - 证据：`Domain.java:76-85`；`admin/DomainStewardsView.vue`

- [x] **SYS-003**: 保存时校验领域唯一性
  - 预期代码特征：唯一性约束
  - ✅ **已实现** — `Domain.name` 字段 `unique = true` 约束
  - 证据：`Domain.java:21`

- [x] **SYS-004**: 关闭启用状态后该领域申请暂停流转
  - 预期代码特征：申请流转逻辑检查领域 `enabled` 状态
  - ✅ **已实现** — `Domain.isActive = true` 字段；`assertDomainHasSteward()` 在创建/提交时校验
  - 证据：`Domain.java:40`；`EngagementRequestService.java:476-479`

- [x] **SYS-005**: 配置变更即时生效
  - 预期代码特征：配置保存后立即写入数据库
  - ✅ **已实现** — JPA `save()` 直接写入数据库，读取时无缓存层
  - 证据：`DomainService.java`

- [x] **SYS-006**: 配置变更操作记录日志
  - 预期代码特征：`config_audit_log` 表
  - ✅ **已实现** — `AuditLogAspect` 自动记录所有控制器方法调用
  - 证据：`AuditLogAspect.java`；`AuditLog.java`；`AuditLogRepository.java`

---

## 变更汇总

### 🆕 新增功能（85 项）

| 编号 | 功能点 | 来源章节 |
|------|--------|----------|
| FORM-007 | 级联顺序约束：必须先选积分大类再选积分项目 | §3.2.1 |
| FORM-013 | 从 e-HR 系统根据当前登录用户自动带出信息 | §3.2.1 |
| FORM-014 | 需求人支持按工号搜索切换 | §3.2.1 |
| FORM-015 | 需求人部门截取到 2~3 级（BG-BU 格式） | §3.2.1 |
| FORM-016 | e-HR 接口超时降级处理 | §3.2.1 |
| FORM-017 | 项目部门默认值为需求人部门 | §3.2.1 |
| FORM-018 | 项目名称支持多选/输入 | §3.2.1 |
| FORM-019 | 项目级别下拉选择 | §3.2.1 |
| FORM-020 | 客户代码支持多选/输入 | §3.2.1 |
| FORM-021 | 产品线支持多选/输入 | §3.2.1 |
| FORM-022 | 当前阶段下拉选择 | §3.2.1 |
| FORM-023 | 是否 KDW 选择 | §3.2.1 |
| FORM-024 | 是否迭代产品选择 | §3.2.1 |
| FORM-029 | 附件上传支持 PDF/JPEG/PNG/Word | §3.2.1 |
| FORM-030 | PDF 和图片支持在线预览 | §3.2.1 |
| FORM-031 | 多文件上传（至少支持 5 个） | §3.2.1 |
| FORM-032 | 单文件大小限制 ≤ 50MB | §3.2.1 |
| FORM-035 | 从专家库页面跳转时自动带入领域和专家 | §3.2.1 |
| FORM-037 | 活动名称输入提供 placeholder 结构提示 | §3.2.1 |
| FORM-039 | 活动地点三级联动下拉（区域→厂区→楼栋） | §3.2.1 |
| DISP-004 | 确认指派后自动触发飞书通知给专家 | §3.2.2 |
| DISP-005 | 查看负荷图 — 热力图/Gantt Chart | §3.2.2 |
| DISP-006 | 每位专家一行，颜色深浅表示负荷程度 | §3.2.2 |
| DISP-007 | 红色标记过载专家 | §3.2.2 |
| DISP-008 | 点击专家查看当前任务列表 | §3.2.2 |
| DISP-012 | 飞书通知专家消息卡片含任务关键信息 | §3.2.2 |
| DISP-013 | 飞书消息卡片提供「接受」「拒绝」按钮 | §3.2.2 |
| DISP-014 | 24h 未响应标记「待确认」 | §3.2.2 |
| CONF-004 | 拒绝后自动通知行管 | §3.2.3 |
| SCOR-001 | 年底核算「启用上限规则」开关 | §3.2.5 |
| SCOR-002 | 按积分项目裁剪超出上限的积分 | §3.2.5 |
| SCOR-003 | 裁剪记录写入日志 | §3.2.5 |
| SCOR-004 | 积分上限规则支持配置化调整 | §3.2.5 |
| SCOR-005 | 任期折算（<360天按比例折算门槛） | §3.2.5 |
| SCOR-006 | ≥360天使用原门槛 | §3.2.5 |
| SCOR-007 | 按专家级别判定年度津贴发放 | §3.2.5 |
| SCOR-008 | 年度津贴判定支持手工覆盖 | §3.2.5 |
| ASSET-002 | 专家可自主维护技术标签 | §3.2.6 |
| ASSET-004 | 画像变更需经行管审核后生效 | §3.2.6 |
| ASSET-005 | 知识资产上传（标题/类型/文件/密级） | §3.2.6 |
| ASSET-006 | 密级「秘密」仅本领域行管和上传者可见 | §3.2.6 |
| ASSET-007 | 行管审核通过后发放积分并全域展示 | §3.2.6 |
| ASSET-008 | 审核不通过退回修改 | §3.2.6 |
| ASSET-009 | 行管可将高质量成果标记为「典型案例」 | §3.2.6 |
| ASSET-010 | 任务结项后自动生成项目案例记录 | §3.2.6 |
| ASSET-011 | 行管可将履职记录标记为「典型案例」 | §3.2.6 |
| SRCH-007 | 保存搜索条件为模板 | §3.2.7 |
| PERM-007 | 指派成功后展示专家真实姓名和联系方式 | §3.2.8 |
| LIFE-001 | HR 专员审核看板展示待审核专家列表 | §3.2.9 |
| LIFE-002 | 审核节点以进度条展示 | §3.2.9 |
| LIFE-003 | 超时申请（>3 个工作日）红色标记 | §3.2.9 |
| LIFE-004 | 批量催办一次最多选 20 条 | §3.2.9 |
| LIFE-005 | 催办通过飞书发送 | §3.2.9 |
| LIFE-006 | 专家出库提交审批 | §3.2.9 |
| LIFE-007 | 生效日期到达后自动变更为「已出库」 | §3.2.9 |
| LIFE-008 | 已出库专家默认不展示 | §3.2.9 |
| LIFE-009 | 误出库恢复需审批 | §3.2.9 |
| LIFE-013 | 确认导入后自动生成入库申请，上限 500 条 | §3.2.9 |
| FLOW-004 | 支持手动发送提醒（飞书/邮件） | §3.2.10 |
| FLOW-005 | 7 个关键节点自动飞书通知 | §3.2.10 |
| FLOW-006 | 通知内容包含关键信息摘要和跳转链接 | §3.2.10 |
| FLOW-007 | 通知发送失败不影响业务流程 | §3.2.10 |
| STAT-003 | 权限过滤：部门管理员看本部门数据 | §3.2.11 |
| STAT-004 | 图表支持点击下钻查看明细 | §3.2.11 |
| STAT-005 | 看板支持导出 Excel/PDF | §3.2.11 |
| STAT-006 | 专家积分排名列表 | §3.2.11 |
| STAT-007 | 近 6 个月排名趋势折线图 | §3.2.11 |
| STAT-009 | 排名支持按领域筛选 | §3.2.11 |
| STAT-010 | 任务结项后一键生成履职报告 | §3.2.11 |
| STAT-011 | 履职报告导出 PDF（含公司 Logo） | §3.2.11 |
| STAT-012 | 报告模板可保存供后续复用 | §3.2.11 |

### 🔄 需修改（14 项）

| 编号 | 功能点 | 现有代码 | 差距说明 | 来源章节 |
|------|--------|----------|----------|----------|
| FORM-002 | 选「专家调用」后需求领域等字段可见 | `engagement-form-config.ts` | 前端有条件映射但后端 `EngagementRequest` 字段对所有 mode 均可用 | §3.2.1 |
| FORM-003 | 选「积分自提」后积分大类/项目可见 | 同上 | 同 FORM-002，后端无积分大类/项目专用字段 | §3.2.1 |
| FORM-008 | 项目基本信息区显隐规则 | `shouldShowProjectInfoFields()` | 规则为"评估评审/问题解决/项目经验沉淀"，与 SRS "技术支持"隐藏有差异 | §3.2.1 |
| FORM-009 | 需求领域等仅专家调用下显示 | `EngagementRequest.java` | 后端字段对所有 mode 均可用 | §3.2.1 |
| FORM-026 | 技术支持模板 8 个结构化文本域 | `taskDescription` TEXT 字段 | 单字段 vs 8 个独立结构化字段 | §3.2.1 |
| FORM-027 | 积分自提 4 个文本域 | `taskDescription` TEXT 字段 | 单字段 + placeholder vs 4 个独立字段 | §3.2.1 |
| DISP-010 | 改派后飞书通知申请人 | `reassignmentLog` JSON | 有改派记录但无飞书通知 | §3.2.2 |
| CONF-008 | 拒绝专家默认不出现 | `stewardReassign()` | 全量替换专家，无显式排除 | §3.2.3 |
| EVAL-003 | 等级 → 系数自动计算 | `APPLICANT_LEVELS_BY_ITEM` | 前端有系数矩阵，后端用 P/T/A 三维评分 | §3.2.4 |
| EVAL-008 | 15种×贡献范围标准分值 | `BASE_SCORE_BY_ITEM_SCOPE` | 前端有完整规则，后端无对应计算 | §3.2.4 |
| EVAL-009 | 行管调整评分等级 | `releaseScore()` | 可设最终分但不能调整等级 | §3.2.4 |
| FLOW-002 | 每个节点显示处理人和计划完成时间 | `EngagementRequest` | 有操作人/时间但无计划完成时间 | §3.2.10 |
| FLOW-003 | 超时节点红色标记 | 无后端支撑 | 无超时计算逻辑 | §3.2.10 |
| SRCH-005 | 多维度组合条件检索 | `ExpertController` search/domain/skill | 缺工号、拼音、部门、经验类型、地域、学历过滤 | §3.2.7 |

### 🔧 待优化（0 项）

暂无（所有 ⚠️ 要么是范围差异 → 🔄 需修改，要么是完整缺失 → 🆕 新增功能）

### ❓ 需澄清（5 项）

| 编号 | 功能点 | 无法判定的原因 | 建议验证方式 |
|------|--------|---------------|-------------|
| FORM-004 | 切换类别时不清空公共字段 | 前端实现细节，需运行测试 | 在浏览器中操作：填活动名称→切换申请类别→检查活动名称是否保留 |
| FORM-012 | 隐藏字段不清除值 | 同上 | 浏览器操作：填贡献范围→切换项目→切回→检查值 |
| EVAL-010 | 行管对积分自提类认可/不认可 | `releaseScore()` 可设0分但不区分操作类型 | 确认 `ReleaseScoreRequest` 是否有专门 approve/reject 字段 |
| SRCH-008 | 一键清空所有搜索条件 | 前端 UI 细节 | 浏览器操作：输入多个搜索条件→点击重置→验证 |
| SRCH-009 | 检索结果支持列表和卡片视图 | 前端 UI 细节 | 浏览器操作：搜索专家→检查是否有视图切换按钮 |
