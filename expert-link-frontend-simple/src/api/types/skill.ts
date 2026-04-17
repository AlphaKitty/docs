/**
 * 技能分类
 */
export enum SkillCategory {
  TECHNICAL = 'TECHNICAL',
  BUSINESS = 'BUSINESS',
  DESIGN = 'DESIGN',
  MANAGEMENT = 'MANAGEMENT',
  LANGUAGE = 'LANGUAGE',
  OTHER = 'OTHER',
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
  createdAt: string;
  updatedAt: string;
}

/**
 * 技能详情
 */
export interface SkillDetail extends SkillBase {
  parentId?: number;
  parentName?: string;
  children: SkillBase[];
  expertCount: number;
  projectCount: number;
  averageProficiency: number;
  tags: string[];
}

/**
 * 创建技能请求
 */
export interface CreateSkillRequest {
  name: string;
  description: string;
  category: SkillCategory;
  parentId?: number;
  icon?: string;
  tags?: string[];
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
  parentId?: number;
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