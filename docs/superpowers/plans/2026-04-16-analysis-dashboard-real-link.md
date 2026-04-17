# Analysis Dashboard Real Link Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the mock data and placeholder charts in `AnalysisDashboard.vue` with real aggregated frontend data sourced from existing APIs.

**Architecture:** Keep the implementation frontend-only and avoid `/stats/...` endpoints entirely. Add a focused aggregation layer in the analysis view (or a nearby helper module if the file becomes too large) that loads paginated real entities, expands project-expert assignments via per-project fetches, normalizes source fields, and feeds deterministic card, table, and chart view models into the existing Element Plus layout.

**Tech Stack:** Vue 3 Composition API, TypeScript, Element Plus, ECharts, Axios service layer

---

### File Structure

**Files and responsibilities:**
- Modify: `expert-link-frontend/src/views/analysis/AnalysisDashboard.vue`
  - Own page loading/error/empty states
  - Call real services
  - Aggregate page view models for cards, tables, and charts
  - Replace placeholder chart blocks with live ECharts rendering
- Modify: `expert-link-frontend/src/api/services/stats.service.ts`
  - De-emphasize or stop using dead `/stats/...` endpoints from this page
  - Only touch if a small helper is useful; do not depend on missing backend routes
- Modify: `expert-link-frontend/src/api/types/stats.ts` only if the analysis page needs local view-model types aligned more tightly
- Test: touched-file diagnostics plus live API verification via frontend proxy

Keep the implementation centered in `AnalysisDashboard.vue` unless the aggregation helpers become too noisy; if so, extract only pure local helpers inside the same feature area.

**Deterministic bucket definitions:**
- Project budget buckets:
  - `0-10万`
  - `10万-50万`
  - `50万-100万`
  - `100万以上`
- Project duration buckets:
  - `0-30天`
  - `31-90天`
  - `91-180天`
  - `180天以上`
- Assignment completion buckets:
  - `0-25%`
  - `26-50%`
  - `51-75%`
  - `76-100%`

**Concrete field confirmations to use during implementation:**
- Experts:
  - status/distribution uses `availabilityStatus` if present, otherwise a normalized fallback field already used by the page/store
  - company distribution uses `currentCompany`, fallback bucket `未填写公司`
  - skill distribution uses `skills[].name`
- Projects:
  - active project count uses `status === 'IN_PROGRESS'`
  - budget uses `budget`
  - duration uses `startDate` and `endDate`, or current date when `endDate` is missing and status is ongoing
  - project-side fallback ordering fields for matches use `updatedAt`, then `createdAt`
- Assignments:
  - primary ordering uses `startDate`
  - status distribution uses raw assignment `status`
  - score column uses `completionPercentage` only; if absent, render status instead of score

### Task 1: Lock down live data inputs and pagination

**Files:**
- Modify: `expert-link-frontend/src/views/analysis/AnalysisDashboard.vue`
- Test: live API checks against `experts`, `projects`, `skills`, `domains`, `project-experts`

- [ ] **Step 1: Confirm the current failure signal**

Inspect `AnalysisDashboard.vue`.
Expected: mock `stats`, `topSkills`, and `recentMatches` are hardcoded and no real service-backed load pipeline exists.

- [ ] **Step 2: Confirm live source shapes**

Read the real service/type definitions used by the page:
- `expert-link-frontend/src/api/services/expert.service.ts`
- `expert-link-frontend/src/api/services/project.service.ts`
- `expert-link-frontend/src/api/services/skill.service.ts`
- `expert-link-frontend/src/api/services/project-expert.service.ts`
- `expert-link-frontend/src/api/types/project.ts`

Expected: concrete fields for budget, start/end dates, assignment status, completion percentage, company, and skills are identified before implementation.

- [ ] **Step 3: Write minimal implementation**

Implement:
- `loadAllPages(fetchPage)` helper that:
  - requests page `0` with `size=200`
  - uses `totalPages` to fetch remaining pages
  - returns a merged array while preserving `totalElements` for total-only metrics
- page-level loader that fetches:
  - experts via `ExpertService.getExperts`
  - projects via `ProjectService.getProjects`
  - skills via `SkillService.getSkills`
  - domains via `DomainService.getDomains`
  - these four base entity fetches run in parallel
  - remaining pages for each dataset continue sequentially after page `0`
- project-assignment expansion:
  - iterate all fetched projects
  - call `ProjectExpertService.getAssignmentsByProjectId(project.id)`
  - dedupe by `projectId + expertId`
  - choose winner using the spec rule:
    - later `startDate`
    - else higher `completionPercentage`
    - else larger `id`

- [ ] **Step 4: Verify live fetch path**

Run live checks through the frontend proxy for the required endpoints.
Expected: all data sources return real payloads the page can aggregate.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Replace top cards and tables with real aggregated data

**Files:**
- Modify: `expert-link-frontend/src/views/analysis/AnalysisDashboard.vue`
- Test: touched-file diagnostics and live value spot checks

- [ ] **Step 1: Confirm the current failure signal**

Inspect the template and script.
Expected:
- top cards read from fake `stats`
- ranking table reads fake `topSkills`
- recent matches table reads fake `recentMatches`

- [ ] **Step 2: Define the page-level aggregation outputs**

