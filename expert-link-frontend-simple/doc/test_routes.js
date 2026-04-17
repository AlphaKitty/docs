// 路由配置测试
const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: 'Dashboard',
    meta: { title: '仪表板' }
  },
  {
    path: '/experts',
    name: 'ExpertList',
    component: 'ExpertList',
    meta: { title: '专家列表' }
  },
  {
    path: '/projects',
    name: 'ProjectList',
    component: 'ProjectList',
    meta: { title: '项目列表' }
  },
  {
    path: '/skills',
    name: 'SkillList',
    component: 'SkillList',
    meta: { title: '技能列表' }
  },
  {
    path: '/skills/add',
    name: 'SkillAdd',
    component: 'SkillAdd',
    meta: { title: '添加技能' }
  },
  {
    path: '/skills/:id',
    name: 'SkillDetail',
    component: 'SkillDetail',
    meta: { title: '技能详情' }
  },
  {
    path: '/analysis',
    name: 'AnalysisDashboard',
    component: 'AnalysisDashboard',
    meta: { title: '数据分析' }
  },
  {
    path: '/settings',
    name: 'SystemSettings',
    component: 'SystemSettings',
    meta: { title: '系统设置' }
  },
  {
    path: '/experts/:id',
    name: 'ExpertDetail',
    component: 'ExpertDetail',
    meta: { title: '专家详情' }
  },
  {
    path: '/projects/:id',
    name: 'ProjectDetail',
    component: 'ProjectDetail',
    meta: { title: '项目详情' }
  },
  {
    path: '/experts/add',
    name: 'AddExpert',
    component: 'AddExpert',
    meta: { title: '添加专家' }
  },
  {
    path: '/projects/add',
    name: 'AddProject',
    component: 'AddProject',
    meta: { title: '添加项目' }
  }
]

console.log('=== Expert Link 前端路由配置测试 ===')
console.log(`总路由数: ${routes.length}`)
console.log('\n技能管理相关路由:')
routes.filter(route => route.path.includes('skills')).forEach(route => {
  console.log(`  ${route.path.padEnd(15)} -> ${route.name.padEnd(15)} (${route.meta.title})`)
})

console.log('\n专家管理相关路由:')
routes.filter(route => route.path.includes('experts')).forEach(route => {
  console.log(`  ${route.path.padEnd(15)} -> ${route.name.padEnd(15)} (${route.meta.title})`)
})

console.log('\n项目管理相关路由:')
routes.filter(route => route.path.includes('projects')).forEach(route => {
  console.log(`  ${route.path.padEnd(15)} -> ${route.name.padEnd(15)} (${route.meta.title})`)
})

console.log('\n=== 路由测试完成 ===')
console.log('\n预期的导航流程:')
console.log('1. 用户访问 /skills (技能列表)')
console.log('2. 点击"查看"按钮 -> 导航到 /skills/1 (技能详情)')
console.log('3. 点击"添加技能"按钮 -> 导航到 /skills/add (添加技能)')
console.log('4. 从详情页返回 -> 导航回 /skills (技能列表)')