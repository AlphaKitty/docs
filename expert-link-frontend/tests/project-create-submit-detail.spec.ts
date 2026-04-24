import { expect, test } from '@playwright/test'

test('创建项目 -> 提交 -> 查看详情', async ({ page }) => {
  const createdProjectId = 98765
  const projectName = `Playwright项目-${Date.now()}`
  const projectCode = `PW-${Date.now()}`

  await page.addInitScript(() => {
    window.localStorage.setItem('auth_token', 'pw-token')
  })

  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: {
          userId: 1,
          username: 'admin',
          email: 'admin@example.com',
          fullName: '管理员',
          roles: ['SUPER_ADMIN'],
          pointsBalance: 100,
        },
      }),
    })
  })

  await page.route('**/api/settings/role-groups', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: {
          steward: ['DOMAIN_STEWARD', 'SUPER_ADMIN'],
          expert: ['EXPERT_USER', 'SUPER_ADMIN'],
          superAdmin: ['SUPER_ADMIN'],
        },
      }),
    })
  })

  await page.route('**/api/settings/menu-permissions', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: {
          menuKeys: ['projects', 'dashboard', 'settings'],
          roleMenus: {
            SUPER_ADMIN: ['projects', 'dashboard', 'settings'],
          },
        },
      }),
    })
  })

  await page.route('**/api/skills**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: {
          content: [{ id: 11, name: 'Playwright' }],
          totalElements: 1,
          totalPages: 1,
          size: 200,
          number: 0,
        },
      }),
    })
  })

  await page.route('**/api/users**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: {
          content: [{ id: 101, username: 'admin', fullName: '管理员A', email: 'admin@example.com' }],
          totalElements: 1,
          totalPages: 1,
          size: 200,
          number: 0,
        },
      }),
    })
  })

  await page.route('**/api/projects**', async (route) => {
    const method = route.request().method()
    const url = route.request().url()

    if (method === 'POST' && /\/api\/projects(\?.*)?$/.test(url)) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          message: 'ok',
          data: {
            id: createdProjectId,
            name: projectName,
            code: projectCode,
            description: 'Playwright可视化调试流程验证',
            status: 'IN_PROGRESS',
            startDate: '2026-04-24',
            endDate: '2026-06-30',
            budget: 100000,
            progress: 10,
            managerName: '管理员A',
            requiredSkills: [{ id: 11, skillName: 'Playwright', requiredCount: 1 }],
            domainName: '技术咨询',
          },
        }),
      })
      return
    }

    if (method === 'GET' && url.includes(`/api/projects/${createdProjectId}`)) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          message: 'ok',
          data: {
            id: createdProjectId,
            name: projectName,
            code: projectCode,
            description: 'Playwright可视化调试流程验证',
            clientName: '测试客户',
            domainName: '技术咨询',
            budget: 100000,
            spent: 5000,
            startDate: '2026-04-24',
            endDate: '2026-06-30',
            managerName: '管理员A',
            status: 'IN_PROGRESS',
            progress: 10,
            requiredSkills: [{ id: 11, skillName: 'Playwright', requiredCount: 1 }],
            documents: [],
          },
        }),
      })
      return
    }

    await route.continue()
  })

  await page.route(`**/api/project-experts/project/${createdProjectId}`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: [],
      }),
    })
  })

  await page.route('**/api/experts**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        message: 'ok',
        data: {
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: 100,
          number: 0,
        },
      }),
    })
  })

  await page.goto('/projects/add')

  await page.locator('input[placeholder="请输入项目名称"]').fill(projectName)
  await page.locator('input[placeholder="请输入项目编号"]').fill(projectCode)
  await page.locator('textarea[placeholder="请输入项目描述"]').fill('Playwright可视化调试流程验证')

  await page.locator('input[placeholder="选择开始日期"]').fill('2026-04-24')
  await page.locator('input[placeholder="选择结束日期"]').fill('2026-06-30')
  await page.locator('input[placeholder="请输入预算金额"]').fill('100000')

  await page.locator('input[placeholder="请选择所需技能"]').click()
  await page.getByText('Playwright', { exact: true }).click()

  await page.locator('input[placeholder="请选择项目负责人"]').click()
  await page.getByText('管理员A', { exact: true }).click()

  await page.getByRole('button', { name: '提交' }).click()

  await expect(page).toHaveURL(new RegExp(`/projects/${createdProjectId}$`))
  await expect(page.getByRole('heading', { name: '项目详情' })).toBeVisible()
  await expect(page.getByText(projectName)).toBeVisible()

  await page.screenshot({
    path: 'playwright-artifacts/project-create-submit-detail.png',
    fullPage: true,
  })
})