Before editing the template, write the exact local view-model shapes the page will expose for:
- top cards
- ranked skills
- recent matches

Expected: no part of the template still depends on mock-only field names or fake growth fields.

- [ ] **Step 3: Write minimal implementation**

Implement deterministic view-model builders:
- cards:
  - `expertCount` from experts total
  - `projectCount` from projects with `status === 'IN_PROGRESS'`
  - `skillCount` as real skill total, with domain count shown in secondary copy
  - `matchRate` as `projectsWithAssignments / totalProjects`, with `--` on zero projects
- remove fake growth percentages:
  - replace with truthful helper copy such as current totals / source notes
- top skills table:
  - normalize `demandLevel` using:
    - `CRITICAL -> 5`
    - `HIGH -> 4`
    - `MEDIUM -> 3`
    - `LOW -> 2`
    - fallback `1`
  - sort by `projectCount`, then `expertCount`, then normalized demand
  - assign `rank`
- recent matches table:
  - derive rows from deduped assignments
  - sort by `assignment.startDate`, then `project.updatedAt`, then `project.createdAt`
  - if `completionPercentage` exists, show it in the score column
  - otherwise downgrade the score column into a status-oriented display
  - map real statuses to Chinese text without inventing new statuses
  - show empty state if there are no assignments

- [ ] **Step 4: Verify values against live data**

Run touched-file diagnostics and compare several rendered values against live API data.
Expected:
- card totals match source totals
- hot-skill ordering follows the declared sort rule
- recent matches contain only real rows

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Replace placeholder charts with deterministic ECharts data

**Files:**
- Modify: `expert-link-frontend/src/views/analysis/AnalysisDashboard.vue`
- Test: diagnostics plus manual chart/data spot checks

- [ ] **Step 1: Confirm the current failure signal**

Inspect the chart sections.
Expected: placeholder blocks exist and selectors do not drive any real chart option output.

- [ ] **Step 2: Lock chart option contracts**

Before implementing ECharts output, define the exact selector-to-metric mapping for each chart inside the page:
- expert chart:
  - `skill`
  - `status`
  - `company`
- project chart:
  - `status`
  - `budget`
  - `duration`
- skill chart:
  - `hot`
  - `demand`
  - `domain`
- match chart:
  - `coverage`
  - `status`
  - `completion`

Expected: titles, selectors, and bucket rules exactly match the spec.

- [ ] **Step 3: Write minimal implementation**

Implement ECharts-backed chart options for:
- `专家分布统计`
  - `skill`: aggregate top expert skills
  - `status`: aggregate expert availability/status fields
  - `company`: aggregate company names with fallback bucket `未填写公司`
- `项目进度统计`
  - `status`: project status counts
  - `budget`: use fixed buckets `0-10万 / 10万-50万 / 50万-100万 / 100万以上`
  - `duration`: use fixed buckets `0-30天 / 31-90天 / 91-180天 / 180天以上`
    with `startDate -> endDate`, or `Date.now()` for ongoing projects
- `技能需求分布`
  - rename from “趋势”
  - selector values:
    - `hot`
    - `demand`
    - `domain`
  - ignore `timeRange`
- `项目匹配分布`
  - rename from “匹配成功率趋势”
  - selector values:
    - `coverage`
    - `status`
    - `completion`
  - `completion`: use fixed buckets `0-25% / 26-50% / 51-75% / 76-100%`
  - apply `timeRange` only when assignment date fields exist
- remove fake temporal selector labels (`week/month/quarter`, `growth/match`) where the spec forbids fake history
- make ignored `timeRange` behavior explicit in the UI with short helper text or disabled-state explanation, not silent no-op behavior

- [ ] **Step 4: Verify chart outputs**

Check the page manually and verify chart totals against source aggregates.
Expected:
- no placeholder blocks remain
- each selector changes a real chart
- chart bucket labels and totals are deterministic and explainable

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 4: Add resilient page states and verification matrix

**Files:**
- Modify: `expert-link-frontend/src/views/analysis/AnalysisDashboard.vue`
- Modify if needed: `expert-link-frontend/src/api/types/stats.ts`
- Test: diagnostics and live degraded-state checks

- [ ] **Step 1: Confirm the current failure signal**

Inspect the page.
Expected: no top-level `loading`, `error`, or per-block degraded state logic is present.

- [ ] **Step 2: Lock the partial-failure orchestration**

Define the load orchestration before coding:
- each base source tracks its own success/failure
- successful sources still feed cards/tables/charts
- only blocks that depend on failed sources degrade
- page-level error alert summarizes failed sources without hiding successful sections

- [ ] **Step 3: Write minimal implementation**

Implement:
- page `loading`
- top-level error alert
- per-block empty/degraded copy
- special degradation rule:
  - if `skills` succeeds but `domains` fails, keep skill total and show `领域数据加载失败`
- verification matrix notes in the implementation handoff / final report covering:
  - each card -> endpoints + formula
  - each chart -> endpoints + formula + bucket rules
  - each empty/degraded state -> trigger condition

- [ ] **Step 4: Verify degraded states and diagnostics**

Run:
- touched-file diagnostics
- live endpoint verification
- manual page verification through the dev server

Expected:
- no new diagnostics in touched frontend files
- page loads with real data
- partial failure handling is understandable and localized

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
