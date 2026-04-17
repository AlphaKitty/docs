# Project Detail Actions Closure Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace `ProjectDetail.vue` placeholder actions with usable front-end/business closures without adding a new backend document model.

**Architecture:** Reuse existing project/expert services for export and routing, and keep document behavior local to the detail page state. This gives the page a practical closed loop while avoiding fake backend coupling.

**Tech Stack:** Vue 3, TypeScript, Element Plus, existing `ProjectService`

---

### Task 1: Wire export and contact actions

**Files:**
- Modify: `expert-link-frontend/src/views/projects/ProjectDetail.vue`

- [ ] Add helper utilities for browser download and contact presentation.
- [ ] Validate existing export endpoint behavior and add accurate UI copy for collection-vs-single-project export.
- [ ] Replace `exportProject` toast with real `ProjectService.exportProjects()` download flow.
- [ ] Replace `contactExpert` toast with the defined priority: route to expert detail first, fallback to dialog, use `mailto:` only when real email data exists.

### Task 2: Close document actions locally

**Files:**
- Modify: `expert-link-frontend/src/views/projects/ProjectDetail.vue`

- [ ] Extend document rows with preview/download content fields as needed.
- [ ] Replace `previewDocument` toast with dialog preview.
- [ ] Replace `downloadDocument` toast with generated-file download.
- [ ] Replace upload toast-only handler with local list append behavior and explicit front-end-only messaging.
- [ ] Make local-only semantics explicit: uploaded documents are seeded from current page data and disappear after refresh/navigation.

### Task 3: Verify edited page

**Files:**
- Check: `expert-link-frontend/src/views/projects/ProjectDetail.vue`

- [ ] Run diagnostics for the edited file.
- [ ] Hit the export API or proxy endpoint to confirm it still responds.
