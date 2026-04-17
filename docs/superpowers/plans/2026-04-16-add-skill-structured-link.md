# Add Skill Structured Link Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a structured end-to-end add-skill flow that persists tags, related skills, and documentation, then shows them in the list and detail pages.

**Architecture:** Extend the backend `Skill` domain with three bounded structures: tags, self-related skills, and one-to-one documentation. Keep the frontend page structure, adapt `AddSkill.vue` to the new DTO, and teach `SkillDetail.vue` to prefer the new structured payload while preserving current fallbacks for partial data.

**Tech Stack:** Spring Boot, Spring Data JPA, MySQL, Vue 3, TypeScript, Element Plus, Axios

**Source Of Truth:** Only modify `expert-link-backend` and `expert-link-frontend`. Treat `expert-link-frontend-simple` as reference-only prototype material.

---

### Task 1: Extend backend skill data model

**Files:**
- Create: `expert-link-backend/src/main/java/com/expertlink/domain/Tag.java`
- Create: `expert-link-backend/src/main/java/com/expertlink/domain/SkillDocumentation.java`
- Modify: `expert-link-backend/src/main/java/com/expertlink/domain/Skill.java`
- Modify: `expert-link-backend/src/main/java/com/expertlink/repository/SkillRepository.java`
- Create: `expert-link-backend/src/main/java/com/expertlink/repository/TagRepository.java`
- Create: `expert-link-backend/src/main/java/com/expertlink/repository/SkillDocumentationRepository.java`
- Test: backend compile or targeted test command

- [ ] **Step 1: Write the failing test**

Use backend compilation or a targeted controller/service call as the failure signal after referencing the new structured fields.

- [ ] **Step 2: Run test to verify it fails**

Run a backend verification command before the model changes.
Expected: new structured add-skill flow cannot exist yet.

- [ ] **Step 3: Write minimal implementation**

Implement:
- `Tag` entity with unique `name`
- `SkillDocumentation` entity with one-to-one `Skill`
- `Skill.tags` many-to-many
- `Skill.relatedSkills` self many-to-many
- serialization guards to avoid lazy-loading recursion
- shallow DTO strategy for `relatedSkills` in API responses
- stable join table names, unique constraints, and foreign-key relationships for MySQL schema generation

- [ ] **Step 4: Run test to verify it passes**

Run a backend verification command.
Expected: model compiles and can be serialized safely.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Add backend DTO-driven create flow

**Files:**
- Create: `expert-link-backend/src/main/java/com/expertlink/dto/skill/CreateSkillRequest.java`
- Create: `expert-link-backend/src/main/java/com/expertlink/dto/skill/SkillDocumentationDto.java`
- Modify: `expert-link-backend/src/main/java/com/expertlink/controller/SkillController.java`
- Modify: `expert-link-backend/src/main/java/com/expertlink/service/SkillService.java`
- Test: backend compile or live curl verification

- [ ] **Step 1: Write the failing test**

Use a live create request shape or a targeted compile signal that references the new DTO-based endpoint.

- [ ] **Step 2: Run test to verify it fails**

Call the current create endpoint using the desired DTO shape.
Expected: current endpoint contract does not support the new structured body yet.

- [ ] **Step 3: Write minimal implementation**

Implement:
- DTO-based request body for create
- skill-name uniqueness validation before persistence
- domain existence validation for `domainId`
- tag reuse/create behavior
- tag normalization with trim/case-insensitive matching before reuse/create
- related skill lookup and filtering
- explicit rejection of illegal self-association
- documentation creation
- complete skill response payload including tags, related skills, and documentation
- whole create flow wrapped in one transaction

- [ ] **Step 4: Run test to verify it passes**

Verify with a real `curl` request against `POST /api/skills`.
Expected: create returns `201` and includes structured fields.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Align frontend types and service contract

**Files:**
- Modify: `expert-link-frontend/src/api/types/skill.ts`
- Modify: `expert-link-frontend/src/api/services/skill.service.ts`
- Test: touched-file diagnostics and targeted verification

- [ ] **Step 1: Write the failing test**

Use the current frontend type mismatch against the new backend response shape as the failure signal.

- [ ] **Step 2: Run test to verify it fails**

Inspect current type definitions and compare against the new create/detail payload.
Expected: current types do not represent structured tags, related skills, and documentation.

- [ ] **Step 3: Write minimal implementation**

Implement:
- frontend DTOs for create request
- structured response types for tags, related skills, documentation
- `SkillService.createSkill()` contract aligned to the new backend endpoint

- [ ] **Step 4: Run test to verify it passes**

Run touched-file diagnostics and targeted verification.
Expected: no new diagnostics in edited service/type files.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 4: Wire `AddSkill.vue` to real backend

**Files:**
- Modify: `expert-link-frontend/src/views/skills/AddSkill.vue`
- Test: live create verification and touched-file diagnostics

- [ ] **Step 1: Write the failing test**

Use the current page behavior as the failure:
- add-skill page does not submit to real backend
- domain list is not real
- related skills are not real

- [ ] **Step 2: Run test to verify it fails**

Inspect the page and compare its submit flow to the new backend contract.
Expected: current page still uses mock submission.

- [ ] **Step 3: Write minimal implementation**

Implement:
- real domain fetch
- real related-skill fetch
- form-to-DTO mapping
- submit to `SkillService.createSkill()`
- success redirect to skill detail page
- verify the required domain and skill list APIs are sufficient for selector population, otherwise add the minimal fallback query path

- [ ] **Step 4: Run test to verify it passes**

Create a real skill through the live endpoint path used by the page.
Expected: request succeeds and persisted data is queryable.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 5: Show new structured data in skill pages

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillList.vue`
- Modify: `expert-link-frontend/src/views/skills/SkillDetail.vue`
- Test: live read verification and touched-file diagnostics

- [ ] **Step 1: Write the failing test**

Use the current structured-field fallback behavior as the failure:
- detail page does not prefer real tags
- detail page does not prefer real related skills
- detail page does not prefer real documentation

- [ ] **Step 2: Run test to verify it fails**

Inspect current detail/list mapping against the new backend shape.
Expected: new structured fields are not fully consumed yet.

- [ ] **Step 3: Write minimal implementation**

Implement:
- real tag display
- real related skill display
- real documentation display
- preserve graceful fallback for older records without structured fields

- [ ] **Step 4: Run test to verify it passes**

Verify:
- created skill detail shows tags, related skills, documentation
- returning to list shows the created skill

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 6: Final verification

**Files:**
- Modify: touched files only if fixes are needed
- Test: backend create/read checks plus frontend diagnostics

- [ ] **Step 1: Write the failing test**

Treat any new diagnostics in touched files or failed live create/read checks as failures.

- [ ] **Step 2: Run test to verify it fails**

Run:
- touched-file diagnostics
- backend live create/read checks

- [ ] **Step 3: Write minimal implementation**

Fix only issues introduced by this work.

- [ ] **Step 4: Run test to verify it passes**

Confirm:
- add-skill flow persists structured data
- list/detail can read the created data
- no new diagnostics in touched files

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
