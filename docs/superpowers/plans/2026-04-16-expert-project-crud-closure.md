# Expert Project CRUD Closure Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the core CRUD loop for experts and projects by adding real edit flows and expert-side project matching.

**Architecture:** Reuse the existing add pages as add/edit dual-mode forms instead of creating separate edit views. Wire list/detail entry points to the new edit routes, and add a real project-assignment dialog on the expert detail page that uses the existing project-expert API.

**Tech Stack:** Vue 3 Composition API, TypeScript, Pinia, Element Plus, Axios service layer

---

### Task 1: Add edit routes and dual-mode expert/project forms

**Files:**
- Modify: `expert-link-frontend/src/router/index.ts`
- Modify: `expert-link-frontend/src/views/experts/AddExpert.vue`
- Modify: `expert-link-frontend/src/views/projects/AddProject.vue`
- Modify if needed: `expert-link-frontend/src/stores/expert.ts`
- Modify if needed: `expert-link-frontend/src/stores/project.ts`
- Test: touched-file diagnostics

- [ ] **Step 1: Confirm the current failure signal**

Inspect current routes and add pages.
Expected:
- no `/experts/:id/edit`
- no `/projects/:id/edit`
- add pages only support create mode

- [ ] **Step 2: Implement minimal dual-mode routing and loading**

Implement:
- edit routes for experts and projects
- route-based `isEditMode`
- detail fetch + form fill in edit mode
- correct page title / submit text in both modes

- [ ] **Step 3: Implement real update submit path**

Implement:
- expert edit submit uses real update API/store path
- project edit submit uses real update API/store path
- success redirects back to the corresponding detail page

- [ ] **Step 4: Verify the form modes**

Verify:
- add mode still creates correctly
- edit mode loads existing data
- save updates and redirects correctly

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 2: Wire list/detail edit entry points

**Files:**
- Modify: `expert-link-frontend/src/views/experts/ExpertList.vue`
- Modify: `expert-link-frontend/src/views/experts/ExpertDetail.vue`
- Modify: `expert-link-frontend/src/views/projects/ProjectList.vue`
- Modify: `expert-link-frontend/src/views/projects/ProjectDetail.vue`
- Test: live route navigation checks

- [ ] **Step 1: Confirm the current failure signal**

Expected:
- list edit buttons are placeholders
- detail edit buttons point to nonexistent routes

- [ ] **Step 2: Implement route wiring**

Implement:
- expert list edit -> `/experts/:id/edit`
- expert detail edit -> `/experts/:id/edit`
- project list edit -> `/projects/:id/edit`
- project detail edit -> `/projects/:id/edit`

- [ ] **Step 3: Verify entry points**

Verify each edit button opens the correct edit form and the edited data is visible after save.

- [ ] **Step 4: Commit**

Do not commit unless explicitly requested by the user.

### Task 3: Add expert-side real project matching

**Files:**
- Modify: `expert-link-frontend/src/views/experts/ExpertDetail.vue`
- Modify if needed: `expert-link-frontend/src/api/services/project-expert.service.ts`
- Test: live matching flow through frontend proxy

- [ ] **Step 1: Confirm the current failure signal**

Expected:
- `matchProjects()` is still placeholder-only
- expert detail cannot create a real assignment from the expert side

- [ ] **Step 2: Implement the matching dialog**

Implement:
- load real projects for selection
- filter out already assigned projects
- capture minimum assignment fields needed by the existing create API:
  - `projectId`
  - `expertId`
  - `role`
  - optional `startDate`
  - optional `responsibilities`
  - optional `totalHours`

- [ ] **Step 3: Implement real assignment submit**

Implement:
- call `ProjectExpertService.createAssignment(...)`
- refresh expert-side project data after success
- prevent duplicate assignment to the same project

- [ ] **Step 4: Verify cross-page closure**

Verify:
- expert detail can assign a project
- expert page reflects the new project
- the target project detail page reflects the assigned expert

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.

### Task 4: Final verification

**Files:**
- Modify touched files only if fixes are needed
- Test: touched-file diagnostics and live CRUD checks

- [ ] **Step 1: Run diagnostics**

Check touched frontend files for newly introduced diagnostics.

- [ ] **Step 2: Run live CRUD verification**

Verify end to end:
- expert add -> edit -> detail visible
- project add -> edit -> detail visible
- expert detail -> match project -> project detail visible

- [ ] **Step 3: Fix only issues introduced by this work**

- [ ] **Step 4: Re-run diagnostics and live checks**

- [ ] **Step 5: Commit**

Do not commit unless explicitly requested by the user.
