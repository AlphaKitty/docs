# Skill List Structured Enhancement Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance `SkillList.vue` to display structured tags, related-skill counts, and documentation status with a lightweight document preview action.

**Architecture:** Keep the current list page layout and real API loading path. Extend the page-local adapter so each row carries structured fields, then add a few compact table columns and a lightweight document preview action using existing Element Plus components.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Axios

**Source Of Truth:** Only modify `expert-link-frontend`.

---

### Task 1: Extend list row mapping

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillList.vue`
- Test: touched-file diagnostics and live list payload verification

- [ ] **Step 1: Write the failing test**

Use the current list behavior as the failure signal:
- structured fields are returned by the API but not shown in row data

- [ ] **Step 2: Run test to verify it fails**

Compare current row adapter against live `/api/skills` payload.
Expected: tags, related skills, and documentation are ignored.

- [ ] **Step 3: Write minimal implementation**

Add row-level fields for:
- `tags`
- `relatedSkillNames`
- `relatedSkillCount`
- `documentation`
- `hasDocumentation`

- [ ] **Step 4: Run test to verify it passes**

Verify row mapping matches the live payload shape.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Add compact structured columns and actions

**Files:**
- Modify: `expert-link-frontend/src/views/skills/SkillList.vue`
- Test: touched-file diagnostics and live UI-data verification

- [ ] **Step 1: Write the failing test**

Use the current table behavior as the failure signal:
- no visible tag column
- no related-skill summary
- no documentation status or preview action

- [ ] **Step 2: Run test to verify it fails**

Inspect current table columns.
Expected: structured fields are not visible.

- [ ] **Step 3: Write minimal implementation**

Implement:
- compact tags column with truncation
- related-skill count column with tooltip names
- documentation status column
- document preview button in action area

- [ ] **Step 4: Run test to verify it passes**

Verify the created structured skill can expose its tags and documentation from the list view.

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Final verification

**Files:**
- Modify: touched files only if fixes are needed
- Test: touched-file diagnostics and live endpoint checks

- [ ] **Step 1: Write the failing test**

Treat any new diagnostics in touched files as failures.

- [ ] **Step 2: Run test to verify it fails**

Run touched-file diagnostics.

- [ ] **Step 3: Write minimal implementation**

Fix only issues introduced by this enhancement.

- [ ] **Step 4: Run test to verify it passes**

Confirm:
- structured list fields render
- document preview works
- no new diagnostics in touched files

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
