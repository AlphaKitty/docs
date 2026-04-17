/**
 * 项目状态
 */
export enum ProjectStatus {
  PLANNING = 'PLANNING',
  IN_PROGRESS = 'IN_PROGRESS',
  ON_HOLD = 'ON_HOLD',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

/**
 * 项目优先级
 */
export enum ProjectPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

/**
 * 项目基本信息
 */
export interface ProjectBase {
  id: number;
  name: string;
  code: string;
  description: string;
  status: ProjectStatus;
  priority: ProjectPriority;
  startDate: string;
  endDate?: string;
  budget: number;
  spent: number;
  progress: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 项目详情
 */
export interface ProjectDetail extends ProjectBase {
  clientName: string;
  clientContact?: string;
  managerId: number;
  managerName: string;
  domainId: number;
  domainName: string;
  requiredSkills: ProjectSkill[];
  assignedExperts: ProjectExpert[];
  milestones: ProjectMilestone[];
  documents: ProjectDocument[];
  notes: string;
}

/**
 * 项目技能需求
 */
export interface ProjectSkill {
  id: number;
  skillId: number;
  skillName: string;
  requiredLevel: number; // 1-5
  requiredCount: number;
  assignedCount: number;
}

/**
 * 项目专家分配
 */
export interface ProjectExpert {
  id: number;
  expertId: number;
  expertName: string;
  expertTitle: string;
  role: string;
  startDate: string;
  endDate?: string;
  hoursAllocated: number;
  hoursWorked: number;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}

/**
 * 项目里程碑
 */
export interface ProjectMilestone {
  id: number;
  name: string;
  description: string;
  dueDate: string;
  completedDate?: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'DELAYED';
  progress: number;
}

/**
 * 项目文档
 */
export interface ProjectDocument {
  id: number;
  name: string;
  type: string;
  size: number;
  url: string;
  uploadedBy: string;
  uploadedAt: string;
}

/**
 * 创建项目请求
 */
export interface CreateProjectRequest {
  name: string;
  code: string;
  description: string;
  clientName: string;
  clientContact?: string;
  managerId: number;
  domainId: number;
  status: ProjectStatus;
  priority: ProjectPriority;
  startDate: string;
  endDate?: string;
  budget: number;
  notes?: string;
  requiredSkillIds: number[];
  milestoneNames?: string[];
}

/**
 * 更新项目请求
 */
export interface UpdateProjectRequest extends Partial<CreateProjectRequest> {
  id: number;
}

/**
 * 项目查询参数
 */
export interface ProjectQueryParams {
  name?: string;
  code?: string;
  status?: ProjectStatus;
  priority?: ProjectPriority;
  clientName?: string;
  managerId?: number;
  domainId?: number;
  startDateFrom?: string;
  startDateTo?: string;
  endDateFrom?: string;
  endDateTo?: string;
  minBudget?: number;
  maxBudget?: number;
  page?: number;
  size?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

/**
 * 分配专家到项目请求
 */
export interface AssignExpertRequest {
  projectId: number;
  expertId: number;
  role: string;
  startDate: string;
  endDate?: string;
  hoursAllocated: number;
}

/**
 * 更新专家分配请求
 */
export interface UpdateExpertAssignmentRequest {
  assignmentId: number;
  role?: string;
  endDate?: string;
  hoursAllocated?: number;
  status?: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}