# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: project-create-submit-detail.spec.ts >> 创建项目 -> 提交 -> 查看详情
- Location: tests/project-create-submit-detail.spec.ts:3:1

# Error details

```
Error: page.goto: net::ERR_CONNECTION_REFUSED at http://127.0.0.1:3334/projects/add
Call log:
  - navigating to "http://127.0.0.1:3334/projects/add", waiting until "load"

```

# Test source

```ts
  92  |           totalPages: 1,
  93  |           size: 200,
  94  |           number: 0,
  95  |         },
  96  |       }),
  97  |     })
  98  |   })
  99  | 
  100 |   await page.route('**/api/projects**', async (route) => {
  101 |     const method = route.request().method()
  102 |     const url = route.request().url()
  103 | 
  104 |     if (method === 'POST' && /\/api\/projects(\?.*)?$/.test(url)) {
  105 |       await route.fulfill({
  106 |         status: 200,
  107 |         contentType: 'application/json',
  108 |         body: JSON.stringify({
  109 |           code: 200,
  110 |           message: 'ok',
  111 |           data: {
  112 |             id: createdProjectId,
  113 |             name: projectName,
  114 |             code: projectCode,
  115 |             description: 'Playwright可视化调试流程验证',
  116 |             status: 'IN_PROGRESS',
  117 |             startDate: '2026-04-24',
  118 |             endDate: '2026-06-30',
  119 |             budget: 100000,
  120 |             progress: 10,
  121 |             managerName: '管理员A',
  122 |             requiredSkills: [{ id: 11, skillName: 'Playwright', requiredCount: 1 }],
  123 |             domainName: '技术咨询',
  124 |           },
  125 |         }),
  126 |       })
  127 |       return
  128 |     }
  129 | 
  130 |     if (method === 'GET' && url.includes(`/api/projects/${createdProjectId}`)) {
  131 |       await route.fulfill({
  132 |         status: 200,
  133 |         contentType: 'application/json',
  134 |         body: JSON.stringify({
  135 |           code: 200,
  136 |           message: 'ok',
  137 |           data: {
  138 |             id: createdProjectId,
  139 |             name: projectName,
  140 |             code: projectCode,
  141 |             description: 'Playwright可视化调试流程验证',
  142 |             clientName: '测试客户',
  143 |             domainName: '技术咨询',
  144 |             budget: 100000,
  145 |             spent: 5000,
  146 |             startDate: '2026-04-24',
  147 |             endDate: '2026-06-30',
  148 |             managerName: '管理员A',
  149 |             status: 'IN_PROGRESS',
  150 |             progress: 10,
  151 |             requiredSkills: [{ id: 11, skillName: 'Playwright', requiredCount: 1 }],
  152 |             documents: [],
  153 |           },
  154 |         }),
  155 |       })
  156 |       return
  157 |     }
  158 | 
  159 |     await route.continue()
  160 |   })
  161 | 
  162 |   await page.route(`**/api/project-experts/project/${createdProjectId}`, async (route) => {
  163 |     await route.fulfill({
  164 |       status: 200,
  165 |       contentType: 'application/json',
  166 |       body: JSON.stringify({
  167 |         code: 200,
  168 |         message: 'ok',
  169 |         data: [],
  170 |       }),
  171 |     })
  172 |   })
  173 | 
  174 |   await page.route('**/api/experts**', async (route) => {
  175 |     await route.fulfill({
  176 |       status: 200,
  177 |       contentType: 'application/json',
  178 |       body: JSON.stringify({
  179 |         code: 200,
  180 |         message: 'ok',
  181 |         data: {
  182 |           content: [],
  183 |           totalElements: 0,
  184 |           totalPages: 0,
  185 |           size: 100,
  186 |           number: 0,
  187 |         },
  188 |       }),
  189 |     })
  190 |   })
  191 | 
> 192 |   await page.goto('/projects/add')
      |              ^ Error: page.goto: net::ERR_CONNECTION_REFUSED at http://127.0.0.1:3334/projects/add
  193 | 
  194 |   await page.locator('input[placeholder="请输入项目名称"]').fill(projectName)
  195 |   await page.locator('input[placeholder="请输入项目编号"]').fill(projectCode)
  196 |   await page.locator('textarea[placeholder="请输入项目描述"]').fill('Playwright可视化调试流程验证')
  197 | 
  198 |   await page.locator('input[placeholder="选择开始日期"]').fill('2026-04-24')
  199 |   await page.locator('input[placeholder="选择结束日期"]').fill('2026-06-30')
  200 |   await page.locator('input[placeholder="请输入预算金额"]').fill('100000')
  201 | 
  202 |   await page.locator('input[placeholder="请选择所需技能"]').click()
  203 |   await page.getByText('Playwright', { exact: true }).click()
  204 | 
  205 |   await page.locator('input[placeholder="请选择项目负责人"]').click()
  206 |   await page.getByText('管理员A', { exact: true }).click()
  207 | 
  208 |   await page.getByRole('button', { name: '提交' }).click()
  209 | 
  210 |   await expect(page).toHaveURL(new RegExp(`/projects/${createdProjectId}$`))
  211 |   await expect(page.getByRole('heading', { name: '项目详情' })).toBeVisible()
  212 |   await expect(page.getByText(projectName)).toBeVisible()
  213 | 
  214 |   await page.screenshot({
  215 |     path: 'playwright-artifacts/project-create-submit-detail.png',
  216 |     fullPage: true,
  217 |   })
  218 | })
  219 | 
```