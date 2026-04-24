---
name: playwright-cli
description: 使用 Playwright CLI 进行页面可视化调试、截图、录制 trace、回放失败现场。Use when user asks for visual page debugging, browser automation checks, UI interaction diagnosis, screenshot capture, or Playwright command-line troubleshooting.
---

# Playwright CLI Visual Debug

## Purpose

Provide a repeatable command-line workflow for visual page debugging:
- launch browser flows
- capture screenshots
- record and inspect traces
- diagnose flaky UI interactions

## Quick Start

1. Confirm project root and app startup command.
2. Ensure Playwright dependency exists; if missing, install it.
3. Run target flow with one of:
   - headed mode for live observation
   - trace mode for post-mortem inspection
   - screenshot capture for UI state evidence
4. Save artifacts under `playwright-artifacts/`.
5. Report: repro steps, failure point, evidence path, and fix suggestion.

## Standard Commands

Use these defaults unless user requests otherwise.

### Install

```bash
npm i -D @playwright/test
npx playwright install
```

### Run in headed mode (observe behavior)

```bash
npx playwright test --headed
```

### Record trace for debugging

```bash
npx playwright test --trace on
```

### Open trace viewer

```bash
npx playwright show-trace test-results/**/trace.zip
```

### Capture screenshot (single test)

```bash
npx playwright test tests/visual-debug.spec.ts --headed --workers=1
```

## Workflow

Copy this checklist and keep it updated:

```text
Visual Debug Progress
- [ ] Step 1: Reproduce issue with deterministic URL/data/account
- [ ] Step 2: Run Playwright in headed mode or with trace
- [ ] Step 3: Capture screenshot/trace/video artifacts
- [ ] Step 4: Identify exact failing selector/action/assertion
- [ ] Step 5: Propose or apply fix, then re-run to verify
```

## Debug Heuristics

- Prefer stable locators (`getByRole`, `getByTestId`) over brittle CSS chains.
- Add explicit wait on deterministic UI state, not arbitrary sleep.
- Use `--workers=1` when investigating flaky tests.
- Keep one failing case minimal before broad suite reruns.
- Preserve evidence files for regression comparison.

## Output Format

When reporting findings, use:

```markdown
问题复现：
- 入口：
- 操作：
- 期望：
- 实际：

证据：
- 截图：
- Trace：
- 关键日志：

结论与建议：
- 根因：
- 修复建议：
- 验证结果：
```

## Notes

- If project does not have tests yet, create a minimal repro spec first.
- If running in CI/headless environment, avoid GUI-only assumptions.
- Keep artifact paths explicit and reusable in follow-up runs.
