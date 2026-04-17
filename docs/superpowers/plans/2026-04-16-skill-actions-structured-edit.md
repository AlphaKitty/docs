# Skill Actions Structured Edit Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make skill delete real from the list and support full structured editing through a dedicated edit page.

**Architecture:** Upgrade the backend update endpoint to the same structured DTO model used by create, then reuse `AddSkill.vue` as a dual-mode create/edit form. Keep list interactions simple: delete acts in place, edit/config route to the same dedicated page.

**Tech Stack:** Spring Boot, Spring Data JPA, Vue 3, TypeScript, Element Plus, Axios

**Source Of Truth:** Only modify `expert-link-backend` and `expert-link-frontend`.

---

### Task 1: Add structured update flow in backend

**Files:**
- Modify: `expert-link-backend/src/main/java/com/expertlink/dto/skill/CreateSkillRequest.java`
- Modify: `expert-link-backend/src/main/java/com/expertlink/controller/SkillController.java`
- Modify: `expert-link-backend/src/main/java/com/expertlink/service/SkillService.java`
- Test: backend compile and live update verification

- [ ] **Step 1: Write the failing test**

Use the current `PUT /api/skills/{id}` contract mismatch as the failure signal.

- [ ] **Step 2: Run test to verify it fails**

Send a structured update payload to the current update endpoint.
Expected: current endpoint cannot fully update structured fields.

- [ ] **Step 3: Write minimal implementation**

Implement:
- DTO-based structured update
- tag replacement with normalization
- related skill replacement with self-association rejection
- documentation create/update/clear logic
- transaction-wrapped update flow

- [ ] **Step 4: Run test to verify it passes**

Verify a real skill can be updated and queried back.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Add edit-mode route and form reuse

**Files:**
- Modify: `expert-link-frontend/src/router/index.ts`
- Modify: `expert-link-frontend/src/views/skills/AddSkill.vue`
- Modify: `expert-link-frontend/src/api/services/skill.service.ts`
- Test: touched-file diagnostics and live form verification

- [ ] **Step 1: Write the failing test**

Use the current UI behavior as the failure signal:
- no dedicated edit route
- add form cannot load or save existing structured data

- [ ] **Step 2: Run test to verify it fails**

Inspect route definitions and current add-skill form behavior.
Expected: edit flow does not exist.

- [ ] **Step 3: Write minimal implementation**

Implement:
- `/skills/:id/edit`
- dual-mode form title and submit behavior
- load existing skill detail into form
- call `updateSkill()` in edit mode

- [ ] **Step 4: Run test to verify it passes**

Verify an existing skill can be loaded and updated through the form.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Make list actions real

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillList.vue`
- Test: touched-file diagnostics and live delete verification

- [ ] **Step 1: Write the failing test**

Use the current list action behavior as the failure signal:
- delete is still placeholder-only
- edit/config are not routed to real editing

- [ ] **Step 2: Run test to verify it fails**

Inspect current action handlers.
Expected: they do not call real edit/delete flows.

- [ ] **Step 3: Write minimal implementation**

Implement:
- real delete request with post-delete refresh
- edit action routes to edit page
- config action routes to same edit page

- [ ] **Step 4: Run test to verify it passes**

Verify a skill can be deleted and disappears from list data.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 4: Final verification

**Files:**
- Modify: touched files only if fixes are needed
- Test: backend compile, touched-file diagnostics, and live CRUD verification

- [ ] **Step 1: Write the failing test**

Treat any new diagnostics or failed live update/delete checks as failures.

- [ ] **Step 2: Run test to verify it fails**

Run compile/diagnostic/live verification commands.

- [ ] **Step 3: Write minimal implementation**

Fix only issues introduced by this work.

- [ ] **Step 4: Run test to verify it passes**

Confirm:
- structured edit works
- delete works
- no new diagnostics in touched files

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
