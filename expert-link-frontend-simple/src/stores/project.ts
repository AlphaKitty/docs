import { defineStore } from "pinia"
import { useExpertStore } from "./expert"

export type ProjectStatus = "in_progress" | "completed" | "cancelled" | "pending" | "delayed"

export interface Project {
  id: number
  name: string
  description: string
  status: ProjectStatus
  startDate: string
  endDate: string
  manager: string
  budget: number
  progress: number
  requiredSkills: string[]
  code?: string
  type?: string
  priority?: string
}

export interface ProjectFormPayload {
  name: string
  code: string
  description: string
  type: string
  priority: string
  startDate: string
  endDate: string
  budget: number
  status: ProjectStatus
  requiredSkills: string[]
  manager: number
}

const initialProjects: Project[] = [
  {
    id: 1,
    name: "AI智能客服系统",
    description: "基于深度学习的智能客服系统开发",
    status: "in_progress",
    startDate: "2024-01-15",
    endDate: "2024-06-30",
    manager: "张明",
    budget: 500000,
    progress: 65,
    requiredSkills: ["机器学习", "Python", "自然语言处理"]
  },
  {
    id: 2,
    name: "大数据分析平台",
    description: "企业级大数据分析与可视化平台",
    status: "completed",
    startDate: "2023-09-01",
    endDate: "2024-02-28",
    manager: "李华",
    budget: 800000,
    progress: 100,
    requiredSkills: ["数据分析", "Hadoop", "Spark", "数据可视化"]
  },
  {
    id: 3,
    name: "移动端应用开发",
    description: "跨平台移动应用开发项目",
    status: "pending",
    startDate: "2024-03-01",
    endDate: "2024-08-31",
    manager: "王强",
    budget: 300000,
    progress: 0,
    requiredSkills: ["React Native", "TypeScript", "移动端开发"]
  },
  {
    id: 4,
    name: "区块链金融系统",
    description: "基于区块链的金融交易系统",
    status: "in_progress",
    startDate: "2024-02-01",
    endDate: "2024-07-31",
    manager: "赵敏",
    budget: 1000000,
    progress: 45,
    requiredSkills: ["区块链", "智能合约", "金融科技"]
  }
]

export const useProjectStore = defineStore("project", {
  state: () => ({
    projects: [...initialProjects] as Project[]
  }),

  actions: {
    addProject(form: ProjectFormPayload) {
      const expertStore = useExpertStore()
      const managerExpert = expertStore.experts.find((e) => e.id === form.manager)
      const managerName = managerExpert?.name ?? "未指定"
      const nextId = this.projects.reduce((m, p) => Math.max(m, p.id), 0) + 1
      const row: Project = {
        id: nextId,
        name: form.name,
        description: form.description,
        status: form.status,
        startDate: typeof form.startDate === "string" ? form.startDate : "",
        endDate: typeof form.endDate === "string" ? form.endDate : "",
        manager: managerName,
        budget: form.budget,
        progress: form.status === "completed" ? 100 : 0,
        requiredSkills: [...form.requiredSkills],
        code: form.code,
        type: form.type,
        priority: form.priority
      }
      this.projects.push(row)
    },

    deleteProject(id: number) {
      this.projects = this.projects.filter((p) => p.id !== id)
    }
  },

  getters: {
    projectCount: (state) => state.projects.length
  }
})
