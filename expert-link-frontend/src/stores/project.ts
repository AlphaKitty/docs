import { defineStore } from "pinia"
import { ProjectService } from "@/api/services/project.service"
import { ProjectPriority as ApiProjectPriority, ProjectStatus as ApiProjectStatus } from "@/api/types"
import type { CreateProjectRequest, ProjectDetail, UpdateProjectRequest } from "@/api/types"
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

const mapApiStatusToStore = (status?: string): ProjectStatus => {
  switch (status) {
    case ApiProjectStatus.COMPLETED:
      return "completed"
    case ApiProjectStatus.CANCELLED:
      return "cancelled"
    case ApiProjectStatus.PLANNING:
      return "pending"
    case ApiProjectStatus.ON_HOLD:
      return "delayed"
    default:
      return "in_progress"
  }
}

const mapStoreStatusToApi = (status: ProjectStatus): ApiProjectStatus => {
  switch (status) {
    case "completed":
      return ApiProjectStatus.COMPLETED
    case "cancelled":
      return ApiProjectStatus.CANCELLED
    case "pending":
      return ApiProjectStatus.PLANNING
    case "delayed":
      return ApiProjectStatus.ON_HOLD
    default:
      return ApiProjectStatus.IN_PROGRESS
  }
}

const mapStorePriorityToApi = (priority: string): ApiProjectPriority => {
  switch (priority) {
    case "high":
      return ApiProjectPriority.HIGH
    case "low":
      return ApiProjectPriority.LOW
    default:
      return ApiProjectPriority.MEDIUM
  }
}

const toNumber = (value: unknown, fallback = 0): number => {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value
  }

  if (typeof value === "string" && value.trim() !== "") {
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : fallback
  }

  return fallback
}

const adaptProjectFromApi = (project: ProjectDetail & Record<string, any>): Project => ({
  id: toNumber(project.id),
  name: project.name || "未命名项目",
  description: project.description || "",
  status: mapApiStatusToStore(project.status),
  startDate: project.startDate || "",
  endDate: project.endDate || "",
  manager: project.managerName || project.projectManager || "未指定",
  budget: toNumber(project.budget),
  progress: toNumber(project.progress ?? project.completionPercentage),
  requiredSkills: Array.isArray(project.requiredSkills)
    ? project.requiredSkills.map((skill: any) => skill.skillName || skill.name).filter(Boolean)
    : typeof project.keyDeliverables === "string"
      ? project.keyDeliverables.split(/[,，]/).map((skill: string) => skill.trim()).filter(Boolean)
      : [],
  code: project.code || project.projectCode,
  priority: project.priority,
  type: project.domainName || project.domain?.name || project.projectType || ""
})

const adaptProjectToCreateRequest = (form: ProjectFormPayload): CreateProjectRequest => ({
  createdById: form.manager,
  name: form.name,
  code: form.code,
  description: form.description,
  clientName: "默认客户",
  managerId: form.manager,
  domainId: 1,
  status: mapStoreStatusToApi(form.status),
  priority: mapStorePriorityToApi(form.priority),
  startDate: form.startDate,
  endDate: form.endDate,
  budget: form.budget,
  notes: form.type,
  requiredSkillIds: []
})

const adaptProjectToUpdateRequest = (id: number, form: ProjectFormPayload): UpdateProjectRequest => ({
  id,
  ...adaptProjectToCreateRequest(form)
})

export const useProjectStore = defineStore("project", {
  state: () => ({
    projects: [] as Project[],
    loading: false
  }),

  actions: {
    async fetchProjects() {
      this.loading = true
      try {
        const response = await ProjectService.getProjects({ page: 0, size: 100 })
        this.projects = response.content.map(adaptProjectFromApi)
      } finally {
        this.loading = false
      }
    },

    async addProject(form: ProjectFormPayload) {
      const createdProject = await ProjectService.createProject(adaptProjectToCreateRequest(form))
      const row = adaptProjectFromApi(createdProject)
      this.projects.unshift(row)
      return row
    },

    async updateProject(id: number, form: ProjectFormPayload) {
      const updatedProject = await ProjectService.updateProject(id, adaptProjectToUpdateRequest(id, form))
      const row = adaptProjectFromApi(updatedProject)
      const index = this.projects.findIndex((project) => project.id === id)
      if (index !== -1) {
        this.projects[index] = row
      }
      return row
    },

    async deleteProject(id: number) {
      await ProjectService.deleteProject(id)
      this.projects = this.projects.filter((p) => p.id !== id)
    }
  },

  getters: {
    projectCount: (state) => state.projects.length
  }
})
