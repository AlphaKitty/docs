/**
 * 专家状态
 */
export enum ExpertStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING = 'PENDING',
  ARCHIVED = 'ARCHIVED',
}

/**
 * 专家等级
 */
export enum ExpertLevel {
  JUNIOR = 'JUNIOR',
  MIDDLE = 'MIDDLE',
  SENIOR = 'SENIOR',
  EXPERT = 'EXPERT',
}

/**
 * 专家基本信息
 */
export interface ExpertBase {
  id: number;
  name: string;
  title: string;
  avatar?: string;
  status: ExpertStatus;
  level: ExpertLevel;
  rating: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 专家详情
 */
export interface ExpertDetail extends ExpertBase {
  email: string;
  phone?: string;
  wechat?: string;
  company?: string;
  position?: string;
  introduction: string;
  skills: ExpertSkill[];
  domains: ExpertDomain[];
  projects: ExpertProject[];
  experienceYears: number;
  hourlyRate: number;
  availability: boolean;
  tags: string[];
  ownerId?: number;
  designationId?: number;
}

/**
 * 专家技能关联
 */
export interface ExpertSkill {
  id: number;
  skillId: number;
  skillName: string;
  proficiency: number; // 1-5
  yearsOfExperience: number;
}

/**
 * 专家领域关联
 */
export interface ExpertDomain {
  id: number;
  domainId: number;
  domainName: string;
  expertiseLevel: number; // 1-5
}

/**
 * 专家项目关联
 */
export interface ExpertProject {
  id: number;
  projectId: number;
  projectName: string;
  role: string;
  startDate: string;
  endDate?: string;
}

/** 可关联为专家主体的系统用户（尚未有专家档案） */
export interface UserPickerItem {
  id: number
  username: string
  fullName: string
  email: string
}

/**
 * 创建专家请求
 */
export interface CreateExpertRequest {
  ownerId: number;
  /** 称谓/岗位（专家库中维护） */
  designationId: number;
  primaryDomainId?: number;
  /** 头像 URL，需与后端字段长度限制匹配（当前库字段较短，勿传超大 base64） */
  avatar?: string;
  name: string;
  title: string;
  email: string;
  phone?: string;
  wechat?: string;
  company?: string;
  position?: string;
  introduction: string;
  experienceYears: number;
  hourlyRate: number;
  status: ExpertStatus;
  level: ExpertLevel;
  availability: boolean;
  tags: string[];
  skillIds: number[];
  domainIds: number[];
}

/**
 * 更新专家请求
 */
export interface UpdateExpertRequest extends Partial<CreateExpertRequest> {
  id: number;
}

/**
 * 专家查询参数
 */
export interface ExpertQueryParams {
  name?: string;
  status?: ExpertStatus;
  level?: ExpertLevel;
  domainId?: number;
  skillId?: number;
  minRating?: number;
  maxHourlyRate?: number;
  available?: boolean;
  tags?: string[];
  page?: number;
  size?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}