# Project Detail Actions Closure Design

**Goal:** Close the remaining placeholder actions in `ProjectDetail.vue` without introducing a new backend document subsystem.

## Scope

- Keep `编辑项目` / `匹配专家` / `删除项目` as-is.
- Make `导出项目` call the existing backend export API.
- Make `联系专家` provide real, useful navigation/contact behavior from current data.
- Make `预览文档` / `下载文档` / `上传文档` stop being pure placeholder toasts.

## Design

### 1. Export project

Reuse `ProjectService.exportProjects()` and download the returned blob on the client. The implementation must first validate the current backend export contract. If the endpoint only supports collection export, the UI copy must explicitly state that this action exports the current project data set from the backend export capability, rather than pretending to be a dedicated single-project export endpoint.

### 2. Contact expert

Use current assignment/expert data only. The closure priority is:

1. navigate to the assigned expert detail page when the expert id is available
2. show an `ElMessageBox.alert` with expert name, title, and any contact fields present
3. offer `mailto:` only if an email field actually exists

This keeps the action useful and real without inventing a messaging subsystem.

### 3. Project documents

Do not add backend persistence in this round. Treat the document area as a detail-page document workspace:

- `preview`: show document metadata and placeholder content in a dialog
- `download`: generate a local text file from current document metadata/content
- `upload`: append a new document row into the current page state with current timestamp and file size

The UI must clearly remain functional without implying backend persistence. Upload-related copy must explicitly state that newly added documents are only kept in the current page state and will disappear after refresh/navigation unless a real backend document subsystem is added later.

## Error Handling

- Export failures use `ElMessage.error`
- Document preview/download/upload guard against empty data
- Contact action degrades gracefully if only basic expert data exists

## Verification

- IDE diagnostics on modified file
- Frontend build or targeted diagnostics if full build remains noisy
- Live backend call for export endpoint
