export type SkillCategory = string;

export interface SkillTag {
  id: number;
  name: string;
}

export interface RelatedSkillSummary {
  id: number;
  name: string;
  category?: string;
}

export interface SkillDocumentation {
  content?: string;
  url?: string;
}

/**
 * 技能基本信息
 */
export interface SkillBase {
  id: number;
  name: string;
  description: string;
  category: SkillCategory;
  icon?: string;
  iconUrl?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 技能详情
 */
export interface SkillDetail extends SkillBase {
  expertCount: number;
  projectCount: number;
  averageProficiency?: number;
  demandLevel?: string;
  isActive?: boolean;
  displayOrder?: number;
  domain?: {
    id: number;
    name: string;
    description?: string;
  };
  tags?: SkillTag[];
  relatedSkills?: RelatedSkillSummary[];
  documentation?: SkillDocumentation | null;
}

/**
 * 创建技能请求
 */
export interface CreateSkillRequest {
  name: string;
  description: string;
  category: SkillCategory;
  domainId: number;
  demandLevel?: string;
  enabled?: boolean;
  tags?: string[];
  relatedSkillIds?: number[];
  documentation?: SkillDocumentation;
}

/**
 * 更新技能请求
 */
export interface UpdateSkillRequest extends Partial<CreateSkillRequest> {
  id: number;
}

/**
 * 技能查询参数
 */
export interface SkillQueryParams {
  name?: string;
  category?: SkillCategory;
  tags?: string[];
  page?: number;
  size?: number;
  sort?: string;
  order?: 'asc' | 'desc';
}

/**
 * 技能树节点
 */
export interface SkillTreeNode {
  id: number;
  name: string;
  category: SkillCategory;
  children: SkillTreeNode[];
  expertCount: number;
  projectCount: number;
}