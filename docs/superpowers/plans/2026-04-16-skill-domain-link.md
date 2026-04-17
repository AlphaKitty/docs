# Skill Domain Link Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `SkillList` and `SkillDetail` use real backend `skills` and `domains` data while preserving the existing UI structure.

**Architecture:** Keep the current Vue pages, add a small `DomainService`, and introduce page-local adapters that convert backend `skills` and `domains` into the existing list-card-detail view models. The primary join key is `skill.domain.id`; if that field is missing, the adapter may fall back to `domainId` or domain name matching as a guarded fallback. This keeps changes narrow and avoids introducing a new store for this round.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Axios, Spring Boot REST APIs

**Source Of Truth:** Only modify `expert-link-frontend`. Treat `expert-link-frontend-simple` as reference-only prototype material.

---

### Task 1: Add domain API service

**Files:**
- Create: `expert-link-frontend/src/api/services/domain.service.ts`
- Modify: `expert-link-frontend/src/api/services/index.ts`
- Test: targeted frontend type/build check

- [ ] **Step 1: Write the failing test**

Use the existing frontend type/build command as the failing signal after referencing the new service import.

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run build`
Expected: fail before the new service exists or exports correctly.

- [ ] **Step 3: Write minimal implementation**

Add a `DomainService` with:
- `getDomains(params)`
- `getDomainById(id)`

First verify the real `/api/domains` payload shape, then align the service contract to that shape instead of assuming pagination blindly.

- [ ] **Step 4: Run test to verify it passes**

Run a targeted build/type check for touched files.
Expected: no new errors from the domain service integration.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Link real data in `SkillList`

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillList.vue`
- Modify: `expert-link-frontend/src/api/services/skill.service.ts`
- Test: targeted frontend type/build check

- [ ] **Step 1: Write the failing test**

Use the current page assumptions as the failing behavior:
- list page still renders static skills
- top category cards do not reflect real domains

- [ ] **Step 2: Run test to verify it fails**

Run a targeted build/type check and inspect live API payloads.
Expected: current page logic depends on mock-only fields.

- [ ] **Step 3: Write minimal implementation**

Implement:
- skill list fetch on mount
- domain fetch on mount
- fetch enough skill records for stable card aggregation instead of relying only on the visible page
- page-local adapter helpers for list rows and category cards
- real search/filter against adapted data
- `demandLevel` mapping from backend enum to display score/text
- verify frontend proxy/base URL assumptions before judging integration failures
- category card aggregation rules:
  - `name` from domain name
  - `description` from domain description
  - `expertCount` / `projectCount` from domain counters when present
  - `topSkills` from a broader fetched skill set in the same domain, not just the current visible page
  - `matchRate` as a documented placeholder derived in the adapter for display continuity

- [ ] **Step 4: Run test to verify it passes**

Run targeted verification:
- build/type check for touched files
- live `curl` against `/api/skills` and `/api/domains`
Expected: page logic matches real payload shape and produces no new diagnostics.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Link real data in `SkillDetail`

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillDetail.vue`
- Modify: `expert-link-frontend/src/api/services/skill.service.ts`
- Test: targeted frontend type/build check

- [ ] **Step 1: Write the failing test**

Use the current page assumptions as the failing behavior:
- detail page renders static mock content
- related skills are not derived from real backend data

- [ ] **Step 2: Run test to verify it fails**

Inspect current page code and live API shape.
Expected: current detail logic cannot reflect backend records faithfully.

- [ ] **Step 3: Write minimal implementation**

Implement:
- fetch skill detail by route id
- fetch enough additional skills for related/same-category sections so results are not accidentally empty because of pagination
- page-local adapter helpers for missing fields and fallback display
- ensure `SkillService.getSkillById()` and related response typing match the real backend payload
- keep unsupported actions as informational only
- fallback rules:
  - `tags` derived from skill name, category, and domain name when backend tags are absent
  - `documentation` falls back to empty and renders as “暂无文档”
  - `creator` falls back to “系统”
  - `matchRate` falls back to a stable placeholder display value
- selection rules:
  - “相关技能” uses other skills from the same domain first, then same category if needed
  - “同分类技能” uses other skills with the same adapted category, excluding the current skill

- [ ] **Step 4: Run test to verify it passes**

Run targeted verification:
- build/type check for touched files
- live `curl` for `/api/skills/{id}`
Expected: no new diagnostics and detail mapping matches real payloads.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 4: Final verification

**Files:**
- Modify: none or touched files only if fixes are needed
- Test: targeted frontend checks plus live endpoint verification

- [ ] **Step 1: Write the failing test**

Treat any new lints or type errors in touched files as failures.

- [ ] **Step 2: Run test to verify it fails**

Run:
- `ReadLints` on touched files
- targeted build/type check

- [ ] **Step 3: Write minimal implementation**

Fix only issues introduced by this work.

- [ ] **Step 4: Run test to verify it passes**

Confirm:
- no new lints in touched files
- targeted verification commands succeed
- loading, empty, and error states on list/detail remain acceptable

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
