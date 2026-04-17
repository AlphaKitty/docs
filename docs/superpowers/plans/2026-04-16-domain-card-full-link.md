# Domain Card Full Link Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn domain cards into real entry points for domain detail, domain edit, and domain delete flows.

**Architecture:** Keep `SkillList.vue` as the entry page, but move domain detail and edit responsibilities into dedicated views. Reuse the existing backend domain APIs and keep delete as an in-place action with refresh.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Axios

**Source Of Truth:** Only modify `expert-link-frontend`.

---

### Task 1: Add domain routes and views

**Files:**
- Modify: `expert-link-frontend/src/router/index.ts`
- Create: `expert-link-frontend/src/views/skills/DomainDetail.vue`
- Create: `expert-link-frontend/src/views/skills/DomainEdit.vue`
- Test: touched-file diagnostics

- [ ] **Step 1: Write the failing test**

Use the current routing gap as the failure signal.

- [ ] **Step 2: Run test to verify it fails**

Inspect current routes.
Expected: no domain detail/edit pages exist.

- [ ] **Step 3: Write minimal implementation**

Add domain detail/edit routes and scaffold views around real data loading.

- [ ] **Step 4: Run test to verify it passes**

Verify routes are reachable and files are free of new diagnostics.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Implement domain detail and edit flows

**Files:**
- Create: `expert-link-frontend/src/views/skills/DomainDetail.vue`
- Create: `expert-link-frontend/src/views/skills/DomainEdit.vue`
- Modify: `expert-link-frontend/src/api/services/domain.service.ts`
- Test: live read/update verification

- [ ] **Step 1: Write the failing test**

Use the current lack of read/edit UI as the failure signal.

- [ ] **Step 2: Run test to verify it fails**

Inspect current frontend domain support.
Expected: no dedicated detail/edit flow exists.

- [ ] **Step 3: Write minimal implementation**

Implement:
- domain detail loading by id
- parent domain selector
- real update submit
- same-domain skills summary if available from existing skill list API

- [ ] **Step 4: Run test to verify it passes**

Verify a real domain can be opened and updated.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Wire domain card actions

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillList.vue`
- Test: live navigation/delete verification

- [ ] **Step 1: Write the failing test**

Use the current card-action placeholders as the failure signal.

- [ ] **Step 2: Run test to verify it fails**

Inspect current handlers.
Expected: card actions are still placeholder-only.

- [ ] **Step 3: Write minimal implementation**

Implement:
- detail navigation
- edit navigation
- real delete request with refresh and error handling

- [ ] **Step 4: Run test to verify it passes**

Verify:
- detail navigation works
- edit navigation works
- delete removes a domain card when allowed

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 4: Final verification

**Files:**
- Modify: touched files only if fixes are needed
- Test: touched-file diagnostics and live domain checks

- [ ] **Step 1: Write the failing test**

Treat any new diagnostics or failed live checks as failures.

- [ ] **Step 2: Run test to verify it fails**

Run diagnostics and live checks.

- [ ] **Step 3: Write minimal implementation**

Fix only issues introduced by this work.

- [ ] **Step 4: Run test to verify it passes**

Confirm:
- domain detail/edit pages work
- delete works when backend allows it
- no new diagnostics in touched files

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
