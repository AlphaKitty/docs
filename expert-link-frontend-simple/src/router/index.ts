import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue')
    },
    {
      path: '/experts',
      name: 'Experts',
      component: () => import('@/views/experts/ExpertList.vue')
    },
    {
      path: '/experts/add',
      name: 'ExpertAdd',
      component: () => import('@/views/experts/AddExpert.vue')
    },
    {
      path: '/experts/:id',
      name: 'ExpertDetail',
      component: () => import('@/views/experts/ExpertDetail.vue')
    },
    {
      path: '/projects',
      name: 'Projects',
      component: () => import('@/views/projects/ProjectList.vue')
    },
    {
      path: '/projects/add',
      name: 'ProjectAdd',
      component: () => import('@/views/projects/AddProject.vue')
    },
    {
      path: '/projects/:id',
      name: 'ProjectDetail',
      component: () => import('@/views/projects/ProjectDetail.vue')
    },
    {
      path: '/skills',
      name: 'Skills',
      component: () => import('@/views/skills/SkillList.vue')
    },
    {
      path: '/skills/add',
      name: 'SkillAdd',
      component: () => import('@/views/skills/AddSkill.vue')
    },
    {
      path: '/skills/:id',
      name: 'SkillDetail',
      component: () => import('@/views/skills/SkillDetail.vue')
    },
    {
      path: '/analysis',
      name: 'Analysis',
      component: () => import('@/views/analysis/AnalysisDashboard.vue')
    },
    {
      path: '/settings',
      name: 'Settings',
      component: () => import('@/views/settings/SystemSettings.vue')
    }
  ]
})

export default router